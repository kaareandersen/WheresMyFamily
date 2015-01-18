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

import java.util.ArrayList;

import dk.projekt.bachelor.wheresmyfamily.Controller.ChildModelController;
import dk.projekt.bachelor.wheresmyfamily.Controller.MobileServicesClient;
import dk.projekt.bachelor.wheresmyfamily.Controller.ParentModelController;
import dk.projekt.bachelor.wheresmyfamily.DataModel.Child;
import dk.projekt.bachelor.wheresmyfamily.DataModel.Parent;
import dk.projekt.bachelor.wheresmyfamily.R;
import dk.projekt.bachelor.wheresmyfamily.Storage.UserInfoStorage;
import dk.projekt.bachelor.wheresmyfamily.authenticator.AuthenticationApplication;
import dk.projekt.bachelor.wheresmyfamily.helper.BaseActivity;


public class RegisterParent extends BaseActivity implements NfcAdapter.CreateNdefMessageCallback,
        NfcAdapter.OnNdefPushCompleteCallback
{
    //region Fields
    NfcAdapter nfcAdapter;
    Boolean isUserChild;
    private Boolean isNFCMessageNew = true;
    Child child = new Child();
    ArrayList<Child> myChildren = new ArrayList<Child>();
    Parent parent = new Parent();
    private final String TAG = "RegisterParent";
    TextView childPhoneInfoText;
    TextView childNameInfoText;
    TextView childEmailInfoText;
    String userName;
    String userPhone;
    String userMail;
    ArrayList<Child> mChildren = new ArrayList<Child>();
    ArrayList<Parent> mParents = new ArrayList<Parent>();
    String childrenPrefName = "myChildren";
    String parentsPrefName = "myParents";
    String childrenKey = "childrenInfo";
    String parentsKey = "parentsInfo";
    ChildModelController childModelController = new ChildModelController();
    ParentModelController parentModelController = new ParentModelController();
    UserInfoStorage storage = new UserInfoStorage();
    //endregion

    public RegisterParent(){}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_parent);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        childNameInfoText = (TextView) findViewById(R.id.childNameInfo);
        childPhoneInfoText = (TextView) findViewById(R.id.childPhoneInfo);
        childEmailInfoText = (TextView) findViewById(R.id.childEmailInfo);

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
            Toast.makeText(RegisterParent.this,
                    "Klar til registréring af forælder",
                    Toast.LENGTH_LONG).show();
            nfcAdapter.setNdefPushMessageCallback(this, this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        mChildren = childModelController.getMyChildren(this);
        storage.loadParents(this);

        AuthenticationApplication myApp = (AuthenticationApplication) getApplication();
        MobileServicesClient mobileServicesClient = myApp.getAuthService();

        //Fetch auth data (the username and phone number) from the server on load
        mobileServicesClient.getAuthData(new TableJsonQueryCallback() {
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
                    String userMail = item.getAsJsonObject().getAsJsonPrimitive(
                            "Email").getAsString();
                    isUserChild = item.getAsJsonObject().getAsJsonPrimitive(
                            "Child").getAsBoolean();

                    if(isUserChild)
                    {
                        mChildren.add(new Child(userName, userPhone, userMail, null));

                        storage.saveChildren(getApplicationContext(), mChildren);

                        childNameInfoText.setText(userName);
                        childPhoneInfoText.setText(userPhone);
                        childEmailInfoText.setText(userMail);
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

        storage.saveChildren(this, mChildren);
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        mChildren = storage.loadChildren(this);

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
        String childNameString = childNameInfoText.getText().toString();
        String childPhoneString = childPhoneInfoText.getText().toString();
        String childEmailString = childEmailInfoText.getText().toString();

        byte[] childNameOut = childNameString.getBytes();
        byte[] childPhoneOut = childPhoneString.getBytes();
        byte[] childEmailOut = childEmailString.getBytes();

        return new NdefMessage
                (
                 new NdefRecord
                         (NdefRecord.TNF_MIME_MEDIA, "text/plain".getBytes(),
                                 new byte[]{}, childNameOut),
                 new NdefRecord
                         (NdefRecord.TNF_MIME_MEDIA, "text/plain".getBytes(),
                                 new byte[]{}, childPhoneOut),
                 new NdefRecord
                         (NdefRecord.TNF_MIME_MEDIA, "text/plain".getBytes(),
                                 new byte[]{}, childEmailOut)
                );
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
        NdefRecord NdefRecord_2 = inNdefRecords[2];

        userName = new String(NdefRecord_0.getPayload());
        userPhone = new String(NdefRecord_1.getPayload());
        userMail = new String(NdefRecord_2.getPayload());

        Toast.makeText(this, "Processing intent", Toast.LENGTH_SHORT).show();

        mParents.add(new Parent(userName, userPhone, userMail, null));
        storage.saveParents(this, mParents);
        Toast.makeText(this, "Din forælder " + userName + " Tlf. " + userPhone + " er nu registréret",
                Toast.LENGTH_SHORT).show();

        isNFCMessageNew = false;
    }

    @Override
    public void onNdefPushComplete(NfcEvent nfcEvent) {
        Toast.makeText(this, "Din forælder " + parent.getName() + " Tlf. " + parent.getPhone() + " er nu registréret",
                Toast.LENGTH_SHORT).show();
    }
}
