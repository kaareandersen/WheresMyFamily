package dk.projekt.bachelor.wheresmyfamily.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
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

import dk.projekt.bachelor.wheresmyfamily.Controller.MobileServicesClient;
import dk.projekt.bachelor.wheresmyfamily.Controller.NotificationHubController;
import dk.projekt.bachelor.wheresmyfamily.Controller.PushNotificationController;
import dk.projekt.bachelor.wheresmyfamily.DataModel.Parent;
import dk.projekt.bachelor.wheresmyfamily.R;
import dk.projekt.bachelor.wheresmyfamily.UserInfoStorage;
import dk.projekt.bachelor.wheresmyfamily.authenticator.AuthenticationApplication;
import dk.projekt.bachelor.wheresmyfamily.helper.BaseActivity;

public class LoggedInChild extends BaseActivity implements GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener, LocationClient.OnAddGeofencesResultListener,
        LocationListener {

    //region Fields
    private final String TAG = "LoggedInChild";
    protected NotificationHubController mNotificationHubController;
    protected PushNotificationController pushNotificationController;
    private TextView mLblUsernameValue,parentInfoName, parentInfoPhone;
    Parent parent = new Parent();
    private String cEmail;
    Location currentLocation;
    LocationClient locationClient;
    // Define an object that holds accuracy and frequency parameters
    private LocationRequest mLocationRequest;

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

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

    private String SENDER_ID = "911215571794";
    private GoogleCloudMessaging mGcm;
    private NotificationHub mHub;
    private String mRegistrationId;
    public static LoggedInChild instance = null;

    JSONArray mChildren = new JSONArray();
    ArrayList<Parent> mParents = new ArrayList<Parent>();
    String childrenPrefName = "myChildren";
    String parentsPrefName = "myParents";
    String childrenKey = "childrenInfo";
    String parentsKey = "parentsInfo";
    UserInfoStorage storage = new UserInfoStorage();
    LocationActivity locationActivity;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_in_child);

        instance = this;

        //Notifacation hub Azure
        mNotificationHubController = new NotificationHubController(this);
        pushNotificationController = new PushNotificationController(this);

        // Reference UI elements
        mLblUsernameValue = (TextView) findViewById(R.id.lblUsernameValue);
        parentInfoName = (TextView) findViewById(R.id.parentinput);
        parentInfoPhone = (TextView) findViewById(R.id.phoneinput);

        // Get parent info
        mParents = storage.loadParents(this);

        if(mParents.size() > 0)
        {
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
        // Use high accuracy
        mLocationRequest.setPriority(
                LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Set the update interval to 10 seconds
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        // Set the fastest update interval to 10 seconds
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        locationClient.requestLocationUpdates(mLocationRequest, this);

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

        // Toast.makeText(this, "LoggedInChild OnResume", Toast.LENGTH_SHORT).show();

        // Get and show parent info
        mParents = storage.loadParents(this);

        if(mParents.size() > 0)
        {
            parentInfoName.setText(mParents.get(0).getName());
            parentInfoPhone.setText(mParents.get(0).getPhone());
        }

        locationClient.connect();
    }

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



    public void deleteDialogBox(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Bekræft Sletning af Brugerprofil");
        builder.setMessage("Er du sikker?");

        builder.setPositiveButton("JA", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                // Do nothing but close the dialog
                mMobileServicesClient.deleteUser();
                mNotificationHubController.unRegisterNH();
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

    public void getAndPushLocation(){
        //TODO
        String parentEmail = mParents.get(0).getEmail();
        String location = currentLocation.toString();

        pushNotificationController.sendLocationFromChild(parentEmail, location);
    }

    @Override
    public void onLocationChanged(Location location) {

        currentLocation = location;
    }

    @Override
    public void onConnected(Bundle bundle) {

        Toast.makeText(this, "LocationActivity connected", Toast.LENGTH_SHORT).show();

        locationClient.requestLocationUpdates(mLocationRequest, this);

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


        }

    }

    /*public void saveParent(Parent parent)
    {
        try
        {
            InternalStorage.writeObject(this, "Parent", parent);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public Parent loadParent()
    {
        Parent retVal = null;

        try
        {
            retVal = (Parent) InternalStorage.readObject(this, "Parent");
        }
        catch(FileNotFoundException fe)
        {
            fe.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (ClassNotFoundException ce)
        {
            ce.printStackTrace();
        }

        return retVal == null ? new Parent() : retVal;
    }*/
}
