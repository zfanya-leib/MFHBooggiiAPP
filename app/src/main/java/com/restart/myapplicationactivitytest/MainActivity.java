package com.restart.myapplicationactivitytest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.KeyEvent;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.restart.myapplicationactivitytest.databinding.ActivityMainBinding;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.empatica.empalink.EmpaDeviceManager;
import common.Constants;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    private static final String TAG = "MainActivity";
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PERMISSION_ACCESS_COARSE_LOCATION = 1;
    private static final String EMPATICA_API_KEY = "1a25b9decfbd48cb9c833e0a09851279";
    private EmpaDeviceManager deviceManager = null;
    private TextView bvpLabel;
    private TextView edaLabel;
    private ProgressBar edaProgress;
    private TextView batteryLabel;
    private TextView statusLabel;
    private TextView deviceNameLabel;
    private LinearLayout dataCnt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bvpLabel = (TextView) findViewById(R.id.txt_bpm);
        edaLabel = (TextView) findViewById(R.id.txt_eda);
        edaProgress = (ProgressBar)findViewById(R.id.pb_eda);

        startService();
    }

    public void startService() {
        Intent serviceIntent = new Intent(this, EmpaticaConnectionService.class);
        serviceIntent.putExtra("inputExtra", "Empatica Connection Service");
        serviceIntent.setAction(Constants.ACTION_START_FOREGROUND_SERVICE);
        ContextCompat.startForegroundService(this, serviceIntent);
    }
    public void stopService() {
        Intent serviceIntent = new Intent(this, EmpaticaConnectionService.class);
        serviceIntent.setAction(Constants.ACTION_STOP_FOREGROUND_SERVICE);
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        return super.onKeyLongPress(keyCode, event);
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        switch (requestCode) {
//            case REQUEST_PERMISSION_ACCESS_COARSE_LOCATION:
//                // If request is cancelled, the result arrays are empty.
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    // Permission was granted, yay!
//                    initEmpaticaDeviceManager();
//                } else {
//                    // Permission denied, boo!
//                    final boolean needRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION);
//                    new AlertDialog.Builder(this)
//                            .setTitle("Permission required")
//                            .setMessage("Without this permission bluetooth low energy devices cannot be found, allow it in order to connect to the device.")
//                            .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int which) {
//                                    // try again
//                                    if (needRationale) {
//                                        // the "never ask again" flash is not set, try again with permission request
//                                        initEmpaticaDeviceManager();
//                                    } else {
//                                        // the "never ask again" flag is set so the permission requests is disabled, try open app settings to enable the permission
//                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                                        Uri uri = Uri.fromParts("package", getPackageName(), null);
//                                        intent.setData(uri);
//                                        startActivity(intent);
//                                    }
//                                }
//                            })
//                            .setNegativeButton("Exit application", new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int which) {
//                                    // without permission exit is the only way
//                                    finish();
//                                }
//                            })
//                            .show();
//                }
//                break;
//        }
//    }

//    private void initEmpaticaDeviceManager() {
//        // Android 6 (API level 23) now require ACCESS_COARSE_LOCATION permission to use BLE
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, REQUEST_PERMISSION_ACCESS_COARSE_LOCATION);
//        } else {
//
//            if (TextUtils.isEmpty(EMPATICA_API_KEY)) {
//                new AlertDialog.Builder(this)
//                        .setTitle("Warning")
//                        .setMessage("Please insert your API KEY")
//                        .setNegativeButton("Close", new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int which) {
//                                // without permission exit is the only way
//                                finish();
//                            }
//                        })
//                        .show();
//                return;
//            }
//
//            // Create a new EmpaDeviceManager. MainActivity is both its data and status delegate.
//            deviceManager = new EmpaDeviceManager(getApplicationContext(), this, this);
//
//            // Initialize the Device Manager using your API key. You need to have Internet access at this point.
//            deviceManager.authenticateWithAPIKey(EMPATICA_API_KEY);
//        }
//    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if (deviceManager != null) {
//            deviceManager.cleanUp();
//        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopService();
//        if (deviceManager != null) {
//            deviceManager.stopScanning();
//        }
    }

