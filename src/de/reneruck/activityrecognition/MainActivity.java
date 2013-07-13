package de.reneruck.activityrecognition;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

public class MainActivity extends FragmentActivity implements ConnectionCallbacks, OnConnectionFailedListener {

	public static final int MILLISECONDS_PER_SECOND = 1000;
	public static final int DETECTION_INTERVAL_SECONDS = 20;
	public static final int DETECTION_INTERVAL_MILLISECONDS = MILLISECONDS_PER_SECOND * DETECTION_INTERVAL_SECONDS;

	private PendingIntent mActivityRecognitionPendingIntent;
	private ActivityRecognitionClient mActivityRecognitionClient;
	private Context mContext;
	private boolean mInProgress;
	private TextView mCurrentStatusField;
	private TextView mCurrentStatusConfidence;
	private boolean mReceiverRegistered;
	private Button mUpdateButton;

	/*
	 * Define a request code to send to Google Play services This code is
	 * returned in Activity.onActivityResult
	 */
	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_main);
		mContext = getApplicationContext();
		mInProgress = false;

		mActivityRecognitionClient = new ActivityRecognitionClient(mContext, this, this);
		Intent intent = new Intent(mContext, ActivityRecognitionIntentService.class);
		mActivityRecognitionPendingIntent = PendingIntent.getService(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		mUpdateButton = (Button) findViewById(R.id.button_updates);
		mUpdateButton.setOnClickListener(mUdateButtonClickListener);
		mCurrentStatusField = (TextView) findViewById(R.id.current_status);
		mCurrentStatusConfidence = (TextView) findViewById(R.id.current_status_conf);
		
		registerReceiver(mUpdateReceiver, new IntentFilter("de.reneruck.activityrecognition.UPDATE"));
		mReceiverRegistered = true;
	}

	private BroadcastReceiver mUpdateReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			DetectedActivity result = intent.getParcelableExtra(ActivityRecognitionIntentService.RECOGNITION_RESULT);
			String result_name = intent.getStringExtra(ActivityRecognitionIntentService.RECOGNITION_RESULT_NAME);
			if(!mCurrentStatusField.getText().equals(result_name)) {
				mCurrentStatusField.setText(result_name);
				mCurrentStatusConfidence.setText(result.getConfidence() + "%");
			}
		}
	};
	
	OnClickListener mUdateButtonClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			startUpdates();
		}
	};

	public void startUpdates() {
		if (!servicesConnected()) {
			return;
		}
		if (!mInProgress) {
			mInProgress = true;
			mUpdateButton.setEnabled(false);
			mActivityRecognitionClient.connect();
		} else {
			Toast.makeText(this, "Already a request in progess", Toast.LENGTH_SHORT).show();
		}
	}

	/*
	 * Handle results returned to the FragmentActivity by Google Play services
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (requestCode) {
			case CONNECTION_FAILURE_RESOLUTION_REQUEST:
				switch (resultCode) {
				case Activity.RESULT_OK:
					/*
					 * Try the request again
					 */
					break;
				}
			}
	}

	private boolean servicesConnected() {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		
		if (ConnectionResult.SUCCESS == resultCode) {
			Log.d("Activity Recognition", "Google Play services is available.");
			return true;
		} else {
			Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, CONNECTION_FAILURE_RESOLUTION_REQUEST);

			if (errorDialog != null) {
				ErrorDialogFragment errorFragment = new ErrorDialogFragment();
				errorFragment.setDialog(errorDialog);
				errorFragment.show(getFragmentManager(), "Activity Recognition");
			}
			return false;
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
        mInProgress = false;
        Toast.makeText(mContext, "CONNECTION FAILED!!", Toast.LENGTH_LONG).show();
	}

	@Override
	public void onConnected(Bundle arg0) {
		mActivityRecognitionClient.requestActivityUpdates(DETECTION_INTERVAL_MILLISECONDS, mActivityRecognitionPendingIntent);
		mInProgress = false;
		mUpdateButton.setEnabled(true);
		mActivityRecognitionClient.disconnect();
	}

	@Override
	public void onDisconnected() {
		mInProgress = false;
		mUpdateButton.setEnabled(true);
        mActivityRecognitionClient = null;
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if(mReceiverRegistered) {
			unregisterReceiver(mUpdateReceiver);
			mReceiverRegistered = false;
		}
	}
	
	public static class ErrorDialogFragment extends DialogFragment {
		
		private Dialog mDialog;

		public ErrorDialogFragment() {
			super();
			mDialog = null;
		}

		public void setDialog(Dialog dialog) {
			mDialog = dialog;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return mDialog;
		}
	}
}
