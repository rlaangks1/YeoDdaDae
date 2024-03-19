package com.bucheon.yeoddadae;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.skt.Tmap.TMapView;


public class MapActivity extends AppCompatActivity implements LocationListener {

    Button gpsButton;
    private LocationManager locationManager;

    double latitude; // 위도
    double longitude; // 경도


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        gpsButton = findViewById(R.id.gpsButton);

        FrameLayout frameLayoutTmap = (FrameLayout)findViewById(R.id.linearLayoutTmap);
        TMapView tMapView = new TMapView(this);

        tMapView.setSKTMapApiKey( "iqTSQ2hMuj8E7t2sy3WYA5m73LuX4iUD5iHgwRGf" );
        frameLayoutTmap.addView( tMapView );

        // 위치 관리자 초기화
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        gpsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, String.valueOf(latitude) + ", " + String.valueOf(longitude));

                if (tMapView != null) {
                    tMapView.setCenterPoint(longitude, latitude, true);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 위치 업데이트 요청
        if (locationManager != null) {
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 1F, this);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 위치 업데이트 중지
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // 위치 제공자의 상태가 변경될 때 호출됩니다.
    }

    @Override
    public void onProviderEnabled(String provider) {
        // 사용자가 위치 제공자를 사용 가능하게 만들 때 호출됩니다.
    }

    @Override
    public void onProviderDisabled(String provider) {
        // 사용자가 위치 제공자를 사용할 수 없게 만들 때 호출됩니다.
    }
}

