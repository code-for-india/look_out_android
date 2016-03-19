package com.cfi.lookout;

import java.util.List;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

class CameraSurface  extends SurfaceView implements SurfaceHolder.Callback
{
	SurfaceHolder mHolder;
	Camera mCamera;

	CameraSurface(Context context)
	{
	    super(context);
	    mHolder = getHolder();
	    mHolder.addCallback(this);
	    mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public void surfaceCreated(SurfaceHolder holder)
	{
		Log.i("CameraSurface", "Surface Created");
	    try
	    {
	    	mCamera = Camera.open();
	    	mCamera.setPreviewDisplay(holder);
	    }
	    catch(Exception e)
	    {
	    	Log.i("CameraSurface", "Exception while opening camera. Message is " + e.getMessage());
	    	if( mCamera != null )
	    		mCamera.release();
	    	mCamera = null;
	    }
	}

	public void surfaceDestroyed(SurfaceHolder holder)
	{
		Log.i("CameraSurface", "Surface destroyed");
	    if (mCamera != null)
	    {
	    	mCamera.stopPreview();
	    	mCamera.setPreviewCallback(null);
	    	mCamera.release();
	    	mCamera = null;
	    }
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h)
	{
		if( mCamera != null )
		{
			Camera.Parameters parameters = mCamera.getParameters();  
			List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();  
			Camera.Size cs = sizes.get(0);  
			parameters.setPreviewSize(cs.width, cs.height);  
			mCamera.setParameters(parameters);
		    mCamera.startPreview();
		    Log.i("Camera-Surface Changed", "h is " + h + " w is " + w + "; width is " + cs.width + " height is " + cs.height);
		}
	}
}
