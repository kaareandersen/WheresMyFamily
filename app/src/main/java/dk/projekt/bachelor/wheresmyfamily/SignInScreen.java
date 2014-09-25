package dk.projekt.bachelor.wheresmyfamily;

import com.google.gson.JsonObject;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.TableJsonOperationCallback;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class SignInScreen extends Activity {

    private final String TAG = "SignInScreen";
    private Button mBtnLogin;
    private EditText mTxtUsername;
    private EditText mTxtPassword;
    private Activity mActivity;
    private AuthService mAuthService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in_screen);

        mActivity = this;


        //Get UI objects
        mBtnLogin = (Button) findViewById(R.id.btnSignIn);
        mTxtUsername = (EditText) findViewById(R.id.etLogEmail);
        mTxtPassword = (EditText) findViewById(R.id.etLogPass);

        //Add on click listeners
        mBtnLogin.setOnClickListener(loginClickListener);
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

    View.OnClickListener loginClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mTxtPassword.getText().toString().equals("") ||
                                mTxtUsername.getText().toString().equals("")) {
                Log.w(TAG, "Email or Password not entered");
                return;
            }
            mAuthService.login(mTxtUsername.getText().toString(), mTxtPassword.getText().toString(), new TableJsonOperationCallback() {
                @Override
                public void onCompleted(JsonObject jsonObject, Exception exception, ServiceFilterResponse response) {
                    if (exception == null) {
                        //If they've registered successfully, we'll save and set the userdata and then
                        //show the logged in activity
                        mAuthService.setUserAndSaveData(jsonObject);
                        Intent loggedInIntent = new Intent(getApplicationContext(), LoggedIn.class);
                        startActivity(loggedInIntent);
                    } else {
                        Log.e(TAG, "Error loggin in: " + exception.getMessage());
                    }
                }
            });
        }
    };
}
