package com.bucheon.yeoddadae;

import static com.google.android.exoplayer2.ExoPlayerLibraryInfo.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.firestore.FieldValue;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.poi_item.TMapPOIItem;

import java.util.ArrayList;
import java.util.HashMap;

public class AddReportDiscountParkActivity extends AppCompatActivity {
    String loginId;
    String poiId;
    double poiLat;
    double poiLon;
    String poiPhone;
    SearchParkAdapter spa;

    ImageButton addReportDiscountParkBackBtn;
    EditText addReportDiscountParkAddressContentEditTxt;
    EditText addReportDiscountParkConditionContentEditTxt;
    EditText addReportDiscountParkBenefitContentEditTxt;
    ImageButton reportBtn;
    ConstraintLayout findLocationConstLayout;
    Button findBackBtn;
    EditText searchContentEditTxt;
    Button searchBtn;
    ListView searchResultListView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_report_discount_park);

        Intent inIntent = getIntent();
        loginId = inIntent.getStringExtra("loginId");

        addReportDiscountParkBackBtn = findViewById(R.id.addReportDiscountParkBackBtn);
        addReportDiscountParkAddressContentEditTxt = findViewById(R.id.addReportDiscountParkAddressContentEditTxt);
        addReportDiscountParkConditionContentEditTxt = findViewById(R.id.addReportDiscountParkConditionContentEditTxt);
        addReportDiscountParkBenefitContentEditTxt = findViewById(R.id.addReportDiscountParkBenefitContentEditTxt);
        reportBtn = findViewById(R.id.reportBtn);
        findLocationConstLayout = findViewById(R.id.findLocationConstLayout);
        findBackBtn = findViewById(R.id.findBackBtn);
        searchContentEditTxt = findViewById(R.id.searchContentEditTxt);
        searchBtn = findViewById(R.id.searchBtn);
        searchResultListView = findViewById(R.id.searchResultListView);

        addReportDiscountParkBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        addReportDiscountParkAddressContentEditTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        findLocationConstLayout.setVisibility(View.VISIBLE);
                    }
                });
            }
        });

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchContent = searchContentEditTxt.getText().toString();

                if (searchContent.isEmpty()) {
                    Log.d(TAG, "검색 내용이 비어있음");
                    return;
                }
                else {
                    TMapData tMapData = new TMapData();

                    tMapData.findAllPOI(searchContent, new TMapData.FindAllPOIListenerCallback() {
                        @Override
                        public void onFindAllPOI(ArrayList<TMapPOIItem> arrayList) {
                            if (arrayList == null) {
                                return;
                            }
                            spa = new SearchParkAdapter();

                            for (int i = 0; i < arrayList.size(); i++) {
                                TMapPOIItem item = arrayList.get(i);

                                if (item.firstNo.equals("0") && item.secondNo.equals("0")) {
                                    spa.addItem(new ParkItem(4, item.name, item.radius, null, item.telNo, null, -1, item.frontLat, item.frontLon, item.id, null));
                                }
                                else {
                                    if (item.name.contains("주차")) {
                                        if (item.name.contains("공영")) {
                                            spa.addItem(new ParkItem(2, item.name, item.radius, null, item.telNo, null, -1, item.frontLat, item.frontLon, item.id, null));
                                        }
                                        else {
                                            spa.addItem(new ParkItem(1, item.name, item.radius, null, item.telNo, null, -1, item.frontLat, item.frontLon, item.id, null));
                                        }
                                    }
                                    else {
                                        spa.addItem(new ParkItem(5, item.name, item.radius, null, item.telNo, null, -1, item.frontLat, item.frontLon, item.id, null));
                                    }

                                }
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    searchResultListView.setAdapter(spa);
                                }
                            });
                        }
                    });
                }
            }
        });

        searchResultListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ParkItem pi = (ParkItem) spa.getItem(position);
                poiId = pi.getPoiId();
                poiLat = pi.getLat();
                poiLon = pi.getLon();
                poiPhone = pi.getPhone();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        addReportDiscountParkAddressContentEditTxt.setText(pi.getName());
                        findLocationConstLayout.setVisibility(View.GONE);
                    }
                });
            }
        });

        findBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        findLocationConstLayout.setVisibility(View.GONE);
                    }
                });
            }
        });

        reportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String parkName = addReportDiscountParkAddressContentEditTxt.getText().toString();
                String condition = addReportDiscountParkConditionContentEditTxt.getText().toString();
                String discount = addReportDiscountParkBenefitContentEditTxt.getText().toString();
                int discountInt;

                if (poiId == null || poiLat == 0 || poiLon == 0 || parkName.equals("")) {
                    Toast.makeText(getApplicationContext(), "위치를 찾으세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (condition.equals("")) {
                    Toast.makeText(getApplicationContext(), "조건을 입력하세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (discount.equals("")) {
                    Toast.makeText(getApplicationContext(), "혜택을 입력하세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    discountInt = Integer.parseInt(discount);
                } catch (NumberFormatException e) {
                    Toast.makeText(getApplicationContext(), "혜택을 숫자로 입력하세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                HashMap<String, Object> hm = new HashMap<>();
                hm.put("reporterId", loginId);
                hm.put("poiID", poiId);
                hm.put("poiLat", poiLat);
                hm.put("poiLon", poiLon);
                hm.put("poiPhone", poiPhone);
                hm.put("parkName", parkName);
                hm.put("parkCondition", condition);
                hm.put("parkDiscount", discountInt);
                hm.put("ratePerfectCount", 0);
                hm.put("rateMistakeCount", 0);
                hm.put("rateWrongCount", 0);
                hm.put("isCancelled", false);
                hm.put("isApproval", false);
                hm.put("upTime", FieldValue.serverTimestamp());

                FirestoreDatabase fd = new FirestoreDatabase();
                fd.insertData("reportDiscountPark", hm, new OnFirestoreDataLoadedListener() {
                    @Override
                    public void onDataLoaded(Object data) {
                        Toast.makeText(getApplicationContext(), "무료/할인 주차장 제보 완료되었습니다", Toast.LENGTH_SHORT).show();
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
}
