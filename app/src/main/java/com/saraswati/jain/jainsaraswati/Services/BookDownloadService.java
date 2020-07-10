package com.saraswati.jain.jainsaraswati.Services;



import android.app.DownloadManager;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.saraswati.jain.jainsaraswati.Models.Book;
import com.saraswati.jain.jainsaraswati.R;

import java.io.File;

import static com.saraswati.jain.jainsaraswati.Helpers.GlobalHelper.BASE_URL;

public class BookDownloadService extends Service {

    DownloadManager downloadManager;
    DownloadManager.Request request;
    Uri uri;
    long downloadposition;
    private final IBinder downloadBinder = new DownloadBinder();

    public class DownloadBinder extends Binder{

        public BookDownloadService getService(){
            return BookDownloadService.this;
        }

    }

    @Override
    public void onCreate() {

        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {



        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return downloadBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    public void startDownload(Book book, long position){
        downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

        uri = Uri.parse(book.getUrl());

        request = new DownloadManager.Request(uri);
        request.setTitle(book.getTitle());
        request.setDescription("Downloading....");
        request.setDestinationInExternalPublicDir(getString(R.string.app_name), File.separator+ "Books" + File.separator + book.getTitle() + ".pdf");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        downloadManager.enqueue(request);
        downloadposition = position;
    }
    public long getDownloadposition(){

        return downloadposition;
    }
    public void resetDownloadPosition(){
        downloadposition = -1;
    }

}
