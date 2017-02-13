package ru.lantimat.mradio_new;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaSessionManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Valdio Veliu on 16-07-11.
 */
public class MediaPlayerService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener,

        AudioManager.OnAudioFocusChangeListener {


    public static String TAG = "MediaPlayerService";

    public static final String ACTION_PLAY = "com.valdioveliu.valdio.audioplayer.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.valdioveliu.valdio.audioplayer.ACTION_PAUSE";
    public static final String ACTION_PREVIOUS = "com.valdioveliu.valdio.audioplayer.ACTION_PREVIOUS";
    public static final String ACTION_NEXT = "com.valdioveliu.valdio.audioplayer.ACTION_NEXT";
    public static final String ACTION_STOP = "com.valdioveliu.valdio.audioplayer.ACTION_STOP";
    public static final String ACTION_STOP_SELF = "com.valdioveliu.valdio.audioplayer.ACTION_STOP_SELF";

    public static final String TITLE = "com.valdioveliu.valdio.audioplayer.TITLE";
    public static final String NAME = "com.valdioveliu.valdio.audioplayer.NAME";
    public static final String IMGURL = "com.valdioveliu.valdio.audioplayer.IMGURL";
    public static final String DURATION = "com.valdioveliu.valdio.audioplayer.DURATION";
    public static final String POSITION = "com.valdioveliu.valdio.audioplayer.POSITION";
    public static final String PLAYBACKSTATUS = "com.valdioveliu.valdio.audioplayer.PLAYBACKSTATUS";
    public static final String STARTPLAYING = "com.valdioveliu.valdio.audioplayer.STARTPLAYING";

    private MediaPlayer mediaPlayer;

    //MediaSession
    private MediaSessionManager mediaSessionManager;
    private MediaSessionCompat mediaSession;
    private MediaControllerCompat.TransportControls transportControls;

    //AudioPlayer notification ID
    private static final int NOTIFICATION_ID = 101;

    //Used to pause/resume MediaPlayer
    private int resumePosition;

    //Длительность Аудио
    int maxDuration = 0;

    //Буферизация плеера
    int bufferedDuration;

    //AudioFocus
    private AudioManager audioManager;

    // Binder given to clients
    private final IBinder iBinder = new LocalBinder();

    //List of available Audio files
    private ArrayList<Audio> audioList;
    private int audioIndex = -1;
    private Audio activeAudio; //an object on the currently playing audio


    //Handle incoming phone calls
    private boolean ongoingCall = false;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;

    NotificationCompat.Builder notificationBuilder;

    Bitmap notificationImg;

    //true когда музыка начала играть
    Boolean isStarted = false;

    /**
     * Service lifecycle methods
     */
    @Override
    public IBinder onBind(Intent intent) {



        return iBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // Perform one-time setup procedures

        // Manage incoming phone calls during playback.
        // Pause MediaPlayer on incoming call,
        // Resume on hangup.
        callStateListener();
        //ACTION_AUDIO_BECOMING_NOISY -- change in audio outputs -- BroadcastReceiver
        registerBecomingNoisyReceiver();
        //Listen for new Audio to play -- BroadcastReceiver
        register_playNewAudio();
        register_audioControl();
        register_playRadio();
    }

    //The system calls this method when an activity, requests the service be started
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Boolean onlyBind;
//        onlyBind = intent.getBooleanExtra("onlyBind", true);

        MediaButtonReceiver.handleIntent(mediaSession, intent);

        try {
            //Load data from SharedPreferences
            StorageUtil storage = new StorageUtil(getApplicationContext());
            audioList = storage.loadAudio();
            audioIndex = storage.loadAudioIndex();

            if (audioIndex != -1 && audioIndex < audioList.size()) {
                //index is in a valid range
                activeAudio = audioList.get(audioIndex);
            } else {
                stopSelf();
            }
        } catch (NullPointerException e) {
            stopSelf();
        }

        //Request audio focus
        if (requestAudioFocus() == false) {
            //Could not gain focus
            stopSelf();
        }

        //if(!onlyBind) {
        if (mediaSessionManager == null) {
            try {
                initMediaSession();
                //-initMediaPlayer();

            } catch (RemoteException e) {
                e.printStackTrace();
                stopSelf();
            }
            // }
        }

        //Handle Intent action from MediaSession.TransportControls
        handleIncomingActions(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mediaSession.release();
        removeNotification();
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            stopMedia();
            mediaPlayer.release();
        }
        removeAudioFocus();
        //Disable the PhoneStateListener
        if (phoneStateListener != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }

        removeNotification();

        //unregister BroadcastReceivers
        unregisterReceiver(becomingNoisyReceiver);
        unregisterReceiver(playNewAudio);

        //clear cached playlist
        new StorageUtil(getApplicationContext()).clearCachedAudioPlaylist();
    }

    /**
     * Service Binder
     */
    public class LocalBinder extends Binder {
        public MediaPlayerService getService() {

            initDurationSendTimer();

            //EventBus отправляет данные о воспроизведении.
            Log.d("MediaPlayerService", "EventBusSendAudioInfo");
            if(activeAudio!=null) {
                EventBus.getDefault().post(new EventBusAudioInfo(activeAudio.getTitle(), activeAudio.getAlbum(), activeAudio.getImgUrl(), activeAudio.getDuration()));
            }
            if(mediaPlayer!=null) {
                if(mediaPlayer.isPlaying()) sendPlayback(true);
                else sendPlayback(false);
            } else sendPlayback(false);

            // Return this instance of LocalService so clients can call public methods

            return MediaPlayerService.this;
        }
    }


    /**
     * MediaPlayer callback methods
     */
    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        //Invoked indicating buffering status of
        //a media resource being streamed over the network.
        if (percent > 0 & percent <= 100) bufferedDuration = percent;
        else bufferedDuration = 0;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        //Invoked when playback of a media source has completed.
        //stopMedia();

        //removeNotification();
        //stop the service
        //stopSelf();
        skipToNext();
        updateMetaData();
        buildNotification(PlaybackStatus.PLAYING);
        sendPlayback(true);

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        //Invoked when there has been an error during an asynchronous operation
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Log.d("MediaPlayer Error", "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.d("MediaPlayer Error", "MEDIA ERROR SERVER DIED " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.d("MediaPlayer Error", "MEDIA ERROR UNKNOWN " + extra);
                break;
        }
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        //Invoked to communicate some info
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        //Invoked when the media source is ready for playback.
        maxDuration = mediaPlayer.getDuration();
        isStarted = true;
        playMedia();
        buildNotification(PlaybackStatus.PLAYING);
        sendPlayback(true);
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        //Invoked indicating the completion of a seek operation.
    }

    @Override
    public void onAudioFocusChange(int focusState) {

        //Invoked when the audio focus of the system is updated.
        switch (focusState) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
               if (mediaPlayer != null) mediaPlayer.setVolume(1.0f, 1.0f);
                Log.d(TAG, "AUDIOFOCUS_GAIN");
                //else if (!mediaPlayer.isPlaying()) mediaPlayer.start();
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                    pauseMedia();
                Log.d(TAG, "AUDIOFOCUS_LOSS");

                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                    pauseMedia();
                Log.d(TAG, "AUDIOFOCUS_LOSS_TRANSIENT");

                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                /*if(mediaPlayer!=null) {
                    if (mediaPlayer.isPlaying()) mediaPlayer.setVolume(0.1f, 0.1f);
                }*/
                Log.d(TAG, "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");

                break;
        }
    }


    /**
     * AudioFocus
     */
    private boolean requestAudioFocus() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            //Focus gained
            return true;
        }
        //Could not gain focus
        return false;
    }

    private boolean removeAudioFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                audioManager.abandonAudioFocus(this);
    }


    /**
     * MediaPlayer actions
     */
    private void initMediaPlayer() {
        if (mediaPlayer == null)
            mediaPlayer = new MediaPlayer();//new MediaPlayer instance
        isStarted = false;

        //Set up MediaPlayer event listeners
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        mediaPlayer.setOnInfoListener(this);
        //Reset so that the MediaPlayer is not pointing to another data source
        mediaPlayer.reset();


        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            // Set the data source to the mediaFile location
            mediaPlayer.setDataSource(activeAudio.getUrl());
        } catch (IOException e) {
            e.printStackTrace();
            stopSelf();
        }
        mediaPlayer.prepareAsync();
        //Загружаем картинку альбома
        notificationImg = null;
        Picasso.with(getApplicationContext()).load(activeAudio.getImgUrl()).into(target);
    }



    private void playMedia() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            buildNotification(PlaybackStatus.PAUSED);
            sendPlayback(true);

        }
    }

    private void stopMedia() {
        if (mediaPlayer == null) return;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            isStarted = false;
        }
    }

    private void pauseMedia() {
        if(mediaPlayer!=null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                resumePosition = mediaPlayer.getCurrentPosition();
                buildNotification(PlaybackStatus.PAUSED);
                sendPlayback(false);
                isStarted = false;
            }
        }
    }

    private void resumeMedia() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(resumePosition);
            mediaPlayer.start();
            buildNotification(PlaybackStatus.PLAYING);
            isStarted = true; //После возобновления
            sendPlayback(true);
        }
    }

    private void skipToNext() {

        if (audioIndex == audioList.size() - 1) {
            //if last in playlist
            audioIndex = 0;
            activeAudio = audioList.get(audioIndex);
        } else {
            //get next in playlist
            activeAudio = audioList.get(++audioIndex);
        }

        //Update stored index
        new StorageUtil(getApplicationContext()).storeAudioIndex(audioIndex);

        stopMedia();
        //reset mediaPlayer
        mediaPlayer.reset();
        initMediaPlayer();
    }

    private void skipToPrevious() {

        if (audioIndex == 0) {
            //if first in playlist
            //set index to the last of audioList
            audioIndex = audioList.size() - 1;
            activeAudio = audioList.get(audioIndex);
        } else {
            //get previous in playlist
            activeAudio = audioList.get(--audioIndex);
        }

        //Update stored index
        new StorageUtil(getApplicationContext()).storeAudioIndex(audioIndex);

        stopMedia();
        //reset mediaPlayer
        mediaPlayer.reset();
        initMediaPlayer();
    }


    /**
     * ACTION_AUDIO_BECOMING_NOISY -- change in audio outputs
     */
    private BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //pause audio on ACTION_AUDIO_BECOMING_NOISY
            pauseMedia();
            buildNotification(PlaybackStatus.PAUSED);
            sendPlayback(false);

        }
    };

    private void registerBecomingNoisyReceiver() {
        //register after getting audio focus
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(becomingNoisyReceiver, intentFilter);
    }

    /**
     * Handle PhoneState changes
     */
    private void callStateListener() {
        // Get the telephony manager
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        //Starting listening for PhoneState changes
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    //if at least one call exists or the phone is ringing
                    //pause the MediaPlayer
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (mediaPlayer != null) {
                            pauseMedia();
                            ongoingCall = true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        // Phone idle. Start playing.
                        if (mediaPlayer != null) {
                            if (ongoingCall) {
                                ongoingCall = false;
                                //resumeMedia();
                            }
                        }
                        break;
                }
            }
        };
        // Register the listener with the telephony manager
        // Listen for changes to the device call state.
        telephonyManager.listen(phoneStateListener,
                PhoneStateListener.LISTEN_CALL_STATE);
    }

    /**
     * MediaSession and Notification actions
     */
    private void initMediaSession() throws RemoteException {
        if (mediaSessionManager != null) return; //mediaSessionManager exists

       // mediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);

        ComponentName mediaButtonReceiver = new ComponentName(getApplicationContext(), MediaButtonReceiver.class);
        //mMediaSessionCompat = new MediaSessionCompat(getApplicationContext(), "Tag", mediaButtonReceiver, null);

        // Create a new MediaSession
        mediaSession = new MediaSessionCompat(getApplicationContext(), "AudioPlayer", mediaButtonReceiver, null);
        //Get MediaSessions transport controls
        transportControls = mediaSession.getController().getTransportControls();
        //set MediaSession -> ready to receive media commands
        mediaSession.setActive(true);
        //indicate that the MediaSession handles transport control commands
        // through its MediaSessionCompat.Callback.
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        //Set mediaSession's MetaData
        updateMetaData();

        // Attach Callback to receive MediaSession updates
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            // Implement callbacks
            @Override
            public void onPlay() {
                super.onPlay();
                if (mediaPlayer == null) initMediaPlayer();
                resumeMedia();
                buildNotification(PlaybackStatus.PLAYING);
                sendPlayback(true);
                startForeground(NOTIFICATION_ID, notificationBuilder.build());

            }

            @Override
            public void onPause() {
                super.onPause();
                pauseMedia();
                buildNotification(PlaybackStatus.PAUSED);
                //stopForeground(false);
                sendPlayback(false);
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();

                skipToNext();
                updateMetaData();
                buildNotification(PlaybackStatus.PLAYING);
                sendPlayback(true);
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();

                skipToPrevious();
                updateMetaData();
                buildNotification(PlaybackStatus.PLAYING);
                sendPlayback(true);
            }

            @Override
            public void onStop() {
                super.onStop();
                removeNotification();
                //Stop the service
                stopSelf();
                //stopForeground(false);
            }

            @Override
            public void onSeekTo(long position) {
                super.onSeekTo(position);
            }
        });
    }

    private void updateMetaData() {
        if(activeAudio!=null) {
            Bitmap albumArt = BitmapFactory.decodeResource(getResources(),
                    R.drawable.ic_access_point_white_48dp); //replace with medias albumArt
            // Update the current metadata
            mediaSession.setMetadata(new MediaMetadataCompat.Builder()
                    .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt)
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, activeAudio.getTitle())
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, activeAudio.getAlbum())
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, activeAudio.getTitle())
                    .build());

            /*try {
                notificationImg =  new generatePictureStyleNotification(getApplicationContext(), audioList.get(audioIndex).getImgUrl()).execute().get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }*/


            EventBus.getDefault().post(new EventBusAudioInfo(activeAudio.getTitle(), activeAudio.getAlbum(), activeAudio.getImgUrl(), activeAudio.getDuration()));
            sendPlayback(activeAudio.getTitle(), activeAudio.getAlbum(), activeAudio.getImgUrl(), activeAudio.getDuration(), audioIndex);

            if(mediaPlayer!=null) {
                if(mediaPlayer.isPlaying()) sendPlayback(true);
                else sendPlayback(false);
            } else sendPlayback(false);
        }

    }

    private PendingIntent createOnDismissedIntent(Context context, int notificationId) {
        Intent intent = new Intent(context, NotificationDismissedReceiver.class);
        intent.putExtra("deleteIntent", notificationId);

        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(context.getApplicationContext(),
                        notificationId, intent, 0);
        return pendingIntent;
    }

    private void buildNotification(PlaybackStatus playbackStatus) {

        /**
         * Notification actions -> playbackAction()
         *  0 -> Play
         *  1 -> Pause
         *  2 -> Next track
         *  3 -> Previous track
         */

        int notificationAction = R.drawable.ic_play_white_24dp;//needs to be initialized
        PendingIntent play_pauseAction = null;

        //Build a new notification according to the current state of the MediaPlayer
        if (playbackStatus == PlaybackStatus.PLAYING) {
            notificationAction = R.drawable.ic_pause_white_24dp;
            //create the pause action
            play_pauseAction = playbackAction(1);

        } else if (playbackStatus == PlaybackStatus.PAUSED) {
            notificationAction = R.drawable.ic_play_white_24dp;
            //create the play action
            play_pauseAction = playbackAction(0);

        }



        //Open Activity

        Intent resultIntent = new Intent(this, PlayerActivity.class);

        // Because clicking the notification opens a new ("special") activity, there's
        // no need to create an artificial back stack.
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        // Create a new Notification
        notificationBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                // Hide the timestamp
                .setShowWhen(false)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                        // Set the Notification style
                .setStyle(new NotificationCompat.MediaStyle()
                        // Attach our MediaSession token
                        .setMediaSession(mediaSession.getSessionToken())
                                // Show our playback controls in the compat view
                        .setShowActionsInCompactView(0, 1, 2))
                        // Set the Notification color
                .setColor(getResources().getColor(R.color.colorAccent))
                        // Set the large and small icons
                .setLargeIcon(notificationImg)
                .setSmallIcon(R.drawable.ic_access_point_white_36dp)
                        // Set Notification content information
                .setContentText(activeAudio.getAlbum())
                .setContentTitle(activeAudio.getTitle())
                .setDeleteIntent(createOnDismissedIntent(getApplicationContext(), NOTIFICATION_ID))
                .setContentIntent(resultPendingIntent)

                // Add playback actions
                .addAction(R.drawable.ic_skip_previous_white_24dp, "previous", playbackAction(3))
                .addAction(notificationAction, "pause", play_pauseAction)
                .addAction(R.drawable.ic_skip_next_white_24dp, "next", playbackAction(2));



        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, notificationBuilder.build());
        if (playbackStatus == PlaybackStatus.PLAYING) {
            startForeground(NOTIFICATION_ID, notificationBuilder.build());
        } else if(playbackStatus == PlaybackStatus.PAUSED) {
            stopForeground(false);
        }
    }

    private Target target = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            // loading of the bitmap was a success
            // TODO do some action with the bitmap
            notificationImg = bitmap;
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            // loading of the bitmap failed
            // TODO do some action/warning/error message
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };

    private PendingIntent playbackAction(int actionNumber) {
        Intent playbackAction = new Intent(this, MediaPlayerService.class);
        switch (actionNumber) {
            case 0:
                // Play
                playbackAction.setAction(ACTION_PLAY);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 1:
                // Pause
                playbackAction.setAction(ACTION_PAUSE);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 2:
                // Next track
                playbackAction.setAction(ACTION_NEXT);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 3:
                // Previous track
                playbackAction.setAction(ACTION_PREVIOUS);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            default:
                break;
        }
        return null;
    }

    private void removeNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    private void handleIncomingActions(Intent playbackAction) {
        if (playbackAction == null || playbackAction.getAction() == null) return;

        String actionString = playbackAction.getAction();
        if (actionString.equalsIgnoreCase(ACTION_PLAY)) {
            transportControls.play();
        } else if (actionString.equalsIgnoreCase(ACTION_PAUSE)) {
            transportControls.pause();
        } else if (actionString.equalsIgnoreCase(ACTION_NEXT)) {
            transportControls.skipToNext();
        } else if (actionString.equalsIgnoreCase(ACTION_PREVIOUS)) {
            transportControls.skipToPrevious();
        } else if (actionString.equalsIgnoreCase(ACTION_STOP)) {
            transportControls.stop();
        }
    }

    public void initDurationSendTimer() {
               Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {

                        try {

                            if (activeAudio != null & mediaPlayer != null & isStarted) {
                                sendDurationInfo(maxDuration, mediaPlayer.getCurrentPosition(), bufferedDuration);
                            }
                        } catch (Exception e) {

                        }

                    }
                }, 1, 1000); // End of your timer code.
    }


    /**
     * Play new Audio
     */
    private BroadcastReceiver playNewAudio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            isStarted = false;

            //Get the new media index form SharedPreferences
            StorageUtil storage = new StorageUtil(getApplicationContext());
            audioList = storage.loadAudio();
            audioIndex = storage.loadAudioIndex();
            if (audioIndex != -1 && audioIndex < audioList.size()) {
                //index is in a valid range
                activeAudio = audioList.get(audioIndex);
            } else {
                stopSelf();
            }

            //A PLAY_NEW_AUDIO action received
            //reset mediaPlayer to play the new Audio
            stopMedia();
            //mediaPlayer.reset();
            initMediaPlayer();
            updateMetaData();
            buildNotification(PlaybackStatus.PLAYING);
            sendPlayback(true);

        }
    };

    private BroadcastReceiver audioControl = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            //Get the new media index form SharedPreferences
            audioIndex = new StorageUtil(getApplicationContext()).loadAudioIndex();
            if (audioIndex != -1 && audioIndex < audioList.size()) {
                //index is in a valid range
                activeAudio = audioList.get(audioIndex);
            } else {
                stopSelf();
            }

            Log.d(TAG, "audioControlIntent");
            switch (intent.getAction()) {
                case ACTION_PLAY:
                   transportControls.play();
                    Log.d(TAG, "ActionPlay");
                    break;
                case ACTION_PAUSE:
                    transportControls.pause();
                    Log.d(TAG, "ActionPause");
                    break;
                case ACTION_NEXT:
                    transportControls.skipToNext();
                    break;
                case ACTION_PREVIOUS:
                    transportControls.skipToPrevious();
                    break;
                case ACTION_STOP:
                    transportControls.stop();
                    break;
                case ACTION_STOP_SELF:
                    Log.d(TAG, "ActionSTOPSELF");

                    stopSelf();
                    break;

            }

        }
    };

    private BroadcastReceiver playRadio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            //Get the new media index form SharedPreferences
            StorageUtil storage = new StorageUtil(getApplicationContext());
            audioList = storage.loadAudio();
            audioIndex = storage.loadAudioIndex();
            if (audioIndex != -1 && audioIndex < audioList.size()) {
                //index is in a valid range
                activeAudio = audioList.get(audioIndex);
            } else {
                stopSelf();
            }

            //A PLAY_NEW_AUDIO action received
            //reset mediaPlayer to play the new Audio
            stopMedia();
            //mediaPlayer.reset();
            initMediaPlayer();
            updateMetaData();
            buildNotification(PlaybackStatus.PLAYING);
            sendPlayback(true);

        }
    };
    private void register_playNewAudio() {
        //Register playNewMedia receiver
        IntentFilter filter = new IntentFilter(MainActivity.Broadcast_PLAY_NEW_AUDIO);
        registerReceiver(playNewAudio, filter);
    }
    private void register_playRadio() {
        //Register playNewMedia receiver
        IntentFilter filter = new IntentFilter(MainActivity.Broadcast_PLAY_RADIO);
        registerReceiver(playRadio, filter);
    }
    private void register_audioControl() {
        //Register playNewMedia receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_PLAY);
        filter.addAction(ACTION_PAUSE);
        filter.addAction(ACTION_NEXT);
        filter.addAction(ACTION_PREVIOUS);
        filter.addAction(ACTION_STOP);
        filter.addAction(ACTION_STOP_SELF);
        registerReceiver(audioControl, filter);
    }

    private void sendPlayback(Boolean bool) {
        Intent broadcastIntent = new Intent(MainActivity.Broadcast_PLAYBACKSTATUS);
        /*if(mediaPlayer!=null) {
            if(mediaPlayer.isPlaying()) broadcastIntent.putExtra("playback", true);
            else broadcastIntent.putExtra("playback",false);
        } else broadcastIntent.putExtra("playback",false);*/
        broadcastIntent.putExtra("playback", bool);
        broadcastIntent.putExtra(STARTPLAYING, isStarted);


        sendBroadcast(broadcastIntent);
    }
    private void sendPlayback(String title, String name, String imgUrl, String duration, int position) {
        Intent broadcastIntent = new Intent(MainActivity.Broadcast_PLAYBACKINFO);
        broadcastIntent.putExtra(TITLE, title);
        broadcastIntent.putExtra(NAME, name);
        broadcastIntent.putExtra(IMGURL, imgUrl);
        broadcastIntent.putExtra(DURATION, duration);
        broadcastIntent.putExtra(POSITION, position);
        sendBroadcast(broadcastIntent);
    }
    private void sendDurationInfo(int durMax, int durNow,int durBuff) {
        Intent broadcastIntent = new Intent(MainActivity.Broadcast_DURATIONINFO);
        broadcastIntent.putExtra("durMax", durMax);
        broadcastIntent.putExtra("durNow", durNow);
        broadcastIntent.putExtra("durBuff", durBuff);

        sendBroadcast(broadcastIntent);
    }



}
    class generatePictureStyleNotification extends AsyncTask<String, Void, Bitmap> {

        private Context mContext;
        private String imageUrl;

        public generatePictureStyleNotification(Context context, String imageUrl) {
            super();
            this.mContext = context;

            this.imageUrl = imageUrl;
        }

        @Override
        protected Bitmap doInBackground(String... params) {

            InputStream in;
            try {
                URL url = new URL(this.imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                in = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(in);
                return myBitmap;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);

            //notificationBuilder.setLargeIcon(result);
            // Because the ID remains unchanged, the existing notification is
            // updated.

            //((NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, notificationBuilder.build());

        }
    }

