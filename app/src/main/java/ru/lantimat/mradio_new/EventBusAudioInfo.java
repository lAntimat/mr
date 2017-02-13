package ru.lantimat.mradio_new;

/**
 * Created by Ильназ on 06.01.2017.
 */
public class EventBusAudioInfo {
    public String title;
    public String name;
    public String imgUrl;
    public String duration;

    public EventBusAudioInfo(String title, String name, String imgUrl, String duration) {
        this.title = title;
        this.name = name;
        this.imgUrl = imgUrl;
        this.duration = duration;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
