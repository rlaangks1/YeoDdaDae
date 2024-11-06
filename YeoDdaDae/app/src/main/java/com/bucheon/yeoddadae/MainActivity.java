package com.bucheon.yeoddadae;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements FragmentToActivityListener, SttService.SttCallback {
    FirebaseAuth mAuth;
    FirebaseUser user;
    FirestoreDatabase fd;
    boolean apiKeyCertified;
    String loginId;

    Intent serviceIntent;
    SttService sttService;
    ServiceConnection serviceConnection;
    SttService.SttCallback sttCallback;
    SttDialog sd;

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

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        App app = (App) getApplication();
        apiKeyCertified = app.isApiKeyCertified();

        fd = new FirestoreDatabase();

        initSttService();

        if (savedInstanceState != null) { // 저장된 상태가 있는 경우 프래그먼트를 복원
            Log.d(TAG, "MainActivity 저장된 상태가 있음");
            String currentFragmentTag = savedInstanceState.getString("currentFragmentTag");
            FragmentManager fragmentManager = getSupportFragmentManager();
            Fragment currentFragment = fragmentManager.findFragmentByTag(currentFragmentTag);

            if (currentFragment != null) {
                fragmentManager.beginTransaction()
                        .replace(containerViewId, currentFragment, currentFragmentTag)
                        .commit();
            } else { // 만약 프래그먼트를 찾을 수 없는 경우 기본 메인 프래그먼트를 설정
                fragmentManager.beginTransaction()
                        .replace(containerViewId, new MainFragment(apiKeyCertified, loginId))
                        .commit();
            }
        }
        else { // 저장된 상태가 없는 경우 기본 메인 프래그먼트를 설정
            Log.d(TAG, "MainActivity 저장된 상태가 없음");
            getSupportFragmentManager().beginTransaction()
                    .replace(containerViewId, new MainFragment(apiKeyCertified, loginId))
                    .commit();
        }

        getNotification();

        bottomNavView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();
                String see = "";

                if (itemId == R.id.bt_main) {
                    selectedFragment = new MainFragment(apiKeyCertified, loginId);
                }
                else if (itemId == R.id.bt_mybook) {
                    selectedFragment = new MyReservationFragment(loginId);
                    see = "recentSawMyReservationTime";
                }
                else if (itemId == R.id.bt_my_shared_park) {
                    selectedFragment = new MyShareParkFragment(loginId);
                    see = "recentSawMyShareParkTime";
                }
                else if (itemId == R.id.bt_my_discount_park_report) {
                    selectedFragment = new MyReportDiscountParkFragment(loginId);
                    see = "recentSawMyReportParkTime";
                }
                else if (itemId == R.id.bt_point) {
                    selectedFragment = new MyYdPointFragment(loginId);
                }

                if (selectedFragment != null) {

                    if (!see.isEmpty()) {
                        fd.updateRecentSawTime(loginId, see, new OnFirestoreDataLoadedListener() {
                            @Override
                            public void onDataLoaded(Object data) {
                                getNotification();
                            }

                            @Override
                            public void onDataLoadError(String errorMessage) {
                                Log.d(TAG, errorMessage);
                                Toast.makeText(getApplicationContext(), "오류 발생", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    else {
                        getNotification();
                    }

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
    protected void onPause() {
        super.onPause();

        if (serviceConnection != null && serviceIntent != null) {
            unbindService(serviceConnection);
            stopService(serviceIntent);
            serviceConnection = null;
            serviceIntent = null;
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        initSttService();

        getNotification();
    }

    void initSttService() {
        sd = new SttDialog(this, new SttDialogListener() {
            @Override
            public void onMessageSend(String message) {
                if (message.equals("SttDialog버튼클릭")) {
                    sd.setSttStatusTxt("메인 명령어 듣는 중");
                    sttService.startListeningForMainCommand();
                }
                else if (message.equals("SttDialog닫힘")) {
                    sttService.startListeningForWakeUpWord();
                }
            }
        });

        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                SttService.SttBinder binder = (SttService.SttBinder) service;
                sttService = binder.getService();
                sttService.setSttCallback(sttCallback);
                Log.d(TAG, "MainActivity : STT 서비스 연결됨");
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.d(TAG, "MainActivity : STT 서비스 연결 해제됨");
            }
        };

        serviceIntent = new Intent(this, SttService.class);
        startService(serviceIntent);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        sttCallback = new SttService.SttCallback() {
            @Override
            public void onMainCommandReceived(String mainCommand) {
                MainActivity.this.onMainCommandReceived(mainCommand);
            }

            @Override
            public void onUpdateUI(String message) {
                MainActivity.this.onUpdateUI(message);
            }
        };
    }

    @Override
    public void onDataPassed(String data) { // Fragment에서 메시지 받기
        if (data.equals("로그아웃")) {
            App app = (App) getApplication();
            app.setLoginId(null);
            loginId = null;
            mAuth.signOut();
            Intent logoutIntent = new Intent(getApplicationContext(), StartActivity.class);
            startActivity(logoutIntent);
            finish();
        }
        else if (data.equals("비밀번호 변경")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("비밀번호 변경 확인");
            builder.setMessage("정말 비밀번호를 변경하시겠습니까?");
            builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    resetPassword();
                }
            });
            builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {}
            });
            AlertDialog dialog = builder.create();
            dialog.show();

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(MainActivity.this, R.color.sub));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(MainActivity.this, R.color.disable));
        }
        else if (data.equals("길찾기 기록")) {
            Intent usageHistoryIntent = new Intent(getApplicationContext(), UsageHistoryActivity.class);
            usageHistoryIntent.putExtra("id", loginId);
            startActivity(usageHistoryIntent);
        }
        else if (data.equals("회원 탈퇴")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("회원 탈퇴 확인");
            builder.setMessage("정말 탈퇴 하시겠습니까");
            builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    fd.withdrawalUser(loginId, new OnFirestoreDataLoadedListener() {
                        @Override
                        public void onDataLoaded(Object data) {
                            Toast.makeText(getApplicationContext(), "회원 탈퇴 되었습니다", Toast.LENGTH_SHORT).show();
                            finish();
                        }

                        @Override
                        public void onDataLoadError(String errorMessage) {
                            Log.d(TAG, errorMessage);
                            Toast.makeText(getApplicationContext(), "오류 발생", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
            builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(MainActivity.this, R.color.sub));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(MainActivity.this, R.color.disable));
        }
        else if (data.equals("stt버튼클릭")) {
            sttService.startListeningForMainCommand();
        }
    }

    void resetPassword() {
        mAuth.sendPasswordResetEmail(user.getEmail())
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), "이메일을 확인하여 비밀번호 변경 후 다시 로그인하세요", Toast.LENGTH_SHORT).show();
                        loginId = null;
                        mAuth.signOut();
                        Intent logoutIntent = new Intent(getApplicationContext(), StartActivity.class);
                        startActivity(logoutIntent);
                        finish();
                    }
                    else {
                        Log.d(TAG, "인증 이메일 전송 실패 : " + task.getException().getMessage());
                        Toast.makeText(getApplicationContext(), "인증 이메일 전송 실패", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void onMainCommandReceived(String mainCommand) {
        Log.d(TAG, "MainActivity에서 받은 명령: " + mainCommand);

        if (mainCommand.contains("로그아웃")) {
            sd.dismiss();
            mAuth.signOut();
            loginId = null;
            Intent logoutIntent = new Intent(getApplicationContext(), StartActivity.class);
            startActivity(logoutIntent);
            finish();
        }
        else if (mainCommand.contains("메인")) {
            sd.dismiss();
            bottomNavView.setSelectedItemId(R.id.bt_main);
        }
        else if (mainCommand.contains("예약")) {
            sd.dismiss();
            bottomNavView.setSelectedItemId(R.id.bt_mybook);
        }
        else if (mainCommand.contains("공유") && mainCommand.contains("주차")) {
            sd.dismiss();
            bottomNavView.setSelectedItemId(R.id.bt_my_shared_park);
        }
        else if (mainCommand.contains("제보")) {
            sd.dismiss();
            bottomNavView.setSelectedItemId(R.id.bt_my_discount_park_report);
        }
        else if (mainCommand.contains("포인트")) {
            sd.dismiss();
            bottomNavView.setSelectedItemId(R.id.bt_point);
        }
        else if (mainCommand.contains("주차")) {
            sd.dismiss();
            if (apiKeyCertified) {
                Intent findParkIntent = new Intent(getApplicationContext(), FindParkActivity.class);
                findParkIntent.putExtra("loginId", loginId);
                if (mainCommand.contains("사용료") || mainCommand.contains("이용료") || mainCommand.contains("주차비") || mainCommand.contains("가격") || mainCommand.contains("싼") || mainCommand.contains("싸") || mainCommand.contains("저렴")) {
                    findParkIntent.putExtra("SortBy", 2);
                }
                else {
                    findParkIntent.putExtra("SortBy", 1);
                }
                startActivity(findParkIntent);
            }
        }
        else if (mainCommand.contains("주유")) {
            sd.dismiss();
            if (apiKeyCertified) { // sortBy는 정렬기준 (1:거리순, 2:평점순, 3:휘발유가순, 4: 경유가순 5: 고급휘발유가순, 6: 고급경유가순
                Intent findGasStationIntent = new Intent(getApplicationContext(), FindGasStationActivity.class);
                if (mainCommand.contains("고급") && (mainCommand.contains("휘발") || mainCommand.contains("가솔린"))) {
                    findGasStationIntent.putExtra("SortBy", 4);
                }
                else if (mainCommand.contains("고급") && (mainCommand.contains("경유") || mainCommand.contains("디젤"))) {
                    findGasStationIntent.putExtra("SortBy", 5);
                }
                else if (mainCommand.contains("휘발") || mainCommand.contains("가솔린")) {
                    findGasStationIntent.putExtra("SortBy", 2);
                }
                else if (mainCommand.contains("경유") || mainCommand.contains("디젤")) {
                    findGasStationIntent.putExtra("SortBy", 3);
                }
                else {
                    findGasStationIntent.putExtra("SortBy", 1);
                }
                startActivity(findGasStationIntent);
            }
        }
        else {
            sd.changeToInactivateIcon();
            sd.setSttStatusTxt(mainCommand + "\n알 수 없는 명령어입니다\n'가까운 주유소 찾아줘'와 같이 말씀해보세요");
        }
    }

    @Override
    public void onUpdateUI(String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (message.equals("메인명령어듣는중")) {
                    if(!sd.isShowing()) {
                        sd.show();
                    }
                    sd.changeToActiveIcon();
                    sd.setSttStatusTxt("메인 명령어 듣는 중");
                }
                else if (message.equals("음성인식실패")) {
                    sd.changeToInactivateIcon();
                    sd.setSttStatusTxt("음성 인식 실패\n'가까운 주유소 찾아줘'와 같이 말씀해보세요");
                }
                else if (message.equals("타임아웃")) {
                    sd.changeToInactivateIcon();
                    sd.setSttStatusTxt("음성 인식 타임 아웃\n'가까운 주유소 찾아줘'와 같이 말씀해보세요");
                }
            }
        });
    }

    public void showBottomNavigationViewBadge(int count1, int count2, int count3) {
        int[] count = {count1, count2, count3};

        for (int i = 1; i < 4; i++) {
            int menuItemId = bottomNavView.getMenu().getItem(i).getItemId(); // 인덱스에 해당하는 메뉴 아이템의 ID 가져오기

            BadgeDrawable badge = bottomNavView.getOrCreateBadge(menuItemId);
            badge.setTextAppearance(R.style.CustomBadgeTextAppearance);

            if (count[i - 1] > 0) {
                badge.setVisible(true);
                badge.setNumber(count[i - 1]); // 뱃지에 표시할 숫자 설정
            } else {
                badge.setVisible(false); // showCount가 0이면 뱃지를 숨김
            }
        }
    }

    public void getNotification() {
        fd = new FirestoreDatabase();

        fd.loadNotification(loginId, new OnFirestoreDataLoadedListener() {
            @Override
            public void onDataLoaded(Object data) {
                int[] temp = (int[]) data;

                Log.d(TAG, "알림 수(예약,공유,제보) : " + temp[0] + ", " + temp[1] + ", " + temp[2]);

                showBottomNavigationViewBadge(temp[0], temp[1], temp[2]);
            }

            @Override
            public void onDataLoadError(String errorMessage) {
                Log.d(TAG, errorMessage);
                Toast.makeText(getApplicationContext(), "오류 발생", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // 현재 프래그먼트를 가져와서 태그를 저장합니다.
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(containerViewId);
        if (currentFragment != null) {
            outState.putString("currentFragmentTag", currentFragment.getTag());
        }
    }
}