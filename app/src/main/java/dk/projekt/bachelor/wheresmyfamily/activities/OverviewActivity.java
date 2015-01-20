package dk.projekt.bachelor.wheresmyfamily.activities;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import dk.projekt.bachelor.wheresmyfamily.Controller.ChildModelController;
import dk.projekt.bachelor.wheresmyfamily.Controller.EventController;
import dk.projekt.bachelor.wheresmyfamily.Controller.PictUtil;
import dk.projekt.bachelor.wheresmyfamily.DataModel.Child;
import dk.projekt.bachelor.wheresmyfamily.DataModel.Event;
import dk.projekt.bachelor.wheresmyfamily.R;


public class OverviewActivity extends ListActivity implements View.OnClickListener, AdapterView.OnItemClickListener
{
    ImageButton btnTackPic;
    ImageView ivThumbnailPhoto;
    Bitmap bitMap;
    static int TAKE_PICTURE = 1;
    Child child = new Child();
    ListView listView ;

    ArrayList<Child> myChildren = new ArrayList<Child>();
    ChildModelController childModelController = new ChildModelController();

    Child currentChild = new Child();

    TextView childNameTextView;
    TextView childPhoneTextView;

    private String filename = null;

    private DrawerLayout drawerLayout;
    private ListView drawerListView;
    private String[] navigationDrawerList;

