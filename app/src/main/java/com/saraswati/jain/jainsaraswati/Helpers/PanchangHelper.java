package com.saraswati.jain.jainsaraswati.Helpers;

import android.content.Context;
import android.util.Pair;

import com.saraswati.jain.jainsaraswati.Models.Date;
import com.saraswati.jain.jainsaraswati.Models.Place;
import com.saraswati.jain.jainsaraswati.Models.Time;
import com.saraswati.jain.jainsaraswati.Models.Tithi;
import com.saraswati.jain.jainsaraswati.R;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import swisseph.DblObj;
import swisseph.SweConst;
import swisseph.SweDate;
import swisseph.SwissEph;

public class PanchangHelper {

    private static final double dfactor = 24.0;
    private static final double navkarshioffset = 0.8;;
    private static SwissEph swe = new SwissEph();



    PanchangHelper() {


    }


    public static Tithi getTithi(Date date, Place place ){
        // returns 1 for ekkam , 2 for beej , ..... 15 for Poonam , .... 30 for Amavasya
        double reqmonth;
        SweDate sweDate = new SweDate(date.getYear(),date.getMonth(),date.getDay(),0);

        //This jd is in local time
        //Tithi Calculation
        double jd = sweDate.getJulDay();
        double sunrise = getSunrise(jd,place);
        double moon_phase = lunar_phase(sunrise);
        int today = (int) Math.ceil(moon_phase/12);
        int tithi = today;
        //Checking for skipped tithi

        //Tomorrow Tithi
        double sunrise_tomorrow = sunrise+1; //Not really sure about this line
        double moon_phase_tomorrow = lunar_phase(sunrise_tomorrow);
        int tithi_tomorrow = (int) Math.ceil(moon_phase_tomorrow/12);

        //Day After Tomorrow tithi
        double sunrise_day_after_tomorrow = sunrise+2; //Not really sure about this line
        double moon_phase_day_after_tomorrow = lunar_phase(sunrise_day_after_tomorrow);
        int tithi_day_after_tomorrow = (int) Math.ceil(moon_phase_day_after_tomorrow/12);

        
        //Checking for skipped tithi
        boolean isSkippedTomorrow = ((tithi_tomorrow - today) % 30)>1;
        if (isSkippedTomorrow){
            int skippedTithi = today+1;
            //Handling According to Jain Calendar (Kshaya Tithi)
            if(skippedTithi == 2 || skippedTithi == 5 || skippedTithi == 8 || skippedTithi == 11 || skippedTithi == 14 || skippedTithi == 15 || skippedTithi==17 || skippedTithi == 20 || skippedTithi == 23 || skippedTithi == 26 || skippedTithi==29 || skippedTithi==30){
                tithi = skippedTithi;
            }

        }

        // Checking for extra tithi

        if (today == tithi_tomorrow){
            //Handling According To Jain Calendar (Vriddhi Tithi)
            if(today == 2 || today == 5 || today == 8 || today == 11 || today == 14 || today == 15 || today==17 || today == 20 || today == 23 || today == 26 || today==29 || today==30){

                tithi = today-1;

            }

        }

        //Checking for skipped tithi
        boolean isSkippedDayAfterTomorrow = ((tithi_day_after_tomorrow-tithi_tomorrow)%30)>1;
        if(isSkippedDayAfterTomorrow){
            int skippedTithi = tithi_tomorrow+1;

            if(skippedTithi == 15 || skippedTithi == 30){
                    tithi = tithi_tomorrow;
            }

        }


        //Checking for extra tithi
        if(tithi == 14 || tithi == 29){
            if((tithi_tomorrow == tithi_day_after_tomorrow) && (tithi_tomorrow == 15 || tithi_tomorrow == 30)){
                tithi = tithi - 1;
            }
        }




        boolean sv = true;
        if(tithi<=15){
            sv=true;
        }else if(tithi <= 30){
            sv=false;
        }
        //Month Calculation
        double last_new_moon = new_moon(sunrise,tithi,-1);
        double this_solar_month = raasi(last_new_moon);

        reqmonth = this_solar_month+1;

        if(reqmonth > 12){
            reqmonth = reqmonth%12;
        }

        return new Tithi(sv,tithi,(int)reqmonth);

    }
    public static Time getLocalSunrise(Date date , Place place){
        SweDate sweDate = new SweDate(date.getYear(),date.getMonth(),date.getDay(),0);
        double jd = sweDate.getJulDay();
        double sunrise = getSunrise(jd,place);

        SweDate sweDate1 = new SweDate(sunrise+GlobalHelper.getTimeZoneOffset(place.getTimezone())/dfactor);

        return to_hms(sweDate1.getHour());

    }

