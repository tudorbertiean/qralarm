package ca.wlu.tbertiean.qralarm.Activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.samples.vision.barcodereader.BarcodeCapture;
import com.google.android.gms.samples.vision.barcodereader.BarcodeGraphic;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.List;

import ca.wlu.tbertiean.qralarm.Background.AlarmReceiver;
import ca.wlu.tbertiean.qralarm.Background.WakeLocker;
import ca.wlu.tbertiean.qralarm.Memory.Helper;
import ca.wlu.tbertiean.qralarm.Objects.Alarm;
import ca.wlu.tbertiean.qralarm.R;
import xyz.belvi.mobilevisionbarcodescanner.BarcodeRetriever;

import static ca.wlu.tbertiean.qralarm.Activities.AlarmListActivity.context;


public class ScannerActivity extends AppCompatActivity implements BarcodeRetriever {
    private static final String TAG = "ScannerActivity";
    private String ARG_SEND_ALARM_TO_RECEIVER = "ca.wlu.tbertiean.SendAlarm";
    private String ARG_IS_ONE_TIME = "ca.wlu.tbertiean.OneTimeAlarm";
    private MediaPlayer player;
    private SeekBar snoozeBar;
    private Vibrator vib;
    private Boolean isVibrate;
    private int snoozeTime;
    private int MINUTE = 60000;
    private String alarm_tone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_scanner);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);


        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        isVibrate = sharedPrefs.getBoolean("alarm_vibrate", false);
        alarm_tone = sharedPrefs.getString("alarm_ringtone", "");

        playAlarm();
        openCameraScanner();

        snoozeBar = (SeekBar) findViewById(R.id.scannerSnoozeBar);
        snoozeBar.setOnSeekBarChangeListener(seekBarListener);
    }

    @Override
    public void onStop () {
        super.onStop();
        Log.d(TAG, "onStop");
        //turnAlarmOff(false);
    }

    public void openCameraScanner(){
        final BarcodeCapture barcodeCapture = (BarcodeCapture) getSupportFragmentManager().findFragmentById(R.id.barcode);
        barcodeCapture.setRetrieval(this);
        barcodeCapture.setShowDrawRect(true);
        barcodeCapture.shouldAutoFocus(true);
        barcodeCapture.refresh();
    }

    public void playAlarm(){
        Log.d(TAG, "playAlarm");
        if (isVibrate) {
            vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            // Play for 500ms, pause for 1000, repeat
            long[] pattern = { 0, 500, 1000};
            if (vib != null) {
                vib.vibrate(pattern,0);
            }
        }

        player = new MediaPlayer();
        // Play the selected alarm or the default piano if none is selected
        if (alarm_tone != null && !alarm_tone.isEmpty())
            player = MediaPlayer.create(this, Uri.parse(alarm_tone));
        else
            player = MediaPlayer.create(this, R.raw.piano_alarm);

        //player.setAudioStreamType(AudioManager.STREAM_ALARM);
        player.setLooping(true);
        player.start();
    }

    private SeekBar.OnSeekBarChangeListener seekBarListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if (seekBar.getProgress() <= 90)
                seekBar.setProgress(0);
            else
                snoozeAlarm();

        }
    };

    public void snoozeAlarm(){
        Intent alarmIntent = new Intent(getApplicationContext(), AlarmReceiver.class);
        alarmIntent.putExtra(ARG_IS_ONE_TIME, getIntent().getBooleanExtra(ARG_IS_ONE_TIME, false));
        alarmIntent.putExtra(ARG_SEND_ALARM_TO_RECEIVER, getIntent().getStringExtra(ARG_SEND_ALARM_TO_RECEIVER));
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, alarmIntent, 0);
        AlarmManager manager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        //In case is was edited,
        manager.cancel(pendingIntent);
        // Set the alarm to start
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String snooze = sharedPrefs.getString("alarm_snooze_time", "5 minutes");
        snoozeTime = Integer.parseInt(snooze.split(" ")[0]) * MINUTE;
        Log.d(TAG, snoozeTime + "");
        manager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() + snoozeTime, pendingIntent);
        turnAlarmOff(true);
    }

    // Once a QR code is scanned
    @Override
    public void onRetrieved(final Barcode barcode) {
        Log.d(TAG, "Barcode read: " + barcode.displayValue);
        finish();
        turnAlarmOff(false);
    }

    public void turnAlarmOff(boolean isSnooze){
        Log.d(TAG, "turnAlarmOff");
        try {
            player.stop();
            player.release();
            if (isVibrate)
                vib.cancel();
        }catch(Exception e){
            e.printStackTrace();
        }

        if (isSnooze) {
            finish();
            return;
        }

        //Turn the alarm off if one time use
        Boolean oneTime = getIntent().getBooleanExtra(ARG_IS_ONE_TIME, false);
        Log.e(TAG, oneTime + "");
        if (oneTime) {
            Gson gson = new Gson();
            String json = getIntent().getStringExtra(ARG_SEND_ALARM_TO_RECEIVER);
            Type type = new TypeToken<Alarm>() {}.getType();
            Alarm alarm = gson.fromJson(json, type);
            alarm.setToggle(false);
            Helper.saveSingleAlarm(this, alarm);
            Log.e(TAG, "TURNED OFF");
        }

        Intent intent = new Intent(ScannerActivity.this, AlarmListActivity.class);
        if (AlarmListActivity.activityVisible != null) { // App is is already open in background, bring it to the front
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else { // The application is in foreground, just close
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
