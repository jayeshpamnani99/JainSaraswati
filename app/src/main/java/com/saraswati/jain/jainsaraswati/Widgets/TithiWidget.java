package com.saraswati.jain.jainsaraswati.Widgets;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.widget.RemoteViews;

import com.saraswati.jain.jainsaraswati.Activities.HomeActivity;
import com.saraswati.jain.jainsaraswati.Helpers.GlobalHelper;
import com.saraswati.jain.jainsaraswati.Helpers.PanchangHelper;
import com.saraswati.jain.jainsaraswati.Models.Date;
import com.saraswati.jain.jainsaraswati.Models.Place;
import com.saraswati.jain.jainsaraswati.Models.Tithi;
import com.saraswati.jain.jainsaraswati.R;


import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;

import java.util.Calendar;


/**
 * Implementation of App Widget functionality.
 */
public class TithiWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        Place place = GlobalHelper.getSelectedPlace(context);
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        month = month + 1;
        int year = calendar.get(Calendar.YEAR);
        String date = day + " | " + month +" | " + year;



        Tithi currentTithi = PanchangHelper.getTithi(new Date(day,month,year), place);
        int tithi = currentTithi.getTithi();
        tithi = tithi % 15;
        if (tithi == 0) {
            tithi = 15;
        }
        boolean sud = currentTithi.getSud();
        int reqmonth = currentTithi.getMonth();
        String sud_vad;
        if(sud){
            sud_vad = context.getString(R.string.Sud);
        }else{
            sud_vad = context.getString(R.string.Vad);
        }



        // Construct the RemoteViews object
        Intent intent = new Intent(context, HomeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0,intent,0);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.tithi_widget);
        views.setOnClickPendingIntent(R.id.widgetlayoutid,pendingIntent);
        views.setTextViewText(R.id.widgetdateid,date);
        views.setTextViewText(R.id.widgetsudvadid, sud_vad);
        views.setTextViewText(R.id.widgettithiid, GlobalHelper.getTithiName(tithi,context));
        views.setTextViewText(R.id.widgetmonthid,GlobalHelper.getTithiMonthName(reqmonth,context));
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if("android.intent.action.TIME_SET".equals(intent.getAction())){
            AppWidgetManager instance = AppWidgetManager.getInstance(context);
            onUpdate(context,instance,instance.getAppWidgetIds(new ComponentName(context.getPackageName(),getClass().getName())));
            return;
        }


        super.onReceive(context, intent);



    }

    @Override
    public void onEnabled(Context context) {


    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

}

