package com.bucheon.yeoddadae;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import android.content.Intent;
import java.util.HashMap;
import java.util.Map;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ShareParkActivity extends AppCompatActivity {
    CalendarView calendarView;
    Spinner timeSpinner, tmSpinner;
    Button shareParkBackBtn, reservationButton;
    EditText parkAddressEditTxt;
    EditText parkDetailAddressEditTxt;
    EditText priceEditText;
    EditText sharerNameEditTxt;
    EditText sharerPhoneEditTxt;
    EditText sharerEmailEditTxt;
    EditText sharerRelationEditTxt;
    Button registrationBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_park);


        shareParkBackBtn = findViewById(R.id.shareParkBackBtn);
        parkAddressEditTxt = findViewById(R.id.parkAddressEditTxt);
        parkDetailAddressEditTxt = findViewById(R.id.parkDetailAddressEditTxt);
        timeSpinner = findViewById(R.id.timeSpinner);
        tmSpinner = findViewById(R.id.tmSpinner);
        priceEditText = findViewById(R.id.priceEditText);
        sharerNameEditTxt = findViewById(R.id.sharerNameEditTxt);
        sharerPhoneEditTxt = findViewById(R.id.sharerPhoneEditTxt);
        sharerEmailEditTxt = findViewById(R.id.sharerEmailEditTxt);
        sharerRelationEditTxt = findViewById(R.id.sharerRelationEditTxt);
        registrationBtn = findViewById(R.id.registrationBtn);
        calendarView = findViewById(R.id.calendarView);
        reservationButton= findViewById(R.id.reservationButton);

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                TextView selectedDateTextView = findViewById(R.id.selectedDateTextView);
                selectedDateTextView.setText(year + "년 " + (month + 1) + "월 " + dayOfMonth + "일");
            }
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.time_units, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeSpinner.setAdapter(adapter);
        tmSpinner.setAdapter(adapter);
        shareParkBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        reservationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ShareParkActivity.this, "예약이 완료되었습니다.", Toast.LENGTH_SHORT).show();
            }
        });
        registrationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirestoreDatabase fd = new FirestoreDatabase();

                String ownerName = sharerNameEditTxt.getText().toString();
                String ownerPhone = sharerPhoneEditTxt.getText().toString();
                String ownerEmail = sharerEmailEditTxt.getText().toString();
                String ownerParkingRelation = sharerRelationEditTxt.getText().toString();
                String parkAddress = parkAddressEditTxt.getText().toString();
                String parkDetailAddress = parkDetailAddressEditTxt.getText().toString();
                String selectedTimeUnit = timeSpinner.getSelectedItem().toString();
                String price = priceEditText.getText().toString();

                HashMap<String, Object> hm = new HashMap<>();
                hm.put("ownerName", ownerName);
                hm.put("ownerPhone", ownerPhone);
                hm.put("ownerEmail", ownerEmail);
                hm.put("ownerParkingRelation", ownerParkingRelation);
                hm.put("parkAddress", parkAddress);
                hm.put("parkDetailAddress", parkDetailAddress);
                hm.put("timeUnit", selectedTimeUnit);
                hm.put("price", price);
                hm.put("isApproval", false);

                fd.insertData("sharePark", hm);

                finish();
            }
        });
    }
}
