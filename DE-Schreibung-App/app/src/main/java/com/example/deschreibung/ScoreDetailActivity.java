package com.example.deschreibung;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.deschreibung.databinding.ActivityScoreDetailBinding;
import com.example.deschreibung.helper.DatabaseHelper;
import com.example.deschreibung.models.ScoreHistory;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ScoreDetailActivity extends AppCompatActivity {

    public static final String EXTRA_SCORE_ID = "extra_score_id";
    private ActivityScoreDetailBinding binding;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityScoreDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        dbHelper = new DatabaseHelper(this);

        setupToolbar();

        long scoreId = getIntent().getLongExtra(EXTRA_SCORE_ID, -1);
        if (scoreId == -1) {
            Toast.makeText(this, "Fehler: Ungültige Bewertungs-ID.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        loadScoreDetails(scoreId);
    }

    private void setupToolbar() {
        binding.toolbarScoreDetail.setNavigationOnClickListener(v -> finish());
    }

    private void loadScoreDetails(long id) {
        ScoreHistory score = dbHelper.getScoreHistoryById(id);
        if (score != null) {
            binding.textViewDetailScore.setText(String.format(Locale.GERMAN, "%d / 100", score.getScore()));
            binding.textViewDetailTimestamp.setText(formatTimestamp(score.getTimestamp()));
            binding.textViewDetailOriginalText.setText(score.getOriginalText());
            binding.textViewDetailFeedbackComment.setText(score.getFeedbackComment());
            binding.textViewDetailGrammaticalExplanation.setText(score.getGrammaticalExplanation());

            // Highlight differences and set the corrected text
            Spannable highlightedText = highlightDifferences(score.getOriginalText(), score.getCorrectedText());
            binding.textViewDetailCorrectedText.setText(highlightedText);
        } else {
            Toast.makeText(this, "Bewertung nicht gefunden.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private Spannable highlightDifferences(String original, String corrected) {
        if (original == null || corrected == null) return new SpannableStringBuilder(corrected);

        SpannableStringBuilder builder = new SpannableStringBuilder(corrected);
        List<String> originalWords = Arrays.asList(original.toLowerCase().split("\\s+|(?=[.,!?;:])|(?<=[.,!?;:])"));

        String[] correctedWords = corrected.split("\\s+|(?=[.,!?;:])|(?<=[.,!?;:])");
        int currentIndex = 0;

        for (String word : correctedWords) {
            int start = corrected.indexOf(word, currentIndex);
            if (start == -1) continue;

            // If the word (ignoring case) doesn't exist in the original text, highlight it
            if (!originalWords.contains(word.toLowerCase())) {
                builder.setSpan(new ForegroundColorSpan(Color.RED), start, start + word.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            currentIndex = start + word.length();
        }
        return builder;
    }

    private String formatTimestamp(String dbTimestamp) {
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        SimpleDateFormat displayFormat = new SimpleDateFormat("dd. MMMM yyyy, HH:mm 'Uhr'", Locale.GERMAN);
        try {
            Date date = dbFormat.parse(dbTimestamp);
            return displayFormat.format(date);
        } catch (ParseException | NullPointerException e) {
            return dbTimestamp; // Fallback
        }
    }
}