/*
 * 	AlarmHelper.java
 * 
 * 	A class of utilities to assist in managing alarms
 * 
 * 	1) Sync database and pending intents
 * 	2) Perform logic to determine next alarm to play
 * 
 */

package com.derekbarnhart.alarmclock;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.util.Log;

public class AlarmHelper 
{
	private static final String TAG= "AlarmHelper";
	public ArrayList<Alarm> alarms;
	private Context context;
	
	
	public AlarmHelper(Context context) {
		super();
		this.context = context;
		this.alarms = new ArrayList<Alarm>();
	}

	/**
	 * This method removes a given alarm from the PendingIntent list
	 * @param alarm An alarm object with an id to be removed.
	 * @return Nothing.
	 */
	public void removeAlarm(Alarm alarm)
    {
		
		
    	Intent intentlocal = new Intent(context,AlarmReceiver.class);
    	// This is vital without it the alarm will not be cancelled
    	int requestCode = Integer.parseInt(Long.toString(alarm.getId()));
    	String RTC_WAKEUP_ALARM = "com.derekbarnhart.alarmclock.alarm";
    	
    	Intent alarmIntent = new Intent(RTC_WAKEUP_ALARM);
    	
    	
    	alarmIntent.putExtra("requestCode", requestCode);
    	
    	PendingIntent pilocal = PendingIntent.getBroadcast(context,requestCode,alarmIntent,PendingIntent.FLAG_ONE_SHOT);
    	AlarmManager alarmManager = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
    	alarmManager.cancel(pilocal);
    	pilocal.cancel();
    	Log.d(TAG,"Alarm "+requestCode + " Cancelled: "+alarm.getHour()+":"+alarm.getMinute());
    }
    
 
	
	/**
	 * This method accepts alarm details, modifies the item in the database and then updates the pending intent 
	 * @param id A long representing the database id of the alarm
	 * @param hour An integer for the hour (24 hr) that the alarm will sound
	 * @param minute An integer for the minute the alarm will sound
	 * @param days A boolean array that contains which days the alarm will sound on
	 * @return Nothing.
	 */
	public void updateAlarm(long id,int hour, int minute, boolean[] days)
    {
    	AlarmAdapter dbAdapter = new AlarmAdapter(context);
    	Alarm newAlarm = new Alarm(hour,minute,days);
    	newAlarm.setId(id);
    	
    	dbAdapter.open();
    	try
    	{
    		// Update the item in the database
    		Log.d(TAG,"Update Result: "+Boolean.toString(dbAdapter.update(id,newAlarm)));
    	
    	}catch(SQLException e)
    	{
    		Log.e(TAG,"Database Error: Adding");
    		Log.e(TAG,e.getMessage().toString());
    	}finally
    	{
    		dbAdapter.close();
    	}
    	
    	removeAlarm(newAlarm);//removes the old from the Alarm queue
    	setAlarm(newAlarm); //Sets the alarm with updates
    	Log.d(TAG,"Alarm "+id+" Successfully Updated");
    	
    }
	
	/**
	 * This method adds an alarm to the database
	 * @param hour An integer for the hour (24 hr) that the alarm will sound
	 * @param minute An integer for the minute the alarm will sound
	 * @param days A boolean array that contains which days the alarm will sound on
	 * @return Nothing.
	 */
	public void addAlarm(int hour, int minute, boolean[] days)
    {
    	AlarmAdapter dbAdapter = new AlarmAdapter(context);
    	Alarm newAlarm = new Alarm(hour,minute,days);
    	
    	long newRow= 0;
    	dbAdapter.open();
    	try
    	{
    		newRow = dbAdapter.insertItem(newAlarm);
    		newAlarm.setId(newRow);
    	}catch(SQLException e)
    	{

    		Log.e(TAG,"Database Error: Adding");
    		Log.e(TAG,e.getMessage().toString());
    		
    	}finally
    	{
    		dbAdapter.close();
    	}
    		
    	Log.d(TAG,"Alarm "+newRow +" Successfully Added");
    	
    	setAlarm(newAlarm);//Set the alarm in the pending intents
    }
   
	
	/**
	 * This method sets an alarm as a pending intent after determining the next time it should occur
	 * @param alarm An alarm object
	 * @return Nothing.
	 */
    public void setAlarm(Alarm alarm)
    {
    	AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);	
    	String RTC_WAKEUP_ALARM = "com.derekbarnhart.alarmclock.alarm";
    	int requestCode = Integer.parseInt(Long.toString(alarm.getId()));
    	
