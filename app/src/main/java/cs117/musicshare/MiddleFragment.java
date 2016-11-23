package cs117.musicshare;


import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.Button;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;
import android.os.*;
import android.widget.Toast;

import static android.content.ContentValues.TAG;
import static java.lang.Thread.sleep;

/**
 * A simple {@link Fragment} subclass.
 */
public class MiddleFragment extends Fragment {
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;

    private ProgressDialog mBluetoothConnectProgressDialog;
    private BluetoothSocket bluetoothSocket;
    private BluetoothServerSocket mmServerSocket;
    ThreadConnected myChannel;

    private BluetoothDevice deviceToConnect;
    private BluetoothAdapter mBluetoothAdapter;
    View view;
    private TextView music_stat;
    public Button bt_server;
    public Button bt_client;
    public Button bt_send;
    public Button bt_receive;
    public TextView textStatus;

    private ProgressDialog progress_server;
    private ProgressDialog progress_client;
    private ConnectingThread clientThread;
    private AcceptThread serverThread;
    private boolean connected = false;

    private UUID MY_UUID = UUID.fromString("0000110E-0000-1000-8000-00805F9B34FB");
    BluetoothSocket mSocket=null;

    public MiddleFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        view = inflater.inflate(R.layout.fragment_middle,
                container, false);

        //setup dialog
        progress_server = new ProgressDialog(getActivity());
        progress_client = new ProgressDialog(getActivity());

