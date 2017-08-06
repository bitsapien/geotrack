package com.example.crahul.geotrack;

/**
 * Created by crahul on 24/07/17.
 */

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;

//for drawerList

public class ListViewAdapter extends ArrayAdapter<String> {
    private final Context context;
    TextView tv_size, tv_price;
    Cursor cursor;
    public ListViewAdapter(Context context, Cursor cursor) {
        super(context, R.layout.listview_entries, new String[]{});
        this.context = context;
        this.cursor = cursor;
    }

    @Override
    public void add(String object) {
        super.add(object);
    }

    @Override
    public int getCount() {
        return cursor.getCount();
    }

    //@TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.listview_entries, parent, false);
        TextView date, place, synced;
        date= (TextView) rowView.findViewById(R.id.column_time);
        place= (TextView) rowView.findViewById(R.id.column_place);
        synced= (TextView) rowView.findViewById(R.id.column_synced);
        cursor.moveToPosition(position);
//        place.setText("Lat:"+cursor.getColumnIndexOrThrow(GeoTrackDBContract.Attendance.COLUMN_GEO_LAT)+", Lon:"+cursor.getColumnIndexOrThrow(GeoTrackDBContract.Attendance.COLUMN_GEO_LONG));
        Log.e("Date", cursor.getColumnIndexOrThrow(GeoTrackDBContract.Attendance.COLUMN_CREATED_AT)+"");
        Log.e("Synced", cursor.getColumnIndexOrThrow(GeoTrackDBContract.Attendance.COLUMN_PUNCH_STATE)+"");

        date.setText(cursor.getColumnIndexOrThrow(GeoTrackDBContract.Attendance.COLUMN_CREATED_AT)+"");
        synced.setText(cursor.getColumnIndexOrThrow(GeoTrackDBContract.Attendance.COLUMN_PUNCH_STATE)+"");
        return rowView;
    }

}