package com.derekbarnhart.alarmclock;

import java.util.Arrays;


public class Alarm {

 private static final String[] dayReference = { "Su","M","Tu","W","Th","F","Sa"};
	
  int hour;
  int minute;
  boolean[] days;
  
  long id;
  
    
public long getId() {
	return id;
}
public void setId(long id) {
	this.id = id;
}
public int getHour() {
	return hour;
}
public void setHour(int hour) {
	this.hour = hour;
}
public int getMinute() {
	return minute;
}
public void setMinute(int minute) {
	this.minute = minute;
}

public boolean getDay(int day)
{
	return days[day];
}

public String getDayString(int day)
{

	if(days[day])
	{
		return dayReference[day];
	}else
	{
		return " ";	
	}
}

public void setDay(int day,boolean value)
{
	days[day] = value;
}

public String getDaysString()
{
	String result = "";
	
	//Loop through every item in the array if that item has been flagged then add it to the string
	for(int i = 0;i<7;i++)
	{
		if(days[i])
		{
			if(result!="") result+=",";
			result += dayReference[i];
		}
	}	
	return result;
}

public void setDaysString(String dayString)
{
	// New code:	
	Arrays.fill(days, false);
	 for (String token : dayString.split(",")) 
	 {	 
		 if(token.matches("Su"))
		 {days[0]= true;}
		 if(token.matches("M"))
		 {days[1]= true;}
		 if(token.matches("Tu"))
		 {days[2]= true;}
		 if(token.matches("W"))
		 {days[3]= true;}
		 if(token.matches("Th"))
		 {days[4]= true;}
		 if(token.matches("F"))
		 {days[5]= true;}
		 if(token.matches("Sa"))
		 {days[6]= true;}
	 }

}


public boolean[] getDays() {
	return days;
}

public void setDays(boolean[] days) {
	this.days = days;
}

public Alarm(int hour, int minute, boolean[] days) {
	super();
	this.hour = hour;
	this.minute = minute;
	this.days = days;
}
public Alarm() {
	super();
	// TODO Auto-generated constructor stub
	
	days = new boolean[7];
}


}