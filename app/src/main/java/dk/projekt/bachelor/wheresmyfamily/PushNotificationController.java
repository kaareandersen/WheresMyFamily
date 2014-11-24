package dk.projekt.bachelor.wheresmyfamily;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.TableJsonOperationCallback;

import dk.projekt.bachelor.wheresmyfamily.authenticator.AuthService;
import dk.projekt.bachelor.wheresmyfamily.helper.BaseActivity;

/**
 * Created by KaareAndersen on 20/11/14.
 */
public class PushNotificationController {

    private final static String TAG = "PushNotificationController";
    private static Context mContext;
    private static AuthService mAuthService;

    public PushNotificationController(Context context) {
        mContext = context;
        mAuthService = new AuthService(mContext);
    }

    public void getEventId(String eventID){

        mAuthService.getCalendarEvent (eventID, new TableJsonOperationCallback() {
            @Override
            public void onCompleted(JsonObject jsonObject, Exception exception,
                                    ServiceFilterResponse response) {
                if (exception == null) {
                    jsonObject.get("id");
                    Toast.makeText(mContext, "Kalender hentet", Toast.LENGTH_LONG).show();

                } else {
                    Log.e(TAG, "There was an error registering the event: " + exception.getMessage());
                }
            }
        });
    }

    //Skal hente lokation på barnets telefon
    public void sendLocationFromChild(String parentEmail, String location){
        mAuthService.sendLocation(parentEmail, location, new TableJsonOperationCallback() {
            @Override
            public void onCompleted(JsonObject jsonObject, Exception exception, ServiceFilterResponse response) {
                if (exception == null){

                }
                else {
                    Log.e(TAG, "There was an exception sending location from Child: " + exception.getMessage());
                }
            }
        });
        Toast.makeText(mContext, "Location sendt", Toast.LENGTH_LONG).show();
    }


    public void askForLocationFromChild(String childEmail){
        mAuthService.getLocation(childEmail, new TableJsonOperationCallback() {
            @Override
            public void onCompleted(JsonObject jsonObject, Exception exception, ServiceFilterResponse response) {
                if (exception == null){

                }
                else {
                    Log.e(TAG, "There was an exception requesting location from child: " + exception.getMessage());
                }
            }
        });
    }
}
