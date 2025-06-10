package com.example.deschreibung;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.deschreibung.databinding.ActivityEditVocabularyBinding;
import com.example.deschreibung.helper.DatabaseHelper;
import com.example.deschreibung.models.Vocabulary;

public class EditVocabularyActivity extends AppCompatActivity {

    private ActivityEditVocabularyBinding binding;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditVocabularyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = new DatabaseHelper(this);

        binding.buttonSave.setOnClickListener(v -> saveVocabulary());
        binding.buttonCancel.setOnClickListener(v -> finish());
    }

    private void saveVocabulary() {
        String germanWord = binding.editTextGermanWord.getText().toString().trim();
        String englishTranslation = binding.editTextEnglishTranslation.getText().toString().trim();
        String exampleSentence = binding.editTextExampleSentence.getText().toString().trim();

        if (TextUtils.isEmpty(germanWord)) {
            Toast.makeText(this, "Deutsches Wort darf nicht leer sein.", Toast.LENGTH_SHORT).show();
            return;
        }

        Vocabulary newVocab = new Vocabulary();
        newVocab.setGermanWord(germanWord);
        newVocab.setEnglishTranslation(englishTranslation);
        newVocab.setExampleSentence(exampleSentence);

        dbHelper.addVocabulary(newVocab);

        Toast.makeText(this, "Vokabel gespeichert!", Toast.LENGTH_SHORT).show();
        finish(); // Close this screen and return to the list
    }
}