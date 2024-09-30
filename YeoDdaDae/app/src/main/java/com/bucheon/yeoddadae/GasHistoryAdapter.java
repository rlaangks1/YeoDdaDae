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

public class GasHistoryAdapter extends BaseAdapter {
    ArrayList<GasHistoryItem> favoriteItems  = new ArrayList<>();
    ArrayList<GasHistoryItem> noFavoriteItems  = new ArrayList<>();
    OnGasHistoryListener listener;

    public void addItem(GasHistoryItem item) {
        if (item.isFavorites()) {
            favoriteItems.add(item);
        }
        else {
            noFavoriteItems.add(item);
        }
    }

    public void removeItem (GasHistoryItem item) {
        if (favoriteItems.contains(item)) {
            favoriteItems.remove(item);
        }
        else if (noFavoriteItems.contains(item)) {
            noFavoriteItems.remove(item);
        }
        notifyDataSetChanged();
    }

    public void changeFavorites (GasHistoryItem item) {
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
            Collections.sort(favoriteItems, new Comparator<GasHistoryItem>() {
                @Override
                public int compare(GasHistoryItem o1, GasHistoryItem o2) {
                    return o2.getUpTime().compareTo(o1.getUpTime()); // 역순으로 정렬
                }
            });
        }
        if (noFavoriteItems != null && noFavoriteItems.size() > 1) {
            Collections.sort(noFavoriteItems, new Comparator<GasHistoryItem>() {
                @Override
                public int compare(GasHistoryItem o1, GasHistoryItem o2) {
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

    public GasHistoryItem findItem(String id, String poiId) {
        for (GasHistoryItem item : favoriteItems) {
            if (item.getId().equals(id) && item.getPoiId().equals(poiId)) {
                return item;
            }
        }
        for (GasHistoryItem item : noFavoriteItems) {
            if (item.getId().equals(id) && item.getPoiId().equals(poiId)) {
                return item;
            }
        }
        return null; // 못 찾은 경우 null 반환
    }

    public void setOnGasHistoryListener(OnGasHistoryListener listener) {
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
            GasHistoryItem gasHistory = (GasHistoryItem) getItem(position);

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.gas_history_item, parent, false);
            }

            ImageButton gasHistoryFavoriteBtn = convertView.findViewById(R.id.gasHistoryFavoriteBtn);
            ImageButton gasHistoryDeleteBtn = convertView.findViewById(R.id.gasHistoryDeleteBtn);
            TextView gasHistoryNameTxt = convertView.findViewById(R.id.gasHistoryNameTxt);
            TextView gasHistoryUpTimeTxt = convertView.findViewById(R.id.gasHistoryUpTimeTxt);

            gasHistoryFavoriteBtn.setImageResource(R.drawable.favorite_icon);

            gasHistoryNameTxt.setText(gasHistory.getPoiName());

            Timestamp timestamp = gasHistory.getUpTime();
            if (timestamp != null) {
                Date date = timestamp.toDate();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일 HH:mm:ss", Locale.KOREA);
                String dateString = sdf.format(date);
                gasHistoryUpTimeTxt.setText(dateString);
            }

            gasHistoryFavoriteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onGasHistoryFavorite(gasHistory, position);
                    }
                }
            });

            gasHistoryDeleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onGasHistoryDelete(gasHistory, position);
                    }
                }
            });

            return convertView;
        }
        else {
            GasHistoryItem gasHistory = (GasHistoryItem) getItem(position);

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.gas_history_item, parent, false);
            }

            ImageButton gasHistoryFavoriteBtn = convertView.findViewById(R.id.gasHistoryFavoriteBtn);
            ImageButton gasHistoryDeleteBtn = convertView.findViewById(R.id.gasHistoryDeleteBtn);
            TextView gasHistoryNameTxt = convertView.findViewById(R.id.gasHistoryNameTxt);
            TextView gasHistoryUpTimeTxt = convertView.findViewById(R.id.gasHistoryUpTimeTxt);

            gasHistoryFavoriteBtn.setImageResource(R.drawable.no_favorite_icon);

            gasHistoryNameTxt.setText(gasHistory.getPoiName());

            Timestamp timestamp = gasHistory.getUpTime();
            if (timestamp != null) {
                Date date = timestamp.toDate();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일 HH:mm:ss", Locale.KOREA);
                String dateString = sdf.format(date);
                gasHistoryUpTimeTxt.setText(dateString);
            }

            gasHistoryFavoriteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onGasHistoryFavorite(gasHistory, position);
                    }
                }
            });

            gasHistoryDeleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onGasHistoryDelete(gasHistory, position);
                    }
                }
            });

            return convertView;
        }
    }
}
