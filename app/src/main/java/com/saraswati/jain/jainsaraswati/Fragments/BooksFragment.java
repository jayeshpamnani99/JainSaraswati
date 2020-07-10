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
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.saraswati.jain.jainsaraswati.Adapters.BookRecyclerViewAdapter;
import com.saraswati.jain.jainsaraswati.Apis.BookApi;
import com.saraswati.jain.jainsaraswati.CustomViews.SuggestionBox;
import com.saraswati.jain.jainsaraswati.Helpers.GlobalHelper;
import com.saraswati.jain.jainsaraswati.Models.Book;
import com.saraswati.jain.jainsaraswati.R;
import com.saraswati.jain.jainsaraswati.Services.BookDownloadService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * A simple {@link Fragment} subclass.
 */
public class BooksFragment extends Fragment {


    private Context context;
    private RecyclerView bookRecyclerView;

    private List<Book> books;

    private ProgressBar loadingbooklistprogress;
    private BookRecyclerViewAdapter bookRecyclerViewAdapter;
    private List<Book> loadedBook= new ArrayList<>();
    private List<Book> updatedBook;

    private boolean binded = false;


    private FirebaseAnalytics firebaseAnalytics;
    public BooksFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_books, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);

        context = getContext();


        firebaseAnalytics = FirebaseAnalytics.getInstance(context);

        Bundle openBooksFragmentBundle = new Bundle();
        openBooksFragmentBundle.putInt(FirebaseAnalytics.Param.ITEM_ID,2);
        openBooksFragmentBundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY,"MEDIA");
        openBooksFragmentBundle.putString(FirebaseAnalytics.Param.ITEM_NAME,"BOOKS");
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN,openBooksFragmentBundle);

        bookRecyclerView = view.findViewById(R.id.bookrecyclerviewid);
        loadingbooklistprogress= view.findViewById(R.id.loadingbooklistprogressid);

        loadingbooklistprogress.setVisibility(View.INVISIBLE);
        bookRecyclerView.setLayoutManager(new GridLayoutManager(context,2));
        bookRecyclerView.setHasFixedSize(true);




        bookRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager linearLayoutManager = (LinearLayoutManager)bookRecyclerView.getLayoutManager();
                assert linearLayoutManager != null;
                int lastvisibleitem = linearLayoutManager.findLastCompletelyVisibleItemPosition();
                int totalitemcount = linearLayoutManager.getItemCount();
                if(lastvisibleitem >= totalitemcount-1 && totalitemcount < books.size()){

                    updatedBook = loadMore(lastvisibleitem);
                    bookRecyclerViewAdapter.setUpdatedBooks(updatedBook);

                }

            }
        });




        getData();





    }

    private ServiceConnection bookDownloadserviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BookDownloadService.DownloadBinder bookdownloadBinder = (BookDownloadService.DownloadBinder) service;
            BookDownloadService bookDownloadService = bookdownloadBinder.getService();
            bookRecyclerViewAdapter  = new BookRecyclerViewAdapter(context,updatedBook, bookDownloadService);
            bookRecyclerView.setAdapter(bookRecyclerViewAdapter);

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    private void getData(){
        loadingbooklistprogress.setVisibility(View.VISIBLE);
        Retrofit retrofit = GlobalHelper.getRetrofitInstance();

        BookApi bookApi = retrofit.create(BookApi.class);

        Call<List<Book>> call = bookApi.getBooks();

        call.enqueue(new Callback<List<Book>>() {
            @Override
            public void onResponse(Call<List<Book>> call, Response<List<Book>> response) {
                if(response.isSuccessful()){
                    loadingbooklistprogress.setVisibility(View.INVISIBLE);
                    books = response.body();
                    updatedBook = loadMore(-1);
                    Intent BookDownloadServiceIntent = new Intent(context, BookDownloadService.class);
                    context.startService(BookDownloadServiceIntent);
                    context.bindService(BookDownloadServiceIntent,bookDownloadserviceConnection, Context.BIND_AUTO_CREATE);
                    binded = true;

                }else{
                    Toast.makeText(context, context.getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                }


            }

            @Override
            public void onFailure(Call<List<Book>> call, Throwable t) {
                Toast.makeText(context, context.getString(R.string.low_network_connection), Toast.LENGTH_SHORT).show();

            }
        });

    }

    private List<Book> loadMore(int position){
        int i=position+1;
            while(books.size()>i && i<=(position+8)){

            loadedBook.add(books.get(i));
            i++;

        }
        return  loadedBook;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.booksmenu,menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.booksearchid).getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                //Logging Event
                Bundle bookSearchEventBundle = new Bundle();
                bookSearchEventBundle.putInt(FirebaseAnalytics.Param.ITEM_ID,2);
                bookSearchEventBundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY,"MEDIA");
                bookSearchEventBundle.putString(FirebaseAnalytics.Param.ITEM_NAME,"Books");
                bookSearchEventBundle.putString(FirebaseAnalytics.Param.SEARCH_TERM,newText);
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.VIEW_SEARCH_RESULTS,bookSearchEventBundle);


                //Actual Search
                if(books!=null) {
                    List<Book> filteredBook = new ArrayList<>();
                    for (Book row : books) {
                        if (row.getTitle().toUpperCase().contains(newText.toUpperCase()) || row.getCategory().toUpperCase().contains(newText.toUpperCase())) {
                            filteredBook.add(row);
                        }
                    }
                    if (bookRecyclerViewAdapter != null) {
                        bookRecyclerViewAdapter.setUpdatedBooks(filteredBook);
                    }
                }
                return false;
            }
        });

    }
    //BroadCast Receivers

    private BroadcastReceiver onBookDownloadCompleted = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (bookRecyclerViewAdapter!=null){
                long referenceDownloadedid = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID,-1);

                DownloadManager mgr = (DownloadManager)
                        context.getSystemService(Context.DOWNLOAD_SERVICE);

                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(referenceDownloadedid);
                assert mgr != null;
                Cursor cur = mgr.query(query);
                int index = cur.getColumnIndex(DownloadManager.COLUMN_STATUS);

                if(cur.moveToFirst()) {
                    if(bookRecyclerViewAdapter != null){
                        bookRecyclerViewAdapter.onBookDownloadCompleted(cur.getInt(index));
                    }


                }

            }
        }
    };

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch(item.getItemId()){
            case R.id.addbooksuggestionmenuid:
                final Dialog dialog = new Dialog(context);
                View view = new SuggestionBox(context,"Books");
                dialog.setContentView(view);
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

    @Override
    public void onResume() {
        super.onResume();

        context.registerReceiver(onBookDownloadCompleted,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

    }

    @Override
    public void onPause() {
        super.onPause();

        context.unregisterReceiver(onBookDownloadCompleted);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(binded){
            context.unbindService(bookDownloadserviceConnection);
        }
    }
}
