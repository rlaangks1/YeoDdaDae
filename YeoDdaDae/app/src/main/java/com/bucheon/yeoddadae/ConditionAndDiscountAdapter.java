package com.bucheon.yeoddadae;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

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
        allDataSet();
        if (items.size() <= 5) {
            items.add(item);
            notifyDataSetChanged();
        }
    }

    public void removeItem(ConditionAndDiscountItem item) {
        allDataSet();
        if (items.size() >= 2) {
            items.remove(item);
            notifyDataSetChanged();

            if (activity instanceof AddReportDiscountParkActivity) {
                ((AddReportDiscountParkActivity) activity).checkAddButtonVisibility();
                ((AddReportDiscountParkActivity) activity).setHeightOfConditionAndDiscountListView();
            }
        }
    }

    public void allDataSet() {
        for (int i = 0; i < items.size(); i++) {
            if (activity instanceof AddReportDiscountParkActivity) {
                ConditionAndDiscountItem item = items.get(i);

                AddReportDiscountParkActivity adpa = (AddReportDiscountParkActivity) activity;

                View view = adpa.getConditionAndDiscountListView().getChildAt(i);

                EditText conditionContentEditTxt = view.findViewById(R.id.conditionContentEditTxt);
                EditText benefitContentEditTxt = view.findViewById(R.id.benefitContentEditTxt);

                String condition = replaceNewlinesAndTrim(conditionContentEditTxt);
                String discountStr = replaceNewlinesAndTrim(benefitContentEditTxt);

                if (condition.isEmpty()) {
                    item.setCondition("");
                }
                else {
                    item.setCondition(condition);
                }
                if (discountStr.isEmpty()) {
                    item.setDiscount(-1);
                }
                else {
                    item.setDiscount(Long.parseLong(discountStr));
                }
            }
        }
    }

    public boolean checkValidationAndAllDataSet() {
        if (items.isEmpty()) {
            return  false;
        }
        for (int i = 0; i < items.size(); i++) {
            if (activity instanceof AddReportDiscountParkActivity) {
                AddReportDiscountParkActivity adpa = (AddReportDiscountParkActivity) activity;

                View view = adpa.getConditionAndDiscountListView().getChildAt(i);

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
        ConditionAndDiscountItem cadi = items.get(position);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_condition_and_discount, parent, false);
        }

        TextView conditionTxt = convertView.findViewById(R.id.conditionTxt);
        EditText conditionContentEditTxt = convertView.findViewById(R.id.conditionContentEditTxt);
        TextView benefitTxt = convertView.findViewById(R.id.benefitTxt);
        EditText benefitContentEditTxt = convertView.findViewById(R.id.benefitContentEditTxt);
        ImageButton removeBtn = convertView.findViewById(R.id.removeBtn);

        conditionTxt.setText("조건 " + (position + 1));

        if (cadi.getCondition() == null || cadi.getCondition().isEmpty()) {
            conditionContentEditTxt.setText("");
        }
        else {
            conditionContentEditTxt.setText(cadi.getCondition());
        }

        benefitTxt.setText("혜택 " + (position + 1));

        if (cadi.getDiscount() == -1) {
            benefitContentEditTxt.setText("");
        }
        else {
            long discount = cadi.getDiscount();

            benefitContentEditTxt.setText(Long.toString(discount));
        }

        if (position == 0) {
            removeBtn.setVisibility(View.GONE);
        }
        else {
            removeBtn.setVisibility(View.VISIBLE);
        }

        removeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeItem(cadi);
            }
        });

        return convertView;
    }

    String replaceNewlinesAndTrim(EditText et) {
        return et.getText().toString().replaceAll("\\n", " ").trim();
    }
}
