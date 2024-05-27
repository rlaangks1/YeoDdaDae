package com.bucheon.yeoddadae;

import static com.google.android.exoplayer2.ExoPlayerLibraryInfo.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.address_info.TMapAddressInfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ApproveShareParkInformationActivity extends AppCompatActivity {
    String documentId;
    HashMap<String, Object> shareParkInfo;

    Button approveShareParkInfoBackBtn;
    TextView approveShareParkInfoIdContentTxt;
    TextView approveShareParkInfoShareParkNewAddressContentTxt;
    TextView approveShareParkInfoShareParkOldAddressContentTxt;
    TextView approveShareParkInfoShareParkDetailaddressContentTxt;
    TextView approveShareParkInfoOwnerIdContentTxt;
    TextView approveShareParkInfoNameContentTxt;
    TextView approveShareParkInfoPhoneContentTxt;
    TextView approveShareParkInfoEmailContentTxt;
    TextView approveShareParkInfoRelationContentTxt;
    TextView approveShareParkInfoPriceContentTxt;
    TextView approveShareParkInfoHourPerTxt;
    TextView approveShareParkInfoWonTxt;
    TextView approveShareParkInfoShareTimeContentTxt;
    TextView approveShareParkInfoUpTimeContentTxt;
    Button approveBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approve_share_park_information);

        Intent inIntent = getIntent();
        documentId = inIntent.getStringExtra("documentId");

        approveShareParkInfoBackBtn = findViewById(R.id.approveShareParkInfoBackBtn);
        approveShareParkInfoIdContentTxt = findViewById(R.id.approveShareParkInfoIdContentTxt);
        approveShareParkInfoShareParkNewAddressContentTxt = findViewById(R.id.approveShareParkInfoShareParkNewAddressContentTxt);
        approveShareParkInfoShareParkOldAddressContentTxt = findViewById(R.id.approveShareParkInfoShareParkOldAddressContentTxt);
        approveShareParkInfoShareParkDetailaddressContentTxt = findViewById(R.id.approveShareParkInfoShareParkDetailaddressContentTxt);
        approveShareParkInfoOwnerIdContentTxt = findViewById(R.id.approveShareParkInfoOwnerIdContentTxt);
        approveShareParkInfoNameContentTxt = findViewById(R.id.approveShareParkInfoNameContentTxt);
        approveShareParkInfoPhoneContentTxt = findViewById(R.id.approveShareParkInfoPhoneContentTxt);
        approveShareParkInfoEmailContentTxt = findViewById(R.id.approveShareParkInfoEmailContentTxt);
        approveShareParkInfoRelationContentTxt = findViewById(R.id.approveShareParkInfoRelationContentTxt);
        approveShareParkInfoPriceContentTxt = findViewById(R.id.approveShareParkInfoPriceContentTxt);
        approveShareParkInfoHourPerTxt = findViewById(R.id.approveShareParkInfoHourPerTxt);
        approveShareParkInfoWonTxt = findViewById(R.id.approveShareParkInfoWonTxt);
        approveShareParkInfoShareTimeContentTxt = findViewById(R.id.approveShareParkInfoShareTimeContentTxt);
        approveShareParkInfoUpTimeContentTxt = findViewById(R.id.approveShareParkInfoUpTimeContentTxt);
        approveBtn = findViewById(R.id.approveBtn);

        FirestoreDatabase fd = new FirestoreDatabase();
        fd.loadShareParkInfo(documentId, new OnFirestoreDataLoadedListener() {
            @Override
            public void onDataLoaded(Object data) {
                shareParkInfo = (HashMap<String, Object>) data;
                init();
            }

            @Override
            public void onDataLoadError(String errorMessage) {
                Log.d(TAG, errorMessage);
                Toast.makeText(getApplicationContext(), "오류 발생", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        approveShareParkInfoBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        approveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirestoreDatabase fd = new FirestoreDatabase();
                fd.approveSharePark(documentId, new OnFirestoreDataLoadedListener() {
                    @Override
                    public void onDataLoaded(Object data) {
                        Toast.makeText(getApplicationContext(), "공유주차장 승인되었습니다", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onDataLoadError(String errorMessage) {
                        Log.d(TAG, errorMessage);
                        Toast.makeText(getApplicationContext(), "오류 발생", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });
    }

    void init() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                approveShareParkInfoIdContentTxt.setText(documentId);

                TMapData tMapData = new TMapData();
                tMapData.reverseGeocoding((double) shareParkInfo.get("lat"), (double) shareParkInfo.get("lon"), "A10", new TMapData.reverseGeocodingListenerCallback() {
                    @Override
                    public void onReverseGeocoding(TMapAddressInfo tMapAddressInfo) {
                        if (tMapAddressInfo != null) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    String [] adrresses = tMapAddressInfo.strFullAddress.split(",");
                                    approveShareParkInfoShareParkNewAddressContentTxt.setText(adrresses[2]);
                                    approveShareParkInfoShareParkOldAddressContentTxt.setText(adrresses[1]);
                                }
                            });
                        }
                    }
                });
                approveShareParkInfoShareParkDetailaddressContentTxt.setText((String) shareParkInfo.get("parkDetailAddress"));

                approveShareParkInfoOwnerIdContentTxt.setText((String) shareParkInfo.get("ownerId"));

                approveShareParkInfoNameContentTxt.setText((String) shareParkInfo.get("ownerName"));

                approveShareParkInfoPhoneContentTxt.setText((String) shareParkInfo.get("ownerPhone"));

                approveShareParkInfoEmailContentTxt.setText((String) shareParkInfo.get("ownerEmail"));

                approveShareParkInfoRelationContentTxt.setText((String) shareParkInfo.get("ownerParkingRelation"));
                
                if ((long) shareParkInfo.get("price") == 0) {
                    approveShareParkInfoHourPerTxt.setVisibility(View.GONE);
                    approveShareParkInfoWonTxt.setVisibility(View.GONE);
                    approveShareParkInfoPriceContentTxt.setText("무료");
                }
                else {
                    approveShareParkInfoPriceContentTxt.setText(Long.toString((long) shareParkInfo.get("price")));
                }

                HashMap<String, ArrayList<String>> shareTime = (HashMap<String, ArrayList<String>>) shareParkInfo.get("time");
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
                String shareTimeString = textBuilder.toString().substring(0, textBuilder.length() - 1);
                approveShareParkInfoShareTimeContentTxt.setText(shareTimeString);

                Timestamp timestamp = (Timestamp) shareParkInfo.get("upTime");
                Date date = timestamp.toDate();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일 HH:mm:ss", Locale.KOREA);
                String dateString = sdf.format(date);
                approveShareParkInfoUpTimeContentTxt.setText(dateString);

            }
        });
    }
}
