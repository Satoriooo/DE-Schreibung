package com.example.deschreibung;

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
    // Formatter to parse the timestamp from the database (e.g., "2025-06-07 18:30:00")
    private final SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    // Formatter to display the timestamp in a user-friendly way
    private final SimpleDateFormat displayFormat = new SimpleDateFormat("dd. MMMM yyyy, HH:mm 'Uhr'", Locale.GERMAN);

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

    class ScoreHistoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewScore;
        private final TextView textViewTimestamp;

        public ScoreHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewScore = itemView.findViewById(R.id.textViewItemScore);
            textViewTimestamp = itemView.findViewById(R.id.textViewItemTimestamp);

            // TODO: In Part 6, set an OnClickListener here to open the detail view
            // itemView.setOnClickListener(v -> { ... });
        }

        public void bind(ScoreHistory item) {
            textViewScore.setText(String.valueOf(item.getScore()));
            textViewTimestamp.setText(formatTimestamp(item.getTimestamp()));
        }

        private String formatTimestamp(String dbTimestamp) {
            try {
                Date date = dbFormat.parse(dbTimestamp);
                return displayFormat.format(date);
            } catch (ParseException | NullPointerException e) {
                // If parsing fails, return the original string as a fallback
                return dbTimestamp;
            }
        }
    }
}