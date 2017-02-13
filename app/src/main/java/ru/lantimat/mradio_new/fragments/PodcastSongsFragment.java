package ru.lantimat.mradio_new.fragments;


import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;

import ru.lantimat.mradio_new.Adapters.RecyclerView_Adapter;
import ru.lantimat.mradio_new.Adapters.Song;
import ru.lantimat.mradio_new.Adapters.SongsAdapter;
import ru.lantimat.mradio_new.Audio;
import ru.lantimat.mradio_new.CustomTouchListener;
import ru.lantimat.mradio_new.MainActivity;
import ru.lantimat.mradio_new.MediaPlayerService;
import ru.lantimat.mradio_new.R;
import ru.lantimat.mradio_new.StorageUtil;

/**
 * Created by Ильназ on 05.01.2017.
 */
public class PodcastSongsFragment extends Fragment {

    String TAG = "PodcastSongFragment";

    View view;
    ListView listView;
    SongsAdapter songsAdapter;
    ArrayList<Song> songsArrayList = new ArrayList<>();
    ArrayList<Audio> audioList = new ArrayList<>();
    ImageView collapsingImageView;
    String imgUrl;
    String toolbarString;

    private MediaPlayerService player;
    boolean serviceBound = false;

    RecyclerView recyclerView;
    RecyclerView_Adapter adapter;
    public PodcastSongsFragment() {}
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_podcast_songs2, container, false);

        imgUrl = getArguments().getString("message");
        //imgUrl = "http://rcysl.com/wp-content/uploads/2017/01/Awesome-Beautiful-Stars-Images-.jpg";
        Log.d(TAG, "URL" + imgUrl);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);

        collapsingImageView = (ImageView) view.findViewById(R.id.collapsingImageView);

        loadCollapsingImage();
        loadAudio();
        initRecyclerView();
        //bindService();
        //loadAudioToService();



       /* FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                playAudio("https://upload.wikimedia.org/wikipedia/commons/6/6c/Grieg_Lyric_Pieces_Kobold.ogg");
                //play the first audio in the ArrayList
//                playAudio(2);
            }
        });*/


        new ParseSongs().execute();

        return view;
    }


    //Binding this Client to the AudioPlayer Service
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            player = binder.getService();
            serviceBound = true;

            Toast.makeText(getContext(), "Service Bound", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);
        }

    @Override
    public void onStart() {
        register_playbackStatus();
        super.onStart();
    }

    @Override
    public void onPause() {
        getActivity().unregisterReceiver(playBackStatus);
        super.onPause();
    }

    private void initRecyclerView() {
        if (audioList.size() > 0) {
            recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview2);
            adapter = new RecyclerView_Adapter(audioList, getActivity().getApplicationContext());
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.addOnItemTouchListener(new CustomTouchListener(getContext(), new CustomTouchListener.onItemClickListener() {
                @Override
                public void onClick(View view, int index) {
                    //playAudio(index);
                    //Toast.makeText(getContext(), "Индекс" + String.valueOf(index), Toast.LENGTH_SHORT).show();
                    //EventBus.getDefault().post(new EventBusStartAudio(index));

                    StorageUtil storage = new StorageUtil(getContext());
                    //storage.clearCachedAudioPlaylist();
                    storage.storeAudio(audioList);
                    storage.storeAudioIndex(index);
                    storage.storePlayType(2);

                    Intent broadcastIntent = new Intent(MainActivity.Broadcast_PLAY_NEW_AUDIO);
                    getActivity().sendBroadcast(broadcastIntent);
                    //recyclerView.invalidate();

                    //adapter.notifyDataSetChanged();
                    //recyclerView.invalidate();


                }
            }));

        }
    }

    private void loadCollapsingImage() {
        Uri uri = Uri.parse(imgUrl);
        Picasso.with(getActivity().getApplicationContext())
                .load(uri)
                .into(collapsingImageView);
    }


    public void loadAudio() {
        audioList.add(new Audio("Молодежное радио", "SportNews", "https://psv4.vk.me/c815131/u42191302/audios/782cd6b952eb.mp3?extra=FO1TgPe213C3bxHTBH7Zb3b8V3gsqFnSkgHX3In7R0FmWMY13hcOWRDuYy6UuzYiuRGMZu5E57DgEB7aLChgP4xrb4T-LyJ45CXGfIl6PSHZsl7kYO39DuBoRUEZawfmtXL_aL5mnY4IaVw", imgUrl, "01:20"));
        audioList.add(new Audio("Rockabye", "2", "https://europaplus.ru/sound/1478515228_Clean_Bandit_feat_Anne-Marie__Sean_Paul_-_Rockabye.mp3", imgUrl, "01:10"));
        audioList.add(new Audio("Human", "3", "https://europaplus.ru/sound/1480697867_RAG_N_BONE_MAN_-_Human.mp3", imgUrl, "02:30"));
        audioList.add(new Audio("Monatik", "4", "https://europaplus.ru/sound/1477670208_MONATIK_-_kruzhit.mp3", imgUrl, "03:30"));
        audioList.add(new Audio("J-Mafia", "5", "https://europaplus.ru/sound/1467970972_Effective_Radio_-_J-Mafia.mp3", imgUrl, "02:20"));

        //songsAdapter = new SongsAdapter(getActivity().getApplicationContext(), songsArrayList);
        //recy.setAdapter(songsAdapter);
    }

    private void playAudio(int audioIndex) {
        //Check is service is active
        if (!serviceBound) {
            //Store Serializable audioList to SharedPreferences
            StorageUtil storage = new StorageUtil(getActivity().getApplicationContext());
            storage.storeAudio(audioList);
            storage.storeAudioIndex(audioIndex);

            Intent playerIntent = new Intent(getActivity().getApplicationContext(), MediaPlayerService.class);
            getActivity().startService(playerIntent);
            getActivity().bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        } else {
            //Store the new audioIndex to SharedPreferences
            StorageUtil storage = new StorageUtil(getActivity().getApplicationContext());
            storage.storeAudioIndex(audioIndex);

            //Service is active
            //Send a broadcast to the service -> PLAY_NEW_AUDIO
            Intent broadcastIntent = new Intent(MainActivity.Broadcast_PLAY_NEW_AUDIO);
            getActivity().sendBroadcast(broadcastIntent);
        }
    }

    private void bindService() {
        if (!serviceBound) {

            StorageUtil storage = new StorageUtil(getActivity().getApplicationContext());
            storage.storeAudio(audioList);
            storage.storeAudioIndex(0);

            Intent playerIntent = new Intent(getActivity().getApplicationContext(), MediaPlayerService.class);
            playerIntent.putExtra("onlyBind", true);
            getActivity().startService(playerIntent);
            getActivity().bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    private void loadAudioToService() {
        StorageUtil storage = new StorageUtil(getActivity().getApplicationContext());
        //storage.clearCachedAudioPlaylist();
        storage.storeAudio(audioList);
        storage.storeAudioIndex(0);
    }

    @Override
    public void onDestroy() {
        if (serviceBound) {
            //getActivity().unbindService(serviceConnection);
            //service is active
            //player.stopSelf();
        }
        super.onDestroy();
    }

    public void register_playbackStatus() {
        //Register playNewMedia receiver
        IntentFilter filter = new IntentFilter(MainActivity.Broadcast_PLAYBACKSTATUS);
        getContext().registerReceiver(playBackStatus, filter);
    }

    private BroadcastReceiver playBackStatus = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            adapter.notifyDataSetChanged();
            recyclerView.invalidate();


        }
    };

    public class ParseSongs extends AsyncTask<String, Void, String> {

        Document document = null;  //Суды парсим страницу
        Elements iventsArea;       //Суды участок с мероприятиями
        Elements iventsRow;       //Суды участок с мероприятием
        Elements title;
        Elements subTitle;
        Elements img;
        Elements func2;
        Elements songUrl;
        Elements data;
        Elements dayOfWeek;

        @Override
        protected void onPreExecute() {


            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {


                try {
                    document = Jsoup.connect("http://www.europaplus.ru/index.php?go=Chart40")
                            .get();
                } catch (Exception e) {
                    e.printStackTrace();
                }


            if(document!=null) {

                iventsArea = document.select("ul.songs-list.players-list");
                iventsRow = iventsArea.select("div.jp_container");

                for (int i = 0; i < iventsRow.size(); i++) {
                    title = iventsRow.get(i).select("strong.title");
                    subTitle = iventsRow.get(i).select("span");
                    img = iventsRow.get(i).select("img");
                    songUrl = iventsRow.get(i).select("a.jp-play");



                            Log.d(TAG, "Title " + title.text());
                            //Log.d(TAG, "SubTitle " + subTitle);
                            Log.d(TAG, "Img " + img.attr("src").substring(2).replace("www.", "https://"));
                            Log.d(TAG, "SongUrl " + songUrl.attr("data-url").substring(2).replace("www.", "https://"));

                            audioList.add(new Audio(title.text(), "", songUrl.attr("data-url").substring(2).replace("www.", "https://"), img.attr("src").substring(2).replace("www.", "https://"),  "0"));

                        }
                    }


            //Log.d(TAG, "Headers " + res.headers().toString());
            // Log.d(TAG, "Headers " + res.charset());


            return null;
        }

        @Override
        protected void onPostExecute(String s) {


            //loadAudioToService();
            initRecyclerView();
            //textView.setText(doc2.text());
            //webView.setVisibility(View.GONE);

            super.onPostExecute(s);
        }
    }



}
