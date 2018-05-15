package ca.wlu.tbertiean.qralarm.Adapters;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.List;

import ca.wlu.tbertiean.qralarm.Objects.Alarm;
import ca.wlu.tbertiean.qralarm.R;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder>{
    private ClickListener mListener;
    private List<Alarm> mAlarmList;
    private String TAG = "AlarmAdapter";

    public interface ClickListener{
        void onLongClick(Alarm alarm);
        void onAlarmToggle(Alarm alarm, boolean on);
    }

    public class AlarmViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener , View.OnLongClickListener{
        public ToggleButton toggle;
        public TextView name, repeating, time;

        public AlarmViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
            time = (TextView) view.findViewById(R.id.alarmTimeText);
            toggle = (ToggleButton) view.findViewById(R.id.alarmToggle);
            name = (TextView) view.findViewById(R.id.alarmNameText);
            repeating = (TextView) view.findViewById(R.id.alarmRepeatingText);
        }

        public void bindAlarm(final Alarm alarm){
            time.setText(alarm.getWake_time());
            toggle.setChecked(alarm.isToggle());
            name.setText(alarm.getName());
            if (alarm.isSelected()) {
                itemView.setBackgroundColor(itemView.getResources().getColor(R.color.alarmSelected));
                toggle.setEnabled(false);
            }
            else {
                itemView.setBackgroundColor(itemView.getResources().getColor(R.color.alarmNotSelected));
                toggle.setEnabled(true);
            }

            String repeatingText = alarm.isOneTime() ? itemView.getResources().getString(R.string.onetime_alarm) : (itemView.getResources().getString(R.string.repeating_alarm));
            repeating.setText(repeatingText);

            toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mListener.onAlarmToggle(alarm, isChecked);
                }
            });
        }

        @Override
        public void onClick(View v) {
            Log.d(TAG, "onClick");
        }

        @Override
        public boolean onLongClick(View v) {
            Log.d(TAG, "onLongClick");
            Alarm alarm = mAlarmList.get(getPosition());
            mListener.onLongClick(alarm);
            return true;
        }
    }

    public AlarmAdapter(List<Alarm> alarms, ClickListener listener){
        this.mAlarmList = alarms;
        this.mListener = listener;
    }

    @Override
    public AlarmAdapter.AlarmViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.alarm_list_row, parent, false);
        return new AlarmViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(AlarmAdapter.AlarmViewHolder holder, int position) {
        Alarm alarm = mAlarmList.get(position);
        holder.bindAlarm(alarm);
    }

    @Override
    public int getItemCount() {
        return mAlarmList.size();
    }
}
