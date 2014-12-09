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

import dk.projekt.bachelor.wheresmyfamily.Controller.ChildModelController;
import dk.projekt.bachelor.wheresmyfamily.Controller.MobileServicesClient;
import dk.projekt.bachelor.wheresmyfamily.Controller.ParentModelController;
import dk.projekt.bachelor.wheresmyfamily.DataModel.Child;
import dk.projekt.bachelor.wheresmyfamily.DataModel.Parent;
import dk.projekt.bachelor.wheresmyfamily.R;
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
    TextView parentEmailTextView;
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
    // UserInfoStorage storage = new UserInfoStorage();
    ChildModelController childModelController = new ChildModelController();
    ParentModelController parentModelController = new ParentModelController();
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.activity_register_child2);

        parentNameTextView = (TextView)findViewById(R.id.parentNameTextView);
        parentNameEditText = (TextView)findViewById(R.id.parentNameInfo);

        parentPhoneTextView = (TextView) findViewById(R.id.parentPhoneTextView);
        parentPhoneEditText = (TextView) findViewById(R.id.parentPhoneInfo);

        parentEmailTextView = (TextView) findViewById(R.id.parentEmailInfo);



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

        mChildren = childModelController.getMyChildren(this);

        mParents = parentModelController.getMyParents(this);

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
                        mParents.add(new Parent(userName, userPhone, userMail, null));
                        parentModelController.setMyParents(getApplicationContext(), mParents);

                        parentNameEditText.setText(userName);
                        parentPhoneEditText.setText(userPhone);
                        parentEmailTextView.setText(userMail);
                    }
                }
                else
                {
                    Log.e(TAG, "There was an exception getting auth data: " +
                            exception.getMessage());
                }
            }
        });

        mParents = parentModelController.getMyParents(this);

        mChildren = childModelController.getMyChildren(this);

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
            // Toast.makeText(this, "Der skete en fejl i registréringen, prøv venligt igen", Toast.LENGTH_SHORT).show();
            e1.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        childModelController.setMyChildren(this, mChildren);

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
        String parentNameString = parentNameEditText.getText().toString();
        String parentPhoneString = parentPhoneEditText.getText().toString();
        String parentEmailString = parentEmailTextView.getText().toString();

        byte[] parentNameOut = parentNameString.getBytes();
        byte[] parentPhoneOut = parentPhoneString.getBytes();
        byte[] parentEmailOut = parentEmailString.getBytes();

        return new NdefMessage(
                new NdefRecord
                        (NdefRecord.TNF_MIME_MEDIA, "text/plain".getBytes(),
                                new byte[] {}, parentNameOut),
                new NdefRecord
                        (NdefRecord.TNF_MIME_MEDIA, "text/plain".getBytes(),
                                new byte[]{}, parentPhoneOut),
                new NdefRecord
                        (NdefRecord.TNF_MIME_MEDIA, "text/plain".getBytes(),
                                new byte[]{} ,parentEmailOut));
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

        mChildren.add(new Child(userName, userPhone, userMail, null));

        childModelController.setMyChildren(this, mChildren);

        /*Toast.makeText(this, "Dit barn " + userName + " Tlf. " + userPhone + " er nu registréret",
                Toast.LENGTH_SHORT).show();*/

        isNFCMessageNew = false;
    }

    @Override
    public void onNdefPushComplete(NfcEvent nfcEvent) {

        /*Toast.makeText(this, "Dit barn " + child.getName() + " Tlf. " + child.getPhone() + " er nu registréret",
                Toast.LENGTH_SHORT).show();*/
    }
}