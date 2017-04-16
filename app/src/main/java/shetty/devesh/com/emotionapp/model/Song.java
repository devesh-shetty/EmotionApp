package shetty.devesh.com.emotionapp.model;

import java.io.Serializable;

/**
 * Created by deveshshetty on 26/03/17.
 *
 * TODO: update to using a builder pattern
 *
 */

public class Song implements Serializable{

  private long id;

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

  public Song(long id, String mPath, String mName, int index) {
    this.id = id;
    this.mPath = mPath;
    this.mName = mName;
    this.index = index;
  }
  public String getPath() {
    return mPath;
  }
  public String getName() {
    return mName;
  }

}