package dk.projekt.bachelor.wheresmyfamily.Services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import dk.projekt.bachelor.wheresmyfamily.R;
import dk.projekt.bachelor.wheresmyfamily.activities.LoggedInChild;
import dk.projekt.bachelor.wheresmyfamily.activities.LoggedInParent;

/**
 * Created by KaareAndersen on 27/11/14.
 */
public class CalEventService extends IntentService {
    private static final int NOTIFICATION_ID = 1;

    private final static String TAG = "CEventChildController";
    Context ctx;

    public CalEventService(Context context){
        super("CalEventService");
        ctx = context;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String msg = "begivenhed startet";
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0,
                new Intent(ctx, LoggedInChild.class), 0);
        long when = System.currentTimeMillis();         // notification time
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(ctx)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Wheres My Family")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg)
                        .setWhen(when);
        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(ctx, msg, duration);
        toast.show();
    }
}
