package com.saraswati.jain.jainsaraswati.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.saraswati.jain.jainsaraswati.Models.Time;
import com.saraswati.jain.jainsaraswati.R;

import java.util.List;

public class BottomSheetRecyclerAdapter extends RecyclerView.Adapter<BottomSheetRecyclerAdapter.BottomSheetRecyclerViewHolder> {

    private Context context;
    private String[] events;
    private List<Time> timeList;
    private String[] chogadiyastitles;
    private String[] chogadiyastimes;
    private String[] horas;
    private String[] horastimings;


    public BottomSheetRecyclerAdapter(Context context, String[] events, List<Time> timeList, String[] chogadiyastitles, String[] chogadiyastimes, String[] horas, String[] horastimings) {
        this.context = context;
        this.events = events;
        this.timeList = timeList;
        this.chogadiyastitles = chogadiyastitles;
        this.chogadiyastimes = chogadiyastimes;
        this.horas = horas;
        this.horastimings = horastimings;
    }

    @NonNull
    @Override
    public BottomSheetRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.bottomsheetcard,parent,false);
        return new BottomSheetRecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BottomSheetRecyclerViewHolder holder, int position) {



        switch (position){
            case 0:
                if(events.length>0){
                    holder.bottomSheetCardTitle.setText(context.getResources().getString(R.string.special_events));
                    holder.bottomSheetRecyclerView.setAdapter(new EventsRecyclerViewAdapter(context,events));
                }else{
                    holder.itemView.setVisibility(View.GONE);
                }

                return;
            case 1:
                holder.bottomSheetCardTitle.setText(context.getResources().getString(R.string.timings));
                TimingsRecyclerViewAdapter timingsRecyclerViewAdapter = new TimingsRecyclerViewAdapter(context,timeList);
                holder.bottomSheetRecyclerView.setAdapter(timingsRecyclerViewAdapter);
                return;
            case 2:
                holder.bottomSheetCardTitle.setText(context.getResources().getString(R.string.chogadiya));
                ChogadiyaRecyclerViewAdapter chogadiyaRecyclerViewAdapter = new ChogadiyaRecyclerViewAdapter(context,chogadiyastitles,chogadiyastimes);
                holder.bottomSheetRecyclerView.setAdapter(chogadiyaRecyclerViewAdapter);
                return;
            case 3:
                holder.bottomSheetCardTitle.setText(context.getResources().getString(R.string.hora));
                HoraRecyclerViewAdapter horaRecyclerViewAdapter = new HoraRecyclerViewAdapter(context,horas,horastimings);
                holder.bottomSheetRecyclerView.setAdapter(horaRecyclerViewAdapter);
                return;
            default:


        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }

    class BottomSheetRecyclerViewHolder extends RecyclerView.ViewHolder{


        TextView bottomSheetCardTitle;
        RecyclerView bottomSheetRecyclerView;
        BottomSheetRecyclerViewHolder(@NonNull View itemView) {
            super(itemView);


            bottomSheetCardTitle  = itemView.findViewById(R.id.bottomsheetcardtitleid);
            bottomSheetRecyclerView = itemView.findViewById(R.id.bottomsheetcardrecyclerid);

            bottomSheetRecyclerView.setLayoutManager(new LinearLayoutManager(context,RecyclerView.HORIZONTAL,false));
            bottomSheetRecyclerView.setHasFixedSize(true);
        }

    }



}
