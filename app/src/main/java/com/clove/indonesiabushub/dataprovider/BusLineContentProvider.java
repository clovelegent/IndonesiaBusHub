package com.clove.indonesiabushub.dataprovider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class BusLineContentProvider extends ContentProvider {
    private static final String TAG = "BLContentProvider";

    public static final String AUTHORITY = "com.clove.indonesiabushub.buslinecontentprovider";
    public static final Uri LINES_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/lines");
    private static final int LINES_CODE = 1;
    private static final String UNKNOW_URI = "Unknown URI ";

    private static final String LINES_CONTENT_TYPE = "vnd.android.cursor.dir/com.clove.indonesiabushub.buslinecontentprovider.lines";

    public static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(AUTHORITY, "lines", LINES_CODE);
    }

    public DatabaseHelper dbHelper = null;

    public BusLineContentProvider() {
    }

    @Override
    public boolean onCreate() {
        dbHelper = new DatabaseHelper(getContext(), DatabaseHelper.DATABASE_NAME, DatabaseHelper.DATABSE_VERSION);
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Log.d(TAG, "query uri:" + uri);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        switch (uriMatcher.match(uri)) {
            case LINES_CODE:
                return db.query(DatabaseHelper.TABLE_LINES, projection, selection, selectionArgs, null, null, sortOrder);
            default:
                throw new IllegalArgumentException(UNKNOW_URI + uri);
        }
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case LINES_CODE:
                return LINES_CONTENT_TYPE;
            default:
                throw new IllegalArgumentException(UNKNOW_URI + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long id = 0;
        switch (uriMatcher.match(uri)) {
            case LINES_CODE:
                id = db.insert(DatabaseHelper.TABLE_LINES, null, values);
                return ContentUris.withAppendedId(uri, id);
            default:
                throw new IllegalArgumentException(UNKNOW_URI + uri);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count = 0;
        switch (uriMatcher.match(uri)) {
            case LINES_CODE:
                count = db.delete(DatabaseHelper.TABLE_LINES, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException(UNKNOW_URI + uri);
        }
        db.close();
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count = 0;
        switch (uriMatcher.match(uri)) {
            case LINES_CODE:
                count = db.update(DatabaseHelper.TABLE_LINES, values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException(UNKNOW_URI + uri);
        }
        db.close();
        return count;
    }
}
