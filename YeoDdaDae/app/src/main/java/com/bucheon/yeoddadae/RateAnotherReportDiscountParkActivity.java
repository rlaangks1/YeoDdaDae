package com.bucheon.yeoddadae;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class RateAnotherReportDiscountParkActivity extends AppCompatActivity {
    String loginId;
    String documentId;

    Button reportInfoBackBtn;
    TextView rateReportIdContentTxt;
    TextView rateReportParkNameContentTxt;
    TextView rateReportParkNewAddressContentTxt;
    TextView rateReportParkOldAddressContentTxt;
    TextView rateReportConditionContentTxt;
    TextView rateReportDiscountContentTxt;
    TextView rateReportUpTimeContentTxt;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_another_report_discount_park);

        Intent inIntent = getIntent();
        loginId = inIntent.getStringExtra("id");
        documentId = inIntent.getStringExtra("documentId");
    }

    void getRateCount () {
        FirestoreDatabase fd = new FirestoreDatabase();

        fd.loadRateCount(loginId, documentId, new OnFirestoreDataLoadedListener() {
            @Override
            public void onDataLoaded(Object data) {

            }

            @Override
            public void onDataLoadError(String errorMessage) {

            }
        });
    }
}