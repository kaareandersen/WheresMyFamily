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

public class RegisterChild extends Activity implements NfcAdapter.CreateNdefMessageCallback
         {

    TextView parentNameTextView;
    TextView parentPhoneTextView;
    EditText parentPhoneEdit;
    EditText parentName;
    private boolean isNFCMessageNew = true;

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

        /*Intent intent = getIntent().setAction("action_addChild");
        String action = intent.getAction();
        if(action.equals(NfcAdapter.ACTION_NDEF_DISCOVERED) && isNFCMessageNew){
            Parcelable[] parcelables =
                    intent.getParcelableArrayExtra(
                            NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage inNdefMessage = (NdefMessage)parcelables[0];
            NdefRecord[] inNdefRecords = inNdefMessage.getRecords();
            NdefRecord NdefRecord_0 = inNdefRecords[0];
            String inMsg = new String(NdefRecord_0.getPayload());


            parentName.setText(inMsg);
        }*/

        try {
            String n = getIntent().getAction();
            String e = NfcAdapter.ACTION_NDEF_DISCOVERED;

            if(e.equals(n) && isNFCMessageNew)
            {
                processIntent(getIntent());
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

     @Override
     protected void onPause() {
         super.onPause();

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

        NdefMessage ndefMessageout = new NdefMessage
                (new NdefRecord
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


}
