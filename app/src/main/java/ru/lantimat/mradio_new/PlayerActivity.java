package ru.lantimat.mradio_new;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

public class PlayerActivity extends AppCompatActivity {

    TextView tvTitle;
    TextView tvName;
    TextView tvDuration;
    TextView tvDurationNow;
    ImageView albumImg;

    Button btnPlay;
    Button btnNext;
    Button btnPrev;
    Button btnStop;

    SeekBar seekBar;

    private MediaPlayerService player;
    boolean serviceBound = false;
    boolean audioPlaying;

    int progressGlobal;
    String durationMax;
    String durationNow;

    private ArrayList<Audio> audioList;
    private int audioIndex = -1;
    private Audio activeAudio; //an object on the currently playing audio

    ViewPager viewPager;

    //Binding this Client to the AudioPlayer Service
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            player = binder.getService();
            serviceBound = true;

            Toast.makeText(getApplicationContext(), "Service Bound PlayerActivity", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //serviceBound = false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        bindService();
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvName = (TextView) findViewById(R.id.tvNowPlay);
        tvDuration = (TextView) findViewById(R.id.tvDurat);
        tvDurationNow = (TextView) findViewById(R.id.tvNowDurat);

        albumImg = (ImageView) findViewById(R.id.imageView);

        btnPlay = (Button) findViewById(R.id.btnPlayPause);
        btnNext = (Button) findViewById(R.id.btnNext);
        btnPrev = (Button) findViewById(R.id.btnPrev);

        viewPager = (ViewPager) findViewById(R.id.pager);

        btnNext.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_skip_next_white_36dp,0,0,0);
        btnPrev.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_skip_previous_white_36dp,0,0,0);


        //initViewPager();
        StorageUtil storage = new StorageUtil(getApplicationContext());
        audioList = storage.loadAudio();
        audioIndex = storage.loadAudioIndex();




        buttonsListeners();
        initSeekBar();

        register_playbackStatus();
        register_playbackInfo();
        register_playbackDurationInfo();

    }

    private void initViewPager() {
            /*infiniteCycleViewPager.setAdapter(new HorizontalPagerAdapter(getApplicationContext()));
            infiniteCycleViewPager.setScrollDuration(500);
            infiniteCycleViewPager.setMediumScaled(true);
            infiniteCycleViewPager.setMaxPageScale(0.8F);
            infiniteCycleViewPager.setMinPageScale(0.5F);
            infiniteCycleViewPager.setCenterPageScaleOffset(30.0F);
            infiniteCycleViewPager.setMinPageScaleOffset(5.0F);

        infiniteCycleViewPager.notifyDataSetChanged();*/

        }

    @Override
    protected void onStart() {
        register_playbackStatus();
        register_playbackInfo();
        register_playbackDurationInfo();
        super.onStart();

    }

    @Override
    protected void onPause() {
        unregisterReceiver(playBackInfo);
        unregisterReceiver(playBackStatus);
        unregisterReceiver(playBackDurationInfo);
        try {
            //unbindService(serviceConnection);
        } catch (Exception e) {

        }

        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
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
        /*if(event.getIsPlaying()) {
            tvBtn.setText("Playing");
        } else tvBtn.setText("Pause");*/
        audioPlaying = event.getIsPlaying();
        btnPlaySetIcon(audioPlaying);
    }


    private void bindService() {
        //if (!serviceBound) {
        Intent playerIntent = new Intent(getApplicationContext(), MediaPlayerService.class);
        startService(playerIntent);
        bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
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
    private void next() {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(MediaPlayerService.ACTION_NEXT);
        sendBroadcast(broadcastIntent);
    }
    private void prev() {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(MediaPlayerService.ACTION_PREVIOUS);
        sendBroadcast(broadcastIntent);
    }

    private void buttonsListeners() {
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (audioPlaying) {
                    pause();
                } else {
                    play();
                }
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                next();
            }
        });

        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prev();
            }
        });

    }
    public void initSeekBar() {
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        //seekBar.setVisibility(View.INVISIBLE);
        seekBar.getProgressDrawable().setColorFilter(new PorterDuffColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY));
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                /*progressGlobal = progress;
                //seekBar.setSecondaryProgress(progress*2);
                int a = progress / 1000 / 60;
                int b = progress / 1000 % 60;
                if (b < 10) {
                    String c = a + ":0" + b;
                    durationNow = c;
                } else {
                    String c = a + ":" + b;
                    durationNow = c;
                }
                    tvDuration.setText(durationNow + "/" + durationMax);*/

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                /*try {
                    iService.seek(progressGlobal * 1000);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }*/

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void btnPlaySetIcon(Boolean bool) {
        if (!bool)
            btnPlay.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_play_white_48dp, 0, 0, 0);
        else
            btnPlay.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pause_white_48dp, 0, 0, 0);
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
            //viewPager.setCurrentItem(intent.getIntExtra(MediaPlayerService.POSITION, 0), true);
            Uri uri = Uri.parse(intent.getStringExtra(MediaPlayerService.IMGURL));
            if(uri!=null) {
                Picasso.with(getApplicationContext())
                        .load(uri)
                        .into(albumImg);
            }
        }
    };

    public void register_playbackDurationInfo() {
        //Register playNewMedia receiver
        IntentFilter filter = new IntentFilter(MainActivity.Broadcast_DURATIONINFO);
        registerReceiver(playBackDurationInfo, filter);
    }

    private BroadcastReceiver playBackDurationInfo = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Обработка данных
            final int durMax;
            final int durNow;
            final int durBuff;
            durMax = intent.getIntExtra("durMax",0);
            durNow = intent.getIntExtra("durNow",0);
            durBuff = intent.getIntExtra("durBuff",0);
            //Log.d("durNow", String.valueOf(durNow));
            //Log.d("durMax", String.valueOf(durMax));
            try {
                int a = durMax/1000 / 60;
                int b = durMax/1000 % 60;
                if (b < 10) {
                    String c = a + ":0" + b;
                    durationMax = c;
                } else {
                    String c = a + ":" + b;
                    durationMax = c;
                }

                a = durNow/1000 / 60;
                b = durNow/1000 % 60;
                if (b < 10) {
                    String c = a + ":0" + b;
                    durationNow = c;
                } else {
                    String c = a + ":" + b;
                    durationNow = c;
                }
            }
            catch (NumberFormatException e) {
                durationNow = "";
                durationMax = "";
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    seekBar.setMax(durMax);
                    seekBar.setProgress(durNow);
                    seekBar.setSecondaryProgress(durBuff);
                    tvDurationNow.setText(durationNow);
                    tvDuration.setText(durationMax);
                }
            });
        }
    };

}
