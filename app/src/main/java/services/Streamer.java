package services;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.amazonaws.AmazonClientException;
import com.amazonaws.mobileconnectors.kinesis.kinesisrecorder.*;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.regions.Regions;

import java.io.File;

public class Streamer {
        static final String TAG = "Streamer";
        static final String TASK_NAME = "FirehoseSender";
        static final int SEND_INTERVAL = 60000; //  One minute

        private static Streamer instance = null;
        private KinesisFirehoseRecorder recorder = null;
        private boolean sending = false;
        private long lastSent = 0L;

        public static Streamer getInstance() {
                return instance;
        }

        public static void init(Context context) {
                AWSMobileClient.getInstance().initialize(context,
                        new Callback<UserStateDetails>() {
                                @Override
                                public void onResult(UserStateDetails userStateDetails) {
                                        Log.i(TAG, userStateDetails.getUserState().toString());
                                }

                                @Override
                                public void onError(Exception e) {
                                        Log.e(TAG, "Initialization error.", e);
                                }
                        }
                );
                instance = new Streamer(context.getCacheDir(), Regions.US_EAST_2.toString());

        }

        private Streamer(File dir, String region) {
                recorder = new KinesisFirehoseRecorder(dir, Regions.fromName(region), AWSMobileClient.getInstance());
                recorder.deleteAllRecords();
                Log.i(TAG, "Kinesis Firehose Recorder ready");
        }

        public void addData(String stream, String data) {
                recorder.saveRecord(data, stream);
                if (!sending && System.currentTimeMillis() - lastSent > SEND_INTERVAL) {
                        send();
                }
        }

        public void send() {
                sending = true;
                new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... v) {
                                try {
                                        recorder.submitAllRecords();
                                        lastSent = System.currentTimeMillis();
                                } catch (AmazonClientException ace) {
                                        Log.e(TAG, "Failed to submit recrords", ace);
                                } finally {
                                        sending = false;
                                }
                                return null;
                        }
                }.execute();
        }
}