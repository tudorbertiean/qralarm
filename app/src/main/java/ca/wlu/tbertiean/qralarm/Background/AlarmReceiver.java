package ca.wlu.tbertiean.qralarm.Background;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import ca.wlu.tbertiean.qralarm.Activities.ScannerActivity;
import ca.wlu.tbertiean.qralarm.Memory.Helper;
import ca.wlu.tbertiean.qralarm.Objects.Alarm;

public class AlarmReceiver extends WakefulBroadcastReceiver {
    private String ARG_SEND_ALARM_TO_RECEIVER = "ca.wlu.tbertiean.SendAlarm";
    private String ARG_IS_ONE_TIME = "ca.wlu.tbertiean.OneTimeAlarm";

    @Override
    public void onReceive(Context context, Intent intent) {
        WakeLocker.acquire(context); // Turn screen on even if a lock screen is present
        Intent i = new Intent(context, ScannerActivity.class);
        i.putExtra(ARG_SEND_ALARM_TO_RECEIVER, intent.getStringExtra(ARG_SEND_ALARM_TO_RECEIVER));
        i.putExtra(ARG_IS_ONE_TIME, intent.getBooleanExtra(ARG_IS_ONE_TIME, false));
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }
}
