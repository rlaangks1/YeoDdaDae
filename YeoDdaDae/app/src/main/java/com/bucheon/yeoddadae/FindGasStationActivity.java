package com.bucheon.yeoddadae;


import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.location.Location;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.skt.Tmap.TMapCircle;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapMarkerItem2;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapView;
import com.skt.Tmap.poi_item.TMapPOIItem;
import com.skt.tmap.engine.navigation.SDKManager;

import java.util.ArrayList;
import java.util.HashMap;

public class FindGasStationActivity extends AppCompatActivity implements TMapGpsManager.onLocationChangedCallback, TMapView.OnClickListenerCallback {
    private static final int PERMISSION_REQUEST_CODE = 1;

    boolean firstOnLocationChangeCalled = false; // onLocationChange가 처음 불림 여부

    int recievedSort;
    int nowSort;

    ListView gasStationListView;
    Button zoomOutBtn;
    Button zoomInBtn;
    Button gpsBtn;
    HorizontalScrollView gasStationSortHorizontalScrollView;
    Button sortByDistanceBtn;
    Button sortByRateBtn;
    Button sortByGasolinePriceBtn;
    Button sortByDieselPriceBtn;
    Button sortByLpgPriceBtn;
    Button cancelNaviBtn;
    Button toStartNaviBtn;

    AlertDialog loadingAlert;

    Bitmap tmapMyLocationIcon;
    Bitmap tmapMarkerIcon;
    Bitmap tmapStartMarkerIcon;
    Bitmap tmapSelectedMarkerIcon;

    // 경복궁
    double lat = 37.578611; // 위도
    double lon = 126.977222; // 경도

    private static String CLIENT_ID = "";
    private static String API_KEY = "iqTSQ2hMuj8E7t2sy3WYA5m73LuX4iUD5iHgwRGf";
    private static String USER_KEY = ""; // USER KEY 입력 필수 아님 : Copy License 기준 서비스 운영시 필요
    private static String DEVICE_KEY = "";

