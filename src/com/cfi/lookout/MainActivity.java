package com.cfi.lookout;


import java.io.File;

import com.cfi.lookout.R;
import com.cfi.lookout.issues.GetIssuesRequest;
import com.cfi.lookout.issues.GetIssuesResponse;
import com.cfi.lookout.loos.GetLoosRequest;
import com.cfi.lookout.loos.GetLoosResponse;
import com.cfi.lookout.request.RequestExecCallback;
import com.cfi.lookout.request.RequestExecTask;
import com.google.android.gms.location.LocationRequest;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public final class MainActivity extends Activity
{
    //View
    public static final int VIEW_MAIN				= 0;
    public static final int VIEW_NEARBY_LOOS		= 1;
    public static final int REVIEW_A_LOO 			= 2;
    public static final int VIEW_SEARCH_PLACES		= 9;
    
    public static int mCurrentView = VIEW_MAIN;
    public static boolean sDetailsView	= false;

    // State of the app
    public static final int cFetchingLocation		= 1;
    public static final int cLocationFetched 		= 2;
    public static final int cFetchingPlaces 		= 3;
    public static final int cPlacesFetched	 		= 4;
    public static final int cCameraView		 		= 5;
    public static final int cListView		 		= 7;
    
    public static int sGlobalState = cFetchingLocation;
    
    public static final int tFood					= 1;
    public static final int tShopping				= 2;
    public static final int tTaxi					= 3;
    public static final int tBus					= 4;
    public static final int tTrain					= 5;
    public static final int tBank					= 6;
    public static final int tFuel					= 7;
    public static final int tDoctor					= 8;
    public static final int tFire					= 9;
    public static final int tPolice					= 10;
    public static final int tLandmarks				= 11;
    public static final int tTemple					= 12;
    public static final int tMosque					= 13;
    public static final int tChurch					= 14;
    public static final int tReligious				= 15;
    public static final int tCloseby				= 16;
    public static final int tLoo					= 17;

    public static double 	mTempLat = 0;
    public static double 	mTempLon = 0;
    public static float 	mTempAcc = 0;
    
    public static boolean 	mIsAdmin = true;
    

    // Mobile Location
    //Sydney//New york//42nd Cross
    public static double mLat = 28.617333;//28.6143647;//-33.857767;//40.759852;//12.91894;
    public static double mLon = 77.194960;//77.1988056;//151.214993;//-73.984561;//77.576684;
    public static final int MESSAGE_LOCATION_DETAILS_AVAILABLE = 101;
    public static final int MESSAGE_LOCATION_TIMER_FAILED	= 102;

    private PowerManager.WakeLock wl;
    private Settings mSettings = new Settings();
    
    public static Bitmap rating_bitmap[] = new Bitmap[11];
    public static Bitmap pause_lens_bitmap = null;
    public static Bitmap play_lens_bitmap = null;
    public static Bitmap share_place_bitmap = null;
    public static Bitmap direction_place_bitmap = null;
    public static Bitmap more_place_bitmap = null;

    public static boolean sAppAlive	= false;
    private int	mAutoRotateFlag = 1;
    private Activity mActivity = null;
    
    private LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        sDetailsView = false;
        sAppAlive = true;
        mActivity = this;
        sGlobalState = cFetchingLocation;

        mSettings.Initialize(this);
        
        //SMSHandler.ParseSMSCommand("Sent From your Twilio trial account - Task Assigned Id:10005, Loo:3, address:Saraswati Vihar, Issue: No Water", this);

        // Wake up lock
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "MainActivityForLookOut");
        wl.acquire();
        PlacesCategoriesAndData.Initialize();

        rating_bitmap[0] = BitmapFactory.decodeResource(getResources(), R.drawable.ratings_05);
        rating_bitmap[1] = BitmapFactory.decodeResource(getResources(), R.drawable.ratings_1);
        rating_bitmap[2] = BitmapFactory.decodeResource(getResources(), R.drawable.ratings_15);
        rating_bitmap[3] = BitmapFactory.decodeResource(getResources(), R.drawable.ratings_2);
        rating_bitmap[4] = BitmapFactory.decodeResource(getResources(), R.drawable.ratings_25);
        rating_bitmap[5] = BitmapFactory.decodeResource(getResources(), R.drawable.ratings_3);
        rating_bitmap[6] = BitmapFactory.decodeResource(getResources(), R.drawable.ratings_35);
        rating_bitmap[7] = BitmapFactory.decodeResource(getResources(), R.drawable.ratings_4);
        rating_bitmap[8] = BitmapFactory.decodeResource(getResources(), R.drawable.ratings_45);
        rating_bitmap[9] = BitmapFactory.decodeResource(getResources(), R.drawable.ratings_5);
        rating_bitmap[10] = BitmapFactory.decodeResource(getResources(), R.drawable.ratings_0);

        pause_lens_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pause_lens_view);
        play_lens_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.play_lens_view);
        share_place_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.share);
        direction_place_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.direction_icon);
        more_place_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.more);
        
        CheckAndToggleScreenRotation();
		SetImageView();

