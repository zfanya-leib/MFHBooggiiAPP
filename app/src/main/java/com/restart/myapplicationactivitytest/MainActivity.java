package com.restart.myapplicationactivitytest;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import androidx.core.app.ActivityCompat;
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
import android.widget.Toast;

import com.empatica.empalink.ConnectionNotAllowedException;
import com.empatica.empalink.EmpaDeviceManager;
import com.empatica.empalink.EmpaticaDevice;
import com.empatica.empalink.config.EmpaSensorStatus;
import com.empatica.empalink.config.EmpaSensorType;
import com.empatica.empalink.config.EmpaStatus;
import com.empatica.empalink.delegate.EmpaDataDelegate;
import com.empatica.empalink.delegate.EmpaStatusDelegate;

import java.util.Stack;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        startService();
    }

        hrvLabel = (TextView) findViewById(R.id.txt_hrv);
        batteryLabel = (TextView) findViewById(R.id.txt_battery);
//        deviceNameLabel = (TextView) findViewById(R.id.deviceName);
        initEmpaticaDeviceManager();

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
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
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

//    @Override
//    public void didUpdateSensorStatus(@EmpaSensorStatus int status, EmpaSensorType type) {
//
//        didUpdateOnWristStatus(status);
//    }

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
        Double sum = ibiArray.stream().mapToDouble(Double::valueOf).sum();
        Double bpm = 60 / sum * ibiArray.size();
        Log.i(TAG, "bpm: " + bpm);

        updateLabel(bpmLabel, "" + bpm.intValue());

        float rmssdTotal = 0;

        for (int i = 1; i < ibiArray.size(); i++) {
            rmssdTotal += pow((ibiArray.get(i - 1) - ibiArray.get(i)) * 1000, 2);
        }

        float rmssd = (float) sqrt(rmssdTotal / (ibiArray.size() - 1));
        Log.i(TAG, "rmssd: " + rmssd);
        updateLabel(hrvLabel, "" + (int) rmssd);

//        float rrTotal=0;
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
//        float sdnn = (float) sqrt(sdnnTotal / (ibiArray.size() - 1));
//        updateLabel(hrvLabel, "" + (int) sdnn);
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
