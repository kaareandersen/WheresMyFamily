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

import dk.projekt.bachelor.wheresmyfamily.Controller.PushNotificationController;
import dk.projekt.bachelor.wheresmyfamily.R;
import dk.projekt.bachelor.wheresmyfamily.activities.LocationActivity;
import dk.projekt.bachelor.wheresmyfamily.activities.LoggedInChild;
import dk.projekt.bachelor.wheresmyfamily.activities.LoggedInParent;


/**
 * Created by KaareAndersen on 30/10/14.
 */
public class MyHandler extends NotificationsHandler {
    private final String TAG = "MyHandler";
    public static int NOTIFICATION_ID = 0;
    private NotificationManager mNotificationManager;
    Context ctx;
    private MyHandler mHandler;
    int id = 0;

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
            NOTIFICATION_ID = 1;

            pushNotificationController.getEventId(eventID);
        }
        if(sepMessage[0].equals("GetLocation")){
            newMessage = "Lokations anmodning modtaget";
            NOTIFICATION_ID = 2;

            LoggedInChild loggedinchild = LoggedInChild.instance;
            loggedinchild.getAndPushLocation();
        }
        if (sepMessage[0].equals("ReceiveLocation")){
            // Bundle[{msg=ReceiveLoc ation:Loca tion[fused 56,147154,10,150219 acc=24 et=+7d15h33m53s517ms alt=80.02072416373049 vel=1.6083485 bear=25.0], from=911215571794, collapse_key=do_not_collapse}]
            String location = sepMessage[1];
            newMessage = "Lokation modtaget";
            NOTIFICATION_ID = 3;

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

        //if (NOTIFICATION_ID == 3) {
            PendingIntent contentIntent = PendingIntent.getActivity(ctx, id,
                    new Intent(ctx, LoggedInParent.class), 0);

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(ctx)
                            .setSmallIcon(R.drawable.family_white)
                            .setContentTitle("Wheres My Family")
                            .setStyle(new NotificationCompat.BigTextStyle()
                                    .bigText(msg))
                            .setContentText(msg);

            mBuilder.setContentIntent(contentIntent);
            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
            Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
       /* }
        else if (NOTIFICATION_ID == 1 || NOTIFICATION_ID == 2){
            PendingIntent contentIntent = PendingIntent.getActivity(ctx, id,
                    new Intent(ctx, LoggedInChild.class), 0);

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(ctx)
                            .setSmallIcon(R.drawable.family_white)
                            .setContentTitle("Wheres My Family")
                            .setStyle(new NotificationCompat.BigTextStyle()
                                    .bigText(msg))
                            .setContentText(msg);

            mBuilder.setContentIntent(contentIntent);
            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
            Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
        }*/
    }
}