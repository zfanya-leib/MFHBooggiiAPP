package com.restart.myapplicationactivitytest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.SignInUIOptions;
import com.amazonaws.mobile.client.UserStateDetails;
import com.restart.myapplicationactivitytest.databinding.ActivityMainBinding;

import java.util.concurrent.TimeUnit;

import common.Constants;


public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    private static final String TAG = "MainActivity";
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PERMISSION_ACCESS_COARSE_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authenticate();
//        try {
//            TimeUnit.SECONDS.sleep(2);
//        } catch (Exception e) {
//            Log.e(TAG, e.toString());
//        }
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        startService();
    }

    private void authenticate() {
        AWSMobileClient.getInstance().initialize(getApplicationContext(), new Callback<UserStateDetails>() {
            @Override
            public void onResult(UserStateDetails userStateDetails) {
                switch (userStateDetails.getUserState()){
                    case SIGNED_IN:
                        break;
                    case SIGNED_OUT:
                        try {
                            AWSMobileClient.getInstance().showSignIn(MainActivity.this, SignInUIOptions.builder().build());
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
//        }

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
