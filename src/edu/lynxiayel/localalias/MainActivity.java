package edu.lynxiayel.localalias;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.janrain.android.engage.JREngage;
import com.janrain.android.engage.JREngageDelegate;
import com.janrain.android.engage.types.JRDictionary;

import edu.lynxiayel.localalias.NewGuess.AddFriendTask;

public class MainActivity extends Activity {
	private final String NEAR_HOST = "http://localalias.sourceforge.net/near.php";
	private final String LUCKY_HOST = "http://localalias.sourceforge.net/lucky.php";
	private final String FRIEND_HOST = "http://localalias.sourceforge.net/friend.php";
	private final String HOST_GETSCORE = "http://localalias.sourceforge.net/getscore.php";
	private final String HOST_GETBOARD = "http://localalias.sourceforge.net/getboard.php";
	private final String HOST_ADDFRIEND = "http://localalias.sourceforge.net/addfriend.php";
	public static Context ctxt;
	private final static String TAG = "MAIN";
	private static EditText keywords;
	private static Button srcBtn;
	private static RadioGroup radio;
	private static RadioButton near;
	private static RadioButton fromFriend;
	private static RadioButton lucky;
	private static Button editpw;
	private static TextView coinCount;
	private static TextView profileUN;
	private LocationManager lm;
	private LocationListener ll;
	private static double currentLongtitude;
	private static double currentLatitude;
	private static GoogleMap map_add = null;
	private static GoogleMap map_guess = null;
	private Fragment addAliasFragment;
	private Fragment guessFragment;
	private Fragment boardFragment;
	private Fragment profileFragment;
	private static CameraPosition map_add_pos = null;
	private static CameraPosition map_guess_pos = null;
	private static OnClickListener findClickListener;
	private static OnClickListener editpwOnClickListener;
	private static OnMapLongClickListener addMapListener;
	private static OnMapClickListener addMapSingleClickListener;
	private static OnInfoWindowClickListener addInfoWindowClickListener;
	private static OnInfoWindowClickListener guessInfoWindowClickListener;
	private static OnCameraChangeListener addCameraChangeListener;
	private static String un;
	private static OnCheckedChangeListener radioCheckedChangeListener;
	private static OnClickListener luckyClickListener;
	private String author;
	private Map<String, String> markerAliases;
	private Map<String, String> markerUsername;
	private Map<String, String> topUserMap = null;
	private List<String> topUsers = null;
	private ScoreArrayAdapter scoreAdapter;
	private ProgressDialog proDialog;
	private Preference pref;

	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.mainlayout);
		ctxt = this;
		un = getIntent().getStringExtra("un");
		pref = new Preference();
		pref.init(ctxt);

		Log.i(TAG, "get username at start: " + un);
		// keywords = (EditText) findViewById(R.id.search);
		// srcBtn = (Button) findViewById(R.id.searchbtn);
		// ActionBar gets initiated
		ActionBar actionbar = getActionBar();
		// Tell the ActionBar we want to use Tabs.
		actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		// initiating the tabs and set text to it.
		ActionBar.Tab addAliasTab = actionbar.newTab().setText("Add Alias");
		ActionBar.Tab guessTab = actionbar.newTab().setText("Let's Guess");
		ActionBar.Tab boardTab = actionbar.newTab().setText("Scoreboard");
		ActionBar.Tab profileTab = actionbar.newTab().setText("Profile");

		// create the fragments we want to use for display content
		addAliasFragment = new AddAliasFragment();
		guessFragment = new GuessFragment();
		boardFragment = new BoardFragment();
		profileFragment = new ProfileFragment();

		// set the Tab listener. Now we can listen for clicks.
		addAliasTab.setTabListener(new MyTabsListener(addAliasFragment));
		guessTab.setTabListener(new MyTabsListener(guessFragment));
		boardTab.setTabListener(new MyTabsListener(boardFragment));
		profileTab.setTabListener(new MyTabsListener(profileFragment));

		// add the tabs to the actionbar
		actionbar.addTab(addAliasTab);
		actionbar.addTab(guessTab);
		actionbar.addTab(boardTab);
		actionbar.addTab(profileTab);

		// FragmentManager fm = getFragmentManager();
		// FragmentTransaction ft= fm.beginTransaction();
		// ft.add(R.id.map_add_layout,add)
		ll = new LocationListener() {
			public void onLocationChanged(Location location) {
			}

			public void onProviderDisabled(String provider) {
				// Provider被disable时触发此函数，比如GPS被关闭
			}

			public void onProviderEnabled(String provider) {
				// Provider被enable时触发此函数，比如GPS被打开
			}

			public void onStatusChanged(String provider, int status,
					Bundle extras) {
				// Provider的转态在可用、暂时不可用和无服务三个状态直接切换时触发此函数
			}
		};
		getLocation();
		// Defining button click event listener for the find button
		findClickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Getting user input location
				String location = keywords.getText().toString();

				if (location != null && !location.equals("")) {
					new SearchTask().execute(location);
				}
			}
		};
		addMapListener = new OnMapLongClickListener() {
			@Override
			public void onMapLongClick(LatLng loc) {
				// TODO Auto-generated method stub
				new AddClickTask().execute(loc);

			}
		};
		addMapSingleClickListener = new OnMapClickListener() {
			@Override
			public void onMapClick(LatLng loc) {
				// TODO Auto-generated method stub
				new AddSingleClickTask().execute(loc);
			}

		};

		addInfoWindowClickListener = new OnInfoWindowClickListener() {

			@Override
			public void onInfoWindowClick(Marker marker) {
				// TODO Auto-generated method stub
				if (marker.getTitle().equals("You are here!"))
					new AddClickTask().execute(marker.getPosition());
				else {
					Intent i = new Intent();
					i.putExtra("un", un);
					i.putExtra("locname", marker.getTitle());
					i.putExtra("longitude", marker.getPosition().longitude);
					i.putExtra("latitude", marker.getPosition().latitude);
					i.setClass(ctxt, NewAlias.class);
					i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
					startActivity(i);
				}
			}
		};
		addCameraChangeListener = new OnCameraChangeListener() {

			@Override
			public void onCameraChange(CameraPosition loc) {
				// TODO Auto-generated method stub
				if (map_add != null && map_add.getMyLocation() != null) {
					double targetla = loc.target.latitude;
					double targetlo = loc.target.longitude;
					double mylocla = map_add.getMyLocation().getLatitude();
					double myloclo = map_add.getMyLocation().getLongitude();
					double result = Math.abs(targetla - mylocla)
							+ Math.abs(targetlo - myloclo);
					if (result < 0.008)// && dist > 0.08)//monitor the click of
										// "mylocation" button
						((AddAliasFragment) addAliasFragment)
								.initMarker(loc.target);
				}
			}

		};
		radioCheckedChangeListener = new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				if (map_guess != null) {
					map_guess.clear();
					GuessATask guessATask = new GuessATask();
					switch (checkedId) {
					case R.id.near:
						guessATask.execute("near");
						break;
					case R.id.friend:
						guessATask.execute("friend");
						break;
					case R.id.luck:
						// luck has a seperated onClickListener of the
						// radiobutton
						// thus no need to process it here.
						// guessATask.execute("lucky");
						break;
					default:
						break;
					}
				}
			}

		};
		guessInfoWindowClickListener = new OnInfoWindowClickListener() {

			@Override
			public void onInfoWindowClick(Marker marker) {
				// TODO start guessActivity and do the guess thing
				Intent i = new Intent();
				i.putExtra("locname", marker.getTitle());
				i.putExtra("alias", markerAliases.get(marker.getId()));
				i.putExtra("aliasusername", markerUsername.get(marker.getId()));
				i.putExtra("longitude", marker.getPosition().longitude);
				i.putExtra("latitude", marker.getPosition().latitude);
				i.putExtra("username", un);
				i.setClass(ctxt, NewGuess.class);
				i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(i);
			}
		};

		luckyClickListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				new GuessATask().execute("lucky");
			}
		};
		editpwOnClickListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i = new Intent();
				i.putExtra("username", un);
				i.setClass(ctxt, EditPassword.class);
				i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(i);
			}

		};

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		// super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.my_options_menu, menu);
		return true;

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.action_logout:
			pref.setRemember(false);
			pref.setUsername("");
			pref.setExpired();
			finish();
			return true;
		case R.id.action_about:
			Intent in = new Intent();
			in.setClass(ctxt, About.class);
			startActivity(in);
			return true;
		case R.id.action_exit:
			map_add = null;
			map_add_pos = null;
			map_guess = null;
			map_guess_pos = null;
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);

		}
	}

	class MyTabsListener implements ActionBar.TabListener {
		public Fragment fragment;

		public MyTabsListener(Fragment fragment) {
			this.fragment = fragment;

		}

		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {
			Toast.makeText(MainActivity.ctxt, "Reselected!", Toast.LENGTH_LONG)
					.show();
		}

		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			// ft.show(fragment);
			ft.replace(R.id.fragment_container, fragment);
		}

		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			ft.remove(fragment);
			// ft.hide(fragment);
		}

	}

	public static class AddAliasFragment extends Fragment {

		private static View view;

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			// Log.i(TAG,"2");
			if (view != null) {
				ViewGroup parent = (ViewGroup) view.getParent();
				if (parent != null)
					parent.removeView(view);
			}
			try {
				view = inflater.inflate(R.layout.addlayout, container, false);
			} catch (InflateException e) {
				/* map is already there, just return view as it is */
			}
			return view;
		}

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			// TODO Auto-generated method stub
			super.onActivityCreated(savedInstanceState);
			LatLng currentLoc = new LatLng(currentLatitude, currentLongtitude);
			if (map_add_pos == null) {
				// Log.i(TAG,"1");
				map_add = getMapFragment().getMap();
				init(currentLoc);
			} else if (map_add != null) {
				// Log.i(TAG,"2");
				map_add.moveCamera(CameraUpdateFactory
						.newCameraPosition(map_add_pos));
			}
			keywords = (EditText) getView().findViewById(R.id.search);
			srcBtn = (Button) getView().findViewById(R.id.searchbtn);
			srcBtn.setOnClickListener(findClickListener);
			map_add.setOnMapLongClickListener(addMapListener);
			map_add.setOnMapClickListener(addMapSingleClickListener);
			map_add.setOnInfoWindowClickListener(addInfoWindowClickListener);
			map_add.setOnCameraChangeListener(addCameraChangeListener);
		}

		@Override
		public void onDestroy() {
			// TODO Auto-generated method stub
			super.onDestroy();
			if (map_add != null)
				map_add_pos = map_add.getCameraPosition();
			// Log.i(TAG,"a");
		}

		public MapFragment getMapFragment() {
			// Log.i(TAG,getFragmentManager().toString());
			return (MapFragment) getFragmentManager().findFragmentById(
					R.id.map_add);
		}

		public void init(LatLng loc) {
			if (map_add != null) {
				initMarker(loc);
				// Move the camera instantly to start with a zoom of 15.
				map_add.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 15));
				// Zoom in, animating the camera.
				map_add.animateCamera(CameraUpdateFactory.zoomTo(14), 2000,
						null);
			}
		}

		public void initMarker(LatLng loc) {
			if (map_add != null) {
				map_add.clear();
				map_add.setMyLocationEnabled(true);
				map_add.getUiSettings().setCompassEnabled(true);
				map_add.getUiSettings().setAllGesturesEnabled(true);
				// Marker start = map_add.addMarker(new
				// MarkerOptions().position(
				// loc).title("You are here!"));
			}
		}
	}

	public static class GuessFragment extends Fragment {
		private static View view;

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			if (view != null) {
				ViewGroup parent = (ViewGroup) view.getParent();
				if (parent != null)
					parent.removeView(view);
			}
			try {
				view = inflater.inflate(R.layout.guesslayout, container, false);
			} catch (InflateException e) {
				/* map is already there, just return view as it is */
			}
			return view;
		}

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			// TODO Auto-generated method stub
			super.onActivityCreated(savedInstanceState);
			LatLng currentLoc = new LatLng(currentLatitude, currentLongtitude);
			near = (RadioButton) getView().findViewById(R.id.near);
			fromFriend = (RadioButton) getView().findViewById(R.id.friend);
			lucky = (RadioButton) getView().findViewById(R.id.luck);
			lucky.setOnClickListener(luckyClickListener);
			radio = (RadioGroup) getView().findViewById(R.id.radio);
			radio.setOnCheckedChangeListener(radioCheckedChangeListener);
			if (map_guess_pos == null) {
				map_guess = getMapFragment().getMap();
				if (map_guess != null) {
					map_guess.setMyLocationEnabled(true);
					map_guess.getUiSettings().setCompassEnabled(true);
					map_guess.getUiSettings().setAllGesturesEnabled(true);
					// Marker start = map_guess.addMarker(new MarkerOptions()
					// .position(currentLoc).title("You are here!"));
					// Move the camera instantly to start with a zoom of 15.
					map_guess.moveCamera(CameraUpdateFactory.newLatLngZoom(
							currentLoc, 15));
					// Zoom in, animating the camera.
					map_guess.animateCamera(CameraUpdateFactory.zoomTo(14),
							2000, null);
				}
			} else if (map_guess != null) {
				// Log.i(TAG,"3");
				map_guess.moveCamera(CameraUpdateFactory
						.newCameraPosition(map_guess_pos));
			}
			map_guess
					.setOnInfoWindowClickListener(guessInfoWindowClickListener);
			near.setChecked(true);
		}

		@Override
		public void onDestroy() {
			// TODO Auto-generated method stub
			super.onDestroy();
			if (map_guess != null)
				map_guess_pos = map_guess.getCameraPosition();
			// Log.i(TAG,"a");
		}

		public MapFragment getMapFragment() {
			return (MapFragment) getFragmentManager().findFragmentById(
					R.id.map_guess);
		}
	}

	public class BoardFragment extends ListFragment {

		private View header = null;

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			// if (header==null)
			// header = View.inflate(ctxt, R.layout.boardheaderlayout, null);
			// return super.onCreateView(inflater, container,
			// savedInstanceState);
			return inflater.inflate(R.layout.boardlayout, container, false);
		}

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			// TODO Auto-generated method stub
			super.onActivityCreated(savedInstanceState);
			Log.i(TAG, "here");
			if (topUserMap == null)// not get the score board yet
			{
				Log.i(TAG, "topUserMap=null");
				topUserMap = new HashMap<String, String>();
				topUsers = new ArrayList<String>();
				new GetBoardTask().execute();
			}
			if (getListView().getAdapter() == null) {
				// getListView().addHeaderView(header);
				scoreAdapter = new ScoreArrayAdapter(ctxt, topUsers);
				setListAdapter(scoreAdapter);
			}

		}

	}

	public class ProfileFragment extends Fragment {
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			return inflater.inflate(R.layout.profilelayout, container, false);
		}

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			// TODO Auto-generated method stub
			super.onActivityCreated(savedInstanceState);
			profileUN = (TextView) getView().findViewById(R.id.profileusername);
			profileUN.setText(un);
			editpw = (Button) getView().findViewById(R.id.editpw);
			coinCount = (TextView) getView().findViewById(R.id.profilescore);
			editpw.setOnClickListener(editpwOnClickListener);
			new GetCoinTask().execute();

		}

	}

	public void getLocation() {
		lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		// lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,ll);
		// Location loc=lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, ll);
		Location loc = lm
				.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		currentLatitude = this.getIntent().getDoubleExtra("latitude",
				loc.getLatitude());
		currentLongtitude = this.getIntent().getDoubleExtra("longtitude",
				loc.getLongitude());
		Log.i(TAG, "la: " + currentLatitude + "lo: " + currentLongtitude);
	}

	// An AsyncTask class for accessing the GeoCoding Web Service
	private class SearchTask extends AsyncTask<String, Void, List<Address>> {

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			showWaiting();
		}

		@Override
		protected List<Address> doInBackground(String... locationName) {
			// Creating an instance of Geocoder class
			Geocoder geocoder = new Geocoder(getBaseContext());
			List<Address> addresses = null;

			try {
				// Getting a maximum of 3 Address that matches the input text
				addresses = geocoder.getFromLocationName(locationName[0], 3);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return addresses;
		}

		@Override
		protected void onPostExecute(List<Address> addresses) {

			LatLng latLng;
			MarkerOptions markerOptions;
			if (addresses == null || addresses.size() == 0) {
				Toast.makeText(getBaseContext(), "No Location found",
						Toast.LENGTH_SHORT).show();
				dismissWaiting();
				return;
			}

			// Clears all the existing markers on the map
			map_add.clear();

			// Adding Markers on Google Map for each matching address
			for (int i = 0; i < addresses.size(); i++) {

				Address address = (Address) addresses.get(i);

				// Creating an instance of GeoPoint, to display in Google Map
				latLng = new LatLng(address.getLatitude(),
						address.getLongitude());

				String addressText = String.format(
						"%s, %s",
						address.getMaxAddressLineIndex() > 0 ? address
								.getAddressLine(0) : "", address
								.getCountryName());

				markerOptions = new MarkerOptions();
				markerOptions.position(latLng);
				markerOptions.title(addressText);

				map_add.addMarker(markerOptions);

				// Locate the first location
				if (i == 0) {
					map_add.animateCamera(CameraUpdateFactory.newLatLng(latLng));
				}
			}
			dismissWaiting();
		}
	}

	private class AddClickTask extends AsyncTask<LatLng, Void, List<Address>> {

		private Intent intent = new Intent();

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			showWaiting();
		}

		@Override
		protected List<Address> doInBackground(LatLng... location) {
			// TODO Auto-generated method stub
			intent.putExtra("longitude", location[0].longitude);
			intent.putExtra("latitude", location[0].latitude);
			Geocoder geocoder = new Geocoder(getBaseContext());
			List<Address> addresses = null;

			try {
				// Getting a maximum of 3 Address that matches the input text
				addresses = geocoder.getFromLocation(location[0].latitude,
						location[0].longitude, 1);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return addresses;
		}

		@Override
		protected void onPostExecute(List<Address> addresses) {

			if (addresses == null || addresses.size() == 0) {
				intent.putExtra("locname", "Unkown place.");
			} else {
				Address address = addresses.get(0);
				String addressText = String.format(
						"%s, %s",
						address.getMaxAddressLineIndex() > 0 ? address
								.getAddressLine(0) : "", address
								.getCountryName());
				intent.putExtra("locname", addressText);
			}
			intent.putExtra("un", un);
			intent.setClass(ctxt, NewAlias.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivity(intent);
			dismissWaiting();
		}
	}

	private class AddSingleClickTask extends
			AsyncTask<LatLng, Void, List<Address>> {

		private Intent intent = new Intent();

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			showWaiting();
		}

		@Override
		protected List<Address> doInBackground(LatLng... location) {
			// TODO Auto-generated method stub
			intent.putExtra("longitude", location[0].longitude);
			intent.putExtra("latitude", location[0].latitude);
			Geocoder geocoder = new Geocoder(getBaseContext());
			List<Address> addresses = null;

			try {
				// Getting a maximum of 3 Address that matches the input text
				addresses = geocoder.getFromLocation(location[0].latitude,
						location[0].longitude, 1);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return addresses;
		}

		@Override
		protected void onPostExecute(List<Address> addresses) {

			LatLng latLng;
			MarkerOptions markerOptions;
			if (addresses == null || addresses.size() == 0) {
				// nothing here, just return
				dismissWaiting();
				return;
			}

			// Clears all the existing markers on the map
			map_add.clear();

			// Adding Markers on Google Map for each matching address

			Address address = (Address) addresses.get(0);

			// Creating an instance of GeoPoint, to display in Google Map
			latLng = new LatLng(address.getLatitude(), address.getLongitude());

			String addressText = String.format("%s, %s", address
					.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0)
					: "", address.getCountryName());

			markerOptions = new MarkerOptions();
			markerOptions.position(latLng);
			markerOptions.title(addressText);

			map_add.addMarker(markerOptions).showInfoWindow();

			// Locate the first location
			map_add.animateCamera(CameraUpdateFactory.newLatLng(latLng));
			dismissWaiting();
		}
	}

	private class GuessATask extends AsyncTask<String, Void, List<Address>> {
		private List<String> aliases = new ArrayList<String>();
		private List<String> users = new ArrayList<String>();

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			showWaiting();
		}

		@Override
		protected List<Address> doInBackground(String... p) {
			// TODO Auto-generated method stub
			String host = NEAR_HOST;
			if (p[0].equals("friend")) {
				host = FRIEND_HOST;
			} else if (p[0].equals("lucky")) {
				host = LUCKY_HOST;
			}
			JSONObject m = new JSONObject();
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(host);
			HttpResponse response;
			try {
				m.put("un", un);
				m.put("longitude", currentLongtitude);
				m.put("latitude", currentLatitude);
				Log.i(TAG, "string to server " + m.toString());
				StringEntity se = new StringEntity(m.toString());
				post.setEntity(se);
				response = client.execute(post);
				String resStr = EntityUtils.toString(response.getEntity());
				Log.i(TAG, "guess response: " + resStr + "");
				JSONArray result = new JSONArray(resStr);
				int len = result.length();
				Geocoder geocoder = new Geocoder(getBaseContext());
				List<Address> addresses = new ArrayList<Address>();
				for (int i = 0; i < len; i++) {
					JSONObject item = result.getJSONObject(i);
					List<Address> resultAddress = geocoder.getFromLocation(
							item.getDouble("latitude"),
							item.getDouble("longitude"), 1);
					if (resultAddress != null && resultAddress.size() != 0) {
						addresses.add(resultAddress.get(0));
						aliases.add(item.getString("alias"));
						users.add(item.getString("username"));
					}
				}
				return addresses;

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
			return null;
		}

		@Override
		protected void onPostExecute(List<Address> addresses) {
			// TODO Auto-generated method stub
			super.onPostExecute(addresses);
			markerAliases = new HashMap<String, String>();
			markerUsername = new HashMap<String, String>();
			LatLng latLng = null;
			List<LatLng> points = new ArrayList<LatLng>();// to store all marked
															// points and help
															// fix the zoom
															// level of the map
			MarkerOptions markerOptions;
			if (addresses == null || addresses.size() == 0) {
				// nothing here, just return
				dismissWaiting();
				return;
			}

			// Clears all the existing markers on the map
			map_guess.clear();

			// Adding Markers on Google Map for each matching address
			Address address;
			// Log.i(TAG,aliases.toString());
			// Log.i(TAG,users.toString());
			for (int i = 0; i < addresses.size(); i++) {
				address = (Address) addresses.get(i);

				// Creating an instance of GeoPoint, to display in Google Map
				latLng = new LatLng(address.getLatitude(),
						address.getLongitude());
				points.add(latLng);
				String addressText = String.format(
						"%s, %s",
						address.getMaxAddressLineIndex() > 0 ? address
								.getAddressLine(0) : "", address
								.getCountryName());

				markerOptions = new MarkerOptions();
				markerOptions.position(latLng);
				markerOptions.title(addressText);
				Marker m = map_guess.addMarker(markerOptions);
				// Log.i(TAG,(markerAliases==null)+"");
				markerAliases.put(m.getId(), aliases.get(i));
				markerUsername.put(m.getId(), users.get(i));
			}
			// zoom to level of including all marked points
			LatLngBounds.Builder bc = new LatLngBounds.Builder();
			for (LatLng item : points) {
				bc.include(item);
			}
			if (latLng != null)
				// map_guess.animateCamera(CameraUpdateFactory.newLatLng(latLng));
				map_guess.moveCamera(CameraUpdateFactory.newLatLngBounds(
						bc.build(), 50));
			dismissWaiting();
		}
	}

	private class GetCoinTask extends AsyncTask<Void, Void, Integer> {

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			showWaiting();
		}

		@Override
		protected Integer doInBackground(Void... params) {
			// TODO Auto-generated method stub
			JSONObject je = new JSONObject();
			try {
				je.put("username", un);
				HttpClient e = new DefaultHttpClient();
				HttpResponse response;
				HttpPost post = new HttpPost(HOST_GETSCORE);
				StringEntity se = new StringEntity(je.toString());
				post.setEntity(se);
				response = e.execute(post);
				String resStr = EntityUtils.toString(response.getEntity());
				Log.i("response", resStr);
				if (resStr.equals(""))
					return 0;
				else
					return Integer.valueOf(resStr);
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
			return 0;
		}

		@Override
		protected void onPostExecute(Integer result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			coinCount.setText(result + "");
			dismissWaiting();
		}

	}

	public class ScoreArrayAdapter extends ArrayAdapter<String> {

		private final Context context;
		private final List<String> topUsers;

		public ScoreArrayAdapter(Context context, List<String> topUsers) {

			super(context, R.layout.boarditemlayout, topUsers);
			// TODO Auto-generated constructor stub
			this.context = context;
			this.topUsers = topUsers;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowView = inflater.inflate(R.layout.boarditemlayout, parent,
					false);
			TextView user = (TextView) rowView.findViewById(R.id.scoreuser);
			TextView score = (TextView) rowView.findViewById(R.id.scorescore);
			String name = topUsers.get(position);

			if (name.equals(un)) {
				user.setText("You");
				user.setTextAppearance(context, Typeface.BOLD_ITALIC);
				user.setTextColor(Color.YELLOW);
				score.setTextAppearance(context, Typeface.BOLD_ITALIC);
				score.setTextColor(Color.YELLOW);
			} else
				user.setText(name);
			score.setText(topUserMap.get(name));
			user.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					author=((TextView)v).getText().toString();
					if (author.equals("You")) {
						Toast.makeText(
								ctxt,
								"Are you kidding? Of course you are already friend with yourself!",
								Toast.LENGTH_LONG).show();
					} else {
						Builder builder=new AlertDialog.Builder(ctxt);
						builder.setTitle("Add Friend")
								.setMessage(
										"Are you sure to add "
												+ author
												+ " as your new friend?")
								.setPositiveButton("Yes",
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												// TODO Auto-generated method
												// stub
												new AddFriendTask()
														.execute(author);
											}
										})
								.setNegativeButton("No",
										new DialogInterface.OnClickListener() {

											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												// TODO Auto-generated method
												// stub
												dialog.cancel();
											}
										}).show();

					}
				}
			});
			return rowView;

		}

	}

	private class GetBoardTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			showWaiting();
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			// TODO Auto-generated method stub
			JSONObject je = new JSONObject();
			try {
				je.put("username", un);
				HttpClient e = new DefaultHttpClient();
				HttpResponse response;
				HttpPost post = new HttpPost(HOST_GETBOARD);
				StringEntity se = new StringEntity(je.toString());
				post.setEntity(se);
				response = e.execute(post);
				String resStr = EntityUtils.toString(response.getEntity());
				Log.i("response", resStr);
				JSONArray result = new JSONArray(resStr);
				if (result != null)
					;
				for (int i = 0; i < result.length(); i++) {
					JSONObject tmp = result.getJSONObject(i);
					topUsers.add(tmp.getString("username"));
					topUserMap.put(tmp.getString("username"),
							tmp.getString("score"));
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
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			scoreAdapter.notifyDataSetChanged();
			dismissWaiting();
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
			String username = un;
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
		proDialog = new ProgressDialog(MainActivity.this);
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
