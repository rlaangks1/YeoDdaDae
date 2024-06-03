package com.bucheon.yeoddadae;

import static com.google.android.exoplayer2.ExoPlayerLibraryInfo.TAG;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ApproveReportInformationActivity extends AppCompatActivity {
    String documentId;
    HashMap<String, Object> reportInfo;

    Button approveReportInfoBackBtn;
    TextView approveReportInfoIdContentTxt;
    TextView approveReportInfoParkNameContentTxt;
    TextView approveReportInfoParkNewAddressContentTxt;
    TextView approveReportInfoParkOldAddressContentTxt;
    TextView approveReportInfoConditionContentTxt;
    TextView approveReportInfoDiscountContentTxt;
    TextView approveReportInfoWonTxt;
    TextView approveReportInfoUpTimeContentTxt;
    Button approveReportInfoApproveBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apporve_report_information);

        approveReportInfoBackBtn = findViewById(R.id.approveReportInfoBackBtn);
        approveReportInfoIdContentTxt = findViewById(R.id.approveReportInfoIdContentTxt);
        approveReportInfoParkNameContentTxt = findViewById(R.id.approveReportInfoParkNameContentTxt);
        approveReportInfoParkNewAddressContentTxt = findViewById(R.id.approveReportInfoParkNewAddressContentTxt);
        approveReportInfoParkOldAddressContentTxt = findViewById(R.id.approveReportInfoParkOldAddressContentTxt);
        approveReportInfoConditionContentTxt = findViewById(R.id.approveReportInfoConditionContentTxt);
        approveReportInfoDiscountContentTxt = findViewById(R.id.approveReportInfoDiscountContentTxt);
        approveReportInfoWonTxt = findViewById(R.id.approveReportInfoWonTxt);
        approveReportInfoUpTimeContentTxt = findViewById(R.id.approveReportInfoUpTimeContentTxt);
        approveReportInfoApproveBtn = findViewById(R.id.approveReportInfoApproveBtn);

        Intent inIntent = getIntent();
        documentId = inIntent.getStringExtra("documentId");

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

        approveReportInfoBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        approveReportInfoApproveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirestoreDatabase fd = new FirestoreDatabase();
                fd.approveReport(documentId, new OnFirestoreDataLoadedListener() {
                    @Override
                    public void onDataLoaded(Object data) {
                        Toast.makeText(getApplicationContext(), "공유주차장 승인되었습니다", Toast.LENGTH_SHORT).show();
                        finish();
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

    void init() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                approveReportInfoIdContentTxt.setText(documentId);

                approveReportInfoParkNameContentTxt.setText((String) reportInfo.get("parkName"));

                TMapData tMapData = new TMapData();
                tMapData.reverseGeocoding((double) reportInfo.get("poiLat"), (double) reportInfo.get("poiLon"), "A10", new TMapData.reverseGeocodingListenerCallback() {
                    @Override
                    public void onReverseGeocoding(TMapAddressInfo tMapAddressInfo) {
                        if (tMapAddressInfo != null) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    String [] adrresses = tMapAddressInfo.strFullAddress.split(",");
                                    approveReportInfoParkNewAddressContentTxt.setText(adrresses[2]);
                                    approveReportInfoParkOldAddressContentTxt.setText(adrresses[1]);
                                }
                            });
                        }
                    }
                });

                approveReportInfoConditionContentTxt.setText((String) reportInfo.get("parkCondition"));

                if ((long) reportInfo.get("parkDiscount") == 0) {
                    approveReportInfoWonTxt.setVisibility(View.GONE);
                    approveReportInfoDiscountContentTxt.setText("무료");
                }
                else {
                    approveReportInfoWonTxt.setVisibility(View.VISIBLE);
                    approveReportInfoDiscountContentTxt.setText(Long.toString((long) reportInfo.get("parkDiscount")));
                }

                Timestamp timestamp = (Timestamp) reportInfo.get("upTime");
                if (timestamp != null) {
                    Date date = timestamp.toDate();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일 HH:mm:ss", Locale.KOREA);
                    String dateString = sdf.format(date);
                    approveReportInfoUpTimeContentTxt.setText(dateString);
                }
            }
        });
    }
}
