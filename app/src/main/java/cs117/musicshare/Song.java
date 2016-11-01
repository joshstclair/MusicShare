package cs117.musicshare;

/**
 * Created by joshua on 10/31/16.
 */

public class Song {

    public Song(long songID, String songTitle, String songArtist) {
        id=songID;
        title=songTitle;
        artist=songArtist;
    }

    public long getID(){return id;}
    public String getTitle(){return title;}
    public String getArtist(){return artist;}
    private long id;
    private String title;
    private String artist;
}

