package com.saraswati.jain.jainsaraswati.Fragments;


import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.saraswati.jain.jainsaraswati.Activities.StartActivity;
import com.saraswati.jain.jainsaraswati.Adapters.BottomSheetRecyclerAdapter;
import com.saraswati.jain.jainsaraswati.Helpers.GlobalHelper;
import com.saraswati.jain.jainsaraswati.Helpers.PanchangHelper;
import com.saraswati.jain.jainsaraswati.Models.Date;
import com.saraswati.jain.jainsaraswati.Models.Place;
import com.saraswati.jain.jainsaraswati.Models.Time;
import com.saraswati.jain.jainsaraswati.Models.Tithi;
import com.saraswati.jain.jainsaraswati.R;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;

/**
 * A simple {@link Fragment} subclass.
 */
public class PanchangFragment extends Fragment {
    
    private Context context;

    private RecyclerView bottomSheetRecyclerView;


    private TextView sudvad;
    private TextView tithi;
    private TextView tithimonth;
    private TextView sunrise;
    private TextView sunset;
    private TextView nakshatra;
    private TextView nakshatraEndTime;

    private Button selectedDateButton;

    private CalendarView panchangCalendarView;

    private ImageButton bottomsheetUpDownButton;


    private Place place;

    private FirebaseAnalytics firebaseAnalytics;
    public PanchangFragment() {
        // Required empty public constructor

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_panchang, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);

        context = getContext();

        assert context != null;
        place = GlobalHelper.getSelectedPlace(context);


