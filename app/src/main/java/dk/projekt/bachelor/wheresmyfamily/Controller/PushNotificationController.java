package dk.projekt.bachelor.wheresmyfamily.Controller;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.TableJsonOperationCallback;

/**
 * Created by KaareAndersen on 20/11/14.
 */
public class PushNotificationController {

    private final static String TAG = "PushNotificationController";
    private static Context mContext;
    private static MobileServicesClient mMobileServicesClient;
    private static CEventChildController cEventChildController;

    public PushNotificationController(Context context) {
        mContext = context;
        mMobileServicesClient = new MobileServicesClient(mContext);
        cEventChildController = new CEventChildController(mContext);
    }

    public void getEventId(String eventID){

        mMobileServicesClient.getCalendarEvent (eventID, new TableJsonOperationCallback() {
            @Override
            public void onCompleted(JsonObject jsonObject, Exception exception,
                                    ServiceFilterResponse response) {
                if (exception == null) {

                    String eventName = jsonObject.getAsJsonPrimitive("EventName").getAsString();
                    String startDate = jsonObject.getAsJsonPrimitive("StartDate").getAsString();
                    String startTime = jsonObject.getAsJsonPrimitive("StartTime").getAsString();
                    String endDate = jsonObject.getAsJsonPrimitive("EndDate").getAsString();
                    String endTime = jsonObject.getAsJsonPrimitive("EndTime").getAsString();

                    String[] sepDate = startDate.split("-");
                    int date = Integer.parseInt(sepDate[0]);
                    int month = Integer.parseInt(sepDate[1]);
                    int year = Integer.parseInt(sepDate[2]);

                    String[] sepTime = startTime.split(":");
                    int hour = Integer.parseInt(sepTime[0]);
                    int minute = Integer.parseInt(sepTime[1]);

                    cEventChildController.startEvent(year,month,date,hour,minute);

                    Toast.makeText(mContext, "Kalender hentet", Toast.LENGTH_LONG).show();

                } else {
                    Log.e(TAG, "There was an error registering the event: " + exception.getMessage());
                }
            }
        });
    }

    public void askForLocationFromChild(String childEmail){
        mMobileServicesClient.getLocation(childEmail, new TableJsonOperationCallback() {
            @Override
            public void onCompleted(JsonObject jsonObject, Exception exception, ServiceFilterResponse response) {
                if (exception == null){
                    Toast.makeText(mContext, "pushnotifikationcontroller send location", Toast.LENGTH_LONG).show();
                }
                else {
                    Log.e(TAG, "There was an exception requesting location from child: " + exception.getMessage());
                }
            }
        });
    }

    //Skal hente lokation på barnets telefon
    public void sendLocationFromChild(String parentEmail, String location){
        mMobileServicesClient.sendLocation(parentEmail, location, new TableJsonOperationCallback() {
            @Override
            public void onCompleted(JsonObject jsonObject, Exception exception, ServiceFilterResponse response) {
                if (exception == null){
                    Toast.makeText(mContext, "Lokation Sendt!", Toast.LENGTH_LONG).show();
                }
                else {
                    Log.e(TAG, "There was an exception sending location from Child: " + exception.getMessage());
                }
            }
        });
    }
}
