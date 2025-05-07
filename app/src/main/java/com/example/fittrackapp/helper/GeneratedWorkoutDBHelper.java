package com.example.fittrackapp.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class GeneratedWorkoutDBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "fittrack.db";
    private static final int DATABASE_VERSION = 3;

    public static final String TABLE_WORKOUT_PLAN = "generatedWorkoutPlan";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_DAY = "day";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_EXERCISES = "exercises";

    public GeneratedWorkoutDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS " + TABLE_WORKOUT_PLAN + " (" +
                        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_DAY + " INTEGER, " +
                        COLUMN_TYPE + " TEXT, " +
                        COLUMN_EXERCISES + " TEXT" +
                        ")"
        );
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WORKOUT_PLAN);
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
}
