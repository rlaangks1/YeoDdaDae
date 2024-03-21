package com.bucheon.yeoddadae;

import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.skt.Tmap.TMapView;


public class MapActivity extends AppCompatActivity  {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        LinearLayout linearLayoutTmap = (LinearLayout)findViewById(R.id.linearLayoutTmap);
        TMapView tMapView = new TMapView(this);

        tMapView.setSKTMapApiKey( "iqTSQ2hMuj8E7t2sy3WYA5m73LuX4iUD5iHgwRGf" );
        linearLayoutTmap.addView( tMapView );
    }
}
