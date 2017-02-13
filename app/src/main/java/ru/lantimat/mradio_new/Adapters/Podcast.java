package ru.lantimat.mradio_new.Adapters;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by Ильназ on 06.11.2015.
 */

@IgnoreExtraProperties
public class Podcast {

        public String title;
        public String urlImage;
        public String urlImageBig;

    public Podcast() {
    }

    public Podcast(String title, String urlImage, String urlImageBig) {
        this.title = title;
        this.urlImage = urlImage;
        this.urlImageBig = urlImageBig;
    }
}
