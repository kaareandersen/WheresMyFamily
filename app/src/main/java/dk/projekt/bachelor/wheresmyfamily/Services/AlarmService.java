package dk.projekt.bachelor.wheresmyfamily.Services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Calendar;

import dk.projekt.bachelor.wheresmyfamily.BroadCastReceiver.AlarmReceiver;
import dk.projekt.bachelor.wheresmyfamily.Controller.ChildModelController;
import dk.projekt.bachelor.wheresmyfamily.Controller.PushNotificationController;
import dk.projekt.bachelor.wheresmyfamily.DataModel.Child;

public class AlarmService extends Service {
    public static final String NEW_CALENDAR_EVENT_ACTION = "new.calendar.event";
    AlarmReceiver alarmReceiver;
    Bundle bundle;
    Calendar newCalendarEventStartTime;
    ArrayList<Child> myChildren = new ArrayList<Child>();
    LatLng currentPosition;
    LatLng peripheralPosition;
    int earthRadius = 6378137;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressWarnings("static-access")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        askForLocation();

        /*
        * Use an approximation to compute the position
        * The radius of earth in meters: 6378137
        * lat = lat0 + (180/pi)*(dy/6378137)
        * lon = lon0 + (180/pi)*(dx/6378137)/cos(lat0)*/

        /*LatLng peripheralPosition = new LatLng
                (currentPosition.latitude + (180/Math.PI)*(25/earthRadius),
                (180/Math.PI)*(25/earthRadius)/Math.cos(Math.PI/180.0*currentPosition.latitude));*/
        Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(500);

        return super.onStartCommand(intent, flags, startId);
    }

    public void askForLocation(){

        ChildModelController childModelController = new ChildModelController();
        myChildren = childModelController.getMyChildren(this);

        PushNotificationController pushNotificationController = new PushNotificationController(this);
        pushNotificationController.askForLocationFromChild(childModelController.getCurrentChild().getEmail());

    }
}
