package com.example.fittrackapp.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.util.Log;

public class FitnessFormDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "fittrack.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "formResponses";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_QUESTION_KEY = "question_key";
    public static final String COLUMN_RESPONSE = "response";

    public FitnessFormDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_QUESTION_KEY + " TEXT, " +
                COLUMN_RESPONSE + " TEXT)";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void insertResponse(String key, String response) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_QUESTION_KEY, key);
            values.put(COLUMN_RESPONSE, response);
            db.insert(TABLE_NAME, null, values);
        } catch (Exception e) {
            Log.e("DB_INSERT", "Error inserting response: " + e.getMessage());
        }
    }

}
