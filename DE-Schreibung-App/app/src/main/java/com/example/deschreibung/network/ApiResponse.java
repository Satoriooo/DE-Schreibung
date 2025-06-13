package com.example.deschreibung.network;

import com.example.deschreibung.models.Vocabulary;
import com.example.deschreibung.network.DetailedScore; // Import the new class
import com.google.gson.annotations.SerializedName;
import java.util.List;

// This class models the root JSON object we receive FROM the server.
public class ApiResponse {

    @SerializedName("originalText")
    private String originalText;

    @SerializedName("correctedText")
    private String correctedText;

    @SerializedName("feedbackComment")
    private String feedbackComment;

    @SerializedName("score")
    private int score;

    // NEW: Add field for the detailed score breakdown
    @SerializedName("detailedScore")
    private DetailedScore detailedScore;

    @SerializedName("grammaticalExplanation")
    private String grammaticalExplanation;

    @SerializedName("vocabularyList")
    private List<Vocabulary> vocabularyList;

    // --- Getters ---
    public String getOriginalText() { return originalText; }
    public String getCorrectedText() { return correctedText; }
    public String getFeedbackComment() { return feedbackComment; }
    public int getScore() { return score; }
    public DetailedScore getDetailedScore() { return detailedScore; } // NEW: Add getter
    public String getGrammaticalExplanation() { return grammaticalExplanation; }
    public List<Vocabulary> getVocabularyList() { return vocabularyList; }
}