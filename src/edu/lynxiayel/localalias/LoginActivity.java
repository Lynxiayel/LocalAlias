package edu.lynxiayel.localalias;

import java.io.IOException;

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
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class LoginActivity extends Activity {
	private Button login;
	private Button register;
	private EditText username;
	private EditText password;
	private CheckBox remember;
	final private String HOST_LOGIN = "http://lyntwip.sourceforge.net/localaliaslogin.php";
	private Builder dialog;
	private Preference pref;
	private ProgressDialog proDialog;

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		Intent intent = new Intent();
		pref = new Preference();
		pref.init(this);
		if (pref.getSettings().getBoolean("remember", false)
				|| !pref.isExpired()) {
			intent.putExtra("un", pref.getSettings()
					.getString("username", null));
			intent.setClass(LoginActivity.this, MainActivity.class);
			startActivity(intent);
			this.finish();
		}
		setContentView(R.layout.loginlayout);
		login = (Button) findViewById(R.id.loginbtn);
		register = (Button) findViewById(R.id.regbtn);
		username = (EditText) findViewById(R.id.usernameedit);
		password = (EditText) findViewById(R.id.passwordedit);
		remember = (CheckBox) findViewById(R.id.rememberme);
		dialog = new AlertDialog.Builder(this);

		login.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				new LoginTask().execute("login");
			}
		});
		register.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				new LoginTask().execute("register");
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.start, menu);
		return true;
	}

	private class LoginTask extends AsyncTask<String, Void, Integer> {

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			showWaiting();
		}

		@Override
		protected Integer doInBackground(String... button) {
			// TODO Auto-generated method stub
			Button btn;
			if (button[0].equals("login"))
				btn = (Button) findViewById(R.id.loginbtn);
			else if (button[0].equals("register"))
				btn = (Button) findViewById(R.id.regbtn);
			else
				return 0;
			Intent i = new Intent();
			if (btn.getText().equals("Login")) {
				String un = username.getText().toString();
				String pw = password.getText().toString();
				JSONObject m = new JSONObject();
				HttpClient client = new DefaultHttpClient();
				HttpPost post = new HttpPost(HOST_LOGIN);
				HttpResponse response;
				try {
					m.put("username", un);
					m.put("password", pw);
					StringEntity se = new StringEntity(m.toString());
					post.setEntity(se);
					response = client.execute(post);
					String resStr = EntityUtils.toString(response.getEntity());
					Log.i("login response", resStr + "");
					if (resStr.contains("ok")) {
						pref.setRemember(remember.isChecked());
						pref.setUsername(un);
						i.putExtra("un", un);
						i.setClass(LoginActivity.this, MainActivity.class);
						startActivity(i);
						finish();
					} else {
						return 1;
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else if (btn.getText().equals("Register")) {
				i.setClass(btn.getContext(), RegisterActivity.class);
				startActivity(i);
				finish();
			}
			return 0;
		}

		@Override
		protected void onPostExecute(Integer result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if (result == 0) {
				dismissWaiting();
				return;
			}

			else {
				dismissWaiting();
				dialog.setMessage(
						"Sorry, the username or password you have typed is incorrect. Please check it up and input again.")
						.setCancelable(false)
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										dialog.cancel();
									}
								}).show();
			}
		}

	}

	public void showWaiting() {
		proDialog = new ProgressDialog(LoginActivity.this);
		proDialog.setMessage("Loading...");
		proDialog.setIndeterminate(false);
		proDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		proDialog.setCancelable(true);
		proDialog.show();
	}

	public void dismissWaiting() {
		if (proDialog.isShowing())
			proDialog.dismiss();
	}
}
