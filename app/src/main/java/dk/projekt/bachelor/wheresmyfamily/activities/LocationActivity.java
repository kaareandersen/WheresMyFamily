package dk.projekt.bachelor.wheresmyfamily.activities;

import android.app.ActionBar;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.fitness.request.DataReadRequest;
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
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import dk.projekt.bachelor.wheresmyfamily.Controller.ChildModelController;
import dk.projekt.bachelor.wheresmyfamily.Controller.GMapV2Direction;
import dk.projekt.bachelor.wheresmyfamily.Controller.GetDirectionsAsyncTask;
import dk.projekt.bachelor.wheresmyfamily.Controller.PushNotificationController;
import dk.projekt.bachelor.wheresmyfamily.DataModel.Child;
import dk.projekt.bachelor.wheresmyfamily.R;
import dk.projekt.bachelor.wheresmyfamily.Storage.GeofenceStorage;

public class LocationActivity extends FragmentActivity implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener, LocationClient.OnAddGeofencesResultListener,
        LocationListener
{

    //region Fields
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private LocationClient locationClient;
    private LatLng mCurrentLocation;
    LatLng currentPosition;

    // Define an object that holds accuracy and frequency parameters
    private LocationRequest mLocationRequest;

    // Setup Location update interval
    private static final int MILLISECONDS_PER_SECOND = 1000;
    private static final int UPDATE_INTERVAL_IN_SECONDS = 30;
    private static final long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    private static final int FASTEST_INTERVAL_IN_SECONDS = 30;
    private static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;
    private static final float SMALLEST_DISPLACEMENT_IN_METERS = 20;

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

    // Store the list of geofence Ids to remove
    List<String> mGeofencesToRemove;

    ArrayList<com.google.android.gms.location.Geofence> mCurrentGeofences;
    // Internal List of WmfGeofence objects
    List<com.google.android.gms.location.Geofence> mGeofenceList;
    private GeofenceStorage geofenceStorage;

    public static LocationActivity instance = null;

    Address address;

    DataReadRequest dataReadRequest;

    public static final String myPrefs = "PrefsFile";
    SharedPreferences.Editor prefsEditor;
    SharedPreferences prefs;
    Child myChild;
    private ArrayList<Child> m_My_children = new ArrayList<Child>();
    ChildModelController childModelController = new ChildModelController();

    // ActionBar actionBar;
    protected PushNotificationController pushNotificationController;

    // Defines the allowable request types.
    public enum REQUEST_TYPE {ADD, REMOVE_INTENT, REMOVE_LIST }
    private REQUEST_TYPE mRequestType;
    // Flag that indicates if a request is underway.
    private boolean mInProgress;
    ImageButton addgeofenceButton;
    //endregion

    //Navigering
    private static final LatLng AMSTERDAM = new LatLng(52.37518, 4.895439);
    private static final LatLng PARIS = new LatLng(48.856132, 2.352448);
    private static final LatLng FRANKFURT = new LatLng(50.111772, 8.682632);
    private SupportMapFragment fragment;
    private LatLngBounds latlngBounds;
    private Button bNavigation;
    private Polyline newPolyline;
    private boolean isTravelingToParis = false;
    private int width, height;

    public LocationActivity(){}

    //region Lifecycle events
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_location);

        instance = this;

        pushNotificationController = new PushNotificationController(this);

        ActionBar actionBar = getActionBar();
        actionBar.setCustomView(R.layout.actionbar_top_new_geofence); //load your layout
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME|ActionBar.DISPLAY_SHOW_CUSTOM|ActionBar.DISPLAY_SHOW_TITLE); //show it
        actionBar.setDisplayHomeAsUpEnabled(true);

        addgeofenceButton = (ImageButton) findViewById(R.id.action_new_geofence);
        addgeofenceButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), FavoritePlaces.class);
                startActivity(intent);
            }
        });

        // Initialize the location
        if (locationClient == null)
            locationClient = new LocationClient(this, this, this);

        locationClient.connect();

        getSreenDimanstions();

        // Initialize the map
        map = ((SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map)).getMap();

        if (map == null)
        {
            Toast.makeText(this, "Google Maps not available", Toast.LENGTH_LONG).show();
        }

        // Since the user is at the map request a new LocationClient
        // with faster location updates
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setSmallestDisplacement(SMALLEST_DISPLACEMENT_IN_METERS);

        // Start with the request flag set to false
        mInProgress = false;

        // Instantiate a new geofence storage area
        geofenceStorage = new GeofenceStorage(this);

        // Instantiate the current List of geofences
        mCurrentGeofences = new ArrayList<com.google.android.gms.location.Geofence>();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        locationClient.connect();

        m_My_children = childModelController.getMyChildren(this);

        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        //new AskForLocation().execute(null, null, null);
        askForLocation();
    }

    @Override
    protected void onPause() {

        // If the client is connected
        if (locationClient.isConnected())
        {
            /* Remove location updates for a listener.*/
            locationClient.removeLocationUpdates(this);
        }

        locationClient.disconnect();

        super.onPause();
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        locationClient.connect();

        // new AskForLocation().execute(null, null, null);
    }

    @Override
    protected void onStop()
    {

        // If the client is connected
        if (locationClient.isConnected())
        {
            /* Remove location updates for a listener.*/
            locationClient.removeLocationUpdates(this);
        }

        locationClient.disconnect();

        super.onStop();
    }

    @Override
        public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.location, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.navigate_to_child:
                findDirections( AMSTERDAM.latitude, AMSTERDAM.longitude,PARIS.latitude, PARIS.longitude, GMapV2Direction.MODE_DRIVING );
                break;
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
                List<Address> _addresses;
                String _language = "da";
                String _country = "DK";
                Locale _local = new Locale(_language, _country);
                Geocoder _geocoder = new Geocoder(this, _local);
                StringBuffer _markerInfo = new StringBuffer();

                if(mCurrentLocation != null)
                {
                    try
                    {
                        // Last parameter is the max number of result wanted
                        _addresses = _geocoder.getFromLocation(mCurrentLocation.latitude, mCurrentLocation.longitude, 1);
                        if(_addresses.size() > 0)
                        {
                            for(int j = 0; j < _addresses.get(0).getMaxAddressLineIndex(); j++)
                            {
                                _markerInfo.append(_addresses.get(0).getAddressLine(j).toString());
                                _markerInfo.append("\n");
                            }
                        }
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }

                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(mCurrentLocation) // Sets the center of the map to current location
                            .zoom(17)    // Sets the zoom
                            .bearing(90) // Sets the orientation of the camera to east
                            .tilt(30)    // Sets the tilt of the camera to 30 degrees
                            .build();    // Creates a CameraPosition from the builder

                    map.animateCamera(CameraUpdateFactory.newCameraPosition(
                            cameraPosition));
                    map.addMarker(new MarkerOptions()
                            .position(mCurrentLocation)
                            .title("Mig\n").snippet(_markerInfo.toString())
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                }
                else
                {
                    Toast.makeText(this, "Lokation endnu ikke modtaget, vent venligst", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.current_child_position:
                List<Address> addresses;
                String language = "da";
                String country = "DK";
                Locale local = new Locale(language, country);
                Geocoder geocoder = new Geocoder(this, local);
                StringBuffer markerInfo = new StringBuffer();
                Child current = childModelController.getCurrentChild();

                if (currentPosition != null)
                {
                    try
                    {
                        addresses = geocoder.getFromLocation(currentPosition.latitude, currentPosition.longitude, 1);
                        if(addresses.size() > 0)
                        {
                            for(int j = 0; j < addresses.get(0).getMaxAddressLineIndex(); j++)
                            {
                                markerInfo.append(addresses.get(0).getAddressLine(j).toString());
                                markerInfo.append("\n");
                            }
                        }
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }

                    // Set the maptype
                    map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                    // Add a marker with the selected Child's name and current location
                    map.addMarker(new MarkerOptions().position(currentPosition)
                            .title(current.getName() + "\n").snippet(markerInfo.toString()));

                    // Set the camera position to zoom in on the current location
                    CameraPosition currentChildPosition = new CameraPosition.Builder()
                            .target(currentPosition).zoom(17).bearing(90).tilt(0).build();
                    map.animateCamera(
                            CameraUpdateFactory.newCameraPosition(currentChildPosition));
                }
                else
                {
                    Toast.makeText(this, "Barnets lokation endnu ikke modtaget, vent venligst", Toast.LENGTH_LONG).show();
                }
                break;
            /*case R.id.navigate_child_position:
                Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse
                        ("http://maps.google.com/maps?saddr=20.344,34.34&daddr=20.5666,45.345"));
                startActivity(intent);
                break;*/
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


    //endregion

    //region Location callback methods
    @Override
    public void onConnected(Bundle bundle)
    {
        Toast.makeText(this, "LocationActivity connected", Toast.LENGTH_SHORT).show();

        locationClient.requestLocationUpdates(mLocationRequest, this);
    }


    @Override
    public void onDisconnected()
    {
        Toast.makeText(this, "LocationActivity disconnected. Please re-connect.",
                Toast.LENGTH_SHORT).show();
        // Turn off the request flag
        mInProgress = false;
        // Destroy the current location client
        locationClient = null;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult)
    {
        // Turn off the request flag
        mInProgress = false;

        if (connectionResult.hasResolution())
        {
            try
            {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);

            }
            catch (IntentSender.SendIntentException e)
            {
                // Log the error
                e.printStackTrace();
            }
        }
        else
        {

        }
    }

    @Override
    public void onLocationChanged(Location location)
    {
        mCurrentLocation = new LatLng(location.getLatitude(), location.getLongitude());
    }
    //endregion



    //region Geofencing

    @Override
    public void onAddGeofencesResult(int statusCode, String[] geofenceRequestIds)
    {
        // If adding the geofences was successful
        if (LocationStatusCodes.SUCCESS == statusCode)
        {
            Toast.makeText(this, "Geofence added", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(this, "Geofence error", Toast.LENGTH_SHORT).show();
        }

        // Turn off the in progress flag and disconnect the client
        mInProgress = false;
        locationClient.disconnect();
    }


    //endregion

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
        String childEmail = "";

        for(int i = 0; i < m_My_children.size(); i++)
        {
            if(m_My_children.get(i).getIsCurrent())
                childEmail = m_My_children.get(i).getEmail();
        }

        pushNotificationController.askForLocationFromChild(childEmail);
    }

    public void receiveLocation(String location){
        //TODO
        //String to convert
        // Bundle[{msg=ReceiveLoc ation:Loca tion[fused 56,147154,10,150219 acc=24 et=+7d15h33m53s517ms
        // alt=80.02072416373049 vel=1.6083485 bear=25.0], from=911215571794, collapse_key=do_not_collapse}]

        StringBuilder stringBuilder = new StringBuilder(location);
        String latitudeString = stringBuilder.substring(15, 24);
        String longitudeString = stringBuilder.substring(25, 35);
        String lat = latitudeString.replace(latitudeString, stringBuilder.substring(15, 17) + "." + stringBuilder.substring(18, 24));
        String lng = longitudeString.replace(longitudeString, stringBuilder.substring(25, 27) + "." + stringBuilder.substring(28, 34));

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
    }
    //endregion

    public void showCurrentPositionOnMap()
    {
        // Show the position of the currently selected child
        List<Address> addresses;
        String language = "da";
        String country = "DK";
        Locale local = new Locale(language, country);
        Geocoder geocoder = new Geocoder(this, local);
        StringBuffer markerInfo = new StringBuffer();
        Child current = childModelController.getCurrentChild();

        if (currentPosition != null)
        {
            try
            {
                // Extract the address from the location coordinates using a geocoder
                addresses = geocoder.getFromLocation(currentPosition.latitude, currentPosition.longitude, 1);
                if(addresses.size() > 0)
                {
                    for(int j = 0; j < addresses.get(0).getMaxAddressLineIndex(); j++)
                    {
                        markerInfo.append(addresses.get(0).getAddressLine(j).toString());
                        markerInfo.append("\n");
                    }
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            // Set the maptype
            map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            // Add a marker with the selected Child's name and current location
            map.addMarker(new MarkerOptions().position(currentPosition)
                    .title(current.getName() + "\n").snippet(markerInfo.toString()));

            // Set the camera position to zoom in on the current location
            CameraPosition currentChildPosition = new CameraPosition.Builder()
                    .target(currentPosition).zoom(17).bearing(90).tilt(0).build();
            map.animateCamera(
                    CameraUpdateFactory.newCameraPosition(currentChildPosition));
        }
        else
        {
            Toast.makeText(this, "Barnets lokation endnu ikke modtaget, vent venligst", Toast.LENGTH_LONG).show();
        }
    }

    public void handleGetDirectionsResult(ArrayList<LatLng> directionPoints) {
        PolylineOptions rectLine = new PolylineOptions().width(10).color(Color.BLUE);

        for(int i = 0 ; i < directionPoints.size() ; i++)
        {
            rectLine.add(directionPoints.get(i));
        }
        if (newPolyline != null)
        {
            newPolyline.remove();
        }
        newPolyline = map.addPolyline(rectLine);
        if (isTravelingToParis)
        {
            latlngBounds = createLatLngBoundsObject(AMSTERDAM, PARIS);
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(latlngBounds, width, height, 150));
        }
        else
        {
            latlngBounds = createLatLngBoundsObject(AMSTERDAM, FRANKFURT);
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(latlngBounds, width, height, 150));
        }

    }

    private void getSreenDimanstions()
    {
        Display display = getWindowManager().getDefaultDisplay();
        width = display.getWidth();
        height = display.getHeight();
    }

    private LatLngBounds createLatLngBoundsObject(LatLng firstLocation, LatLng secondLocation)
    {
        if (firstLocation != null && secondLocation != null)
        {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(firstLocation).include(secondLocation);

            return builder.build();
        }
        return null;
    }

    public void findDirections(double fromPositionDoubleLat, double fromPositionDoubleLong, double toPositionDoubleLat, double toPositionDoubleLong, String mode)
    {
        Map<String, String> map = new HashMap<String, String>();
        map.put(GetDirectionsAsyncTask.USER_CURRENT_LAT, String.valueOf(fromPositionDoubleLat));
        map.put(GetDirectionsAsyncTask.USER_CURRENT_LONG, String.valueOf(fromPositionDoubleLong));
        map.put(GetDirectionsAsyncTask.DESTINATION_LAT, String.valueOf(toPositionDoubleLat));
        map.put(GetDirectionsAsyncTask.DESTINATION_LONG, String.valueOf(toPositionDoubleLong));
        map.put(GetDirectionsAsyncTask.DIRECTIONS_MODE, mode);

        GetDirectionsAsyncTask asyncTask = new GetDirectionsAsyncTask(this);
        asyncTask.execute(map);
    }
}




