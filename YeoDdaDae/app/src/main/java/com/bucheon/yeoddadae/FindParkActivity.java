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
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.exoplayer2.ExoPlayerLibraryInfo;
import com.skt.Tmap.TMapCircle;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapTapi;
import com.skt.Tmap.TMapView;
import com.skt.Tmap.poi_item.TMapPOIItem;
import com.skt.tmap.engine.navigation.SDKManager;

import java.util.ArrayList;

public class FindParkActivity extends AppCompatActivity implements TMapGpsManager.onLocationChangedCallback, TMapView.OnClickListenerCallback {
    String loginId;
    private static final int PERMISSION_REQUEST_CODE = 1;
    boolean firstOnLocationChangeCalled = false; // onLocationChange가 처음 불림 여부
    boolean isItemSelected;
    boolean isLoadingFirstCalled = false;
    int recievedSort;
    int nowSort;
    int clickParkType;
    AlertDialog loadingAlert;
    // 경복궁
    double lat = 37.578611; // 위도
    double lon = 126.977222; // 경도
    TMapPoint nowPoint = new TMapPoint(lat, lon);
    TMapPoint naviEndPoint;
    String naviEndPointName;
    String reservationFirestoreDocumentId;
    TMapGpsManager gpsManager;
    TMapView tMapView;
    TMapCircle tMapCircle;
    TMapData tMapData;
    TMapMarkerItem selectedMarker;
    ParkAdapter parkAdapter;

    ListView parkListView;
    ImageButton findParkBackBtn;
    ImageButton zoomOutBtn;
    ImageButton zoomInBtn;
    ImageButton gpsBtn;
    HorizontalScrollView parkSortHorizontalScrollView;
    Button sortByDistanceBtn;
    Button sortByRateBtn;
    Button sortByParkPriceBtn;
    Button cancelNaviBtn;
    Button toStartNaviBtn;
    Button toReservationBtn;
    ImageButton searchStartBtn;
    ConstraintLayout searchConstraintLayout;
    EditText searchEdTxt;
    Button searchBtn;
    Button searchBackBtn;
    ListView searchListView;

