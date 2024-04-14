package com.bucheon.yeoddadae;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.skt.Tmap.TMapData;
import com.skt.Tmap.address_info.TMapAddressInfo;

import java.text.DecimalFormat;
import java.util.HashMap;

public class ReservationParkActivity extends AppCompatActivity {
    String loginId;
    String reservationFirestoreDocumentId;

    Button reservationBackBtn;
    TextView reservationParkNewAddressContentTxt;
    TextView reservationParkOldAddressContentTxt;
    TextView reservationParkDetailAddressContentTxt;
    TextView reservationSharerNameContentTxt;
    TextView reservationSharerPhoneContentTxt;
    TextView reservationSharerEmailContentTxt;
    TextView reservationSharerRelationContentTxt;
    TextView reservationPriceContentTxt;
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
        reservationBtn = findViewById(R.id.reservationBtn);

        Intent inIntent = getIntent();
        reservationFirestoreDocumentId = inIntent.getStringExtra("fireStoreDocumentId");
        loginId = inIntent.getStringExtra("loginId");
        if (loginId == null || loginId.equals("")) {
            Log.d(TAG, "loginId가 전달되지 않음 (오류)");
            finish();
        }

        reservationBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        FirestoreDatabase fd = new FirestoreDatabase();
        fd. loadShareParkInfo(reservationFirestoreDocumentId, new OnFirestoreDataLoadedListener() {
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
                    String newPriceString = formatter.format(number) + "원";

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

        reservationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, Object> hm = new HashMap<>();

                hm.put("shareParkDocumentName", reservationFirestoreDocumentId);
                hm.put("id", loginId);
                hm.put("startTime", "엄");
                hm.put("endTime", "준식");
                hm.put("isCancelled", false);

                FirestoreDatabase fd = new FirestoreDatabase();
                fd.insertData("reservation", hm);
            }
        });
    }
}
