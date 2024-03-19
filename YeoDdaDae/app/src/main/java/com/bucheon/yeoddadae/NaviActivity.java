package com.bucheon.yeoddadae;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.tmapmobility.tmap.tmapsdk.ui.fragment.NavigationFragment;
import com.tmapmobility.tmap.tmapsdk.ui.util.TmapUISDK;

public class NaviActivity extends AppCompatActivity  {
    private static String CLIENT_ID = "";
    private static String API_KEY = "iqTSQ2hMuj8E7t2sy3WYA5m73LuX4iUD5iHgwRGf";
    private static String USER_KEY = "";	 // USER KEY 입력 필수 아님 : Copy License 기준 서비스 운영시 필요
    private static String DEVICE_KEY = "";

    private NavigationFragment navigationFragment;
    private FragmentManager fragmentManager;
    private FragmentTransaction transaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navi);

        fragmentManager = getSupportFragmentManager();

        navigationFragment = TmapUISDK.Companion.getFragment();

        transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.tmapUILayout, navigationFragment);
        transaction.commitAllowingStateLoss();

        TmapUISDK.Companion.initialize(this, CLIENT_ID, API_KEY, USER_KEY, DEVICE_KEY, new TmapUISDK.InitializeListener() {
            @Override
            public void onSuccess() {
                Log.e(TAG, "success initialize");
            }

            @Override
            public void onFail(int i, @Nullable String s) {
                Toast.makeText(NaviActivity.this, i + "::" + s, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "onFail " + i + " :: " + s);
            }

            @Override
            public void savedRouteInfoExists(@Nullable String dest) {
                Log.e(TAG,"목적지 : " + dest);
                if (dest != null) {
                    //이전 목적지가 존재하면 이어서 안내 여부 Dialog 띄움
                    //showDialogContinueRoute(dest);
                }
            }
        });
    }
}
