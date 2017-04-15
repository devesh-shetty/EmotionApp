package shetty.devesh.com.emotionapp.data;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import shetty.devesh.com.emotionapp.model.Song;

/**
 * Created by deveshshetty on 26/03/17.
 */

public class BeatBox {
  private static final String TAG = "BeatBox";
  private static final long MIN_SONG_DURATION = 90000;
  private List<Song> mSongs = new ArrayList<>();

  private Song mCurrentSound;

  private MediaPlayer mMediaPlayer;

  private ContentResolver mContentResolver;

  public BeatBox(Context context){

    mContentResolver = context.getContentResolver();
    mMediaPlayer = new MediaPlayer();
    mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
      @Override
      public void onPrepared(MediaPlayer mp) {
        togglePlayPause();
      }
    });

    loadSongsOnDevice();

    mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
      @Override
      public void onCompletion(MediaPlayer mediaPlayer) {
        playNextSong();
      }
    });
  }

  private void togglePlayPause() {
    if (mMediaPlayer.isPlaying()) {
      mMediaPlayer.pause();
     // mPlayerControl.setImageResource(R.drawable.ic_play);
    } else {
      mMediaPlayer.start();
    //  mPlayerControl.setImageResource(R.drawable.ic_pause);
    }
  }

  public boolean isMediaPlaying(){
    return mMediaPlayer.isPlaying();
  }

  public void pause(){
    if(isMediaPlaying()){
      togglePlayPause();
    }
  }

  public void play(){
    if(!isMediaPlaying()){
      togglePlayPause();
    }
  }

  public void playFirstSong(){
    Song sound = mSongs.get(0);
    play(sound);

  }

  public void playNextSong(){
    Song sound = nextSound();
    play(sound);
  }

  public Song nextSound(){
    Song sound = null;

    int index = mCurrentSound.getIndex();
    index++;
    index = (index% mSongs.size());

    sound = mSongs.get(index);

    return sound;
  }

  public void play(Song sound) {

      mMediaPlayer.stop();
    //call reset to get it back to idle state
      mMediaPlayer.reset();

    try {
      mCurrentSound = sound;
      mMediaPlayer.setDataSource(sound.getPath());
      mMediaPlayer.prepare();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }


  public List<Song> getSounds() {

    int size = mSongs.size();

    Random random = new Random();
    int newSize = random.nextInt(size);

    Collections.shuffle(mSongs);

    return mSongs.subList(0, newSize);
  }

  public void loadSongsOnDevice(){

    Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

    Cursor songCursor = mContentResolver.query(songUri, null, selection, null, null);

    if(songCursor != null && songCursor.moveToFirst())
    {
      int songId = songCursor.getColumnIndex(MediaStore.Audio.Media._ID);
      int songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
      int songLocation = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
      int durationColumnIndex = songCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);

      int index = 0;

      do {

        String path = songCursor.getString(songLocation);
        long currentId = songCursor.getLong(songId);
        String currentTitle = songCursor.getString(songTitle);
        long duration = songCursor.getLong(durationColumnIndex);

        if(duration < MIN_SONG_DURATION){
          //if duration is less than 1.5 min then don't add it to the list
          continue;
        }

        Song song = new Song(currentId, path, currentTitle, index++);
        mSongs.add(song);

      } while(songCursor.moveToNext());
    }

    songCursor.close();

  }


}