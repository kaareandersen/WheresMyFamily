package dk.projekt.bachelor.wheresmyfamily.Services;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

public class LocationService extends IntentService implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        LocationListener {
    private static final String TAG = "LocationService";
    private boolean currentlyProcessingLocation = false;
    private LocationRequest locationRequest;
    private LocationClient locationClient;
    private int locationRequestInterval = 60000;
    private int fastestLocationRequestInterval = 60000;
    private int mStartId;

    public LocationService() {
        super(TAG);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!currentlyProcessingLocation) {
            currentlyProcessingLocation = true;
            startTracking();
            // currentlyProcessingLocation = true;
        }

        // mStartId = startId;

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected");

        Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();

        // Specify update parameters
        locationRequest = LocationRequest.create();
        // Set the frequency for receiving updates in milliseconds
        locationRequest.setInterval(locationRequestInterval);
        // Set the fastest rate in milliseconds at which this app can handle location updates
        locationRequest.setFastestInterval(fastestLocationRequestInterval);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationClient.requestLocationUpdates(locationRequest, this);
    }

    @Override
    public void onDisconnected() {
        Log.e(TAG, "onDisconnected");

        Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show();
        locationClient = null;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {


        }

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed");

    }

    private void startTracking() {
        Log.d(TAG, "startTracking");

        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
            locationClient = new LocationClient(this,this,this);

            if (!locationClient.isConnected() || !locationClient.isConnecting()) {
                locationClient.connect();

                Toast.makeText(this, "Tracking", Toast.LENGTH_LONG).show();
            }
        } else {
            Log.e(TAG, "unable to connect to google play services.");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // stopSelf(mStartId);
        Toast.makeText(this, "LocationService stopped", Toast.LENGTH_LONG).show();
    }
}
