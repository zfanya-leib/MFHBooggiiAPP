package com.restart.myapplicationactivitytest;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
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
import androidx.navigation.fragment.NavHostFragment;

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
            Bundle savedInstanceState
    ) {

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
                handler.setLocation(LocationType.INDOOR);
            }
        });

        binding.btnImgOutdoor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.setLocation(LocationType.OUTDOOR);
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
                long loopCount = loopCounter.getAndIncrement();

                String param = intent.getStringExtra(Constants.EMPATICA_PARAM);
                Float value = intent.getFloatExtra(Constants.EMPATICA_VALUE, -1);

                switch (param) {
                    case Constants.EDA:
                        //if( loopCount% 10 == 0) {
                        updateLabel((TextView) getActivity().findViewById(R.id.txt_eda), value.toString());
                        updateProgress(value);
                        //}
//                        if(loopCount == 10000000) {
//                            loopCounter.set(0);
//                        }
                        handler.onEDAUpdate(value);
                        break;
                    case Constants.BPM:
                        updateLabel((TextView) getActivity().findViewById(R.id.txt_bpm), value.toString());
                        break;
                    case Constants.HRV:
                        updateLabel((TextView)getActivity().findViewById(R.id.txt_hrv),value.toString());
                        break;
                    case Constants.BATTERY:
                        updateLabel((TextView)getActivity().findViewById(R.id.txt_battery),String.format("%.0f %%", value));
                        break;
                    case Constants.BLUETOOTH:
                        BluetoothAdapter.getDefaultAdapter().enable();
                        break;
                    case Constants.DISCONNECTED:
                        if(!isInitial)
                            showDisconnect();
                        break;
                    case Constants.CONNECTED:
                        showConnected();
                        break;
                }
            }
        };


        this.getActivity().registerReceiver(broadcastReceiver, new IntentFilter(Constants.EMPATICA_MONITOR));
    }
    private void showDisconnect(){
        ImageView disConnectedIcon =(ImageView)getActivity().findViewById(R.id.img_disconnected);
        ImageView connectedIcon = (ImageView)getActivity().findViewById(R.id.img_connected);

        if( connectedIcon != null && disConnectedIcon != null ) {
            connectedIcon.setVisibility(View.INVISIBLE);
            disConnectedIcon.setVisibility(View.VISIBLE);
            disConnectedIcon.startAnimation(getAnimation());

            if(!this.isInitial) {
                String defRingtone = "android.resource://" + getActivity().getPackageName() + "/" + R.raw.disconnected;
                this.disconectedRingtone = RingtoneManager.getRingtone(getActivity(), Uri.parse(defRingtone));
                disconectedRingtone.setVolume(90);
                disconectedRingtone.play();
                disconectedRingtone.setLooping(true);
            }
        }

    }

    private void showConnected(){
        ImageView disConnectedIcon =(ImageView)getActivity().findViewById(R.id.img_disconnected);
        ImageView connectedIcon = (ImageView)getActivity().findViewById(R.id.img_connected);
        if( connectedIcon != null && disConnectedIcon != null ) {
            if(this.disconectedRingtone != null && this.disconectedRingtone.isPlaying()){
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
                binding.pbEda.setProgress(progress.intValue());
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