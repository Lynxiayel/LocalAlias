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
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
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

import com.janrain.android.engage.JREngage;
import com.janrain.android.engage.JREngageDelegate;
import com.janrain.android.engage.JREngageError;
import com.janrain.android.engage.net.async.HttpResponseHeaders;
import com.janrain.android.engage.types.JRActivityObject;
import com.janrain.android.engage.types.JRDictionary;

public class LoginActivity extends Activity {
	private final String TAG = "loginActivity";
	private Button login;
	private Button register;
	private Button otherLogin;
	private EditText username;
	private EditText password;
	private CheckBox remember;
	final private String HOST_LOGIN = "http://localalias.sourceforge.net/localaliaslogin.php";
	final private String HOST_REGISTER = "http://localalias.sourceforge.net/localaliasregister.php";
	private Builder dialog;
	private Preference pref;
	private ProgressDialog proDialog;
	private JREngage mEngage;
	private JREngageDelegate mEngageDelegate;
	private static final String ENGAGE_APP_ID = "kboiilgjkodpifganmlj";
	private static final String ENGAGE_TOKEN_URL = "";
	private Context ctxt;
	private String nameByJanrain;

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		ctxt = this.getApplicationContext();
		Intent intent = new Intent();
		pref = new Preference();
		pref.init(this);
		if (pref.getSettings().getBoolean("remember", false)
				|| !pref.isExpired()) {
			intent.putExtra("un", pref.getSettings()
					.getString("username", null));
			intent.setClass(LoginActivity.this, MainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			this.finish();
			return;
		}
		setContentView(R.layout.loginlayout);
		login = (Button) findViewById(R.id.loginbtn);
		register = (Button) findViewById(R.id.regbtn);
		otherLogin = (Button) findViewById(R.id.otherlogin);
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

		mEngageDelegate = new JREngageDelegate() {

			@Override
			public void jrAuthenticationDidSucceedForUser(
					JRDictionary auth_info, String provider) {
				// TODO Auto-generated method stub
				JRDictionary profile = auth_info.getAsDictionary("profile");
				nameByJanrain = profile.getAsString("displayName");
				new RegisterTask().execute();
			}

			@Override
			public void jrAuthenticationDidReachTokenUrl(String tokenUrl,
					HttpResponseHeaders response, String tokenUrlPayload,
					String provider) {
				// TODO Auto-generated method stub

			}

			@Override
			public void jrSocialDidPublishJRActivity(JRActivityObject activity,
					String provider) {
				// TODO Auto-generated method stub

			}

			@Override
			public void jrSocialDidCompletePublishing() {
				// TODO Auto-generated method stub

			}

			@Override
			public void jrEngageDialogDidFailToShowWithError(JREngageError error) {
				// TODO Auto-generated method stub

			}

			@Override
			public void jrAuthenticationDidNotComplete() {
				// TODO Auto-generated method stub

			}

			@Override
			public void jrAuthenticationDidFailWithError(JREngageError error,
					String provider) {
				// TODO Auto-generated method stub

			}

			@Override
			public void jrAuthenticationCallToTokenUrlDidFail(String tokenUrl,
					JREngageError error, String provider) {
				// TODO Auto-generated method stub

			}

			@Override
			public void jrSocialDidNotCompletePublishing() {
				// TODO Auto-generated method stub

			}

			@Override
			public void jrSocialPublishJRActivityDidFail(
					JRActivityObject activity, JREngageError error,
					String provider) {
				// TODO Auto-generated method stub

			}

		};
		mEngage = JREngage.initInstance(this, ENGAGE_APP_ID, ENGAGE_TOKEN_URL,
				mEngageDelegate);
		otherLogin.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mEngage.showAuthenticationDialog();
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
						i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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

	private class RegisterTask extends AsyncTask<Void, Void, Integer> {

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			// showWaiting();
		}

		@Override
		protected Integer doInBackground(Void... arg) {

			JSONObject je = new JSONObject();
			try {
				je.put("username", nameByJanrain);
				je.put("password", "");
				HttpClient e = new DefaultHttpClient();
				HttpResponse response;
				HttpPost post = new HttpPost(HOST_REGISTER);
				StringEntity se = new StringEntity(je.toString());
				post.setEntity(se);
				response = e.execute(post);
				String resStr = EntityUtils.toString(response.getEntity());
				Log.i("response", resStr);
				if (resStr.equals("2")) {
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

			// }
			return 0;
		}

		@Override
		protected void onPostExecute(Integer result) {
			Intent i = new Intent();
			pref.setUsername(nameByJanrain);
			i.putExtra("un", nameByJanrain);
			i.setClass(LoginActivity.this, MainActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);
			// dismissWaiting();
			finish();
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