    TMapPoint nowPoint = new TMapPoint(lat, lon);
    TMapPoint naviEndPoint;
    String naviEndPointName;
    TMapGpsManager gpsManager;
    TMapView tMapView;
    TMapCircle tMapCircle;
    TMapData tMapData;
    TMapMarkerItem selectedMarker;
    GasStationAdapter gasStationAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_gas_station);

        // 로딩 시작
        loadingStart();

        // 메인액티비티에서 정렬기준 받기
        Intent inIntent = getIntent();
        recievedSort = inIntent.getIntExtra("SortBy", 0);
        if (recievedSort == 0) {
            Log.d(TAG, "sttSort가 0임 (오류)");
            finish();
        }

        // 뷰 정의
        gasStationListView = findViewById(R.id.gasStationListView);
        zoomOutBtn = findViewById(R.id.zoomOutBtn);
        zoomInBtn = findViewById(R.id.zoomInBtn);
        gpsBtn = findViewById(R.id.gpsBtn);
        gasStationSortHorizontalScrollView = findViewById(R.id.gasStationSortHorizontalScrollView);
        sortByDistanceBtn = findViewById(R.id.sortByDistanceBtn);
        sortByRateBtn = findViewById(R.id.sortByRateBtn);
        sortByGasolinePriceBtn = findViewById(R.id.sortByGasolinePriceBtn);
        sortByDieselPriceBtn = findViewById(R.id.sortByDieselPriceBtn);
        sortByLpgPriceBtn = findViewById(R.id.sortByLpgPriceBtn);
        cancelNaviBtn = findViewById(R.id.cancelNaviBtn);
        toStartNaviBtn = findViewById(R.id.toStartNaviBtn);

        // Bitmap 정의
        tmapMyLocationIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.temp_tmap_my_location);
        tmapMarkerIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.temp_tmap_marker);
        tmapStartMarkerIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.temp_tmap_start_marker);
        tmapSelectedMarkerIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.temp_tmap_selected_marker);

        // 로딩까지 뷰 없애기
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                gasStationListView.setVisibility(View.GONE);
                sortByDistanceBtn.setVisibility(View.GONE);
                sortByRateBtn.setVisibility(View.GONE);
                sortByGasolinePriceBtn.setVisibility(View.GONE);
                sortByDieselPriceBtn.setVisibility(View.GONE);
                sortByLpgPriceBtn.setVisibility(View.GONE);
            }
        });

        // 권한 확인 후 초기화
        checkPermission();
    }

    private void checkPermission() {
        if (checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "권한 있음");
            init();
        }
        else {
            Log.d(TAG, "권한 없음. 요청");
            String[] permissionArr = {android.Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
            requestPermissions(permissionArr, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "권한요청에서 허가");
            init();
        }
        else {
            Log.d (TAG, "권한요청에서 거부or문제");
            finish();
        }
    }

    private void init() {
        // 현재 위치 받기
        Location currentLocation = SDKManager.getInstance().getCurrentPosition();
        lat = currentLocation.getLatitude();
        lon = currentLocation.getLongitude();
        nowPoint = new TMapPoint(lat, lon);

        // GPSManager 설정
        gpsManager = new TMapGpsManager(this);
        gpsManager.setMinTime(1000); // ms단위
        gpsManager.setMinDistance(1); // m단위
        gpsManager.setProvider(gpsManager.GPS_PROVIDER);
        gpsManager.OpenGps();
        // 가상머신 말고 실제 기기에선 필요
        /*
        gpsManager.setProvider(gpsManager.NETWORK_PROVIDER);
        gpsManager.OpenGps();
        */

        // TMapView 생성 및 API Key 설정
        tMapView = new TMapView(this);
        LinearLayout linearLayoutTmap = (LinearLayout)findViewById(R.id.linearLayoutTmap);
        linearLayoutTmap.addView( tMapView );

        // TMapView 초기설정
        tMapView.setCenterPoint(lon, lat);
        tMapView.setLocationPoint(lon, lat);
        tMapView.setIcon(tmapMyLocationIcon);
        tMapView.setIconVisibility(true);
        tMapView.setSightVisible(true);

        // TMapCircle 초기설정
        tMapCircle = new TMapCircle();
        tMapCircle.setRadius(3000);
        tMapCircle.setCircleWidth(0);
        tMapCircle.setAreaColor(Color.rgb(0, 0, 0));
        tMapCircle.setAreaAlpha(25);

        // TMapCircle 추가
        tMapView.addTMapCircle("Circle", tMapCircle);

        findGasStation(recievedSort);

        // 버튼 클릭 이벤트 리스너들
        gpsBtn.setOnClickListener(new View.OnClickListener() { // GPS 현재 위치로 맵 중심점 이동
            @Override
            public void onClick(View v) {
                tMapView.setCenterPoint(lon, lat);
                tMapView.setZoomLevel(16);
            }
        });

        zoomOutBtn.setOnClickListener(new View.OnClickListener() { // 맵 축소
            @Override
            public void onClick(View v) {
                if (tMapView.getZoomLevel() >= 8 ) {
                    tMapView.MapZoomOut();
                    Log.d(TAG, "축소, 현재 ZoomLevel : " + tMapView.getZoomLevel());
                }
            }
        });

        zoomInBtn.setOnClickListener(new View.OnClickListener() { // 맵 확대
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
            }
        });

        sortByRateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingStart();
                findGasStation(2);
            }
        });

        sortByGasolinePriceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingStart();
                findGasStation(3);
            }
        });

        sortByDieselPriceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingStart();
                findGasStation(4);
            }
        });

        sortByLpgPriceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingStart();
                findGasStation(5);
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

                // gasStationListView에 clickedGasStation만 있도록
                GasStationAdapter tempGasStationAdapter = new GasStationAdapter();
                tempGasStationAdapter.addItem(clickedGasStation);
                gasStationListView.setAdapter(tempGasStationAdapter);

                naviEndPoint = new TMapPoint (clickedGasStation.getLat(), clickedGasStation.getLon());
                naviEndPointName = clickedGasStation.getName();

                TMapData tmapdata = new TMapData();

                // 길 찾기 및 선 표시
                tmapdata.findPathData(nowPoint, naviEndPoint, new TMapData.FindPathDataListenerCallback() {
                    @Override
                    public void onFindPathData(TMapPolyLine polyLine) {
                        if (selectedMarker != null) {
                            selectedMarker.setIcon(tmapMarkerIcon);
                        }

                        tMapView.setTMapPathIcon(tmapStartMarkerIcon, null);
                        TMapMarkerItem endMarker = tMapView.getMarkerItemFromID(clickedGasStation.getName());
                        endMarker.setIcon(tmapSelectedMarkerIcon);

                        selectedMarker = endMarker;

                        tMapView.addTMapPath(polyLine);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                                float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, displayMetrics);
                                gasStationListView.getLayoutParams().height = (int) px;
                                gasStationListView.requestLayout();

                                TextView gasStationOrder = findViewById(R.id.gasStationOrder);
                                gasStationOrder.setVisibility(View.GONE);

                                TextView gasStationName = findViewById(R.id.gasStationName);
                                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) gasStationName.getLayoutParams();
                                params.leftToLeft = R.id.gasStationItemConstraintLayout;
                                gasStationName.setLayoutParams(params);

                                gasStationSortHorizontalScrollView.setVisibility(View.GONE);
                                cancelNaviBtn.setVisibility(View.VISIBLE);
                                toStartNaviBtn.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                });
            }
        });

        cancelNaviBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tMapView.removeTMapPath();

                        gasStationSortHorizontalScrollView.setVisibility(View.VISIBLE);
                        cancelNaviBtn.setVisibility(View.GONE);
                        toStartNaviBtn.setVisibility(View.GONE);

                        findGasStation(nowSort);
                    }
                });
            }
        });

        toStartNaviBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent naviIntent = new Intent(getApplicationContext(), NavigationActivity.class);
                naviIntent.putExtra("endPointLat", naviEndPoint.getLatitude());
                naviIntent.putExtra("endPointLon", naviEndPoint.getLongitude());
                naviIntent.putExtra("endPointName", naviEndPointName);
                startActivity(naviIntent);
            }
        });

        // 뷰 보이기
        gasStationListView.setVisibility(View.VISIBLE);
        sortByDistanceBtn.setVisibility(View.VISIBLE);
        sortByRateBtn.setVisibility(View.VISIBLE);
        sortByGasolinePriceBtn.setVisibility(View.VISIBLE);
        sortByDieselPriceBtn.setVisibility(View.VISIBLE);
        sortByLpgPriceBtn.setVisibility(View.VISIBLE);
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

                            if (arrayList.size() == 0) {
                                // 디스플레이 매트릭스를 가져옴
                                DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                                gasStationListView.getLayoutParams().height = 0;
                                gasStationListView.requestLayout();
                            }
                            else if(arrayList.size() == 1) {
                                DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                                float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, displayMetrics);
                                gasStationListView.getLayoutParams().height = (int) px;
                                gasStationListView.requestLayout();
                            }
                            else if (arrayList.size() == 2) {
                                DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                                float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200, displayMetrics);
                                gasStationListView.getLayoutParams().height = (int) px;
                                gasStationListView.requestLayout();
                            }
                            else {
                                DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                                float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 240, displayMetrics);
                                gasStationListView.getLayoutParams().height = (int) px;
                                gasStationListView.requestLayout();
                            }
                            TMapPOIItem item = arrayList.get(i);
                            gasStationAdapter.addItem(new GasStationItem(item.name, item.radius, item.hhPrice, item.ggPrice, item.llPrice, item.telNo, item.menu1, 0, item.frontLat, item.frontLon));
                            TMapPoint tpoint = new TMapPoint(Double.parseDouble(item.frontLat), Double.parseDouble(item.frontLon));
                            TMapMarkerItem tItem = new TMapMarkerItem();
                            tItem.setTMapPoint(tpoint);
                            tItem.setName(item.name);
                            tItem.setVisible(TMapMarkerItem.VISIBLE);
                            tItem.setIcon(tmapMarkerIcon);
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
                                    break; case 3 : sortByDistanceBtn.setBackgroundColor(originalBackgroundColor);
                                    sortByRateBtn.setBackgroundColor(originalBackgroundColor);
                                    sortByGasolinePriceBtn.setBackgroundColor(selectedBackgroundColor);
                                    sortByDieselPriceBtn.setBackgroundColor(originalBackgroundColor);
                                    sortByLpgPriceBtn.setBackgroundColor(originalBackgroundColor);
                                    gasStationAdapter.sortByGasolinePrice();
                                    break; case 4 : sortByDistanceBtn.setBackgroundColor(originalBackgroundColor);
                                    sortByRateBtn.setBackgroundColor(originalBackgroundColor);
                                    sortByGasolinePriceBtn.setBackgroundColor(originalBackgroundColor);
                                    sortByDieselPriceBtn.setBackgroundColor(selectedBackgroundColor);
                                    sortByLpgPriceBtn.setBackgroundColor(originalBackgroundColor);
                                    gasStationAdapter.sortByDieselPrice();
                                    break; case 5 : sortByDistanceBtn.setBackgroundColor(originalBackgroundColor);
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
                        nowSort = sortBy;
                        tMapView.removeAllTMapCircle();
                        tMapCircle.setCenterPoint( nowPoint );
                        tMapView.addTMapCircle("Circle", tMapCircle);
                    }
                });
            }
        });
        loadingStop();
    }

    // TMapView 터치이벤트
    @Override
    public boolean onPressEvent(ArrayList<TMapMarkerItem> arrayList, ArrayList<TMapPOIItem> arrayList1, TMapPoint tMapPoint, PointF pointF) {
        if (arrayList.size() > 0) { // TMapMarkerItem이 클릭됬다면
            TMapMarkerItem endMarker = arrayList.get(0); // endMarker = 클릭한 TMapMarkerItem

            GasStationItem clickedGasStation = gasStationAdapter.findItem(endMarker.getName());
            naviEndPoint = endMarker.getTMapPoint();
            naviEndPointName = clickedGasStation.getName();
            TMapData tmapdata = new TMapData();

            Log.d(TAG, "TMapView에서 주유소 마커 클릭함 : " + endMarker.getName() + ", " + naviEndPoint.getLatitude() + ", " + naviEndPoint.getLongitude());

            // gasStationListView에 clickedGasStation만 있도록
            GasStationAdapter tempGasStationAdapter = new GasStationAdapter();
            tempGasStationAdapter.addItem(clickedGasStation);
            gasStationListView.setAdapter(tempGasStationAdapter);

            // 길 찾기 및 선 표시
            tmapdata.findPathData(nowPoint, naviEndPoint, new TMapData.FindPathDataListenerCallback() {
                @Override
                public void onFindPathData(TMapPolyLine polyLine) {
                    if (selectedMarker != null) {
                        selectedMarker.setIcon(tmapMarkerIcon);
                    }

                    tMapView.setTMapPathIcon(tmapStartMarkerIcon, null);
                    endMarker.setIcon(tmapSelectedMarkerIcon);

                    selectedMarker = endMarker;

                    tMapView.addTMapPath(polyLine);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                            float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, displayMetrics);
                            gasStationListView.getLayoutParams().height = (int) px;
                            gasStationListView.requestLayout();

                            TextView gasStationOrder = findViewById(R.id.gasStationOrder);
                            gasStationOrder.setVisibility(View.GONE);

                            TextView gasStationName = findViewById(R.id.gasStationName);
                            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) gasStationName.getLayoutParams();
                            params.leftToLeft = R.id.gasStationItemConstraintLayout;
                            gasStationName.setLayoutParams(params);

                            gasStationSortHorizontalScrollView.setVisibility(View.GONE);
                            cancelNaviBtn.setVisibility(View.VISIBLE);
                            toStartNaviBtn.setVisibility(View.VISIBLE);
                        }
                    });
                }
            });
        }
        return false;
    }

    @Override
    public boolean onPressUpEvent(ArrayList<TMapMarkerItem> arrayList, ArrayList<TMapPOIItem> arrayList1, TMapPoint tMapPoint, PointF pointF) {
        return false;
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
            tMapView.setCenterPoint(lon, lat);
            findGasStation(recievedSort);
            firstOnLocationChangeCalled = true;
        }
    }

    public void loadingStart() {
        Log.d(TAG, "로딩 시작");

        // 로딩 AlertDialog 지정
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("로딩 중").setCancelable(false).setNegativeButton("액티비티 종료", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });
        loadingAlert = builder.create();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        loadingAlert.show();
    }

    public void loadingStop() {
        Log.d(TAG, "로딩 끝");

        loadingAlert.dismiss();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        loadingAlert.dismiss();
    }
}
