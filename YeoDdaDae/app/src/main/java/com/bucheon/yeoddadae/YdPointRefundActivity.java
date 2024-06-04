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

import java.text.NumberFormat;
import java.util.Locale;

public class YdPointRefundActivity extends AppCompatActivity {
    String loginId;
    long ydPoint;
    int refundPoint;

    Button refundBackBtn;
    TextView refundHavePointContentTxt;
    TextView refundTargetPointContentEditTxt;
    Button refundBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yd_point_refund);

        refundBackBtn = findViewById(R.id.refundBackBtn);
        refundHavePointContentTxt = findViewById(R.id.refundHavePointContentTxt);
        refundTargetPointContentEditTxt = findViewById(R.id.refundTargetPointContentEditTxt);
        refundBtn = findViewById(R.id.refundBtn);

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
                        refundHavePointContentTxt.setText(formattedYdPoint);
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

        refundBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        refundBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    refundPoint = Integer.parseInt(refundTargetPointContentEditTxt.getText().toString());
                }
                catch (NumberFormatException e) {
                    Toast.makeText(getApplicationContext(), "환급할 금액을 숫자로 입력해주세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                fd.refundYdPoint(loginId, refundPoint, new OnFirestoreDataLoadedListener() {
                    @Override
                    public void onDataLoaded(Object data) {
                        Toast.makeText(getApplicationContext(), "환급 완료", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onDataLoadError(String errorMessage) {
                        Log.d(TAG, errorMessage);
                        if (errorMessage.equals("환급 포인트가 보유 포인트보다 큽니다")) {
                            Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "오류 발생", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
}
