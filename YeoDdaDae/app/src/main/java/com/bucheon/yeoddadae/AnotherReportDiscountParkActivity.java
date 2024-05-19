package com.bucheon.yeoddadae;

import static com.google.android.exoplayer2.ExoPlayerLibraryInfo.TAG;

import android.content.ContentValues;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.skt.Tmap.TMapGpsManager;
import com.skt.tmap.engine.navigation.SDKManager;

import java.util.ArrayList;
import java.util.HashMap;

public class AnotherReportDiscountParkActivity extends AppCompatActivity implements TMapGpsManager.onLocationChangedCallback {
    String loginId;
    double nowLat;
    double nowLon;
    TMapGpsManager gpsManager;

    Button anotherReportBackBtn;
    Spinner anotherReportDistanceSpinner;
    ListView anotherReportListView;
    Button toAddReportBtn;
    Button toMyreportBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_another_report_discount_park);

        Location currentLocation = SDKManager.getInstance().getCurrentPosition();
        nowLat = currentLocation.getLatitude();
        nowLon = currentLocation.getLongitude();

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

        anotherReportBackBtn = findViewById(R.id.anotherReportBackBtn);
        anotherReportDistanceSpinner = findViewById(R.id.anotherReportDistanceSpinner);
        anotherReportListView = findViewById(R.id.anotherReportListView);
        toAddReportBtn = findViewById(R.id.toAddReportBtn);
        toMyreportBtn = findViewById(R.id.toMyreportBtn);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent inIntent = getIntent();
        loginId = inIntent.getStringExtra("loginId");

        Log.d(TAG, "현재 Lat : "  + nowLat);
        Log.d(TAG, "현재 Lon : "  + nowLon);

        anotherReportBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        toAddReportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addReportIntent = new Intent(getApplicationContext(), AddReportDiscountParkActivity.class);
                addReportIntent.putExtra("loginId", loginId);
                startActivity(addReportIntent);
            }
        });

        toMyreportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toMyReportsIntent = new Intent(getApplicationContext(), MyReportDiscountParkActivity.class);
                toMyReportsIntent.putExtra("loginId", loginId);
                startActivity(toMyReportsIntent);
            }
        });

        anotherReportDistanceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                if (selectedItem.equals("무제한")) {
                    getReports(0, nowLat, nowLon);
                }
                else {
                    int km = Integer.parseInt(selectedItem.substring(0, 1));
                    getReports (km, nowLat, nowLon);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        anotherReportListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                /*
                Intent toReportDiscountParkInformationIntent = new Intent(getApplicationContext(), ReportDiscountParkInformationActivity.class);
                toReportDiscountParkInformationIntent.putExtra("id", loginId);
                toReportDiscountParkInformationIntent.putExtra("documentId", ((ReportDiscountParkItem) ra.getItem(position)).getDocumentId());
                startActivity(toReportDiscountParkInformationIntent);
                 */
            }
        });
    }

    public void getReports (int distanceKm, double nowLat, double nowLon) {

        ReportDiscountParkAdapter ra = new ReportDiscountParkAdapter(AnotherReportDiscountParkActivity.this);
        anotherReportListView.setAdapter(ra);
        FirestoreDatabase fd = new FirestoreDatabase();

        fd.loadAnotherReports(distanceKm, nowLat, nowLon, loginId, new OnFirestoreDataLoadedListener() {
            @Override
            public void onDataLoaded(Object data) {
                ArrayList<HashMap<String, Object>> myReports = (ArrayList<HashMap<String, Object>>) data;
                for (HashMap<String, Object> oneReport : myReports) {
                    String reporterId = (String) oneReport.get("reporterId");
                    String parkName = (String) oneReport.get("parkName");
                    String condition = (String) oneReport.get("parkCondition");
                    long discount = (long) oneReport.get("parkDiscount");
                    long ratePerfectCount = (long) oneReport.get("ratePerfectCount");
                    long rateMistakeCount = (long) oneReport.get("rateMistakeCount");
                    long rateWrongCount = (long) oneReport.get("rateWrongCount");
                    boolean isCancelled = (boolean) oneReport.get("isCancelled");
                    boolean isApproval = (boolean) oneReport.get("isApproval");
                    Timestamp upTime = (Timestamp) oneReport.get("upTime");
                    String poiID = (String) oneReport.get("poiID");
                    String documentId = (String) oneReport.get("documentId");
                    ra.addItem(new ReportDiscountParkItem(reporterId, parkName, condition, discount, ratePerfectCount, rateMistakeCount, rateWrongCount, isCancelled, isApproval, upTime, poiID, documentId));
                }
            }
            @Override
            public void onDataLoadError(String errorMessage) {
            }
        });
    }

    @Override
    public void onLocationChange(Location location) {
        nowLat = location.getLatitude();
        Log.d(ContentValues.TAG, "onLocationChange 호출됨 : nowLat(경도) = " + nowLat);

        nowLon = location.getLongitude();
        Log.d(ContentValues.TAG, "onLocationChange 호출됨 : nowLon(위도) = " + nowLon);
    }
}