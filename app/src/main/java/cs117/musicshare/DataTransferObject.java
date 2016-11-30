package cs117.musicshare;

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

    public Song getSongPayload() {
        return songPayload;
    }

    public void setSongPayload(Song songPayload) {
        payloadFlag = song;
        this.songPayload = songPayload;
    }

    public List<Song> getSongListPayload() {
        return songListPayload;
    }

    public void setSongListPayload(List<Song> songListPayload) {
        payloadFlag = songList;
        this.songListPayload = songListPayload;
    }

    public String getPayloadFlag() {
        return payloadFlag;
    }

    private String payloadFlag;
    private Song songPayload;
    private List<Song> songListPayload;


}
