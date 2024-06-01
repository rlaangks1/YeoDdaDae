package com.bucheon.yeoddadae;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class AdminMainActivity extends AppCompatActivity {

    String loginId;

    Button toApproveShareParkBtn;
    Button toApproveReportBtn;
    Button toStatisticsBtn;
    Button adminLogoutBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        toApproveShareParkBtn = findViewById(R.id.toApproveShareParkBtn);
        toApproveReportBtn = findViewById(R.id.toApproveReportBtn);
        toStatisticsBtn = findViewById(R.id.toStatisticsBtn);
        adminLogoutBtn = findViewById(R.id.adminLogoutBtn);

        Intent inIntent = getIntent();
        loginId = inIntent.getStringExtra("loginId");

        toApproveShareParkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent approveShareParkIntent = new Intent(getApplicationContext(), ApproveShareParkActivity.class);
                startActivity(approveShareParkIntent);
            }
        });

        toApproveReportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent approveReportIntent = new Intent(getApplicationContext(), ApproveReportActivity.class);
                startActivity(approveReportIntent);

            }
        });

        toStatisticsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        adminLogoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginId = null;
                Intent logoutIntent = new Intent(getApplicationContext(), StartActivity.class);
                startActivity(logoutIntent);
                finish();
            }
        });
    }
}