package com.clove.indonesiabushub.dataprovider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";
    public static final String DATABASE_NAME = "bus_lines.db";
    public static final int DATABSE_VERSION = 1;
    public static final String TABLE_LINES = "lines";

    static String dbName() {
        return DATABASE_NAME;
    }

    private void createModeLines(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS lines (" + "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "line TEXT,"
                + "name TEXT UNIQUE ON CONFLICT REPLACE,"
                + "latitude DOUBLE,"
                + "longitude DOUBLE"+ ");");
    }

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public DatabaseHelper(Context context, String name, int version) {
        this(context, name, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createModeLines(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
