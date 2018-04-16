package ca.wlu.tbertiean.qralarm.Objects;

import android.util.Log;

import java.util.ArrayList;

public class Alarm {
    private int hour;
    private int minute;
    private boolean toggle;
    private boolean selected;
    private ArrayList<Integer> ids;
    private boolean oneTime;
    private ArrayList<Integer> daysActive;
    private String name;

    public Alarm(int hour, int minute, ArrayList<Integer> ids, boolean oneTime, ArrayList<Integer> daysActive, String name){
        this.hour = hour;
        this.minute = minute;
        this.selected = false;
        this.oneTime = oneTime;
        this.daysActive = daysActive;
        this.name = name;
        this.ids = ids;
        Log.d("Alarm Object", ids.toString());
    }

    public ArrayList<Integer> getIds() {
        return ids;
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }

    public String getWake_time() {
        String meridiem;
        String hour;
        String minute;
        if (this.hour > 11) {
            meridiem = "PM";
            if (this.hour != 12)
                hour = Integer.toString(this.hour - 12);
            else
                hour = "12";
        }
        else {
            meridiem = "AM";
            if (this.hour == 0)
                hour = "12";
            else
                hour = Integer.toString(this.hour);
        }
        if (this.minute < 10)
            minute = "0" + Integer.toString(this.minute);
        else
            minute = Integer.toString(this.minute);

        return hour + ":" + minute + " " + meridiem;
    }

    public boolean isToggle() {
        return toggle;
    }

    public void setToggle(boolean toggle) {
        this.toggle = toggle;
    }

    public boolean isSelected(){
        return selected;
    }

    public void setSelected(boolean selected){
        this.selected = selected;
    }

    public ArrayList<Integer> getDaysActive() {
        return daysActive;
    }

    public void setDaysActive(ArrayList<Integer> daysActive) {
        this.daysActive = daysActive;
    }

    public boolean isOneTime(){
        return oneTime;
    }

    public void setOneTime(boolean oneTime){
        this.oneTime = oneTime;
    }

    public String getName(){
        return name;
    }
}