    	Intent alarmIntent = new Intent(RTC_WAKEUP_ALARM);
    	alarmIntent.putExtra("requestCode", requestCode);
    
    	PendingIntent rtcIntent = PendingIntent.getBroadcast(context, requestCode, alarmIntent, PendingIntent.FLAG_ONE_SHOT);
    	Date t = new Date();
    	
    	//Find out the next time this is supposed to fire
    	//Figure out which day it is
    	t.setTime(java.lang.System.currentTimeMillis());
    	int currentDay = t.getDay();
    	boolean[] days = alarm.getDays();
    	
    	//Find the next day in the list
    	int index = findNextAlarm(currentDay,days);
    	
    	//Default the number of days left till that day to 0
    	int daysLeft=0;
  
    	int t_time = t.getHours()*60+t.getMinutes();//Current Time
    	int a_time = alarm.getHour()*60+alarm.getMinute();//Alarm Time
    	
    	if(index == currentDay)
    	{
    		//This alarm is set for today
    		//Check to see if it has already passed
    		  		
	    	if(t_time<a_time)
	    	{//Alarm has not passed
	    			
	    	}else
	    	{//Alarm has already passed so we need to find the next one
	    		
	    		index = findNextAlarm(currentDay+1,days);
	    		if(index == currentDay)
	    		{// This alarm must only happen once a week	
	    			daysLeft = 7; 
	    			}else
	    		{// Find the number of days left till the next alarm
	    			daysLeft = (index-currentDay);
	    	    	if(daysLeft<0) daysLeft = 7+daysLeft;
	    		}
	    	}
    	}else		
    	{
    		// Find the number of days left till the next alarm
    		daysLeft = (index-currentDay);
    		if(daysLeft<0) daysLeft = 7+daysLeft;
    	}
    	
    	Date a = new Date();
    	
    	//The current time - milliseconds since day starter + 
    	//(milliseconds to the start of the next alarm + 
    	// milliseconds to the alarm during that day)
    	
    	// Build an alarm date using the given alarm time of day 
    	// and the amount of days left until its next occurance
    	a.setTime((t.getTime()-(t_time*60*1000)-(t.getSeconds()*1000))+(daysLeft*24*60*60*1000)+(a_time*60*1000));
    	
    	Log.d(TAG,"Alarm"+ requestCode+" set for: "+ a.toString());
    	
