package com.bucheon.yeoddadae;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public class UserAdapter extends BaseAdapter {
    ArrayList<UserItem> items = new ArrayList<>();
    ArrayList<UserItem> savedItems = new ArrayList<>();
    boolean isItemsSaved = false;

    public void addItem(UserItem item) {
        items.add(item);
    }

    public void clear() {
        items.clear();
        savedItems.clear();
        isItemsSaved = false;
        notifyDataSetChanged();
    }

    public void sortByRegisterTime() {
        if (items != null && items.size() > 1) {
            Collections.sort(items, new Comparator<UserItem>() {
                @Override
                public int compare(UserItem o1, UserItem o2) {
                    return o2.getRegisterTime().compareTo(o1.getRegisterTime());
                }
            });

            notifyDataSetChanged();
        }
    }

    public int searchUser(String keyword) {
        if (!isItemsSaved) { // 검색을 수행하기 전에 items의 복사본을 savedItems에 저장
            savedItems = new ArrayList<>(items); // items의 얕은 복사본 생성
            isItemsSaved = true;
        }

        ArrayList<UserItem> removeItems = new ArrayList<>();

        for (UserItem item : items) {
            if (!item.getId().contains(keyword) && !item.getEmail().contains(keyword)) {
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
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Context context = parent.getContext();
        UserItem item = items.get(position);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_user, parent, false);
        }

        TextView idTxt = convertView.findViewById(R.id.idTxt);
        TextView emailTxt = convertView.findViewById(R.id.emailTxt);
        TextView registerTimeTxt = convertView.findViewById(R.id.registerTimeTxt);

        idTxt.setText(item.getId());

        emailTxt.setText(item.getEmail());

        Timestamp timestamp = item.getRegisterTime();
        if (timestamp != null) {
            Date date = timestamp.toDate();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일 HH:mm:ss", Locale.KOREA);
            String dateString = sdf.format(date);
            registerTimeTxt.setText(dateString);
        }

        return convertView;
    }
}
