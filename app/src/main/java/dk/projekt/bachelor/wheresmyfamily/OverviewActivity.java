package dk.projekt.bachelor.wheresmyfamily;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import dk.projekt.bachelor.wheresmyfamily.activities.LocationActivity;
import dk.projekt.bachelor.wheresmyfamily.activities.RegisterChild;


public class OverviewActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);
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
}
