package com.bucheon.yeoddadae;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Date;

public class UsageHistoryActivity extends AppCompatActivity {

    private ImageButton backBtn;
    private ListView listView;
    private Spinner typeFilterSpinner;
    private UsageHistoryAdapter adapter;
    private ArrayList<UsageHistoryItem> usageHistoryList;
    private ArrayList<UsageHistoryItem> filteredList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usage_history); // usage_history 레이아웃

        backBtn = findViewById(R.id.recordBackBtn);
        listView = findViewById(R.id.usageHistoryListView); // 리스트뷰 ID 연결
        typeFilterSpinner = findViewById(R.id.typeFilterSpinner); // 스피너 ID 연결

        // 임시 데이터 초기화
        int[] type = {1, 2, 1, 1, 2}; // 1: 주차장, 2: 주유소
        String[] starts = {"가", "나", "다", "라", "마"}; // 출발지
        String[] ends = {"바", "사", "아", "자", "차"}; // 도착지
        Timestamp[] times = {new Timestamp(new Date()), new Timestamp(new Date()), new Timestamp(new Date()), new Timestamp(new Date()), new Timestamp(new Date())};

        // 리스트에 데이터 추가
        usageHistoryList = new ArrayList<>();
        for (int i = 0; i < type.length; i++) {
            usageHistoryList.add(new UsageHistoryItem(type[i], starts[i], ends[i], times[i]));
        }

        filteredList = new ArrayList<>(usageHistoryList); // 기본적으로 모든 데이터를 보여줌

        // 어댑터 설정
        adapter = new UsageHistoryAdapter(this, filteredList);
        listView.setAdapter(adapter);

        // 스피너에 (전체, 주차장, 주유소) 옵션 추가
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.my_spinner_usage_history, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeFilterSpinner.setAdapter(spinnerAdapter);

        // 스피너에서 항목 선택 시 필터링 동작
        typeFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterList(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 아무것도 선택되지 않았을 때 기본 리스트 표시
                filteredList.clear();
                filteredList.addAll(usageHistoryList);
                adapter.notifyDataSetChanged();
            }
        });

        // 뒤로가기 버튼 클릭 시 액티비티 종료
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    // 선택된 필터에 따라 리스트를 필터링하는 메서드
    private void filterList(int filterType) {
        filteredList.clear();
        if (filterType == 0) { // 전체
            filteredList.addAll(usageHistoryList);
        } else {
            for (UsageHistoryItem item : usageHistoryList) {
                if (filterType == 1 && item.getType() == 1) { // 주차장
                    filteredList.add(item);
                } else if (filterType == 2 && item.getType() == 2) { // 주유소
                    filteredList.add(item);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }
}
