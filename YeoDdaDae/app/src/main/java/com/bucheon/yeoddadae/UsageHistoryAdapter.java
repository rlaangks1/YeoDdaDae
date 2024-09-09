package com.bucheon.yeoddadae;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class UsageHistoryAdapter extends BaseAdapter {

    private ArrayList<UsageHistoryItem> records = new ArrayList<>(); // 검색 기록을 저장할 리스트

    // 검색 기록을 어댑터에 추가하는 메서드
    public void addRecord(UsageHistoryItem item) {
        records.add(item);
    }

    // 최신순으로 검색 기록을 정렬하는 메서드
    public void sortByTime() {
        Collections.sort(records, new Comparator<UsageHistoryItem>() {
            @Override
            public int compare(UsageHistoryItem o1, UsageHistoryItem o2) {
                return Long.compare(o2.getTimestamp(), o1.getTimestamp());  // 최신순으로 정렬
            }
        });
    }

    @Override
    public int getCount() {
        return records.size();
    }

    @Override
    public Object getItem(int position) {
        return records.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.usage_history_item, parent, false);
        }

        // 검색 기록 텍스트 설정
        UsageHistoryItem record = records.get(position);
        TextView searchText = convertView.findViewById(R.id.searchText);
        searchText.setText(record.getSearchQuery());

        // 검색 시간 텍스트 설정 (필요 시 추가 구현)
        // TextView searchTime = convertView.findViewById(R.id.searchTime);
        // searchTime.setText("검색 시간: " + record.getFormattedTimestamp());

        return convertView;
    }

    public void clear() {
        records.clear();
        notifyDataSetChanged();
    }
}
