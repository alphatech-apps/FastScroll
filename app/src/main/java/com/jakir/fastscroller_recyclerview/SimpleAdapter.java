package com.jakir.fastscroller_recyclerview;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SimpleAdapter extends RecyclerView.Adapter<SimpleAdapter.Holder> {

    int total;

    public SimpleAdapter(int total) {
        this.total = total;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_row, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.text.setText("Item: " + position);
    }

    @Override
    public int getItemCount() {
        return total;
    }

    static class Holder extends RecyclerView.ViewHolder {
        TextView text;
        public Holder(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.rowText);
        }
    }
}
