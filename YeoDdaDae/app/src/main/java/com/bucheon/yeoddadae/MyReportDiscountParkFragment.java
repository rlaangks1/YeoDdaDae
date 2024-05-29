package com.bucheon.yeoddadae;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.HashMap;

public class MyReportDiscountParkFragment extends Fragment {
    String loginId;

    Button myReportBackBtn;
    ListView myReportListView;
    Button toAddReportBtn;

    public MyReportDiscountParkFragment (String id){
        this.loginId = id;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_report_discount_park, container, false);

        myReportBackBtn = view.findViewById(R.id.myReportBackBtn);
        myReportListView = view.findViewById(R.id.myReportListView);
        toAddReportBtn = view.findViewById(R.id.toAddReportBtn);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FirestoreDatabase fd = new FirestoreDatabase();
        ReportDiscountParkAdapter ra = new ReportDiscountParkAdapter(getActivity());
        myReportListView.setAdapter(ra);

        fd.loadMyReports(loginId, new OnFirestoreDataLoadedListener() {
            @Override
            public void onDataLoaded(Object data) {
                ArrayList<HashMap<String, Object>> myReports = (ArrayList<HashMap<String, Object>>) data;

                for (HashMap<String, Object> oneReport : myReports) {
                    String reporterId = (String) oneReport.get("reporterId");
                    String parkName = (String) oneReport.get("parkName");
                    String condition = (String) oneReport.get("parkCondition");
                    long discount = (long) oneReport.get("parkDiscount");
                    long ratePerfectCount = (long) oneReport.get("ratePerfectCount");
                    long rateMistakeCount = (long) oneReport.get("rateMistakeCount");
                    long rateWrongCount = (long) oneReport.get("rateWrongCount");
                    boolean isCancelled = (boolean) oneReport.get("isCancelled");
                    boolean isApproval = (boolean) oneReport.get("isApproval");
                    Timestamp upTime = (Timestamp) oneReport.get("upTime");
                    String poiID = (String) oneReport.get("poiID");
                    String documentId = (String) oneReport.get("documentId");

                    ra.addItem(new ReportDiscountParkItem(reporterId, parkName, condition, discount, ratePerfectCount, rateMistakeCount, rateWrongCount, isCancelled, isApproval, upTime, poiID, documentId));
                }
            }

            @Override
            public void onDataLoadError(String errorMessage) {
                Log.d(TAG, errorMessage);
                Toast.makeText(getContext(), "오류 발생", Toast.LENGTH_SHORT).show();
            }
        });

        myReportListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent toReportDiscountParkInformationIntent = new Intent(getContext(), ReportDiscountParkInformationActivity.class);
                toReportDiscountParkInformationIntent.putExtra("id", loginId);
                toReportDiscountParkInformationIntent.putExtra("documentId", ((ReportDiscountParkItem) ra.getItem(position)).getDocumentId());
                startActivity(toReportDiscountParkInformationIntent);
            }
        });

        myReportBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        toAddReportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addReportIntent = new Intent(getContext(), AddReportDiscountParkActivity.class);
                addReportIntent.putExtra("loginId", loginId);
                startActivity(addReportIntent);
            }
        });
    }
}