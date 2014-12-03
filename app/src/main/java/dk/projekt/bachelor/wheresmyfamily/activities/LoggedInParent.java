package dk.projekt.bachelor.wheresmyfamily.activities;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
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
import com.microsoft.windowsazure.notifications.NotificationsManager;

import org.json.JSONArray;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dk.projekt.bachelor.wheresmyfamily.BroadCastReceiver.MyHandler;
import dk.projekt.bachelor.wheresmyfamily.Controller.MobileServicesClient;
import dk.projekt.bachelor.wheresmyfamily.Controller.NotificationHubController;
import dk.projekt.bachelor.wheresmyfamily.DataModel.Child;
import dk.projekt.bachelor.wheresmyfamily.R;
import dk.projekt.bachelor.wheresmyfamily.Services.ReceiveTransitionsIntentService;
import dk.projekt.bachelor.wheresmyfamily.UserInfoStorage;
import dk.projekt.bachelor.wheresmyfamily.authenticator.AuthenticationApplication;


public class LoggedInParent extends ListActivity implements GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener, LocationClient.OnAddGeofencesResultListener,
        LocationListener {

    //region Fields
    private final String TAG = "LoggedInParent";
    private TextView mLblUserIdValue;
    private TextView mLblUsernameValue;
    private EditText parentName;
    private ListView m_list;
    protected MobileServicesClient mMobileServicesClient;
    protected NotificationHubController mNotificationHubController;

    private ProgressDialog m_ProgressDialog = null;
    private ArrayList<Child> m_My_children = new ArrayList<Child>();
    private ChildAdapter m_adapter;
    private Runnable viewChild;
    private String uEmail;
    private String uName;
    private String id;

    private String SENDER_ID = "911215571794";
    private GoogleCloudMessaging mGcm;
    private NotificationHub mHub;
    private String mRegistrationId;
    LocationClient locationClient;

    ArrayList<Child> mChildren = new ArrayList<Child>();
    JSONArray mParents = new JSONArray();
    String childrenPrefName = "myChildren";
    String parentsPrefName = "myParents";
    String childrenKey = "childrenInfo";
    String parentsKey = "parentsInfo";
    UserInfoStorage storage = new UserInfoStorage();
    // Define an object that holds accuracy and frequency parameters
    private LocationRequest mLocationRequest;

    // Setup Location update interval
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
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

    // Stores the PendingIntent used to request geofence monitoring
    private PendingIntent mGeofenceRequestIntent;
    // Defines the allowable request types.
    public enum REQUEST_TYPE {ADD, REMOVE_INTENT, REMOVE_LIST, START, STOP }
    private REQUEST_TYPE mRequestType;
    // Flag that indicates if a request is underway.
    private boolean mInProgress;
    // Store the list of geofence Ids to remove
    List<String> mGeofencesToRemove;

    // Store the PendingIntent used to send activity recognition events back to the app
    private PendingIntent mActivityRecognitionPendingIntent;
    // Store the current activity recognition client
    private ActivityRecognitionClient mActivityRecognitionClient;

    ListView myList;

    //endregion

    //region Lifecycle events
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_in_parent);

        getListView().setOnItemClickListener(listlistener);

        //Because BaseActivity extension isn't possible
        mNotificationHubController = new NotificationHubController(this);

        //Because BaseActivity extension isnt possible
        AuthenticationApplication myApp = (AuthenticationApplication) getApplication();
        myApp.setCurrentActivity(this);
        mMobileServicesClient = myApp.getAuthService();

        m_adapter = new ChildAdapter(this, R.layout.row, m_My_children);
        myList = (ListView)findViewById(android.R.id.list);
        myList.setAdapter(m_adapter);

        viewChild = new Runnable() {
            @Override
            public void run() {
                try {
                    getChild();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        Thread thread = new Thread(null, viewChild, "MagenToBackground");
        thread.start();
        m_ProgressDialog = ProgressDialog.show(LoggedInParent.this, "Please wait...", "Retrieving data ...", true);

        mGcm = GoogleCloudMessaging.getInstance(this);

        String connectionString =
                "Endpoint=sb://wheresmyfamilumshub-ns.servicebus.windows.net/;SharedAccessKeyName=DefaultListenSharedAccessSignature;SharedAccessKey=ND9FwY7wdab88K5p7jxxUEgmHk8z1LCHGfDEqg8UFHY=";
        mHub = new NotificationHub("WheresMyFamiluMSHub", connectionString, this);
        NotificationsManager.handleNotifications(this, SENDER_ID, MyHandler.class);

        //get UI elements
        mLblUsernameValue = (TextView) findViewById(R.id.lblUsernameValue);

        //Fetch auth data (the username) on load
        MobileServicesClient mobileServicesClient = myApp.getAuthService();
        mobileServicesClient.getAuthData(new TableJsonQueryCallback() {
            @Override
            public void onCompleted(JsonElement result, int count, Exception exception,
                                    ServiceFilterResponse response) {
                if (exception == null) {
                    JsonArray results = result.getAsJsonArray();
                    JsonElement item = results.get(0);
                    uEmail = item.getAsJsonObject().getAsJsonPrimitive("Email").getAsString();
                    uName = item.getAsJsonObject().getAsJsonPrimitive("UserName").getAsString();
                    mNotificationHubController.registerWithNotificationHubs(uEmail);
                } else {
                    Log.e(TAG, "There was an exception getting auth data: " + exception.getMessage());
                }
            }
        });

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
    }

    @Override
    protected void onResume() {
        super.onResume();

        m_My_children = storage.loadChildren(this);

        // If any children are registered
        if(m_My_children.size() > 0)
        {
            // Since we are on the home page, set current child to none
            for(int i = 0; i < m_My_children.size(); i++)
            {
                m_My_children.get(i).setIsCurrent(false);
            }
        }
        // Refresh the list of children
        myList.setAdapter(new ChildAdapter(this, R.layout.row, m_My_children));
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    //endregion

    private Runnable returnRes = new Runnable() {
        @Override
        public void run() {
            if (m_My_children != null && m_My_children.size() > 0){
                m_adapter.notifyDataSetChanged();
                for (int i=0;i< m_My_children.size();i++)
                    m_adapter.add(m_My_children.get(i));
            }
            m_ProgressDialog.dismiss();
            m_adapter.notifyDataSetChanged();
        }
    };

    private void getChild() throws FileNotFoundException, IOException {
        try
        {
            m_My_children = storage.loadChildren(this);

            Thread.sleep(2000);
            Log.i("ARRAY", "" + m_My_children.size());
        } catch (Exception e){
            Log.e("BACKGROUND_PROC", e.getMessage());
        }

        runOnUiThread(returnRes);
    }

    //region Location callback methods
    @Override
    public void onConnected(Bundle bundle) {
        Toast.makeText(this, "LocationActivity connected", Toast.LENGTH_SHORT).show();

        locationClient.requestLocationUpdates(mLocationRequest, this);

        /*if (mRequestType != null)
        {
            switch (mRequestType)
            {
                case ADD:
                    mGeofenceRequestIntent = getTransitionPendingIntent();
                    // Send a request to add the current geofences
                    locationClient.addGeofences(mCurrentGeofences, mGeofenceRequestIntent, this);
                case REMOVE_INTENT:
                    mGeofenceRequestIntent = getTransitionPendingIntent();
                    locationClient.removeGeofences(mGeofenceRequestIntent,
                            (LocationClient.OnRemoveGeofencesResultListener) this);
                    break;
                case REMOVE_LIST:
                    locationClient.removeGeofences(mGeofencesToRemove,
                            (LocationClient.OnRemoveGeofencesResultListener) this);
                    break;
                case START :
                    *//*
                     * Request activity recognition updates using the
                     * preset detection interval and PendingIntent.
                     * This call is synchronous.
                     *//*
                    mActivityRecognitionClient.requestActivityUpdates(DETECTION_INTERVAL_MILLISECONDS,
                            mActivityRecognitionPendingIntent);
                    break;
                case STOP :
                    mActivityRecognitionClient.removeActivityUpdates(mActivityRecognitionPendingIntent);
                    break;
                    *//*
                     * An enum was added to the definition of REQUEST_TYPE,
                     * but it doesn't match a known case. Throw an exception.
                     *//*
                default :
                    try {
                        throw new Exception("Unknown request type in onConnected().");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }*/

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
        /*mInProgress = false;
        mActivityRecognitionClient.disconnect();*/
    }

    @Override
    public void onDisconnected() {
        // Turn off the request flag
        mInProgress = false;
        // Destroy the current location client
        locationClient = null;
        // Delete the client
        mActivityRecognitionClient = null;
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onAddGeofencesResult(int i, String[] strings) {


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
            *//*
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
                errorFragment.show(getSupportFragmentManager(), "Error Detection in" + getCallingActivity());*/
        }
    }
    //endregion


    //region Child listadapter
    private class ChildAdapter extends ArrayAdapter<Child>
    {
        public ChildAdapter(Context context, int textViewResourceId, ArrayList<Child> items)
        {
            super(context, textViewResourceId, items);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            View v = convertView;
            if(v == null)
            {
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.row, null);
            }

            Child c = null;
            c = m_My_children.get(position);

            if (c != null)
            {
                TextView tt = (TextView) v.findViewById(R.id.toptext);
                TextView bt = (TextView) v.findViewById(R.id.bottomtext);
                if (tt != null)
                {
                    tt.setText("Name: " + c.getName());
                }
                if (bt != null)
                {
                    bt.setText("Phone: " + c.getPhone());
                }
            }

            return v;
        }
    }

    //Clicklistener for click on listview
    private AdapterView.OnItemClickListener listlistener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView parent, View arg1, int position,long arg3) {
            Intent childClick = new Intent(LoggedInParent.this, OverviewActivity.class);
            startActivity(childClick);

            // Set selected user to current user
            m_My_children.get(position).setIsCurrent(true);
        }
    };
    //endregion

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.logged_in, menu);
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
                Intent register = new Intent(this, RegisterChild.class);
                startActivity(register);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        storage.saveChildren(this, m_My_children);
    }

    public void callApi(View view) {

        mMobileServicesClient.callApi();
    }

    public void reg(View v)
    {
        Intent register = new Intent(this, RegisterChild.class);
        startActivity(register);
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

    public void createLocation(View view)
    {
        Intent intent = new Intent(this, OverviewActivity.class);
        startActivity(intent);
    }

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

    /* public void saveChildren(ArrayList<Child> myChildren)
    {
        try
        {
            InternalStorage.writeObject(this, "Children", myChildren);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

   /* public ArrayList<Child> loadChildren()
    {
        ArrayList<Child> retVal = null;

        try
        {
            retVal = (ArrayList<Child>) InternalStorage.readObject(this, "Children");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return retVal == null ? new ArrayList<Child>() : retVal;
    }*/
}
