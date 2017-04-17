package shetty.devesh.com.emotionapp.model;

/**
 * Created by deveshshetty on 18/04/17.
 */

  import java.util.List;
  import com.google.gson.annotations.Expose;
  import com.google.gson.annotations.SerializedName;

public class SongFetcher {

  @SerializedName("data")
  @Expose
  private List<String> data = null;
  @SerializedName("error")
  @Expose
  private Boolean error;
  @SerializedName("status")
  @Expose
  private Integer status;

  /**
   * No args constructor for use in serialization
   *
   */
  public SongFetcher() {
  }

  /**
   *
   * @param error
   * @param status
   * @param data
   */
  public SongFetcher(List<String> data, Boolean error, Integer status) {
    super();
    this.data = data;
    this.error = error;
    this.status = status;
  }

  public List<String> getData() {
    return data;
  }

  public void setData(List<String> data) {
    this.data = data;
  }

  public Boolean getError() {
    return error;
  }

  public void setError(Boolean error) {
    this.error = error;
  }

  public Integer getStatus() {
    return status;
  }

  public void setStatus(Integer status) {
    this.status = status;
  }

  @Override
  public String toString() {
    String res = "SongFetcher: {  ";
    for (String d : data) {
      res+= d+" ";
    }
    res+= " }";

    return res;
  }
}