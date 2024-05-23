package com.bucheon.yeoddadae;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.HashMap;

public class ApproveShareParkActivity extends AppCompatActivity {

    Button approveShareParkBackBtn;
    ListView approveShareParkListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approve_share_park);

        approveShareParkBackBtn = findViewById(R.id.approveShareParkBackBtn);
        approveShareParkListView = findViewById(R.id.approveShareParkListView);

        FirestoreDatabase fd = new FirestoreDatabase();
        fd.loadUnapprovedShareParks(new OnFirestoreDataLoadedListener() {
            @Override
            public void onDataLoaded(Object data) {
                ArrayList<HashMap<String, Object>> shareParks = (ArrayList<HashMap<String, Object>>) data;
                for (HashMap<String, Object> sharePark : shareParks) {
                    /*
                    String reporterId = (String) oneReport.get("reporterId");
                    String parkName = (String) oneReport.get("parkName");
                    String condition = (String) oneReport.get("parkCondition");
                    long discount = (long) oneReport.get("parkDiscount");
                    long ratePerfectCount = (long) oneReport.get("ratePerfectCount");
                    long rateMistakeCount = (long) oneReport.get("rateMistakeCount");
                    long rateWrongCount = (long) oneReport.get("rateWrongCount");
                    boolean isCancelled = (boolean) oneReport.get("isCancelled");
                    boolean isApproval = (boolean) oneReport.get("isApproval");
                    Timestamp upTime = (Timestamp) oneReport.get("upTime");
                    String poiID = (String) oneReport.get("poiID");
                    String documentId = (String) oneReport.get("documentId");
                    ra.addItem(new ReportDiscountParkItem(reporterId, parkName, condition, discount, ratePerfectCount, rateMistakeCount, rateWrongCount, isCancelled, isApproval, upTime, poiID, documentId));

                     */
                }
            }

            @Override
            public void onDataLoadError(String errorMessage) {

            }
        });

    }
}
