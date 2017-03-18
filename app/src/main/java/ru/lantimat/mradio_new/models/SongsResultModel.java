package ru.lantimat.mradio_new.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SongsResultModel {

    @SerializedName("cover")
    @Expose
    private String cover;
    @SerializedName("content_type")
    @Expose
    private String contentType;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("items")
    @Expose
    private List<SongItems> items = null;

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public SongsResultModel withCover(String cover) {
        this.cover = cover;
        return this;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public SongsResultModel withContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SongsResultModel withName(String name) {
        this.name = name;
        return this;
    }

    public List<SongItems> getItems() {
        return items;
    }

    public void setItems(List<SongItems> items) {
        this.items = items;
    }

    public SongsResultModel withItems(List<SongItems> items) {
        this.items = items;
        return this;
    }

}