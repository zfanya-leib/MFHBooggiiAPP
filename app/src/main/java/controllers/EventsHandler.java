package controllers;

import android.Manifest;
import androidx.annotation.NonNull;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.core.model.query.Where;
import com.amplifyframework.core.model.temporal.Temporal;
import com.amplifyframework.datastore.generated.model.Event;
import com.restart.myapplicationactivitytest.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import common.Constants;
import common.LocationType;
import common.State;

import static android.content.Context.MODE_PRIVATE;

public class EventsHandler implements LocationListener {
    private final String TAG = "EventsHandler";
    private float edaStatus;
    private State state = State.NORMAL;
    private int INITIAL_THREASHOLD_WAIT = 20;
    private AtomicInteger thresholdCounter = new AtomicInteger(INITIAL_THREASHOLD_WAIT);
    private LocationType location;
    private FragmentActivity currentActivity;
    private MediaController mediaController;
    private boolean videoToggle = false;
    private Ringtone sosRingtone;
    private LocationManager locationManager;
    private Vibrator vibrator;
    private ArrayList<Integer> videoArr=new ArrayList<>();
    private Location currentLocation;
    private boolean isSoS = false;

    public EventsHandler(FragmentActivity activity) {
        this.currentActivity = activity;
        mediaController = new MediaController(activity);

        activateLocation();
        this.vibrator = (Vibrator) this.currentActivity.getSystemService(Context.VIBRATOR_SERVICE);

        this.videoArr.add(R.raw.a1);
        this.videoArr.add(R.raw.a2);
        this.videoArr.add(R.raw.a3);
        this.videoArr.add(R.raw.a4);
        this.videoArr.add(R.raw.a5);
    }

    public void setLocation(LocationType location){
        this.location = location;
        if (location == LocationType.OUTDOOR){
            writeStartEventToDb(Constants.OUTSIDE);
        } else {
            writeEndEventToDb(Constants.OUTSIDE);
        }
    }

    public int getEDAThreshold(){

        SharedPreferences prefs = this.currentActivity.getSharedPreferences(Constants.SHARED_PREP_DATA, MODE_PRIVATE);
        int edaThreshold = Integer.MAX_VALUE;
        if(this.location == LocationType.INDOOR) {
            edaThreshold = prefs.getInt(Constants.EDA_INDOOR_THRESHOLD, Integer.MAX_VALUE);
        }
        if(this.location == LocationType.OUTDOOR) {
            edaThreshold = prefs.getInt(Constants.EDA_OUTDOOR_THRESHOLD, Integer.MAX_VALUE);
        }

        return edaThreshold;
    }

    public boolean isEDAThreshold(){

        SharedPreferences prefs = this.currentActivity.getSharedPreferences(Constants.SHARED_PREP_DATA, MODE_PRIVATE);
        int edaThreshold = Integer.MAX_VALUE;
        if(this.location == LocationType.INDOOR) {
            edaThreshold = prefs.getInt(Constants.EDA_INDOOR_THRESHOLD, Integer.MAX_VALUE);
            return edaThreshold > this.edaStatus;
        }
        if(this.location == LocationType.OUTDOOR) {
            edaThreshold = prefs.getInt(Constants.EDA_OUTDOOR_THRESHOLD, Integer.MAX_VALUE);
            return edaThreshold > this.edaStatus;
        }

        return false;
    }

    public void onEDAUpdate(float edaVal){
        if(this.state == State.SOS){
            return;
        }
        if(this.location == null){
            return;
        }
        this.edaStatus = edaVal;

        SharedPreferences prefs = this.currentActivity.getSharedPreferences(Constants.SHARED_PREP_DATA, MODE_PRIVATE);

        if(this.location == LocationType.INDOOR){
            int edaThreshold = prefs.getInt(Constants.EDA_INDOOR_THRESHOLD,Integer.MAX_VALUE);
            if( edaVal > edaThreshold){
                int thresholdStatus = this.thresholdCounter.decrementAndGet();
                if(thresholdStatus == 0){
                    if( this.state == State.NORMAL) {
                        this.state = State.SOS;
                        onSOS();
                    }
                }
            }
            else{
                this.thresholdCounter.set(INITIAL_THREASHOLD_WAIT);
            }
        }
        if(this.location == LocationType.OUTDOOR){
            int edaThreshold = prefs.getInt(Constants.EDA_OUTDOOR_THRESHOLD,Integer.MAX_VALUE);
            if( edaVal > edaThreshold){
                int thresholdStatus = this.thresholdCounter.decrementAndGet();
                if(thresholdStatus == 0){
                    ProgressBar pb = (ProgressBar) this.currentActivity.findViewById(R.id.pb_eda);
                    pb.getIndeterminateDrawable().setColorFilter(new BlendModeColorFilter(Color.RED, BlendMode.SRC_ATOP));
                    if( this.state == State.NORMAL) {
                        this.state = State.SOS;
                        onSOS();
                    }
                }
            }
            else{
                ProgressBar pb = (ProgressBar) this.currentActivity.findViewById(R.id.pb_eda);
                pb.getIndeterminateDrawable().setColorFilter(new BlendModeColorFilter(Color.CYAN, BlendMode.SRC_ATOP));
                this.thresholdCounter.set(INITIAL_THREASHOLD_WAIT);
            }
        }
    }

