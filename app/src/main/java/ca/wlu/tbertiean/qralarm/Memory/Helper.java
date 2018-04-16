package ca.wlu.tbertiean.qralarm.Memory;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import ca.wlu.tbertiean.qralarm.Objects.Alarm;

/**
 * Created by tudor on 14/03/17.
 */

public class Helper {
    private static String TAG = "Helper";
    public Helper(){}

    public static void saveAlarmsToDisk(Context context, List<Alarm> alarm){
        Log.d(TAG, "Saving alarms to disk");

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefsEditor = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(alarm);
        prefsEditor.putString("Alarm", json);
        prefsEditor.commit();
    }

    public static void saveSingleAlarm(Context context, Alarm alarm){
        Log.d(TAG, "Saving alarm to disk");

        List<Alarm> alarms = getAlarms(context);
        alarms.get(0).setToggle(false);
        saveAlarmsToDisk(context, alarms);
    }

    public static List<Alarm> getAlarms(Context context){
        Log.d(TAG, "Getting all alarms from disk");

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Gson gson = new Gson();
        String json = sharedPrefs.getString("Alarm", null);
        Type type = new TypeToken<List<Alarm>>(){}.getType();
        return gson.fromJson(json, type);
    }

    public static void clearSharedPreferences(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefsEditor = preferences.edit();
        prefsEditor.clear();
        prefsEditor.apply();
    }

    public static int incrementAlarmCounter(Context context){
        Log.d(TAG, "Incrementing alarm counter");

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        int counter = preferences.getInt("AlarmCounter", 0);
        counter++;
        SharedPreferences.Editor prefsEditor = preferences.edit();
        prefsEditor.putInt("AlarmCounter", counter);
        prefsEditor.commit();
        return counter;
    }
}
