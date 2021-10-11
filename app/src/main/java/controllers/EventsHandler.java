package controllers;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.telephony.SmsManager;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.restart.myapplicationactivitytest.R;

public class EventsHandler implements LocationListener {

    private FragmentActivity currentActivity;
    private MediaController mediaController;
    private boolean videoToggle = false;
    private Ringtone sosRingtone;
    private LocationManager locationManager;
    // Get instance of Vibrator from current Context
    private Vibrator vibrator;


    public EventsHandler(FragmentActivity activity) {
        this.currentActivity = activity;
        mediaController = new MediaController(activity);

        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        this.sosRingtone = RingtoneManager.getRingtone(this.currentActivity, notification);

        this.locationManager = (LocationManager) this.currentActivity.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        this.vibrator = (Vibrator) this.currentActivity.getSystemService(Context.VIBRATOR_SERVICE);
    }


    public void onSOS() {

        if (!this.videoToggle) {

            this.currentActivity.findViewById(R.id.btn_img_sos).startAnimation(getAnimation());
            this.currentActivity.findViewById(R.id.img_btn_drug).startAnimation(getAnimation());
            this.currentActivity.findViewById(R.id.img_btn_call1).startAnimation(getAnimation());
            this.currentActivity.findViewById(R.id.img_btn_location).startAnimation(getAnimation());


            this.sosRingtone.setVolume(90);
            this.sosRingtone.play();
            this.sosRingtone.setLooping(true);


            // Vibrate for 400 milliseconds
            this.vibrator.vibrate(VibrationEffect.createOneShot(20000,255));

            this.videoToggle = true;
            ImageView img = (ImageView) this.currentActivity.findViewById(R.id.imageView);
            img.setVisibility(View.INVISIBLE);
            VideoView video = (VideoView) this.currentActivity.findViewById(R.id.video_player);
            video.setVideoURI(Uri.parse("android.resource://" + this.currentActivity.getPackageName() + "/" + R.raw.a));
            video.setVisibility(View.VISIBLE);
            video.setMediaController(mediaController);
            video.start();
        } else {
            if (this.sosRingtone.isPlaying()) {
                this.sosRingtone.stop();
                this.vibrator.cancel();
            } else {
                this.currentActivity.findViewById(R.id.btn_img_sos).getAnimation().cancel();

                if(this.currentActivity.findViewById(R.id.img_btn_location).getAnimation()!=null) {
                    this.currentActivity.findViewById(R.id.img_btn_location).getAnimation().cancel();
                }

                if(this.currentActivity.findViewById(R.id.img_btn_call1).getAnimation()!=null) {
                    this.currentActivity.findViewById(R.id.img_btn_call1).getAnimation().cancel();
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
            }
        }
    }

    public void makePhoneCall() {
        if( this.currentActivity.findViewById(R.id.img_btn_call1).getAnimation()!=null) {
            Intent i = new Intent(Intent.ACTION_CALL);
            i.setData(Uri.parse("tel:+972528456649"));
            this.currentActivity.startActivity(i);

            this.currentActivity.findViewById(R.id.img_btn_call1).getAnimation().cancel();
        }
    }

    public void onLocationChanged(Location location) {
        SmsManager smsManager = SmsManager.getDefault();
        StringBuffer smsBody = new StringBuffer();
        smsBody.append("please come to help me ASAP!!!");
        smsBody.append("\r\n");
        smsBody.append("http://maps.google.com?q=");
        smsBody.append(location.getLatitude());
        smsBody.append(",");
        smsBody.append(location.getLongitude());
        smsManager.sendTextMessage("+972528456649", null, smsBody.toString(), null, null);

        this.locationManager.removeUpdates(this);
    }

    public void sendLocation() {
        if( this.currentActivity.findViewById(R.id.img_btn_location).getAnimation() != null &&
                this.currentActivity.findViewById(R.id.img_btn_location).getAnimation().hasStarted()) {
            if (ActivityCompat.checkSelfPermission(this.currentActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this.currentActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            this.currentActivity.findViewById(R.id.img_btn_location).getAnimation().cancel();
        }
    }

    public void stopRingtone(){
        if(this.sosRingtone != null){
            this.sosRingtone.stop();
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
}
