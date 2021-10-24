package com.restart.myapplicationactivitytest;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.restart.myapplicationactivitytest.databinding.FragmentSecondBinding;

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
                final String eventName = "inCar";
                if (v.isSelected() == false) {
                    v.setSelected(true);
                    v.setBackgroundTintList(getResources().getColorStateList(R.color.sign_in_separator_color));
                    handler.writeStartEventToDb(eventName);
                } else {
                    v.setSelected(false);
                    v.setBackgroundTintList(getResources().getColorStateList(R.color.white));
                    handler.writeEndEventToDb(eventName);
                }
            }
        });

        binding.btnImgClosedSpace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String eventName = "closedSpace";
                if (v.isSelected() == false) {
                    v.setSelected(true);
                    v.setBackgroundTintList(getResources().getColorStateList(R.color.sign_in_separator_color));
                    handler.writeStartEventToDb(eventName);
                } else {
                    v.setSelected(false);
                    v.setBackgroundTintList(getResources().getColorStateList(R.color.white));
                    handler.writeEndEventToDb(eventName);
                }
            }
        });

        binding.btnImgCrowded.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String eventName = "crowded";
                if (v.isSelected() == false) {
                    v.setSelected(true);
                    v.setBackgroundTintList(getResources().getColorStateList(R.color.sign_in_separator_color));
                    handler.writeStartEventToDb(eventName);
                } else {
                    v.setSelected(false);
                    v.setBackgroundTintList(getResources().getColorStateList(R.color.white));
                    handler.writeEndEventToDb(eventName);
                }
            }
        });

        binding.btnImgDriving.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String eventName = "driving";
                if (v.isSelected() == false) {
                    v.setSelected(true);
                    v.setBackgroundTintList(getResources().getColorStateList(R.color.sign_in_separator_color));
                    handler.writeStartEventToDb(eventName);
                } else {
                    v.setSelected(false);
                    v.setBackgroundTintList(getResources().getColorStateList(R.color.white));
                    handler.writeEndEventToDb(eventName);
                }
            }
        });

        binding.btnImgMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String eventName = "media";
                if (v.isSelected() == false) {
                    v.setSelected(true);
                    v.setBackgroundTintList(getResources().getColorStateList(R.color.sign_in_separator_color));
                    handler.writeStartEventToDb(eventName);
                } else {
                    v.setSelected(false);
                    v.setBackgroundTintList(getResources().getColorStateList(R.color.white));
                    handler.writeEndEventToDb(eventName);
                }
            }
        });

        binding.btnImgResting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String eventName = "layDown";
                if (v.isSelected() == false) {
                    v.setSelected(true);
                    v.setBackgroundTintList(getResources().getColorStateList(R.color.sign_in_separator_color));
                    handler.writeStartEventToDb(eventName);
                } else {
                    v.setSelected(false);
                    v.setBackgroundTintList(getResources().getColorStateList(R.color.white));
                    handler.writeEndEventToDb(eventName);
                }
            }
        });

        binding.btnImgSleep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String eventName = "sleeping";
                if (v.isSelected() == false) {
                    v.setSelected(true);
                    v.setBackgroundTintList(getResources().getColorStateList(R.color.sign_in_separator_color));
                    handler.writeStartEventToDb(eventName);
                } else {
                    v.setSelected(false);
                    v.setBackgroundTintList(getResources().getColorStateList(R.color.white));
                    handler.writeEndEventToDb(eventName);
                }
            }
        });

        binding.btnImgWalking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String eventName = "walking";
                if (v.isSelected() == false) {
                    v.setSelected(true);
                    v.setBackgroundTintList(getResources().getColorStateList(R.color.sign_in_separator_color));
                    handler.writeStartEventToDb(eventName);
                } else {
                    v.setSelected(false);
                    v.setBackgroundTintList(getResources().getColorStateList(R.color.white));
                    handler.writeEndEventToDb(eventName);
                }
            }
        });

        binding.imgBtnDrug.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String eventName = "medication";
                if (v.isSelected() == false) {
                    v.setSelected(true);
                    v.setBackgroundTintList(getResources().getColorStateList(R.color.sign_in_separator_color));
                    handler.writeStartEventToDb(eventName);
                } else {
                    v.setSelected(false);
                    v.setBackgroundTintList(getResources().getColorStateList(R.color.white));
                    handler.writeEndEventToDb(eventName);
                }
            }
        });

        binding.imgWakeUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String eventName = "wakeUp";
                if (v.isSelected() == false) {
                    v.setSelected(true);
                    v.setBackgroundTintList(getResources().getColorStateList(R.color.sign_in_separator_color));
                    handler.writeStartEventToDb(eventName);
                } else {
                    v.setSelected(false);
                    v.setBackgroundTintList(getResources().getColorStateList(R.color.white));
                    handler.writeEndEventToDb(eventName);
                }
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}