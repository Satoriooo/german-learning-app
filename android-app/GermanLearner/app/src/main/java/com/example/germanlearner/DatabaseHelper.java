// DatabaseHelper.java
// Updated to save all the feedback details.
package com.example.germanlearner;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "GermanLearner.db";
    // IMPORTANT: We've increased the database version to trigger an update.
    private static final int DATABASE_VERSION = 2;
    public static final String TABLE_SCORES = "scores";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_SCORE = "score";
    // --- NEW COLUMNS ---
    public static final String COLUMN_TOPIC = "topic";
    public static final String COLUMN_ORIGINAL_TEXT = "original_text";
    public static final String COLUMN_CORRECTED_TEXT = "corrected_text";
    public static final String COLUMN_EVALUATION = "evaluation";
    public static final String COLUMN_EXPLANATION = "explanation";

    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_SCORES + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_DATE + " TEXT, " +
                    COLUMN_TIME + " TEXT, " +
                    COLUMN_SCORE + " INTEGER, " +
                    COLUMN_TOPIC + " TEXT, " +
                    COLUMN_ORIGINAL_TEXT + " TEXT, " +
                    COLUMN_CORRECTED_TEXT + " TEXT, " +
                    COLUMN_EVALUATION + " TEXT, " +
                    COLUMN_EXPLANATION + " TEXT" +
                    ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This simple migration deletes the old table and creates a new one.
        // NOTE: This will erase all existing user history upon app update.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCORES);
        onCreate(db);
    }

    // New method to add a complete record
    public void addRecord(String date, String time, int score, String topic, String original, String corrected, String eval, String explanation) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_TIME, time);
        values.put(COLUMN_SCORE, score);
        values.put(COLUMN_TOPIC, topic);
        values.put(COLUMN_ORIGINAL_TEXT, original);
        values.put(COLUMN_CORRECTED_TEXT, corrected);
        values.put(COLUMN_EVALUATION, eval);
        values.put(COLUMN_EXPLANATION, explanation);
        db.insert(TABLE_SCORES, null, values);
        db.close();
    }

    // This method to get all scores remains the same.
    public Cursor getAllScores() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_SCORES, null, null, null, null, null, COLUMN_ID + " DESC");
    }

    // New method to get a single record by its ID
    public Cursor getRecordById(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_SCORES, null, COLUMN_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
    }
}