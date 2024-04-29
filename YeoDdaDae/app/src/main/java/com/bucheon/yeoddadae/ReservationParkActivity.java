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
import android.widget.Toast;

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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ReservationParkActivity extends AppCompatActivity {
    String loginId;
    String reservationFirestoreDocumentId;

    int pricePerHour = -1;

    HashMap<String, ArrayList<String>> shareTime;
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
    TextView reservationWonTxt;
    TextView reservationShareTimeContentTxt;
    TextView reservationedTimeContentTxt;
    Button resetBtn;
    MaterialCalendarView reservationParkDateCalendar;
    ListView reservationParkTimeListView;
    TextView reservationTotalPriceContentTxt;
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
        reservationWonTxt = findViewById(R.id.reservationWonTxt);
        reservationShareTimeContentTxt = findViewById(R.id.reservationShareTimeContentTxt);
        reservationedTimeContentTxt = findViewById(R.id.reservationedTimeContentTxt);
        resetBtn = findViewById(R.id.resetBtn);
        reservationParkDateCalendar = findViewById(R.id.reservationParkDateCalendar);
        reservationParkTimeListView = findViewById(R.id.reservationParkTimeListView);
        reservationTotalPriceContentTxt = findViewById(R.id.reservationTotalPriceContentTxt);
        reservationBtn = findViewById(R.id.reservationBtn);

        Intent inIntent = getIntent();
        reservationFirestoreDocumentId = inIntent.getStringExtra("fireStoreDocumentId");
        loginId = inIntent.getStringExtra("loginId");
        if (loginId == null || loginId.equals("")) {
            Log.d(TAG, "loginId가 전달되지 않음 (오류)");
            finish();
        }

        ta = new TimeAdapter(this);
        reservationParkTimeListView.setAdapter(ta);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                reservationTotalPriceContentTxt.setText("무료");
                reservationWonTxt.setVisibility(View.GONE);
            }
        });

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
                pricePerHour = number;
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

                shareTime = (HashMap<String, ArrayList<String>>) hm.get("time");

                List<String> keys = new ArrayList<>(shareTime.keySet());
                Collections.sort(keys);

                StringBuilder textBuilder = new StringBuilder();
                for (String key : keys) {
                    ArrayList<String> values = shareTime.get(key);

                    int year = Integer.parseInt(key.substring(0, 4));
                    int month = Integer.parseInt(key.substring(4, 6));
                    int day = Integer.parseInt(key.substring(6));

                    String startTimeString = values.get(0).substring(0,2) + ":" + values.get(0).substring(2);
                    String endTimeString = values.get(1).substring(0,2) + ":" + values.get(1).substring(2);

                    textBuilder.append(year + "년 " + month + "월 " + day + "일 " + startTimeString + "부터 " + endTimeString + "까지\n");
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        reservationShareTimeContentTxt.setText(textBuilder.toString());
                    }
                });


                HashSet<CalendarDay> enabledDays = new HashSet<>();
                for (String key : shareTime.keySet()) {
                    int year = Integer.parseInt(key.substring(0, 4));
                    int month = Integer.parseInt(key.substring(4, 6));
                    int day = Integer.parseInt(key.substring(6));

                    CalendarDay calendarDay = CalendarDay.from(year, month, day);
                    enabledDays.add(calendarDay);
                }

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

                // 모든 날짜를 비활성화하는 Decorator 추가
                reservationParkDateCalendar.addDecorator(new DisableAllDaysDecorator());
                reservationParkDateCalendar.invalidateDecorators();

                // 선택 가능한 날짜들만 활성화하는 Decorator 추가
                EnableSpecificDaysDecorator enableDecorator = new EnableSpecificDaysDecorator(enabledDays);
                reservationParkDateCalendar.addDecorator(enableDecorator);
                reservationParkDateCalendar.invalidateDecorators();
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

                ArrayList<String> al = new ArrayList<>();

                for (Map.Entry<String, HashMap<String, ArrayList<String>>> entry : anotherReservations.entrySet()) {
                    HashMap<String, ArrayList<String>> shareTime = entry.getValue();

                    List<String> keys = new ArrayList<>(shareTime.keySet());
                    Collections.sort(keys);

                    for (String key : keys) {
                        ArrayList<String> values = shareTime.get(key);

                        int year = Integer.parseInt(key.substring(0, 4));
                        int month = Integer.parseInt(key.substring(4, 6));
                        int day = Integer.parseInt(key.substring(6));

                        String startTimeString = values.get(0).substring(0, 2) + ":" + values.get(0).substring(2);
                        String endTimeString = values.get(1).substring(0, 2) + ":" + values.get(1).substring(2);

                        al.add(year + "년 " + month + "월 " + day + "일 " + startTimeString + "부터 " + endTimeString + "까지\n");
                    }
                }
                Collections.sort(al);

                String tempString = "";
                for (String s : al) {
                    tempString += s;

                }
                final String reservationsText = tempString;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (reservationsText.equals("")) {
                            reservationedTimeContentTxt.setText("없음");
                        }
                        else {
                            reservationedTimeContentTxt.setText(reservationsText);
                        }
                    }
                });
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
                setTotalHeightofListView(reservationParkTimeListView);
                calculatePrice();
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

        reservationParkDateCalendar.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                if (selected) {
                    Log.d(TAG, "선택한 날짜: " + date.getYear() + "년 " + date.getMonth() + "월 " + date.getDay() + "일");
                    ta.addItem(new TimeItem(date));
                    ta.sortByDate();
                    setTotalHeightofListView(reservationParkTimeListView);
                    calculatePrice();
                }
                else {
                    Log.d(TAG, "선택 해제 한 날짜: " + date.getYear() + "년 " + date.getMonth() + "월 " + date.getDay() + "일");
                    ta.removeItem(ta.findItem(date));
                    setTotalHeightofListView(reservationParkTimeListView);
                    calculatePrice();
                }
            }
        });

        reservationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isInReservationTime = true;
                boolean isNotInAnotherReservationTime = true;

                HashMap<String, ArrayList<String>> targetTimes = ta.getAllTime();
                Set<String> keys = targetTimes.keySet();

                for (String key : keys) {
                    ArrayList<String> tTimes = targetTimes.get(key);
                    ArrayList<String> cTimes = shareTime.get(key);

                    if (cTimes == null || !isInBetweenTime(tTimes.get(0), tTimes.get(1), cTimes.get(0), cTimes.get(1))) {
                        isInReservationTime = false;
                        break;
                    }
                }

                Loop1 :
                for (String key : keys) {
                    ArrayList<String> tTimes = targetTimes.get(key);

                    Set<String> anotherKeys = anotherReservations.keySet();
                    for (String anotherKey : anotherKeys) {
                        HashMap<String, ArrayList<String>> temp = anotherReservations.get(anotherKey);
                        ArrayList<String> aTimes = temp.get(key);

                        if (aTimes != null && !isNotOverlapping(tTimes.get(0), tTimes.get(1), aTimes.get(0), aTimes.get(1))) {
                            isNotInAnotherReservationTime = false;
                            break Loop1;
                        }
                    }
                }

                if (!isInReservationTime) {
                    Toast.makeText(getApplicationContext(), "공유 시간 범위 내에 있지 않습니다", Toast.LENGTH_SHORT).show();
                }
                else if (!isNotInAnotherReservationTime) {
                    Toast.makeText(getApplicationContext(), "다른 예약 시간과 겹칩니다", Toast.LENGTH_SHORT).show();
                }
                else {
                    HashMap<String, Object> hm = new HashMap<>();

                    hm.put("shareParkDocumentName", reservationFirestoreDocumentId);
                    hm.put("id", loginId);
                    hm.put("time", ta.getAllTime());
                    hm.put("isCancelled", false);
                    hm.put("upTime", FieldValue.serverTimestamp());

                    FirestoreDatabase fd = new FirestoreDatabase();
                    fd.insertData("reservation", hm);

                    Toast.makeText(getApplicationContext(), "예약되었습니다", Toast.LENGTH_SHORT).show();

                    finish();
                }
            }
        });
    }

    public void setTotalHeightofListView(ListView listView) {
        int numberOfItems = ta.getCount();
        int itemHeight = dpToPx(60); // 아이템 높이를 dp 단위로 변환하여 사용

        // Calculate total height of all items.
        int totalItemsHeight = numberOfItems * itemHeight;

        // Calculate total height of all item dividers.
        int totalDividersHeight = listView.getDividerHeight() * (numberOfItems - 1);

        // Set list height.
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalItemsHeight + totalDividersHeight;
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    public void calculatePrice () {
        if (pricePerHour != -1) {
            int totalPrice = ta.getTotalMinute() * pricePerHour / 60;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (totalPrice == 0) {
                        reservationTotalPriceContentTxt.setText("무료");
                        reservationWonTxt.setVisibility(View.GONE);
                    }
                    else {
                        reservationTotalPriceContentTxt.setText(Integer.toString(totalPrice));
                        reservationWonTxt.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
    }

    public boolean isInBetweenTime (String targetStartTime, String targetEndTime, String compareStartTime, String compareEndTime) {
        int targetStart = Integer.parseInt(targetStartTime);
        int targetEnd = Integer.parseInt(targetEndTime);
        int compareStart = Integer.parseInt(compareStartTime);
        int compareEnd = Integer.parseInt(compareEndTime);

        return targetStart >= compareStart && targetEnd <= compareEnd;
    }

    public boolean isNotOverlapping(String targetStartTime, String targetEndTime, String compareStartTime, String compareEndTime) {
        int targetStart = Integer.parseInt(targetStartTime);
        int targetEnd = Integer.parseInt(targetEndTime);
        int compareStart = Integer.parseInt(compareStartTime);
        int compareEnd = Integer.parseInt(compareEndTime);

        return targetEnd <= compareStart || targetStart >= compareEnd;
    }
}
