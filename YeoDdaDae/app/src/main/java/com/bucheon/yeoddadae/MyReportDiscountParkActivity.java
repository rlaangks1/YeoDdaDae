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
    ListView myReportBackBtnListView;
    Button toAddReportBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_report_discount_park);

        myReportBackBtn = findViewById(R.id.myReportBackBtn);
        myReportBackBtnListView = findViewById(R.id.myReportBackBtnListView);
        toAddReportBtn = findViewById(R.id.toAddReportBtn);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent inIntent = getIntent();
        loginId = inIntent.getStringExtra("loginId");

        /*
        FirestoreDatabase fd = new FirestoreDatabase();
        ReservationAdapter ra = new ReservationAdapter(MyReportDiscountParkActivity.this);
        myReservationListView.setAdapter(ra);

        fd.loadMyReservations(loginId, new OnFirestoreDataLoadedListener() {
            @Override
            public void onDataLoaded(Object data) {
                ArrayList<HashMap<String, Object>> myReservations = (ArrayList<HashMap<String, Object>>) data;

                for (HashMap<String, Object> oneReservation : myReservations) {
                    String id = (String) oneReservation.get("id");
                    boolean isCancelled = (boolean) oneReservation.get("isCancelled");
                    String shareParkDocumentName = (String) oneReservation.get("shareParkDocumentName");
                    HashMap<String, ArrayList<String>> reservationTime = (HashMap<String, ArrayList<String>>) oneReservation.get("time");
                    Timestamp upTime = (Timestamp) oneReservation.get("upTime");
                    String documentId = (String) oneReservation.get("documentId");

                    ra.addItem(new ReservationItem(id, isCancelled, shareParkDocumentName, reservationTime, upTime, documentId));
                }
            }

            @Override
            public void onDataLoadError(String errorMessage) {
            }
        });

        myReservationListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent toReservationInformationActivityIntent = new Intent(getApplicationContext(), ReservationInformationActivity.class);
                toReservationInformationActivityIntent.putExtra("id", loginId);
                toReservationInformationActivityIntent.putExtra("documentId", ((ReservationItem) ra.getItem(position)).getDocumentId());
                startActivity(toReservationInformationActivityIntent);
            }
        });

         */

        myReportBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        toAddReportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
            }
        });
    }
}