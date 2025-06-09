package com.example.deschreibung;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.deschreibung.databinding.ActivityVocabularyDetailBinding;
import com.example.deschreibung.helper.DatabaseHelper;
import com.example.deschreibung.models.Vocabulary;

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

        long vocabId = getIntent().getLongExtra(EXTRA_VOCAB_ID, -1);
        if (vocabId == -1) {
            Toast.makeText(this, "Fehler: Ungültige Vokabel-ID.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        loadVocabularyDetails(vocabId);
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
}