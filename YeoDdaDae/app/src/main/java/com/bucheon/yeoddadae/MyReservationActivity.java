package com.bucheon.yeoddadae;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

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
