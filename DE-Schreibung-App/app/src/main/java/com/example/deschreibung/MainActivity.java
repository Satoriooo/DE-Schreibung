package com.example.deschreibung;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.deschreibung.databinding.ActivityMainBinding;
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

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private DatabaseHelper dbHelper;
    private final Random random = new Random();

    // The predefined list of 30 German writing prompts as required.
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
        // Inflate the layout using View Binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize the database helper
        dbHelper = new DatabaseHelper(this);

        setNewRandomTopic();
        setupClickListeners();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_view_history) {
            Intent intent = new Intent(this, ScoreHistoryActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupClickListeners() {
        binding.buttonNewTopic.setOnClickListener(v -> setNewRandomTopic());
        binding.buttonGetFeedback.setOnClickListener(v -> handleGetFeedbackClick());
    }

    private void setNewRandomTopic() {
        // Pick and set a random topic from the list
        String randomTopic = topics.get(random.nextInt(topics.size()));
        binding.editTextTopic.setText(randomTopic);

        // Reset the UI for a new entry
        binding.editTextUserText.setText("");
        binding.editTextUserText.setVisibility(View.VISIBLE);
        binding.scrollViewFeedback.setVisibility(View.GONE);
    }

    private void handleGetFeedbackClick() {
        String userText = binding.editTextUserText.getText().toString().trim();

        if (TextUtils.isEmpty(userText)) {
            Toast.makeText(this, "Bitte schreiben Sie zuerst einen Text.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading state and start the network request
        toggleLoadingState(true);

        ApiRequest request = new ApiRequest(userText);
        Call<ApiResponse> call = RetrofitClient.getApiService().evaluateText(request);

        // Execute the call asynchronously
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse> call, @NonNull Response<ApiResponse> response) {
                toggleLoadingState(false);
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    // On success, save the data and display it
                    saveAndDisplayFeedback(apiResponse);
                } else {
                    // Handle API errors (e.g., 400, 500)
                    Toast.makeText(MainActivity.this, "Fehler vom Server: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse> call, @NonNull Throwable t) {
                // Handle network failures (e.g., no internet)
                toggleLoadingState(false);
                Toast.makeText(MainActivity.this, "Netzwerkfehler: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void toggleLoadingState(boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.buttonGetFeedback.setEnabled(false);
            binding.buttonGetFeedback.setText(""); // Hide text to make progress bar visible
        } else {
            binding.progressBar.setVisibility(View.GONE);
            binding.buttonGetFeedback.setEnabled(true);
            binding.buttonGetFeedback.setText("Feedback Holen");
        }
    }

    private void saveAndDisplayFeedback(ApiResponse apiResponse) {
        // --- Step 1: Save the data to the local SQLite database ---
        long historyId = saveScoreHistory(apiResponse);
        if (historyId != -1) { // -1 indicates an error
            Toast.makeText(this, "Feedback gespeichert!", Toast.LENGTH_SHORT).show();
            if (apiResponse.getVocabularyList() != null && !apiResponse.getVocabularyList().isEmpty()) {
                for (Vocabulary vocab : apiResponse.getVocabularyList()) {
                    saveVocabulary(vocab);
                }
            }
        } else {
            Toast.makeText(this, "Fehler beim Speichern des Feedbacks.", Toast.LENGTH_SHORT).show();
        }

        // --- Step 2: Display the feedback in the UI ---
        binding.editTextUserText.setVisibility(View.GONE); // Hide input box
        binding.scrollViewFeedback.setVisibility(View.VISIBLE); // Show feedback view

        binding.textViewScore.setText(String.format(java.util.Locale.GERMAN, "Bewertung: %d/100", apiResponse.getScore()));
        binding.textViewFeedbackComment.setText(apiResponse.getFeedbackComment());
        binding.textViewCorrectedText.setText(apiResponse.getCorrectedText());
        binding.textViewGrammaticalExplanation.setText(apiResponse.getGrammaticalExplanation());
    }

    private long saveScoreHistory(ApiResponse data) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DatabaseHelper.KEY_ORIGINAL_TEXT, data.getOriginalText());
        values.put(DatabaseHelper.KEY_CORRECTED_TEXT, data.getCorrectedText());
        values.put(DatabaseHelper.KEY_FEEDBACK_COMMENT, data.getFeedbackComment());
        values.put(DatabaseHelper.KEY_SCORE, data.getScore());
        values.put(DatabaseHelper.KEY_GRAMMATICAL_EXPLANATION, data.getGrammaticalExplanation());
        // The timestamp column has a DEFAULT CURRENT_TIMESTAMP, so we don't need to add it here.

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

        // Use `insertWithOnConflict` with `CONFLICT_IGNORE`. Because the `germanWord` column
        // is marked as UNIQUE, this command will simply do nothing if the word already
        // exists, preventing duplicate entries and app crashes.
        db.insertWithOnConflict(DatabaseHelper.TABLE_VOCABULARY, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        db.close();
    }
}