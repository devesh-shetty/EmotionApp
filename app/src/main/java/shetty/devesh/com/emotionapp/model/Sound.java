package shetty.devesh.com.emotionapp.model;

/**
 * Created by deveshshetty on 26/03/17.
 */

public class Sound {
  private String mAssetPath;
  private String mName;
  private int index;

  public int getIndex() {
    return index;
  }
  public void setIndex(int index) {
    this.index = index;
  }
  public Sound(String assetPath) {
    mAssetPath = assetPath;
    String[] components = assetPath.split("/");
    String filename = components[components.length - 1];
    mName = filename.replace(".mp3", "");
  }
  public String getAssetPath() {
    return mAssetPath;
  }
  public String getName() {
    return mName;
  }

}