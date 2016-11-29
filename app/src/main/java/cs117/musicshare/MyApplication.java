package cs117.musicshare;

import android.app.Application;

import java.net.Inet4Address;
import java.net.InetAddress;

/**
 * Created by Arman on 11/28/2016.
 */
public class MyApplication extends Application {

    private InetAddress ip_address = null;
    private boolean host = false;

    public InetAddress getIP() {
        return ip_address;
    }

    public void setIP(InetAddress someVariable) {
        this.ip_address = someVariable;
    }

    public boolean getHost() {
        return this.host;
    }

    public void setHost(boolean someVariable) {
        this.host = someVariable;
    }

}
