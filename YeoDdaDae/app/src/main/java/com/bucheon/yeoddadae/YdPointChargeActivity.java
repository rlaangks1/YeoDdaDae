package com.bucheon.yeoddadae;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FieldValue;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class YdPointChargeActivity extends AppCompatActivity {
    String loginId;
    long ydPoint;
    int chargePoint;

    ImageButton chargeBackBtn;
    TextView chargeHavePointContentTxt;
    TextView chargeTargetPointContentEditTxt;
    ImageButton chargeBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yd_point_charge);

        chargeBackBtn = findViewById(R.id.chargeBackBtn);
        chargeHavePointContentTxt = findViewById(R.id.chargeHavePointContentTxt);
        chargeTargetPointContentEditTxt = findViewById(R.id.chargeTargetPointContentEditTxt);
        chargeBtn = findViewById(R.id.chargeBtn);

        Intent inIntent = getIntent();
        loginId = inIntent.getStringExtra("loginId");

        FirestoreDatabase fd = new FirestoreDatabase();
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

        chargeBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        chargeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    chargePoint = Integer.parseInt(chargeTargetPointContentEditTxt.getText().toString());
                }
                catch (NumberFormatException e) {
                    Toast.makeText(getApplicationContext(), "충전할 금액을 숫자로 입력해주세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                fd.chargeYdPoint(loginId, chargePoint, new OnFirestoreDataLoadedListener() {
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
        });
    }
}
