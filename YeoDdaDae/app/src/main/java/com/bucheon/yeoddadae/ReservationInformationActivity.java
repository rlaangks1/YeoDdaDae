package com.bucheon.yeoddadae;

import static android.graphics.ColorSpace.Model.RGB;
import static com.google.android.exoplayer2.ExoPlayerLibraryInfo.TAG;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.address_info.TMapAddressInfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class ReservationInformationActivity extends AppCompatActivity {
    String loginId;
    String documentId;
    HashMap<String, Object> reservationInfo;
    HashMap<String, Object> shareParkInfo;

    Button reservationInfoBackBtn;
    TextView reservationInfoIdContentTxt;
    TextView reservationInfoIsCanceledContentTxt;
    TextView reservationInfoUpTimeContentTxt;
    TextView reservationInfoTimeContentTxt;
    TextView reservationInfoPriceContentTxt;
    TextView reservationInfoShareParkNewAddressContentTxt;
    TextView reservationInfoShareParkOldAddressContentTxt;
    TextView reservationInfoShareParkDetailaddressContentTxt;
    Button reservationInfoCancelBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation_information);

        reservationInfoBackBtn = findViewById(R.id.reservationInfoBackBtn);
        reservationInfoIdContentTxt = findViewById(R.id.reservationInfoIdContentTxt);
        reservationInfoIsCanceledContentTxt = findViewById(R.id.reservationInfoIsCanceledContentTxt);
        reservationInfoUpTimeContentTxt = findViewById(R.id.reservationInfoUpTimeContentTxt);
        reservationInfoTimeContentTxt = findViewById(R.id.reservationInfoTimeContentTxt);
        reservationInfoPriceContentTxt = findViewById(R.id.reservationInfoPriceContentTxt);
        reservationInfoShareParkNewAddressContentTxt = findViewById(R.id.reservationInfoShareParkNewAddressContentTxt);
        reservationInfoShareParkOldAddressContentTxt = findViewById(R.id.reservationInfoShareParkOldAddressContentTxt);
        reservationInfoShareParkDetailaddressContentTxt = findViewById(R.id.reservationInfoShareParkDetailaddressContentTxt);
        reservationInfoCancelBtn = findViewById(R.id.reservationInfoCancelBtn);

        Intent inIntent = getIntent();
        loginId = inIntent.getStringExtra("id");
        documentId = inIntent.getStringExtra("documentId");

        FirestoreDatabase fd = new FirestoreDatabase();

        fd.loadReservation(loginId, documentId, new OnFirestoreDataLoadedListener() {
            @Override
            public void onDataLoaded(Object data) {
                reservationInfo = (HashMap<String, Object>) data;

                fd.loadShareParkInfo((String) reservationInfo.get("shareParkDocumentName"), new OnFirestoreDataLoadedListener() {
                    @Override
                    public void onDataLoaded(Object data) {
                        shareParkInfo = (HashMap<String, Object>) data;
                        init();
                    }

                    @Override
                    public void onDataLoadError(String errorMessage) {

                    }
                });
            }

            @Override
            public void onDataLoadError(String errorMessage) {

            }
        });

        reservationInfoBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        reservationInfoCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ReservationInformationActivity.this);
                builder.setTitle("예약 취소 확인");
                builder.setMessage("정말 취소하시겠습니까\n(되돌릴 수 없습니다)");
                builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        fd.cancelReservation(loginId, documentId, new OnFirestoreDataLoadedListener() {
                            @Override
                            public void onDataLoaded(Object data) {
                                Toast.makeText(getApplicationContext(), "예약 취소되었습니다", Toast.LENGTH_SHORT).show();
                                finish();
                            }

                            @Override
                            public void onDataLoadError(String errorMessage) {
                            }
                        });
                    }
                });
                builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    private void init() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                reservationInfoIdContentTxt.setText(documentId);

                boolean isCancelled = (boolean) reservationInfo.get("isCancelled");
                reservationInfoIsCanceledContentTxt.setText(Boolean.toString(isCancelled));
                if (isCancelled) {
                    reservationInfoIsCanceledContentTxt.setTextColor(android.graphics.Color.rgb(255, 0, 0));
                    reservationInfoCancelBtn.setVisibility(View.GONE);
                }

                Timestamp timestamp = (Timestamp) reservationInfo.get("upTime");
                Date date = timestamp.toDate();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일 HH:mm:ss", Locale.KOREA);
                String dateString = sdf.format(date);
                reservationInfoUpTimeContentTxt.setText(dateString);

                HashMap<String, ArrayList<String>> reservationTime = (HashMap<String, ArrayList<String>>) reservationInfo.get("time");
                ArrayList<String> keys = new ArrayList<>(reservationTime.keySet());
                Collections.sort(keys);
                String reservationTimeString = "";
                for (String key : keys) {
                    int year = Integer.parseInt(key.substring(0, 4));
                    int month = Integer.parseInt(key.substring(4, 6));
                    int day = Integer.parseInt(key.substring(6, 8));

                    String startTimeHour = reservationTime.get(key).get(0).substring(0,2);
                    String startTimeMinute = reservationTime.get(key).get(0).substring(2,4);
                    String endTimeHour = reservationTime.get(key).get(1).substring(0,2);
                    String endTimeMinute = reservationTime.get(key).get(1).substring(2,4);

                    reservationTimeString += year + "년 " + month + "월 " + day + "일 " + startTimeHour + ":" + startTimeMinute + "부터 " + endTimeHour + ":" + endTimeMinute + "까지\n";
                }
                reservationTimeString = reservationTimeString.substring(0, reservationTimeString.length() - 1); // 마지막 줄바꿈 제거
                reservationInfoTimeContentTxt.setText(reservationTimeString);

                reservationInfoPriceContentTxt.setText(((Long) reservationInfo.get("price")).toString());

                reservationInfoShareParkDetailaddressContentTxt.setText((String) shareParkInfo.get("parkDetailAddress"));
            }
        });
        TMapData tMapdata = new TMapData();
        tMapdata.reverseGeocoding((double) shareParkInfo.get("lat"), (double) shareParkInfo.get("lon"), "A10", new TMapData.reverseGeocodingListenerCallback() {
            @Override
            public void onReverseGeocoding(TMapAddressInfo tMapAddressInfo) {
                if (tMapAddressInfo != null) {
                    String[] adrresses = tMapAddressInfo.strFullAddress.split(",");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            reservationInfoShareParkNewAddressContentTxt.setText(adrresses[2]);
                            reservationInfoShareParkOldAddressContentTxt.setText(adrresses[1]);
                        }
                    });
                }
            }
        });
    }
}
