package dk.projekt.bachelor.wheresmyfamily;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.microsoft.windowsazure.mobileservices.*;

import java.net.MalformedURLException;

public class Main extends Activity {
    private MobileServiceClient mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            mClient = new MobileServiceClient(
                    "https://wheresmyfamilymobileservice.azure-mobile.net/",
                    "pxLbxStZdNLVcRfgvOBsuROQwhFukR85",
                    this
            );
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void signUpScreen(View view) {
        Intent signUp = new Intent(this, SignUpScreen.class);
        startActivity(signUp);
    }

    public void signInScreen(View view) {
        Intent signIn = new Intent(this, SignInScreen.class);
        startActivity(signIn);
    }
}
