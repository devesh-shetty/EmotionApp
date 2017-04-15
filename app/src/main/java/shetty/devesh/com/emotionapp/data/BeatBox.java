package shetty.devesh.com.emotionapp.data;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import shetty.devesh.com.emotionapp.model.Song;

/**
 * Created by deveshshetty on 26/03/17.
 */

public class BeatBox {
  private static final String TAG = "BeatBox";
  private static final String SOUNDS_FOLDER = "emotion_music";
  private List<Song> mSongs = new ArrayList<>();

  private Song mCurrentSound;

  private MediaPlayer mMediaPlayer;

  private AssetManager mAssetManager;

  private ContentResolver mContentResolver;

  public BeatBox(Context context){
    mAssetManager = context.getAssets();

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
    return mSongs;
  }

  public void loadSongsOnDevice(){

    Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    Cursor songCursor = mContentResolver.query(songUri, null, null, null, null);

    if(songCursor != null && songCursor.moveToFirst())
    {
      int songId = songCursor.getColumnIndex(MediaStore.Audio.Media._ID);
      int songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
      int songLocation = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA);

      int index = 0;

      do {

        String path = songCursor.getString(songLocation);
        Song sound = new Song(path);
        sound.setIndex(index++);
        mSongs.add(sound);

        //long currentId = songCursor.getLong(songId);
        //String currentTitle = songCursor.getString(songTitle);
        //arrayList.add(new Songs(currentId, currentTitle, currentArtist));
      } while(songCursor.moveToNext());
    }

    songCursor.close();

  }


}