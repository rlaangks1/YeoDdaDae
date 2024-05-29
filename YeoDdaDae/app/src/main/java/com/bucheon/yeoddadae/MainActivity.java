package com.bucheon.yeoddadae;

import static com.google.android.exoplayer2.ExoPlayerLibraryInfo.TAG;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.skt.Tmap.TMapTapi;
import com.skt.Tmap.TMapView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements FragmentToActivityListener {
    boolean apiKeyCertified = false;
    String loginId;

    int containerViewId;
    BottomNavigationView bottomNavView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        containerViewId = R.id.fragmentContainer;
        bottomNavView = findViewById(R.id.bottomNavView);

        Intent inIntent = getIntent();
        loginId = inIntent.getStringExtra("loginId");

        App app = (App) getApplication();
        apiKeyCertified = app.isApiKeyCertified();

        getSupportFragmentManager().beginTransaction()
                .replace(containerViewId, new MainFragment(apiKeyCertified, loginId))
                .commit();

        bottomNavView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();
                if (itemId == R.id.bt_main) {
                    selectedFragment = new MainFragment(apiKeyCertified, loginId);
                }
                else if (itemId == R.id.bt_mybook) {
                    selectedFragment = new MyReservationFragment(loginId);
                }
                else if (itemId == R.id.bt_my_shared_park) {
                    selectedFragment = new MyShareParkFragment(loginId);
                }
                else if (itemId == R.id.bt_my_discount_park_report) {
                    selectedFragment = new MyReportDiscountParkFragment(loginId);
                }
                else if (itemId == R.id.bt_point) {
                    // 포인트기록 선택 시 처리
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(containerViewId, selectedFragment)
                            .commit();
                    return true;
                }

                return false;
            }
        });
    }

    @Override
    public void onDataPassed(String data) {
        if (data.equals("로그아웃")) {
            loginId = null;
            Intent logoutIntent = new Intent(getApplicationContext(), StartActivity.class);
            startActivity(logoutIntent);
            finish();
        }
    }
}