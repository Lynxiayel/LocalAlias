package edu.lynxiayel.localalias;

import android.app.Activity;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class About extends Activity {
	TextView version;
	TextView copyright;
	Button ok;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		version=(TextView)findViewById(R.id.version);
		ok=(Button) findViewById(R.id.about_ok);
		copyright=(TextView)findViewById(R.id.about_right);
		copyright.setText("Developed by Lynxiayel.\n"+"All rights reserved.");
		try {
			version.setText("Version: "+this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ok.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				finish();				
			}
			
		});
	}

}
