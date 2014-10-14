package dk.projekt.bachelor.wheresmyfamily;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;


public class ParentInfo extends Activity implements View.OnClickListener {

    Parent parent = new Parent();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_info);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        Button submit = (Button) findViewById(R.id.btnsubmit);
        submit.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        saveParent(parent);
    }

    @Override
    protected void onPause() {
        super.onPause();

        saveParent(parent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        parent = loadParent();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.parent_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view)
    {
        String name = ((TextView) findViewById(R.id.parentNameInput)).getText().toString();
        String phone = ((TextView) findViewById(R.id.parentPhoneInput)).getText().toString();

        parent = new Parent(name, phone);

        Toast.makeText(getApplicationContext(), "Forælder oprettet med navn: " + name +
                " og telefonnummer: " + phone, Toast.LENGTH_SHORT).show();

    }

    private void saveParent(Parent _parent)
    {
        try {
            InternalStorage.writeObject(this, "Parent", parent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Parent loadParent()
    {
        Parent retVal = null;

        try
        {
            retVal = (Parent) InternalStorage.readObject(this, "Parent");
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }

        if(retVal == null)
            return new Parent();
        else
            return retVal;
    }
}
