package dk.projekt.bachelor.wheresmyfamily.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.TableJsonOperationCallback;
import com.microsoft.windowsazure.mobileservices.TableJsonQueryCallback;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import dk.projekt.bachelor.wheresmyfamily.BroadCastReceiver.AlarmReceiver;
import dk.projekt.bachelor.wheresmyfamily.Controller.ChildModelController;
import dk.projekt.bachelor.wheresmyfamily.Controller.WmfGeofenceController;
import dk.projekt.bachelor.wheresmyfamily.DataModel.Child;
import dk.projekt.bachelor.wheresmyfamily.DataModel.WmfGeofence;
import dk.projekt.bachelor.wheresmyfamily.R;
import dk.projekt.bachelor.wheresmyfamily.Storage.GeofenceStorage;
import dk.projekt.bachelor.wheresmyfamily.helper.BaseActivity;


public class NewCalEventActivity extends BaseActivity implements
        View.OnClickListener, OnItemSelectedListener {
    private final String TAG = "NewCalEventActivity";
    // UserInfoStorage storage = new UserInfoStorage();
    private ArrayList<Child> m_My_children = new ArrayList<Child>();
    private ArrayList<WmfGeofence> geofences;
    GeofenceStorage geofenceStorage;

    private Activity mActivity;
    // Widget GUI
    private EditText txtStartDate, txtStartTime, txtEndDate, txtEndTime, txtEvent, txtChild;
    private Spinner spinnerLocation, spinnerRepeat;
    private Button btnNewLocation;

    // Variable for storing current date and time
    private int mYear, mMonth, mDay, mHour, mMinute;
    private String spinnerLoc, spinnerRep, pEmail, cEmail, selectedChild, eventID;
    Spinner spinner;
    AlarmReceiver alarmReceiver;
    TimePickerDialog tpd;

    private String[] locationList;
    private String[] repeat = {"", "Ja" , "Nej"};
    private PendingIntent pendingIntent;
    Child currentChild;
    public static final String NEW_CALENDAR_EVENT_ACTION = "new.calendar.event";
    // AlarmService alarmService;
    Bundle bundle = new Bundle();
    ChildModelController childModelController = new ChildModelController();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_cal_event);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        geofenceStorage = new GeofenceStorage(this);
        geofences = geofenceStorage.getGeofences(this);
        locationList = new String[geofences.size()];

        m_My_children = childModelController.getMyChildren(this);

        mActivity = this;

        txtStartDate = (EditText) findViewById(R.id.txtStartDate);
        txtStartTime = (EditText) findViewById(R.id.txtStartTime);
        txtEndDate = (EditText) findViewById(R.id.txtEndDate);
        txtEndTime = (EditText) findViewById(R.id.txtEndTime);
        txtEvent = (EditText) findViewById(R.id.txtEvent);
        txtChild = (EditText) findViewById(R.id.txtChild);
        btnNewLocation = (Button) findViewById(R.id.btnnewlocation);
        spinner = (Spinner) findViewById(R.id.spinnerRepeat);

        txtStartDate.setOnClickListener(this);
        txtStartTime.setOnClickListener(this);
        txtEndDate.setOnClickListener(this);
        txtEndTime.setOnClickListener(this);
        btnNewLocation.setOnClickListener(this);

        geofences = geofenceStorage.getGeofences(this);

        for (int i = 0; i < geofences.size(); i++)
        {
            locationList[i] = geofences.get(i).getGeofenceId();
        }

        spinnerLocation = (Spinner) findViewById(R.id.spinnerPlace);
        ArrayAdapter<String> adapter_state = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, locationList);
        adapter_state.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // View position = adapter_state.getDropDownView(getIntent().getIntExtra("Position", 0), spinnerLocation, FavoritePlaces.PlaceAdapter.);
        spinnerLocation.setAdapter(adapter_state);
        spinnerLocation.setOnItemSelectedListener(this);

        spinnerRepeat = (Spinner) findViewById(R.id.spinnerRepeat);

        ArrayAdapter<String> adapter_repeat = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, repeat);
        adapter_state.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRepeat.setAdapter(adapter_repeat);
        spinnerRepeat.setOnItemSelectedListener(this);


        //Fetch auth data (the username) on load
        mMobileServicesClient.getAuthData(new TableJsonQueryCallback() {
            @Override
            public void onCompleted(JsonElement result, int count, Exception exception,
                                    ServiceFilterResponse response)
            {
                if (exception == null)
                {
                    JsonArray results = result.getAsJsonArray();
                    JsonElement item = results.get(0);
                    String email = item.getAsJsonObject().getAsJsonPrimitive(
                            "Email").getAsString();
                    pEmail = email;
                }
                else
                {
                    Log.e(TAG, "There was an exception getting auth data: " +
                            exception.getMessage());
                }
            }
        });

        alarmReceiver = new AlarmReceiver();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        bundle.putString("event_name", txtEvent.getText().toString());
        bundle.putString("spinner_location", spinnerLoc);
        bundle.putString("child_name", txtChild.getText().toString());
        bundle.putString("start_date", txtStartDate.getText().toString());
        bundle.putString("start_time", txtStartTime.getText().toString());
        bundle.putString("end_date", txtEndDate.getText().toString());
        bundle.putString("end_time", txtEndTime.getText().toString());
        bundle.putString("spinner_repeat", spinnerRep);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        txtEndDate.setText(savedInstanceState.getString("event_name"));
        txtEndDate.setText(savedInstanceState.getString("spinner_location"));
        txtEndDate.setText(savedInstanceState.getString("child_name"));
        txtEndDate.setText(savedInstanceState.getString("start_date"));
        txtEndDate.setText(savedInstanceState.getString("start_time"));
        txtEndDate.setText(savedInstanceState.getString("end_date"));
        txtEndDate.setText(savedInstanceState.getString("end_time"));
        txtEndDate.setText(savedInstanceState.getString("spinner_repeat"));
    }

    @Override
    protected void onResume() {
        super.onResume();

        m_My_children = childModelController.getMyChildren(this);

        txtChild.setText(childModelController.getCurrentChild().getName());
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.new_cal_event, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_save) {
            Intent intent = new Intent(this, AlarmReceiver.class);

            int date;
            int month;
            int year;

            String startDate = txtStartDate.getText().toString();
            String startTime = txtStartTime.getText().toString();
            String endDate = txtEndDate.getText().toString();
            String endTime = txtEndTime.getText().toString();
            //int eventId = Integer.parseInt(bundle.getString("event_id"));

            //Convert date/month/year to int
            String[] sepDate = startDate.split("-");
            date = Integer.parseInt(sepDate[0]);
            month = Integer.parseInt(sepDate[1]);
            year = Integer.parseInt(sepDate[2]);

            //Convert minute/hour to int
            String[] sepTime = startTime.split(":");

            int hour = Integer.parseInt(sepTime[0]);
            int minute = Integer.parseInt(sepTime[1]);
            // int seconds = Integer.parseInt(sepDate[2]);

            // calendar.set(Calendar.AM_PM, Calendar.AM);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());

            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);

            // January is month 0!!!!
            // Very important to remember to roll back the time one month!!!!
            --month;

            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.DAY_OF_MONTH, date);

            Bundle bundle = new Bundle();
            bundle.putSerializable("Geofences", geofences);

            WmfGeofenceController wmfGeofenceController = new WmfGeofenceController();
            PendingIntent pendingIntent1 = wmfGeofenceController.getTransitionPendingIntent(this);
            try {
                pendingIntent1.send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }


            PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), 0, intent, 0);
            AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            // Don't do anything here as this will fire the alarm instantly
            saveEvent();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View v) {

        if (v == txtStartDate) {

            // Process to get Current Date
            final Calendar c = Calendar.getInstance();
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);

            // Launch Date Picker Dialog
            DatePickerDialog dpd = new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {
                            // Display Selected date in textbox
                            txtStartDate.setText(dayOfMonth + "-"
                                    + (monthOfYear + 1) + "-" + year);

                        }
                    }, mYear, mMonth, mDay);
            dpd.show();
        }
        if (v == txtStartTime) {

            // Process to get Current Time
            final Calendar c = Calendar.getInstance();
            mHour = c.get(Calendar.HOUR_OF_DAY);
            mMinute = c.get(Calendar.MINUTE);

            // Launch Time Picker Dialog
            tpd = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay,
                                          int minute) {
                        // Display Selected time in textbox
                        txtStartTime.setText(hourOfDay + ":" + minute);
                    }
                }, mHour, mMinute, true);

            tpd.show();
        }

        if (v == txtEndDate) {

            // Process to get Current Date
            final Calendar c = Calendar.getInstance();
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);

            // Launch Date Picker Dialog
            DatePickerDialog dpd = new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {
                            // Display Selected date in textbox
                            txtEndDate.setText(dayOfMonth + "-"
                                    + (monthOfYear + 1) + "-" + year);

                        }
                    }, mYear, mMonth, mDay);
            dpd.show();
        }
        if (v == txtEndTime) {

            // Process to get Current Time
            final Calendar c = Calendar.getInstance();
            mHour = c.get(Calendar.HOUR_OF_DAY);
            mMinute = c.get(Calendar.MINUTE);

            // Launch Time Picker Dialog
            TimePickerDialog tpd = new TimePickerDialog(this,
                    new TimePickerDialog.OnTimeSetListener() {

                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay,
                                              int minute) {
                            // Display Selected time in textbox
                            txtEndTime.setText(hourOfDay + ":" + minute);
                        }
                    }, mHour, mMinute, true);
            tpd.show();
        }
        if (v == btnNewLocation) {
            Intent newLocationIntent = new Intent(getApplicationContext(), AddNewLocation.class);
            startActivity(newLocationIntent);
        }
    }

    public void onItemSelected(AdapterView<?> parent, View view, int position,
                               long id) {

        /*spinnerLocation.setSelection(position);
        spinnerLoc = (String) spinnerLocation.getSelectedItem();

        spinnerRepeat.setSelection(position);
        spinnerRep = (String) spinnerRepeat.getSelectedItem();*/
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public void saveEvent(){
        if (txtEvent.getText().toString().equals("") ||
                txtEvent.getText().toString().equals("") ||
                txtEndTime.getText().toString().equals("") ||
                txtStartDate.getText().toString().equals("") ||
                txtStartTime.getText().toString().equals("")) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.SaveEventErrorMessage1), Toast.LENGTH_SHORT).show();
            Log.w(TAG, "You must enter all fields to save event");
            return;
        } else {
            mMobileServicesClient.newCalEvent(pEmail, cEmail, txtEvent.getText().toString(),
                    spinnerLoc, selectedChild,
                    txtStartDate.getText().toString(),
                    txtStartTime.getText().toString(),
                    txtEndDate.getText().toString(),
                    txtEndTime.getText().toString(),
                    spinnerRep,
                    new TableJsonOperationCallback() {
                        @Override
                        public void onCompleted(JsonObject jsonObject, Exception exception,
                                                ServiceFilterResponse response) {
                            if (exception == null) {
                                eventID = jsonObject.get("id").getAsString();
                                //TODO
                                //Gem id i shared preff her!!
                                mActivity.finish();
                            } else {
                                Toast.makeText(getApplicationContext(),
                                        "There was an error registering the event: " + exception.getCause().getMessage(), Toast.LENGTH_LONG).show();
                                Log.e(TAG, "There was an error registering the event: " + exception.getMessage());
                            }
                        }
                    });
            }
        }

    /*public void startRepeatingTimer() {
        Context context = this.getApplicationContext();
        if(alarmReceiver != null){
            alarmReceiver.SetAlarm(context);
        }else{
            Toast.makeText(context, "Alarm is null", Toast.LENGTH_SHORT).show();
        }
    }

    public void cancelRepeatingTimer(){
        Context context = this.getApplicationContext();
        if(alarmReceiver != null){
            alarmReceiver.CancelAlarm(context);
        }else{
            Toast.makeText(context, "Alarm is null", Toast.LENGTH_SHORT).show();
        }
    }

    public void onetimeTimer(){
        Context context = this.getApplicationContext();
        if(alarmReceiver != null){
            alarmReceiver.setOnetimeAlarm(context);
        }else{
            Toast.makeText(context, "Alarm is null", Toast.LENGTH_SHORT).show();
        }
    }*/

    public Calendar getAlarmTime(Intent intent)
    {
        Calendar calendar = Calendar.getInstance();
        TimeZone timeZone = TimeZone.getDefault();
        calendar.setTimeZone(timeZone);

        int date;
        int month;
        int year;

        /*Intent intent = new Intent(this, AlarmService.class);
        intent.putExtras(bundle);*/
        // Bundle bundle = intent.getExtras();
        //String eventName = bundle.getString("event_name");
        // String newEventName = bundle.getString("message");
        String startDate = intent.getStringExtra("start_date");
        String startTime = intent.getStringExtra("start_time");
        String endDate = intent.getStringExtra("end_date");
        String endTime = intent.getStringExtra("end_time");
        //int eventId = Integer.parseInt(bundle.getString("event_id"));

        //Convert date/month/year to int
        String[] sepDate = startDate.split("-");
        date = Integer.parseInt(sepDate[0]);
        month = Integer.parseInt(sepDate[1]);
        year = Integer.parseInt(sepDate[2]);

        //Convert minute/hour to int
        String[] sepTime = startTime.split(":");

        int hour = Integer.parseInt(sepTime[0]);
        int minute = Integer.parseInt(sepTime[1]);
        // int seconds = Integer.parseInt(sepDate[2]);

        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.AM_PM,Calendar.PM);

        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.DAY_OF_MONTH, date);
        // January is month 0!!!!
        // Very important to remember to roll back the time one month!!!!
        // calendar.roll(Calendar.MONTH, false);

        return calendar;
    }
}
