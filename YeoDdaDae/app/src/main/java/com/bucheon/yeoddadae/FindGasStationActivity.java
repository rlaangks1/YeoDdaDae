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
import android.widget.EditText;
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
    boolean isItemSelected;
    boolean isLoadingFirstCalled = false;
    int recievedSort;
    int nowSort;
    AlertDialog loadingAlert;
    // 경복궁
    double lat = 37.578611; // 위도
    double lon = 126.977222; // 경도
    TMapPoint nowPoint = new TMapPoint(lat, lon);
    TMapPoint naviEndPoint;
    String naviEndPointName;
    TMapGpsManager gpsManager;
    TMapView tMapView;
    TMapCircle tMapCircle;
    TMapData tMapData;
    TMapMarkerItem selectedMarker;
    GasStationAdapter gasStationAdapter;

    ListView gasStationListView;
    Button findGasStationBackBtn;
    Button zoomOutBtn;
    Button zoomInBtn;
    Button gpsBtn;
    HorizontalScrollView gasStationSortHorizontalScrollView;
    Button sortByDistanceBtn;
    Button sortByRateBtn;
    Button sortByGasolinePriceBtn;
    Button sortByDieselPriceBtn;
    Button sortByHighGasolinePriceBtn;
    Button sortByHighDieselPriceBtn;
    Button cancelNaviBtn;
    Button toStartNaviBtn;

    Bitmap tmapMyLocationIcon;
    Bitmap tmapMarkerIcon;
    Bitmap tmapStartMarkerIcon;
    Bitmap tmapSelectedMarkerIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_gas_station);

        // 메인액티비티에서 정렬기준 받기
        Intent inIntent = getIntent();
        recievedSort = inIntent.getIntExtra("SortBy", 0);
        if (recievedSort == 0) {
            Log.d(TAG, "sttSort가 0임 (오류)");
            finish();
        }

        // 뷰 정의
        gasStationListView = findViewById(R.id.gasStationListView);
        findGasStationBackBtn = findViewById(R.id.findGasStationBackBtn);
        zoomOutBtn = findViewById(R.id.zoomOutBtn);
        zoomInBtn = findViewById(R.id.zoomInBtn);
        gpsBtn = findViewById(R.id.gpsBtn);
        gasStationSortHorizontalScrollView = findViewById(R.id.gasStationSortHorizontalScrollView);
        sortByDistanceBtn = findViewById(R.id.sortByDistanceBtn);
        sortByRateBtn = findViewById(R.id.sortByRateBtn);
        sortByGasolinePriceBtn = findViewById(R.id.sortByGasolinePriceBtn);
        sortByDieselPriceBtn = findViewById(R.id.sortByDieselPriceBtn);
        sortByHighGasolinePriceBtn = findViewById(R.id.sortByHighGasolinePriceBtn);
        sortByHighDieselPriceBtn = findViewById(R.id.sortByHighDieselPriceBtn);
        cancelNaviBtn = findViewById(R.id.cancelNaviBtn);
        toStartNaviBtn = findViewById(R.id.toStartNaviBtn);

        // Bitmap 정의
        tmapMyLocationIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.temp_tmap_my_location);
        tmapMarkerIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.temp_tmap_marker);
        tmapStartMarkerIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.temp_tmap_start_marker);
        tmapSelectedMarkerIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.temp_tmap_selected_marker);

        loadingStart(); // 로딩 시작

        // 로딩 완료까지 뷰 없애기
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                gasStationListView.setVisibility(View.GONE);
                gasStationSortHorizontalScrollView.setVisibility(View.GONE);
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
            Toast.makeText(getApplicationContext(), "GPS 권한이 거부되었습니다", Toast.LENGTH_SHORT).show();
            Log.d (TAG, "권한요청에서 거부or문제");
            finish();
        }
    }

    private void init() { // 초기화
        // 최초 위치 한 번 받기
        Location currentLocation = SDKManager.getInstance().getCurrentPosition();
        lat = currentLocation.getLatitude();
        lon = currentLocation.getLongitude();
        nowPoint = new TMapPoint(lat, lon);

        // TMapGpsManager 설정
        gpsManager = new TMapGpsManager(this);
        gpsManager.setMinTime(1000); // ms단위
        gpsManager.setMinDistance(1); // m단위
        gpsManager.setProvider(gpsManager.GPS_PROVIDER);
        gpsManager.OpenGps();
        /* 가상머신 말고 실제 기기로 실내에서 사용 시 필요
        gpsManager.setProvider(gpsManager.NETWORK_PROVIDER);
        gpsManager.OpenGps();
        */

        // TMapView 생성 및 보이기
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
        tMapView.addTMapCircle("Circle", tMapCircle);

        findGasStation(recievedSort);

        // 버튼 클릭 이벤트 리스너들
        findGasStationBackBtn.setOnClickListener(new View.OnClickListener() { // 액티비티 종료
            @Override
            public void onClick(View v) {
                finish();
            }
        });

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
                findGasStation(1);
            }
        });

        sortByRateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findGasStation(2);
            }
        });

        sortByGasolinePriceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findGasStation(3);
            }
        });

        sortByDieselPriceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findGasStation(4);
            }
        });

        sortByHighGasolinePriceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findGasStation(5);
            }
        });

        sortByHighDieselPriceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findGasStation(6);
            }
        });

        gasStationListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!isItemSelected) {
                    // 클릭한 GasStationItem을 가져옴
                    GasStationItem clickedGasStation = (GasStationItem) parent.getItemAtPosition(position);

                    Log.d(TAG, "리스트뷰에서 주유소 아이템 클릭함 : " + clickedGasStation.getName() + ", " + clickedGasStation.getRadius() + ", " + clickedGasStation.getGasolinePrice()
                            + ", " +  clickedGasStation.getDieselPrice() + ", " +  clickedGasStation.getPhone() + ", " + clickedGasStation.getAddition()
                            + ", " + clickedGasStation.getStarRate() + ", " + clickedGasStation.getLat() + ", " + clickedGasStation.getLon());

                    // gasStationListView에 clickedGasStation만 있도록
                    GasStationAdapter tempGasStationAdapter = new GasStationAdapter();
                    tempGasStationAdapter.addItem(clickedGasStation);
                    gasStationListView.setAdapter(tempGasStationAdapter);

                    // 도착점 설정
                    naviEndPoint = new TMapPoint (clickedGasStation.getLat(), clickedGasStation.getLon());
                    naviEndPointName = clickedGasStation.getName();

                    // 길 찾기 및 선 표시
                    TMapData tmapdata = new TMapData();
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
                                    tMapView.setCenterPoint(selectedMarker.longitude, selectedMarker.latitude);

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

                                    isItemSelected = true;
                                }
                            });
                        }
                    });
                }
            }
        });

        cancelNaviBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        isItemSelected = false;
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
        gasStationSortHorizontalScrollView.setVisibility(View.VISIBLE);
    }

    public void findGasStation(int sortBy) { // sortBy는 정렬기준 (1:거리순, 2:평점순, 3:휘발유가순, 4: 경유가순
        Log.d (TAG, "findGasStation 시작");

        loadingStart();

        tMapData = new TMapData();
        tMapData.findAroundNamePOI(nowPoint, "주유소", 3, 200, new TMapData.FindAroundNamePOIListenerCallback () {
            @Override
            public void onFindAroundNamePOI(ArrayList<TMapPOIItem> arrayList) {
                if (arrayList == null) {
                    loadingStop();
                    return;
                }
                tMapView.removeAllTMapCircle();
                tMapView.removeAllMarkerItem();
                gasStationAdapter = new GasStationAdapter();

                for (int i = 0; i < arrayList.size(); i++) {
                    TMapPOIItem item = arrayList.get(i);

                    Log.d(TAG,
                            "=========="+ "\n"
                                    +item.id+ "\n"
                                    +item.name+ "\n"
                                    +item.telNo+ "\n"
                                    +item.frontLat+ "\n"
                                    +item.frontLon+ "\n"
                                    +item.noorLat+ "\n"
                                    +item.noorLon+ "\n"
                                    +item.upperAddrName+ "\n"
                                    +item.middleAddrName+ "\n"
                                    +item.lowerAddrName+ "\n"
                                    +item.detailAddrName+ "\n"
                                    +item.firstNo+ "\n"
                                    +item.secondNo+ "\n"
                                    +item.upperBizName+ "\n"
                                    +item.middleBizName+ "\n"
                                    +item.lowerBizName+ "\n"
                                    +item.detailBizName+ "\n"
                                    +item.rpFlag+ "\n"
                                    +item.parkFlag+ "\n"
                                    +item.detailInfoFlag+ "\n"
                                    +item.desc+ "\n"
                                    +item.distance+ "\n"
                                    +item.roadName+ "\n"
                                    +item.buildingNo1+ "\n"
                                    +item.buildingNo2+ "\n"
                                    +item.merchanFlag+ "\n"
                                    +item.radius+ "\n"
                                    +item.pkey+ "\n"
                                    +item.navSeq+ "\n"
                                    +item.collectionType+ "\n"
                                    +item.firstBuildNo+ "\n"
                                    +item.secondBuildNo+ "\n"
                                    +item.bizName+ "\n"
                                    +item.dataKind+ "\n"
                                    +item.stId+ "\n"
                                    +item.highHhSale+ "\n"
                                    +item.minOilYn+ "\n"
                                    +item.oilBaseSdt+ "\n"
                                    +item.hhPrice+ "\n"
                                    +item.ggPrice+ "\n"
                                    +item.llPrice+ "\n"
                                    +item.highHhPrice+ "\n"
                                    +item.highGgPrice+ "\n"
                                    +item.viewId+ "\n"
                                    +item.dbKind+ "\n"
                                    +item.lcdName+ "\n"
                                    +item.mcdName+ "\n"
                                    +item.scdName+ "\n"
                                    +item.dcdName+ "\n"
                                    +item.bldAddr+ "\n"
                                    +item.roadScdName+ "\n"
                                    +item.bldNo1+ "\n"
                                    +item.bldNo2+ "\n"
                                    +item.menu1+ "\n"
                                    +item.menu2+ "\n"
                                    +item.menu3+ "\n"
                                    +item.menu4+ "\n"
                                    +item.menu5+ "\n"
                                    +item.twFlag+ "\n"
                                    +item.yaFlag+ "\n"
                                    +item.facility+ "\n"
                                    +item.upperLegalCode+ "\n"
                                    +item.middleLegalCode+ "\n"
                                    +item.lowerLegalCode+ "\n"
                                    +item.detailLegalCode+ "\n"
                                    +item.upperAdminCode+ "\n"
                                    +item.middleAdminCode+ "\n"
                                    +item.lowerAdminCode+ "\n"
                                    +item.upperCode+ "\n"
                                    +item.middleCode+ "\n"
                                    +item.lowerCode+ "\n"
                                    +item.participant+ "\n"
                                    +item.point+ "\n"
                                    +item.merchantFlag+ "\n"
                                    +item.merchantDispType+ "\n"
                                    +item.mngName+ "\n"
                                    +item.mngId+ "\n"
                                    +item.freeYn+ "\n"
                                    +item.reservYn+ "\n"
                                    +item.useTime+ "\n"
                                    +item.payYn+ "\n"
                                    +item.fee+ "\n"
                                    +item.updateDt+ "\n"
                                    +item.totalCnt+ "\n"
                                    + "==========");


                    gasStationAdapter.addItem(new GasStationItem(item.name, item.radius, item.hhPrice, item.ggPrice, item.highHhPrice, item.highGgPrice, item.telNo, item.stId, item.menu1, 0, item.frontLat, item.frontLon));
                    TMapPoint tpoint = new TMapPoint(Double.parseDouble(item.frontLat), Double.parseDouble(item.frontLon));
                    TMapMarkerItem tItem = new TMapMarkerItem();
                    tItem.setTMapPoint(tpoint);
                    tItem.setName(item.name);
                    tItem.setVisible(TMapMarkerItem.VISIBLE);
                    tItem.setIcon(tmapMarkerIcon);
                    tItem.setPosition(0.5f,1.0f); // 마커의 중심점을 하단, 중앙으로 설정
                    tMapView.addMarkerItem(item.name, tItem);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        gasStationListView.setAdapter(gasStationAdapter);

                        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                        float px;

                        switch (arrayList.size()) {
                            case 0:
                                px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, displayMetrics);
                                break;
                            case 1:
                                px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, displayMetrics);
                                break;
                            case 2:
                                px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200, displayMetrics);
                                break;
                            default:
                                px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 240, displayMetrics);
                        }

                        gasStationListView.getLayoutParams().height = (int) px;
                        gasStationListView.requestLayout();

                        int originalBackgroundColor = Color.rgb(128,128,128);
                        int selectedBackgroundColor = Color.rgb(0,0,255);

                        switch (sortBy) {
                            case 1 :
                                sortByDistanceBtn.setBackgroundColor(selectedBackgroundColor);
                                sortByRateBtn.setBackgroundColor(originalBackgroundColor);
                                sortByGasolinePriceBtn.setBackgroundColor(originalBackgroundColor);
                                sortByDieselPriceBtn.setBackgroundColor(originalBackgroundColor);
                                sortByHighGasolinePriceBtn.setBackgroundColor(originalBackgroundColor);
                                sortByHighDieselPriceBtn.setBackgroundColor(originalBackgroundColor);
                                break;
                            case 2 :
                                sortByDistanceBtn.setBackgroundColor(originalBackgroundColor);
                                sortByRateBtn.setBackgroundColor(selectedBackgroundColor);
                                sortByGasolinePriceBtn.setBackgroundColor(originalBackgroundColor);
                                sortByDieselPriceBtn.setBackgroundColor(originalBackgroundColor);
                                sortByHighGasolinePriceBtn.setBackgroundColor(originalBackgroundColor);
                                sortByHighDieselPriceBtn.setBackgroundColor(originalBackgroundColor);
                                gasStationAdapter.sortByRate();
                                break;
                            case 3 :
                                sortByDistanceBtn.setBackgroundColor(originalBackgroundColor);
                                sortByRateBtn.setBackgroundColor(originalBackgroundColor);
                                sortByGasolinePriceBtn.setBackgroundColor(selectedBackgroundColor);
                                sortByDieselPriceBtn.setBackgroundColor(originalBackgroundColor);
                                sortByHighGasolinePriceBtn.setBackgroundColor(originalBackgroundColor);
                                sortByHighDieselPriceBtn.setBackgroundColor(originalBackgroundColor);
                                gasStationAdapter.sortByGasolinePrice();
                                break;
                            case 4 :
                                sortByDistanceBtn.setBackgroundColor(originalBackgroundColor);
                                sortByRateBtn.setBackgroundColor(originalBackgroundColor);
                                sortByGasolinePriceBtn.setBackgroundColor(originalBackgroundColor);
                                sortByDieselPriceBtn.setBackgroundColor(selectedBackgroundColor);
                                sortByHighGasolinePriceBtn.setBackgroundColor(originalBackgroundColor);
                                sortByHighDieselPriceBtn.setBackgroundColor(originalBackgroundColor);
                                gasStationAdapter.sortByDieselPrice();
                                break;
                            case 5 :
                                sortByDistanceBtn.setBackgroundColor(originalBackgroundColor);
                                sortByRateBtn.setBackgroundColor(originalBackgroundColor);
                                sortByGasolinePriceBtn.setBackgroundColor(originalBackgroundColor);
                                sortByDieselPriceBtn.setBackgroundColor(originalBackgroundColor);
                                sortByHighGasolinePriceBtn.setBackgroundColor(selectedBackgroundColor);
                                sortByHighDieselPriceBtn.setBackgroundColor(originalBackgroundColor);
                                gasStationAdapter.sortByHighGasolinePrice();
                                break;
                            case 6 :
                                sortByDistanceBtn.setBackgroundColor(originalBackgroundColor);
                                sortByRateBtn.setBackgroundColor(originalBackgroundColor);
                                sortByGasolinePriceBtn.setBackgroundColor(originalBackgroundColor);
                                sortByDieselPriceBtn.setBackgroundColor(originalBackgroundColor);
                                sortByHighGasolinePriceBtn.setBackgroundColor(originalBackgroundColor);
                                sortByHighDieselPriceBtn.setBackgroundColor(selectedBackgroundColor);
                                gasStationAdapter.sortByHighDieselPrice();
                                break;

                        }
                        gasStationListView.setVisibility(View.VISIBLE);
                        gasStationSortHorizontalScrollView.setVisibility(View.VISIBLE);
                    }
                });
                tMapCircle.setCenterPoint( nowPoint );
                tMapView.addTMapCircle("Circle", tMapCircle);

                nowSort = sortBy;

                loadingStop();
            }
        });
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
                            tMapView.setCenterPoint(selectedMarker.longitude, selectedMarker.latitude);

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

                            isItemSelected = true;
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

        if (!isLoadingFirstCalled) { // 로딩 AlertDialog 지정
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("로딩 중").setCancelable(false).setNegativeButton("액티비티 종료", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    finish();
                }
            });
            loadingAlert = builder.create();
            isLoadingFirstCalled = true;
        }

        if (loadingAlert != null && loadingAlert.isShowing()) {
            return; // 이미 보여지고 있다면 함수 종료
        }

        if (!isFinishing()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadingAlert.show();
                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                }
            });
        }
    }

    public void loadingStop() {
        Log.d(TAG, "로딩 끝");

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (loadingAlert != null && loadingAlert.isShowing()) {
                    loadingAlert.dismiss();
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadingAlert.dismiss();
            }
        });
    }
}
