package com.cfi.lookout;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.LocationManager;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.cfi.lookout.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class utils
{
	public static void AddAdView(Activity activity)
	{
	}

	public static void AddAdView(View view, Activity activity)
	{
	}
	
	public static boolean IsLocationInfoAvailable(Context context)
	{
		boolean isGPSEnabled = false;
		boolean isNetworkEnabled = false;

    	// Acquire a reference to the system Location Manager
    	LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		
	    List<String> providers = locationManager.getProviders(true);
	    for (String providerName:providers)
	    {
	        if (providerName.equals(LocationManager.GPS_PROVIDER))
	        {
	        	isGPSEnabled = locationManager.isProviderEnabled(providerName);
	        }
	        else if (providerName.equals(LocationManager.NETWORK_PROVIDER))
	        {
	        	isNetworkEnabled = locationManager.isProviderEnabled(providerName);
	        }
	    }
	    
	    if( isGPSEnabled || isNetworkEnabled )
	    {
	    	return true;
	    }
	    return false;
	}

	
	public static String GetUniqueID(HttpPost httppost, Context context)
	{
		HttpResponse httpResponse;
		int responseCode;
		String message;
		String response;

		DefaultHttpClient httpclient = new DefaultHttpClient();
		//if( Constants.ENABLE_TOAST )
			//Toast.makeText(context, "httppost Data!", Toast.LENGTH_SHORT).show();

		try
		{
			httpResponse = httpclient.execute(httppost);
			responseCode = httpResponse.getStatusLine().getStatusCode();
			message = httpResponse.getStatusLine().getReasonPhrase();

			HttpEntity entity = httpResponse.getEntity();

			if (entity != null)
			{
				InputStream instream = entity.getContent();
				response = convertStreamToString(instream);

				//if( Constants.ENABLE_TOAST )
          			//Toast.makeText(context, "Response : " + response, Toast.LENGTH_SHORT).show();

          		// Closing the input stream will trigger connection release
          		instream.close();

          		//if( Constants.ENABLE_TOAST )
  					//Toast.makeText(context, "Response to Unique ID " + response, Toast.LENGTH_LONG).show();

				return response;
			}
		}
		catch(Exception e)
		{
			int u = 9;
			u = u+1;
			//if( Constants.ENABLE_TOAST )
				//Toast.makeText(context, "Exception while sending string : " + e.toString(), Toast.LENGTH_LONG).show();
		}
		return null;
	}

	  private static String convertStreamToString(InputStream is)
	  {
	      BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	      StringBuilder sb = new StringBuilder();
	
	      String line = null;
	      try {
	          while ((line = reader.readLine()) != null) {
	              sb.append(line);
	          }
	      } catch (IOException e) {
	          e.printStackTrace();
	      } finally {
	          try {
	              is.close();
	          } catch (IOException e) {
	              e.printStackTrace();
	          }
	      }
	      return sb.toString();
	  }
	  
	  public static int convertDipToPixels(float dips, Context context)
	  {
	      return (int) (dips * context.getResources().getDisplayMetrics().density + 0.5f);
	  }
	  
	  public static Bitmap loadBitmap(String url)
	  {
	 	Bitmap bm = null;
	    InputStream is = null;
	    BufferedInputStream bis = null;
	    try 
	    {
	        URLConnection conn = new URL(url).openConnection();
	        conn.connect();
	        is = conn.getInputStream();
	        bis = new BufferedInputStream(is, 8192);
	        bm = BitmapFactory.decodeStream(bis);
	    }
	    catch (Exception e) 
	    {
	        e.printStackTrace();
	    }
	    finally {
	        if (bis != null) 
	        {
	            try 
	            {
	                bis.close();
	            }
	            catch (IOException e) 
	            {
	                e.printStackTrace();
	            }
	        }
	        if (is != null) 
	        {
	            try 
	            {
	                is.close();
	            }
	            catch (IOException e) 
	            {
	                e.printStackTrace();
	            }
	        }
	    }
	    return bm;
	}
	  
	  public static Bitmap getBitmapFromURL(String src) {
		    try {
		        URL url = new URL(src);
		        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		        connection.setDoInput(true);
		        connection.connect();
		        InputStream input = connection.getInputStream();
		        Bitmap myBitmap = BitmapFactory.decodeStream(input);
		        return myBitmap;
		    } catch (IOException e) {
		        e.printStackTrace();
		        return null;
		    }
		}
	 
	public static Bitmap getBitmapFromType(int type, Context context)
	{
		switch(type)
		{
			case MainActivity.tLoo:
				return BitmapFactory.decodeResource(context.getResources(), R.drawable.loo_lens);
			case MainActivity.tFood:
				return BitmapFactory.decodeResource(context.getResources(), R.drawable.food_lens);
			case MainActivity.tShopping:
				return BitmapFactory.decodeResource(context.getResources(), R.drawable.shopping_lens);
			case MainActivity.tTaxi:
				return BitmapFactory.decodeResource(context.getResources(), R.drawable.taxi_lens);		
			case MainActivity.tBus:
				return BitmapFactory.decodeResource(context.getResources(), R.drawable.bus_lens);
			case MainActivity.tTrain:
				return BitmapFactory.decodeResource(context.getResources(), R.drawable.train_lens);
			case MainActivity.tBank:
				return BitmapFactory.decodeResource(context.getResources(), R.drawable.bank_lens);
			case MainActivity.tFuel:
				return BitmapFactory.decodeResource(context.getResources(), R.drawable.fuel_lens);
			case MainActivity.tDoctor:
				return BitmapFactory.decodeResource(context.getResources(), R.drawable.doctor_lens);
			case MainActivity.tFire:
				return BitmapFactory.decodeResource(context.getResources(), R.drawable.fire_lens);
			case MainActivity.tPolice:
				return BitmapFactory.decodeResource(context.getResources(), R.drawable.police_lens);
			case MainActivity.tLandmarks:
				return BitmapFactory.decodeResource(context.getResources(), R.drawable.landmarks_lens);	
			case MainActivity.tTemple:
				return BitmapFactory.decodeResource(context.getResources(), R.drawable.temple_lens);
			case MainActivity.tMosque:
				return BitmapFactory.decodeResource(context.getResources(), R.drawable.mosque_lens);
			case MainActivity.tChurch:
				return BitmapFactory.decodeResource(context.getResources(), R.drawable.church_lens);
			case MainActivity.tReligious:
				return BitmapFactory.decodeResource(context.getResources(), R.drawable.religious_lens);
		}
		return BitmapFactory.decodeResource(context.getResources(), R.drawable.closeby_lens);
	}
	  
	public static float getDistanceFromLatLonInKm(double lat1,double lon1,double lat2,double lon2)
	{
    	  float R = 6371; // Radius of the earth in km
    	  float dLat = deg2rad((float) (lat2-lat1));  // deg2rad below
    	  float dLon = deg2rad((float) (lon2-lon1)); 
    	  float a = (float) (Math.sin(dLat/2) * Math.sin(dLat/2) +
							Math.cos(deg2rad((float) lat1)) * Math.cos(deg2rad((float) lat2)) * 
							Math.sin(dLon/2) * Math.sin(dLon/2));
	   	  float c = (float) (2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)));
	   	  return R * c; // Distance in km
	}

	private static float deg2rad(float deg)
	{
	  return (float) (deg * (Math.PI/180));
	}

	public static float GetAngle(double lat1, double lon1, double lat2, double lon2)
	{
		double dy = lat1 - lat2;
		double dx = Math.cos(3.1415/180*lat1)*(lon1 - lon2);
		float angle = (float) Math.atan2(dy, dx);
		angle = (float) (angle * 180 / 3.1415);
    	if( angle >= 0 )
    	{
    		angle = 180 - angle;
    	}
    	else
    	{
    		angle = -180 - angle;
    	}
		return angle;
	}
	
    public static int GetType(String [] type)
    {
    	switch(MainActivity.mCurrentView)
		{
			case MainActivity.REVIEW_A_LOO:
				return MainActivity.tFood;
		}
    	if( type != null )
    	{
	    	for(int i = 0; i < type.length; i++)
	    	{
	        	if( type[i] == null )
	        		continue;
	        	
	        	if( type[i].contentEquals("loo") )
	        		return MainActivity.tLoo;
	
	    		if( type[i].contains("food") || type[i].contains("bar") || type[i].contains("bakery") || type[i].contains("cafe") || type[i].contains("restaraunt") || type[i].contains("night_club") || type[i].contains("meal_delivery") || type[i].contains("meal_takeaway") )
	    		{
	    			return MainActivity.tFood;
	    		}
	    		if( type[i].contains("shopping_mall") || type[i].contains("store") || type[i].contains("grocery_or_supermarket") )
	    		{
	    			return MainActivity.tShopping;
	    		}
	    		if( type[i].contains("taxi_stand") )
	    		{
	    			return MainActivity.tTaxi;
	    		}
	    		if( type[i].contains("bus_station") )
	    		{
	    			return MainActivity.tBus;
	    		}
	    		if( type[i].contains("subway_station") || type[i].contains("train_station") )
	    		{
	    			return MainActivity.tTrain;
	    		}
	    		if( type[i].contains("atm") || type[i].contains("bank") )
	    		{
	    			return MainActivity.tBank;
	    		}
	    		if( type[i].contains("gas_station") )
	    		{
	    			return MainActivity.tFuel;
	    		}
	    		if( type[i].contains("doctor") || type[i].contains("hospital") || type[i].contains("dentist") )
	    		{
	    			return MainActivity.tDoctor;
	    		}
	    		if( type[i].contains("fire_station") )
	    		{
	    			return MainActivity.tFire;
	    		}
	    		if( type[i].contains("police") )
	    		{
	    			return MainActivity.tPolice;
	    		}
	    		if( type[i].contains("lodging") || type[i].contains("department_store") || type[i].contains("movie_theater") || type[i].contains("establishment") || type[i].contains("university") )
	    		{
	    			return MainActivity.tLandmarks;
	    		}
	    		if( type[i].contains("hindu_temple") )
	    		{
	    			return MainActivity.tTemple;
	    		}
	    		if( type[i].contains("mosque") )
	    		{
	    			return MainActivity.tMosque;
	    		}
	    		if( type[i].contains("church") )
	    		{
	    			return MainActivity.tChurch;
	    		}
	    		if( type[i].contains("place_of_worship") )
	    		{
	    			return MainActivity.tReligious;
	    		}
	    	}
    	}
    	return MainActivity.tCloseby;
    }
    
    public static void RotateImage(ImageView image, float angle)
    {
    	if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB)
    	{
        	Matrix matrix = new Matrix();
        	image.setScaleType(ScaleType.MATRIX);
        	matrix.postRotate((float) angle, image.getDrawable().getBounds().width()/2, image.getDrawable().getBounds().height()/2);
        	image.setImageMatrix(matrix);
    	}
    	else
    		image.setRotation(angle);
    }
    
    public static String GetDate(long milliSeconds, String dateFormat)
    {
        // Create a DateFormatter object for displaying date in specified format.
        DateFormat formatter = new SimpleDateFormat(dateFormat);

        // Create a calendar object that will convert the date and time value in milliseconds to date. 
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }
    
	public static void sendMail(Context context)
	{
	    final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
	    emailIntent .setType("text/html");
	    emailIntent .putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"playapps4mob@gmail.com"});
	    emailIntent .putExtra(android.content.Intent.EXTRA_SUBJECT, "Feedback for Street Lens");
	    context.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
	}
	
	public static String GetCountryZipCode(Context context)
	{
		String CountryID = "";

		TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		// getNetworkCountryIso

		CountryID = manager.getSimCountryIso().toUpperCase();
		return CountryID;
	}
}


