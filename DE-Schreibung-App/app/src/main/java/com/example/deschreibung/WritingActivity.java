package com.example.deschreibung;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.deschreibung.databinding.ActivityWritingBinding;
import com.example.deschreibung.helper.DatabaseHelper;
import com.example.deschreibung.models.Vocabulary;
import com.example.deschreibung.network.ApiRequest;
import com.example.deschreibung.network.ApiResponse;
import com.example.deschreibung.network.DetailedScore; // NEW
import com.example.deschreibung.network.RetrofitClient;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale; // NEW
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WritingActivity extends AppCompatActivity {
    // ... (class properties are unchanged) ...
    private ActivityWritingBinding binding;
    private DatabaseHelper dbHelper;
    private final Random random = new Random();

    private final List<String> topics = Arrays.asList(
            "Was hast du am Wochenende gemacht?", "Beschreibe deine Heimatstadt.",
            "Was sind deine Pläne für die Zukunft?", "Erzähle von deinem letzten Urlaub.",
            "Was ist dein Lieblingsbuch und warum?", "Welche Rolle spielt Technologie in deinem Leben?",
            "Beschreibe einen perfekten Tag.", "Was ist wichtiger: Geld oder Glück?",
            "Welche Fremdsprache möchtest du noch lernen?", "Erzähle von einer wichtigen Person in deinem Leben.",
            "Wie bleibst du gesund und fit?", "Was war das beste Geschenk, das du je bekommen hast?",
            "Beschreibe dein Traumhaus.", "Welche Musik hörst du gerne?",
            "Was würdest du tun, wenn du im Lotto gewinnen würdest?", "Erzähle von einer lustigen Kindheitserinnerung.",
            "Was ist deine Meinung zum Thema Umweltschutz?", "Welchen Film hast du zuletzt im Kino gesehen?",
            "Beschreibe deinen Job oder dein Studium.", "Was ist dein Lieblingsessen?",
            "Wie hat sich dein Leben in den letzten fünf Jahren verändert?", "Was bedeutet Freundschaft für dich?",
            "Reist du lieber allein oder in einer Gruppe?", "Was ist eine Fähigkeit, die du gerne beherrschen würdest?",
            "Beschreibe das Wetter in deiner Region.", "Was machst du, um Stress abzubauen?",
            "Welche historische Epoche findest du faszinierend?", "Erzähle von einem Fehler, aus dem du gelernt hast.",
            "Was ist dein Lieblings-Feiertag?", "Wie wichtig ist dir die Familie?"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWritingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        dbHelper = new DatabaseHelper(this);
        setNewRandomTopic();
        setupClickListeners();
    }

    // ... (setupClickListeners, setNewRandomTopic, handleGetFeedbackClick, toggleLoadingState are unchanged) ...
    private void setupClickListeners() {
        binding.buttonNewTopic.setOnClickListener(v -> setNewRandomTopic());
        binding.buttonGetFeedback.setOnClickListener(v -> handleGetFeedbackClick());
    }

    private void setNewRandomTopic() {
        String randomTopic = topics.get(random.nextInt(topics.size()));
        binding.editTextTopic.setText(randomTopic);
        binding.editTextUserText.setText("");
        binding.scrollViewFeedback.setVisibility(View.GONE);
    }

    private void handleGetFeedbackClick() {
        String userText = binding.editTextUserText.getText().toString().trim();
        if (TextUtils.isEmpty(userText)) {
            Toast.makeText(this, "Bitte schreiben Sie zuerst einen Text.", Toast.LENGTH_SHORT).show();
            return;
        }
        toggleLoadingState(true);
        ApiRequest request = new ApiRequest(userText);
        Call<ApiResponse> call = RetrofitClient.getApiService().evaluateText(request);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse> call, @NonNull Response<ApiResponse> response) {
                toggleLoadingState(false);
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    saveAndDisplayFeedback(apiResponse);
                } else {
                    Toast.makeText(WritingActivity.this, "Fehler vom Server: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse> call, @NonNull Throwable t) {
                toggleLoadingState(false);
                Toast.makeText(WritingActivity.this, "Netzwerkfehler: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void toggleLoadingState(boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.buttonGetFeedback.setEnabled(false);
            binding.buttonGetFeedback.setText("");
        } else {
            binding.progressBar.setVisibility(View.GONE);
            binding.buttonGetFeedback.setEnabled(true);
            binding.buttonGetFeedback.setText("Feedback Holen");
        }
    }


    // UPDATED: Now displays the detailed score breakdown
    private void saveAndDisplayFeedback(ApiResponse apiResponse) {
        long historyId = saveScoreHistory(apiResponse);
        if (historyId != -1) {
            if (apiResponse.getVocabularyList() != null && !apiResponse.getVocabularyList().isEmpty()) {
                for (Vocabulary vocab : apiResponse.getVocabularyList()) {
                    dbHelper.addVocabulary(vocab); // Use the helper method
                }
            }
        } else {
            Toast.makeText(this, "Fehler beim Speichern des Feedbacks.", Toast.LENGTH_SHORT).show();
        }

        binding.scrollViewFeedback.setVisibility(View.VISIBLE);
        binding.textViewScore.setText(String.format(Locale.GERMAN, "Bewertung: %d/100", apiResponse.getScore()));

        // NEW: Display the detailed score breakdown
        DetailedScore detailedScore = apiResponse.getDetailedScore();
        if (detailedScore != null) {
            String detailedScoreText = String.format(Locale.GERMAN,
                    "Grammatik: %d/35, Wortschatz: %d/25, Aufbau: %d/20, Ausdruck: %d/20",
                    detailedScore.getGrammar(),
                    detailedScore.getVocabulary(),
                    detailedScore.getCohesion(),
                    detailedScore.getExpressiveness());
            binding.textViewDetailedScore.setText(detailedScoreText);
            binding.textViewDetailedScore.setVisibility(View.VISIBLE);
        } else {
            binding.textViewDetailedScore.setVisibility(View.GONE);
        }

        binding.textViewFeedbackComment.setText(apiResponse.getFeedbackComment());
        binding.textViewCorrectedText.setText(apiResponse.getCorrectedText());
        binding.textViewGrammaticalExplanation.setText(apiResponse.getGrammaticalExplanation());
        Spannable highlightedText = highlightDifferences(apiResponse.getOriginalText(), apiResponse.getCorrectedText());
        binding.textViewCorrectedText.setText(highlightedText);
    }

    // ... (highlightDifferences is unchanged) ...
    private Spannable highlightDifferences(String original, String corrected) {
        if (original == null || corrected == null) {
            return new SpannableStringBuilder(corrected != null ? corrected : "");
        }
        SpannableStringBuilder builder = new SpannableStringBuilder(corrected);
        Set<String> originalWordsSet = new HashSet<>();
        Pattern wordPattern = Pattern.compile("\\w+");
        Matcher originalMatcher = wordPattern.matcher(original.toLowerCase());
        while (originalMatcher.find()) {
            originalWordsSet.add(originalMatcher.group());
        }
        Matcher correctedMatcher = wordPattern.matcher(corrected);
        while (correctedMatcher.find()) {
            String correctedWord = correctedMatcher.group();
            String cleanedCorrectedWord = correctedWord.toLowerCase();
            if (!originalWordsSet.contains(cleanedCorrectedWord)) {
                builder.setSpan(
                        new ForegroundColorSpan(Color.RED),
                        correctedMatcher.start(),
                        correctedMatcher.end(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                );
            }
        }
        return builder;
    }


    // UPDATED: Now saves the detailed score breakdown to the database
    private long saveScoreHistory(ApiResponse data) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.KEY_ORIGINAL_TEXT, data.getOriginalText());
        values.put(DatabaseHelper.KEY_CORRECTED_TEXT, data.getCorrectedText());
        values.put(DatabaseHelper.KEY_FEEDBACK_COMMENT, data.getFeedbackComment());
        values.put(DatabaseHelper.KEY_SCORE, data.getScore());
        values.put(DatabaseHelper.KEY_GRAMMATICAL_EXPLANATION, data.getGrammaticalExplanation());

        // NEW: Add detailed scores to the database record
        DetailedScore detailedScore = data.getDetailedScore();
        if (detailedScore != null) {
            values.put(DatabaseHelper.KEY_SCORE_GRAMMAR, detailedScore.getGrammar());
            values.put(DatabaseHelper.KEY_SCORE_VOCABULARY, detailedScore.getVocabulary());
            values.put(DatabaseHelper.KEY_SCORE_COHESION, detailedScore.getCohesion());
            values.put(DatabaseHelper.KEY_SCORE_EXPRESSIVENESS, detailedScore.getExpressiveness());
        }

        long id = db.insert(DatabaseHelper.TABLE_SCORE_HISTORY, null, values);
        db.close();
        return id;
    }

    // This method was incorrect; it should use the DatabaseHelper method.
    private void saveVocabulary(Vocabulary vocab) {
        dbHelper.addVocabulary(vocab);
    }
}