package ru.lantimat.mradio_new.Adapters;

/**
 * Created by ������ on 10.09.2015.
 */
public class Song {

    String title;
    String artist;
    String url;
    String duration;


    public Song(String _title, String _artist, String _url, String _duration) {
        title = _title;
        artist = _artist;
        url = _url;
        duration = _duration;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}
