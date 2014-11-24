package dk.projekt.bachelor.wheresmyfamily.activities;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;

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

import dk.projekt.bachelor.wheresmyfamily.R;


public class OverviewActivity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener
{
    EditText placeField;
    AutoCompleteTextView autoCompView;

    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String OUT_JSON = "/json";

    private static final String API_KEY = "AIzaSyDIniWz3GEaexDmz81ytotIowpcS3CPbVI";
    private static final String LOG_TAG = "WheresMyFamily";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);

        placeField = (EditText) findViewById(R.id.place_edit_text);
        autoCompView = (AutoCompleteTextView) findViewById(R.id.address_autocomplete_edit_text);
        autoCompView.setAdapter(new PlacesAutoCompleteAdapter(this, R.layout.list_item));
        autoCompView.setOnItemClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.overview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_overview) {
            return true;
        }
        if (id == R.id.action_calendar) {
            Intent register = new Intent(this, CalendarActivity.class);
            startActivity(register);
            return true;
        }
        if (id == R.id.action_map) {
            Intent register = new Intent(this, LocationActivity.class);
            startActivity(register);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(this, LocationActivity.class);
        intent.putExtra("Place", placeField.getText());

        LocationActivity locationActivity = new LocationActivity();
        PendingIntent pendingIntent = PendingIntent.getActivity(this, ConnectionResult.SUCCESS,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        locationActivity.createGeofences(pendingIntent);
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

        return resultList;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id)
    {
        List<Address> addresses;
        String language = "da";
        String country = "DK";
        // String variant = "_";
        Locale local = new Locale(language, country);

        /*Locale[] locale = Locale.getAvailableLocales();

        for (int i = 0; i < locale.length; i++)
        {
            String temp = locale[i].toString();
            String vari = locale[i].getVariant();
            // Log.e("Locale", temp);
            Log.e("Variant", vari);
        }*/

        Geocoder geocoder = new Geocoder(this, local);

        String str = (String) adapterView.getItemAtPosition(position);
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();

        try
        {
            /*double lowerLeftLatitude = 0;
            double lowerLeftLongitude = 0;
            double upperRightLatitude= 0;
            double upperRightLongitude= 0;*/
            // Geocoding = get GPS coordinates from address
            addresses = geocoder.getFromLocationName(str, 1);
            if(addresses.size() > 0)
            {
                double latitude = addresses.get(0).getLatitude();
                double longitude = addresses.get(0).getLongitude();
                String postalCode = addresses.get(0).getPostalCode();
                String a = addresses.get(0).getAddressLine(0);
                String b = addresses.get(0).getAdminArea();
                String c = addresses.get(0).getCountryCode();
                String d = addresses.get(0).getCountryName();
                String e = addresses.get(0).getFeatureName();
                String f = addresses.get(0).getLocality();
                String g = addresses.get(0).getPhone();
                String h = addresses.get(0).getPostalCode();
                String i = addresses.get(0).getPremises();
                String j = addresses.get(0).getSubAdminArea();
                String k = addresses.get(0).getSubLocality();
                String l = addresses.get(0).getSubThoroughfare();
                String m = addresses.get(0).getThoroughfare();
                String n = addresses.get(0).getUrl();
                String o = addresses.get(0).getLocale().toString();
                Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
            }
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
}
