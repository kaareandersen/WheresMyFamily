package dk.projekt.bachelor.wheresmyfamily.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.TableJsonOperationCallback;
import com.microsoft.windowsazure.mobileservices.TableJsonQueryCallback;

import dk.projekt.bachelor.wheresmyfamily.Controller.MobileServicesClient;
import dk.projekt.bachelor.wheresmyfamily.R;
import dk.projekt.bachelor.wheresmyfamily.authenticator.AuthenticationApplication;
import dk.projekt.bachelor.wheresmyfamily.helper.BaseActivity;


public class LogInScreen extends BaseActivity {

    private final String TAG = "LogInScreen";
    private Button mBtnLogin;
    private EditText mTxtEmail;
    private EditText mTxtPassword;
    private TextView mTxtForgetPass;
    private Activity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in_screen);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        mActivity = this;


        //Get UI objects
        mBtnLogin = (Button) findViewById(R.id.btnSignIn);

        mTxtEmail = (EditText) findViewById(R.id.etLogEmail);
        mTxtPassword = (EditText) findViewById(R.id.etLogPass);
        mTxtForgetPass = (TextView) findViewById(R.id.txtForgetPass);

        //Add on click listeners
        mBtnLogin.setOnClickListener(loginClickListener);
        mTxtForgetPass.setOnClickListener(loginClickListener);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.sign_in_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void sendEmail(){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Send password");
        alert.setMessage("Skriv din email for at modtage dit password");

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String value = input.getText().toString();
                        //Pass email to azure
                        mMobileServicesClient.sendEmailPassW(value,
                                new TableJsonOperationCallback() {
                            @Override
                            public void onCompleted(JsonObject jsonObject, Exception exception, ServiceFilterResponse response) {
                                if (exception == null){}
                                else {
                                    Log.e(TAG, "There was an exception sending email: " +
                                            exception.getMessage());
                                }
                            }
                        });
                    }
                });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();
    }

    View.OnClickListener loginClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view == mBtnLogin) {
                if (mTxtPassword.getText().toString().equals("") ||
                        mTxtEmail.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(),
                            "Email eller Password er ikke indtastet", Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "Email or Password not entered");
                    return;
                }
                mMobileServicesClient.login(mTxtEmail.getText().toString(), mTxtPassword.getText().toString(), new TableJsonOperationCallback() {
                    @Override
                    public void onCompleted(JsonObject jsonObject, Exception exception, ServiceFilterResponse response) {
                        if (exception == null) {
                            //If they've registered successfully, we'll save and set the userdata and then
                            //show the logged in activity
                            mMobileServicesClient.setUserAndSaveData(jsonObject);
                            AuthenticationApplication myApp = (AuthenticationApplication) getApplication();
                            MobileServicesClient mobileServicesClient = myApp.getAuthService();

                            //Fetch auth data (the username) on load
                            mobileServicesClient.getAuthData(new TableJsonQueryCallback() {
                                @Override
                                public void onCompleted(JsonElement result, int count, Exception exception,
                                                        ServiceFilterResponse response) {
                                    if (exception == null) {
                                        JsonArray results = result.getAsJsonArray();
                                        JsonElement item = results.get(0);
                                        boolean _child = Boolean.valueOf(item.getAsJsonObject().getAsJsonPrimitive("Child").getAsBoolean());
                                        if (_child == true) {
                                            Intent loggedInIntent = new Intent(getApplicationContext(), LoggedInChild.class);
                                            startActivity(loggedInIntent);
                                        } else {
                                            Intent loggedInIntent = new Intent(getApplicationContext(), LoggedInParent.class);
                                            startActivity(loggedInIntent);
                                        }
                                    } else {
                                        Log.e(TAG, "There was an exception getting auth data: " + exception.getMessage());
                                    }
                                }
                            });


                        } else {
                            Toast.makeText(getApplicationContext(),
                                    "Login mislykkedes: " + exception.getCause().getMessage(), Toast.LENGTH_LONG).show();
                            Log.e(TAG, "Error loggin in: " + exception.getMessage());
                        }
                    }
                });
            }
            if (view == mTxtForgetPass){
                sendEmail();
            }
        }
    };

}