    public void onSOS() {
        if (!this.videoToggle) {

            this.isSoS = true;
            this.currentActivity.findViewById(R.id.btn_img_sos).startAnimation(getAnimation());
            this.currentActivity.findViewById(R.id.img_btn_drug).startAnimation(getAnimation());
            this.currentActivity.findViewById(R.id.img_btn_call1).startAnimation(getAnimation());
            this.currentActivity.findViewById(R.id.img_btn_call2).startAnimation(getAnimation());
            this.currentActivity.findViewById(R.id.img_btn_location).startAnimation(getAnimation());
            this.currentActivity.findViewById(R.id.img_btn_location2).startAnimation(getAnimation());

            setRingtone();
            this.sosRingtone.setVolume(90);
            this.sosRingtone.play();
            this.sosRingtone.setLooping(true);


            // Vibrate for 400 milliseconds
            this.vibrator.vibrate(VibrationEffect.createOneShot(10000,255));

            this.videoToggle = true;
            ImageView img = (ImageView) this.currentActivity.findViewById(R.id.imageView);
            img.setVisibility(View.INVISIBLE);

            // select random video
            VideoView video = getVideoView();
            video.start();
        } else {
            if (this.sosRingtone.isPlaying()) {
                this.sosRingtone.stop();

                if( this.vibrator!= null) {
                    this.vibrator.cancel();
                }
            } else {
                this.currentActivity.findViewById(R.id.btn_img_sos).getAnimation().cancel();

                if(this.currentActivity.findViewById(R.id.img_btn_location).getAnimation()!=null) {
                    this.currentActivity.findViewById(R.id.img_btn_location).getAnimation().cancel();
                }

                if(this.currentActivity.findViewById(R.id.img_btn_location2).getAnimation()!=null) {
                    this.currentActivity.findViewById(R.id.img_btn_location2).getAnimation().cancel();
                }

                if(this.currentActivity.findViewById(R.id.img_btn_call1).getAnimation()!=null) {
                    this.currentActivity.findViewById(R.id.img_btn_call1).getAnimation().cancel();
                }

                if(this.currentActivity.findViewById(R.id.img_btn_call2).getAnimation()!=null) {
                    this.currentActivity.findViewById(R.id.img_btn_call2).getAnimation().cancel();
                }

                if(this.currentActivity.findViewById(R.id.img_btn_drug).getAnimation()!=null) {
                    this.currentActivity.findViewById(R.id.img_btn_drug).getAnimation().cancel();
                }


                this.videoToggle = false;
                VideoView video = (VideoView) this.currentActivity.findViewById(R.id.video_player);
                video.stopPlayback();
                video.setVisibility(View.INVISIBLE);
                ImageView img = (ImageView) this.currentActivity.findViewById(R.id.imageView);
                img.setVisibility(View.VISIBLE);
                this.state = State.NORMAL;

                this.isSoS = false;
            }
        }
    }


