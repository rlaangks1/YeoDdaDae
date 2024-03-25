package com.bucheon.yeoddadae;


import static android.content.ContentValues.TAG;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.skt.Tmap.TMapCircle;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapMarkerItem2;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapView;
import com.skt.Tmap.poi_item.TMapPOIItem;

import java.util.ArrayList;

public class FindGasStationActivity extends AppCompatActivity implements TMapGpsManager.onLocationChangedCallback, TMapView.OnClickListenerCallback {

    int sttSort;

    ListView gasStationListView;
    Button zoomOutBtn;
    Button zoomInBtn;
    Button gpsBtn;
    Button sortByDistanceBtn;
    Button sortByRateBtn;
    Button sortByGasolinePriceBtn;
    Button sortByDieselPriceBtn;
    Button sortByLpgPriceBtn;

    AlertDialog loadingAlert;

    // 경복궁
    double lat = 37.578611; // 위도
    double lon = 126.977222; // 경도

    private static String CLIENT_ID = "";
    private static String API_KEY = "iqTSQ2hMuj8E7t2sy3WYA5m73LuX4iUD5iHgwRGf";
    private static String USER_KEY = ""; // USER KEY 입력 필수 아님 : Copy License 기준 서비스 운영시 필요
    private static String DEVICE_KEY = "";

