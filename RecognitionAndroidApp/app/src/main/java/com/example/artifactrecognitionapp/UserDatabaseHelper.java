package com.example.artifactrecognitionapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class UserDatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "users.db";
    public static final int DATABASE_VERSION = 1;

    // Users table
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_SURNAME = "surname";

    // Art history table
    public static final String TABLE_ART_HISTORY = "art_history";
    public static final String COLUMN_ART_HISTORY_ID = "_id";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_IMAGE_URI = "image_uri";
    public static final String COLUMN_PREDICTION = "prediction";

    private static final String SQL_CREATE_TABLE_USERS =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_USERNAME + " TEXT UNIQUE NOT NULL," +
                    COLUMN_PASSWORD + " TEXT NOT NULL," +
                    COLUMN_NAME + " TEXT NOT NULL," +
                    COLUMN_SURNAME + " TEXT NOT NULL)";

    private static final String SQL_CREATE_TABLE_ART_HISTORY =
            "CREATE TABLE " + TABLE_ART_HISTORY + " (" +
                    COLUMN_ART_HISTORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_USER_ID + " INTEGER NOT NULL," +
                    COLUMN_IMAGE_URI + " TEXT NOT NULL," +
                    COLUMN_PREDICTION + " TEXT NOT NULL," +
                    "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + "))";

    private static final String SQL_DELETE_TABLE_USERS =
            "DROP TABLE IF EXISTS " + TABLE_USERS;

    private static final String SQL_DELETE_TABLE_ART_HISTORY =
            "DROP TABLE IF EXISTS " + TABLE_ART_HISTORY;

    public UserDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE_USERS);
        db.execSQL(SQL_CREATE_TABLE_ART_HISTORY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_TABLE_USERS);
        db.execSQL(SQL_DELETE_TABLE_ART_HISTORY);
        onCreate(db);
    }

    // Add a new art history item for a user
    public void addArtHistoryItem(int userId, String imageUri, String prediction) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_IMAGE_URI, imageUri);
        values.put(COLUMN_PREDICTION, prediction);
        long newRowId = db.insert(TABLE_ART_HISTORY, null, values);

        if (newRowId == -1) {
            Log.e("UserDatabaseHelper", "Art history item insertion failed for user: " + userId);
        } else {
            Log.i("UserDatabaseHelper", "Art history item inserted successfully for user: " + userId);
        }
    }

    // burası sonradan eklendi
    public int getUserId(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_ID};
        String selection = COLUMN_USERNAME + " = ? AND " + COLUMN_PASSWORD + " = ?";
        String[] selectionArgs = {username, password};

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);

        if (cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(COLUMN_ID);
            if (idIndex != -1) {
                int userId = cursor.getInt(idIndex);
                cursor.close();
                return userId;
            }
        }

        cursor.close();
        return -1;
    }


    // Get the art history for a specific user
    public Cursor getArtHistoryByUserId(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_USER_ID + " = ?";
        String[] selectionArgs = {String.valueOf(userId)};
        return db.query(TABLE_ART_HISTORY, null, selection, selectionArgs, null, null, null);
    }

}
