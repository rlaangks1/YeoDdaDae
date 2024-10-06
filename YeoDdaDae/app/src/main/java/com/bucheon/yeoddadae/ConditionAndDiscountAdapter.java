package com.bucheon.yeoddadae;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.Timestamp;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class ConditionAndDiscountAdapter extends BaseAdapter {
    Activity activity;
    LayoutInflater inflater;
    ArrayList<ConditionAndDiscountItem> items = new ArrayList<>();

    public ConditionAndDiscountAdapter(Activity activity) {
        this.activity = activity;
        this.inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        items.add(new ConditionAndDiscountItem());
    }

    public void addItem(ConditionAndDiscountItem item) {
        if (items.size() <= 5) {
            items.add(item);
            notifyDataSetChanged();
        }
    }

    public void removeItem(int i) {
        if (items.size() >= 2) {
            items.remove(i);
            notifyDataSetChanged();

            if (activity instanceof AddReportDiscountParkActivity) {
                ((AddReportDiscountParkActivity) activity).checkAddButtonVisibility();
                ((AddReportDiscountParkActivity) activity).setTotalHeightofListView();
            }
        }
    }

    public boolean allDataSet() {
        if (items.isEmpty()) {
            return  false;
        }
        for (int i = 0; i < items.size(); i++) {
            if (activity instanceof AddReportDiscountParkActivity) {
                AddReportDiscountParkActivity adpa = (AddReportDiscountParkActivity) activity;

                View view = adpa.getListView().getChildAt(i);

                EditText conditionContentEditTxt = view.findViewById(R.id.conditionContentEditTxt);
                EditText benefitContentEditTxt = view.findViewById(R.id.benefitContentEditTxt);

                String condition = replaceNewlinesAndTrim(conditionContentEditTxt);
                String discountStr = replaceNewlinesAndTrim(benefitContentEditTxt);

                if (condition.isEmpty()) {
                    Toast.makeText(activity.getApplicationContext(), (i + 1) + "번째 조건을 입력하세요", Toast.LENGTH_SHORT).show();
                    return false;
                }
                if (discountStr.isEmpty()) {
                    Toast.makeText(activity.getApplicationContext(), (i + 1) + "번째 혜택을 입력하세요", Toast.LENGTH_SHORT).show();
                    return false;
                }
                long discount;
                try {
                    discount = Long.parseLong(discountStr);
                }
                catch (NumberFormatException e) {
                    Toast.makeText(activity.getApplicationContext(), (i + 1) + "번째 혜택을 숫자로 입력하세요", Toast.LENGTH_SHORT).show();
                    return false;
                }

                // 각 아이템의 값을 설정
                ConditionAndDiscountItem item = items.get(i);
                item.setCondition(condition);
                item.setDiscount(discount);
            }
        }
        return true;
    }

    public ArrayList<String> getConditions() {
        ArrayList<String> result = new ArrayList<>();

        for (ConditionAndDiscountItem item : items) {
            result.add(item.getCondition());
        }
        return result;
    }

    public ArrayList<Long> getDiscounts() {
        ArrayList<Long> result = new ArrayList<>();

        for (ConditionAndDiscountItem item : items) {
            result.add(item.getDiscount());
        }
        return result;
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
        return items.get(position).hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.condition_and_discount_item, parent, false);
        }

        TextView conditionTxt = convertView.findViewById(R.id.conditionTxt);
        TextView benefitTxt = convertView.findViewById(R.id.benefitTxt);
        ImageButton removeBtn = convertView.findViewById(R.id.removeBtn);

        conditionTxt.setText("조건 " + (position + 1));

        benefitTxt.setText("혜택 " + (position + 1));

        if (position == 0) {
            removeBtn.setVisibility(View.GONE);
        }

        removeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeItem(position);
            }
        });

        return convertView;
    }

    String replaceNewlinesAndTrim(EditText et) {
        return et.getText().toString().replaceAll("\\n", " ").trim();
    }
}
