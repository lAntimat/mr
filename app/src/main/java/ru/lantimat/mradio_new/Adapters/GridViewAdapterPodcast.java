package ru.lantimat.mradio_new.Adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import ru.lantimat.mradio_new.R;
import ru.lantimat.mradio_new.models.PodcastResultModel;


/**
 * Created by Ильназ on 06.11.2015.
 */
public class GridViewAdapterPodcast extends BaseAdapter {

    Context ctx;
    LayoutInflater inflator;
    ArrayList<Podcast> objects;
    String duration;
    Holder holder;
    private List<PodcastResultModel> podcast;
    //View rowView;
    Podcast ar;
    PodcastResultModel podcas;

    public GridViewAdapterPodcast(Context context, List<PodcastResultModel> _podcast) {
        // TODO Auto-generated constructor stub
        ctx = context;
        //objects = ImagesArrayLists;
        podcast = _podcast;
        inflator = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return podcast.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return podcast.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public class Holder {
        TextView tv;
        ImageView img;

    }

    public Podcast getImagesArrayList(int position) {
        return ((Podcast) getItem(position));
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub

            Holder holder;
            View myView;
        //= convertView;

        //ar = getImagesArrayList(position);
           podcas = podcast.get(position);

        //if (myView == null) {
            myView = inflator.inflate(R.layout.podcast_item_layout, parent, false);
            holder = new Holder();
            holder.tv = (TextView) myView.findViewById(R.id.textView1);
            holder.img = (ImageView) myView.findViewById(R.id.imageView1);
            holder.tv.setText(podcas.getName());
            Uri uri = Uri.parse(podcas.getCover());
            Picasso.with(ctx).load(uri).into(holder.img);
            //myView.setTag(holder);
        //} else {
          //  holder = (Holder) myView.getTag();
   // }




        return myView;
    }
}

