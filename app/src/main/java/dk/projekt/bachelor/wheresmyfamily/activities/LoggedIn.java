package dk.projekt.bachelor.wheresmyfamily.activities;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.TableJsonQueryCallback;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.util.Log;

import java.util.ArrayList;

import dk.projekt.bachelor.wheresmyfamily.helper.BaseActivity;
import dk.projekt.bachelor.wheresmyfamily.R;
import dk.projekt.bachelor.wheresmyfamily.authenticator.AuthService;
import dk.projekt.bachelor.wheresmyfamily.authenticator.AuthenticationApplication;


public class LoggedIn extends ListActivity {

    private final String TAG = "LoggedIn";
    private TextView mLblUserIdValue;
    private TextView mLblUsernameValue;
    private EditText parentName;
    AuthService mAuthService;

    /*private ProgressDialog m_ProgressDialog = null;
    private ArrayList<Order> m_orders = null;
    private OrderAdapter m_adapter;
    private Runnable viewOrders;*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_in);

        /*m_orders = new ArrayList<Order>();
        this.m_adapter = new OrderAdapter(this, R.layout.row, m_orders);
        setListAdapter(this, m_adapter);*/





        //get UI elements
        mLblUsernameValue = (TextView) findViewById(R.id.lblUsernameValue);

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
    }


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
                mAuthService.deleteUser("Accounts");
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
