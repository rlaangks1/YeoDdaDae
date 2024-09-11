package com.bucheon.yeoddadae;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class UsageHistoryAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<UsageHistoryItem> usageHistoryList;

    // 생성자
    public UsageHistoryAdapter(Context context, ArrayList<UsageHistoryItem> usageHistoryList) {
        this.context = context;
        this.usageHistoryList = usageHistoryList;
    }

    @Override
    public int getCount() {
        return usageHistoryList.size();
    }

    @Override
    public Object getItem(int position) {
        return usageHistoryList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.activity_usage_history_item, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.typeText = convertView.findViewById(R.id.typeText);
            viewHolder.startText = convertView.findViewById(R.id.startText);
            viewHolder.endText = convertView.findViewById(R.id.endText);
            viewHolder.timeText = convertView.findViewById(R.id.timeText);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // 현재 아이템 가져오기
        UsageHistoryItem currentItem = (UsageHistoryItem) getItem(position);

        // 아이템 데이터 설정
        viewHolder.typeText.setText(currentItem.getType() == 1 ? "주차장" : "주유소");
        viewHolder.startText.setText("출발지: " + currentItem.getStarts());
        viewHolder.endText.setText("도착지: " + currentItem.getEnds());
        viewHolder.timeText.setText("시간: " + currentItem.getFormattedTimestamp());

        return convertView;
    }

    // ViewHolder 패턴 사용으로 성능 최적화
    private static class ViewHolder {
        TextView typeText;
        TextView startText;
        TextView endText;
        TextView timeText;
    }
}
