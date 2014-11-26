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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import dk.projekt.bachelor.wheresmyfamily.DataModel.Child;
import dk.projekt.bachelor.wheresmyfamily.R;
import dk.projekt.bachelor.wheresmyfamily.UserInfoStorage;


public class OverviewActivity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener
{
    ImageButton btnTackPic;
    ImageView ivThumbnailPhoto;
    Bitmap bitMap;
    static int TAKE_PICTURE = 1;
    Child child = new Child();

    ArrayList<Child> myChildren = new ArrayList<Child>();
    UserInfoStorage storage = new UserInfoStorage();
    Child currentChild = new Child();

    TextView childNameTextView;
    TextView childPhoneTextView;
    TextView childPositionTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);

        myChildren = storage.loadChildren(this);

        btnTackPic = (ImageButton) findViewById(R.id.btnTakePic);

        ivThumbnailPhoto = (ImageView) findViewById(R.id.ivThumbnailPhoto);
        childNameTextView = (TextView) findViewById(R.id.overview_child_name);
        childPhoneTextView = (TextView) findViewById(R.id.overview_child_phone);
        childPositionTextView = (TextView) findViewById(R.id.overview_child_position);

        // add onclick listener to the button
        btnTackPic.setOnClickListener(this);

        ImageButton button = (ImageButton) findViewById(R.id.callchild);
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

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
        childPositionTextView.setText(currentChild.getEmail());
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
            ivThumbnailPhoto.setImageBitmap(bitMap);

        }
    }
}
