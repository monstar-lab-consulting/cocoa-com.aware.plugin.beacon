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
    public static final String DATABASE_NAME = "beacon.db";
    public static final String TABLE_NAME = "beacon";
    public static final String[] DATABASE_TABLES = {
            TABLE_NAME
    };
    //data type: 0-inferences, 1-features
    public static final String[] TABLES_FIELDS = {
            BeaconData._ID + " integer primary key autoincrement," +
                    BeaconData.TIMESTAMP + " real default 0," +
                    BeaconData.DEVICE_ID + " text default ''," +
                    BeaconData.UUID + " text default ''," +
                    BeaconData.MAJOR + " integer default ''," +
                    BeaconData.MINOR + " integer default ''," +
                    BeaconData.RSSI + " integer default ''," +
                    BeaconData.TX_POWER + " integer default ''"
    };
    private static final int URI_CHECK_SCAN = 1;
    private static final int URI_CHECK_SCAN_ID = 2;
    /**
     * Provider authority: com.aware.plugin.beacon.provider.beacon
     */
    public static String AUTHORITY = "com.aware.plugin.beacon.provider.beacon";
    private static UriMatcher uRIMatcher;
    private static HashMap<String, String> databaseMap;
    private static SQLiteDatabase database;
    private DatabaseHelper dbHelper;

    /**
     * Returns the provider authority that is dynamic
     *
     * @return
     */
    public static String getAuthority(Context context) {
        AUTHORITY = context.getPackageName() + ".provider.beacon";
        return AUTHORITY;
    }

    private void initialiseDatabase() {
        if (dbHelper == null)
            dbHelper = new DatabaseHelper(getContext(), DATABASE_NAME, null, DATABASE_VERSION, DATABASE_TABLES, TABLES_FIELDS);
        if (database == null)
            database = dbHelper.getWritableDatabase();
    }

    @Override
    public boolean onCreate() {

        AUTHORITY = getContext().getPackageName() + ".provider.beacon";

        uRIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uRIMatcher.addURI(AUTHORITY, DATABASE_TABLES[0], URI_CHECK_SCAN);
        uRIMatcher.addURI(AUTHORITY, DATABASE_TABLES[0] + "/#", URI_CHECK_SCAN_ID);

        databaseMap = new HashMap<>();
        databaseMap.put(BeaconData._ID, BeaconData._ID);
        databaseMap.put(BeaconData.TIMESTAMP, BeaconData.TIMESTAMP);
        databaseMap.put(BeaconData.DEVICE_ID, BeaconData.DEVICE_ID);
        databaseMap.put(BeaconData.UUID, BeaconData.UUID);
        databaseMap.put(BeaconData.MAJOR, BeaconData.MAJOR);
        databaseMap.put(BeaconData.MINOR, BeaconData.MINOR);
        databaseMap.put(BeaconData.RSSI, BeaconData.RSSI);
        databaseMap.put(BeaconData.TX_POWER, BeaconData.TX_POWER);
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
                return BeaconData.CONTENT_TYPE;
            case URI_CHECK_SCAN_ID:
                return BeaconData.CONTENT_ITEM_TYPE;
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
                long beacon_id = database.insertWithOnConflict(DATABASE_TABLES[0], BeaconData.DEVICE_ID, values, SQLiteDatabase.CONFLICT_IGNORE);
                if (beacon_id > 0) {
                    Uri new_uri = ContentUris.withAppendedId(
                            BeaconData.CONTENT_URI,
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

    public static final class BeaconData implements BaseColumns {
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

        private BeaconData() {
        }
    }
}
