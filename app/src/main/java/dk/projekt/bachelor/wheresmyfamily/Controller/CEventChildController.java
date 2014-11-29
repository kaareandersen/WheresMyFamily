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

    public void startEvent() {

    }
}
