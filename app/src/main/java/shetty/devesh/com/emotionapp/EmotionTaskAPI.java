package shetty.devesh.com.emotionapp;

import retrofit2.Call;
import retrofit2.http.GET;
import shetty.devesh.com.emotionapp.model.SongFetcher;

/**
 * Created by deveshshetty on 18/04/17.
 */

public interface EmotionTaskAPI {

  @GET(Constant.SLUG_HAPPY_SONGS)
  Call<SongFetcher> fetchHappySongs();

}
