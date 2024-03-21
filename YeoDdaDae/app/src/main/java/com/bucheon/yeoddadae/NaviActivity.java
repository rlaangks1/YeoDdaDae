package com.bucheon.yeoddadae;


import static android.content.ContentValues.TAG;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapTapi;
import com.skt.Tmap.TMapView;
import com.skt.Tmap.poi_item.TMapPOIItem;

import java.util.ArrayList;

public class NaviActivity extends AppCompatActivity implements TMapGpsManager.onLocationChangedCallback {

    ListView gasStationListView;
    Button gpsBtn;

    GasStationAdapter gasStationAdapter;

    TMapGpsManager gpsManager;
    TMapPoint nowPoint = new TMapPoint(37.570841, 126.985302);;

    private static String CLIENT_ID = "";
    private static String API_KEY = "iqTSQ2hMuj8E7t2sy3WYA5m73LuX4iUD5iHgwRGf";
    private static String USER_KEY = ""; // USER KEY 입력 필수 아님 : Copy License 기준 서비스 운영시 필요
    private static String DEVICE_KEY = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navi);

        gasStationListView = findViewById(R.id.gasStationListView);
        gpsBtn = findViewById(R.id.gpsBtn);

        gasStationAdapter = new GasStationAdapter();

        gpsManager = new TMapGpsManager(this);
        gpsManager.setMinTime(1000);
        gpsManager.setMinDistance(5);
        gpsManager.setProvider(gpsManager.GPS_PROVIDER);
        gpsManager.OpenGps();

        LinearLayout frameLayoutTmap = (LinearLayout)findViewById(R.id.linearLayoutTmap);
        TMapView tMapView = new TMapView(this);
        tMapView.setSKTMapApiKey( API_KEY );
        frameLayoutTmap.addView( tMapView );

        gpsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findGasStation();
            }
        });
    }

    public void findGasStation() {
        TMapData tMapData = new TMapData();

        tMapData.findAroundNamePOI(nowPoint, "주유소", 10, 20, new TMapData.FindAroundNamePOIListenerCallback ()
        {
            @Override
            public void onFindAroundNamePOI(ArrayList<TMapPOIItem> arrayList) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < arrayList.size(); i++) {
                            TMapPOIItem item = arrayList.get(i);
                            gasStationAdapter.addItem(new GasStationItem(item.name, item.distance, item.hhPrice, item.ggPrice, item.telNo, 0));
                        }
                        gasStationListView.setAdapter(gasStationAdapter);
                    }
                });
            }
        });
    }

    @Override
    public void onLocationChange(Location location){
        nowPoint = gpsManager.getLocation();
    }
}