    public  static  Time getLocalSunset(Date date,Place place){
        SweDate sweDate = new SweDate(date.getYear(),date.getMonth(),date.getDay(),0);
        double jd = sweDate.getJulDay();
        double sunrise = getSunset(jd,place);

        SweDate sweDate1 = new SweDate(sunrise+GlobalHelper.getTimeZoneOffset(place.getTimezone())/dfactor);

        return to_hms(sweDate1.getHour());


    }

    private static double getSunrise(double jd, Place place){


        //returns Sunrise time for given date and place in UTC Julian Day

        double[] pos = {place.getLongitude(),place.getLatitude(),0};
        double tz = GlobalHelper.getTimeZoneOffset(place.getTimezone());
        DblObj rise = new DblObj();
        StringBuffer stringBuffer = new StringBuffer("Something Went Wrong!!");
        StringBuffer star = new StringBuffer();
        swe.swe_rise_trans(
                jd-(tz/dfactor), //This steps converts local time JD to UTC jd
                SweConst.SE_SUN,
                star,
                SweConst.SEFLG_SWIEPH,
                SweConst.SE_CALC_RISE + SweConst.SE_BIT_DISC_CENTER ,
                pos,
                0.0,
                0.0,
                rise ,
                stringBuffer
        );



        return rise.val; // returns sunrise in UTC jd
    }
    private static double getSunset(double jd, Place place){


        //returns Sunset time for given date and place in UTC Julian Day format

        double[] pos = {place.getLongitude(),place.getLatitude(),0};
        double tz = GlobalHelper.getTimeZoneOffset(place.getTimezone());
        DblObj set = new DblObj();
        StringBuffer stringBuffer = new StringBuffer("Something Went Wrong!!");
        StringBuffer star = new StringBuffer();
        swe.swe_rise_trans(
                jd-(tz/dfactor),
                SweConst.SE_SUN,
                star,
                SweConst.SEFLG_SWIEPH,
                SweConst.SE_CALC_SET + SweConst.SE_BIT_DISC_CENTER ,
                pos,
                0.0,
                0.0,
                set ,
                stringBuffer
        );



        return set.val; // UTC JD
    }

    private static double getMoonLongitude(double jd){
        //returns double Moon Longitudinal Position for given date given in Julian Day format
        // jd should be in UTC JD not local time JD
        double[] mvalues = new double[6];
        double mlongitude;
        StringBuffer errorBuffer = new StringBuffer("Something Went Wrong");
        swe.swe_calc_ut(
                jd,
                SweConst.SE_MOON,
                SweConst.SEFLG_SWIEPH,
                mvalues,
                errorBuffer
        );
        mlongitude = mvalues[0];

        return mlongitude;
    }

    private static double getSunLongitude(double jd){
        //returns double Sun Longitudinal Position for given date given in Julian Day format
        //jd should be in UTC JD not local time JD
        double[] svalues = new double[6];
        double slongitude;
        StringBuffer errorBuffer = new StringBuffer("Something Went Wrong");
        swe.swe_calc_ut(
                jd,
                SweConst.SE_SUN,
                SweConst.SEFLG_SWIEPH,
                svalues,
                errorBuffer
        );
        slongitude = svalues[0];

        return slongitude;
    }


    public static String[] getChogadiyas(Date date, Context context){
        String[] daychogadhiya =  new String[]{
                context.getString(R.string.udveg),
                context.getString(R.string.chal),
                context.getString(R.string.labh),
                context.getString(R.string.amrit),
                context.getString(R.string.kaal),
                context.getString(R.string.shubh),
                context.getString(R.string.rog),
        };
        String[] nightchogadhiya  = new String[]{
                context.getString(R.string.shubh),
                context.getString(R.string.amrit),
                context.getString(R.string.chal),
                context.getString(R.string.rog),
                context.getString(R.string.kaal),
                context.getString(R.string.labh),
                context.getString(R.string.udveg),
        };
        Calendar calendar = Calendar.getInstance();
        calendar.set(date.getYear(),date.getMonth()-1,date.getDay());
        String[] chogadys = new String[16];
        int wday = calendar.get(Calendar.DAY_OF_WEEK);
        wday = wday-1;
        int i = (wday*3)%7;
        int j = (wday*2)%7;
        int count1=0;
        int count2=8;
        while (true){
            if (count2<16){
                chogadys[count1] = daychogadhiya[i];
                chogadys[count2] = nightchogadhiya[j];
                i= (i+1)%7;
                j = (j+1)%7;
                count1++;
                count2++;

            }else{
                return chogadys;
            }

        }
    }

