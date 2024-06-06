package com.bucheon.yeoddadae;

import static com.google.android.exoplayer2.ExoPlayerLibraryInfo.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;

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

    Button statisticsBackBtn;
    Spinner statisticsTimeSpinner;
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
    }

    void init() {
        if (statisticsDatas != null) {
            statisticsRegisterCountContentTxt.setText(Long.toString(statisticsDatas.get("회원")));

            statisticsYdPointChargeContentTxt.setText(Long.toString(statisticsDatas.get("충전총액")) + "/" + Long.toString(statisticsDatas.get("충전수")));

            statisticsYdPointRefundContentTxt.setText(Long.toString(statisticsDatas.get("환급총액")) + "/" + Long.toString(statisticsDatas.get("환급수")));

            statisticsShareParkCountContentTxt.setText(Long.toString(statisticsDatas.get("승인공유주차장수")) + "/" + Long.toString(statisticsDatas.get("취소공유주차장수")) + "/" + Long.toString(statisticsDatas.get("총공유주차장수")));

            statisticsReservationCountContentTxt.setText(Long.toString(statisticsDatas.get("취소예약수")) + "/" + Long.toString(statisticsDatas.get("총예약수")));

            statisticsReportParkCountContentTxt.setText(Long.toString(statisticsDatas.get("승인제보주차장수")) + "/" + Long.toString(statisticsDatas.get("취소제보주차장수")) + "/" + Long.toString(statisticsDatas.get("총제보주차장수")));
        }
    }
}
