package ru.lantimat.mradio_new;

/**
 * Created by Ильназ on 20.01.2017.
 */
public class Slider {

    String title;
    String url;

    public Slider() {

    }

    public Slider(String title, String url) {
        this.title = title;
        this.url = url;
    }

    @Override
    public boolean equals(Object object)
    {
        boolean isEqual= false;

        if (object != null && object instanceof Slider)
        {
            isEqual = (this.title == ((Slider) object).title);
        }

        return isEqual;
    }

    @Override
    public int hashCode() {
        return Integer.parseInt(this.title);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
