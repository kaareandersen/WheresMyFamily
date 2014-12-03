package dk.projekt.bachelor.wheresmyfamily.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import dk.projekt.bachelor.wheresmyfamily.Controller.PictUtil;
import dk.projekt.bachelor.wheresmyfamily.DataModel.Child;
import dk.projekt.bachelor.wheresmyfamily.R;
import dk.projekt.bachelor.wheresmyfamily.Storage.UserInfoStorage;


public class OverviewActivity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener
{
    ImageButton btnTackPic;
    ImageView ivThumbnailPhoto;
    Bitmap bitMap;
    static int TAKE_PICTURE = 1;
    Child child = new Child();
    ListView listView ;

    ArrayList<Child> myChildren = new ArrayList<Child>();
    UserInfoStorage storage = new UserInfoStorage();
    Child currentChild = new Child();

    TextView childNameTextView;
    TextView childPhoneTextView;

    private String filename = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);

        myChildren = storage.loadChildren(this);

        btnTackPic = (ImageButton) findViewById(R.id.btnTakePic);

        ivThumbnailPhoto = (ImageView) findViewById(R.id.ivThumbnailPhoto);
        childNameTextView = (TextView) findViewById(R.id.overview_child_name);
        childPhoneTextView = (TextView) findViewById(R.id.overview_child_phone);

        // add onclick listener to the button
        btnTackPic.setOnClickListener(this);

        listEvents();

        ImageButton button = (ImageButton) findViewById(R.id.callchild);
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Child temp = new Child();
                child = temp.getCurrentChild(myChildren);

                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + child.getPhone()));
                startActivity(callIntent);
            }

        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        myChildren = storage.loadChildren(this);

        Child temp = new Child();
        currentChild = temp.getCurrentChild(myChildren);

        childNameTextView.setText(currentChild.getName());
        childPhoneTextView.setText(currentChild.getPhone());
        //set filename for bmp
        filename = currentChild.getPhone();
        // Load bmp
        ivThumbnailPhoto.setImageBitmap(PictUtil.loadFromCacheFile(filename));
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

    public void listEvents(){
        // Get ListView object from xml
        listView = (ListView) findViewById(R.id.list);

        // Defined Array values to show in ListView
        String[] values = new String[] { "Android List View",
                "Adapter implementation",
                "Simple List View In Android",
                "Create List View Android",
                "Android Example",
                "List View Source Code",
                "List View Array Adapter",
                "Android Example List View"
        };

        // Define a new Adapter
        // First parameter - Context
        // Second parameter - Layout for the row
        // Third parameter - ID of the TextView to which the data is written
        // Forth - the Array of data

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, values);


        // Assign adapter to ListView
        listView.setAdapter(adapter);

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
}
