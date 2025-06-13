package com.example.deschreibung.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;
import android.database.Cursor;
import com.example.deschreibung.models.ScoreHistory;
import java.util.ArrayList;
import java.util.List;
import android.text.TextUtils;
import com.example.deschreibung.models.Vocabulary;
import java.util.Collections;
import android.content.ContentValues;


public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "DeSchreibung.db";
    // IMPORTANT: Increment the database version to trigger onUpgrade()
    private static final int DATABASE_VERSION = 2;

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

    // NEW: Column names for detailed scores
    public static final String KEY_SCORE_GRAMMAR = "scoreGrammar";
    public static final String KEY_SCORE_VOCABULARY = "scoreVocabulary";
    public static final String KEY_SCORE_COHESION = "scoreCohesion";
    public static final String KEY_SCORE_EXPRESSIVENESS = "scoreExpressiveness";


    // VOCABULARY Table - Column Names
    public static final String KEY_GERMAN_WORD = "germanWord";
    public static final String KEY_ENGLISH_TRANSLATION = "englishTranslation";
    public static final String KEY_EXAMPLE_SENTENCE = "exampleSentence";

    // --- Table Creation SQL Statements ---

    // UPDATED: Added columns for detailed scores
    private static final String CREATE_TABLE_SCORE_HISTORY = "CREATE TABLE "
            + TABLE_SCORE_HISTORY + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_ORIGINAL_TEXT + " TEXT,"
            + KEY_CORRECTED_TEXT + " TEXT,"
            + KEY_FEEDBACK_COMMENT + " TEXT,"
            + KEY_SCORE + " INTEGER,"
            + KEY_GRAMMATICAL_EXPLANATION + " TEXT,"
            + KEY_TIMESTAMP + " TEXT DEFAULT CURRENT_TIMESTAMP,"
            + KEY_SCORE_GRAMMAR + " INTEGER,"
            + KEY_SCORE_VOCABULARY + " INTEGER,"
            + KEY_SCORE_COHESION + " INTEGER,"
            + KEY_SCORE_EXPRESSIVENESS + " INTEGER" + ")";

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
        db.execSQL(CREATE_TABLE_SCORE_HISTORY);
        db.execSQL(CREATE_TABLE_VOCABULARY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCORE_HISTORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VOCABULARY);
        onCreate(db);
    }

    // UPDATED: Retrieve detailed scores
    public List<ScoreHistory> getAllScoreHistory() {
        List<ScoreHistory> scoreHistoryList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_SCORE_HISTORY + " ORDER BY " + KEY_TIMESTAMP + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                ScoreHistory scoreHistory = new ScoreHistory();
                scoreHistory.setId(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ID)));
                scoreHistory.setOriginalText(cursor.getString(cursor.getColumnIndexOrThrow(KEY_ORIGINAL_TEXT)));
                scoreHistory.setCorrectedText(cursor.getString(cursor.getColumnIndexOrThrow(KEY_CORRECTED_TEXT)));
                scoreHistory.setFeedbackComment(cursor.getString(cursor.getColumnIndexOrThrow(KEY_FEEDBACK_COMMENT)));
                scoreHistory.setScore(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_SCORE)));
                scoreHistory.setGrammaticalExplanation(cursor.getString(cursor.getColumnIndexOrThrow(KEY_GRAMMATICAL_EXPLANATION)));
                scoreHistory.setTimestamp(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TIMESTAMP)));
                // NEW
                scoreHistory.setScoreGrammar(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_SCORE_GRAMMAR)));
                scoreHistory.setScoreVocabulary(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_SCORE_VOCABULARY)));
                scoreHistory.setScoreCohesion(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_SCORE_COHESION)));
                scoreHistory.setScoreExpressiveness(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_SCORE_EXPRESSIVENESS)));

                scoreHistoryList.add(scoreHistory);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return scoreHistoryList;
    }

    // ... (getAllVocabulary and deleteVocabularyItems are unchanged) ...
    public List<Vocabulary> getAllVocabulary() {
        List<Vocabulary> vocabularyList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_VOCABULARY + " ORDER BY " + KEY_GERMAN_WORD + " ASC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Vocabulary vocab = new Vocabulary();
                vocab.setId(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ID)));
                vocab.setGermanWord(cursor.getString(cursor.getColumnIndexOrThrow(KEY_GERMAN_WORD)));
                vocab.setEnglishTranslation(cursor.getString(cursor.getColumnIndexOrThrow(KEY_ENGLISH_TRANSLATION)));
                vocab.setExampleSentence(cursor.getString(cursor.getColumnIndexOrThrow(KEY_EXAMPLE_SENTENCE)));
                vocabularyList.add(vocab);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return vocabularyList;
    }

    public void deleteVocabularyItems(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        SQLiteDatabase db = this.getWritableDatabase();
        String placeholders = TextUtils.join(",", Collections.nCopies(ids.size(), "?"));
        String[] selectionArgs = new String[ids.size()];
        for (int i = 0; i < ids.size(); i++) {
            selectionArgs[i] = String.valueOf(ids.get(i));
        }

        db.beginTransaction();
        try {
            db.delete(TABLE_VOCABULARY, KEY_ID + " IN (" + placeholders + ")", selectionArgs);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
    }


    // UPDATED: Retrieve detailed scores
    public ScoreHistory getScoreHistoryById(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        ScoreHistory scoreHistory = null;
        try (Cursor cursor = db.query(TABLE_SCORE_HISTORY, null, KEY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                scoreHistory = new ScoreHistory();
                scoreHistory.setId(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ID)));
                scoreHistory.setOriginalText(cursor.getString(cursor.getColumnIndexOrThrow(KEY_ORIGINAL_TEXT)));
                scoreHistory.setCorrectedText(cursor.getString(cursor.getColumnIndexOrThrow(KEY_CORRECTED_TEXT)));
                scoreHistory.setFeedbackComment(cursor.getString(cursor.getColumnIndexOrThrow(KEY_FEEDBACK_COMMENT)));
                scoreHistory.setScore(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_SCORE)));
                scoreHistory.setGrammaticalExplanation(cursor.getString(cursor.getColumnIndexOrThrow(KEY_GRAMMATICAL_EXPLANATION)));
                scoreHistory.setTimestamp(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TIMESTAMP)));
                // NEW
                scoreHistory.setScoreGrammar(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_SCORE_GRAMMAR)));
                scoreHistory.setScoreVocabulary(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_SCORE_VOCABULARY)));
                scoreHistory.setScoreCohesion(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_SCORE_COHESION)));
                scoreHistory.setScoreExpressiveness(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_SCORE_EXPRESSIVENESS)));
            }
        } finally {
            db.close();
        }
        return scoreHistory;
    }

    // ... (getVocabularyById and addVocabulary are unchanged) ...
    public Vocabulary getVocabularyById(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Vocabulary vocab = null;
        try (Cursor cursor = db.query(TABLE_VOCABULARY, null, KEY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                vocab = new Vocabulary();
                vocab.setId(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ID)));
                vocab.setGermanWord(cursor.getString(cursor.getColumnIndexOrThrow(KEY_GERMAN_WORD)));
                vocab.setEnglishTranslation(cursor.getString(cursor.getColumnIndexOrThrow(KEY_ENGLISH_TRANSLATION)));
                vocab.setExampleSentence(cursor.getString(cursor.getColumnIndexOrThrow(KEY_EXAMPLE_SENTENCE)));
            }
        } finally {
            db.close();
        }
        return vocab;
    }

    public void addVocabulary(Vocabulary vocab) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_GERMAN_WORD, vocab.getGermanWord());
        values.put(KEY_ENGLISH_TRANSLATION, vocab.getEnglishTranslation());
        values.put(KEY_EXAMPLE_SENTENCE, vocab.getExampleSentence());
        db.insertWithOnConflict(TABLE_VOCABULARY, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        db.close();
    }
}