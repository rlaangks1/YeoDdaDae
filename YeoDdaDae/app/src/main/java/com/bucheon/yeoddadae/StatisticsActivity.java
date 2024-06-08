package com.bucheon.yeoddadae;

import static com.google.android.exoplayer2.ExoPlayerLibraryInfo.TAG;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.firebase.Timestamp;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class StatisticsActivity extends AppCompatActivity {
    HashMap<String, Long> statisticsDatas;
    String startDate;
    String startTime;
    String endDate;
    String endTime;

    Button statisticsBackBtn;
    Spinner statisticsTimeSpinner;
    ConstraintLayout statisticsCustomTimeConstLayout;
    EditText statisticsCustomTimeStartDateEditTxt;
    EditText statisticsCustomTimeStartTimeEditTxt;
    EditText statisticsCustomTimeEndDateEditTxt;
    EditText statisticsCustomTimeEndTimeEditTxt;
    TextView statisticsRegisterCountContentTxt;
    TextView statisticsYdPointChargeContentTxt;
    TextView statisticsYdPointRefundContentTxt;
    TextView statisticsShareParkCountContentTxt;
    TextView statisticsReservationCountContentTxt;
    TextView statisticsReportParkCountContentTxt;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        statisticsBackBtn = findViewById(R.id.statisticsBackBtn);
        statisticsTimeSpinner = findViewById(R.id.statisticsTimeSpinner);
        statisticsCustomTimeConstLayout = findViewById(R.id.statisticsCustomTimeConstLayout);
        statisticsCustomTimeStartDateEditTxt = findViewById(R.id.statisticsCustomTimeStartDateEditTxt);
        statisticsCustomTimeStartTimeEditTxt = findViewById(R.id.statisticsCustomTimeStartTimeEditTxt);
        statisticsCustomTimeEndDateEditTxt = findViewById(R.id.statisticsCustomTimeEndDateEditTxt);
        statisticsCustomTimeEndTimeEditTxt = findViewById(R.id.statisticsCustomTimeEndTimeEditTxt);
        statisticsRegisterCountContentTxt = findViewById(R.id.statisticsRegisterCountContentTxt);
        statisticsYdPointChargeContentTxt = findViewById(R.id.statisticsYdPointChargeContentTxt);
        statisticsYdPointRefundContentTxt = findViewById(R.id.statisticsYdPointRefundContentTxt);
        statisticsShareParkCountContentTxt = findViewById(R.id.statisticsShareParkCountContentTxt);
        statisticsReservationCountContentTxt = findViewById(R.id.statisticsReservationCountContentTxt);
        statisticsReportParkCountContentTxt = findViewById(R.id.statisticsReportParkCountContentTxt);

        statisticsBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        statisticsTimeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                statisticsCustomTimeConstLayout.setVisibility(View.GONE);

                startDate = null;
                startTime = null;
                endDate = null;
                endTime = null;

                statisticsCustomTimeStartDateEditTxt.setText("");
                statisticsCustomTimeStartTimeEditTxt.setText("");
                statisticsCustomTimeEndDateEditTxt.setText("");
                statisticsCustomTimeEndTimeEditTxt.setText("");

                Timestamp startTimestamp = null;
                Timestamp endTimestamp = null;

                FirestoreDatabase fd = new FirestoreDatabase();

                if (selectedItem.equals("오늘")) {
                    Calendar calendar = Calendar.getInstance();
                    endTimestamp = new Timestamp(calendar.getTime());

                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);
                    startTimestamp = new Timestamp(calendar.getTime());

                }
                else if (selectedItem.equals("이번 주")) {
                    Calendar calendar = Calendar.getInstance();
                    endTimestamp = new Timestamp(calendar.getTime());

                    calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);
                    startTimestamp = new Timestamp(calendar.getTime());
                }
                else if (selectedItem.equals("이번 달")) {
                    Calendar calendar = Calendar.getInstance();
                    endTimestamp = new Timestamp(calendar.getTime());

                    calendar.set(Calendar.DAY_OF_MONTH, 1);
                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);
                    startTimestamp = new Timestamp(calendar.getTime());
                }
                else if (selectedItem.equals("올해")) {
                    Calendar calendar = Calendar.getInstance();
                    endTimestamp = new Timestamp(calendar.getTime());

                    calendar.set(Calendar.MONTH, Calendar.JANUARY);
                    calendar.set(Calendar.DAY_OF_MONTH, 1);
                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);
                    startTimestamp = new Timestamp(calendar.getTime());

                }
                else if (selectedItem.equals("직접 설정")) {
                    statisticsCustomTimeConstLayout.setVisibility(View.VISIBLE);

                    statisticsRegisterCountContentTxt.setText("");
                    statisticsYdPointChargeContentTxt.setText("");
                    statisticsYdPointRefundContentTxt.setText("");
                    statisticsShareParkCountContentTxt.setText("");
                    statisticsReservationCountContentTxt.setText("");
                    statisticsReportParkCountContentTxt.setText("");
                }

                if (startTimestamp != null) {
                    Date date = startTimestamp.toDate();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일 HH:mm:ss", Locale.KOREA);
                    String dateString = sdf.format(date);
                    Log.d(TAG, "시작시각 : " + dateString);
                }
                if (endTimestamp != null) {
                    Date date = endTimestamp.toDate();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일 HH:mm:ss", Locale.KOREA);
                    String dateString = sdf.format(date);
                    Log.d(TAG, "끝시각 : " + dateString);
                }

                if (startTimestamp != null && endTimestamp != null) {
                    fd.getStatistics(startTimestamp, endTimestamp, new OnFirestoreDataLoadedListener() {
                        @Override
                        public void onDataLoaded(Object data) {
                            statisticsDatas = (HashMap<String, Long>) data;
                            init();
                        }

                        @Override
                        public void onDataLoadError(String errorMessage) {
                            Toast.makeText(getApplicationContext(), "오류 발생", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        statisticsCustomTimeStartDateEditTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomDatePickerDialog sdpd = new CustomDatePickerDialog(StatisticsActivity.this, 0);
                sdpd.show();
            }
        });

        statisticsCustomTimeStartTimeEditTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(StatisticsActivity.this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                        String timeFormat;
                        int amPmHour;
                        if (selectedHour >= 12) {
                            timeFormat = "오후";
                            if (selectedHour > 12) {
                                amPmHour = selectedHour - 12;
                            } else {
                                amPmHour = selectedHour;
                            }
                        } else {
                            timeFormat = "오전";
                            if (selectedHour == 0) {
                                amPmHour = 12;
                            } else {
                                amPmHour = selectedHour;
                            }
                        }
                        String formattedTime = String.format(Locale.getDefault(), "%s %02d:%02d", timeFormat, amPmHour, selectedMinute);
                        statisticsCustomTimeStartTimeEditTxt.setText(formattedTime);
                        startTime = String.format(Locale.getDefault(), "%02d%02d", selectedHour, selectedMinute);
                        try {
                            customDateInit();
                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }, 0, 0, false); // is24HourView를 false로 설정

                timePickerDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent); // 배경 투명하게 설정
                timePickerDialog.show();
            }
        });

        statisticsCustomTimeEndDateEditTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomDatePickerDialog sdpd = new CustomDatePickerDialog(StatisticsActivity.this, 1);
                sdpd.show();
            }
        });

        statisticsCustomTimeEndTimeEditTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(StatisticsActivity.this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                        String timeFormat;
                        int amPmHour;
                        if (selectedHour >= 12) {
                            timeFormat = "오후";
                            if (selectedHour > 12) {
                                amPmHour = selectedHour - 12;
                            } else {
                                amPmHour = selectedHour;
                            }
                        } else {
                            timeFormat = "오전";
                            if (selectedHour == 0) {
                                amPmHour = 12;
                            } else {
                                amPmHour = selectedHour;
                            }
                        }
                        String formattedTime = String.format(Locale.getDefault(), "%s %02d:%02d", timeFormat, amPmHour, selectedMinute);
                        statisticsCustomTimeEndTimeEditTxt.setText(formattedTime);
                        endTime = String.format(Locale.getDefault(), "%02d%02d", selectedHour, selectedMinute);
                        try {
                            customDateInit();
                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }, 0, 0, false); // is24HourView를 false로 설정

                timePickerDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent); // 배경 투명하게 설정
                timePickerDialog.show();
            }
        });
    }

    void customDateInit() throws ParseException {
        if (startDate != null && startTime != null && endDate != null && endTime != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm", Locale.KOREA);

            Date startDateTime = dateFormat.parse(startDate + startTime);
            Timestamp startTs = new Timestamp(startDateTime);

            Date endDateTime = dateFormat.parse(endDate + endTime);
            Timestamp endTs = new Timestamp(endDateTime);

            FirestoreDatabase fd2 = new FirestoreDatabase();
            fd2.getStatistics(startTs, endTs, new OnFirestoreDataLoadedListener() {
                @Override
                public void onDataLoaded(Object data) {
                    statisticsDatas = (HashMap<String, Long>) data;
                    init();
                }

                @Override
                public void onDataLoadError(String errorMessage) {
                    Toast.makeText(getApplicationContext(), "오류 발생", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    void init() {
        if (statisticsDatas != null) {
            NumberFormat numberFormat = NumberFormat.getInstance();
            String registerCount = numberFormat.format(statisticsDatas.get("회원"));
            String totalChargedPoint = numberFormat.format(statisticsDatas.get("충전총포인트"));
            String totalChargedPrice = numberFormat.format(statisticsDatas.get("충전총액"));
            String totalChargedRevenue = numberFormat.format(statisticsDatas.get("충전총수익"));
            String chargeCount = numberFormat.format(statisticsDatas.get("충전수"));
            String totalRefundPrice = numberFormat.format(statisticsDatas.get("환급총액"));
            String refundCount = numberFormat.format(statisticsDatas.get("환급수"));
            String approvedShareParkCount = numberFormat.format(statisticsDatas.get("승인공유주차장수"));
            String cancelledShareParkCount = numberFormat.format(statisticsDatas.get("취소공유주차장수"));
            String shareParkCount = numberFormat.format(statisticsDatas.get("총공유주차장수"));
            String cancelledReservationCount = numberFormat.format(statisticsDatas.get("취소예약수"));
            String reservatitonCount = numberFormat.format(statisticsDatas.get("총예약수"));
            String approvedReportParkCount = numberFormat.format(statisticsDatas.get("승인제보주차장수"));
            String cancelledReportParkCount = numberFormat.format(statisticsDatas.get("취소제보주차장수"));
            String reportParkCount = numberFormat.format(statisticsDatas.get("총제보주차장수"));


            statisticsRegisterCountContentTxt.setText(registerCount + "건");

            statisticsYdPointChargeContentTxt.setText(totalChargedPoint + "pt / " + totalChargedPrice + "원 / " + totalChargedRevenue + "원 / " + chargeCount + "건");

            statisticsYdPointRefundContentTxt.setText(totalRefundPrice + "원 / " + refundCount + "건");

            statisticsShareParkCountContentTxt.setText(approvedShareParkCount + "개 / " + cancelledShareParkCount + "개 / " + shareParkCount + "개");

            statisticsReservationCountContentTxt.setText(cancelledReservationCount + "건 / " + reservatitonCount + "건");

            statisticsReportParkCountContentTxt.setText(approvedReportParkCount + "개 / " + cancelledReportParkCount + "개 / " + reportParkCount + "개");
        }
    }

    void receiveStartDateFromDialog (String s) throws ParseException {
        this.startDate = s;
        statisticsCustomTimeStartDateEditTxt.setText(s.substring(0, 4) + "년 " + s.substring(4, 6) + "월 " + s.substring(6) + "일");
        customDateInit();
    }

    void receiveEndDateFromDialog (String s) throws ParseException {
        this.endDate = s;
        statisticsCustomTimeEndDateEditTxt.setText(s.substring(0, 4) + "년 " + s.substring(4, 6) + "월 " + s.substring(6) + "일");
        customDateInit();
    }
}
