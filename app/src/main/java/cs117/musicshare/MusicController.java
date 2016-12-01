package cs117.musicshare;


import android.content.Context;
import android.view.KeyEvent;
import android.widget.MediaController;

/**
 * Created by joshua on 11/1/16.
 */

public class MusicController extends MediaController {
    Context c;
    public MusicController(Context c){
        super(c);
        this.c = c;
    }

    //public void hide(){}
    public int mTimeout = 4000;

    @Override
    public void show() {
        show(mTimeout);
    }

    @Override
    public void show(int timeout) {
        super.show(mTimeout);
    }
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if(keyCode == KeyEvent.KEYCODE_BACK){
            ((MainActivity)c).onBackPressed();
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

}