//    @Override
//    public void didDiscoverDevice(EmpaticaDevice bluetoothDevice, String deviceName, int rssi, boolean allowed) {
//        Log.i(TAG, "didDiscoverDevice" + deviceName + "allowed: " + allowed);
//
//        if (allowed) {
//            // Stop scanning. The first allowed device will do.
//            deviceManager.stopScanning();
//            try {
//                // Connect to the device
//                deviceManager.connectDevice(bluetoothDevice);
//                updateLabel(deviceNameLabel, "To: " + deviceName);
//            } catch (ConnectionNotAllowedException e) {
//                // This should happen only if you try to connect when allowed == false.
//                Toast.makeText(MainActivity.this, "Sorry, you can't connect to this device", Toast.LENGTH_SHORT).show();
//                Log.e(TAG, "didDiscoverDevice" + deviceName + "allowed: " + allowed + " - ConnectionNotAllowedException", e);
//            }
//        }
//    }
//
//    @Override
//    public void didFailedScanning(int errorCode) {
//
//        /*
//         A system error occurred while scanning.
//         @see https://developer.android.com/reference/android/bluetooth/le/ScanCallback
//        */
//        switch (errorCode) {
//            case ScanCallback.SCAN_FAILED_ALREADY_STARTED:
//                Log.e(TAG,"Scan failed: a BLE scan with the same settings is already started by the app");
//                break;
//            case ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED:
//                Log.e(TAG,"Scan failed: app cannot be registered");
//                break;
//            case ScanCallback.SCAN_FAILED_FEATURE_UNSUPPORTED:
//                Log.e(TAG,"Scan failed: power optimized scan feature is not supported");
//                break;
//            case ScanCallback.SCAN_FAILED_INTERNAL_ERROR:
//                Log.e(TAG,"Scan failed: internal error");
//                break;
//            default:
//                Log.e(TAG,"Scan failed with unknown error (errorCode=" + errorCode + ")");
//                break;
//        }
//    }
//
//    @Override
//    public void didRequestEnableBluetooth() {
//        // Request the user to enable Bluetooth
//        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
//    }
//
//    @Override
//    public void bluetoothStateChanged() {
//        // E4link detected a bluetooth adapter change
//        // Check bluetooth adapter and update your UI accordingly.
//        boolean isBluetoothOn = BluetoothAdapter.getDefaultAdapter().isEnabled();
//        Log.i(TAG, "Bluetooth State Changed: " + isBluetoothOn);
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // The user chose not to enable Bluetooth
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            // You should deal with this
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

//    @Override
//    public void didUpdateSensorStatus(@EmpaSensorStatus int status, EmpaSensorType type) {
//
//        didUpdateOnWristStatus(status);
//    }
//
//    @Override
//    public void didUpdateStatus(EmpaStatus status) {
//        // Update the UI
//        updateLabel(statusLabel, status.name());
//
//        // The device manager is ready for use
//        if (status == EmpaStatus.READY) {
//            updateLabel(statusLabel, status.name() + " - Turn on your device");
//            // Start scanning
//            deviceManager.startScanning();
//            // The device manager has established a connection
//
//            hide();
//
//        } else if (status == EmpaStatus.CONNECTED) {
//
//            show();
//            // The device manager disconnected from a device
//        } else if (status == EmpaStatus.DISCONNECTED) {
//
//            updateLabel(deviceNameLabel, "");
//
//            hide();
//        }
//    }
//
//    @Override
//    public void didReceiveAcceleration(int x, int y, int z, double timestamp) {
////        updateLabel(accel_xLabel, "" + x);
////        updateLabel(accel_yLabel, "" + y);
////        updateLabel(accel_zLabel, "" + z);
//    }
//
//    @Override
//    public void didReceiveBVP(float bvp, double timestamp) {
//
//        //updateLabel(bvpLabel, "" + bvp);
//    }
//
//    @Override
//    public void didReceiveBatteryLevel(float battery, double timestamp) {
//        updateLabel(batteryLabel, String.format("%.0f %%", battery * 100));
//    }
//
//    @Override
//    public void didReceiveGSR(float gsr, double timestamp) {
//        updateLabel(edaLabel, "" + gsr);
//        updateProgress(gsr);
//
//    }
//
//    @Override
//    public void didReceiveIBI(float ibi, double timestamp) {
////        updateLabel(ibiLabel, "" + ibi);
//    }
//
//    @Override
//    public void didReceiveTemperature(float temp, double timestamp) {
////        updateLabel(temperatureLabel, "" + temp);
//    }

    // Update a label with some text, making sure this is run in the UI thread
//    private void updateLabel(final TextView label, final String text) {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                if( label != null){
//                    String lableText = text;
//                    if(lableText != null && lableText.length() > 4)
//                        lableText = lableText.substring(0,4);
//                    label.setText(lableText);
//                }
//            }
//        });
//    }
//
//    private void updateProgress(Float progress){
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                edaProgress.setProgress(progress.intValue());
//            }
//        });
//    }
//
//    @Override
//    public void didReceiveTag(double timestamp) {
//
//    }
//
//    @Override
//    public void didEstablishConnection() {
//
//        show();
//    }
//
//    @Override
//    public void didUpdateOnWristStatus(@EmpaSensorStatus final int status) {
//
//        runOnUiThread(new Runnable() {
//
//            @Override
//            public void run() {
//
//                if (status == EmpaSensorStatus.ON_WRIST) {
//
////                    ((TextView) findViewById(R.id.wrist_status_label)).setText("ON WRIST");
//                }
//                else {
//
////                    ((TextView) findViewById(R.id.wrist_status_label)).setText("NOT ON WRIST");
//                }
//            }
//        });
//    }
//
//    void show() {
//
//        runOnUiThread(new Runnable() {
//
//            @Override
//            public void run() {
//                if(dataCnt != null){
//                    dataCnt.setVisibility(View.VISIBLE);
//                }
//            }
//        });
//    }
//
//    void hide() {
//
//        runOnUiThread(new Runnable() {
//
//            @Override
//            public void run() {
//                if(dataCnt != null) {
//                    dataCnt.setVisibility(View.INVISIBLE);
//                }
//            }
//        });
//    }
}
