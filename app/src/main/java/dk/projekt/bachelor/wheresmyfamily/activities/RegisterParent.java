package dk.projekt.bachelor.wheresmyfamily.activities;

import android.app.ActionBar;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.TableJsonQueryCallback;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import dk.projekt.bachelor.wheresmyfamily.DataModel.Child;
import dk.projekt.bachelor.wheresmyfamily.InternalStorage;
import dk.projekt.bachelor.wheresmyfamily.DataModel.Parent;
import dk.projekt.bachelor.wheresmyfamily.R;
import dk.projekt.bachelor.wheresmyfamily.authenticator.AuthService;
import dk.projekt.bachelor.wheresmyfamily.authenticator.AuthenticationApplication;
import dk.projekt.bachelor.wheresmyfamily.helper.BaseActivity;


public class RegisterParent extends BaseActivity implements NfcAdapter.CreateNdefMessageCallback
{

    //region Fields
    NfcAdapter nfcAdapter;
    Boolean isUserChild;
    private Boolean isNFCMessageNew = true;
    Child child = new Child();
    ArrayList<Child> myChildren = new ArrayList<Child>();
    Parent parent = new Parent();
    private final String TAG = "AuthService";
    EditText childPhoneEditText;
    EditText childNameEditText;
    String userName;
    String userPhone;
    //endregion

    public RegisterParent(){}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_parent);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        Toast.makeText(this, "RegisterParent OnCreate", Toast.LENGTH_SHORT).show();

        childNameEditText = (EditText) findViewById(R.id.childNameInfo);
        childPhoneEditText = (EditText) findViewById(R.id.childPhoneInfo);

        myChildren = loadChildren();

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if(!nfcAdapter.isEnabled())
        {
            Toast.makeText(getApplicationContext(),
                    "Please activate NFC and press Back to return to the application!",
                    Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
        }
        else{
            Toast.makeText(RegisterParent.this,
                    "Klar til registréring af forælder",
                    Toast.LENGTH_LONG).show();
            nfcAdapter.setNdefPushMessageCallback(this, this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Toast.makeText(this, "RegisterParent OnResume", Toast.LENGTH_SHORT).show();

        myChildren = loadChildren();

        AuthenticationApplication myApp = (AuthenticationApplication) getApplication();
        AuthService authService = myApp.getAuthService();

        //Fetch auth data (the username and phone number) from the server on load
        authService.getAuthData(new TableJsonQueryCallback() {
            @Override
            public void onCompleted(JsonElement result, int count, Exception exception,
                                    ServiceFilterResponse response)
            {
                if (exception == null)
                {
                    JsonArray results = result.getAsJsonArray();
                    JsonElement item = results.get(0);
                    String userName = item.getAsJsonObject().getAsJsonPrimitive(
                            "UserName").getAsString();
                    String userPhone = item.getAsJsonObject().getAsJsonPrimitive(
                            "Phone").getAsString();
                    String userMail = item.getAsJsonObject().getAsJsonPrimitive("Email").getAsString();

                    isUserChild = item.getAsJsonObject().getAsJsonPrimitive("Child").getAsBoolean();

                    if(isUserChild)
                    {
                        myChildren.add(new Child(userName, userPhone));

                        saveChildren(myChildren);

                        childNameEditText.setText(userName);
                        childPhoneEditText.setText(userPhone);
                    }
                }
                else
                {
                    Log.e(TAG, "There was an exception getting auth data: " +
                            exception.getMessage());
                }
            }
        });

        try
        {
            String n = getIntent().getAction();
            String e = NfcAdapter.ACTION_NDEF_DISCOVERED;

            if(e.equals(n) && isNFCMessageNew)
            {
                processIntent(getIntent());
            }
        }
        catch (Exception e1)
        {
            e1.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        Toast.makeText(this, "RegisterParent OnPause", Toast.LENGTH_SHORT).show();

        saveChildren(myChildren);
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        Toast.makeText(this, "RegisterParent OnRestart", Toast.LENGTH_SHORT).show();

        myChildren = loadChildren();
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        isNFCMessageNew = true;
        setIntent(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.register_parent, menu);
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
    public NdefMessage createNdefMessage(NfcEvent nfcEvent)
    {
        String stringOut = childNameEditText.getText().toString();
        String childPhoneString = childPhoneEditText.getText().toString();

        byte[] childNameOut = stringOut.getBytes();
        byte[] childPhoneOut = childPhoneString.getBytes();

        /*return new NdefMessage
                (
                new NdefRecord[]
                        {
                        NdefRecord.createExternal("app/dk.projekt.bachelor.wheresmyfamily", "ChildName", childNameOut),
                        NdefRecord.createExternal("app/dk.projekt.bachelor.wheresmyfamily", "ChildPhone", childPhoneOut)
                        }
                );*/
        return new NdefMessage
                (
                 new NdefRecord
                         (NdefRecord.TNF_MIME_MEDIA, "text/plain".getBytes(),
                                new byte[] {}, childNameOut),
                new NdefRecord
                        (NdefRecord.TNF_MIME_MEDIA, "text/plain".getBytes(),
                                new byte[]{}, childPhoneOut)
                );

        /*return new NdefMessage
                (
                        new NdefRecord[]
                                {
                                        NdefRecord.createMime("app/dk.projekt.bachelor.wheresmyfamily", childNameOut),
                                        NdefRecord.createMime("app/dk.projekt.bachelor.wheresmyfamily", childPhoneOut)
                                        // NdefRecord.createApplicationRecord("dk.projekt.bachelor.wheresmyfamily")
                                }
                );*/

    }

    void processIntent(Intent intent)
    {
        Parcelable[] parcelables =
                intent.getParcelableArrayExtra(
                        NfcAdapter.EXTRA_NDEF_MESSAGES);
        NdefMessage inNdefMessage = (NdefMessage)parcelables[0];
        NdefRecord[] inNdefRecords = inNdefMessage.getRecords();
        NdefRecord NdefRecord_0 = inNdefRecords[0];
        NdefRecord NdefRecord_1 = inNdefRecords[1];

        userName = new String(NdefRecord_0.getPayload());
        userPhone = new String(NdefRecord_1.getPayload());

        Toast.makeText(this, "Processing intent", Toast.LENGTH_SHORT).show();

        parent = new Parent(userName, userPhone);

        saveParent(parent);

        isNFCMessageNew = false;
    }

    public void saveParent(Parent parent)
    {
        try
        {
            InternalStorage.writeObject(this, "Parent", parent);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public Parent loadParent()
    {
        Parent retVal = null;

        try
        {
            retVal = (Parent) InternalStorage.readObject(this, "Parent");
        }
        catch(FileNotFoundException fe)
        {
            fe.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (ClassNotFoundException ce)
        {
            ce.printStackTrace();
        }

        return retVal == null ? new Parent() : retVal;
    }

    public void saveChildren(ArrayList<Child> myChildren)
    {
        try
        {
            InternalStorage.writeObject(this, "Children", myChildren);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Child> loadChildren()
    {
        ArrayList<Child> retVal = null;

        try
        {
            retVal = (ArrayList<Child>) InternalStorage.readObject(this, "Children");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return retVal == null ? new ArrayList<Child>() : retVal;
    }
}
