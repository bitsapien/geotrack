package com.example.crahul.geotrack;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Date;



public class PunchActivity extends AppCompatActivity {
    private TrackGPS gps;
    private double latitude;
    private double longitude;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_punch);

        final Button button = (Button) findViewById(R.id.button1);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                saveToDB();
                showDBData();
                System.out.println("baaaah");
            }
        });
    }

    private void showDBData() {
        SQLiteDatabase database = new GeoTrackDBSQLiteHelper(this).getReadableDatabase();
        String[] projection = {
                GeoTrackDBContract.Attendance._ID,
                GeoTrackDBContract.Attendance.COLUMN_USER_ID,
                GeoTrackDBContract.Attendance.COLUMN_GEO_LAT,
                GeoTrackDBContract.Attendance.COLUMN_GEO_LONG,
                GeoTrackDBContract.Attendance.COLUMN_PHOTO,
                GeoTrackDBContract.Attendance.COLUMN_CREATED_AT

        };
        Cursor cursor = database.query(
                GeoTrackDBContract.Attendance.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null
        );
        System.out.println("The total cursor count is " + cursor.getCount());
        for(int i = 0; i < cursor.getCount(); i++){
            cursor.moveToPosition(i);
            String created_at = cursor.getString(
                    cursor.getColumnIndexOrThrow(GeoTrackDBContract.Attendance.COLUMN_CREATED_AT)
            );
            String user_id = cursor.getString(
                    cursor.getColumnIndexOrThrow(GeoTrackDBContract.Attendance.COLUMN_USER_ID)
            );
            String geo_lat = cursor.getString(
                    cursor.getColumnIndexOrThrow(GeoTrackDBContract.Attendance.COLUMN_GEO_LAT)
            );
            String geo_long = cursor.getString(
                    cursor.getColumnIndexOrThrow(GeoTrackDBContract.Attendance.COLUMN_GEO_LONG)
            );
            String photo = cursor.getString(
                    cursor.getColumnIndexOrThrow(GeoTrackDBContract.Attendance.COLUMN_PHOTO)
            );

            System.out.println("UserID: "+user_id+" CreatedAt : "+created_at+" LAT: "+geo_lat+" LONG: "+geo_long+" PHOTO: "+photo);
        }
        cursor.close();


    }

    private void saveToDB() {
        SQLiteDatabase database = new GeoTrackDBSQLiteHelper(this).getWritableDatabase();

        ContentValues values = new ContentValues();

        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());

        gps = new TrackGPS(PunchActivity.this);
        if(gps.canGetLocation()){


            longitude = gps.getLongitude();
            latitude = gps .getLatitude();

            Toast.makeText(getApplicationContext(),"Longitude:"+Double.toString(longitude)+"\nLatitude:"+Double.toString(latitude),Toast.LENGTH_SHORT).show();
        }
        else
        {

            gps.showSettingsAlert();
        }


        values.put(GeoTrackDBContract.Attendance.COLUMN_USER_ID, "1");
        values.put(GeoTrackDBContract.Attendance.COLUMN_GEO_LAT, "1.2");
        values.put(GeoTrackDBContract.Attendance.COLUMN_GEO_LONG, "1.3");
        values.put(GeoTrackDBContract.Attendance.COLUMN_CREATED_AT, currentDateTimeString);


        long newRowId = database.insert(GeoTrackDBContract.Attendance.TABLE_NAME, null, values);

        Toast.makeText(this, "The new Row Id is " + newRowId, Toast.LENGTH_LONG).show();
    }
}
