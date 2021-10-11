package com.restart.myapplicationactivitytest;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.restart.myapplicationactivitytest.databinding.FragmentFirstBinding;

import controllers.EventsHandler;
import models.SettingsModel;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private EventsHandler handler;
    private SettingsModel settings;


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


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}