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

public class GasHistoryDialog extends Dialog {
    String id;
    Context context;
    GasHistoryAdapter pha;
    FirestoreDatabase fd;

    ImageButton gasHistoryBackBtn;
    ListView gasHistoryListView;

    public GasHistoryDialog(String id, @NonNull Context context) {
        super(context);
        this.id = id;
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_gas_history);

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes(params);

        gasHistoryBackBtn = findViewById(R.id.gasHistoryBackBtn);
        gasHistoryListView = findViewById(R.id.gasHistoryListView);

        pha = new GasHistoryAdapter();

        pha.setOnGasHistoryListener(new OnGasHistoryListener() {
            @Override
            public void onGasHistoryFavorite(GasHistoryItem item, int position) {
                fd = new FirestoreDatabase();
                if (item.isFavorites()) {
                    fd.unfavoriteGasHistory(id, item.getPoiId(), new OnFirestoreDataLoadedListener() {
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
                    fd.favoriteGasHistory(id, item.getPoiId(), new OnFirestoreDataLoadedListener() {
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
            public void onGasHistoryDelete(GasHistoryItem item, int position) {
                fd = new FirestoreDatabase();
                fd.deleteGasHistory(id, item.getPoiId(), new OnFirestoreDataLoadedListener() {
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

        gasHistoryBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        getGasHistory();
    }

    void getGasHistory() {
        Log.d(TAG, "getGasHistory()");
        pha.clear();

        fd = new FirestoreDatabase();
        fd.loadGasHistory(id, new OnFirestoreDataLoadedListener() {
            @Override
            public void onDataLoaded(Object data) {
                ArrayList<HashMap<String, Object>> resultArrayList = (ArrayList<HashMap<String, Object>>) data;

                for (HashMap<String, Object> one : resultArrayList) {
                    pha.addItem(new GasHistoryItem((String) one.get("id"), (String) one.get("type"), (String) one.get("poiId"), (String) one.get("poiName"), (double) one.get("lat"), (double) one.get("lon"), (boolean) one.get("favorites"), (Timestamp) one.get("upTime")));
                }

                pha.sortByUpTime();
                gasHistoryListView.setAdapter(pha);
            }

            @Override
            public void onDataLoadError(String errorMessage) {
                Log.d(TAG, errorMessage);
                Toast.makeText(context, "오류 발생", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
