package com.cfi.lookout;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class TiltDetector implements SensorEventListener
{
	public static float swRoll;
	public static float swPitch;
	public static float swAzimuth;

	public static SensorManager mSensorManager;
	public static Sensor accelerometer;
	public static Sensor magnetometer;

	public static float[] mAccelerometer = null;
	public static float[] mGeomagnetic = null;

	public TiltDetector(Context context)
	{
	    mSensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
	    accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	    magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

	    mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
	    mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME);
	    
	    swRoll = 0;
	    swPitch = 0;
	    swAzimuth = 0;
	}
	
	public void Clear()
	{
		mSensorManager.unregisterListener(this, accelerometer);
		mSensorManager.unregisterListener(this, magnetometer);
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy)
	{
	}

	@Override
	public void onSensorChanged(SensorEvent event)
	{
	    // onSensorChanged gets called for each sensor so we have to remember the values
	    if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
	    {
	        mAccelerometer = event.values;
	    }

	    if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
	    {
	        mGeomagnetic = event.values;
	    }

	    if (mAccelerometer != null && mGeomagnetic != null)
	    {
	        float R[] = new float[9];
	        float I[] = new float[9];
	        boolean success = SensorManager.getRotationMatrix(R, I, mAccelerometer, mGeomagnetic);

	        if (success)
	        {
	            float orientation[] = new float[3];
	            SensorManager.getOrientation(R, orientation);
	       
	            // at this point, orientation contains the azimuth(direction), pitch and roll values.
	            swAzimuth = (float) (180 * orientation[0] / Math.PI);
	            swPitch = (float) (180 * orientation[1] / Math.PI);
	            swRoll = (float) (180 * orientation[2] / Math.PI);
	            
	            //Log.d("Sensor demo", ": , azimuth: " + swAzimuth + ", pitch: " + swPitch + ", roll: " + swRoll);
	        }
	    }
	}

	public void GetLatestValues(xmlresultclasses.OrientationValues val)
	{
	    val.Roll 	= swRoll;
	    val.Pitch 	= swPitch;
	    val.Azimuth = swAzimuth;
	}
}
