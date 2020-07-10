package com.saraswati.jain.jainsaraswati.Adapters;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.saraswati.jain.jainsaraswati.Helpers.GlobalHelper;
import com.saraswati.jain.jainsaraswati.Models.Music;
import com.saraswati.jain.jainsaraswati.R;
import com.saraswati.jain.jainsaraswati.Services.TotalMusicService;

import java.io.File;
import java.util.List;

public class MusicRecyclerViewAdapter extends RecyclerView.Adapter<MusicRecyclerViewAdapter.MusicRecyclerViewHolder> implements ActivityCompat.OnRequestPermissionsResultCallback {
    private Context context;
    private List<Music> musicList;
    private TotalMusicService totalMusicService;
    private long playingposition;
    private long downloadingposition;
    private MusicRecyclerViewHolder playingHolder;
    private File[] files;
    private String filePath;

    private FirebaseAnalytics firebaseAnalytics;


    public MusicRecyclerViewAdapter(Context context, List<Music> musicList, TotalMusicService totalMusicService) {
        this.context = context;
        this.musicList = musicList;
        this.totalMusicService = totalMusicService;
        filePath= Environment.getExternalStoragePublicDirectory(context.getString(R.string.app_name)) +File.separator+"Stavans";
        firebaseAnalytics = FirebaseAnalytics.getInstance(context);
    }

