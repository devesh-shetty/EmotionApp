package shetty.devesh.com.emotionapp.data;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.SoundPool;
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
  private static final String SOUNDS_FOLDER = "sounds";
  private List<Sound> mSounds = new ArrayList<>();
  private static final int MAX_SOUNDS = 1;
  private SoundPool mSoundPool;


  private AssetManager mAssetManager;

  public BeatBox(Context context){
    mAssetManager = context.getAssets();
    // This old constructor is deprecated, but we need it for
    // compatibility.
    mSoundPool = new SoundPool(MAX_SOUNDS, AudioManager.STREAM_MUSIC, 0);
    loadSounds();
  }
  private void loadSounds() {
    String[] soundNames;
    try {
      soundNames = mAssetManager.list(SOUNDS_FOLDER);
      Log.i(TAG, "Found " + soundNames.length + " sounds");
    } catch (IOException ioe) {
      Log.e(TAG, "Could not list assets", ioe);
      return; }

    for (String filename : soundNames) {

      try {
        String assetPath = SOUNDS_FOLDER + "/" + filename;
        Sound sound = new Sound(assetPath);
        load(sound);
        mSounds.add(sound);
      } catch (IOException e) {
        e.printStackTrace();
      }

    }
  }

  public void play(Sound sound) {
    Integer soundId = sound.getSoundId();
    if (soundId == null) {
      return; }
    mSoundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f);
  }

  public List<Sound> getSounds() {
    return mSounds;
  }

  private void load(Sound sound) throws IOException {
    AssetFileDescriptor afd = mAssetManager.openFd(sound.getAssetPath());
    int soundId = mSoundPool.load(afd, 1);
    sound.setSoundId(soundId);
  }

}