package dk.projekt.bachelor.wheresmyfamily.helper;

/**
 * Created by KaareAndersen on 26/09/14.
 */
import android.app.Activity;
import android.os.Bundle;

import dk.projekt.bachelor.wheresmyfamily.Controller.MobileServicesClient;
import dk.projekt.bachelor.wheresmyfamily.authenticator.AuthenticationApplication;

public class BaseActivity extends Activity {

    protected MobileServicesClient mMobileServicesClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        AuthenticationApplication myApp = (AuthenticationApplication) getApplication();
        myApp.setCurrentActivity(this);
        mMobileServicesClient = myApp.getAuthService();
    }
}
