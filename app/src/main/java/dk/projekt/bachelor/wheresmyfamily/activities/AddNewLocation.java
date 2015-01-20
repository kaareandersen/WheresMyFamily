package dk.projekt.bachelor.wheresmyfamily.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dk.projekt.bachelor.wheresmyfamily.DataModel.WmfGeofence;
import dk.projekt.bachelor.wheresmyfamily.R;
import dk.projekt.bachelor.wheresmyfamily.Storage.GeofenceStorage;

public class AddNewLocation extends Activity  implements View.OnClickListener, AdapterView.OnItemClickListener {

    //region Fields
    Button btnAddPlace;
    EditText placeField;
    EditText radiusField;
    AutoCompleteTextView autoCompView;
    List<Address> addresses;

    private WmfGeofence wmfGeofence;
    private GeofenceStorage geofenceStorage;
    // Internal List of WmfGeofence objects
    ArrayList<WmfGeofence> geofenceList;

    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String OUT_JSON = "/json";

    private static final String API_KEY = "AIzaSyDIniWz3GEaexDmz81ytotIowpcS3CPbVI";
    private static final String LOG_TAG = "WheresMyFamily";
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_location);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        geofenceStorage = new GeofenceStorage(this);

        placeField = (EditText) findViewById(R.id.place_edit_text);
        radiusField = (EditText) findViewById(R.id.radius_edit_text);
        autoCompView = (AutoCompleteTextView) findViewById(R.id.address_autocomplete_edit_text);
        autoCompView.setAdapter(new PlacesAutoCompleteAdapter(this, R.layout.list_item));
        autoCompView.setOnItemClickListener(this);

        btnAddPlace = (Button) findViewById(R.id.btnaddplace);
    }

    @Override
    protected void onResume() {
        super.onResume();

        geofenceList = geofenceStorage.getGeofences(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        geofenceStorage.setGeofences(this, geofenceList);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_location, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        ////noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.action_help){
            helpMenu();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private ArrayList<String> autocomplete(String input)
    {
        ArrayList<String> resultList = null;

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
            sb.append("?key=" + API_KEY);
            sb.append("&components=country:dk");
            sb.append("&input=" + URLEncoder.encode(input, "utf8"));

            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error processing Places API URL", e);
            return resultList;
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error connecting to Places API", e);
            return resultList;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

            // Extract the Place descriptions from the results
            resultList = new ArrayList<String>(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) {
                resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
        }

        if(resultList != null)
            return resultList;
        else
            Toast.makeText(this, "Ukorrekt adresse, prøv venligst igen", Toast.LENGTH_SHORT).show();

        return new ArrayList<String>();
    }

    @Override
    public void onClick(View view) {
        if(view == btnAddPlace)
        {

            if(autoCompView != null && placeField != null && radiusField != null &&
                    addresses != null)
            {
                createGeofence();

                Toast.makeText(this, placeField.getText().toString() + " er føjet til favoritter",
                        Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(this, "Udfyld alle felter og prøv igen", Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

        String language = "da";
        String country = "DK";
        Locale local = new Locale(language, country);

        Geocoder geocoder = new Geocoder(this, local);

        String str = (String) adapterView.getItemAtPosition(position);
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();

        try
        {
            addresses = geocoder.getFromLocationName(str, 1);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private class PlacesAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable
    {
        private ArrayList<String> resultList;

        public PlacesAutoCompleteAdapter(Context context, int textViewResourceId){
            super(context, textViewResourceId);
        }

        @Override
        public int getCount() {
            return resultList.size();
        }

        @Override
        public String getItem(int index) {
            return resultList.get(index);
        }

        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if (constraint != null) {
                        // Retrieve the autocomplete results.
                        resultList = autocomplete(constraint.toString());

                        // Assign the data to the FilterResults
                        filterResults.values = resultList;
                        filterResults.count = resultList.size();
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, Filter.FilterResults results) {
                    if (results != null && results.count > 0) {
                        notifyDataSetChanged();
                    }
                    else {
                        notifyDataSetInvalidated();
                    }
                }};

            return filter;
        }
    }

    private void createGeofence()
    {
        wmfGeofence = new WmfGeofence(
                placeField.getText().toString(),
                addresses.get(0).getLatitude(),
                addresses.get(0).getLongitude(),
                Float.valueOf(radiusField.getText().toString()),
                com.google.android.gms.location.Geofence.NEVER_EXPIRE,
                // This geofence records both entry and exit transitions
                com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER |
                        com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_EXIT,
                false, false);

        // Store this geofence
        geofenceStorage = new GeofenceStorage(this);
        geofenceList.add(geofenceList.size(), wmfGeofence);
        geofenceStorage.setGeofences(this, geofenceList);
    }

    public void helpMenu(){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Hjælp");
        alert.setMessage("Stednavn er et vilkårligt navn, du selv vælger \n \n" +
                "Adresse - Vælg et resultat fra listen, der fremkommer under indtastning \n \n"
                + "Radius - Størrelsen på området skal vurderes og angives" );
        alert.setIcon(R.drawable.ic_action_help);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });

        alert.show();
    }
}
