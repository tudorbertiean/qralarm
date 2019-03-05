package ca.wlu.tbertiean.qralarm.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.WriterException;


import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import androidmads.library.qrgenearator.QRGSaver;
import ca.wlu.tbertiean.qralarm.Memory.Helper;
import ca.wlu.tbertiean.qralarm.Objects.Alarm;
import ca.wlu.tbertiean.qralarm.Objects.Days;
import ca.wlu.tbertiean.qralarm.R;

public class AddAlarmActivity extends AppCompatActivity{
    private String TAG;
    private TimePicker mTimePicker;
    private Button mQRDownloadBtn;
    private Bitmap bitmap;
    private QRGEncoder qrgEncoder;
    private CheckBox oneTimeBox;
    private EditText alarmName;
    private ToggleButton sunBtn, monBtn, tuesBtn, wedBtn, thursBtn, friBtn, satBtn;
    private String savePath = Environment.getExternalStorageDirectory().getPath() + "/QRCode/";
    private String ARG_ALARM = "ca.wlu.tbertiean.AddAlarm";
    private String ARG_EDIT_ALARM = "ca.wlu.tbertiean.EditAlarm";
    private int ADD_ALARM_RESULT = 1;
    private int EDIT_ALARM_RESULT = 2;
    private boolean isEdit = false;
    private Alarm editAlarm;
    private ArrayList<Integer> daysActive = new ArrayList<>();
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_alarm);
        TAG = getLocalClassName();
        if (getIntent().getStringExtra(ARG_EDIT_ALARM) != null){
            isEdit = true;
            Gson gson = new Gson();
            String json = getIntent().getStringExtra(ARG_EDIT_ALARM);
            Type type = new TypeToken<Alarm>(){}.getType();
            editAlarm = gson.fromJson(json, type);
        }
        setupUI();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_alarm, menu);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        Intent intent = new Intent();
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                if (isEdit){
                    setResult(EDIT_ALARM_RESULT, intent);
                }
                setResult(RESULT_CANCELED);
                finish();
                return true;

            case R.id.addAlarm:
                Log.d(TAG, "Add Alarm");
                // Ensure camera permissions is enabled for user
                int hasCameraPermission = checkSelfPermission(Manifest.permission.CAMERA);
                if (hasCameraPermission != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[] {Manifest.permission.CAMERA},
                            REQUEST_CODE_ASK_PERMISSIONS);
                } else {
                    if (isEdit) {
                        intent.putExtra(ARG_EDIT_ALARM, createAlarm());
                        setResult(EDIT_ALARM_RESULT, intent);
                    }else{
                        intent.putExtra(ARG_ALARM, createAlarm());
                        setResult(ADD_ALARM_RESULT, intent);
                    }
                    finish();
                }

            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    public void setupUI(){
        mTimePicker = (TimePicker) findViewById(R.id.addAlarmPicker);
        alarmName = (EditText) findViewById(R.id.addAlarmEditText);
        oneTimeBox = (CheckBox) findViewById(R.id.oneTimeBox);
        oneTimeBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    sunBtn.setChecked(false);
                    monBtn.setChecked(false);
                    tuesBtn.setChecked(false);
                    wedBtn.setChecked(false);
                    thursBtn.setChecked(false);
                    friBtn.setChecked(false);
                    satBtn.setChecked(false);
                }
            }
        });
        setToggleListeners();

        mQRDownloadBtn = (Button) findViewById(R.id.downQrBtn);
        mQRDownloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadQRCode();
            }
        });

        if (isEdit){
            alarmName.setText(editAlarm.getName());
            mTimePicker.setCurrentHour(editAlarm.getHour());
            mTimePicker.setCurrentMinute(editAlarm.getMinute());
            oneTimeBox.setChecked(editAlarm.isOneTime());
            if (!editAlarm.isOneTime()) {
                for (Integer day : editAlarm.getDaysActive()) {
                    switch (day) {
                        case Days.SUN:
                            sunBtn.setChecked(true);
                            break;
                        case Days.MON:
                            monBtn.setChecked(true);
                            break;
                        case Days.TUES:
                            tuesBtn.setChecked(true);
                            break;
                        case Days.WED:
                            wedBtn.setChecked(true);
                            break;
                        case Days.THURS:
                            thursBtn.setChecked(true);
                            break;
                        case Days.FRI:
                            friBtn.setChecked(true);
                            break;
                        case Days.SAT:
                            satBtn.setChecked(true);
                            break;
                    }
                }
            }
        }else{
            final Calendar c = Calendar.getInstance();
            int cHour = c.get(Calendar.HOUR_OF_DAY);
            int cMinute = c.get(Calendar.MINUTE);
            mTimePicker.setCurrentHour(cHour);
            mTimePicker.setCurrentMinute(cMinute);
        }
    }

    public void downloadQRCode(){
        WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        int width = point.x;
        int height = point.y;
        int smallerDimension = width < height ? width : height;
        smallerDimension = smallerDimension * 3 / 4;

        qrgEncoder = new QRGEncoder(
                "Alarm", null,
                QRGContents.Type.TEXT,
                smallerDimension);
        try {
            bitmap = qrgEncoder.encodeAsBitmap();
        } catch (WriterException e) {
            Log.v(TAG, e.toString());
        }
        try {
            Boolean save = QRGSaver.save(savePath, "Alarm", bitmap, QRGContents.ImageType.IMAGE_JPEG);
            String result = save ? "Image Saved" : "Image Not Saved";
            Toast.makeText(getApplicationContext(), result + " in /QRCode/", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String createAlarm(){
        //Create
        ArrayList<Integer> ids = new ArrayList<>();
        if (daysActive.size() == 0)
            daysActive.add(0);
        for (Integer ignored : daysActive){
            ids.add(Helper.incrementAlarmCounter(getApplicationContext()));
        }
        Alarm alarm = new Alarm(mTimePicker.getCurrentHour(), mTimePicker.getCurrentMinute(), ids, oneTimeBox.isChecked(), daysActive, alarmName.getText().toString());
        Gson gson = new Gson();
        return gson.toJson(alarm);
    }

    public void setToggleListeners(){
        sunBtn = (ToggleButton) findViewById(R.id.sunToggle);
        sunBtn.setOnCheckedChangeListener(new OnDayToggleListener(Days.SUN));
        monBtn = (ToggleButton) findViewById(R.id.monToggle);
        monBtn.setOnCheckedChangeListener(new OnDayToggleListener(Days.MON));
        tuesBtn = (ToggleButton) findViewById(R.id.tuesToggle);
        tuesBtn.setOnCheckedChangeListener(new OnDayToggleListener(Days.TUES));
        wedBtn = (ToggleButton) findViewById(R.id.wedToggle);
        wedBtn.setOnCheckedChangeListener(new OnDayToggleListener(Days.WED));
        thursBtn = (ToggleButton) findViewById(R.id.thursToggle);
        thursBtn.setOnCheckedChangeListener(new OnDayToggleListener(Days.THURS));
        friBtn = (ToggleButton) findViewById(R.id.friToggle);
        friBtn.setOnCheckedChangeListener(new OnDayToggleListener(Days.FRI));
        satBtn = (ToggleButton) findViewById(R.id.satToggle);
        satBtn.setOnCheckedChangeListener(new OnDayToggleListener(Days.SAT));
    }

    public class OnDayToggleListener implements CompoundButton.OnCheckedChangeListener {
        Integer day;

        public OnDayToggleListener(Integer day){
            this.day = day;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                if (oneTimeBox.isChecked())
                    oneTimeBox.setChecked(false);
                daysActive.add(this.day);
                oneTimeBox.setEnabled(true);
            }
            else {
                if (daysActive.contains(this.day))
                    daysActive.remove(this.day);
                if (daysActive.size() == 0) {
                    oneTimeBox.setChecked(true);
                    oneTimeBox.setEnabled(false);
                }
            }
        }
    }
}