//		Intent intent = new Intent(this, PlacesDetails.class);
//		intent.putExtra("reference", "CnRkAAAAl3Oo48-0zNXXCiPSCbHffJOc8_Ha2Z_ofVgOYXtUt_iAeS_3e0ImfYJgIGabjQNHyFvA1ucnmC3nLrG42Dr7u5OP24jbrPBEewseEzIwteO7ho_Wgox_F-hksoLSTYkEOVUtMe3eS4kxecgd9odQJxIQXu4yMuVIIrkrzXycaK4r5RoU8vObSquU_6cG3Qxpcjy-F-iokTA");
//		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//		this.startActivity(intent);
		
		mTempLat = mLat;
    	mTempLon = mLon;
    	mTempAcc = 20;

    	if( mTempAcc <= 100 )
    		mHandler.dispatchMessage(mHandler.obtainMessage(MainActivity.MESSAGE_LOCATION_DETAILS_AVAILABLE));
    	
    	if( mIsAdmin )
    	{
    		TextView reviews = (TextView) findViewById(R.id.feedback_review);
    		reviews.setText("Reported Issues");
    	}
    }
    
    private void getIssues()
    {
        GetIssuesRequest request = new GetIssuesRequest(this);
        RequestExecTask<GetIssuesRequest, GetIssuesResponse> execTask = new RequestExecTask<GetIssuesRequest, GetIssuesResponse>(
                new RequestExecCallback<GetIssuesResponse>() {
                    @Override
                    public void onPostRequestExec(GetIssuesResponse result) {
                        if(result != null && result.isSuccess())
                        {
                            Log.e("Loo-kOut", result.getHttpStatusCode() + ": " + result.getHttpStatusText());
                            //Toast.makeText(MainActivity.this, result.getHttpStatusCode() + ": " + result.getHttpStatusText(), Toast.LENGTH_LONG).show();
                        }
                    }
                }
        );
        execTask.execute(request);
    }
    
    private void getLoos()
    {
        GetLoosRequest request = new GetLoosRequest(this);
        RequestExecTask<GetLoosRequest, GetLoosResponse> execTask = new RequestExecTask<GetLoosRequest, GetLoosResponse>(
                new RequestExecCallback<GetLoosResponse>() {
                    @Override
                    public void onPostRequestExec(GetLoosResponse result) {
                        if(result != null && result.isSuccess())
                        {
                            Log.e("lookout", result.getHttpStatusCode() + ": " + result.getHttpStatusText());
                        }
                        else {
                            Log.e("lookout", result.getHttpStatusCode() + ": " + result.getHttpStatusText());
                        }

                    }
                }
        );
        execTask.execute(request);
    }
    
	@Override
	public void onStart()
	{
		super.onStart();
	}
	
	@Override
	public void onStop()
	{
		super.onStop();
	}

    private void CheckAndToggleScreenRotation()
    {
    	try
    	{
    		mAutoRotateFlag = android.provider.Settings.System.getInt(getContentResolver(),
								android.provider.Settings.System.ACCELEROMETER_ROTATION);

    		if( mAutoRotateFlag == 0 )
    			android.provider.Settings.System.putInt(getContentResolver(),android.provider.Settings.System.ACCELEROMETER_ROTATION, 1);
    	}
    	catch(Exception e){}
    }
    
    private void ToggleBackScreenRotation()
    {
    	if( mAutoRotateFlag == 0 )
			android.provider.Settings.System.putInt(getContentResolver(),android.provider.Settings.System.ACCELEROMETER_ROTATION, 0);
    }

    private void SetImageView()
    {
    	sGlobalState = cFetchingLocation;
    	//LocationInfo.GetLocation(this, mHandler);
    	//sGlobalState = cLocationFetched;
    	setContentView(R.layout.main_images);
    }

	public void imageClick(View v)
	{
		if (v.getTag().equals("closeby_loo"))
		{
			mCurrentView = VIEW_NEARBY_LOOS;
		}
		else if (v.getTag().equals("review_a_loo"))
		{
			startActivity(new Intent(this, ReportAnIssue.class));
			return;
		}
		else if( v.getTag().equals("settings"))
		{
			return;
		}
		else if( v.getTag().equals("search"))
		{
			return;
		}

		if( utils.IsLocationInfoAvailable(this) )
		{
			ControllerView.MainActivityHandler = mHandler;
			startActivity(new Intent(this, ControllerView.class));
		}
		else
		{
			new AlertDialog.Builder(this)
	    	.setTitle("No way to fetch location!")
	    	.setMessage("There is no way to fetch location info of your device. Request you to enable reading location info via GPS or netowrk.")
	        .setPositiveButton("Go to settings", new DialogInterface.OnClickListener(){
	            public void onClick(DialogInterface dialog, int which)
	            {
	            	dialog.dismiss();
	            	Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
	                startActivity(intent);
	            }})
	        .setNeutralButton("Cancel",new DialogInterface.OnClickListener(){
	            public void onClick(DialogInterface dialog, int which)
	            {
	            	dialog.dismiss();
	            } })
	        .setCancelable(false)
	        .show();
		}
	}

    private Handler mHandler = new Handler()
    {
    	@Override
        public void handleMessage(Message msg)
        {
        	if( msg.what == MESSAGE_LOCATION_DETAILS_AVAILABLE )
        	{
        		if( sGlobalState <= cLocationFetched )
        		{
	        		mLat = mTempLat;
	        		mLon = mTempLon;
	        		sGlobalState = cLocationFetched;
	        		
	        		// TODO: set miles/km unit here
        		}
        		//Toast.makeText(mActivity, "Location info received", Toast.LENGTH_LONG).show();
        	}
        	else if( msg.what == MESSAGE_LOCATION_TIMER_FAILED )
        	{
        		new AlertDialog.Builder(mActivity)
    	    	.setTitle("Couldn't locate the device!")
    	    	.setMessage("Failed to locate the device. Try again later!")
    	        .setPositiveButton("OK", new DialogInterface.OnClickListener(){
    	            public void onClick(DialogInterface dialog, int which)
    	            {
    	            	finish();
    	            }})
    	        .setCancelable(false)
    	        .show();        		
        	}
        }
    };

    @Override
    protected void onResume()
    {
        super.onResume();
        if( sGlobalState >= cLocationFetched )
        	sGlobalState = cLocationFetched;
        else
        	SetImageView();
    }

    @Override
    protected void onDestroy()
    {
    	sAppAlive = false;
    	wl.release();
    	for(int i = 0; i < 11 ; i++ )
    	{
    		rating_bitmap[i] = null;
    	}

    	PlacesCategoriesAndData.CleanUp();
    	trimCache(this);
    	ToggleBackScreenRotation();
    	super.onDestroy();
    }

    public static void trimCache(Context context)
    {
        try
        {
           File dir = context.getCacheDir();
           if (dir != null && dir.isDirectory())
              deleteDir(dir);
        }
        catch (Exception e) {}
    }

    public static boolean deleteDir(File dir)
    {
        if (dir != null && dir.isDirectory())
        {
           String[] children = dir.list();
           for (int i = 0; i < children.length; i++)
           {
              boolean success = deleteDir(new File(dir, children[i]));
              if (!success)
                 return false;
           }
        }
        // The directory is now empty so delete it
        return dir.delete();
    }
}
