package com.cfi.lookout;

import com.cfi.lookout.R;
import com.cfi.lookout.publicLoosUtils.Loo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

public class SMSHandler extends BroadcastReceiver
{
    private	Settings 	mSettings	 		= new Settings();

	@Override
	public void onReceive(Context context, Intent intent)
	{
		if( intent.getAction().equalsIgnoreCase("android.provider.Telephony.SMS_RECEIVED") )
		{
			if( ParseIncomingSMS(context, intent, mSettings, this) )
			{
				if( Constants.ENABLE_TOAST )
					Toast.makeText(context, "Intent aborted from propagating!", Toast.LENGTH_LONG).show();
			}
		}
	}

	public static boolean ParseIncomingSMS(Context context, Intent intent, Settings settings, BroadcastReceiver broadcastReceiver)
	{
		Bundle bundle = intent.getExtras();

		if (bundle != null)
		{
			Object[] pdus = (Object[])bundle.get("pdus");
			SmsMessage sms = SmsMessage.createFromPdu((byte[])pdus[0]);

			boolean result = ParseSMSCommand(sms.getMessageBody(), context);

			if( result == true )
			{
				broadcastReceiver.abortBroadcast();
				Uri deleteUri = Uri.parse("content://sms");
				context.getContentResolver().delete(deleteUri, "address=? and date=?", new String[] {sms.getOriginatingAddress(), String.valueOf(sms.getTimestampMillis())});
			}

			return result;
		}

		return false;
	}

	public static boolean ParseSMSCommand(String content, Context context)
	{
		if( content != null && content.toLowerCase().startsWith("sent from your twilio trial account") )
		{
			String looName = null;
			String heading = null;
			String message = null;
			Intent intent = null;
			if(MainActivity.mIsAdmin)
			{
				String looIdString = content.split("Loo:")[1].split(",")[0];
				int looId = 1;
				try{
				if(looIdString != null )
				{
					looId = Integer.parseInt(looIdString);
				}
				}catch(Exception e){
					Toast.makeText(context, "Invalid id", Toast.LENGTH_LONG).show();
				}
				
				String issueType = content.split("Issue:")[1];
				try{
				if(looIdString != null )
				{
					issueType = issueType.replaceAll("^\\s+", "");
					issueType = issueType.replaceAll("\\s+$", "");
				}
				}catch(Exception e){
					Toast.makeText(context, "Invalid IssueId", Toast.LENGTH_LONG).show();
				}

				Loo loo = publicLoosUtils.getLooInfo("" + looId);
				
				if( loo == null )
					return false;

				looName = loo.address;

				intent = new Intent(context, ReportedIssue.class);
				intent.putExtra("new_issues", true);
				intent.putExtra("name", loo.address);
				intent.putExtra("looId", ""+looId);
				intent.putExtra("issueType", issueType);

				heading = "Issue reported!";
				message = "An issue is reported for the loo at " + looName;
			}
			else
			{
				Loo loo = publicLoosUtils.getLooInfo("" + LooDetails.sLastVisitedLoo);
				looName = loo.address;
				intent = new Intent(context, LooDetails.class);
				intent.putExtra("resolved", true);
				intent.putExtra("name", looName);
				intent.putExtra("reference", "" + loo.id);
				intent.putExtra("distance", 0);
		        intent.putExtra("angle", 0);
		        intent.putExtra("latitude", loo.latitude);
		        intent.putExtra("longitude", loo.longitude);
		        intent.putExtra("name", loo.address);
		        intent.putExtra("type", loo.type);
		        intent.putExtra("address", loo.address);
		        intent.putExtra("rating", loo.average_rating);

				
				heading = "Issue resolved!";
				message = "Your reported issue is now resolved. Thanks for reporting";
			}

			sendNotification(context, looName, heading, message, intent);
			return true;
		}
		else
			return false;
	}
	
	public static void sendNotification(Context context, String looName, String heading, String message, Intent intent)
	{
		intent.putExtra("name", looName);
		NotificationManager mManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(R.drawable.icon,
											heading, System.currentTimeMillis());  
	    									notification.setLatestEventInfo(context,  
	    									"Loo-k Out", message,
	    									PendingIntent.getActivity(context, 0, intent,  
	    									PendingIntent.FLAG_CANCEL_CURRENT));
	    notification.flags |= Notification.FLAG_AUTO_CANCEL;
	    mManager.notify(0, notification);
	}
}
