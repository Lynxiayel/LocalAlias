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
import android.app.ProgressDialog;
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

public class NewAlias extends Activity {
	final private String HOST_ADDALIAS = "http://localalias.sourceforge.net/addalias.php";
	private EditText alias;
	private TextView locname;
	private Button cancel;
	private Button submit;
	private Intent intent;
	private final String TAG = "NewAlias";
	private ProgressDialog proDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.newalias);
		alias = (EditText) findViewById(R.id.newalias);
		locname = (TextView) findViewById(R.id.locname);
		cancel = (Button) findViewById(R.id.aliascancel);
		submit = (Button) findViewById(R.id.aliassubmit);
		intent = getIntent();
		locname.setText(intent.getStringExtra("locname"));
		cancel.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				NewAlias.this.finish();
			}
		});
		submit.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				new AddAliasATask().execute();
			}
		});

	}

	public class AddAliasATask extends AsyncTask<Void, Void, Integer> {

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			showWaiting();
		}

		@Override
		protected Integer doInBackground(Void... arg) {
			// TODO Auto-generated method stub
			double longitude = intent.getDoubleExtra("longitude", 0);
			double latitude = intent.getDoubleExtra("latitude", 0);
			String aliasString = alias.getText().toString();
			String un = intent.getStringExtra("un");
			JSONObject m = new JSONObject();
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(HOST_ADDALIAS);
			HttpResponse response;
			try {
				m.put("un", un);
				m.put("longitude", longitude);
				m.put("latitude", latitude);
				m.put("alias", aliasString);
				Log.i(TAG, "string to server " + m.toString());
				StringEntity se = new StringEntity(m.toString());
				post.setEntity(se);
				response = client.execute(post);
				String resStr = EntityUtils.toString(response.getEntity());
				Log.i(TAG, "add alias response: " + resStr + "");
				if (resStr.equals("3")) {
					return 3;
				} else if (resStr.equals("2")) {
					return 2;
				} else
					return 1;
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
			return 1;
		}

		@Override
		protected void onPostExecute(Integer result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			switch (result) {
			case 1:
				Toast.makeText(getApplication(),
						"Failed to add, please try again.", Toast.LENGTH_SHORT)
						.show();
				dismissWaiting();
				break;
			case 2:
				Toast.makeText(getApplication(), "New alias added, and you get 1 coin reward!",
						Toast.LENGTH_SHORT).show();
				dismissWaiting();
				finish();
				break;
			case 3:
				Toast.makeText(
						getApplication(),
						"This alias is already added. Come on, show me something new!",
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
		proDialog = new ProgressDialog(NewAlias.this);
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
