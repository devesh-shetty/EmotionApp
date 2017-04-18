package shetty.devesh.com.emotionapp.model;

/**
 * Created by deveshshetty on 18/04/17.
 */

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SongFetcher {

  @SerializedName("error")
  @Expose
  private Boolean error;
  @SerializedName("status")
  @Expose
  private Integer status;
  @SerializedName("slug")
  @Expose
  private String slug;
  @SerializedName("data")
  @Expose
  private List<String> data = null;

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
   * @param slug
   */
  public SongFetcher(Boolean error, Integer status, String slug, List<String> data) {
    super();
    this.error = error;
    this.status = status;
    this.slug = slug;
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

  public String getSlug() {
    return slug;
  }

  public void setSlug(String slug) {
    this.slug = slug;
  }

  public List<String> getData() {
    return data;
  }

  public void setData(List<String> data) {
    this.data = data;
  }

  @Override
  public String toString() {

    StringBuilder res = new StringBuilder("{ ");
    for (String x:
         data) {
      res.append(x+" ");
    }
    res.append(" }, ");

    res.append(" [slug = {"+slug+"}] ");

    return res.toString();
  }
}