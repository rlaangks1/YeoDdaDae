package com.bucheon.yeoddadae;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.HashMap;

public class MyReservationActivity extends AppCompatActivity {
    String loginId;

    Button myReservationBackBtn;
    ListView myReservationListView;
    Button detailBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_reservation);

        Intent inIntent = getIntent();
        loginId = inIntent.getStringExtra("loginId");

        myReservationBackBtn = findViewById(R.id.myReservationBackBtn);
        myReservationListView = findViewById(R.id.myReservationListView);
        detailBtn = findViewById(R.id.detailBtn);

        FirestoreDatabase fd = new FirestoreDatabase();
        ReservationAdapter ra = new ReservationAdapter();
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


                    ra.addItem(new ReservationItem(id, isCancelled, shareParkDocumentName, reservationTime, upTime));
                }
            }

            @Override
            public void onDataLoadError(String errorMessage) {

            }
        });

        myReservationBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        detailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}
