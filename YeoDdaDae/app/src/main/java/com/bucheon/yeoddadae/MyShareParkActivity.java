package com.bucheon.yeoddadae;

import static com.google.android.exoplayer2.ExoPlayerLibraryInfo.TAG;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.HashMap;

public class MyShareParkActivity extends AppCompatActivity {
    String loginId;
    ShareParkAdapter spa;
    FirestoreDatabase fd;

    Button myShareParkBackBtn;
    ListView myShareParkListView;
    Button myShareParkAddBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_share_park);

        myShareParkBackBtn = findViewById(R.id.myShareParkBackBtn);
        myShareParkListView = findViewById(R.id.myShareParkListView);
        myShareParkAddBtn = findViewById(R.id.myShareParkAddBtn);

        Intent inIntent = getIntent();
        loginId = inIntent.getStringExtra("loginId");

        spa = new ShareParkAdapter(MyShareParkActivity.this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        spa.clearItem();

        fd = new FirestoreDatabase();

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
                    boolean isCalculated = (boolean) oneSharePark.get("isCalculated");
                    long price = (long) oneSharePark.get("price");
                    HashMap<String, ArrayList<String>> time = (HashMap<String, ArrayList<String>>) oneSharePark.get("time");
                    Timestamp upTime = (Timestamp) oneSharePark.get("upTime");
                    String documentId = (String) oneSharePark.get("documentId");

                    spa.addItem(new ShareParkItem(null, lat, lon, parkDetailAddress, isApproval, isCancelled, isCalculated, price, time, upTime, documentId));
                }

                myShareParkListView.setAdapter(spa);
            }

            @Override
            public void onDataLoadError(String errorMessage) {
                Log.d(TAG, errorMessage);
                Toast.makeText(getApplicationContext(), "오류 발생", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        myShareParkListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent toMyShareParkInformationIntent = new Intent(getApplicationContext(), MyShareParkInformationActivity.class);
                toMyShareParkInformationIntent.putExtra("id", loginId);
                toMyShareParkInformationIntent.putExtra("documentId", ((ShareParkItem) spa.getItem(position)).getDocumentId());
                startActivity(toMyShareParkInformationIntent);
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