package dk.projekt.bachelor.wheresmyfamily.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import dk.projekt.bachelor.wheresmyfamily.helper.BaseActivity;
import dk.projekt.bachelor.wheresmyfamily.R;

public class Main extends BaseActivity {

    private final String TAG = "Main";
    private Button btnLoginWithEmail;
    private Activity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mActivity = this;

        //Get UI Properties
        btnLoginWithEmail = (Button) findViewById(R.id.btnMainSignIn);

        //If user is already authenticated, bypass logging in
        if (mAuthService.isUserAuthenticated()) {
            Intent loggedInIntent = new Intent(getApplicationContext(), LoggedIn.class);
            startActivity(loggedInIntent);
        }

        //Set onclick listeners
        btnLoginWithEmail.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent customLoginIntent = new Intent(getApplicationContext(), LogInScreen.class);
                startActivity(customLoginIntent);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //
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
        Intent signUp = new Intent(this, CreateUserScreen.class);
        startActivity(signUp);
    }

    public void signInScreen(View view) {
        Intent signIn = new Intent(this, LogInScreen.class);
        startActivity(signIn);
    }
}
