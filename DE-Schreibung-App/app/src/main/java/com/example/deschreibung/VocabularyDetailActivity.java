package com.example.deschreibung;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.deschreibung.databinding.ActivityVocabularyDetailBinding;
import com.example.deschreibung.helper.DatabaseHelper;
import com.example.deschreibung.models.Vocabulary;
import com.example.deschreibung.network.MoreExamplesRequest;
import com.example.deschreibung.network.MoreExamplesResponse;
import com.example.deschreibung.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VocabularyDetailActivity extends AppCompatActivity {

    public static final String EXTRA_VOCAB_ID = "extra_vocab_id";
    private ActivityVocabularyDetailBinding binding;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVocabularyDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        dbHelper = new DatabaseHelper(this);

        setupToolbar();
        long vocabularyId = getIntent().getLongExtra(EXTRA_VOCAB_ID, -1);
        if (vocabularyId == -1) {
            Toast.makeText(this, "Fehler: Ungültige Vokabel-ID.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        loadVocabularyDetails(vocabularyId);

        // NEW: Set up the listener for the new button
        binding.buttonMoreExamples.setOnClickListener(v -> fetchMoreExamples());
    }

    private void setupToolbar() {
        binding.toolbarVocabularyDetail.setNavigationOnClickListener(v -> finish());
    }

    private void loadVocabularyDetails(long id) {
        Vocabulary vocab = dbHelper.getVocabularyById(id);
        if (vocab != null) {
            binding.textViewDetailGermanWord.setText(vocab.getGermanWord());
            binding.textViewDetailEnglishTranslation.setText(vocab.getEnglishTranslation());
            binding.textViewDetailExampleSentence.setText(vocab.getExampleSentence());
        } else {
            Toast.makeText(this, "Vokabel nicht gefunden.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    // --- NEW: All logic for the new feature ---
    private void fetchMoreExamples() {
        String germanWord = binding.textViewDetailGermanWord.getText().toString();
        if (germanWord.isEmpty()) {
            return;
        }

        toggleLoadingState(true);

        MoreExamplesRequest request = new MoreExamplesRequest(germanWord);
        Call<MoreExamplesResponse> call = RetrofitClient.getApiService().getMoreExamples(request);
        call.enqueue(new Callback<MoreExamplesResponse>() {
            @Override
            public void onResponse(@NonNull Call<MoreExamplesResponse> call, @NonNull Response<MoreExamplesResponse> response) {
                toggleLoadingState(false);
                if (response.isSuccessful() && response.body() != null) {
                    displayMoreExamples(response.body());
                } else {
                    Toast.makeText(VocabularyDetailActivity.this, "Fehler: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<MoreExamplesResponse> call, @NonNull Throwable t) {
                toggleLoadingState(false);
                Toast.makeText(VocabularyDetailActivity.this, "Netzwerkfehler.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayMoreExamples(MoreExamplesResponse response) {
        binding.containerMoreExamples.removeAllViews(); // Clear old examples first
        binding.containerMoreExamples.setVisibility(View.VISIBLE);

        if (response.getExamples() == null || response.getExamples().isEmpty()) {
            binding.containerMoreExamples.addView(createExampleTextView("Keine weiteren Beispiele gefunden."));
            return;
        }

        for (String example : response.getExamples()) {
            binding.containerMoreExamples.addView(createExampleTextView("• " + example));
        }
    }

    private TextView createExampleTextView(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyMedium);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 4, 0, 4);
        textView.setLayoutParams(params);
        return textView;
    }

    private void toggleLoadingState(boolean isLoading) {
        if (isLoading) {
            binding.buttonMoreExamples.setVisibility(View.INVISIBLE);
            binding.progressMoreExamples.setVisibility(View.VISIBLE);
        } else {
            binding.buttonMoreExamples.setVisibility(View.VISIBLE);
            binding.progressMoreExamples.setVisibility(View.GONE);
        }
    }
}