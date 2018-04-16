package ca.wlu.tbertiean.qralarm.Activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.samples.vision.barcodereader.BarcodeCapture;
import com.google.android.gms.samples.vision.barcodereader.BarcodeGraphic;
import com.google.android.gms.vision.barcode.Barcode;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import ca.wlu.tbertiean.qralarm.Background.AlarmReceiver;
import ca.wlu.tbertiean.qralarm.R;
import xyz.belvi.mobilevisionbarcodescanner.BarcodeRetriever;


public class ScannerActivity extends AppCompatActivity implements BarcodeRetriever {
    private static final String TAG = "ScannerActivity";
    private MediaPlayer player;
    private TextView snoozeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_scanner);

//        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN |
//                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
//                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
//                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN |
//                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
//                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
//                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        playAlarm();
        openCameraScanner();

        snoozeText = (TextView) findViewById(R.id.snoozeTxt);
        snoozeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snoozeAlarm();
            }
        });
    }

    @Override
    public void onStop () {
        super.onStop();
        Log.d(TAG, "onStop");
        turnAlarmOff(false);
    }

    public void openCameraScanner(){
        final BarcodeCapture barcodeCapture = (BarcodeCapture) getSupportFragmentManager().findFragmentById(R.id.barcode);
        barcodeCapture.setRetrieval(this);
        barcodeCapture.setShowDrawRect(true);
        barcodeCapture.shouldAutoFocus(true);
        barcodeCapture.refresh();
    }

    public void playAlarm(){
        player = new MediaPlayer();
        player = MediaPlayer.create(this, R.raw.piano_alarm);
        player.start();
        player.setLooping(true);
    }

    public void snoozeAlarm(){
        Intent alarmIntent = new Intent(getApplicationContext(), AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, alarmIntent, 0);
        AlarmManager manager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        //In case is was edited,
        manager.cancel(pendingIntent);
        // Set the alarm to start
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        manager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() + 300000, pendingIntent);
        turnAlarmOff(true);
    }

    @Override
    public void onRetrieved(final Barcode barcode) {
        Log.d(TAG, "Barcode read: " + barcode.displayValue);
        finish();
        turnAlarmOff(false);
    }

    public void turnAlarmOff(boolean isSnooze){
        try {
            player.stop();
            player.release();
        }catch(Exception e){
            e.printStackTrace();
        }

        if (isSnooze)
            finish();

        Intent intent = new Intent(ScannerActivity.this, AlarmListActivity.class);
        if (AlarmListActivity.activityVisible != null) { // App is is already open in background, bring it to the front
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else { // The application is not in the background, just close
            finish();
        }

    }

    @Override
    public void onRetrievedMultiple(final Barcode closetToClick, final List<BarcodeGraphic> barcodeGraphics) {
    }

    @Override
    public void onBitmapScanned(SparseArray<Barcode> sparseArray) {
    }

    @Override
    public void onRetrievedFailed(String reason) {
    }

}