    public static String[] getChogadiyaTimings(Time sunrise1,Time sunset , Time sunrise2){

        String[] timings = new String[17];
        double rise1 = sunrise1.getHours()+sunrise1.getMinutes()/60.0+sunrise1.getSeconds()/3600.0;
        double set = sunset.getHours()+sunset.getMinutes()/60.0+sunset.getSeconds()/3600.0;
        double rise2 = sunrise2.getHours()+sunrise2.getMinutes()/60.0+sunrise2.getSeconds()/3600.0;
        rise2 +=24.0;
        double hour =rise1;
        double offset1 = (set-rise1)/8.0;
        double offset2 = (rise2-set)/8.0;

        int i=0;
        while(true){
            if(i<17){
                Time hms;
                hms = to_hms(hour);

                if(hms.getHours()>=24){
                    hms.setHours(hms.getHours()-24);
                }
                NumberFormat f = new DecimalFormat("00");
                timings[i] = String.format("%s:%s", f.format(hms.getHours()), f.format(hms.getMinutes()));

                if(i<8){
                    hour = hour + offset1;
                }else{
                    hour = hour + offset2;
                }
                i++;

            }else{
                return timings;
            }

        }


    }


    public static String[] getHoras(Date date, Context context){


        Calendar calendar = Calendar.getInstance();
        calendar.set(date.getYear(),    date.getMonth()-1,date.getDay());
        int week_day = calendar.get(Calendar.DAY_OF_WEEK);
        String[] horas = new String[]{
                context.getString(R.string.sun),
                context.getString(R.string.venus),
                context.getString(R.string.mercury),
                context.getString(R.string.moon),
                context.getString(R.string.saturn),
                context.getString(R.string.jupiter),
                context.getString(R.string.mars)
        };

        String[] weekdayhora = new String[24];
        week_day = week_day - 1;
        int i = (week_day * 3) % 7;

        int count = 0;
        while (true) {
            if (count < 24) {
                weekdayhora[count] = horas[i];
                i = (i + 1) % 7;
                count++;
            } else {
                return weekdayhora;

            }

        }


    }


    public static String[] getHorasTimings(Time sunrise1 , Time sunset , Time sunrise2){

        String[] horatimings = new String[25];
        double rise1 = sunrise1.getHours()+sunrise1.getMinutes()/60.0+sunrise1.getSeconds()/3600.0;
        double set = sunset.getHours()+sunset.getMinutes()/60.0+sunset.getSeconds()/3600.0;
        double rise2 = sunrise2.getHours()+sunrise2.getMinutes()/60.0+sunrise2.getSeconds()/3600.0;
        rise2 += 24;
        double offset1 = (set - rise1) / 12.0;
        double offset2 = (rise2 - set) / 12.0;
        double hour = rise1;

        int i = 0;
        while (true) {
            if (i<25) {
                Time hms;
                hms = to_hms(hour);
                if(hms.getHours()>=24){
                    hms.setHours(hms.getHours()-24);
                }
                NumberFormat f = new DecimalFormat("00");
                horatimings[i] = String.format("%s:%s", f.format(hms.getHours()), f.format(hms.getMinutes()));
                if (i < 12) {
                    hour = hour + offset1;
                } else {
                    hour = hour + offset2;
                }
                i++;
            } else {
                return horatimings;
            }

        }


    }

