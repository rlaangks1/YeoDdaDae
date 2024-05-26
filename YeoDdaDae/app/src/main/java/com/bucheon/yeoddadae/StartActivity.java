package com.bucheon.yeoddadae;

import static com.google.android.exoplayer2.ExoPlayerLibraryInfo.TAG;

import android.Manifest;
import android.app.Application;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.skt.Tmap.TMapView;

public class StartActivity extends AppCompatActivity {
    final int PERMISSION_REQUEST_CODE = 1;
    final int loginIntentRequestCode = 2;
    String loginId;
    boolean isAdmin;
    ImageButton toLoginBtn;
    Button loginSkipBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        toLoginBtn = findViewById(R.id.toLoginBtn);
        loginSkipBtn = findViewById(R.id.loginSkipBtn);

        toLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkConnected()) {
                    Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivityForResult(loginIntent, loginIntentRequestCode);
                }
                else {
                    Toast.makeText(getApplicationContext(), "인터넷 연결을 확인하세요", Toast.LENGTH_SHORT).show();
                }
            }
        });

        loginSkipBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkConnected()) {
                    checkPermission(); // 권한 확인. 권한이 이미 있으면 onRequestPermissionsResult는 호출되지 않음
                    // 권한이 이미 있을 경우 바로 proceedAfterPermissionGranted를 호출할 수 있지만,
                    // 이 경우는 onRequestPermissionsResult에서 권한이 있을 때 호출되므로 거기서 처리
                }
                else {
                    Toast.makeText(getApplicationContext(), "인터넷 연결을 확인하세요", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == loginIntentRequestCode && resultCode == RESULT_OK) {
            loginId = data.getStringExtra("loginId");
            isAdmin = data.getBooleanExtra("isAdmin", false);
            checkPermission(); // 권한 확인. 결과 처리는 onRequestPermissionsResult에서 진행
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private void proceedAfterPermissionGranted() {
        // 권한이 모두 승인된 후에 실행할 로직
        Intent intent;
        if (loginId != null && !loginId.isEmpty()) {
            // 로그인이 완료된 경우
            intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra("loginId", loginId);
            intent.putExtra("isAdmin", isAdmin);
        } else {
            // 로그인을 건너뛴 경우
            intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra("loginId", "user111");
            intent.putExtra("isAdmin", false);
        }
        startActivity(intent);
        finish();
    }

    private void checkPermission() {
        if (checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            Log.d(ContentValues.TAG, "권한 이미 있음");
            proceedAfterPermissionGranted();
        }
        else {
            Log.d(ContentValues.TAG, "권한 없음. 요청");
            String[] permissionArr = {android.Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.RECORD_AUDIO};
            requestPermissions(permissionArr, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            // 모든 필수 권한이 승인되었는지 확인
            boolean allPermissionsGranted = true;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }

            if (allPermissionsGranted) {
                Log.d(TAG, "권한요청에서 허가");
                proceedAfterPermissionGranted(); // 권한이 승인된 후에 처리
            } else {
                Toast.makeText(getApplicationContext(), "필수 권한이 거부되었습니다", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
