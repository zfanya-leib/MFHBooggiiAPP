package com.restart.myapplicationactivitytest;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.PreferenceManager;

import com.empatica.empalink.config.EmpaStatus;
import com.restart.myapplicationactivitytest.databinding.FragmentFirstBinding;

import java.util.concurrent.atomic.AtomicLong;

import common.Constants;
import common.LocationType;
import controllers.EventsHandler;
import models.SettingsModel;

public class FirstFragment extends Fragment {

    private static final int REQUEST_ENABLE_BT = 1;
    private FragmentFirstBinding binding;
    private EventsHandler handler;
    private SettingsModel settings;
    private BroadcastReceiver broadcastReceiver;
    private AtomicLong loopCounter = new AtomicLong();
    Ringtone disconectedRingtone;
    private Boolean isInitial = true;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        this.handler = new EventsHandler(getActivity());
        this.settings = new SettingsModel();

        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.btnEventsOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });

        binding.btnImgSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("message", "From Activity");
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SettingsFragment);
            }
        });

        binding.btnImgIndoor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String eventName = Constants.OUTSIDE; // The report to db is only is_outside
                if (v.isSelected() == false) {
                    v.setSelected(true);
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                    preferences.edit().putBoolean(Constants.OUTSIDE, false).apply();
                    v.setBackgroundTintList(getResources().getColorStateList(R.color.green_background));
                    binding.btnImgOutdoor.setSelected(false);
                    binding.btnImgOutdoor.setBackgroundTintList(getResources().getColorStateList(R.color.white));
                    handler.writeEndEventToDb(eventName);
                    handler.setLocation(LocationType.INDOOR);
                }
            }
        });

        binding.btnImgOutdoor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String eventName = Constants.OUTSIDE;
                if (v.isSelected() == false) {
                    v.setSelected(true);
                    v.setBackgroundTintList(getResources().getColorStateList(R.color.green_background));
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                    preferences.edit().putBoolean(Constants.OUTSIDE, true).apply();
                    binding.btnImgIndoor.setSelected(false);
                    binding.btnImgIndoor.setBackgroundTintList(getResources().getColorStateList(R.color.white));
                    handler.writeStartEventToDb(eventName);
                    handler.setLocation(LocationType.OUTDOOR);
                }
            }
        });

        binding.btnImgInterventionNeeded.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.isSelected() == false) {
                    v.setSelected(true);
                    v.setBackgroundTintList(getResources().getColorStateList(R.color.green_background));
                    handler.writeStartEventToDb(Constants.INTERVENTION_NEEDED);
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                    preferences.edit().putBoolean(Constants.INTERVENTION_NEEDED, true).apply();
                } else {
                    v.setSelected(false);
                    v.setBackgroundTintList(getResources().getColorStateList(R.color.white));
                    handler.writeEndEventToDb(Constants.INTERVENTION_NEEDED);
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                    preferences.edit().putBoolean(Constants.INTERVENTION_NEEDED, false).apply();
                }
            }
        });

        binding.btnImgSevereAttack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.isSelected() == false) {
                    v.setSelected(true);
                    v.setBackgroundTintList(getResources().getColorStateList(R.color.green_background));
                    handler.writeStartEventToDb(Constants.MAJOR_EVENT);
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                    preferences.edit().putBoolean(Constants.MAJOR_EVENT, true).apply();
                } else {
                    v.setSelected(false);
                    v.setBackgroundTintList(getResources().getColorStateList(R.color.white));
                    handler.writeEndEventToDb(Constants.MAJOR_EVENT);
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                    preferences.edit().putBoolean(Constants.MAJOR_EVENT, false).apply();
                }
            }
        });

        binding.btnImgSos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.onSOS();
            }
        });

        binding.imgBtnCall1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.makePhoneCall1();
            }
        });

        binding.imgBtnCall2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.makePhoneCall2();
            }
        });

        binding.imgBtnDrug.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.onDrugTaken();
            }
        });

        binding.imgBtnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.sendLocation();
            }
        });

        binding.imgBtnLocation2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.sendLocation2();
            }
        });

        binding.imgDisconnected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(disconectedRingtone != null && disconectedRingtone.isPlaying()){
                    disconectedRingtone.stop();
                }
                else{
                    if(isInitial) {
                        Intent serviceIntent = new Intent(getActivity(), EmpaticaConnectionService.class);
                        serviceIntent.putExtra("inputExtra", "Empatica Connection Service");
                        serviceIntent.setAction(Constants.ACTION_START_E4_CONNECT);
                        ContextCompat.startForegroundService(getActivity(), serviceIntent);
                        showDisconnect();
                    }
                }
            }
        });
        registerReceiver();
    }


    @Override
    public void onResume() {
        super.onResume();
        Intent serviceIntent = new Intent(getActivity(), EmpaticaConnectionService.class);
        serviceIntent.putExtra("inputExtra", "Empatica Connection Service");
        serviceIntent.setAction(Constants.ACTION_SERVICE_CONNECTION_STATUS);
        ContextCompat.startForegroundService(getActivity(), serviceIntent);
        setLastButtonsState();
        if (EmpaticaConnectionService.connectionStatus == EmpaStatus.CONNECTED)
            setLastMeasurements();
    }

    private void setLastMeasurements() {
        try {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            TextView txtBattery = (TextView)getActivity().findViewById(R.id.txt_battery);
            Log.i("setLastUIState","preferences battery: " + preferences.getString(Constants.BATTERY, "---"));
            String batteryLevel = preferences.getString(Constants.BATTERY, "---");
            updateLabel(txtBattery, batteryLevel);
            TextView txtBpm = (TextView)getActivity().findViewById(R.id.txt_bpm);
            String bpm = preferences.getString(Constants.BPM, "---");
            updateLabel(txtBpm, bpm);
            TextView txtHrv = (TextView)getActivity().findViewById(R.id.txt_hrv);
            String hrv = preferences.getString(Constants.HRV, "---");
            updateLabel(txtHrv, hrv);
            TextView txtEda = (TextView)getActivity().findViewById(R.id.txt_eda);
            String eda = preferences.getString(Constants.EDA, "---");
            updateLabel(txtEda, eda);
        }
        catch (Exception ex){
            Log.e("First Fragment", "setLastUI failed with error: " + ex.getLocalizedMessage());
        }
    }

    private void setLastButtonsState() {
        try {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            Boolean isOutside = preferences.getBoolean(Constants.OUTSIDE, false);
            binding.btnImgIndoor.setSelected(!isOutside);
            binding.btnImgOutdoor.setSelected(isOutside);
            if (isOutside) {
                binding.btnImgOutdoor.setBackgroundTintList(getResources().getColorStateList(R.color.green_background));
                binding.btnImgIndoor.setBackgroundTintList(getResources().getColorStateList(R.color.white));
            } else {
                binding.btnImgIndoor.setBackgroundTintList(getResources().getColorStateList(R.color.green_background));
                binding.btnImgOutdoor.setBackgroundTintList(getResources().getColorStateList(R.color.white));
            }
            Boolean isInterventionNeeded = preferences.getBoolean(Constants.INTERVENTION_NEEDED, false);
            binding.btnImgInterventionNeeded.setSelected(isInterventionNeeded);
            if (isInterventionNeeded)
                binding.btnImgInterventionNeeded.setBackgroundTintList(getResources().getColorStateList(R.color.green_background));
            Boolean isMajorEvent = preferences.getBoolean(Constants.MAJOR_EVENT, false);
            binding.btnImgSevereAttack.setSelected(isMajorEvent);
            if (isMajorEvent)
                binding.btnImgSevereAttack.setBackgroundTintList(getResources().getColorStateList(R.color.green_background));
        }
        catch (Exception ex){
            Log.e("First Fragment", "setLastUI failed with error: " + ex.getLocalizedMessage());
        }
    }

    public EventsHandler getHandler(){
        return this.handler;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void registerReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try{
                    String param = intent.getStringExtra(Constants.EMPATICA_PARAM);
                    Float value = intent.getFloatExtra(Constants.EMPATICA_VALUE, -1);

                    if(param == null){
                        return;
                    }

                    switch (param) {
                        case Constants.EDA:
                            TextView txtEDA =(TextView) getActivity().findViewById(R.id.txt_eda);
                            if( txtEDA != null && value != null && value != -1 ) {
                                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                                preferences.edit().putString(Constants.EDA, value.toString()).apply();
                                updateLabel(txtEDA, value.toString());
                                updateProgress(value);
                                handler.onEDAUpdate(value);
                            }
                            break;
                        case Constants.BPM:
                            TextView txtBpm = (TextView) getActivity().findViewById(R.id.txt_bpm);
                            if(txtBpm != null && value != null && value != -1) {
                                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                                preferences.edit().putString(Constants.BPM, String.valueOf(value.intValue())).apply();
                                updateLabel(txtBpm, String.valueOf(value.intValue()));
                            }
                            break;
                        case Constants.HRV:
                            TextView txtHRV = (TextView)getActivity().findViewById(R.id.txt_hrv);
                            if( txtHRV != null && value != null && value != -1) {
                                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                                preferences.edit().putString(Constants.HRV, String.valueOf(value.intValue())).apply();
                                updateLabel(txtHRV, String.valueOf(value.intValue()));
                            }
                            break;
                        case Constants.BATTERY:
                            Log.i("First Fragment","preferences battery: " + String.format("%.0f %%", value));
                            TextView txtBattery = (TextView)getActivity().findViewById(R.id.txt_battery);
                            if(txtBattery != null && value != null && value != -1) {
                                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                                preferences.edit().putString(Constants.BATTERY, String.valueOf(value.intValue()).concat("%")).apply();
                                updateLabel(txtBattery, String.valueOf(value.intValue()).concat("%"));
                                Log.i("First Fragment","preferences battery: " + preferences.getString(Constants.BATTERY, "---"));
                            }
                            break;
                        case Constants.BLUETOOTH:
                            BluetoothAdapter.getDefaultAdapter().enable();
                            break;
                        case Constants.DISCONNECTED:
                            resetMeasurements();
                            if(!isInitial) {
                                showDisconnect();
                            }
                            break;
                        case Constants.CONNECTED:
                            showConnected();
                            break;
                    }
                }
                catch(Exception e){
                    Log.e("First Fragment","broadcast ui updated failed with error" + e.getLocalizedMessage());
                }
            }
        };

        LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(broadcastReceiver, new IntentFilter(Constants.EMPATICA_MONITOR));
