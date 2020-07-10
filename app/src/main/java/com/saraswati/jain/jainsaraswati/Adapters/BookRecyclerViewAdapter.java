package com.saraswati.jain.jainsaraswati.Adapters;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.saraswati.jain.jainsaraswati.Helpers.GlobalHelper;
import com.saraswati.jain.jainsaraswati.Models.Book;
import com.saraswati.jain.jainsaraswati.R;
import com.saraswati.jain.jainsaraswati.Services.BookDownloadService;

import java.io.File;
import java.util.List;

import static com.saraswati.jain.jainsaraswati.Helpers.GlobalHelper.BASE_URL;
import static com.saraswati.jain.jainsaraswati.Helpers.GlobalHelper.isStoragePermissionGranted;

public class BookRecyclerViewAdapter extends RecyclerView.Adapter<BookRecyclerViewAdapter.BookRecyclerViewHolder> implements ActivityCompat.OnRequestPermissionsResultCallback {

    private Context context;
    private List<Book> bookList;
    private BookDownloadService bookDownloadService;
    private File[] files;
    private String path;
    private long downloadPosition;

    private FirebaseAnalytics firebaseAnalytics;

    public BookRecyclerViewAdapter(Context context, List<Book> bookList, final BookDownloadService bookDownloadService) {
        this.context = context;
        this.bookList = bookList;
        this.bookDownloadService = bookDownloadService;


        path = Environment.getExternalStoragePublicDirectory(context.getString(R.string.app_name))+ File.separator+"Books";


        firebaseAnalytics = FirebaseAnalytics.getInstance(context);
    }

    @NonNull
    @Override
    public BookRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.bookcard,parent,false);
        return new BookRecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final BookRecyclerViewHolder holder, int position) {
        final Book book = bookList.get(position);
        holder.booktitle.setText(book.getTitle());


        downloadPosition = bookDownloadService.getDownloadposition();

        if(isStoragePermissionGranted(context) && GlobalHelper.isExternalStorageReadable()){
            File file = new File(path);

            if(file.exists()) {
                files = file.listFiles();
            }
        }


        if(hasFile(book.getTitle()) && book.getId()!=downloadPosition){
            holder.bookdownload.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_done_orange_48dp));

        }else if(book.getId() == downloadPosition) {
            holder.bookdownload.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_data_usage_orange_48dp));
        }else if(book.getId()!=downloadPosition){
            holder.bookdownload.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_file_download_orange_48dp));
        }




        holder.bookdownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bookDownloadBundle = new Bundle();
                bookDownloadBundle.putInt(FirebaseAnalytics.Param.ITEM_ID,2);
                bookDownloadBundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY,"BOOKS");
                bookDownloadBundle.putString(FirebaseAnalytics.Param.ITEM_NAME,holder.booktitle.getText().toString());
                firebaseAnalytics.logEvent("BOOK_DOWNLOAD",bookDownloadBundle);

                downloadPosition = book.getId();
                if(GlobalHelper.isStoragePermissionGranted(context)){
                    if(GlobalHelper.isExternalStorageWritable()){
                        letsDownload(book);
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
        holder.bookview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bookViewBundle = new Bundle();
                bookViewBundle.putInt(FirebaseAnalytics.Param.ITEM_ID,2);
                bookViewBundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY,"BOOKS");
                bookViewBundle.putString(FirebaseAnalytics.Param.ITEM_NAME,holder.booktitle.getText().toString());
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM,bookViewBundle);

                if(GlobalHelper.isNetworkAvailable(context)){
                    String book_url = book.getUrl();
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(book_url));
                    context.startActivity(browserIntent);
                }else{
                    Toast.makeText(context, context.getString(R.string.low_network_connection), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    public void setUpdatedBooks(List<Book> bookList){
        this.bookList = bookList;
        notifyDataSetChanged();
    }

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

    class BookRecyclerViewHolder extends RecyclerView.ViewHolder{
        TextView booktitle;
        ImageView bookdownload;
        ImageView bookview;

        BookRecyclerViewHolder(@NonNull View itemView) {
            super(itemView);

            booktitle = itemView.findViewById(R.id.booktitleid);
            bookdownload = itemView.findViewById(R.id.bookdownloadid);
            bookview = itemView.findViewById(R.id.bookviewid);
        }
    }

    private boolean hasFile(String fileName){
        if(files != null) {
            fileName = fileName + ".pdf";
            for (File file : files) {
                String inFileName = file.getName();
                if (inFileName.equals(fileName)) {
                    return true;
                }

            }
        }
        return false;
    }

    //Handling Broadcast Receivers


    public void onBookDownloadCompleted(int DOWNLOAD_STATUS){


                if (DOWNLOAD_STATUS == DownloadManager.STATUS_SUCCESSFUL) {
                    Toast.makeText(context, context.getString(R.string.download_complete), Toast.LENGTH_SHORT).show();
                    notifyDataSetChanged();

                } else if (DOWNLOAD_STATUS == DownloadManager.STATUS_FAILED) {
                    Toast.makeText(context, context.getString(R.string.download_failed), Toast.LENGTH_SHORT).show();
                    downloadPosition = -1;


                }
                bookDownloadService.resetDownloadPosition();

    }



    private void letsDownload(Book book){
        if(bookDownloadService.getDownloadposition()<=0) {
            if(GlobalHelper.isNetworkAvailable(context)) {
                bookDownloadService.startDownload(book, book.getId());

                notifyDataSetChanged();

                Toast.makeText(context, context.getString(R.string.downloading) + book.getTitle(), Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(context, context.getString(R.string.low_network_connection), Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(context,context.getString(R.string.alreadyrunning), Toast.LENGTH_SHORT).show();
        }
    }



}