    private VideoView getVideoView() {
        SharedPreferences prefs = this.currentActivity.getSharedPreferences(Constants.SHARED_PREP_DATA, MODE_PRIVATE);
        VideoView video = (VideoView) this.currentActivity.findViewById(R.id.video_player);

        if( prefs.contains(Constants.DEFAULT_VIDEO) && prefs.getBoolean(Constants.DEFAULT_VIDEO,true)){
            Random rand = new Random();
            int randomNum = rand.nextInt((5 - 1) + 1) ;
            video.setVideoURI(Uri.parse("android.resource://" + this.currentActivity.getPackageName() + "/" + this.videoArr.get(randomNum)));
            video.setVisibility(View.VISIBLE);
            video.setMediaController(mediaController);
            video.setOnCompletionListener(mp -> {
                int nextRandom = rand.nextInt((5 - 1) + 1) ;
                video.setVideoURI(Uri.parse("android.resource://" + this.currentActivity.getPackageName() + "/" + this.videoArr.get(nextRandom)));
                video.start();
            });
        }
        else{
            video.setVideoURI(Uri.parse(prefs.getString(Constants.VIDEO,"")));
            video.setVisibility(View.VISIBLE);
            video.setMediaController(mediaController);
            video.setOnCompletionListener(mp -> {
                video.start();
            });
        }

        return video;
    }

    private void setRingtone() {
        String defRingtone ="android.resource://" + this.currentActivity.getPackageName() + "/" + R.raw.emergency_alarm;

        SharedPreferences prefs = this.currentActivity.getSharedPreferences(Constants.SHARED_PREP_DATA, MODE_PRIVATE);
        if(prefs.getBoolean(Constants.DEFAULT_RINGTONE,true)){
            Uri notification = Uri.parse(defRingtone);
            this.sosRingtone = RingtoneManager.getRingtone(this.currentActivity, notification);
        }
        else{
            Uri notification = Uri.parse(prefs.getString(Constants.RINGTONE,defRingtone));
            this.sosRingtone = RingtoneManager.getRingtone(this.currentActivity, notification);
        }
    }

    public void makePhoneCall1() {
        if( this.currentActivity.findViewById(R.id.img_btn_call1).getAnimation()!=null) {
            SharedPreferences prefs = this.currentActivity.getApplication().getSharedPreferences(Constants.SHARED_PREP_DATA,MODE_PRIVATE);
            String phoneNumber = prefs.getString(Constants.PHONE_CALL_1,"");
            if( phoneNumber != "") {
                Intent i = new Intent(Intent.ACTION_CALL);
                i.setData(Uri.parse(String.format("tel:%s", phoneNumber)));
                this.currentActivity.startActivity(i);

                if (this.currentActivity.findViewById(R.id.img_btn_call1).getAnimation() != null) {
                    this.currentActivity.findViewById(R.id.img_btn_call1).getAnimation().cancel();
                }
            }
        }
    }

    public void makePhoneCall2() {
        if( this.currentActivity.findViewById(R.id.img_btn_call2).getAnimation()!=null) {
            SharedPreferences prefs = this.currentActivity.getApplication().getSharedPreferences(Constants.SHARED_PREP_DATA,MODE_PRIVATE);
            String phoneNumber = prefs.getString(Constants.PHONE_CALL_2,"");
            if( phoneNumber != ""){
                Intent i = new Intent(Intent.ACTION_CALL);
                i.setData(Uri.parse(String.format("tel:%s",phoneNumber)));
                this.currentActivity.startActivity(i);

                if(this.currentActivity.findViewById(R.id.img_btn_call2).getAnimation()!=null) {
                    this.currentActivity.findViewById(R.id.img_btn_call2).getAnimation().cancel();
                }
            }
        }
    }

    public void onLocationChanged(Location location) {
        this.currentLocation = location;
    }

