package com.example.deschreibung;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.deschreibung.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.buttonGoToWriting.setOnClickListener(v ->
                startActivity(new Intent(this, WritingActivity.class)));

        binding.buttonGoToVocabulary.setOnClickListener(v ->
                startActivity(new Intent(this, VocabularyListActivity.class)));

        binding.buttonGoToHistory.setOnClickListener(v ->
                startActivity(new Intent(this, ScoreHistoryActivity.class)));
    }
}