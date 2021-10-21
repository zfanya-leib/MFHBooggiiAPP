package com.restart.myapplicationactivitytest;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.le.ScanCallback;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.amplifyframework.AmplifyException;
import com.amplifyframework.api.aws.AWSApiPlugin;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.core.model.temporal.Temporal;
import com.amplifyframework.datastore.AWSDataStorePlugin;
import com.amplifyframework.datastore.generated.model.Measurement;
import com.empatica.empalink.ConnectionNotAllowedException;
import com.empatica.empalink.EmpaDeviceManager;
import com.empatica.empalink.EmpaticaDevice;
import com.empatica.empalink.config.EmpaSensorType;
import com.empatica.empalink.config.EmpaStatus;
import com.empatica.empalink.delegate.EmpaDataDelegate;
import com.empatica.empalink.delegate.EmpaStatusDelegate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import common.Constants;

public class EmpaticaConnectionService extends Service implements EmpaDataDelegate, EmpaStatusDelegate {
    private static final String TAG = "EmpaticaService";
    public static final String CHANNEL_ID = "EmpaticaServiceChannel";
    private static final int REQUEST_PERMISSION_ACCESS_COARSE_LOCATION = 1;
    private static final String EMPATICA_API_KEY = "1a25b9decfbd48cb9c833e0a09851279";
    private int max_last_ibi_samples_to_cache = 15;
    private Stack<Float> ibiArray = new SizedStack<Float>(max_last_ibi_samples_to_cache);

    private static EmpaticaConnectionService _instance = null;

    private EmpaDeviceManager deviceManager = null;
    private String userName = "ronenbh";
    private List<Measurement> measurements = new ArrayList<Measurement>(1001);
    static final int DEFAULT_THREAD_POOL_SIZE = 4;

