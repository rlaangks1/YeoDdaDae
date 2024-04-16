package com.bucheon.yeoddadae;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import java.util.HashMap;
import java.util.Map;

public class ReservationParkActivity extends AppCompatActivity {
    String loginId;
    String reservationFirestoreDocumentId;

    HashMap<String,  HashMap<String, ArrayList<String>>> anotherReservations;

    TimeAdapter ta;

    Button reservationBackBtn;
    TextView reservationParkNewAddressContentTxt;
    TextView reservationParkOldAddressContentTxt;
    TextView reservationParkDetailAddressContentTxt;
    TextView reservationSharerNameContentTxt;
    TextView reservationSharerPhoneContentTxt;
    TextView reservationSharerEmailContentTxt;
    TextView reservationSharerRelationContentTxt;
    TextView reservationPriceContentTxt;
    Button resetBtn;
    MaterialCalendarView reservationParkDateCalendar;
    ListView reservationParkTimeListView;
    Button reservationBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation_park);

        reservationBackBtn = findViewById(R.id.reservationBackBtn);
        reservationParkNewAddressContentTxt = findViewById(R.id.reservationParkNewAddressContentTxt);
        reservationParkOldAddressContentTxt = findViewById(R.id.reservationParkOldAddressContentTxt);
        reservationParkDetailAddressContentTxt = findViewById(R.id.reservationParkDetailAddressContentTxt);
        reservationSharerNameContentTxt = findViewById(R.id.reservationSharerNameContentTxt);
        reservationSharerPhoneContentTxt = findViewById(R.id.reservationSharerPhoneContentTxt);
        reservationSharerEmailContentTxt = findViewById(R.id.reservationSharerEmailContentTxt);
        reservationSharerRelationContentTxt = findViewById(R.id.reservationSharerRelationContentTxt);
        reservationPriceContentTxt = findViewById(R.id.reservationPriceContentTxt);
        resetBtn = findViewById(R.id.resetBtn);
        reservationParkDateCalendar = findViewById(R.id.reservationParkDateCalendar);
        reservationParkTimeListView = findViewById(R.id.reservationParkTimeListView);
        reservationBtn = findViewById(R.id.reservationBtn);

        Intent inIntent = getIntent();
        reservationFirestoreDocumentId = inIntent.getStringExtra("fireStoreDocumentId");
        loginId = inIntent.getStringExtra("loginId");
        if (loginId == null || loginId.equals("")) {
            Log.d(TAG, "loginId가 전달되지 않음 (오류)");
            finish();
        }

        ta = new TimeAdapter();
        reservationParkTimeListView.setAdapter(ta);

        reservationBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        FirestoreDatabase fd = new FirestoreDatabase();
        fd.loadShareParkInfo(reservationFirestoreDocumentId, new OnFirestoreDataLoadedListener() {
            @Override
            public void onDataLoaded(Object data) {
                HashMap<String, Object> hm = (HashMap<String, Object>) data;

                for (String key : hm.keySet()) {
                    Object value = hm.get(key);
                    Log.d(TAG, "Key: " + key + ", Value: " + value);

                    TMapData tMapData = new TMapData();
                    tMapData.reverseGeocoding((Double) hm.get("lat"), (Double) hm.get("lon"), "A10", new TMapData.reverseGeocodingListenerCallback() {
                        @Override
                        public void onReverseGeocoding(TMapAddressInfo tMapAddressInfo) {
                            if (tMapAddressInfo != null) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        String [] adrresses = tMapAddressInfo.strFullAddress.split(",");

                                        reservationParkNewAddressContentTxt.setText(adrresses[2]);
                                        reservationParkOldAddressContentTxt.setText(adrresses[1]);
                                    }
                                });
                            }
                        }
                    });

                    String originalPhoneString = (String) hm.get("ownerPhone");
                    String newPhoneString = "";
                    if (originalPhoneString != null) {
                        if (originalPhoneString.length() == 10) {
                            newPhoneString = originalPhoneString.substring(0, 3) + "-" + originalPhoneString.substring(3, 6) + "-" + originalPhoneString.substring(6);
                        }
                        else if (originalPhoneString.length() == 11) {
                            newPhoneString = originalPhoneString.substring(0, 3) + "-" + originalPhoneString.substring(3, 7) + "-" + originalPhoneString.substring(7);
                        }
                        else {
                            newPhoneString = originalPhoneString;
                        }
                    }
                    String finalNewPhoneString = newPhoneString;

                    DecimalFormat formatter = new DecimalFormat("#,###");
                    int number = ((Long) hm.get("price")).intValue();
                    String newPriceString = formatter.format(number);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            reservationParkDetailAddressContentTxt.setText((String) hm.get("parkDetailAddress"));
                            reservationSharerNameContentTxt.setText((String) hm.get("ownerName"));
                            reservationSharerPhoneContentTxt.setText(finalNewPhoneString);
                            reservationSharerEmailContentTxt.setText((String) hm.get("ownerEmail"));
                            reservationSharerRelationContentTxt.setText((String) hm.get("ownerParkingRelation"));
                            reservationPriceContentTxt.setText(newPriceString);
                        }
                    });
                }
            }

            @Override
            public void onDataLoadError(String errorMessage) {
                Log.d(TAG, errorMessage);
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        });

        fd.loadAnotherReservations(reservationFirestoreDocumentId, new OnFirestoreDataLoadedListener() {
            @Override
            public void onDataLoaded(Object data) {
                anotherReservations = (HashMap<String,  HashMap<String, ArrayList<String>>>) data;


                // anotherReservations의 내용을 로깅
                for (Map.Entry<String, HashMap<String, ArrayList<String>>> outerEntry : anotherReservations.entrySet()) {
                    String outerKey = outerEntry.getKey();
                    HashMap<String, ArrayList<String>> innerMap = outerEntry.getValue();

                    for (Map.Entry<String, ArrayList<String>> innerEntry : innerMap.entrySet()) {
                        String innerKey = innerEntry.getKey();
                        ArrayList<String> valuesList = innerEntry.getValue();

                        // ArrayList<String>의 각 값들을 하나의 문자열로 결합
                        StringBuilder valuesStringBuilder = new StringBuilder();
                        for (String value : valuesList) {
                            if (valuesStringBuilder.length() > 0) {
                                valuesStringBuilder.append(", ");
                            }
                            valuesStringBuilder.append(value);
                        }

                        // 로그 메세지 출력
                        Log.d("FirestoreData", "Outer Key: " + outerKey + ", Inner Key: " + innerKey + ", Values: " + valuesStringBuilder.toString());
                    }
                }
            }

            @Override
            public void onDataLoadError(String errorMessage) {

            }
        });

        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (CalendarDay element : ta.clear()) {
                    reservationParkDateCalendar.setDateSelected(element, false);
                }
                setTotalHeightofListView(reservationParkTimeListView, ta);
            }
        });

        reservationParkDateCalendar.setWeekDayFormatter(new ArrayWeekDayFormatter(getResources().getTextArray(R.array.custom_weekdays)));
        reservationParkDateCalendar.setTitleFormatter(new TitleFormatter() {
            @Override
            public CharSequence format(CalendarDay day) {
                // CalendarDay라는 클래스는 LocalDate 클래스를 기반으로 만들어진 클래스다
                // 때문에 MaterialCalendarView에서 연/월 보여주기를 커스텀하려면 CalendarDay 객체의 getDate()로 연/월을 구한 다음 LocalDate 객체에 넣어서
                // LocalDate로 변환하는 처리가 필요하다
                LocalDate inputText = day.getDate();
                String[] calendarHeaderElements = inputText.toString().split("-");
                StringBuilder calendarHeaderBuilder = new StringBuilder();
                calendarHeaderBuilder.append(calendarHeaderElements[0])
                        .append(" ")
                        .append(calendarHeaderElements[1]);
                return calendarHeaderBuilder.toString();
            }
        });

        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        CalendarDay currentCalendarDay = CalendarDay.from(currentYear, currentMonth + 1, currentDay);

        calendar.add(Calendar.DAY_OF_MONTH, 99);
        int futureYear = calendar.get(Calendar.YEAR);
        int futureMonth = calendar.get(Calendar.MONTH);
        int futureDay = calendar.get(Calendar.DAY_OF_MONTH);
        CalendarDay futureCalendarDay = CalendarDay.from(futureYear, futureMonth + 1, futureDay);

        reservationParkDateCalendar.state().edit()
                .setMinimumDate(currentCalendarDay)
                .setMaximumDate(futureCalendarDay)
                .commit();

        reservationParkDateCalendar.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                if (selected) {
                    Log.d(TAG, "선택한 날짜: " + date.getYear() + "년 " + date.getMonth() + "월 " + date.getDay() + "일");
                    ta.addItem(new TimeItem(date));
                    ta.sortByDate();
                    setTotalHeightofListView(reservationParkTimeListView, ta);
                }
                else {
                    Log.d(TAG, "선택 해제 한 날짜: " + date.getYear() + "년 " + date.getMonth() + "월 " + date.getDay() + "일");
                    ta.removeItem(ta.findItem(date));
                    setTotalHeightofListView(reservationParkTimeListView, ta);
                }
            }
        });

        reservationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, Object> hm = new HashMap<>();

                hm.put("shareParkDocumentName", reservationFirestoreDocumentId);
                hm.put("id", loginId);
                hm.put("time", ta.getAllTime());
                hm.put("isCancelled", false);
                hm.put("upTime", FieldValue.serverTimestamp());

                FirestoreDatabase fd = new FirestoreDatabase();
                fd.insertData("reservation", hm);
            }
        });
    }

    public void setTotalHeightofListView(ListView listView, TimeAdapter ta) {
        int totalHeight = 0;
        for (int i = 0; i < ta.getCount(); i++) {
            View mView = ta.getView(i, null, listView);
            mView.measure( View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            totalHeight += mView.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = (totalHeight + (listView.getDividerHeight() * (ta.getCount() - 1)))/2;
        listView.setLayoutParams(params); listView.requestLayout();
    }
}
