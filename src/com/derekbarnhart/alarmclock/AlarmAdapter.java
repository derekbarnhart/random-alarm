/*
 * 	AlarmAdapter.java
 * 
 * 	This provides database access to the SQLite database
 * 
 */
package com.derekbarnhart.alarmclock;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

public class AlarmAdapter {

	
	
	private static final String DATABASE_NAME = "randomalarms.db";
  private static final String DATABASE_TABLE = "alarms";
  private static final int DATABASE_VERSION = 1;
 
  public static final String KEY_ID = "_id";
  public static final String KEY_HOUR = "hour";
  public static final String KEY_MINUTE = "minute";
  public static final String KEY_DAYS = "days";
  
  public static final String TAG = "AlarmAdapter";
  
  private SQLiteDatabase db;
  private final Context context;
  private DBOpenHelper dbHelper;
  
  public AlarmAdapter(Context _context) {
    this.context = _context;
    dbHelper = new DBOpenHelper(context, DATABASE_NAME, 
                                    null, DATABASE_VERSION);
  }
  
  public void close() {
    db.close();
  }
  
  public void open() throws SQLiteException {  
    try {
      db = dbHelper.getWritableDatabase();
    } catch (SQLiteException ex) {
      db = dbHelper.getReadableDatabase();
    }
  }  
  
  //Insert a new task
  public long insertItem(Alarm _entry) {
    // Create a new row of values to insert.
    ContentValues newValues = new ContentValues();
    // Assign values for each row.
    newValues.put(KEY_HOUR, _entry.getHour());
    newValues.put(KEY_MINUTE, _entry.getMinute());
    
    Log.d(TAG,"Day String: "+_entry.getDaysString());
    newValues.put(KEY_DAYS, _entry.getDaysString());
    
    // Insert the row.
    return db.insert(DATABASE_TABLE, null, newValues);
  }

  // Remove an alarm based on its index
  public boolean remove(long _rowIndex) {
    return db.delete(DATABASE_TABLE, KEY_ID + "=" + _rowIndex, null) > 0;
  }

  // Update an alarm
  public boolean update(long _rowIndex,Alarm _entry) {
    ContentValues newValue = new ContentValues();
    newValue.put(KEY_HOUR, _entry.getHour());
    newValue.put(KEY_MINUTE, _entry.getMinute());
    newValue.put(KEY_DAYS, _entry.getDaysString());
   
    return db.update(DATABASE_TABLE, newValue, KEY_ID + "=" + _rowIndex, null) > 0;
  }
  
  public Cursor getAllItemsCursor() {
     try{
	 
	  return db.query(DATABASE_TABLE, 
                    new String[] {KEY_ID, KEY_HOUR,KEY_MINUTE,KEY_DAYS}, 
                    null, null, null, null, null);
     }catch(Exception e)
     {
    	 Log.e(TAG, "Error getting Cursors: "+e.toString());
    	 return null; 
     }
  }
  
   

  public Cursor setCursorToItem(long _rowIndex) throws SQLException {
    Cursor result = db.query(true, DATABASE_TABLE, 
	                           new String[] {KEY_ID, KEY_HOUR,KEY_MINUTE, KEY_DAYS},
                             KEY_ID + "=" + _rowIndex, null, null, null, 
                             null, null);
    if ((result.getCount() == 0) || !result.moveToFirst()) {
      throw new SQLException("No items found for row: " + _rowIndex);
    }
    return result;
  }

  public Alarm getItem(long _rowIndex) throws SQLException {
    Cursor cursor = db.query(true, DATABASE_TABLE, 
                             new String[] {KEY_ID, KEY_HOUR,KEY_MINUTE,KEY_DAYS},
                             KEY_ID + "=" + _rowIndex, null, null, null, 
                             null, null);
    if ((cursor.getCount() == 0) || !cursor.moveToFirst()) {
      throw new SQLException("No item found for row: " + _rowIndex);
    }
	  
    //Build the result
    Alarm result = new Alarm();
    result.setHour(cursor.getInt(cursor.getColumnIndex(KEY_HOUR)));
    result.setMinute(cursor.getInt(cursor.getColumnIndex(KEY_MINUTE)));
    result.setDaysString(cursor.getString(cursor.getColumnIndex(KEY_DAYS)));
    result.setId(cursor.getLong(cursor.getColumnIndex(KEY_ID)));
    
    //Debugging
    Log.d(TAG," Out of the database: "+cursor.getLong(cursor.getColumnIndex(KEY_ID)));
    Log.d(TAG," Out of the object: "+result.getId());
    return result;  
  }
   
  //Helper class for working with SQLite
  private static class DBOpenHelper extends SQLiteOpenHelper {

	  public DBOpenHelper(Context context, String name,
	                          CursorFactory factory, int version) {
	    super(context, name, factory, version);
	  }

	  // SQL Statement to create a new database.
	  private static final String DATABASE_CREATE = "create table " + 
	    DATABASE_TABLE + " (" + 
	    KEY_ID + " integer primary key autoincrement, " +
	    KEY_HOUR + " integer not null," + 
	    KEY_MINUTE + " integer not null," +
	    KEY_DAYS + " text not null);";

	  @Override
	  public void onCreate(SQLiteDatabase _db) {
	    _db.execSQL(DATABASE_CREATE);	       
	  }

	  @Override
	  public void onUpgrade(SQLiteDatabase _db, int _oldVersion, int _newVersion) {
	    Log.w(TAG, "Upgrading from version " + 
	                           _oldVersion + " to " +
	                           _newVersion + ", which will destroy all old data");

	    // Drop the old table.
	    _db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
	    // Create a new one.
	    onCreate(_db);
	  }
	}
}