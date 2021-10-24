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
                if (v.isSelected() == false) {
                    v.setSelected(true);
                    v.setBackgroundTintList(getResources().getColorStateList(R.color.sign_in_separator_color));
                    handler.writeStartEventToDb("inCar");
                } else {
                    v.setSelected(false);
                    v.setBackgroundTintList(getResources().getColorStateList(R.color.white));
                    handler.writeEndEventToDb("inCar");
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