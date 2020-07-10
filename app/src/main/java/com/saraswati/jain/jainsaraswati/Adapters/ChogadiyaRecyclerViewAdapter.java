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

public class ChogadiyaRecyclerViewAdapter  extends  RecyclerView.Adapter<ChogadiyaRecyclerViewAdapter.ChogadiyaRecyclerViewHolder> {

    private Context context;
    private String[] chogadiyas;
    private String[] ctimings;

    ChogadiyaRecyclerViewAdapter(Context context, String[] chogadiyas, String[] ctimings) {

        this.context = context;
        this.chogadiyas = chogadiyas;
        this.ctimings = ctimings;
    }

    @NonNull
    @Override
    public ChogadiyaRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chogadiyacard,parent,false);
        return new ChogadiyaRecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChogadiyaRecyclerViewHolder holder, int position) {

            holder.chogadiyatitle.setText(chogadiyas[position]);
            holder.chogadiyastarttime.setText(ctimings[position]);
            holder.chogadiyaendtime.setText(ctimings[position+1]);


            if(position<8){
                holder.chogadiyacard.setCardBackgroundColor(context.getResources().getColor(android.R.color.holo_orange_light));
            }else{
                holder.chogadiyacard.setCardBackgroundColor(context.getResources().getColor(android.R.color.holo_blue_dark));
            }

            String chogadiyatitle = chogadiyas[position];

        if(chogadiyatitle.equals(context.getString(R.string.shubh)) || chogadiyatitle.equals(context.getString(R.string.labh))){
            holder.chogadiyatitle.setBackgroundColor(context.getResources().getColor((android.R.color.holo_green_light)));
        }else if (chogadiyatitle.equals(context.getString(R.string.amrit))){
            holder.chogadiyatitle.setBackgroundColor(context.getResources().getColor((android.R.color.holo_green_dark)));
        }else if (chogadiyatitle.equals(context.getString(R.string.chal))){
            holder.chogadiyatitle.setBackgroundColor(context.getResources().getColor((android.R.color.darker_gray)));
        }else if (chogadiyatitle.equals(context.getString(R.string.udveg)) || chogadiyatitle.equals(context.getString(R.string.rog)) || chogadiyatitle.equals(context.getString(R.string.kaal))){
            holder.chogadiyatitle.setBackgroundColor(context.getResources().getColor((android.R.color.holo_red_dark)));
        }

    }

    @Override
    public int getItemCount() {
        return 16;
    }

    class ChogadiyaRecyclerViewHolder extends RecyclerView.ViewHolder{

        CardView chogadiyacard;

        TextView chogadiyatitle;
        TextView chogadiyastarttime;
        TextView chogadiyaendtime;

        ChogadiyaRecyclerViewHolder(View itemView) {
            super(itemView);

            chogadiyacard = itemView.findViewById(R.id.chogadiyacardid);

            chogadiyatitle = itemView.findViewById(R.id.chogadiyatitleid);
            chogadiyastarttime = itemView.findViewById(R.id.chogadiyastarttimeid);
            chogadiyaendtime = itemView.findViewById(R.id.chogadiyaendtimeid);

        }
    }
}
