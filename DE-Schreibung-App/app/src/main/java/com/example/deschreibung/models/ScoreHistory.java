package com.example.deschreibung.models;

public class ScoreHistory {
    private long id;
    private String originalText;
    private String correctedText;
    private String feedbackComment;
    private int score;
    private String grammaticalExplanation;
    private String timestamp;

    // NEW: Fields for the detailed score breakdown
    private int scoreGrammar;
    private int scoreVocabulary;
    private int scoreCohesion;
    private int scoreExpressiveness;


    // --- Constructors ---
    public ScoreHistory() {
    }

    // --- Getters and Setters ---
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getOriginalText() { return originalText; }
    public void setOriginalText(String originalText) { this.originalText = originalText; }

    public String getCorrectedText() { return correctedText; }
    public void setCorrectedText(String correctedText) { this.correctedText = correctedText; }

    public String getFeedbackComment() { return feedbackComment; }
    public void setFeedbackComment(String feedbackComment) { this.feedbackComment = feedbackComment; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public String getGrammaticalExplanation() { return grammaticalExplanation; }
    public void setGrammaticalExplanation(String grammaticalExplanation) { this.grammaticalExplanation = grammaticalExplanation; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    // NEW: Getters and Setters for detailed scores
    public int getScoreGrammar() { return scoreGrammar; }
    public void setScoreGrammar(int scoreGrammar) { this.scoreGrammar = scoreGrammar; }

    public int getScoreVocabulary() { return scoreVocabulary; }
    public void setScoreVocabulary(int scoreVocabulary) { this.scoreVocabulary = scoreVocabulary; }

    public int getScoreCohesion() { return scoreCohesion; }
    public void setScoreCohesion(int scoreCohesion) { this.scoreCohesion = scoreCohesion; }

    public int getScoreExpressiveness() { return scoreExpressiveness; }
    public void setScoreExpressiveness(int scoreExpressiveness) { this.scoreExpressiveness = scoreExpressiveness; }
}