package com.bucheon.yeoddadae;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Locale;

public class FindParkActivity extends AppCompatActivity implements TMapGpsManager.onLocationChangedCallback, TMapView.OnClickListenerCallback, SttService.SttCallback {
    String loginId;
    private static final int PERMISSION_REQUEST_CODE = 1;
    boolean firstOnLocationChangeCalled = false; // onLocationChange가 처음 불림 여부
    boolean isItemSelected;
    boolean isSearchLayoutShowing;
    boolean isLoadingFirstCalled = false;
    int recievedSort;
    int nowSort;
    int nowOnly;
    int clickParkType;
    String startDate;
    String startTime;
    String endDate;
    String endTime;
    String startString;
    String endString;
    AlertDialog loadingAlert;
    // 경복궁
    double lat = 37.578611; // 위도
    double lon = 126.977222; // 경도
    TMapPoint nowPoint = new TMapPoint(lat, lon);
    TMapPoint naviEndPoint;
    int naviEndPointType;
    String naviEndPointName;
    String naviEndPointPoiId;
    String reservationFirestoreDocumentId;
    boolean firstSearchLayoutOpened = false;
    boolean isSearching = false;
    TMapGpsManager gpsManager;
    TMapView tMapView;
    TMapCircle tMapCircle;
    TMapData tMapData;
    TMapMarkerItem tempMarker;
    ParkItem tempPark;
    TMapPoint saveScrollPoint;
    TMapMarkerItem selectedMarker;
    ParkAdapter parkAdapter;
    SearchParkAdapter spa;
    ParkHistoryDialog phd;
    FirestoreDatabase fd;


    Intent serviceIntent;
    SttService sttService;
    ServiceConnection serviceConnection;
    SttService.SttCallback sttCallback;
    SttDialog sd;

    ConstraintLayout topConstraintLayout;
    ListView parkListView;
    ImageButton findParkBackBtn;
    ImageButton zoomOutBtn;
    ImageButton zoomInBtn;
    ImageButton gpsBtn;
    ImageButton findParkSttBtn;
    ConstraintLayout onlyConstLayout;
    ImageButton findParkOnlyReportParkBtn;
    ImageButton findParkOnlyShareParkBtn;
    ImageButton findParkTimeSetBtn;
    ConstraintLayout findParkCustomTimeConstLayout;
    EditText findParkCustomTimeStartDateEditTxt;
    EditText findParkCustomTimeStartTimeEditTxt;
    EditText findParkCustomTimeEndDateEditTxt;
    EditText findParkCustomTimeEndTimeEditTxt;
    HorizontalScrollView parkSortHorizontalScrollView;
    ImageButton sortByDistanceBtn;
    ImageButton sortByParkPriceBtn;
    ConstraintLayout naviConstLayout;
    ImageButton cancelNaviBtn;
    ImageButton toStartNaviBtn;
    ImageButton toReservationBtn;
    ConstraintLayout additionalFunctionConstLayout;
    ImageButton searchStartBtn;
    ImageButton parkHistoryBtn;
    ConstraintLayout searchConstraintLayout;
    EditText searchEdTxt;
    ImageButton searchTxtClearBtn;
    ImageButton searchBtn;
    ImageButton searchBackBtn;
    ListView searchListView;
    TextView searchNoTxt;

    Bitmap tmapMyLocationIcon;
    Bitmap tmapViewCenterPointIcon;
    Bitmap tmapMarkerIcon;
    Bitmap tmapStartMarkerIcon;
    Bitmap tmapSelectedMarkerIcon;
    Bitmap tmapShareParkMarkerIcon;
    Bitmap tmapSelectedShareParkMarkerIcon;
    Bitmap tmapReportParkMarkerIcon;
    Bitmap tmapSelectedReportParkMarkerIcon;
    Bitmap tmapSearchPlaceMarker;

    public interface OnFindParkCompletedListener {
        void onFindParkCompleted();
        void onFindParkError(String errorMessage);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_park);
  
        // 뷰 정의
        topConstraintLayout = findViewById(R.id.topConstraintLayout);
        parkListView = findViewById(R.id.parkListView);
        findParkBackBtn = findViewById(R.id.findParkBackBtn);
        zoomOutBtn = findViewById(R.id.zoomOutBtn);
        zoomInBtn = findViewById(R.id.zoomInBtn);
        gpsBtn = findViewById(R.id.gpsBtn);
        findParkSttBtn = findViewById(R.id.findParkSttBtn);
        parkSortHorizontalScrollView = findViewById(R.id.parkSortHorizontalScrollView);
        sortByDistanceBtn = findViewById(R.id.sortByDistanceBtn);
        sortByParkPriceBtn = findViewById(R.id.sortByParkPriceBtn);
        naviConstLayout = findViewById(R.id.naviConstLayout);
        cancelNaviBtn = findViewById(R.id.cancelNaviBtn);
        toStartNaviBtn = findViewById(R.id.toStartNaviBtn);
        toReservationBtn = findViewById(R.id.toReservationBtn);
        onlyConstLayout = findViewById(R.id.onlyConstLayout);
        findParkOnlyReportParkBtn = findViewById(R.id.findParkOnlyReportParkBtn);
        findParkOnlyShareParkBtn = findViewById(R.id.findParkOnlyShareParkBtn);
        findParkTimeSetBtn = findViewById(R.id.findParkTimeSetBtn);
        findParkCustomTimeConstLayout = findViewById(R.id.findParkCustomTimeConstLayout);
        findParkCustomTimeStartDateEditTxt = findViewById(R.id.findParkCustomTimeStartDateEditTxt);
        findParkCustomTimeStartTimeEditTxt = findViewById(R.id.findParkCustomTimeStartTimeEditTxt);
        findParkCustomTimeEndDateEditTxt = findViewById(R.id.findParkCustomTimeEndDateEditTxt);
        findParkCustomTimeEndTimeEditTxt = findViewById(R.id.findParkCustomTimeEndTimeEditTxt);
        additionalFunctionConstLayout = findViewById(R.id.additionalFunctionConstLayout);
        searchStartBtn = findViewById(R.id.searchStartBtn);
        parkHistoryBtn = findViewById(R.id.parkHistoryBtn);
        searchConstraintLayout = findViewById(R.id.searchConstraintLayout);
        searchEdTxt = findViewById(R.id.searchEdTxt);
        searchTxtClearBtn = findViewById(R.id.searchTxtClearBtn);
        searchBtn = findViewById(R.id.searchBtn);
        searchBackBtn = findViewById(R.id.searchBackBtn);
        searchListView = findViewById(R.id.searchListView);
        searchNoTxt = findViewById(R.id.searchNoTxt);
        
