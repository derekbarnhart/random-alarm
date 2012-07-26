/*	AlarmReceiver.java
 * 
 * 	This class has 2 functions
 * 	1) Reload the alarms after a reboot since all pending intents are wiped out by that event
 * 	2) Receive the pending intent from an alarm once triggered and launch the alarm activity
 * 
 */

package com.derekbarnhart.alarmclock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {

	private static final String TAG = "AlarmReceiver";
	
	@Override
	public void onReceive(Context context, Intent intent) 
	{
		
		if(intent.getAction().matches("android.intent.action.BOOT_COMPLETED"))
		{
		Log.d(TAG,"Got Boot");
		
		AlarmHelper alarmHelper = new AlarmHelper(context);
		alarmHelper.loadAlarms(true);
		
			
		}else if(intent.getAction().matches("com.derekbarnhart.alarmclock.alarm"))
		{
			int requestCode = intent.getIntExtra("requestCode", 0);
			Log.d(TAG,"Alarm Received - Request Code: "+ requestCode);
			
			Intent alarmIntent = new Intent(context,AlarmActivity.class);
			alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			alarmIntent.putExtra("requestCode", requestCode);
			context.startActivity(alarmIntent);
		}
	}

}