        //set up server dialog
        Window window = progress_server.getWindow();
        window.setLayout(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);
        progress_server.setMessage("Opening server for requests...");
        progress_server.setCancelable(false);
        progress_server.setButton(DialogInterface.BUTTON_NEGATIVE, "Stop", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        //set up client dialog
        window = progress_client.getWindow();
        window.setLayout(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);
        progress_client.setMessage("Sending request to server...");
        progress_server.setCancelable(false);
        progress_client.setButton(DialogInterface.BUTTON_NEGATIVE, "Stop", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        music_stat = (TextView) view.findViewById(R.id.music_status);
        bt_server = (Button) view.findViewById(R.id.button_server);
        bt_client = (Button) view.findViewById(R.id.button_client);
        bt_send = (Button) view.findViewById(R.id.button_send);
        textStatus = (TextView)view.findViewById(R.id.status);
        bt_receive = (Button) view.findViewById(R.id.button_receive);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices == null || pairedDevices.size() == 0) {
            music_stat.setText("No connected devices.");
            music_stat.setVisibility(View.VISIBLE);
            bt_server.setEnabled(false);
            bt_client.setEnabled(false);
            bt_send.setEnabled(false);
        } else {
            deviceToConnect = pairedDevices.iterator().next();

            music_stat.setText("Connected to " + deviceToConnect.getName());
            music_stat.setVisibility(View.VISIBLE);
            bt_server.setEnabled(true);
            bt_client.setEnabled(true);

            bt_server.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    progress_server.show();
                    serverThread= new AcceptThread();
                    serverThread.start();
                }
            });
            bt_client.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    progress_client.show();
                    clientThread= new ConnectingThread(deviceToConnect);
                    clientThread.start();
                }
            });
            bt_send.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showMessage("sending stuff");
                    byte[] b = "testing".getBytes();
                    myChannel.write(b);
                }
            });
            bt_receive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showMessage("Receiving stuff");
                    /*try {
                        showMessage("Receiving stuff");
                        myChannel.receiveData();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }*/
                }
            });
            //bt_get

            //receiveData();

        }
        return view;
    }
    public synchronized void manageConnectedSocket(BluetoothSocket mSocket) throws IOException {
        //showMessage("Connected succesfully!");
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getActivity().getApplicationContext(), "***Ready to use socket!***",Toast.LENGTH_SHORT).show();
            }
        });
        if(serverThread != null) {
            serverThread.cancel();
            serverThread = null;
        }
        if(clientThread != null) {
            clientThread.cancel();
            clientThread = null;
        }
        if(myChannel != null) {
            myChannel.cancel();
            myChannel = null;
        }

        progress_server.dismiss();
        progress_client.dismiss();
        connected = true;
        myChannel = new ThreadConnected(mSocket);

        SystemClock.sleep(15000);
        myChannel.start();
        mHandler.obtainMessage(3)
                .sendToTarget();
        //byte[] b = "testing".getBytes();
        //myChannel.write(b);
    }


    private void showMessage(String message) {
        Toast.makeText(getActivity().getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                //Write to device
                case 2:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    showMessage("Sending:  " + writeMessage);
                    break;
                //Read from device
                case 1:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    showMessage("Reading: " + readMessage);
                    break;
                //enable send
                case 3:
                    bt_send.setEnabled(true);
                    bt_receive.setEnabled(true);
                    //start sending info
                /*case 4:
                    try {
                        myChannel.receiveData();
                        showMessage("Receiving stuff");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                        SystemClock.sleep(3000);
                        showMessage("sending stuff:" + myChannel);
                        byte[] b = ("Connected to: " + deviceToConnect.getName()).getBytes();
                        myChannel.write(b);
                */

            }
        }
    };

    private class ThreadConnected extends Thread {
        private final BluetoothSocket connectedBluetoothSocket;
        private final InputStream connectedInputStream;
        private final OutputStream connectedOutputStream;

        public ThreadConnected(final BluetoothSocket socket) {

            connectedBluetoothSocket = socket;
            InputStream in = null;
            OutputStream out = null;

            try {
                in = socket.getInputStream();
                out = socket.getOutputStream();
                final InputStream finalIn = in;
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getActivity().getApplicationContext(), "***Got the streams ready!***"+ finalIn,Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            connectedInputStream = in;
            connectedOutputStream = out;
            //mHandler.obtainMessage(4)
              //      .sendToTarget();
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    bytes = connectedInputStream.read(buffer);

                    String strReceived = new String(buffer, 0, bytes);
                    final String msgReceived = String.valueOf(bytes) +
                            " bytes received:\n"
                            + strReceived;

                    getActivity().runOnUiThread(new Runnable(){

                        @Override
                        public void run() {
                            textStatus.setText(msgReceived);
                        }});

                } catch (IOException e) {

                    // TODO Auto-generated catch block
                    e.printStackTrace();

                    final String msgConnectionLost = "Connection lost:\n"
                            + e.getMessage();
                    getActivity().runOnUiThread(new Runnable(){

                        @Override
                        public void run() {
                            textStatus.setText(msgConnectionLost);
                        }});
                }
            }
        }
        public synchronized void receiveData() throws IOException{
            try {
                final Handler handler = new Handler();
                final byte delimiter = 10;
                readBufferPosition = 0;
                readBuffer = new byte[1024];

                workerThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (!Thread.currentThread().isInterrupted()) {
                            try {
                                int bytesAvailable = connectedInputStream.available();

                                if (bytesAvailable > 0) {
                                    byte[] packetBytes = new byte[bytesAvailable];
                                    connectedInputStream.read(packetBytes);

                                    for (int i = 0; i < bytesAvailable; i++) {
                                        byte b = packetBytes[i];
                                        if (b == delimiter) {
                                            byte[] encodedBytes = new byte[readBufferPosition];
                                            System.arraycopy(readBuffer,0,encodedBytes,0,encodedBytes.length);

                                            //US ASCII code
                                            final String data = new String(encodedBytes, "US-ASCII");
                                            readBufferPosition = 0;

                                            handler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(getActivity(),"data successfully sent", Toast.LENGTH_SHORT).show();
                                                    textStatus.setText(data);
                                                }
                                            });
                                        } else {
                                            readBuffer[readBufferPosition++] = b;
                                        }
                                    }
                                }

                            } catch (IOException ex) {
                            }
                        }
                    }
                });
                workerThread.start();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        public synchronized void write(byte[] buffer) {
            try {
                connectedOutputStream.write(buffer);
                mHandler.obtainMessage(2, -1, -1, buffer)
                        .sendToTarget();
                connectedOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void cancel() {
            try {
                connectedBluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "create ConnectedThread: ");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(getActivity().getApplicationContext(), "Finished initializing connection!! ",Toast.LENGTH_SHORT).show();
                }
            });
        }

        public void run() {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(getActivity().getApplicationContext(), "Setup listening thread>>",Toast.LENGTH_SHORT).show();
                }
            });
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;

            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);

                    // Send the obtained bytes to the UI Activity
                    final int finalBytes = bytes;
                    // Send the obtained bytes to the UI Activity
                    mHandler.obtainMessage(1, finalBytes, -1, buffer)
                            .sendToTarget();
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    try {
                        myChannel.cancel();
                        manageConnectedSocket(mmSocket);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    break;
                }
            }
        }
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
                // Share the sent message back to the UI Activity
                mHandler.obtainMessage(2, -1, -1, buffer)
                        .sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    //Server side
    private class AcceptThread extends Thread {

        public AcceptThread() {
            showMessage("Server init");
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("Music_share", MY_UUID);
            } catch (IOException e) { }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    break;
                }
                // If a connection was accepted
                if (socket != null) {
                    // Do work to manage the connection (in a separate thread)
                    try {
                        final BluetoothSocket finalSocket = socket;
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getActivity().getApplicationContext(), "Accepted!!" + finalSocket, Toast.LENGTH_LONG).show();
                            }
                        });
                        manageConnectedSocket(socket);
                        mmServerSocket.close();
                        break;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        /** Will cancel the listening socket, and cause the thread to finish */
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) { }
        }
    }

    //client side
    private class ConnectingThread extends Thread {
        private final BluetoothDevice bluetoothDevice;

        public ConnectingThread(BluetoothDevice device) {
            showMessage("client init");
            BluetoothSocket temp = null;
            bluetoothDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                temp = bluetoothDevice.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            bluetoothSocket = temp;
        }

        public void run() {
            boolean success = false;
            try {
                // This will block until it succeeds in connecting to the device
                // through the bluetoothSocket or throws an exception
                bluetoothSocket.connect();
                success = true;
            } catch (IOException connectException) {
                connectException.printStackTrace();
                try {
                    bluetoothSocket.close();
                } catch (IOException closeException) {
                    closeException.printStackTrace();
                }
                return;
            }
            SystemClock.sleep(2000);
            // Code to manage the connection in a separate thread
            try {
                if(success) {
                    final String msgconnected = "connect successful:\n"
                            + "BluetoothSocket: " + bluetoothSocket + "\n"
                            + "BluetoothDevice: " + bluetoothDevice;
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getActivity().getApplicationContext(), msgconnected, Toast.LENGTH_LONG).show();
                        }
                    });
                    manageConnectedSocket(bluetoothSocket);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Cancel an open connection and terminate the thread
        public void cancel() {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}


