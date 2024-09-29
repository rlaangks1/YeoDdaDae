package com.bucheon.yeoddadae;

import static android.content.ContentValues.TAG;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.HashMap;

public class ParkHistoryDialog extends Dialog {
    String id;
    Context context;
    ParkHistoryAdapter pha;
    ParkHistoryDialogListener listener;
    FirestoreDatabase fd;

    ImageButton parkHistoryBackBtn;
    ListView parkHistoryListView;

    public ParkHistoryDialog(String id, @NonNull Context context, ParkHistoryDialogListener listener) {
        super(context);
        this.id = id;
        this.context = context;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_park_history);

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes(params);

        parkHistoryBackBtn = findViewById(R.id.parkHistoryBackBtn);
        parkHistoryListView = findViewById(R.id.parkHistoryListView);

        pha = new ParkHistoryAdapter();

        pha.setOnParkHistoryListener(new OnParkHistoryListener() {
            @Override
            public void onParkHistoryFavorite(ParkHistoryItem item, int position) {
                fd = new FirestoreDatabase();
                if (item.isFavorites()) {
                    fd.unfavoriteParkHistory(id, item.getPoiId(), new OnFirestoreDataLoadedListener() {
                        @Override
                        public void onDataLoaded(Object data) {
                            pha.changeFavorites(item);
                            pha.sortByUpTime();
                        }

                        @Override
                        public void onDataLoadError(String errorMessage) {
                            Log.d(TAG, errorMessage);
                            Toast.makeText(context, "오류 발생", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else {
                    fd.favoriteParkHistory(id, item.getPoiId(), new OnFirestoreDataLoadedListener() {
                        @Override
                        public void onDataLoaded(Object data) {
                            pha.changeFavorites(item);
                            pha.sortByUpTime();
                        }

                        @Override
                        public void onDataLoadError(String errorMessage) {
                            Log.d(TAG, errorMessage);
                            Toast.makeText(context, "오류 발생", Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }

            @Override
            public void onParkHistoryDelete(ParkHistoryItem item, int position) {
                fd = new FirestoreDatabase();
                fd.deleteParkHistory(id, item.getPoiId(), new OnFirestoreDataLoadedListener() {
                    @Override
                    public void onDataLoaded(Object data) {
                        pha.removeItem(item);
                    }

                    @Override
                    public void onDataLoadError(String errorMessage) {
                        Log.d(TAG, errorMessage);
                        Toast.makeText(context, "오류 발생", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        parkHistoryBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        parkHistoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        getParkHistory();
    }

    @Override
    protected void onStop() {
        super.onStop();
        listener.onMessageSend("???닫힘");
    }

    void getParkHistory() {
        Log.d(TAG, "getParkHistory()");
        pha.clear();

        fd = new FirestoreDatabase();
        fd.loadParkHistory(id, new OnFirestoreDataLoadedListener() {
            @Override
            public void onDataLoaded(Object data) {
                ArrayList<HashMap<String, Object>> resultArrayList = (ArrayList<HashMap<String, Object>>) data;

                for (HashMap<String, Object> one : resultArrayList) {
                    pha.addItem(new ParkHistoryItem((String) one.get("id"), (String) one.get("type"), (String) one.get("poiId"), (String) one.get("poiName"), (double) one.get("lat"), (double) one.get("lon"), (boolean) one.get("favorites"), (Timestamp) one.get("upTime")));
                }

                pha.sortByUpTime();
                parkHistoryListView.setAdapter(pha);
            }

            @Override
            public void onDataLoadError(String errorMessage) {
                Log.d(TAG, errorMessage);
                Toast.makeText(context, "오류 발생", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
