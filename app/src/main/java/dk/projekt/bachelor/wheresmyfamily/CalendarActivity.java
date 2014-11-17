package dk.projekt.bachelor.wheresmyfamily;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CalendarView;
import android.widget.ImageButton;
import android.widget.Toast;

import dk.projekt.bachelor.wheresmyfamily.activities.LocationActivity;
import dk.projekt.bachelor.wheresmyfamily.activities.NewCalEventActivity;


public class CalendarActivity extends Activity {
    CalendarView calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        ActionBar actionBar = getActionBar();
        actionBar.setCustomView(R.layout.actionbar_top_calendar); //load your layout
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME|ActionBar.DISPLAY_SHOW_CUSTOM|ActionBar.DISPLAY_SHOW_TITLE); //show it
        actionBar.setDisplayHomeAsUpEnabled(true);

        calendar = (CalendarView) findViewById(R.id.calendar);

        // sets the first day of week according to Calendar.
        // here we set Monday as the first day of the Calendar
        calendar.setFirstDayOfWeek(2);

        //sets the listener to be notified upon selected date change.
        calendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            //show the selected date as a toast
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int day) {
                Toast.makeText(getApplicationContext(), day + "/" + month + "/" + year, Toast.LENGTH_LONG).show();
            }
        });

        ImageButton button = (ImageButton) findViewById(R.id.action_newevent);
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                //add event to calendar
                Intent loggedInIntent = new Intent(getApplicationContext(), NewCalEventActivity.class);
                startActivity(loggedInIntent);
            }

        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.calendar, menu);
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
            Intent register = new Intent(this, OverviewActivity.class);
            startActivity(register);
            return true;
        }
        if (id == R.id.action_calendar) {

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
