package com.saraswati.jain.jainsaraswati.Helpers;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;

import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.saraswati.jain.jainsaraswati.Models.Event;
import com.saraswati.jain.jainsaraswati.Models.Place;
import com.saraswati.jain.jainsaraswati.R;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GlobalHelper {

    public static final String BASE_URL = "https://us-central1-jainsaraswati-5ea8e.cloudfunctions.net/app/";


    static double getTimeZoneOffset(String timezone){
        TimeZone tz = TimeZone.getTimeZone(timezone);
        double minutes = TimeUnit.MINUTES.convert(tz.getRawOffset(),TimeUnit.MILLISECONDS);
        double offset  = minutes/60.0;

        return offset;

    }

    public static boolean isStoragePermissionGranted(Context context){

        return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

    }

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }


    public static String getTithiName(int tithi, Context context){
        String[] tithinames = new String[]{
                context.getString(R.string.one),
                context.getString(R.string.two),
                context.getString(R.string.three),
                context.getString(R.string.four),
                context.getString(R.string.five),
                context.getString(R.string.six),
                context.getString(R.string.seven),
                context.getString(R.string.eigth),
                context.getString(R.string.nine),
                context.getString(R.string.ten),
                context.getString(R.string.eleven),
                context.getString(R.string.twelve),
                context.getString(R.string.thirteen),
                context.getString(R.string.fourteen),
                context.getString(R.string.fifteen)

        };
        return tithinames[tithi-1];
    }

    public static String getTithiMonthName(int month, Context context){
        String[] tithimonths = new String[]{
                context.getString(R.string.chaitra),
                context.getString(R.string.vaisakh),
                context.getString(R.string.jeth),
                context.getString(R.string.aashad),
                context.getString(R.string.shravan),
                context.getString(R.string.Bhadra),
                context.getString(R.string.Aaso),
                context.getString(R.string.kartik),
                context.getString(R.string.magasr),
                context.getString(R.string.posh),
                context.getString(R.string.maha),
                context.getString(R.string.fagun)
        };
        return tithimonths[month-1];
    }

    public static String getNakshatraName(int nakshatra , Context context){
        String[] nakshatras = new String[]{
                context.getString(R.string.ashwini),
                context.getString(R.string.bhairani),
                context.getString(R.string.kritika),
                context.getString(R.string.rohini),
                context.getString(R.string.mrigashirsha),
                context.getString(R.string.ardra),
                context.getString(R.string.punarvasu),
                context.getString(R.string.pushya),
                context.getString(R.string.ashlesha),
                context.getString(R.string.magha),
                context.getString(R.string.purva_falguni),
                context.getString(R.string.uttara_falguni),
                context.getString(R.string.hasta),
                context.getString(R.string.chitra),
                context.getString(R.string.swati),
                context.getString(R.string.vishakha),
                context.getString(R.string.anuradha),
                context.getString(R.string.jyeshtha),
                context.getString(R.string.mula),
                context.getString(R.string.purva_ashadha),
                context.getString(R.string.uttara_ashadha),
                context.getString(R.string.shravana),
                context.getString(R.string.dhanishtha),
                context.getString(R.string.shatabhisha),
                context.getString(R.string.purva_bhadrapada),
                context.getString(R.string.uttara_bhadrapada),
                context.getString(R.string.revati)
        };
        return nakshatras[nakshatra-1];
    }


    public static Retrofit getRetrofitInstance(){
        return new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
    }

    public static boolean isNetworkAvailable(Context context){

        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        return networkInfo!=null && networkInfo.isConnected();


    }

    public static List<Place> getPlacesList(Context context){
        String json = null;
        try {
            InputStream inputStream = context.getAssets().open("cities.json");
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            json = new String(buffer, "UTF-8");

        } catch (IOException e) {
            e.printStackTrace();
        }

        Type collectionType = new TypeToken<List<Place>>(){}.getType();
        return new Gson().fromJson( json, collectionType);
    }

    public static Place getPlace(String cityName, Context context){
        List<Place> placeList = getPlacesList(context);
        for(Place place : placeList){
            if(place.getTitle().equals(cityName)){
                return place;
            }
        }

        return null;

    }

    public static void setSelectedPlace(Context context,Place place){

        context.getSharedPreferences("PREFERENCE",Context.MODE_PRIVATE).edit().putString("SELECTED_PLACE",new Gson().toJson(place)).apply();
    }


    public static Place getSelectedPlace(Context context){

        String place = context.getSharedPreferences("PREFERENCE",Context.MODE_PRIVATE).getString("SELECTED_PLACE",null);
        if(place != null){
            return new Gson().fromJson(place,Place.class);
        }
        return null;


    }

    private static List<Event> getEventsList(Context context){
        String json = null;
        InputStream inputStream;
        try {
            if(LocaleHelper.getLanguage(context).equals("gu")){
                inputStream = context.getAssets().open("eventsg.json");
            }else if (LocaleHelper.getLanguage(context).equals("hi")){
                inputStream = context.getAssets().open("eventsh.json");
            }else {
                inputStream = context.getAssets().open("events.json");
            }
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            json = new String(buffer, "UTF-8");

        } catch (IOException e) {
            e.printStackTrace();
        }

        Type collectiontype= new TypeToken<List<Event>>(){}.getType();
        return new Gson().fromJson(json,collectiontype);

    }

    public static String[] getEvents(int day , int month, Context context){
        List<Event> eventList = getEventsList(context);

        String[] tempevents;

        for(Event event : eventList){
            if(event.getDay() == day && event.getMonth() == month){
                tempevents = event.getEvents();
                return tempevents;
            }
        }

        return new String[]{};
    }




}
