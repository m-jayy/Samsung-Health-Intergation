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

import static com.mohsin.samsunghealth.Home.APP_TAG;

public class HeightReporter {
    private final HealthDataStore mStore;
    private HeightObserver mHeightObserver;
    private static final long ONE_DAY_IN_MILLIS = 24 * 60 * 60 * 1000L;

    public HeightReporter(HealthDataStore store) {
        mStore = store;
    }

    public void start(HeightObserver listener) {
        mHeightObserver = listener;
        // Register an observer to listen changes of step count and get today step count
        HealthDataObserver.addObserver(mStore, HealthConstants.Height.HEALTH_DATA_TYPE, mObserver);
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


    // Read the today's step count on demand
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

        HealthDataResolver.Filter filter = HealthDataResolver.Filter.and(HealthDataResolver.Filter.greaterThanEquals(HealthConstants.Height.START_TIME, startTime),
                HealthDataResolver.Filter.lessThanEquals(HealthConstants.HeartRate.START_TIME, endTime));
        HealthDataResolver.ReadRequest request = new HealthDataResolver.ReadRequest.Builder()
                .setDataType(HealthConstants.Height.HEALTH_DATA_TYPE)
                .setProperties(new String[]{
                        HealthConstants.Height.HEIGHT,
                        HealthConstants.Height.CREATE_TIME
                })
                .setFilter(filter)
                .build();

        try {
            resolver.read(request).setResultListener(new HealthResultHolder.ResultListener<HealthDataResolver.ReadResult>() {
                @Override
                public void onResult(HealthDataResolver.ReadResult result) {
                    Log.w(APP_TAG, "Getting Height...");

                    long datetime;
                    long height;
                    Cursor c = null;

                    try {
                        c = result.getResultCursor();
                        if (c != null) {
                            if (c.getCount() == 0) {
                                Log.d(APP_TAG, "No Height entry found.");
                            }
                            while (c.moveToNext()) {
                                height = c.getLong(c.getColumnIndex(HealthConstants.Height.HEIGHT));
                                datetime  = c.getLong(c.getColumnIndex(HealthConstants.Height.CREATE_TIME));

                                Log.w(APP_TAG, "height :" + height  + ", datetime : "+ getDate(datetime, "dd/MM/yyyy hh:mm:ss.SSS")
                                );
                            }
                        }
                    } finally {
                        if (c != null) {
                            c.close();
                        }
                    }

                    // TODO: Save heart rate to DB
                    //mCountDownLatch.countDown();
                }
            });
        }
        catch (Exception e) {
            Log.d(APP_TAG, e.getClass().getName() + " - " + e.getMessage());
            Log.d(APP_TAG, "Getting heart rate failed.");
        }

//        try {
//            resolver.read(request).setResultListener(mListener);
//        } catch (Exception e) {
//            Log.e("*&*&*&", "Getting Heart fails.", e);
//        }
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

        if (mHeightObserver != null) {
            mHeightObserver.onChanged(count);
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

    public interface HeightObserver {
        void onChanged(int count);
    }
}
