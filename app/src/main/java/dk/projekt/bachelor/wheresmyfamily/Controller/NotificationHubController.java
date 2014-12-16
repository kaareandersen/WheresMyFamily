package dk.projekt.bachelor.wheresmyfamily.Controller;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.microsoft.windowsazure.messaging.NotificationHub;
import com.microsoft.windowsazure.notifications.NotificationsManager;

import dk.projekt.bachelor.wheresmyfamily.BroadCastReceiver.MyHandler;

/**
 * Created by KaareAndersen on 20/11/14.
 */
public class    NotificationHubController {

    private final String TAG = "NotificationHubController";

    private Context mContext;

    private String SENDER_ID = "911215571794";
    private GoogleCloudMessaging mGcm;
    private NotificationHub mHub;
    private String mRegistrationId;

    public NotificationHubController(Context context){
        mContext = context;

        mGcm = GoogleCloudMessaging.getInstance(mContext);

        String connectionString =
                "Endpoint=sb://wheresmyfamilumshub-ns.servicebus.windows.net/;SharedAccessKeyName=DefaultListenSharedAccessSignature;SharedAccessKey=ND9FwY7wdab88K5p7jxxUEgmHk8z1LCHGfDEqg8UFHY=";
        mHub = new NotificationHub("WheresMyFamiluMSHub", connectionString, mContext);
        NotificationsManager.handleNotifications(mContext, SENDER_ID, MyHandler.class);
    }

    @SuppressWarnings("unchecked")
    public void registerWithNotificationHubs(final String email) {
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object... params) {
                try {
                    mRegistrationId = mGcm.register(SENDER_ID);
                    Log.i(TAG, "Registered with id: " + mRegistrationId + "Tag: " + email);
                    mHub.register(mRegistrationId, email);
                } catch (Exception e) {
                    Log.e(TAG, "Issue registering with hub: " + e.getMessage());
                    return e;
                }
                return null;
            }
        }.execute(null, null, null);
    }

    @SuppressWarnings("unchecked")
    public void unRegisterNH() {
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object... params) {
                try {
                    mHub.unregister();
                } catch (Exception e) {
                    Log.e(TAG, "Issue unregistering with hub: " + e.getMessage());
                    return e;
                }
                return null;
            }
        }.execute(null, null, null);
    }

}
