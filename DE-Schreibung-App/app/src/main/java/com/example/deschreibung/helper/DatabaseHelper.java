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

    /**
     * Retrieves all score history entries from the database, ordered by the most recent first.
     * @return A list of ScoreHistory objects.
     */
    public List<ScoreHistory> getAllScoreHistory() {
        List<ScoreHistory> scoreHistoryList = new ArrayList<>();
        // Query to select all entries, ordering by timestamp in descending order
        String selectQuery = "SELECT * FROM " + TABLE_SCORE_HISTORY + " ORDER BY " + KEY_TIMESTAMP + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // Loop through all rows and add them to the list
        if (cursor.moveToFirst()) {
            do {
                ScoreHistory scoreHistory = new ScoreHistory();
                // Using getColumnIndexOrThrow to be safe against column name errors
                scoreHistory.setId(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ID)));
                scoreHistory.setOriginalText(cursor.getString(cursor.getColumnIndexOrThrow(KEY_ORIGINAL_TEXT)));
                scoreHistory.setCorrectedText(cursor.getString(cursor.getColumnIndexOrThrow(KEY_CORRECTED_TEXT)));
                scoreHistory.setFeedbackComment(cursor.getString(cursor.getColumnIndexOrThrow(KEY_FEEDBACK_COMMENT)));
                scoreHistory.setScore(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_SCORE)));
                scoreHistory.setGrammaticalExplanation(cursor.getString(cursor.getColumnIndexOrThrow(KEY_GRAMMATICAL_EXPLANATION)));
                scoreHistory.setTimestamp(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TIMESTAMP)));

                scoreHistoryList.add(scoreHistory);
            } while (cursor.moveToNext());
        }

        // Clean up resources
        cursor.close();
        db.close();

        return scoreHistoryList;
    }
    /**
     * Retrieves all vocabulary entries from the database, ordered alphabetically by the German word.
     * @return A list of Vocabulary objects.
     */
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

    /**
     * Deletes multiple vocabulary items from the database using a list of their IDs.
     * @param ids A list of the primary key IDs of the words to be deleted.
     */
    public void deleteVocabularyItems(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        SQLiteDatabase db = this.getWritableDatabase();
        // Create a string of '?' placeholders, e.g., "?,?,?"
        String placeholders = TextUtils.join(",", Collections.nCopies(ids.size(), "?"));
        // Convert the list of Longs to an array of Strings for the query arguments
        String[] selectionArgs = new String[ids.size()];
        for (int i = 0; i < ids.size(); i++) {
            selectionArgs[i] = String.valueOf(ids.get(i));
        }

        db.beginTransaction();
        try {
            // Execute the delete query: "DELETE FROM Vocabulary WHERE id IN (?,?,?)"
            db.delete(TABLE_VOCABULARY, KEY_ID + " IN (" + placeholders + ")", selectionArgs);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
    }
    // ... (inside the DatabaseHelper class)

    /**
     * Retrieves a single ScoreHistory entry by its primary key ID.
     * @param id The ID of the score history entry to retrieve.
     * @return A ScoreHistory object, or null if not found.
     */
    public ScoreHistory getScoreHistoryById(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        ScoreHistory scoreHistory = null;
        // Use a parameterized query to prevent SQL injection
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
            }
        } finally {
            db.close();
        }
        return scoreHistory;
    }

    /**
     * Retrieves a single Vocabulary entry by its primary key ID.
     * @param id The ID of the vocabulary entry to retrieve.
     * @return A Vocabulary object, or null if not found.
     */
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
    /**
     * Adds a new vocabulary word. Uses CONFLICT_IGNORE to prevent crashes on duplicate entries.
     * If a germanWord already exists, the new one is not inserted.
     */
    public void addVocabulary(Vocabulary vocab) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_GERMAN_WORD, vocab.getGermanWord());
        values.put(KEY_ENGLISH_TRANSLATION, vocab.getEnglishTranslation());
        values.put(KEY_EXAMPLE_SENTENCE, vocab.getExampleSentence());

        // Using `CONFLICT_IGNORE` ensures that if a word already exists (due to the UNIQUE constraint),
        // the new insert is simply ignored instead of throwing an error.
        db.insertWithOnConflict(TABLE_VOCABULARY, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        db.close();
    }
}