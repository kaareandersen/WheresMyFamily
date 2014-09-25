package dk.projekt.bachelor.wheresmyfamily;


import com.google.gson.JsonObject;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.TableJsonOperationCallback;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class SignUpScreen extends Activity {

    private final String TAG = "SignUpScreen";
    private Button btnRegister;
    private EditText mTxtUsername;
    private EditText mTxtPassword;
    private EditText mTxtConfirm;
    private EditText mTxtEmail;
    private Activity mActivity;
    private AuthService mAuthService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_screen);

        mActivity = this;

        //Get UI elements
        btnRegister = (Button) findViewById(R.id.btnRegister);
        mTxtUsername = (EditText) findViewById(R.id.txtRegisterUsername);
        mTxtPassword = (EditText) findViewById(R.id.txtRegisterPassword);
        mTxtConfirm = (EditText) findViewById(R.id.txtRegisterConfirm);
        mTxtEmail = (EditText) findViewById(R.id.txtRegisterEmail);

        //Set click listeners
        btnRegister.setOnClickListener(registerClickListener);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.sign_up_screen, menu);
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

    View.OnClickListener registerClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            //We're just logging the validation errors, we should be showing something to the user
            if (mTxtUsername.getText().toString().equals("") ||
                    mTxtPassword.getText().toString().equals("") ||
                    mTxtConfirm.getText().toString().equals("") ||
                    mTxtEmail.getText().toString().equals("")) {
                Log.w(TAG, "You must enter all fields to register");
                return;
            } else if (!mTxtPassword.getText().toString().equals(mTxtConfirm.getText().toString())) {
                Log.w(TAG, "The passwords you've entered don't match");
                return;
            } else {
                mAuthService.registerUser(mTxtUsername.getText().toString(),
                        mTxtPassword.getText().toString(),
                        mTxtConfirm.getText().toString(),
                        mTxtEmail.getText().toString(),
                        new TableJsonOperationCallback() {
                            @Override
                            public void onCompleted(JsonObject jsonObject, Exception exception,
                                                    ServiceFilterResponse response) {
                                if (exception == null) {
                                    //If that was successful, set and save the user data
                                    mAuthService.setUserAndSaveData(jsonObject);
                                    //Finish this activity and run the logged in activity
                                    mActivity.finish();
                                    Intent loggedInIntent = new Intent(getApplicationContext(), LoggedIn.class);
                                    startActivity(loggedInIntent);
                                } else {
                                    Log.e(TAG, "There was an error registering the user: " + exception.getMessage());
                                }
                            }
                        });
            }
        }
    };
}
