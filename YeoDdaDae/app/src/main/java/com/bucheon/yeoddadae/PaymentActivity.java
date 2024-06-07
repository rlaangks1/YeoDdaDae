package com.bucheon.yeoddadae;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.ExoPlayerLibraryInfo;
import com.google.firebase.firestore.FieldValue;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.format.ArrayWeekDayFormatter;
import com.prolificinteractive.materialcalendarview.format.TitleFormatter;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.address_info.TMapAddressInfo;

import org.threeten.bp.LocalDate;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PaymentActivity extends AppCompatActivity {
    String payType;
    String shareParkDocumentName;
    String loginId;
    HashMap<String, ArrayList<String>> reservationTime;
    int price;
    long ydPoint;
    FirestoreDatabase fd;

    Button paymentBackBtn;
    TextView paymentTotalPriceContentTxt;
    TextView paymentYdPointContentTxt;
    Button paymentPayBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        paymentBackBtn = findViewById(R.id.paymentBackBtn);
        paymentTotalPriceContentTxt = findViewById(R.id.paymentTotalPriceContentTxt);
        paymentYdPointContentTxt = findViewById(R.id.paymentYdPointContentTxt);
        paymentPayBtn = findViewById(R.id.paymentPayBtn);

        Intent inIntent = getIntent();
        shareParkDocumentName = inIntent.getStringExtra("shareParkDocumentName");
        payType = inIntent.getStringExtra("payType");
        loginId = inIntent.getStringExtra("id");
        reservationTime = (HashMap<String, ArrayList<String>>) inIntent.getSerializableExtra("time");
        price = inIntent.getIntExtra("price", -1);

        if (price == -1) {
            Log.d(TAG, "price값 오류");
            finish();
        }

        fd = new FirestoreDatabase();

        paymentBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                setResult(RESULT_CANCELED, returnIntent);
                finish();
            }
        });

        paymentPayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fd = new FirestoreDatabase();
                fd.payByYdPoint(loginId, price, new OnFirestoreDataLoadedListener() {
                    @Override
                    public void onDataLoaded(Object data) {
                        HashMap<String, Object> hm = new HashMap<>();
                        hm.put("shareParkDocumentName", shareParkDocumentName);
                        hm.put("id", loginId);
                        hm.put("time", reservationTime);
                        hm.put("isCancelled", false);
                        hm.put("upTime", FieldValue.serverTimestamp());
                        hm.put("price", price);
                        fd.insertData("reservation", hm, new OnFirestoreDataLoadedListener() {
                            @Override
                            public void onDataLoaded(Object data) {
                                String reservationDocumentId = (String) data;

                                HashMap<String, Object> hm = new HashMap<>();
                                hm.put("id", loginId);
                                hm.put("type", payType);
                                hm.put("reservationId", reservationDocumentId);
                                hm.put("price", price);
                                hm.put("upTime", FieldValue.serverTimestamp());
                                fd.insertData("spendYdPointHistory", hm, new OnFirestoreDataLoadedListener() {
                                    @Override
                                    public void onDataLoaded(Object data) {
                                        Toast.makeText(getApplicationContext(), "예약되었습니다", Toast.LENGTH_SHORT).show();

                                        Intent returnIntent = new Intent();
                                        setResult(RESULT_OK, returnIntent);

                                        finish();
                                    }

                                    @Override
                                    public void onDataLoadError(String errorMessage) {
                                        Log.d(TAG, errorMessage);
                                        Toast.makeText(getApplicationContext(), "소비 문서 추가 중 오류 발생", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            @Override
                            public void onDataLoadError(String errorMessage) {
                                Log.d(TAG, errorMessage);
                                Toast.makeText(getApplicationContext(), "예약 문서 추가 중 오류 발생", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onDataLoadError(String errorMessage) {
                        if (errorMessage.equals("포인트가 부족합니다")) {
                            new AlertDialog.Builder(PaymentActivity.this)
                                    .setTitle("포인트가 부족합니다")
                                    .setMessage("충전하시겠습니까?")
                                    .setPositiveButton("예", (dialog, which) -> {
                                        Intent ydPointChargeIntent = new Intent(getApplicationContext(), YdPointChargeActivity.class);
                                        ydPointChargeIntent.putExtra("loginId", loginId);
                                        startActivity(ydPointChargeIntent);
                                    })
                                    .setNegativeButton("아니오", (dialog, which) -> dialog.dismiss())
                                    .show();
                        } else {
                            Log.d(TAG, errorMessage);
                            Toast.makeText(getApplicationContext(), "결제 시도 중 오류 발생", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        fd.loadYdPoint(loginId, new OnFirestoreDataLoadedListener() {
            @Override
            public void onDataLoaded(Object data) {
                ydPoint = (long) data;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        paymentYdPointContentTxt.setText(Long.toString(ydPoint));
                        paymentTotalPriceContentTxt.setText(Integer.toString(price));
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
