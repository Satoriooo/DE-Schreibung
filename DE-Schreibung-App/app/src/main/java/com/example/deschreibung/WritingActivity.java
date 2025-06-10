package com.example.deschreibung;

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
import com.example.deschreibung.network.RetrofitClient;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WritingActivity extends AppCompatActivity {

    private ActivityWritingBinding binding;
    private DatabaseHelper dbHelper;
    private final Random random = new Random();

    // The list of topics... (no changes here)
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

    private void setupClickListeners() {
        binding.buttonNewTopic.setOnClickListener(v -> setNewRandomTopic());
        binding.buttonGetFeedback.setOnClickListener(v -> handleGetFeedbackClick());
    }

    private void setNewRandomTopic() {
        String randomTopic = topics.get(random.nextInt(topics.size()));
        binding.editTextTopic.setText(randomTopic);
        binding.editTextUserText.setText("");
        // Only hide the feedback view. The EditText is ALWAYS visible.
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

    private void saveAndDisplayFeedback(ApiResponse apiResponse) {
        long historyId = saveScoreHistory(apiResponse);
        if (historyId != -1) {
            // Optional: You can uncomment this toast if you want a confirmation message.
            // Toast.makeText(this, "Feedback gespeichert!", Toast.LENGTH_SHORT).show();
            if (apiResponse.getVocabularyList() != null && !apiResponse.getVocabularyList().isEmpty()) {
                for (Vocabulary vocab : apiResponse.getVocabularyList()) {
                    saveVocabulary(vocab);
                }
            }
        } else {
            Toast.makeText(this, "Fehler beim Speichern des Feedbacks.", Toast.LENGTH_SHORT).show();
        }

        // Only show the feedback view. The EditText is ALWAYS visible.
        binding.scrollViewFeedback.setVisibility(View.VISIBLE);
        binding.textViewScore.setText(String.format(java.util.Locale.GERMAN, "Bewertung: %d/100", apiResponse.getScore()));
        binding.textViewFeedbackComment.setText(apiResponse.getFeedbackComment());
        binding.textViewCorrectedText.setText(apiResponse.getCorrectedText());
        binding.textViewGrammaticalExplanation.setText(apiResponse.getGrammaticalExplanation());
    }

    // saveScoreHistory and saveVocabulary methods remain unchanged.
    private long saveScoreHistory(ApiResponse data) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.KEY_ORIGINAL_TEXT, data.getOriginalText());
        values.put(DatabaseHelper.KEY_CORRECTED_TEXT, data.getCorrectedText());
        values.put(DatabaseHelper.KEY_FEEDBACK_COMMENT, data.getFeedbackComment());
        values.put(DatabaseHelper.KEY_SCORE, data.getScore());
        values.put(DatabaseHelper.KEY_GRAMMATICAL_EXPLANATION, data.getGrammaticalExplanation());
        long id = db.insert(DatabaseHelper.TABLE_SCORE_HISTORY, null, values);
        db.close();
        return id;
    }

    private void saveVocabulary(Vocabulary vocab) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.KEY_GERMAN_WORD, vocab.getGermanWord());
        values.put(DatabaseHelper.KEY_ENGLISH_TRANSLATION, vocab.getEnglishTranslation());
        values.put(DatabaseHelper.KEY_EXAMPLE_SENTENCE, vocab.getExampleSentence());
        db.insertWithOnConflict(DatabaseHelper.TABLE_VOCABULARY, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        db.close();
    }
}