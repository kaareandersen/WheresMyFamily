package dk.projekt.bachelor.wheresmyfamily.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.fitness.HistoryApi;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import dk.projekt.bachelor.wheresmyfamily.CalendarActivity;
import dk.projekt.bachelor.wheresmyfamily.OverviewActivity;
import dk.projekt.bachelor.wheresmyfamily.R;

public class LocationActivity extends FragmentActivity implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        LocationListener {

    ActionBar actionBar;

    //region Fields
    private final static int
            CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    LocationClient mLocationClient;
    LocationListener locationListener;
    LatLng mCurrentLocation;
    HistoryApi historyApi;
    GoogleApiClient googleApiClient;

    // Define an object that holds accuracy and frequency parameters
    LocationRequest mLocationRequest;

    // Setup Location update interval
    private static final int MILLISECONDS_PER_SECOND = 1000;
    private static final int UPDATE_INTERVAL_IN_SECONDS = 10;
    private static final long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    private static final int FASTEST_INTERVAL_IN_SECONDS = 10;
    private static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;

    // Map
    GoogleMap map;

    // Hardcoded location
    private static final LatLng GOLDEN_GATE_BRIDGE =
            new LatLng(37.828891,-122.485884);

    String provider;
    LocationManager locationManager;

    DataReadRequest dataReadRequest;
    //endregion


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

        //mLocationClient.requestLocationUpdates(mLocationRequest, this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.location, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {

            case R.id.menu_sethybrid:
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
                map.addMarker(new MarkerOptions().position(mCurrentLocation));
                break;
            case R.id.menu_getcurrentlocation:
                // ---get your current location and display a blue dot---
                map.setMyLocationEnabled(true);
                break;
            case R.id.menu_showcurrentlocation:
                Location myLocation = map.getMyLocation();
                mCurrentLocation = new LatLng(myLocation.getLatitude(),
                        myLocation.getLongitude());

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
                Intent caldendar = new Intent(this, CalendarActivity.class);
                startActivity(caldendar);
                break;
            case R.id.action_map:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        provider = LocationManager.GPS_PROVIDER;
    }

    @Override
    protected void onStart() {
        super.onStart();

        mLocationClient.connect();
    }

    @Override
    protected void onStop() {
        // If the client is connected
        if (mLocationClient.isConnected()) {
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

    @Override
    public void onConnected(Bundle bundle) {
        Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();

        mLocationClient.requestLocationUpdates(mLocationRequest, this);
    }

    @Override
    public void onDisconnected() {
        Toast.makeText(this, "Disconnected. Please re-connect.",
                Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            Toast.makeText(this, connectionResult.getErrorCode(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        // location = locationManager.getLastKnownLocation(provider);

        mCurrentLocation = new LatLng(location.getLatitude(), location.getLongitude());
    }

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

    /*
     * Handle results returned to the FragmentActivity
     * by Google Play services
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        // Decide what to do based on the original request code
        switch (requestCode) {

            case CONNECTION_FAILURE_RESOLUTION_REQUEST:
            /*
             * If the result code is Activity.RESULT_OK, try
             * to connect again
             */
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        /*
                        * Try the request again
                        */
                        onActivityResult(requestCode, resultCode, data);
                        break;
                }
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
            Log.d("Location Updates",
                    "Google Play services is available.");
            // Continue
            return true;
            // Google Play services was not available for some reason.
            // resultCode holds the error code.
        } else {
            // Get the error dialog from Google Play services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                    resultCode,
                    this,
                    CONNECTION_FAILURE_RESOLUTION_REQUEST);

            // If Google Play services can provide an error dialog
            if (errorDialog != null) {
                // Create a new DialogFragment for the error dialog
                ErrorDialogFragment errorFragment =
                        new ErrorDialogFragment();
                // Set the dialog in the DialogFragment
                errorFragment.setDialog(errorDialog);
                // Show the error dialog in the DialogFragment
                errorFragment.show(getSupportFragmentManager(),
                        "Location Updates");
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
}
