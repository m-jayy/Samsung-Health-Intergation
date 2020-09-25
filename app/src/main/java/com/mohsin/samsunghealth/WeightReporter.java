package com.mohsin.samsunghealth;

import android.database.Cursor;
import android.util.Log;

import com.samsung.android.sdk.healthdata.HealthConstants;
import com.samsung.android.sdk.healthdata.HealthData;
import com.samsung.android.sdk.healthdata.HealthDataObserver;
import com.samsung.android.sdk.healthdata.HealthDataResolver;
import com.samsung.android.sdk.healthdata.HealthDataStore;
import com.samsung.android.sdk.healthdata.HealthResultHolder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static com.mohsin.samsunghealth.Home.APP_TAG;

public class WeightReporter {
    private final HealthDataStore mStore;
    private WeightObserver mWeightObserver;

    public WeightReporter(HealthDataStore store) {
        mStore = store;
    }

    public void start(WeightObserver listener) {
        mWeightObserver = listener;

        HealthDataObserver.addObserver(mStore, HealthConstants.Weight.HEALTH_DATA_TYPE, mObserver);
        readTodayWeightCount();
    }

    public static String getDate(long milliSeconds, String dateFormat)
    {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    private void readTodayWeightCount() {
        HealthDataResolver resolver = new HealthDataResolver(mStore, null);

        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();

        cal.set(2012, 7, 13);
        cal.set(Calendar.HOUR_OF_DAY, 0); //so it
        cal.set(Calendar.MINUTE, 0);
        long startTime = cal.getTimeInMillis();

        HealthDataResolver.Filter filter = HealthDataResolver.Filter.and(HealthDataResolver.Filter.greaterThanEquals(HealthConstants.Weight.START_TIME, startTime),
                HealthDataResolver.Filter.lessThanEquals(HealthConstants.HeartRate.START_TIME, endTime));
        HealthDataResolver.ReadRequest request = new HealthDataResolver.ReadRequest.Builder()
                .setDataType(HealthConstants.Weight.HEALTH_DATA_TYPE)
                .setProperties(new String[]{
                        HealthConstants.Weight.WEIGHT,
                        HealthConstants.Weight.CREATE_TIME
                })
                .setFilter(filter)
                .build();

        try {
            resolver.read(request).setResultListener(new HealthResultHolder.ResultListener<HealthDataResolver.ReadResult>() {
                @Override
                public void onResult(HealthDataResolver.ReadResult result) {
                    Log.w(APP_TAG, "Getting Weight...");

                    long datetime;
                    long weight;
                    Cursor c = null;

                    try {
                        c = result.getResultCursor();
                        if (c != null) {
                            if (c.getCount() == 0) {
                                Log.d(APP_TAG, "No Weight entry found.");
                            }
                            while (c.moveToNext()) {
                                weight = c.getLong(c.getColumnIndex(HealthConstants.Weight.WEIGHT));
                                datetime  = c.getLong(c.getColumnIndex(HealthConstants.Weight.CREATE_TIME));

                                Log.w(APP_TAG, "weight :" + weight  + ", datetime : "+ getDate(datetime, "dd/MM/yyyy hh:mm:ss.SSS")
                                );
                            }
                        }
                    } finally {
                        if (c != null) {
                            c.close();
                        }
                    }

                }
            });
        }
        catch (Exception e) {
            Log.d(APP_TAG, e.getClass().getName() + " - " + e.getMessage());
            Log.d(APP_TAG, "Getting heart rate failed.");
        }
    }


    private final HealthResultHolder.ResultListener<HealthDataResolver.ReadResult> mListener = result -> {
        int count = 0;

        try {
            for (HealthData data : result) {
                count += data.getInt(HealthConstants.StepCount.COUNT);
            }
        } finally {
            result.close();
        }

        if (mWeightObserver != null) {
            mWeightObserver.onChanged(count);
        }
    };

    private final HealthDataObserver mObserver = new HealthDataObserver(null) {

        // Update the step count when a change event is received
        @Override
        public void onChange(String dataTypeName) {
            Log.d(MainActivity.APP_TAG, "Observer receives a data changed event");
            readTodayWeightCount();
        }
    };

    public interface WeightObserver {
        void onChanged(int count);
    }
}
