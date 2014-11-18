package dk.projekt.bachelor.wheresmyfamily.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.MapFragment;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.windowsazure.messaging.NotificationHub;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.TableJsonOperationCallback;
import com.microsoft.windowsazure.mobileservices.TableJsonQueryCallback;
import com.microsoft.windowsazure.notifications.NotificationsManager;

import java.io.FileNotFoundException;
import java.io.IOException;

import dk.projekt.bachelor.wheresmyfamily.InternalStorage;
import dk.projekt.bachelor.wheresmyfamily.DataModel.Parent;
import dk.projekt.bachelor.wheresmyfamily.LocationService;
import dk.projekt.bachelor.wheresmyfamily.MyHandler;
import dk.projekt.bachelor.wheresmyfamily.helper.BaseActivity;
import dk.projekt.bachelor.wheresmyfamily.R;
import dk.projekt.bachelor.wheresmyfamily.authenticator.AuthService;
import dk.projekt.bachelor.wheresmyfamily.authenticator.AuthenticationApplication;


public class LoggedInChild extends BaseActivity implements LocationListener {

    private final String TAG = "LoggedInChild";
    private TextView mLblUsernameValue,parentInfoName, parentInfoPhone;
    Parent parent = new Parent();
    private String provider;
    LocationManager locationManager;
    Location currentLocation;

    private String SENDER_ID = "911215571794";
    private GoogleCloudMessaging mGcm;
    private NotificationHub mHub;
    private String mRegistrationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_in_child);

        //Notifacation hub Azure
        mGcm = GoogleCloudMessaging.getInstance(this);
        String connectionString = "Endpoint=sb://wheresmyfamilumshub-ns.servicebus.windows.net/;SharedAccessKeyName=DefaultListenSharedAccessSignature;SharedAccessKey=ND9FwY7wdab88K5p7jxxUEgmHk8z1LCHGfDEqg8UFHY=";
        mHub = new NotificationHub("WheresMyFamiluMSHub", connectionString, this);
        NotificationsManager.handleNotifications(this, SENDER_ID, MyHandler.class);
        registerWithNotificationHubs();

        // Reference UI elements
        mLblUsernameValue = (TextView) findViewById(R.id.lblUsernameValue);
        parentInfoName = (TextView) findViewById(R.id.parentinput);
        parentInfoPhone = (TextView) findViewById(R.id.phoneinput);

        Toast.makeText(this, "LoggedInChild OnCreate", Toast.LENGTH_SHORT).show();

        MapFragment mapFragment = MapFragment.newInstance();
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);


        // Get parent info
        parent = loadParent();

        parentInfoName.setText(parent.name);
        parentInfoPhone.setText(parent.phone);


        // Authenticate
        AuthenticationApplication myApp = (AuthenticationApplication) getApplication();
        AuthService authService = myApp.getAuthService();

        //Fetch auth data (the username) on load
           authService.getAuthData(new TableJsonQueryCallback() {
               @Override
               public void onCompleted(JsonElement result, int count, Exception exception,
                                       ServiceFilterResponse response) {
                   if (exception == null) {
                       JsonArray results = result.getAsJsonArray();
                       JsonElement item = results.get(0);
                       mLblUsernameValue.setText(item.getAsJsonObject().getAsJsonPrimitive("UserName").getAsString());
                   } else {
                       Log.e(TAG, "There was an exception getting auth data: " + exception.getMessage());
                   }
               }
           });

        // Setup location using  Google's location manager
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }

        Intent intent = new Intent(this, LocationService.class);
        startService(intent);

        ImageButton button = (ImageButton) findViewById(R.id.callchild);
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + parent.phone));
                startActivity(callIntent);
            }

        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        Toast.makeText(this, "LoggedInChild OnResume", Toast.LENGTH_SHORT).show();

        // Get and show parent info
        parent = loadParent();

        parentInfoName.setText(parent.name);
        parentInfoPhone.setText(parent.phone);

        // Define the criteria on how to select the location provider
        Criteria criteria = new Criteria();
        provider =  locationManager.getBestProvider(criteria, false);

        currentLocation = locationManager.getLastKnownLocation(provider);

        if(currentLocation != null)
        {
            System.out.println("Udbyder " + provider + " er valgt");
            onLocationChanged(currentLocation);
        }
        else
            Toast.makeText(this, "Position utilgængelig", Toast.LENGTH_SHORT).show();
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
                mAuthService.logout(true);
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

    public void saveParent(Parent parent)
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
    }

    @Override
    public void onLocationChanged(Location location) {

        currentLocation = location;
    }

    @SuppressWarnings("unchecked")
    private void registerWithNotificationHubs() {
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object... params) {
                try {
                    mRegistrationId = mGcm.register(SENDER_ID);
                    Log.i(TAG, "Registered with id: " + mRegistrationId);
                    mHub.register(mRegistrationId, "parent");
                    //mAuthService.callApi();
                } catch (Exception e) {
                    Log.e(TAG, "Issue registering with hub: " + e.getMessage());
                    return e;
                }
                return null;
            }
        }.execute(null, null, null);
    }

    public void deleteDialogBox(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Bekræft Sletning af Brugerprofil");
        builder.setMessage("Er du sikker?");

        builder.setPositiveButton("JA", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                // Do nothing but close the dialog
                mAuthService.deleteUser();
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

    public void getEventId(String eventID){

        //Bundle bundle = getIntent().getExtras();
        //String message = bundle.getString("message");

        mAuthService.getCalendarEvent (eventID, new TableJsonOperationCallback() {
            @Override
            public void onCompleted(JsonObject jsonObject, Exception exception,
                                    ServiceFilterResponse response) {
                if (exception == null) {
                    jsonObject.get("id");
                    Toast.makeText(getApplicationContext(), "Kalender hentet", Toast.LENGTH_LONG).show();

                } else {
                    Log.e(TAG, "There was an error registering the event: " + exception.getMessage());
                }
            }
        });
    }

    public void sendLocation(){
        String parentmail = "";
        String location = "";
        mAuthService.sendLocation(parentmail, location, new TableJsonOperationCallback() {
            @Override
            public void onCompleted(JsonObject jsonObject, Exception exception, ServiceFilterResponse response) {
                if (exception == null){

                }
                else {
                    Log.e(TAG, "There was an exception sending email: " + exception.getMessage());
                }
            }
        });
        Toast.makeText(getApplicationContext(), "Location sendt", Toast.LENGTH_LONG).show();
    }
}
