package com.restart.myapplicationactivitytest;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.restart.myapplicationactivitytest.databinding.FragmentSettingsBinding;

import common.Constants;

import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment {
    private FragmentSettingsBinding binding;
    public SettingsFragment() {
        // Required empty public constructor
    }

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
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
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {


        try {
            TabLayout tabLayout=(TabLayout)getActivity().findViewById(R.id.tabs);
            FrameLayout frameLayout=(FrameLayout)getActivity().findViewById(R.id.frameLayout);
            TabLayout.Tab infoTab = binding.tabs.getTabAt(1);
            binding.tabs.selectTab(infoTab);

            SettingsInfoFragment fragment = new SettingsInfoFragment();
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frameLayout, fragment);
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            fragmentTransaction.commit();


            binding.tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                                                      @Override
                                                      public void onTabSelected(TabLayout.Tab tab) {
                                                         if(tab.getPosition() == 0){
                                                             SettingsMediaFragment fragment = new SettingsMediaFragment();
                                                             FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                                                             FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                                             fragmentTransaction.replace(R.id.frameLayout, fragment);
                                                             fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                                                             fragmentTransaction.commit();
                                                         }
                                                         if(tab.getPosition() ==1){
                                                             SettingsInfoFragment fragment = new SettingsInfoFragment();
                                                             FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                                                             FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                                             fragmentTransaction.replace(R.id.frameLayout, fragment);
                                                             fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                                                             fragmentTransaction.commit();
                                                         }
                                                      }

                                                      @Override
                                                      public void onTabUnselected(TabLayout.Tab tab) {

                                                      }

                                                      @Override
                                                      public void onTabReselected(TabLayout.Tab tab) {

                                                      }
                                                  }
            );
        }
        catch (Exception e){
            String s = e.getMessage();
        }
        /*
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

        TextView txtRingtone = (TextView)view.findViewById(R.id.txt_ringtone);
        SharedPreferences.Editor prefsEdit = getActivity().getSharedPreferences(Constants.SHARED_PREP_DATA, MODE_PRIVATE).edit();
        if(!prefs.contains(Constants.RINGTONE)){
            prefsEdit.putBoolean(Constants.DEFAULT_RINGTONE,true);
            txtRingtone.setText("emergency_alarm");
        }
        else {
            prefsEdit.putBoolean(Constants.DEFAULT_RINGTONE,false);
            Uri ringtoneUri = Uri.parse(prefs.getString(Constants.RINGTONE,""));
            txtRingtone.setText(ringtoneUri.getQueryParameter("title"));
        }

        TextView txtVideo= (TextView)view.findViewById(R.id.txt_video);
        if(!prefs.contains(Constants.VIDEO)){
            prefsEdit.putBoolean(Constants.DEFAULT_VIDEO,true);
            txtVideo.setText("");
        }
        else {
            prefsEdit.putBoolean(Constants.DEFAULT_VIDEO,false);
            Uri videoUri = Uri.parse(prefs.getString(Constants.VIDEO,""));
            txtVideo.setText(videoUri.getLastPathSegment());
        }

        super.onViewCreated(view, savedInstanceState);


        ActivityResultLauncher<Intent> ringtoneSelectionResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            // There are no request codes
                           
                            Intent data = result.getData();
                            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                            if( uri != null) {
                                SharedPreferences.Editor prefsEdit = getActivity().getSharedPreferences(Constants.SHARED_PREP_DATA, MODE_PRIVATE).edit();

                                prefsEdit.putString(Constants.RINGTONE, uri.toString());
                                txtRingtone.setText(uri.getQueryParameter("title"));
                                prefsEdit.putBoolean(Constants.DEFAULT_RINGTONE, false);
                                prefsEdit.apply();
                            }
                        }
                    }
                });

            binding.btnSelectRingtone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chooseFile = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);

                ringtoneSelectionResult.launch(chooseFile);
            }
        });

        ActivityResultLauncher<Intent> videoSelectionResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            // There are no request codes
                            Uri selectedImageUri = result.getData().getData();
                            //Intent data = result.getData();
                            //Uri uri = data.getParcelableExtra("");
                            SharedPreferences.Editor prefsEdit = getActivity().getSharedPreferences(Constants.SHARED_PREP_DATA, MODE_PRIVATE).edit();

                            prefsEdit.putString(Constants.VIDEO,selectedImageUri.toString());
                            txtVideo.setText(selectedImageUri.getLastPathSegment());
                            prefsEdit.putBoolean(Constants.DEFAULT_VIDEO,false);
                            prefsEdit.apply();
                        }
                    }
                });
        binding.btnSelectVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
                chooseFile.setType("video/*");

                videoSelectionResult.launch(chooseFile);
            }
        });
        binding.btnResetVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtVideo.setText("");
                SharedPreferences.Editor prefsEdit = getActivity().getSharedPreferences(Constants.SHARED_PREP_DATA, MODE_PRIVATE).edit();
                prefsEdit.putBoolean(Constants.DEFAULT_VIDEO,true);
                prefsEdit.putString(Constants.VIDEO,"");
                prefsEdit.apply();
            }
        });
        binding.btnBackFromSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(SettingsFragment.this)
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
                prefs.apply();

                NavHostFragment.findNavController(SettingsFragment.this)
                        .navigate(R.id.action_SettingsFragment_to_FirstFragment);
            }
        });

        }
        catch (Exception e){
            String s = e.getMessage();
        }
*/
    }


}