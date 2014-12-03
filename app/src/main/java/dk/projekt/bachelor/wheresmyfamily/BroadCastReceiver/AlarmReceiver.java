package dk.projekt.bachelor.wheresmyfamily.BroadCastReceiver;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.Calendar;

import dk.projekt.bachelor.wheresmyfamily.Services.AlarmService;

/**
 * Created by KaareAndersen on 28/11/14.
 */
public class AlarmReceiver extends BroadcastReceiver{
    public static final int NOTIFICATION_ID = 10;
    private static final String ONE_TIME = "onetime";
    private NotificationManager mNotificationManager;

    public static final String NEW_CALENDAR_EVENT_ACTION = "new.calendar.event";
    Bundle bundle;

    public AlarmReceiver(){}

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("AlarmTime", "Alarm is fired");

        Intent intent1 = new Intent(context, AlarmService.class);
        context.startService(intent1);
    }

    public void setAlarm(Context context, Bundle bundle){
        Log.d("Carbon", "Alrm SET !!");

        // get a Calendar object with current time

        Calendar calendar = Calendar.getInstance();
        int date;
        int month;
        int year;
        // add 30 seconds to the calendar object
        // cal.add(Calendar.SECOND, 30);
        // Bundle newBundle = bundle;
        String startDate = bundle.getString("start_date");
        String startTime = bundle.getString("start_time");
        String endDate = bundle.getString("end_date");
        String endTime = bundle.getString("end_time");
        //int eventId = Integer.parseInt(bundle.getString("event_id"));

        //Convert date/month/year to int
        String[] sepDate = startDate.split("-");
        date = Integer.parseInt(sepDate[0]);
        month = Integer.parseInt(sepDate[1]);
        year = Integer.parseInt(sepDate[2]);

        //Convert minute/hour to int
        String[] sepTime = startTime.split(":");

        int hour = Integer.parseInt(sepTime[0]);
        int minute = Integer.parseInt(sepTime[1]);
        // int seconds = Integer.parseInt(sepDate[2]);

        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.AM_PM, Calendar.PM);

        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.DAY_OF_MONTH, date);
        // January is month 0!!!!
        // Very important to remember to roll back the time one month!!!!
        // calendar.roll(Calendar.MONTH, -1);
    }
}
