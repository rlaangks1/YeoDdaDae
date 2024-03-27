package com.bucheon.yeoddadae;

import static com.google.android.exoplayer2.ExoPlayerLibraryInfo.TAG;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.skt.Tmap.TMapView;

public class MainActivity extends AppCompatActivity implements SttService.SttCallback {

    final int loginIntentRequestCode = 1;

    boolean apiKeyCertified;

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

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SttService.SttBinder binder = (SttService.SttBinder) service;
            sttService = binder.getService();
            sttService.setSttCallback(sttCallback);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // 필요한 경우 연결이 끊겼을 때 처리
        }
    };

    private static String API_KEY = "iqTSQ2hMuj8E7t2sy3WYA5m73LuX4iUD5iHgwRGf";

    Button toLoginBtn;
    TextView nowIdTxt;
    TextView isAdminTxt;
    TextView toSttBtn;
    TextView sttStatus;
    Button toFindParkBtn;
    Button toFindGasStationBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toLoginBtn = findViewById(R.id.toLoginBtn);
        nowIdTxt = findViewById(R.id.nowId);
        isAdminTxt = findViewById(R.id.isAdmin);
        toSttBtn = findViewById(R.id.toSttBtn);
        sttStatus = findViewById(R.id.sttStatus);
        toFindParkBtn = findViewById(R.id.toFindParkBtn);
        toFindGasStationBtn = findViewById(R.id.toFindGasStationBtn);

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

        nowIdTxt.setText(loginId);
        isAdminTxt.setText(Boolean.toString(isAdmin));

        serviceIntent = new Intent(this, SttService.class);
        startService(serviceIntent);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        toLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (loginId == null) {
                    Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivityForResult(loginIntent, loginIntentRequestCode);
                } else {
                    loginId = null;
                    isAdmin = false;

                    nowIdTxt.setText(loginId);
                    isAdminTxt.setText(Boolean.toString(isAdmin));

                    toLoginBtn.setText("로그인");
                }
            }
        });

        toSttBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sttService != null) {
                    sttService.startListeningForMainCommand();
                }
            }
        });

        toFindParkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (apiKeyCertified) {
                    Intent findParkIntent = new Intent(getApplicationContext(), FindParkActivity.class);
                    findParkIntent.putExtra("SortBy", 1);
                    startActivity(findParkIntent);
                }
                else {
                    Log.d(TAG, "인증되지 않아 액티비티 start 할 수 없음");
                }
            }
        });

        toFindGasStationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (apiKeyCertified) {
                    Intent findGasStationIntent = new Intent(getApplicationContext(), FindGasStationActivity.class);
                    findGasStationIntent.putExtra("SortBy", 1);
                    startActivity(findGasStationIntent);
                }
                else {
                    Log.d(TAG, "인증되지 않아 액티비티 start 할 수 없음");
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        sttService.stopListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startService(serviceIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == loginIntentRequestCode) {
            if (resultCode == RESULT_OK) {
                loginId = data.getStringExtra("loginId");
                isAdmin = data.getBooleanExtra("isAdmin", false);

                nowIdTxt.setText(loginId);
                isAdminTxt.setText(Boolean.toString(isAdmin));

                toLoginBtn.setText("로그아웃");
            }
        }
        startService(serviceIntent);
    }

    @Override
    public void onMainCommandReceived(String mainCommand) {
        Log.d("TAG", "MainActivity에서 받은 명령: " + mainCommand);

        if (mainCommand.contains("로그인")) {
            if (loginId == null) {
                Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivityForResult(loginIntent, loginIntentRequestCode);
            } else {
                Log.d("TAG", "이미 로그인 상태임");
            }
        }
        else if (mainCommand.contains("로그아웃")) {
            if (loginId != null) {
                loginId = null;
                isAdmin = false;

                Button toLoginBtn = findViewById(R.id.toLoginBtn);
                TextView nowIdTxt = findViewById(R.id.nowId);
                TextView isAdminTxt = findViewById(R.id.isAdmin);

                nowIdTxt.setText(loginId);
                isAdminTxt.setText(Boolean.toString(isAdmin));

                toLoginBtn.setText("로그인");
            }
            else {
                Log.d("TAG", "이미 로그아웃 상태임");
            }
        }
        // 사투리도 처리????
        else if (mainCommand.contains("주유소")) {
            if (mainCommand.contains("가까") || mainCommand.contains("가깝") || mainCommand.contains("근접") || mainCommand.contains("인접") || mainCommand.contains("거리")) {
                Intent findGasStationIntent = new Intent(getApplicationContext(), FindGasStationActivity.class);
                findGasStationIntent.putExtra("SortBy", 1);
                startActivity(findGasStationIntent);
            }
            else if (mainCommand.contains("평점") || mainCommand.contains("별점") || mainCommand.contains("리뷰")) {
                Intent findGasStationIntent = new Intent(getApplicationContext(), FindGasStationActivity.class);
                findGasStationIntent.putExtra("SortBy", 2);
                startActivity(findGasStationIntent);
            }
            else if (mainCommand.contains("휘발") || mainCommand.contains("가솔린")) {
                Intent findGasStationIntent = new Intent(getApplicationContext(), FindGasStationActivity.class);
                findGasStationIntent.putExtra("SortBy", 3);
                startActivity(findGasStationIntent);
            }
            else if (mainCommand.contains("경유") || mainCommand.contains("디젤")) {
                Intent findGasStationIntent = new Intent(getApplicationContext(), FindGasStationActivity.class);
                findGasStationIntent.putExtra("SortBy", 4);
                startActivity(findGasStationIntent);
            }
            else if (mainCommand.contains("LPG") || mainCommand.contains("엘피지")) {
                Intent findGasStationIntent = new Intent(getApplicationContext(), FindGasStationActivity.class);
                findGasStationIntent.putExtra("SortBy", 5);
                startActivity(findGasStationIntent);
            }
        }
        else {}
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
        unbindService(serviceConnection);
    }
}