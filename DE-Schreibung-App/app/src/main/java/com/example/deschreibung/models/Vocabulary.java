package com.example.deschreibung.models;

import com.google.gson.annotations.SerializedName;

public class Vocabulary {

    // Note: The 'id' field is for the local SQLite database and is not part of the API JSON.
    private long id;

    @SerializedName("germanWord")
    private String germanWord;

    @SerializedName("englishTranslation")
    private String englishTranslation;

    @SerializedName("exampleSentence")
    private String exampleSentence;

    // --- Constructors ---
    public Vocabulary() {
    }

    public Vocabulary(long id, String germanWord, String englishTranslation, String exampleSentence) {
        this.id = id;
        this.germanWord = germanWord;
        this.englishTranslation = englishTranslation;
        this.exampleSentence = exampleSentence;
    }

    // --- Getters and Setters ---
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getGermanWord() { return germanWord; }
    public void setGermanWord(String germanWord) { this.germanWord = germanWord; }

    public String getEnglishTranslation() { return englishTranslation; }
    public void setEnglishTranslation(String englishTranslation) { this.englishTranslation = englishTranslation; }

    public String getExampleSentence() { return exampleSentence; }
    public void setExampleSentence(String exampleSentence) { this.exampleSentence = exampleSentence; }
}