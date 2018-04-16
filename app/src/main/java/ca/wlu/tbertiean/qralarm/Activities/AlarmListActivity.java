package ca.wlu.tbertiean.qralarm.Activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.TimeUnit;

import ca.wlu.tbertiean.qralarm.Adapters.AlarmAdapter;
import ca.wlu.tbertiean.qralarm.Adapters.SimpleDividerItemDecoration;
import ca.wlu.tbertiean.qralarm.Background.AlarmReceiver;
import ca.wlu.tbertiean.qralarm.Memory.Helper;
import ca.wlu.tbertiean.qralarm.Objects.Alarm;
import ca.wlu.tbertiean.qralarm.R;

public class AlarmListActivity extends AppCompatActivity implements AlarmAdapter.ClickListener {
    public static String TAG;
    private List<Alarm> alarmList;
    private List<Alarm> alarmsSelected;
    private RecyclerView recyclerView;
    private AlarmAdapter mAdapter;
    public static Context context;
    private int ADD_ALARM_RESULT = 1;
    private int EDIT_ALARM_RESULT = 2;
    private String ARG_SEND_ALARM_TO_RECEIVER = "ca.wlu.tbertiean.SendAlarm";
    private String ARG_IS_ONE_TIME = "ca.wlu.tbertiean.OneTimeAlarm";
    private String ARG_ADD_ALARM = "ca.wlu.tbertiean.AddAlarm";
    private String ARG_EDIT_ALARM = "ca.wlu.tbertiean.EditAlarm";
    private MenuItem mSettingsMenu, mDeleteMenu, mEditMenu;
    public static Boolean activityVisible; //Use when alarm is triggered to know if it should make a new activtiy or not

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onResume");
        setContentView(R.layout.activity_alarm_list);
        TAG = getLocalClassName();
        context = this;
        activityVisible = true;

        retrieveAlarms();
        setupUI();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult");
        if(requestCode == ADD_ALARM_RESULT && resultCode != RESULT_CANCELED) {
            Gson gson = new Gson();
            String json = data.getStringExtra(ARG_ADD_ALARM);
            Type type = new TypeToken<Alarm>(){}.getType();
            Alarm alarm =  gson.fromJson(json, type);
            setAlarm(alarm, true);
            alarmList.add(0, alarm);
            mAdapter.notifyItemInserted(0);
            Helper.saveAlarmsToDisk(context, alarmList);
        }else if(requestCode == EDIT_ALARM_RESULT && resultCode != RESULT_CANCELED){
            mEditMenu.setVisible(false);
            mDeleteMenu.setVisible(false);
            mSettingsMenu.setVisible(true);
            Gson gson = new Gson();
            String json = data.getStringExtra(ARG_EDIT_ALARM);
            Type type = new TypeToken<Alarm>(){}.getType();
            Alarm alarm =  gson.fromJson(json, type);
            setAlarm(alarm, true);
            alarm.setSelected(false);
            int index = alarmList.indexOf(alarmsSelected.get(0));
            alarmsSelected.clear();
            alarmList.set(index, alarm);
            mAdapter.notifyItemChanged(index);
            Helper.saveAlarmsToDisk(context, alarmList);
        }else if (requestCode == EDIT_ALARM_RESULT && resultCode == RESULT_CANCELED){
            mEditMenu.setVisible(false);
            mDeleteMenu.setVisible(false);
            mSettingsMenu.setVisible(true);
            int index = alarmList.indexOf(alarmsSelected.get(0));
            alarmList.get(index).setSelected(false);
            alarmList.set(index, alarmList.get(index));
            mAdapter.notifyItemChanged(index);
            Helper.saveAlarmsToDisk(context, alarmList);
            alarmsSelected.clear();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_alarm_list, menu);
        mSettingsMenu = menu.findItem(R.id.settingsMenu);
        mDeleteMenu = menu.findItem(R.id.deleteMenu);
        mEditMenu = menu.findItem(R.id.editMenu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch(item.getItemId()){
            case R.id.settingsMenu:
                intent = new Intent(AlarmListActivity.this, SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.editMenu:
                intent = new Intent(AlarmListActivity.this, AddAlarmActivity.class);
                Gson gson = new Gson();
                String json = gson.toJson(alarmsSelected.get(0));
                intent.putExtra(ARG_EDIT_ALARM, json);
                startActivityForResult(intent, EDIT_ALARM_RESULT);
                break;
            case R.id.deleteMenu:
                deleteAlarms();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupUI(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AlarmListActivity.this, AddAlarmActivity.class);
                startActivityForResult(intent, ADD_ALARM_RESULT);
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.alarm_recycler);
        mAdapter = new AlarmAdapter(alarmList, this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));
        recyclerView.setAdapter(mAdapter);
    }

    private void retrieveAlarms(){
        alarmList = Helper.getAlarms(context);
        if (alarmList == null)
            alarmList = new ArrayList<>();
    }

