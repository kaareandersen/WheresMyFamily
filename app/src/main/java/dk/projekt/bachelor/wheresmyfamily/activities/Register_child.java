package dk.projekt.bachelor.wheresmyfamily.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.TableJsonQueryCallback;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import dk.projekt.bachelor.wheresmyfamily.Child;
import dk.projekt.bachelor.wheresmyfamily.InternalStorage;
import dk.projekt.bachelor.wheresmyfamily.Parent;
import dk.projekt.bachelor.wheresmyfamily.R;
import dk.projekt.bachelor.wheresmyfamily.activities.LoggedIn;
import dk.projekt.bachelor.wheresmyfamily.authenticator.AuthService;
import dk.projekt.bachelor.wheresmyfamily.authenticator.AuthenticationApplication;

public class Register_child extends Activity implements CreateNdefMessageCallback, NfcAdapter.OnNdefPushCompleteCallback{

    ArrayList<Child> children = new ArrayList<Child>();

    private NfcAdapter nfcAdapter;
    String currentChildName;
    TextView parentName;
    TextView parentPhone;
    private final String TAG = "Register_child";
    String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_child);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if(!nfcAdapter.isEnabled())
        {
            Toast.makeText(getApplicationContext(), "Please activate NFC and press Back to return to the application!", Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
        }
        else
        {
            Toast.makeText(Register_child.this,
                    "Set Callback(s)",
                    Toast.LENGTH_LONG).show();

            nfcAdapter.setNdefPushMessageCallback(this, this);
            nfcAdapter.setOnNdefPushCompleteCallback(this, this);


        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.register_child, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        try
        {
            Intent intent = getIntent();
            String action = intent.getAction();

            if(action.equals(NfcAdapter.ACTION_NDEF_DISCOVERED))
                processIntent(getIntent());
        }
        catch(Exception e)
        {
            finish();
        }
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


        String stringOut = parentName.getText().toString();
        byte[] byteOut = stringOut.getBytes();

        NdefRecord record = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, "text/plain".getBytes(),
                new byte[] {}, byteOut);

        NdefMessage message = new NdefMessage(record);

        return message;

        /*NdefMessage msg = new NdefMessage(
                new NdefRecord[]
                        {
                                NdefRecord.createExternal(
                                        "app/dk.projekt.bachelor.wheresmyfamily",
                                        "ParentName", parent.name.getBytes()),
                                NdefRecord.createExternal(
                                        "app/dk.projekt.bachelor.wheresmyfamily",
                                        "ParentPhone", parent.phone.getBytes()),
                                NdefRecord.createExternal(
                                        "app/dk.projekt.bachelor.wheresmyfamily",
                                        "ChildName", currentChildName.getBytes())
                        }
        );*/


        // return msg;
    }

    private ArrayList<Child> loadChildren()
    {
        ArrayList<Child> retVal = null;

        try
        {
            retVal = (ArrayList<Child>) InternalStorage.readObject(this, "Children");
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        catch(ClassNotFoundException e)
        {
            e.printStackTrace();
        }

        if(retVal == null)
            return new ArrayList<Child>();
        else
            return retVal;
    }

    public Parent loadParent()
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
        catch(IOException e)
        {
            e.printStackTrace();
        }
        catch(ClassNotFoundException e)
        {
            e.printStackTrace();
        }

        if(retVal == null)
            return new Parent();
        else
            return retVal;
    }

    private void saveChildren(ArrayList<Child> children)
    {
        try
        {
            InternalStorage.writeObject(this, "Children", children);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // onResume bliver kaldt efter denne funktion for at håndtere intent

        // isNdefMessageNew = true;
        setIntent(intent);
    }

    void processIntent(Intent intent) {
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
                NfcAdapter.EXTRA_NDEF_MESSAGES);
        NdefMessage inNdefMessage = (NdefMessage) rawMsgs[0];
        NdefRecord[] inNdefRecords = inNdefMessage.getRecords();
        NdefRecord NdefRecord_0 = inNdefRecords[0];
        String inMsg = new String(NdefRecord_0.getPayload());
        parentName.setText(inMsg);
    }


    public void openRegisterDialog(final String deviceId)
    {
        final Dialog regDialog = new Dialog(this);
        regDialog.setTitle("Registrér barn");
        regDialog.setContentView(R.layout.register_child_dialog);

        Button registerChildButton = (Button) regDialog.findViewById(R.id.register_child_button);
        EditText deviceView = (EditText) regDialog.findViewById(R.id.device_id);
        deviceView.setText(deviceId);
        deviceView.setEnabled(false);

        registerChildButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String name = ((EditText)
                        regDialog.findViewById(R.id.register_name)).getText().toString();

                if(name.isEmpty())
                    Toast.makeText(getApplicationContext(), "Feltet 'Navn' må ikke være tomt",
                            Toast.LENGTH_SHORT).show();

                String phone = ((EditText)
                        regDialog.findViewById(R.id.register_phone)).getText().toString();

                if(phone.isEmpty())
                    Toast.makeText(getApplicationContext(),
                            "Feltet 'telefonnummer må ikke være tomt", Toast.LENGTH_SHORT).show();

                currentChildName = name;
                children.add(new Child(name, phone, deviceId));
                Parent parent1 = new Parent(getIntent().getData().getUserInfo(), "test");

                regDialog.dismiss();
                Toast.makeText(getApplicationContext(), "Dit barn " + name + " er nu registréret",
                        Toast.LENGTH_SHORT).show();
            }

        });

        regDialog.show();
    }

    @Override
    public void onNdefPushComplete(NfcEvent nfcEvent) {
        final String eventString = "Dit barn NAVN HER er nu registreret";
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),
                        eventString,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    public void childView(View v)
    {
        Intent childList = new Intent(this, LoggedIn.class);
        startActivity(childList);
    }

}
