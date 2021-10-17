package com.restart.myapplicationactivitytest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.restart.myapplicationactivitytest.databinding.FragmentFirstBinding;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import common.Constants;
import common.LocationType;
import controllers.EventsHandler;
import models.SettingsModel;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private EventsHandler handler;
    private SettingsModel settings;
    private BroadcastReceiver broadcastReceiver;
    private AtomicLong loopCounter = new AtomicLong();

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

        registerReceiver();

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
        if(broadcastReceiver != null) {
            getActivity().unregisterReceiver(broadcastReceiver);
        }
    }

    private void registerReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long loopCount = loopCounter.getAndIncrement();
                if( loopCount% 20 == 0) {
                    String param = intent.getStringExtra(Constants.EMPATICA_PARAM);
                    Float value = intent.getFloatExtra(Constants.EMPATICA_VALUE, -1);

                    switch (param) {
                        case Constants.EDA:
                            updateLabel((TextView) getActivity().findViewById(R.id.txt_eda), value.toString());
                            updateProgress(value);
                            handler.onEDAUpdate(value);
                            break;
                        case Constants.BPM:
                            updateLabel((TextView) getActivity().findViewById(R.id.txt_bpm), value.toString());
                            break;
                        case Constants.HRV:
                            //updateLabel((TextView)getActivity().findViewById(R.id.txt_hrv),value.toString());
                            break;
                        case Constants.BATTERY:
                            //updateLabel((TextView)getActivity().findViewById(R.id.txt_battery),value.toString());
                            break;
                    }

                   if(loopCount == 10000000) {
                       loopCounter.set(0);
                   }
                }
            }
        };


        this.getActivity().registerReceiver(broadcastReceiver, new IntentFilter(Constants.EMPATICA_MONITOR));
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
                ProgressBar edaProgress = (ProgressBar) getActivity().findViewById(R.id.pb_eda);
                edaProgress.setProgress(progress.intValue());
            }
        });
    }

}