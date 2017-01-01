package com.iq.vibhu.musicplayer.song;

import android.annotation.TargetApi;
import android.content.Context;
import android.widget.MediaController;
import android.os.Build;
import android.support.annotation.RequiresApi;

/**
 * Created by Vibhu on 08/12/2016.
 */

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class MusicControl extends MediaController {

    public MusicControl(Context c){
        super(c);
    }

    public void hide(){}

}
