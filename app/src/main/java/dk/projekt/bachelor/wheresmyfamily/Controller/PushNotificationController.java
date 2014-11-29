package dk.projekt.bachelor.wheresmyfamily.Controller;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.TableJsonOperationCallback;

import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import dk.projekt.bachelor.wheresmyfamily.BroadCastReceiver.AlarmReceiver;

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

                    int id = 10;
                    //Get values for event from Azure tables
                    String eventid = jsonObject.getAsJsonPrimitive("id").getAsString();
                    String eventName = jsonObject.getAsJsonPrimitive("EventName").getAsString();
                    String startDate = jsonObject.getAsJsonPrimitive("StartDate").getAsString();
                    String startTime = jsonObject.getAsJsonPrimitive("StartTime").getAsString();
                    String endDate = jsonObject.getAsJsonPrimitive("EndDate").getAsString();
                    String endTime = jsonObject.getAsJsonPrimitive("EndTime").getAsString();

                    //Convert date/month/year to int
                    String[] sepDate = startDate.split("-");
                    int date = Integer.parseInt(sepDate[0]);
                    int month = Integer.parseInt(sepDate[1]);
                    int year = Integer.parseInt(sepDate[2]);

                    //Convert minute/hour to int
                    String[] sepTime = startTime.split(":");
                    int hour = Integer.parseInt(sepTime[0]);
                    int minute = Integer.parseInt(sepTime[1]);

                    //Id number of chosen event
                    //int id = Integer.parseInt(eventid);

                    //Convert starttime to milliseconds NOT IN USE
                    long min = Integer.parseInt(startTime.substring(0, 2));
                    long sec = Integer.parseInt(startTime.substring(3));
                    long t = (min * 60L) + sec;
                    long result = TimeUnit.SECONDS.toMillis(t);

                    //Convert timedifference setdate - currentdate to milliseconds
                    GregorianCalendar currentDay=new  GregorianCalendar (Locale.GERMANY);
                    GregorianCalendar nextDay=new  GregorianCalendar (year,month,date,hour,minute,0);

                    long diff_in_ms=nextDay. getTimeInMillis()-currentDay. getTimeInMillis();

                    Intent intent = new Intent(mContext,
                            AlarmReceiver.class);
                    intent.putExtra("message", "Ny begivenhed");
                    intent.putExtra("event_name", eventName);
                    intent.putExtra("event_id", eventid);
                    intent.putExtra("end_date", endDate);
                    intent.putExtra("end_time", endTime);

                    PendingIntent mAlarmSender;

                    mAlarmSender = PendingIntent.getBroadcast(
                           mContext, 0, intent, 0);

                    AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
                    am.set(AlarmManager.RTC_WAKEUP, diff_in_ms,
                            mAlarmSender);

                    //cEventChildController.startEvent(year,month,date,hour,minute);

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
