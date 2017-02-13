package ru.lantimat.mradio_new;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import ru.lantimat.mradio_new.fragments.EventBusStartAudio;
import ru.lantimat.mradio_new.fragments.MoreFragment;
import ru.lantimat.mradio_new.fragments.PodcastFragment;
import ru.lantimat.mradio_new.fragments.RadioFragment;

public class MainActivity extends AppCompatActivity {

    public static final String Broadcast_PLAY_NEW_AUDIO = "ru.lantimat.mradio_new.PlayNewAudio";
    public static final String Broadcast_PLAY_RADIO = "ru.lantimat.mradio_new.PLAY_RADIO";
    public static final String Broadcast_CONTROL = "ru.lantimat.mradio_new.PlayPause";
    public static final String Broadcast_PLAYBACKSTATUS = "ru.lantimat.mradio_new.Broadcast_PLAYBACKSTATUS";
    public static final String Broadcast_PLAYBACKINFO = "ru.lantimat.mradio_new.Broadcast_PLAYBACKINFO";
    public static final String Broadcast_DURATIONINFO = "ru.lantimat.mradio_new.Broadcast_DURATIONINFO";


    private MediaPlayerService player;
    boolean serviceBound = false;

    TextView tvTitle;
    TextView tvName;
    Button btnPlayMini;
    boolean audioPlaying;

    Toolbar toolbar;


    //Binding this Client to the AudioPlayer Service
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            player = binder.getService();
            serviceBound = true;

            //Toast.makeText(MainActivity.this, "Service Bound", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvName = (TextView) findViewById(R.id.tvName);
        btnPlayMini = (Button) findViewById(R.id.btnPlayMini);

        btnPlayMini.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (audioPlaying) {
                    pause();
                } else {
                    play();
                }
            }
        });

        btnPlaySetIcon(false);


        BottomBar bottomBar = (BottomBar) findViewById(R.id.bottomBar);
        if (bottomBar != null) {
            bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
                @Override
                public void onTabSelected(@IdRes int tabId) {

                    Fragment fragment = null;

                    // на основании выбранного элемента меню
                    // вызываем соответственный ему фрагмент
                    switch (tabId) {
                        case R.id.tab_radio:
                            fragment = new RadioFragment();
                            break;
                        case R.id.tab_podcast:
                            fragment = new PodcastFragment();
                            break;
                        case R.id.tab_more:
                            fragment = new MoreFragment();
                            break;

                        default:
                            break;
                    }
                    if (fragment != null) {
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
                    }
                }
            });
        }




    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("ServiceState", serviceBound);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        serviceBound = savedInstanceState.getBoolean("ServiceState");
    }

    @Override
    protected void onStart() {
        super.onStart();


    }

    @Override
    protected void onResume() {
        bindService();
        register_playbackStatus();
        register_playbackInfo();
        super.onResume();
    }

    @Override
    protected void onPause() {
       //unregisterReceiver(playBackInfo);
       //unregisterReceiver(playBackStatus);
        try {
            //unbindService(serviceConnection);
        } catch (Exception e) {

        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe
    public void onEvent(EventBusStartAudio event){
        // Обработка данных

        //Store the new audioIndex to SharedPreferences
        StorageUtil storage = new StorageUtil(getApplicationContext());
        storage.storeAudioIndex(event.getIndex());

        //Service is active
        //Send a broadcast to the service -> PLAY_NEW_AUDIO
        Intent broadcastIntent = new Intent(Broadcast_PLAY_NEW_AUDIO);
        sendBroadcast(broadcastIntent);
    }

    @Subscribe
    public void onEvent(EventBusAudioInfo event){
        // Обработка данных
        tvTitle.setText(event.getTitle());
        tvName.setText(event.getName());
    }

    @Subscribe
    public void onEvent(EventBusPlaybackInfo event){
        // Обработка данных
        //btnPlaySetIcon(event.getIsPlaying());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    private void bindService() {
        //if (!serviceBound) {
            Intent playerIntent = new Intent(getApplicationContext(), MediaPlayerService.class);
            playerIntent.putExtra("onlyBind", true);
            startService(playerIntent);
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
            Log.d("MainActivity", "Binded");
        // }
    }

    private void play() {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(MediaPlayerService.ACTION_PLAY);
        sendBroadcast(broadcastIntent);
    }
    private void pause() {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(MediaPlayerService.ACTION_PAUSE);
        sendBroadcast(broadcastIntent);
    }


    public void playerMiniClick(View view) {
        Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
        startActivity(intent);
    }

    private void btnPlaySetIcon(Boolean bool) {
        if (!bool)
            btnPlayMini.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_play_grey600_24dp, 0, 0, 0);
        else
            btnPlayMini.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pause_grey600_24dp, 0, 0, 0);
    }


    public void register_playbackStatus() {
        //Register playNewMedia receiver
        IntentFilter filter = new IntentFilter(MainActivity.Broadcast_PLAYBACKSTATUS);
        registerReceiver(playBackStatus, filter);
    }

    private BroadcastReceiver playBackStatus = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            audioPlaying = intent.getBooleanExtra("playback", false);
            btnPlaySetIcon(intent.getBooleanExtra("playback", false));
        }
    };

    public void register_playbackInfo() {
        //Register playNewMedia receiver
        IntentFilter filter = new IntentFilter(MainActivity.Broadcast_PLAYBACKINFO);
        registerReceiver(playBackInfo, filter);
    }

    private BroadcastReceiver playBackInfo = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            btnPlaySetIcon(intent.getBooleanExtra("playback", false));
            tvTitle.setText(intent.getStringExtra(MediaPlayerService.TITLE));
            tvName.setText(intent.getStringExtra(MediaPlayerService.NAME));
        }
    };

    public Fragment getVisibleFragment(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        if(fragments != null){
            for(Fragment fragment : fragments){
                if(fragment != null && fragment.isVisible())
                    return fragment;
            }
        }
        return null;
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            this.finish();
        }
    }


    /*private void playAudio(int audioIndex) {
        //Check is service is active
        if (!serviceBound) {
            //Store Serializable audioList to SharedPreferences
            StorageUtil storage = new StorageUtil(getApplicationContext());
            //storage.storeAudio(audioList);
            storage.storeAudioIndex(audioIndex);

            Intent playerIntent = new Intent(this, MediaPlayerService.class);
            startService(playerIntent);
            //bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        } else {
            //Store the new audioIndex to SharedPreferences
            StorageUtil storage = new StorageUtil(getApplicationContext());
            storage.storeAudioIndex(audioIndex);

            //Service is active
            //Send a broadcast to the service -> PLAY_NEW_AUDIO
            Intent broadcastIntent = new Intent(Broadcast_PLAY_NEW_AUDIO);
            sendBroadcast(broadcastIntent);
        }
    }*/

}
