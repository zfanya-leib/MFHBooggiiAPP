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
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
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
                final String eventName = "outside"; // The report to db is only is_outside
                if (v.isSelected() == false) {
                    v.setSelected(true);
                    v.setBackgroundTintList(getResources().getColorStateList(R.color.sign_in_separator_color));
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
                final String eventName = "outside";
                if (v.isSelected() == false) {
                    v.setSelected(true);
                    v.setBackgroundTintList(getResources().getColorStateList(R.color.sign_in_separator_color));
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
                v.setRotation(v.getRotation() + 45);
                if (v.isSelected() == false) {
                    v.setSelected(true);
                    handler.writeStartEventToDb("interventionNeeded");
                } else {
                    v.setSelected(false);
                    handler.writeEndEventToDb("interventionNeeded");
                }
            }
        });

        binding.btnImgSevereAttack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setRotation(v.getRotation() + 45);
                if (v.isSelected() == false) {
                    v.setSelected(true);
                    handler.writeStartEventToDb("majorEvent");
                } else {
                    v.setSelected(false);
                    handler.writeEndEventToDb("majorEvent");
                }
            }
        });

        binding.btnImgInterventionNeeded.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setRotation(v.getRotation() + 45);
                if (v.isSelected() == false) {
                    v.setSelected(true);
                    handler.writeStartEventToDb("interventionNeeded");
                } else {
                    v.setSelected(false);
                    handler.writeEndEventToDb("interventionNeeded");
                }
            }
        });

        binding.btnImgSevereAttack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setRotation(v.getRotation() + 45);
                if (v.isSelected() == false) {
                    v.setSelected(true);
                    handler.writeStartEventToDb("majorEvent");
                } else {
                    v.setSelected(false);
                    handler.writeEndEventToDb("majorEvent");
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
        if (MainActivity.lastBatteryLevel != -1) {
            setLastUIState();
        }
    }

    private void setLastUIState() {
        TextView txtBattery = (TextView)getActivity().findViewById(R.id.txt_battery);
        updateLabel(txtBattery, String.format("%.0f %%", MainActivity.lastBatteryLevel));
        TextView txtBpm = (TextView)getActivity().findViewById(R.id.txt_bpm);
        updateLabel(txtBpm, String.valueOf(MainActivity.lastBpm));
        TextView txtHrv = (TextView)getActivity().findViewById(R.id.txt_hrv);
        updateLabel(txtHrv, String.valueOf(MainActivity.lastHrv));
        TextView txtEda = (TextView)getActivity().findViewById(R.id.txt_eda);
        updateLabel(txtEda, String.valueOf(MainActivity.lastEda));
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
                String param = intent.getStringExtra(Constants.EMPATICA_PARAM);
                Float value = intent.getFloatExtra(Constants.EMPATICA_VALUE, -1);

                switch (param) {
                    case Constants.EDA:
                        TextView txtEDA =(TextView) getActivity().findViewById(R.id.txt_eda);
                        if( txtEDA != null && value != null && value != -1 ) {
                            MainActivity.lastEda = value;
                            updateLabel(txtEDA, value.toString());
                            updateProgress(value);
                            handler.onEDAUpdate(value);
                        }
                        break;
                    case Constants.BPM:
                        TextView txtBpm = (TextView) getActivity().findViewById(R.id.txt_bpm);
                        if(txtBpm != null && value != null && value != -1) {
                            MainActivity.lastBpm = value;
                            updateLabel(txtBpm, value.toString());
                        }
                        break;
                    case Constants.HRV:
                        TextView txtHRV = (TextView)getActivity().findViewById(R.id.txt_hrv);
                        if( txtHRV != null && value != null && value != -1) {
                            MainActivity.lastHrv = value;
                            updateLabel(txtHRV, value.toString());
                        }
                        break;
                    case Constants.BATTERY:
                        TextView txtBattery = (TextView)getActivity().findViewById(R.id.txt_battery);
                        if(txtBattery != null && value != null && value != -1) {
                            MainActivity.lastBatteryLevel = value;
                            updateLabel(txtBattery, String.format("%.0f %%", value));
                        }
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

        LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(broadcastReceiver, new IntentFilter(Constants.EMPATICA_MONITOR));
//        this.getActivity().registerReceiver(broadcastReceiver, new IntentFilter(Constants.EMPATICA_MONITOR));
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