package com.restart.myapplicationactivitytest;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.ScanCallback;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.SignInUIOptions;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amplifyframework.AmplifyException;
import com.amplifyframework.api.aws.AWSApiPlugin;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.core.model.temporal.Temporal;
import com.amplifyframework.datastore.AWSDataStorePlugin;
import com.amplifyframework.datastore.generated.model.Measurement;
import com.empatica.empalink.ConnectionNotAllowedException;
import com.empatica.empalink.EmpaDeviceManager;
import com.empatica.empalink.EmpaticaDevice;
import com.empatica.empalink.config.EmpaSensorStatus;
import com.empatica.empalink.config.EmpaSensorType;
import com.empatica.empalink.config.EmpaStatus;
import com.empatica.empalink.delegate.EmpaDataDelegate;
import com.empatica.empalink.delegate.EmpaStatusDelegate;
import com.restart.myapplicationactivitytest.databinding.ActivityMainBinding;

import java.time.Instant;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements EmpaDataDelegate, EmpaStatusDelegate {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    private static final String TAG = "MainActivity";
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PERMISSION_ACCESS_COARSE_LOCATION = 1;
    private static final String EMPATICA_API_KEY = "1a25b9decfbd48cb9c833e0a09851279";
    private EmpaDeviceManager deviceManager = null;
    private TextView bpmLabel;
    private TextView hrvLabel;
    private int max_last_ibi_samples_to_cache = 15;
    private Stack<Float> ibiArray = new SizedStack<Float>(max_last_ibi_samples_to_cache);
    private TextView edaLabel;
    private ProgressBar edaProgress;
    private TextView batteryLabel;
    private TextView statusLabel;
//    private TextView deviceNameLabel;
    private LinearLayout dataCnt;
    private String userName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        statusLabel = (TextView) findViewById(R.id.txt_status);
//        dataCnt = (LinearLayout) findViewById(R.id.dataArea);
        bpmLabel = (TextView) findViewById(R.id.txt_bpm);
        edaLabel = (TextView) findViewById(R.id.txt_eda);
        edaProgress = (ProgressBar)findViewById(R.id.pb_eda);

        hrvLabel = (TextView) findViewById(R.id.txt_hrv);
        batteryLabel = (TextView) findViewById(R.id.txt_battery);
//        deviceNameLabel = (TextView) findViewById(R.id.deviceName);

        authenticate();
        init_amplify();
//        test_db();
        initEmpaticaDeviceManager();
    }

    private void authenticate() {
        AWSMobileClient.getInstance().initialize(getApplicationContext(), new Callback<UserStateDetails>() {
            @Override
            public void onResult(UserStateDetails userStateDetails) {
                switch (userStateDetails.getUserState()){
                    case SIGNED_IN:
                        userName = AWSMobileClient.getInstance().getUsername();
                        Log.i(TAG, "userName: " + userName);
                        break;
                    case SIGNED_OUT:
                        try {
                            AWSMobileClient.getInstance().showSignIn(MainActivity.this, SignInUIOptions.builder().build());
                            userName = AWSMobileClient.getInstance().getUsername();
                            Log.i(TAG, "userName: " + userName);
                        } catch (Exception e) {
                            Log.e(TAG, e.toString());
                        }
                        break;
                    default:
                        AWSMobileClient.getInstance().signOut();
                        break;
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e("INIT", e.toString());
            }
        });
    }

    private void init_amplify() {
        try {
            Amplify.addPlugin(new AWSDataStorePlugin());
            Amplify.addPlugin(new AWSApiPlugin());
            Amplify.configure(getApplicationContext());
            Log.i(TAG, "Initialized Amplify");
        } catch (AmplifyException error) {
            Log.e(TAG, "Could not initialize Amplify", error);
        }
    }

    private void test_db() {
        // wait for authentication to set the username
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        writeIbiToDb(0.123, Instant.now().toEpochMilli() / 1000);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSION_ACCESS_COARSE_LOCATION:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted, yay!
                    initEmpaticaDeviceManager();
                } else {
                    // Permission denied, boo!
                    final boolean needRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION);
                    new AlertDialog.Builder(this)
                            .setTitle("Permission required")
                            .setMessage("Without this permission bluetooth low energy devices cannot be found, allow it in order to connect to the device.")
                            .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // try again
                                    if (needRationale) {
                                        // the "never ask again" flash is not set, try again with permission request
                                        initEmpaticaDeviceManager();
                                    } else {
                                        // the "never ask again" flag is set so the permission requests is disabled, try open app settings to enable the permission
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                                        intent.setData(uri);
                                        startActivity(intent);
                                    }
                                }
                            })
                            .setNegativeButton("Exit application", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // without permission exit is the only way
                                    finish();
                                }
                            })
                            .show();
                }
                break;
        }
    }

    private void initEmpaticaDeviceManager() {
        // Android 6 (API level 23) now require ACCESS_COARSE_LOCATION permission to use BLE
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, REQUEST_PERMISSION_ACCESS_COARSE_LOCATION);
        } else {

            if (TextUtils.isEmpty(EMPATICA_API_KEY)) {
                new AlertDialog.Builder(this)
                        .setTitle("Warning")
                        .setMessage("Please insert your API KEY")
                        .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // without permission exit is the only way
                                finish();
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
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (deviceManager != null) {
            deviceManager.cleanUp();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (deviceManager != null) {
            deviceManager.stopScanning();
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
//                updateLabel(deviceNameLabel, "To: " + deviceName);
            } catch (ConnectionNotAllowedException e) {
                // This should happen only if you try to connect when allowed == false.
                Toast.makeText(MainActivity.this, "Sorry, you can't connect to this device", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "didDiscoverDevice" + deviceName + "allowed: " + allowed + " - ConnectionNotAllowedException", e);
            }
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
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    @Override
    public void bluetoothStateChanged() {
        // E4link detected a bluetooth adapter change
        // Check bluetooth adapter and update your UI accordingly.
        boolean isBluetoothOn = BluetoothAdapter.getDefaultAdapter().isEnabled();
        Log.i(TAG, "Bluetooth State Changed: " + isBluetoothOn);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // The user chose not to enable Bluetooth
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            // You should deal with this
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void didUpdateSensorStatus(@EmpaSensorStatus int status, EmpaSensorType type) {

        didUpdateOnWristStatus(status);
    }

    @Override
    public void didUpdateStatus(EmpaStatus status) {
        // Update the UI
        updateLabel(statusLabel, status.name());

        // The device manager is ready for use
        if (status == EmpaStatus.READY) {
            updateLabel(statusLabel, status.name() + " - Turn on your device");
            // Start scanning
            deviceManager.startScanning();
            // The device manager has established a connection

//            hide();

        } else if (status == EmpaStatus.CONNECTED) {

//            show();
            // The device manager disconnected from a device
        } else if (status == EmpaStatus.DISCONNECTED) {

//            updateLabel(deviceNameLabel, "");

//            hide();
        }
    }

    @Override
    public void didReceiveAcceleration(int x, int y, int z, double timestamp) {
//        updateLabel(accel_xLabel, "" + x);
//        updateLabel(accel_yLabel, "" + y);
//        updateLabel(accel_zLabel, "" + z);
    }

    @Override
    public void didReceiveBVP(float bvp, double timestamp) {
        //updateLabel(bvpLabel, "" + bvp);
    }

    @Override
    public void didReceiveBatteryLevel(float battery, double timestamp) {
        Log.i(TAG, "didReceiveBatteryLevel" + String.format("%.0f %%", battery * 100));
        updateLabel(batteryLabel, String.format("%.0f %%", battery * 100));
    }

    @Override
    public void didReceiveGSR(float gsr, double timestamp) {
        updateLabel(edaLabel, String.format("%.2f", gsr));
        updateProgress(gsr);
    }

    @Override
    public void didReceiveIBI(float ibi, double timestamp) {
        Log.i(TAG, "didReceiveIBI" + ibi);
        ibiArray.push(ibi);
        updateLabel(bpmLabel, "" + calcBpm().intValue());
        updateLabel(hrvLabel, "" + (int)calcHrv());
        writeIbiToDb((double) ibi, timestamp);
    }

    private void writeIbiToDb(double ibi, double timestamp) {
        Measurement item = Measurement.builder()
                .name("IBI")
                .value(ibi)
                .timestamp(new Temporal.Timestamp((long) timestamp, TimeUnit.SECONDS))
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

    @Override
    public void didReceiveTemperature(float temp, double timestamp) {
//        updateLabel(temperatureLabel, "" + temp);
    }

    // Update a label with some text, making sure this is run in the UI thread
    private void updateLabel(final TextView label, final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if( label != null){
                    label.setText(text);
                }
            }
        });
    }

    private void updateProgress(Float progress){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                edaProgress.setProgress(progress.intValue());
            }
        });
    }

    @Override
    public void didReceiveTag(double timestamp) {

    }

    @Override
    public void didEstablishConnection() {

//        show();
    }

    @Override
    public void didUpdateOnWristStatus(@EmpaSensorStatus final int status) {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                if (status == EmpaSensorStatus.ON_WRIST) {

//                    ((TextView) findViewById(R.id.wrist_status_label)).setText("ON WRIST");
                }
                else {

//                    ((TextView) findViewById(R.id.wrist_status_label)).setText("NOT ON WRIST");
                }
            }
        });
    }

    void show() {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if(dataCnt != null){
                    dataCnt.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    void hide() {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if(dataCnt != null) {
                    dataCnt.setVisibility(View.INVISIBLE);
                }
            }
        });
    }
}
