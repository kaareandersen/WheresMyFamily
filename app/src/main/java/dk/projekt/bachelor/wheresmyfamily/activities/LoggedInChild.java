package dk.projekt.bachelor.wheresmyfamily.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.TableJsonQueryCallback;

import java.io.FileNotFoundException;
import java.io.IOException;

import dk.projekt.bachelor.wheresmyfamily.InternalStorage;
import dk.projekt.bachelor.wheresmyfamily.DataModel.Parent;
import dk.projekt.bachelor.wheresmyfamily.helper.BaseActivity;
import dk.projekt.bachelor.wheresmyfamily.R;
import dk.projekt.bachelor.wheresmyfamily.authenticator.AuthService;
import dk.projekt.bachelor.wheresmyfamily.authenticator.AuthenticationApplication;


public class LoggedInChild extends BaseActivity {

    private final String TAG = "LoggedInChild";
    private TextView mLblUsernameValue;
    Parent parent = new Parent();
    EditText parentInfoName;
    EditText parentInfoPhone;
    TextView parentNameTextView;
    EditText parentNameEditText;
    TextView parentPhoneTextView;
    EditText parentPhoneEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_in_child);

        //get UI elements
        mLblUsernameValue = (TextView) findViewById(R.id.lblUsernameValue);
        parentInfoName = (EditText) findViewById(R.id.parentinput);
        parentInfoPhone = (EditText) findViewById(R.id.phoneinput);

        Toast.makeText(this, "LoggedInChild OnCreate", Toast.LENGTH_SHORT).show();

        parent = loadParent();

        parentInfoName.setText(parent.name);
        parentInfoPhone.setText(parent.phone);

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

        // saveParent(parent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Toast.makeText(this, "LoggedInChild OnResume", Toast.LENGTH_SHORT).show();

        parent = loadParent();

        parentInfoName.setText(parent.name);
        parentInfoPhone.setText(parent.phone);
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
               // mAuthService.deleteUser("Accounts");
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
             /*parentNameEditText.setText(retVal.name);
             parentPhoneEditText.setText(retVal.phone);*/

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

        if(retVal == null)
            return new Parent();
        else
            return retVal;
    }
}
