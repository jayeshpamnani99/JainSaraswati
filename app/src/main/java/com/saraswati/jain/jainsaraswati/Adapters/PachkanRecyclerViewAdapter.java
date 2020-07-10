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
import com.saraswati.jain.jainsaraswati.Services.PachkanService;

import java.io.File;
import java.util.List;

public class PachkanRecyclerViewAdapter extends RecyclerView.Adapter<PachkanRecyclerViewAdapter.PachkanRecyclerViewHolder> implements ActivityCompat.OnRequestPermissionsResultCallback {

    private List<Music> pachkanList;
    private Context context;
    private PachkanService pachkanService;
    private long playingposition;
    private long downloadingposition;
    private PachkanRecyclerViewHolder playingHolder;
    private File[] files;
    private String filePath;
    
    private FirebaseAnalytics firebaseAnalytics;

    public PachkanRecyclerViewAdapter(Context context,List<Music> pachkanList,  PachkanService pachkanService) {
        this.context = context;
        this.pachkanList = pachkanList;
        this.pachkanService = pachkanService;

        filePath = Environment.getExternalStoragePublicDirectory(context.getString(R.string.app_name)) + File.separator + "Stavans";
        firebaseAnalytics = FirebaseAnalytics.getInstance(context);
    }

    @NonNull
    @Override
    public PachkanRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.pachkancard,parent,false);
        return new PachkanRecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final PachkanRecyclerViewHolder holder, int position) {
        final Music music = pachkanList.get(position);
        holder.pachkantitle.setText(music.getTitle());
        playingposition = pachkanService.getPlayingPosition();
        long loadingposition = pachkanService.getLoadingPosition();


        //Updating Playing UI
        if(music.getId() == loadingposition){
            holder.pachkanLoadingProgressBar.setVisibility(View.VISIBLE);
            holder.playpauseButton.setEnabled(false);
        }else if(music.getId() == playingposition && music.getId()!= loadingposition){

            holder.playpauseButton.setEnabled(true);
            playingHolder = holder;
            holder.pachkanLoadingProgressBar.setVisibility(View.INVISIBLE);
            holder.pachkanSeekbar.setVisibility(View.VISIBLE);
            holder.pachkanSeekbar.setMax(pachkanService.getMaximumDuration());
            holder.pachkanSeekbar.setProgress(pachkanService.getCurrentPosition());
            mSeekbarUpdateHandler.postDelayed(mUpdateSeekbar, 0);
            if(pachkanService.isPlayingSomething()) {
                holder.playpauseButton.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_pause_orange_48dp));

            }else{

                holder.playpauseButton.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_play_arrow_orange_48dp));
                mSeekbarUpdateHandler.removeCallbacks(mUpdateSeekbar);
            }


        }else{
            holder.playpauseButton.setEnabled(true);
            holder.playpauseButton.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_play_arrow_orange_48dp));
            holder.pachkanSeekbar.setVisibility(View.INVISIBLE);
            holder.pachkanLoadingProgressBar.setVisibility(View.INVISIBLE);
        }
        holder.playpauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bundle musicViewBundle = new Bundle();
                musicViewBundle.putInt(FirebaseAnalytics.Param.ITEM_ID,0);
                musicViewBundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY,"PACHKANS");
                musicViewBundle.putString(FirebaseAnalytics.Param.ITEM_NAME,holder.pachkantitle.getText().toString());
                firebaseAnalytics.logEvent("MUSIC_DOWNLOAD",musicViewBundle);
                
                if (music.getId() == playingposition) {
                    if(pachkanService.isPlayingSomething()){
                        pachkanService.pauseMusic();
                        holder.playpauseButton.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_play_arrow_orange_48dp));

                    }else{
                        pachkanService.resumeMusic();
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
        holder.pachkanSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser) {
                    pachkanService.seek(progress);
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
        downloadingposition = pachkanService.getDownloadingposition();
        File file = new File(filePath);
        if (file.exists()) {
            files = file.listFiles();
        }

        if(hasFile(music.getTitle())){
            holder.downloadPachkanButton.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_done_orange_48dp));
        }else if(music.getId() == downloadingposition){
            holder.downloadPachkanButton.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_data_usage_orange_48dp));
        }else{
            holder.downloadPachkanButton.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_file_download_orange_48dp));
        }


        holder.downloadPachkanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bundle musicDownloadBundle = new Bundle();
                musicDownloadBundle.putInt(FirebaseAnalytics.Param.ITEM_ID,1);
                musicDownloadBundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY,"PACHKANS");
                musicDownloadBundle.putString(FirebaseAnalytics.Param.ITEM_NAME,holder.pachkantitle.getText().toString());
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
        return pachkanList.size();
    }

    private final Handler mSeekbarUpdateHandler = new Handler();
    private Runnable mUpdateSeekbar = new Runnable() {
        @Override
        public void run() {
            if(pachkanService.getPlayingPosition()!=-1) {
                playingHolder.pachkanSeekbar.setProgress(pachkanService.getCurrentPosition());
                mSeekbarUpdateHandler.postDelayed(this, 50);
            }else{
                pachkanService.resetPlayinPosition();
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

    class PachkanRecyclerViewHolder extends RecyclerView.ViewHolder{
        TextView pachkantitle;
        ImageView playpauseButton;
        ImageView downloadPachkanButton;
        SeekBar pachkanSeekbar;
        ProgressBar pachkanLoadingProgressBar;
        PachkanRecyclerViewHolder(@NonNull View itemView) {
            super(itemView);

            pachkantitle = itemView.findViewById(R.id.pachkantitleid);
            playpauseButton = itemView.findViewById(R.id.pachkanplayid);
            downloadPachkanButton = itemView.findViewById(R.id.pachkandownloadid);
            pachkanLoadingProgressBar = itemView.findViewById(R.id.pachkanloadingprogressbarid);
            pachkanSeekbar = itemView.findViewById(R.id.pachkanseekbarid);

            pachkanSeekbar.setVisibility(View.INVISIBLE);
            playpauseButton.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_play_arrow_orange_48dp));

            downloadPachkanButton.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_file_download_orange_48dp));
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


    // BroadCast Receivers Handling

    public void onMusicDownloaded(int DOWNLOAD_STATUS){

            if(DOWNLOAD_STATUS == DownloadManager.STATUS_SUCCESSFUL){
                Toast.makeText(context,context.getString(R.string.download_complete), Toast.LENGTH_SHORT).show();
                notifyDataSetChanged();

            }else if(DOWNLOAD_STATUS == DownloadManager.STATUS_FAILED){
                Toast.makeText(context, context.getString(R.string.download_failed), Toast.LENGTH_SHORT).show();



            }
            pachkanService.resetDownloadPosition();

    }



    private void letsDownload(Music music){
        if(pachkanService.getDownloadingposition()<=0) {
            if(GlobalHelper.isNetworkAvailable(context)) {


                pachkanService.startDownload(music,music.getId());
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
            pachkanService.resetLoadingPosition();
            pachkanService.resetPlayinPosition();
            mSeekbarUpdateHandler.removeCallbacks(mUpdateSeekbar);
        }

        @Override
        protected Void doInBackground(Music... music) {

            Music music1 = music[0];
            pachkanService.playMusic(music1,music1.getId());

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            notifyDataSetChanged();

        }
    }


}
