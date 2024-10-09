package com.bucheon.yeoddadae;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.Timestamp;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class YdPointRefundActivity extends AppCompatActivity {
    String loginId;
    long ydPoint;
    int refundPoint;
    int defaultTextColor;
    FirestoreDatabase fd;

    ImageButton refundBackBtn;
    TextView refundNowTimeContentTxt;
    TextView refundHavePointContentTxt;
    EditText refundTargetPointContentEditTxt;
    TextView refundWonTxt;
    EditText refundBankContentEditTxt;
    EditText refundAccountNumberContentEditTxt;
    TextView refundAfterRefundPointContentTxt;
    TextView refundAfterRefundPointPtTxt;
    ImageButton refundBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yd_point_refund);

        refundBackBtn = findViewById(R.id.refundBackBtn);
        refundNowTimeContentTxt =findViewById(R.id.refundNowTimeContentTxt);
        refundHavePointContentTxt = findViewById(R.id.refundHavePointContentTxt);
        refundTargetPointContentEditTxt = findViewById(R.id.refundTargetPointContentEditTxt);
        refundWonTxt = findViewById(R.id.refundWonTxt);
        refundBankContentEditTxt = findViewById(R.id.refundBankContentEditTxt);
        refundAccountNumberContentEditTxt = findViewById(R.id.refundAccountNumberContentEditTxt);
        refundAfterRefundPointContentTxt = findViewById(R.id.refundAfterRefundPointContentTxt);
        refundAfterRefundPointPtTxt = findViewById(R.id.refundAfterRefundPointPtTxt);
        refundBtn = findViewById(R.id.refundBtn);

        defaultTextColor = refundAfterRefundPointContentTxt.getCurrentTextColor();

        Intent inIntent = getIntent();
        loginId = inIntent.getStringExtra("loginId");

        fd = new FirestoreDatabase();

        refundBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        refundTargetPointContentEditTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String refundWonString = replaceNewlinesAndTrim(refundTargetPointContentEditTxt);

                DecimalFormat formatter = new DecimalFormat("#,###");

                if (refundWonString == null || refundWonString.isEmpty()) {
                    refundWonTxt.setText("(0 원)");
                    refundAfterRefundPointContentTxt.setText(formatter.format(ydPoint));
                    refundAfterRefundPointContentTxt.setTextColor(defaultTextColor);
                    refundAfterRefundPointPtTxt.setTextColor(defaultTextColor);
                }
                else {
                    long won = Long.parseLong(replaceNewlinesAndTrim(refundTargetPointContentEditTxt));
                    String formattedWon = formatter.format(won);
                    refundWonTxt.setText("(" +  formattedWon+ " 원)");

                    String formattedAfterRefundPoint = NumberFormat.getNumberInstance(Locale.KOREA).format(ydPoint - won);
                    refundAfterRefundPointContentTxt.setText(formattedAfterRefundPoint);

                    if (ydPoint - won < 0) {
                        int redColor = Color.rgb(255, 64, 64);

                        refundAfterRefundPointContentTxt.setTextColor(redColor);
                        refundAfterRefundPointPtTxt.setTextColor(redColor);
                    }
                    else if (ydPoint - won == 0) {
                        refundAfterRefundPointContentTxt.setTextColor(defaultTextColor);
                        refundAfterRefundPointPtTxt.setTextColor(defaultTextColor);
                    }
                    else if (ydPoint - won > 0) {
                        int blueColor = Color.rgb(64, 64, 255);

                        refundAfterRefundPointContentTxt.setTextColor(blueColor);
                        refundAfterRefundPointPtTxt.setTextColor(blueColor);
                    }
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        refundBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String targetPoint = replaceNewlinesAndTrim(refundTargetPointContentEditTxt);
                String bank = replaceNewlinesAndTrim(refundBankContentEditTxt);
                String accountNumber = replaceNewlinesAndTrim(refundAccountNumberContentEditTxt);

                if (targetPoint.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "환급할 금액을 입력해주세요", Toast.LENGTH_SHORT).show();
                }
                else if (bank.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "환급할 계좌의 은행을 입력해주세요", Toast.LENGTH_SHORT).show();
                }
                else if (accountNumber.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "환급할 계좌번호를 입력해주세요", Toast.LENGTH_SHORT).show();
                }
                else if (accountNumber.length() < 10 || accountNumber.length() > 14) {
                    Toast.makeText(getApplicationContext(), "계좌번호는 10~14자 입니다", Toast.LENGTH_SHORT).show();
                }
                else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(YdPointRefundActivity.this);
                    builder.setTitle("환급하시겠습니까?");
                    builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            refundPoint = Integer.parseInt(replaceNewlinesAndTrim(refundTargetPointContentEditTxt));

                            fd.refundYdPoint(loginId, refundPoint, bank, accountNumber, new OnFirestoreDataLoadedListener() {
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
                    builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();

                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(YdPointRefundActivity.this, R.color.sub));
                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(YdPointRefundActivity.this, R.color.disable));
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        getYdPoint();
    }

    void getYdPoint() {
        Timestamp now = Timestamp.now();

        if (now != null) {
            Date date = now.toDate();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일 HH:mm:ss", Locale.KOREA);
            String dateString = sdf.format(date);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    refundNowTimeContentTxt.setText(dateString);
                }
            });
        }

        fd.loadYdPoint(loginId, new OnFirestoreDataLoadedListener() {
            @Override
            public void onDataLoaded(Object data) {
                ydPoint = (long) data;
                DecimalFormat formatter = new DecimalFormat("#,###");
                String formattedYdPoint = formatter.format(ydPoint);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        refundHavePointContentTxt.setText(formattedYdPoint);
                        refundAfterRefundPointContentTxt.setText(formattedYdPoint);
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

    String replaceNewlinesAndTrim(EditText et) {
        return et.getText().toString().replaceAll("\\n", " ").trim();
    }
}
