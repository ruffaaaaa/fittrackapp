package com.example.fittrackapp.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;

public class FittrackDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "fittrack.db";
    private static final int DATABASE_VERSION = 5;

    public static final String TABLE_WORKOUT_PLAN = "generatedWorkoutPlan";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_DAY = "day";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_EXERCISES = "exercises";

    public static final String TABLE_FORM_RESPONSES = "formResponses";
    public static final String COLUMN_FORM_ID = "id";
    public static final String COLUMN_QUESTION_KEY = "question_key";
    public static final String COLUMN_RESPONSE = "response";

    public FittrackDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createWorkoutPlanTable = "CREATE TABLE IF NOT EXISTS " + TABLE_WORKOUT_PLAN + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_DAY + " INTEGER, " +
                COLUMN_TYPE + " TEXT, " +
                COLUMN_EXERCISES + " TEXT" +
                ")";
        db.execSQL(createWorkoutPlanTable);

        String createFormResponsesTable = "CREATE TABLE IF NOT EXISTS " + TABLE_FORM_RESPONSES + " (" +
                COLUMN_FORM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_QUESTION_KEY + " TEXT, " +
                COLUMN_RESPONSE + " TEXT" +
                ")";
        db.execSQL(createFormResponsesTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop both tables and recreate them
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WORKOUT_PLAN);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FORM_RESPONSES);
        onCreate(db);
    }

    public void insertWorkoutPlan(int day, String type, String exercises) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DAY, day);
        values.put(COLUMN_TYPE, type);
        values.put(COLUMN_EXERCISES, exercises);
        db.insert(TABLE_WORKOUT_PLAN, null, values);
        db.close();
    }

    public void clearWorkoutPlans() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_WORKOUT_PLAN, null, null);
        db.close();
    }

    public void insertResponse(String key, String response) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_QUESTION_KEY, key);
        values.put(COLUMN_RESPONSE, response);
        db.insert(TABLE_FORM_RESPONSES, null, values);
        db.close();
    }

}
