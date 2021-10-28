package services;

import android.content.Context;
import android.util.Log;
import com.amazonaws.mobileconnectors.kinesis.kinesisrecorder.*;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.regions.Regions;

public class Streamer {
        static final String TAG = "Streamer";
        static final Stirng TASK_NAME = "FirehoseSender";
        static final int SEND_INTERVAL = 60,000; //  One minute

        private static Streamer instance = null;
        private KinesisFirehoseRecorder recorder = null;
        private boolean sending = false;
        private long lastSent = 0L;

        public static getInstance() {
                return instance;
        }

        static void init(Context context) {
                AWSMobileClient.getInstance().initialize(getApplicationContext(),
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
                instance = new Streamer(context.getCachedDir(), Regions.EU_WEST_1);

        }

        private Streamer(String dir, String region) {
                recorder = KinesisFirehoseRecorder(dir, region, AWSMobileClient.getInstance());
                recorder.deleteAllRecords();
                Log.i(TAG, "Kinesis Firehose Recorder ready");
        }

        public void addData(String stream, String data)
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
                                        Log.e(TAG, "Failed to submit recrords", ace)
                                } finally {
                                        sending = false;
                                }
                        }
                }.execute();
        }


        }