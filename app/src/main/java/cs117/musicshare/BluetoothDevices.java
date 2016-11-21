package cs117.musicshare;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Button;
import android.widget.TextView;
import android.app.ProgressDialog;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import android.content.DialogInterface;

import java.util.List;
import java.util.Set;
import android.widget.Toast;
import android.graphics.Color;

public class BluetoothDevices extends AppCompatActivity {
    private TextView mbt_status;
    private Button mScan_button;
    private Button on_button;
    private ListView btViewList;

    //shows progress menu
    private ProgressDialog progress_dialog;
    private ProgressDialog bt_dialog;
    //create list to store devices
    private ArrayList<BluetoothDevice> device_list = new ArrayList<BluetoothDevice>();
    //create an instance of Bluetooth
    private BluetoothAdapter mBluetoothAdapter;
    ArrayAdapter<BluetoothDevice> pairedDeviceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_devices);

        //link button to variable
        mScan_button = (Button) findViewById(R.id.button_scan);
        mbt_status = (TextView) findViewById(R.id.btList);
        btViewList= (ListView)findViewById(R.id.bluelist);
        on_button = (Button) findViewById(R.id.buttonOn);

        //get bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        progress_dialog = new ProgressDialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        bt_dialog = new ProgressDialog(this);

        //setup bt dialog
        bt_dialog.setMessage("Enabling bluetooth to allow functionality..");
        bt_dialog.setCancelable(false);
        bt_dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                show_Message("Turning ON bluetooth..");
                //Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                //startActivityForResult(enableBT, 1);
                mBluetoothAdapter.enable();
                showOn();
            }
        });
        //set up loading dialog
        Window window = progress_dialog.getWindow();
        window.setLayout(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);
        progress_dialog.setMessage("Scanning for devices...");
        progress_dialog.setCancelable(false);
        progress_dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Stop", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                mBluetoothAdapter.cancelDiscovery();
            }
        });
        //if user clicks "scan" start discovery of devices
        mScan_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mBluetoothAdapter.startDiscovery();
            }
        });
        //turn on/off bluetooth
        on_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBluetoothAdapter.isEnabled()) {
                    mBluetoothAdapter.disable();

                    showOff();
                } else {
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intent, 1000);
                }
            }
        });
        //if Bluetooth is on
        if (!mBluetoothAdapter.isEnabled()) {
            showOn();
            showPaired();
        }
        else{
            showOff();
            View view = this.getWindow().getDecorView();
            view.setBackgroundColor(0xFFFF69B4);
            mScan_button.setEnabled(false);
            showPaired();
        }
        IntentFilter filter = new IntentFilter();

        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        registerReceiver(mReceiver, filter);
    }
    private void showPaired(){
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        //show no devices message
        if (pairedDevices == null || pairedDevices.size() == 0) {
            findViewById(R.id.bluelist).setVisibility(View.INVISIBLE);
            mbt_status.setText("No devices are currently paired.");
        }
        //or create a new intent for the device list
        else {
            findViewById(R.id.bluelist).setVisibility(View.VISIBLE);
            mbt_status.setText("Devices currently paired.");
            ArrayList<BluetoothDevice> list = new ArrayList<BluetoothDevice>();
            list.addAll(pairedDevices);

            pairedDeviceAdapter = new ArrayAdapter<BluetoothDevice>(this,
                    android.R.layout.simple_list_item_1, list);
            btViewList.setAdapter(pairedDeviceAdapter);

            btViewList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                //if user clicks on paired devices
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    final BluetoothDevice device =
                            (BluetoothDevice)parent.getItemAtPosition(position);
                    final String device_name = device.getName();
                    new AlertDialog.Builder(BluetoothDevices.this)
                            .setTitle("Device information")
                            .setMessage("Name: " + device.getName() + "\n"
                                    + "Address: " + device.getAddress() + "\n"
                                    + "BondState: " + device.getBondState() + "\n"
                                    + "BluetoothClass: " + device.getBluetoothClass() )
                            //Dismiss message or unpair
                            .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                }
                            })
                            .setPositiveButton("unpair", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    try {
                                        Method m = device.getClass()
                                                .getMethod("removeBond", (Class[]) null);
                                        m.invoke(device, (Object[]) null);
                                        finish();
                                        startActivity(getIntent());
                                        show_Message("Unpaired device: " + device_name);
                                    } catch (Exception e) {
                                        Log.e("fail", e.getMessage());
                                    }
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }});
        }
    }

    //if bluetooth is on
    private void showOn() {
        bt_dialog.dismiss();
        View view = this.getWindow().getDecorView();
        view.setBackgroundColor(0xFFADD8E6);
        mScan_button.setEnabled(true);
    }
    //if bluetooth is off
    private void showOff() {
        bt_dialog.dismiss();
        View view = this.getWindow().getDecorView();
        view.setBackgroundColor(0xFFffb2b2);
        mScan_button.setEnabled(true);
    }
    //want to clear bluetooth instance when exiting
    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    //Pop up a message to the user
    private void show_Message(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    //main bluetooth adapter tasks
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            //if bluetooth adapter is activated
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                if (state == BluetoothAdapter.STATE_ON) {
                    show_Message("Bluetooth enabled, enjoy the music!");
                    showOn();
                }
            }
            //if bluetooth starts discovering, show message for discovery and init new device list
            else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                device_list = new ArrayList<BluetoothDevice>();
                progress_dialog.show();
            }
            //if bluetooth finishes discovery, show all discovered items
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                progress_dialog.dismiss();
                Intent newIntent = new Intent(BluetoothDevices.this, DeviceList.class);
                newIntent.putParcelableArrayListExtra("device.list", device_list);
                startActivity(newIntent);
                //mBluetoothAdapter.startDiscovery();
            }
            // when discovering, show message of discovery and add to list
            else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                device_list.add(device);
                show_Message("Found device " + device.getName());
            }
        }
    };

    /**
     *
     * @param obj the data object (song info, list of songs, or song file)
     * @return the equivalent in a byte stream
     * @throws IOException
     */
    private byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        ObjectOutputStream o = new ObjectOutputStream(b);
        o.writeObject(obj);
        return b.toByteArray();
    }

    /**
     *
     * @param bytes the byte stream obtained from the bluetooth connection
     * @return the object corresponding to the data (song info, list of songs, or song file)
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream b = new ByteArrayInputStream(bytes);
        ObjectInputStream o = new ObjectInputStream(b);
        return o.readObject();
    }

    // get a list of the songs info on the phone
    private List<Song> getSongList() {
        List<Song> songList = new ArrayList<Song>();
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
        if(musicCursor!=null && musicCursor.moveToFirst()){
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                songList.add(new Song(thisId, thisTitle, thisArtist));
            }
            while (musicCursor.moveToNext());
        }
        return songList;
    }

}