package com.sayan.sample.sinchvoipsample;

import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.sinch.android.rtc.MissingPermissionException;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import com.sinch.android.rtc.calling.CallClientListener;
import com.sinch.android.rtc.calling.CallListener;

import java.lang.ref.WeakReference;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Call call;
    private Button button;
    private SinchClient sinchClient;
    private SinchCallListener sinchCallListener;
    private Button button2;
    private AudioPlayer mAudioPlayer;
    private EditText callerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String userId = getIntent().getStringExtra("userId");
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.button);
        button2 = findViewById(R.id.button2);
        callerId = findViewById(R.id.callerId);
        mAudioPlayer = new AudioPlayer(this);


        sinchClient = Sinch.getSinchClientBuilder()
                .context(this)
                .userId(userId)
                .applicationKey("44438399-8fae-4ce7-b037-7e6f9fe8686e")
                .applicationSecret("oY89kurdJEeZ8QSKxKgA+g==")
                .environmentHost("sandbox.sinch.com")
                .build();

        sinchClient.setSupportCalling(true);
        sinchClient.startListeningOnActiveConnection();
        sinchClient.start();

        sinchClient.getCallClient().addCallClientListener(new SinchCallClientListener(this, button, button2, callerId, mAudioPlayer));


    }

    public void CallNow(View view) {
        Button button = (Button) view;
        String text = button.getText().toString();
        switch (text) {
            case "Call":
                try {
                    call = sinchClient.getCallClient().callUser(callerId.getText().toString().trim());
                    sinchCallListener = new SinchCallListener(this, call, button, button2, callerId, mAudioPlayer);
                    call.addCallListener(sinchCallListener);
                    button.setText("Hang Up");
                    callerId.setVisibility(View.GONE);
                }catch (MissingPermissionException e){
                    ActivityCompat.requestPermissions(this, new String[]{e.getRequiredPermission()}, 0);
                }
                break;
            case "Pick Up":
                mAudioPlayer.stopRingtone();
                call.answer();
                call.addCallListener(new SinchCallListener(this, call, button, button2, callerId, mAudioPlayer));
                button.setText("Hang Up");
                callerId.setVisibility(View.GONE);
                break;
            case "Hang Up":
                call.hangup();
                button.setText("Call");
                callerId.setVisibility(View.VISIBLE);
                button2.setVisibility(View.GONE);
                break;
        }
    }

    public void cancel(View view) {
        try {
            call.removeCallListener(sinchCallListener);
            call.hangup();
            mAudioPlayer.stopRingtone();
        } catch (Exception e) {
            e.printStackTrace();
        }
        button2.setVisibility(View.GONE);
        button.setText("Call");
        callerId.setVisibility(View.VISIBLE);
    }

    public void setCallValue(Call callValue) {
        this.call = callValue;
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private static class SinchCallListener implements CallListener {
        private WeakReference<MainActivity> mainActivityWeakReference;
        private Call call;
        private Button button;
        private Button button2;
        private EditText callerId;
        private AudioPlayer mAudioPlayer;

        public SinchCallListener(MainActivity mainActivity, Call call, Button button, Button button2, EditText callerId, AudioPlayer mAudioPlayer) {
            this.mainActivityWeakReference = new WeakReference<>(mainActivity);
            this.call = call;
            this.button = button;
            this.button2 = button2;
            this.callerId = callerId;
            this.mAudioPlayer = mAudioPlayer;
        }

        @Override
        public void onCallEnded(Call endedCall) {
            //call ended by either party
            mainActivityWeakReference.get().showToast(call.getDetails().getDuration() + "");
            call.removeCallListener(this);
            call = null;
            button.setText("Call");
            button2.setVisibility(View.GONE);
            callerId.setVisibility(View.VISIBLE);
            mainActivityWeakReference.get().setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
            mAudioPlayer.stopProgressTone();
            mAudioPlayer.stopRingtone();
        }

        @Override
        public void onCallEstablished(Call establishedCall) {
            //incoming call was picked up
            mAudioPlayer.stopProgressTone();
            mainActivityWeakReference.get().setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);

        }

        @Override
        public void onCallProgressing(Call progressingCall) {
            //call is ringing
            mAudioPlayer.playProgressTone();
            Toast.makeText(mainActivityWeakReference.get(), "Ringing", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> pushPairs) {
            //don't worry about this right now
            Toast.makeText(mainActivityWeakReference.get(), "push", Toast.LENGTH_SHORT).show();
        }
    }

    private static class SinchCallClientListener implements CallClientListener {
        private final WeakReference<MainActivity> mainActivityWeakReference;
        private final Button button;
        private Button button2;
        private EditText callerId;
        private AudioPlayer mAudioPlayer;

        public SinchCallClientListener(MainActivity mainActivity, Button button, Button button2, EditText callerId, AudioPlayer mAudioPlayer) {
            this.mainActivityWeakReference = new WeakReference<>(mainActivity);
            this.button = button;
            this.button2 = button2;
            this.callerId = callerId;
            this.mAudioPlayer = mAudioPlayer;
        }

        @Override
        public void onIncomingCall(CallClient callClient, Call incomingCall) {
            //Pick up the call!
            callerId.setVisibility(View.GONE);
            button.setText("Pick Up");
            button2.setVisibility(View.VISIBLE);
            mainActivityWeakReference.get().setCallValue(incomingCall);
            mAudioPlayer.playRingtone();
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "You may now continue the service", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "This application needs permission to use your microphone to function properly.", Toast
                    .LENGTH_LONG).show();
        }
    }
}