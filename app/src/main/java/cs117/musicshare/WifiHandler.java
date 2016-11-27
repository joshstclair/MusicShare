package cs117.musicshare;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;

/**
 * Created by Arman on 11/26/2016.
 */

public class WifiHandler extends BroadcastReceiver{

        private WifiP2pManager mManager;
        private WifiP2pManager.Channel mChannel;
        private RightFragment mActivity;

        public WifiHandler(WifiP2pManager manager, WifiP2pManager.Channel channel,
                                           RightFragment activity) {
            super();
            this.mManager = manager;
            this.mChannel = channel;
            this.mActivity = activity;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                // Check to see if Wi-Fi is enabled and notify appropriate activity
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    //wifi p2p is enabled
                    mActivity.show_Message("Wifi p2p is enabled");
                    //mActivity.setIsWifiP2pEnabled(true);
                } else {
                    // Wi-Fi P2P is not enabled
                    mActivity.show_Message("Wifi p2p is not enabled!");
                    //mActivity.setIsWifiP2pEnabled(false);
                }
            } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                // Call WifiP2pManager.requestPeers() to get a list of current peers
                // request available peers from the wifi p2p manager. This is an
                // asynchronous call and the calling activity is notified with a
                // callback on PeerListListener.onPeersAvailable()
                if (mManager != null) {
                    mManager.requestPeers(mChannel, mActivity);
                }
            } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                // Respond to new connection or disconnections
                if (mManager == null) {
                    return;
                }
                NetworkInfo networkInfo = (NetworkInfo) intent
                        .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

                if (networkInfo.isConnected()) {
                    // We are connected with the other device, request connection
                    // info to find group owner IP

                    mManager.requestConnectionInfo(mChannel, (WifiP2pManager.ConnectionInfoListener) mActivity);
                }
                //disconnected
                else{
                    mActivity.updateList();
                }
            } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
                // Respond to this device's wifi state changing
                if (mManager == null) {
                    mActivity.show_Message("Wifi p2p is not enabled!");
                }

            }
        }
    }
