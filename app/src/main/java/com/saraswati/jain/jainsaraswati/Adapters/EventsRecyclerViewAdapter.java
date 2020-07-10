package com.saraswati.jain.jainsaraswati.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.saraswati.jain.jainsaraswati.R;

public class EventsRecyclerViewAdapter extends RecyclerView.Adapter<EventsRecyclerViewAdapter.EventsRecyclerViewHolder> {

    private Context context;
    private String[] events;

    EventsRecyclerViewAdapter(Context context, String[] events) {
        this.context = context;
        this.events = events;
    }

    @NonNull
    @Override
    public EventsRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.eventscard,parent,false);
        return new EventsRecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventsRecyclerViewHolder holder, int position) {
        holder.eventsTitle.setText(events[position]);
    }

    @Override
    public int getItemCount() {
        return events.length;
    }

    class EventsRecyclerViewHolder extends RecyclerView.ViewHolder{

        TextView eventsTitle;

        EventsRecyclerViewHolder(@NonNull View itemView) {
            super(itemView);

            eventsTitle = itemView.findViewById(R.id.eventstitleid);
        }
    }
}
