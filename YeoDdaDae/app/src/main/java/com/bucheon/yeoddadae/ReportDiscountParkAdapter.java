package com.bucheon.yeoddadae;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.firebase.Timestamp;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ReportDiscountParkAdapter extends BaseAdapter {
    ArrayList<ReportDiscountParkItem> items = new ArrayList<>();
    ArrayList<ReportDiscountParkItem> savedItems = new ArrayList<>();
    Activity activity;
    LayoutInflater inflater;
    boolean isItemsSaved = false;

    public ReportDiscountParkAdapter(Activity activity) {
        this.activity = activity;
        this.inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void addItem(ReportDiscountParkItem item) {
        items.add(item);
        notifyDataSetChanged();
    }

    public ReportDiscountParkItem findItem(String reporterId, Timestamp upTime) {
        for (ReportDiscountParkItem item : items) {
            if (item.getReporterId().equals(reporterId) && item.getUpTime().equals(upTime)) {
                return item;
            }
        }
        return null; // 못 찾은 경우 null 반환
    }

    public void clear () {
        items.clear();
        savedItems.clear();
        isItemsSaved = false;
        notifyDataSetChanged();
    }

    public int searchReportDiscountPark(String keyword) {
        if (!isItemsSaved) {
            savedItems = new ArrayList<>(items);
            isItemsSaved = true;
        }

        ArrayList<ReportDiscountParkItem> removeItems = new ArrayList<>();
        for (ReportDiscountParkItem item : items) {
            if (!item.getDocumentId().contains(keyword) && !item.getStatus().contains(keyword) && (item.getParkName() == null || !item.getParkName().contains(keyword))) {
                removeItems.add(item);
            }
        }
        items.removeAll(removeItems);


        notifyDataSetChanged();

        return items.size();
    }

    public void loadSavedItems() {
        if (isItemsSaved) {
            items = new ArrayList<>(savedItems); // savedItems의 복사본으로 items 복원
            savedItems.clear();
            isItemsSaved = false;

            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getDocumentId().hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ReportDiscountParkItem report = items.get(position);

        if (activity instanceof MainActivity) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.my_report_discount_park_item, parent, false);
            }

            // 파인드 뷰
            TextView reportDiscountParkAddressTxt = convertView.findViewById(R.id.reportDiscountParkAddressTxt);
            TextView reportDiscountParkConditionAndDiscountTxt = convertView.findViewById(R.id.reportDiscountParkConditionAndDiscountTxt);
            TextView reportDiscountParkRateTxt = convertView.findViewById(R.id.reportDiscountParkRateTxt);
            TextView reportDiscountParkIsCancelledTxt = convertView.findViewById(R.id.reportDiscountParkIsCancelledTxt);
            TextView reportDiscountParkUpTimeTxt = convertView.findViewById(R.id.reportDiscountParkUpTimeTxt);

            reportDiscountParkAddressTxt.setText(report.getParkName());

            String conditionAndDscountString = "";
            if (report.getCondition() != null && report.getDiscount() != null && report.getCondition().size() == report.getDiscount().size()) {
                DecimalFormat formatter = new DecimalFormat("#,###");

                for (int i = 0; i < report.getCondition().size(); i++) {
                    conditionAndDscountString += report.getCondition().get(i);

                        if (report.getDiscount().get(i) == 0) {
                        conditionAndDscountString += "/무료";
                    }
                    else {
                        long discount = report.getDiscount().get(i);
                        String formattedDiscount= formatter.format(discount);
                        conditionAndDscountString += "/" + formattedDiscount + "원 할인";
                    }
                    if (i != report.getCondition().size() - 1) {
                        conditionAndDscountString += "\n";
                    }
                }
            }
            reportDiscountParkConditionAndDiscountTxt.setText(conditionAndDscountString);

            String rate = report.getRatePerfectCount() + " / " + report.getRateMistakeCount() + " / " + report.getRateWrongCount();
            reportDiscountParkRateTxt.setText(rate);

            String status = report.getStatus();
            int statusColor = reportDiscountParkAddressTxt.getCurrentTextColor();
            switch (status) {
                case "승인됨" :
                    statusColor = Color.rgb(0, 0, 255);
                    break;
                case "취소됨" :
                    statusColor = Color.rgb(255, 0, 0);
                    break;
                case "평가 중" :
                    break;
            }
            reportDiscountParkIsCancelledTxt.setText(status);
            reportDiscountParkIsCancelledTxt.setTextColor(statusColor);

            Timestamp timestamp = report.getUpTime();
            if (timestamp != null) {
                Date date = timestamp.toDate();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일 HH:mm:ss", Locale.KOREA);
                String dateString = sdf.format(date);
                reportDiscountParkUpTimeTxt.setText(dateString);
            }
        }

        else if (activity instanceof AnotherReportDiscountParkActivity || activity instanceof ApproveReportActivity) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.report_discount_park_item, parent, false);
            }

            // 파인드 뷰
            TextView reportDiscountParkNameTxt = convertView.findViewById(R.id.reportDiscountParkNameTxt);
            TextView reportDiscountParkConditionAndDiscountTxt = convertView.findViewById(R.id.reportDiscountParkConditionAndDiscountTxt);
            TextView reportDiscountParkRateTxt = convertView.findViewById(R.id.reportDiscountParkRateTxt);
            TextView reportDiscountParkIDTxt = convertView.findViewById(R.id.reportDiscountParkIDTxt);
            TextView reportDiscountParkUpTimeTxt = convertView.findViewById(R.id.reportDiscountParkUpTimeTxt);

            reportDiscountParkNameTxt.setText(report.getParkName());

            String conditionAndDscountString = "";
            if (report.getCondition() != null && report.getDiscount() != null && report.getCondition().size() == report.getDiscount().size()) {
                DecimalFormat formatter = new DecimalFormat("#,###");

                for (int i = 0; i < report.getCondition().size(); i++) {
                    conditionAndDscountString += report.getCondition().get(i);

                    if (report.getDiscount().get(i) == 0) {
                        conditionAndDscountString += "/무료";
                    }
                    else {
                        long discount = report.getDiscount().get(i);
                        String formattedDiscount= formatter.format(discount);
                        conditionAndDscountString += "/" + formattedDiscount + "원 할인";
                    }
                    if (i != report.getCondition().size() - 1) {
                        conditionAndDscountString += "\n";
                    }
                }
            }
            reportDiscountParkConditionAndDiscountTxt.setText(conditionAndDscountString);

            String rate = report.getRatePerfectCount() + " / " + report.getRateMistakeCount() + " / " + report.getRateWrongCount();
            reportDiscountParkRateTxt.setText(rate);

            reportDiscountParkIDTxt.setText(report.getReporterId());

            Timestamp timestamp = report.getUpTime();
            if (timestamp != null) {
                Date date = timestamp.toDate();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일 HH:mm:ss", Locale.KOREA);
                String dateString = sdf.format(date);
                reportDiscountParkUpTimeTxt.setText(dateString);
            }
        }

        return convertView;
    }
}
