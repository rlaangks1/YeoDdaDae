package com.bucheon.yeoddadae;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class UsageHistoryActivity extends AppCompatActivity {

    ImageButton backBtn;
    private UsageHistoryAdapter usageHistoryAdapter; // 어댑터
    private ListView myrecordListView; // 리스트뷰
    private ArrayList<String> parkingSearchHistoryList; // 주차장 검색 기록 리스트
    private ArrayList<String> gasStationSearchHistoryList; // 주유소 검색 기록 리스트

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.usage_history);

        backBtn = findViewById(R.id.recordBackBtn);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                finish();
            }
        });

        // ListView 연결
        myrecordListView = findViewById(R.id.myrecordListView);

        // Intent로부터 검색 기록을 받아옴. null일 경우 빈 리스트로 초기화
        Intent intent = getIntent();
        parkingSearchHistoryList = intent.getStringArrayListExtra("parkingSearchHistory");
        gasStationSearchHistoryList = intent.getStringArrayListExtra("gasStationSearchHistory");

        if (parkingSearchHistoryList == null) {
            // 검색 기록이 null인 경우 빈 리스트로 초기화
            parkingSearchHistoryList = new ArrayList<>();
        }

        if (gasStationSearchHistoryList == null) {
            gasStationSearchHistoryList = new ArrayList<>();
        }

        // 어댑터 초기화
        usageHistoryAdapter = new UsageHistoryAdapter();
        myrecordListView.setAdapter(usageHistoryAdapter);

        // 초기에는 주차장 기록을 보여줌
        showParkingRecords();

    }

    // 주차장 검색 기록을 표시하는 메서드
    private void showParkingRecords() {
        usageHistoryAdapter.clear(); // 어댑터의 기존 데이터 삭제
        if (parkingSearchHistoryList != null && !parkingSearchHistoryList.isEmpty()) {
            for (String searchRecord : parkingSearchHistoryList) {
                usageHistoryAdapter.addRecord(new UsageHistoryItem(searchRecord, System.currentTimeMillis())); // 기록과 시간을 저장
            }
            usageHistoryAdapter.sortByTime(); // 최신순으로 정렬
        } else {
            Toast.makeText(this, "검색 기록이 없습니다.", Toast.LENGTH_SHORT).show();
        }
        usageHistoryAdapter.notifyDataSetChanged();
    }

    // 주유소 검색 기록을 표시하는 메서드
    private void showGasStationRecords() {
        usageHistoryAdapter.clear(); // 어댑터의 기존 데이터 삭제
        if (gasStationSearchHistoryList != null && !gasStationSearchHistoryList.isEmpty()) {
            for (String searchRecord : gasStationSearchHistoryList) {
                usageHistoryAdapter.addRecord(new UsageHistoryItem(searchRecord, System.currentTimeMillis())); // 기록과 시간을 저장
            }
            usageHistoryAdapter.sortByTime(); // 최신순으로 정렬
        } else {
            Toast.makeText(this, "검색 기록이 없습니다.", Toast.LENGTH_SHORT).show();
        }
        usageHistoryAdapter.notifyDataSetChanged();
    }

    // 버튼 클릭 시 호출되는 메서드
    public void onParkingRecordButtonClick(View view) {
        showParkingRecords();
    }

    // 주유소 버튼 클릭 시 호출되는 메서드
    public void onGasStationRecordButtonClick(View view) {
        showGasStationRecords();
    }
}
