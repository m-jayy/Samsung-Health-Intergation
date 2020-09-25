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

public class HeartRateReporter {
    private final HealthDataStore mStore;
    private HeartRateObserver mHeartRateObserver;
    private static final long ONE_DAY_IN_MILLIS = 24 * 60 * 60 * 1000L;

    public HeartRateReporter(HealthDataStore store) {
        mStore = store;
    }

    public void start(HeartRateObserver listener) {
        mHeartRateObserver = listener;
        HealthDataObserver.addObserver(mStore, HealthConstants.HeartRate.HEALTH_DATA_TYPE, mObserver);
        readHeartRate();
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

    private void readHeartRate() {
        HealthDataResolver resolver = new HealthDataResolver(mStore, null);


        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();

        cal.set(2012, 7, 13);
        cal.set(Calendar.HOUR_OF_DAY, 0); //so it
        cal.set(Calendar.MINUTE, 0);
        long startTime = cal.getTimeInMillis();


        HealthDataResolver.Filter filter = HealthDataResolver.Filter.and(HealthDataResolver.Filter.greaterThanEquals(HealthConstants.HeartRate.START_TIME, startTime),
                HealthDataResolver.Filter.lessThanEquals(HealthConstants.HeartRate.START_TIME, endTime));
        HealthDataResolver.ReadRequest request = new HealthDataResolver.ReadRequest.Builder()
                .setDataType(HealthConstants.HeartRate.HEALTH_DATA_TYPE)
                .setProperties(new String[]{
                        HealthConstants.HeartRate.HEART_BEAT_COUNT,
                        HealthConstants.HeartRate.HEART_RATE,
                        HealthConstants.HeartRate.CREATE_TIME
                })
                .setFilter(filter)
                .build();

        try {
            resolver.read(request).setResultListener(new HealthResultHolder.ResultListener<HealthDataResolver.ReadResult>() {
                @Override
                public void onResult(HealthDataResolver.ReadResult result) {
                    Log.w(APP_TAG, "Getting heart rate...");

                    int heartBeatCount;
                    long datetime;
                    long heartRate;
                    Cursor c = null;

                    try {
                        c = result.getResultCursor();
                        if (c != null) {
                            if (c.getCount() == 0) {
                                Log.d(APP_TAG, "No heart rate entry found.");
                            }
                            while (c.moveToNext()) {
                                heartBeatCount = c.getInt(c.getColumnIndex(HealthConstants.HeartRate.HEART_BEAT_COUNT));
                                heartRate = c.getLong(c.getColumnIndex(HealthConstants.HeartRate.HEART_RATE));
                                datetime  = c.getLong(c.getColumnIndex(HealthConstants.HeartRate.CREATE_TIME));

                                Log.w(APP_TAG, "Heart beat count " + heartBeatCount
                                        + ", heart rate " + heartRate + "bpm" + ", datetime : "+ getDate(datetime, "dd/MM/yyyy hh:mm:ss.SSS")
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

    }


    private final HealthResultHolder.ResultListener<HealthDataResolver.ReadResult> mListener = result -> {
        int count = 0;

        try {
            for (HealthData data : result) {
                count += data.getInt(HealthConstants.HeartRate.HEART_RATE);
            }
        } finally {
            result.close();
        }

        if (mHeartRateObserver != null) {
            mHeartRateObserver.onChanged(count);
        }
    };

    private final HealthDataObserver mObserver = new HealthDataObserver(null) {

        @Override
        public void onChange(String dataTypeName) {
            Log.d("*&*&*&", "Observer receives a data changed event");
            readHeartRate();
        }
    };

    public interface HeartRateObserver {
        void onChanged(int count);
    }
}
