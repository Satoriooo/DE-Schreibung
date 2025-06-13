package com.example.deschreibung.network;

import com.example.deschreibung.models.ExampleSentence; // <-- IMPORTANT IMPORT
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MoreExamplesResponse {
    @SerializedName("examples")
    private List<ExampleSentence> examples; // <-- CHANGED from List<String>

    public List<ExampleSentence> getExamples() { // <-- CHANGED from List<String>
        return examples;
    }
}