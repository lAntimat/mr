package ru.lantimat.mradio_new.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PodcastModel {

    @SerializedName("result")
    @Expose
    private List<PodcastResultModel> result = null;

    public List<PodcastResultModel> getResult() {
        return result;
    }

    public void setResult(List<PodcastResultModel> result) {
        this.result = result;
    }

    public PodcastModel withResult(List<PodcastResultModel> result) {
        this.result = result;
        return this;
    }

}
