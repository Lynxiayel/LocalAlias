package edu.lynxiayel.localalias;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivity extends Activity {
	private EditText password;
	private EditText passwordConf;
	private EditText username;
	private Button submit;
	private Button cancel;
	final private String HOST_REGISTER = "http://localalias.sourceforge.net/localaliasregister.php";
	private Preference pref;
	final private String TAG = "register";
	private ProgressDialog proDialog;

	public void onCreate(Bundle b) {
		super.onCreate(b);
		setContentView(R.layout.registerlayout);
		pref = new Preference();
		pref.init(this);
		username = (EditText) findViewById(R.id.regun);
		password = (EditText) findViewById(R.id.regpw);
		passwordConf = (EditText) findViewById(R.id.regpwconfirm);
		submit = (Button) findViewById(R.id.regsubmit);
		cancel = (Button) findViewById(R.id.regcancel);

		submit.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				new RegisterTask().execute();
			}
		});
		cancel.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				Intent i = new Intent();
				i.setClass(getBaseContext(), LoginActivity.class);
				startActivity(i);
				finish();
			}
		});
	}

	private class RegisterTask extends AsyncTask<Void, Void, Integer> {

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			showWaiting();
		}

		@Override
		protected Integer doInBackground(Void... arg) {

			if (passwordConf.getText().toString()
					.equals(password.getText().toString())) {
				Log.i("pressed submit", passwordConf.getText() + "  "
						+ password.getText());
				JSONObject je = new JSONObject();
				try {
					je.put("username", username.getText());
					je.put("password", password.getText());
					HttpClient e = new DefaultHttpClient();
					HttpResponse response;
					HttpPost post = new HttpPost(HOST_REGISTER);
					StringEntity se = new StringEntity(je.toString());
					post.setEntity(se);
					response = e.execute(post);
					String resStr = EntityUtils.toString(response.getEntity());
					Log.i("response", resStr);
					if (resStr.equals("truetrue")) {
						return 2;
					} else {
						return 1;
					}

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				return 3;
			}
			// }
			return 0;
		}

		@Override
		protected void onPostExecute(Integer result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if (result == 0)
				return;
			else {
				switch (result) {
				case 1:
					Toast.makeText(
							getBaseContext(),
							"Sorry, the name has already been taken, please try another one.",
							Toast.LENGTH_LONG).show();
					username.setText("");
					dismissWaiting();
					break;
				case 2:
					Toast.makeText(getBaseContext(), "Register successfully!",
							Toast.LENGTH_SHORT).show();
					pref.setRemember(false);
					pref.setUsername(username.getText().toString());
					Intent i = new Intent();
					i.putExtra("un", username.getText().toString());
					i.setClass(RegisterActivity.this, MainActivity.class);
					i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(i);
					dismissWaiting();
					finish();
					break;
				case 3:
					Toast.makeText(
							getBaseContext(),
							"Sorry, the passwords you typed in are not consistent, please try again",
							Toast.LENGTH_LONG).show();
					password.setText("");
					passwordConf.setText("");
					dismissWaiting();
					break;
				default:
					dismissWaiting();
					break;
				}
			}

		}
	}

	public void showWaiting(){
	    proDialog = new ProgressDialog(RegisterActivity.this);
        proDialog.setMessage("Loading...");
        proDialog.setIndeterminate(false);
        proDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        proDialog.setCancelable(true);
        proDialog.show();
	}
	public void dismissWaiting(){
		if(proDialog.isShowing())
			proDialog.dismiss();
	}
}
