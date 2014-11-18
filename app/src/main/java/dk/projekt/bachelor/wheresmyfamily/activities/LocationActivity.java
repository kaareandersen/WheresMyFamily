package dk.projekt.bachelor.wheresmyfamily.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Address;
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
import com.google.android.gms.location.Geofence;
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

import java.util.ArrayList;
import java.util.List;

import dk.projekt.bachelor.wheresmyfamily.ActivityRecognitionIntentService;
import dk.projekt.bachelor.wheresmyfamily.CalendarActivity;
import dk.projekt.bachelor.wheresmyfamily.R;
import dk.projekt.bachelor.wheresmyfamily.ReceiveTransitionsIntentService;

public class LocationActivity extends FragmentActivity implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener, LocationClient.OnAddGeofencesResultListener,
        LocationListener {

    ActionBar actionBar;

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
    private SimpleGeofence mUIGeofence1;
    private SimpleGeofence mUIGeofence2;
    // List of currentGeofences
    ArrayList<Geofence> mCurrentGeofences;
    // Internal List of Geofence objects
    List<Geofence> mGeofenceList;
    // Persistent storage for geofences
    private SimpleGeofenceStore mGeofenceStorage;

    // Hardcoded location for testing
    private static final LatLng GOLDEN_GATE_BRIDGE = new LatLng(37.828891,-122.485884);

    // Store the PendingIntent used to send activity recognition events back to the app
    private PendingIntent mActivityRecognitionPendingIntent;
    // Store the current activity recognition client
    private ActivityRecognitionClient mActivityRecognitionClient;

    Address address;

    String provider;
    LocationManager locationManager;

    DataReadRequest dataReadRequest;
    //endregion

    //region Lifecycle events
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

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
        mGeofenceStorage = new SimpleGeofenceStore(this);

        // Instantiate the current List of geofences
        mCurrentGeofences = new ArrayList<Geofence>();

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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
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
            case R.id.menu_showtraffic:
                map.setTrafficEnabled(true);
                break;
            case R.id.menu_zoomin:
                map.animateCamera(CameraUpdateFactory.zoomIn());
                break;
            case R.id.menu_zoomout:
                map.animateCamera(CameraUpdateFactory.zoomOut());
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
                        .title("Home").snippet("Population 3")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                break;
            case R.id.menu_showcurrentlocation:
                // Get the current location and mark it on the map
                map.setMyLocationEnabled(true);

                // Set the camera position to zoom in on the current location
                CameraPosition myPosition = new CameraPosition.Builder()
                        .target(mCurrentLocation).zoom(17).bearing(90).tilt(30).build();
                map.animateCamera(
                        CameraUpdateFactory.newCameraPosition(myPosition));
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
       /* mActivityRecognitionClient.requestActivityUpdates(
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

    /**
     * A single Geofence object, defined by its center and radius.
     */
    public class SimpleGeofence
    {
        // Instance variables
        private final String mId;
        private final double mLatitude;
        private final double mLongitude;
        private final float mRadius;
        private long mExpirationDuration;
        private int mTransitionType;

        /**
         * @param geofenceId The Geofence's request ID
         * @param latitude Latitude of the Geofence's center.
         * @param longitude Longitude of the Geofence's center.
         * @param radius Radius of the geofence circle.
         * @param expiration Geofence expiration duration
         * @param transition Type of Geofence transition.
         */
        public SimpleGeofence(String geofenceId, double latitude, double longitude, float radius,
                              long expiration, int transition)
        {
            // Set the instance fields from the constructor
            this.mId = geofenceId;
            this.mLatitude = latitude;
            this.mLongitude = longitude;
            this.mRadius = radius;
            this.mExpirationDuration = expiration;
            this.mTransitionType = transition;
        }

        // region Instance field getters
        public String getId(){
            return mId;
        }
        public double getLatitude()
        {
            return mLatitude;
        }
        public double getLongitude() {
            return mLongitude;
        }
        public float getRadius() {
            return mRadius;
        }
        public long getExpirationDuration() {
            return mExpirationDuration;
        }
        public int getTransitionType() {
            return mTransitionType;
        }
        //endregion
        /**
         * Creates a Location Services Geofence object from a
         * SimpleGeofence.
         *
         * @return A Geofence object
         */
        public Geofence toGeofence()
        {
            // Build a new Geofence object
            return new Geofence.Builder().setRequestId(getId()).setTransitionTypes(mTransitionType)
                    .setCircularRegion(getLatitude(), getLongitude(), getRadius())
                    .setExpirationDuration(mExpirationDuration).build();
        }
    }

    /**
     * Storage for geofence values, implemented in SharedPreferences.
     */
    public class SimpleGeofenceStore
    {
        //region Fields
        // Keys for flattened geofences stored in SharedPreferences
        public static final String KEY_LATITUDE = "dk.projekt.bachelor.wheresmyfamily.KEY_LATITUDE";
        public static final String KEY_LONGITUDE = "dk.projekt.bachelor.wheresmyfamily.KEY_LONGITUDE";
        public static final String KEY_RADIUS = "dk.projekt.bachelor.wheresmyfamily.KEY_RADIUS";
        public static final String KEY_EXPIRATION_DURATION = "dk.projekt.bachelor.wheresmyfamily.KEY_EXPIRATION_DURATION";
        public static final String KEY_TRANSITION_TYPE = "dk.projekt.bachelor.wheresmyfamily.KEY_TRANSITION_TYPE";
        // The prefix for flattened geofence keys
        public static final String KEY_PREFIX = "dk.projekt.bachelor.wheresmyfamily.KEY";
        /*
        * Invalid values, used to test geofence storage when
        * retrieving geofences
        */
        public static final long INVALID_LONG_VALUE = -999l;
        public static final float INVALID_FLOAT_VALUE = -999.0f;
        public static final int INVALID_INT_VALUE = -999;
        // The SharedPreferences object in which geofences are stored
        private final SharedPreferences mPrefs;
        // The name of the SharedPreferences
        private static final String SHARED_PREFERENCES = "SharedPreferences";
        //endregion

        // Create the SharedPreferences storage with private access only
        public SimpleGeofenceStore(Context context)
        {
            mPrefs = context.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
        }
        /**
         * Returns a stored geofence by its id, or returns null
         * if it's not found.
         *
         * @param id The ID of a stored geofence
         * @return A geofence defined by its center and radius. See
         */
        public SimpleGeofence getGeofence(String id)
        {
            /*
            * Get the latitude for the geofence identified by id, or
            * INVALID_FLOAT_VALUE if it doesn't exist
            */
            double lat = mPrefs.getFloat(getGeofenceFieldKey(id, KEY_LATITUDE), INVALID_FLOAT_VALUE);
            /*
             * Get the longitude for the geofence identified by id, or
             * INVALID_FLOAT_VALUE if it doesn't exist
             */
            double lng = mPrefs.getFloat(getGeofenceFieldKey(id, KEY_LONGITUDE), INVALID_FLOAT_VALUE);
            /*
             * Get the radius for the geofence identified by id, or
             * INVALID_FLOAT_VALUE if it doesn't exist
             */
            float radius = mPrefs.getFloat(getGeofenceFieldKey(id, KEY_RADIUS), INVALID_FLOAT_VALUE);
            /*
             * Get the expiration duration for the geofence identified
             * by id, or INVALID_LONG_VALUE if it doesn't exist
             */
            long expirationDuration =
                    mPrefs.getLong(getGeofenceFieldKey(id, KEY_EXPIRATION_DURATION), INVALID_LONG_VALUE);
            /*
             * Get the transition type for the geofence identified by
             * id, or INVALID_INT_VALUE if it doesn't exist
             */
            int transitionType = mPrefs.getInt(getGeofenceFieldKey(id, KEY_TRANSITION_TYPE),
                    INVALID_INT_VALUE);
            // If none of the values is incorrect, return the object
            if (
                    lat != SimpleGeofenceStore.INVALID_FLOAT_VALUE &&
                            lng != SimpleGeofenceStore.INVALID_FLOAT_VALUE &&
                            radius != SimpleGeofenceStore.INVALID_FLOAT_VALUE &&
                            expirationDuration != SimpleGeofenceStore.INVALID_LONG_VALUE &&
                            transitionType != SimpleGeofenceStore.INVALID_INT_VALUE
                    )
            {
                // Return a true Geofence object
                return new SimpleGeofence(id, lat, lng, radius, expirationDuration, transitionType);
                // Otherwise, return null.
            }
            else
                return null;
        }
        /**
         * Save a geofence.
         * @param geofence The SimpleGeofence containing the
         * values you want to save in SharedPreferences
         */
        public void setGeofence(String id, SimpleGeofence geofence)
        {
            /*
             * Get a SharedPreferences editor instance. Among other
             * things, SharedPreferences ensures that updates are atomic
             * and non-concurrent
             */
            SharedPreferences.Editor editor = mPrefs.edit();
            // Write the Geofence values to SharedPreferences
            editor.putFloat(getGeofenceFieldKey(id, KEY_LATITUDE), (float) geofence.getLatitude());
            editor.putFloat(getGeofenceFieldKey(id, KEY_LONGITUDE), (float) geofence.getLongitude());
            editor.putFloat(getGeofenceFieldKey(id, KEY_RADIUS), geofence.getRadius());
            editor.putLong(getGeofenceFieldKey(id, KEY_EXPIRATION_DURATION),
                    geofence.getExpirationDuration());
            editor.putInt(getGeofenceFieldKey(id, KEY_TRANSITION_TYPE), geofence.getTransitionType());
            // Commit the changes
            editor.commit();
        }

        public void clearGeofence(String id) {
            /*
             * Remove a flattened geofence object from storage by
             * removing all of its keys
             */
            SharedPreferences.Editor editor = mPrefs.edit();
            editor.remove(getGeofenceFieldKey(id, KEY_LATITUDE));
            editor.remove(getGeofenceFieldKey(id, KEY_LONGITUDE));
            editor.remove(getGeofenceFieldKey(id, KEY_RADIUS));
            editor.remove(getGeofenceFieldKey(id, KEY_EXPIRATION_DURATION));
            editor.remove(getGeofenceFieldKey(id, KEY_TRANSITION_TYPE));
            editor.commit();
        }
        /**
         * Given a Geofence object's ID and the name of a field
         * (for example, KEY_LATITUDE), return the key name of the
         * object's values in SharedPreferences.
         *
         * @param id The ID of a Geofence object
         * @param fieldName The field represented by the key
         * @return The full key name of a value in SharedPreferences
         */
        private String getGeofenceFieldKey(String id, String fieldName)
        {
            return KEY_PREFIX + "_" + id + "_" + fieldName;
        }
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
        mUIGeofence1 = new SimpleGeofence
            (
            "1",
            Double.valueOf(mLatitude1.getText().toString()),
            Double.valueOf(mLongitude1.getText().toString()),
            Float.valueOf(mRadius1.getText().toString()),
            GEOFENCE_EXPIRATION_TIME,
            // This geofence records only entry transitions
            Geofence.GEOFENCE_TRANSITION_ENTER
            );
        // Store this flat version
        mGeofenceStorage.setGeofence("1", mUIGeofence1);
        // Create another internal object. Set its ID to "2"
        mUIGeofence2 = new SimpleGeofence(
                "2",
                Double.valueOf(mLatitude2.getText().toString()),
                Double.valueOf(mLongitude2.getText().toString()),
                Float.valueOf(mRadius2.getText().toString()),
                Geofence.NEVER_EXPIRE,
                // This geofence records both entry and exit transitions
                Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_EXIT);
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

    public void receiveLocation(String location){
        Toast.makeText(getApplicationContext(), "Location modtaget" + location, Toast.LENGTH_LONG).show();
    }
    //endregion
}