    ExecutorService executorService = Executors.newFixedThreadPool(DEFAULT_THREAD_POOL_SIZE);

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null) {
            String action = intent.getAction();
            switch (action) {
                case Constants.ACTION_START_FOREGROUND_SERVICE:
                    if(_instance == null) {
                        startForegroundService();
                        _instance = this;
                    }
                    break;
                case Constants.ACTION_STOP_FOREGROUND_SERVICE:
                    stopForegroundService(intent);
            }
        }

        return START_STICKY;
    }

    private void startForegroundService(){

            createNotificationChannel();
            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this,
                    0, notificationIntent, 0);
            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("booggii et")
                    .setContentText("Empatica Connection Service")
                    .setSmallIcon(R.drawable.icon)
                    .setContentIntent(pendingIntent)
                    .build();
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE);
            initAmplify();
            initEmpaticaDeviceManager();
    }

    private void stopForegroundService(Intent intent){
        stopService(intent);
        _instance = null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Empatica Connection Service Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        if (deviceManager != null) {
            deviceManager.cleanUp();
        }
        super.onDestroy();
    }

    @Override
    public boolean stopService(Intent name) {
        if (deviceManager != null) {
            deviceManager.stopScanning();
        }
        return super.stopService(name);
    }


    private void initAmplify() {
        try {
//            userName = AWSMobileClient.getInstance().getUsername();
            Amplify.addPlugin(new AWSDataStorePlugin());
            Amplify.addPlugin(new AWSApiPlugin());
            Amplify.configure(getApplicationContext());
            Log.i(TAG, "Initialized Amplify");
//            Amplify.DataStore.clear(
//                    () -> Log.i("MyAmplifyApp", "DataStore is cleared."),
//                    failure -> Log.e("MyAmplifyApp", "Failed to clear DataStore.")
//            );
        } catch (AmplifyException error) {
            Log.e(TAG, "Could not initialize Amplify", error);
        }
    }

    private void testDb() {
        // wait for authentication to set the username
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        executorService.execute(new Runnable(){
            @Override
            public void run() {
                while (true) {
                    try {
                        writeIbiToDb(0.123, Instant.now().toEpochMilli() / 1000);
                        TimeUnit.MILLISECONDS.sleep(1000);
                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }
                }
            }
        });
    }

    private void initEmpaticaDeviceManager() {
        // Android 6 (API level 23) now require ACCESS_COARSE_LOCATION permission to use BLE
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //ring[] { Manifest.permission.ACCESS_FINE_LOCATION }, REQUEST_PERMISSION_ACCESS_COARSE_LOCATION);
        } else {

            if (TextUtils.isEmpty(EMPATICA_API_KEY)) {
                new AlertDialog.Builder(this)
                        .setTitle("Warning")
                        .setMessage("Please insert your API KEY")
                        .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // without permission exit is the only way
                                //finish();
                            }
                        })
                        .show();
                return;
            }

            // Create a new EmpaDeviceManager. MainActivity is both its data and status delegate.
            deviceManager = new EmpaDeviceManager(getApplicationContext(), this, this);

            // Initialize the Device Manager using your API key. You need to have Internet access at this point.
            deviceManager.authenticateWithAPIKey(EMPATICA_API_KEY);
        }
    }



    @Override
    public void didDiscoverDevice(EmpaticaDevice bluetoothDevice, String deviceName, int rssi, boolean allowed) {
        Log.i(TAG, "didDiscoverDevice" + deviceName + "allowed: " + allowed);

        if (allowed) {
            // Stop scanning. The first allowed device will do.
            deviceManager.stopScanning();
            try {
                // Connect to the device
                deviceManager.connectDevice(bluetoothDevice);

            } catch (ConnectionNotAllowedException e) {
                // This should happen only if you try to connect when allowed == false.
                // TODO: send error notification
                Log.e(TAG, "didDiscoverDevice" + deviceName + "allowed: " + allowed + " - ConnectionNotAllowedException", e);
            }
        }
    }
    @Override
    public void didUpdateStatus(EmpaStatus status) {

        // The device manager is ready for use
        if (status == EmpaStatus.READY) {
            onReciveUpdate(Constants.DISCONNECTED,0);
            // Start scanning
            deviceManager.startScanning();

            // The device manager has established a connection
        } else if (status == EmpaStatus.CONNECTED) {

            onReciveUpdate(Constants.CONNECTED,0);
            // The device manager disconnected from a device
        } else if (status == EmpaStatus.DISCONNECTED) {
            deviceManager.startScanning();
            onReciveUpdate(Constants.DISCONNECTED,0);
        }
    }

    @Override
    public void didFailedScanning(int errorCode) {
/*
         A system error occurred while scanning.
         @see https://developer.android.com/reference/android/bluetooth/le/ScanCallback
        */
        switch (errorCode) {
            case ScanCallback.SCAN_FAILED_ALREADY_STARTED:
                Log.e(TAG,"Scan failed: a BLE scan with the same settings is already started by the app");
                break;
            case ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED:
                Log.e(TAG,"Scan failed: app cannot be registered");
                break;
            case ScanCallback.SCAN_FAILED_FEATURE_UNSUPPORTED:
                Log.e(TAG,"Scan failed: power optimized scan feature is not supported");
                break;
            case ScanCallback.SCAN_FAILED_INTERNAL_ERROR:
                Log.e(TAG,"Scan failed: internal error");
                break;
            default:
                Log.e(TAG,"Scan failed with unknown error (errorCode=" + errorCode + ")");
                break;
        }
    }

    @Override
    public void didRequestEnableBluetooth() {
        // Request the user to enable Bluetooth
        onReciveUpdate(Constants.BLUETOOTH,1);
    }

    @Override
    public void bluetoothStateChanged() {
// E4link detected a bluetooth adapter change
        // Check bluetooth adapter and update your UI accordingly.
//        boolean isBluetoothOn = BluetoothAdapter.getDefaultAdapter().isEnabled();
//        Log.i(TAG, "Bluetooth State Changed: " + isBluetoothOn);
    }

    @Override
    public void didUpdateOnWristStatus(int status) {

    }

    @Override
    public void didReceiveGSR(float gsr, double timestamp) {
        Log.i(TAG, "EDA value:" + gsr);
        writeEdaToDb((double) gsr, timestamp);
        onReciveUpdate(Constants.EDA,gsr);
    }

    @Override
    public void didReceiveBVP(float bvp, double timestamp) {
        writeBvpToDb((double) bvp, timestamp);
    }

    @Override
    public void didReceiveIBI(float ibi, double timestamp) {
        Log.i(TAG, "didReceiveIBI" + ibi);
        ibiArray.push(ibi);

        writeIbiToDb((double) ibi, timestamp);

        Double bpm = calcBpm();
//        writeHrToDb(bpm, timestamp);

        float hrv = calcHrv();
//        writeHrvToDb(hrv, timestamp);

        onReciveUpdate(Constants.HRV,(int) hrv);
        onReciveUpdate(Constants.BPM, bpm.intValue());
    }

    @Override
    public void didReceiveTemperature(float t, double timestamp) {
        writeTemperatureToDb((double) t, timestamp);
    }

    @Override
    public void didReceiveAcceleration(int x, int y, int z, double timestamp) {
        writeMeasurementToDb("ACC_X", x, (long) timestamp);
        writeMeasurementToDb("ACC_Y", y, (long) timestamp);
        writeMeasurementToDb("ACC_Z", z, (long) timestamp);
    }

    @Override
    public void didReceiveBatteryLevel(float level, double timestamp) {
        Log.i(TAG, "Battery level:" + level);
        onReciveUpdate(Constants.BATTERY,level * 100);
    }

    @Override
    public void didReceiveTag(double timestamp) {

    }

    private void onReciveUpdate(String param, float value){
        Intent updateIntent = new Intent(Constants.EMPATICA_MONITOR);
        updateIntent.putExtra(Constants.EMPATICA_PARAM,param);
        updateIntent.putExtra(Constants.EMPATICA_VALUE,value);

        this.sendBroadcast(updateIntent);
    }

    @Override
    public void didEstablishConnection() {
    }

    @Override
    public void didUpdateSensorStatus(int status, EmpaSensorType type) {

    }

    private void writeIbiToDb(double ibi, double timestamp) {
        writeMeasurementToDb("IBI", ibi, (long) timestamp);
    }

    private void writeHrToDb(double hr, double timestamp) {
        writeMeasurementToDb("HR", hr, (long) timestamp);
    }

    private void writeHrvToDb(double hrv, double timestamp) {
        writeMeasurementToDb("HRV", hrv, (long) timestamp);
    }

    private void writeEdaToDb(double eda, double timestamp) {
        writeMeasurementToDb("EDA", eda, (long) timestamp);
    }

    private void writeBvpToDb(double bvp, double timestamp) {
        writeMeasurementToDb("BVP", bvp, (long) timestamp);
    }

    private void writeTemperatureToDb(double temp, double timestamp) {
        writeMeasurementToDb("TEMPERATURE", temp, (long) timestamp);
    }

    private void writeMeasurementToDb(String name, double value, long timestamp) {
        Measurement item = Measurement.builder()
                .name(name)
                .value(value)
                .timestamp(new Temporal.Timestamp(timestamp, TimeUnit.SECONDS))
                .username(userName)
                .build();
        measurements.add(item);
        if (measurements.size() > 1000) {
            final List<Measurement> measurements_copy = new ArrayList<Measurement>(measurements);
            measurements.clear();
            executorService.execute(new Runnable(){
                @Override
                public void run() {
                    for (Measurement m : measurements_copy) {
                        Amplify.DataStore.save(
                                m,
                                success -> Log.i("Amplify", "Saved item: " + success.item().getId()),
                                error -> Log.e("Amplify", "Could not save item to DataStore", error)
                        );
                    }
                }
            });
        }
//        Measurement item = Measurement.builder()
//                .name(name)
//                .value(value)
//                .timestamp(new Temporal.Timestamp(timestamp, TimeUnit.SECONDS))
//                .username(userName)
//                .build();
//        measurements.add(item);
//        if (measurements.size() > 1000) {
//            for (Measurement m : measurements) {
//        Amplify.DataStore.save(
//                item,
//                success -> Log.i("Amplify", "Saved item: " + success.item().getId()),
//                error -> Log.e("Amplify", "Could not save item to DataStore", error)
//        );
//            }
//            measurements.clear();
//        }
    }

    private float calcHrv() {
        float rmssdTotal = 0;

        for (int i = 1; i < ibiArray.size(); i++) {
            rmssdTotal += pow((ibiArray.get(i - 1) - ibiArray.get(i)) * 1000, 2);
        }

        float rmssd = (float) sqrt(rmssdTotal / (ibiArray.size() - 1));
        Log.i(TAG, "rmssd: " + rmssd);
        return rmssd;
        //        float rrTotal=0;
//
//        for (int i = 1; i < ibiArray.size(); i++) {
//            rrTotal += ibiArray.get(i).intValue();
//        }
//
//        float mrr = rrTotal / (ibiArray.size() - 1);
//        float sdnnTotal = 0;
//
//        for (int i = 1; i < ibiArray.size(); i++) {
//            sdnnTotal += pow(ibiArray.get(i).intValue() - mrr, 2);
//        }
//
//        float sdnn = (float) sqrt(sdnnTotal / (ibiArray.size() - 1));
//        updateLabel(hrvLabel, "" + (int) sdnn);

    }

    @NonNull
    private Double calcBpm() {
        Double sum = ibiArray.stream().mapToDouble(Double::valueOf).sum();
        Double bpm = 60 / sum * ibiArray.size();
        Log.i(TAG, "bpm: " + bpm);
        return bpm;
    }

}