    static public List<Time> getTimings(Date date, Place place){
        //returns Navkarshi,Porshi,SadhPorshi Timings
        List<Time> timeList = new ArrayList<>();
        double tz = GlobalHelper.getTimeZoneOffset(place.getTimezone());
        SweDate sweDate = new SweDate(date.getYear(),date.getMonth(),date.getDay(),0);
        double jd = sweDate.getJulDay();

        double sunrise = getSunrise(jd,place);
        double sunset = getSunset(jd,place);
        SweDate sweDate1 = new SweDate(sunrise + tz/dfactor);
        SweDate sweDate2 = new SweDate(sunset + tz/dfactor);
        double sunrisehour = sweDate1.getHour();
        double sunsethour = sweDate2.getHour();
        double totaldaytime = sunsethour - sunrisehour;
        double porshioffset = totaldaytime/4.0;
        double sadhporshioffset = porshioffset*(1.5);
        double purimadhoffset = totaldaytime*(0.75);


        timeList.add(to_hms(sunrisehour));
        timeList.add(to_hms(sunrisehour+navkarshioffset));
        timeList.add(to_hms(sunrisehour + porshioffset));
        timeList.add(to_hms(sunrisehour + sadhporshioffset));
        timeList.add(to_hms(sunrisehour + purimadhoffset));
        timeList.add(to_hms(sunsethour));


        return timeList;
    }
    public static Pair<Integer,Time> getNakshatras(Date date, Place place){
        //returns 1 ,2 ,,,,etc
        //jd is in LOCAL TIME
        SweDate sweDate = new SweDate(date.getYear(),date.getMonth(),date.getDay(),0);
        double jd = sweDate.getJulDay();
        swe.swe_set_sid_mode(SweConst.SE_SIDM_LAHIRI);
        double sunrise = getSunrise(jd,place);
        double nakshatra;
        double[] longitudes = new double[5];
        double[] offsets = new double[]{
                0.0,0.25,0.5,0.75,1.0
        };

        for(int i=0;i<5;i++){
            longitudes[i] = ((((getMoonLongitude(sunrise+i/4.0) - swe.swe_get_ayanamsa_ut(sunrise))%360)+360)%360);
        }

        nakshatra = longitudes[0]*(27/360.0);
        nakshatra = Math.ceil(nakshatra);
        double[] y = unwrap_angles(longitudes);
        double[] x = offsets;
        double approx_end = inverse_lagrange(x,y,nakshatra*(360/27.0));
        double ends = ((sunrise - jd + approx_end)*dfactor + GlobalHelper.getTimeZoneOffset(place.getTimezone()));


//        /**double nak_tmrw = Math.ceil(longitudes[4]*(27/360.0));
//         boolean isSkipped = ((nak_tmrw - nakshatra)%27)>1;
//         if(isSkipped){
//         double leap_nak = nakshatra + 1;
//         approx_end = inverse_lagrange(offsets, longitudes, leap_nak * 360 / 27);
//         ends = (sunrise - jd + approx_end) *dfactor + place[2];
//
//         }**/

        Time endTime = to_hms(ends);

        return new Pair<>((int) nakshatra,endTime);
    }
    private static double lunar_phase(double jd){
        double mlongitude = getMoonLongitude(jd);
        double slongitude = getSunLongitude(jd);

        if(mlongitude<slongitude){
            mlongitude = mlongitude + 360;
        }


        return (mlongitude - slongitude);
    }
    private static double raasi(double jd){
        SwissEph s = new SwissEph();
        s.swe_set_sid_mode(SweConst.SE_SIDM_LAHIRI);
        double solar_nirayana = (getSunLongitude(jd) - swe.swe_get_ayanamsa_ut(jd))%360;
        return Math.ceil(solar_nirayana/30.0);
    }
    private static double new_moon(double jd,double tithi,int opt){
        double start = 0;
        if (opt==-1){
            start = jd - tithi;
        }else if(opt ==1){
            start = jd + (30-tithi);
        }

        double[] x = new double[17];
        for(int i=0;i<17;i++){
            x[i] = (-2+((double)(i)/4));
        }
        double[] y = new double[17];
        for(int i=0;i<17;i++){
            y[i] = lunar_phase(start + x[i]);

        }
        y = unwrap_angles(y);
        double y0 = inverse_lagrange(x,y,360);


        return (start+y0);
    }
    // Helping Functions
    private static double[] unwrap_angles(double[] angles){

        for (int i=1;i<angles.length;i++){
            if(angles[i] < angles[i-1]){
                angles[i] += 360;
            }
        }

        return angles;
    }

    private static double inverse_lagrange(double[] x,double[] y ,double ya){

        double total =0;

        for (int i=0;i<x.length;i++){
            double numer = 1;
            double denom = 1;
            for (int j=0;j<x.length;j++){
                if(j!=i){
                    numer *= (ya - y[j]);
                    denom *= (y[i]-y[j]);
                }
            }
            total+= (((numer)*(x[i]))/denom);
        }



        return total;
    }

    public static Time to_hms(double hour){

        int reqhour = (int) hour;
        double min = (hour - reqhour)*60;
        int reqmin = (int) min;
        double seconds = (min-reqmin)*60;




        return new Time(reqhour,reqmin,(int)seconds);
    }



}
