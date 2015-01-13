package dk.projekt.bachelor.wheresmyfamily.Services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;

import java.util.List;

import static com.google.android.gms.location.LocationClient.getTriggeringGeofences;


public class ReceiveTransitionsIntentService extends IntentService
{

    public ReceiveTransitionsIntentService()
    {
        super("ReceiveTransitionsIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        // First check for errors
        if (LocationClient.hasError(intent))
        {
            // Get the error code with a static method
            int errorCode = LocationClient.getErrorCode(intent);
            // Log the error
            Log.e("ReceiveTransitionsIntentService",
                    "Location Services error: " +
                            Integer.toString(errorCode));

        }
        else
        {
            Toast.makeText(this, "ReceiveTransitionsIntentService running", Toast.LENGTH_SHORT).show();

            // Get the type of transition (entry or exit)
            int transitionType =
                    LocationClient.getGeofenceTransition(intent);
            // Test that a valid transition was reported
            if (
                    (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER) ||
                    (transitionType == Geofence.GEOFENCE_TRANSITION_EXIT)
                )
            {
                List<Geofence> triggerList = getTriggeringGeofences(intent);

                String[] triggerIds = new String[triggerList.size()]; //geofenceList?? FIXME

                for (int i = 0; i < triggerIds.length; i++)
                {
                    // Store the Id of each geofence
                    triggerIds[i] = triggerList.get(i).getRequestId();
                    if(transitionType == Geofence.GEOFENCE_TRANSITION_ENTER)
                        Toast.makeText(this, "Ankomst" + triggerIds[i], Toast.LENGTH_SHORT).show();
                    else if (transitionType == Geofence.GEOFENCE_TRANSITION_EXIT)
                        Toast.makeText(this, "Afgang" + triggerIds[i], Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(this, "Barnet er på positionen", Toast.LENGTH_SHORT).show();
                }

                /*
                * At this point,  store the IDs for further use
                * display them, or display the details associated with
                * them. FIXME
                */

            }
             else
            {
                // An invalid transition was reported
                Log.e("ReceiveTransitionsIntentService", "Geofence transition error: " +
                        Integer.toString(transitionType));
            }
        }
    }
}

