package com.example.deschreibung.network;

// This class models the JSON object we send TO the server.
public class ApiRequest {
    final String text;

    public ApiRequest(String text) {
        this.text = text;
    }
}