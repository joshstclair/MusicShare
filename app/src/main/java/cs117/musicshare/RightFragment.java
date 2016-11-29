package cs117.musicshare;


import android.app.AlertDialog;
import android.app.Application;
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
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Toast;

import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.widget.AdapterView.OnItemClickListener;

import org.apache.http.conn.util.InetAddressUtils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.os.Looper.getMainLooper;


/**
 * A simple {@link Fragment} subclass.
 */
public class RightFragment extends Fragment implements WifiP2pManager.ConnectionInfoListener, WifiP2pManager.PeerListListener {


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

        //-------wifi direct init----
        mManager = (WifiP2pManager) getActivity().getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(getActivity(), getMainLooper(), null);
        mReceiver = new WifiHandler(mManager, mChannel, this);

        //init wifi direct intent-filters
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        //Search for near by peers
        bSearch = (Button) view.findViewById(R.id.searcher);
        bSearch.setOnClickListener(new View.OnClickListener() {
            public void onClick (View v) {
                peersConnect.clear();
                list.setVisibility(ListView.VISIBLE);
                peersConnect.clear();
                peers.clear();
                peersName.clear();
                discoverDevices();
            }
        });
/*
        bDisconnect = (Button) view.findViewById(R.id.disconnecter);
        bDisconnect.setOnClickListener(new View.OnClickListener() {
            public void onClick (View v) {
                disconnectDevices();
                peersConnect.clear();
                bDisconnect.setVisibility(View.INVISIBLE);
            }
        });*/

        list = (ListView) view.findViewById(R.id.connectList);
        list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        list.setTextFilterEnabled(true);

        listC =(ListView) view.findViewById(R.id.connectedList);
        listC.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listC.setTextFilterEnabled(true);



