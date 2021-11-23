package com.restart.myapplicationactivitytest;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.PreferenceManager;

import com.restart.myapplicationactivitytest.databinding.FragmentSecondBinding;

import common.Constants;
import controllers.EventsHandler;

public class SecondFragment extends Fragment {

    private FragmentSecondBinding binding;
    private EventsHandler handler;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        this.handler = new EventsHandler(getActivity());
        binding = FragmentSecondBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
          binding.btnBack.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                  NavHostFragment.findNavController(SecondFragment.this)
                        .navigate(R.id.action_SecondFragment_to_FirstFragment);
              }
          });

        binding.btnImgIncar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String eventName = Constants.IN_CAR;
                handleEvent(v, eventName);
            }
        });

        binding.btnImgClosedSpace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String eventName = Constants.CLOSED_SPACE;
                handleEvent(v, eventName);
            }
        });

        binding.btnImgCrowded.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String eventName = Constants.CROWDED;
                handleEvent(v, eventName);
            }
        });

        binding.btnImgDriving.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String eventName = Constants.DRIVING;
                handleEvent(v, eventName);
            }
        });

        binding.btnImgMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String eventName = Constants.MEDIA;
                handleEvent(v, eventName);
            }
        });

        binding.btnImgResting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String eventName = Constants.LAY_DOWN;
                handleEvent(v, eventName);
            }
        });

        binding.btnImgSleep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String eventName = Constants.SLEEPING;
                handleEvent(v, eventName);
            }
        });

        binding.btnImgWalking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String eventName = Constants.WALKING;
                handleEvent(v, eventName);
            }
        });

        binding.imgBtnDrug.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String eventName = Constants.MEDICATION;
                handleEvent(v, eventName);
            }
        });

        binding.imgWakeUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String eventName = Constants.WAKE_UP;
                handleEvent(v, eventName);
            }
        });

//        binding.buttonSecond.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                NavHostFragment.findNavController(SecondFragment.this)
//                        .navigate(R.id.action_SecondFragment_to_FirstFragment);
//            }
//        });
    }

    private void handleEvent(@NonNull View v, String eventName) {
        try {
            if (v.isSelected() == false) {
                handler.writeStartEventToDb(eventName);
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                preferences.edit().putBoolean(eventName, true).apply();
            } else {
                handler.writeEndEventToDb(eventName);
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                preferences.edit().putBoolean(eventName, false).apply();
            }
            setButtonSelection(v, !v.isSelected());
        }
        catch (Exception e){
            Log.e("Second Fragment", "handleEvent failed with error: " + e.getLocalizedMessage());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        setButtonSelection(binding.btnImgClosedSpace, preferences.getBoolean(Constants.CLOSED_SPACE, false));
        setButtonSelection(binding.btnImgCrowded, preferences.getBoolean(Constants.CROWDED, false));
        setButtonSelection(binding.btnImgDriving, preferences.getBoolean(Constants.DRIVING, false));
        setButtonSelection(binding.btnImgIncar, preferences.getBoolean(Constants.IN_CAR, false));
        setButtonSelection(binding.btnImgMedia, preferences.getBoolean(Constants.MEDIA, false));
        setButtonSelection(binding.btnImgResting, preferences.getBoolean(Constants.LAY_DOWN, false));
        setButtonSelection(binding.btnImgSleep, preferences.getBoolean(Constants.SLEEPING, false));
        setButtonSelection(binding.btnImgWalking, preferences.getBoolean(Constants.WALKING, false));
        setButtonSelection(binding.imgBtnDrug, preferences.getBoolean(Constants.MEDICATION, false));
        setButtonSelection(binding.imgWakeUp, preferences.getBoolean(Constants.WAKE_UP, false));
    }

    private void setButtonSelection(View btn, Boolean isSelected) {
        btn.setSelected(isSelected);
        if (isSelected)
            btn.setBackgroundTintList(getResources().getColorStateList(R.color.sign_in_separator_color));
        else
            btn.setBackgroundTintList(getResources().getColorStateList(R.color.white));
    }
}