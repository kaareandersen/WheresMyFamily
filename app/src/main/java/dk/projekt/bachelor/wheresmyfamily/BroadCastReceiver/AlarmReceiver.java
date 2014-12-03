package dk.projekt.bachelor.wheresmyfamily.BroadCastReceiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import dk.projekt.bachelor.wheresmyfamily.Controller.CEventChildController;
import dk.projekt.bachelor.wheresmyfamily.R;
import dk.projekt.bachelor.wheresmyfamily.Services.CalEventService;
import dk.projekt.bachelor.wheresmyfamily.activities.LoggedInParent;

/**
 * Created by KaareAndersen on 28/11/14.
 */
public class AlarmReceiver extends BroadcastReceiver{
    public static final int NOTIFICATION_ID = 10;
    private NotificationManager mNotificationManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            Bundle bundle = intent.getExtras();
            String eventName = bundle.getString("event_name");
            String newEventName = bundle.getString("message");
            String endDate = bundle.getString("end_date");
            String endTime = bundle.getString("end_time");
            //int eventId = Integer.parseInt(bundle.getString("event_id"));

            mNotificationManager = (NotificationManager)
                    context.getSystemService(Context.NOTIFICATION_SERVICE);
            CharSequence text = eventName + " "+ newEventName;

            Toast.makeText(context, "BOOOOOOOOOM fra RECEIVER", Toast.LENGTH_LONG).show();

            Intent newIntent = new Intent(context,
                    CEventChildController.class);
            //newIntent.putExtra("event_id", eventId);
            newIntent.putExtra("end_date", endDate);
            newIntent.putExtra("end_time", endTime);

            PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                    newIntent, 0);

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(R.drawable.family_white)
                            .setContentTitle("Wheres My Family")
                            .setStyle(new NotificationCompat.BigTextStyle()
                                    .bigText(text))
                            .setContentText(text);

            mBuilder.setContentIntent(contentIntent);
            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());

        } catch (Exception e) {
            Toast
                    .makeText(
                            context,
                            "There was an error somewhere, but we still received an alarm",
                            Toast.LENGTH_SHORT).show();
            e.printStackTrace();

        }
    }
}
