package com.example.deschreibung.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "DeSchreibung.db";
    private static final int DATABASE_VERSION = 1;

    // Table Names
    public static final String TABLE_SCORE_HISTORY = "ScoreHistory";
    public static final String TABLE_VOCABULARY = "Vocabulary";

    // Common column name
    public static final String KEY_ID = "id";

    // SCORE_HISTORY Table - Column Names
    public static final String KEY_ORIGINAL_TEXT = "originalText";
    public static final String KEY_CORRECTED_TEXT = "correctedText";
    public static final String KEY_FEEDBACK_COMMENT = "feedbackComment";
    public static final String KEY_SCORE = "score";
    public static final String KEY_GRAMMATICAL_EXPLANATION = "grammaticalExplanation";
    public static final String KEY_TIMESTAMP = "timestamp";

    // VOCABULARY Table - Column Names
    public static final String KEY_GERMAN_WORD = "germanWord";
    public static final String KEY_ENGLISH_TRANSLATION = "englishTranslation";
    public static final String KEY_EXAMPLE_SENTENCE = "exampleSentence";

    // --- Table Creation SQL Statements ---

    private static final String CREATE_TABLE_SCORE_HISTORY = "CREATE TABLE "
            + TABLE_SCORE_HISTORY + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_ORIGINAL_TEXT + " TEXT,"
            + KEY_CORRECTED_TEXT + " TEXT,"
            + KEY_FEEDBACK_COMMENT + " TEXT,"
            + KEY_SCORE + " INTEGER,"
            + KEY_GRAMMATICAL_EXPLANATION + " TEXT,"
            + KEY_TIMESTAMP + " TEXT DEFAULT CURRENT_TIMESTAMP" + ")";

    private static final String CREATE_TABLE_VOCABULARY = "CREATE TABLE "
            + TABLE_VOCABULARY + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_GERMAN_WORD + " TEXT UNIQUE,"
            + KEY_ENGLISH_TRANSLATION + " TEXT,"
            + KEY_EXAMPLE_SENTENCE + " TEXT" + ")";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // This method is called only once, when the database is first created.
        db.execSQL(CREATE_TABLE_SCORE_HISTORY);
        db.execSQL(CREATE_TABLE_VOCABULARY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This method is called when DATABASE_VERSION is incremented.
        // For simplicity, we drop and recreate the tables.
        // In a real-world app, you would migrate user data.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCORE_HISTORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VOCABULARY);
        onCreate(db);
    }
}