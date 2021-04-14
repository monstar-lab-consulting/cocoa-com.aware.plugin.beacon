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

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "folkbears.db";
    public static final String TABLE_NAME_BEACON = "plugin_beacon";
    public static final String TABLE_NAME_DEEP_CONTACTS = "deep_contacts";
    public static final String TABLE_NAME_TEMP_USER_ID = "temp_user_ids";
    public static final String[] DATABASE_TABLES = {
            TABLE_NAME_BEACON,
            TABLE_NAME_DEEP_CONTACTS,
            TABLE_NAME_TEMP_USER_ID
    };

    //data type: 0-inferences, 1-features
    public static final String TABLES_FIELDS_BEACON =
            BeaconData._ID + " integer primary key autoincrement," +
                    BeaconData.TIMESTAMP + " real default 0," +
                    BeaconData.DEVICE_ID + " text default ''," +
                    BeaconData.UUID + " text default ''," +
                    BeaconData.MAJOR + " integer default ''," +
                    BeaconData.MINOR + " integer default ''," +
                    BeaconData.RSSI + " integer default ''," +
                    BeaconData.TX_POWER + " integer default ''";

    public static final String TABLES_FIELDS_DEEP_CONTACT =
            DeepContactData._ID + " integer primary key autoincrement," +
                    DeepContactData.TIMESTAMP + " real default 0," +
                    DeepContactData.USER_ID + " text default ''," +
                    DeepContactData.CONTACTED_USER_ID + " text default ''," +
                    DeepContactData.START_TIME + " integer default ''," +
                    DeepContactData.END_TIME + " integer default ''";

    public static final String TABLES_FIELDS_TEMP_USER_ID =
            TempUserIdData._ID + " integer primary key autoincrement," +
                    TempUserIdData.TEMP_ID + " text default ''," +
                    TempUserIdData.TIMESTAMP + " real default 0," +
                    TempUserIdData.START_TIME + " integer default ''," +
                    TempUserIdData.EXPIRY_TIME + " integer default ''," +
                    TempUserIdData.INFLECTION_FLAG + " text default ''";

    public static final String[] TABLES_FIELDS = {
            TABLES_FIELDS_BEACON,
            TABLES_FIELDS_DEEP_CONTACT,
            TABLES_FIELDS_TEMP_USER_ID
    };

    private static final int URI_BEACON = 1;
    private static final int URI_BEACON_ID = 2;
    private static final int URI_DEEP_CONTACT = 3;
    private static final int URI_DEEP_CONTACT_ID = 4;
    private static final int URI_TEMP_USER_ID = 5;
    private static final int URI_TEMP_USER_ID_ID = 6;
    /**
     * Provider authority: com.aware.plugin.beacon.provider.beacon
     */
    public static String AUTHORITY = "com.aware.plugin.beacon.provider.beacon";
    private static UriMatcher uriMatcher;
    private static HashMap<String, String> databaseMapBeacon;
    private static HashMap<String, String> databaseMapDeepContact;
    private static HashMap<String, String> databaseMapTempUserId;
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

        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, DATABASE_TABLES[0], URI_BEACON);
        uriMatcher.addURI(AUTHORITY, DATABASE_TABLES[0] + "/#", URI_BEACON_ID);
        uriMatcher.addURI(AUTHORITY, DATABASE_TABLES[1], URI_DEEP_CONTACT);
        uriMatcher.addURI(AUTHORITY, DATABASE_TABLES[1] + "/#", URI_DEEP_CONTACT_ID);
        uriMatcher.addURI(AUTHORITY, DATABASE_TABLES[2], URI_TEMP_USER_ID);
        uriMatcher.addURI(AUTHORITY, DATABASE_TABLES[2] + "/#", URI_TEMP_USER_ID_ID);

        databaseMapBeacon = new HashMap<>();
        databaseMapBeacon.put(BeaconData._ID, BeaconData._ID);
        databaseMapBeacon.put(BeaconData.TIMESTAMP, BeaconData.TIMESTAMP);
        databaseMapBeacon.put(BeaconData.DEVICE_ID, BeaconData.DEVICE_ID);
        databaseMapBeacon.put(BeaconData.UUID, BeaconData.UUID);
        databaseMapBeacon.put(BeaconData.MAJOR, BeaconData.MAJOR);
        databaseMapBeacon.put(BeaconData.MINOR, BeaconData.MINOR);
        databaseMapBeacon.put(BeaconData.RSSI, BeaconData.RSSI);
        databaseMapBeacon.put(BeaconData.TX_POWER, BeaconData.TX_POWER);

        databaseMapDeepContact = new HashMap<>();
        databaseMapDeepContact.put(DeepContactData._ID, DeepContactData._ID);
        databaseMapDeepContact.put(DeepContactData.TIMESTAMP, DeepContactData.TIMESTAMP);
        databaseMapDeepContact.put(DeepContactData.USER_ID, DeepContactData.USER_ID);
        databaseMapDeepContact.put(DeepContactData.CONTACTED_USER_ID, DeepContactData.CONTACTED_USER_ID);
        databaseMapDeepContact.put(DeepContactData.START_TIME, DeepContactData.START_TIME);
        databaseMapDeepContact.put(DeepContactData.END_TIME, DeepContactData.END_TIME);

        databaseMapTempUserId = new HashMap<>();
        databaseMapTempUserId.put(TempUserIdData._ID, TempUserIdData._ID);
        databaseMapTempUserId.put(TempUserIdData.TIMESTAMP, TempUserIdData.TIMESTAMP);
        databaseMapTempUserId.put(TempUserIdData.TEMP_ID, TempUserIdData.TEMP_ID);
        databaseMapTempUserId.put(TempUserIdData.START_TIME, TempUserIdData.START_TIME);
        databaseMapTempUserId.put(TempUserIdData.EXPIRY_TIME, TempUserIdData.EXPIRY_TIME);
        databaseMapTempUserId.put(TempUserIdData.INFLECTION_FLAG, TempUserIdData.INFLECTION_FLAG);
        return true;
    }

    @Override
    public synchronized int delete(Uri uri, String selection, String[] selectionArgs) {
        initialiseDatabase();

        database.beginTransaction();

        int count;
        switch (uriMatcher.match(uri)) {
            case URI_BEACON:
                count = database.delete(DATABASE_TABLES[0], selection, selectionArgs);
                break;
            case URI_DEEP_CONTACT:
                count = database.delete(DATABASE_TABLES[1], selection, selectionArgs);
                break;
            case URI_TEMP_USER_ID:
                count = database.delete(DATABASE_TABLES[2], selection, selectionArgs);
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
        switch (uriMatcher.match(uri)) {
            case URI_BEACON:
                return BeaconData.CONTENT_TYPE;
            case URI_BEACON_ID:
                return BeaconData.CONTENT_ITEM_TYPE;
            case URI_DEEP_CONTACT:
                return DeepContactData.CONTENT_TYPE;
            case URI_DEEP_CONTACT_ID:
                return DeepContactData.CONTENT_ITEM_TYPE;
            case URI_TEMP_USER_ID:
                return TempUserIdData.CONTENT_TYPE;
            case URI_TEMP_USER_ID_ID:
                return TempUserIdData.CONTENT_ITEM_TYPE;
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

        switch (uriMatcher.match(uri)) {
            case URI_BEACON:
                long beaconId = database.insertWithOnConflict(DATABASE_TABLES[0], BeaconData.DEVICE_ID, values, SQLiteDatabase.CONFLICT_IGNORE);
                if (beaconId > 0) {
                    Uri newUri = ContentUris.withAppendedId(
                            BeaconData.CONTENT_URI,
                            beaconId);
                    getContext().getContentResolver().notifyChange(newUri, null, false);
                    database.setTransactionSuccessful();
                    database.endTransaction();
                    return newUri;
                }
                database.endTransaction();
                throw new SQLException("Failed to insert row into " + uri);
            case URI_DEEP_CONTACT:
                long deepContactId = database.insertWithOnConflict(DATABASE_TABLES[1], DeepContactData.USER_ID, values, SQLiteDatabase.CONFLICT_IGNORE);
                if (deepContactId > 0) {
                    Uri newUri = ContentUris.withAppendedId(
                            DeepContactData.CONTENT_URI,
                            deepContactId);
                    getContext().getContentResolver().notifyChange(newUri, null, false);
                    database.setTransactionSuccessful();
                    database.endTransaction();
                    return newUri;
                }
                database.endTransaction();
                throw new SQLException("Failed to insert row into " + uri);
            case URI_TEMP_USER_ID:
                long tempUserIdId = database.insertWithOnConflict(DATABASE_TABLES[2], DeepContactData.USER_ID, values, SQLiteDatabase.CONFLICT_REPLACE);
                if (tempUserIdId > 0) {
                    Uri newUri = ContentUris.withAppendedId(
                            TempUserIdData.CONTENT_URI,
                            tempUserIdId);
                    getContext().getContentResolver().notifyChange(newUri, null, false);
                    database.setTransactionSuccessful();
                    database.endTransaction();
                    return newUri;
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
        switch (uriMatcher.match(uri)) {
            case URI_BEACON:
                qb.setTables(DATABASE_TABLES[0]);
                qb.setProjectionMap(databaseMapBeacon);
                break;
            case URI_DEEP_CONTACT:
                qb.setTables(DATABASE_TABLES[1]);
                qb.setProjectionMap(databaseMapDeepContact);
                break;
            case URI_TEMP_USER_ID:
                qb.setTables(DATABASE_TABLES[2]);
                qb.setProjectionMap(databaseMapTempUserId);
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
        switch (uriMatcher.match(uri)) {
            case URI_BEACON:
                count = database.update(DATABASE_TABLES[0], values, selection,
                        selectionArgs);
                break;
            case URI_DEEP_CONTACT:
                count = database.update(DATABASE_TABLES[1], values, selection,
                        selectionArgs);
                break;
            case URI_TEMP_USER_ID:
                count = database.update(DATABASE_TABLES[2], values, selection,
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
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME_BEACON);
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

    public static final class DeepContactData implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME_DEEP_CONTACTS);
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.aware.plugin.beacon.deep_contact";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.ANY_CURSOR_ITEM_TYPE + "/vnd.aware.plugin.beacon.deep_contact";
        public static final String _ID = "_id";
        public static final String TIMESTAMP = "timestamp";
        public static final String USER_ID = "user_id";
        public static final String CONTACTED_USER_ID = "contacted_user_id";
        public static final String START_TIME = "start_time";
        public static final String END_TIME = "end_time";

        private DeepContactData() {
        }
    }

    public static final class TempUserIdData {
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME_TEMP_USER_ID);
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.aware.plugin.beacon.temp_user_id";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.ANY_CURSOR_ITEM_TYPE + "/vnd.aware.plugin.beacon.temp_user_id";
        public static final String _ID = "_id";
        public static final String TIMESTAMP = "timestamp";
        public static final String TEMP_ID = "temp_id";
        public static final String START_TIME = "start_time";
        public static final String EXPIRY_TIME = "expiry_time";
        public static final String INFLECTION_FLAG = "inflection_flag";

        private TempUserIdData() {
        }
    }
}
