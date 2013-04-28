package edu.lynxiayel.localalias;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class Popcoin extends Activity {
	 @Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.coinlayout);
	        Intent i=getIntent();
	        
	    }

}