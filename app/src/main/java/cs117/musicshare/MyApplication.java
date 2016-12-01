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
    private boolean myMiddleFrag = false;
    private MusicService sharedMusicServ;

    public boolean getIsMiddle(){
        return myMiddleFrag;
    }
    public void setIsMiddle(boolean set){
        myMiddleFrag = set;
    }

    public void setSharedMusicServ(MusicService s){
        this.sharedMusicServ = s;
    }
    public MusicService getSharedMusicServ(){
        return this.sharedMusicServ;
    }

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
