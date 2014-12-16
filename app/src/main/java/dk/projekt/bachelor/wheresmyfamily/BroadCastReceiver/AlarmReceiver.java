package dk.projekt.bachelor.wheresmyfamily.BroadCastReceiver;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

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
}
