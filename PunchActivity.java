package com.example.crahul.geotrack;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;


public class PunchActivity extends AppCompatActivity {
    private TrackGPS gps;
    private double latitude;
    private double longitude;
    private static final int CAMERA_REQUEST = 1888;
    long newRowId;
    public NetworkChangeReceiver receiver;
    Boolean network_broadcast = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_punch);

        final Button punchButton = (Button) findViewById(R.id.punch);
        punchButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                saveToDB();
                showDBData();
                System.out.println("baaaah");
            }
        });
        final Button syncButton = (Button) findViewById(R.id.sync);
        syncButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                saveToRemoteDB();
                System.out.println("booo");
            }
        });
    }

    private void saveToRemoteDB() {
        if (network_broadcast) {
            System.out.println("Internet Up");
            Cursor cursor = showDBData();
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
                String photo = cursor.getBlob(
                        cursor.getColumnIndexOrThrow(GeoTrackDBContract.Attendance.COLUMN_PHOTO)
                ).toString();

                System.out.println("UserID: "+user_id+" CreatedAt : "+created_at+" LAT: "+geo_lat+" LONG: "+geo_long);
                ArrayList<String> params = new ArrayList<String>();
                ArrayList<String> values = new ArrayList<String>();
                params.add("user_id");
                values.add(user_id);
                params.add("created_at");
                values.add(created_at);
                params.add("geo_lat");
                values.add(geo_lat);
                params.add("geo_long");
                values.add(geo_long);
                params.add("photo");
                values.add(photo);
                new BackgroundTaskPost(getString(R.string.remote_address) + "/upload", params, values, new BackgroundTaskPost.AsyncResponse() {
                    @Override
                    public void processFinish(String output) {
                        System.out.println(output);
                    }
                }).execute();
        }
        cursor.close();

        }
    }

    private Cursor showDBData() {
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
        return cursor;
//        System.out.println("The total cursor count is " + cursor.getCount());
//        for(int i = 0; i < cursor.getCount(); i++){
//            cursor.moveToPosition(i);
//            String created_at = cursor.getString(
//                    cursor.getColumnIndexOrThrow(GeoTrackDBContract.Attendance.COLUMN_CREATED_AT)
//            );
//            String user_id = cursor.getString(
//                    cursor.getColumnIndexOrThrow(GeoTrackDBContract.Attendance.COLUMN_USER_ID)
//            );
//            String geo_lat = cursor.getString(
//                    cursor.getColumnIndexOrThrow(GeoTrackDBContract.Attendance.COLUMN_GEO_LAT)
//            );
//            String geo_long = cursor.getString(
//                    cursor.getColumnIndexOrThrow(GeoTrackDBContract.Attendance.COLUMN_GEO_LONG)
//            );
////            String photo = cursor.getString(
////                    cursor.getColumnIndexOrThrow(GeoTrackDBContract.Attendance.COLUMN_PHOTO)
////            );
//
//            System.out.println("UserID: "+user_id+" CreatedAt : "+created_at+" LAT: "+geo_lat+" LONG: "+geo_long);
//        }
//        cursor.close();


    }

    private void saveToDB() {

        SQLiteDatabase database = new GeoTrackDBSQLiteHelper(this).getWritableDatabase();
        ContentValues values = new ContentValues();

        // Datetime
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());

        // GPS
        gps = new TrackGPS(PunchActivity.this);
        if (gps.canGetLocation()) {


            longitude = gps.getLongitude();
            latitude = gps.getLatitude();

            Toast.makeText(getApplicationContext(), "Longitude:" + Double.toString(longitude) + "\nLatitude:" + Double.toString(latitude), Toast.LENGTH_SHORT).show();
        } else {

            gps.showSettingsAlert();
        }

        // Camera
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
        String MyPREFERENCES = "MyPrefs";
        SharedPreferences prefs = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        int user_id = prefs.getInt("user_id", 0);

        values.put(GeoTrackDBContract.Attendance.COLUMN_USER_ID, Integer.toString(user_id));
        values.put(GeoTrackDBContract.Attendance.COLUMN_GEO_LAT, Double.toString(latitude));
        values.put(GeoTrackDBContract.Attendance.COLUMN_GEO_LONG, Double.toString(longitude));
        values.put(GeoTrackDBContract.Attendance.COLUMN_CREATED_AT, currentDateTimeString);


        newRowId = database.insert(GeoTrackDBContract.Attendance.TABLE_NAME, null, values);

        Toast.makeText(this, "The new Row Id is " + newRowId, Toast.LENGTH_LONG).show();

    }

    public void checkInternet() {
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        receiver = new NetworkChangeReceiver(this);
        registerReceiver(receiver, filter);
        network_broadcast = receiver.is_connected();
        Log.d("Boolean ", network_broadcast.toString());

    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        SQLiteDatabase database = new GeoTrackDBSQLiteHelper(this).getWritableDatabase();
        if (requestCode == CAMERA_REQUEST) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] imageByteArray = stream.toByteArray();
            ContentValues values = new ContentValues();
            values.put(GeoTrackDBContract.Attendance.COLUMN_PHOTO, imageByteArray);
            String[] selectionArgs = {Long.toString(newRowId)};

            database.update(GeoTrackDBContract.Attendance.TABLE_NAME,values,GeoTrackDBContract.Attendance._ID+"=?",selectionArgs);

        }
    }
}
