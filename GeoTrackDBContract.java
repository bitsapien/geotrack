package com.example.crahul.geotrack;

import android.provider.BaseColumns;

/**
 * Created by crahul on 02/06/17.
 */

public final class GeoTrackDBContract {
    private GeoTrackDBContract(){

    }
    public static class Attendance implements BaseColumns {
        public static final String TABLE_NAME = "attendance";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_GEO_LAT = "geo_lat";
        public static final String COLUMN_GEO_LONG = "geo_long";
        public static final String COLUMN_PHOTO = "photo";
        public static final String COLUMN_CREATED_AT = "created_at";

        public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " +
                TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USER_ID + " INTEGER, " +
                COLUMN_PHOTO + " BLOB, " +
                COLUMN_CREATED_AT + " TEXT, " +
                COLUMN_GEO_LAT + " REAL, " +
                COLUMN_GEO_LONG + " REAL" + ")";

    }
}
