package dk.projekt.bachelor.wheresmyfamily.activities;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.microsoft.windowsazure.messaging.NotificationHub;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.TableJsonQueryCallback;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Calendar;

import dk.projekt.bachelor.wheresmyfamily.Controller.MobileServicesClient;
import dk.projekt.bachelor.wheresmyfamily.Controller.NotificationHubController;
import dk.projekt.bachelor.wheresmyfamily.Controller.ParentModelController;
import dk.projekt.bachelor.wheresmyfamily.Controller.PushNotificationController;
import dk.projekt.bachelor.wheresmyfamily.Controller.WmfGeofenceController;
import dk.projekt.bachelor.wheresmyfamily.DataModel.Parent;
import dk.projekt.bachelor.wheresmyfamily.DataModel.WmfGeofence;
import dk.projekt.bachelor.wheresmyfamily.R;
import dk.projekt.bachelor.wheresmyfamily.Services.ActivityRecognitionIntentService;
import dk.projekt.bachelor.wheresmyfamily.Services.ReceiveTransitionsIntentService;
import dk.projekt.bachelor.wheresmyfamily.authenticator.AuthenticationApplication;
import dk.projekt.bachelor.wheresmyfamily.helper.BaseActivity;

public class LoggedInChild extends BaseActivity implements GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener, LocationClient.OnAddGeofencesResultListener,
        LocationListener {

    //region Fields
    private final String TAG = "LoggedInChild";
    protected NotificationHubController mNotificationHubController;
    protected PushNotificationController pushNotificationController;
    private TextView mLblUsernameValue, parentInfoName, parentInfoPhone;
    Parent parent = new Parent();
    private String cEmail;
    Location currentLocation;
    LocationClient locationClient;
    // Define an object that holds accuracy and frequency parameters
    private LocationRequest mLocationRequest;
    String parentEmail;
    ListView listView ;

    // Flag that indicates if a request is underway.
    private boolean mInProgress;

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    // Setup Location update interval
    private static final int MILLISECONDS_PER_SECOND = 1000;
    private static final int UPDATE_INTERVAL_IN_SECONDS = 100;
    private static final long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    private static final int FASTEST_INTERVAL_IN_SECONDS = 100;
    private static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;
    private static final float SMALLEST_DISPLACEMENT_IN_METERS = 25;

    private static final long SECONDS_PER_HOUR = 60;
    private static final long GEOFENCE_EXPIRATION_IN_HOURS = 12;
    private static final long GEOFENCE_EXPIRATION_TIME =
            GEOFENCE_EXPIRATION_IN_HOURS * SECONDS_PER_HOUR * MILLISECONDS_PER_SECOND;

    // Constants that define the activity detection interval
    public static final int DETECTION_INTERVAL_SECONDS = 20;
    public static final int DETECTION_INTERVAL_MILLISECONDS =
            MILLISECONDS_PER_SECOND * DETECTION_INTERVAL_SECONDS;

    private String SENDER_ID = "911215571794";
    private GoogleCloudMessaging mGcm;
    private NotificationHub mHub;
    private String mRegistrationId;
    public static LoggedInChild instance = null;

    JSONArray mChildren = new JSONArray();
    ArrayList<Parent> mParents = new ArrayList<Parent>();

    ParentModelController parentModelController;
    WmfGeofenceController wmfGeofenceController;
    // Store the PendingIntent used to send activity recognition events back to the app
    private PendingIntent mActivityRecognitionPendingIntent;
    // Store the current activity recognition client
    private ActivityRecognitionClient mActivityRecognitionClient;

    // Defines the allowable request types.
    public enum REQUEST_TYPE { ADD, REMOVE_INTENT, REMOVE_LIST,
        START, STOP
    }

    private REQUEST_TYPE mRequestType;

    private PendingIntent mGeofenceRequestIntent;

    ArrayList<com.google.android.gms.location.Geofence> mCurrentGeofences;
    ArrayList<WmfGeofence> wmfGeofences;
    //endregion

    //region Lifecycle events
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_in_child);

        instance = this;

        //Notification hub Azure
        mNotificationHubController = new NotificationHubController(this);
        pushNotificationController = new PushNotificationController(this);

        parentModelController = new ParentModelController();
        wmfGeofenceController = new WmfGeofenceController();

        // Reference UI elements
        mLblUsernameValue = (TextView) findViewById(R.id.lblUsernameValue);
        parentInfoName = (TextView) findViewById(R.id.parentinput);
        parentInfoPhone = (TextView) findViewById(R.id.phoneinput);

        mParents = parentModelController.getMyParents(this);

        if (mParents.size() > 0) {
            parentInfoName.setText(mParents.get(0).getName());
            parentInfoPhone.setText(mParents.get(0).getPhone());
        }

        // Authenticate
        AuthenticationApplication myApp = (AuthenticationApplication) getApplication();
        MobileServicesClient mobileServicesClient = myApp.getAuthService();

        //Fetch auth data (the username) on load
        mMobileServicesClient.getAuthData(new TableJsonQueryCallback() {
            @Override
            public void onCompleted(JsonElement result, int count, Exception exception,
                                    ServiceFilterResponse response) {
                if (exception == null) {
                    JsonArray results = result.getAsJsonArray();
                    JsonElement item = results.get(0);
                    mLblUsernameValue.setText(item.getAsJsonObject().getAsJsonPrimitive("UserName").getAsString());
                    cEmail = item.getAsJsonObject().getAsJsonPrimitive("Email").getAsString();
                    mNotificationHubController.registerWithNotificationHubs(cEmail);
                } else {
                    Log.e(TAG, "There was an exception getting auth data: " + exception.getMessage());
                }
            }
        });

        // Initialize the location
        if (locationClient == null)
            locationClient = new LocationClient(this, this, this);

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setSmallestDisplacement(SMALLEST_DISPLACEMENT_IN_METERS);

        locationClient.connect();

        mActivityRecognitionClient =
                new ActivityRecognitionClient(this, this, this);

        mActivityRecognitionClient.connect();

        Intent intent = new Intent(
                this, ActivityRecognitionIntentService.class);

        mActivityRecognitionPendingIntent =
                PendingIntent.getService(this, 0, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);


        // startUpdates();

        ImageButton button = (ImageButton) findViewById(R.id.callchild);
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + parent.getPhone()));
                startActivity(callIntent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        /*Intent intent = new Intent(this, ActivityRecognitionIntentService.class);
        startService(intent);*/

        mParents = parentModelController.getMyParents(this);

        wmfGeofences = wmfGeofenceController.getAllGeofences(this);

        /*for(int i = 0; i < wmfGeofences.size(); i++)
        {
            mCurrentGeofences.add(wmfGeofences.get(i).toGeofence());
        }*/

        if (mParents.size() > 0) {
            parentInfoName.setText(mParents.get(0).getName());
            parentInfoPhone.setText(mParents.get(0).getPhone());
            parentEmail = mParents.get(0).getEmail();
        }

        locationClient.connect();
    }

    @Override
    protected void onStart() {
        super.onStart();

        /*Intent intent = new Intent(this, ReceiveTransitionsIntentService.class);
        startService(intent);*/
    }

    //endregion

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.logged_in_child, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_logout:
                mMobileServicesClient.logout(true);
                mNotificationHubController.unRegisterNH();
                return true;
            case R.id.action_deleteusr:
                deleteDialogBox();
                return true;
            case R.id.action_addChild:
                Intent register = new Intent(this, RegisterParent.class);
                startActivity(register);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void deleteDialogBox() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Bekræft Sletning af Brugerprofil");
        builder.setMessage("Er du sikker?");

        builder.setPositiveButton("JA", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                // Do nothing but close the dialog
                mMobileServicesClient.deleteUser();
                mNotificationHubController.unRegisterNH();
                Toast.makeText(getApplicationContext(),
                        "Din Brugerprofil er nu slettet!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }

        });

        builder.setNegativeButton("NEJ", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    public void getAndPushLocation() {

        String location = currentLocation.toString();

        pushNotificationController.sendLocationFromChild(parentEmail, location);
    }

    //region Location callback methods
    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
        // Send the location change to the server FIXME
        /*String parentEmail = mParents.get(0).getEmail();
        pushNotificationController.sendLocationFromChild(parentEmail, location.toString());*/
        // sendLocationUpdates();
    }

    @Override
    public void onConnected(Bundle bundle) {

        Toast.makeText(this, "LocationActivity connected", Toast.LENGTH_SHORT).show();

        locationClient.requestLocationUpdates(mLocationRequest, this);

        if (mRequestType != null)
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
                default:
                    try {
                        throw new Exception("Unknown request type in onConnected().");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }

            mInProgress = false;
        }
    }

    @Override
    public void onDisconnected() {

        locationClient = null;

    }

    @Override
    public void onAddGeofencesResult(int i, String[] strings) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);

            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {

            // Get the error code
            int errorCode = connectionResult.getErrorCode();
            // Get the error dialog from Google Play services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(errorCode, this,
                    CONNECTION_FAILURE_RESOLUTION_REQUEST);
        }
    }
    //endregion

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Decide what to do based on the original request code
        switch (requestCode) {
            case CONNECTION_FAILURE_RESOLUTION_REQUEST:

                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // Try the request again
                        onActivityResult(requestCode, resultCode, data);
                        break;
                }
        }
    }

    //region Activity Recognition


    public void startUpdates() {
        // Set the request type to START
        mRequestType = REQUEST_TYPE.START;

        // Check for Google Play services
        if (!servicesConnected())
            return;

        // If a request is not already underway
        if (!mInProgress) {
            // Indicate that a request is in progress
            mInProgress = true;
            // Request a connection to Location Services
            mActivityRecognitionClient.connect();
            //
        } else {
            // A request is already underway. To handle this situation:
            // Disconnect the client
            locationClient.disconnect();
            // Reset the flag
            mInProgress = false;
            // Retry the request.
            startUpdates();
        }
    }


    public void stopUpdates() {
        // Set the request type to STOP
        mRequestType = REQUEST_TYPE.STOP;

        if (!servicesConnected())
            return;

        // If a request is not already underway
        if (!mInProgress) {
            // Indicate that a request is in progress
            mInProgress = true;
            // Request a connection to Location Services
            mActivityRecognitionClient.connect();
            //
        } else {
            // Disconnect the client
            locationClient.disconnect();
            // Reset the flag
            mInProgress = false;
            // Retry the request.
            stopUpdates();
        }
    }
    //endregion

    @SuppressWarnings("unchecked")
    public void sendLocationUpdates() {
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object... params) {
                try {

                        // Send the updated location to the server
                        // Set the update interval according to the broadcast
                        // from ActivityRecognitionIntentService
                        // getAndPushLocation();
                } catch (Exception e) {
                    Log.e(TAG, "Problem med at kontakte serveren: " + e.getMessage());
                    return e;
                }
                return null;
            }
        }.execute(null, null, null);
    }


    // region Error dialog

    // Define a DialogFragment that displays the error dialog
    public static class ErrorDialogFragment extends DialogFragment {
        // Global field to contain the error dialog
        private Dialog mDialog;

        // Default constructor. Sets the dialog field to null
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        // Set the dialog to display
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        // Return a Dialog to the DialogFragment.
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }

    private boolean servicesConnected() {
        // Check that Google Play services is available
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
            locationClient.disconnect();
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

    private PendingIntent getTransitionPendingIntent()
    {
        // Create an explicit Intent
        Intent intent = new Intent(this, ReceiveTransitionsIntentService.class);
        startService(intent);

        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void listEvents(){
        // Get ListView object from xml
        listView = (ListView) findViewById(R.id.event_list_child);

        // Defined Array values to show in ListView
        String[] values = new String[] {};

        // Define a new Adapter
        // First parameter - Context
        // Second parameter - Layout for the row
        // Third parameter - ID of the TextView to which the data is written
        // Forth - the Array of data

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, values);


        // Assign adapter to ListView
        listView.setAdapter(adapter);

        // ListView Item Click Listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                // ListView Clicked item index
                int itemPosition     = position;

                // ListView Clicked item value
                String  itemValue    = (String) listView.getItemAtPosition(position);

                // Show Alert
                Toast.makeText(getApplicationContext(),
                        "Position :"+itemPosition+"  ListItem : " +itemValue , Toast.LENGTH_LONG)
                        .show();

            }

        });
    }

    public void AlarmHandler(int expiration, int startHour, int startMinute, int startMonth, int startYear, int startDate)
    {
        Intent intent = new Intent(this, ReceiveTransitionsIntentService.class);

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

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(),
                0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendarStart.getTimeInMillis(), pendingIntent);
    }
}
    //endregion

