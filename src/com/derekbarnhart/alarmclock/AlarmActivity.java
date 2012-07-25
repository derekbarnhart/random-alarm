package com.derekbarnhart.alarmclock;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class AlarmActivity extends Activity 
{
	final static String TAG = "AlarmActivity";
	Ringtone r;
	Vibrator vibrator;
	PowerManager.WakeLock wl;
	int requestCode;
	boolean mRepeat = true;
	Thread repeatThread;
	long mDelay = 500;
	long mAutoCancelDelay = 2;// How long the alarm plays before automatically stopping in minutes
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.alarm);
		
		 Window thisWindow = this.getWindow();
		 thisWindow.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
				 WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON|
				 WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED );
			
		Timer thisTimer = new Timer("autoAlarmKiller"); 
		thisTimer.schedule(forceCancel, 1000 * 60 * mAutoCancelDelay);
		
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "My Tag");
		 
		 wl.acquire();
		 Intent intent = getIntent();
		 requestCode = intent.getIntExtra("requestCode", 0);

		 Toast.makeText(this, "Alarm Triggered.",
				Toast.LENGTH_LONG).show();
		
		RingtoneManager ringManager = new RingtoneManager(this);
		//ringManager.setType(RingtoneManager.TYPE_ALARM);
		
		Cursor ringtoneCursor = ringManager.getCursor();
		Uri notification;
		if(ringtoneCursor.moveToFirst())
		{
			
			int columns = ringtoneCursor.getColumnCount();
			String columnName = "";
			for(int i = 0; i<columns;i++)
			{
				columnName += ringtoneCursor.getColumnName(i)+"("+i+") "; 
				
			}
			Log.d(TAG,columnName);
			
			
			int ringtoneCount = ringtoneCursor.getCount();
			int index = Math.round((float)(Math.random()*ringtoneCount));			
				
			notification = ringManager.getRingtoneUri(index);
						
		}else
		{
		//Play the default one in here	
			notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		}
		
		r = RingtoneManager.getRingtone(this, notification);
		r.setStreamType(AudioManager.STREAM_ALARM);
		ringManager.stopPreviousRingtone();
		//r.play();
		repeatThread = new Thread(alarmRepeater);
		repeatThread.start();
		// Vibrate the mobile phone
		vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
		long[] pattern = {0,500,500};
		vibrator.vibrate(pattern,0);	
		
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		 
		
		
		if(wl != null)
		{
			wl.release();
		}
		super.onDestroy();
	}



	public void buttonHandler(View view)
	{
		switch(view.getId())
		{
			case R.id.alarmStop:
				/*mRepeat = false;				
				r.stop();// Shut off the tone
				vibrator.cancel();// Shut off the vibration
				
				//Reset the alarm
				AlarmHelper alarmHelper = new AlarmHelper(this);
				alarmHelper.resetAlarm(requestCode);
				Toast.makeText(this, "Alarm Reset",
				Toast.LENGTH_LONG).show();
			*/
				killAlarm();
					
				break;
			default:
			
				break;
		}
		
	}
	
	
	Runnable alarmRepeater = new Runnable()
	{
		@Override
		public void run() 
		{
			// TODO Auto-generated method stub
			while(mRepeat)
			{
				if(!r.isPlaying())
				{
					r.play();	
				}else
				{
					try
					{
						this.wait(mDelay);
					}catch(Exception e)
					{
					}
				}
			}
		}
	};
	
	
	TimerTask forceCancel = new TimerTask(){

		@Override
		public void run() {
			// TODO Auto-generated method stub	
		
			killAlarm();
		}	
	};

	public void killAlarm()
	{
		mRepeat = false;				
		r.stop();// Shut off the tone
		vibrator.cancel();// Shut off the vibration
		
		//Reset the alarm
		AlarmHelper alarmHelper = new AlarmHelper(this);
		alarmHelper.resetAlarm(requestCode);
		AlarmActivity.this.finish();
	}
	
}
