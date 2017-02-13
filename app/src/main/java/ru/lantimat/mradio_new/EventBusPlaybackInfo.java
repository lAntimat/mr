package ru.lantimat.mradio_new;

/**
 * Created by Ильназ on 06.01.2017.
 */
public class EventBusPlaybackInfo {
    Boolean isPlaying;

    public EventBusPlaybackInfo(Boolean isPlaying) {
        this.isPlaying = isPlaying;
    }

    public Boolean getIsPlaying() {
        return isPlaying;
    }

    public void setIsPlaying(Boolean isPlaying) {
        this.isPlaying = isPlaying;
    }
}
