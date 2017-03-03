package ru.lantimat.mradio_new.Adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import ru.lantimat.mradio_new.Audio;
import ru.lantimat.mradio_new.R;


/**
 * Created by Ильназ on 06.11.2015.
 */
public class SongsAdapter extends BaseAdapter {

    Context ctx;
    LayoutInflater inflator;
    ArrayList<Audio> objects;
    String duration;
    Holder holder;
    //View rowView;
    Audio ar;
    int nowPlayPosition = -1;

    public SongsAdapter(Context context, ArrayList<Audio> songsArrayLists) {
        // TODO Auto-generated constructor stub
        ctx = context;
        objects = songsArrayLists;
        inflator = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return objects.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return objects.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }


    public class Holder {
        TextView tvTitle;
        TextView tvDescription;
    }

    public Audio get(int position) {
        return ((Audio) getItem(position));
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        Holder holder;
        View myView = convertView;

        ar = get(position);

        if (myView == null) {
            myView = inflator.inflate(R.layout.item_layout, parent, false);
            holder = new Holder();
            //rowView = lInflater.inflate(R.layout.releases_item_layout, null);
            holder.tvTitle = (TextView) myView.findViewById(R.id.title);
            holder.tvDescription = (TextView) myView.findViewById(R.id.description);

            holder.tvTitle.setText(ar.getTitle());
            holder.tvDescription.setText((ar.getAlbum()));
            Uri uri = Uri.parse(ar.getUrl());


            if (nowPlayPosition == position & nowPlayPosition != -1) {
                holder.tvDescription.setText("Playing");
            } else {
                holder.tvDescription.setText("Pause");
            }
            myView.setTag(holder);
        } else {
            holder = (Holder) myView.getTag();
    }





                /*rowView.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        Toast.makeText(ctx, "You Clicked " + String.valueOf(position), Toast.LENGTH_LONG).show();
                    }
                });

        //ar = getImagesArrayList(position);
        View myView = convertView;
        if (convertView == null) {
            myView = inflator.inflate(R.layout.releases_item_layout, parent, false);
            holder = new Holder();
            holder.tv = (TextView) myView.findViewById(R.id.textView1);
            holder.img = (ImageView) myView.findViewById(R.id.imageView1);
            myView.setTag(holder);
        } else {
            holder = (Holder) myView.getTag();
        }

        holder.tv.setText(ar.title);
        if (!ar.urlImage.equals(ServiceManager.NULL)) {
            Uri uri = Uri.parse(ar.urlImage);
            Picasso.with(ctx) //�������� �������� ����������
                    .load(uri)
                    .into(((ImageView) myView.findViewById(R.id.imageView1)));
        }*/

        return myView;
    }
}

