package com.bucheon.yeoddadae;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.Timestamp;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapTapi;
import com.skt.Tmap.address_info.TMapAddressInfo;
import com.skt.tmap.engine.navigation.SDKManager;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ReservationInformationActivity extends AppCompatActivity implements TMapGpsManager.onLocationChangedCallback {
    boolean IS_REAL_ANDROID_MACHINE = true;
    String loginId;
    String documentId;
    HashMap<String, Object> reservationInfo;
    HashMap<String, Object> shareParkInfo;
    String naviEndPointName;
    double nowLat = 37.578611; // 위도
    double nowLon = 126.977222; // 경도
    boolean isFirstLocationChangeCalled = false;
    TMapPoint nowPoint;
    TMapGpsManager gpsManager;

    ImageButton reservationInfoBackBtn;
    TextView reservationInfoIdContentTxt;
    TextView reservationInfoStatusContentTxt;
    TextView reservationInfoUpTimeContentTxt;
    TextView reservationInfoTimeContentTxt;
    TextView reservationInfoPriceContentTxt;
    TextView reservationInfoPtTxt;
    TextView reservationInfoShareParkNewAddressContentTxt;
    TextView reservationInfoShareParkOldAddressContentTxt;
    TextView reservationInfoShareParkDetailaddressContentTxt;
    ImageButton reservationInfoCancelBtn;
    TextView reservationInfoCancelTxt;
    ImageButton reservationInfoNaviBtn;
    TextView reservationInfoNaviTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation_information);

        reservationInfoBackBtn = findViewById(R.id.reservationInfoBackBtn);
        reservationInfoIdContentTxt = findViewById(R.id.reservationInfoIdContentTxt);
        reservationInfoStatusContentTxt = findViewById(R.id.reservationInfoStatusContentTxt);
        reservationInfoUpTimeContentTxt = findViewById(R.id.reservationInfoUpTimeContentTxt);
        reservationInfoTimeContentTxt = findViewById(R.id.reservationInfoTimeContentTxt);
        reservationInfoPriceContentTxt = findViewById(R.id.reservationInfoPriceContentTxt);
        reservationInfoPtTxt = findViewById(R.id.reservationInfoPtTxt);
        reservationInfoShareParkNewAddressContentTxt = findViewById(R.id.reservationInfoShareParkNewAddressContentTxt);
        reservationInfoShareParkOldAddressContentTxt = findViewById(R.id.reservationInfoShareParkOldAddressContentTxt);
        reservationInfoShareParkDetailaddressContentTxt = findViewById(R.id.reservationInfoShareParkDetailaddressContentTxt);
        reservationInfoCancelBtn = findViewById(R.id.reservationInfoCancelBtn);
        reservationInfoCancelTxt = findViewById(R.id.reservationInfoCancelTxt);
        reservationInfoNaviBtn = findViewById(R.id.reservationInfoNaviBtn);
        reservationInfoNaviTxt = findViewById(R.id.reservationInfoNaviTxt);

        Intent inIntent = getIntent();
        loginId = inIntent.getStringExtra("id");
        documentId = inIntent.getStringExtra("documentId");

        gpsManager = new TMapGpsManager(this);
        gpsManager.setMinTime(500); // ms단위
        gpsManager.setMinDistance(1); // m단위
        gpsManager.setProvider(gpsManager.GPS_PROVIDER);
        gpsManager.OpenGps();
        if (IS_REAL_ANDROID_MACHINE) {
            gpsManager.setProvider(gpsManager.NETWORK_PROVIDER);
            gpsManager.OpenGps();
        }

        Location currentLocation = SDKManager.getInstance().getCurrentPosition();
        nowLat = currentLocation.getLatitude();
        nowLon = currentLocation.getLongitude();

        FirestoreDatabase fd = new FirestoreDatabase();
        fd.loadReservation(loginId, documentId, new OnFirestoreDataLoadedListener() {
            @Override
            public void onDataLoaded(Object data) {
                reservationInfo = (HashMap<String, Object>) data;

                fd.loadShareParkInfo((String) reservationInfo.get("shareParkDocumentName"), new OnFirestoreDataLoadedListener() {
                    @Override
                    public void onDataLoaded(Object data) {
                        shareParkInfo = (HashMap<String, Object>) data;

                        TMapData tMapdata = new TMapData();
                        tMapdata.reverseGeocoding((double) shareParkInfo.get("lat"), (double) shareParkInfo.get("lon"), "A10", new TMapData.reverseGeocodingListenerCallback() {
                            @Override
                            public void onReverseGeocoding(TMapAddressInfo tMapAddressInfo) {
                                if (tMapAddressInfo != null) {
                                    String [] adrresses = tMapAddressInfo.strFullAddress.split(",");
                                    naviEndPointName = adrresses[2] + " : " + shareParkInfo.get("parkDetailAddress");
                                }
                            }
                        });

                        init();
                    }

                    @Override
                    public void onDataLoadError(String errorMessage) {
                        Log.d(TAG, errorMessage);
                        Toast.makeText(getApplicationContext(), "오류 발생", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }

            @Override
            public void onDataLoadError(String errorMessage) {
                Log.d(TAG, errorMessage);
                Toast.makeText(getApplicationContext(), "오류 발생", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        reservationInfoBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        reservationInfoNaviBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFirstLocationChangeCalled) {
                    fd.insertRoute(loginId, "공유주차장", nowLat, nowLon, null, naviEndPointName, (double) shareParkInfo.get("lat"), (double) shareParkInfo.get("lon"), new OnFirestoreDataLoadedListener() {
                                @Override
                                public void onDataLoaded(Object data) {}

                                @Override
                                public void onDataLoadError(String errorMessage) {
                                    Log.d(TAG, errorMessage);
                                    Toast.makeText(getApplicationContext(), "오류 발생", Toast.LENGTH_SHORT).show();
                                }
                            }
                    );
                }

                TMapTapi tt = new TMapTapi(ReservationInformationActivity.this);
                boolean isTmapApp = tt.isTmapApplicationInstalled();
                if (isTmapApp) {
                    tt.invokeRoute(naviEndPointName, (float) ((double) shareParkInfo.get("lon")), (float) ((double) shareParkInfo.get("lat")));
                }
                else {
                    Toast.makeText(getApplicationContext(), "TMAP이 설치되어 있지 않습니다", Toast.LENGTH_SHORT).show();
                }
            }
        });

        reservationInfoCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ReservationInformationActivity.this);
                builder.setTitle("예약 취소 확인");
                builder.setMessage("정말 취소하시겠습니까\n(되돌릴 수 없습니다)");
                builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        fd.cancelReservation(loginId, documentId, new OnFirestoreDataLoadedListener() {
                            @Override
                            public void onDataLoaded(Object data) {
                                Toast.makeText(getApplicationContext(), "예약 취소되었습니다", Toast.LENGTH_SHORT).show();
                                finish();
                            }

                            @Override
                            public void onDataLoadError(String errorMessage) {
                                if (errorMessage.equals("예약 시간이 지나서 취소 불가")) {
                                    Toast.makeText(getApplicationContext(), "예약 시간이 지나서 취소 불가합니다", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    Log.d(TAG, errorMessage);
                                    Toast.makeText(getApplicationContext(), "오류 발생", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });
                builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();

                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(ReservationInformationActivity.this, R.color.sub));
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(ReservationInformationActivity.this, R.color.disable));
            }
        });
    }

    private void init() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                reservationInfoIdContentTxt.setText(documentId);

                boolean isCancelled = (boolean) reservationInfo.get("isCancelled");

                if (isCancelled) {
                    reservationInfoStatusContentTxt.setText("취소됨");
                    reservationInfoCancelBtn.setVisibility(View.GONE);
                    reservationInfoCancelTxt.setVisibility(View.GONE);
                }
                else {
                    Calendar ca = Calendar.getInstance();
                    int year = ca.get(Calendar.YEAR);
                    int month = ca.get(Calendar.MONTH) + 1;
                    int day = ca.get(Calendar.DAY_OF_MONTH);
                    int hour = ca.get(Calendar.HOUR_OF_DAY);
                    int minute = ca.get(Calendar.MINUTE);
                    String nowString = "";
                    nowString += year;
                    if (month < 10) {
                        nowString += "0";
                    }
                    nowString += month;
                    if (day < 10) {
                        nowString += "0";
                    }
                    nowString += day;
                    if (hour < 10) {
                        nowString += "0";
                    }
                    nowString += hour;
                    if (minute < 10) {
                        nowString += "0";
                    }
                    nowString += minute;

                    HashMap<String, ArrayList<String>> reservationTime = (HashMap<String, ArrayList<String>>) reservationInfo.get("time");
                    List<String> sortedKeys = new ArrayList<>(reservationTime.keySet());
                    Collections.sort(sortedKeys);
                    String firstTime = sortedKeys.get(0) + reservationTime.get(sortedKeys.get(0)).get(0);
                    String endTime = sortedKeys.get(sortedKeys.size() - 1) + reservationTime.get(sortedKeys.get(sortedKeys.size() - 1)).get(1);

                    Log.d(TAG, nowString);
                    Log.d(TAG, firstTime);
                    Log.d(TAG, endTime);

                    if (nowString.compareTo(firstTime) < 0) {
                        reservationInfoStatusContentTxt.setText("사용 예정");
                        reservationInfoCancelBtn.setVisibility(View.VISIBLE);
                        reservationInfoCancelTxt.setVisibility(View.VISIBLE);
                    } else if (nowString.compareTo(endTime) < 0) {
                        reservationInfoStatusContentTxt.setText("사용 중");
                        reservationInfoCancelBtn.setVisibility(View.GONE);
                        reservationInfoCancelTxt.setVisibility(View.GONE);
                    } else {
                        reservationInfoStatusContentTxt.setText("사용 종료");
                        reservationInfoCancelBtn.setVisibility(View.GONE);
                        reservationInfoCancelTxt.setVisibility(View.GONE);
                    }
                }

                Timestamp timestamp = (Timestamp) reservationInfo.get("upTime");
                if (timestamp != null) {
                    Date date = timestamp.toDate();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일 HH:mm:ss", Locale.KOREA);
                    String dateString = sdf.format(date);
                    reservationInfoUpTimeContentTxt.setText(dateString);
                }

                HashMap<String, ArrayList<String>> reservationTime = (HashMap<String, ArrayList<String>>) reservationInfo.get("time");
                ArrayList<String> keys = new ArrayList<>(reservationTime.keySet());
                Collections.sort(keys);
                String reservationTimeString = "";
                for (String key : keys) {
                    int year = Integer.parseInt(key.substring(0, 4));
                    int month = Integer.parseInt(key.substring(4, 6));
                    int day = Integer.parseInt(key.substring(6, 8));

                    String startTimeHour = reservationTime.get(key).get(0).substring(0,2);
                    String startTimeMinute = reservationTime.get(key).get(0).substring(2,4);
                    String endTimeHour = reservationTime.get(key).get(1).substring(0,2);
                    String endTimeMinute = reservationTime.get(key).get(1).substring(2,4);

                    reservationTimeString += year + "년 " + month + "월 " + day + "일 " + startTimeHour + ":" + startTimeMinute + "부터 " + endTimeHour + ":" + endTimeMinute + "까지\n";
                }
                if (!reservationTimeString.isEmpty()) {
                    reservationTimeString = reservationTimeString.substring(0, reservationTimeString.length() - 1);
                }
                reservationInfoTimeContentTxt.setText(reservationTimeString);

                if ((long) reservationInfo.get("price") == 0) {
                    reservationInfoPriceContentTxt.setText("무료");
                    reservationInfoPtTxt.setVisibility(View.GONE);

                }
                else {
                    String formattedYdPoint = NumberFormat.getNumberInstance(Locale.KOREA).format((long) reservationInfo.get("price"));

                    reservationInfoPriceContentTxt.setText(formattedYdPoint);
                    reservationInfoPtTxt.setVisibility(View.VISIBLE);
                }

                reservationInfoShareParkDetailaddressContentTxt.setText((String) shareParkInfo.get("parkDetailAddress"));
            }
        });
        TMapData tMapdata = new TMapData();
        tMapdata.reverseGeocoding((double) shareParkInfo.get("lat"), (double) shareParkInfo.get("lon"), "A10", new TMapData.reverseGeocodingListenerCallback() {
            @Override
            public void onReverseGeocoding(TMapAddressInfo tMapAddressInfo) {
                if (tMapAddressInfo != null) {
                    String[] adrresses = tMapAddressInfo.strFullAddress.split(",");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            reservationInfoShareParkNewAddressContentTxt.setText(adrresses[2]);
                            reservationInfoShareParkOldAddressContentTxt.setText(adrresses[1]);
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onLocationChange(Location location) {
        nowLat = location.getLatitude();
        nowLon = location.getLongitude();
        Log.d(TAG, "onLocationChange 호출됨 : lat(경도) = " + nowLat + ", lon(위도) = " + nowLon);
        nowPoint = new TMapPoint(nowLat, nowLon);

        if (!isFirstLocationChangeCalled) {
            isFirstLocationChangeCalled = true;
        }
    }
}
