package com.cfi.lookout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.cfi.lookout.R;


public class Splash extends Activity implements android.view.View.OnClickListener
{
	private boolean mActive = false;

    private ListView listview;
    private ArrayList<StringList> mListItem;
    private ListAdapter mListAdapter = null;
    private boolean mDisplayInMiles = false;

    private Bitmap mEmptyBitmap = null;; 
    
    //private static float angle = 0;
    
    private PowerManager.WakeLock wl;
 
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_view);
        utils.AddAdView(this);

        mContext = this;
        listview = (ListView) findViewById(R.id.list_view);
        mListItem = getItems();
        
        SharedPreferences myPref = PreferenceManager.getDefaultSharedPreferences(this);

        int miles = Integer.parseInt(myPref.getString("distanceunit", "0"));
        if( miles == 1 )
        	mDisplayInMiles = true;

        if( mListItem.size() == 0 )
        {
        	Toast.makeText(this, "No details available!", Toast.LENGTH_LONG).show();
        }
        else
        {
        	mListAdapter = new ListAdapter(Splash.this, R.id.list_view, mListItem);
        	listview.setAdapter(mListAdapter);
        }

        mActive = true;
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "DoNjfdhotDimScreen");

        ImageView head_image = (ImageView) findViewById(R.id.list_view_icon);
        TextView head_text = (TextView) findViewById(R.id.list_view_text);

        switch(MainActivity.mCurrentView)
        {
        	case MainActivity.VIEW_NEARBY_LOOS:
        		head_image.setImageResource(R.drawable.loo);
        		head_text.setText(" Closeby");
        		break;
        }
        
        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        Bitmap mEmptyBitmap = Bitmap.createBitmap(5, 5, conf); 
    }
    
    @Override
    protected void onPause()
    {
    	super.onPause();
    	wl.release();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        wl.acquire();

        int orientation = getResources().getConfiguration().orientation;
        //Log.i("Orientation", " " + orientation);
        
        if( orientation == Configuration.ORIENTATION_LANDSCAPE )
        {
        	mActive = false;
    		finish();
        }
    }

    @Override
    protected void onDestroy()
    {
    	mActive = false;
        try {
        wl.release();
        }catch(Exception e){}
        super.onDestroy();
    }
    
    @Override
    public void onBackPressed()
    {
    	mActive = false;
    	ControllerView.mBackKeyPressed = true;
    	finish();
    }
 
    // ***ListAdapter***
    @SuppressLint("NewApi")
	private class ListAdapter extends ArrayAdapter<StringList>
    {
        private ArrayList<StringList> mList;
        private Context mContext;
 
        public ListAdapter(Context context, int textViewResourceId, ArrayList<StringList> list)
        {
            super(context, textViewResourceId, list);

            this.mList = list;
            this.mContext = context;
            startRefreshLogicThread();
        }
 
        public View getView(int position, View convertView, ViewGroup parent)
        {
            View view = convertView;
            
            //Log.i("ArraList", "getView called po:" + position);

            //try
            {
                if (view == null)
                {
                    LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    view = vi.inflate(R.layout.google_contact, null);
                }

                final StringList listItem = (StringList) mList.get(position);
                
                if (listItem != null)
                {
                	String distance;
					
					if( mDisplayInMiles )
					{
						float fdist = listItem.distance * 0.621371f;
						distance = String.format("%.2f", fdist) + " miles";
					}
					else
					{
						if( listItem.distance < 1 )
						{
							int dist = (int)(listItem.distance*1000);
							distance = dist + " metres";
						}
						else
						{
							distance = String.format("%.1f", listItem.distance) + " KM";
						}
					}

                    // setting list_item views
                    ((TextView) view.findViewById(R.id.place_distance))
                            .setText(distance);
                    ((TextView) view.findViewById(R.id.place_description))
                    	.setText(listItem.getMessage());

                    float angle = 0;
                	angle = listItem.angle - ControllerView.mTiltValues.Azimuth + 90;
                    ImageView tilt_image = (ImageView) view.findViewById(R.id.location_tilt_icon);
                    utils.RotateImage(tilt_image, angle);

			        if( listItem.rating >= 0.1 )
			        {
			        	int indexForRatingBitmap = 0;
			        	if( listItem.rating < 0.75 )
			        		indexForRatingBitmap = 0;
			        	else if( listItem.rating < 1.25 )
			        		indexForRatingBitmap = 1;
			        	else if( listItem.rating < 1.75 )
			        		indexForRatingBitmap = 2;
			        	else if( listItem.rating < 2.25 )
			        		indexForRatingBitmap = 3;
			        	else if( listItem.rating < 2.75 )
			        		indexForRatingBitmap = 4;
			        	else if( listItem.rating < 3.25 )
			        		indexForRatingBitmap = 5;
			        	else if( listItem.rating < 3.75 )
			        		indexForRatingBitmap = 6;
			        	else if( listItem.rating < 4.25 )
			        		indexForRatingBitmap = 7;
			        	else if( listItem.rating < 4.75 )
			        		indexForRatingBitmap = 8;
			        	else 
			        		indexForRatingBitmap = 9;
			        	ImageView rating_image = (ImageView) view.findViewById(R.id.rating_image);
			        	rating_image.setImageBitmap(MainActivity.rating_bitmap[indexForRatingBitmap]);
			        }
			        else
			        {
			        	ImageView rating_image = (ImageView) view.findViewById(R.id.rating_image);
			        	rating_image.setImageBitmap(mEmptyBitmap);
			        }

			        view.setOnClickListener(new android.view.View.OnClickListener()
                    {
                        public void onClick(View arg0)
                        {
                            Intent myIntent = new Intent(Splash.this, PlacesDetails.class);
                            myIntent.putExtra("reference", listItem.reference);
                            myIntent.putExtra("distance", listItem.distance);
                            myIntent.putExtra("angle", listItem.angle);
                            myIntent.putExtra("latitude", listItem.latitude);
                            myIntent.putExtra("longitude", listItem.longitude);
                            myIntent.putExtra("name", listItem.message);
                            myIntent.putExtra("type", listItem.type);
                            myIntent.putExtra("rating", listItem.rating);
                            startActivity(myIntent);
                        }
                    });
			        
			        tilt_image.setOnClickListener( new OnClickListener()
			        {
						@Override
						public void onClick(View v)
						{
							Intent intent = new Intent(android.content.Intent.ACTION_VIEW, 
							Uri.parse("http://maps.google.com/maps?saddr=" + MainActivity.mLat +","+ MainActivity.mLon + "&daddr=" + listItem.latitude + "," + listItem.longitude ));
							intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
							startActivity(intent);
						}
					});
                }
            }
//            catch (Exception e)
//            {
//                Log.e("Splash - crash", "Message is " + e.getMessage());
//            }
            return view;
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
                            // This is your target delta. 25ms = 40fps
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
		                        	 mListAdapter.notifyDataSetChanged();
		                         }
				  			});
                        }
                        catch(Exception e){}
                    }
                }
            }.start();
        }
    }

	@Override
	public void onClick(View v)
	{
		//mListAdapter.notifyDataSetChanged();
	}

    static Context mContext;
    public static ArrayList<StringList> getItems()
    {
        ArrayList<StringList> list = new ArrayList<StringList>();
		StringList item;
		int numItems = ControllerView.lc.count;
		if( numItems != 0 )
		{
			for(int i = numItems - 1; i >= 0; i--)
			{
				item = new StringList();
				item.message = ControllerView.lc.name[i];
				item.rating = ControllerView.lc.rating[i];
				item.latitude = ControllerView.lc.latitude[i];
				item.longitude = ControllerView.lc.longitude[i];
				item.distance = ControllerView.lc.distance[i];
				item.angle = ControllerView.lc.angle[i];
				item.reference = ControllerView.lc.reference[i];
				item.type = ControllerView.lc.type[i];
				list.add(item);
		  	}
		}

		Collections.sort(list, new Comparator<StringList>()
		{
	        @Override
	        public int compare(StringList s1, StringList s2)
	        {
	        	return s1.distance > s2.distance? 1 : (s1.distance == s2.distance) ? 0 : -1;
	        }
	    });
        return list;
    }

    public void stopRefreshLogicThread()
    {
    	mActive = false;
    }
}