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

public class MyShareParkActivity extends AppCompatActivity {
    String loginId;

    Button myShareParkBackBtn;
    ListView myShareParkListView;
    Button myShareParkAddBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_share_park);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent inIntent = getIntent();
        loginId = inIntent.getStringExtra("loginId");

        myShareParkBackBtn = findViewById(R.id.myShareParkBackBtn);
        myShareParkListView = findViewById(R.id.myShareParkListView);
        myShareParkAddBtn = findViewById(R.id.myShareParkAddBtn);

        ShareParkAdapter spa = new ShareParkAdapter(MyShareParkActivity.this);
        myShareParkListView.setAdapter(spa);

        FirestoreDatabase fd = new FirestoreDatabase();
        fd.loadMyShareParks(loginId, new OnFirestoreDataLoadedListener() {
            @Override
            public void onDataLoaded(Object data) {
                ArrayList<HashMap<String, Object>> myShareParks = (ArrayList<HashMap<String, Object>>) data;

                for (HashMap<String, Object> oneSharePark : myShareParks) {
                    double lat = (double) oneSharePark.get("lat");
                    double lon = (double) oneSharePark.get("lon");
                    String parkDetailAddress = (String) oneSharePark.get("parkDetailAddress");
                    boolean isApproval = (boolean) oneSharePark.get("isApproval");
                    boolean isCancelled = (boolean) oneSharePark.get("isCancelled");
                    long price = (long) oneSharePark.get("price");
                    HashMap<String, ArrayList<String>> time = (HashMap<String, ArrayList<String>>) oneSharePark.get("time");
                    Timestamp upTime = (Timestamp) oneSharePark.get("upTime");
                    String documentId = (String) oneSharePark.get("documentId");

                    spa.addItem(new ShareParkItem(lat, lon, parkDetailAddress, isApproval, isCancelled, price, time, upTime, documentId));
                }
            }

            @Override
            public void onDataLoadError(String errorMessage) {

            }
        });

        myShareParkListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                /*
                Intent toReservationInformationActivityIntent = new Intent(getApplicationContext(), ReservationInformationActivity.class);
                toReservationInformationActivityIntent.putExtra("id", loginId);
                toReservationInformationActivityIntent.putExtra("documentId", ((ReservationItem) ra.getItem(position)).getDocumentId());
                startActivity(toReservationInformationActivityIntent);
                 */
            }
        });

        myShareParkBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        myShareParkAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareParkIntent = new Intent(getApplicationContext(), ShareParkActivity.class);
                shareParkIntent.putExtra("loginId", loginId);
                startActivity(shareParkIntent);
            }
        });
    }
}