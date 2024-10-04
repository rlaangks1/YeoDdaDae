package com.bucheon.yeoddadae;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.Timestamp;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.address_info.TMapAddressInfo;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class UserManagementInformationActivity extends AppCompatActivity {
    String id;
    String email;
    long ydPoint;
    Timestamp registerTime;
    String status;
    Timestamp pauseLimit;
    String pauseLimitDateString;
    String pauseLimitTimeString;
    FirestoreDatabase fd;

    ImageButton userManagementInfoBackBtn;
    TextView userManagementInfoIdContentTxt;
    TextView userManagementInfoEmailContentTxt;
    TextView userManagementInfoYdPointContentTxt;
    TextView userManagementInfoRegisterTimeContentTxt;
    TextView userManagementInfoStatusContentTxt;
    View tempViewForMargin;
    View userManagementInfoStatusLine;
    TextView userManagementInfoPauseLimitTxt;
    TextView userManagementInfoPauseLimitContentTxt;
    ImageButton userManagementInfoResumeBtn;
    TextView userManagementInfoResumeTxt;
    ImageButton userManagementInfoPauseBtn;
    TextView userManagementInfoPauseTxt;
    ImageButton userManagementInfoForceWithdrawalBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_management_information);

        userManagementInfoBackBtn = findViewById(R.id.userManagementInfoBackBtn);
        userManagementInfoIdContentTxt = findViewById(R.id.userManagementInfoIdContentTxt);
        userManagementInfoEmailContentTxt = findViewById(R.id.userManagementInfoEmailContentTxt);
        userManagementInfoYdPointContentTxt = findViewById(R.id.userManagementInfoYdPointContentTxt);
        userManagementInfoRegisterTimeContentTxt = findViewById(R.id.userManagementInfoRegisterTimeContentTxt);
        userManagementInfoStatusContentTxt = findViewById(R.id.userManagementInfoStatusContentTxt);
        tempViewForMargin = findViewById(R.id.tempViewForMargin);
        userManagementInfoStatusLine = findViewById(R.id.userManagementInfoStatusLine);
        userManagementInfoPauseLimitTxt = findViewById(R.id.userManagementInfoPauseLimitTxt);
        userManagementInfoPauseLimitContentTxt = findViewById(R.id.userManagementInfoPauseLimitContentTxt);
        userManagementInfoResumeBtn = findViewById(R.id.userManagementInfoResumeBtn);
        userManagementInfoResumeTxt = findViewById(R.id.userManagementInfoResumeTxt);
        userManagementInfoPauseBtn = findViewById(R.id.userManagementInfoPauseBtn);
        userManagementInfoPauseTxt = findViewById(R.id.userManagementInfoPauseTxt);
        userManagementInfoForceWithdrawalBtn = findViewById(R.id.userManagementInfoForceWithdrawalBtn);

        Intent inIntent = getIntent();
        id = inIntent.getStringExtra("id");
        email = inIntent.getStringExtra("email");
        ydPoint = inIntent.getLongExtra("ydPoint", -1);
        long registerTimeSec = inIntent.getLongExtra("registerTimeSec", -1);
        int registerTimeNanoSec = inIntent.getIntExtra("registerTimeNanoSec", -1);

        if (id == null || id.isEmpty() || email == null || email.isEmpty() || ydPoint == -1 || registerTimeSec == -1 || registerTimeNanoSec == -1) {
            Log.d(TAG, "인텐트 extra 전달 받기 오류");
            Toast.makeText(getApplicationContext(), "오류 발생", Toast.LENGTH_SHORT).show();
            finish();
        }
        registerTime = new Timestamp(registerTimeSec, registerTimeNanoSec);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                userManagementInfoIdContentTxt.setText(id);
                userManagementInfoEmailContentTxt.setText(email);

                DecimalFormat formatter = new DecimalFormat("#,###");
                userManagementInfoYdPointContentTxt.setText(formatter.format(ydPoint));

                if (registerTime != null) {
                    Date date = registerTime.toDate();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일 HH:mm:ss", Locale.KOREA);
                    String dateString = sdf.format(date);
                    userManagementInfoRegisterTimeContentTxt.setText(dateString);
                }
            }
        });

        userManagementInfoBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        userManagementInfoResumeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(UserManagementInformationActivity.this);
                builder.setTitle("사용 재개 확인");
                builder.setMessage("정말 사용 재개 하시겠습니까");
                builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        fd.resumeUser(id, new OnFirestoreDataLoadedListener() {
                            @Override
                            public void onDataLoaded(Object data) {
                                Toast.makeText(getApplicationContext(), "사용 재게 되었습니다", Toast.LENGTH_SHORT).show();
                                getStatus();
                            }

                            @Override
                            public void onDataLoadError(String errorMessage) {
                                Log.d(TAG, errorMessage);
                                Toast.makeText(getApplicationContext(), "오류 발생", Toast.LENGTH_SHORT).show();
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

                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(UserManagementInformationActivity.this, R.color.sub));
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(UserManagementInformationActivity.this, R.color.disable));
            }
        });

        userManagementInfoPauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(UserManagementInformationActivity.this);
                builder.setTitle("사용 정지 확인");
                builder.setMessage("정말 사용 정지 하시겠습니까");
                builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CustomDatePickerDialog sdpd = new CustomDatePickerDialog(UserManagementInformationActivity.this, null, 0);
                        sdpd.show();
                    }
                });
                builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();

                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(UserManagementInformationActivity.this, R.color.sub));
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(UserManagementInformationActivity.this, R.color.disable));
            }
        });

        userManagementInfoForceWithdrawalBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(UserManagementInformationActivity.this);
                builder.setTitle("강제 탈퇴 확인");
                builder.setMessage("정말 강제 탈퇴 하시겠습니까");
                builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        fd.forceWithdrawalUser(id, new OnFirestoreDataLoadedListener() {
                            @Override
                            public void onDataLoaded(Object data) {
                                Toast.makeText(getApplicationContext(), "강제 탈퇴 되었습니다", Toast.LENGTH_SHORT).show();
                                getStatus();
                            }

                            @Override
                            public void onDataLoadError(String errorMessage) {
                                Log.d(TAG, errorMessage);
                                Toast.makeText(getApplicationContext(), "오류 발생", Toast.LENGTH_SHORT).show();
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

                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(UserManagementInformationActivity.this, R.color.sub));
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(UserManagementInformationActivity.this, R.color.disable));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        getStatus();
    }

    void getStatus() {
        fd = new FirestoreDatabase();
        fd.loadStatusOfUser(id, new OnFirestoreDataLoadedListener() {
            @Override
            public void onDataLoaded(Object data) {
                if (data instanceof String) {
                    status = (String) data;
                }
                else if (data instanceof HashMap<?, ?>) {
                    HashMap<String, Object> result = (HashMap<String, Object>) data;

                    status = (String) result.get("status");
                    pauseLimit = (Timestamp) result.get("pauseLimit");
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        userManagementInfoStatusContentTxt.setText(status);

                        if (pauseLimit != null) {
                            Date date = pauseLimit.toDate();
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일 HH:mm:ss", Locale.KOREA);
                            String dateString = sdf.format(date);
                            userManagementInfoPauseLimitContentTxt.setText(dateString);
                        }
                    }
                });

                if (status.equals("정상")) {
                    userManagementInfoResumeBtn.setVisibility(View.GONE);
                    userManagementInfoResumeTxt.setVisibility(View.GONE);
                    userManagementInfoPauseBtn.setVisibility(View.VISIBLE);
                    userManagementInfoPauseTxt.setVisibility(View.VISIBLE);
                    tempViewForMargin.setVisibility(View.VISIBLE);
                    userManagementInfoStatusLine.setVisibility(View.GONE);
                    userManagementInfoPauseLimitTxt.setVisibility(View.GONE);
                    userManagementInfoPauseLimitContentTxt.setVisibility(View.GONE);
                }
                else if (status.equals("사용 정지")) {
                    userManagementInfoResumeBtn.setVisibility(View.VISIBLE);
                    userManagementInfoResumeTxt.setVisibility(View.VISIBLE);
                    userManagementInfoPauseBtn.setVisibility(View.GONE);
                    userManagementInfoPauseTxt.setVisibility(View.GONE);
                    tempViewForMargin.setVisibility(View.GONE);
                    userManagementInfoStatusLine.setVisibility(View.VISIBLE);
                    userManagementInfoPauseLimitTxt.setVisibility(View.VISIBLE);
                    userManagementInfoPauseLimitContentTxt.setVisibility(View.VISIBLE);
                }
                else if (status.equals("직접 탈퇴") || status.equals("강제 탈퇴")) {
                    finish();
                }
            }

            @Override
            public void onDataLoadError(String errorMessage) {
                Log.d(TAG, errorMessage);
                Toast.makeText(getApplicationContext(), "오류 발생", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    public void receiveDateFromDialog(String date) {
        pauseLimitDateString = date;

        TimePickerDialog timePickerDialog = new TimePickerDialog(UserManagementInformationActivity.this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                pauseLimitTimeString = String.format(Locale.KOREA, "%02d%02d", selectedHour, selectedMinute);

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm", Locale.KOREA);

                Date time = null;
                try {
                    time = dateFormat.parse(pauseLimitDateString + pauseLimitTimeString);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }

                Timestamp pauseLimit = new Timestamp(time);

                fd.pauseUser(id, pauseLimit, new OnFirestoreDataLoadedListener() {
                    @Override
                    public void onDataLoaded(Object data) {
                        Toast.makeText(getApplicationContext(), "사용 정지 되었습니다", Toast.LENGTH_SHORT).show();
                        getStatus();
                    }

                    @Override
                    public void onDataLoadError(String errorMessage) {
                        Log.d(TAG, errorMessage);
                        Toast.makeText(getApplicationContext(), "오류 발생", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }, 0, 0, true);

        timePickerDialog.setOnCancelListener(dialog -> {
            pauseLimitDateString = null;
        });

        timePickerDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent); // 배경 투명하게 설정
        timePickerDialog.show();
    }
}
