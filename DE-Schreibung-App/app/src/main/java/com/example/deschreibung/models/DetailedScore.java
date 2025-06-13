package com.example.deschreibung.network;

import com.google.gson.annotations.SerializedName;

/**
 * Represents the detailed breakdown of the score from the API response.
 * This class is used by GSON to parse the nested "detailedScore" JSON object.
 */
public class DetailedScore {

    @SerializedName("grammar")
    private int grammar;

    @SerializedName("vocabulary")
    private int vocabulary;

    @SerializedName("cohesion")
    private int cohesion;

    @SerializedName("expressiveness")
    private int expressiveness;

    // --- Getters ---
    public int getGrammar() {
        return grammar;
    }

    public int getVocabulary() {
        return vocabulary;
    }

    public int getCohesion() {
        return cohesion;
    }

    public int getExpressiveness() {
        return expressiveness;
    }
}