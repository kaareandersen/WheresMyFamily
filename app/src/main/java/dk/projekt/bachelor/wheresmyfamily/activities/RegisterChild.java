package dk.projekt.bachelor.wheresmyfamily.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import dk.projekt.bachelor.wheresmyfamily.R;
import dk.projekt.bachelor.wheresmyfamily.authenticator.AuthService;

public class RegisterChild extends Activity implements NfcAdapter.CreateNdefMessageCallback
{

    TextView parentNameTextView;
    TextView parentPhoneTextView;
    EditText parentPhoneEdit;
    EditText parentName;
    private boolean isNFCMessageNew = true;
    Parent parent = new Parent();

    NfcAdapter nfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_child2);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        parentNameTextView = (TextView)findViewById(R.id.parentNameTextView);
        parentName = (EditText)findViewById(R.id.parentNameInfo);

        parentPhoneTextView = (TextView) findViewById(R.id.parentPhoneTextView);
        parentPhoneEdit = (EditText) findViewById(R.id.parentPhoneInfo);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if(nfcAdapter==null)
        {
            Toast.makeText(RegisterChild.this,
                    "nfcAdapter==null, no NFC adapter exists",
                    Toast.LENGTH_LONG).show();
        }
        else
        {
        if(!nfcAdapter.isEnabled())
        {
            Toast.makeText(getApplicationContext(), "Please activate NFC and press Back to return to the application!", Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
        }
        else{
            Toast.makeText(RegisterChild.this,
                    "Set Callback(s)",
                    Toast.LENGTH_LONG).show();
            nfcAdapter.setNdefPushMessageCallback(this, this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        parent = loadParent();

        AuthService auth = new AuthService(this);

        parent = loadParent();

        parent.name = auth.getUserId();

        EditText pName = (EditText) findViewById(R.id.parentNameInfo);
        EditText pPhone = (EditText) findViewById(R.id.parentPhoneInfo);

        pName.setText(parent.name);
        pPhone.setText(parent.phone);

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

        saveParent(parent);

        nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onStart() {
        super.onStart();



       /* EditText pName = (EditText) findViewById(R.id.parentNameInfo);
        // EditText pPhone = (EditText) findViewById(R.id.parentPhoneInput);

        pName.setText(parent.name);
        // pPhone.setText(parent.phone);*/
    }

    @Override
    protected void onStop() {
        super.onStop();

        saveParent(parent);

        // nfcAdapter.disableForegroundDispatch(this);
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
    protected void onNewIntent(Intent intent)
    {
        isNFCMessageNew = true;
        setIntent(intent);
    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent nfcEvent) {
        String stringOut = parentName.getText().toString();
        String parentPhoneString = parentPhoneEdit.getText().toString();

        byte[] parentNameOut = stringOut.getBytes();
        byte[] parentPhoneOut = parentPhoneString.getBytes();

        NdefRecord ndefRecordOut = new NdefRecord(
                NdefRecord.TNF_MIME_MEDIA,
                "text/plain".getBytes(),
                new byte[] {},
                parentNameOut);

        NdefMessage ndefMessageout = new NdefMessage(
                new NdefRecord
                        (NdefRecord.TNF_MIME_MEDIA, "text/plain".getBytes(),
                                new byte[] {}, parentNameOut),
                new NdefRecord
                        (NdefRecord.TNF_MIME_MEDIA, "text/plain".getBytes(),
                        new byte[]{}, parentPhoneOut));

        return ndefMessageout;
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
        String pName = new String(NdefRecord_0.getPayload());
        String phoneNumber = new String(NdefRecord_1.getPayload());


        parentName.setText(pName);
        parentPhoneEdit.setText(phoneNumber);


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
             parentName.setText(retVal.name);
             parentPhoneEdit.setText(retVal.phone);

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

         if(retVal == null)
             return new Parent();
         else
             return retVal;
     }
}
