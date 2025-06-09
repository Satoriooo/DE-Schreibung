package com.example.deschreibung;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.deschreibung.models.ScoreHistory;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ScoreHistoryAdapter extends RecyclerView.Adapter<ScoreHistoryAdapter.ScoreHistoryViewHolder> {

    private final List<ScoreHistory> scoreHistoryList;

    public ScoreHistoryAdapter(List<ScoreHistory> scoreHistoryList) {
        this.scoreHistoryList = scoreHistoryList;
    }

    @NonNull
    @Override
    public ScoreHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_score_history, parent, false);
        return new ScoreHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScoreHistoryViewHolder holder, int position) {
        // Find the data for the current item
        ScoreHistory currentItem = scoreHistoryList.get(position);
        // Tell the ViewHolder to update its views with this data
        holder.bind(currentItem);
    }

    @Override
    public int getItemCount() {
        return scoreHistoryList.size();
    }


    /**
     * The ViewHolder now contains all the logic for a single list item.
     * It finds the views, formats the data, and handles the click event.
     */
    static class ScoreHistoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewScore;
        private final TextView textViewTimestamp;

        public ScoreHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewScore = itemView.findViewById(R.id.textViewItemScore);
            textViewTimestamp = itemView.findViewById(R.id.textViewItemTimestamp);
        }

        /**
         * This method takes a ScoreHistory object and updates the views.
         * It also sets the click listener.
         */
        public void bind(final ScoreHistory item) {
            textViewScore.setText(String.valueOf(item.getScore()));
            textViewTimestamp.setText(formatTimestamp(item.getTimestamp()));

            // Set the click listener for the entire list item view
            itemView.setOnClickListener(v -> {
                // Get the context (the activity) from the item's view
                Context context = itemView.getContext();
                // Create an Intent to open ScoreDetailActivity
                Intent intent = new Intent(context, ScoreDetailActivity.class);
                // Attach the unique ID of the clicked item to the Intent