    public void sendLocationSms(){
        SharedPreferences prefs = this.currentActivity.getApplication().getSharedPreferences(Constants.SHARED_PREP_DATA,MODE_PRIVATE);

        String phoneNumber = prefs.getString(Constants.PHONE_LOCATION_1,"");
        String smsMessage = prefs.getString(Constants.EMERGENCY_SMS_TEXT,"");
        if(phoneNumber != ""){
            SmsManager smsManager = SmsManager.getDefault();
            StringBuffer smsBody = new StringBuffer();
            smsBody.append(smsMessage);
            smsBody.append("\r\n");
            smsBody.append("http://maps.google.com?q=");
            smsBody.append(currentLocation.getLatitude());
            smsBody.append(",");
            smsBody.append(currentLocation.getLongitude());

            PendingIntent sentPI = PendingIntent.getBroadcast(currentActivity, 0, new Intent("SMS SENT"), 0);

            PendingIntent deliveredPI = PendingIntent.getBroadcast(currentActivity, 0, new Intent("SMS DELIVERED"), 0);

            currentActivity.registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context arg0, Intent arg1) {
                    int resultCode = getResultCode();
                    switch (resultCode) {
                        case Activity.RESULT_OK:
                            Log.i(TAG,"sms send successfuly");
                            break;
                        case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                            Log.i(TAG,"failed to send sms, general sms error, probalby sms too long");
                            break;
                        case SmsManager.RESULT_ERROR_NO_SERVICE:
                            Log.i(TAG,"failed to send sms, error no service");
                            break;
                        case SmsManager.RESULT_ERROR_NULL_PDU:
                            Log.i(TAG,"failed to send sms, error null pdu");
                            break;
                        case SmsManager.RESULT_ERROR_RADIO_OFF:
                            Log.i(TAG,"failed to send sms, radio is off");
                            break;
                    }
                }
            }, new IntentFilter("SMS SENT"));

            currentActivity.registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context arg0, Intent arg1) {
                    int resultCode = getResultCode();
                    switch (resultCode) {
                        case Activity.RESULT_OK:
                            Log.i(TAG,"sms send successfuly");
                            break;
                        case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                            Log.i(TAG,"failed to send sms, general sms error, probalby sms too long");
                            break;
                        case SmsManager.RESULT_ERROR_NO_SERVICE:
                            Log.i(TAG,"failed to send sms, error no service");
                            break;
                        case SmsManager.RESULT_ERROR_NULL_PDU:
                            Log.i(TAG,"failed to send sms, error null pdu");
                            break;
                        case SmsManager.RESULT_ERROR_RADIO_OFF:
                            Log.i(TAG,"failed to send sms, radio is off");
                            break;
                    }
                }
            }, new IntentFilter("SMS DELIVERED"));
            ArrayList<String> smsParts = smsManager.divideMessage(smsBody.toString());
            ArrayList<PendingIntent> pendingIntents = new ArrayList<>();
            ArrayList<PendingIntent> deliveredIntents = new ArrayList<>();
            for(String part : smsParts){
                pendingIntents.add(sentPI);
                deliveredIntents.add(deliveredPI);
            }
            smsManager.sendMultipartTextMessage(phoneNumber, null, smsParts, pendingIntents, deliveredIntents);

           // smsManager.sendTextMessage(phoneNumber, null, smsBody.toString(), sentPI, deliveredPI);
        }
    }

    public void sendLocationSms2(){
        SharedPreferences prefs = this.currentActivity.getApplication().getSharedPreferences(Constants.SHARED_PREP_DATA,MODE_PRIVATE);

        String phoneNumber = prefs.getString(Constants.PHONE_LOCATION_2,"");
        String smsMessage = prefs.getString(Constants.EMERGENCY_SMS_TEXT,"");
        if(phoneNumber != ""){
            SmsManager smsManager = SmsManager.getDefault();
            StringBuffer smsBody = new StringBuffer();
            smsBody.append(smsMessage);
            smsBody.append("\r\n");
            smsBody.append("http://maps.google.com?q=");
            smsBody.append(currentLocation.getLatitude());
            smsBody.append(",");
            smsBody.append(currentLocation.getLongitude());

            PendingIntent sentPI = PendingIntent.getBroadcast(currentActivity, 0, new Intent("SMS SENT2"), 0);

            PendingIntent deliveredPI = PendingIntent.getBroadcast(currentActivity, 0, new Intent("SMS DELIVERED2"), 0);

            currentActivity.registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context arg0, Intent arg1) {
                    int resultCode = getResultCode();
                    switch (resultCode) {
                        case Activity.RESULT_OK:
                            Log.i(TAG,"sms send successfuly");
                            break;
                        case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                            Log.i(TAG,"failed to send sms, general sms error, probalby sms too long");
                            break;
                        case SmsManager.RESULT_ERROR_NO_SERVICE:
                            Log.i(TAG,"failed to send sms, error no service");
                            break;
                        case SmsManager.RESULT_ERROR_NULL_PDU:
                            Log.i(TAG,"failed to send sms, error null pdu");
                            break;
                        case SmsManager.RESULT_ERROR_RADIO_OFF:
                            Log.i(TAG,"failed to send sms, radio is off");
                            break;
                    }
                }
            }, new IntentFilter("SMS SENT2"));

            currentActivity.registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context arg0, Intent arg1) {
                    int resultCode = getResultCode();
                    switch (resultCode) {
                        case Activity.RESULT_OK:
                            Log.i(TAG,"sms send successfuly");
                            break;
                        case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                            Log.i(TAG,"failed to send sms, general sms error, probalby sms too long");
                            break;
                        case SmsManager.RESULT_ERROR_NO_SERVICE:
                            Log.i(TAG,"failed to send sms, error no service");
                            break;
                        case SmsManager.RESULT_ERROR_NULL_PDU:
                            Log.i(TAG,"failed to send sms, error null pdu");
                            break;
                        case SmsManager.RESULT_ERROR_RADIO_OFF:
                            Log.i(TAG,"failed to send sms, radio is off");
                            break;
                    }
                }
            }, new IntentFilter("SMS DELIVERED2"));
            ArrayList<String> smsParts = smsManager.divideMessage(smsBody.toString());
            ArrayList<PendingIntent> pendingIntents = new ArrayList<>();
            ArrayList<PendingIntent> deliveredIntents = new ArrayList<>();
            for(String part : smsParts){
                pendingIntents.add(sentPI);
                deliveredIntents.add(deliveredPI);
            }
            smsManager.sendMultipartTextMessage(phoneNumber, null, smsParts, pendingIntents, deliveredIntents);
            //smsManager.sendTextMessage(phoneNumber, null, smsParts, sentPI, deliveredPI);
        }
    }

    public void activateLocation(){
        if (ActivityCompat.checkSelfPermission(this.currentActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this.currentActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        this.locationManager = (LocationManager) this.currentActivity.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }

    public void sendLocation() {
        if( this.isSoS && this.currentLocation != null) {
           sendLocationSms();

           if(this.currentActivity.findViewById(R.id.img_btn_location).getAnimation() != null) {
               this.currentActivity.findViewById(R.id.img_btn_location).getAnimation().cancel();
           }
        }
    }

    public void sendLocation2() {
        if( this.isSoS && this.currentLocation != null) {
            sendLocationSms2();

            if(this.currentActivity.findViewById(R.id.img_btn_location2).getAnimation() != null) {
                this.currentActivity.findViewById(R.id.img_btn_location2).getAnimation().cancel();
            }
        }
    }

    public void stopRingtone(){
        if(this.sosRingtone != null){
            this.sosRingtone.stop();
        }
        if(this.vibrator != null){
            this.vibrator.cancel();
        }
    }

    public void onDrugTaken(){
        if(this.currentActivity.findViewById(R.id.img_btn_drug).getAnimation() != null)
            this.currentActivity.findViewById(R.id.img_btn_drug).getAnimation().cancel();
    }

    private AlphaAnimation getAnimation(){
        AlphaAnimation anim = new AlphaAnimation(0.0f, 0.7f);
        anim.setDuration(800); //You can manage the blinking time with this parameter
        anim.setStartOffset(40);
        anim.setRepeatMode(Animation.ZORDER_NORMAL);
        anim.setRepeatCount(Animation.INFINITE);

        return anim;
    }

    public void writeStartEventToDb(String name) {
        Event item = Event.builder()
                .name(name)
                .startLocalTime(new Temporal.DateTime(Calendar.getInstance().getTime(), (int)(Calendar.getInstance().getTimeZone().getRawOffset() / 1000)))
                .userName(AWSMobileClient.getInstance().getUsername())
                .build();
        Amplify.DataStore.save(
                item,
                success -> Log.i(TAG, "Saved item: " + success.item().getId()),
                error -> Log.e(TAG, "Could not save item to DataStore", error)
        );
    }

    public void writeEndEventToDb(String name) {
        Amplify.DataStore.query(
                Event.class,
                Where.matches(Event.NAME.eq(name)).sorted(Event.START_LOCAL_TIME.descending()),
                matches -> {
                    if (matches.hasNext()) {
                        Event original = matches.next();
                        Event edited = original.copyOfBuilder()
                                .endLocalTime(new Temporal.DateTime(Calendar.getInstance().getTime(), (int)(Calendar.getInstance().getTimeZone().getRawOffset() / 1000)))
                                .build();
                        Amplify.DataStore.save(edited,
                                updated -> Log.i(TAG, "Updated a post."),
                                failure -> Log.e(TAG, "Update failed.", failure)
                        );
                    }
                },
                failure -> Log.e(TAG, "Query failed.", failure)
        );
    }
}