        // Bitmap 정의
        tmapMyLocationIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.temp_tmap_my_location);
        tmapViewCenterPointIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.temp_tmap_view_center_point);
        tmapMarkerIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.temp_tmap_marker);
        tmapSelectedMarkerIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.temp_tmap_selected_marker);
        tmapStartMarkerIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.temp_tmap_start_marker);
        tmapShareParkMarkerIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.temp_tmap_share_park_marker);
        tmapSelectedShareParkMarkerIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.temp_tmap_selected_share_park_marker);
        tmapReportParkMarkerIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.temp_tmap_report_park_marker);
        tmapSelectedReportParkMarkerIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.temp_tmap_selected_report_park_marker);
        tmapSearchPlaceMarker = BitmapFactory.decodeResource(this.getResources(), R.drawable.temp_tmap_search_place_marker);

        loadingStart(); // 로딩 시작

        // 메인액티비티에서 정렬기준과 아이디 받기
        Intent inIntent = getIntent();
        loginId = inIntent.getStringExtra("loginId");
        recievedSort = inIntent.getIntExtra("SortBy", 0);
        if (recievedSort == 0) {
            Log.d(TAG, "sttSort가 0임 (오류)");
            finish();
        }
        if (loginId == null || loginId.isEmpty()) {
            Log.d(TAG, "loginId가 전달되지 않음 (오류)");
            finish();
        }

        spa = new SearchParkAdapter(FindParkActivity.this);

        initSttService();

        nowOnly = 0;
        onlyBtnColorSet();

        // 로딩 완료까지 뷰 없애기
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                parkListView.setVisibility(View.GONE);
                parkSortHorizontalScrollView.setVisibility(View.GONE);
            }
        });

        findParkBackBtn.setOnClickListener(new View.OnClickListener() { // 액티비티 종료
            @Override
            public void onClick(View v) {
                finish();
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
        // gpsManager.setProvider(gpsManager.NETWORK_PROVIDER);
        // gpsManager.OpenGps();

        // TMapView 생성 및 보이기
        tMapView = new TMapView(this);
        LinearLayout linearLayoutTmap = (LinearLayout)findViewById(R.id.linearLayoutTmap);
        linearLayoutTmap.addView( tMapView );

        // TMapView 초기설정
        tMapView.setCenterPoint(lon, lat);
        tMapView.setLocationPoint(lon, lat);
        tMapView.setIcon(tmapMyLocationIcon);
        tMapView.setIconVisibility(true);

        // 가운데 마커 초기 설정

        // TMapCircle 초기설정
        tMapCircle = new TMapCircle();
        tMapCircle.setRadius(3000);
        tMapCircle.setCircleWidth(0);
        tMapCircle.setAreaColor(Color.rgb(0, 0, 0));
        tMapCircle.setAreaAlpha(25);
        tMapView.addTMapCircle("Circle", tMapCircle);

        findPark(recievedSort, null, null, new OnFindParkCompletedListener() {
            @Override
            public void onFindParkCompleted() {}

            @Override
            public void onFindParkError(String errorMessage) {
                Log.d(TAG, errorMessage);
            }
        });

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

        findParkSttBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sttService.startListeningForMainCommand();
            }
        });

        sortByDistanceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findPark(1, null, null, new OnFindParkCompletedListener() {
                    @Override
                    public void onFindParkCompleted() {}

                    @Override
                    public void onFindParkError(String errorMessage) {
                        Log.d(TAG, errorMessage);
                    }
                });
            }
        });

        sortByParkPriceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findPark(2, null, null, new OnFindParkCompletedListener() {
                    @Override
                    public void onFindParkCompleted() {}

                    @Override
                    public void onFindParkError(String errorMessage) {
                        Log.d(TAG, errorMessage);
                    }
                });
            }
        });

        cancelNaviBtn.setOnClickListener(new View.OnClickListener() { // 네비게이션 취소
            @Override
            public void onClick(View v) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        isItemSelected = false;

                        naviEndPoint = null;
                        naviEndPointType = 0;
                        naviEndPointName = null;
                        naviEndPointPoiId = null;

                        tMapView.removeTMapPath();

                        parkSortHorizontalScrollView.setVisibility(View.VISIBLE);
                        additionalFunctionConstLayout.setVisibility(View.VISIBLE);
                        onlyConstLayout.setVisibility(View.VISIBLE);
                        naviConstLayout.setVisibility(View.GONE);

                        findPark(nowSort, null, null, new OnFindParkCompletedListener() {
                            @Override
                            public void onFindParkCompleted() {}

                            @Override
                            public void onFindParkError(String errorMessage) {
                                Log.d(TAG, errorMessage);
                            }
                        });
                    }
                });
            }
        });

        toStartNaviBtn.setOnClickListener(new View.OnClickListener() { // 네비게이션 시작
            @Override
            public void onClick(View v) {
                String typeString = "";
                
                if (naviEndPointType == 1) {
                    typeString = "일반주차장";
                }
                else if (naviEndPointType == 2) {
                    typeString = "공영주차장";
                }
                else if (naviEndPointType == 3) {
                    typeString = "공유주차장";
                }
                else if (naviEndPointType == 6) {
                    typeString = "제보주차장";
                }
                
                fd.insertRoute(loginId, typeString, lat, lon, naviEndPointPoiId, naviEndPointName, naviEndPoint.getLatitude(), naviEndPoint.getLongitude(), new OnFirestoreDataLoadedListener() {
                            @Override
                            public void onDataLoaded(Object data) {}

                            @Override
                            public void onDataLoadError(String errorMessage) {
                                Log.d(TAG, errorMessage);
                                Toast.makeText(getApplicationContext(), "오류 발생", Toast.LENGTH_SHORT).show();
                            }
                        }
                );
                
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
                        if (!firstSearchLayoutOpened) {
                            getSearchHistory();
                            firstSearchLayoutOpened = true;
                        }

                        findParkCustomTimeConstLayout.setVisibility(View.GONE);
                        searchConstraintLayout.setVisibility(View.VISIBLE);
                        isSearchLayoutShowing = true;
                    }
                });
            }
        });

        searchEdTxt.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    searchBtn.callOnClick();
                }

                return false;
            }
        });

        searchEdTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (searchEdTxt.getText().toString().isEmpty()) {
                    searchTxtClearBtn.setVisibility(View.GONE);
                    getSearchHistory();
                }
                else {
                    searchTxtClearBtn.setVisibility(View.VISIBLE);
                }
            }
        });

        searchTxtClearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!searchEdTxt.getText().toString().isEmpty()) {
                    searchEdTxt.setText("");
                }
            }
        });

        searchBtn.setOnClickListener(new View.OnClickListener() { // 검색 시작
            @Override
            public void onClick(View v) {
                if (isSearching) {
                    return;
                }

                isSearching = true;
                searchBtn.setEnabled(true);

                loadingStart();

                if (!replaceNewlinesAndTrim(searchEdTxt).isEmpty()) {
                    fd.insertOrUpdateSearchKeyword(loginId, replaceNewlinesAndTrim(searchEdTxt), new OnFirestoreDataLoadedListener() {
                        @Override
                        public void onDataLoaded(Object data) {}

                        @Override
                        public void onDataLoadError(String errorMessage) {
                            Log.d(TAG, errorMessage);
                            Toast.makeText(getApplicationContext(), "오류 발생", Toast.LENGTH_SHORT).show();
                        }
                    });

                    if (spa != null) {
                        spa.clearPark();
                        spa.clearHistory();
                    }

                    tMapData = new TMapData();
                    tMapData.findAllPOI(replaceNewlinesAndTrim(searchEdTxt), new TMapData.FindAllPOIListenerCallback() {
                        @Override
                        public void onFindAllPOI(ArrayList<TMapPOIItem> arrayList) {
                            if (arrayList != null) {
                                for (int i = 0; i < arrayList.size(); i++) {
                                    TMapPOIItem item = arrayList.get(i);

                                    TMapPolyLine tpolyline = new TMapPolyLine();
                                    tpolyline.addLinePoint(nowPoint);
                                    tpolyline.addLinePoint(new TMapPoint(Double.parseDouble(item.frontLat), Double.parseDouble(item.frontLon)));
                                    double distance = tpolyline.getDistance() / 1000; // km단위

                                    if (item.firstNo.equals("0") && item.secondNo.equals("0")) {
                                        spa.addPark(new ParkItem(4, item.name, Double.toString(distance), null, item.telNo, null, null, item.frontLat, item.frontLon, item.id, null));
                                    }
                                    else {
                                        if (item.name.contains("주차")) {
                                            if (item.name.contains("공영")) {
                                                spa.addPark(new ParkItem(2, item.name, Double.toString(distance), null, item.telNo, null, null, item.frontLat, item.frontLon, item.id, null));
                                            }
                                            else {
                                                spa.addPark(new ParkItem(1, item.name, Double.toString(distance), null, item.telNo, null, null, item.frontLat, item.frontLon, item.id, null));
                                            }
                                        }
                                        else {
                                            spa.addPark(new ParkItem(5, item.name, Double.toString(distance), null, item.telNo, null, null, item.frontLat, item.frontLon, item.id, null));
                                        }
                                    }
                                }

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        spa.duplicationRemove();
                                        searchListView.setAdapter(spa);
                                    }
                                });

                                if (spa.getCount() == 0) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            searchListView.setVisibility(View.GONE);
                                            searchNoTxt.setVisibility(View.VISIBLE);
                                            searchBtn.setEnabled(true);
                                            isSearching = false;
                                            loadingStop();
                                        }
                                    });
                                }
                                else {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            searchListView.setVisibility(View.VISIBLE);
                                            searchNoTxt.setVisibility(View.GONE);
                                            searchBtn.setEnabled(true);
                                            isSearching = false;
                                            loadingStop();
                                        }
                                    });
                                }
                            }
                            else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        searchListView.setAdapter(spa);
                                        searchListView.setVisibility(View.GONE);
                                        searchNoTxt.setVisibility(View.VISIBLE);
                                        searchBtn.setEnabled(true);
                                        isSearching = false;
                                        loadingStop();
                                    }
                                });
                            }
                        }
                    });
                }
                else {
                    searchBtn.setEnabled(true);
                    isSearching = false;
                    loadingStop();
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
                        isSearchLayoutShowing = false;
                    }
                });

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(searchEdTxt.getWindowToken(), 0);
                }
            }
        });

        parkHistoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phd = new ParkHistoryDialog(loginId, FindParkActivity.this, new ParkHistoryDialogListener() {
                    @Override
                    public void onMessageSend(String message) {
                        if (message.startsWith("주차장기록아이템클릭")) {
                            String dataPart = message.replace("주차장기록아이템클릭 : ", "");
                            String[] numbers = dataPart.split("/");

                            double historyLat = Double.parseDouble(numbers[0]);
                            double historyLon = Double.parseDouble(numbers[1]);
                            String poiId = numbers[2];
                            Log.d(TAG, poiId);

                            tMapView.setCenterPoint(historyLon, historyLat);

                            findPark(nowSort, null, null, new OnFindParkCompletedListener() {
                                @Override
                                public void onFindParkCompleted() {
                                    ParkItem pi = parkAdapter.findItemByPoiId(poiId);
                                    TMapMarkerItem targetMarker = tMapView.getMarkerItemFromID(pi.getName());

                                    parkSelect(targetMarker, pi);
                                }

                                @Override
                                public void onFindParkError(String errorMessage) {
                                    Log.d(TAG, errorMessage);
                                }
                            });
                        }
                    }
                });

                phd.show();
            }
        });

        findParkOnlyReportParkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nowOnly == 1) {
                    nowOnly = 0;
                }
                else {
                    nowOnly = 1;
                }
                onlyBtnColorSet();
                findPark(nowSort, null, null, new OnFindParkCompletedListener() {
                    @Override
                    public void onFindParkCompleted() {}

                    @Override
                    public void onFindParkError(String errorMessage) {
                        Log.d(TAG, errorMessage);
                    }
                });
            }
        });

        findParkOnlyShareParkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nowOnly == 2) {
                    nowOnly = 0;
                }
                else {
                    nowOnly = 2;
                }
                onlyBtnColorSet();
                findPark(nowSort, null, null, new OnFindParkCompletedListener() {
                    @Override
                    public void onFindParkCompleted() {}

                    @Override
                    public void onFindParkError(String errorMessage) {
                        Log.d(TAG, errorMessage);
                    }
                });
            }
        });

        findParkTimeSetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (findParkCustomTimeConstLayout.getVisibility() == View.VISIBLE) {
                    findParkCustomTimeConstLayout.setVisibility(View.GONE);
                }
                else {
                    findParkCustomTimeConstLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        findParkCustomTimeStartDateEditTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomDatePickerDialog sdpd = new CustomDatePickerDialog(FindParkActivity.this, null, 0);
                sdpd.show();
            }
        });

        findParkCustomTimeStartTimeEditTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int initHour = 0;
                int initMinute = 0;

                if (startTime != null) {
                    initHour = Integer.parseInt(startTime.substring(0, 2));
                    initMinute = Integer.parseInt(startTime.substring(2, 4));
                }
                TimePickerDialog timePickerDialog = new TimePickerDialog(FindParkActivity.this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                        String formattedTime = String.format(Locale.KOREA, "%02d:%02d", selectedHour, selectedMinute);
                        findParkCustomTimeStartTimeEditTxt.setText(formattedTime);
                        startTime = String.format(Locale.KOREA, "%02d%02d", selectedHour, selectedMinute);
                        checkAllCustomTimeSetted();
                    }
                }, initHour, initMinute, true);

                timePickerDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent); // 배경 투명하게 설정
                timePickerDialog.show();
            }
        });

        findParkCustomTimeEndDateEditTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomDatePickerDialog sdpd = new CustomDatePickerDialog(FindParkActivity.this, null, 1);
                sdpd.show();
            }
        });

        findParkCustomTimeEndTimeEditTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int initHour = 0;
                int initMinute = 0;

                if (endTime != null) {
                    initHour = Integer.parseInt(endTime.substring(0, 2));
                    initMinute = Integer.parseInt(endTime.substring(2, 4));
                }
                TimePickerDialog timePickerDialog = new TimePickerDialog(FindParkActivity.this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                        if (selectedHour == 0 && selectedMinute == 0) {
                            findParkCustomTimeEndTimeEditTxt.setText("24:00");
                        }
                        else {
                            String formattedTime = String.format(Locale.KOREA, "%02d:%02d", selectedHour, selectedMinute);
                            findParkCustomTimeEndTimeEditTxt.setText(formattedTime);
                        }
                        if (String.format(Locale.KOREA, "%02d%02d", selectedHour, selectedMinute).equals("0000")) {
                            endTime = "2400";
                        }
                        else {
                            endTime = String.format(Locale.KOREA, "%02d%02d", selectedHour, selectedMinute);
                        }
                        checkAllCustomTimeSetted();
                    }
                }, initHour, initMinute, true);

                timePickerDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent); // 배경 투명하게 설정
                timePickerDialog.show();
            }
        });

        parkListView.setOnItemClickListener(new AdapterView.OnItemClickListener() { // 주차장 리스트뷰 아이템 클릭
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!isItemSelected) {
                    ParkItem targetPark = (ParkItem) parent.getItemAtPosition(position);
                    TMapMarkerItem targetMarker = tMapView.getMarkerItemFromID(targetPark.getName());

                    if (targetMarker != null && targetPark != null) {
                        parkSelect(targetMarker, targetPark);
                    }
                }
            }
        });

        searchListView.setOnItemClickListener(new AdapterView.OnItemClickListener() { // 주소,장소 검색 리스트뷰 아이템 클릭
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object target = spa.getItem(position);

                if (target instanceof SearchHistoryItem) {
                    SearchHistoryItem targetSearchHistory = (SearchHistoryItem) target;

                    searchEdTxt.setText(targetSearchHistory.getKeyword());
                    searchEdTxt.setSelection(searchEdTxt.getText().toString().length());

                    searchBtn.callOnClick();
                }
                else if (target instanceof ParkItem) {
                    ParkItem targetSearchPark = (ParkItem) target;

                    if (targetSearchPark != null) {
                        searchSelect(targetSearchPark);
                    }
                }
            }
        });

        spa.setOnHistoryDeleteListener(new OnHistoryDeleteListener() {
            @Override
            public void onHistoryDelete(SearchHistoryItem item, int position) {
                fd.deleteSearchKeyword(loginId, item.getKeyword(), new OnFirestoreDataLoadedListener() {
                    @Override
                    public void onDataLoaded(Object data) {
                        spa.deleteHistory(position);
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

    @Override
    public boolean onPressEvent(ArrayList<TMapMarkerItem> arrayList, ArrayList<TMapPOIItem> arrayList1, TMapPoint tMapPoint, PointF pointF) {
        if (arrayList.size() > 0) {
            tempMarker = arrayList.get(0);
            tempPark = parkAdapter.findItem(tempMarker.getName());
            saveScrollPoint = tMapView.getCenterPoint();
        }

        return false;
    }

    @Override
    public boolean onPressUpEvent(ArrayList<TMapMarkerItem> arrayList, ArrayList<TMapPOIItem> arrayList1, TMapPoint tMapPoint, PointF pointF) {
        if (arrayList.size() > 0) {
            TMapMarkerItem targetMarker = arrayList.get(0); // endMarker = 클릭한 TMapMarkerItem
            ParkItem targetPark = parkAdapter.findItem(targetMarker.getName());

            if (saveScrollPoint!= null && saveScrollPoint.equals(tMapView.getCenterPoint()) && targetMarker == tempMarker && targetPark == tempPark && targetPark != null && targetMarker != null) {
                parkSelect(targetMarker, targetPark);
            }
        }

        tempMarker = null;
        tempPark = null;
        saveScrollPoint = null;

        return false;
    }

    void parkSelect(TMapMarkerItem targetMarker, ParkItem targetPark) {
        // parkListView에 targetPark만 있도록
        ParkAdapter tempParkAdapter = new ParkAdapter();
        tempParkAdapter.addItem(targetPark);
        parkListView.setAdapter(tempParkAdapter);

        // 도착점 설정
        naviEndPoint = new TMapPoint(targetPark.getLat(), targetPark.getLon());
        naviEndPointType = targetPark.getType();
        naviEndPointName = targetPark.getName();
        naviEndPointPoiId = targetPark.getPoiId();

        // 길 찾기 및 선 표시
        TMapData tmapdata = new TMapData();
        tmapdata.findPathData(nowPoint, naviEndPoint, new TMapData.FindPathDataListenerCallback() {
            @Override
            public void onFindPathData(TMapPolyLine polyLine) {
                if (selectedMarker != null) {
                    if (clickParkType == 3) {
                        selectedMarker.setIcon(tmapShareParkMarkerIcon);
                    }
                    else if (clickParkType == 6) {
                        selectedMarker.setIcon(tmapReportParkMarkerIcon);
                    }
                    else {
                        selectedMarker.setIcon(tmapMarkerIcon);
                    }
                }

                tMapView.setTMapPathIcon(tmapStartMarkerIcon, null);
                if (targetPark.getType() == 3) {
                    targetMarker.setIcon(tmapSelectedShareParkMarkerIcon);
                }
                else if (targetPark.getType() == 6) {
                    targetMarker.setIcon(tmapSelectedReportParkMarkerIcon);
                }
                else {
                    targetMarker.setIcon(tmapSelectedMarkerIcon);
                }

                clickParkType = targetPark.getType();
                selectedMarker = targetMarker;
                tMapView.bringMarkerToFront(selectedMarker);

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
                        parkOrder.setVisibility(View.INVISIBLE);

                        TextView parkName = findViewById(R.id.parkName);
                        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) parkName.getLayoutParams();
                        params.leftToLeft = R.id.parkItemConstraintLayout;
                        parkName.setLayoutParams(params);

                        parkSortHorizontalScrollView.setVisibility(View.GONE);
                        additionalFunctionConstLayout.setVisibility(View.GONE);
                        onlyConstLayout.setVisibility(View.GONE);
                        findParkCustomTimeConstLayout.setVisibility(View.GONE);
                        naviConstLayout.setVisibility(View.VISIBLE);
                        if (targetPark.getType() == 3) {
                            reservationFirestoreDocumentId = targetPark.getFirebaseDocumentId();
                            toReservationBtn.setVisibility(View.VISIBLE);
                        } else {
                            toReservationBtn.setVisibility(View.GONE);
                        }

                        isItemSelected = true;
                    }
                });
            }
        });
    }

    void searchSelect(ParkItem targetSearchPark) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(searchEdTxt.getWindowToken(), 0);
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tMapView.setCenterPoint(targetSearchPark.getLon(), targetSearchPark.getLat());
                tMapView.setZoomLevel(13);

                searchConstraintLayout.setVisibility(View.GONE);
                isSearchLayoutShowing = false;
            }
        });

        findPark(nowSort, null, null, new OnFindParkCompletedListener() {
            @Override
            public void onFindParkCompleted() {
                if (targetSearchPark.getType() == 1 || targetSearchPark.getType() == 2) {
                    tMapView.setZoomLevel(13);
                    TMapMarkerItem targetMarker = tMapView.getMarkerItemFromID(targetSearchPark.getName());

                    parkSelect(targetMarker, targetSearchPark);
                }
            }

            @Override
            public void onFindParkError(String errorMessage) {
                Log.d(TAG, errorMessage);
            }
        });
    }

    void findPark(int sortBy, String startString2, String endString2, OnFindParkCompletedListener listener) { // sortBy는 정렬기준 (1:거리순, 2:평점순, 3:주차가순)
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
                    listener.onFindParkError("주차장 검색 결과 없음");
                    return;
                }
                fd = new FirestoreDatabase();
                fd.findSharePark(loginId, centerPoint.getLatitude(), centerPoint.getLongitude(), 3, startString2, endString2, new OnFirestoreDataLoadedListener() {
                    @Override
                    public void onDataLoaded(Object data) {
                        ArrayList<ParkItem> shareParkList = (ArrayList<ParkItem>) data;
                        fd.findDicountPark(centerPoint.getLatitude(), centerPoint.getLongitude(), 3, new OnFirestoreDataLoadedListener() {
                            @Override
                            public void onDataLoaded(Object data) {
                                ArrayList<ParkItem> reportParkList = (ArrayList<ParkItem>) data;
                                tMapView.removeAllTMapCircle();
                                tMapView.removeAllMarkerItem();
                                parkAdapter = new ParkAdapter();
                                if (nowOnly == 0) {
                                    for (int i = 0; i < arrayList.size(); i++) { // TMAP 검색
                                        TMapPOIItem item = arrayList.get(i);

                                        TMapPolyLine tpolyline = new TMapPolyLine();
                                        tpolyline.addLinePoint(nowPoint);
                                        tpolyline.addLinePoint(new TMapPoint(Double.parseDouble(item.frontLat), Double.parseDouble(item.frontLon)));
                                        double distance = tpolyline.getDistance() / 1000; // km단위

                                        if (item.name.contains("공영")) {
                                            parkAdapter.addItem(new ParkItem(2, item.name, Double.toString(distance), item.fee, item.telNo, null, null, item.frontLat, item.frontLon, item.id, null));
                                        }
                                        else {
                                            parkAdapter.addItem(new ParkItem(1, item.name, Double.toString(distance), item.fee, item.telNo, null, null, item.frontLat, item.frontLon, item.id, null));
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
                                    for (ParkItem item : shareParkList) { // Firestore 검색
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
                                    for (ParkItem item : reportParkList) { // Firestore 검색
                                        parkAdapter.addItem(item);
                                        TMapPoint tpoint = new TMapPoint(item.getLat(), item.getLon());
                                        TMapMarkerItem tItem = new TMapMarkerItem();
                                        tItem.setTMapPoint(tpoint);
                                        tItem.setName(item.getName());
                                        tItem.setVisible(TMapMarkerItem.VISIBLE);
                                        tItem.setIcon(tmapReportParkMarkerIcon);
                                        tItem.setPosition(0.5f,1.0f); // 마커의 중심점을 하단, 중앙으로 설정
                                        tMapView.addMarkerItem(item.getName(), tItem);
                                    }
                                    parkAdapter.poiOverReport();
                                }
                                else if (nowOnly == 1) {
                                    for (ParkItem item : reportParkList) { // Firestore 검색
                                        parkAdapter.addItem(item);
                                        TMapPoint tpoint = new TMapPoint(item.getLat(), item.getLon());
                                        TMapMarkerItem tItem = new TMapMarkerItem();
                                        tItem.setTMapPoint(tpoint);
                                        tItem.setName(item.getName());
                                        tItem.setVisible(TMapMarkerItem.VISIBLE);
                                        tItem.setIcon(tmapReportParkMarkerIcon);
                                        tItem.setPosition(0.5f,1.0f); // 마커의 중심점을 하단, 중앙으로 설정
                                        tMapView.addMarkerItem(item.getName(), tItem);
                                    }
                                }
                                else if (nowOnly == 2) {
                                    for (ParkItem item : shareParkList) { // Firestore 검색
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
                                        switch (parkAdapter.getSize()) {
                                            case 0:
                                                px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, displayMetrics);
                                                break; case 1: px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, displayMetrics);
                                                break; case 2: px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200, displayMetrics);
                                                break; default: px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 240, displayMetrics);
                                        }
                                        parkListView.getLayoutParams().height = (int) px;
                                        parkListView.requestLayout();
                                        switch (sortBy) {
                                            case 1 :
                                                sortByDistanceBtn.setImageResource(R.drawable.gradate_button);
                                                sortByParkPriceBtn.setImageResource(R.drawable.disabled_button);
                                                parkAdapter.sortByDistance();
                                                break; case 2 : sortByDistanceBtn.setImageResource(R.drawable.disabled_button);
                                                sortByParkPriceBtn.setImageResource(R.drawable.gradate_button);
                                                parkAdapter.sortByParkPrice();
                                                break;
                                        }
                                        parkListView.setVisibility(View.VISIBLE);
                                        parkSortHorizontalScrollView.setVisibility(View.VISIBLE);
                                        naviConstLayout.setVisibility(View.GONE);
                                        additionalFunctionConstLayout.setVisibility(View.VISIBLE);
                                    }
                                });

                                isItemSelected = false;
                                naviEndPoint = null;
                                naviEndPointType = 0;
                                naviEndPointName = null;
                                naviEndPointPoiId = null;

                                tMapCircle.setCenterPoint(centerPoint);
                                tMapView.addTMapCircle("Circle", tMapCircle);
                                TMapMarkerItem centerMarker = new TMapMarkerItem();
                                centerMarker.setTMapPoint(centerPoint);
                                centerMarker.setName("centerMarker");
                                centerMarker.setIcon(tmapViewCenterPointIcon);
                                centerMarker.setPosition(0.5f,0.5f);
                                centerMarker.setVisible(TMapMarkerItem.VISIBLE);
                                tMapView.addMarkerItem("centerMarker", centerMarker);
                                nowSort = sortBy;

                                if (startString2 == null || endString2 == null) {
                                    clearCustomTimeConstLayout();
                                }

                                loadingStop();
                                listener.onFindParkCompleted();
                            }
                            @Override
                            public void onDataLoadError(String errorMessage) {
                                Log.d(TAG, errorMessage);
                                loadingStop();
                                Toast.makeText(getApplicationContext(), "오류 발생", Toast.LENGTH_SHORT).show();
                                listener.onFindParkError(errorMessage);
                            }
                        });
                    }
                    @Override
                    public void onDataLoadError(String errorMessage) {
                        Log.d(TAG, errorMessage);
                        loadingStop();
                        Toast.makeText(getApplicationContext(), "오류 발생", Toast.LENGTH_SHORT).show();
                        listener.onFindParkError(errorMessage);
                    }
                });
            }
        });
    }

    void getSearchHistory () {
        loadingStart();

        spa.clearPark();
        spa.clearHistory();

        fd.selectSearchKeywords(loginId, new OnFirestoreDataLoadedListener() {
            @Override
            public void onDataLoaded(Object data) {
                ArrayList<SearchHistoryItem> result = (ArrayList<SearchHistoryItem>) data;

                for (SearchHistoryItem item : result) {
                    spa.addHistory(item);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        searchListView.setVisibility(View.VISIBLE);
                        searchNoTxt.setVisibility(View.GONE);
                        searchListView.setAdapter(spa);
                    }
                });

                loadingStop();
            }

            @Override
            public void onDataLoadError(String errorMessage) {
                loadingStop();
                Log.d(TAG, errorMessage);
                Toast.makeText(getApplicationContext(), "오류 발생", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onLocationChange(Location location){
        lat = location.getLatitude();
        lon = location.getLongitude();
        Log.d(TAG, "onLocationChange 호출됨 : lat(경도) = " + lat + ", lon(위도) = " + lon);

        nowPoint = gpsManager.getLocation();
        tMapView.setLocationPoint(lon, lat);

        if (!firstOnLocationChangeCalled) {
            tMapView.setCenterPoint(lon, lat);
            findPark(recievedSort, null, null, new OnFindParkCompletedListener() {
                @Override
                public void onFindParkCompleted() {}

                @Override
                public void onFindParkError(String errorMessage) {
                    Log.d(TAG, errorMessage);
                }
            });
            firstOnLocationChangeCalled = true;
        }
    }

    public void loadingStart() {
        Log.d(TAG, "로딩 시작");

        if (loadingAlert != null && loadingAlert.isShowing()) {
            return; // 이미 보여지고 있다면 함수 종료
        }

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

        if (!isFinishing()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadingAlert.show();
                    loadingAlert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(FindParkActivity.this, R.color.disable));
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

    void initSttService() {
        sd = new SttDialog(this, new SttDialogListener() {
            @Override
            public void onMessageSend(String message) {
                if (message.equals("SttDialog버튼클릭")) {
                    sd.setSttStatusTxt("메인 명령어 듣는 중");
                    sttService.startListeningForMainCommand();
                }
                else if (message.equals("SttDialog닫힘")) {
                    sttService.startListeningForWakeUpWord();
                }
            }
        });

        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                SttService.SttBinder binder = (SttService.SttBinder) service;
                sttService = binder.getService();
                sttService.setSttCallback(sttCallback);
                Log.d(TAG, "FindParkActivity : STT 서비스 연결됨");
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.d(TAG, "FindParkActivity : STT 서비스 연결 해제됨");
            }
        };

        serviceIntent = new Intent(this, SttService.class);
        startService(serviceIntent);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        sttCallback = new SttService.SttCallback() {
            @Override
            public void onMainCommandReceived(String mainCommand) {
                FindParkActivity.this.onMainCommandReceived(mainCommand);
            }

            @Override
            public void onUpdateUI(String message) {
                FindParkActivity.this.onUpdateUI(message);
            }
        };
    }

    @Override
    public void onMainCommandReceived(String mainCommand) {
        Log.d(TAG, "FindParkActivity에서 받은 명령: " + mainCommand);

        // sortBy는 정렬기준 (1:거리순, 2:평점순, 3:주차가순)
        if (mainCommand.contains("메인")) {
            sd.dismiss();
            finish();
        }
        else if (mainCommand.contains("돌아") || mainCommand.contains("이전")) {
            if (isItemSelected) {
                sd.dismiss();
                cancelNaviBtn.callOnClick();
            }
            else if (isSearchLayoutShowing) {
                sd.dismiss();
                searchBackBtn.callOnClick();
            }
            else {
                sd.dismiss();
                finish();
            }
        }
        else if (mainCommand.contains("검색")) {
            sd.dismiss();
            if (!isItemSelected) {
                int index = mainCommand.indexOf("검색");
                String seartchStirng = mainCommand.substring(0, index).trim();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(searchConstraintLayout.getVisibility() == View.GONE) {
                            searchConstraintLayout.setVisibility(View.VISIBLE);
                            isSearchLayoutShowing = true;
                        }
                        searchEdTxt.setText(seartchStirng);

                        searchBtn.callOnClick();
                    }
                });
            }
        }
        else if (!isItemSelected && (mainCommand.contains("순") || mainCommand.contains("정렬") || mainCommand.contains("찾") )) {
            sd.dismiss();
            if (mainCommand.contains("차비") && (mainCommand.contains("요금") || mainCommand.contains("료"))) {
                findPark(2, null, null, new OnFindParkCompletedListener() {
                    @Override
                    public void onFindParkCompleted() {}

                    @Override
                    public void onFindParkError(String errorMessage) {
                        Log.d(TAG, errorMessage);
                    }
                });
            }
            else {
                findPark(1, null, null, new OnFindParkCompletedListener() {
                    @Override
                    public void onFindParkCompleted() {}

                    @Override
                    public void onFindParkError(String errorMessage) {
                        Log.d(TAG, errorMessage);
                    }
                });
            }
        }
        else if (isItemSelected && (mainCommand.contains("안내") || mainCommand.contains("내비") || mainCommand.contains("시작") || mainCommand.contains("출발"))) {
            sd.dismiss();

            toStartNaviBtn.callOnClick();
        }
        else {
            int number = 0;

            if (mainCommand.matches(".*\\d.*")) {
                String numberStr = mainCommand.replaceAll("\\D", ""); // 숫자 이외의 문자 제거
                number = Integer.parseInt(numberStr);
            }
            else if (mainCommand.matches(".*[일이삼사오육칠팔구십].*")){
                String numberStr = mainCommand.replaceAll("[^일이삼사오육칠팔구십]", ""); // 한글 숫자 이외의 문자 제거
                switch (numberStr) {
                    case "일":
                        number = 1;
                        break;
                    case "이":
                        number = 2;
                        break;
                    case "삼":
                        number = 3;
                        break;
                    case "사":
                        number = 4;
                        break;
                    case "오":
                        number = 5;
                        break;
                    case "육":
                        number = 6;
                        break;
                    case "칠":
                        number = 7;
                        break;
                    case "팔":
                        number = 8;
                        break;
                    case "구":
                        number = 9;
                        break;
                    case "십":
                        number = 10;
                        break;
                }
            }
            Log.d (TAG, "number 는 " + number);
            if (number != 0) {
                if (isSearchLayoutShowing) {
                    if (spa != null &&  0 < spa.getCount() && 0 < number && number <= spa.getCount()) {
                        sd.dismiss();

                        ParkItem targetSearchPark = (ParkItem) spa.getItem(number - 1);

                        if (targetSearchPark != null) {
                            searchSelect(targetSearchPark);
                        }
                    }
                }
                else {
                    if (parkAdapter != null &&  0 < parkAdapter.getSize() && 0 < number && number <= parkAdapter.getSize()) {
                        sd.dismiss();

                        ParkItem targetPark = (ParkItem) parkAdapter.getItem(number - 1);
                        TMapMarkerItem targetMarker = tMapView.getMarkerItemFromID(targetPark.getName());

                        if (targetMarker != null && targetPark != null) {
                            parkSelect(targetMarker, targetPark);
                        }
                    }
                    else {
                        sd.changeToInactivateIcon();
                        sd.setSttStatusTxt(number + "번째 결과를 찾을 수 없습니다");
                    }
                }
            }
            else {
                sd.changeToInactivateIcon();
                sd.setSttStatusTxt(mainCommand + "\n알 수 없는 명령어입니다");
            }
        }
    }

    @Override
    public void onUpdateUI(String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (message.equals("메인명령어듣는중")) {
                    if (!sd.isShowing()) {
                        sd.show();
                    }
                    sd.changeToActiveIcon();
                    sd.setSttStatusTxt("메인 명령어 듣는 중");
                }
                else if (message.equals("음성인식실패")) {
                    sd.changeToInactivateIcon();
                    sd.setSttStatusTxt("음성 인식 실패");
                }
                else if (message.equals("타임아웃")) {
                    sd.changeToInactivateIcon();
                    sd.setSttStatusTxt("음성 인식 타임 아웃");
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (loadingAlert.isShowing()) {
            loadingAlert.dismiss();
        }
        if (sd.isShowing()) {
            sd.dismiss();
        }

        if (serviceConnection != null && serviceIntent != null) {
            unbindService(serviceConnection);
            stopService(serviceIntent);
            serviceConnection = null;
            serviceIntent = null;
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        initSttService();
    }

    void onlyBtnColorSet () {
        if (nowOnly == 0) {
            findParkOnlyReportParkBtn.setImageResource(R.drawable.disabled_button);
            findParkOnlyShareParkBtn.setImageResource(R.drawable.disabled_button);
            findParkTimeSetBtn.setVisibility(View.GONE);
            findParkCustomTimeConstLayout.setVisibility(View.GONE);
        }
        else if (nowOnly == 1) {
            findParkOnlyReportParkBtn.setImageResource(R.drawable.gradate_button);
            findParkOnlyShareParkBtn.setImageResource(R.drawable.disabled_button);
            findParkTimeSetBtn.setVisibility(View.GONE);
            findParkCustomTimeConstLayout.setVisibility(View.GONE);
        }
        else if (nowOnly == 2) {
            findParkOnlyReportParkBtn.setImageResource(R.drawable.disabled_button);
            findParkOnlyShareParkBtn.setImageResource(R.drawable.gradate_button);
            findParkTimeSetBtn.setVisibility(View.VISIBLE);
        }
    }

    void checkAllCustomTimeSetted () {
        if (startDate != null && !startDate.isEmpty() && startTime != null && !startTime.isEmpty() && endDate != null && !endDate.isEmpty() && endTime != null && !endTime.isEmpty()) {

            startString = startDate + startTime;

            endString = endDate + endTime;

            Log.d(TAG, startString + "    " + endString);

            findPark(nowSort, startString, endString, new OnFindParkCompletedListener() {
                @Override
                public void onFindParkCompleted() {}

                @Override
                public void onFindParkError(String errorMessage) {
                    Log.d(TAG, errorMessage);
                }
            });
        }
    }

    void receiveStartDateFromDialog (String s) throws ParseException {
        this.startDate = s;
        findParkCustomTimeStartDateEditTxt.setText(s.substring(0, 4) + "년 " + s.substring(4, 6) + "월 " + s.substring(6) + "일");
        checkAllCustomTimeSetted();
    }

    void receiveEndDateFromDialog (String s) throws ParseException {
        this.endDate = s;
        findParkCustomTimeEndDateEditTxt.setText(s.substring(0, 4) + "년 " + s.substring(4, 6) + "월 " + s.substring(6) + "일");
        checkAllCustomTimeSetted();
    }

    void clearCustomTimeConstLayout() {
        startDate = null;
        startTime = null;
        endDate = null;
        endTime = null;

        startString = null;
        endString = null;

        findParkCustomTimeStartDateEditTxt.setText("");
        findParkCustomTimeStartTimeEditTxt.setText("");
        findParkCustomTimeEndDateEditTxt.setText("");
        findParkCustomTimeEndTimeEditTxt.setText("");
    }

    String replaceNewlinesAndTrim(EditText et) {
        return et.getText().toString().replaceAll("\\n", " ").trim();
    }
}