        firebaseAnalytics = FirebaseAnalytics.getInstance(context);
        Bundle panchangOpenBundle = new Bundle();
        panchangOpenBundle.putInt(FirebaseAnalytics.Param.ITEM_ID,-1);
        panchangOpenBundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY,"PANCHANG");
        panchangOpenBundle.putString(FirebaseAnalytics.Param.ITEM_NAME,"PANCHANG");

        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN,panchangOpenBundle);


        sudvad = view.findViewById(R.id.sudvadid);
        tithi = view.findViewById(R.id.tithiid);
        tithimonth = view.findViewById(R.id.tithimonthid);
        sunrise = view.findViewById(R.id.sunriseid);
        sunset = view.findViewById(R.id.sunsetid);
        nakshatra = view.findViewById(R.id.nakshatraid);
        nakshatraEndTime = view.findViewById(R.id.nakshatraendtimeid);

        bottomsheetUpDownButton = view.findViewById(R.id.bottomsheetupdownbuttonid);

        selectedDateButton = view.findViewById(R.id.selecteddateid);

        panchangCalendarView = view.findViewById(R.id.panchangcalendarviewid);



        final BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(view.findViewById(R.id.bottomsheetbehaviorid));
        bottomSheetBehavior.setPeekHeight(context.getResources().getDimensionPixelSize(R.dimen.BottomSheet_PeakHeight));

        bottomSheetRecyclerView = view.findViewById(R.id.bottomsheetrecyclerid);

        bottomsheetUpDownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Bundle bottomSheetBundle = new Bundle();


                bottomSheetBundle.putInt(FirebaseAnalytics.Param.ITEM_ID,-1);
                bottomSheetBundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY,"PANCHANG");
                bottomSheetBundle.putString(FirebaseAnalytics.Param.ITEM_NAME,"BOTTOM_SHEET");



                if(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED){
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    firebaseAnalytics.logEvent("EXPANDED",bottomSheetBundle);

                }else if(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED){
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    firebaseAnalytics.logEvent("COLLAPSED",bottomSheetBundle);

                }

            }
        });

        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int i) {
                if(i == BottomSheetBehavior.STATE_EXPANDED){
                    bottomsheetUpDownButton.setBackgroundResource(R.drawable.ic_keyboard_arrow_down_orange_24dp);
                }else{
                    bottomsheetUpDownButton.setBackgroundResource(R.drawable.ic_keyboard_arrow_up_orange_24dp);
                }
            }

            @Override
            public void onSlide(@NonNull View view, float v) {

            }
        });

        bottomSheetRecyclerView.setHasFixedSize(true);
        bottomSheetRecyclerView.setLayoutManager(new LinearLayoutManager(context));


        Calendar todayCalendar = Calendar.getInstance();
        Date todaysDate = new Date(todayCalendar.get(Calendar.DAY_OF_MONTH),todayCalendar.get(Calendar.MONTH)+1,todayCalendar.get(Calendar.YEAR));
        new CalculatePanchang().execute(todaysDate);
        selectedDateButton.setText(todaysDate.getDay()+"|"+todaysDate.getMonth()+"|"+todaysDate.getYear());

        selectedDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }
        });


        panchangCalendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int i, int i1, int i2) {

                selectedDateButton.setText(i2+"|"+(i1+1)+"|"+i);

                new CalculatePanchang().execute(new Date(i2,i1+1,i));
            }
        });





    }

    private class CalculatePanchang extends AsyncTask<Date,Void,Void>{

        Tithi currentTithi;
        Time sunriseTime;
        Time sunsetTime;
        Pair<Integer , Time > currentNakshatra;
        BottomSheetRecyclerAdapter bottomSheetRecyclerAdapter;



        @Override
        protected Void doInBackground(Date... dates) {
            Date date = dates[0];



            currentTithi = PanchangHelper.getTithi(date,place);
            sunriseTime = PanchangHelper.getLocalSunrise(date,place);
            sunsetTime = PanchangHelper.getLocalSunset(date,place);
            currentNakshatra = PanchangHelper.getNakshatras(date,place);





            Calendar calendar = Calendar.getInstance();
            calendar.set(date.getYear(),date.getMonth()-1,date.getDay());
            calendar.add(Calendar.DAY_OF_MONTH,1);

            Time sunrise2 = PanchangHelper.getLocalSunrise(new Date(calendar.get(Calendar.DAY_OF_MONTH),calendar.get(Calendar.MONTH)+1,calendar.get(Calendar.YEAR)),place);

            bottomSheetRecyclerAdapter = new BottomSheetRecyclerAdapter(context,GlobalHelper.getEvents(currentTithi.getTithi(),currentTithi.getMonth(),context),PanchangHelper.getTimings(date,place),PanchangHelper.getChogadiyas(date,context),PanchangHelper.getChogadiyaTimings(sunriseTime,sunsetTime,sunrise2),PanchangHelper.getHoras(date,context),PanchangHelper.getHorasTimings(sunriseTime,sunsetTime,sunrise2));


            Bundle calculatePanchangBundle = new Bundle();

            calculatePanchangBundle.putInt(FirebaseAnalytics.Param.ITEM_ID,-1);
            calculatePanchangBundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY,"PANCHANG");
            calculatePanchangBundle.putString(FirebaseAnalytics.Param.ITEM_NAME,date.getDay()+":"+date.getMonth()+":"+date.getYear());
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM,calculatePanchangBundle);


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if(currentTithi.getSud()){
                sudvad.setText(context.getString(R.string.Sud));
            }else{
                sudvad.setText(context.getString(R.string.Vad));
            }
            int tithi_value = currentTithi.getTithi();
            if(tithi_value > 15){
                tithi_value -= 15;
            }
            tithi.setText(GlobalHelper.getTithiName(tithi_value,context));
            tithimonth.setText(GlobalHelper.getTithiMonthName(currentTithi.getMonth(),context));

            NumberFormat f = new DecimalFormat("00");

            sunrise.setText(String.format("%s:%s:%s", f.format(sunriseTime.getHours()), f.format(sunriseTime.getMinutes()), f.format(sunriseTime.getSeconds())));
            sunset.setText(String.format("%s:%s:%s", f.format(sunsetTime.getHours()), f.format(sunsetTime.getMinutes()), f.format(sunsetTime.getSeconds())));
            nakshatra.setText(GlobalHelper.getNakshatraName(currentNakshatra.first,context));
            nakshatraEndTime.setText(String.format("%s:%s:%s", f.format(currentNakshatra.second.getHours()), f.format(currentNakshatra.second.getMinutes()), f.format(currentNakshatra.second.getSeconds())));

            bottomSheetRecyclerView.setAdapter(bottomSheetRecyclerAdapter);

        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.panchangmenu,menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch(item.getItemId()){
            case R.id.settingsid:
                startActivity(new Intent(context, StartActivity.class));
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


    private void showDatePickerDialog(){
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                selectedDateButton.setText(i2+"|"+(i1+1)+"|"+i);
                Calendar calendar = Calendar.getInstance();
                calendar.set(i,i1,i2);
                panchangCalendarView.setDate(calendar.getTimeInMillis());
                 new CalculatePanchang().execute(new Date(i2,i1+1,i));
            }
        },year,month,day);



        datePickerDialog.show();
    }

}

