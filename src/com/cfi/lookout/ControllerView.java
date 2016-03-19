package com.cfi.lookout;
//http://kml4earth.appspot.com/icons.html
//http://mapicons.nicolasmollet.com/category/markers/restaurants-bars/?style=dark

import java.net.URLEncoder;

import org.apache.http.client.methods.HttpPost;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.cfi.lookout.xmlresultclasses.GetLocation;
import com.cfi.lookout.xmlresultclasses.OrientationValues;
import com.cfi.lookout.xmlresultclasses.Places;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.gson.Gson;

public class ControllerView extends Activity
{
	private 			Context 					mContext 		= null;
	public static 		TiltDetector 				mTiltDetector 	= null;
	private 			CameraSurface 				mPreview 		= null;
	private 			RenderPlaces 				mDraw 			= null;
	public	 			static boolean				mActive			= false;
	public static 		boolean						mHorizontal		= true;
    private 			boolean						mIsInitialized 	= false;
    public static		boolean						mMapNotAvailable= false;
	public static 		OrientationValues 			mTiltValues 	= new OrientationValues(); 
    public static 		GetLocation 				lc 				= new GetLocation();
    public 		 		OrganizeLensView 			orgLensView 	= null;
    public static 		RowInfo []					sRowInfo 	= null;
    public static		Handler						MainActivityHandler = null;

    public static Places data = null;
    public static final int MESSAGE_LOCATION_DETAILS_AVAILABLE = 101;
    public static final int MESSAGE_LOCATION_DETAILS_FAILED = 102;
    public static final int MESSAGE_LOCATION_DETAILS_FETCH = 103;
    public static final int MESSAGE_LOCATION_DETAILS_LOAD_CAM = 104;
    public static final int MESSAGE_CHECK_ORIENTATION = 105;
    public static final int MESSAGE_LOCATION_TIMER_FAILED = 106;

    public static boolean mBackKeyPressed = false;
    public static boolean PlacesDetailsFetched = false;
    public static boolean mMapThreadActive = false;
    
    private static SeekBar mSeekBar = null;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
	    super.onCreate(savedInstanceState);
	    mContext = this;

	    mIsInitialized = false;
	    mMapNotAvailable = false;

	    orgLensView 	= new OrganizeLensView();

	    // Start TiltDetector
	    mTiltDetector = new TiltDetector(this);
	    mPreview = new CameraSurface(this);
	    
	    mSeekBar = new SeekBar(mContext);
	    mSeekBar.setVisibility(View.GONE);
	    
	    mDraw = new RenderPlaces(this, mTiltDetector, mHandler, mSeekBar);

