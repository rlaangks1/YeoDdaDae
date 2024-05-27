package com.bucheon.yeoddadae;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
import java.util.Locale;

public class ReportDiscountParkInformationActivity extends AppCompatActivity {
    String loginId;
    String documentId;
    HashMap<String, Object> reportInfo;

    Button reportInfoBackBtn;
    TextView reportInfoIdContentTxt;
    TextView reportInfoStatusContentTxt;
    TextView reportInfoParkNameContentTxt;
    TextView reportInfoParkNewAddressContentTxt;
    TextView reportInfoParkOldAddressContentTxt;
    TextView reportInfoConditionContentTxt;
    TextView reportInfoDiscountContentTxt;
    TextView reportInfoWonTxt;
    TextView reportInfoRateContentTxt;
    TextView reportInfoUpTimeContentTxt;
    Button reportInfoCancelBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_discount_park_information);

        reportInfoBackBtn = findViewById(R.id.reportInfoBackBtn);
        reportInfoIdContentTxt = findViewById(R.id.reportInfoIdContentTxt);
        reportInfoStatusContentTxt = findViewById(R.id.reportInfoStatusContentTxt);
        reportInfoParkNameContentTxt = findViewById(R.id.reportInfoParkNameContentTxt);
        reportInfoParkNewAddressContentTxt = findViewById(R.id.reportInfoParkNewAddressContentTxt);
        reportInfoParkOldAddressContentTxt = findViewById(R.id.reportInfoParkOldAddressContentTxt);
        reportInfoConditionContentTxt = findViewById(R.id.reportInfoConditionContentTxt);
        reportInfoDiscountContentTxt = findViewById(R.id.reportInfoDiscountContentTxt);
        reportInfoWonTxt = findViewById(R.id.reportInfoWonTxt);
        reportInfoRateContentTxt = findViewById(R.id.reportInfoRateContentTxt);
        reportInfoUpTimeContentTxt = findViewById(R.id.reportInfoUpTimeContentTxt);
        reportInfoCancelBtn = findViewById(R.id.reportInfoCancelBtn);

        Intent inIntent = getIntent();
        loginId = inIntent.getStringExtra("id");
        documentId = inIntent.getStringExtra("documentId");

        FirestoreDatabase fd = new FirestoreDatabase();
        fd.loadMyOneReport(loginId, documentId, new OnFirestoreDataLoadedListener() {
            @Override
            public void onDataLoaded(Object data) {
                reportInfo = (HashMap<String, Object>) data;
                init();
            }

            @Override
            public void onDataLoadError(String errorMessage) {

            }
        });

        reportInfoBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        reportInfoCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ReportDiscountParkInformationActivity.this);
                builder.setTitle("제보 취소 확인");
                builder.setMessage("정말 취소하시겠습니까\n(되돌릴 수 없습니다)");
                builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        fd.cancelReport(loginId, documentId, new OnFirestoreDataLoadedListener() {
                            @Override
                            public void onDataLoaded(Object data) {
                                Toast.makeText(getApplicationContext(), "제보 취소되었습니다", Toast.LENGTH_SHORT).show();
                                finish();
                            }

                            @Override
                            public void onDataLoadError(String errorMessage) {

                            }
                        });
                    }
                });
                builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    void init () {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                reportInfoIdContentTxt.setText(documentId);

                if ((boolean) reportInfo.get("isApproval")) {
                    reportInfoStatusContentTxt.setText("승인됨");
                    reportInfoCancelBtn.setVisibility(View.GONE);
                }
                else if ((boolean) reportInfo.get("isCancelled")) {
                    reportInfoStatusContentTxt.setText("취소됨");
                    reportInfoCancelBtn.setVisibility(View.GONE);
                }
                else {
                    reportInfoStatusContentTxt.setText("평가 중");
                    reportInfoCancelBtn.setVisibility(View.VISIBLE);
                }

                reportInfoParkNameContentTxt.setText((String) reportInfo.get("parkName"));

                reportInfoConditionContentTxt.setText((String) reportInfo.get("parkCondition"));

                long discount = (long) reportInfo.get("parkDiscount");

                if (discount == 0) {
                    reportInfoDiscountContentTxt.setText("무료");
                    reportInfoWonTxt.setVisibility(View.GONE);
                }
                else {
                    reportInfoDiscountContentTxt.setText(Long.toString(discount));
                    reportInfoWonTxt.setVisibility(View.VISIBLE);
                }

                String rate = (long) reportInfo.get("ratePerfectCount") + " / " + (long) reportInfo.get("rateMistakeCount") + " / " + (long) reportInfo.get("rateWrongCount");
                reportInfoRateContentTxt.setText(rate);

                Timestamp timestamp = (Timestamp) reportInfo.get("upTime");
                Date date = timestamp.toDate();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일 HH:mm:ss", Locale.KOREA);
                String dateString = sdf.format(date);
                reportInfoUpTimeContentTxt.setText(dateString);

                TMapData tMapdata = new TMapData();
                tMapdata.reverseGeocoding((double) reportInfo.get("poiLat"), (double) reportInfo.get("poiLon"), "A10", new TMapData.reverseGeocodingListenerCallback() {
                    @Override
                    public void onReverseGeocoding(TMapAddressInfo tMapAddressInfo) {
                        if (tMapAddressInfo != null) {
                            String[] adrresses = tMapAddressInfo.strFullAddress.split(",");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    reportInfoParkNewAddressContentTxt.setText(adrresses[2]);
                                    reportInfoParkOldAddressContentTxt.setText(adrresses[1]);
                                }
                            });
                        }
                    }
                });
            }
        });
    }
}