    private ArrayList<Event> currentEvents;
    private EventController eventController;
    private EventAdapter eventAdapter;
    ListView myList;
    private Runnable viewChild;
    private ProgressDialog m_ProgressDialog = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);

        myChildren = childModelController.getMyChildren(this);

        eventController = new EventController();
        currentEvents = eventController.getAllEvents(this);

        btnTackPic = (ImageButton) findViewById(R.id.btnTakePic);

        ivThumbnailPhoto = (ImageView) findViewById(R.id.ivThumbnailPhoto);
        childNameTextView = (TextView) findViewById(R.id.overview_child_name);
        childPhoneTextView = (TextView) findViewById(R.id.overview_child_phone);

        // add onclick listener to the button
        btnTackPic.setOnClickListener(this);

        listEvents();

        eventAdapter = new EventAdapter(this, R.layout.event_row, currentEvents);
        myList = (ListView)findViewById(android.R.id.list);
        myList.setAdapter(eventAdapter);

        viewChild = new Runnable() {
            @Override
            public void run() {
                try {
                    getChild();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        Thread thread = new Thread(null, viewChild, "MagenToBackground");
        thread.start();
        m_ProgressDialog = ProgressDialog.show(OverviewActivity.this, "Vent venligst...",
                "Henter data ...", true);

        ImageButton button = (ImageButton) findViewById(R.id.callchild);
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                child = childModelController.getCurrentChild();

                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + child.getPhone()));
                startActivity(callIntent);
            }

        });

        /*drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        drawerListView = (ListView) findViewById(R.id.left_drawer);
        navigationDrawerList = getResources().getStringArray(R.array.navigation_drawer_array);
        drawerListView.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, navigationDrawerList));
        drawerListView.setOnItemClickListener(this);*/
    }

    @Override
    protected void onResume() {
        super.onResume();

        myChildren = childModelController.getMyChildren(this);

        currentChild = childModelController.getCurrentChild();

        currentEvents = eventController.getAllEvents(this);

        // Refresh the list of events
        myList.setAdapter(new EventAdapter(this, R.layout.event_row, currentEvents));

        childNameTextView.setText(currentChild.getName());
        childPhoneTextView.setText(currentChild.getPhone());
        //set filename for bmp
        filename = currentChild.getPhone();
        // Load bmp
        ivThumbnailPhoto.setImageBitmap(PictUtil.loadFromCacheFile(filename));
    }

    @Override
    protected void onPause() {
        super.onPause();

        eventController.setMyEvents(this, currentEvents);
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


        if(view == btnTackPic) {
            // create intent with ACTION_IMAGE_CAPTURE action
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            // start camera activity
            startActivityForResult(intent, TAKE_PICTURE);
        }
    }



    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id)
    {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (requestCode == TAKE_PICTURE && resultCode== RESULT_OK && intent != null){
            // get bundle
            Bundle extras = intent.getExtras();

            // get bitmap
            bitMap = (Bitmap) extras.get("data");
            // save picture to sharedPreff
            PictUtil.saveToCacheFile(bitMap, filename);
        }
    }

    public void listEvents()
    {
        // Get ListView object from xml
        listView = (ListView) findViewById(android.R.id.list);

        /*// Defined Array values to show in ListView
        String[] values = new String[] { "Skole d. 21.12.14 kl. 8.00 - 15.00",
                "Fodbold d. 21.12.14 kl. 16.00 - 17.00",
                "Skole d. 22.12.14 kl. 8.00 - 14.00",
                "Tandlæge d. 22.12.14 kl. 15.00 - 15.30",
        };*/

        // Define a new Adapter
        // First parameter - Context
        // Second parameter - Layout for the row
        // Third parameter - ID of the TextView to which the data is written
        // Forth - the Array of data

        String[] values = new String[currentEvents.size()];

        for(int i = 0; i < currentEvents.size(); i++)
        {
            values[i] = currentEvents.get(i).toString();
        }

        /*ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, values);


        // Assign adapter to ListView
        listView.setAdapter(adapter);*/

        // ListView Item Click Listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                // ListView Clicked item index
                int itemPosition     = position;

                // ListView Clicked item value
                String  itemValue    = (String) listView.getItemAtPosition(position);

                // Show Alert
                Toast.makeText(getApplicationContext(),
                        "Position :"+itemPosition+"  ListItem : " +itemValue , Toast.LENGTH_LONG)
                        .show();
            }

        });
    }

    private Runnable returnRes = new Runnable() {
        @Override
        public void run() {
            if (currentEvents != null && currentEvents.size() > 0){
                eventAdapter.notifyDataSetChanged();
                for (int i = 0; i < currentEvents.size(); i++)
                    eventAdapter.add(currentEvents.get(i));
            }
            m_ProgressDialog.dismiss();
            eventAdapter.notifyDataSetChanged();
        }
    };

    private void getChild() throws FileNotFoundException, IOException {
        try
        {
            Thread.sleep(500);
            Log.i("ARRAY", "" + currentEvents.size());
        } catch (Exception e){
            Log.e("BACKGROUND_PROC", e.getMessage());
        }

        runOnUiThread(returnRes);
    }

    private class EventAdapter extends ArrayAdapter<Event>
    {
        public EventAdapter(Context context, int textViewResourceId, ArrayList<Event> items)
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
                v = vi.inflate(R.layout.event_row, null);
            }

            Event c = null;
            c = currentEvents.get(position);

            if (c != null)
            {
                TextView eventName = (TextView) v.findViewById(R.id.event_name_textview);
                TextView startDate = (TextView) v.findViewById(R.id.event_startdate_textview);
                TextView startTime = (TextView) v.findViewById(R.id.event_starttime_textview);
                TextView endDate = (TextView) v.findViewById(R.id.event_enddate_textview);
                TextView endTime = (TextView) v.findViewById(R.id.event_endtime_textview);

                if (eventName != null)
                {
                    eventName.setText("Begivenhed: " + c.getEventName());
                }
                if (startDate != null)
                {
                    startDate.setText("Dato: " + c.getStartDate() + " til ");
                }
                if (endDate != null)
                {
                    endDate.setText(c.getEndDate());
                }
                if (startTime != null)
                {
                    startTime.setText("Fra " + c.getStartTime() + " til ");
                }
                if (endTime != null)
                {
                    endTime.setText(c.getEndTime());
                }
            }

            return v;
        }
    }
}