	    // Start Camera Preview
	    setContentView(mPreview);
	  	addContentView(mDraw, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
	  	addContentView(mSeekBar, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
	  	mDraw.startRefreshLogicThread();
  		startChangeViewLogicThread();
	}

    public void onBackPressed()
    {
    	mActive = false;

    	if( mDraw != null )
    		mDraw.stopRefreshLogicThread();

        mPreview = null;
        if( mTiltDetector != null )
        	mTiltDetector.Clear();
        mTiltDetector = null;
        mDraw = null;

    	finish();
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
        if( Constants.ENABLE_LOGGING )
        	Log.i("State", "onResume - Global State " + MainActivity.sGlobalState);
        if( mBackKeyPressed == true )
        {
        	mBackKeyPressed = false;
        	mActive = false;
        	finish();
        }
        else if( MainActivity.sGlobalState >= MainActivity.cCameraView )
        {
        	if( MainActivity.sDetailsView != true )
        	{
		        int orientation = getResources().getConfiguration().orientation;

		        if( Constants.ENABLE_LOGGING )
		        	Log.i("Orientation", " " + orientation);

		        if( orientation == Configuration.ORIENTATION_PORTRAIT )
		        {
		    		startActivity(new Intent(mContext, Splash.class));
		    		MainActivity.sGlobalState = MainActivity.cListView;
		        }
        	}
        }
    }

    @Override
    protected void onDestroy()
    {
    	mActive = false;
    	if( mTiltDetector != null )
    	{
    		mTiltDetector.Clear();
    		mTiltDetector = null;
    	}
        super.onDestroy();
    }

    private Handler mHandler = new Handler()
    {
    	@Override
        public void handleMessage(Message msg)
        {
        	try
        	{
  	        	if( msg.what == MESSAGE_LOCATION_DETAILS_AVAILABLE )
  	        	{
        			MainActivity.sGlobalState = MainActivity.cPlacesFetched;
		        	MainActivity.sGlobalState = MainActivity.cCameraView;
		        	mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_CHECK_ORIENTATION));
  	        	}
  	        	else if( msg.what == MESSAGE_LOCATION_DETAILS_FAILED )
  	        	{
  	        		Toast.makeText(mContext, "Failed to get location info! Try again later!", Toast.LENGTH_LONG).show();
  	        		finish();
  	        	}
  	        	else if( msg.what == MESSAGE_LOCATION_DETAILS_FETCH )
  	        	{
  	        		if( PlacesCategoriesAndData.AreDetailsAlreadyFetched(MainActivity.mCurrentView) == true )
  	        		{
  	        			lc = PlacesCategoriesAndData.GetDetails(MainActivity.mCurrentView);
  	  	        		sRowInfo = orgLensView.organize(lc, (Activity) mContext);
  	        			dispatchMessage(obtainMessage(MESSAGE_LOCATION_DETAILS_AVAILABLE));
  	        			return;
  	        		}

	  				// Get Places details here
	    			new Thread(new Runnable()
					{
					        public void run()
					        {
								// Toast message don't work here
				        		String deviceID = null;
				        		String safeUrl = null;
				        		String locationTag = null;
				        		boolean textSearch = false;

				        		try 
				        		{
				        			switch(MainActivity.mCurrentView)
				        			{
				        				default:
				        				case MainActivity.VIEW_NEARBY_LOOS:
				        					locationTag = "cafe|restaurant|shopping_mall|grocery_or_supermarket|gas_station";
				        					break;
				        				case MainActivity.REVIEW_A_LOO:
				        					locationTag = "food|bar|bakery|cafe|restaurant|night_club|meal_delivery|meal_takeaway";
				        					break;
				        				case MainActivity.VIEW_SEARCH_PLACES:
				        					textSearch = true;
				        					break;
				        			}
				        		}
				        		catch(Exception e){}

				        		int distanceInMetres = 500;
				        		int max_repeatition = 1;

				        		SharedPreferences myPref = PreferenceManager.getDefaultSharedPreferences(mContext);
				        		if( myPref != null )
				        		{
					    			distanceInMetres = Integer.parseInt(myPref.getString("otherdistance", Constants.MAX_DISTANCE));
					    			max_repeatition = Integer.parseInt(myPref.getString("othermaxnumplaces", Constants.MAX_NUM_PLACES))/20;
				        		}

				        		String pagetoken = "";
				        		try
				        		{
					        		for(int repeatation = 0; repeatation < max_repeatition; repeatation++ )
					        		{
					        			if( repeatation == 0 )
					        			{
					        				if( textSearch )
					        				{
					        				}
					        				else
					        				{
							        			safeUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + MainActivity.mLat 
				        								+ "," + MainActivity.mLon + "&radius=" + distanceInMetres + "&types=" + URLEncoder.encode(locationTag, "UTF-8")
				        								+ "&sensor=false&key=";
					        				}
					        			}
					        			else
					        			{
					        				if( textSearch )
					        				{
					        				}
					        				else
					        				{
						        				safeUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + MainActivity.mLat 
				        								+ "," + MainActivity.mLon + "&radius="  + distanceInMetres 
				        								+ "&sensor=false&pagetoken=" + pagetoken
			        								+ "&key=";
					        				}
					        			}
	
					        			if( Constants.ENABLE_LOGGING )
					        				Log.i("SafeURL", safeUrl);
					        			HttpPost httppost = new HttpPost(safeUrl);
						        		deviceID = utils.GetUniqueID(httppost, mContext);
						        		
						        		if( Constants.ENABLE_LOGGING )
						        			Log.i("Response", deviceID);

						        		JSON_Parse placesData = new JSON_Parse();
						        		placesData.parseString(deviceID);

						  	      		lc = PlacesCategoriesAndData.AddData(MainActivity.mCurrentView, data, mContext);
						  	      		orgLensView.Clear();
						  	      		sRowInfo = orgLensView.organize(lc, (Activity) mContext);

						  	      		if( repeatation == 0 )
						  	      		{
				    			  			((Activity)mContext).runOnUiThread(new Runnable()
				    			  			{
				    	                         @Override
				    	                         public void run()
				    	                         {
				    	                        	 mHandler.dispatchMessage(mHandler.obtainMessage(MESSAGE_LOCATION_DETAILS_AVAILABLE));
				    	                         }
				    			  			});
						  	      		}
						  	      		Log.i("Size", "Length received is " + data.results.length);
						  	      		if( data.results.length >= 20 )
						  	      		{
						  	      			pagetoken = data.next_page_token;
				    			  			Thread.sleep(1200);
						  	      		}
						  	      		else
						  	      			break;
					        		}
				        		}
				        		catch(Exception e)
				        		{
				        			Log.e("Exception inside handler", "e is " + e.getMessage());
				        		}

				        		if( lc.count == 0)	
			  	        		{
			  	        			((Activity)mContext).runOnUiThread(new Runnable()
		    			  			{
		    	                         @Override
		    	                         public void run()
		    	                         {
		    	                        	 mHandler.dispatchMessage(mHandler.obtainMessage(MESSAGE_LOCATION_DETAILS_FAILED));
		    	                         }
		    			  			});
			  	        		}
					        }
					}).start();
  	        	}
  	        	else if( msg.what == MESSAGE_CHECK_ORIENTATION )
  	        	{
            		int cur_orientation = getResources().getConfiguration().orientation;
            		if( Constants.ENABLE_LOGGING )
        	        	Log.i("Orientation - check from CheckOrientation", " " + cur_orientation);

        	        if( cur_orientation == Configuration.ORIENTATION_PORTRAIT )
        	        {
        	    		startActivity(new Intent(mContext, Splash.class));
        	    		MainActivity.sGlobalState = MainActivity.cListView;
        	        }  	        		
  	        	}
  	        	else if( msg.what ==  MESSAGE_LOCATION_TIMER_FAILED )
  	        	{
  	        		if( MainActivityHandler != null )
  	        		{
  	        			MainActivityHandler.dispatchMessage(MainActivityHandler.obtainMessage(MainActivity.MESSAGE_LOCATION_TIMER_FAILED));
  	        		}
  	        		finish();
  	        	}
        	}
        	catch(Exception e)
        	{
        		Log.e("Exception inside handler", "msg.what " + msg.what + ", e is " + e.getMessage());
        	    
        	}
        }
    };

    public void startChangeViewLogicThread()
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
                        // This is your target delta. 25ms = 40fps
                        Thread.sleep(100);
                        if( !mActive ) return;
                    } 
                    catch (InterruptedException e1)
                    {
                        e1.printStackTrace();
                    }

                    if( mBackKeyPressed == true )
                    {
                    	mBackKeyPressed = false;
                    	mActive = false;
                    	finish();
                    	return;
                    }

                    if( MainActivity.sDetailsView != true )
                    {
	                    int orientation = getResources().getConfiguration().orientation;
	                    if( Constants.ENABLE_LOGGING )
	                    	Log.i("Orientation", "Inside Check orientation loop. Orientation is " + orientation);
	
	                    if( orientation ==  Configuration.ORIENTATION_LANDSCAPE )
	                    {
	                    	mTiltDetector.GetLatestValues(mTiltValues);
	                    	if( MainActivity.sGlobalState == MainActivity.cListView )
		                    {
			                    if( !(mTiltValues.Roll < 40 && mTiltValues.Roll > -40) )
			                    {
			                    	MainActivity.sGlobalState = MainActivity.cCameraView;
			                    	mHorizontal = false;
			                    	mIsInitialized = true;
			                    }
		                    }
		                    if( MainActivity.sGlobalState == MainActivity.cCameraView )
		                    {
		                    	if( Constants.ENABLE_LOGGING )
		                    		Log.i("Tilt", "Az " + mTiltValues.Azimuth + " Pi " + mTiltValues.Pitch + " Ro " + mTiltValues.Roll);
			                    if( mTiltValues.Roll < 40 && mTiltValues.Roll > -40 && (mIsInitialized == false || mHorizontal == false) )
			                    {
			                    	mHorizontal = true;
			                    	mIsInitialized = true;
			                    }
			                    else if( !(mTiltValues.Roll < 40 && mTiltValues.Roll > -40) )
			                    {
			                    	MainActivity.sGlobalState = MainActivity.cCameraView;
			                    	mHorizontal = false;
			                    	mIsInitialized = true;
			                    }
		                    }
	                    }
	                    else
	                    {
	                    	if( MainActivity.sGlobalState == MainActivity.cCameraView )
	                    	{
	                    		// Start the list view activity
	                    		//startActivity(new Intent(mContext, Splash.class));
	                    		//MainActivity.sGlobalState = MainActivity.cListView;
	                    	}
	                    	else if( MainActivity.sGlobalState == MainActivity.cListView )
	                    	{
	                    	}
	                    }
                    }
                }
            }
        }.start();
    }

    public class JSON_Parse 
    {
        public void parseString(String json1)
        {
            data = new Gson().fromJson(json1, Places.class);
        }
    }
}
