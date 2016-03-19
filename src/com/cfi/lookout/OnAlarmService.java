package com.cfi.lookout;

//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.sql.Date;
//
//import org.apache.http.HttpEntity;
//import org.apache.http.HttpResponse;
//import org.apache.http.client.methods.HttpGet;
//import org.apache.http.impl.client.DefaultHttpClient;
//import android.telephony.TelephonyManager;
//import android.telephony.gsm.GsmCellLocation;
//import android.text.format.DateFormat;
//import android.util.Log;
//import android.os.Build;
//import android.content.Context;

import java.sql.Date;

import com.cfi.lookout.R;
import com.cfi.lookout.publicLoosUtils.Loo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

public class OnAlarmService extends Service
{
	@Override
	public IBinder onBind(Intent arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate()
	{
		super.onCreate();
		
		Loo loo = publicLoosUtils.getLooInfo("" + LooDetails.sLastVisitedLoo);
		String looName = loo.address;

		Intent intent = new Intent(this, ReportAnIssue.class);
		intent.putExtra("origin", "1");
		intent.putExtra("name", looName);
		intent.putExtra("date", new Date(System.currentTimeMillis()));
		NotificationManager mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(R.drawable.icon,
											"Request your feedback!", System.currentTimeMillis());  
	    									notification.setLatestEventInfo(this,  
	    									"Loo-k Out","Looks like you visited " + looName + ". Request your feedback!",  
	    									PendingIntent.getActivity(this.getBaseContext(), 0, intent,  
	    									PendingIntent.FLAG_CANCEL_CURRENT));
	    notification.flags |= Notification.FLAG_AUTO_CANCEL;
	    mManager.notify(0, notification);

        // Exit
		stopSelf();
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}
}

