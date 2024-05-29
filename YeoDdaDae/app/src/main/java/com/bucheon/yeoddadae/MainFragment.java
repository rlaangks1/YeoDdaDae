package com.bucheon.yeoddadae;

import static com.google.android.exoplayer2.ExoPlayerLibraryInfo.TAG;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapTapi;
import com.skt.Tmap.TMapView;

public class MainFragment extends Fragment implements SttService.SttCallback {
    private int PERMISSION_REQUEST_CODE = 1;
    boolean recordAudioPermissionGranted = false;
    boolean apiKeyCertified;
    String loginId;
    SttDialog sd;
    FragmentToActivityListener dataPasser;

    ImageButton toSttImgBtn;
    ImageButton toFindParkImgBtn;
    ImageButton toFindGasStationImgBtn;
    ImageButton toSharedParkImgBtn;
    ImageButton toMyReportDiscountParkImgBtn;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ImageButton menubarBtn;

    private Intent serviceIntent;
    private SttService sttService;

    private SttService.SttCallback sttCallback = new SttService.SttCallback() {
        @Override
        public void onMainCommandReceived(String mainCommand) {
            MainFragment.this.onMainCommandReceived(mainCommand);
        }

        @Override
        public void onUpdateUI(String message) {
            MainFragment.this.onUpdateUI(message);
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

    public MainFragment(boolean apiKeyCertified, String loginId) {
        this.apiKeyCertified = apiKeyCertified;
        this.loginId = loginId;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        toSttImgBtn = view.findViewById(R.id.toSttImgBtn);
        toFindParkImgBtn = view.findViewById(R.id.toFindParkImgBtn);
        toFindGasStationImgBtn = view.findViewById(R.id.toFindGasStationImgBtn);
        toSharedParkImgBtn = view.findViewById(R.id.toSharedParkImgBtn);
        toMyReportDiscountParkImgBtn = view.findViewById(R.id.toMyReportDiscountParkImgBtn);
        drawerLayout = view.findViewById(R.id.drawer_layout);
        navigationView = view.findViewById(R.id.navigation_view);
        menubarBtn = view.findViewById(R.id.menubarBtn);

        // 네비게이션뷰 아이디 표시
        View headerView = navigationView.getHeaderView(0);
        TextView navHeaderUsername = headerView.findViewById(R.id.nowIdTxt);
        if (loginId != null) {
            navHeaderUsername.setText(loginId);
        }

        sd = new SttDialog(getActivity(), new SttDialogListener() {
            @Override
            public void onMessageSend(String message) {
                if (message.equals("버튼클릭")) {
                    sd.setSttStatusTxt("메인 명령어 듣는 중");
                    sttService.startListeningForMainCommand();
                } else if (message.equals("닫기")) {
                    sttService.stopContinuousListening();
                    sttService.startListeningForWakeUpWord();
                }
            }
        });

        serviceIntent = new Intent(getActivity(), SttService.class);
        checkPermission();
        if (recordAudioPermissionGranted) {
            getActivity().startService(serviceIntent);
            getActivity().bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
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
                int id = item.getItemId();
                if (id == R.id.nav_logout) {
                    dataPasser.onDataPassed("로그아웃");
                }
                return false;
            }
        });

        toSttImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermission();
                if (recordAudioPermissionGranted) {
                    sttService.startListeningForMainCommand();
                    sd.show();
                    sd.setSttStatusTxt("메인 명령어 듣는 중");
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "설정에서 마이크 권한을 부여하세요", Toast.LENGTH_SHORT).show();
                }
            }
        });

        toFindParkImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (apiKeyCertified) {
                    Intent findParkIntent = new Intent(getActivity().getApplicationContext(), FindParkActivity.class);
                    findParkIntent.putExtra("loginId", loginId);
                    findParkIntent.putExtra("SortBy", 1);
                    startActivity(findParkIntent);
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "API 키가 인증되지 않았습니다", Toast.LENGTH_SHORT).show();
                }
            }
        });

        toFindGasStationImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (apiKeyCertified) {
                    Intent findGasStationIntent = new Intent(getActivity().getApplicationContext(), FindGasStationActivity.class);
                    findGasStationIntent.putExtra("SortBy", 1);
                    startActivity(findGasStationIntent);
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "API 키가 인증되지 않았습니다", Toast.LENGTH_SHORT).show();
                }
            }
        });

        toSharedParkImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (apiKeyCertified) {
                    Intent myReservationIntent = new Intent(getActivity().getApplicationContext(), ShareParkActivity.class);
                    myReservationIntent.putExtra("loginId", loginId);
                    startActivity(myReservationIntent);
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "API 키가 인증되지 않았습니다", Toast.LENGTH_SHORT).show();
                }
            }
        });

        toSharedParkImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                if (apiKeyCertified) {
                    Intent shareParkIntent = new Intent(getActivity().getApplicationContext(), MyShareParkActivity.class);
                    shareParkIntent.putExtra("loginId", loginId);
                    startActivity(shareParkIntent);
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "API 키가 인증되지 않았습니다", Toast.LENGTH_SHORT).show();
                }

                 */
            }
        });

        toMyReportDiscountParkImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (apiKeyCertified) {
                    Intent AnotherReportDiscountParkIntent = new Intent(getActivity().getApplicationContext(), AnotherReportDiscountParkActivity.class);
                    AnotherReportDiscountParkIntent.putExtra("loginId", loginId);
                    startActivity(AnotherReportDiscountParkIntent);
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "API 키가 인증되지 않았습니다", Toast.LENGTH_SHORT).show();
                }
            }
        });

        /*
        toYdPointChargeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ydPointChargeIntent = new Intent(getActivity().getApplicationContext(), YdPointChargeActivity.class);
                ydPointChargeIntent.putExtra("loginId", loginId);
                startActivity(ydPointChargeIntent);
            }
        });
        toYdPointHistoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        */

        return view;
    }
    @Override
    public void onPause() {
        super.onPause();
        if (recordAudioPermissionGranted) {
            sttService.stopContinuousListening();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (recordAudioPermissionGranted) {
            getActivity().startService(serviceIntent);
        }
    }

    @Override
    public void onMainCommandReceived(String mainCommand) {
        Log.d("TAG", "MainFragment에서 받은 명령: " + mainCommand);

        if (mainCommand.contains("로그아웃")) { // 로그아웃
            if (loginId != null) {
                Intent logoutIntent = new Intent(getActivity().getApplicationContext(), StartActivity.class);
                startActivity(logoutIntent);
                getActivity().finish();
            }
        } else if (mainCommand.contains("주유")) { // 주유소찾기 (사투리도 처리????)
            if (apiKeyCertified) {
                if (mainCommand.contains("평점") || mainCommand.contains("별점") || mainCommand.contains("리뷰")) {
                    Intent findGasStationIntent = new Intent(getActivity().getApplicationContext(), FindGasStationActivity.class);
                    findGasStationIntent.putExtra("SortBy", 2);
                    startActivity(findGasStationIntent);
                } else if (mainCommand.contains("휘발") || mainCommand.contains("가솔린")) {
                    Intent findGasStationIntent = new Intent(getActivity().getApplicationContext(), FindGasStationActivity.class);
                    findGasStationIntent.putExtra("SortBy", 3);
                    startActivity(findGasStationIntent);
                } else if (mainCommand.contains("경유") || mainCommand.contains("디젤")) {
                    Intent findGasStationIntent = new Intent(getActivity().getApplicationContext(), FindGasStationActivity.class);
                    findGasStationIntent.putExtra("SortBy", 4);
                    startActivity(findGasStationIntent);
                } else {
                    Intent findGasStationIntent = new Intent(getActivity().getApplicationContext(), FindGasStationActivity.class);
                    findGasStationIntent.putExtra("SortBy", 1);
                    startActivity(findGasStationIntent);
                }
            }
        } else {
            sd.setSttStatusTxt(mainCommand + "\n알 수 없는 명령어입니다\n'가까운 주유소 찾아줘'와 같이 말씀해보세요");
        }
    }

    @Override
    public void onUpdateUI(String message) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (message.equals("메인명령어듣는중")) {
                    sd.show();
                    sd.setSttStatusTxt("메인 명령어 듣는 중");
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (serviceConnected) { // 서비스에 연결되어 있다면
            getActivity().unbindService(serviceConnection); // 서비스 언바인딩
            serviceConnected = false; // 플래그 재설정
        }
    }

    private void checkPermission() {
        if (getActivity().checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "권한 있음");
            recordAudioPermissionGranted = true;
        } else {
            Log.d(TAG, "권한 없음. 요청");
            String[] permissionArr = { Manifest.permission.RECORD_AUDIO };
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
            Toast.makeText(getActivity().getApplicationContext(), "마이크 권한 거부되었습니다", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "권한요청에서 거부or문제");
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // context가 OnDataPass 인터페이스를 구현하는지 확인 후, dataPasser에 할당
        if (context instanceof FragmentToActivityListener) {
            dataPasser = (FragmentToActivityListener) context;
        } else {
            throw new ClassCastException(context.toString() + " must implement OnDataPass");
        }
    }
}
