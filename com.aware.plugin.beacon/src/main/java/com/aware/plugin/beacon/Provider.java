package com.aware.plugin.beacon;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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

    @Override
    public boolean onCreate() {
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    public static final class BeaconData implements BaseColumns {
        private BeaconData() {
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
            BeaconData._ID + " integer primary key autoincrement," +
                    BeaconData.TIMESTAMP + " real default 0," +
                    BeaconData.DEVICE_ID + " text default ''," +
                    BeaconData.UUID + " text default ''," +
                    BeaconData.MAJOR + " integer default ''," +
                    BeaconData.MINOR + " integer default ''," +
                    BeaconData.RSSI + " integer default ''," +
                    BeaconData.TX_POWER + " integer default ''"
    };
}
