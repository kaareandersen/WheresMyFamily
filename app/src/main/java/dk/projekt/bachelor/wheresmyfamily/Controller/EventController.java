package dk.projekt.bachelor.wheresmyfamily.Controller;

import android.content.Context;

import java.util.ArrayList;

import dk.projekt.bachelor.wheresmyfamily.DataModel.Event;
import dk.projekt.bachelor.wheresmyfamily.Storage.EventStorage;

/**
 * Created by Tommy on 20-01-2015.
 */
public class EventController {

    ArrayList<Event> myEvents = new ArrayList<Event>();
    ArrayList<Event> activeGeofences;
    Event currentEvent;
    EventStorage eventStorage = new EventStorage();
    ArrayList<Event> events = new ArrayList<Event>();
    Event event;

    public EventController() {}

    public ArrayList<Event> getAllEvents(Context context)
    {
        events = eventStorage.loadEvent(context);

        return events;
    }

    public void setMyEvents(Context context, ArrayList<Event> _myEvents)
    {
        eventStorage.saveEvent(context, _myEvents);
    }

    /*public void setActiveGeofences(Context context, ArrayList<Geofence> _myGeofences)
    {
        eventStorage = new GeofenceStorage(context);
        events = eventStorage.loadEvent(context);

        for(int j = 0; j < events.size(); j++)
        {
            event = events.get(j);
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
    }*/
}
