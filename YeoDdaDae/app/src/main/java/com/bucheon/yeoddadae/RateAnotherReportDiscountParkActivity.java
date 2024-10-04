package com.bucheon.yeoddadae;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

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
    FirestoreDatabase fd;

    ImageButton reportInfoBackBtn;
    TextView rateReportIdContentTxt;
    TextView rateReportParkNameContentTxt;
    TextView rateReportParkNewAddressContentTxt;
    TextView rateReportParkOldAddressContentTxt;
    TextView rateReportConditionContentTxt;
    TextView rateReportDiscountContentTxt;
    TextView rateReportWonTxt;
    TextView rateReportUpTimeContentTxt;
    ImageButton rateReportPerfectBtn;
    TextView perfectTxt;
    ImageButton rateReportMistakeBtn;
    TextView mistakeTxt;
    ImageButton rateReportWrongBtn;
    TextView wrongTxt;
    View tempViewForMargin;
    ConstraintLayout rateReportReasonConstLayout;
    EditText rateReportReasonEditTxt;
    ImageButton rateReportReasonUploadBtn;

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
        perfectTxt = findViewById(R.id.perfectTxt);
        rateReportMistakeBtn = findViewById(R.id.rateReportMistakeBtn);
        mistakeTxt = findViewById(R.id.mistakeTxt);
        rateReportWrongBtn = findViewById(R.id.rateReportWrongBtn);
        wrongTxt = findViewById(R.id.wrongTxt);
        tempViewForMargin = findViewById(R.id.tempViewForMargin);
        rateReportReasonConstLayout = findViewById(R.id.rateReportReasonConstLayout);
        rateReportReasonEditTxt = findViewById(R.id.rateReportReasonEditTxt);
        rateReportReasonUploadBtn = findViewById(R.id.rateReportReasonUploadBtn);

        fd = new FirestoreDatabase();
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

        rateReportReasonUploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!replaceNewlinesAndTrim(rateReportReasonEditTxt).isEmpty()) {
                    fd = new FirestoreDatabase();
                    fd.updateReason(loginId, documentId, replaceNewlinesAndTrim(rateReportReasonEditTxt), new OnFirestoreDataLoadedListener() {
                        @Override
                        public void onDataLoaded(Object data) {
                            Toast.makeText(getApplicationContext(), "평가 이유 업로드 되었습니다", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onDataLoadError(String errorMessage) {
                            Log.d(TAG, errorMessage);
                            Toast.makeText(getApplicationContext(), "오류 발생", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    void getRateCount () {
        FirestoreDatabase fd = new FirestoreDatabase();

        fd.loadRateCount(loginId, documentId, new OnFirestoreDataLoadedListener() {
            @Override
            public void onDataLoaded(Object data) {
                long[] rates = (long[]) data;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        perfectTxt.setText(Long.toString(rates[0]));
                        mistakeTxt.setText(Long.toString(rates[1]));
                        wrongTxt.setText(Long.toString(rates[2]));

                        if (rates[3] == 0) {
                            rateReportPerfectBtn.setImageResource(R.drawable.disabled_button);
                            rateReportMistakeBtn.setImageResource(R.drawable.disabled_button);
                            rateReportWrongBtn.setImageResource(R.drawable.disabled_button);
                            tempViewForMargin.setVisibility(View.VISIBLE);
                            rateReportReasonConstLayout.setVisibility(View.GONE);
                        }
                        else if (rates[3] == 1) {
                            rateReportPerfectBtn.setImageResource(R.drawable.gradate_button);
                            rateReportMistakeBtn.setImageResource(R.drawable.disabled_button);
                            rateReportWrongBtn.setImageResource(R.drawable.disabled_button);
                            tempViewForMargin.setVisibility(View.VISIBLE);
                            rateReportReasonConstLayout.setVisibility(View.GONE);
                        }
                        else if (rates[3] == 2) {
                            rateReportPerfectBtn.setImageResource(R.drawable.disabled_button);
                            rateReportMistakeBtn.setImageResource(R.drawable.gradate_button);
                            rateReportWrongBtn.setImageResource(R.drawable.disabled_button);
                            tempViewForMargin.setVisibility(View.GONE);
                            rateReportReasonConstLayout.setVisibility(View.VISIBLE);
                            getReason();
                        }
                        else if (rates[3] == 3) {
                            rateReportPerfectBtn.setImageResource(R.drawable.disabled_button);
                            rateReportMistakeBtn.setImageResource(R.drawable.disabled_button);
                            rateReportWrongBtn.setImageResource(R.drawable.gradate_button);
                            tempViewForMargin.setVisibility(View.VISIBLE);
                            rateReportReasonConstLayout.setVisibility(View.GONE);
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

    void getReason() {
        fd = new FirestoreDatabase();
        fd.loadReason(loginId, documentId, new OnFirestoreDataLoadedListener() {
            @Override
            public void onDataLoaded(Object data) {
                String reason = (String) data;
                rateReportReasonEditTxt.setText(reason);
            }

            @Override
            public void onDataLoadError(String errorMessage) {
                Log.d(TAG, errorMessage);
                Toast.makeText(getApplicationContext(), "오류 발생", Toast.LENGTH_SHORT).show();
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

    String replaceNewlinesAndTrim(EditText et) {
        return et.getText().toString().replaceAll("\\n", " ").trim();
    }
}