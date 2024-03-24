package com.bucheon.yeoddadae;

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

public class MainActivity extends AppCompatActivity implements SttService.SttCallback {

    String loginId = null;
    boolean isAdmin = false;

    private Intent serviceIntent;

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

    private SttService sttService;

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

    final int loginIntentRequestCode = 1;

    Button toLoginBtn;
    TextView nowIdTxt;
    TextView isAdminTxt;
    TextView toSttBtn;
    TextView sttStatus;
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
        toFindGasStationBtn = findViewById(R.id.toFindGasStationBtn);

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

        toFindGasStationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent naviIntent = new Intent(getApplicationContext(), FindGasStationActivity.class);
                naviIntent.putExtra("sttSort", 1);
                startActivity(naviIntent);
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
                Intent naviIntent = new Intent(getApplicationContext(), FindGasStationActivity.class);
                naviIntent.putExtra("sttSort", 1);
                startActivity(naviIntent);
            }
            else if (mainCommand.contains("평점") || mainCommand.contains("별점") || mainCommand.contains("리뷰")) {
                Intent naviIntent = new Intent(getApplicationContext(), FindGasStationActivity.class);
                naviIntent.putExtra("sttSort", 2);
                startActivity(naviIntent);
            }
            else if (mainCommand.contains("휘발") || mainCommand.contains("가솔린")) {
                Intent naviIntent = new Intent(getApplicationContext(), FindGasStationActivity.class);
                naviIntent.putExtra("sttSort", 3);
                startActivity(naviIntent);
            }
            else if (mainCommand.contains("경유") || mainCommand.contains("디젤")) {
                Intent naviIntent = new Intent(getApplicationContext(), FindGasStationActivity.class);
                naviIntent.putExtra("sttSort", 4);
                startActivity(naviIntent);
            }
            else if (mainCommand.contains("LPG") || mainCommand.contains("엘피지")) {
                Intent naviIntent = new Intent(getApplicationContext(), FindGasStationActivity.class);
                naviIntent.putExtra("sttSort", 5);
                startActivity(naviIntent);
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