        return view;
        //return inflater.inflate(R.layout.fragment_right, container, false);
    }

    //connect disconnect ---------------------------

    public void onPeersAvailable(WifiP2pDeviceList peerList) {
        // Out with the old, in with the new.
        peers.clear();
        peers.addAll(peerList.getDeviceList());
        getDeviceName();
        list.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, peersName));
        list.setVisibility(ListView.VISIBLE);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //WifiP2pDevice device = (WifiP2pDevice) parent.getItemAtPosition(position);
                final WifiP2pDevice device = (WifiP2pDevice) peers.get(position);
                //show_Message("Connecting :" + device);
                new AlertDialog.Builder(getActivity())
                        .setTitle("Device information")
                        .setMessage("Connecting..." + device)
                        .setNegativeButton("Connect", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                peersConnect.add(device);
                                peersConnected.add(device);
                                connectDevices();
                            }
                        })
                        .setPositiveButton("cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                return;
                            }})
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();;
            }
        });

        if (peers.size() == 0) {
            return;
        }
    }

    public void onConnectionInfoAvailable(final WifiP2pInfo info) {
        peers_Connect_list.clear();
        String groupOwnerAddress = info.groupOwnerAddress.getHostAddress();
        InetAddress groupOwner = info.groupOwnerAddress;

        // After the group negotiation, we can determine the group owner.
        if (info.groupFormed && info.isGroupOwner) {
            peers_Connect_list.add(groupOwnerAddress + "-- Owner");
            listC.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, peers_Connect_list));
            listC.setVisibility(ListView.VISIBLE);

            listC.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    new AlertDialog.Builder(getActivity())
                            .setTitle("Connection managment")
                            .setMessage("Device is the owner, device can disconnect connection.")
                            .setNegativeButton("Okay", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                return;
                                }
                            })
                            .setPositiveButton("Disconnect", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    disconnectDevices();
                                }})
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();;
                }
            });
            ((MyApplication) getActivity().getApplication()).setIP(null);
            ((MyApplication) getActivity().getApplication()).setHost(true);

            // Do whatever tasks are specific to the group owner.
            // One common case is creating a server thread and accepting
            // incoming connections.
        } else if (info.groupFormed) {
            peers_Connect_list.add(groupOwnerAddress + "-- Not Owner");
            listC.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, peers_Connect_list));
            listC.setVisibility(ListView.VISIBLE);

            listC.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    new AlertDialog.Builder(getActivity())
                            .setTitle("Connection managment")
                            .setMessage("Device is not the owner, cannot disconnect")
                            .setNegativeButton("Okay", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    return;
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();;
                }
            });
            ((MyApplication) getActivity().getApplication()).setIP(groupOwner);
            ((MyApplication) getActivity().getApplication()).setHost(false);
            // The other device acts as the client. In this case,
            // you'll want to create a client thread that connects to the group
            // owner.
        }
        show_Message("IP:" + getIpAddress());
    }
    public void updateList(){
        listC.setAdapter(null);
        //listC.setVisibility(ListView.INVISIBLE);
    }
    private void discoverDevices() {
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(getActivity(), "Discovery Initiated",
                        Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onFailure(int reasonCode) {
                Toast.makeText(getActivity(), "Discovery Failed : " + reasonCode,
                        Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void connectDevices() {
        for(int i = 0; i < peersConnect.size(); i++) {

            // Picking the first device found on the network.
            WifiP2pDevice device = peersConnect.get(i);

            WifiP2pConfig config = new WifiP2pConfig();
            config.deviceAddress = device.deviceAddress;
            config.wps.setup = WpsInfo.PBC;

            mManager.connect(mChannel, config, new ActionListener() {

                @Override
                public void onSuccess() {
                    // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
                    Toast.makeText(getActivity(), "Connection requested...", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(int reason) {
                    Toast.makeText(getActivity(), "Connect failed. Retry.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void disconnectDevices() {
        if (mManager != null && mChannel != null) {
            mManager.requestGroupInfo(mChannel, new WifiP2pManager.GroupInfoListener() {

                public void onGroupInfoAvailable(WifiP2pGroup group) {
                    if (group != null && mManager != null && mChannel != null && group.isGroupOwner()) {
                        mManager.removeGroup(mChannel, new ActionListener() {
                            @Override
                            public void onSuccess() {
                                show_Message("Connection disconnected succesfully!");
                            }

                            @Override
                            public void onFailure(int reason) {
                                show_Message("Connection disconnected failed, try again!");
                            }
                        });
                    }
                }
            });
        }
    }


    private void getDeviceName() {
        int i = 0;
        peersName.clear();
        while(i < peers.size()) {
            peersName.add(peers.get(i).deviceName);
            i++;
        }
    }


    //---------------------------------------------


    /* register the broadcast receiver with the intent values to be matched */
    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(mReceiver, mIntentFilter);
    }
    /* unregister the broadcast receiver */
    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mReceiver);
    }

    //Pop up a message to the user
    public void show_Message(String message) {
        Toast.makeText(getActivity().getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public static String getIpAddress() {
        try {
            List<NetworkInterface> interfaces = Collections
                    .list(NetworkInterface.getNetworkInterfaces());

            for (NetworkInterface intf : interfaces) {
                if (!intf.getName().contains("p2p"))
                    continue;

                List<InetAddress> addrs = Collections.list(intf
                        .getInetAddresses());

                for (InetAddress addr : addrs) {
                    // Log.v(TAG, "inside");

                    if (!addr.isLoopbackAddress()) {
                        // Log.v(TAG, "isnt loopback");
                        String sAddr = addr.getHostAddress().toUpperCase();

                        boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);

                        if (isIPv4) {
                            if (sAddr.contains("192.168.49.")) {
                                return sAddr;
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            //don't handle this
            return "";
        } // for now eat exceptions
        return "";
    }

    public static String getMACAddress(String interfaceName) {
        try {
            List<NetworkInterface> interfaces = Collections
                    .list(NetworkInterface.getNetworkInterfaces());

            for (NetworkInterface intf : interfaces) {
                if (interfaceName != null) {
                    if (!intf.getName().equalsIgnoreCase(interfaceName))
                        continue;
                }
                byte[] mac = intf.getHardwareAddress();
                if (mac == null)
                    return "";
                StringBuilder buf = new StringBuilder();
                for (int idx = 0; idx < mac.length; idx++)
                    buf.append(String.format("%02X:", mac[idx]));
                if (buf.length() > 0)
                    buf.deleteCharAt(buf.length() - 1);
                return buf.toString();
            }
        } catch (Exception ex) {
        } // for now eat exceptions
        return "";
    }

    View view;

    //intent filter for wifi direct
    WifiP2pManager.PeerListListener myPeerListListener;
    IntentFilter mIntentFilter;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    BroadcastReceiver mReceiver;

    //Connecting-disconnecting
    private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    //connected peers
    private List<WifiP2pDevice> peersConnect = new ArrayList<WifiP2pDevice>();
    private List<WifiP2pDevice> peersConnected = new ArrayList<WifiP2pDevice>();
    private ArrayList<String> peersName = new ArrayList<String>();

    private ArrayList<String> peers_Connect_list = new ArrayList<String>();

    private ListView list;
    private ListView listC;
    private Button bSearch;

}