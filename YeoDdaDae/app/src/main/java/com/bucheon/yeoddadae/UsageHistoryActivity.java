package com.bucheon.yeoddadae;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.HashMap;

public class UsageHistoryActivity extends AppCompatActivity {
    String loginId;
    UsageHistoryAdapter uha;

    ImageButton usageHistoryBackBtn;
    Spinner usageHistoryParkOrGasSpinner;
    ListView usageHistoryListView;
    TextView usageHistoryNoTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usage_history);

        usageHistoryBackBtn = findViewById(R.id.usageHistoryBackBtn);
        usageHistoryParkOrGasSpinner = findViewById(R.id.usageHistoryParkOrGasSpinner);
        usageHistoryListView = findViewById(R.id.usageHistoryListView);
        usageHistoryNoTxt = findViewById(R.id.usageHistoryNoTxt);

        Intent inIntent = getIntent();
        loginId = inIntent.getStringExtra("id");

        usageHistoryBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.my_spinner_usage_history,
                R.layout.my_spinner
        );
        usageHistoryParkOrGasSpinner.setAdapter(adapter);

        usageHistoryParkOrGasSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();

                getHistory(selectedItem);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    public void getHistory (String type){
        if (uha != null) {
            uha.clearItem();
        }
        uha = new UsageHistoryAdapter();

        FirestoreDatabase fd = new FirestoreDatabase();
        fd.selectRoutes(loginId, new OnFirestoreDataLoadedListener() {
            @Override
            public void onDataLoaded(Object data) {
                ArrayList<HashMap<String, Object>> myHistories = (ArrayList<HashMap<String, Object>>) data;

                for (HashMap<String, Object> myHistory : myHistories) {
                    String type = (String) myHistory.get("type");
                    double startLat = (double) myHistory.get("startLat");
                    double startLon = (double) myHistory.get("startLon");
                    String poiId = (String) myHistory.get("poiId");
                    String poiName = (String) myHistory.get("poiName");
                    double endLat = (double) myHistory.get("endLat");
                    double endLon = (double) myHistory.get("endLon");
                    Timestamp upTime = (Timestamp) myHistory.get("upTime");

                    uha.addItem(new UsageHistoryItem(type, startLat, startLon, poiId, poiName, endLat, endLon, upTime));
                }

                if (type.equals("주차장")) {
                    uha.onlyPark();
                }
                else if (type.equals("주유소")) {
                    uha.onlyGas();
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        usageHistoryListView.setAdapter(uha);

                        if (uha.getCount() == 0) {
                            usageHistoryListView.setVisibility(View.GONE);
                            usageHistoryNoTxt.setVisibility(View.VISIBLE);
                        } else {
                            usageHistoryListView.setVisibility(View.VISIBLE);
                            usageHistoryNoTxt.setVisibility(View.GONE);
                        }
                    }
                });
            }

            @Override
            public void onDataLoadError(String errorMessage) {
                Log.d(TAG, errorMessage);
                Toast.makeText(getApplicationContext(), "오류 발생", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}
