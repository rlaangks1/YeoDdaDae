package com.bucheon.yeoddadae;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.HashMap;

public class MyReportDiscountParkFragment extends Fragment {
    MainActivity ma;
    String loginId;
    ReportDiscountParkAdapter rdpa;

    ImageButton toAddReportBtn;
    EditText searchContentEditTxt;
    ImageButton searchTxtClearBtn;
    ImageButton searchBtn;
    ListView myReportListView;
    TextView myReportNoTxt;


    public MyReportDiscountParkFragment (String id){
        this.loginId = id;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_report_discount_park, container, false);

        myReportListView = view.findViewById(R.id.myReportListView);
        searchContentEditTxt = view.findViewById(R.id.searchContentEditTxt);
        searchTxtClearBtn = view.findViewById(R.id.searchTxtClearBtn);
        searchBtn = view.findViewById(R.id.searchBtn);
        toAddReportBtn = view.findViewById(R.id.toAddReportBtn);
        myReportNoTxt = view.findViewById(R.id.myReportNoTxt);

        ma = (MainActivity) getActivity();

        rdpa = new ReportDiscountParkAdapter(getActivity());

        toAddReportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addReportIntent = new Intent(getActivity(), AddReportDiscountParkActivity.class);
                addReportIntent.putExtra("loginId", loginId);
                startActivity(addReportIntent);
            }
        });

        searchContentEditTxt.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    searchBtn.callOnClick();
                }

                return false;
            }
        });

        searchContentEditTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (searchContentEditTxt.getText().toString().isEmpty()) {
                    searchTxtClearBtn.setVisibility(View.GONE);
                }
                else {
                    searchTxtClearBtn.setVisibility(View.VISIBLE);
                }
            }
        });

        searchTxtClearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchContentEditTxt.setText("");
                getReportDiscountParks();
            }
        });

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) ma.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(searchContentEditTxt.getWindowToken(), 0);
                }

                if (!replaceNewlinesAndTrim(searchContentEditTxt).isEmpty()) {
                    rdpa.loadSavedItems();
                    int searchCount = rdpa.searchReportDiscountPark(replaceNewlinesAndTrim(searchContentEditTxt));

                    ma.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (searchCount == 0) {
                                myReportListView.setVisibility(View.GONE);
                                myReportNoTxt.setVisibility(View.VISIBLE);
                            }
                            else {
                                myReportListView.setVisibility(View.VISIBLE);
                                myReportNoTxt.setVisibility(View.GONE);
                            }
                        }
                    });
                }
                else {
                    Toast.makeText(ma.getApplicationContext(), "검색어를 입력해주세요", Toast.LENGTH_SHORT).show();
                }
            }
        });

        myReportListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent toReportDiscountParkInformationIntent = new Intent(getActivity(), ReportDiscountParkInformationActivity.class);
                toReportDiscountParkInformationIntent.putExtra("id", loginId);
                toReportDiscountParkInformationIntent.putExtra("documentId", ((ReportDiscountParkItem) rdpa.getItem(position)).getDocumentId());
                startActivity(toReportDiscountParkInformationIntent);
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        getReportDiscountParks();
    }

    public void getReportDiscountParks() {
        rdpa.clear();
        searchContentEditTxt.setText("");

        FirestoreDatabase fd = new FirestoreDatabase();
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

                    rdpa.addItem(new ReportDiscountParkItem(reporterId, parkName, condition, discount, ratePerfectCount, rateMistakeCount, rateWrongCount, isCancelled, isApproval, upTime, poiID, documentId));
                }

                if (ma != null) {
                    ma.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            myReportListView.setAdapter(rdpa);

                            if (rdpa.getCount() == 0) {
                                myReportListView.setVisibility(View.GONE);
                                myReportNoTxt.setVisibility(View.VISIBLE);
                            }
                            else {
                                for (int i = 0; i < rdpa.getCount(); i++) {
                                    rdpa.getView(i, null, myReportListView);
                                }

                                myReportListView.setVisibility(View.VISIBLE);
                                myReportNoTxt.setVisibility(View.GONE);
                            }
                        }
                    });
                }
            }

            @Override
            public void onDataLoadError(String errorMessage) {
                Log.d(TAG, errorMessage);
                Toast.makeText(getContext(), "오류 발생", Toast.LENGTH_SHORT).show();
            }
        });
    }

    String replaceNewlinesAndTrim(EditText et) {
        return et.getText().toString().replaceAll("\\n", " ").trim();
    }
}