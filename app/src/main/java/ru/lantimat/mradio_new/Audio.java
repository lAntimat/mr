package ru.lantimat.mradio_new;

/**
 * Created by Ильназ on 05.01.2017.
 */
import java.io.Serializable;

/**
 * Created by Valdio Veliu on 16-07-18.
 */
public class Audio implements Serializable {

    private String title;
    private String album;
    private String url;
    private String imgUrl;
    private String duration;

    public Audio(String title, String album, String url, String imgUrl, String duration) {
        this.title = title;
        this.album = album;
        this.url = url;
        this.imgUrl = imgUrl;
        this.duration = duration;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}
