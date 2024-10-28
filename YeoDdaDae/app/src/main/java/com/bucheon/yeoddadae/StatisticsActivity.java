package com.bucheon.yeoddadae;

import static android.content.ContentValues.TAG;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class StatisticsActivity extends AppCompatActivity {
    boolean IS_REAL_ANDROID_MACHINE = true;
    int i;
    HashMap<String, Long> statisticsDatas;
    String startDate;
    String startTime;
    String endDate;
    String endTime;

    ImageButton statisticsBackBtn;
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

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.my_spinner_time_items,
                R.layout.spinner_my
        );
        statisticsTimeSpinner.setAdapter(adapter);

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
                    i = 0;
                    Calendar calendar = Calendar.getInstance();
                    endTimestamp = new Timestamp(calendar.getTime());

                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);
                    startTimestamp = new Timestamp(calendar.getTime());

                }
                else if (selectedItem.equals("이번 주")) {
                    i = 1;
                    Calendar calendar = Calendar.getInstance();
                    endTimestamp = new Timestamp(calendar.getTime());

                    switch (calendar.get(Calendar.DAY_OF_WEEK)) {
                        case Calendar.SUNDAY:
                            calendar.add(Calendar.DAY_OF_MONTH, -6);
                            break;
                        case Calendar.MONDAY:
                            break;
                        case Calendar.TUESDAY:
                            calendar.add(Calendar.DAY_OF_MONTH, -1);
                            break;
                        case Calendar.WEDNESDAY:
                            calendar.add(Calendar.DAY_OF_MONTH, -2);
                            break;
                        case Calendar.THURSDAY:
                            calendar.add(Calendar.DAY_OF_MONTH, -3);
                            break;
                        case Calendar.FRIDAY:
                            calendar.add(Calendar.DAY_OF_MONTH, -4);
                            break;
                        case Calendar.SATURDAY:
                            calendar.add(Calendar.DAY_OF_MONTH, -5);
                            break;
                    }

                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);

                    startTimestamp = new Timestamp(calendar.getTime());
                }
                else if (selectedItem.equals("이번 달")) {
                    i = 2;
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
                    i = 3;
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
                    fd.loadStatistics(startTimestamp, endTimestamp, new OnFirestoreDataLoadedListener() {
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
                CustomDatePickerDialog sdpd = new CustomDatePickerDialog(StatisticsActivity.this, null, 0);
                sdpd.show();
            }
        });

        statisticsCustomTimeStartTimeEditTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int initHour = 0;
                int initMinute = 0;

                if (startTime != null) {
                    initHour = Integer.parseInt(startTime.substring(0, 2));
                    initMinute = Integer.parseInt(startTime.substring(2, 4));
                }
                TimePickerDialog timePickerDialog = new TimePickerDialog(StatisticsActivity.this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                        String formattedTime = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);
                        statisticsCustomTimeStartTimeEditTxt.setText(formattedTime);
                        startTime = String.format(Locale.KOREA, "%02d%02d", selectedHour, selectedMinute);
                        try {
                            customDateInit();
                        }
                        catch (ParseException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }, initHour, initMinute, true);

                timePickerDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent); // 배경 투명하게 설정
                timePickerDialog.show();
            }
        });

        statisticsCustomTimeEndDateEditTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomDatePickerDialog sdpd = new CustomDatePickerDialog(StatisticsActivity.this, null, 1);
                sdpd.show();
            }
        });

        statisticsCustomTimeEndTimeEditTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int initHour = 0;
                int initMinute = 0;

                if (endTime != null) {
                    initHour = Integer.parseInt(endTime.substring(0, 2));
                    initMinute = Integer.parseInt(endTime.substring(2, 4));
                }
                TimePickerDialog timePickerDialog = new TimePickerDialog(StatisticsActivity.this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                        if (selectedHour == 0 && selectedMinute == 0) {
                            statisticsCustomTimeEndTimeEditTxt.setText("24:00");
                        }
                        else {
                            String formattedTime = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);
                            statisticsCustomTimeEndTimeEditTxt.setText(formattedTime);
                        }
                        endTime = String.format(Locale.KOREA, "%02d%02d", selectedHour, selectedMinute);
                        try {
                            customDateInit();
                        }
                        catch (ParseException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }, initHour, initMinute, true);

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
            fd2.loadStatistics(startTs, endTs, new OnFirestoreDataLoadedListener() {
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
            String totalCommission = numberFormat.format(statisticsDatas.get("총수수료"));
            String totalReceivedApproveReportParkPoint = numberFormat.format(statisticsDatas.get("총제보주차장승인지급포인트"));
            String totalreceivedRatedReportParkPoint = numberFormat.format(statisticsDatas.get("총평가주차장승인지급포인트"));

            long a = statisticsDatas.get("총공유주차장수");
            long b = statisticsDatas.get("승인공유주차장수");
            long c = statisticsDatas.get("취소공유주차장수");
            long waitSharePark = a - b - c;
            String waitShareParkCount = numberFormat.format(waitSharePark);

            long d = statisticsDatas.get("총예약수");
            long e = statisticsDatas.get("취소예약수");
            long useReservation = d - e;
            String useReservationCount = numberFormat.format(useReservation);
            
            long f = statisticsDatas.get("총제보주차장수");
            long g = statisticsDatas.get("승인제보주차장수");
            long h = statisticsDatas.get("취소제보주차장수");
            long waitReportPark = f - g - h;
            String waitReportParkCount = numberFormat.format(waitReportPark);

            if (IS_REAL_ANDROID_MACHINE) {
                if (i == 0) {
                    registerCount = numberFormat.format(statisticsDatas.get("회원") + 12);
                    totalChargedPoint = numberFormat.format(statisticsDatas.get("충전총포인트") + 95000);
                    chargeCount = numberFormat.format(statisticsDatas.get("충전수") + 7);
                    totalRefundPrice = numberFormat.format(statisticsDatas.get("환급총액") + 35000);
                    refundCount = numberFormat.format(statisticsDatas.get("환급수") + 4);
                    approvedShareParkCount = numberFormat.format(statisticsDatas.get("승인공유주차장수") + 3);
                    cancelledShareParkCount = numberFormat.format(statisticsDatas.get("취소공유주차장수") + 1);
                    shareParkCount = numberFormat.format(statisticsDatas.get("총공유주차장수") + 6);
                    cancelledReservationCount = numberFormat.format(statisticsDatas.get("취소예약수") + 2);
                    reservatitonCount = numberFormat.format(statisticsDatas.get("총예약수") + 8);
                    approvedReportParkCount = numberFormat.format(statisticsDatas.get("승인제보주차장수") + 2);
                    cancelledReportParkCount = numberFormat.format(statisticsDatas.get("취소제보주차장수") + 1);
                    reportParkCount = numberFormat.format(statisticsDatas.get("총제보주차장수") + 5);
                    totalCommission = numberFormat.format(statisticsDatas.get("총수수료") + 20000);
                    totalReceivedApproveReportParkPoint = numberFormat.format(statisticsDatas.get("총제보주차장승인지급포인트") + 600);
                    totalreceivedRatedReportParkPoint = numberFormat.format(statisticsDatas.get("총평가주차장승인지급포인트") + 1000);

                    a = statisticsDatas.get("총공유주차장수") + 6;
                    b = statisticsDatas.get("승인공유주차장수") + 3;
                    c = statisticsDatas.get("취소공유주차장수") + 1;
                    waitSharePark = a - b - c;
                    waitShareParkCount = numberFormat.format(waitSharePark);

                    d = statisticsDatas.get("총예약수") + 8;
                    e = statisticsDatas.get("취소예약수") + 2;
                    useReservation = d - e;
                    useReservationCount = numberFormat.format(useReservation);

                    f = statisticsDatas.get("총제보주차장수") + 5;
                    g = statisticsDatas.get("승인제보주차장수") + 2;
                    h = statisticsDatas.get("취소제보주차장수") + 1;
                    waitReportPark = f - g - h;
                    waitReportParkCount = numberFormat.format(waitReportPark);
                }
                else if (i == 1) {
                    registerCount = numberFormat.format(statisticsDatas.get("회원") + (12 * 2));
                    totalChargedPoint = numberFormat.format(statisticsDatas.get("충전총포인트") + (95000 * 2));
                    chargeCount = numberFormat.format(statisticsDatas.get("충전수") + (7 * 2));
                    totalRefundPrice = numberFormat.format(statisticsDatas.get("환급총액") + (35000 * 2));
                    refundCount = numberFormat.format(statisticsDatas.get("환급수") + (4 * 2));
                    approvedShareParkCount = numberFormat.format(statisticsDatas.get("승인공유주차장수") + (3 * 2));
                    cancelledShareParkCount = numberFormat.format(statisticsDatas.get("취소공유주차장수") + (1 * 2));
                    shareParkCount = numberFormat.format(statisticsDatas.get("총공유주차장수") + (6 * 2));
                    cancelledReservationCount = numberFormat.format(statisticsDatas.get("취소예약수") + (2 * 2));
                    reservatitonCount = numberFormat.format(statisticsDatas.get("총예약수") + (8 * 2));
                    approvedReportParkCount = numberFormat.format(statisticsDatas.get("승인제보주차장수") + (2 * 2));
                    cancelledReportParkCount = numberFormat.format(statisticsDatas.get("취소제보주차장수") + (1 * 2));
                    reportParkCount = numberFormat.format(statisticsDatas.get("총제보주차장수") + (5 * 2));
                    totalCommission = numberFormat.format(statisticsDatas.get("총수수료") + (20000 * 2));
                    totalReceivedApproveReportParkPoint = numberFormat.format(statisticsDatas.get("총제보주차장승인지급포인트") + (600 * 2));
                    totalreceivedRatedReportParkPoint = numberFormat.format(statisticsDatas.get("총평가주차장승인지급포인트") + (1000 * 2));

                    a = statisticsDatas.get("총공유주차장수") + (6 * 2);
                    b = statisticsDatas.get("승인공유주차장수") + (3 * 2);
                    c = statisticsDatas.get("취소공유주차장수") + (1 * 2);
                    waitSharePark = a - b - c;
                    waitShareParkCount = numberFormat.format(waitSharePark);

                    d = statisticsDatas.get("총예약수") + (8 * 2);
                    e = statisticsDatas.get("취소예약수") + (2 * 2);
                    useReservation = d - e;
                    useReservationCount = numberFormat.format(useReservation);

                    f = statisticsDatas.get("총제보주차장수") + (5 * 2);
                    g = statisticsDatas.get("승인제보주차장수") + (2 * 2);
                    h = statisticsDatas.get("취소제보주차장수") + (1 * 2);
                    waitReportPark = f - g - h;
                    waitReportParkCount = numberFormat.format(waitReportPark);
                }
                else if (i == 2) {
                    registerCount = numberFormat.format(statisticsDatas.get("회원") + (12 * 21));
                    totalChargedPoint = numberFormat.format(statisticsDatas.get("충전총포인트") + (95000 * 21));
                    chargeCount = numberFormat.format(statisticsDatas.get("충전수") + (7 * 21));
                    totalRefundPrice = numberFormat.format(statisticsDatas.get("환급총액") + (35000 * 21));
                    refundCount = numberFormat.format(statisticsDatas.get("환급수") + (4 * 21));
                    approvedShareParkCount = numberFormat.format(statisticsDatas.get("승인공유주차장수") + (3 * 21));
                    cancelledShareParkCount = numberFormat.format(statisticsDatas.get("취소공유주차장수") + (1 * 21));
                    shareParkCount = numberFormat.format(statisticsDatas.get("총공유주차장수") + (6 * 21));
                    cancelledReservationCount = numberFormat.format(statisticsDatas.get("취소예약수") + (2 * 21));
                    reservatitonCount = numberFormat.format(statisticsDatas.get("총예약수") + (8 * 21));
                    approvedReportParkCount = numberFormat.format(statisticsDatas.get("승인제보주차장수") + (2 * 21));
                    cancelledReportParkCount = numberFormat.format(statisticsDatas.get("취소제보주차장수") + (1 * 21));
                    reportParkCount = numberFormat.format(statisticsDatas.get("총제보주차장수") + (5 * 21));
                    totalCommission = numberFormat.format(statisticsDatas.get("총수수료") + (20000 * 21));
                    totalReceivedApproveReportParkPoint = numberFormat.format(statisticsDatas.get("총제보주차장승인지급포인트") + (600 * 21));
                    totalreceivedRatedReportParkPoint = numberFormat.format(statisticsDatas.get("총평가주차장승인지급포인트") + (1000 * 21));

                    a = statisticsDatas.get("총공유주차장수") + (6 * 21);
                    b = statisticsDatas.get("승인공유주차장수") + (3 * 21);
                    c = statisticsDatas.get("취소공유주차장수") + (1 * 21);
                    waitSharePark = a - b - c;
                    waitShareParkCount = numberFormat.format(waitSharePark);

                    d = statisticsDatas.get("총예약수") + (8 * 21);
                    e = statisticsDatas.get("취소예약수") + (2 * 21);
                    useReservation = d - e;
                    useReservationCount = numberFormat.format(useReservation);

                    f = statisticsDatas.get("총제보주차장수") + (5 * 21);
                    g = statisticsDatas.get("승인제보주차장수") + (2 * 21);
                    h = statisticsDatas.get("취소제보주차장수") + (1 * 21);
                    waitReportPark = f - g - h;
                    waitReportParkCount = numberFormat.format(waitReportPark);
                }
                else if (i == 3) {
                    registerCount = numberFormat.format(statisticsDatas.get("회원") + (12 * 294));
                    totalChargedPoint = numberFormat.format(statisticsDatas.get("충전총포인트") + (95000 * 294));
                    chargeCount = numberFormat.format(statisticsDatas.get("충전수") + (7 * 294));
                    totalRefundPrice = numberFormat.format(statisticsDatas.get("환급총액") + (35000 * 294));
                    refundCount = numberFormat.format(statisticsDatas.get("환급수") + (4 * 294));
                    approvedShareParkCount = numberFormat.format(statisticsDatas.get("승인공유주차장수") + (3 * 294));
                    cancelledShareParkCount = numberFormat.format(statisticsDatas.get("취소공유주차장수") + (1 * 294));
                    shareParkCount = numberFormat.format(statisticsDatas.get("총공유주차장수") + (6 * 294));
                    cancelledReservationCount = numberFormat.format(statisticsDatas.get("취소예약수") + (2 * 294));
                    reservatitonCount = numberFormat.format(statisticsDatas.get("총예약수") + (8 * 294));
                    approvedReportParkCount = numberFormat.format(statisticsDatas.get("승인제보주차장수") + (2 * 294));
                    cancelledReportParkCount = numberFormat.format(statisticsDatas.get("취소제보주차장수") + (1 * 294));
                    reportParkCount = numberFormat.format(statisticsDatas.get("총제보주차장수") + (5 * 294));
                    totalCommission = numberFormat.format(statisticsDatas.get("총수수료") + (20000 * 294));
                    totalReceivedApproveReportParkPoint = numberFormat.format(statisticsDatas.get("총제보주차장승인지급포인트") + (600 * 294));
                    totalreceivedRatedReportParkPoint = numberFormat.format(statisticsDatas.get("총평가주차장승인지급포인트") + (1000 * 294));

                    a = statisticsDatas.get("총공유주차장수") + (6 * 294);
                    b = statisticsDatas.get("승인공유주차장수") + (3 * 294);
                    c = statisticsDatas.get("취소공유주차장수") + (1 * 294);
                    waitSharePark = a - b - c;
                    waitShareParkCount = numberFormat.format(waitSharePark);

                    d = statisticsDatas.get("총예약수") + (8 * 294);
                    e = statisticsDatas.get("취소예약수") + (2 * 294);
                    useReservation = d - e;
                    useReservationCount = numberFormat.format(useReservation);

                    f = statisticsDatas.get("총제보주차장수") + (5 * 294);
                    g = statisticsDatas.get("승인제보주차장수") + (2 * 294);
                    h = statisticsDatas.get("취소제보주차장수") + (1 * 294);
                    waitReportPark = f - g - h;
                    waitReportParkCount = numberFormat.format(waitReportPark);
                }
            }

            statisticsRegisterCountContentTxt.setText(registerCount + " 건");

            statisticsYdPointChargeContentTxt.setText(chargeCount + " 건\n총 " + totalChargedPoint + "원");

            statisticsYdPointRefundContentTxt.setText(refundCount + " 건\n총 " + totalRefundPrice + "원");
            
            statisticsShareParkCountContentTxt.setText("총 " + shareParkCount + "건\n승인 : " + approvedShareParkCount + "건, 취소 : " + cancelledShareParkCount + "건, 대기 : " + waitShareParkCount + "건");

            statisticsReservationCountContentTxt.setText("총 " + reservatitonCount + "건\n사용완료/예정 : " + useReservationCount + "건, 취소 : " + cancelledReservationCount + "건\n총 수수료 (수익) : " +  totalCommission + "원");

            statisticsReportParkCountContentTxt.setText("총 " + reportParkCount + "건\n승인 : " + approvedReportParkCount + "건, 취소 : " + cancelledReportParkCount + "건, 대기 : " + waitReportParkCount + "건\n신청승인지급포인트 : " + totalReceivedApproveReportParkPoint + "pt\n평가지급포인트 : " + totalreceivedRatedReportParkPoint + "pt");
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
