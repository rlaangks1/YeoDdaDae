package com.bucheon.yeoddadae;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class AdminMainActivity extends AppCompatActivity {

    String loginId;

    ImageButton toApproveShareParkBtn;
    ImageButton toApproveReportBtn;
    ImageButton toStatisticsBtn;
    ImageButton adminLogoutBtn;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ImageButton menubarBtn;

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

        menubarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.END);
            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_logout) {
                    loginId = null;
                    Intent logoutIntent = new Intent(getApplicationContext(), StartActivity.class);
                    startActivity(logoutIntent);
                    finish();
                }
                return false;
            }
        });

        // 네비게이션뷰 아이디 표시
        View headerView = navigationView.getHeaderView(0);
        TextView navHeaderUsername = headerView.findViewById(R.id.nowIdTxt);
        if (loginId != null) {
            navHeaderUsername.setText(loginId);
        }

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