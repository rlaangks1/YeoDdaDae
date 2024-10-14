package com.bucheon.yeoddadae;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.Timestamp;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class YdPointChargeActivity extends AppCompatActivity {
    String loginId;
    long ydPoint = -1;
    int chargePoint;
    FirestoreDatabase fd;

    ImageButton chargeBackBtn;
    TextView chargeNowTimeContentTxt;
    TextView chargeHavePointContentTxt;
    Spinner chargePointSpinner;
    TextView chargePointWonTxt;
    TextView chargeAfterChargePointContentTxt;
    ImageButton chargeBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yd_point_charge);

        chargeBackBtn = findViewById(R.id.chargeBackBtn);
        chargeNowTimeContentTxt =findViewById(R.id.chargeNowTimeContentTxt);
        chargeHavePointContentTxt = findViewById(R.id.chargeHavePointContentTxt);
        chargePointSpinner = findViewById(R.id.chargePointSpinner);
        chargePointWonTxt = findViewById(R.id.chargePointWonTxt);
        chargeAfterChargePointContentTxt = findViewById(R.id.chargeAfterChargePointContentTxt);
        chargeBtn = findViewById(R.id.chargeBtn);

        Intent inIntent = getIntent();
        loginId = inIntent.getStringExtra("loginId");

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.my_spinner_charge_point_items,
                R.layout.spinner_my_2
        );
        chargePointSpinner.setAdapter(adapter);

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

                if (selectedItem.equals("1,000pt")) {
                    chargePoint = 1000;
                }
                else if (selectedItem.equals("5,000pt")) {
                    chargePoint = 5000;
                }
                else if (selectedItem.equals("10,000pt")) {
                    chargePoint = 10000;
                }
                else if (selectedItem.equals("30,000pt")) {
                    chargePoint = 30000;
                }
                else if (selectedItem.equals("50,000pt")) {
                    chargePoint = 50000;
                }
                else if (selectedItem.equals("100,000pt")) {
                    chargePoint = 100000;
                }

                String formattedWon = NumberFormat.getNumberInstance(Locale.KOREA).format(chargePoint);
                chargePointWonTxt.setText("(" + formattedWon + "원 )");

                if (ydPoint != -1 && chargePoint != 0) {
                    String formattedAfterChargePoint = NumberFormat.getNumberInstance(Locale.KOREA).format(ydPoint + chargePoint);

                    chargeAfterChargePointContentTxt.setText(formattedAfterChargePoint);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        chargeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (chargePoint != 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(YdPointChargeActivity.this);
                    builder.setTitle("충전하시겠습니까?");
                    builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
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
                    builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();

                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(YdPointChargeActivity.this, R.color.sub));
                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(YdPointChargeActivity.this, R.color.disable));
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

        getYdPoint();
    }

    void getYdPoint () {
        Timestamp now = Timestamp.now();

        if (now != null) {
            Date date = now.toDate();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일 HH:mm:ss", Locale.KOREA);
            String dateString = sdf.format(date);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    chargeNowTimeContentTxt.setText(dateString);
                }
            });
        }

        fd.loadYdPoint(loginId, new OnFirestoreDataLoadedListener() {
            @Override
            public void onDataLoaded(Object data) {
                ydPoint = (long) data;
                String formattedYdPoint = NumberFormat.getNumberInstance(Locale.KOREA).format(ydPoint);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        chargeHavePointContentTxt.setText(formattedYdPoint);

                        if (ydPoint != -1 && chargePoint != 0) {
                            String formattedAfterChargePoint = NumberFormat.getNumberInstance(Locale.KOREA).format(ydPoint + chargePoint);

                            chargeAfterChargePointContentTxt.setText(formattedAfterChargePoint);
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
}
