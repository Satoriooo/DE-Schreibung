package com.example.deschreibung.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MoreExamplesResponse {
    @SerializedName("examples")
    private List<String> examples;

    public List<String> getExamples() {
        return examples;
    }
}