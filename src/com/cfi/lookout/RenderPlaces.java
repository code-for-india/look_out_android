package com.cfi.lookout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.cfi.lookout.xmlresultclasses.OrientationValues;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class RenderPlaces extends View
{
	private boolean 			mActive 		= false; 
	private TiltDetector 		mTiltDetector 	= null;
	private OrientationValues 	mTiltValues 	= new OrientationValues();
	private Context 			mContext 		= null;
	private Handler 			mHandler 		= null;

	private boolean 			mDisplayInMiles = false;

	private int 				foreground 		= Color.BLACK;
	private int 				background 		= Color.WHITE;
	private Paint 				mPaint 			= new Paint();
	private	int 				mRadius 		= MainActivity.pause_lens_bitmap == null ? 0 : MainActivity.pause_lens_bitmap.getWidth()/2 + 5;
	
	private int					screen_width 	= 480;
	private int					screen_height	= 320;
	private int					item_height		= 0;
	private int					item_width		= 0;
	private long 				mPrevTime 		= 0;
	
	private float 				mPrevAzimuth	= -1000;
	private boolean				mFreeze			= false;
	private int					mSweepAngle		= 45;

	ArrayList<Float> 			coordx 			= new ArrayList<Float>();
	ArrayList<Float> 			coordy 			= new ArrayList<Float>();
	ArrayList<Float> 			index 			= new ArrayList<Float>();
	
	private int					mPreviewIndex 	= -1;

	private final double		degree2radian	= 0.017453292519;
	
	private int 				max_distance 	= 0;
	private int 				max_distance_for_circle	= 0;
	private int					max_distance_to_show = 0;
	private int					min_distance 	= 500;
	
	private boolean				mShowPreview 	= false;
	
	private int					mPreviewFontSize 	= 20;
	private int					mRectFontSize 		= 25;
	private int					mFetchFontSize 		= 50;
	
	private int					mFetchRectWidth 	= 300;
	private int					mFetchRectHeight 	= 40;
	private int					mPreviewGap		 	= 8;
	private int					mPreviewPadding	 	= 10;
	private int					mRectWidth		 	= 240;
	private int					mRectHeightPadding	= 5;
	private int					mGapBetweenRects	= 1;

	// Preview pane
	private class Preview
	{
		public String reference;
		public String name;
		public float distance;
		public float angle;
		public double latitude;
		public double longitude;
		public String address;
		public Bitmap bitmap;
		public float rating;
		public int type;
	};
	
	private Preview mCPreview = new Preview();
	TextPaint tpForPreview = new TextPaint();

	// Seek Bar for controlling distance
	private SeekBar mSeekBar = null;
	private boolean mIsSeekBarVisible = false;
	private boolean mIsSeekBarMadeVisible = false;
	private RectF	mRoundRect	= new RectF();
	private int		mMilesVal	= 0;

    public RenderPlaces(Context context, TiltDetector tiltDetector, Handler handler, SeekBar seekbar)
    {
        super(context);
        mTiltDetector = tiltDetector;
        mHandler = handler;
        mContext = context;
        mSeekBar = seekbar;

        mRectFontSize	= getResources().getDimensionPixelSize(R.dimen.lens_view_rect_font_size);
        mRectWidth 		= getResources().getDimensionPixelSize(R.dimen.lens_view_rect_width);

        mPreviewFontSize = getResources().getDimensionPixelSize(R.dimen.lens_view_preview_font_size);
        mFetchFontSize 	= getResources().getDimensionPixelSize(R.dimen.lens_view_fetch_font_size);

        mFetchRectWidth	= getResources().getDimensionPixelSize(R.dimen.lens_view_fetch_rect_width);
        mFetchRectHeight = getResources().getDimensionPixelSize(R.dimen.lens_view_fetch_rect_height);
        mPreviewGap = getResources().getDimensionPixelSize(R.dimen.lens_view_preview_gap);
        mPreviewPadding = getResources().getDimensionPixelSize(R.dimen.lens_view_preview_padding);
        mRadius = getResources().getDimensionPixelSize(R.dimen.lens_view_radius);

        mRectHeightPadding = getResources().getDimensionPixelSize(R.dimen.lens_view_rect_height_padding);
        mGapBetweenRects = getResources().getDimensionPixelSize(R.dimen.lens_view_gap_between_rects);

        SharedPreferences myPref = PreferenceManager.getDefaultSharedPreferences(mContext);

        int miles = Integer.parseInt(myPref.getString("distanceunit", "0"));
        if( miles == 1 )
        	mDisplayInMiles = true;

        String theme = myPref.getString("theme", "1");
        int theme_int = 0;
        if( theme != null )
        	theme_int = Integer.parseInt(theme);

        if( theme_int == 0 )
        {
        	foreground = Color.BLACK;
        	background = Color.WHITE;
        }
        else
        {
        	foreground = Color.WHITE;
        	background = Color.BLACK;
        }

        if(android.os.Build.VERSION.SDK_INT >= 13)
        {
     	   Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
     	   Point size = new Point();
     	   display.getSize(size);
     	   screen_width = size.x;
     	   screen_height = size.y;
        }
        else
        {
     	   Display display = ((Activity)context).getWindowManager().getDefaultDisplay(); 
     	   screen_width = display.getWidth();
     	   screen_height = display.getHeight();
        }

        mFreeze = false;
        mSweepAngle = 45;

        if( myPref != null )
			max_distance = Integer.parseInt(myPref.getString("otherdistance", Constants.MAX_DISTANCE));
		
		max_distance_for_circle = (int) (max_distance*1.1);
		min_distance = 500;

        coordx.clear();
        coordy.clear();
        index.clear();

        mIsSeekBarVisible = false;
        max_distance_to_show = max_distance;
        mSeekBar.setVisibility(View.GONE);
        mSeekBar.setMax(100);
        mSeekBar.setProgress(100);

        mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
        {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				mMilesVal = mSeekBar.getProgress();
				max_distance_to_show = min_distance + (max_distance - min_distance)*mMilesVal/100;
				max_distance_for_circle = (int) (max_distance_to_show*1.1);
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				mMilesVal = mSeekBar.getProgress();
				max_distance_to_show = min_distance + (max_distance - min_distance)*mMilesVal/100;
				max_distance_for_circle = (int) (max_distance_to_show*1.1);
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				mMilesVal = mSeekBar.getProgress();
				max_distance_to_show = min_distance + (max_distance - min_distance)*mMilesVal/100;
				max_distance_for_circle = (int) (max_distance_to_show*1.1);
			}
		});

        this.setOnTouchListener(new OnTouchListener()
        {
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				//Toast.makeText(mContext, " Count " + count + " Touch at x:" + event.getX() + ", y:" + event.getY() + ", w:" + item_width + ", h:" + item_height
				//		, Toast.LENGTH_LONG).show();

				if( MainActivity.sDetailsView == true || event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL)
					return false;

				// Check for Freeze/Unfreeze option first
				if( event.getX() < mPreviewPadding + MainActivity.pause_lens_bitmap.getWidth() && 
						( (event.getY() > screen_height - mPreviewPadding - MainActivity.pause_lens_bitmap.getHeight()) && 
								(event.getY() < screen_height)) )
				{
					Calendar c = Calendar.getInstance();
					long curTime = c.getTimeInMillis();
					
					if( curTime - mPrevTime < 500 )
						return true;

					mPrevTime = curTime;
					mFreeze = !mFreeze;
					return true;
				}

				// Check for Angle change option
				// OR display Seekbar for controlling distance
				if( event.getX() > screen_width - mPreviewPadding - mRadius*2 && 
						event.getY() < mPreviewPadding + mRadius*2 )
				{
					Calendar c = Calendar.getInstance();
					long curTime = c.getTimeInMillis();
					
					if( curTime - mPrevTime < 500 )
						return true;

					mPrevTime = curTime;

					if( !Constants.NARROW_MODE )
					{
						if( mIsSeekBarVisible )
						{
							mSeekBar.setVisibility(View.GONE);
							mIsSeekBarVisible = false;
							mIsSeekBarMadeVisible = true;
						}
						else
						{
							mShowPreview = false;
							mPreviewIndex = -1;
							mIsSeekBarVisible = true;
							DisplaySeekBar(null);
							mIsSeekBarMadeVisible = false;
							//mSeekBar.setVisibility(View.VISIBLE);
							postInvalidate();
						}
					}
					else
					{	
						if( mSweepAngle == 45 ) mSweepAngle = 10;
						else mSweepAngle = 45;
					}					
					return true;
				}

				if( mShowPreview == true )
				{
					if( event.getX() > screen_width - mRadius*4 - mPreviewPadding - mPreviewGap && 
							event.getX() < screen_width - mPreviewPadding - mRadius*2 &&
							event.getY() < mPreviewPadding + mRadius*2 )
					{
						LaunchDetailView();

						mShowPreview = false;
						mPreviewIndex = -1;
						return true;
					}
					else if( event.getX() > screen_width - mRadius*6 - mPreviewPadding - mPreviewGap*2 &&
								event.getX() < screen_width - mRadius*4 - mPreviewPadding - mPreviewGap &&
								event.getY() < mPreviewPadding + mRadius*2 )
					{
			       		Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
			       		sharingIntent.setType("text/plain");
			       		sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Check out this place - " + mCPreview.name);
			       		sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Check out this place which I found using Street Lens Android app - " + 
			       				"https://maps.google.com/maps?q=loc:" + mCPreview.latitude + "," + mCPreview.longitude +
			       				"\n\nTo download Street Lens app, visit - https://play.google.com/store/apps/details?id=com.trackyapps.street_lens");
			       		((Activity)mContext).startActivity(Intent.createChooser(sharingIntent, "Share via"));
			       		((Activity)mContext).finish();
			       		return true;
					}
					else if( event.getX() > screen_width - mRadius*8 - mPreviewPadding - mPreviewGap*3 &&
							event.getX() < screen_width - mRadius*6 - mPreviewPadding - mPreviewGap*2 &&
							event.getY() < mPreviewPadding + mRadius*2 )
					{
						Intent intent = new Intent(android.content.Intent.ACTION_VIEW, 
						Uri.parse("http://maps.google.com/maps?saddr=" + MainActivity.mLat +","+ MainActivity.mLon + "&daddr=" + mCPreview.latitude + "," + mCPreview.longitude ));
						intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
						((Activity)mContext).startActivity(intent);
						return true;
					}
					else if( event.getY() < mPreviewPadding + mRadius*2 )
					{
						LaunchDetailView();
						
						mShowPreview = false;
						mPreviewIndex = -1;
						return true;
					}
				}

				for( int j = 0; j < coordx.size(); j++ )
				{
					if( event.getX() >= coordx.get(j) && event.getX() <= coordx.get(j) + item_width &&
							event.getY() >= coordy.get(j) && event.getY() <= coordy.get(j) + item_height )
					{
						int i = index.get(j).intValue();
						mPreviewIndex = i;
						mShowPreview = true;
						mCPreview.reference = ControllerView.lc.reference[i];
						mCPreview.distance = ControllerView.lc.distance[i];
						mCPreview.angle = ControllerView.lc.angle[i];
						mCPreview.latitude = ControllerView.lc.latitude[i];
						mCPreview.longitude = ControllerView.lc.longitude[i];
						mCPreview.name = ControllerView.lc.name[i];
						mCPreview.bitmap = ControllerView.lc.bitmap[i];
						mCPreview.type = ControllerView.lc.type[i];
						mCPreview.rating = (float)ControllerView.lc.rating[i];
						mCPreview.address = null;
		    			new Thread(new Runnable()
						{
					        public void run()
					        {
					        	try
					        	{
					        		Thread.sleep(200);
									Geocoder gc = new Geocoder(mContext, Locale.getDefault());
									List<Address> addresses = gc.getFromLocation(mCPreview.latitude, mCPreview.longitude, 1);
									if( addresses != null && addresses.size() > 0 )
									{
										Address address = addresses.get(0);
										mCPreview.address = address.getAddressLine(0) + ", " + address.getLocality();
									}
								}
								catch(Exception e)
								{
									e.printStackTrace();
								}
					        }
						}).start();

		    			if( mIsSeekBarVisible == true )
		    			{
		    				mSeekBar.setVisibility(View.GONE);
		    				mIsSeekBarVisible = false;
		    			}

		    			postInvalidate();
						return true;
					}
				}
				mShowPreview = false;
				mPreviewIndex = -1;
				return false;
			}
        });

        // Timer
        new Thread(new Runnable() {
            @Override
            public void run() 
            {
            	try{
            	Thread.sleep(15000);
            	}catch(Exception e){}
            	if(MainActivity.sGlobalState <= MainActivity.cFetchingLocation)
            	{
            		((Activity)mContext).runOnUiThread(new Runnable()
		  			{
                         @Override
                         public void run()
                         {
                        	 mHandler.dispatchMessage(mHandler.obtainMessage(ControllerView.MESSAGE_LOCATION_TIMER_FAILED));
                         }
		  			});
            	}
            }
        }).start();
    }
    
    private void LaunchDetailView()
    {
		MainActivity.sDetailsView = true;
        Intent myIntent = new Intent(mContext, PlacesDetails.class);
        myIntent.putExtra("reference", mCPreview.reference);
        myIntent.putExtra("distance", mCPreview.distance);
        myIntent.putExtra("angle", mCPreview.angle);
        myIntent.putExtra("latitude", mCPreview.latitude);
        myIntent.putExtra("longitude", mCPreview.longitude);
        myIntent.putExtra("name", mCPreview.name);
        myIntent.putExtra("type", mCPreview.type);
        myIntent.putExtra("address", mCPreview.address);
        myIntent.putExtra("rating", mCPreview.rating);
        ((Activity)mContext).startActivity(myIntent);
    }

    private void DisplayStringAtCenter(Canvas canvas, String str)
    {
    	int width = getWidth();
    	int height = getHeight();

    	mPaint.setStyle(Paint.Style.FILL);
		mPaint.setColor(Color.BLACK);
		mPaint.setTextSize(mFetchFontSize);
		mFetchRectWidth = (int) mPaint.measureText(str);
		Rect r = new Rect(width/2 - mFetchRectWidth/2, height/2 + mFetchRectHeight/4, width/2 + mFetchRectWidth/2, height/2 - mFetchRectHeight/2);

		canvas.drawRect(r, mPaint);

		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeWidth(1);
		mPaint.setColor(Color.WHITE);
		mPaint.setAntiAlias(true);
		mPaint.setTextSize(mFetchFontSize);
		canvas.drawText(str, width/2 - mFetchRectWidth/2, height/2, mPaint);
    }
    
    @Override
    protected void onDraw(Canvas canvas)
    {
    	boolean atLeastOneItemDisplayed = false;
    	if( ControllerView.mActive == false || MainActivity.sDetailsView == true )
    		return;

    	// Ignore small changes to Azimuth so that the place items don't flicker
    	if( !(mPrevAzimuth != -1000 && 
    			(mPrevAzimuth <= mTiltValues.Azimuth + 2) && (mPrevAzimuth >= mTiltValues.Azimuth - 2)) && !mFreeze)
    	{
    		mPrevAzimuth = mTiltValues.Azimuth;
    	}

    	int width = getWidth();
    	int height = getHeight();

    	if( MainActivity.sGlobalState == MainActivity.cFetchingLocation )
    	{
    		DisplayStringAtCenter(canvas, "Locating Device! Please Wait...!");
    	}
    	else if( MainActivity.sGlobalState == MainActivity.cLocationFetched )
    	{
    		MainActivity.sGlobalState = MainActivity.cFetchingPlaces;
    		mHandler.dispatchMessage(mHandler.obtainMessage(ControllerView.MESSAGE_LOCATION_DETAILS_FETCH));
    		DisplayStringAtCenter(canvas, "Fetching Places! Please Wait...!");
    	}
    	else if( MainActivity.sGlobalState == MainActivity.cFetchingPlaces )
    	{
    		DisplayStringAtCenter(canvas, "Fetching Places! Please Wait...!");
    	}
    	else if( MainActivity.sGlobalState == MainActivity.cCameraView )
    	{
    		int save_count = canvas.save();
	        Paint paint = new Paint();
	        paint.setStyle(Paint.Style.FILL);
	        paint.setColor(Color.BLACK);
	        paint.setTextSize(50);
	        
	        float convertAzToAngle = mPrevAzimuth;
	        if( convertAzToAngle < -180 ) 
	        	convertAzToAngle += 360;

	        convertAzToAngle += 180;
	        float startAngle = convertAzToAngle - mSweepAngle;
	        if(startAngle < 0 )
	        	startAngle += 360;
	        int fromDistance = (int) (startAngle/360*screen_width*4);

	        float endAngle = convertAzToAngle + mSweepAngle;
	        if(endAngle > 360 )
	        	endAngle -= 360;
	        int toDistance = (int) (endAngle/360*screen_width*4);
	        RowInfo [] rowInfo = ControllerView.sRowInfo;

	        try
	        {
		        coordx.clear();
		        coordy.clear();
		        index.clear();
	
		        if( rowInfo == null )
		        	return;

		        for( int i = 0; i < rowInfo.length; i++ )
		        {
		        	if( rowInfo[i] == null || rowInfo[i].placeItem == null ) continue;
	
		        	for( int j = 0; j < rowInfo[i].placeItem.size(); j++ )
		        	{
		        		if( fromDistance < toDistance && (rowInfo[i].startY.get(j) < fromDistance || rowInfo[i].startY.get(j) > toDistance ) )
		        			continue;
		        		
		        		if( fromDistance > toDistance && (rowInfo[i].startY.get(j) > toDistance && rowInfo[i].startY.get(j) < fromDistance ) )
		        			continue;
	
		        		{
			        		float distance = rowInfo[i].startY.get(j);
			        		int rating_bitmap_width = 0;
			        		
			        		if( fromDistance < toDistance )
			        			distance -= fromDistance;
			        		else if( distance >= fromDistance )
			        			distance -= fromDistance;
			        		else
			        			distance += screen_width*4 - fromDistance;
			        		
			        		if( mSweepAngle == 10 )
			        			distance = (float) (distance*4.5);
	
			        		paint.setColor(Color.CYAN);
			        		int bitmap_height = 71;
			        		int bitmap_width = 71;
			        		int lc_index = rowInfo[i].placeItem.get(j).index;
			        		
			        		if( ControllerView.lc.distance[lc_index] > max_distance_to_show/1000 )
			        			continue;

			        		if( ControllerView.lc.bitmap[lc_index] != null )
			        		{
			        			bitmap_height = ControllerView.lc.bitmap[lc_index].getHeight();
			        			bitmap_width = ControllerView.lc.bitmap[lc_index].getWidth();
			        			paint.setStrokeWidth(0);
						        paint.setColor(Color.GREEN);
						        //canvas.drawRect(distance - 3, i*bitmap_height - 3, distance+bitmap_width + 3, i*bitmap_height + bitmap_height + 3, paint );
			        			canvas.drawBitmap(ControllerView.lc.bitmap[lc_index], distance,
			        									i*(bitmap_height + mGapBetweenRects), paint);
			        		}
			        		if( mPreviewIndex != lc_index )
			        			paint.setColor(foreground);
			        		else
			        			paint.setColor(background);
					        paint.setStrokeWidth(3);
					        canvas.drawRect(distance+bitmap_width, i*(bitmap_height + mGapBetweenRects), distance+bitmap_width + mRectWidth, i*(bitmap_height + mGapBetweenRects) + bitmap_height, paint);
					        canvas.drawRect(distance+bitmap_width, i*(bitmap_height + mGapBetweenRects), distance+bitmap_width + mRectWidth, i*(bitmap_height + mGapBetweenRects) + bitmap_height, paint);
					        paint.setStrokeWidth(0);
					        if( mPreviewIndex != lc_index )
					        	paint.setColor(background);
					        else
					        	paint.setColor(foreground);
					        canvas.drawRect(distance+bitmap_width+3, i*(bitmap_height + mGapBetweenRects), distance+bitmap_width + mRectWidth + 3, i*(bitmap_height + mGapBetweenRects) + bitmap_height, paint );
					        
					        float rateDisplayed = 0;
					        if( ControllerView.lc.rating[lc_index] >= 0.1 )
					        {
					        	int indexForRatingBitmap = 0;
					        	if( ControllerView.lc.rating[lc_index] < 0.75 )
					        		indexForRatingBitmap = 0;
					        	else if( ControllerView.lc.rating[lc_index] < 1.25 )
					        		indexForRatingBitmap = 1;
					        	else if( ControllerView.lc.rating[lc_index] < 1.75 )
					        		indexForRatingBitmap = 2;
					        	else if( ControllerView.lc.rating[lc_index] < 2.25 )
					        		indexForRatingBitmap = 3;
					        	else if( ControllerView.lc.rating[lc_index] < 2.75 )
					        		indexForRatingBitmap = 4;
					        	else if( ControllerView.lc.rating[lc_index] < 3.25 )
					        		indexForRatingBitmap = 5;
					        	else if( ControllerView.lc.rating[lc_index] < 3.75 )
					        		indexForRatingBitmap = 6;
					        	else if( ControllerView.lc.rating[lc_index] < 4.25 )
					        		indexForRatingBitmap = 7;
					        	else if( ControllerView.lc.rating[lc_index] < 4.75 )
					        		indexForRatingBitmap = 8;
					        	else 
					        		indexForRatingBitmap = 9;
					        	rateDisplayed = (indexForRatingBitmap + 1)/2; 
					        	rating_bitmap_width = MainActivity.rating_bitmap[indexForRatingBitmap].getWidth();
					        	canvas.drawBitmap(MainActivity.rating_bitmap[indexForRatingBitmap], distance+bitmap_width+3, i*(bitmap_height + mGapBetweenRects) + 0.5f * bitmap_height, paint);
					        }

					        
					        paint.setColor(Color.BLACK);
					        paint.setTextSize(mRectFontSize);

					        TextPaint tp = new TextPaint();
					        if( mPreviewIndex != lc_index )
					        	tp.setColor(foreground);
					        else
					        	tp.setColor(background);
					        tp.setTextSize(mRectFontSize);
					        tp.setFakeBoldText(true);

							String address_string = "";

							save_count = canvas.save();
							String name = ControllerView.lc.name[lc_index];
							if( name.length() > 17 )
							{
								name = name.substring(0, 15) + "...";
							}
					        
							float dist = ControllerView.lc.distance[lc_index];
							String unit = " KM";
							String distance_str = "";
							
							if( mDisplayInMiles )
							{
								dist *= 0.621371;
								unit = " mi";

								String format = "%.2f";
								if( rateDisplayed >= 4 )
								{
									unit = " mi";
									format = "%.1f";
								}
								address_string += name;
								distance_str = String.format(format, dist) + unit;
							}
							else
							{
								address_string += name;
								distance_str = String.format("%.1f", dist) + unit;
							}
	
					        StaticLayout sl = new StaticLayout(address_string, tp, mRectWidth, 
					        						Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
					        canvas.translate(distance+bitmap_width+3+3, i*(bitmap_height + mGapBetweenRects) + mRectHeightPadding);
					        sl.draw(canvas);
					        canvas.restoreToCount(save_count);
					        
					        save_count = canvas.save();
					        StaticLayout s2 = new StaticLayout(distance_str, tp, mRectWidth - rating_bitmap_width, 
	        						Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
					        canvas.translate(distance+bitmap_width+3+3 + rating_bitmap_width, i*(bitmap_height + mGapBetweenRects) + bitmap_height/2);
					        s2.draw(canvas);
					        canvas.restoreToCount(save_count);
					        
					        

					        item_height = bitmap_height;
					        item_width = bitmap_width + mRectWidth + 3;
					        // Add it to the array
					        coordx.add(distance);
					        coordy.add((float) (i*(bitmap_height + mGapBetweenRects)));
					        index.add((float) lc_index);
					        
					        atLeastOneItemDisplayed = true;
				        }
		        	}
		        }

		    	if( atLeastOneItemDisplayed == true )
		    	{
		    		if( mFreeze == false )
		    		{
		    	    	canvas.drawBitmap(	MainActivity.pause_lens_bitmap,
		    	    				mPreviewPadding,	screen_height - MainActivity.pause_lens_bitmap.getHeight() - mPreviewPadding, null);
		    		}
		    		else
		    		{
		    	    	canvas.drawBitmap(	MainActivity.play_lens_bitmap,
		    	    				mPreviewPadding,	screen_height - MainActivity.play_lens_bitmap.getHeight() - mPreviewPadding, null);
		    		}		    		
		    	}
		    	
	            try{
	            	if( mShowPreview )
	            		DrawPreviewArea(canvas, mPaint, rowInfo, width, height, save_count);
	                DrawStreetRadiusCircle(canvas, mPaint, rowInfo, mPrevAzimuth);
	                if( mIsSeekBarVisible )
	                {
	                	DisplaySeekBar(canvas);
	                }
	            }catch(Exception e){}

	        }
	        catch(Exception e)
	        {
	        	Log.e("OnDraw", "Crashed! e is " + e.getMessage());
	        }
        }

	    super.onDraw(canvas);
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
                        Thread.sleep(100);
                    } 
                    catch (InterruptedException e1)
                    {
                        e1.printStackTrace();
                    }

                    if( MainActivity.sDetailsView != true )
                    {
	                    mTiltDetector.GetLatestValues(mTiltValues);
	                    postInvalidate();
                    }
                }
            }
        }.start();
    }
    
    public void stopRefreshLogicThread()
    {
    	mActive = false;
    }

    private void DrawPreviewArea(Canvas canvas, Paint paint, RowInfo [] rowInfo, int width, int height, int save_count)
    {
    	paint.setColor(Color.WHITE);
    	//paint.setAlpha(255);
    	paint.setStyle(Paint.Style.FILL_AND_STROKE);
    	paint.setAlpha(200);
    	paint.setAntiAlias(true);

    	canvas.drawRect(mPreviewPadding, mPreviewPadding,
    						screen_width - mRadius*2 - mPreviewPadding,
    						mRadius*2 + mPreviewPadding, paint);

    	// Show category icon
    	if( mCPreview.bitmap != null )
    		canvas.drawBitmap(Bitmap.createScaledBitmap(mCPreview.bitmap, mRadius*2, mRadius*2, false), mPreviewPadding, mPreviewPadding, paint);
    	canvas.drawBitmap(Bitmap.createScaledBitmap(MainActivity.direction_place_bitmap, mRadius*2, mRadius*2, false), screen_width - mPreviewPadding - mPreviewGap*3 - mRadius*8, mPreviewPadding, paint);
    	canvas.drawBitmap(Bitmap.createScaledBitmap(MainActivity.share_place_bitmap, mRadius*2, mRadius*2, false), screen_width - mPreviewPadding - mPreviewGap*2 - mRadius*6, mPreviewPadding, paint);
    	canvas.drawBitmap(Bitmap.createScaledBitmap(MainActivity.more_place_bitmap, mRadius*2, mRadius*2, false), screen_width - mPreviewPadding - mPreviewGap - mRadius*4, mPreviewPadding, paint);
    	
    	// Show place name above and address below
    	tpForPreview.setColor(Color.BLACK);
    	tpForPreview.setTypeface(Typeface.DEFAULT_BOLD);
    	tpForPreview.setTextSize(mPreviewFontSize);
    	save_count = canvas.save();
    	StaticLayout sl = new StaticLayout(mCPreview.name, tpForPreview, screen_width - mPreviewPadding*2 - mPreviewGap*4 - mRadius*10, 
				Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
		canvas.translate(mRadius*2 + mPreviewGap + mPreviewPadding, mPreviewPadding + mPreviewGap);
		sl.draw(canvas);

		canvas.restoreToCount(save_count);
		if( mCPreview.address != null && mCPreview.address.length() > 2 )
		{
			save_count = canvas.save();
			tpForPreview.setTextSize(mPreviewFontSize - 2);
			StaticLayout s2 = new StaticLayout(mCPreview.address, tpForPreview, screen_width - mPreviewPadding*2 - mPreviewGap*4 - mRadius*10, 
					Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
			canvas.translate(mRadius*2 + mPreviewGap + mPreviewPadding, mPreviewPadding + mRadius - mPreviewGap);
			s2.draw(canvas);
			canvas.restoreToCount(save_count);
		}
    }

    private void DrawStreetRadiusCircle(Canvas canvas, Paint paint, RowInfo [] rowInfo, float azimuth)
    {	
    	int item_radius = 1;
    	int center_x = screen_width - mRadius - mPreviewPadding;
    	int center_y = mRadius + mPreviewPadding;
    	
    	// draw circle
    	paint.setColor(Color.BLACK);
    	paint.setAlpha(100);
    	paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setAntiAlias(true);
    	canvas.drawCircle(center_x, center_y, mRadius, paint);

    	// draw arc
    	paint.setColor(Color.RED);
    	paint.setAlpha(80);
    	
    	RectF rectF = new RectF(center_x - mRadius, center_y - mRadius, center_x + mRadius, center_y + mRadius);
    	canvas.drawArc(rectF, -90 - mSweepAngle, mSweepAngle*2, true, paint);
    	// draw items
    	paint.setColor(Color.WHITE);
    	paint.setAlpha(255);
    	for( int i = 0; i < rowInfo.length; i++ )
    	{
    		for( int j = 0; j < rowInfo[i].placeItem.size(); j++ )
    		{
    			int index = rowInfo[i].placeItem.get(j).index;
    			float distance = ControllerView.lc.distance[index];
    			if( distance > max_distance_to_show/1000 )
    				continue;
    			
    			float angle = ControllerView.lc.angle[index];

    			distance = distance*1000/max_distance_for_circle*mRadius;
    			if( distance > mRadius)
    				distance = mRadius;

    			angle += azimuth*-1 - 90;
    			//angle *= -1;

    	        int x = (int) (center_x + distance * Math.cos(degree2radian*angle));
    	        int y = (int) (center_y + distance * Math.sin(degree2radian*angle));
    			canvas.drawCircle(x, y, item_radius, paint);
    		}
    	}    	
    }

    void DisplaySeekBar(Canvas canvas)
    {
    	if( Constants.NARROW_MODE == false )
    	{
    		if( android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB )
    		{
				mSeekBar.setLeft(screen_width - mPreviewPadding - mRadius*2 - mRadius*6);
				mSeekBar.setRight(screen_width - mPreviewPadding*3 - mRadius*2);
				mSeekBar.setTop(mPreviewPadding + mRadius/2);
				mSeekBar.setBottom(mPreviewPadding + 3*mRadius/2);
    		}
    		else
    		{
				LayoutParams layoutParams = new LayoutParams( mRadius*6 - mPreviewPadding*2, mRadius);
			    layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
			    layoutParams.setMargins(screen_width - mPreviewPadding - mRadius*2 - mRadius*6,
			    							mPreviewPadding + mRadius/2, screen_width - mPreviewPadding*3 - mRadius*2, 
			    							mPreviewPadding + 3*mRadius/2);
			    mSeekBar.setLayoutParams(layoutParams);
    		}

			if( mIsSeekBarMadeVisible == false && mIsSeekBarVisible == true )
			{
				mSeekBar.setVisibility(View.VISIBLE);
				mIsSeekBarMadeVisible = true;
			}

			if( canvas != null )
			{
				mPaint.setColor(Color.WHITE);
				mPaint.setAlpha(220);
				mPaint.setStyle(Paint.Style.FILL_AND_STROKE);

				mRoundRect.set(screen_width - mPreviewPadding - mRadius*2 - mRadius*8 - mPreviewPadding*2,
								mPreviewPadding*2,
								screen_width - mPreviewPadding*2 - mRadius*2, 
								mPreviewPadding + mRadius*2 - mPreviewPadding);
				canvas.drawRoundRect(mRoundRect, 15.0f, 15.0f, mPaint);

				String seekBarString = "";
				if( mDisplayInMiles )
				{
					seekBarString = String.format("%.2f", max_distance_to_show * 0.621371/1000 ) + " mi";
				}
				else
				{
					seekBarString = String.format("%.1f", max_distance_to_show/1000.0f) + " km";
				}

				tpForPreview.setColor(Color.BLACK);
		    	tpForPreview.setTypeface(Typeface.DEFAULT_BOLD);
		    	tpForPreview.setTextSize(mPreviewFontSize);

		    	int save_count = canvas.save();
		    	StaticLayout sl = new StaticLayout(seekBarString, tpForPreview, mRadius*8 + mPreviewPadding, 
						Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
				canvas.translate(screen_width - mPreviewPadding*2 - mRadius*2 - mRadius*8, mPreviewPadding + mRadius - mSeekBar.getHeight()/4);
				sl.draw(canvas);

				canvas.restoreToCount(save_count);
			}
    	}
    }
}
