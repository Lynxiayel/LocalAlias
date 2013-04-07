package edu.lynxiayel.localalias;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;

public class Preference {
	private SharedPreferences mSettings = null;
	private SharedPreferences.Editor mEditor = null;
	private final String PREFS_NAME = "localalias";
	

	public void init(Context context) {
		mSettings = context.getSharedPreferences(PREFS_NAME, 0);
		mEditor = mSettings.edit();
	}

	public void setRemember(boolean r) {
		mEditor.putBoolean("remember", r);
		mEditor.commit();
	}

	public void setUsername(String u) {
		mEditor.putString("username", u);
		renewExpire();
		mEditor.commit();
	}
	
	public void renewExpire(){
		mEditor.putLong("expire", System.currentTimeMillis()+2*3600000);
	}
	
	public void setExpired()
	{
		mEditor.putLong("expire", System.currentTimeMillis()-10);
		mEditor.commit();
	}
	public boolean isExpired(){
		return System.currentTimeMillis()>mSettings.getLong("expire", 0);
	}
	
	public SharedPreferences getSettings(){
		return mSettings;
	}
	public SharedPreferences.Editor getEditor(){
		return mEditor;
	}

}
