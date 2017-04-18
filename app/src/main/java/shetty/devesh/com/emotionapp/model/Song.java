package shetty.devesh.com.emotionapp.model;

import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;

/**
 * Created by deveshshetty on 26/03/17.
 *
 * TODO: update to using a builder pattern
 *
 */
@IgnoreExtraProperties
public class Song implements Serializable{

  private long id;

  private String mPath;
  private String name;
  private int index;

  private String link;

  public Song(String name, String link) {
    this.name = name;
    this.link = link;
  }

  public int getIndex() {
    return index;
  }
  public void setIndex(int index) {
    this.index = index;
  }

  public Song() {
  }

  public Song(String path) {
    mPath = path;
    String[] components = path.split("/");
    String filename = components[components.length - 1];
    name = filename.replace(".mp3", "");
  }

  public Song(long id, String mPath, String mName, int index) {
    this.id = id;
    this.mPath = mPath;
    this.name = mName;
    this.index = index;
  }
  public String getPath() {
    return mPath;
  }
  public String getName() {
    return name;
  }

  public String getLink() {
    return link;
  }
  public void setLink(String link) {
    this.link = link;
  }
  @Override
  public String toString() {
    return "Song[ Name: "+name+" ], [link: "+link+"]";
  }
}