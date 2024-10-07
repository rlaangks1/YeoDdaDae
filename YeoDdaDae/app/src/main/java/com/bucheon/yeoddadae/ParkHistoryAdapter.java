package com.bucheon.yeoddadae;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public class ParkHistoryAdapter extends BaseAdapter {
    ArrayList<ParkHistoryItem> favoriteItems  = new ArrayList<>();
    ArrayList<ParkHistoryItem> noFavoriteItems  = new ArrayList<>();
    OnParkHistoryListener listener;

    public void addItem(ParkHistoryItem item) {
        if (item.isFavorites()) {
            favoriteItems.add(item);
        }
        else {
            noFavoriteItems.add(item);
        }
    }

    public void removeItem (ParkHistoryItem item) {
        if (favoriteItems.contains(item)) {
            favoriteItems.remove(item);
        }
        else if (noFavoriteItems.contains(item)) {
            noFavoriteItems.remove(item);
        }
        notifyDataSetChanged();
    }

    public void changeFavorites (ParkHistoryItem item) {
        if (item.isFavorites()) {
            item.setFavorites(false);
            favoriteItems.remove(item);
            noFavoriteItems.add(item);
        }
        else {
            item.setFavorites(true);
            noFavoriteItems.remove(item);
            favoriteItems.add(item);
        }

        notifyDataSetChanged();
    }

    public void sortByUpTime() {
        if (favoriteItems != null && favoriteItems.size() > 1) {
            Collections.sort(favoriteItems, new Comparator<ParkHistoryItem>() {
                @Override
                public int compare(ParkHistoryItem o1, ParkHistoryItem o2) {
                    return o2.getUpTime().compareTo(o1.getUpTime()); // 역순으로 정렬
                }
            });
        }
        if (noFavoriteItems != null && noFavoriteItems.size() > 1) {
            Collections.sort(noFavoriteItems, new Comparator<ParkHistoryItem>() {
                @Override
                public int compare(ParkHistoryItem o1, ParkHistoryItem o2) {
                    return o2.getUpTime().compareTo(o1.getUpTime()); // 역순으로 정렬
                }
            });
        }

        notifyDataSetChanged(); // Notify adapter that dataset has changed
    }

    public void clear() {
        favoriteItems.clear();
        noFavoriteItems.clear();
        notifyDataSetChanged();
    }

    public ParkHistoryItem findItem(String id, String poiId) {
        for (ParkHistoryItem item : favoriteItems) {
            if (item.getId().equals(id) && item.getPoiId().equals(poiId)) {
                return item;
            }
        }
        for (ParkHistoryItem item : noFavoriteItems) {
            if (item.getId().equals(id) && item.getPoiId().equals(poiId)) {
                return item;
            }
        }
        return null; // 못 찾은 경우 null 반환
    }

    public void setOnParkHistoryListener(OnParkHistoryListener listener) {
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return favoriteItems.size() + noFavoriteItems.size();
    }

    @Override
    public Object getItem(int position) {
        if (position < favoriteItems.size()) {
            return favoriteItems.get(position);
        } else {
            return noFavoriteItems.get(position - favoriteItems.size());
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Context context = parent.getContext();

        if (position < favoriteItems.size()) {
            ParkHistoryItem parkHistory = (ParkHistoryItem) getItem(position);

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.park_history_item, parent, false);
            }

            ImageButton parkHistoryFavoriteBtn = convertView.findViewById(R.id.parkHistoryFavoriteBtn);
            ImageButton parkHistoryDeleteBtn = convertView.findViewById(R.id.parkHistoryDeleteBtn);
            TextView parkHistoryNameTxt = convertView.findViewById(R.id.parkHistoryNameTxt);
            TextView parkHistoryTypeTxt = convertView.findViewById(R.id.parkHistoryTypeTxt);
            TextView parkHistoryUpTimeTxt = convertView.findViewById(R.id.parkHistoryUpTimeTxt);

            parkHistoryFavoriteBtn.setImageResource(R.drawable.favorite_icon);

            parkHistoryNameTxt.setText(parkHistory.getPoiName());

            parkHistoryTypeTxt.setText(parkHistory.getType());

            Timestamp timestamp = parkHistory.getUpTime();
            if (timestamp != null) {
                Date date = timestamp.toDate();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일 HH:mm:ss", Locale.KOREA);
                String dateString = sdf.format(date);
                parkHistoryUpTimeTxt.setText(dateString);
            }

            parkHistoryFavoriteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onParkHistoryFavorite(parkHistory, position);
                    }
                }
            });

            parkHistoryDeleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onParkHistoryDelete(parkHistory, position);
                    }
                }
            });

            return convertView;
        }
        else {
            ParkHistoryItem parkHistory = (ParkHistoryItem) getItem(position);

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.park_history_item, parent, false);
            }

            ImageButton parkHistoryFavoriteBtn = convertView.findViewById(R.id.parkHistoryFavoriteBtn);
            ImageButton parkHistoryDeleteBtn = convertView.findViewById(R.id.parkHistoryDeleteBtn);
            TextView parkHistoryNameTxt = convertView.findViewById(R.id.parkHistoryNameTxt);
            TextView parkHistoryTypeTxt = convertView.findViewById(R.id.parkHistoryTypeTxt);
            TextView parkHistoryUpTimeTxt = convertView.findViewById(R.id.parkHistoryUpTimeTxt);

            parkHistoryFavoriteBtn.setImageResource(R.drawable.no_favorite_icon);

            parkHistoryNameTxt.setText(parkHistory.getPoiName());

            parkHistoryTypeTxt.setText(parkHistory.getType());

            Timestamp timestamp = parkHistory.getUpTime();
            if (timestamp != null) {
                Date date = timestamp.toDate();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일 HH:mm:ss", Locale.KOREA);
                String dateString = sdf.format(date);
                parkHistoryUpTimeTxt.setText(dateString);
            }

            parkHistoryFavoriteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onParkHistoryFavorite(parkHistory, position);
                    }
                }
            });

            parkHistoryDeleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onParkHistoryDelete(parkHistory, position);
                    }
                }
            });

            return convertView;
        }
    }
}
