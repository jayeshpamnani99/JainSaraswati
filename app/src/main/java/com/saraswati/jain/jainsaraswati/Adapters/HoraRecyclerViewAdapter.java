package com.saraswati.jain.jainsaraswati.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.saraswati.jain.jainsaraswati.R;

public class HoraRecyclerViewAdapter extends  RecyclerView.Adapter<HoraRecyclerViewAdapter.HoraRecyclerViewHolder> {

    private Context context;
    private String[] horas;
    private String[] horastimings;


    HoraRecyclerViewAdapter(Context context, String[] horas, String[] horastimings) {
        this.context = context;
        this.horas = horas;
        this.horastimings = horastimings;
    }

    @NonNull
    @Override
    public HoraRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.horacard,parent,false);
        return new HoraRecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HoraRecyclerViewHolder holder, int position) {

        holder.horatitle.setText(horas[position]);
        holder.horastartime.setText(horastimings[position]);
        holder.horaendtime.setText(horastimings[position+1]);
    
        String horatitle = horas[position];
        if (horatitle.equals(context.getString(R.string.mercury))){
            holder.horatitle.setBackgroundColor(context.getResources().getColor(android.R.color.holo_green_light));
        }else if (horatitle.equals(context.getString(R.string.jupiter))){
            holder.horatitle.setBackgroundColor(context.getResources().getColor(android.R.color.holo_orange_dark));
        }else if (horatitle.equals(context.getString(R.string.venus)) || horatitle.equals(context.getString(R.string.moon))){
            holder.horatitle.setBackgroundColor(context.getResources().getColor(android.R.color.white));
        }else if (horatitle.equals(context.getString(R.string.saturn))){
            holder.horatitle.setBackgroundColor(context.getResources().getColor(android.R.color.darker_gray));
        }else if (horatitle.equals(context.getString(R.string.sun))){
            holder.horatitle.setBackgroundColor(context.getResources().getColor(android.R.color.holo_red_light));
        }else if (horatitle.equals(context.getString(R.string.mars))){
            holder.horatitle.setBackgroundColor(context.getResources().getColor(android.R.color.holo_red_dark));
        }

        if(position<12){
            holder.horacard.setCardBackgroundColor(context.getResources().getColor(android.R.color.holo_orange_light));
        }else{
            holder.horacard.setCardBackgroundColor(context.getResources().getColor(android.R.color.holo_blue_dark));
        }

    }

    @Override
    public int getItemCount() {
        return 24;
    }

    class HoraRecyclerViewHolder extends RecyclerView.ViewHolder{
        
        CardView horacard;
        
        TextView horastartime;
        TextView horaendtime;
        TextView horatitle;

        HoraRecyclerViewHolder(@NonNull View itemView) {
            super(itemView);

            horacard = itemView.findViewById(R.id.horacardid);
            
            horastartime = itemView.findViewById(R.id.horastarttimeid);
            horaendtime = itemView.findViewById(R.id.horaendtimeid);
            horatitle = itemView.findViewById(R.id.horatitleid);
        }
    }
}
