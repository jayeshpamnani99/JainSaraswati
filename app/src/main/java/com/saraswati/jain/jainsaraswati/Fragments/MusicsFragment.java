package com.saraswati.jain.jainsaraswati.Fragments;


import android.app.Dialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.saraswati.jain.jainsaraswati.Adapters.MusicRecyclerViewAdapter;
import com.saraswati.jain.jainsaraswati.Apis.MusicApi;
import com.saraswati.jain.jainsaraswati.CustomViews.SuggestionBox;
import com.saraswati.jain.jainsaraswati.Helpers.GlobalHelper;
import com.saraswati.jain.jainsaraswati.Models.Music;
import com.saraswati.jain.jainsaraswati.R;
import com.saraswati.jain.jainsaraswati.Services.TotalMusicService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * A simple {@link Fragment} subclass.
 */
public class MusicsFragment extends Fragment {

    private Context context;

    private CardView stavansInfoCard;
    private ImageButton stavansCancelInfoButton;

    private RecyclerView musicRecyclerView;
    private List<Music> musics;
    private MusicRecyclerViewAdapter musicRecyclerViewAdapter;
    private ProgressBar loadingmusiclistprogress;
    private List<Music> updatedList;
    private List<Music> loadedMusic = new ArrayList<>();
    private FirebaseAnalytics firebaseAnalytics;



    private boolean binded = false;