    Bitmap tmapMyLocationIcon;
    Bitmap tmapMarkerIcon;
    Bitmap tmapStartMarkerIcon;
    Bitmap tmapSelectedMarkerIcon;
    Bitmap tmapShareParkMarkerIcon;
    Bitmap tmapSelectedShareParkMarkerIcon;
    Bitmap tmapSearchPlaceMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_park);
  
        // 뷰 정의
        parkListView = findViewById(R.id.parkListView);
        findParkBackBtn = findViewById(R.id.findParkBackBtn);
        zoomOutBtn = findViewById(R.id.zoomOutBtn);
        zoomInBtn = findViewById(R.id.zoomInBtn);
        gpsBtn = findViewById(R.id.gpsBtn);
        parkSortHorizontalScrollView = findViewById(R.id.parkSortHorizontalScrollView);
        sortByDistanceBtn = findViewById(R.id.sortByDistanceBtn);
        sortByRateBtn = findViewById(R.id.sortByRateBtn);
        sortByParkPriceBtn = findViewById(R.id.sortByParkPriceBtn);
        cancelNaviBtn = findViewById(R.id.cancelNaviBtn);
        toStartNaviBtn = findViewById(R.id.toStartNaviBtn);
        toReservationBtn = findViewById(R.id.toReservationBtn);
        searchStartBtn = findViewById(R.id.searchStartBtn);
        searchConstraintLayout = findViewById(R.id.searchConstraintLayout);
        searchEdTxt = findViewById(R.id.searchEdTxt);
        searchBtn = findViewById(R.id.searchBtn);
        searchBackBtn = findViewById(R.id.searchBackBtn);
        searchListView = findViewById(R.id.searchListView);
        
        // Bitmap 정의
        tmapMyLocationIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.temp_tmap_my_location);
        tmapMarkerIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.temp_tmap_marker);
        tmapSelectedMarkerIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.temp_tmap_selected_marker);
        tmapStartMarkerIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.temp_tmap_start_marker);
        tmapShareParkMarkerIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.temp_tmap_share_park_marker);
        tmapSelectedShareParkMarkerIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.temp_tmap_selected_share_park_marker);
        tmapSearchPlaceMarker = BitmapFactory.decodeResource(this.getResources(), R.drawable.temp_tmap_search_place_marker);

        loadingStart(); // 로딩 시작

        // 메인액티비티에서 정렬기준 받기
        Intent inIntent = getIntent();
        recievedSort = inIntent.getIntExtra("SortBy", 0);
        loginId = inIntent.getStringExtra("loginId");
        if (recievedSort == 0) {
            Log.d(TAG, "sttSort가 0임 (오류)");
            finish();
        }
        if (loginId == null || loginId.equals("")) {
            Log.d(TAG, "loginId가 전달되지 않음 (오류)");
            finish();
        }

        findParkBackBtn.setOnClickListener(new View.OnClickListener() { // 액티비티 종료
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // 로딩 완료까지 뷰 없애기
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                parkListView.setVisibility(View.GONE);
                parkSortHorizontalScrollView.setVisibility(View.GONE);
            }
        });

        // 권한 확인 후 초기화
        checkPermission();
    }

    private void checkPermission() {
        if (checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "권한 이미 있음");
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
        gpsManager.setMinTime(500); // ms단위
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

        // TMapCircle 초기설정
        tMapCircle = new TMapCircle();
        tMapCircle.setRadius(3000);
        tMapCircle.setCircleWidth(0);
        tMapCircle.setAreaColor(Color.rgb(0, 0, 0));
        tMapCircle.setAreaAlpha(25);
        tMapView.addTMapCircle("Circle", tMapCircle);

        findPark(recievedSort);

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
                findPark(1);
            }
        });

        sortByRateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findPark(2);
            }
        });

        sortByParkPriceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findPark(3);
            }
        });

        cancelNaviBtn.setOnClickListener(new View.OnClickListener() { // 네비게이션 취소
            @Override
            public void onClick(View v) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        isItemSelected = false;
                        tMapView.removeTMapPath();

                        parkSortHorizontalScrollView.setVisibility(View.VISIBLE);
                        searchStartBtn.setVisibility(View.VISIBLE);
                        cancelNaviBtn.setVisibility(View.GONE);
                        toStartNaviBtn.setVisibility(View.GONE);
                        toReservationBtn.setVisibility(View.GONE);

                        findPark(nowSort);
                    }
                });
            }
        });

        toStartNaviBtn.setOnClickListener(new View.OnClickListener() { // 네비게이션 시작
            @Override
            public void onClick(View v) {
                TMapTapi tt = new TMapTapi(FindParkActivity.this);
                boolean isTmapApp = tt.isTmapApplicationInstalled();
                if (isTmapApp) {
                    tt.invokeRoute(naviEndPointName, (float) naviEndPoint.getLongitude(), (float) naviEndPoint.getLatitude());
                }
                else {
                    Toast.makeText(getApplicationContext(), "TMAP이 설치되어 있지 않습니다", Toast.LENGTH_SHORT).show();
                }
            }
        });

        toReservationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent reservationIntent = new Intent(getApplicationContext(), ReservationParkActivity.class);
                reservationIntent.putExtra("loginId", loginId);
                reservationIntent.putExtra("fireStoreDocumentId", reservationFirestoreDocumentId);
                startActivity(reservationIntent);
            }
        });

        searchStartBtn.setOnClickListener(new View.OnClickListener() { // 검색창 띄우기
            @Override
            public void onClick(View v) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        searchConstraintLayout.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
        searchBtn.setOnClickListener(new View.OnClickListener() { // 검색 시작
            @Override
            public void onClick(View v) {
                if (!searchEdTxt.getText().toString().equals("")) {
                    tMapData = new TMapData();

                    tMapData.findAllPOI(searchEdTxt.getText().toString(), new TMapData.FindAllPOIListenerCallback() {
                        @Override
                        public void onFindAllPOI(ArrayList<TMapPOIItem> arrayList) {
                            if (arrayList == null) {
                                return;
                            }
                            SearchParkAdapter spa = new SearchParkAdapter();

                            for (int i = 0; i < arrayList.size(); i++) {
                                TMapPOIItem item = arrayList.get(i);

                                if (item.firstNo.equals("0") && item.secondNo.equals("0")) {
                                    spa.addItem(new ParkItem(4, item.name, item.radius, null, item.telNo, null, 0, item.frontLat, item.frontLon, item.id, null));
                                }
                                else {
                                    if (item.name.contains("주차")) {
                                        if (item.name.contains("공영")) {
                                            spa.addItem(new ParkItem(2, item.name, item.radius, null, item.telNo, null, 0, item.frontLat, item.frontLon, item.id, null));
                                        }
                                        else {
                                            spa.addItem(new ParkItem(1, item.name, item.radius, null, item.telNo, null, 0, item.frontLat, item.frontLon, item.id, null));
                                        }
                                    }
                                    else {
                                        spa.addItem(new ParkItem(5, item.name, item.radius, null, item.telNo, null, 0, item.frontLat, item.frontLon, item.id, null));
                                    }
                                }
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    searchListView.setAdapter(spa);
                                }
                            });
                        }
                    });
                }
            }
        });

        searchBackBtn.setOnClickListener(new View.OnClickListener() { // 검색창 닫기
            @Override
            public void onClick(View v) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        searchConstraintLayout.setVisibility(View.GONE);
                    }
                });
            }
        });

        parkListView.setOnItemClickListener(new AdapterView.OnItemClickListener() { // 주차장 리스트뷰 아이템 클릭
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "sdsdsdsdsdss");
                if (!isItemSelected) {
                    // 클릭한 ParkItem을 가져옴
                    ParkItem clickedPark = (ParkItem) parent.getItemAtPosition(position);

                    Log.d(TAG, "리스트뷰에서 주유소 아이템 클릭함 : " + clickedPark.getType()  + ", " + clickedPark.getName()
                            + ", " + clickedPark.getRadius() + ", " + clickedPark.getParkPrice() + ", " + clickedPark.getPhone()
                            + ", " + clickedPark.getAddition() + ", " + clickedPark.getStarRate() + ", " + clickedPark.getLat()
                            + ", " + clickedPark.getLon());

                    // parkListView에 clickedPark만 있도록
                    ParkAdapter pa = new ParkAdapter();
                    pa.addItem(clickedPark);
                    parkListView.setAdapter(pa);

                    // 도착점 설정
                    naviEndPoint = new TMapPoint (clickedPark.getLat(), clickedPark.getLon());
                    naviEndPointName = clickedPark.getName();

                    // 길 찾기 및 선 표시
                    TMapData tmapdata = new TMapData();
                    tmapdata.findPathData(nowPoint, naviEndPoint, new TMapData.FindPathDataListenerCallback() {
                        @Override
                        public void onFindPathData(TMapPolyLine polyLine) {
                            if (selectedMarker != null) {
                                if (clickedPark.getType() == 3) {
                                    selectedMarker.setIcon(tmapShareParkMarkerIcon);
                                }
                                else {
                                    selectedMarker.setIcon(tmapMarkerIcon);
                                }
                            }

                            tMapView.setTMapPathIcon(tmapStartMarkerIcon, null);
                            TMapMarkerItem endMarker = tMapView.getMarkerItemFromID(clickedPark.getName());
                            if (clickedPark.getType() == 3) {
                                endMarker.setIcon(tmapSelectedShareParkMarkerIcon);
                            }
                            else {
                                endMarker.setIcon(tmapSelectedMarkerIcon);
                            }
                            tMapView.bringMarkerToFront(endMarker);

                            selectedMarker = endMarker;

                            tMapView.addTMapPath(polyLine);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tMapView.setCenterPoint(selectedMarker.longitude, selectedMarker.latitude);

                                    DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                                    float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, displayMetrics);
                                    parkListView.getLayoutParams().height = (int) px;
                                    parkListView.requestLayout();

                                    TextView parkOrder = findViewById(R.id.parkOrder);
                                    parkOrder.setVisibility(View.GONE);

                                    TextView parkName = findViewById(R.id.parkName);
                                    ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) parkName.getLayoutParams();
                                    params.leftToLeft = R.id.parkItemConstraintLayout;
                                    parkName.setLayoutParams(params);

                                    parkSortHorizontalScrollView.setVisibility(View.GONE);
                                    searchStartBtn.setVisibility(View.GONE);
                                    cancelNaviBtn.setVisibility(View.VISIBLE);
                                    toStartNaviBtn.setVisibility(View.VISIBLE);
                                    if (clickedPark.getType() == 3) {
                                        reservationFirestoreDocumentId = clickedPark.getFirebaseDocumentId();
                                        toReservationBtn.setVisibility(View.VISIBLE);
                                    }
                                    else {
                                        toReservationBtn.setVisibility(View.GONE);
                                    }

                                    isItemSelected = true;
                                }
                            });
                        }
                    });
                }
            }
        });

        searchListView.setOnItemClickListener(new AdapterView.OnItemClickListener() { // 주소,장소 검색 리스트뷰 아이템 클릭
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 클릭한 ParkItem을 가져옴
                ParkItem clickedPark = (ParkItem) parent.getItemAtPosition(position);

                Log.d(TAG, "검색 리스트뷰에서 아이템 클릭함 : " + clickedPark.getType()  + ", " + clickedPark.getName()
                        + ", " + clickedPark.getRadius() + ", " + clickedPark.getParkPrice() + ", " + clickedPark.getPhone()
                        + ", " + clickedPark.getAddition() + ", " + clickedPark.getStarRate() + ", " + clickedPark.getLat()
                        + ", " + clickedPark.getLon());

                // 키보드 내리기
                InputMethodManager manager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (manager != null && getCurrentFocus() != null && getCurrentFocus().getWindowToken() != null) {
                    manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }

                tMapView.setCenterPoint(clickedPark.getLon(), clickedPark.getLat());
                findPark(nowSort);

                if (clickedPark.getType() == 1 || clickedPark.getType() == 2 || clickedPark.getType() == 3) {
                    Log.d(TAG, "검색 리스트뷰에서 주유소 아이템 클릭함 : " + clickedPark.getType()  + ", " + clickedPark.getName()
                            + ", " + clickedPark.getRadius() + ", " + clickedPark.getParkPrice() + ", " + clickedPark.getPhone()
                            + ", " + clickedPark.getAddition() + ", " + clickedPark.getStarRate() + ", " + clickedPark.getLat()
                            + ", " + clickedPark.getLon());

                    // parkListView에 clickedPark만 있도록
                    ParkAdapter pa = new ParkAdapter();
                    pa.addItem(clickedPark);
                    parkListView.setAdapter(pa);

                    // 도착점 설정
                    naviEndPoint = new TMapPoint (clickedPark.getLat(), clickedPark.getLon());
                    naviEndPointName = clickedPark.getName();

                    // 길 찾기 및 선 표시
                    TMapData tmapdata = new TMapData();
                    tmapdata.findPathData(nowPoint, naviEndPoint, new TMapData.FindPathDataListenerCallback() {
                        @Override
                        public void onFindPathData(TMapPolyLine polyLine) {
                            if (selectedMarker != null) {
                                if (clickedPark.getType() == 3) {
                                    selectedMarker.setIcon(tmapShareParkMarkerIcon);
                                }
                                else {
                                    selectedMarker.setIcon(tmapMarkerIcon);
                                }
                            }

                            tMapView.setTMapPathIcon(tmapStartMarkerIcon, null);
                            tMapView.addMarkerItem(clickedPark.getName(), new TMapMarkerItem());
                            TMapMarkerItem endMarker = tMapView.getMarkerItemFromID(clickedPark.getName());
                            if (clickedPark.getType() == 3) {
                                endMarker.setIcon(tmapSelectedShareParkMarkerIcon);
                            }
                            else {
                                endMarker.setIcon(tmapSelectedMarkerIcon);
                            }

                            tMapView.bringMarkerToFront(endMarker);
                            selectedMarker = endMarker;

                            tMapView.addTMapPath(polyLine);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tMapView.setCenterPoint(selectedMarker.longitude, selectedMarker.latitude);

                                    DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                                    float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, displayMetrics);
                                    parkListView.getLayoutParams().height = (int) px;
                                    parkListView.requestLayout();

                                    TextView parkOrder = findViewById(R.id.parkOrder);
                                    parkOrder.setVisibility(View.GONE);

                                    TextView parkName = findViewById(R.id.parkName);
                                    ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) parkName.getLayoutParams();
                                    params.leftToLeft = R.id.parkItemConstraintLayout;
                                    parkName.setLayoutParams(params);

                                    parkSortHorizontalScrollView.setVisibility(View.GONE);
                                    searchStartBtn.setVisibility(View.GONE);
                                    cancelNaviBtn.setVisibility(View.VISIBLE);
                                    toStartNaviBtn.setVisibility(View.VISIBLE);
                                    if (clickedPark.getType() == 3) {
                                        reservationFirestoreDocumentId = clickedPark.getFirebaseDocumentId();
                                        toReservationBtn.setVisibility(View.VISIBLE);
                                    }
                                    else {
                                        toReservationBtn.setVisibility(View.GONE);
                                    }
                                }
                            });
                        }
                    });
                }

                if (clickedPark.getType() == 4) {
                    // 주소
                }
                else if (clickedPark.getType() == 5){
                    // 장소
                    TMapPoint tpoint = new TMapPoint(clickedPark.getLat(), clickedPark.getLon());
                    TMapMarkerItem tItem = new TMapMarkerItem();
                    tItem.setTMapPoint(tpoint);
                    tItem.setName(clickedPark.getName());
                    tItem.setVisible(TMapMarkerItem.VISIBLE);
                    tItem.setIcon(tmapSearchPlaceMarker);
                    tItem.setPosition(0.5f,1.0f); // 마커의 중심점을 하단, 중앙으로 설정
                    tMapView.addMarkerItem(clickedPark.getName(), tItem);
                    tMapView.setZoomLevel(13);
                }
                searchConstraintLayout.setVisibility(View.GONE);
            }
        });
    } // init 끝

    public void findPark(int sortBy) { // sortBy는 정렬기준 (1:거리순, 2:평점순, 3:주차가순
        Log.d (TAG, "findPark 시작");
        loadingStart();
        TMapPoint centerPoint = tMapView.getCenterPoint();

        // 보통 주차장 찾기
        tMapData = new TMapData();
        tMapData.findAroundNamePOI(centerPoint, "주차장", 3, 200, new TMapData.FindAroundNamePOIListenerCallback ()
        {
            @Override
            public void onFindAroundNamePOI(ArrayList<TMapPOIItem> arrayList) {
                if (arrayList == null) {
                    loadingStop();
                    return;
                }
                FirestoreDatabase fd = new FirestoreDatabase();
                fd.findSharePark(loginId, centerPoint.getLatitude(), centerPoint.getLongitude(), 3, new OnFirestoreDataLoadedListener() {
                    @Override
                    public void onDataLoaded(Object data) {
                        tMapView.removeAllTMapCircle();
                        tMapView.removeAllMarkerItem();
                        parkAdapter = new ParkAdapter();

                        for (int i = 0; i < arrayList.size(); i++) { // TMAP 검색
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

                            if (item.name.contains("공영")) {
                                parkAdapter.addItem(new ParkItem(2, item.name, item.radius, item.fee, item.telNo, item.additionalInfo, 0, item.frontLat, item.frontLon, item.id, null));
                            }
                            else {
                                parkAdapter.addItem(new ParkItem(1, item.name, item.radius, item.fee, item.telNo, item.additionalInfo, 0, item.frontLat, item.frontLon, item.id, null));
                            }
                            TMapPoint tpoint = new TMapPoint(Double.parseDouble(item.frontLat), Double.parseDouble(item.frontLon));
                            TMapMarkerItem tItem = new TMapMarkerItem();
                            tItem.setTMapPoint(tpoint);
                            tItem.setName(item.name);
                            tItem.setVisible(TMapMarkerItem.VISIBLE);
                            tItem.setIcon(tmapMarkerIcon);
                            tItem.setPosition(0.5f,1.0f); // 마커의 중심점을 하단, 중앙으로 설정
                            tMapView.addMarkerItem(item.name, tItem);
                        }

                        if (data != null) {
                            ArrayList<ParkItem> dataList = (ArrayList<ParkItem>) data;

                            for (ParkItem item : dataList) { // Firestore 검색
                                parkAdapter.addItem(item);
                                TMapPoint tpoint = new TMapPoint(item.getLat(), item.getLon());
                                TMapMarkerItem tItem = new TMapMarkerItem();
                                tItem.setTMapPoint(tpoint);
                                tItem.setName(item.getName());
                                tItem.setVisible(TMapMarkerItem.VISIBLE);
                                tItem.setIcon(tmapShareParkMarkerIcon);
                                tItem.setPosition(0.5f,1.0f); // 마커의 중심점을 하단, 중앙으로 설정
                                tMapView.addMarkerItem(item.getName(), tItem);
                            }
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                parkListView.setAdapter(parkAdapter);

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

                                parkListView.getLayoutParams().height = (int) px;
                                parkListView.requestLayout();

                                int originalBackgroundColor = Color.rgb(128,128,128);
                                int selectedBackgroundColor = Color.rgb(0,0,255);

                                switch (sortBy) {
                                    case 1 :
                                        sortByDistanceBtn.setBackgroundColor(selectedBackgroundColor);
                                        sortByRateBtn.setBackgroundColor(originalBackgroundColor);
                                        sortByParkPriceBtn.setBackgroundColor(originalBackgroundColor);
                                        parkAdapter.sortByDistance();
                                        break;
                                    case 2 :
                                        sortByDistanceBtn.setBackgroundColor(originalBackgroundColor);
                                        sortByRateBtn.setBackgroundColor(selectedBackgroundColor);
                                        sortByParkPriceBtn.setBackgroundColor(originalBackgroundColor);
                                        parkAdapter.sortByRate();
                                        break;
                                    case 3 :
                                        sortByDistanceBtn.setBackgroundColor(originalBackgroundColor);
                                        sortByRateBtn.setBackgroundColor(originalBackgroundColor);
                                        sortByParkPriceBtn.setBackgroundColor(selectedBackgroundColor);
                                        parkAdapter.sortByParkPrice();
                                        break;
                                }

                                parkListView.setVisibility(View.VISIBLE);
                                parkSortHorizontalScrollView.setVisibility(View.VISIBLE);
                            }
                        });
                        tMapCircle.setCenterPoint(centerPoint);
                        tMapView.addTMapCircle("Circle", tMapCircle);

                        nowSort = sortBy;

                        loadingStop();
                    }

                    @Override
                    public void onDataLoadError(String errorMessage) {
                        Log.d(TAG, errorMessage);
                        Toast.makeText(getApplicationContext(), "오류 발생", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }

    // TMapView 터치이벤트
    @Override
    public boolean onPressEvent(ArrayList<TMapMarkerItem> arrayList, ArrayList<TMapPOIItem> arrayList1, TMapPoint tMapPoint, PointF pointF) {
        if (arrayList.size() > 0) { // TMapMarkerItem이 클릭됬다면
            TMapMarkerItem endMarker = arrayList.get(0); // endMarker = 클릭한 TMapMarkerItem

            ParkItem clickedPark = parkAdapter.findItem(endMarker.getName());
            if (clickedPark == null) {
                return false;
            }
            naviEndPoint = endMarker.getTMapPoint();
            naviEndPointName = clickedPark.getName();
            TMapData tmapdata = new TMapData();

            Log.d(TAG, "TMapView에서 주유소 마커 클릭함 : " + endMarker.getName() + ", " + naviEndPoint.getLatitude() + ", " + naviEndPoint.getLongitude());

            // parkListView에 clickedPark만 있도록
            ParkAdapter tempParkAdapter = new ParkAdapter();
            tempParkAdapter.addItem(clickedPark);
            parkListView.setAdapter(tempParkAdapter);

            // 길 찾기 및 선 표시
            tmapdata.findPathData(nowPoint, naviEndPoint, new TMapData.FindPathDataListenerCallback() {
                @Override
                public void onFindPathData(TMapPolyLine polyLine) {
                    if (selectedMarker != null) {
                        if (clickParkType == 3) {
                            selectedMarker.setIcon(tmapShareParkMarkerIcon);
                        }
                        else {
                            selectedMarker.setIcon(tmapMarkerIcon);
                        }
                    }

                    clickParkType = clickedPark.getType();

                    tMapView.setTMapPathIcon(tmapStartMarkerIcon, null);
                    if (clickedPark.getType() == 3) {
                        endMarker.setIcon(tmapSelectedShareParkMarkerIcon);
                    }
                    else {
                        endMarker.setIcon(tmapSelectedMarkerIcon);
                    }

                    selectedMarker = endMarker;

                    tMapView.addTMapPath(polyLine);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tMapView.setCenterPoint(selectedMarker.longitude, selectedMarker.latitude);

                            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                            float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, displayMetrics);
                            parkListView.getLayoutParams().height = (int) px;
                            parkListView.requestLayout();

                            TextView parkOrder = findViewById(R.id.parkOrder);
                            parkOrder.setVisibility(View.GONE);

                            TextView parkName = findViewById(R.id.parkName);
                            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) parkName.getLayoutParams();
                            params.leftToLeft = R.id.parkItemConstraintLayout;
                            parkName.setLayoutParams(params);

                            parkSortHorizontalScrollView.setVisibility(View.GONE);
                            searchStartBtn.setVisibility(View.GONE);
                            cancelNaviBtn.setVisibility(View.VISIBLE);
                            toStartNaviBtn.setVisibility(View.VISIBLE);
                            if (clickedPark.getType() == 3) {
                                reservationFirestoreDocumentId = clickedPark.getFirebaseDocumentId();
                                toReservationBtn.setVisibility(View.VISIBLE);
                            }
                            else {
                                toReservationBtn.setVisibility(View.GONE);
                            }

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
            findPark(recievedSort);
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
