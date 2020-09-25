package com.mohsin.samsunghealth;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.samsung.android.sdk.healthdata.HealthConnectionErrorResult;
import com.samsung.android.sdk.healthdata.HealthConstants;
import com.samsung.android.sdk.healthdata.HealthDataService;
import com.samsung.android.sdk.healthdata.HealthDataStore;
import com.samsung.android.sdk.healthdata.HealthPermissionManager;
import com.samsung.android.sdk.healthdata.HealthResultHolder;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    public static final String APP_TAG = "SimpleHealth";

    private static MainActivity mInstance = null;
    private HealthDataStore mStore;
    private HealthConnectionErrorResult mConnError;
    private Set<HealthPermissionManager.PermissionKey> mKeySet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mInstance = this;
        mKeySet = new HashSet<HealthPermissionManager.PermissionKey>();
        mKeySet.add(new HealthPermissionManager.PermissionKey(HealthConstants.StepCount.HEALTH_DATA_TYPE, HealthPermissionManager.PermissionType.READ));
        mKeySet.add(new HealthPermissionManager.PermissionKey("com.samsung.shealth.step_daily_trend", HealthPermissionManager.PermissionType.READ));
        mKeySet.add(new HealthPermissionManager.PermissionKey("com.samsung.health.weight", HealthPermissionManager.PermissionType.READ));
        mKeySet.add(new HealthPermissionManager.PermissionKey("com.samsung.health.height", HealthPermissionManager.PermissionType.READ));
        mKeySet.add(new HealthPermissionManager.PermissionKey("com.samsung.health.heart_rate", HealthPermissionManager.PermissionType.READ));
        mKeySet.add(new HealthPermissionManager.PermissionKey("com.samsung.health.blood_pressure", HealthPermissionManager.PermissionType.READ));
        mKeySet.add(new HealthPermissionManager.PermissionKey("com.samsung.health.sleep", HealthPermissionManager.PermissionType.READ));
        mKeySet.add(new HealthPermissionManager.PermissionKey("com.samsung.health.body_temperature", HealthPermissionManager.PermissionType.READ));

        // Create a HealthDataStore instance and set its listener
        mStore = new HealthDataStore(this, mConnectionListener);
        // Request the connection to the health data store
        mStore.connectService();

    }

    @Override
    public void onDestroy() {
        mStore.disconnectService();
        super.onDestroy();
    }

    private final HealthDataStore.ConnectionListener mConnectionListener = new HealthDataStore.ConnectionListener() {

        @Override
        public void onConnected() {
            Log.d(APP_TAG, "Health data service is connected.");
            HealthPermissionManager pmsManager = new HealthPermissionManager(mStore);

//            mReporter = new StepCountReporter(mStore);
            if (isPermissionAcquired()) {
                Log.d("data",HealthConstants.StepCount.HEALTH_DATA_TYPE+"");
                getdata();
            } else {
                requestPermission();
            }
//            try {
//                Map<HealthPermissionManager.PermissionKey, Boolean> resultMap = pmsManager.isPermissionAcquired(mKeySet);
//                    HealthPermissionManager.PermissionKey permKey = new
//                            HealthPermissionManager.PermissionKey(HealthConstants.StepCount.HEALTH_DATA_TYPE, HealthPermissionManager.PermissionType.READ);
//                    try {
//                        // Show user permission UI for allowing user to change options
//                        pmsManager.requestPermissions(Collections.singleton(permKey),
//                                MainActivity.this)
//                                .setResultListener(result -> {
//                                    Log.d(APP_TAG, "Permission callback is received.");
//                                    Map<HealthPermissionManager.PermissionKey, Boolean> resultMap2 = result.getResultMap();
//                                    if (resultMap2.containsValue(Boolean.FALSE)) {
//                                        Toast.makeText(MainActivity.this, "ds", Toast.LENGTH_SHORT).show();
//                                    } else {
//
//                                        Log.d("resultmap",resultMap2.get(HealthConstants.StepCount.COUNT)+"");
//                                    }
//                                });
//                    } catch (Exception e) { Log.e(APP_TAG, "Permission setting fails.", e); }
//
//            } catch (Exception e) {
//                Log.e(APP_TAG, e.getClass().getName() + " - " + e.getMessage());
//                Log.e(APP_TAG, "Permission setting fails.");
//            }
        }
        @Override
        public void onConnectionFailed(HealthConnectionErrorResult healthConnectionErrorResult) {
            AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
            mConnError = healthConnectionErrorResult;
            String message = "Connection with Samsung Health is not available";

            if (mConnError.hasResolution()) {
                switch(healthConnectionErrorResult.getErrorCode()) {
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

            if (healthConnectionErrorResult.hasResolution()) {
                alert.setNegativeButton("Cancel", null);
            }

            alert.show();

        }
        @Override
        public void onDisconnected() {

        }
    };

    private boolean isPermissionAcquired() {
        HealthPermissionManager.PermissionKey permKey = new HealthPermissionManager.PermissionKey(HealthConstants.StepCount.HEALTH_DATA_TYPE,
                HealthPermissionManager.PermissionType.READ);
        HealthPermissionManager pmsManager = new HealthPermissionManager(mStore);
        try {
            // Check whether the permissions that this application needs are acquired
            Map<HealthPermissionManager.PermissionKey, Boolean> resultMap =
                    pmsManager.isPermissionAcquired(Collections.singleton(permKey));
            return resultMap.get(permKey);
        } catch (Exception e) {
            Log.e(APP_TAG, "Permission request fails.", e);
        }
        return false;
    }

    private void requestPermission() {
        HealthPermissionManager.PermissionKey permKey = new
                HealthPermissionManager.PermissionKey(HealthConstants.StepCount.HEALTH_DATA_TYPE, HealthPermissionManager.PermissionType.READ);
        HealthPermissionManager pmsManager = new HealthPermissionManager(mStore);
        try {
            // Show user permission UI for allowing user to change options
            pmsManager.requestPermissions(Collections.singleton(permKey),
                    MainActivity.this)
                    .setResultListener(result -> {
                        Log.d(APP_TAG, "Permission callback is received.");
                        Map<HealthPermissionManager.PermissionKey, Boolean> resultMap = result.getResultMap();
                        if (resultMap.containsValue(Boolean.FALSE)) {

                            Toast.makeText(getApplicationContext(), "Ops Something went wrong", Toast.LENGTH_SHORT).show();
//                            updateStepCountView("");
//                            showPermissionAlarmDialog();
                            finish();
                        } else {
                            // Get the current step count and display it
                            getdata();
                        }
                    });
        } catch (Exception e) { Log.e(APP_TAG, "Permission setting fails.", e); }
    }


    public void getdata(){
        Toast.makeText(getApplicationContext(), "aya", Toast.LENGTH_SHORT).show();
    }



    private final HealthResultHolder.ResultListener<HealthPermissionManager.PermissionResult> mPermissionListener =
            new HealthResultHolder.ResultListener<HealthPermissionManager.PermissionResult>() {

                @Override
                public void onResult(HealthPermissionManager.PermissionResult result) {
                    Log.d(APP_TAG, "Permission callback is received.");
                    Map<HealthPermissionManager.PermissionKey, Boolean> resultMap = result.getResultMap();

                    if (resultMap.containsValue(Boolean.FALSE)) {
                        Toast.makeText(MainActivity.this, "aje", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "naa", Toast.LENGTH_SHORT).show();
                    }
                }
            };

}

