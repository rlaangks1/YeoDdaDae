package com.bucheon.yeoddadae;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ShareParkActivity extends AppCompatActivity {

    Button shareParkBackBtn;
    EditText parkAddressEditTxt;
    EditText parkDetailAddressEditTxt;
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
        sharerNameEditTxt = findViewById(R.id.sharerNameEditTxt);
        sharerPhoneEditTxt = findViewById(R.id.sharerPhoneEditTxt);
        sharerEmailEditTxt = findViewById(R.id.sharerEmailEditTxt);
        sharerRelationEditTxt = findViewById(R.id.sharerRelationEditTxt);
        registrationBtn = findViewById(R.id.registrationBtn);

        shareParkBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        registrationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 등록신청버튼 클릭시 동작
            }
        });
    }
}
