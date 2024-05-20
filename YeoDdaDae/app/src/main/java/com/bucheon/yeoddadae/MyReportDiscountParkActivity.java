package com.bucheon.yeoddadae;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.HashMap;

public class MyReportDiscountParkActivity extends AppCompatActivity {
    String loginId;

    Button myReportBackBtn;
    ListView myReportListView;
    Button toAddReportBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_report_discount_park);

        myReportBackBtn = findViewById(R.id.myReportBackBtn);
        myReportListView = findViewById(R.id.myReportListView);
        toAddReportBtn = findViewById(R.id.toAddReportBtn);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent inIntent = getIntent();
        loginId = inIntent.getStringExtra("loginId");

        FirestoreDatabase fd = new FirestoreDatabase();
        ReportDiscountParkAdapter ra = new ReportDiscountParkAdapter(MyReportDiscountParkActivity.this);
        myReportListView.setAdapter(ra);

        fd.loadMyReports(loginId, new OnFirestoreDataLoadedListener() {
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

        myReportListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent toReportDiscountParkInformationIntent = new Intent(getApplicationContext(), ReportDiscountParkInformationActivity.class);
                toReportDiscountParkInformationIntent.putExtra("id", loginId);
                toReportDiscountParkInformationIntent.putExtra("documentId", ((ReportDiscountParkItem) ra.getItem(position)).getDocumentId());
                startActivity(toReportDiscountParkInformationIntent);
            }
        });

        myReportBackBtn.setOnClickListener(new View.OnClickListener() {
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
    }
}