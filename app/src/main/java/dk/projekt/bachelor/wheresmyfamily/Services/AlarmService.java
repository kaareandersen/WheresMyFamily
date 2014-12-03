package dk.projekt.bachelor.wheresmyfamily.Services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;
import java.util.TimeZone;

import dk.projekt.bachelor.wheresmyfamily.BroadCastReceiver.AlarmReceiver;
import dk.projekt.bachelor.wheresmyfamily.activities.LoggedInParent;

public class AlarmService extends Service {
    public static final String NEW_CALENDAR_EVENT_ACTION = "new.calendar.event";
    AlarmReceiver alarmReceiver;
    Bundle bundle;
    Calendar newCalendarEventStartTime;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressWarnings("static-access")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(500);

        //Toast.makeText(this, "Your time is up, bitch", Toast.LENGTH_SHORT).show();

        return super.onStartCommand(intent, flags, startId);
    }

    /*public void startRepeatingTimer()
    {
        if(alarmReceiver != null)
        {
            alarmReceiver.SetAlarm(this);
            Toast.makeText(this, "Alarm is set", Toast.LENGTH_SHORT).show();
        }

        else
            Toast.makeText(this, "Alarm is null", Toast.LENGTH_SHORT).show();
    }

    public void cancelRepeatingTimer()
    {
        Context context = this.getApplicationContext();

        if(alarmReceiver != null)
            alarmReceiver.CancelAlarm(context);
        else
            Toast.makeText(context, "Alarm is null", Toast.LENGTH_SHORT).show();
    }

    public void startOneTimeTimer()
    {
        Context context = getApplicationContext();
        if(alarmReceiver != null)
            alarmReceiver.setOnetimeAlarm(context);
        else
            Toast.makeText(context, "Alarm is null", Toast.LENGTH_SHORT).show();
    }*/

    public void SetAlarm()
    {
        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        // intent.putExtra(ONE_TIME, Boolean.FALSE);
        intent.putExtras(bundle);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent, 0);

        //After after 5 seconds
        am.setRepeating(AlarmManager.RTC_WAKEUP, newCalendarEventStartTime.getTimeInMillis(), 100 * 1000 , pi);
        Toast.makeText(this, "hmhm", Toast.LENGTH_SHORT).show();
    }

    public void CancelAlarm()
    {
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }

    public void setOnetimeAlarm(){
        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, LoggedInParent.class);
        PendingIntent pi = PendingIntent.getBroadcast(this.getApplicationContext(), 0, intent, 0);

        am.set(AlarmManager.RTC_WAKEUP, getAlarmTime().getTimeInMillis(), pi);
        Log.e("AlarmManagerComplete", "Your time has come, bitch!!");
    }

    public Calendar getAlarmTime()
    {
        Calendar calendar = Calendar.getInstance();
        TimeZone timeZone = TimeZone.getDefault();
        calendar.setTimeZone(timeZone);

        int date;
        int month;
        int year;

        /*Intent intent = new Intent(this, AlarmService.class);
        intent.putExtras(bundle);*/
        // Bundle bundle = intent.getExtras();
        //String eventName = bundle.getString("event_name");
        // String newEventName = bundle.getString("message");
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
        calendar.set(Calendar.AM_PM,Calendar.PM);

        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.DAY_OF_MONTH, date);
        // January is month 0!!!!
        // Very important to remember to roll back the time one month!!!!
        calendar.roll(Calendar.MONTH, false);

        return calendar;
    }
}