//        this.getActivity().registerReceiver(broadcastReceiver, new IntentFilter(Constants.EMPATICA_MONITOR));
    }

    private void resetMeasurements() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        preferences.edit().putString(Constants.EDA, "---").apply();
        TextView txtEda =(TextView) getActivity().findViewById(R.id.txt_eda);
        updateLabel(txtEda, "---");
        preferences.edit().putString(Constants.BPM, "---").apply();
        TextView txtBPM =(TextView) getActivity().findViewById(R.id.txt_bpm);
        updateLabel(txtBPM, "---");
        preferences.edit().putString(Constants.HRV, "---").apply();
        TextView txtHrv =(TextView) getActivity().findViewById(R.id.txt_hrv);
        updateLabel(txtHrv, "---");
        preferences.edit().putString(Constants.BATTERY, "---").apply();
        TextView txtBATTERY =(TextView) getActivity().findViewById(R.id.txt_battery);
        updateLabel(txtBATTERY, "---");
    }

    private void showDisconnect(){
        try {
            ImageView disConnectedIcon = (ImageView) getActivity().findViewById(R.id.img_disconnected);
            ImageView connectedIcon = (ImageView) getActivity().findViewById(R.id.img_connected);

            if (connectedIcon != null && disConnectedIcon != null) {
                connectedIcon.setVisibility(View.INVISIBLE);
                disConnectedIcon.setVisibility(View.VISIBLE);
                disConnectedIcon.startAnimation(getAnimation());

                if (!this.isInitial) {
                    String defRingtone = "android.resource://" + getActivity().getPackageName() + "/" + R.raw.disconnected;
                    this.disconectedRingtone = RingtoneManager.getRingtone(getActivity(), Uri.parse(defRingtone));
                    disconectedRingtone.setVolume(90);
                    disconectedRingtone.play();
                    disconectedRingtone.setLooping(true);
                }
            }
        }
        catch (Exception e){
            Log.e("First Fragment", "show disconnected failed with error: " + e.getLocalizedMessage());
        }

    }

    private void showConnected(){
        try {
            ImageView disConnectedIcon = (ImageView) getActivity().findViewById(R.id.img_disconnected);
            ImageView connectedIcon = (ImageView) getActivity().findViewById(R.id.img_connected);
            if (connectedIcon != null && disConnectedIcon != null) {
                if (this.disconectedRingtone != null && this.disconectedRingtone.isPlaying()) {
                    this.disconectedRingtone.stop();
                }
                this.isInitial = false;
                connectedIcon.setVisibility(View.VISIBLE);
                disConnectedIcon.setVisibility(View.INVISIBLE);
                if (disConnectedIcon.getAnimation() != null) {
                    disConnectedIcon.getAnimation().cancel();
                }
            }
        }
        catch (Exception e) {
            Log.e("First Fragment", "show connectee failed with error: " + e.getLocalizedMessage());
        }
    }
    // Update a label with some text, making sure this is run in the UI thread
    private void updateLabel(final TextView label, final String text) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if( label != null){
                    String lableText = text;
                    if(lableText != null && lableText.length() > 4)
                        lableText = lableText.substring(0,4);
                    label.setText(lableText);
                }
            }
        });
    }

    private void updateProgress(Float progress){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ProgressBar pb = (ProgressBar) getActivity().findViewById(R.id.pb_eda);
                if(pb != null){
                  pb.setProgress(progress.intValue(), true);
                }
                //binding.pbEda.setProgress(progress.intValue());
            }
        });
    }

    private AlphaAnimation getAnimation(){
        AlphaAnimation anim = new AlphaAnimation(0.0f, 0.7f);
        anim.setDuration(800); //You can manage the blinking time with this parameter
        anim.setStartOffset(40);
        anim.setRepeatMode(Animation.ZORDER_NORMAL);
        anim.setRepeatCount(Animation.INFINITE);

        return anim;
    }

}