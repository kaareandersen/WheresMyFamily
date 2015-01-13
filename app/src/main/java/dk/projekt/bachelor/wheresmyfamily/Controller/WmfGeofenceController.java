package dk.projekt.bachelor.wheresmyfamily.Controller;

import android.content.Context;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;

import java.util.ArrayList;

import dk.projekt.bachelor.wheresmyfamily.DataModel.WmfGeofence;
import dk.projekt.bachelor.wheresmyfamily.Storage.GeofenceStorage;

/**
 * Created by Tommy on 03-12-2014.
 */
public class WmfGeofenceController {

    ArrayList<Geofence> myGeofences = new ArrayList<Geofence>();
    ArrayList<Geofence> activeGeofences;
    WmfGeofence currentGeofence;
    GeofenceStorage geofenceStorage;
    ArrayList<WmfGeofence> wmfGeofences;
    WmfGeofence wmfGeofence;

    public WmfGeofenceController() {}

    public ArrayList<WmfGeofence> getAllGeofences(Context context)
    {
        geofenceStorage = new GeofenceStorage(context);
        wmfGeofences = geofenceStorage.getGeofences(context);

        if(wmfGeofences.size() > 0)
        {
            return wmfGeofences;
        }
        else
            return new ArrayList<WmfGeofence>();
    }

    public void setMyGeofences(Context context, ArrayList<WmfGeofence> _myGeofences)
    {
        geofenceStorage = new GeofenceStorage(context);
        geofenceStorage.setGeofences(context, _myGeofences);
    }

    public void setActiveGeofences(Context context, ArrayList<Geofence> _myGeofences)
    {
        geofenceStorage = new GeofenceStorage(context);
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

    public WmfGeofence getCurrentGeofence(Context context)
    {
        currentGeofence = new WmfGeofence();

        if(myGeofences.size() > 0)
        {
            for(int i = 0; i < wmfGeofences.size(); i++)
            {
                if(wmfGeofences.get(i).getIsActive())
                    currentGeofence = wmfGeofences.get(i);
            }
        }

        if(currentGeofence != null)
            return currentGeofence;
        else
            return new WmfGeofence();
    }

    public void noCurrentGeofence(ArrayList<WmfGeofence> _myGeofences)
    {
        for(int i = 0; i <_myGeofences.size(); i++)
        {
            _myGeofences.get(i).setIsCurrent(false);
        }
    }
}
