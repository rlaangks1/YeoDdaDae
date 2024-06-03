package com.bucheon.yeoddadae;

import static com.google.android.exoplayer2.ExoPlayerLibraryInfo.TAG;

import java.text.NumberFormat;
import java.util.Locale;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.HashMap;

public class MyYdPointFragment extends Fragment {
    String loginId;
    long ydPoint;

    TextView myYdPointTxt;
    Button toChargeYdPointBtn;

    public MyYdPointFragment(String id) {
        this.loginId = id;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_yd_point, container, false);

        myYdPointTxt = view.findViewById(R.id.myYdPointTxt);
        toChargeYdPointBtn = view.findViewById(R.id.toChargeYdPointBtn);

        toChargeYdPointBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareParkIntent = new Intent(getActivity(), YdPointChargeActivity.class);
                shareParkIntent.putExtra("loginId", loginId);
                startActivity(shareParkIntent);
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirestoreDatabase fd = new FirestoreDatabase();
        fd.loadYdPoint(loginId, new OnFirestoreDataLoadedListener() {
            @Override
            public void onDataLoaded(Object data) {
                ydPoint = (long) data;
                String formattedYdPoint = NumberFormat.getNumberInstance(Locale.KOREA).format(ydPoint);
                myYdPointTxt.setText(formattedYdPoint);
            }

            @Override
            public void onDataLoadError(String errorMessage) {
                Log.d(TAG, errorMessage);
                Toast.makeText(getActivity().getApplicationContext(), "오류 발생", Toast.LENGTH_SHORT).show();
            }
        });
    }
}