package com.example.crahul.geotrack;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.sql.Blob;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;

import static com.example.crahul.geotrack.LoginActivity.UserLoginTask.MyPREFERENCES;


public class PunchActivity extends AppCompatActivity {
    private TrackGPS gps;
    private double latitude;
    private double longitude;
    private static final int CAMERA_REQUEST = 1888;
    long newRowId;
    public NetworkChangeReceiver receiver;
    Boolean network_broadcast = true;
    public Button punchButton;
    SharedPreferences sharedpreferences;
    String[] weeks= new String[]{"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedpreferences = getSharedPreferences(getResources().getString(R.string.my_preferences), Context.MODE_PRIVATE);
        setContentView(R.layout.activity_punch);
        punchButton = (Button) findViewById(R.id.punch_in);
        switchPunchButton();
        String[] permissions = new String[]{Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};

        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(this,
                    permissions[i])
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, permissions,
                        99);
            }
        }

        SimpleDateFormat sdfDate = new SimpleDateFormat("dd MMMM yy");
        // get time and location
        String day = weeks[new Date().getDay()];
        TextView date = (TextView) findViewById(R.id.dateView);
        date.setText(day+", "+sdfDate.format(new Date()));
        TextView timeView = (TextView) findViewById(R.id.timeView);
        sdfDate = new SimpleDateFormat("HH:mm");
        timeView.setText(sdfDate.format(new Date()));

        ListView listview = (ListView) findViewById(R.id.list_item);
        ListViewAdapter adapter=new ListViewAdapter(this, showDBData());
        listview.setAdapter(adapter);
        punchButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                int punch_state = decidePunchState();
                switchPunchButton();
                saveToDB(punch_state);
                showDBData();
            }
        });
        final Button syncButton = (Button) findViewById(R.id.sync);
        syncButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                saveToRemoteDB();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 99: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private int decidePunchState() {
        int punch_state = sharedpreferences.getInt("punch_state", 0);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        if(punch_state == 0) {
            punch_state = 1;
        }
        else {
            punch_state = 0;
        }

        editor.putInt("punch_state", punch_state);

        editor.commit();
        switchPunchButton();

        return punch_state;
    }

    private void switchPunchButton(){
        int punch_state = sharedpreferences.getInt("punch_state", 0);
        if(punch_state == 1)
            punchButton.setText(R.string.punch_out_attendance);
        else
            punchButton.setText(R.string.punch_in_attendance);
    }

    private void saveToRemoteDB() {
        if (network_broadcast) {
            System.out.println("Internet Up");
            Cursor cursor = showDBData();
            sendData(cursor, 0);

            Toast.makeText(getApplicationContext(), "Synced!", Toast.LENGTH_SHORT).show();

        }
    }

    private void sendData(final Cursor cursor, final int i) {
        if(i == cursor.getCount()){
            cursor.close();
            return;
        }

        cursor.moveToPosition(i);

        final int saved_state = cursor.getInt(
                cursor.getColumnIndexOrThrow(GeoTrackDBContract.Attendance.COLUMN_SAVED_STATE)
        );
        if(saved_state==1){
            sendData(cursor, i+1);
            return;
        }
        String created_at = cursor.getString(
                cursor.getColumnIndexOrThrow(GeoTrackDBContract.Attendance.COLUMN_CREATED_AT)
        );
        final Long _id = cursor.getLong(
                cursor.getColumnIndexOrThrow(GeoTrackDBContract.Attendance._ID)
        );
        final String user_id = cursor.getString(
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
        int punch_flag = cursor.getInt(
                cursor.getColumnIndexOrThrow(GeoTrackDBContract.Attendance.COLUMN_PUNCH_STATE)
        );
        String punch_state;
        if(punch_flag == 0) {
            punch_state = "punch_out";
        } else {
            punch_state = "punch_in";
        }


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
        params.add("punch_state");
        values.add(punch_state);
        Log.e("XXXX", "Punch state being send:"+punch_state);
        new BackgroundTaskPost(getResources().getString(R.string.remote_address) + "/upload", params, values, new BackgroundTaskPost.AsyncResponse() {
            @Override
            public void processFinish(String output) {
                //update local table with returned flag
                if(output.contains("true"))
                {
                    //success, update in local db
                    ContentValues values = new ContentValues();
                    values.put(GeoTrackDBContract.Attendance.COLUMN_SAVED_STATE, 1);
                    String[] selectionArgs = {Long.toString(_id)};
                    SQLiteDatabase database = new GeoTrackDBSQLiteHelper(PunchActivity.this).getWritableDatabase();
                    database.update(GeoTrackDBContract.Attendance.TABLE_NAME,values,GeoTrackDBContract.Attendance._ID+"=?",selectionArgs);

                }
                sendData(cursor, i+1);
            }
        }).execute();
    }

    private Cursor showDBData() {
        SQLiteDatabase database = new GeoTrackDBSQLiteHelper(this).getReadableDatabase();
        String[] projection = {
                GeoTrackDBContract.Attendance._ID,
                GeoTrackDBContract.Attendance.COLUMN_USER_ID,
                GeoTrackDBContract.Attendance.COLUMN_GEO_LAT,
                GeoTrackDBContract.Attendance.COLUMN_GEO_LONG,
                GeoTrackDBContract.Attendance.COLUMN_PHOTO,
                GeoTrackDBContract.Attendance.COLUMN_CREATED_AT,
                GeoTrackDBContract.Attendance.COLUMN_PUNCH_STATE,
                GeoTrackDBContract.Attendance.COLUMN_SAVED_STATE
        };
        Cursor cursor = database.query(
                GeoTrackDBContract.Attendance.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null,
                null
        );
        return cursor;


    }

    private void saveToDB(int punch_state) {

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
        values.put(GeoTrackDBContract.Attendance.COLUMN_PUNCH_STATE, punch_state);


        newRowId = database.insert(GeoTrackDBContract.Attendance.TABLE_NAME, null, values);
        Log.e("XXXX", "Punch state saved to local:"+punch_state);
//        Toast.makeText(this, "The new Row Id is " + newRowId, Toast.LENGTH_LONG).show();


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
            Log.e("XXXX1", "width:"+photo.getWidth()+", height:"+photo.getHeight());
            photo.compress(Bitmap.CompressFormat.PNG, 100, stream);
            Log.e("XXXX2", "width:"+photo.getWidth()+", height:"+photo.getHeight());
//            photo = scaleDown(photo, )
            byte[] imageByteArray = stream.toByteArray();
            String base64Photo = Base64.encodeToString(imageByteArray, Base64.DEFAULT);
            ContentValues values = new ContentValues();
            values.put(GeoTrackDBContract.Attendance.COLUMN_PHOTO, base64Photo);
            String[] selectionArgs = {Long.toString(newRowId)};

            database.update(GeoTrackDBContract.Attendance.TABLE_NAME,values,GeoTrackDBContract.Attendance._ID+"=?",selectionArgs);


        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.punch_activity, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id){
            case R.id.sync:
                saveToRemoteDB();
                break;
            case R.id.action_logout:
                SharedPreferences sharedpreferences = getSharedPreferences(getResources().getString(R.string.my_preferences), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.clear();
                editor.commit();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public static Bitmap scaleDown(Bitmap realImage, float maxImageSize,
                                   boolean filter) {
        float ratio = Math.min(
                (float) maxImageSize / realImage.getWidth(),
                (float) maxImageSize / realImage.getHeight());
        int width = Math.round((float) ratio * realImage.getWidth());
        int height = Math.round((float) ratio * realImage.getHeight());

        Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width,
                height, filter);
        return newBitmap;
    }
}
