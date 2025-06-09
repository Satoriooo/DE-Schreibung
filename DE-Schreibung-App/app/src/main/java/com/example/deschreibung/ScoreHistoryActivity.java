package com.example.deschreibung;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.deschreibung.databinding.ActivityScoreHistoryBinding;
import com.example.deschreibung.helper.DatabaseHelper;
import com.example.deschreibung.models.ScoreHistory;
import java.util.List;

public class ScoreHistoryActivity extends AppCompatActivity {

    private ActivityScoreHistoryBinding binding;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityScoreHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = new DatabaseHelper(this);

        setupToolbar();
        setupRecyclerView();
    }



    private void setupToolbar() {
        // Sets the click listener for the back arrow (navigation icon)
        binding.toolbarScoreHistory.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        // Set the layout manager for the RecyclerView
        binding.recyclerViewScoreHistory.setLayoutManager(new LinearLayoutManager(this));

        // Fetch the data from the database
        List<ScoreHistory> scoreHistoryList = dbHelper.getAllScoreHistory();

        // Create and set the adapter
        ScoreHistoryAdapter adapter = new ScoreHistoryAdapter(scoreHistoryList);
        binding.recyclerViewScoreHistory.setAdapter(adapter);
    }
}