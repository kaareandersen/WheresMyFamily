package dk.projekt.bachelor.wheresmyfamily.Controller;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.gson.JsonObject;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.TableJsonOperationCallback;

import java.util.ArrayList;
import java.util.Calendar;

import dk.projekt.bachelor.wheresmyfamily.BroadCastReceiver.AlarmReceiver;
import dk.projekt.bachelor.wheresmyfamily.DataModel.WmfGeofence;
import dk.projekt.bachelor.wheresmyfamily.Services.ReceiveTransitionsIntentService;
import dk.projekt.bachelor.wheresmyfamily.activities.LoggedInChild;

/**
 * Created by KaareAndersen on 20/11/14.
 */
public class PushNotificationController {

    private final static String TAG = "PushNotificationController";
    private static Context mContext;
    private static MobileServicesClient mMobileServicesClient;
    private static LoggedInChild loggedInChild;
    private static CEventChildController cEventChildController;
    private ArrayList<WmfGeofence> currentGeofences;
    private WmfGeofenceController wmfGeofenceController;

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

                    currentGeofences = new ArrayList<WmfGeofence>();

                    int id = 10;
                    //Get values for event from Azure tables
                    String eventid = jsonObject.getAsJsonPrimitive("id").getAsString();
                    String eventName = jsonObject.getAsJsonPrimitive("EventName").getAsString();
                    String startDate = jsonObject.getAsJsonPrimitive("StartDate").getAsString();
                    String startTime = jsonObject.getAsJsonPrimitive("StartTime").getAsString();
                    String geofenceID = jsonObject.getAsJsonPrimitive("GeofenceId").getAsString();
                    String endDate = jsonObject.getAsJsonPrimitive("EndDate").getAsString();
                    String endTime = jsonObject.getAsJsonPrimitive("EndTime").getAsString();
                    String latitude = jsonObject.getAsJsonPrimitive("Latitude").getAsString();
                    String longitude = jsonObject.getAsJsonPrimitive("Longitude").getAsString();
                    String radius = jsonObject.getAsJsonPrimitive("Radius").getAsString();
                    String expiration = jsonObject.getAsJsonPrimitive("Expiration").getAsString();

                    WmfGeofence temp = new WmfGeofence();
                    temp.setGeofenceId(geofenceID);
                    temp.setRadius(Float.parseFloat(radius));
                    temp.setLatitude(Double.parseDouble(latitude));
                    temp.setLongitude(Double.parseDouble(longitude));
                    temp.setTransitionType(Geofence.GEOFENCE_TRANSITION_ENTER);
                    temp.setExpirationDuration(Long.parseLong(expiration));

                    currentGeofences.add(temp);

                    wmfGeofenceController = new WmfGeofenceController();
                    wmfGeofenceController.setMyGeofences(mContext, currentGeofences);

                    // loggedInChild.addGeofences();
                    loggedInChild = LoggedInChild.instance;
                    loggedInChild.addGeofences();

                    //Convert date/month/year to int
                    String[] sepDate = startDate.split("-");
                    int date = Integer.parseInt(sepDate[0]);
                    int month = Integer.parseInt(sepDate[1]);
                    int year = Integer.parseInt(sepDate[2]);

                    //Convert minute/hour to int
                    String[] sepTime = startTime.split(":");
                    int hour = Integer.parseInt(sepTime[0]);
                    int minute = Integer.parseInt(sepTime[1]);

                    //loggedInChild = new LoggedInChild();
                    //loggedInChild.AlarmHandler(hour, minute, month, year, date);
                    AlarmHandler(hour, minute, month,year,date);
                    /*
                    Calendar c = Calendar.getInstance();
                    int currentyear = c.get(Calendar.YEAR);
                    int currentmonth = c.get(Calendar.MONTH);
                    int currentday = c.get(Calendar.DATE);
                    int currenthour = c.get(Calendar.HOUR_OF_DAY);
                    int currentminute = c.get(Calendar.MINUTE);
                    int currentseconds = c.get(Calendar.SECOND);

                    //Convert timedifference setdate - currentdate to milliseconds
                    GregorianCalendar currentDay=new  GregorianCalendar (currentyear, currentmonth, currentday, currenthour, currentminute, 0);
                    GregorianCalendar nextDay=new  GregorianCalendar (year,month,date,hour,minute,0);

                    long diff_in_ms=nextDay. getTimeInMillis()-currentDay. getTimeInMillis();
                    long timemilli = nextDay.  getTimeInMillis();

                    Intent intent = new Intent(mContext,
                            AlarmReceiver.class);
                    intent.putExtra("message", "Ny begivenhed");
                    intent.putExtra("event_name", eventName);
                    intent.putExtra("event_id", eventid);
                    intent.putExtra("end_date", endDate);
                    intent.putExtra("end_time", endTime);

                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.DAY_OF_MONTH, date);
                    calendar.set(Calendar.HOUR_OF_DAY, hour);
                    calendar.set(Calendar.MINUTE, minute);
                    calendar.set(Calendar.SECOND, 0);*/



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
                if (exception == null) {
                    Toast.makeText(mContext, "pushnotifikationcontroller send location", Toast.LENGTH_LONG).show();
                } else {
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
                if (exception == null) {
                    Toast.makeText(mContext, "Lokation Sendt!", Toast.LENGTH_LONG).show();
                } else {
                    Log.e(TAG, "There was an exception sending location from Child: " + exception.getMessage());
                }
            }
        });
    }

    public void AlarmHandler(int startHour, int startMinute, int startMonth, int startYear, int startDate)
    {
        Intent intent = new Intent(mContext, AlarmReceiver.class);
        Intent intent1 = new Intent(mContext, ReceiveTransitionsIntentService.class);
        mContext.startService(intent1);

        Calendar calendarStart = Calendar.getInstance();
        calendarStart.setTimeInMillis(System.currentTimeMillis());

        calendarStart.set(Calendar.HOUR_OF_DAY, startHour);
        calendarStart.set(Calendar.MINUTE, startMinute);
        calendarStart.set(Calendar.SECOND, 0);

        // January is month 0!!!!
        // Very important to remember to roll back the time one month!!!!
        --startMonth;

        calendarStart.set(Calendar.MONTH, startMonth);
        calendarStart.set(Calendar.YEAR, startYear);
        calendarStart.set(Calendar.DAY_OF_MONTH, startDate);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext,0, intent, 0);

        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendarStart.getTimeInMillis(), pendingIntent);
    }
}
