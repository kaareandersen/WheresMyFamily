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
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.TableJsonQueryCallback;

import org.json.JSONException;

import java.util.ArrayList;

import dk.projekt.bachelor.wheresmyfamily.DataModel.Child;
import dk.projekt.bachelor.wheresmyfamily.DataModel.Parent;
import dk.projekt.bachelor.wheresmyfamily.R;
import dk.projekt.bachelor.wheresmyfamily.UserInfoStorage;
import dk.projekt.bachelor.wheresmyfamily.Controller.MobileServicesClient;
import dk.projekt.bachelor.wheresmyfamily.authenticator.AuthenticationApplication;
import dk.projekt.bachelor.wheresmyfamily.helper.BaseActivity;

public class RegisterChild extends BaseActivity implements NfcAdapter.CreateNdefMessageCallback,
        NfcAdapter.OnNdefPushCompleteCallback // FIXME
{

    //region Fields
    TextView parentNameTextView;
    TextView parentPhoneTextView;
    TextView parentPhoneEditText;
    TextView parentNameEditText;
    private boolean isNFCMessageNew = true;
    private final String TAG = "RegisterChild";
    Parent parent = new Parent();
    Child child = new Child();
    Boolean isUserParent;
    String userName;
    String userPhone;
    String userMail;
    NfcAdapter nfcAdapter;
    ArrayList<Child> mChildren = new ArrayList<Child>();
    ArrayList<Parent> mParents = new ArrayList<Parent>();
    String childrenPrefName = "myChildren";
    String parentsPrefName = "myParents";
    String childrenKey = "childrenInfo";
    String parentsKey = "parentsInfo";
    UserInfoStorage storage = new UserInfoStorage();
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.activity_register_child2);

        // Toast.makeText(this, "RegisterChild onCreate", Toast.LENGTH_SHORT).show();

        parentNameTextView = (TextView)findViewById(R.id.parentNameTextView);
        parentNameEditText = (TextView)findViewById(R.id.parentNameInfo);

        parentPhoneTextView = (TextView) findViewById(R.id.parentPhoneTextView);
        parentPhoneEditText = (TextView) findViewById(R.id.parentPhoneInfo);

        mChildren = storage.loadChildren(this);

        mParents = storage.loadParents(this);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if(!nfcAdapter.isEnabled())
        {
            Toast.makeText(getApplicationContext(),
                    "Please activate NFC and press Back to return to the application!",
                    Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
        }
        else
        {
            // NFC is available, set callback methods
            nfcAdapter.setNdefPushMessageCallback(this, this);
            nfcAdapter.setOnNdefPushCompleteCallback(this, this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Toast.makeText(this, "RegisterChild onResume", Toast.LENGTH_SHORT).show();

        AuthenticationApplication myApp = (AuthenticationApplication) getApplication();
        MobileServicesClient mobileServicesClient = myApp.getAuthService();

        //Fetch auth data (the username) on load
        mobileServicesClient.getAuthData(new TableJsonQueryCallback() {
            @Override
            public void onCompleted(JsonElement result, int count, Exception exception,
                                    ServiceFilterResponse response)
            {
                if (exception == null)
                {
                    JsonArray results = result.getAsJsonArray();
                    JsonElement item = results.get(0);
                    userName = item.getAsJsonObject().getAsJsonPrimitive(
                            "UserName").getAsString();
                    userPhone = item.getAsJsonObject().getAsJsonPrimitive(
                            "Phone").getAsString();
                    userMail = item.getAsJsonObject().getAsJsonPrimitive("Email").getAsString();

                    isUserParent = !item.getAsJsonObject().getAsJsonPrimitive("Child").getAsBoolean();

                    if(isUserParent)
                    {
                        mParents.add(new Parent(userName, userPhone, userMail));
                        storage.saveParents(getApplicationContext(), mParents);

                        parentNameEditText.setText(userName);
                        parentPhoneEditText.setText(userPhone);
                    }
                }
                else
                {
                    Log.e(TAG, "There was an exception getting auth data: " +
                            exception.getMessage());
                }
            }
        });

        mParents = storage.loadParents(this);

        mChildren = storage.loadChildren(this);

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

        // Toast.makeText(this, "RegisterChild onPause", Toast.LENGTH_SHORT).show();

        storage.saveChildren(this, mChildren);

        nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.register_child, menu);
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
    public NdefMessage createNdefMessage(NfcEvent nfcEvent) {
        String stringOut = parentNameEditText.getText().toString();
        String parentPhoneString = parentPhoneEditText.getText().toString();

        byte[] parentNameOut = stringOut.getBytes();
        byte[] parentPhoneOut = parentPhoneString.getBytes();

        return new NdefMessage(
                new NdefRecord
                        (NdefRecord.TNF_MIME_MEDIA, "text/plain".getBytes(),
                                new byte[] {}, parentNameOut),
                new NdefRecord
                        (NdefRecord.TNF_MIME_MEDIA, "text/plain".getBytes(),
                        new byte[]{}, parentPhoneOut));
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        isNFCMessageNew = true;
        setIntent(intent);
    }

    void processIntent(Intent intent) throws JSONException {
        Parcelable[] parcelables =
                intent.getParcelableArrayExtra(
                        NfcAdapter.EXTRA_NDEF_MESSAGES);
        NdefMessage inNdefMessage = (NdefMessage)parcelables[0];
        NdefRecord[] inNdefRecords = inNdefMessage.getRecords();
        NdefRecord NdefRecord_0 = inNdefRecords[0];
        NdefRecord NdefRecord_1 = inNdefRecords[1];
        NdefRecord NdefRecord_2 = inNdefRecords[2];
        userName = new String(NdefRecord_0.getPayload());
        userPhone = new String(NdefRecord_1.getPayload());
        userMail = new String(NdefRecord_2.getPayload());

        Toast.makeText(this, "Processing intent", Toast.LENGTH_SHORT).show();

        mChildren.add(new Child(userName, userPhone, userMail));

        storage.saveChildren(this, mChildren);

        /*Toast.makeText(this, "Dit barn " + userName + " Tlf. " + userPhone + " er nu registréret",
                Toast.LENGTH_SHORT).show();*/

        isNFCMessageNew = false;
    }

    @Override
    public void onNdefPushComplete(NfcEvent nfcEvent) {

        Toast.makeText(this, "Dit barn " + child.childName + " Tlf. " + child.childPhone + " er nu registréret",
                Toast.LENGTH_SHORT).show();
    }

    /* public void saveParent(Parent parent)
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
    }*/
}