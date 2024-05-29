package com.bucheon.yeoddadae;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.address_info.TMapAddressInfo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class RateAnotherReportDiscountParkActivity extends AppCompatActivity {
    String loginId;
    String documentId;
    HashMap<String, Object> reportInfo;

    Button reportInfoBackBtn;
    TextView rateReportIdContentTxt;
    TextView rateReportParkNameContentTxt;
    TextView rateReportParkNewAddressContentTxt;
    TextView rateReportParkOldAddressContentTxt;
    TextView rateReportConditionContentTxt;
    TextView rateReportDiscountContentTxt;
    TextView rateReportWonTxt;
    TextView rateReportUpTimeContentTxt;
    Button rateReportPerfectBtn;
    Button rateReportMistakeBtn;
    Button rateReportWrongBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_another_report_discount_park);

        Intent inIntent = getIntent();
        loginId = inIntent.getStringExtra("id");
        documentId = inIntent.getStringExtra("documentId");

        reportInfoBackBtn = findViewById(R.id.reportInfoBackBtn);
        rateReportIdContentTxt = findViewById(R.id.rateReportIdContentTxt);
        rateReportParkNameContentTxt = findViewById(R.id.rateReportParkNameContentTxt);
        rateReportParkNewAddressContentTxt = findViewById(R.id.rateReportParkNewAddressContentTxt);
        rateReportParkOldAddressContentTxt = findViewById(R.id.rateReportParkOldAddressContentTxt);
        rateReportConditionContentTxt = findViewById(R.id.rateReportConditionContentTxt);
        rateReportDiscountContentTxt = findViewById(R.id.rateReportDiscountContentTxt);
        rateReportWonTxt = findViewById(R.id.rateReportWonTxt);
        rateReportUpTimeContentTxt = findViewById(R.id.rateReportUpTimeContentTxt);
        rateReportPerfectBtn = findViewById(R.id.rateReportPerfectBtn);
        rateReportMistakeBtn = findViewById(R.id.rateReportMistakeBtn);
        rateReportWrongBtn = findViewById(R.id.rateReportWrongBtn);

        FirestoreDatabase fd = new FirestoreDatabase();
        fd.loadOneReport(documentId, new OnFirestoreDataLoadedListener() {
            @Override
            public void onDataLoaded(Object data) {
                reportInfo = (HashMap<String, Object>) data;
                init();
            }

            @Override
            public void onDataLoadError(String errorMessage) {
                Log.d(TAG, errorMessage);
                Toast.makeText(getApplicationContext(), "오류 발생", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        reportInfoBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        rateReportPerfectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fd.updateRate(0, loginId, documentId, new OnFirestoreDataLoadedListener() {
                    @Override
                    public void onDataLoaded(Object data) {
                        getRateCount();
                    }

                    @Override
                    public void onDataLoadError(String errorMessage) {
                        Log.d(TAG, errorMessage);
                        Toast.makeText(getApplicationContext(), "오류 발생", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        rateReportMistakeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fd.updateRate(1, loginId, documentId, new OnFirestoreDataLoadedListener() {
                    @Override
                    public void onDataLoaded(Object data) {
                        getRateCount();
                    }

                    @Override
                    public void onDataLoadError(String errorMessage) {
                        Log.d(TAG, errorMessage);
                        Toast.makeText(getApplicationContext(), "오류 발생", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        rateReportWrongBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fd.updateRate(2, loginId, documentId, new OnFirestoreDataLoadedListener() {
                    @Override
                    public void onDataLoaded(Object data) {
                        getRateCount();
                    }

                    @Override
                    public void onDataLoadError(String errorMessage) {
                        Log.d(TAG, errorMessage);
                        Toast.makeText(getApplicationContext(), "오류 발생", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    void getRateCount () {
        FirestoreDatabase fd = new FirestoreDatabase();

        fd.loadRateCount(documentId, new OnFirestoreDataLoadedListener() {
            @Override
            public void onDataLoaded(Object data) {
                int[] rates = (int[]) data;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        rateReportPerfectBtn.setText(Integer.toString(rates[0]));
                        rateReportMistakeBtn.setText(Integer.toString(rates[1]));
                        rateReportWrongBtn.setText(Integer.toString(rates[2]));
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

    void init () {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                rateReportIdContentTxt.setText(documentId);

                rateReportParkNameContentTxt.setText((String) reportInfo.get("parkName"));

                rateReportConditionContentTxt.setText((String) reportInfo.get("parkCondition"));

                long discount = (long) reportInfo.get("parkDiscount");

                if (discount == 0) {
                    rateReportDiscountContentTxt.setText("무료");
                    rateReportWonTxt.setVisibility(View.GONE);
                }
                else {
                    rateReportDiscountContentTxt.setText(Long.toString(discount));
                    rateReportWonTxt.setVisibility(View.VISIBLE);
                }

                Timestamp timestamp = (Timestamp) reportInfo.get("upTime");
                if (timestamp != null) {
                    Date date = timestamp.toDate();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일 HH:mm:ss", Locale.KOREA);
                    String dateString = sdf.format(date);
                    rateReportUpTimeContentTxt.setText(dateString);
                }

                TMapData tMapdata = new TMapData();
                tMapdata.reverseGeocoding((double) reportInfo.get("poiLat"), (double) reportInfo.get("poiLon"), "A10", new TMapData.reverseGeocodingListenerCallback() {
                    @Override
                    public void onReverseGeocoding(TMapAddressInfo tMapAddressInfo) {
                        if (tMapAddressInfo != null) {
                            String[] adrresses = tMapAddressInfo.strFullAddress.split(",");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    rateReportParkNewAddressContentTxt.setText(adrresses[2]);
                                    rateReportParkOldAddressContentTxt.setText(adrresses[1]);
                                }
                            });
                        }
                    }
                });
            }
        });

        getRateCount();
    }
}