package com.bucheon.yeoddadae;

import static android.content.ContentValues.TAG;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.recyclerview.widget.RecyclerView;

import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class TimeAdapter extends BaseAdapter {
    ArrayList<TimeItem> items = new ArrayList<>();

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
            if (item.getDate() == date) {
                return item;
            }
        }
        return null; // 못 찾은 경우 null 반환
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
        HashMap<String, ArrayList<String>> hm = new HashMap<>();

        if (items.size() == 0) {
            hm = null;
            return hm;
        }

        for (TimeItem item : items) {
            CalendarDay cd = item.getDate();
            String cdString = "" + cd.getYear() + cd.getMonth() + cd.getDay();
            String itemStartTime = item.getStartTime();
            String itemEndTime = item.getEndTime();
            if (itemEndTime == "0000") {
                itemEndTime = "2400";
            }

            if ((itemStartTime != itemEndTime) && (itemStartTime.compareTo(itemEndTime) < 0)) {
                hm.put(cdString, new ArrayList<>(Arrays.asList(itemStartTime, itemEndTime)));
            }
            else {
                hm = null;
                return hm;
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
            convertView = inflater.inflate(R.layout.time_item, parent, false);
        }

        TextView textViewDate = convertView.findViewById(R.id.textViewDate);
        EditText startTime = convertView.findViewById(R.id.startTime);
        EditText endTime = convertView.findViewById(R.id.endTime);

        String dayString = "";
        if (1 <= cd.getDay() && cd.getDay() <= 9) {
            dayString += "0" + cd.getDay();
        }
        else {
            dayString += cd.getDay();
        }

        textViewDate.setText (cd.getYear() + "년 " + cd.getMonth() + "월 " + dayString + "일");

        startTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] hour = {Integer.parseInt(time.getStartTime().substring(0, 2))};
                int[] minute = {Integer.parseInt(time.getStartTime().substring(2, 4))};

                TimePickerDialog timePickerDialog = new TimePickerDialog(context, android.R.style.Theme_Holo_Light_Dialog_NoActionBar, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                        String timeFormat;
                        int amPmHour = 0;
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
                        startTime.setText(formattedTime);
                        String timeString = String.format(Locale.getDefault(), "%02d%02d", selectedHour, selectedMinute);
                        time.setStartTime(timeString);
                        hour[0] = Integer.parseInt(time.getStartTime().substring(0, 2));
                        minute[0] = Integer.parseInt(time.getStartTime().substring(2, 4));
                    }
                }, hour[0], minute[0], false); // is24HourView를 false로 설정


                timePickerDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent); // 배경 투명하게 설정
                timePickerDialog.show();
            }
        });

        endTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] hour = {Integer.parseInt(time.getStartTime().substring(0, 2))};
                int[] minute = {Integer.parseInt(time.getStartTime().substring(2, 4))};

                TimePickerDialog timePickerDialog = new TimePickerDialog(context, android.R.style.Theme_Holo_Light_Dialog_NoActionBar, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                        String timeFormat;
                        int amPmHour = 0;
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
                        endTime.setText(formattedTime);
                        String timeString = String.format(Locale.getDefault(), "%02d%02d", selectedHour, selectedMinute);
                        time.setEndTime(timeString);
                        hour[0] = Integer.parseInt(time.getStartTime().substring(0, 2));
                        minute[0] = Integer.parseInt(time.getStartTime().substring(2, 4));
                    }
                }, hour[0], minute[0], false); // is24HourView를 false로 설정

                timePickerDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent); // 배경 투명하게 설정
                timePickerDialog.show();
            }
        });

        return convertView;
    }
}
