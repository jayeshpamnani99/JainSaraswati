package com.saraswati.jain.jainsaraswati.Services;


import android.app.DownloadManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.saraswati.jain.jainsaraswati.Activities.HomeActivity;
import com.saraswati.jain.jainsaraswati.Models.Music;
import com.saraswati.jain.jainsaraswati.R;

import java.io.File;
import java.io.IOException;

import static com.saraswati.jain.jainsaraswati.Helpers.GlobalHelper.BASE_URL;

public class TotalMusicService  extends Service{
    long playingposition;
    long downloadingposition;
    long loadingPosition;
    private final IBinder musicBinder = new TotalMusicBinder();
    private MediaPlayer mediaPlayer;
    DownloadManager downloadManager;
    AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener;
    boolean wasPlaying = false;
    NotificationManagerCompat notificationManager;
    public class TotalMusicBinder extends Binder{
        public TotalMusicService getService(){
            return TotalMusicService.this;
        }
    }

    @Override
    public void onCreate() {
        mediaPlayer = new MediaPlayer();
        downloadManager = null;


        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return musicBinder;
    }
    // Music Public Functions
    public void playMusic(final Music music, final long pposition){



        final AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        assert audioManager != null;
        onAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                    if(mediaPlayer.isPlaying()) {
                        pauseMusic();
                        wasPlaying = true;
                    }
                } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                    if(wasPlaying) {
                        audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
                        resumeMusic();
                    }

                }else if(focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK){
                    audioManager.adjustVolume(AudioManager.ADJUST_LOWER,AudioManager.FLAG_PLAY_SOUND);

                }else if(focusChange == AudioManager.AUDIOFOCUS_LOSS){
                    audioManager.abandonAudioFocus(onAudioFocusChangeListener);
                    if(mediaPlayer!=null){

                        mediaPlayer.release();
                        mediaPlayer = null;


                    }


                }
            }
        };

        final int result = audioManager.requestAudioFocus(onAudioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
        );
        if(result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED){




            try {
                if(mediaPlayer!=null) {
                    mediaPlayer.reset();
                }

                mediaPlayer = new MediaPlayer();

                resetPlayinPosition();
                String url = music.getUrl();
                Uri uri = Uri.parse(url);

                loadingPosition = pposition;
                mediaPlayer.setDataSource(this,uri);
                mediaPlayer.prepareAsync();

            } catch (IOException e) {
                e.printStackTrace();
            }


            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mediaPlayer.start();
                    showMusicPlayingNotification(music.getTitle());
                    playingposition = pposition;
                    resetLoadingPosition();
                    Intent musicPreparedIntent = new Intent();
                    musicPreparedIntent.setAction("MUSIC_PREPARED");
                    sendBroadcast(musicPreparedIntent);




                }
            });
            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    if(mediaPlayer!=null) {
                        mediaPlayer.reset();
                    }
                    return true;
                }
            });
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    resetLoadingPosition();
                    resetPlayinPosition();
                    Intent musicCompletedIntent = new Intent();
                    musicCompletedIntent.setAction("MUSIC_COMPLETED");
                    sendBroadcast(musicCompletedIntent);
                    audioManager.abandonAudioFocus(onAudioFocusChangeListener);
                    if(mediaPlayer!=null) {
                        mediaPlayer.release();
                    }
                    mediaPlayer=null;


                }
            });

        }else{
            resetPlayinPosition();
            resetLoadingPosition();
        }


    }
    public void pauseMusic(){
        if(mediaPlayer!=null) {
            if (mediaPlayer.isPlaying()) {

                mediaPlayer.pause();
            }
        }
    }
    public void resumeMusic(){
        if(mediaPlayer!=null) {
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
            }
        }

    }
    public void seek(int position){
        if(mediaPlayer!=null) {
            mediaPlayer.seekTo(position);
        }

    }
    public long getPlayingPosition(){

        return  playingposition;
    }
    public void resetPlayinPosition(){
        playingposition = -1;

    }
    public long getLoadingPosition(){
        return loadingPosition;
    }

    public void resetLoadingPosition(){
        loadingPosition = -1;
    }
    public boolean isPlayingSomething(){
        if(mediaPlayer!=null) {
            return mediaPlayer.isPlaying();
        }
        return false;
    }
    public int getMaximumDuration(){
        if(mediaPlayer!=null) {
            return mediaPlayer.getDuration();
        }
        return 0;
    }
    public int getCurrentPosition(){
        if(mediaPlayer!=null){
            return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }
    private void showMusicPlayingNotification(String title){
        createNotificationChannel();
        Intent intent1 = new Intent(this, HomeActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent1,0);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this,getString(R.string.music_channel_id))
                .setSmallIcon(R.drawable.ic_library_music_black_24dp)
                .setContentTitle(title)
                .setContentText("Control your playing Stavan")
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true);


        notificationManager = NotificationManagerCompat.from(this);

// notificationId is a unique int for each notification that you must define
        notificationManager.notify(1, mBuilder.build());
    }
    private void createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Stavans Notifications";
            String description = "This channel is to show Stavans Notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(getString(R.string.music_channel_id), name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager channelnotificationManager = getSystemService(NotificationManager.class);
            assert channelnotificationManager != null;
            channelnotificationManager.createNotificationChannel(channel);
        }

    }



    //Download Music Functions
    public void startDownload(Music music , long dposition){
        String url = music.getUrl();
        Uri uri =  Uri.parse(url);

        downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle(music.getTitle());
        request.setDescription("Downloading...");
        request.setDestinationInExternalPublicDir(getString(R.string.app_name), File.separator + "Stavans"+File.separator+music.getTitle()+".mp3");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        downloadManager.enqueue(request);

        downloadingposition = dposition;
    }
    public long getDownloadingposition(){
        return downloadingposition;
    }

    public void resetDownloadPosition(){
        downloadingposition = -1;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        assert notificationManager != null;
        notificationManager.cancel(1);

    }
}
