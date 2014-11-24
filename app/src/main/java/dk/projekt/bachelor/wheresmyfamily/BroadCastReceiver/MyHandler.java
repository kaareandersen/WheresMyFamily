package dk.projekt.bachelor.wheresmyfamily.BroadCastReceiver;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.microsoft.windowsazure.notifications.NotificationsHandler;

import dk.projekt.bachelor.wheresmyfamily.PushNotificationController;
import dk.projekt.bachelor.wheresmyfamily.R;
import dk.projekt.bachelor.wheresmyfamily.activities.LocationActivity;
import dk.projekt.bachelor.wheresmyfamily.activities.LoggedInChild;
import dk.projekt.bachelor.wheresmyfamily.activities.LoggedInParent;


/**
 * Created by KaareAndersen on 30/10/14.
 */
public class MyHandler extends NotificationsHandler {
    private final String TAG = "MyHandler";
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    Context ctx;
    private MyHandler mHandler;

    @Override
    public void onReceive(Context context, Bundle bundle) {
        ctx = context;
        mHandler = this;
        PushNotificationController pushNotificationController = new PushNotificationController(ctx);
        String newMessage = null;
        String nhMessage = bundle.getString("msg");
        String[] sepMessage = nhMessage.split(":");

        if(sepMessage[0].equals("NewEvent")){
            newMessage = "Du har modtaget en ny kalender begivenhed";
            String eventID = sepMessage[1];

            //LoggedInChild loggedinchild = LoggedInChild.instance;
            //loggedinchild.getEventId(eventID);

            pushNotificationController.getEventId(eventID);

        }
        if(sepMessage[0].equals("GetLocation")){
            newMessage = "Lokations anmodning modtaget";

            LoggedInChild loggedinchild = LoggedInChild.instance;
            loggedinchild.getAndPushLocation();
        }
        if (sepMessage[0].equals("ReceiveLocation")){
            String location = sepMessage[1];
            newMessage = "Lokation modtaget";

            LocationActivity locationactivity = LocationActivity.instance;
            locationactivity.receiveLocation(location);
        }


        Log.d(TAG, "onReceive");
        sendNotification(newMessage);
    }

    private void sendNotification(String msg) {
        Log.d(TAG, "sendNotification");
        mNotificationManager = (NotificationManager)
                ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0,
                new Intent(ctx, LoggedInParent.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(ctx)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Wheres My Family")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg);
        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(ctx, msg, duration);
        toast.show();
    }

}