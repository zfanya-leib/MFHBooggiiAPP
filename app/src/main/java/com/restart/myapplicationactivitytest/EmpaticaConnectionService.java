package com.restart.myapplicationactivitytest;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.ScanCallback;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.empatica.empalink.ConnectionNotAllowedException;
import com.empatica.empalink.EmpaDeviceManager;
import com.empatica.empalink.EmpaticaDevice;
import com.empatica.empalink.config.EmpaSensorType;
import com.empatica.empalink.config.EmpaStatus;
import com.empatica.empalink.delegate.EmpaDataDelegate;
import com.empatica.empalink.delegate.EmpaStatusDelegate;

import java.util.Stack;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

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
        onReciveUpdate(Constants.EDA,gsr);
    }

    @Override
    public void didReceiveBVP(float bvp, double timestamp) {

    }

    @Override
    public void didReceiveIBI(float ibi, double timestamp) {
        Log.i(TAG, "didReceiveIBI" + ibi);
        ibiArray.push(ibi);
        Double sum = ibiArray.stream().mapToDouble(Double::valueOf).sum();
        Double bpm = 60 / sum * ibiArray.size();
        Log.i(TAG, "bpm: " + bpm);

        onReciveUpdate(Constants.BPM, bpm.intValue());

        float rmssdTotal = 0;

        for (int i = 1; i < ibiArray.size(); i++) {
            rmssdTotal += pow((ibiArray.get(i - 1) - ibiArray.get(i)) * 1000, 2);
        }

        float rmssd = (float) sqrt(rmssdTotal / (ibiArray.size() - 1));
        Log.i(TAG, "rmssd: " + rmssd);
        onReciveUpdate(Constants.HRV,(int) rmssd);
    }

    @Override
    public void didReceiveTemperature(float t, double timestamp) {

    }

    @Override
    public void didReceiveAcceleration(int x, int y, int z, double timestamp) {

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
}
