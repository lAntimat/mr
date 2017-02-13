package ru.lantimat.mradio_new.fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import es.claucookie.miniequalizerlibrary.EqualizerView;
import ru.lantimat.mradio_new.Audio;
import ru.lantimat.mradio_new.MainActivity;
import ru.lantimat.mradio_new.MediaPlayerService;
import ru.lantimat.mradio_new.R;
import ru.lantimat.mradio_new.Slider;
import ru.lantimat.mradio_new.StorageUtil;

/**
 * Created by Ильназ on 05.01.2017.
 */
public class RadioFragment extends Fragment {

    Button btnPlay;
    ArrayList<Audio> audioList = new ArrayList<>();
    EqualizerView equalizer;
    SliderLayout sliderShow;
    HashMap<String,String> url_maps = new HashMap<String, String>();
    ArrayList<Slider> arSlider = new ArrayList<>();
    TextSliderView textSliderView;
    public RadioFragment() {}
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_radio, container, false);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);

         sliderShow = (SliderLayout) view.findViewById(R.id.slider);


        //set toolbar appearance
        //toolbar.setBackground(R.color.material_blue_grey_800);
        //for crate home button
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setTitle("Радио");


        btnPlay = (Button) view.findViewById(R.id.btnPlayPause);

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadAudioToService();
                Intent broadcastIntent = new Intent(MainActivity.Broadcast_PLAY_RADIO);
                getActivity().sendBroadcast(broadcastIntent);
                btnPlay.setVisibility(View.INVISIBLE);
            }
        });

        btnPlaySetIcon(false);



        equalizer = (EqualizerView) view.findViewById(R.id.equalizer_view);
        equalizer.setVisibility(View.INVISIBLE);


        arSlider = new StorageUtil(getActivity().getApplicationContext()).loadSlider();
        if(arSlider!=null) {
            for (Slider slider : arSlider) {
                sliderShowAddItems(slider);
            }
        }


        //loadAudioToService();
        sliderShow.startAutoCycle(1000, 5000, true);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        fireBase();
        super.onAttach(context);
    }

    @Override
    public void onStart() {
        register_playbackStatus();
        super.onStart();
    }

    private void sliderShowAddItems(Slider slider) {

        //url_maps.put("Hannibal", "https://pp.vk.me/c622816/v622816404/5513d/wFz7SB0wTR8.jpg");
        //url_maps.put("Big Bang Theory", "https://pp.vk.me/c622816/v622816404/55144/JvQg_a-pmTs.jpg");
        //url_maps.put("House of Cards", "https://pp.vk.me/c622816/v622816404/5511d/jT8eeSI5EPM.jpg");

        textSliderView = new TextSliderView(getContext());

        // initialize a SliderLayout
            textSliderView
                    .description(slider.getTitle())
                    .image(slider.getUrl())
                    .setScaleType(BaseSliderView.ScaleType.CenterCrop);

            //add your extra information
            textSliderView.bundle(new Bundle());
            textSliderView.getBundle()
                    .putString("extra",slider.getTitle());

            sliderShow.addSlider(textSliderView);
        }

    private void fireBase() {
        FirebaseDatabase mFirebaseInstance = FirebaseDatabase.getInstance();

        // get reference to 'users' node
        DatabaseReference mFirebaseDatabase = mFirebaseInstance.getReference().child("slider");

        mFirebaseDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList arSlider1 = new ArrayList<Slider>();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Slider slider = postSnapshot.getValue(Slider.class);
                    Log.d("Slider", slider.getUrl());
                    arSlider1.add(slider);
                    try {
                        StorageUtil storage = new StorageUtil(getActivity().getApplicationContext());
                        if(storage.loadSlider()==null) {
                            sliderShowAddItems(slider);
                        }
                        storage.storeSlider(arSlider1);
                    } catch (Exception e) {

                    }


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    private void loadAudioToService() {
        StorageUtil storage = new StorageUtil(getActivity().getApplicationContext());
        //storage.clearCachedAudioPlaylist();

        audioList.add(new Audio("Молодёжное радио", "On Air", "http://80.87.195.139:8000/live", "https://pp.vk.me/c638926/v638926898/15d09/TJauWGjG4wE.jpg", "0"));

        storage.storeAudio(audioList);
        storage.storePlayType(1);
        storage.storeAudioIndex(0);
    }

    private void btnPlaySetIcon(Boolean bool) {
        if (!bool)
            btnPlay.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_play_grey600_36dp, 0, 0, 0);
        else
            btnPlay.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pause_grey600_36dp, 0, 0, 0);
    }

    public void register_playbackStatus() {
        //Register playNewMedia receiver
        IntentFilter filter = new IntentFilter(MainActivity.Broadcast_PLAYBACKSTATUS);
        getActivity().registerReceiver(playBackStatus, filter);
    }

    private BroadcastReceiver playBackStatus = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            StorageUtil storageUtil = new StorageUtil(getActivity().getApplicationContext());
            int playType = storageUtil.loadPlayType();

            Boolean playStatus = intent.getBooleanExtra("playback", false);
            Boolean startPlaying = intent.getBooleanExtra(MediaPlayerService.STARTPLAYING, false);

            if(playStatus & startPlaying & playType == 1) {
                equalizer.setVisibility(View.VISIBLE);
                equalizer.animateBars();
                btnPlay.setVisibility(View.INVISIBLE);
            } else if(playStatus & playType == 1) {
                equalizer.setVisibility(View.INVISIBLE);
                btnPlay.setVisibility(View.INVISIBLE);
            } else {
                btnPlaySetIcon(false);
                btnPlay.setVisibility(View.VISIBLE);
                equalizer.setVisibility(View.INVISIBLE);

            }

        }
    };

    @Override
    public void onPause() {
        getActivity().unregisterReceiver(playBackStatus);
        super.onPause();
    }

    @Override
    public void onStop() {
        if(sliderShow !=null)  sliderShow.stopAutoCycle();
        super.onStop();
    }
}
