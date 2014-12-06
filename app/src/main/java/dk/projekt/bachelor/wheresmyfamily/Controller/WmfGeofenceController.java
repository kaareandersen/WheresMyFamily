package dk.projekt.bachelor.wheresmyfamily.Controller;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;

import java.util.ArrayList;

import dk.projekt.bachelor.wheresmyfamily.DataModel.WmfGeofence;
import dk.projekt.bachelor.wheresmyfamily.Services.ReceiveTransitionsIntentService;
import dk.projekt.bachelor.wheresmyfamily.Storage.GeofenceStorage;

/**
 * Created by Tommy on 03-12-2014.
 */
public class WmfGeofenceController {

    ArrayList<Geofence> myGeofences = new ArrayList<Geofence>();
    ArrayList<Geofence> activeGeofences;
    GeofenceStorage geofenceStorage;
    ArrayList<WmfGeofence> wmfGeofences;
    WmfGeofence wmfGeofence;

    public WmfGeofenceController() {}

    public ArrayList<Geofence> getAllGeofences(Context context)
    {
        wmfGeofences = geofenceStorage.getGeofences(context);

        for(int i = 0; i < wmfGeofences.size(); i++)
        {
            wmfGeofence = wmfGeofences.get(i);
            myGeofences.add(i, wmfGeofence.toGeofence());
        }

        return myGeofences;
    }

    public void setActiveGeofences(Context context, ArrayList<Geofence> _myGeofences)
    {
        wmfGeofences = geofenceStorage.getGeofences(context);

        for(int j = 0; j < wmfGeofences.size(); j++)
        {
            wmfGeofence = wmfGeofences.get(j);
            _myGeofences.add(wmfGeofence.toGeofence());
        }
    }

    public ArrayList<Geofence> getActiveGeofence(Context context)
    {
        activeGeofences = new ArrayList<Geofence>();

        if(myGeofences.size() > 0)
        {
            for(int i = 0; i < wmfGeofences.size(); i++)
            {
                if(wmfGeofences.get(i).getIsActive())
                    activeGeofences.add(i, myGeofences.get(i));
            }
        }

        if(activeGeofences != null)
            return activeGeofences;
        else
        {
            Toast.makeText(context, "Der er ingen aktive begivenheder", Toast.LENGTH_SHORT).show();
        }

        return null;
    }

    /*
     * Create a PendingIntent that triggers an IntentService in your
     * app when a geofence transition occurs.
     */
    public PendingIntent getTransitionPendingIntent(Context context)
    {
        // Create an explicit Intent
        Intent intent = new Intent(context, ReceiveTransitionsIntentService.class);
        /*
         * Return the PendingIntent
         */
        return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