    public MusicsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_musics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);

        context = getContext();

        stavansInfoCard = view.findViewById(R.id.stavansinfocardid);
        stavansCancelInfoButton = view.findViewById(R.id.cancelstavansinfotextid);

        musicRecyclerView = view.findViewById(R.id.musicrecyclerviewid);
        loadingmusiclistprogress = view.findViewById(R.id.loadingmusiclistprogressbarid);

        musicRecyclerView.setHasFixedSize(true);
        musicRecyclerView.setLayoutManager(new GridLayoutManager(context,2));

        firebaseAnalytics = FirebaseAnalytics.getInstance(context);


        Bundle openMusicFragmentBundle = new Bundle();
        openMusicFragmentBundle.putInt(FirebaseAnalytics.Param.ITEM_ID,1);
        openMusicFragmentBundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY,"MEDIA");
        openMusicFragmentBundle.putString(FirebaseAnalytics.Param.ITEM_NAME,"STAVANS");
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN,openMusicFragmentBundle);

        boolean isStavansInfoCardViewed = context.getSharedPreferences("PREFERENCE",Context.MODE_PRIVATE).getBoolean("VIEWED_STAVANS_INFO_CARD",false);

        if(!isStavansInfoCardViewed){

            stavansCancelInfoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    context.getSharedPreferences("PREFERENCE",Context.MODE_PRIVATE).edit().putBoolean("VIEWED_STAVANS_INFO_CARD",true).apply();

                    stavansInfoCard.setVisibility(View.GONE);
                }
            });

        }else{
            stavansInfoCard.setVisibility(View.GONE);
        }

        getMusic();


        musicRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {


            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager linearLayoutManager = (LinearLayoutManager)musicRecyclerView.getLayoutManager();
                assert linearLayoutManager != null;
                int lastvisibleitem = linearLayoutManager.findLastCompletelyVisibleItemPosition();
                int totalitemcount = linearLayoutManager.getItemCount();
                if(lastvisibleitem >= totalitemcount-1 && totalitemcount < musics.size()){
                    updatedList = loadMore(lastvisibleitem);
                    musicRecyclerViewAdapter.setUpdatedList(updatedList);

                }


            }
        });

    }

    private ServiceConnection musicServiceConntection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            TotalMusicService.TotalMusicBinder totalMusicBinder = (TotalMusicService.TotalMusicBinder) service;
            TotalMusicService totalMusicService = totalMusicBinder.getService();
            musicRecyclerViewAdapter = new MusicRecyclerViewAdapter(context,updatedList, totalMusicService);
            musicRecyclerView.setAdapter(musicRecyclerViewAdapter);

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.stavansmenu,menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.stavansearchid).getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                //Logging Search Event
                Bundle musicSearchEventBundle = new Bundle();
                musicSearchEventBundle.putInt(FirebaseAnalytics.Param.ITEM_ID,2);
                musicSearchEventBundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY,"MEDIA");
                musicSearchEventBundle.putString(FirebaseAnalytics.Param.ITEM_NAME,"Stavans");
                musicSearchEventBundle.putString(FirebaseAnalytics.Param.SEARCH_TERM,newText);
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.VIEW_SEARCH_RESULTS,musicSearchEventBundle);

                //Actual Search
                if (musics!=null) {
                    List<Music> filteredMusic = new ArrayList<>();
                    for (Music row : musics) {
                        if (row.getTitle().toUpperCase().contains(newText.toUpperCase()) || row.getCategory().toUpperCase().contains(newText.toUpperCase())) {
                            filteredMusic.add(row);
                        }
                    }
                    if (musicRecyclerViewAdapter != null) {
                        musicRecyclerViewAdapter.setFilteredList(filteredMusic);
                    }
                }

                return false;


            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch(item.getItemId()){
            case R.id.addstavansuggestionmenuid:
                final Dialog dialog = new Dialog(context);
                dialog.setContentView(new SuggestionBox(context,"Stavans"));
                dialog.setTitle(R.string.enter_your_suggestion);
                dialog.show();
                return true;

            case R.id.shareid:

                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=com.saraswati.jain.jainsaraswati&hl=en");
                sendIntent.setType("text/plain");

                Intent shareIntent = Intent.createChooser(sendIntent, null);
                startActivity(shareIntent);


                return true;


            default:
                return false;
        }


    }

    private void getMusic(){
        loadingmusiclistprogress.setVisibility(View.VISIBLE);
        Retrofit retrofit = GlobalHelper.getRetrofitInstance();
        final MusicApi musicApi = retrofit.create(MusicApi.class);

        Call<List<Music>> call = musicApi.getMusic();
        call.enqueue(new Callback<List<Music>>() {
            @Override
            public void onResponse(Call<List<Music>> call, Response<List<Music>> response) {
                loadingmusiclistprogress.setVisibility(View.GONE);
                if(response.isSuccessful()) {
                    musics = response.body();

                    updatedList = loadMore(-1);


                    Intent intent = new Intent(context,TotalMusicService.class);
                    context.startService(intent);
                    context.bindService(intent,musicServiceConntection, Context.BIND_AUTO_CREATE);
                    binded = true;

                }else{
                    Toast.makeText(context, context.getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Music>> call, Throwable t) {
                Toast.makeText(context, context.getString(R.string.low_network_connection), Toast.LENGTH_SHORT).show();
            }
        });
    }



    private List<Music> loadMore(int position) {
        int i = position+1;
        while(musics.size()>i && i<=position+8){
            loadedMusic.add(musics.get(i));
            i++;
        }

        return  loadedMusic;
    }


    //BroadCast Receivers
    private BroadcastReceiver musicPreparedBroadCastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(musicRecyclerViewAdapter!=null){
                musicRecyclerViewAdapter.notifyDataSetChanged();
            }
        }
    };


    private BroadcastReceiver musicCompletedBroadCastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(musicRecyclerViewAdapter!=null){
                musicRecyclerViewAdapter.notifyDataSetChanged();
            }

        }
    };

    private BroadcastReceiver onMusicDownloadCompleted = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (musicRecyclerViewAdapter!=null){
                long referenceDownloadedid = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID,-1);

                DownloadManager mgr = (DownloadManager)
                        context.getSystemService(Context.DOWNLOAD_SERVICE);

                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(referenceDownloadedid);
                assert mgr != null;
                Cursor cur = mgr.query(query);
                int index = cur.getColumnIndex(DownloadManager.COLUMN_STATUS);

                if(cur.moveToFirst()) {
                    musicRecyclerViewAdapter.onMusicDownloaded(cur.getInt(index));

                }

            }
        }
    };




    @Override
    public void onResume() {
        super.onResume();

        context.registerReceiver(musicPreparedBroadCastReceiver, new IntentFilter("MUSIC_PREPARED"));
        context.registerReceiver(musicCompletedBroadCastReceiver,new IntentFilter("MUSIC_COMPLETED"));
        context.registerReceiver(onMusicDownloadCompleted,new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    @Override
    public void onPause() {
        super.onPause();

        context.unregisterReceiver(musicPreparedBroadCastReceiver);
        context.unregisterReceiver(musicCompletedBroadCastReceiver);
        context.unregisterReceiver(onMusicDownloadCompleted);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(binded){
            context.unbindService(musicServiceConntection);
        }
    }
}
