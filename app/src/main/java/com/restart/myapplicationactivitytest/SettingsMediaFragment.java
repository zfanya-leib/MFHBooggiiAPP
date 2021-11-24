package com.restart.myapplicationactivitytest;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.UriPermission;
import android.database.Cursor;
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
import androidx.navigation.fragment.NavHostFragment;

import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.restart.myapplicationactivitytest.databinding.FragmentSettingsInfoBinding;
import com.restart.myapplicationactivitytest.databinding.FragmentSettingsMediaBinding;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import common.Constants;

import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingsMediaFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsMediaFragment extends Fragment {

    FragmentSettingsMediaBinding binding;
    public SettingsMediaFragment() {
        // Required empty public constructor
    }

    public static SettingsMediaFragment newInstance() {
        SettingsMediaFragment fragment = new SettingsMediaFragment();
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
        binding = FragmentSettingsMediaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            SharedPreferences prefs = getActivity().getSharedPreferences(Constants.SHARED_PREP_DATA, MODE_PRIVATE);

            TextView txtRingtone = (TextView) view.findViewById(R.id.txt_ringtone);
            SharedPreferences.Editor prefsEdit = getActivity().getSharedPreferences(Constants.SHARED_PREP_DATA, MODE_PRIVATE).edit();
            if (!prefs.contains(Constants.RINGTONE)) {
                prefsEdit.putBoolean(Constants.DEFAULT_RINGTONE, true);
                txtRingtone.setText("emergency_alarm");
            } else {
                try
                {
                    prefsEdit.putBoolean(Constants.DEFAULT_RINGTONE, false);
                    Uri ringtoneUri = Uri.parse(prefs.getString(Constants.RINGTONE, ""));

                    Cursor returnCursor =
                            getActivity().getContentResolver().query(ringtoneUri, null, null, null, null);
                    int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    returnCursor.moveToFirst();
                    String fileName = returnCursor.getString(nameIndex);

                    txtRingtone.setText(fileName);
                }
                catch (Exception e){
                    txtRingtone.setText("");
                }
            }

            TextView txtDrugMedia = (TextView) view.findViewById(R.id.txt_drug_media);
            String drugMediaUri = prefs.getString(Constants.DRUG_MEDIA, "");
            if (drugMediaUri != "") {
                try {
                    Uri ringtoneUri = Uri.parse(drugMediaUri);
                    Cursor returnCursor =
                            getActivity().getContentResolver().query(ringtoneUri, null, null, null, null);
                    int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    returnCursor.moveToFirst();
                    String fileName = returnCursor.getString(nameIndex);

                    txtDrugMedia.setText(fileName);
                }
                catch(Exception e){
                    txtDrugMedia.setText("");
                }

            } else {
                txtDrugMedia.setText("");
            }


            TextView txtVideo = (TextView) view.findViewById(R.id.txt_video);
            if (!prefs.contains(Constants.VIDEO)) {
                prefsEdit.putBoolean(Constants.DEFAULT_VIDEO, true);
                txtVideo.setText("");
            } else {
                prefsEdit.putBoolean(Constants.DEFAULT_VIDEO, false);
                Uri videoUri = Uri.parse(prefs.getString(Constants.VIDEO, ""));
                txtVideo.setText(videoUri.getLastPathSegment());
            }

            ActivityResultLauncher<Intent> ringtoneSelectionResult = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult result) {
                            if (result.getResultCode() == Activity.RESULT_OK) {

                                Intent data = result.getData();
                                Uri uri = data.getData();
                                if (uri != null) {
                                    final int takeFlags = data.getFlags()
                                            & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                                            | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                                    try {
                                        getActivity().getContentResolver().takePersistableUriPermission(uri, takeFlags);
                                        Cursor returnCursor =
                                                getActivity().getContentResolver().query(uri, null, null, null, null);
                                        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                                        returnCursor.moveToFirst();
                                        String fileName = returnCursor.getString(nameIndex);

                                        txtRingtone.setText(fileName);
                                    }
                                    catch (SecurityException e) {
                                        Log.e("MediaSettingsFragment", "failed to get ringtone file permissions");
                                    }

                                    SharedPreferences.Editor prefsEdit = getActivity().getSharedPreferences(Constants.SHARED_PREP_DATA, MODE_PRIVATE).edit();

                                    prefsEdit.putString(Constants.RINGTONE, uri.toString());
                                    prefsEdit.putBoolean(Constants.DEFAULT_RINGTONE, false);
                                    prefsEdit.apply();
                                }
                            }
                        }
                    });

            binding.btnSelectRingtone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent chooseFile = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
                    chooseFile.setType("audio/*");
                    ringtoneSelectionResult.launch(chooseFile);
                }
            });

            ActivityResultLauncher<Intent> drugSelectionResult = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult result) {
                            if (result.getResultCode() == Activity.RESULT_OK) {

                                Intent data = result.getData();
                                Uri uri = data.getData();
                                if (uri != null) {
                                    final int takeFlags = data.getFlags()
                                            & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                                            | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                                    try {
                                        getActivity().getContentResolver().takePersistableUriPermission(uri, takeFlags);
                                        Cursor returnCursor =
                                                getActivity().getContentResolver().query(uri, null, null, null, null);
                                        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                                        returnCursor.moveToFirst();
                                        String fileName = returnCursor.getString(nameIndex);
                                        txtDrugMedia.setText(fileName);
                                    }
                                    catch (SecurityException e) {
                                        Log.e("MediaSettingsFragment", "failed to get ringtone file permissions");
                                    }

                                    SharedPreferences.Editor prefsEdit = getActivity().getSharedPreferences(Constants.SHARED_PREP_DATA, MODE_PRIVATE).edit();
                                    prefsEdit.putString(Constants.DRUG_MEDIA, uri.toString());
                                    prefsEdit.apply();
                                }
                            }
                        }
                    });

            binding.btnDrugMp3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent chooseFile = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
                    chooseFile.setType("audio/*");
                    drugSelectionResult.launch(chooseFile);
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

                                prefsEdit.putString(Constants.VIDEO, selectedImageUri.toString());
                                txtVideo.setText(selectedImageUri.getLastPathSegment());
                                prefsEdit.putBoolean(Constants.DEFAULT_VIDEO, false);
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
                    prefsEdit.putBoolean(Constants.DEFAULT_VIDEO, true);
                    prefsEdit.putString(Constants.VIDEO, "");
                    prefsEdit.apply();
                }
            });
            binding.btnResetDrugMedia.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    txtDrugMedia.setText("");
                    SharedPreferences.Editor prefsEdit = getActivity().getSharedPreferences(Constants.SHARED_PREP_DATA, MODE_PRIVATE).edit();
                    prefsEdit.putString(Constants.DRUG_MEDIA, "");
                    prefsEdit.apply();
                }
            });

            binding.btnResetRingtone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    txtRingtone.setText("");
                    SharedPreferences.Editor prefsEdit = getActivity().getSharedPreferences(Constants.SHARED_PREP_DATA, MODE_PRIVATE).edit();
                    prefsEdit.putBoolean(Constants.DEFAULT_RINGTONE, true);
                    prefsEdit.putString(Constants.RINGTONE, "");
                    prefsEdit.apply();
                }
            });

            binding.btnBackFromMediaSettings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NavHostFragment.findNavController(SettingsMediaFragment.this)
                            .navigate(R.id.action_SettingsFragment_to_FirstFragment);
                }
            });

            binding.btnMediaSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NavHostFragment.findNavController(SettingsMediaFragment.this)
                            .navigate(R.id.action_SettingsFragment_to_FirstFragment);
                }
            });
        }
        catch (Exception e){
            Log.e("MediaSettingsFragment","error during fragment load" + e.getLocalizedMessage());
        }
    }
}