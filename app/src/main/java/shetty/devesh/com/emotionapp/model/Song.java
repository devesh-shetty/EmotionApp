package shetty.devesh.com.emotionapp.model;

/**
 * Created by deveshshetty on 26/03/17.
 */

public class Song {
  private String mPath;
  private String mName;
  private int index;

  public int getIndex() {
    return index;
  }
  public void setIndex(int index) {
    this.index = index;
  }
  public Song(String path) {
    mPath = path;
    String[] components = path.split("/");
    String filename = components[components.length - 1];
    mName = filename.replace(".mp3", "");
  }
  public String getPath() {
    return mPath;
  }
  public String getName() {
    return mName;
  }

}