package com.bucheon.yeoddadae;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.skt.Tmap.TMapData;
import com.skt.Tmap.address_info.TMapAddressInfo;

import java.util.HashMap;

public class ReservationParkActivity extends AppCompatActivity {
    String reservationFirestoreDocumentId;

    TextView reservationParkAddressContentTxt;
    TextView reservationParkDetailAddressContentTxt;
    TextView reservationSharerNameContentTxt;
    TextView reservationSharerPhoneContentTxt;
    TextView reservationSharerEmailContentTxt;
    TextView reservationSharerRelationContentTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation_park);

        reservationParkAddressContentTxt = findViewById(R.id.reservationParkAddressContentTxt);
        reservationParkDetailAddressContentTxt = findViewById(R.id.reservationParkDetailAddressContentTxt);
        reservationSharerNameContentTxt = findViewById(R.id.reservationSharerNameContentTxt);
        reservationSharerPhoneContentTxt = findViewById(R.id.reservationSharerPhoneContentTxt);
        reservationSharerEmailContentTxt = findViewById(R.id.reservationSharerEmailContentTxt);
        reservationSharerRelationContentTxt = findViewById(R.id.reservationSharerRelationContentTxt);

        Intent inIntent = getIntent();
        reservationFirestoreDocumentId = inIntent.getStringExtra("fireStoreDocumentId");

        FirestoreDatabase fd = new FirestoreDatabase();
        fd. loadShareParkInfo(reservationFirestoreDocumentId, new OnFirestoreDataLoadedListener() {
            @Override
            public void onDataLoaded(Object data) {
                HashMap<String, Object> hm = (HashMap<String, Object>) data;

                for (String key : hm.keySet()) {
                    Object value = hm.get(key);
                    Log.d(TAG, "Key: " + key + ", Value: " + value);

                    TMapData tMapData = new TMapData();
                    tMapData.reverseGeocoding((Double) hm.get("lat"), (Double) hm.get("lon"), "A02", new TMapData.reverseGeocodingListenerCallback() {
                        @Override
                        public void onReverseGeocoding(TMapAddressInfo tMapAddressInfo) {
                            reservationParkAddressContentTxt.setText(tMapAddressInfo.strFullAddress);
                        }
                    });

                    reservationParkDetailAddressContentTxt.setText((String) hm.get("parkDetailAddress"));
                    reservationSharerNameContentTxt.setText((String) hm.get("ownerName"));

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
                    reservationSharerPhoneContentTxt.setText(newPhoneString);
                    reservationSharerEmailContentTxt.setText((String) hm.get("ownerPhone"));
                    reservationSharerRelationContentTxt.setText((String) hm.get("ownerParkingRelation"));
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
    }
}
