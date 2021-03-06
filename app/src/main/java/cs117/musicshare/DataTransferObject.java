package cs117.musicshare;

import java.io.File;
import java.io.Serializable;
import java.util.List;

/**
 * Created by joshua on 11/29/16.
 */

public class DataTransferObject implements Serializable {

    public DataTransferObject() {
        payloadFlag = "";
    }

    public static final String song = "SONG";           // song info object
    public static final String songList = "SONG_LIST";  // array list of songs
    public static final String songFile = "SONG_FILE";  // actual song media file

    public void setSongPayload(Song songPayload) {
        payloadFlag = song;
        this.songPayload = songPayload;
    }

    public void setSongListPayload(List<Song> songListPayload) {
        payloadFlag = songList;
        this.songListPayload = songListPayload;
    }

    public void setSongFilePayload(byte[] songFilePayload) {
        payloadFlag = songFile;
        this.songFilePayload = songFilePayload;
    }

    public Song getSongPayload() {
        return songPayload;
    }
    public List<Song> getSongListPayload() {
        return songListPayload;
    }
    public byte[] getSongFilePayload() {
        return songFilePayload;
    }
    public String getPayloadFlag() {
        return payloadFlag;
    }

    private String payloadFlag;
    private Song songPayload;
    private List<Song> songListPayload;
    private byte[] songFilePayload;

}
