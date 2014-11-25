package dk.projekt.bachelor.wheresmyfamily.authenticator;

import android.app.Activity;
import android.app.Application;

import dk.projekt.bachelor.wheresmyfamily.Controller.MobileServicesClient;

public class AuthenticationApplication extends Application {
    private MobileServicesClient mMobileServicesClient;
    private Activity mCurrentActivity;

    public AuthenticationApplication() {}

    public MobileServicesClient getAuthService() {
        if (mMobileServicesClient == null) {
            mMobileServicesClient = new MobileServicesClient(this);
        }
        return mMobileServicesClient;
    }

    public void setCurrentActivity(Activity activity) {
        mCurrentActivity = activity;
    }

    public Activity getCurrentActivity() {
        return mCurrentActivity;
    }
}