    	//Toast.makeText(this, "Current Day: "+a.getDay()+" Index: "+index+"Days to go: "+ daysLeft, Toast.LENGTH_SHORT).show();	
    	//t.setTime();//Set this to go off 10 seconds from now
    	alarms.set(AlarmManager.RTC_WAKEUP, a.getTime(), rtcIntent);	
    }
   
   
    public void resetAlarm(long rowID)
    {
    	AlarmAdapter dbAdapter = new AlarmAdapter(context);
    	
    	try
    	{
    		dbAdapter.open();
    		this.setAlarm(dbAdapter.getItem(rowID));
        	   		
    	}catch(SQLException e)
    	{
    		Log.d(TAG,"Database Error: Reseting Alarm");
        	Log.e(TAG,e.getMessage());
        	
    	}finally
    	{
    		dbAdapter.close();

    	}	
    	
    	Log.d(TAG,"Alarm "+ rowID+" Successfully Reset");
    }
    
    
    /**
	 * This method deletes an alarm from the database
	 * @param id A long reference to an item
	 * @return Nothing.
	 */
    public void deleteAlarm(long row)
    {
    	AlarmAdapter dbAdapter = new AlarmAdapter(context);
    	dbAdapter.open();
    	try
    	{
    		removeAlarm(dbAdapter.getItem(row));		
    		dbAdapter.remove(row);
    	}catch(SQLException e)
    	{

    		Log.e(TAG,"Database Error: Deleting");
    		Log.e(TAG,e.getMessage().toString());
    		
    	}finally
    	{
    		dbAdapter.close();
    	}
    	Log.d(TAG,"Alarm "+ row+ " Successfully Deleted");
    	
    	
    }
    
    
    /**
	 * This method takes the current day in the form of an integer from 0-6 as well
	 * as an array representing which days to play the alarm on and returns the next 
	 * day on which an alarm will be played 
	 * @param today An integer from 0-6 representing a day of the week
	 * @param days A boolean array representing which days to play the alarm on
	 * @return integer An integer from 0-6 representing a day of the week on which the alarm should next be played
	 */
  private int findNextAlarm(int today, boolean[] days)
    { 	
		int index = today;
	  	for(int i = 0 ; i<7;i++)
	  	{
	  		if(days[index]) break;
	  		index++;
	  		if(!(index<7)) index = 0;
	  	}
	  	return index;
    }
	
	
  private void clearAlarms()
  { 
	  alarms.clear();
  }
  
  /**
	 * This method loads the alarms from the database into a List object and optionally will set them to go be played 
	 * @param setAlarm A boolean flag noting if the alarms should be set to go off or just loaded into the List object
	 * @return List object containing information for all the alarms in the database
	 */
  public List<HashMap<String, String>> loadAlarms(boolean setAlarm)
  {
  	List<HashMap<String, String>> fillMaps = new ArrayList<HashMap<String, String>>();
  	
  	//Get an adapter to access the database
  	AlarmAdapter dbAdapter = new AlarmAdapter(context);
      Cursor thisCursor;
      
      //Clear all items before begining
      clearAlarms();
      dbAdapter.open();
      try
      {
      	thisCursor = dbAdapter.getAllItemsCursor();
      	
      	if(thisCursor.moveToFirst())
      	{
      		//Get the indexes for the particular columns of interest
      		int hourColumn = thisCursor.getColumnIndex("hour");
      		int minuteColumn = thisCursor.getColumnIndex("minute");
      		int daysColumn = thisCursor.getColumnIndex("days");
      		int idColumn = thisCursor.getColumnIndex("_id");
      		
	        	do
	        	{
	        		//Build the alarm
	        		Alarm thisAlarm = new Alarm();
	        		thisAlarm.setHour(thisCursor.getInt(hourColumn));
	        		thisAlarm.setMinute(thisCursor.getInt(minuteColumn));
	        		String testDayString = thisCursor.getString(daysColumn);
	        		thisAlarm.setDaysString(testDayString);
	        		thisAlarm.setId(thisCursor.getLong(idColumn));
	        		
	        		alarms.add(thisAlarm);
	        		
	        		if(setAlarm)
	        		{//Set the alarm if the flag was set
	        			this.setAlarm(thisAlarm);	
	        		}
	        			
	        		
	        		HashMap<String, String> map = new HashMap<String, String>();
	        		int hour = thisAlarm.getHour();
	        		
	        		
	        		//Load the time
	        		//Format it for 12 hours
	        		if(hour>11)
	        		{	
	        			if(hour!=12)
	        			{
	        				map.put("hour", pad(hour % 12," "));      	
	        			}else
	        			{
	        				map.put("hour",""+12);
	        			}
	        			map.put("minute", pad(thisAlarm.getMinute(),"0"));
	        			map.put("am", "PM");
	        			
	        		}else
	        		{
	        			map.put("hour", pad(thisAlarm.getHour()," "));      	
	        			map.put("minute", pad(thisAlarm.getMinute(),"0"));
	        			map.put("am", "AM");
	        		}
  	            
	        	
	        	//Load the day abbreviation into the map keys	
	        	for(int i = 0; i<7;i++)
	        	{
	        		String dayString = thisAlarm.getDayString(i);
	        		if(dayString.length()==1)
	        		{
	        			//Build the map key using the index i and add the string for that particular day
	        			map.put("day"+(i+1), " " + thisAlarm.getDayString(i));			
	        		}else
	        		{
	        			map.put("day"+(i+1), "" + thisAlarm.getDayString(i));
	        		}
	        	}
	        	
  	            fillMaps.add(map);
  	            
  	            //loop while there are still alarm entries in the database  		
	        	}while(thisCursor.moveToNext());	
      	}
      	
      	}catch (SQLException e)
      	{
      		Log.e(TAG,"Database Error:Reading" );
      		Log.e(TAG,e.getMessage().toString());	
      	}finally
      	{	
      		dbAdapter.close();
      	}
      	
      	return fillMaps;	
  }
	
  
  //Utility to fill in 
  private String pad(int unpadded_number,String padString)
  {
		  if(unpadded_number<10)
		  {
			  return padString + unpadded_number; 
		  }else
		  {
			  return Integer.toString(unpadded_number);
		  }
  }
	
	
}