    public void deleteAlarms(){
        for (Alarm alarm : alarmsSelected){
            setAlarm(alarm, false);
            int index = alarmList.indexOf(alarm);
            alarmList.remove(index);
            mAdapter.notifyItemRemoved(index);
        }
        alarmsSelected.clear();
        Helper.saveAlarmsToDisk(context, alarmList);
        mSettingsMenu.setVisible(true);
        mDeleteMenu.setVisible(false);
        mEditMenu.setVisible(false);
        Snackbar.make(getCurrentFocus(), "Alarm(s) deleted!", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    @Override
    public void onLongClick(Alarm alarm) {
        Log.d(TAG, "onLongClick");
        if (alarmsSelected == null)
            alarmsSelected = new ArrayList<>();

        if (alarm.isSelected()){
            alarm.setSelected(false);
            mAdapter.notifyDataSetChanged();
            alarmsSelected.remove(alarm);
            if (alarmsSelected.size() == 0){
                mSettingsMenu.setVisible(true);
                mEditMenu.setVisible(false);
                mDeleteMenu.setVisible(false);
            }
        }else{
            alarm.setSelected(true);
            mAdapter.notifyDataSetChanged();
            alarmsSelected.add(alarm);
            mSettingsMenu.setVisible(false);
            mDeleteMenu.setVisible(true);
        }
        if (alarmsSelected.size() == 1)
            mEditMenu.setVisible(true);
        else
            mEditMenu.setVisible(false);
    }

    @Override
    public void onAlarmToggle(Alarm alarm, boolean on) {
        setAlarm(alarm, on);
        Helper.saveAlarmsToDisk(context, alarmList);
    }

    private void setAlarm(Alarm alarm, boolean toggle){
        alarm.setToggle(toggle);
        ArrayList<Integer> alarmDays = alarm.getDaysActive();
        boolean snackbarShown = false;
        long triggerAlarmAt = 0;
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        ListIterator iter = alarmDays.listIterator();

        while (iter.hasNext()) {
            int alarmId = alarm.getIds().get(iter.nextIndex());
            Intent alarmIntent = new Intent(context, AlarmReceiver.class);

            int day = (int) iter.next();
            if (toggle) {
                // Set the alarm to start
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, alarm.getHour());
                calendar.set(Calendar.MINUTE, alarm.getMinute());
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);

                Calendar currentCalendar = Calendar.getInstance();
                if (alarm.isOneTime()) {
                    Log.d(TAG, "Alarm with id: " + alarmId + " set for one time trigger");
                    // Let the receiver class know its a one time alarm so it can toggle it off
                    alarmIntent.putExtra(ARG_IS_ONE_TIME, true);
                    Gson gson = new Gson();
                    alarmIntent.putExtra(ARG_SEND_ALARM_TO_RECEIVER, gson.toJson(alarm));
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, alarmId, alarmIntent, 0);
                    // If the alarm is set for a time before the current time, set the alarm for the next day
                    if (calendar.getTimeInMillis() < currentCalendar.getTimeInMillis())
                        calendar.set(Calendar.DATE, currentCalendar.get(Calendar.DATE) + 1);

                    manager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                    showTimeToTrigger(calendar.getTimeInMillis());
                }
                else {
                    Log.d(TAG, "Alarm with id: " + alarmId + " set for repeating on day: " + day);
                    calendar.set(Calendar.DAY_OF_WEEK, day);
                    alarmIntent.putExtra(ARG_IS_ONE_TIME, false);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, alarmId, alarmIntent, 0);
                    manager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), manager.INTERVAL_DAY * 7, pendingIntent);

                    // Determine which "time to trigger" to show on the snackbar
                    if (day - currentCalendar.get(Calendar.DAY_OF_WEEK) == 0 && currentCalendar.getTimeInMillis() < calendar.getTimeInMillis()) {
                        showTimeToTrigger(calendar.getTimeInMillis());
                        snackbarShown = true;
                    }
                    if (day - currentCalendar.get(Calendar.DAY_OF_WEEK) == 1) {
                        triggerAlarmAt = calendar.getTimeInMillis();
                    }
                    if (!iter.hasNext() && !snackbarShown && triggerAlarmAt != 0)
                        showTimeToTrigger(triggerAlarmAt);
                }
            }
            else { //Alarm has been turned off
                PendingIntent pendingIntent1 = PendingIntent.getBroadcast(context, alarmId, alarmIntent, 0);
                PendingIntent pendingIntent2 = PendingIntent.getBroadcast(context, 0, alarmIntent, 0); // Turn off snooze is it was triggered
                manager.cancel(pendingIntent1);
                manager.cancel(pendingIntent2);
            }
        }
    }

    private void showTimeToTrigger(long end){
        Calendar startDate = Calendar.getInstance();
        long start = startDate.getTimeInMillis();

        long hours = TimeUnit.MILLISECONDS.toHours(Math.abs(end - start));
        long minutes = TimeUnit.MILLISECONDS.toMinutes(Math.abs(end - start - TimeUnit.HOURS.toMillis(hours)));
        minutes++;
        if (minutes == 60){
            minutes = 0;
            hours++;
        }
        String timeToTrigger = "Alarm set for " + hours + " hours and " + minutes + " minutes from now";
        Snackbar.make(getCurrentFocus(), timeToTrigger, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }
}
