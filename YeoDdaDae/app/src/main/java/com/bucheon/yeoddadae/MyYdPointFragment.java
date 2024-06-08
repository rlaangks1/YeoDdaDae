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
    YdPointHistoryAdapter ypha;

    TextView myYdPointTxt;
    Button toChargeYdPointBtn;
    Button toRefundYdPointBtn;
    ListView pointHistoryListView;

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
        toRefundYdPointBtn = view.findViewById(R.id.toRefundYdPointBtn);
        pointHistoryListView = view.findViewById(R.id.pointHistoryListView);

        toChargeYdPointBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chargeYdPointIntent = new Intent(getActivity(), YdPointChargeActivity.class);
                chargeYdPointIntent.putExtra("loginId", loginId);
                startActivity(chargeYdPointIntent);
            }
        });

        toRefundYdPointBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent refundYdPointIntent = new Intent(getActivity(), YdPointRefundActivity.class);
                refundYdPointIntent.putExtra("loginId", loginId);
                startActivity(refundYdPointIntent);
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

        fd.loadYdPointHistory(loginId, new OnFirestoreDataLoadedListener() {
            @Override
            public void onDataLoaded(Object data) {
                if (ypha != null) {
                    ypha.clear();
                }

                ypha = new YdPointHistoryAdapter();

                ArrayList<ArrayList<HashMap<String, Object>>> result = (ArrayList<ArrayList<HashMap<String, Object>>>) data;
                ArrayList<HashMap<String, Object>> charge = result.get(0);
                ArrayList<HashMap<String, Object>> refund = result.get(1);
                ArrayList<HashMap<String, Object>> spend = result.get(2);
                ArrayList<HashMap<String, Object>> receive = result.get(3);

                for (HashMap<String, Object> hm : charge) {
                    ypha.addItem(new YdPointHistoryItem("충전", (long) hm.get("price"), null, null, null, null, null, (Timestamp) hm.get("upTime"), (String) hm.get("documentId")));
                }
                for (HashMap<String, Object> hm : refund) {
                    ypha.addItem(new YdPointHistoryItem("환급", (long) hm.get("refundedYdPoint"), (String) hm.get("bank"), (String) hm.get("accountNumber"), null, null, null, (Timestamp) hm.get("upTime"), (String) hm.get("documentId")));
                }
                for (HashMap<String, Object> hm : spend) {
                    ypha.addItem(new YdPointHistoryItem("사용", (long) hm.get("price"), null, null, (String) hm.get("type"), (String) hm.get("reservationId"), null, (Timestamp) hm.get("upTime"), (String) hm.get("documentId")));
                }
                for (HashMap<String, Object> hm : receive) {
                    ypha.addItem(new YdPointHistoryItem("받음", (long) hm.get("receivedYdPoint"), null, null, null, null, (String) hm.get("type"), (Timestamp) hm.get("upTime"), (String) hm.get("documentId")));
                }

                ypha.sortByUpTime();

                pointHistoryListView.setAdapter(ypha);
            }

            @Override
            public void onDataLoadError(String errorMessage) {

            }
        });
    }
}