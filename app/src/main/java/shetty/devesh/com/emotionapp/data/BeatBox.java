package shetty.devesh.com.emotionapp.data;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import shetty.devesh.com.emotionapp.model.Sound;

/**
 * Created by deveshshetty on 26/03/17.
 */

public class BeatBox {
  private static final String TAG = "BeatBox";
  private static final String SOUNDS_FOLDER = "emotion_music";
  private List<Sound> mSounds = new ArrayList<>();

  private Sound mCurrentSound;

  private MediaPlayer mMediaPlayer;

  private AssetManager mAssetManager;

  public BeatBox(Context context){
    mAssetManager = context.getAssets();

    mMediaPlayer = new MediaPlayer();
    mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
      @Override
      public void onPrepared(MediaPlayer mp) {
        togglePlayPause();
      }
    });
    loadSounds();

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

  private void loadSounds() {
    String[] soundNames;
    try {
      soundNames = mAssetManager.list(SOUNDS_FOLDER);
      Log.i(TAG, "Found " + soundNames.length + " emotion_music");
    } catch (IOException ioe) {
      Log.e(TAG, "Could not list assets", ioe);
      return; }

    int index = 0;

    for (String filename : soundNames) {
        String assetPath = SOUNDS_FOLDER + "/" + filename;
        Sound sound = new Sound(assetPath);
        sound.setIndex(index++);
        mSounds.add(sound);
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
    Sound sound = mSounds.get(0);
    play(sound);

  }

  public void playNextSong(){
    Sound sound = nextSound();
    play(sound);
  }

  public Sound nextSound(){
    Sound sound = null;

    int index = mCurrentSound.getIndex();
    index++;
    index = (index%mSounds.size());

    sound = mSounds.get(index);

    return sound;
  }

  public void play(Sound sound) {

      mMediaPlayer.stop();
    //call reset to get it back to idle state
      mMediaPlayer.reset();

    try {
      mCurrentSound = sound;
      AssetFileDescriptor afd = mAssetManager.openFd(sound.getAssetPath());
      mMediaPlayer.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
      mMediaPlayer.prepare();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }


  public List<Sound> getSounds() {
    return mSounds;
  }



}