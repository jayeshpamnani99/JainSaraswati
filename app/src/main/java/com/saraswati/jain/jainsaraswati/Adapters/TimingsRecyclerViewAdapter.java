package com.saraswati.jain.jainsaraswati.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.saraswati.jain.jainsaraswati.Models.Time;
import com.saraswati.jain.jainsaraswati.R;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

public class TimingsRecyclerViewAdapter extends RecyclerView.Adapter<TimingsRecyclerViewAdapter.TimingsRecyclerViewHolder> {
    private Context context;
    private List<Time> timeList;
    private String[] titles;
    TimingsRecyclerViewAdapter(Context context, List<Time> timeList) {
        this.context = context;
        this.timeList = timeList;
        titles = new String[]{context.getString(R.string.sunrise),context.getString(R.string.navkarshi),context.getString(R.string.porshi),context.getString(R.string.sadh_porshi),context.getString(R.string.avadh),context.getString(R.string.chauvihar)};
    }

    @NonNull
    @Override
    public TimingsRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.timingscard,parent,false);
        return new TimingsRecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimingsRecyclerViewHolder holder, int position) {
        Time time = timeList.get(position);

        holder.timingsTitle.setText(titles[position]);
        NumberFormat f = new DecimalFormat("00");

        holder.timingsTime.setText(String.format("%s:%s", f.format(time.getHours()), f.format(time.getMinutes())));
    }

    @Override
    public int getItemCount() {
        return timeList.size();
    }

    class TimingsRecyclerViewHolder extends RecyclerView.ViewHolder{

        TextView timingsTitle;
        TextView timingsTime;

        TimingsRecyclerViewHolder(@NonNull View itemView) {
            super(itemView);

            timingsTitle = itemView.findViewById(R.id.timingstitleid);
            timingsTime = itemView.findViewById(R.id.timingstimeid);
        }
    }
}
