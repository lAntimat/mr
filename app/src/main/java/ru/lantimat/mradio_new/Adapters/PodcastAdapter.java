package ru.lantimat.mradio_new.Adapters;

/**
 * Created by Ильназ on 16.03.2017.
 */

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import ru.lantimat.mradio_new.R;
import ru.lantimat.mradio_new.models.PodcastResultModel;

public class PodcastAdapter extends RecyclerView.Adapter<PodcastAdapter.ViewHolder> {

    private List<PodcastResultModel> podcast;
    private Context ctx;

    public PodcastAdapter(Context ctx, List<PodcastResultModel> podcast) {
        this.podcast = podcast;
        this.ctx = ctx;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.podcast_item_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        PodcastResultModel podcas = podcast.get(position);
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder.post.setText(Html.fromHtml(podcas.getName(), Html.FROM_HTML_MODE_LEGACY));
        } else {
            holder.post.setText(Html.fromHtml(podcas.getName()));
        }*/
        holder.title.setText(podcas.getName());
        Picasso.with(ctx).load(podcas.getCover()).into(holder.iv);
    }

    @Override
    public int getItemCount() {
        if (podcast == null)
            return 0;
        return podcast.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView iv;

        public ViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.textView1);
            iv = (ImageView) itemView.findViewById(R.id.imageView1);
        }
    }
}
