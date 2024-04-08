package com.bucheon.yeoddadae;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;

public class ReservationParkActivity extends AppCompatActivity {
    String reservationFirestoreDocumentId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation_park);

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
                }
            }

            @Override
            public void onDataLoadError(String errorMessage) {
                Log.d(TAG, errorMessage);
            }
        });
    }
}
