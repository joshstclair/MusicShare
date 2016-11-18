package cs117.musicshare;


import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class DeviceList extends Activity {
    private ListView mListView;
    private ArrayList<BluetoothDevice> mDeviceList;
    ArrayAdapter<BluetoothDevice> pairedDeviceAdapter;

    ArrayList<BluetoothDevice> pairedDeviceArrayList;
    BluetoothAdapter bluetoothAdapter;
    ListView listViewPairedDevice;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);


        mDeviceList = getIntent().getExtras().getParcelableArrayList("device.list");

        mListView = (ListView) findViewById(R.id.bluelist);


        pairedDeviceAdapter = new ArrayAdapter<BluetoothDevice>(this,
                android.R.layout.simple_list_item_1, mDeviceList);
        mListView.setAdapter(pairedDeviceAdapter);


        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            public void onItemClick(AdapterView<?> parent, View view,
                                    final int position, long id) {
                final BluetoothDevice device =
                        (BluetoothDevice)parent.getItemAtPosition(position);
                            final String device_name = device.getName();
                            new AlertDialog.Builder(DeviceList.this)
                                    .setTitle("Device information")
                                    .setMessage("Name: " + device.getName() + "\n"
                                            + "Address: " + device.getAddress() + "\n"
                                            + "BondState: " + device.getBondState() + "\n"
                                            + "BluetoothClass: " + device.getBluetoothClass() )
                                    //Dismiss message or unpair
                                    .setPositiveButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            // do nothing
                                        }
                                    })
                                    .setNegativeButton("pair/unpair", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            if(device.getBondState() != BluetoothDevice.BOND_BONDED){
                                                try {
                                                    Method m = device.getClass()
                                                            .getMethod("createBond", (Class[]) null);
                                                    m.invoke(device, (Object[]) null);
                                                    mListView.getChildAt(position).setBackgroundColor(0x7778AB46);
                                                    show_Message("Paired device: " + device_name);
                                                } catch (Exception e) {
                                                    Log.e("fail", e.getMessage());
                                                }
                                            }
                                            else{
                                                try {
                                                    Method method = device.getClass().getMethod("removeBond", (Class[]) null);
                                                    method.invoke(device, (Object[]) null);
                                                    mListView.getChildAt(position).setBackgroundColor(0);
                                                    show_Message("Unpaired device: " + device_name);

                                                } catch (Exception e) {
                                                    Log.e("fail", e.getMessage());
                                                }
                                            }
                                        }
                                    })
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .show();
                }
            });
    }
    //Pop up a message to the user
    private void show_Message(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
    //go back to BT menu
    public void backMain(View view) {
        finish();
    }
}