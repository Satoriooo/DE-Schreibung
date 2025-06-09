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
        ScoreHistory currentItem = scoreHistoryList.get(position);
        holder.bind(currentItem);
    }

    @Override
    public int getItemCount() {
        return scoreHistoryList.size();
    }

    static class ScoreHistoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewScore;
        private final TextView textViewTimestamp;

        public ScoreHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewScore = itemView.findViewById(R.id.textViewItemScore);
            textViewTimestamp = itemView.findViewById(R.id.textViewItemTimestamp);
        }

        public void bind(final ScoreHistory item) {
            textViewScore.setText(String.valueOf(item.getScore()));
            textViewTimestamp.setText(formatTimestamp(item.getTimestamp()));

            itemView.setOnClickListener(v -> {
                Context context = itemView.getContext();
                Intent intent = new Intent(context, ScoreDetailActivity.class);
                intent.putExtra(ScoreDetailActivity.EXTRA_SCORE_ID, item.getId());
                context.startActivity(intent);
            });
        }

        // This method must be INSIDE the ViewHolder class to be found
        private String formatTimestamp(String dbTimestamp) {
            SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd. MMMM<x_bin_615>, HH:mm 'Uhr'", Locale.GERMAN);
            try {
                Date date = dbFormat.parse(dbTimestamp);
                return displayFormat.format(date);
            } catch (ParseException | NullPointerException e) {
                return dbTimestamp;
            }
        }
    }
}