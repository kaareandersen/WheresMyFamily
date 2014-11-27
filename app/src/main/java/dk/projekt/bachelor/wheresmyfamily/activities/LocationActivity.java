package dk.projekt.bachelor.wheresmyfamily.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.fitness.HistoryApi;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dk.projekt.bachelor.wheresmyfamily.Controller.PushNotificationController;
import dk.projekt.bachelor.wheresmyfamily.DataModel.Child;
import dk.projekt.bachelor.wheresmyfamily.GeofenceStorage;
import dk.projekt.bachelor.wheresmyfamily.R;
import dk.projekt.bachelor.wheresmyfamily.Services.ActivityRecognitionIntentService;
import dk.projekt.bachelor.wheresmyfamily.Services.ReceiveTransitionsIntentService;
import dk.projekt.bachelor.wheresmyfamily.UserInfoStorage;
import dk.projekt.bachelor.wheresmyfamily.WmfGeofence;

public class LocationActivity extends FragmentActivity implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener, LocationClient.OnAddGeofencesResultListener,
        LocationListener {

    //region Fields
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private LocationClient mLocationClient;
    private LocationListener locationListener;
    private LatLng mCurrentLocation;
    private HistoryApi historyApi;
    private GoogleApiClient googleApiClient;

    // Define an object that holds accuracy and frequency parameters
    private LocationRequest mLocationRequest;

    // Setup Location update interval
    private static final int MILLISECONDS_PER_SECOND = 1000;
    private static final int UPDATE_INTERVAL_IN_SECONDS = 10;
    private static final long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    private static final int FASTEST_INTERVAL_IN_SECONDS = 10;
    private static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;

    /*
     * Use to set an expiration time for a geofence. After this amount
     * of time Location Services will stop tracking the geofence.
     */
    private static final long SECONDS_PER_HOUR = 60;
    private static final long GEOFENCE_EXPIRATION_IN_HOURS = 12;
    private static final long GEOFENCE_EXPIRATION_TIME =
            GEOFENCE_EXPIRATION_IN_HOURS * SECONDS_PER_HOUR * MILLISECONDS_PER_SECOND;

    // Constants that define the activity detection interval
    public static final int DETECTION_INTERVAL_SECONDS = 20;
    public static final int DETECTION_INTERVAL_MILLISECONDS =
            MILLISECONDS_PER_SECOND * DETECTION_INTERVAL_SECONDS;

    // Map
    GoogleMap map;

    // Stores the PendingIntent used to request geofence monitoring
    private PendingIntent mGeofenceRequestIntent;
    // Defines the allowable request types.
    public enum REQUEST_TYPE {ADD, REMOVE_INTENT, REMOVE_LIST, START, STOP }
    private REQUEST_TYPE mRequestType;
    // Flag that indicates if a request is underway.
    private boolean mInProgress;
    // Store the list of geofence Ids to remove
    List<String> mGeofencesToRemove;

    /*
     * Handles to UI views containing geofence data
     */
    // Handle to geofence 1 latitude in the UI
    private EditText mLatitude1;
    // Handle to geofence 1 longitude in the UI
    private EditText mLongitude1;
    // Handle to geofence 1 radius in the UI
    private EditText mRadius1;
    // Handle to geofence 2 latitude in the UI
    private EditText mLatitude2;
    // Handle to geofence 2 longitude in the UI
    private EditText mLongitude2;
    // Handle to geofence 2 radius in the UI
    private EditText mRadius2;
    /*
     * Internal geofence objects for geofence 1 and 2
     */
    private WmfGeofence mUIGeofence1;
    private WmfGeofence mUIGeofence2;
    // List of currentGeofences
    ArrayList<com.google.android.gms.location.Geofence> mCurrentGeofences;
    // Internal List of WmfGeofence objects
    List<com.google.android.gms.location.Geofence> mGeofenceList;
    // Persistent storage for geofences
    private GeofenceStorage mGeofenceStorage;

    // Hardcoded location for testing
    private static final LatLng GOLDEN_GATE_BRIDGE = new LatLng(37.828891,-122.485884);

    // Store the PendingIntent used to send activity recognition events back to the app
    private PendingIntent mActivityRecognitionPendingIntent;
    // Store the current activity recognition client
    private ActivityRecognitionClient mActivityRecognitionClient;

    public static LocationActivity instance = null;

    Address address;

    String provider;
    LocationManager locationManager;
    DataReadRequest dataReadRequest;

    public static final String myPrefs = "PrefsFile";
    SharedPreferences.Editor prefsEditor;
    SharedPreferences prefs;
    Child myChild;
    private ArrayList<Child> m_My_children = new ArrayList<Child>();
    UserInfoStorage storage = new UserInfoStorage();

    ActionBar actionBar;
    protected PushNotificationController pushNotificationController;

    LatLng currentPosition;
    //endregion

    //region Lifecycle events
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        instance = this;

        pushNotificationController = new PushNotificationController(this);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Initialize the location
        if (mLocationClient == null)
            mLocationClient = new LocationClient(this, this, this);

        map = ((SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map)).getMap();

        if (map == null) {
            Toast.makeText(this, "Google Maps not available",
                    Toast.LENGTH_LONG).show();
        }

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create();
        // Use high accuracy
        mLocationRequest.setPriority(
                LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Set the update interval to 10 seconds
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        // Set the fastest update interval to 10 seconds
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        // mLocationClient.requestLocationUpdates(mLocationRequest, this);

        // Start with the request flag set to false
        mInProgress = false;

        // Instantiate a new geofence storage area
        mGeofenceStorage = new GeofenceStorage(this);

        // Instantiate the current List of geofences
        mCurrentGeofences = new ArrayList<com.google.android.gms.location.Geofence>();

        /*
         * Instantiate a new activity recognition client. Since the
         * parent Activity implements the connection listener and
         * connection failure listener, the constructor uses "this"
         * to specify the values of those parameters.
         */
        mActivityRecognitionClient =
                new ActivityRecognitionClient(this, this, this);
        /*
         * Create the PendingIntent that Location Services uses
         * to send activity recognition updates back to this app.
         */
        Intent intent = new Intent(
                this, ActivityRecognitionIntentService.class);
        /*
         * Return a PendingIntent that starts the IntentService.
         */
        mActivityRecognitionPendingIntent =
                PendingIntent.getService(this, 0, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.location, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menu_set_hybrid:
                map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
            case R.id.menu_set_normal:
                map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case R.id.menu_set_satellite:
                map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.menu_set_terrain:
                map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            case R.id.menu_gotolocation:
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(mCurrentLocation) // Sets the center of the map to
                                // Golden Gate Bridge
                        .zoom(17)    // Sets the zoom
                        .bearing(90) // Sets the orientation of the camera to east
                        .tilt(30)    // Sets the tilt of the camera to 30 degrees
                        .build();    // Creates a CameraPosition from the builder

                map.animateCamera(CameraUpdateFactory.newCameraPosition(
                        cameraPosition));
                map.addMarker(new MarkerOptions()
                        .position(mCurrentLocation)
                        .title("Home").snippet("\nPopulation 3")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                break;
            case R.id.menu_showcurrentlocation:
                // Get the current location and mark it on the map
                map.setMyLocationEnabled(true);

                // Set the camera position to zoom in on the current location
                CameraPosition myPosition = new CameraPosition.Builder()
                        .target(mCurrentLocation).zoom(20).bearing(180).tilt(0).build();
                map.animateCamera(
                        CameraUpdateFactory.newCameraPosition(myPosition));
                break;
            case R.id.menu_test_item:
                Child current = new Child();
                List<Address> addresses;
                String language = "da";
                String country = "DK";
                Locale local = new Locale(language, country);
                StringBuffer markerInfo = new StringBuffer();

                for(int i = 0; i < m_My_children.size(); i++)
                {
                    if(m_My_children.get(i).getIsCurrent())
                        current = m_My_children.get(i);
                }

                /*WmfGeofence wmfGeofence =
                    new WmfGeofence("Hjemme hos Kåre", currentPosition.latitude, currentPosition.longitude,
                            20, GEOFENCE_EXPIRATION_TIME, com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER |
                            com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_EXIT);

                wmfGeofence.toGeofence();*/

                Geocoder geocoder = new Geocoder(this, local);
                try {
                     addresses = geocoder.getFromLocation(currentPosition.latitude, currentPosition.longitude, 1);
                    if(addresses.size() > 0)
                    {
                        // double latitude = addresses.get(0).getLatitude();
                        // double longitude = addresses.get(0).getLongitude();

                        for(int bip = 0; bip < addresses.size(); bip++)
                        {
                            markerInfo.append(addresses.get(bip).toString());
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Set the maptype
                map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                // Add a marker with the selected Child's name and current location
                map.addMarker(new MarkerOptions().position(currentPosition)
                        .title(current.getName()).snippet(markerInfo.toString()));

                // Set the camera position to zoom in on the current location
                CameraPosition currentChildPosition = new CameraPosition.Builder()
                        .target(currentPosition).zoom(22).bearing(90).tilt(0).build();
                map.animateCamera(
                        CameraUpdateFactory.newCameraPosition(currentChildPosition));
                break;
            case R.id.action_overview:
                Intent overview = new Intent(this, OverviewActivity.class);
                startActivity(overview);
                break;
            case R.id.action_calendar:
                Intent calendar = new Intent(this, CalendarActivity.class);
                startActivity(calendar);
                break;
            case R.id.action_map:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLocationClient.connect();

        provider = LocationManager.GPS_PROVIDER;

        m_My_children = storage.loadChildren(this);

        askForLocation();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        // If the client is connected
        if (mLocationClient.isConnected())
        {
            /*
             * Remove location updates for a listener.
             * The current Activity is the listener, so
             * the argument is "this".
             */
            mLocationClient.removeLocationUpdates(this);
        }

        mLocationClient.disconnect();

        super.onStop();
    }
    //endregion

    //region Location callback methods
    @Override
    public void onConnected(Bundle bundle)
    {
        Toast.makeText(this, "LocationActivity connected", Toast.LENGTH_SHORT).show();

        mLocationClient.requestLocationUpdates(mLocationRequest, this);

        if (mRequestType != null)
        {
            switch (mRequestType)
            {
                case ADD:
                    mGeofenceRequestIntent = getTransitionPendingIntent();
                    // Send a request to add the current geofences
                    mLocationClient.addGeofences(mCurrentGeofences, mGeofenceRequestIntent, this);
                case REMOVE_INTENT:
                    mGeofenceRequestIntent = getTransitionPendingIntent();
                    mLocationClient.removeGeofences(mGeofenceRequestIntent,
                            (LocationClient.OnRemoveGeofencesResultListener) this);
                    break;
                case REMOVE_LIST:
                    mLocationClient.removeGeofences(mGeofencesToRemove,
                            (LocationClient.OnRemoveGeofencesResultListener) this);
                    break;
                case START :
                    /*
                     * Request activity recognition updates using the
                     * preset detection interval and PendingIntent.
                     * This call is synchronous.
                     */
                    mActivityRecognitionClient.requestActivityUpdates(DETECTION_INTERVAL_MILLISECONDS,
                            mActivityRecognitionPendingIntent);
                    break;
                case STOP :
                    mActivityRecognitionClient.removeActivityUpdates(mActivityRecognitionPendingIntent);
                    break;
                    /*
                     * An enum was added to the definition of REQUEST_TYPE,
                     * but it doesn't match a known case. Throw an exception.
                     */
                default :
                    try {
                        throw new Exception("Unknown request type in onConnected().");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }

        /*
         * Request activity recognition updates using the preset
         * detection interval and PendingIntent. This call is
         * synchronous.
         */
        /*mActivityRecognitionClient.requestActivityUpdates(
                DETECTION_INTERVAL_MILLISECONDS,
                mActivityRecognitionPendingIntent);*/
        /*
         * Since the preceding call is synchronous, turn off the
         * in progress flag and disconnect the client
         */
        mInProgress = false;
        mActivityRecognitionClient.disconnect();
    }

    @Override
    public void onDisconnected()
    {
        Toast.makeText(this, "LocationActivity disconnected. Please re-connect.",
                Toast.LENGTH_SHORT).show();
        // Turn off the request flag
        mInProgress = false;
        // Destroy the current location client
        mLocationClient = null;
        // Delete the client
        mActivityRecognitionClient = null;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult)
    {
        // Turn off the request flag
        mInProgress = false;

        /*
        * Google Play services can resolve some errors it detects.
        * If the error has a resolution, try sending an Intent to
        * start a Google Play services activity that can resolve
        * error.
        */
        if (connectionResult.hasResolution())
        {
            try
            {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                * Thrown if Google Play services canceled the original
                * PendingIntent
                */
            }
            catch (IntentSender.SendIntentException e)
            {
                // Log the error
                e.printStackTrace();
            }
        }
        else
        {
            /*
            * If no resolution is available, display a dialog to the
            * user with the error.
            */
            // Get the error code
            int errorCode = connectionResult.getErrorCode();
            // Get the error dialog from Google Play services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(errorCode, this,
                    CONNECTION_FAILURE_RESOLUTION_REQUEST);
            // If Google Play services can provide an error dialog
            if (errorDialog != null)
            {
                // Create a new DialogFragment for the error dialog
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                // Set the dialog in the DialogFragment
                errorFragment.setDialog(errorDialog);
                // Show the error dialog in the DialogFragment
                errorFragment.show(getSupportFragmentManager(), "Error Detection in" + getCallingActivity());
            }
        }
    }

    @Override
    public void onLocationChanged(Location location)
    {
        mCurrentLocation = new LatLng(location.getLatitude(), location.getLongitude());
    }
    //endregion

    //region Geofencing

    /*
     * Create a PendingIntent that triggers an IntentService in your
     * app when a geofence transition occurs.
     */
    private PendingIntent getTransitionPendingIntent()
    {
        // Create an explicit Intent
        Intent intent = new Intent(this, ReceiveTransitionsIntentService.class);
        /*
         * Return the PendingIntent
         */
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onAddGeofencesResult(int statusCode, String[] geofenceRequestIds)
    {
        // If adding the geofences was successful
        if (LocationStatusCodes.SUCCESS == statusCode)
        {
            /*
             * Handle successful addition of geofences here.
             * You can send out a broadcast intent or update the UI.
             * geofences into the Intent's extended data. FIXME
             */
            Toast.makeText(this, "Geofence added", Toast.LENGTH_SHORT).show();
        }
        else
        {
            // If adding the geofences failed
            /*
             * Report errors here.
             * You can log the error using Log.e() or update
             * the UI. FIXME
             */
        }

        // Turn off the in progress flag and disconnect the client
        mInProgress = false;
        mLocationClient.disconnect();
    }

    /**
     * Start a request for geofence monitoring by calling
     * LocationClient.connect().
     */
    public void addGeofences(PendingIntent requestIntent)
    {
        // Start a request to add geofences
        mRequestType = REQUEST_TYPE.ADD;
        /*
         * Test for Google Play services after setting the request type.
         * If Google Play services isn't present, the proper request
         * can be restarted.
         */
        if (!servicesConnected())
            return;

        // Store the PendingIntent
        mGeofenceRequestIntent = requestIntent;
        /*
         * Create a new location client object. Since the current
         * activity class implements ConnectionCallbacks and
         * OnConnectionFailedListener, pass the current activity object
         * as the listener for both parameters
         */
        mLocationClient = new LocationClient(this, this, this);
        // If a request is not already underway
        if (!mInProgress)
        {
            // Indicate that a request is underway
            mInProgress = true;
            // Request a connection from the client to Location Services
            mLocationClient.connect();
        }
        else
        {
            // A request is already underway. To handle this situation:
            // Disconnect the client
            mLocationClient.disconnect();
            // Reset the flag
            mInProgress = false;
            // Retry the request.
            addGeofences(requestIntent); // mRequestType = REQUEST_TYPE.ADD; ?? FIXME
        }
    }
    /**
     * Start a request to remove geofences by calling
     * LocationClient.connect()
     */
    public void removeAllGeofences(PendingIntent requestIntent)
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

        // Store the PendingIntent
        mGeofenceRequestIntent = requestIntent;
        /*
         * Create a new location client object. Since the current
         * activity class implements ConnectionCallbacks and
         * OnConnectionFailedListener, pass the current activity object
         * as the listener for both parameters
         */
        mLocationClient = new LocationClient(this, this, this);
        // If a request is not already underway
        if (!mInProgress)
        {
            // Indicate that a request is underway
            mInProgress = true;
            // Request a connection from the client to Location Services
            mLocationClient.connect();
        }
        else
        {
            // A request is already underway. To handle this situation:
            // Disconnect the client
            mLocationClient.disconnect();
            // Reset the flag
            mInProgress = false;
            // Retry the request.
            removeAllGeofences(requestIntent); // mRequestType = REQUEST_TYPE.REMOVE_INTENT; ?? FIXME
        }
    }

    /**
     * Get the geofence parameters for each geofence from the UI
     * and add them to a List.
     */
    public void createGeofences(PendingIntent pendingIntent)
    {
        /*
         * Create an internal object to store the data. Set its
         * ID to "1". This is a "flattened" object that contains
         * a set of strings
         */
        /*mUIGeofence1 = new WmfGeofence
            (
            "1",
            Double.valueOf(mLatitude1.getText().toString()),
            Double.valueOf(mLongitude1.getText().toString()),
            Float.valueOf(mRadius1.getText().toString()),
            GEOFENCE_EXPIRATION_TIME,
            // This geofence records only entry transitions
            com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER
            );
        // Store this flat version
        mGeofenceStorage.setGeofence("1", mUIGeofence1);*/
        // Create another internal object. Set its ID to "2"
        mUIGeofence2 = new WmfGeofence(
                "2",
                Double.valueOf(mLatitude2.getText().toString()),
                Double.valueOf(mLongitude2.getText().toString()),
                Float.valueOf(mRadius2.getText().toString()),
                com.google.android.gms.location.Geofence.NEVER_EXPIRE,
                // This geofence records both entry and exit transitions
                com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER |
                        com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_EXIT);
        // Store this flat version
        mGeofenceStorage.setGeofence("2", mUIGeofence2);
        mGeofenceList.add(mUIGeofence1.toGeofence());
        mGeofenceList.add(mUIGeofence2.toGeofence());
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
        mLocationClient = new LocationClient(this, this, this);
        // If a request is not already underway
        if (!mInProgress)
        {
            // Indicate that a request is underway
            mInProgress = true;
            // Request a connection from the client to Location Services
            mLocationClient.connect();
        }
        else
        {
            // A request is already underway. To handle this situation:
            // Disconnect the client
            mLocationClient.disconnect();
            // Reset the flag
            mInProgress = false;
            // Retry the request.
            removeGeofences(geofenceIds); // mRequestType = REQUEST_TYPE.REMOVE_LIST; ?? FIXME
        }
    }

    /*
     * From input arguments, create a single Location with provider set to
     * "flp"
     */
    public Location createLocation(double lat, double lng, float accuracy)
    {
        // Create a new Location
        Location newLocation = new Location(provider);
        newLocation.setLatitude(lat);
        newLocation.setLongitude(lng);
        newLocation.setAccuracy(accuracy);
        return newLocation;
    }
    //endregion

    // region Error dialog

    // Define a DialogFragment that displays the error dialog
    public static class ErrorDialogFragment extends DialogFragment
    {
        // Global field to contain the error dialog
        private Dialog mDialog;

        // Default constructor. Sets the dialog field to null
        public ErrorDialogFragment()
        {
            super();
            mDialog = null;
        }

        // Set the dialog to display
        public void setDialog(Dialog dialog)
        {
            mDialog = dialog;
        }

        // Return a Dialog to the DialogFragment.
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState)
        {
            return mDialog;
        }
    }
    //endregion

    /*
     * Handle results returned to the FragmentActivity
     * by Google Play services
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        // Decide what to do based on the original request code
        switch (requestCode)
        {
            case CONNECTION_FAILURE_RESOLUTION_REQUEST:
                /*
                * If the result code is Activity.RESULT_OK, try
                * to connect again
                */
                switch (resultCode)
                {
                    case Activity.RESULT_OK:
                        // Try the request again
                        onActivityResult(requestCode, resultCode, data);
                        break;
                }
        }
    }

    //region Activity Recognition
    /**
     * Request activity recognition updates based on the current
     * detection interval.
     *
     */
    public void startUpdates()
    {
        // Set the request type to START
        mRequestType = REQUEST_TYPE.START;

        // Check for Google Play services
        if (!servicesConnected())
            return;

        // If a request is not already underway
        if (!mInProgress)
        {
            // Indicate that a request is in progress
            mInProgress = true;
            // Request a connection to Location Services
            mActivityRecognitionClient.connect();
            //
        }
        else
        {
            // A request is already underway. To handle this situation:
            // Disconnect the client
            mLocationClient.disconnect();
            // Reset the flag
            mInProgress = false;
            // Retry the request.
            startUpdates();
        }
    }

    /**
     * Turn off activity recognition updates
     *
     */
    public void stopUpdates()
    {
        // Set the request type to STOP
        mRequestType = REQUEST_TYPE.STOP;
        /*
         * Test for Google Play services after setting the request type.
         * If Google Play services isn't present, the request can be
         * restarted.
         */
        if (!servicesConnected())
            return;

        // If a request is not already underway
        if (!mInProgress)
        {
            // Indicate that a request is in progress
            mInProgress = true;
            // Request a connection to Location Services
            mActivityRecognitionClient.connect();
            //
        }
        else
        {
            // A request is already underway. To handle this situation:
            // Disconnect the client
            mLocationClient.disconnect();
            // Reset the flag
            mInProgress = false;
            // Retry the request.
            stopUpdates();
        }
    }
    //endregion

    //region Google play services
    private boolean servicesConnected()
    {
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode)
        {
            // In debug mode, log the status
            Log.d("Location Updates", "Google Play services is available.");
            // Continue
            return true;
            // Google Play services was not available for some reason.
            // resultCode holds the error code.
        }
        else
        {
            // Get the error dialog from Google Play services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog
                    (resultCode, this, CONNECTION_FAILURE_RESOLUTION_REQUEST);

            // If Google Play services can provide an error dialog
            if (errorDialog != null)
            {
                // Create a new DialogFragment for the error dialog
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                // Set the dialog in the DialogFragment
                errorFragment.setDialog(errorDialog);
                // Show the error dialog in the DialogFragment
                errorFragment.show(getSupportFragmentManager(), "Error in " + getCallingActivity());
            }

            return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_MOVE) {
            toggleActionBar();
        }
        return true;
    }

    private void toggleActionBar() {
        ActionBar actionBar = getActionBar();

        if(actionBar != null) {
            if(actionBar.isShowing()) {
                actionBar.hide();
            }
            else {
                actionBar.show();
            }
        }
    }

    public void askForLocation(){
        //TODO
        String childEmail = "";

        for(int i = 0; i < m_My_children.size(); i++)
        {
            if(m_My_children.get(i).getIsCurrent())
                childEmail = m_My_children.get(i).getEmail();
        }

        pushNotificationController.askForLocationFromChild(childEmail);

        Toast.makeText(getApplicationContext(), "Ask for location", Toast.LENGTH_LONG).show();
    }

    public void receiveLocation(String location){
        //TODO
        //DO something
        // String to convert = Location[gps 56,172339,10,191438 acc=2 et=+5d4h30m56s48ms alt=82.02612592977187
        // vel=0.1622365 bear=280.9743 {Bundle[mParcelledData.dataSize=44]}]
        // TextUtils.SimpleStringSplitter stringSplitter = new TextUtils.SimpleStringSplitter(" ");

        StringBuilder stringBuilder = new StringBuilder(location);
        String latitudeString = stringBuilder.substring(12, 22);
        String longitudeString = stringBuilder.substring(23, 32);
        String lat = latitudeString.replace(latitudeString, stringBuilder.substring(13, 15) + "." + stringBuilder.substring(16, 22));
        String lng = longitudeString.replace(longitudeString, stringBuilder.substring(23, 25) + "." + stringBuilder.substring(26, 32));

        double latitude = 0;
        double longitude = 0;

        try
        {
            latitude = Double.valueOf(lat);
            longitude = Double.valueOf(lng);
        }
        catch(NumberFormatException e)
        {
            Toast.makeText(this, "Kan ikke modtage position, prøv venligst igen", Toast.LENGTH_SHORT).show();
        }

        currentPosition = new LatLng(latitude, longitude);

        Toast.makeText(this, currentPosition.toString(), Toast.LENGTH_LONG).show();

        /*List<Address> adress = new List<Address>() {
            @Override
            public void add(int i, Address address) {

            }

            @Override
            public boolean add(Address address) {
                return false;
            }

            @Override
            public boolean addAll(int i, Collection<? extends Address> addresses) {
                return false;
            }

            @Override
            public boolean addAll(Collection<? extends Address> addresses) {
                return false;
            }

            @Override
            public void clear() {

            }

            @Override
            public boolean contains(Object o) {
                return false;
            }

            @Override
            public boolean containsAll(Collection<?> objects) {
                return false;
            }

            @Override
            public Address get(int i) {
                return null;
            }

            @Override
            public int indexOf(Object o) {
                return 0;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @NonNull
            @Override
            public Iterator<Address> iterator() {
                return null;
            }

            @Override
            public int lastIndexOf(Object o) {
                return 0;
            }

            @NonNull
            @Override
            public ListIterator<Address> listIterator() {
                return null;
            }

            @NonNull
            @Override
            public ListIterator<Address> listIterator(int i) {
                return null;
            }

            @Override
            public Address remove(int i) {
                return null;
            }

            @Override
            public boolean remove(Object o) {
                return false;
            }

            @Override
            public boolean removeAll(Collection<?> objects) {
                return false;
            }

            @Override
            public boolean retainAll(Collection<?> objects) {
                return false;
            }

            @Override
            public Address set(int i, Address address) {
                return null;
            }

            @Override
            public int size() {
                return 0;
            }

            @NonNull
            @Override
            public List<Address> subList(int i, int i2) {
                return null;
            }

            @NonNull
            @Override
            public Object[] toArray() {
                return new Object[0];
            }

            @NonNull
            @Override
            public <T> T[] toArray(T[] ts) {
                return null;
            }
        };*/
    }
    //endregion
}