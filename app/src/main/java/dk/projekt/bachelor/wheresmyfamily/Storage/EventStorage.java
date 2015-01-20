package dk.projekt.bachelor.wheresmyfamily.Storage;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dk.projekt.bachelor.wheresmyfamily.DataModel.Event;

/**
 * Created by Tommy on 20-01-2015.
 */
public class EventStorage {

    // The SharedPreferences object in which geofences are stored
    private SharedPreferences eventPrefs;
    // The name of the SharedPreferences
    private static final String SHARED_PREFERENCES = "eventPreferences";

    private static final String PREFIX = "json";
    public static final String EVENT_PREFS_NAME = "EVENT_PREFS";
    public static final String EVENT_FAVORITES = "EVENT_FAVORITES";

    public EventStorage()
    {
        // eventPrefs = context.getSharedPreferences(EVENT_PREFS_NAME, Context.MODE_PRIVATE);

        super();
    }

    // These eight methods are used for maintaining favorites.
    public void saveEvent(Context context, List<Event> events) {
        SharedPreferences settings;
        SharedPreferences.Editor editor;

        settings = context.getSharedPreferences(EVENT_PREFS_NAME,
                Context.MODE_PRIVATE);
        editor = settings.edit();

        Gson gson = new Gson();
        String jsonEventFavorites = gson.toJson(events);

        editor.putString(EVENT_FAVORITES, jsonEventFavorites);

        editor.commit();
    }

    public ArrayList<Event> loadEvent(Context context) {
        SharedPreferences settings;
        List<Event> events;

        settings = context.getSharedPreferences(EVENT_PREFS_NAME,
                Context.MODE_PRIVATE);

        if (!settings.contains(EVENT_FAVORITES)) {
            return new ArrayList<Event>();
        } else {
            String jsonFavorites = settings.getString(EVENT_FAVORITES, null);
            Gson gson = new Gson();
            Event[] favoriteItems = gson.fromJson(jsonFavorites,
                    Event[].class);

            events = Arrays.asList(favoriteItems);
            events = new ArrayList<Event>(events);
            return (ArrayList<Event>) events;
        }
    }


}
