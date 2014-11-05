package dk.projekt.bachelor.wheresmyfamily.activities;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ArrayAdapter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import android.util.Log;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.TableJsonQueryCallback;

import dk.projekt.bachelor.wheresmyfamily.DataModel.Child;
import dk.projekt.bachelor.wheresmyfamily.InternalStorage;
import dk.projekt.bachelor.wheresmyfamily.R;
import dk.projekt.bachelor.wheresmyfamily.authenticator.AuthService;
import dk.projekt.bachelor.wheresmyfamily.authenticator.AuthenticationApplication;


public class LoggedInParent extends ListActivity {

    private final String TAG = "LoggedInParent";
    private TextView mLblUserIdValue;
    private TextView mLblUsernameValue;
    private EditText parentName;
    private ListView m_list;
    protected AuthService mAuthService;

    private ProgressDialog m_ProgressDialog = null;
    private ArrayList<Child> m_My_children = null;
    private ChildAdapter m_adapter;
    private Runnable viewChild;
    private String partitionKey;
    private String rowKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_in_parent);

        Toast.makeText(this, "LoggedInParent OnCreate", Toast.LENGTH_SHORT).show();

        m_My_children = loadChildren();

        getListView().setOnItemClickListener(listlistener);

        //Because BaseActivity extension isnt possible
        AuthenticationApplication myApp = (AuthenticationApplication) getApplication();
        myApp.setCurrentActivity(this);
        mAuthService = myApp.getAuthService();

        this.m_adapter = new ChildAdapter(this, R.layout.row, m_My_children);
        ListView myList=(ListView)findViewById(android.R.id.list);
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

        //get UI elements

        mLblUsernameValue = (TextView) findViewById(R.id.lblUsernameValue);

        AuthService authService = myApp.getAuthService();

        //Fetch auth data (the username) on load
        authService.getAuthData(new TableJsonQueryCallback() {
            @Override
            public void onCompleted(JsonElement result, int count, Exception exception,
                                    ServiceFilterResponse response) {
                if (exception == null) {
                    JsonArray results = result.getAsJsonArray();
                    JsonElement item = results.get(0);
                    partitionKey = item.getAsJsonObject().getAsJsonPrimitive("Email").getAsString();
                    rowKey = item.getAsJsonObject().getAsJsonPrimitive("UserName").getAsString();
                } else {
                    Log.e(TAG, "There was an exception getting auth data: " + exception.getMessage());
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        getActionBar().setDisplayHomeAsUpEnabled(true);

        m_My_children = loadChildren();
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
            m_My_children = loadChildren();

            /*Child test = new Child();
            test.setChildName("Per");
            test.setPhone("12345678");
            m_My_children.add(test);

            saveChildren(m_My_children);*/

            // m_My_children = loadChildren();

            Thread.sleep(2000);
            Log.i("ARRAY", "" + m_My_children.size());
        } catch (Exception e){
            Log.e("BACKGROUND_PROC", e.getMessage());
        }
        runOnUiThread(returnRes);
    }

    public void saveChildren(ArrayList<Child> myChildren)
    {
        try
        {
            InternalStorage.writeObject(this, "Children", myChildren);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Child> loadChildren()
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
    }

    private class ChildAdapter extends ArrayAdapter<Child>{
        private ArrayList<Child> items;
        public ChildAdapter(Context context, int textViewResourceId, ArrayList<Child> items){
            super(context, textViewResourceId,items);
            this.items = items;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if(v == null){
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.row, null);
            }
            Child c = items.get(position);
            if (c != null){
                TextView tt = (TextView) v.findViewById(R.id.toptext);
                TextView bt = (TextView) v.findViewById(R.id.bottomtext);
                if (tt != null){
                    tt.setText("Name: " + c.getChildName());
                }
                if (bt != null){
                    bt.setText("Status: " + c.getChildStatus());
                }
            }
            return v;
        }
    }

    //Clicklistener for click on listview
    private AdapterView.OnItemClickListener listlistener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView parent, View arg1, int position,long arg3) {
            //Toast.makeText(getApplicationContext(), "You have clicked on " +
                   // ((Child)parent.getItemAtPosition(Position)).getChildName(), Toast.LENGTH_SHORT).show();
            Intent childClick = new Intent(LoggedInParent.this, SwipeMenu.class);
            startActivity(childClick);
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
                mAuthService.logout(true);
                return true;
            case R.id.action_deleteusr:
                mAuthService.deleteUser("accounts", rowKey, partitionKey);
                return true;
            case R.id.action_addChild:
                Intent register = new Intent(this, RegisterChild.class);
                startActivity(register);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void open(View view) {

        Intent intent = new Intent(this, SwipeMenu.class);
        startActivity(intent);
    }

    public void reg(View v)
    {
        Intent register = new Intent(this, RegisterChild.class);
        startActivity(register);
    }
}
