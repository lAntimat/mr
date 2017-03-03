package ru.lantimat.mradio_new.Adapters;

/**
 * Created by Ильназ on 05.01.2017.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import es.claucookie.miniequalizerlibrary.EqualizerView;
import ru.lantimat.mradio_new.Audio;
import ru.lantimat.mradio_new.MainActivity;
import ru.lantimat.mradio_new.MediaPlayerService;
import ru.lantimat.mradio_new.R;
import ru.lantimat.mradio_new.StorageUtil;

/**
 * Created by Valdio Veliu on 16-07-08.
 */
public class RecyclerView_Adapter extends RecyclerView.Adapter<ViewHolder> {

    List<Audio> list = Collections.emptyList();
    Context context;
    int nowPlayPosition = -1;
    Boolean playStatus = false;
    Boolean startPlaying = false;
    AnimationDrawable mAnimationDrawable;


    public RecyclerView_Adapter(List<Audio> list, Context context) {
        this.list = list;
        this.context = context;
        register_playbackStatus();

        //Да да, все через попу, но это для того чтобы узнать, началось воспроизведение или нет
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(MediaPlayerService.ACTION_SEND_PLAYBACK);
        context.sendBroadcast(broadcastIntent);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Inflate the layout, initialize the View Holder
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
        ViewHolder holder = new ViewHolder(v);
        return holder;

    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        //Use the provided View Holder on the onCreateViewHolder method to populate the current row on the RecyclerView
        holder.title.setText(list.get(position).getTitle());
        holder.shortDescription.setText(list.get(position).getAlbum());

        StorageUtil storageUtil = new StorageUtil(context);

        nowPlayPosition = storageUtil.loadAudioIndex();
        int playType = storageUtil.loadPlayType();

        //Toast.makeText(context, nowPlayPosition + " " + position, Toast.LENGTH_SHORT).show();
        //Log.d("RecyclerView", nowPlayPosition + " " + position);


       if (nowPlayPosition == position & playStatus & !startPlaying & playType == 2) {
            //holder.equalizer.animateBars(); // Whenever you want to tart the animation
            //holder.equalizer.setVisibility(View.VISIBLE);
            holder.play_eq.setBackgroundResource(R.drawable.ic_equalizer_white_36dp);


        } if(nowPlayPosition == position & playStatus & startPlaying & playType == 2) {

            //holder.equalizer.animateBars();
            //holder.play_eq.setImageResource(R.drawable.ic_equalizer_white_36dp);
            holder.play_eq.setBackgroundResource(R.drawable.ic_equalizer_white_36dp);
            mAnimationDrawable = (AnimationDrawable) holder.play_eq.getBackground();
            mAnimationDrawable.start();
            Log.d("RecyclerView", "AnimationDrawable  start");
            holder.play_eq.invalidate();



        } else if(nowPlayPosition!= position) {
           // holder.equalizer.stopBars();
            //holder.equalizer.setVisibility(View.INVISIBLE);
            //holder.play_eq.setImageResource(R.drawable.ic_play_black_36dp);
            holder.play_eq.setBackgroundResource(R.drawable.ic_play_grey600_36dp);

        }


    }

    @Override
    public int getItemCount() {
        //returns the number of elements the RecyclerView will display
        return list.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void register_playbackStatus() {
        //Register playNewMedia receiver
        IntentFilter filter = new IntentFilter(MainActivity.Broadcast_PLAYBACKSTATUS);
        context.registerReceiver(playBackStatus, filter);
    }

    private BroadcastReceiver playBackStatus = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //nowPlayPosition = intent.getIntExtra(MediaPlayerService.POSITION, -1);
            //viewPager.setCurrentItem(intent.getIntExtra(MediaPlayerService.POSITION, 0), true);
            playStatus = intent.getBooleanExtra("playback", false);
            startPlaying = intent.getBooleanExtra(MediaPlayerService.STARTPLAYING, false);
            /*if(mAnimationDrawable!=null) {
                if (startPlaying) mAnimationDrawable.start();
            }*/

            Log.d("RecyclerView", "playStatus " + playStatus);
            Log.d("RecyclerView", "startPlaying " + startPlaying);
            //notifyDataSetChanged();

        }
    };
}


class ViewHolder extends RecyclerView.ViewHolder {

    TextView title;
    TextView shortDescription;
    ImageView play_eq;
    EqualizerView equalizer;


    ViewHolder(View itemView) {
        super(itemView);
        title = (TextView) itemView.findViewById(R.id.title);
        shortDescription = (TextView) itemView.findViewById(R.id.description);
        play_eq = (ImageView) itemView.findViewById(R.id.play_eq);
        //equalizer = (EqualizerView) itemView.findViewById(R.id.equalizer_view);


    }


}
