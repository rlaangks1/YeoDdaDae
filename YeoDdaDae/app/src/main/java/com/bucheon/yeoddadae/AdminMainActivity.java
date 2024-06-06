package com.bucheon.yeoddadae;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AdminMainActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    FirebaseUser user;
    String loginId;

    ImageButton toApproveShareParkBtn;
    ImageButton toApproveReportBtn;
    ImageButton toStatisticsBtn;
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
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        menubarBtn = findViewById(R.id.menubarBtn);

        Intent inIntent = getIntent();
        loginId = inIntent.getStringExtra("loginId");

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        View headerView = navigationView.getHeaderView(0);
        TextView navHeaderUsername = headerView.findViewById(R.id.nowIdTxt);
        if (loginId != null) {
            navHeaderUsername.setText(loginId);
        }

        menubarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.END);
            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.nav_logout) {
                    loginId = null;
                    mAuth.signOut();
                    Intent logoutIntent = new Intent(getApplicationContext(), StartActivity.class);
                    startActivity(logoutIntent);
                    finish();
                }
                else if (item.getItemId() == R.id.nav_change_password) {
                    mAuth.sendPasswordResetEmail(user.getEmail())
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(), "이메일을 확인하여 비밀번호 변경 후 다시 로그인하세요", Toast.LENGTH_SHORT).show();
                                    loginId = null;
                                    mAuth.signOut();
                                    Intent logoutIntent = new Intent(getApplicationContext(), StartActivity.class);
                                    startActivity(logoutIntent);
                                    finish();
                                }
                                else {
                                    Log.d(ContentValues.TAG, "인증 이메일 전송 실패 : " + task.getException().getMessage());
                                    Toast.makeText(getApplicationContext(), "인증 이메일 전송 실패", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
                return false;
            }
        });

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
                Intent statisticsIntent = new Intent(getApplicationContext(), StatisticsActivity.class);
                startActivity(statisticsIntent);
            }
        });
    }
}