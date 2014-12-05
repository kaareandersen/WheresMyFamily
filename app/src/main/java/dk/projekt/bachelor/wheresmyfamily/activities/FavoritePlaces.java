package dk.projekt.bachelor.wheresmyfamily.activities;

import android.app.ActionBar;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import dk.projekt.bachelor.wheresmyfamily.Storage.GeofenceStorage;
import dk.projekt.bachelor.wheresmyfamily.R;
import dk.projekt.bachelor.wheresmyfamily.DataModel.WmfGeofence;

public class FavoritePlaces extends ListActivity {

    //region Fields
    private ListView m_list;
    GeofenceStorage geofenceStorage;
    WmfGeofence wmfGeofence;
    private PlaceAdapter geofenceAdapter;
    private ArrayList<WmfGeofence> myGeofences;
    private Runnable viewChild;
    ListView myList;
    private final String TAG = "Mine steder";
    private TextView geoFencename;
    private EditText parentName;
    private ProgressDialog progressDialog = null;
    //endregion



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_favorite_places);

        myGeofences = new ArrayList<WmfGeofence>();
        geofenceStorage = new GeofenceStorage(this);

        getListView().setOnItemClickListener(listlistener);

        this.geofenceAdapter = new PlaceAdapter(this, R.layout.geofence_favorites_row, myGeofences);
        myList = (ListView)findViewById(android.R.id.list);
        myList.setAdapter(geofenceAdapter);

        viewChild = new Runnable() {
            @Override
            public void run() {
                try {
                    getGeofenceWait();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        Thread thread = new Thread(null, viewChild, "MagenToBackground");
        thread.start();
        progressDialog = ProgressDialog.show(FavoritePlaces.this, "Please wait...", "Retrieving data ...", true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // geofenceAdapter.notifyDataSetChanged();
        myGeofences = geofenceStorage.getGeofences(this);

        myList.setAdapter(new PlaceAdapter(this, R.layout.geofence_favorites_row, myGeofences));
    }

    @Override
    protected void onPause() {
        super.onPause();

        geofenceStorage.setGeofences(this, myGeofences);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_favorite_places, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add_place) {
            Intent intent = new Intent(this, AddNewLocation.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //Clicklistener for click on listview
    private AdapterView.OnItemClickListener listlistener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView parent, View arg1, int position,long arg3) {
            Intent childClick = new Intent(FavoritePlaces.this, OverviewActivity.class);
            startActivity(childClick);
        }
    };

    private class PlaceAdapter extends ArrayAdapter<WmfGeofence>
    {
        public PlaceAdapter(Context context, int textViewResourceId, ArrayList<WmfGeofence> items)
        {
            super(context, textViewResourceId, items);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            View v = convertView;
            if(v == null)
            {
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.geofence_favorites_row, null);
            }

            /*Child c = null;
            c = m_My_children.get(position);*/
            WmfGeofence wmfGeofence = null;
            wmfGeofence = myGeofences.get(position);

            if (wmfGeofence != null)
            {
                TextView tt = (TextView) v.findViewById(R.id.geofence_name_text_view);

                if (tt != null)
                    tt.setText("Name: " + wmfGeofence.getGeofenceId());
            }

            return v;
        }
    }

    private Runnable returnRes = new Runnable() {
        @Override
        public void run() {
            if (myGeofences != null && myGeofences.size() > 0){
                geofenceAdapter.notifyDataSetChanged();
                for (int i = 0; i < myGeofences.size(); i++)
                    geofenceAdapter.add(myGeofences.get(i));
            }
            progressDialog.dismiss();
            geofenceAdapter.notifyDataSetChanged();
        }
    };

    private void getGeofenceWait() throws FileNotFoundException, IOException {
        try {
            myGeofences = geofenceStorage.getGeofences(this);

            Thread.sleep(2000);
            Log.i("ARRAY", "" + myGeofences.size());
        } catch (Exception e) {
            if(e.getMessage() != null)
                Log.e("BACKGROUND_PROC", e.getMessage());
        }
        runOnUiThread(returnRes);
    }
}
