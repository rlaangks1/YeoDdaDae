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

public class MyShareParkFragment extends Fragment {
    MainActivity ma;
    String loginId;
    ShareParkAdapter spa;

    ImageButton myShareParkAddBtn;
    EditText searchContentEditTxt;
    ImageButton searchTxtClearBtn;
    ImageButton searchBtn;
    ListView myShareParkListView;
    TextView myShareParkNoTxt;

    public MyShareParkFragment(String id) {
        this.loginId = id;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_share_park, container, false);

        myShareParkAddBtn = view.findViewById(R.id.myShareParkAddBtn);
        searchContentEditTxt = view.findViewById(R.id.searchContentEditTxt);
        searchTxtClearBtn = view.findViewById(R.id.searchTxtClearBtn);
        searchBtn = view.findViewById(R.id.searchBtn);
        myShareParkListView = view.findViewById(R.id.myShareParkListView);
        myShareParkNoTxt = view.findViewById(R.id.myShareParkNoTxt);

        ma = (MainActivity) getActivity();

        spa = new ShareParkAdapter(getActivity());

        myShareParkAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareParkIntent = new Intent(getActivity(), ShareParkActivity.class);
                shareParkIntent.putExtra("loginId", loginId);
                startActivity(shareParkIntent);
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
                getShareParks();
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
                    spa.loadSavedItems();
                    int searchCount = spa.searchSharePark(replaceNewlinesAndTrim(searchContentEditTxt));

                    ma.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (searchCount == 0) {
                                myShareParkListView.setVisibility(View.GONE);
                                myShareParkNoTxt.setVisibility(View.VISIBLE);
                            }
                            else {
                                myShareParkListView.setVisibility(View.VISIBLE);
                                myShareParkNoTxt.setVisibility(View.GONE);
                            }
                        }
                    });
                }
                else {
                    Toast.makeText(ma.getApplicationContext(), "검색어를 입력해주세요", Toast.LENGTH_SHORT).show();
                }
            }
        });

        myShareParkListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent toMyShareParkInformationIntent = new Intent(getActivity(), MyShareParkInformationActivity.class);
                toMyShareParkInformationIntent.putExtra("id", loginId);
                toMyShareParkInformationIntent.putExtra("documentId", ((ShareParkItem) spa.getItem(position)).getDocumentId());
                startActivity(toMyShareParkInformationIntent);
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        getShareParks();
    }

    public void getShareParks() {
        spa.clear();
        searchContentEditTxt.setText("");

        FirestoreDatabase fd = new FirestoreDatabase();
        fd.calculateFreeSharePark(loginId, new OnFirestoreDataLoadedListener() {
            @Override
            public void onDataLoaded(Object data) {
                FirestoreDatabase fd2 = new FirestoreDatabase();
                fd2.loadMyShareParks(loginId, new OnFirestoreDataLoadedListener() {
                    @Override
                    public void onDataLoaded(Object data) {
                        ArrayList<HashMap<String, Object>> myShareParks = (ArrayList<HashMap<String, Object>>) data;

                        for (HashMap<String, Object> oneSharePark : myShareParks) {
                            double lat = (double) oneSharePark.get("lat");
                            double lon = (double) oneSharePark.get("lon");
                            String parkDetailAddress = (String) oneSharePark.get("parkDetailAddress");
                            boolean isApproval = (boolean) oneSharePark.get("isApproval");
                            boolean isCancelled = (boolean) oneSharePark.get("isCancelled");
                            boolean isCalculated = (boolean) oneSharePark.get("isCalculated");
                            long price = (long) oneSharePark.get("price");
                            HashMap<String, ArrayList<String>> time = (HashMap<String, ArrayList<String>>) oneSharePark.get("time");
                            Timestamp upTime = (Timestamp) oneSharePark.get("upTime");
                            String documentId = (String) oneSharePark.get("documentId");

                            spa.addItem(new ShareParkItem(null, lat, lon, parkDetailAddress, isApproval, isCancelled, isCalculated, price, time, upTime, documentId));
                        }

                        if (ma != null) {
                            ma.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    myShareParkListView.setAdapter(spa);

                                    if (spa.getCount() == 0) {
                                        myShareParkListView.setVisibility(View.GONE);
                                        myShareParkNoTxt.setVisibility(View.VISIBLE);
                                    }
                                    else {
                                        for (int i = 0; i < spa.getCount(); i++) {
                                            spa.getView(i, null, myShareParkListView);
                                        }

                                        myShareParkListView.setVisibility(View.VISIBLE);
                                        myShareParkNoTxt.setVisibility(View.GONE);
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