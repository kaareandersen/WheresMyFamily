package dk.projekt.bachelor.wheresmyfamily;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.TableJsonOperationCallback;
import com.microsoft.windowsazure.mobileservices.TableJsonQueryCallback;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.util.Log;


public class LoggedIn extends Activity {

    private final String TAG = "LoggedIn";
    private TextView mLblUserIdValue;
    private TextView mLblUsernameValue;
    private Button mBtnLogout;
    private Button mBtnTestNoRetry;
    private Button mBtnTestRetry;
    private TextView mLblInfo;
    private AuthService mAuthService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_in);

        //get UI elements
        mLblUserIdValue = (TextView) findViewById(R.id.lblUserIdValue);
        mLblUsernameValue = (TextView) findViewById(R.id.lblUsernameValue);
        mBtnLogout = (Button) findViewById(R.id.btnLogout);

        //Set click listeners
        mBtnLogout.setOnClickListener(logoutClickListener);

        AuthenticationApplication myApp = (AuthenticationApplication) getApplication();
        AuthService authService = myApp.getAuthService();

        mLblUserIdValue.setText(authService.getUserId());

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

    View.OnClickListener logoutClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            //Just trigger a logout if this is clicked
            mAuthService.logout(true);
        }
    };
}
