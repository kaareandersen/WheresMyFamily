package dk.projekt.bachelor.wheresmyfamily.activities;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

import com.google.android.gms.gcm.GoogleCloudMessaging;
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

import dk.projekt.bachelor.wheresmyfamily.BroadCastReceiver.MyHandler;
import dk.projekt.bachelor.wheresmyfamily.Controller.MobileServicesClient;
import dk.projekt.bachelor.wheresmyfamily.Controller.NotificationHubController;
import dk.projekt.bachelor.wheresmyfamily.DataModel.Child;
import dk.projekt.bachelor.wheresmyfamily.R;
import dk.projekt.bachelor.wheresmyfamily.Services.LocationService;
import dk.projekt.bachelor.wheresmyfamily.UserInfoStorage;
import dk.projekt.bachelor.wheresmyfamily.authenticator.AuthenticationApplication;


public class LoggedInParent extends ListActivity {

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

    ArrayList<Child> mChildren = new ArrayList<Child>();
    JSONArray mParents = new JSONArray();
    String childrenPrefName = "myChildren";
    String parentsPrefName = "myParents";
    String childrenKey = "childrenInfo";
    String parentsKey = "parentsInfo";
    UserInfoStorage storage = new UserInfoStorage();
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_in_parent);

        // Toast.makeText(this, "LoggedInParent OnCreate", Toast.LENGTH_SHORT).show();

        getListView().setOnItemClickListener(listlistener);

        //Because BaseActivity extension isn't possible
        mNotificationHubController = new NotificationHubController(this);

        //Because BaseActivity extension isnt possible
        AuthenticationApplication myApp = (AuthenticationApplication) getApplication();
        myApp.setCurrentActivity(this);
        mMobileServicesClient = myApp.getAuthService();

        this.m_adapter = new ChildAdapter(this, R.layout.row, m_My_children);
        ListView myList = (ListView)findViewById(android.R.id.list);
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

        Intent intent = new Intent(this, LocationService.class);
        startService(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        m_My_children = storage.loadChildren(this);

        // If any children are registered
        if(m_My_children.size() > 0)
        {
            // Since we are on the home page, set current user to none
            for(int i = 0; i < m_My_children.size(); i++)
            {
                m_My_children.get(i).setIsCurrent(false);
            }
        }



    }

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

    private class ChildAdapter extends ArrayAdapter<Child>{

        public ChildAdapter(Context context, int textViewResourceId, ArrayList<Child> items){
            super(context, textViewResourceId, items);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if(v == null){
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
