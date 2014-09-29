package dk.projekt.bachelor.wheresmyfamily;

/**
 * Created by KaareAndersen on 26/09/14.
 */
import android.app.Activity;
import android.os.Bundle;

public class BaseActivity extends Activity {

    protected AuthService mAuthService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        AuthenticationApplication myApp = (AuthenticationApplication) getApplication();
        myApp.setCurrentActivity(this);
        mAuthService = myApp.getAuthService();
    }
}
