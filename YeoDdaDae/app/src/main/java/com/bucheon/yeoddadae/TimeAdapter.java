package com.bucheon.yeoddadae;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;

public class TimeAdapter extends BaseAdapter {
    private final Activity activity;

    ArrayList<TimeItem> items = new ArrayList<>();

    public TimeAdapter(Activity activity) {
        this.activity = activity;
    }

    public void addItem(TimeItem item) {
        items.add(item);
        notifyDataSetChanged();
    }

    public void removeItem(TimeItem item) {
        items.remove(item);
        notifyDataSetChanged();
    }

    public ArrayList<CalendarDay> clear() {
        ArrayList<CalendarDay> al = new ArrayList<>();

        for (TimeItem item : items) {
            al.add(item.getDate());
        }
        items.clear();
        notifyDataSetChanged();

        return al;
    }

    public TimeItem findItem(CalendarDay date) {
        for (TimeItem item : items) {
            if (item.getDate().equals(date)) {
                return item;
            }
        }
        return null; // 못 찾은 경우 null 반환
    }

    public int getTotalMinute () {
        int totalMinutes = 0;

        for (TimeItem item : items) {
            int startHour = Integer.parseInt(item.getStartTime().substring(0, 2));
            int startMinute = Integer.parseInt(item.getStartTime().substring(2, 4));
            int endHour = Integer.parseInt(item.getEndTime().substring(0, 2));
            int endMinute = Integer.parseInt(item.getEndTime().substring(2, 4));

            int startTimeInMinutes = startHour * 60 + startMinute;
            int endTimeInMinutes = endHour * 60 + endMinute;

            if (endTimeInMinutes == 0) {
                endTimeInMinutes = 24 * 60;
            }

            totalMinutes += (endTimeInMinutes - startTimeInMinutes);
        }

        return totalMinutes;
    }

    public void sortByDate () {
        if (items != null && items.size() > 1) {
            Collections.sort(items, new Comparator<TimeItem>() {
                @Override
                public int compare(TimeItem o1, TimeItem o2) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(o1.getDate().getYear(), o1.getDate().getMonth(), o1.getDate().getDay());
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                    int formattedDate1 = Integer.parseInt(sdf.format(calendar.getTime()));

                    calendar = Calendar.getInstance();
                    calendar.set(o2.getDate().getYear(), o2.getDate().getMonth(), o2.getDate().getDay());
                    sdf = new SimpleDateFormat("yyyyMMdd");
                    int formattedDate2 = Integer.parseInt(sdf.format(calendar.getTime()));

                    return Integer.compare(formattedDate1, formattedDate2);
                }
            });
            notifyDataSetChanged(); // Notify adapter that dataset has changed
        }
    }

    public HashMap<String, ArrayList<String>> getAllTime() {
        sortByDate();
        HashMap<String, ArrayList<String>> hm = new HashMap<>();

        if (items.size() == 0) {
            return null;
        }

        for (TimeItem item : items) {
            CalendarDay cd = item.getDate();
            String cdMonth;
            String cdDay;

            if (cd.getMonth() < 10) {
                cdMonth = "0" + cd.getMonth();
            }
            else {
                cdMonth = cd.getMonth() + "";
            }

            if (cd.getDay() < 10) {
                cdDay = "0" + cd.getDay();
            }
            else {
                cdDay = cd.getDay() + "";
            }

            String cdString = cd.getYear() + cdMonth + cdDay;

            String itemStartTime = item.getStartTime();
            String itemEndTime = item.getEndTime();
            if (itemEndTime.equals("0000")) {
                itemEndTime = "2400";
            }

            if ((itemStartTime != itemEndTime) && (itemStartTime.compareTo(itemEndTime) < 0)) {
                hm.put(cdString, new ArrayList<>(Arrays.asList(itemStartTime, itemEndTime)));
            }
            else {
                return null;
            }
        }

        return hm;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Context context = parent.getContext();
        TimeItem time = items.get(position);
        CalendarDay cd = time.getDate();

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_time, parent, false);
        }
        convertView.setEnabled(false);

        TextView textViewDate = convertView.findViewById(R.id.textViewDate);
        EditText startTime = convertView.findViewById(R.id.startTime);
        EditText endTime = convertView.findViewById(R.id.endTime);

        String mothString = "";
        if (1 <= cd.getMonth() && cd.getMonth() <= 9) {
            mothString += "0" + cd.getMonth();
        }
        else {
            mothString += cd.getMonth();
        }

        String dayString = "";
        if (1 <= cd.getDay() && cd.getDay() <= 9) {
            dayString += "0" + cd.getDay();
        }
        else {
            dayString += cd.getDay();
        }

        textViewDate.setText (cd.getYear() + "년 " + mothString + "월 " + dayString + "일");

        startTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] hour = {Integer.parseInt(time.getStartTime().substring(0, 2))};
                int[] minute = {Integer.parseInt(time.getStartTime().substring(2, 4))};

                TimePickerDialog timePickerDialog = new TimePickerDialog(context, android.R.style.Theme_Holo_Light_Dialog_NoActionBar, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                        String formattedTime = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);
                        startTime.setText(formattedTime);

                        String timeString = String.format(Locale.KOREA, "%02d%02d", selectedHour, selectedMinute);
                        time.setStartTime(timeString);

                        hour[0] = Integer.parseInt(time.getStartTime().substring(0, 2));
                        minute[0] = Integer.parseInt(time.getStartTime().substring(2, 4));

                        if (activity instanceof ReservationParkActivity) {
                            ((ReservationParkActivity) activity).calculatePrice();
                        }
                    }
                }, hour[0], minute[0], true);


                timePickerDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent); // 배경 투명하게 설정
                timePickerDialog.show();
            }
        });

        endTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] hour = {Integer.parseInt(time.getEndTime().substring(0, 2))};
                int[] minute = {Integer.parseInt(time.getEndTime().substring(2, 4))};

                if (hour[0] == 24 && minute[0] == 0) {
                    hour[0] = 0;
                }

                TimePickerDialog timePickerDialog = new TimePickerDialog(context, android.R.style.Theme_Holo_Light_Dialog_NoActionBar, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                        if (selectedHour == 0 && selectedMinute == 0) {
                            endTime.setText("24:00");
                        }
                        else {
                            String formattedTime = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);
                            endTime.setText(formattedTime);
                        }
                        String timeString = String.format(Locale.KOREA, "%02d%02d", selectedHour, selectedMinute);
                        time.setEndTime(timeString);

                        hour[0] = Integer.parseInt(time.getStartTime().substring(0, 2));
                        minute[0] = Integer.parseInt(time.getStartTime().substring(2, 4));

                        if (activity instanceof ReservationParkActivity) {
                            ((ReservationParkActivity) activity).calculatePrice();
                        }
                    }
                }, hour[0], minute[0], true);

                timePickerDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent); // 배경 투명하게 설정
                timePickerDialog.show();
            }
        });

        return convertView;
    }
}
