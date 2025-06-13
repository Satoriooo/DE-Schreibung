package com.example.deschreibung.models;

import com.google.gson.annotations.SerializedName;

public class ExampleSentence {
    @SerializedName("german")
    private String german;

    @SerializedName("english")
    private String english;

    public String getGerman() {
        return german;
    }

    public String getEnglish() {
        return english;
    }
}