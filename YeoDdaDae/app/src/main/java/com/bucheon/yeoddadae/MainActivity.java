package com.bucheon.yeoddadae;

import static com.google.android.exoplayer2.ExoPlayerLibraryInfo.TAG;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.AudioManager;
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

import com.skt.Tmap.TMapTapi;
import com.skt.Tmap.TMapView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements SttService.SttCallback {
    boolean recordAudioPermissionGranted = false;
    private int PERMISSION_REQUEST_CODE = 1;

    boolean apiKeyCertified = true;
    private String API_KEY = "iqTSQ2hMuj8E7t2sy3WYA5m73LuX4iUD5iHgwRGf";

    String loginId = null;
    boolean isAdmin = false;

    TextView nowIdTxt;
    TextView isAdminTxt;
    TextView sttStatus;
    ImageButton toSttImgBtn;
    ImageButton toFindParkImgBtn;
    ImageButton toFindGasStationImgBtn;
    ImageButton toMyReservationImgBtn;
    ImageButton toShareParkBtn;
    Button toYdPointChargeBtn;
    ImageButton logoutBtn;
    ImageButton toMyReportDiscountParkBtn;

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
        toMyReservationImgBtn = findViewById(R.id.toMyReservationImgBtn);
        toShareParkBtn = findViewById(R.id.toShareParkBtn);
        toMyReportDiscountParkBtn = findViewById(R.id.toMyReportDiscountParkBtn);
        toYdPointChargeBtn = findViewById(R.id.toYdPointChargeBtn);
        logoutBtn = findViewById(R.id.logoutBtn);

        Intent inIntent = getIntent();
        loginId = inIntent.getStringExtra("loginId");
        isAdmin = inIntent.getBooleanExtra("isAdmin", false);

        if (savedInstanceState == null) { // 액티비티 최초 실행인지 확인
            // TMapAPI 인증 (앱 종료까지 유효)
            TMapTapi tmaptapi = new TMapTapi(this);
            tmaptapi.setOnAuthenticationListener(new TMapTapi.OnAuthenticationListenerCallback() {
                @Override
                public void SKTMapApikeySucceed() {
                    Log.d(TAG, "키 인증 성공");
                    apiKeyCertified = true;
                }

                @Override
                public void SKTMapApikeyFailed(String s) {
                    Log.d(TAG, "키 인증 실패");
                    finish();
                    apiKeyCertified = false;
                }
            });
            tmaptapi.setSKTMapAuthentication(API_KEY);
        }

        if (isAdmin) {
            Intent toAdminIntent =new Intent(getApplicationContext(), AdminMainActivity.class);
            toAdminIntent.putExtra("loginId", loginId);
            startActivity(toAdminIntent);
            finish();
        }

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
                } else {
                    Toast.makeText(getApplicationContext(), "설정에서 마이크 권한을 부여하세요", Toast.LENGTH_SHORT).show();
                }
            }
        });

        toFindParkImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (apiKeyCertified) {
                    Intent findParkIntent = new Intent(getApplicationContext(), FindParkActivity.class);
                    findParkIntent.putExtra("loginId", loginId);
                    findParkIntent.putExtra("SortBy", 1);
                    startActivity(findParkIntent);
                } else {
                    Toast.makeText(getApplicationContext(), "API 키가 인증되지 않았습니다", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(getApplicationContext(), "API 키가 인증되지 않았습니다", Toast.LENGTH_SHORT).show();
                }
            }
        });

        toMyReservationImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (apiKeyCertified) {
                    Intent myReservationIntent = new Intent(getApplicationContext(), MyReservationActivity.class);
                    myReservationIntent.putExtra("loginId", loginId);
                    startActivity(myReservationIntent);
                }
                else {
                    Toast.makeText(getApplicationContext(), "API 키가 인증되지 않았습니다", Toast.LENGTH_SHORT).show();
                }
            }
        });

        toShareParkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (apiKeyCertified) {
                    Intent shareParkIntent = new Intent(getApplicationContext(), MyShareParkActivity.class);
                    shareParkIntent.putExtra("loginId", loginId);
                    startActivity(shareParkIntent);
                }
                else {
                    Toast.makeText(getApplicationContext(), "API 키가 인증되지 않았습니다", Toast.LENGTH_SHORT).show();
                }
            }
        });

        toMyReportDiscountParkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (apiKeyCertified) {
                    Intent AnotherReportDiscountParkIntent = new Intent(getApplicationContext(), AnotherReportDiscountParkActivity.class);
                    AnotherReportDiscountParkIntent.putExtra("loginId", loginId);
                    startActivity(AnotherReportDiscountParkIntent);
                }
                else {
                    Toast.makeText(getApplicationContext(), "API 키가 인증되지 않았습니다", Toast.LENGTH_SHORT).show();
                }
            }
        });

        toYdPointChargeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ydPointChargeIntent = new Intent(getApplicationContext(), YdPointChargeActivity.class);
                ydPointChargeIntent.putExtra("loginId", loginId);
                startActivity(ydPointChargeIntent);
            }
        });

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent logoutIntent = new Intent(getApplicationContext(), StartActivity.class);
                loginId = null;
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
         * if (requestCode == loginIntentRequestCode) {
         * if (resultCode == RESULT_OK) {
         * loginId = data.getStringExtra("loginId");
         * isAdmin = data.getBooleanExtra("isAdmin", false);
         *
         * if (loginId == null) {
         * nowIdTxt.setText("미 로그인 상태");
         * }
         * else {
         * nowIdTxt.setText(loginId);
         * }
         * isAdminTxt.setText("관리자여부 : " + isAdmin);
         *
         * toLoginBtn.setText("로그아웃");
         * }
         * }
         */
    }

    @Override
    public void onMainCommandReceived(String mainCommand) {
        Log.d("TAG", "MainActivity에서 받은 명령: " + mainCommand);

        if (mainCommand.contains("로그아웃")) { // 로그아웃
            if (loginId != null) {
                Intent logoutIntent = new Intent(getApplicationContext(), StartActivity.class);
                startActivity(logoutIntent);
                finish();
            }
        } else if (mainCommand.contains("주유")) { // 주유소찾기 (사투리도 처리????)
            if (apiKeyCertified) {
                if (mainCommand.contains("평점") || mainCommand.contains("별점") || mainCommand.contains("리뷰")) {
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
                } else {
                    Intent findGasStationIntent = new Intent(getApplicationContext(), FindGasStationActivity.class);
                    findGasStationIntent.putExtra("SortBy", 1);
                    startActivity(findGasStationIntent);
                }
            }
        } else { // 모두 아님 (서비스 다시시작)
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
        } else {
            Log.d(TAG, "권한 없음. 요청");
            String[] permissionArr = { android.Manifest.permission.RECORD_AUDIO };
            requestPermissions(permissionArr, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "권한요청에서 허가");
            recordAudioPermissionGranted = true;
        } else {
            Toast.makeText(getApplicationContext(), "마이크 권한 거부되었습니다", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "권한요청에서 거부or문제");
        }
    }
}