    @NonNull
    @Override
    public MusicRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.musiccard,parent,false);
        return new MusicRecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MusicRecyclerViewHolder holder, int position) {
        final Music music = musicList.get(position);
        holder.musictitle.setText(music.getTitle());
        playingposition = totalMusicService.getPlayingPosition();
        long loadingposition = totalMusicService.getLoadingPosition();


        //Updating Playing UI
        if(music.getId() == loadingposition){
            holder.musicLoadingProgressBar.setVisibility(View.VISIBLE);
            holder.playpauseButton.setEnabled(false);
        }else if(music.getId() == playingposition && music.getId()!= loadingposition){

            holder.playpauseButton.setEnabled(true);
            playingHolder = holder;
            holder.musicLoadingProgressBar.setVisibility(View.INVISIBLE);
            holder.musicSeekbar.setVisibility(View.VISIBLE);
            holder.musicSeekbar.setMax(totalMusicService.getMaximumDuration());
            holder.musicSeekbar.setProgress(totalMusicService.getCurrentPosition());
            mSeekbarUpdateHandler.postDelayed(mUpdateSeekbar, 0);
            if(totalMusicService.isPlayingSomething()) {
                holder.playpauseButton.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_pause_orange_48dp));

            }else{

                holder.playpauseButton.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_play_arrow_orange_48dp));
                mSeekbarUpdateHandler.removeCallbacks(mUpdateSeekbar);
            }


        }else{
            holder.playpauseButton.setEnabled(true);
            holder.playpauseButton.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_play_arrow_orange_48dp));
            holder.musicSeekbar.setVisibility(View.INVISIBLE);
            holder.musicLoadingProgressBar.setVisibility(View.INVISIBLE);
        }
        holder.playpauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle musicViewBundle = new Bundle();
                musicViewBundle.putInt(FirebaseAnalytics.Param.ITEM_ID,1);
                musicViewBundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY,"STAVANS");
                musicViewBundle.putString(FirebaseAnalytics.Param.ITEM_NAME,holder.musictitle.getText().toString());
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM,musicViewBundle);
                
                if (music.getId() == playingposition) {
                    if(totalMusicService.isPlayingSomething()){
                        totalMusicService.pauseMusic();
                        holder.playpauseButton.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_play_arrow_orange_48dp));

                    }else{
                        totalMusicService.resumeMusic();
                        holder.playpauseButton.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_pause_orange_48dp));
                    }
                } else {
                    if(GlobalHelper.isNetworkAvailable(context)) {
                        new startPlaying().execute(music);
                    }else{
                        Toast.makeText(context, context.getString(R.string.low_network_connection), Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });
        holder.musicSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser) {
                    totalMusicService.seek(progress);
                }


            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        //Updating Downloads UI
        downloadingposition = totalMusicService.getDownloadingposition();
        if(GlobalHelper.isStoragePermissionGranted(context) && GlobalHelper.isExternalStorageReadable()){
            File file = new File(filePath);
            if (file.exists()) {
                files = file.listFiles();
            }
        }


        if(hasFile(music.getTitle())){
            holder.downloadMusicButton.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_done_orange_48dp));
        }else if(music.getId() == downloadingposition){
            holder.downloadMusicButton.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_data_usage_orange_48dp));
        }else{
            holder.downloadMusicButton.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_file_download_orange_48dp));
        }


        holder.downloadMusicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bundle musicDownloadBundle = new Bundle();
                musicDownloadBundle.putInt(FirebaseAnalytics.Param.ITEM_ID,1);
                musicDownloadBundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY,"STAVANS");
                musicDownloadBundle.putString(FirebaseAnalytics.Param.ITEM_NAME,holder.musictitle.getText().toString());
                firebaseAnalytics.logEvent("MUSIC_DOWNLOAD",musicDownloadBundle);

                if(GlobalHelper.isStoragePermissionGranted(context)){
                    if(GlobalHelper.isExternalStorageWritable()){
                        letsDownload(music);
                    }else{
                        Toast.makeText(context, R.string.storage_busy, Toast.LENGTH_SHORT).show();
                    }

                }else{
                    ActivityCompat.requestPermissions(((AppCompatActivity) context),
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            0);

                }

            }
        });

    }

    @Override
    public int getItemCount() {
        return musicList.size();
    }
    public void setFilteredList(List<Music> filteredList){
        this.musicList = filteredList;
        notifyDataSetChanged();
    }
    public void setUpdatedList(List<Music> updatedList){
        this.musicList = updatedList;
        notifyDataSetChanged();
    }

    private final Handler mSeekbarUpdateHandler = new Handler();
    private Runnable mUpdateSeekbar = new Runnable() {
        @Override
        public void run() {
            if(totalMusicService.getPlayingPosition()!=-1) {
                playingHolder.musicSeekbar.setProgress(totalMusicService.getCurrentPosition());
                mSeekbarUpdateHandler.postDelayed(this, 50);
            }else{
                totalMusicService.resetPlayinPosition();
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 0) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, context.getString(R.string.permission_granted), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, context.getString(R.string.permission_denied), Toast.LENGTH_SHORT).show();
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    class MusicRecyclerViewHolder extends  RecyclerView.ViewHolder {
        TextView musictitle;
        ImageView playpauseButton;
        ImageView downloadMusicButton;
        SeekBar musicSeekbar;
        ProgressBar musicLoadingProgressBar;

        MusicRecyclerViewHolder(View itemView) {
            super(itemView);
            musictitle = itemView.findViewById(R.id.musictitleid);
            playpauseButton = itemView.findViewById(R.id.musicplayid);
            downloadMusicButton = itemView.findViewById(R.id.musicdownloadid);
            musicLoadingProgressBar = itemView.findViewById(R.id.musicloadingprogressbarid);
            musicSeekbar = itemView.findViewById(R.id.musicseekbarid);
            musicSeekbar.setVisibility(View.INVISIBLE);
            playpauseButton.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_play_arrow_orange_48dp));

            downloadMusicButton.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_file_download_orange_48dp));

        }



    }

    private boolean hasFile(String music_name){
        String music_ext_name = music_name+".mp3";
        if(files != null){
            for (File file : files) {
                if (file.getName().equals(music_ext_name)) {
                    return true;
                }
            }
        }

        return false;
    }


    // BroadCast Receivers

    public void onMusicDownloaded(int DOWNLOAD_STATUS){

        if(DOWNLOAD_STATUS == DownloadManager.STATUS_SUCCESSFUL){
            Toast.makeText(context,context.getString(R.string.download_complete), Toast.LENGTH_SHORT).show();
            notifyDataSetChanged();

        }else if(DOWNLOAD_STATUS == DownloadManager.STATUS_FAILED){
            Toast.makeText(context, context.getString(R.string.download_failed), Toast.LENGTH_SHORT).show();
        }

        totalMusicService.resetDownloadPosition();
    }




    private void letsDownload(Music music){
        if(totalMusicService.getDownloadingposition()<=0) {
            if(GlobalHelper.isNetworkAvailable(context)) {


                totalMusicService.startDownload(music,music.getId());
                notifyDataSetChanged();

                Toast.makeText(context, context.getString(R.string.downloading) + music.getTitle(), Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(context, context.getString(R.string.low_network_connection), Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(context,context.getString(R.string.alreadyrunning), Toast.LENGTH_SHORT).show();
        }
    }
    private class startPlaying extends AsyncTask<Music, Void, Void> {



        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            totalMusicService.resetLoadingPosition();
            totalMusicService.resetPlayinPosition();
            mSeekbarUpdateHandler.removeCallbacks(mUpdateSeekbar);
        }

        @Override
        protected Void doInBackground(Music... music) {

            Music music1 = music[0];
            totalMusicService.playMusic(music1,music1.getId());

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            notifyDataSetChanged();

        }
    }
}
