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
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class EditPassword extends Activity {
	final private String HOST_EDITPASSWORD = "http://lyntwip.sourceforge.net/localalias/editpassword.php";
	private EditText password;
	private EditText passwordConf;
	private String username;
	private Button submit;
	private Button cancel;
	private Intent intent;
	private final String TAG = "EditPassword";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.editpwlayout);
		password = (EditText) findViewById(R.id.editpw);
		passwordConf = (EditText) findViewById(R.id.editpwconfirm);
		cancel = (Button) findViewById(R.id.editpwcancel);
		submit = (Button) findViewById(R.id.editpwsubmit);
		intent = getIntent();
		username = intent.getStringExtra("username");
		cancel.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				finish();
			}
		});
		submit.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				// new AddAliasATask().execute();
				new UpdatePasswordTask().execute();
			}
		});

	}

	public class UpdatePasswordTask extends AsyncTask<Void, Void, Integer> {

		@Override
		protected Integer doInBackground(Void... arg) {
			// TODO Auto-generated method stub
			if (passwordConf.getText().toString()
					.equals(password.getText().toString())) {
				Log.i("pressed submit", passwordConf.getText() + "  "
						+ password.getText());
				JSONObject je = new JSONObject();
				try {
					je.put("username", username);
					je.put("password", password.getText());
					HttpClient e = new DefaultHttpClient();
					HttpResponse response;
					HttpPost post = new HttpPost(HOST_EDITPASSWORD);
					StringEntity se = new StringEntity(je.toString());
					post.setEntity(se);
					response = e.execute(post);
					String resStr = EntityUtils.toString(response.getEntity());
					Log.i("response", resStr);
					if (resStr.equals("1")) {
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
			return 0;
		}

		@Override
		protected void onPostExecute(Integer result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if (result == 0)
				return;
			switch (result) {
			case 1:
				Toast.makeText(
						getBaseContext(),
						"Something wrong happened when trying to update the password, please try again later.",
						3).show();
				finish();
				break;
			case 2:
				Toast.makeText(getBaseContext(), "New password updated!", 2).show();
				finish();
				break;
			case 3:
				Toast.makeText(getBaseContext(), "Sorry, the passwords you typed in are not consistent, please try again", 3).show();
				password.setText("");
				passwordConf.setText("");
				break;
			default:
				break;
			}
		}

	}

}

// Handler myUpdateHandler = new Handler() {
// public void handleMessage(Message msg) {
// switch(msg.what){
// case 1:
// dialog.setMessage("Sorry, the name has already been taken, please try another one.")
// .setPositiveButton("OK", new DialogInterface.OnClickListener() {
// public void onClick(DialogInterface dialog, int id) {
// username.setText("");
// dialog.cancel();
// }
// });
// break;
// case 2:
// dialog.setMessage("Register successfully!")
// .setCancelable(false)
// .setPositiveButton("OK", new DialogInterface.OnClickListener() {
// public void onClick(DialogInterface dialog, int id) {
// dialog.cancel();
// pref.setRemember(false);
// pref.setUsername(username.getText().toString());
// Intent i=new Intent();
// i.putExtra("un",username.getText().toString());
// i.setClass(RegisterActivity.this, MainActivity.class);
// startActivity(i);
// finish();
// }
// }).show();
// break;
// case 3:
// dialog.setMessage("Sorry, the passwords you typed in are not consistent, please try again")
// .setCancelable(false)
// .setPositiveButton("OK", new DialogInterface.OnClickListener() {
// public void onClick(DialogInterface dialog, int id) {
// password.setText("");
// passwordConf.setText("");
// dialog.cancel();
// }
// }).show();
// break;
// default:
// break;
// }
// }
// };

