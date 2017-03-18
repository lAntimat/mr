package ru.lantimat.mradio_new.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Ильназ on 16.03.2017.
 */

public class SongItems {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("link")
    @Expose
    private String link;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("song")
    @Expose
    private String song;
    @SerializedName("playlist")
    @Expose
    private String playlist;
    @SerializedName("image600")
    @Expose
    private String image600;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public SongItems withId(Integer id) {
        this.id = id;
        return this;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public SongItems withLink(String link) {
        this.link = link;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public SongItems withTitle(String title) {
        this.title = title;
        return this;
    }

    public String getSong() {
        return song;
    }

    public void setSong(String song) {
        this.song = song;
    }

    public SongItems withSong(String song) {
        this.song = song;
        return this;
    }

    public String getPlaylist() {
        return playlist;
    }

    public void setPlaylist(String playlist) {
        this.playlist = playlist;
    }

    public SongItems withPlaylist(String playlist) {
        this.playlist = playlist;
        return this;
    }

    public String getImage600() {
        return image600;
    }

    public void setImage600(String image600) {
        this.image600 = image600;
    }

    public SongItems withImage600(String image600) {
        this.image600 = image600;
        return this;
    }

}
