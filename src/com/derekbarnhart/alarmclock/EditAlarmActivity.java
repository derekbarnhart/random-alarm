//  EditAlarmActivity
//
//	Purpose: To provide an interface that:
//		1) Displays the current settings for an alarm
//		2) Allows modification to those settings
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
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TimePicker;
import android.widget.ToggleButton;

public class EditAlarmActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit);
        
        Intent intent = getIntent();
        Bundle extras = getIntent().getExtras();
        
        if(intent.hasExtra("hour"))
        {
        	//This is an edit
        	//Setup with the the passed extras
        	Alarm thisAlarm = new Alarm(intent.getIntExtra("hour", 0),
        	intent.getIntExtra("minute", 0),
        	intent.getBooleanArrayExtra("days"));
        	setupInterface(thisAlarm);
        } else
        {
        	setDay();
        	
        }
    }
    
    public void buttonHandler(View view)
    {
    	switch(view.getId())
    	{
    	case R.id.btnDone:
    		
    		TimePicker time =(TimePicker) findViewById(R.id.timePicker1);
    		ToggleButton day1 = (ToggleButton) findViewById(R.id.toggleDay1);
    		ToggleButton day2 = (ToggleButton) findViewById(R.id.toggleDay2);
    		ToggleButton day3 = (ToggleButton) findViewById(R.id.toggleDay3);
    		ToggleButton day4 = (ToggleButton) findViewById(R.id.toggleDay4);
    		ToggleButton day5 = (ToggleButton) findViewById(R.id.toggleDay5);
    		ToggleButton day6 = (ToggleButton) findViewById(R.id.toggleDay6);
    		ToggleButton day7 = (ToggleButton) findViewById(R.id.toggleDay7);

    	Intent result = new Intent();
    	result.putExtra("hour", time.getCurrentHour().intValue());
    	result.putExtra("minute", time.getCurrentMinute().intValue());
    	
    	if(day1.isChecked()||day2.isChecked()||day3.isChecked()||day4.isChecked()
    	   ||day5.isChecked()||day6.isChecked()||day7.isChecked())
    	{
	    	result.putExtra("day1", day1.isChecked());
	    	result.putExtra("day2", day2.isChecked());
	    	result.putExtra("day3", day3.isChecked());
	    	result.putExtra("day4", day4.isChecked());
	    	result.putExtra("day5", day5.isChecked());
	    	result.putExtra("day6", day6.isChecked());
	    	result.putExtra("day7", day7.isChecked());
    		setResult(RESULT_OK,result);
    		finish();
    	}else
    	{
    		AlertDialog ad = new AlertDialog.Builder(this).create();  
    		ad.setCancelable(false); // This blocks the 'BACK' button  
    		ad.setTitle("Which Days?");
    		ad.setMessage("Let me know which days to alert you.");  
    		ad.setButton("OK", new DialogInterface.OnClickListener() {  
    		    @Override  
    		    public void onClick(DialogInterface dialog, int which) {  
    		        dialog.dismiss();                      
    		    }  
    		});  
    		ad.show();	
    	}
    	break;
    	
    	case R.id.btnCancel:
    		setResult(Activity.RESULT_CANCELED,null);
    		finish();
    		break;
    	
    	default:
    		//Not a clue what this could be
    	break;
    	} 	
    }
    
    
    public void setupInterface(Alarm thisAlarm)
    {
    	TimePicker time =(TimePicker) findViewById(R.id.timePicker1);
		ToggleButton day1 = (ToggleButton) findViewById(R.id.toggleDay1);
		ToggleButton day2 = (ToggleButton) findViewById(R.id.toggleDay2);
		ToggleButton day3 = (ToggleButton) findViewById(R.id.toggleDay3);
		ToggleButton day4 = (ToggleButton) findViewById(R.id.toggleDay4);
		ToggleButton day5 = (ToggleButton) findViewById(R.id.toggleDay5);
		ToggleButton day6 = (ToggleButton) findViewById(R.id.toggleDay6);
		ToggleButton day7 = (ToggleButton) findViewById(R.id.toggleDay7);
		
		time.setCurrentHour(thisAlarm.getHour());
		time.setCurrentMinute(thisAlarm.getMinute());
    	
    	boolean[] theseDays = thisAlarm.getDays();
    	day1.setChecked(theseDays[0]);
    	day2.setChecked(theseDays[1]);
    	day3.setChecked(theseDays[2]);
    	day4.setChecked(theseDays[3]);
    	day5.setChecked(theseDays[4]);
    	day6.setChecked(theseDays[5]);
    	day7.setChecked(theseDays[6]);
    	
    }
    
    
    void setDay()
    {
    	Date t = new Date();
		t.setTime(java.lang.System.currentTimeMillis());
		SimpleDateFormat dt1 = new SimpleDateFormat("h:mm a");
		
		TimePicker time =(TimePicker) findViewById(R.id.timePicker1);
		ToggleButton day1 = (ToggleButton) findViewById(R.id.toggleDay1);
		ToggleButton day2 = (ToggleButton) findViewById(R.id.toggleDay2);
		ToggleButton day3 = (ToggleButton) findViewById(R.id.toggleDay3);
		ToggleButton day4 = (ToggleButton) findViewById(R.id.toggleDay4);
		ToggleButton day5 = (ToggleButton) findViewById(R.id.toggleDay5);
		ToggleButton day6 = (ToggleButton) findViewById(R.id.toggleDay6);
		ToggleButton day7 = (ToggleButton) findViewById(R.id.toggleDay7);
	
    	switch (t.getDay())
    	{
    		case 0:
    			day1.setChecked(true);
    			break;
    		case 1:
    			day2.setChecked(true);
    			break;
    		case 2:
    			day3.setChecked(true);
    			break;
    		case 3:
    			day4.setChecked(true);
    			break;
    		case 4:
    			day5.setChecked(true);
    			break;
    		case 5:
    			day6.setChecked(true);
    			break;
    		case 6:
    			day7.setChecked(true);
    			break;
    	}

    }
    

}