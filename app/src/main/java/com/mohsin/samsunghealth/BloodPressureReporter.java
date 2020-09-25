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

public class BloodPressureReporter {
    private final HealthDataStore mStore;
    private BloodPressureObserver mBloodPressureObserver;
    private static final long ONE_DAY_IN_MILLIS = 24 * 60 * 60 * 1000L;

    public BloodPressureReporter(HealthDataStore store) {
        mStore = store;
    }

    public void start(BloodPressureObserver listener) {
        mBloodPressureObserver = listener;
        HealthDataObserver.addObserver(mStore, HealthConstants.BloodPressure.HEALTH_DATA_TYPE, mObserver);
        readBloodPressureRate();
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

    private void readBloodPressureRate() {
        HealthDataResolver resolver = new HealthDataResolver(mStore, null);

        // Set time range from start time of today to the current time
//        long startTime = getStartTimeOfToday();
//        long endTime = startTime + ONE_DAY_IN_MILLIS;

        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();

        cal.set(2012, 7, 13);
        cal.set(Calendar.HOUR_OF_DAY, 0); //so it
        cal.set(Calendar.MINUTE, 0);
        long startTime = cal.getTimeInMillis();

//        HealthDataResolver.ReadRequest request = new HealthDataResolver.ReadRequest.Builder()
//                .setDataType(HealthConstants.HeartRate.HEART_RATE)
//                .setProperties(new String[] {HealthConstants.HeartRate.HEART_RATE})
//                .setLocalTimeRange(HealthConstants.HeartRate.START_TIME, HealthConstants.HeartRate.TIME_OFFSET,
//                        startTime, endTime)
//                .build();

        HealthDataResolver.Filter filter = HealthDataResolver.Filter.and(HealthDataResolver.Filter.greaterThanEquals(HealthConstants.BloodPressure.START_TIME, startTime),
                HealthDataResolver.Filter.lessThanEquals(HealthConstants.HeartRate.START_TIME, endTime));
        HealthDataResolver.ReadRequest request = new HealthDataResolver.ReadRequest.Builder()
                .setDataType(HealthConstants.BloodPressure.HEALTH_DATA_TYPE)
                .setProperties(new String[]{
                        HealthConstants.BloodPressure.DIASTOLIC,
                        HealthConstants.BloodPressure.SYSTOLIC,
                        HealthConstants.BloodPressure.CREATE_TIME
                })
                .setFilter(filter)
                .build();

        try {
            resolver.read(request).setResultListener(new HealthResultHolder.ResultListener<HealthDataResolver.ReadResult>() {
                @Override
                public void onResult(HealthDataResolver.ReadResult result) {
                    Log.w(APP_TAG, "Getting Blood Pressure...");

                    long DIASTOLIC;
                    long SYSTOLIC;
                    long datetime;
                    Cursor c = null;

                    try {
                        c = result.getResultCursor();
                        if (c != null) {
                            if (c.getCount() == 0) {
                                Log.d(APP_TAG, "No Blood Pressure entry found.");
                            }
                            while (c.moveToNext()) {
                                DIASTOLIC = c.getLong(c.getColumnIndex(HealthConstants.BloodPressure.DIASTOLIC));
                                SYSTOLIC = c.getLong(c.getColumnIndex(HealthConstants.BloodPressure.SYSTOLIC));
                                datetime  = c.getLong(c.getColumnIndex(HealthConstants.BloodPressure.CREATE_TIME));
                                //Toast.makeText(context,""+heartRate,Toast.LENGTH_LONG).show();
                                //mBeatcount.setText(""+heartRate);
                                Log.w(APP_TAG, "DIASTOLIC : " + DIASTOLIC
                                        + ", SYSTOLIC : " + SYSTOLIC + ", datetime : " + getDate(datetime, "dd/MM/yyyy hh:mm:ss.SSS")
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
            Log.d(APP_TAG, "Getting Blood Pressure failed.");
        }

//        try {
//            resolver.read(request).setResultListener(mListener);
//        } catch (Exception e) {
//            Log.e("*&*&*&", "Getting Heart fails.", e);
//        }
    }

    private long getStartTimeOfToday() {
        Calendar today = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        return today.getTimeInMillis();
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

        if (mBloodPressureObserver != null) {
            mBloodPressureObserver.onChanged(count);
        }
    };

    private final HealthDataObserver mObserver = new HealthDataObserver(null) {

        @Override
        public void onChange(String dataTypeName) {
            Log.d("*&*&*&", "Observer receives a data changed event");
            readBloodPressureRate();
        }
    };

    public interface BloodPressureObserver {
        void onChanged(int count);
    }
}
