package dk.projekt.bachelor.wheresmyfamily.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.TableJsonOperationCallback;
import com.microsoft.windowsazure.mobileservices.TableJsonQueryCallback;

import java.util.ArrayList;
import java.util.Calendar;

import dk.projekt.bachelor.wheresmyfamily.DataModel.Child;
import dk.projekt.bachelor.wheresmyfamily.R;
import dk.projekt.bachelor.wheresmyfamily.UserInfoStorage;
import dk.projekt.bachelor.wheresmyfamily.helper.BaseActivity;


public class NewCalEventActivity extends BaseActivity implements
        View.OnClickListener, OnItemSelectedListener {
    private final String TAG = "NewCalEventActivity";
    UserInfoStorage storage = new UserInfoStorage();
    private ArrayList<Child> m_My_children = new ArrayList<Child>();

    private Activity mActivity;
    // Widget GUI
    private EditText txtStartDate, txtStartTime, txtEndDate, txtEndTime, txtEvent, txtChild;
    private Spinner spinnerLocation, spinnerRepeat;
    private Button btnNewLocation;

    // Variable for storing current date and time
    private int mYear, mMonth, mDay, mHour, mMinute;
    private String spinnerLoc, spinnerRep, pEmail, cEmail, selectedChild, eventID;

    private String[] location = { "Lokation", "Skole", "Hjem", "Grim Ven", "Saltmine" };
    private String[] repeat = {"", "Ja" , "Nej"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_cal_event);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        mActivity = this;

        txtStartDate = (EditText) findViewById(R.id.txtStartDate);
        txtStartTime = (EditText) findViewById(R.id.txtStartTime);
        txtEndDate = (EditText) findViewById(R.id.txtEndDate);
        txtEndTime = (EditText) findViewById(R.id.txtEndTime);
        txtEvent = (EditText) findViewById(R.id.txtEvent);
        txtChild=(EditText) findViewById(R.id.txtChild);
        btnNewLocation= (Button) findViewById(R.id.btnnewlocation);

        txtStartDate.setOnClickListener(this);
        txtStartTime.setOnClickListener(this);
        txtEndDate.setOnClickListener(this);
        txtEndTime.setOnClickListener(this);
        btnNewLocation.setOnClickListener(this);

        txtChild.setText(selectedChild);

        spinnerLocation = (Spinner) findViewById(R.id.spinnerPlace);
        ArrayAdapter<String> adapter_state = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, location);
        adapter_state
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLocation.setAdapter(adapter_state);
        spinnerLocation.setOnItemSelectedListener(this);

        spinnerRepeat = (Spinner) findViewById(R.id.spinnerRepeat);
        ArrayAdapter<String> adapter_repeat = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, repeat);
        adapter_state
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
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
    }

    @Override
    protected void onResume() {
        super.onResume();

        m_My_children = storage.loadChildren(this);

        getChildInfo();
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
            TimePickerDialog tpd = new TimePickerDialog(this,
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
        spinnerLocation.setSelection(position);
        spinnerLoc = (String) spinnerLocation.getSelectedItem();

        spinnerRepeat.setSelection(position);
        spinnerRep = (String) spinnerRepeat.getSelectedItem();
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

    private void getChildInfo(){


        for(int i = 0; i < m_My_children.size(); i++)
        {
            if(m_My_children.get(i).getIsCurrent())
                cEmail = m_My_children.get(i).getEmail();
                selectedChild = m_My_children.get(i).getName();
        }
    }
}
