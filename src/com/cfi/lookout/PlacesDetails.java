package com.cfi.lookout;

import java.io.File;
import java.io.FileOutputStream;

import org.apache.http.client.methods.HttpPost;

import com.cfi.lookout.R;
import com.cfi.lookout.xmlresultclasses.PlaceDetails;
import com.cfi.lookout.xmlresultclasses.PlaceDetails.Result.Reviews;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Point;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class PlacesDetails extends android.support.v4.app.FragmentActivity
{
	private boolean 		mActive 		= false;
	private String 			mReference 		= null;
	private double			mDistance		= 0;
	private Context 		mContext 		= null;
	private PlaceDetails 	mPlaceDetails 	= null;
	private Activity 		mActivity		= null;
	private float			mAngle			= 0;
	private String			mPhotoReference = null;
	private Bitmap			mBitmap 		= null;
	private String			mName			= null;
	private double			mLatitude		= 0;
	private double			mLongitude		= 0;
	private boolean 		mDisplayInMiles = false;
	private boolean			mInitialized	= false;
	private float			mRating 		= 3;
	private String			mAddress		= "";

    public static final int MESSAGE_PLACE_DETAILS_AVAILABLE = 201;
    public static final int MESSAGE_PLACE_DETAILS_FAILED = 202;
    public static final int MESSAGE_PLACE_DETAILS_FETCH = 203;
    public static final int MESSAGE_IMAGE_DETAILS_FETCH = 204;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        MainActivity.sDetailsView = true;
        
        mContext = this;
        mActivity = this;
        setContentView(R.layout.placesdetails);
        utils.AddAdView(this);
        
        String ref = this.getIntent().getExtras().getString("reference");
        if(ref != null)
        	mReference = this.getIntent().getExtras().getString("reference");
        
        mDistance = this.getIntent().getExtras().getFloat("distance");
        
        mAngle = this.getIntent().getExtras().getFloat("angle");
        
        mLatitude = this.getIntent().getExtras().getDouble("latitude");
        mLongitude = this.getIntent().getExtras().getDouble("longitude");
        mName = this.getIntent().getExtras().getString("name");
        mRating = this.getIntent().getExtras().getFloat("rating");
        mAddress = this.getIntent().getExtras().getString("address");
        
        mInitialized = false;
        int type = this.getIntent().getExtras().getInt("type");
        if( type == MainActivity.tLoo )
        {
        	//Launch another activity
        	Intent myIntent = new Intent(this, LooDetails.class);
            myIntent.putExtra("reference", mReference);
            myIntent.putExtra("distance", mDistance);
            myIntent.putExtra("angle", mAngle);
            myIntent.putExtra("latitude", mLatitude);
            myIntent.putExtra("longitude", mLongitude);
            myIntent.putExtra("name", mName);
            myIntent.putExtra("type", type);
            myIntent.putExtra("address", mAddress);
            myIntent.putExtra("rating", mRating);
            startActivity(myIntent);
        	finish();
        }
    }

    @Override
    protected void onPause()
    {
    	super.onPause();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        
        if( mInitialized == false )
        {
	        if( ControllerView.mMapNotAvailable == false )
	        {
		        GoogleMap mMap;
		        SupportMapFragment mMapFragment;
		        
		        mMapFragment = ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map));
		        
		        if( mMapFragment == null )
		        	ControllerView.mMapNotAvailable = true;
		        
		        if( mMapFragment != null )
		        {
			        mMap = mMapFragment.getMap();
			        
			        if(mMap != null )
			        {
				    	MarkerOptions marker = new MarkerOptions();
						marker.position(new LatLng(mLatitude, mLongitude));
						marker.title(mName);
						if( mMap != null )
							mMap.addMarker(marker);
	
				        LatLng latLng = new LatLng(mLatitude, mLongitude);
						CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 14);
	
						if( mMap != null )
							mMap.animateCamera(cameraUpdate, 1, null);
			        }
			        else
			        	ControllerView.mMapNotAvailable = true;
		        }
	        }
	
	        if( ControllerView.mMapNotAvailable == true )
	        {
	        	findViewById(R.id.map_fragment_layout).setVisibility(View.GONE);
	        }
	        
	        SharedPreferences myPref = PreferenceManager.getDefaultSharedPreferences(this);
	        int miles = Integer.parseInt(myPref.getString("distanceunit", "0"));
	        if( miles == 1 )
	        	mDisplayInMiles = true;
	
		    mHandler.dispatchMessage(mHandler.obtainMessage(MESSAGE_PLACE_DETAILS_FETCH));
		    
		    mInitialized = true;
        }
    }

    @Override
    protected void onDestroy()
    {
    	MainActivity.sDetailsView = false;
    	mActive = false;
        super.onDestroy();
    }
    
    @Override
    public void onBackPressed()
    {
    	mActive = false;
    	MainActivity.sDetailsView = false;
    	finish();
    }
    
    private Handler mHandler = new Handler()
    {
    	@Override
        public void handleMessage(Message msg)
        {
        	try
        	{
  	        	if( msg.what == MESSAGE_PLACE_DETAILS_AVAILABLE )
  	        	{
  	        		PopulateUIViews();
  	        		mActive = true;
  	        		startRefreshLogicThread();
  	        		if( mPlaceDetails.result.photos != null && mPlaceDetails.result.photos.length > 0 
  	        			&& mPlaceDetails.result.photos[0] != null && mPlaceDetails.result.photos[0].photo_reference != null )
  	        		{
  	        			mPhotoReference = mPlaceDetails.result.photos[0].photo_reference;
  	        			mHandler.dispatchMessage(mHandler.obtainMessage(MESSAGE_IMAGE_DETAILS_FETCH));
  	        		}
  	        	}
  	        	else if( msg.what == MESSAGE_PLACE_DETAILS_FAILED )
  	        	{
  	        		Toast.makeText(mContext, "Failed to get place info! Try again later!", Toast.LENGTH_LONG).show();
  	        		finish();
  	        	}
  	        	else if( msg.what == MESSAGE_PLACE_DETAILS_FETCH )
  	        	{
	  				// Get Places details here
	    			new Thread(new Runnable()
					{
					        public void run()
					        {
								// Toast message don't work here
				        		String response = null;
				        		String safeUrl = null;

				        		try
				        		{
			        				safeUrl = "https://maps.googleapis.com/maps/api/place/details/json?reference=" + mReference
	        								+ "&sensor=false&key=AIzaSyCljUfAeaCF--PdZ-qYsgEB7Y2E";

				        			//Log.i("SafeURL", safeUrl);
				        			HttpPost httppost = new HttpPost(safeUrl);
				        			response = utils.GetUniqueID(httppost, mContext);
					        		
					        		//Log.i("Response", response);

					        		JSON_ParsePlaceDetails placeDetails = new JSON_ParsePlaceDetails();
					        		placeDetails.parseString(response);

		    			  			((Activity)mContext).runOnUiThread(new Runnable()
		    			  			{
		    	                         @Override
		    	                         public void run()
		    	                         {
		    	                        	 mHandler.dispatchMessage(mHandler.obtainMessage(MESSAGE_PLACE_DETAILS_AVAILABLE));
		    	                         }
		    			  			});
					  	      		//Log.i("Size", "Length received is " + mPlaceDetails.result.length);
				        		}
				        		catch(Exception e){
				        			Log.e("Exception", "while gathering Places Details. Message " + e.getMessage());
				        		}

//			  	        		{
//			  	        			((Activity)mContext).runOnUiThread(new Runnable()
//		    			  			{
//		    	                         @Override
//		    	                         public void run()
//		    	                         {
//		    	                        	 mHandler.dispatchMessage(mHandler.obtainMessage(MESSAGE_PLACE_DETAILS_FAILED));
//		    	                         }
//		    			  			});
//			  	        		}
					        }
					}).start();
  	        	}
  	        	else if( msg.what == MESSAGE_IMAGE_DETAILS_FETCH )
  	        	{
	  				// Get Places details here
	    			new Thread(new Runnable()
					{
					        public void run()
					        {
								// Toast message don't work here
				        		String safeUrl = null;

				        		try
				        		{
			        				safeUrl = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=" + mPhotoReference
	        								+ "&sensor=false&key=AIzaSyCljUfAeaCF--PdZ-qYsgEB7Y2E";

//				        			Log.i("SafeURL", safeUrl);
//				        			HttpPost httppost = new HttpPost(safeUrl);
//				        			response = utils.GetUniqueID(httppost, mContext);

				        			mBitmap = utils.loadBitmap(safeUrl);

					        		//Log.i("Response", response);

				        			if( mBitmap != null )
				        			{
			    			  			((Activity)mContext).runOnUiThread(new Runnable()
			    			  			{
			    	                         @Override
			    	                         public void run()
			    	                         {
				    	                        	 ImageView im = (ImageView) mActivity.findViewById(R.id.details_places_pic);
				    	                        	 im.setImageBitmap(mBitmap);
				    	                           
				    	                           int width = 200;
				    	                           int height = 150;
				    	                           int image_width = width/2;
				    	                           if(android.os.Build.VERSION.SDK_INT >= 13)
				    	                           {
				    	                        	   Display display = getWindowManager().getDefaultDisplay();
				    	                        	   Point size = new Point();
				    	                        	   display.getSize(size);
				    	                        	   width = size.x;
				    	                        	   height = size.y;
				    	                           }
				    	                           else
				    	                           {
				    	                        	   Display display = getWindowManager().getDefaultDisplay(); 
				    	                        	   width = display.getWidth();
				    	                        	   height = display.getHeight();
				    	                           }
				    	                           
				    	                           if( width > height )
				    	                        	   image_width = width/3;
				    	                           else
				    	                        	   image_width = width/2;
		
				    	                           ImageView mImageView = (ImageView) findViewById(R.id.details_places_pic);
				    	                           LayoutParams lp = new LinearLayout.LayoutParams(image_width,utils.convertDipToPixels(150, mContext));
				    	                           mImageView.setLayoutParams(lp);
				    	                           mImageView.setScaleType(ScaleType.FIT_XY);
			    	                         }
			    			  			});
				        			}
				        		}
				        		catch(Exception e)
				        		{
				        			Log.e("Exception", "while gathering Places Details. Message " + e.getMessage());
				        		}
					        }
					}).start();
  	        		
  	        	}
        	}
        	catch(Exception e)
        	{
        		Log.e("Exception inside handler", "msg.what " + msg.what + ", e is " + e.getMessage());
        	}
        }
    };

    private void PopulateUIViews()
    {
    	if( mPlaceDetails.result.name != null )
    	{
	  		TextView name = (TextView)  findViewById(R.id.details_main_name);
	  		name.setText(mPlaceDetails.result.name);
    	}
    	else
    		return;
  		
    	if( mPlaceDetails.result.formatted_address != null )
    	{
	  		TextView address = (TextView)  findViewById(R.id.details_place_address);
	  		address.setText(mPlaceDetails.result.formatted_address);
    	}

        if( mPlaceDetails.result.rating >= 0.1 )
        {
        	ImageView rating_image = (ImageView)  findViewById(R.id.details_rating);
        	int indexForRatingBitmap = 0;
        	if( mPlaceDetails.result.rating < 0.75 )
        		indexForRatingBitmap = 0;
        	else if( mPlaceDetails.result.rating < 1.25 )
        		indexForRatingBitmap = 1;
        	else if( mPlaceDetails.result.rating < 1.75 )
        		indexForRatingBitmap = 2;
        	else if( mPlaceDetails.result.rating < 2.25 )
        		indexForRatingBitmap = 3;
        	else if( mPlaceDetails.result.rating < 2.75 )
        		indexForRatingBitmap = 4;
        	else if( mPlaceDetails.result.rating < 3.25 )
        		indexForRatingBitmap = 5;
        	else if( mPlaceDetails.result.rating < 3.75 )
        		indexForRatingBitmap = 6;
        	else if( mPlaceDetails.result.rating < 4.25 )
        		indexForRatingBitmap = 7;
        	else if( mPlaceDetails.result.rating < 4.75 )
        		indexForRatingBitmap = 8;
        	else 
        		indexForRatingBitmap = 9;
        	rating_image.setImageBitmap(MainActivity.rating_bitmap[indexForRatingBitmap]);
        }

       	if( mPlaceDetails.result.formatted_phone_number != null )
       	{
       		TextView phone_number = (TextView)  findViewById(R.id.details_phone_number);
       		phone_number.setText(mPlaceDetails.result.formatted_phone_number);
       	}
       	else
       	{
       		TextView phone_number = (TextView)  findViewById(R.id.details_phone_label);
       		phone_number.setVisibility(View.GONE);
       	}

        if( mPlaceDetails.result.url != null )
        {
        	TextView url = (TextView)  findViewById(R.id.details_website_string);
        	url.setText(mPlaceDetails.result.url+"\n");
        }
        else
        {
        	TextView url = (TextView)  findViewById(R.id.details_url_label);
        	url.setVisibility(View.GONE);
        }

        if( mDistance > 0 )
        {
        	TextView distance = (TextView)  findViewById(R.id.details_distance_string);
        	String dist_str;
			if( mDisplayInMiles )
			{
				float fdist = (float) (mDistance * 0.621371f);
				dist_str = String.format("%.2f", fdist) + " miles";
			}
			else
			{
				if( mDistance < 1 )
				{
					int dist = (int)(mDistance*1000);
					dist_str = dist + " metres";
				}
				else
				{
					dist_str = String.format("%.1f", mDistance) + " KM";
				}
			}
			distance.setText(dist_str);
        }

        if( mPlaceDetails.result.types != null && mPlaceDetails.result.types.length > 0 )
        {
        	TextView types_tv = (TextView)  findViewById(R.id.details_types_string);
        	String types = "";
        	for( int i = 0; i < mPlaceDetails.result.types.length; i++ )
        	{
        		types = types + mPlaceDetails.result.types[i];
        		if( i != mPlaceDetails.result.types.length - 1)
        			types = types + ", ";
        	}
        	types_tv.setText(types);
        }
        else
        {
        	TextView types_tv = (TextView)  findViewById(R.id.details_types_label);
        	types_tv.setVisibility(View.GONE);
        }

        // Reviews
        LinearLayout review_layout = (LinearLayout) findViewById(R.id.details_review_view);
        
        if( mPlaceDetails.result.reviews != null && mPlaceDetails.result.reviews.length > 0 )
        {
        	for(int i = 0; i < mPlaceDetails.result.reviews.length; i++ )
        	{
        		InsertReview(review_layout, mPlaceDetails.result.reviews[i]);
        	}
        }
        else
        {
        	review_layout.setVisibility(View.GONE);
        }
    }
    
    private void InsertReview(LinearLayout view, Reviews review)
    {
        TextView name = new TextView(mContext);
        name.setText(review.author_name);
        name.setTextSize(15);
        name.setTextColor(0xFF000000);
        name.setTypeface(Typeface.DEFAULT_BOLD);
        name.setPadding(7, 0, 0, 0);
        // make it bold
        view.addView(name);
        
        TextView date = new TextView(mContext);
        if( review.time > 0 )
        	date.setText("" + utils.GetDate(review.time * 1000, "dd/MMM/yyyy hh:mm a"));
        else
        	date.setText("Unknown");
        date.setTextSize(13);
        date.setTextColor(0xFF777777);
        date.setPadding(7, 0, 0, 0);
        // make it bold
        view.addView(date);
        
        if( review.aspects != null && review.aspects.length > 0 )
        {
        	for( int j = 0; j < review.aspects.length; j++ )
        	{
		        LinearLayout ll = new LinearLayout(mContext);
	
		        TextView category = new TextView(mContext);
		        category.setText(review.aspects[j].type + ": ");
		        category.setTextSize(14);
		        category.setTextColor(0xFF000000);
		        category.setPadding(7, 0, 0, 0);
		        // make it bold
		        ll.addView(category);
		        
		        ImageView image = new ImageView(mContext);
	        	int indexForRatingBitmap = 0;
	        	
	        	if( review.aspects[j].rating <= 0.1 )
	        		indexForRatingBitmap = 10;
	        	else if( review.aspects[j].rating < 0.75 )
	        		indexForRatingBitmap = 0;
	        	else if( review.aspects[j].rating < 1.25 )
	        		indexForRatingBitmap = 1;
	        	else if( review.aspects[j].rating < 1.75 )
	        		indexForRatingBitmap = 2;
	        	else if( review.aspects[j].rating < 2.25 )
	        		indexForRatingBitmap = 3;
	        	else if( review.aspects[j].rating < 2.75 )
	        		indexForRatingBitmap = 4;
	        	else if( review.aspects[j].rating < 3.25 )
	        		indexForRatingBitmap = 5;
	        	else if( review.aspects[j].rating < 3.75 )
	        		indexForRatingBitmap = 6;
	        	else if( review.aspects[j].rating < 4.25 )
	        		indexForRatingBitmap = 7;
	        	else if( review.aspects[j].rating < 4.75 )
	        		indexForRatingBitmap = 8;
	        	else 
	        		indexForRatingBitmap = 9;
	        	image.setImageBitmap(MainActivity.rating_bitmap[indexForRatingBitmap]);

	        	ll.addView(image);
		        view.addView(ll);
        	}
        }

        TextView review_str = new TextView(mContext);
        review_str.setText(review.text);
        review_str.setTextSize(15);
        review_str.setTextColor(0xFF000000);
        review_str.setPadding(7, 0, 7, 7);
        
        view.addView(review_str);
        
        View greyline = new View(mContext);
        
        greyline.setMinimumHeight(6);
        greyline.setBackgroundColor(0xFFAAAAAA);
        greyline.setPadding(20, 0, 20, 0);
        view.addView(greyline);
	
//	     <TextView
//	            android:id="@+id/url_label"
//			 	android:layout_width="wrap_content"
//	            android:layout_height="wrap_content"
//	            android:layout_gravity="left|top"
//	            android:gravity="left|top"
//	            android:text="Something something about the restaraunt. Something good.. and sometimes something bad."
//	            android:paddingTop="5dip"
//	            android:paddingLeft="5dip"
//	            android:textColor="#FF000000"
//	            android:textSize="12dip">
//      		</TextView>
//     	<!-- Grey line -->
//     	<View
//			android:layout_width="match_parent"
//			android:layout_height="0.5dp"
//	 		android:paddingLeft="15dip"
//	 		android:paddingRight="15dip"
//			android:background="#ffAAAAAA" />

    }

    public void startRefreshLogicThread()
    {
    	mActive = true;
        new Thread()
        {
            public void run()
            {
                // Once active is false, this loop (and thread) terminates.
                while (mActive)
                {
                    try
                    {
                        Thread.sleep(500);
                    } 
                    catch (InterruptedException e1)
                    {
                        e1.printStackTrace();
                    }

                    try
                    {
	                    ControllerView.mTiltDetector.GetLatestValues(ControllerView.mTiltValues);
	                    
			  			((Activity)mContext).runOnUiThread(new Runnable()
			  			{
	                        @Override
	                        public void run()
	                        {
								float angle = 0;
								angle = mAngle - ControllerView.mTiltValues.Azimuth + 90;
								ImageView image = (ImageView) mActivity.findViewById(R.id.location_tilt_icon);
								utils.RotateImage(image, angle);
	                        }
			  			});
                    }
                    catch(Exception e){}
                }
            }
        }.start();
    }

   public void stopRefreshLogicThread()
    {
    	mActive = false;
    }
   
   public class JSON_ParsePlaceDetails 
   {
       public void parseString(String json1)
       {
    	   mPlaceDetails = new Gson().fromJson(json1, PlaceDetails.class);
       }
   }
   
   public void ClickOnDetails(View v)
   {
		if (v.getTag().equals("pic"))
		{
			if(mBitmap != null)
			{
                //String path = context.getCacheDir().getAbsolutePath() + "/view.png"; 
                //File f = new File(context.getCacheDir().getAbsolutePath(),"MemeCache");
                //if(!f.exists())
                //    f.mkdirs();
                String path = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/streetlens_image.png"; 
                Toast.makeText(mContext, "opening in gallery", Toast.LENGTH_SHORT).show();
                File file = new File(path); 
                FileOutputStream fos = null;
                try
                {
                    fos = new FileOutputStream(file);
                    mBitmap.compress(CompressFormat.PNG, 100, fos ); 
                    fos.close(); 
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(new File(path)), "image/png");
                mActivity.startActivity(intent);
            }
		}
	}

   public void imageClick(View v)
	{
		if (v.getTag().equals("getdirections"))
		{
			Intent intent = new Intent(android.content.Intent.ACTION_VIEW, 
			Uri.parse("http://maps.google.com/maps?saddr=" + MainActivity.mLat +","+ MainActivity.mLon + "&daddr=" + mLatitude + "," + mLongitude ));
			intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
			startActivity(intent);
		}
	}
}