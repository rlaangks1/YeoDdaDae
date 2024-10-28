package com.bucheon.yeoddadae;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.Timestamp;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.address_info.TMapAddressInfo;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class ApproveReportInformationActivity extends AppCompatActivity {
    String documentId;
    HashMap<String, Object> reportInfo;
    ReasonDialog rd;

    ImageButton approveReportInfoBackBtn;
    TextView approveReportInfoIdContentTxt;
    TextView approveReportInfoParkNameContentTxt;
    TextView approveReportInfoParkNewAddressContentTxt;
    TextView approveReportInfoParkOldAddressContentTxt;
    TextView approveReportInfoConditionAndDiscountContentTxt;
    TextView approveReportInfoRateContentTxt;
    TextView approveReportInfoUpTimeContentTxt;
    ImageButton approveReportInfoShowReasonBtn;
    ImageButton approveReportInfoApproveBtn;
    ImageButton approveReportInfoRejectionBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apporve_report_information);

        approveReportInfoBackBtn = findViewById(R.id.approveReportInfoBackBtn);
        approveReportInfoIdContentTxt = findViewById(R.id.approveReportInfoIdContentTxt);
        approveReportInfoParkNameContentTxt = findViewById(R.id.approveReportInfoParkNameContentTxt);
        approveReportInfoParkNewAddressContentTxt = findViewById(R.id.approveReportInfoParkNewAddressContentTxt);
        approveReportInfoParkOldAddressContentTxt = findViewById(R.id.approveReportInfoParkOldAddressContentTxt);
        approveReportInfoConditionAndDiscountContentTxt = findViewById(R.id.approveReportInfoConditionAndDiscountContentTxt);
        approveReportInfoRateContentTxt =findViewById(R.id.approveReportInfoRateContentTxt);
        approveReportInfoUpTimeContentTxt = findViewById(R.id.approveReportInfoUpTimeContentTxt);
        approveReportInfoShowReasonBtn = findViewById(R.id.approveReportInfoShowReasonBtn);
        approveReportInfoApproveBtn = findViewById(R.id.approveReportInfoApproveBtn);
        approveReportInfoRejectionBtn = findViewById(R.id.approveReportInfoRejectionBtn);

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

        approveReportInfoShowReasonBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rd = new ReasonDialog(documentId, ApproveReportInformationActivity.this);
                rd.show();
            }
        });

        approveReportInfoApproveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fd.approveReport(documentId, new OnFirestoreDataLoadedListener() {
                    @Override
                    public void onDataLoaded(Object data) {
                        Toast.makeText(getApplicationContext(), "제보주차장 승인되었습니다", Toast.LENGTH_SHORT).show();
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

        approveReportInfoRejectionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ApproveReportInformationActivity.this);
                builder.setTitle("거부 사유 입력");

                // Set up the input
                final EditText input = new EditText(ApproveReportInformationActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String cancelReason = replaceNewlinesAndTrim(input);
                        fd.cancelReport((String) reportInfo.get("reporterId"), documentId, cancelReason, new OnFirestoreDataLoadedListener() {
                            @Override
                            public void onDataLoaded(Object data) {
                                Toast.makeText(getApplicationContext(), "제보주차장 거부되었습니다", Toast.LENGTH_SHORT).show();
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
                builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();

                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(ApproveReportInformationActivity.this, R.color.sub));
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(ApproveReportInformationActivity.this, R.color.disable));
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

                ArrayList<String> condition = (ArrayList<String>) reportInfo.get("condition");
                ArrayList<Long> discount = (ArrayList<Long>) reportInfo.get("discount");
                String conditionAndDscountString = "";
                if (condition != null && discount != null && condition.size() == discount.size()) {
                    DecimalFormat formatter = new DecimalFormat("#,###");

                    for (int i = 0; i < condition.size(); i++) {
                        conditionAndDscountString += condition.get(i);

                        if (discount.get(i) == 0) {
                            conditionAndDscountString += "/무료";
                        }
                        else {
                            long oneDiscount = discount.get(i);
                            String formattedDiscount= formatter.format(oneDiscount);
                            conditionAndDscountString += "/" + formattedDiscount + "원 할인";
                        }
                        if (i != condition.size() - 1) {
                            conditionAndDscountString += "\n";
                        }
                    }
                }
                approveReportInfoConditionAndDiscountContentTxt.setText(conditionAndDscountString);

                approveReportInfoRateContentTxt.setText((long) reportInfo.get("ratePerfectCount") + " / " + (long) reportInfo.get("rateMistakeCount") + " / " + (long) reportInfo.get("rateWrongCount"));

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

    String replaceNewlinesAndTrim(EditText et) {
        return et.getText().toString().replaceAll("\\n", " ").trim();
    }
}
