package cs117.musicshare;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.os.ParcelUuid;
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
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import android.os.*;
import android.widget.Toast;

import static android.R.style.Theme_DeviceDefault_Dialog;
import static android.R.style.Theme_Translucent;
import static android.R.style.Theme_Translucent_NoTitleBar;
import static android.content.ContentValues.TAG;
import static java.lang.Thread.sleep;

/**
 * A simple {@link Fragment} subclass.
 */
public class MiddleFragment extends Fragment {
    View view;
    InetAddress communicationIP;
    Boolean host;
    ServerSocket serverSocket;
    Socket socket;
    boolean serverRun = false;
    boolean clientRun = false;

    SocketClientThread client;
    SocketServerThread server;

    private Button share;
    private Button send;
    private ListView songs;

    ProgressDialog loading;
    ThreadConnected myChannel;
    public MiddleFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        view = inflater.inflate(R.layout.fragment_middle,
                container, false);
        share = (Button) view.findViewById(R.id.share);
        send = (Button) view.findViewById(R.id.send);
        songs = (ListView) view.findViewById(R.id.song_list);

        loading = new ProgressDialog(getActivity(), Theme_DeviceDefault_Dialog);
        //new ProgressDialog.Builder(getActivity())
        loading.setTitle("Sharing menu");
        loading.setMessage("Waiting for other device to be ready...");
        loading.setButton(DialogInterface.BUTTON_POSITIVE, "Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        serverRun = false;
                        clientRun = false;
                        return;
                    }
                });

        share.setOnClickListener(new View.OnClickListener() {
            public void onClick (View v) {
                share();
                if(communicationIP == null && host == false) {
                    loading.show();
                }
                else{
                    clientRun = true;
                    serverRun = true;
                    loading.show();
                }
            }
        });

        return view;
    }
    public void share(){
        communicationIP = ((MyApplication) getActivity().getApplication()).getIP();
        host = ((MyApplication) getActivity().getApplication()).getHost();

        if(host){
            if(communicationIP == null){
                show_Message("blank---server");
            }
            try {
                serverStuff();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            if(communicationIP != null){
                show_Message("not blank ---client:" + communicationIP);
            }
            try {
                clientStuff();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    //----------------------------Server socket setup ----------------------------
    public void serverStuff() throws IOException {
        serverSocket = null;
        server = new SocketServerThread();
        server.start();
    }

    private class SocketServerThread extends Thread{

        static final int SocketServerPORT = 8080;

        @Override
        public void run() {
            try {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getActivity(), "in here waiting..." + serverRun, Toast.LENGTH_SHORT).show();
                    }
                });
                serverSocket = new ServerSocket(SocketServerPORT);
                while (serverRun) {
                    socket = serverSocket.accept();
                    if(socket != null){
                        break;
                    }
                }
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getActivity(), "Connnected----server", Toast.LENGTH_SHORT).show();
                    }
                });
                loading.dismiss();
                mHandler.obtainMessage(1).sendToTarget();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    //------------------client socket setup ---------------------------
    public void clientStuff() throws IOException {

        client = new SocketClientThread();
        client.start();


    }
    private class SocketClientThread extends Thread {

        static final int port = 8080;
        int timeout = 5000;

        @Override
        public void run() {
            while (clientRun) {
                try {
                    Thread.sleep(3000);
                    socket = new Socket();
                    socket.bind(null);
                    socket.connect((new InetSocketAddress(communicationIP, port)), timeout);
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getActivity(), "Connnected---client", Toast.LENGTH_SHORT).show();
                        }
                    });
                    loading.dismiss();
                    clientRun = false;
                    mHandler.obtainMessage(1).sendToTarget();
                }
                catch(IOException e){
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    //--------handler ----------
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                //Write to device
                        case 1:
                            transferData();
                        /*case 2:
                            byte[] readBuf = (byte[]) msg.obj;
                            String readMessage = new String(readBuf, 0, msg.arg1);
                            show_Message("Reading: " + readMessage);*/
                        }
            }
        };
    //-----------data transfer--------------------
    public void transferData()
    {
        songs.setVisibility(View.VISIBLE);
        send.setVisibility(View.VISIBLE);
        share.setVisibility(View.INVISIBLE);

        myChannel = new ThreadConnected();
        myChannel.start();

        send.setOnClickListener(new View.OnClickListener() {
            public void onClick (View v) {
                if(host == false) {
                    byte[] b = "testing from client".getBytes();
                    try {
                        myChannel.write(b);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    byte[] b = "testing from server".getBytes();
                    try {
                        myChannel.write(b);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }
    private class ThreadConnected extends Thread {
        private InputStream connectedInputStream;
        private OutputStream connectedOutputStream;

        public ThreadConnected() {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = socket.getInputStream();
                out = socket.getOutputStream();
                final InputStream finalIn1 = in;
                final OutputStream finalOut = out;
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getActivity().getApplicationContext(), "***Got the <IN> streams ready:" + finalIn1, Toast.LENGTH_SHORT).show();
                        Toast.makeText(getActivity().getApplicationContext(), "***Got the <OUT> streams ready:" + finalOut, Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
            connectedInputStream = in;
            connectedOutputStream = out;
        }
        @Override
        public void run() {
            final byte delimiter = 10;
            int readBufferPosition = 0;
            byte[] readBuffer = new byte[1024];
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(3000);
                    /*readBufferPosition = connectedInputStream.read(readBuffer);
                    final int finalBytes = readBufferPosition;
                    mHandler.obtainMessage(2, finalBytes, -1, readBuffer).sendToTarget();*/

                    int bytesAvailable = connectedInputStream.available();
                    if (bytesAvailable > 0) {
                        byte[] packetBytes = new byte[bytesAvailable];
                        connectedInputStream.read(packetBytes);
                        final String message = new String(packetBytes, "UTF-8");
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getActivity(), "Mesage Recieved: " + message, Toast.LENGTH_SHORT).show();
                            }
                        });

                        for (int i = 0; i < bytesAvailable; i++) {
                            byte b = packetBytes[i];
                            if (b == delimiter) {
                                byte[] encodedBytes = new byte[readBufferPosition];
                                System.arraycopy(readBuffer,0,encodedBytes,0,encodedBytes.length);
                                final String data = new String(encodedBytes, "US-ASCII");
                                readBufferPosition = 0;
                                getActivity().runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(getActivity(), "Date recieved: " + data, Toast.LENGTH_SHORT).show();
                                    }
                                });
                                }
                            else {
                                readBuffer[readBufferPosition++] = b;
                            }
                        }
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getActivity(), "0 bytes available: ", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                catch (IOException ex) {
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getActivity(), "Could not recieve data... ", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            }
        public void write(byte[] buffer) throws IOException {
            connectedOutputStream.write(buffer);
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(getActivity(), "Date sending... ", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    //Pop up a message to the user
    public void show_Message(String message) {
        Toast.makeText(getActivity().getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onPause() {
        super.onPause();
        serverRun = false;
        clientRun = false;
    }
}