//	if( false )
//	{
//		lc.count = 4;
//		lc.latitude = new double[lc.count];
//		lc.longitude = new double[lc.count];
//		
//		lc.rating = new double[lc.count];
//		lc.name = new String[lc.count];
//		lc.icon = new String[lc.count];
//		lc.bitmap = new Bitmap[lc.count];
//		lc.distance = new float[lc.count];
//		lc.angle = new float[lc.count];
//
////		lc.latitude[0] = 12.91992;
////		lc.longitude[0] = 77.575549;
////		lc.name[0] = "Adigas, Jayanagar, 8th Block";
////		
////		lc.latitude[1] = 12.916930;
////		lc.longitude[1] = 77.578430;
////		lc.name[1] = "More Shop, Jayanagar, 8th Block";
////		
////		lc.latitude[2] = 12.920004;
////		lc.longitude[2] = 77.58347;
////		lc.name[2] = "Mainland China, Jayanagar, 5th Block";
////		
////		lc.latitude[3] = 12.928246;
////		lc.longitude[3] = 77.577703;
////		lc.name[3] = "Akshaya veg, Jayanagar, 5th Block";
//		
//		lc.latitude[0] = MainActivity.mLat - 0.01;
//		lc.longitude[0] = MainActivity.mLon;
//		lc.name[0] = "South";
//		
//		lc.latitude[1] = MainActivity.mLat + 0.01;
//		lc.longitude[1] = MainActivity.mLon;
//		lc.name[1] = "North";
//		
//		lc.latitude[2] = MainActivity.mLat;
//		lc.longitude[2] = MainActivity.mLon - 0.01;
//		lc.name[2] = "West";
//		
//		lc.latitude[3] = MainActivity.mLat;
//		lc.longitude[3] = MainActivity.mLon + 0.01;
//		lc.name[3] = "East";
//		
//		for(int i = 0; i < lc.count; i++ )
//		{
//			lc.distance[i] = utils.getDistanceFromLatLonInKm(MainActivity.mLat, MainActivity.mLon, lc.latitude[i], lc.longitude[i]);
//			lc.angle[i] = utils.GetAngle(MainActivity.mLat, MainActivity.mLon, lc.latitude[i], lc.longitude[i]);
//		}
//	MainActivity.sGlobalState = MainActivity.cPlacesFetched;
//	MainActivity.sGlobalState = MainActivity.cCameraView;
//	}
//	else