    TMapPoint nowPoint = new TMapPoint(lat, lon);
    TMapGpsManager gpsManager;
    boolean firstOnLocationChangeCalled = false;
    TMapView tMapView;
    TMapCircle tMapCircle;
    TMapData tMapData;
    TMapMarkerItem selectedMarker;
    GasStationAdapter gasStationAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_gas_station);

        // 로딩 AlertDialog 지정
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("로딩 중").setCancelable(false).setNegativeButton("액티비티 종료", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });
        loadingAlert = builder.create();

        loadingStart();

        Intent inIntent = getIntent();
        sttSort = inIntent.getIntExtra("sttSort", 0);
        if (sttSort == 0) {
            Log.d(TAG, "sttSort가 0임 (오류)");
            finish();
        }

        // 뷰 정의
        gasStationListView = findViewById(R.id.gasStationListView);
        zoomOutBtn = findViewById(R.id.zoomOutBtn);
        zoomInBtn = findViewById(R.id.zoomInBtn);
        gpsBtn = findViewById(R.id.gpsBtn);
        sortByDistanceBtn = findViewById(R.id.sortByDistanceBtn);
        sortByRateBtn = findViewById(R.id.sortByRateBtn);
        sortByGasolinePriceBtn = findViewById(R.id.sortByGasolinePriceBtn);
        sortByDieselPriceBtn = findViewById(R.id.sortByDieselPriceBtn);
        sortByLpgPriceBtn = findViewById(R.id.sortByLpgPriceBtn);

        // GPSManager 설정
        gpsManager = new TMapGpsManager(this);
        gpsManager.setMinTime(1000);
        gpsManager.setMinDistance(1);
        gpsManager.setProvider(gpsManager.GPS_PROVIDER);
        gpsManager.OpenGps();
        gpsManager.setProvider(gpsManager.NETWORK_PROVIDER);
        gpsManager.OpenGps();
        
        // TMapView 보이기
        LinearLayout linearLayoutTmap = (LinearLayout)findViewById(R.id.linearLayoutTmap);
        tMapView = new TMapView(this);
        tMapView.setSKTMapApiKey( API_KEY );
        linearLayoutTmap.addView( tMapView );

        // TMapView 설정
        tMapView.setCenterPoint(lon, lat);
        tMapView.setLocationPoint(lon, lat);
        tMapView.setIcon(BitmapFactory.decodeResource(this.getResources(), R.drawable.temp_tmap_my_location));
        tMapView.setIconVisibility(true);
        tMapView.setSightVisible(true);

        // TMapCircle 설정
        tMapCircle = new TMapCircle();
        tMapCircle.setRadius(3000);
        tMapCircle.setCircleWidth(0);
        tMapCircle.setAreaColor(Color.rgb(0, 0, 0));
        tMapCircle.setAreaAlpha(25);

        // TMapCircle 추가
        tMapView.addTMapCircle("Circle", tMapCircle);

        // GPS 현재 위치로 맵 중심점 이동
        gpsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tMapView.setCenterPoint(lon, lat);
                tMapView.setZoomLevel(16);
            }
        });

        // 맵 축소
        zoomOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tMapView.getZoomLevel() >= 8 ) {
                    tMapView.MapZoomOut();
                    Log.d(TAG, "축소, 현재 ZoomLevel : " + tMapView.getZoomLevel());
                }
            }
        });

        // 맵 확대
        zoomInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tMapView.getZoomLevel() <= 17 ) {
                    tMapView.MapZoomIn();
                    Log.d(TAG, "확대, 현재 ZoomLevel : " + tMapView.getZoomLevel());
                }
            }
        });

        sortByDistanceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingStart();
                findGasStation(1);
                loadingStop();
            }
        });

        sortByRateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingStart();
                findGasStation(2);
                loadingStop();
            }
        });

        sortByGasolinePriceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingStart();
                findGasStation(3);
                loadingStop();
            }
        });

        sortByDieselPriceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingStart();
                findGasStation(4);
                loadingStop();
            }
        });

        sortByLpgPriceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingStart();
                findGasStation(5);
                loadingStop();
            }
        });

        gasStationListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 클릭한 gasStation의 정보를 가져옵니다.
                GasStationItem clickedGasStation = (GasStationItem) parent.getItemAtPosition(position);

                Log.d(TAG, "리스트뷰에서 주유소 아이템 클릭함 : " + clickedGasStation.getName() + ", " + clickedGasStation.getRadius() + ", " + clickedGasStation.getGasolinePrice()
                        + ", " +  clickedGasStation.getDieselPrice() + ", " + clickedGasStation.getLpgPrice() + ", " + clickedGasStation.getPhone() + ", " + clickedGasStation.getAddition()
                        + ", " + clickedGasStation.getStarRate() + ", " + clickedGasStation.getLat() + ", " + clickedGasStation.getLon());

                TMapData tmapdata = new TMapData();
                TMapPoint end = new TMapPoint (clickedGasStation.getLat(), clickedGasStation.getLon());
                tmapdata.findPathData(nowPoint, end, new TMapData.FindPathDataListenerCallback() {
                    @Override
                    public void onFindPathData(TMapPolyLine polyLine) {
                        if (selectedMarker != null) {
                            selectedMarker.setIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(),R.drawable.temp_tmap_marker));
                        }

                        tMapView.setTMapPathIcon(null, null);

                        TMapMarkerItem endMarker = tMapView.getMarkerItemFromID(clickedGasStation.getName());
                        endMarker.setIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(),R.drawable.temp_tmap_selected_marker));

                        selectedMarker = endMarker;

                        tMapView.addTMapPath(polyLine);
                    }
                });
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        gasStationListView.setVisibility(View.GONE);
        sortByDistanceBtn.setVisibility(View.GONE);
        sortByRateBtn.setVisibility(View.GONE);
        sortByGasolinePriceBtn.setVisibility(View.GONE);
        sortByDieselPriceBtn.setVisibility(View.GONE);
        sortByLpgPriceBtn.setVisibility(View.GONE);
    }

    public void findGasStation(int sortBy) { // sortBy는 정렬기준 (1:거리순, 2:평점순, 3:휘발유가순, 4: 경유가순, 5:LPG가순
        tMapView.removeAllMarkerItem();

        // tMapData 초기화
        tMapData = new TMapData();

        tMapData.findAroundNamePOI(nowPoint, "주유소", 3, 200, new TMapData.FindAroundNamePOIListenerCallback ()
        {
            @Override
            public void onFindAroundNamePOI(ArrayList<TMapPOIItem> arrayList) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int originalBackgroundColor = Color.rgb(128,128,128);
                        int selectedBackgroundColor = Color.rgb(0,0,255);

                        gasStationAdapter = new GasStationAdapter();

                        for (int i = 0; i < arrayList.size(); i++) {
                            TMapPOIItem item = arrayList.get(i);
                            gasStationAdapter.addItem(new GasStationItem(item.name, item.radius, item.hhPrice, item.ggPrice, item.llPrice, item.telNo, item.menu1, 0, item.frontLat, item.frontLon));

                            TMapPoint tpoint = new TMapPoint(Double.parseDouble(item.frontLat), Double.parseDouble(item.frontLon));
                            TMapMarkerItem tItem = new TMapMarkerItem();

                            tItem.setTMapPoint(tpoint);
                            tItem.setName(item.name);
                            tItem.setVisible(TMapMarkerItem.VISIBLE);

                            Bitmap bitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(),R.drawable.temp_tmap_marker);
                            tItem.setIcon(bitmap);

                            tItem.setPosition(0.5f,1.0f); // 마커의 중심점을 하단, 중앙으로 설정

                            tMapView.addMarkerItem(item.name, tItem);
                        }
                        switch (sortBy) {
                            case 1 :
                                sortByDistanceBtn.setBackgroundColor(selectedBackgroundColor);
                                sortByRateBtn.setBackgroundColor(originalBackgroundColor);
                                sortByGasolinePriceBtn.setBackgroundColor(originalBackgroundColor);
                                sortByDieselPriceBtn.setBackgroundColor(originalBackgroundColor);
                                sortByLpgPriceBtn.setBackgroundColor(originalBackgroundColor);
                                break;
                            case 2 :
                                sortByDistanceBtn.setBackgroundColor(originalBackgroundColor);
                                sortByRateBtn.setBackgroundColor(selectedBackgroundColor);
                                sortByGasolinePriceBtn.setBackgroundColor(originalBackgroundColor);
                                sortByDieselPriceBtn.setBackgroundColor(originalBackgroundColor);
                                sortByLpgPriceBtn.setBackgroundColor(originalBackgroundColor);

                                gasStationAdapter.sortByRate();
                                break;
                            case 3 :
                                sortByDistanceBtn.setBackgroundColor(originalBackgroundColor);
                                sortByRateBtn.setBackgroundColor(originalBackgroundColor);
                                sortByGasolinePriceBtn.setBackgroundColor(selectedBackgroundColor);
                                sortByDieselPriceBtn.setBackgroundColor(originalBackgroundColor);
                                sortByLpgPriceBtn.setBackgroundColor(originalBackgroundColor);

                                gasStationAdapter.sortByGasolinePrice();
                                break;
                            case 4 :
                                sortByDistanceBtn.setBackgroundColor(originalBackgroundColor);
                                sortByRateBtn.setBackgroundColor(originalBackgroundColor);
                                sortByGasolinePriceBtn.setBackgroundColor(originalBackgroundColor);
                                sortByDieselPriceBtn.setBackgroundColor(selectedBackgroundColor);
                                sortByLpgPriceBtn.setBackgroundColor(originalBackgroundColor);

                                gasStationAdapter.sortByDieselPrice();
                                break;
                            case 5 :
                                sortByDistanceBtn.setBackgroundColor(originalBackgroundColor);
                                sortByRateBtn.setBackgroundColor(originalBackgroundColor);
                                sortByGasolinePriceBtn.setBackgroundColor(originalBackgroundColor);
                                sortByDieselPriceBtn.setBackgroundColor(originalBackgroundColor);
                                sortByLpgPriceBtn.setBackgroundColor(selectedBackgroundColor);

                                gasStationAdapter.sortByLpgPrice();
                                break;
                        }
                        gasStationListView.setAdapter(gasStationAdapter);

                        gasStationListView.setVisibility(View.VISIBLE);
                        sortByDistanceBtn.setVisibility(View.VISIBLE);
                        sortByRateBtn.setVisibility(View.VISIBLE);
                        sortByGasolinePriceBtn.setVisibility(View.VISIBLE);
                        sortByDieselPriceBtn.setVisibility(View.VISIBLE);
                        sortByLpgPriceBtn.setVisibility(View.VISIBLE);

                        tMapView.removeAllTMapCircle();

                        tMapCircle.setCenterPoint( nowPoint );
                        tMapView.addTMapCircle("Circle", tMapCircle);
                    }
                });
            }
        });
    }

    @Override
    public void onLocationChange(Location location){
        Log.d(TAG, "onLocationChange 호출됨");

        lat = location.getLatitude();
        Log.d(TAG, "onLocationChange 호출됨 : lat(경도) = " + lat);

        lon = location.getLongitude();
        Log.d(TAG, "onLocationChange 호출됨 : lon(위도) = " + lon);

        nowPoint = gpsManager.getLocation();
        tMapView.setLocationPoint(lon, lat);

        if (!firstOnLocationChangeCalled) {
            findGasStation(sttSort);
            tMapView.setCenterPoint(lon, lat);
            firstOnLocationChangeCalled = true;
            loadingStop();
        }
    }

    public void loadingStart() {
        Log.d(TAG, "로딩 시작");
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        loadingAlert.show();
    }

    public void loadingStop() {
        Log.d(TAG, "로딩 끝");
        loadingAlert.dismiss();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    @Override
    public boolean onPressEvent(ArrayList<TMapMarkerItem> arrayList, ArrayList<TMapPOIItem> arrayList1, TMapPoint tMapPoint, PointF pointF) {
        if (arrayList.size() > 0) {
            TMapMarkerItem endMarker = arrayList.get(0);
            TMapData tmapdata = new TMapData();
            TMapPoint endPoint = endMarker.getTMapPoint();

            Log.d(TAG, "TMapView에서 주유소 마커 클릭함 : " + endMarker.getName() + ", " + endPoint.getLatitude() + ", " + endPoint.getLongitude());

            tmapdata.findPathData(nowPoint, endPoint, new TMapData.FindPathDataListenerCallback() {
                @Override
                public void onFindPathData(TMapPolyLine polyLine) {
                    if (selectedMarker != null) {
                        selectedMarker.setIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(),R.drawable.temp_tmap_marker));
                    }

                    tMapView.setTMapPathIcon(null, null);

                    endMarker.setIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(),R.drawable.temp_tmap_selected_marker));

                    selectedMarker = endMarker;

                    tMapView.addTMapPath(polyLine);
                }
            });
        }
        return false;
    }

    @Override
    public boolean onPressUpEvent(ArrayList<TMapMarkerItem> arrayList, ArrayList<TMapPOIItem> arrayList1, TMapPoint tMapPoint, PointF pointF) {
        return false;
    }
}
