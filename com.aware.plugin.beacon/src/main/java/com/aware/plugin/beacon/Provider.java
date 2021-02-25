package com.aware.plugin.beacon;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import com.aware.Aware;
import com.aware.utils.DatabaseHelper;

import java.util.HashMap;

/**
 * Created by at-trinhnguyen2
 */
public class Provider extends ContentProvider {

    public static final int DATABASE_VERSION = 7;

    /**
     * Provider authority: com.aware.plugin.beacon.provider.beacon
     */
    public static String AUTHORITY = "com.aware.plugin.beacon.provider.beacon";

    private static final int URI_CHECK_SCAN = 1;
    private static final int URI_CHECK_SCAN_ID = 2;

    public static final String DATABASE_NAME = "beacon.db";
    public static final String TABLE_NAME = "beacon";

    public static final String[] DATABASE_TABLES = {
            TABLE_NAME
    };

    public static final class Beacon_Data implements BaseColumns {
        private Beacon_Data() {
        }

        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.aware.plugin.beacon";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.ANY_CURSOR_ITEM_TYPE + "/vnd.aware.plugin.beacon";

        public static final String _ID = "_id";
        public static final String TIMESTAMP = "timestamp";
        public static final String DEVICE_ID = "device_id";
        public static final String UUID = "uuid";
        public static final String MAJOR = "major";
        public static final String MINOR = "minor";
        public static final String RSSI = "rssi";
        public static final String TX_POWER = "tx_power";
    }

    //data type: 0-inferences, 1-features
    public static final String[] TABLES_FIELDS = {
            Beacon_Data._ID + " integer primary key autoincrement," +
                    Beacon_Data.TIMESTAMP + " real default 0," +
                    Beacon_Data.DEVICE_ID + " text default ''," +
                    Beacon_Data.UUID + " text default ''," +
                    Beacon_Data.MAJOR + " integer default ''," +
                    Beacon_Data.MINOR + " integer default ''," +
                    Beacon_Data.RSSI + " integer default ''," +
                    Beacon_Data.TX_POWER + " integer default ''"
    };

    private static UriMatcher uRIMatcher;
    private static HashMap<String, String> databaseMap;
    private DatabaseHelper dbHelper;
    private static SQLiteDatabase database;

    private void initialiseDatabase() {
        if (dbHelper == null)
            dbHelper = new DatabaseHelper(getContext(), DATABASE_NAME, null, DATABASE_VERSION, DATABASE_TABLES, TABLES_FIELDS);
        if (database == null)
            database = dbHelper.getWritableDatabase();
    }

    /**
     * Returns the provider authority that is dynamic
     *
     * @return
     */
    public static String getAuthority(Context context) {
        AUTHORITY = context.getPackageName() + ".provider.beacon";
        return AUTHORITY;
    }

    @Override
    public boolean onCreate() {

        AUTHORITY = getContext().getPackageName() + ".provider.beacon";

        uRIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uRIMatcher.addURI(AUTHORITY, DATABASE_TABLES[0], URI_CHECK_SCAN);
        uRIMatcher.addURI(AUTHORITY, DATABASE_TABLES[0] + "/#", URI_CHECK_SCAN_ID);

        databaseMap = new HashMap<>();
        databaseMap.put(Beacon_Data._ID, Beacon_Data._ID);
        databaseMap.put(Beacon_Data.TIMESTAMP, Beacon_Data.TIMESTAMP);
        databaseMap.put(Beacon_Data.DEVICE_ID, Beacon_Data.DEVICE_ID);
        databaseMap.put(Beacon_Data.UUID, Beacon_Data.UUID);
        databaseMap.put(Beacon_Data.MAJOR, Beacon_Data.MAJOR);
        databaseMap.put(Beacon_Data.MINOR, Beacon_Data.MINOR);
        databaseMap.put(Beacon_Data.RSSI, Beacon_Data.RSSI);
        databaseMap.put(Beacon_Data.TX_POWER, Beacon_Data.TX_POWER);
        return true;
    }

    @Override
    public synchronized int delete(Uri uri, String selection, String[] selectionArgs) {
        initialiseDatabase();

        database.beginTransaction();

        int count;
        switch (uRIMatcher.match(uri)) {
            case URI_CHECK_SCAN:
                count = database.delete(DATABASE_TABLES[0], selection, selectionArgs);
                break;
            default:
                database.endTransaction();
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        database.setTransactionSuccessful();
        database.endTransaction();
        getContext().getContentResolver().notifyChange(uri, null, false);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        switch (uRIMatcher.match(uri)) {
            case URI_CHECK_SCAN:
                return Beacon_Data.CONTENT_TYPE;
            case URI_CHECK_SCAN_ID:
                return Beacon_Data.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public synchronized Uri insert(Uri uri, ContentValues initialValues) {
        initialiseDatabase();

        ContentValues values = (initialValues != null) ? new ContentValues(
                initialValues) : new ContentValues();

        database.beginTransaction();

        switch (uRIMatcher.match(uri)) {
            case URI_CHECK_SCAN:
                long beacon_id = database.insertWithOnConflict(DATABASE_TABLES[0], Beacon_Data.DEVICE_ID, values, SQLiteDatabase.CONFLICT_IGNORE);
                if (beacon_id > 0) {
                    Uri new_uri = ContentUris.withAppendedId(
                            Beacon_Data.CONTENT_URI,
                            beacon_id);
                    getContext().getContentResolver().notifyChange(new_uri, null, false);
                    database.setTransactionSuccessful();
                    database.endTransaction();
                    return new_uri;
                }
                database.endTransaction();
                throw new SQLException("Failed to insert row into " + uri);
            default:
                database.endTransaction();
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        initialiseDatabase();

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        switch (uRIMatcher.match(uri)) {
            case URI_CHECK_SCAN:
                qb.setTables(DATABASE_TABLES[0]);
                qb.setProjectionMap(databaseMap);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        try {
            Cursor c = qb.query(database, projection, selection, selectionArgs,
                    null, null, sortOrder);
            c.setNotificationUri(getContext().getContentResolver(), uri);
            return c;
        } catch (IllegalStateException e) {
            if (Aware.DEBUG)
                Log.e(Aware.TAG, e.getMessage());

            return null;
        }
    }

    @Override
    public synchronized int update(Uri uri, ContentValues values, String selection,
                                   String[] selectionArgs) {

        initialiseDatabase();

        database.beginTransaction();

        int count;
        switch (uRIMatcher.match(uri)) {
            case URI_CHECK_SCAN:
                count = database.update(DATABASE_TABLES[0], values, selection,
                        selectionArgs);
                break;
            default:
                database.endTransaction();
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        database.setTransactionSuccessful();
        database.endTransaction();
        getContext().getContentResolver().notifyChange(uri, null, false);
        return count;
    }
}
