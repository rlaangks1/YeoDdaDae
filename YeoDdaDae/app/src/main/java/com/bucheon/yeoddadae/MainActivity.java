package com.bucheon.yeoddadae;

import static com.google.android.exoplayer2.ExoPlayerLibraryInfo.TAG;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.skt.Tmap.TMapView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements SttService.SttCallback {
    boolean recordAudioPermissionGranted = false;
    private int PERMISSION_REQUEST_CODE = 1;

    boolean apiKeyCertified;
    private String API_KEY = "iqTSQ2hMuj8E7t2sy3WYA5m73LuX4iUD5iHgwRGf";

    String loginId = null;
    boolean isAdmin = false;

    private Intent serviceIntent;

    private SttService sttService;

    private SttService.SttCallback sttCallback = new SttService.SttCallback() {
        @Override
        public void onMainCommandReceived(String mainCommand) {
            MainActivity.this.onMainCommandReceived(mainCommand);
        }

        @Override
        public void onUpdateUI(String message) {
            MainActivity.this.onUpdateUI(message);
        }
    };

    private boolean serviceConnected = false;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SttService.SttBinder binder = (SttService.SttBinder) service;
            sttService = binder.getService();
            sttService.setSttCallback(sttCallback);
            serviceConnected = true; // 서비스에 연결되었음을 표시
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceConnected = false; // 서비스 연결 해제됨을 표시
            // 필요한 경우 연결이 끊겼을 때 처리
        }
    };

    TextView nowIdTxt;
    TextView isAdminTxt;
    TextView sttStatus;
    ImageButton toSttImgBtn;
    ImageButton toFindParkImgBtn;
    ImageButton toFindGasStationImgBtn;
    Button toShareParkBtn;
    Button logoutBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nowIdTxt = findViewById(R.id.nowIdTxt);
        isAdminTxt = findViewById(R.id.isAdminTxt);
        sttStatus = findViewById(R.id.sttStatusTxt);
        toSttImgBtn = findViewById(R.id.toSttImgBtn);
        toFindParkImgBtn = findViewById(R.id.toFindParkImgBtn);
        toFindGasStationImgBtn = findViewById(R.id.toFindGasStationImgBtn);
        toShareParkBtn = findViewById(R.id.toShareParkBtn);
        logoutBtn = findViewById(R.id.logoutBtn);

        Intent inIntent = getIntent();
        loginId = inIntent.getStringExtra("loginId");
        isAdmin = inIntent.getBooleanExtra("isAdmin", false);

        // TMapView 인증 (앱 종료까지 유효)
        TMapView tMapView = new TMapView(this);
        tMapView.setSKTMapApiKey(API_KEY);
        tMapView.setOnApiKeyListener(new TMapView.OnApiKeyListenerCallback() {
            @Override
            public void SKTMapApikeySucceed() {
                Log.d(TAG, "키 인증 성공");
                apiKeyCertified = true;
            }

            @Override
            public void SKTMapApikeyFailed(String s) {
                Log.d(TAG, "키 인증 실패");
                apiKeyCertified = false;
            }
        });

        serviceIntent = new Intent(this, SttService.class);
        checkPermission();

        if (recordAudioPermissionGranted) {
            startService(serviceIntent);
            bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        }

        if (loginId != null) {
            nowIdTxt.setText(loginId);
            isAdminTxt.setText("관리자여부 : " + isAdmin);
        }

        toSttImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermission();
                if (recordAudioPermissionGranted) {
                    if (sttService != null) {
                        sttService.startListeningForMainCommand();
                    }
                }
            }
        });

        toFindParkImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (apiKeyCertified) {
                    Intent findParkIntent = new Intent(getApplicationContext(), FindParkActivity.class);
                    findParkIntent.putExtra("SortBy", 1);
                    startActivity(findParkIntent);
                } else {
                    Log.d(TAG, "인증되지 않아 액티비티 start 할 수 없음");
                }
            }
        });

        toFindGasStationImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (apiKeyCertified) {
                    Intent findGasStationIntent = new Intent(getApplicationContext(), FindGasStationActivity.class);
                    findGasStationIntent.putExtra("SortBy", 1);
                    startActivity(findGasStationIntent);
                } else {
                    Log.d(TAG, "인증되지 않아 액티비티 start 할 수 없음");
                }
            }
        });

        toShareParkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareParkIntent = new Intent(getApplicationContext(), ShareParkActivity.class);
                startActivity(shareParkIntent);
            }
        });

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent logoutIntent = new Intent (getApplicationContext(), StartActivity.class);
                startActivity(logoutIntent);
                finish();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (recordAudioPermissionGranted) {
            sttService.stopContinuousListening();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (recordAudioPermissionGranted) {
            startService(serviceIntent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /*
        if (requestCode == loginIntentRequestCode) {
            if (resultCode == RESULT_OK) {
                loginId = data.getStringExtra("loginId");
                isAdmin = data.getBooleanExtra("isAdmin", false);

                if (loginId == null) {
                    nowIdTxt.setText("미 로그인 상태");
                }
                else {
                    nowIdTxt.setText(loginId);
                }
                isAdminTxt.setText("관리자여부 : " + isAdmin);

                toLoginBtn.setText("로그아웃");
            }
        }
        */
    }

    @Override
    public void onMainCommandReceived(String mainCommand) {
        Log.d("TAG", "MainActivity에서 받은 명령: " + mainCommand);

        // 로그아웃
        if (mainCommand.contains("로그아웃")) {
            if (loginId != null) {
                Intent logoutIntent = new Intent (getApplicationContext(), StartActivity.class);
                startActivity(logoutIntent);
                finish();
            }
        }

        // 주유소찾기
        // 사투리도 처리????
        else if (mainCommand.contains("주유소")) {
            if (mainCommand.contains("가까") || mainCommand.contains("가깝") || mainCommand.contains("근접")
                    || mainCommand.contains("인접") || mainCommand.contains("거리")) {
                Intent findGasStationIntent = new Intent(getApplicationContext(), FindGasStationActivity.class);
                findGasStationIntent.putExtra("SortBy", 1);
                startActivity(findGasStationIntent);
            } else if (mainCommand.contains("평점") || mainCommand.contains("별점") || mainCommand.contains("리뷰")) {
                Intent findGasStationIntent = new Intent(getApplicationContext(), FindGasStationActivity.class);
                findGasStationIntent.putExtra("SortBy", 2);
                startActivity(findGasStationIntent);
            } else if (mainCommand.contains("휘발") || mainCommand.contains("가솔린")) {
                Intent findGasStationIntent = new Intent(getApplicationContext(), FindGasStationActivity.class);
                findGasStationIntent.putExtra("SortBy", 3);
                startActivity(findGasStationIntent);
            } else if (mainCommand.contains("경유") || mainCommand.contains("디젤")) {
                Intent findGasStationIntent = new Intent(getApplicationContext(), FindGasStationActivity.class);
                findGasStationIntent.putExtra("SortBy", 4);
                startActivity(findGasStationIntent);
            } else if (mainCommand.contains("LPG") || mainCommand.contains("엘피지")) {
                Intent findGasStationIntent = new Intent(getApplicationContext(), FindGasStationActivity.class);
                findGasStationIntent.putExtra("SortBy", 5);
                startActivity(findGasStationIntent);
            }
        }

        // 모두 아님 (서비스 다시시작)
        else {
            startService(serviceIntent);
        }
    }

    @Override
    public void onUpdateUI(String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                sttStatus.setText(message);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceConnected) { // 서비스에 연결되어 있다면
            unbindService(serviceConnection); // 서비스 언바인딩
            serviceConnected = false; // 플래그 재설정
        }
    }

    private void checkPermission() {
        if (checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "권한 있음");
            recordAudioPermissionGranted = true;
        }
        else {
            Log.d(TAG, "권한 없음. 요청");
            String[] permissionArr = {android.Manifest.permission.RECORD_AUDIO};
            requestPermissions(permissionArr, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "권한요청에서 허가");
            recordAudioPermissionGranted = true;
        }
        else {
            Log.d (TAG, "권한요청에서 거부or문제");
        }
    }
}