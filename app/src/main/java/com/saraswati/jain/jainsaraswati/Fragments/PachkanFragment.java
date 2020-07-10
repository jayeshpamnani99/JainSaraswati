package com.saraswati.jain.jainsaraswati.Fragments;


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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;


import com.google.firebase.analytics.FirebaseAnalytics;
import com.saraswati.jain.jainsaraswati.Activities.StartActivity;
import com.saraswati.jain.jainsaraswati.Adapters.PachkanRecyclerViewAdapter;
import com.saraswati.jain.jainsaraswati.Apis.PachkanApi;
import com.saraswati.jain.jainsaraswati.Helpers.GlobalHelper;
import com.saraswati.jain.jainsaraswati.Models.Music;
import com.saraswati.jain.jainsaraswati.R;
import com.saraswati.jain.jainsaraswati.Services.PachkanService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * A simple {@link Fragment} subclass.
 */
public class PachkanFragment extends Fragment {

    private RecyclerView pachkanrecyclerview;
    private ProgressBar pachkanprogressbar;
    private List<Music> pachkans;
    
    private Context context;

    private PachkanRecyclerViewAdapter pachkanRecyclerViewAdapter;

    private boolean binded = false;

    public PachkanFragment() {
        // Required empty public constructor

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pachkan, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);

        context = getContext();

        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(context);

        Bundle openPachkanFragmentBundle = new Bundle();
        openPachkanFragmentBundle.putInt(FirebaseAnalytics.Param.ITEM_ID,0);
        openPachkanFragmentBundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY,"MEDIA");
        openPachkanFragmentBundle.putString(FirebaseAnalytics.Param.ITEM_NAME,"PACHKANS");
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN,openPachkanFragmentBundle);

        pachkanrecyclerview = view.findViewById(R.id.pachkanrecyclerviewid);
        pachkanprogressbar = view.findViewById(R.id.loadingpachkanlistprogressid);

        pachkanrecyclerview.setHasFixedSize(true);
        pachkanrecyclerview.setLayoutManager(new LinearLayoutManager(context));
        loadData();
    }

    private ServiceConnection pachkanServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PachkanService.PachkanBinder pachkanMusicBinder = (PachkanService.PachkanBinder) service;
            PachkanService pachkanMusicService = pachkanMusicBinder.getService();
            pachkanRecyclerViewAdapter = new PachkanRecyclerViewAdapter(context, pachkans, pachkanMusicService);
            pachkanrecyclerview.setAdapter(pachkanRecyclerViewAdapter);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };



    private void loadData(){

        Retrofit retrofit = GlobalHelper.getRetrofitInstance();
        final PachkanApi pachkanApi = retrofit.create(PachkanApi.class);

        Call<List<Music>> call = pachkanApi.getPachkan();
        call.enqueue(new Callback<List<Music>>() {
            @Override
            public void onResponse(Call<List<Music>> call, Response<List<Music>> response) {
                if(response.isSuccessful()){
                    pachkanprogressbar.setVisibility(View.INVISIBLE);
                    pachkans =response.body();
                    Intent pachkanServiceIntent = new Intent(context, PachkanService.class);
                    context.startService(pachkanServiceIntent);
                    context.bindService(pachkanServiceIntent,pachkanServiceConnection, Context.BIND_AUTO_CREATE);
                    binded = true;

                }else{
                    Toast.makeText(context, context.getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Music>> call, Throwable t) {
                Log.d("Connecting",t.getLocalizedMessage());
                Toast.makeText(context, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                Toast.makeText(context, context.getString(R.string.low_network_connection), Toast.LENGTH_SHORT).show();
            }
        });
    }


    //BroadCast Receivers
    private BroadcastReceiver musicPreparedBroadCastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(pachkanRecyclerViewAdapter!=null){
                pachkanRecyclerViewAdapter.notifyDataSetChanged();
            }
        }
    };


    private BroadcastReceiver musicCompletedBroadCastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(pachkanRecyclerViewAdapter!=null){
                pachkanRecyclerViewAdapter.notifyDataSetChanged();
            }

        }
    };

    private BroadcastReceiver onMusicDownloadCompleted = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (pachkanRecyclerViewAdapter!=null){
                long referenceDownloadedid = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID,-1);

                DownloadManager mgr = (DownloadManager)
                        context.getSystemService(Context.DOWNLOAD_SERVICE);

                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(referenceDownloadedid);
                assert mgr != null;
                Cursor cur = mgr.query(query);
                int index = cur.getColumnIndex(DownloadManager.COLUMN_STATUS);

                if(cur.moveToFirst()) {
                    pachkanRecyclerViewAdapter.onMusicDownloaded(cur.getInt(index));

                }

            }
        }
    };


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.commonmenu,menu);

    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch(item.getItemId()){

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




    @Override
    public void onResume() {
        super.onResume();

        context.registerReceiver(musicPreparedBroadCastReceiver, new IntentFilter("PACHKAN_PREPARED"));
        context.registerReceiver(musicCompletedBroadCastReceiver, new IntentFilter("PACHKAN_COMPLETED"));
        context.registerReceiver(onMusicDownloadCompleted, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

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
            context.unbindService(pachkanServiceConnection);
        }

    }
}
