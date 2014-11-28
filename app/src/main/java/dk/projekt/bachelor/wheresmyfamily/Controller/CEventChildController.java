package dk.projekt.bachelor.wheresmyfamily.Controller;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.security.Permission;
import java.util.Calendar;

import dk.projekt.bachelor.wheresmyfamily.Services.CalEventService;

/**
 * Created by KaareAndersen on 26/11/14.
 */
public class CEventChildController {

    private final static String TAG = "CEventChildController";
    private static Context mContext;

    public CEventChildController(Context context){
        mContext = context;
    }

    public void startEvent(int year, int month, int date, int hour, int minute) {
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(mContext.ALARM_SERVICE);
        Calendar calendar1 = Calendar.getInstance();
        calendar1.set(year, month, date, hour, minute);
        long when = calendar1.getTimeInMillis();
        Intent intent = new Intent(mContext, CalEventService.class);
        PendingIntent pendingIntent = PendingIntent.getService(mContext, 0, intent, 0);
        alarmManager.set(AlarmManager.RTC, when, pendingIntent);
    }
}
