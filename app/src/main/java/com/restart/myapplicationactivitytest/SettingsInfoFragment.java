package com.restart.myapplicationactivitytest;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.textfield.TextInputEditText;
import com.restart.myapplicationactivitytest.databinding.FragmentSettingsBinding;
import com.restart.myapplicationactivitytest.databinding.FragmentSettingsInfoBinding;

import common.Constants;

import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingsInfoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsInfoFragment extends Fragment {

    private FragmentSettingsInfoBinding binding;
    public SettingsInfoFragment() {
        // Required empty public constructor
    }

    public static SettingsInfoFragment newInstance() {
        SettingsInfoFragment fragment = new SettingsInfoFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSettingsInfoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull @org.jetbrains.annotations.NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SharedPreferences prefs = getActivity().getSharedPreferences(Constants.SHARED_PREP_DATA, MODE_PRIVATE);
        TextInputEditText txtInEda = (TextInputEditText)view.findViewById(R.id.txt_in_eda);

        int v = prefs.getInt(Constants.EDA_INDOOR_THRESHOLD,8);
        txtInEda.setText(String.valueOf(v));
        TextInputEditText txtOutEda = (TextInputEditText)view.findViewById(R.id.txt_outdoor_eda);
        txtOutEda.setText(String.valueOf(prefs.getInt(Constants.EDA_OUTDOOR_THRESHOLD,14)));

        TextInputEditText txtCall1 = (TextInputEditText)view.findViewById(R.id.txt_first_emg_number);
        txtCall1.setText(prefs.getString(Constants.PHONE_CALL_1,""));
        TextInputEditText txtCall2 = (TextInputEditText)view.findViewById(R.id.txt_second_emg_number);
        txtCall2.setText(prefs.getString(Constants.PHONE_CALL_2,""));

        TextInputEditText txtLocation = (TextInputEditText)view.findViewById(R.id.txt_location_number);
        txtLocation.setText(prefs.getString(Constants.PHONE_LOCATION_1,""));

        TextInputEditText txtLocation2 = (TextInputEditText)view.findViewById(R.id.txt_location_number2);
        txtLocation2.setText(prefs.getString(Constants.PHONE_LOCATION_2,""));

        TextInputEditText txtEmergencySms = (TextInputEditText)view.findViewById(R.id.txt_emergency_sms);
        txtEmergencySms.setText(prefs.getString(Constants.EMERGENCY_SMS_TEXT,""));

        binding.btnBackFromSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(SettingsInfoFragment.this)
                        .navigate(R.id.action_SettingsFragment_to_FirstFragment);
            }
        });



        binding.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor prefs = getActivity().getSharedPreferences(Constants.SHARED_PREP_DATA, MODE_PRIVATE).edit();

                prefs.putInt(Constants.EDA_INDOOR_THRESHOLD,Integer.parseInt(txtInEda.getText().toString()));
                prefs.putInt(Constants.EDA_OUTDOOR_THRESHOLD,Integer.parseInt(txtOutEda.getText().toString()));
                prefs.putString(Constants.PHONE_CALL_1,txtCall1.getText().toString());
                prefs.putString(Constants.PHONE_CALL_2,txtCall2.getText().toString());
                prefs.putString(Constants.PHONE_LOCATION_1,txtLocation.getText().toString());
                prefs.putString(Constants.PHONE_LOCATION_2,txtLocation2.getText().toString());
                prefs.putString(Constants.EMERGENCY_SMS_TEXT,txtEmergencySms.getText().toString());
                prefs.apply();

                NavHostFragment.findNavController(SettingsInfoFragment.this)
                        .navigate(R.id.action_SettingsFragment_to_FirstFragment);
            }
        });
    }
}