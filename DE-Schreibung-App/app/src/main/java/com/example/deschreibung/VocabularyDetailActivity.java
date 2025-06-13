package com.example.deschreibung;

import android.graphics.Color; // <-- ADD
import android.os.Bundle;
import android.text.Spannable; // <-- ADD
import android.text.SpannableStringBuilder; // <-- ADD
import android.text.style.ForegroundColorSpan; // <-- ADD
import android.text.style.StyleSpan; // <-- ADD
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.deschreibung.databinding.ActivityVocabularyDetailBinding;
import com.example.deschreibung.helper.DatabaseHelper;
import com.example.deschreibung.models.ExampleSentence; // <-- ADD
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
            finish();
            return;
        }
        loadVocabularyDetails(vocabularyId);
        binding.buttonMoreExamples.setOnClickListener(v -> fetchMoreExamples());
    }

    private void setupToolbar() {
        binding.toolbarVocabularyDetail.setNavigationOnClickListener(v -> finish());
    }

    private void loadVocabularyDetails(long id) {
        // ... (this method is unchanged)
    }

    private void fetchMoreExamples() {
        // ... (this method is unchanged)
    }

    // THIS METHOD IS UPDATED TO HANDLE THE NEW RESPONSE STRUCTURE
    private void displayMoreExamples(MoreExamplesResponse response) {
        binding.containerMoreExamples.removeAllViews();
        binding.containerMoreExamples.setVisibility(View.VISIBLE);

        if (response.getExamples() == null || response.getExamples().isEmpty()) {
            binding.containerMoreExamples.addView(createExampleTextView("Keine weiteren Beispiele gefunden.", false));
            return;
        }

        for (ExampleSentence example : response.getExamples()) {
            binding.containerMoreExamples.addView(createExampleTextView(example));
        }
    }

    // NEW: Overloaded method to handle the new ExampleSentence object
    private TextView createExampleTextView(ExampleSentence example) {
        // Example: "• Der deutsche Satz.\n  (The English sentence.)"
        String germanPart = "• " + example.getGerman() + "\n";
        String englishPart = "  (" + example.getEnglish() + ")";

        SpannableStringBuilder builder = new SpannableStringBuilder(germanPart + englishPart);

        // Make the English part grey and italic
        builder.setSpan(new ForegroundColorSpan(Color.GRAY), germanPart.length(), builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.setSpan(new StyleSpan(android.graphics.Typeface.ITALIC), germanPart.length(), builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return createBaseTextView(builder);
    }

    // NEW: Overloaded method for simple text
    private TextView createExampleTextView(String text, boolean isError) {
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        if (isError) {
            builder.setSpan(new StyleSpan(android.graphics.Typeface.ITALIC), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return createBaseTextView(builder);
    }

    // NEW: Base method to create and style a TextView to reduce code duplication
    private TextView createBaseTextView(CharSequence text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyMedium);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 8, 0, 8); // Added a bit more margin
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