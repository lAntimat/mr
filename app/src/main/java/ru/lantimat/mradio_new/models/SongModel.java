package ru.lantimat.mradio_new.models;

/**
 * Created by Ильназ on 16.03.2017.
 */

        import com.google.gson.annotations.Expose;
        import com.google.gson.annotations.SerializedName;

public class SongModel {

    @SerializedName("result")
    @Expose
    private SongsResultModel result;

    public SongsResultModel getResult() {
        return result;
    }

    public void setResult(SongsResultModel result) {
        this.result = result;
    }

    public SongModel withResult(SongsResultModel result) {
        this.result = result;
        return this;
    }

}
