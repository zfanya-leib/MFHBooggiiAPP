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
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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

import com.amazonaws.mobile.client.AWSMobileClient;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.time.ZoneId;

import common.Constants;
import services.Streamer;

public class EmpaticaConnectionService extends Service implements EmpaDataDelegate, EmpaStatusDelegate {
    private static final String TAG = "EmpaticaService";
    public static final String CHANNEL_ID = "EmpaticaServiceChannel";
    private static final int REQUEST_PERMISSION_ACCESS_COARSE_LOCATION = 1;
    private static final String EMPATICA_API_KEY = "1a25b9decfbd48cb9c833e0a09851279";
    private final int max_last_ibi_samples_to_cache = 15;
    private Stack<Float> ibiArray = new SizedStack<Float>(max_last_ibi_samples_to_cache);
    private AtomicBoolean scanningComplete = new AtomicBoolean(false);

    private static EmpaticaConnectionService _instance = null;
    private EmpaStatus connectionStatus = EmpaStatus.INITIAL;
    private EmpaDeviceManager deviceManager = null;
    private final ReentrantLock lock = new ReentrantLock();
    private String userName = null;
    private List<Measurement> measurements = new ArrayList<Measurement>(1001);
    static final int DEFAULT_THREAD_POOL_SIZE = 4;
    static final DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

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
                case Constants.ACTION_START_E4_CONNECT:
                    initEmpaticaDeviceManager();
                    break;
                case Constants.ACTION_STOP_FOREGROUND_SERVICE:
                    stopForegroundService(intent);
                    break;
                case Constants.ACTION_SERVICE_CONNECTION_STATUS:
                    statusNotification();
                    break;
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
    }

    private void stopForegroundService(Intent intent){
        stopService(intent);
        _instance = null;
    }

    private void statusNotification(){
        if(this.deviceManager == null){
            //initEmpaticaDeviceManager();
            return;
        }

        if(this.connectionStatus == EmpaStatus.CONNECTED){
            onReciveUpdate(Constants.CONNECTED,0);
        }
        else{
            onReciveUpdate(Constants.DISCONNECTED,0);
        }
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
            Amplify.addPlugin(new AWSDataStorePlugin());
            Amplify.addPlugin(new AWSApiPlugin());
            Amplify.configure(getApplicationContext());
            Log.i(TAG, "Initialized Amplify");
            userName = AWSMobileClient.getInstance().getUsername();
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
                try {
                    writeIbiToDb((float) 0.123, (double)Instant.now().toEpochMilli() / 1000);
                    TimeUnit.MILLISECONDS.sleep(1000);
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
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

        if (allowed && !this.scanningComplete.get()) {
            this.lock.lock();

            try {
                // Stop scanning. The first allowed device will do.
                this.scanningComplete.set(true);
                deviceManager.stopScanning();
                // Connect to the device
                deviceManager.connectDevice(bluetoothDevice);

            } catch (ConnectionNotAllowedException e) {
                // This should happen only if you try to connect when allowed == false.
                // TODO: send error notification
                Log.e(TAG, "didDiscoverDevice" + deviceName + "allowed: " + allowed + " - ConnectionNotAllowedException", e);
            }
            finally {
                this.lock.unlock();
            }
        }
    }
    @Override
    public void didUpdateStatus(EmpaStatus status) {

        // The device manager is ready for use
        this.lock.lock();
        try {
            if (status == EmpaStatus.READY && !this.scanningComplete.get()) {
                Log.i(TAG, "app is ready, start scanning for devices");
                deviceManager.startScanning();

                // The device manager has established a connection
            } else if (status == EmpaStatus.CONNECTED) {
                Log.i(TAG, "device is connected");
                connectionStatus = EmpaStatus.CONNECTED;
                onReciveUpdate(Constants.CONNECTED, 0);
                // The device manager disconnected from a device
            } else if (status == EmpaStatus.DISCONNECTED) {
                Log.i(TAG, "device was disconnected");
                scanningComplete.set(false);
                connectionStatus = EmpaStatus.DISCONNECTED;
                deviceManager.startScanning();
                onReciveUpdate(Constants.DISCONNECTED, 0);
            }
        }
        catch (Exception e){
            Log.e(TAG,e.getLocalizedMessage());
        }
        finally {
            this.lock.unlock();
        }
    }

    @Override
    public void didFailedScanning(int errorCode) {
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
        writeEdaToDb(gsr, timestamp);
        onReciveUpdate(Constants.EDA,gsr);
    }

    @Override
    public void didReceiveBVP(float bvp, double timestamp) {
        writeBvpToDb(bvp, timestamp);
    }

    @Override
    public void didReceiveIBI(float ibi, double timestamp) {
        Log.i(TAG, "didReceiveIBI" + ibi);
        ibiArray.push(ibi);

        writeIbiToDb(ibi, timestamp);

        Double bpm = calcBpm();
//        writeHrToDb(bpm, timestamp);

        float hrv = calcHrv();
//        writeHrvToDb(hrv, timestamp);

        onReciveUpdate(Constants.HRV,(int) hrv);
        onReciveUpdate(Constants.BPM, bpm.intValue());
    }

    @Override
    public void didReceiveTemperature(float t, double timestamp) {
        writeTemperatureToDb(t, timestamp);
    }

    @Override
    public void didReceiveAcceleration(int x, int y, int z, double timestamp) {
        writeAccToDb(x, y, z, timestamp);
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

    private void writeAccToDb(int x, int y, int z, double timestamp) {
        writeMeasurementToDb("ACC", x + "," + y + "," + z, timestamp);
    }

    private void writeIbiToDb(float ibi, double timestamp) {
        writeMeasurementToDb("IBI", Float.toString(ibi), timestamp);
    }

    private void writeHrToDb(float hr, double timestamp) {
        writeMeasurementToDb("HR", Float.toString(hr), timestamp);
    }

    private void writeHrvToDb(float hrv, double timestamp) {
        writeMeasurementToDb("HRV", Float.toString(hrv), timestamp);
    }

    private void writeEdaToDb(float eda, double timestamp) {
        writeMeasurementToDb("EDA", Float.toString(eda), timestamp);
    }

    private void writeBvpToDb(float bvp, double timestamp) {
        writeMeasurementToDb("BVP", Float.toString(bvp), timestamp);
    }

    private void writeTemperatureToDb(float temp, double timestamp) {
        writeMeasurementToDb("TEMP", Float.toString(temp), timestamp);
    }

    private void writeMeasurementToDb(String name, String value, double timestamp) {
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli((long)timestamp*1000),
                ZoneId.systemDefault());
        Streamer.getInstance().addData("booggii-empatica-" + name.toLowerCase(),
                userName + "," + value + "," + dateTime);
    }

    private void oldWriteToDb(String name, double value, long timestamp) {
        Measurement item = Measurement.builder()
                .name(name)
                .value(value)
                .timestamp(new Temporal.Timestamp(timestamp, TimeUnit.SECONDS))
                .username(userName)
                .build();
        Amplify.DataStore.save(
                item,
                success -> Log.i("Amplify", "Saved item: " + success.item().getId()),
                error -> Log.e("Amplify", "Could not save item to DataStore", error)
        );
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

