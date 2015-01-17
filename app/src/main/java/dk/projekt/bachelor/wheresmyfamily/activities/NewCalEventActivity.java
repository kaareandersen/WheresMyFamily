package dk.projekt.bachelor.wheresmyfamily.activities;

import android.app.ActionBar;
import android.app.Activity;
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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.TableJsonOperationCallback;
import com.microsoft.windowsazure.mobileservices.TableJsonQueryCallback;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import dk.projekt.bachelor.wheresmyfamily.BroadCastReceiver.AlarmReceiver;
import dk.projekt.bachelor.wheresmyfamily.Controller.ChildModelController;
import dk.projekt.bachelor.wheresmyfamily.Controller.WmfGeofenceController;
import dk.projekt.bachelor.wheresmyfamily.DataModel.Child;
import dk.projekt.bachelor.wheresmyfamily.DataModel.WmfGeofence;
import dk.projekt.bachelor.wheresmyfamily.R;
import dk.projekt.bachelor.wheresmyfamily.Services.ReceiveTransitionsIntentService;
import dk.projekt.bachelor.wheresmyfamily.Storage.GeofenceStorage;
import dk.projekt.bachelor.wheresmyfamily.helper.BaseActivity;


public class NewCalEventActivity extends BaseActivity implements
        View.OnClickListener, OnItemSelectedListener, GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener, LocationClient.OnAddGeofencesResultListener,
        LocationClient.OnRemoveGeofencesResultListener
{
    //region Fields
    private final String TAG = "NewCalEventActivity";
    private ArrayList<Child> m_My_children = new ArrayList<Child>();
    private ArrayList<WmfGeofence> geofences;
    GeofenceStorage geofenceStorage;
    private ArrayList<Geofence> activeGeofences = new ArrayList<Geofence>();

    private LocationClient locationClient;

    private PendingIntent mGeofenceRequestIntent;

    // Store the list of geofence Ids to remove
    List<String> mGeofencesToRemove;

    // Defines the allowable request types.
    public enum REQUEST_TYPE {ADD, REMOVE_INTENT, REMOVE_LIST }
    private REQUEST_TYPE mRequestType;
    // Flag that indicates if a request is underway.
    private boolean mInProgress;
    ArrayList<WmfGeofence> mCurrentGeofences;


    private Activity mActivity;
    // Widget GUI
    private EditText txtStartDate, txtStartTime, txtEndDate, txtEndTime, txtEvent, txtChild;
    private Spinner spinnerLocation, spinnerRepeat;
    private Button btnNewLocation;

    // Variable for storing current date and time
    private int mYear, mMonth, mDay, mHour, mMinute;
    private long expiration;
    private String spinnerLoc, spinnerRep, pEmail, cEmail, selectedChild, eventID;
    Spinner spinner;
    AlarmReceiver alarmReceiver;
    TimePickerDialog tpd;

    private String[] locationList;
    private String[] repeat = {"", "Ja" , "Nej"};
    private PendingIntent pendingIntent;
    Child currentChild;
    public static final String NEW_CALENDAR_EVENT_ACTION = "new.calendar.event";

    Bundle bundle = new Bundle();
    ChildModelController childModelController = new ChildModelController();
    //endregion

    //region Life cycle events
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_cal_event);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        mInProgress = false;

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
        spinnerLocation.setAdapter(adapter_state);
        spinnerLocation.setOnItemSelectedListener(this);
        // Select the item parsed from the favorite list selection performed by the user
        spinnerLocation.setSelection(getIntent().getIntExtra("Position", 0));

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

        locationClient = new LocationClient(this, this, this);

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
        cEmail = childModelController.getCurrentChild().getEmail();
        selectedChild = childModelController.getCurrentChild().getName();
    }

    @Override
    protected void onStart() {
        super.onStart();

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
        switch (id) {
            case R.id.action_save:
                Intent intent = new Intent(this, AlarmReceiver.class);

                String startDateText = txtStartDate.getText().toString();
                String startTimeText = txtStartTime.getText().toString();
                String endDateText = txtEndDate.getText().toString();
                String endTimeText = txtEndTime.getText().toString();
                //int eventId = Integer.parseInt(bundle.getString("event_id"));

                //Convert start date/month/year to int
                String[] sepStartDate = startDateText.split("-");
                int startDate = Integer.parseInt(sepStartDate[0]);
                int startMonth = Integer.parseInt(sepStartDate[1]);
                int startYear = Integer.parseInt(sepStartDate[2]);

                //Convert start minute/hour to int
                String[] sepStartTime = startTimeText.split(":");
                int startHour = Integer.parseInt(sepStartTime[0]);
                int startMinute = Integer.parseInt(sepStartTime[1]);
                // int seconds = Integer.parseInt(sepDate[2]);

                // calendar.set(Calendar.AM_PM, Calendar.AM);
                Calendar calendarStart = Calendar.getInstance();
                calendarStart.setTimeInMillis(System.currentTimeMillis());

                calendarStart.set(Calendar.HOUR_OF_DAY, startHour);
                calendarStart.set(Calendar.MINUTE, startMinute);
                calendarStart.set(Calendar.SECOND, 0);

                // January is month 0!!!!
                // Very important to remember to roll back the time one month!!!!
                --startMonth;

                calendarStart.set(Calendar.MONTH, startMonth);
                calendarStart.set(Calendar.YEAR, startYear);
                calendarStart.set(Calendar.DAY_OF_MONTH, startDate);

                Calendar calendarEnd = Calendar.getInstance();

                //Convert end date/month/year to int
                String[] sepEndDate = endDateText.split("-");
                int endDate = Integer.parseInt(sepEndDate[0]);
                int endMonth = Integer.parseInt(sepEndDate[1]);
                int endYear = Integer.parseInt(sepEndDate[2]);

                //Convert end minute/hour to int
                String[] sepEndTime = endTimeText.split(":");
                int endHour = Integer.parseInt(sepEndTime[0]);
                int endMinute = Integer.parseInt(sepEndTime[1]);

                calendarEnd.set(Calendar.HOUR_OF_DAY, endHour);
                calendarEnd.set(Calendar.MINUTE, endMinute);
                calendarEnd.set(Calendar.SECOND, 0);

                // January is month 0!!!!
                // Very important to remember to roll back the time one month!!!!
                --endMonth;

                calendarEnd.set(Calendar.MONTH, endMonth);
                calendarEnd.set(Calendar.YEAR, endYear);
                calendarEnd.set(Calendar.DAY_OF_MONTH, endDate);

                WmfGeofenceController wmfGeofenceController = new WmfGeofenceController();
                ArrayList<WmfGeofence> temp = wmfGeofenceController.getAllGeofences(this);
                spinnerLocation.getSelectedItemPosition();
                String text = spinnerLocation.getSelectedItem().toString();
                mCurrentGeofences = new ArrayList<WmfGeofence>();
                expiration = calendarEnd.getTimeInMillis();

                for (int i = 0; i < temp.size(); i++) {
                    if (temp.get(i).getGeofenceId().toString().equals(text))
                    {
                        temp.get(i).setExpirationDuration(calendarEnd.getTimeInMillis());
                        mCurrentGeofences.add(temp.get(i));
                    }
                }

                // addGeofences();

                /*PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(),
                        0, intent, 0);
                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                alarmManager.set(AlarmManager.RTC_WAKEUP, calendarStart.getTimeInMillis(), pendingIntent);*/

                saveEvent();

                return true;
        }

        return super.onOptionsItemSelected(item);
    }
    //endregion


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
        /*WmfGeofenceController wmfGeofenceController = new WmfGeofenceController();
        wmfGeofenceController.noCurrentGeofence(geofences);
        geofences.get(position).setIsCurrent(true);
        mCurrentGeofences = new ArrayList<Geofence>();
        mCurrentGeofences.add(geofences.get(position).toGeofence());*/
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public void saveEvent(){
        spinnerLoc = spinnerLocation.getSelectedItem().toString();
        spinnerRep = spinnerRepeat.getSelectedItem().toString();

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
                    mCurrentGeofences.get(0), selectedChild,
                    txtStartDate.getText().toString(),
                    txtStartTime.getText().toString(),
                    txtEndDate.getText().toString(),
                    txtEndTime.getText().toString(), expiration,
                    spinnerRep,
                    new TableJsonOperationCallback() {
                        @Override
                        public void onCompleted(JsonObject jsonObject, Exception exception,
                                                ServiceFilterResponse response) {
                            if (exception == null) {
                                eventID = jsonObject.get("id").getAsString();
                                //TODO
                                //Gem id i shared prefs her!!
                                mActivity.finish();
                            } else {
                                Toast.makeText(getApplicationContext(),
                                        "There was an error registering the event: " +
                                                exception.getCause().getMessage(),
                                                    Toast.LENGTH_LONG).show();
                                Log.e(TAG, "There was an error registering the event: " +
                                        exception.getMessage());
                            }
                        }
                    });
            }
        }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onConnected(Bundle bundle) {

/*        if (mRequestType != null)
        {
            switch (mRequestType)
            {
                case ADD:
                    mInProgress = true;
                    mGeofenceRequestIntent = getTransitionPendingIntent();
                    // Send a request to add the current geofences
                    locationClient.addGeofences(mCurrentGeofences, mGeofenceRequestIntent, this);
                    Toast.makeText(this, "AddGeofence request sent", Toast.LENGTH_SHORT).show();
                    break;
                case REMOVE_INTENT:
                    mInProgress = true;
                    mGeofenceRequestIntent = getTransitionPendingIntent();
                    locationClient.removeGeofences(mGeofenceRequestIntent, this);
                    Toast.makeText(this, "RemoveGeofence request sent", Toast.LENGTH_SHORT).show();
                    break;
                case REMOVE_LIST:
                    mInProgress = true;
                    locationClient.removeGeofences(mGeofencesToRemove, this);
                    Toast.makeText(this, "RemoveAllGeofences request sent", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    try {
                        throw new Exception("Unknown request type in onConnected().");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }

            mInProgress = false;
        }*/
    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onAddGeofencesResult(int i, String[] strings) {

    }

    private PendingIntent getTransitionPendingIntent()
    {
        // Create an explicit Intent
        Intent intent = new Intent(this, ReceiveTransitionsIntentService.class);
        startService(intent);

        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onRemoveGeofencesByRequestIdsResult(int i, String[] strings) {

    }

    @Override
    public void onRemoveGeofencesByPendingIntentResult(int i, PendingIntent pendingIntent) {

    }

    private boolean servicesConnected() {

        int resultCode =
                GooglePlayServicesUtil.
                        isGooglePlayServicesAvailable(this);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d("Activity Recognition",
                    "Google Play services is available.");
            // Continue
            return true;
            // Google Play services was not available for some reason
        } else {

            return false;
        }
    }

    /**
     * Start a request for geofence monitoring by calling
     * LocationClient.connect().
     */
    public void addGeofences()
    {
        /*// Disconnect the locationClient to ensure call to onConnected
        if(locationClient.isConnected())
            locationClient.disconnect();*/

        // Start a request to add geofences
        mRequestType = REQUEST_TYPE.ADD;
        /*
         * Test for Google Play services after setting the request type.
         * If Google Play services isn't present, the proper request
         * can be restarted.
         */
        if (!servicesConnected())
            return;


        // If a request is not already underway
        if (!mInProgress)
        {
            // Indicate that a request is underway
            mInProgress = true;

            locationClient = new LocationClient(this, this, this);
            // Request a connection from the client to Location Services
            locationClient.connect();
        }
        else
        {
            // A request is already underway. To handle this situation:
            // Disconnect the client
            locationClient.disconnect();
            // Reset the flag
            mInProgress = false;
            // Retry the request.
            addGeofences(); // requestIntent
        }
    }
    /**
     * Start a request to remove geofences by calling
     * LocationClient.connect()
     */
    public void removeAllGeofences() // PendingIntent requestIntent
    {
        // Record the type of removal request
        mRequestType = REQUEST_TYPE.REMOVE_INTENT;
        /*
         * Test for Google Play services after setting the request type.
         * If Google Play services isn't present, the request can be
         * restarted.
         */
        if (!servicesConnected())
            return;

        /*// Store the PendingIntent
        mGeofenceRequestIntent = requestIntent;*/
        /*
         * Create a new location client object. Since the current
         * activity class implements ConnectionCallbacks and
         * OnConnectionFailedListener, pass the current activity object
         * as the listener for both parameters
         */
        locationClient = new LocationClient(this, this, this);
        // If a request is not already underway
        if (!mInProgress)
        {
            // Indicate that a request is underway
            mInProgress = true;
            // Request a connection from the client to Location Services
            locationClient.connect();
        }
        else
        {
            // A request is already underway. To handle this situation:
            // Disconnect the client
            locationClient.disconnect();
            // Reset the flag
            mInProgress = false;
            // Retry the request.
            removeAllGeofences(); // requestIntent
        }
    }



    /**
     * Start a request to remove monitoring by
     * calling LocationClient.connect()
     *
     */
    public void removeGeofences(List<String> geofenceIds)
    {
        // If Google Play services is unavailable, exit
        // Record the type of removal request
        mRequestType = REQUEST_TYPE.REMOVE_LIST;
        /*
         * Test for Google Play services after setting the request type.
         * If Google Play services isn't present, the request can be
         * restarted.
         */
        if (!servicesConnected())
            return;

        // Store the list of geofences to remove
        mGeofencesToRemove = geofenceIds;
        /*
         * Create a new location client object. Since the current
         * activity class implements ConnectionCallbacks and
         * OnConnectionFailedListener, pass the current activity object
         * as the listener for both parameters
         */
        locationClient = new LocationClient(this, this, this);
        // If a request is not already underway
        if (!mInProgress)
        {
            // Indicate that a request is underway
            mInProgress = true;
            // Request a connection from the client to Location Services
            locationClient.connect();
        }
        else
        {
            // A request is already underway. To handle this situation:
            // Disconnect the client
            locationClient.disconnect();
            // Reset the flag
            mInProgress = false;
            // Retry the request.
            removeGeofences(geofenceIds);
        }
    }
}
