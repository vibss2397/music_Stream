package com.iq.vibhu.musicplayer.song;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.iq.vibhu.musicplayer.R;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Vibhu on 08/12/2016.
 */
public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener,AudioManager.OnAudioFocusChangeListener {
    private final IBinder musicBind = new MusicBinder();
    private MediaPlayer player;
    //song list
    private ArrayList<Song> songs;
    //current position
    private int songPos;
    private String songTitle="";
    private boolean shuffle=false;
    private Random rand;
    private static final int NOTIFY_ID=1;
    public void setList(ArrayList<Song> theSongs){
        songs=theSongs;
    }
    public long currSong =0;
    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
                if (player == null) initMusicPlayer();
                else if (!player.isPlaying()) player.start();
                player.setVolume(1.0f, 1.0f);
                break;

            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                if (player.isPlaying()) player.stop();
                player.release();
                player = null;
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (player.isPlaying()) player.pause();
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (player.isPlaying()) player.setVolume(0.1f, 0.1f);
                break;
        }
    }

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent){
        player.stop();
        player.release();
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(player.getCurrentPosition()>0){
            mp.reset();
            playNext();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();return false;
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onPrepared(MediaPlayer mp) {
    mp.start();
        Intent notIntent = new Intent(this, MainActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0, notIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentIntent(pendInt)
                .setSmallIcon(R.drawable.ic_music_note_white_24dp)
                .setTicker(songTitle)
                .setOngoing(true)
                .setContentTitle("Playing")

                .setContentText(songTitle)
                .addAction(R.drawable.ic_play_arrow_black_24dp,"play",pendInt)
                .addAction(R.drawable.ic_pause_black_24dp,"pause",pendInt);
        Notification not = builder.build();

        startForeground(NOTIFY_ID, not);
    }
//    public boolean requestFocus() {
//        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
//                am.requestAudioFocus(mContext, AudioManager.STREAM_MUSIC,
//                        AudioManager.AUDIOFOCUS_GAIN);
//    }
    @Override
    public void onDestroy() {
        stopForeground(true);
    }

    public void setSong(int songIndex){
        songPos=songIndex;
    }

    public void onCreate(){
//        AudioManager am ;

        super.onCreate();
//initialize position
        songPos=0;
//create player
        player = new MediaPlayer();
        initMusicPlayer();
        rand=new Random();
    }

    public void setShuffle(){
        if(shuffle) shuffle=false;
        else shuffle=true;
    }

    public void initMusicPlayer(){
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    public void playSong(){
        player.reset();
        Song playSong = songs.get(songPos);

//get id

        songTitle=playSong.getTitle();
        currSong= playSong.getID();
//set uri
        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currSong);
        try{
            player.setDataSource(getApplicationContext(), trackUri);
        }
        catch(Exception e){
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        player.prepareAsync();
    }

    public int getPosn(){
        return player.getCurrentPosition();
    }

    public int getDur(){
        return player.getDuration();
    }

    public boolean isPng(){
        return player.isPlaying();
    }

    public void pausePlayer(){
        player.pause();
    }

    public void seek(int posn){
        player.seekTo(posn);
    }

    public void go(){
        player.start();
    }

    public void playPrev(){
        songPos--;
        if(songPos<0) songPos=songs.size()-1;
        playSong();
    }

    public void playNext(){
        if(shuffle){
            int newSong = songPos;
            while(newSong==songPos){
                newSong=rand.nextInt(songs.size());
            }
            songPos=newSong;
        }
        else{
            songPos++;
            if(songPos>=songs.size()) songPos=0;
        }
        playSong();
    }

    public Song getSo(int pos) {
        return songs.get(pos);
    }
    public long getCurrSong(){
        return currSong;
    }
}

