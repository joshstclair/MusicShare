package cs117.musicshare;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import android.os.Handler;

/**
 * A simple {@link Fragment} subclass.
 */
public class RightFragment extends Fragment {


    public RightFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_right,
                container, false);

        //FrameLayout f2 = (FrameLayout) inflater.inflate(R.layout.fragment_left, container, false);
        //link button to variable
        mScan_button = (Button) view.findViewById(R.id.button_scan);
        mbt_status = (TextView) view.findViewById(R.id.btList);

        btViewList = (ListView) view.findViewById(R.id.bluelist);
        on_button = (Button) view.findViewById(R.id.buttonOn);
        refresh_button = (Button) view.findViewById(R.id.refresh);

        //get bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null) {
            Toast.makeText(getActivity(), "No Bluetooth adapter available",Toast.LENGTH_SHORT).show();
        }
        progress_dialog = new ProgressDialog(getActivity());
        bt_dialog = new ProgressDialog(getActivity());

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
                showEnabled();
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
            public void onClick(View v) {
                //Toast.makeText(getActivity(), "Helloo",Toast.LENGTH_SHORT).show();
                mBluetoothAdapter.startDiscovery();


            }
        });

        //turn on/off bluetooth
        on_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBluetoothAdapter.isEnabled()) {
                    mBluetoothAdapter.disable();
                    showDisabled();
                } else {
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intent, 1000);
                }
            }
        });

        //if Bluetooth is on
        if (mBluetoothAdapter.isEnabled()) {
            showEnabled();
            showPaired(view);
        } else {
            showDisabled();
        }
        IntentFilter filter = new IntentFilter();

        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        getActivity().registerReceiver(mReceiver, filter);

        return view;
        //return inflater.inflate(R.layout.fragment_right, container, false);
    }

    //Pop up a message to the user
    private void show_Message(String message) {
        Toast.makeText(getActivity().getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    //if bluetooth is on
    private void showEnabled() {
        on_button.setText("Turn off Bluetooth");
        bt_dialog.dismiss();
        View view = getActivity().getWindow().getDecorView();
        //view.setBackgroundColor(0xFFADD8A7);//0xFFADD8E6
        mScan_button.setEnabled(true);
        refresh_button.setEnabled(true);
        mbt_status.setVisibility(view.VISIBLE);
        btViewList.setVisibility(view.VISIBLE);
    }

    //if bluetooth is off
    private void showDisabled() {
        View view = getActivity().getWindow().getDecorView();
        //view.setBackgroundColor(0xFFADD8A7);
        on_button.setText("Turn on Bluetooth");
        mScan_button.setEnabled(false);
        refresh_button.setEnabled(false);
        mbt_status.setVisibility(view.INVISIBLE);
        btViewList.setVisibility(view.INVISIBLE);
    }

    public void showPaired(View v) {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        //show no devices message

        if (pairedDevices == null || pairedDevices.size() == 0) {
            v.findViewById(R.id.bluelist).setVisibility(View.INVISIBLE);
            mbt_status.setText("No devices are currently paired.");
        }
        //or create a new intent for the device list
        else {
            v.findViewById(R.id.bluelist).setVisibility(View.VISIBLE);
            mbt_status.setText("Devices currently paired.");
            ArrayList<BluetoothDevice> list = new ArrayList<BluetoothDevice>();
            list.addAll(pairedDevices);

            pairedDeviceAdapter = new ArrayAdapter<BluetoothDevice>(getActivity(),
                    android.R.layout.simple_list_item_1, list);
            pairedDeviceAdapter.notifyDataSetChanged();

            btViewList.setAdapter(pairedDeviceAdapter);

            btViewList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                //if user clicks on paired devices
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    mmDevice =
                            (BluetoothDevice) parent.getItemAtPosition(position);
                    final String device_name = mmDevice.getName();
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Device information")
                            .setMessage("Name: " + mmDevice.getName() + "\n"
                                    + "Address: " + mmDevice.getAddress() + "\n"
                                    + "BondState: " + mmDevice.getBondState() + "\n"
                                    + "BluetoothClass: " + mmDevice.getBluetoothClass())
                            //Dismiss message or unpair
                            .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                }
                            })
                            .setPositiveButton("unpair", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    try {
                                        Method m = mmDevice.getClass()
                                                .getMethod("removeBond", (Class[]) null);
                                        m.invoke(mmDevice, (Object[]) null);
                                        getActivity().finish();
                                        startActivity(getActivity().getIntent());
                                        show_Message("Unpaired device: " + device_name);
                                        pairedDeviceAdapter.notifyDataSetChanged();
                                    } catch (Exception e) {
                                        Log.e("fail", e.getMessage());
                                    }
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            });
        }
    }
    @Override
    public void onDestroy() {
        getActivity().unregisterReceiver(mReceiver);
        super.onDestroy();
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
                    showEnabled();
                }
            }
            //if bluetooth starts discovering, show message for discovery and init new device list
            else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                device_list = new ArrayList<>();
                progress_dialog.show();
            }
            //if bluetooth finishes discovery, show all discovered items
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                progress_dialog.dismiss();
                Intent newIntent = new Intent(getActivity() /*BluetoothDevices.this*/, DeviceList.class);
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

    //Bluetooth Variables
    //-----------------------------------------------
    private TextView mbt_status;
    private Button mScan_button;
    private ListView btViewList;
    private Button on_button;
    private Button refresh_button;
    View view;
    //shows progress menu
    private ProgressDialog progress_dialog;
    private ProgressDialog bt_dialog;
    //create list to store devices
    private ArrayList<BluetoothDevice> device_list = new ArrayList<>();
    //create an instance of Bluetooth
    private BluetoothAdapter mBluetoothAdapter;
    ArrayAdapter<BluetoothDevice> pairedDeviceAdapter;

    public BluetoothDevice mmDevice;
    public BluetoothSocket mmSocket;


    //-----------------------------------------------
}