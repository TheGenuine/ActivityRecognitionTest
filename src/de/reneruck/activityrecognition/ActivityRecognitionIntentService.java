package de.reneruck.activityrecognition;

import android.app.IntentService;
import android.content.Intent;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

public class ActivityRecognitionIntentService extends IntentService {

    public static final String RECOGNITION_RESULT = "result";
	public static final String RECOGNITION_RESULT_NAME = "result_name";

	public ActivityRecognitionIntentService() {
        // Set the label for the service's background thread
        super("ActivityRecognitionIntentService");
    }

	@Override
	protected void onHandleIntent(Intent intent) {
		if (ActivityRecognitionResult.hasResult(intent)) {
			// Get the update
			ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

			DetectedActivity mostProbableActivity = result.getMostProbableActivity();
			int confidence = mostProbableActivity.getConfidence();
			int activityType = mostProbableActivity.getType();
			String activityName = getNameFromType(activityType);
			
			Intent i = new Intent("de.reneruck.activityrecognition.UPDATE");
			i.putExtra(RECOGNITION_RESULT_NAME, activityName);
			i.putExtra(RECOGNITION_RESULT, mostProbableActivity);
			sendBroadcast(i);
		} else {
			/*
			 * This implementation ignores intents that don't contain an
			 * activity update. If you wish, you can report them as errors.
			 */
		}
	}

	private String getNameFromType(int activityType) {
        switch(activityType) {
            case DetectedActivity.IN_VEHICLE:
                return "in_vehicle";
            case DetectedActivity.ON_BICYCLE:
                return "on_bicycle";
            case DetectedActivity.ON_FOOT:
                return "on_foot";
            case DetectedActivity.STILL:
                return "still";
            case DetectedActivity.UNKNOWN:
                return "unknown";
            case DetectedActivity.TILTING:
                return "tilting";
        }
        return "unknown";
    }

}
