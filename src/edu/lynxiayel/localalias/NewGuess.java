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
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class NewGuess extends Activity {
	private final String HOST_UPDATESCORE = "http://localalias.sourceforge.net/updatescore.php";
	private final String HOST_ADDFRIEND = "http://localalias.sourceforge.net/addfriend.php";
	private Context ctxt;
	private EditText alias;
	private TextView locname;
	private Button cancel;
	private Button submit;
	private TextView author;
	private Intent intent;
	private final String TAG = "NewGuess";
	private String knownAlias;
	private Builder builder;
	private ProgressDialog proDialog;

	// private Dialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.newguess);
		ctxt = this;
		builder = new AlertDialog.Builder(this);
		// dialog=new Dialog(ctxt);
		// dialog.setContentView(getLayoutInflater().inflate(R.layout.coinlayout,
		// null));
		// dialog.setCancelable(true);
		alias = (EditText) findViewById(R.id.newguess);
		locname = (TextView) findViewById(R.id.guesslocname);
		author = (TextView) findViewById(R.id.author);
		cancel = (Button) findViewById(R.id.guesscancel);
		submit = (Button) findViewById(R.id.guesssubmit);
		intent = getIntent();
		locname.setText(intent.getStringExtra("locname"));
		author.setText(intent.getStringExtra("aliasusername"));
		knownAlias = intent.getStringExtra("alias");
		cancel.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				finish();
			}
		});
		submit.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				// new AddAliasATask().execute();
				UpdateScoreTask uTask = new UpdateScoreTask();
				Log.i(TAG, knownAlias.toLowerCase() + "  "
						+ alias.getText().toString().toLowerCase());
				if (knownAlias.toLowerCase().contains(
						";" + alias.getText().toString().toLowerCase() + ";")) {
					// TODO: update the score here
					uTask.execute(5);
				} else {
					uTask.execute(-1);
				}
			}
		});

		author.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (author.getText().equals(intent.getStringExtra("username"))) {
					Toast.makeText(
							ctxt,
							"Are you kidding? Of course you are already friend with yourself!",
							Toast.LENGTH_LONG).show();
				} else {
					builder.setTitle("Add Friend")
							.setMessage(
									"Are you sure to add " + author.getText()
											+ " as your new friend?")
							.setPositiveButton("Yes",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											// TODO Auto-generated method stub
											new AddFriendTask().execute(author
													.getText().toString());

										}
									})
							.setNegativeButton("No",
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											// TODO Auto-generated method stub
											dialog.cancel();
										}
									}).show();

				}
			}
		});

	}

	public class UpdateScoreTask extends AsyncTask<Integer, Void, Integer> {

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			showWaiting();
		}

		@Override
		protected Integer doInBackground(Integer... arg) {
			// TODO Auto-generated method stub
			double longitude = intent.getDoubleExtra("longitude", 0);
			double latitude = intent.getDoubleExtra("latitude", 0);
			// String aliasString=alias.getText().toString();
			if (arg[0] < 0)
				return 3;
			String un = intent.getStringExtra("username");
			JSONObject m = new JSONObject();
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(HOST_UPDATESCORE);
			HttpResponse response;
			try {
				m.put("username", un);
				m.put("newpoints", arg[0]);
				m.put("longitude", longitude);
				m.put("latitude", latitude);
				// m.put("alias", aliasString);
				Log.i(TAG, "string to server " + m.toString());
				StringEntity se = new StringEntity(m.toString());
				post.setEntity(se);
				response = client.execute(post);
				String resStr = EntityUtils.toString(response.getEntity());
				Log.i(TAG, "guess response: " + resStr + "");
				if (resStr.equals("truetrue")) {// right guess, get 5 coin
					return 1;
				} else if (resStr.equals("false")) {// guessed, no coin
					return 2;
				} else
					// wrong guess
					return 3;
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
			return 3;
		}

		@Override
		protected void onPostExecute(Integer result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			switch (result) {
			case 1:
				dismissWaiting();
				Toast.makeText(getApplication(),
						"You got it! You just get 5 coins!", Toast.LENGTH_SHORT)
						.show();
				// dialog.show();
				finish();
				break;
			case 2:
				dismissWaiting();
				Toast.makeText(
						getApplication(),
						"Sorry, you already guessed about this one, try refresh for another location!",
						Toast.LENGTH_LONG).show();
				break;
			case 3:
				dismissWaiting();
				Toast.makeText(getApplication(), "Sorry, you are not right!",
						Toast.LENGTH_SHORT).show();
				break;
			default:
				dismissWaiting();
				break;
			}

		}

	}

	public class AddFriendTask extends AsyncTask<String, Void, Integer> {
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			showWaiting();
		}

		@Override
		protected Integer doInBackground(String... arg) {
			// TODO Auto-generated method stub
			String friend = arg[0];
			String username = intent.getStringExtra("username");
			JSONObject m = new JSONObject();
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(HOST_ADDFRIEND);
			HttpResponse response;
			try {
				m.put("username", username);
				m.put("friend", friend);
				Log.i(TAG, "string to server " + m.toString());
				StringEntity se = new StringEntity(m.toString());
				post.setEntity(se);
				response = client.execute(post);
				String resStr = EntityUtils.toString(response.getEntity());
				Log.i(TAG, "guess response: " + resStr + "");
				if (resStr.equals("true")) {// add successfully
					return 1;
				} else if (resStr.equals("false")) {// already friends
					return 2;
				} else
					// wrong guess
					return 3;
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
			return 3;
		}

		@Override
		protected void onPostExecute(Integer result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			switch (result) {
			case 1:
				Toast.makeText(getApplication(), "Success!", Toast.LENGTH_SHORT)
						.show();
				dismissWaiting();
				finish();
				break;
			case 2:
				Toast.makeText(getApplication(),
						"Sorry, you guys are already friends!",
						Toast.LENGTH_LONG).show();
				dismissWaiting();
				break;
			case 3:
				Toast.makeText(getApplication(),
						"Sorry, something is wrong with the network!",
						Toast.LENGTH_SHORT).show();
				dismissWaiting();
				break;
			default:
				dismissWaiting();
				break;
			}

		}

	}

	public void showWaiting() {
		proDialog = new ProgressDialog(NewGuess.this);
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
