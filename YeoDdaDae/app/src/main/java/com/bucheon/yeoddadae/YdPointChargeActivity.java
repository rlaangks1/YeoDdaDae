package com.bucheon.yeoddadae;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldValue;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class YdPointChargeActivity extends AppCompatActivity {
    String loginId;
    long ydPoint;
    int chargePoint;
    int price;
    FirestoreDatabase fd;

    ImageButton chargeBackBtn;
    TextView chargeHavePointContentTxt;
    Spinner chargePointSpinner;
    TextView anotherReportWonTxt;
    ImageButton chargeBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yd_point_charge);

        chargeBackBtn = findViewById(R.id.chargeBackBtn);
        chargeHavePointContentTxt = findViewById(R.id.chargeHavePointContentTxt);
        chargePointSpinner = findViewById(R.id.chargePointSpinner);
        anotherReportWonTxt = findViewById(R.id.anotherReportWonTxt);
        chargeBtn = findViewById(R.id.chargeBtn);

        Intent inIntent = getIntent();
        loginId = inIntent.getStringExtra("loginId");

        fd = new FirestoreDatabase();

        chargeBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        chargePointSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();

                if (selectedItem.equals("1000pt")) {
                    chargePoint = 1000;
                }
                else if (selectedItem.equals("5000pt")) {
                    chargePoint = 5000;
                }
                else if (selectedItem.equals("10000pt")) {
                    chargePoint = 10000;
                }
                else if (selectedItem.equals("30000pt")) {
                    chargePoint = 30000;
                }
                else if (selectedItem.equals("50000pt")) {
                    chargePoint = 50000;
                }
                else if (selectedItem.equals("100000pt")) {
                    chargePoint = 100000;
                }
                price = chargePoint * 110 / 100;

                String formattedPrice = NumberFormat.getNumberInstance(Locale.KOREA).format(price);

                anotherReportWonTxt.setText("(" + formattedPrice + "원)");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        chargeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (chargePoint != 0 && price != 0) {
                    fd.chargeYdPoint(loginId, chargePoint, price, new OnFirestoreDataLoadedListener() {
                        @Override
                        public void onDataLoaded(Object data) {
                            Toast.makeText(getApplicationContext(), "충전 완료", Toast.LENGTH_SHORT).show();
                            finish();
                        }

                        @Override
                        public void onDataLoadError(String errorMessage) {
                            Log.d(TAG, errorMessage);
                            Toast.makeText(getApplicationContext(), "오류 발생", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else {
                    Toast.makeText(getApplicationContext(), "포인트를 설정해주세요", Toast.LENGTH_SHORT).show();
                }
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
                String formattedYdPoint = NumberFormat.getNumberInstance(Locale.KOREA).format(ydPoint);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        chargeHavePointContentTxt.setText(formattedYdPoint);
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
