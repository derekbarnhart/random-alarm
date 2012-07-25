//  RandomAlarmClockActivity
//
//	Purpose: To provide an interface that:
//		1) Displays items in a database of saved alarms
//		2) Allows addition and deletion of new alarms
//		3) Allows activity to be launched that modifies a particular alarm
//
//

package com.derekbarnhart.alarmclock;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;


public class RandomAlarmClockActivity extends Activity {
	private static final int ADD_ALARM= 1;
	private static final int EDIT_ALARM= 2;
	private static final String TAG= "RandomAlarmClock";
	
	SimpleAdapter adapter;
	ListView alarmListView;
	AlarmHelper alarmHelper;
	TextView tvClock;
	
	ArrayList<Alarm> mAlarms;
	long rowId=0L;
	// Mapping of columns to views
	String[] from = new String[] {"hour", "minute","am", "day1", "day2", "day3", "day4", "day5", "day6", "day7"};
    int[] to = new int[] { R.id.tvHour, R.id.tvMinute, R.id.tvAM, R.id.tvDay1, R.id.tvDay2, R.id.tvDay3, R.id.tvDay4, R.id.tvDay5, R.id.tvDay6, R.id.tvDay7 };
     
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
       alarmListView = (ListView) findViewById(R.id.alarmListView); 
       mAlarms = new ArrayList<Alarm>();
       alarmHelper = new AlarmHelper(this);
       tvClock =(TextView) findViewById(R.id.tvClock);
       setClock();
       updateInterface(false);
        
       //View footer = getLayoutInflater().inflate(R.layout.footer, null);
       //alarmListView.addFooterView(footer);
      
       
       IntentFilter filter = new IntentFilter();
       filter.addAction(Intent.ACTION_TIME_TICK);
       registerReceiver(mReceiver, filter);
       
       
       
       this.registerForContextMenu(alarmListView);    
        alarmListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				/* Toast.makeText(RandomAlarmClockActivity.this,
						//"Item in position " + position + " clicked",
						//Toast.LENGTH_LONG).show();
				*/
		
        Intent editIntent = new Intent(RandomAlarmClockActivity.this, EditAlarmActivity.class);
		Bundle sendBundle = new Bundle();
		
		editIntent.putExtra("hour",alarmHelper.alarms.get(position).getHour());
		editIntent.putExtra("minute", alarmHelper.alarms.get(position).getMinute());
		editIntent.putExtra("days", alarmHelper.alarms.get(position).getDays());		
		rowId = alarmHelper.alarms.get(position).getId();
		
		Log.d(TAG,"rowId "+rowId);
        startActivityForResult(editIntent,EDIT_ALARM);   		
        return;	
			}     		
        });  
    }

    @Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
    	
    	unregisterReceiver(mReceiver);
		super.onDestroy();
	}

	public void buttonHandler(View view)
    {
    	switch(view.getId())
    	{
    	case R.id.btnAdd:  		
    		//Alarm testAlarm = new Alarm();		
    		//Log.d("RandomAlarm","TestDays String: "+ testAlarm.getDaysString());		
    		Intent editIntent = new Intent(this, EditAlarmActivity.class);
    		startActivityForResult(editIntent,ADD_ALARM);		
    	break;
  	
    	default:
    		//Not a clue what this could be
    	break;
    	
    	}
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
         
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;     
        rowId = alarmHelper.alarms.get(info.position).getId();
        
        Log.d(TAG,"rowId: "+rowId);
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.delete:
                alarmHelper.deleteAlarm(rowId);
                updateInterface(false);
                Toast.makeText(this, "Alarm removed",
    					Toast.LENGTH_LONG).show();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == Activity.RESULT_OK)
		{
		boolean[] daysResult= new boolean[7];
		String logString = "";
		switch(requestCode)
		{
		case ADD_ALARM:	
			for(int i = 0; i<7;i++)
			{
				daysResult[i] = data.getBooleanExtra("day"+(i+1), false);
				logString +=" day"+(i+1)+ Boolean.toString(daysResult[i]);
			}
			alarmHelper.addAlarm(data.getIntExtra("hour",0),data.getIntExtra("minute",0),daysResult);
			
			Toast.makeText(this, "Alarm set",
					Toast.LENGTH_LONG).show();
			break;

		case EDIT_ALARM:
				
			for(int i = 0; i<7;i++)
			{
				daysResult[i] = data.getBooleanExtra("day"+(i+1), false);
				logString +=" day"+(i+1)+ Boolean.toString(daysResult[i]);	
			}	
			alarmHelper.updateAlarm(rowId,data.getIntExtra("hour",0),data.getIntExtra("minute",0),daysResult);
			
			Toast.makeText(this, "Alarm updated",
					Toast.LENGTH_LONG).show();
			break;
		default:
			
			break;
		}
		updateInterface(false);
		}
	}
    
    public void updateInterface(boolean updateFlag)
    {
        // create the grid item mapping
       List<HashMap<String, String>> fillMaps = alarmHelper.loadAlarms(updateFlag);
        // fill in the grid_item layout
        adapter = new SimpleAdapter(this, fillMaps, R.layout.alarm_item, from, to);
        alarmListView.setAdapter(adapter);   	
    }
    
    
    BroadcastReceiver mReceiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) 
		{
			//Capture time tick here
			setClock();
		}	
    };
    
    
    public void setClock()
    {
    	Date t = new Date();
		t.setTime(java.lang.System.currentTimeMillis());
		SimpleDateFormat dt1 = new SimpleDateFormat("h:mm a");
		tvClock.setText(dt1.format(t));
    	//Find out the next time this is supposed to fire
    	//Figure out which day it is
    }
     
}