package com.mohsin.samsunghealth;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.samsung.android.sdk.healthdata.HealthConnectionErrorResult;
import com.samsung.android.sdk.healthdata.HealthConstants;
import com.samsung.android.sdk.healthdata.HealthDataService;
import com.samsung.android.sdk.healthdata.HealthDataStore;
import com.samsung.android.sdk.healthdata.HealthPermissionManager;
import com.samsung.android.sdk.healthdata.HealthResultHolder;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Home extends AppCompatActivity {


    public static final String APP_TAG = "SimpleHealth";

    private static Home mInstance = null;
    private HealthDataStore mStore;
    private HealthConnectionErrorResult mConnError;
    private Set<HealthPermissionManager.PermissionKey> mKeySet;
    private StepCountReporter mReporter;
    private StepCountReporter mstepCountRepoter;
    private HeartRateReporter mHeartReporter;
    private WeightReporter mWeightReporter;
    private BloodPressureReporter mBloodPressureReporter;
    private TemperatureReporter mTemperatureReporter;
    private HeightReporter mHeightReporter;


    private final HealthResultHolder.ResultListener<HealthPermissionManager.PermissionResult> mPermissionListener =
            new HealthResultHolder.ResultListener<HealthPermissionManager.PermissionResult>() {

                @Override
                public void onResult(HealthPermissionManager.PermissionResult result) {
                    Log.d(APP_TAG, "Permission callback is received.");
                    Map<HealthPermissionManager.PermissionKey, Boolean> resultMap = result.getResultMap();

                    if (resultMap.containsValue(Boolean.FALSE)) {
                        // Requesting permission fails
                    } else {
                        mReporter = new StepCountReporter(mStore);
                        mReporter.start(mStepCountObserver);

                        mHeartReporter = new HeartRateReporter(mStore);
                        mHeartReporter.start(heartRateObserver);

                        mWeightReporter = new WeightReporter(mStore);
                        mWeightReporter.start(weightRateObserver);

                        mBloodPressureReporter = new BloodPressureReporter(mStore);
                        mBloodPressureReporter.start(bloodPressureObserver);

                        mTemperatureReporter = new TemperatureReporter(mStore);
                        mTemperatureReporter.start(temperatureObserver);

                        mHeightReporter = new HeightReporter(mStore);
                        mHeightReporter.start(heightObserver);
                    }
                }
            };


    private final HealthDataStore.ConnectionListener mConnectionListener = new HealthDataStore.ConnectionListener() {

        @Override
        public void onConnected() {
            Log.d(APP_TAG, "Health data service is connected.");
            HealthPermissionManager pmsManager = new HealthPermissionManager(mStore);


            try {
                // Check whether the permissions that this application needs are acquired
                Map<HealthPermissionManager.PermissionKey, Boolean> resultMap = pmsManager.isPermissionAcquired(mKeySet);

                if (resultMap.containsValue(Boolean.FALSE)) {
                    // Request the permission for reading step counts if it is not acquired
                    pmsManager.requestPermissions(mKeySet, Home.this).setResultListener(mPermissionListener);
                } else {
                    // Get the current step count and display it
                    // ...

                    mReporter = new StepCountReporter(mStore);
                    mReporter.start(mStepCountObserver);

                    mHeartReporter = new HeartRateReporter(mStore);
                    mHeartReporter.start(heartRateObserver);

                    mWeightReporter = new WeightReporter(mStore);
                    mWeightReporter.start(weightRateObserver);

                    mBloodPressureReporter = new BloodPressureReporter(mStore);
                    mBloodPressureReporter.start(bloodPressureObserver);

                    mTemperatureReporter = new TemperatureReporter(mStore);
                    mTemperatureReporter.start(temperatureObserver);

                    mHeightReporter = new HeightReporter(mStore);
                    mHeightReporter.start(heightObserver);
                }
            } catch (Exception e) {
                Log.e(APP_TAG, e.getClass().getName() + " - " + e.getMessage());
                Log.e(APP_TAG, "Permission setting fails.");
            }
        }

        @Override
        public void onConnectionFailed(HealthConnectionErrorResult error) {
            Log.d(APP_TAG, "Health data service is not available.");
            showConnectionFailureDialog(error);
        }

        @Override
        public void onDisconnected() {
            Log.d(APP_TAG, "Health data service is disconnected.");
        }
    };
                @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
                    mInstance = this;

                    mKeySet = new HashSet<HealthPermissionManager.PermissionKey>();
                    mKeySet.add(new HealthPermissionManager.PermissionKey(HealthConstants.StepCount.HEALTH_DATA_TYPE, HealthPermissionManager.PermissionType.READ));
                    mKeySet.add(new HealthPermissionManager.PermissionKey(HealthConstants.HeartRate.HEALTH_DATA_TYPE, HealthPermissionManager.PermissionType.READ));
                    mKeySet.add(new HealthPermissionManager.PermissionKey(HealthConstants.Weight.HEALTH_DATA_TYPE, HealthPermissionManager.PermissionType.READ));
                    mKeySet.add(new HealthPermissionManager.PermissionKey(HealthConstants.BloodPressure.HEALTH_DATA_TYPE, HealthPermissionManager.PermissionType.READ));
                    mKeySet.add(new HealthPermissionManager.PermissionKey(HealthConstants.BodyTemperature.HEALTH_DATA_TYPE, HealthPermissionManager.PermissionType.READ));
                    mKeySet.add(new HealthPermissionManager.PermissionKey(HealthConstants.Height.HEALTH_DATA_TYPE, HealthPermissionManager.PermissionType.READ));

                    HealthDataService healthDataService = new HealthDataService();
                    try {
                        healthDataService.initialize(this);
                    } catch (Exception e) {
                        e.printStackTrace();}

                    mStore = new HealthDataStore(this, mConnectionListener);
                    // Request the connection to the health data store
                    mStore.connectService();
                }

    @Override
    public void onDestroy() {
        mStore.disconnectService();
        super.onDestroy();
    }

    private StepCountReporter.StepCountObserver mStepCountObserver = count -> {

        updateStepCountView(String.valueOf(count));
    };

    private HeartRateReporter.HeartRateObserver  heartRateObserver = count -> {
        updateHeartRateView(String.valueOf(count));
    };

    private WeightReporter.WeightObserver  weightRateObserver = count -> {
        updateWeightView(String.valueOf(count));
    };

    private BloodPressureReporter.BloodPressureObserver  bloodPressureObserver = count -> {
        updateBloodPressuretView(String.valueOf(count));
    };

    private TemperatureReporter.TemperatureObserver  temperatureObserver = count -> {
        updateTemperature(String.valueOf(count));
    };

    private HeightReporter.HeightObserver  heightObserver = count -> {
        updateHeight(String.valueOf(count));
    };

    private void updateHeight(String count) {
        //stepCount.setText(count);
        Log.d(APP_TAG, "Height : " + count);
    }

    private void updateTemperature(String count) {
        //stepCount.setText(count);
        Log.d(APP_TAG, "Temperature : " + count);
    }

    private void updateBloodPressuretView(String count) {
        //stepCount.setText(count);
        Log.d(APP_TAG, "BloodPressuret : " + count);
    }

    private void updateWeightView(String count) {
        //stepCount.setText(count);
        Log.d(APP_TAG, "Weight : " + count);

    }


    private void updateHeartRateView(String valueOf) {
        Log.d(APP_TAG, "heart rate reported : " + valueOf);
        //updateHeartRateView(String.valueOf(valueOf));
    }

    private void updateStepCountView(String count) {
        //stepCount.setText(count);
        Log.d(APP_TAG, "Step reported : " + count);

    }

    private void showConnectionFailureDialog(HealthConnectionErrorResult error) {

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        mConnError = error;
        String message = "Connection with Samsung Health is not available";

        if (mConnError.hasResolution()) {
            switch(error.getErrorCode()) {
                case HealthConnectionErrorResult.PLATFORM_NOT_INSTALLED:
                    message = "Please install Samsung Health";
                    break;
                case HealthConnectionErrorResult.OLD_VERSION_PLATFORM:
                    message = "Please upgrade Samsung Health";
                    break;
                case HealthConnectionErrorResult.PLATFORM_DISABLED:
                    message = "Please enable Samsung Health";
                    break;
                case HealthConnectionErrorResult.USER_AGREEMENT_NEEDED:
                    message = "Please agree with Samsung Health policy";
                    break;
                default:
                    message = "Please make Samsung Health available";
                    break;
            }
        }

        alert.setMessage(message);

        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                if (mConnError.hasResolution()) {
                    mConnError.resolve(mInstance);
                }
            }
        });

        if (error.hasResolution()) {
            alert.setNegativeButton("Cancel", null);
        }

        alert.show();
    }
}