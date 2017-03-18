package ru.lantimat.mradio_new;

/**
 * Created by Ильназ on 16.03.2017.
 */

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import ru.lantimat.mradio_new.models.PodcastModel;
import ru.lantimat.mradio_new.models.SongModel;

//http://www.radiorecord.ru/radioapi/podcast/?id=5990
public interface RecordApi {
    @GET("/radioapi/podcasts/")
    Call<PodcastModel> getPodcast();
    @GET("radioapi/podcast/?id=5990")
    Call<SongModel> getSongs(@Query("id") int count);

}

