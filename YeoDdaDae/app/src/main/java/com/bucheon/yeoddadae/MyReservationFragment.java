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

public class MyReservationFragment extends Fragment {
    MainActivity ma;
    String loginId;
    ReservationAdapter ra;

    EditText searchContentEditTxt;
    ImageButton searchTxtClearBtn;
    ImageButton searchBtn;
    ListView myReservationListView;
    TextView myReservationNoTxt;

    public MyReservationFragment (String id) {
        this.loginId = id;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_reservation, container, false);

        searchContentEditTxt = view.findViewById(R.id.searchContentEditTxt);
        searchTxtClearBtn = view.findViewById(R.id.searchTxtClearBtn);
        searchBtn = view.findViewById(R.id.searchBtn);
        myReservationListView = view.findViewById(R.id.myReservationListView);
        myReservationNoTxt = view.findViewById(R.id.myReservationNoTxt);

        ma = (MainActivity) getActivity();

        ra = new ReservationAdapter(getActivity());

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
                getReservations();
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
                    ra.loadSavedItems();
                    int searchCount = ra.searchReservation(replaceNewlinesAndTrim(searchContentEditTxt));

                    ma.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (searchCount == 0) {
                                myReservationListView.setVisibility(View.GONE);
                                myReservationNoTxt.setVisibility(View.VISIBLE);
                            }
                            else {
                                myReservationListView.setVisibility(View.VISIBLE);
                                myReservationNoTxt.setVisibility(View.GONE);
                            }
                        }
                    });
                }
                else {
                    Toast.makeText(ma.getApplicationContext(), "검색어를 입력해주세요", Toast.LENGTH_SHORT).show();
                }
            }
        });

        myReservationListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (ra.getItem(position) != null) {
                    Intent toReservationInformationActivityIntent = new Intent(getActivity(), ReservationInformationActivity.class);
                    toReservationInformationActivityIntent.putExtra("id", loginId);
                    toReservationInformationActivityIntent.putExtra("documentId", ((ReservationItem) ra.getItem(position)).getDocumentId());
                    startActivity(toReservationInformationActivityIntent);
                }
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        getReservations();
    }

    public void getReservations() {
        ra.clear();
        searchContentEditTxt.setText("");

        FirestoreDatabase fd = new FirestoreDatabase();
        fd.loadMyReservations(loginId, new OnFirestoreDataLoadedListener() {
            @Override
            public void onDataLoaded(Object data) {
                ArrayList<HashMap<String, Object>> myReservations = (ArrayList<HashMap<String, Object>>) data;

                for (HashMap<String, Object> oneReservation : myReservations) {
                    String id = (String) oneReservation.get("id");
                    boolean isCancelled = (boolean) oneReservation.get("isCancelled");
                    String shareParkDocumentName = (String) oneReservation.get("shareParkDocumentName");
                    HashMap<String, ArrayList<String>> reservationTime = (HashMap<String, ArrayList<String>>) oneReservation.get("time");
                    Timestamp upTime = (Timestamp) oneReservation.get("upTime");
                    String documentId = (String) oneReservation.get("documentId");

                    ra.addItem(new ReservationItem(id, isCancelled, shareParkDocumentName, reservationTime, upTime, documentId));
                }

                if (ma != null) {
                    ma.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            myReservationListView.setAdapter(ra);

                            if (ra.getCount() == 0) {
                                myReservationListView.setVisibility(View.GONE);
                                myReservationNoTxt.setVisibility(View.VISIBLE);
                            }
                            else {
                                for (int i = 0; i < ra.getCount(); i++) {
                                    ra.getView(i, null, myReservationListView);
                                }

                                myReservationListView.setVisibility(View.VISIBLE);
                                myReservationNoTxt.setVisibility(View.GONE);
                            }
                        }
                    });
                }
            }

            @Override
            public void onDataLoadError(String errorMessage) {
                Log.d(TAG, errorMessage);
                Toast.makeText(requireContext(), "오류 발생", Toast.LENGTH_SHORT).show();
            }
        });
    }

    String replaceNewlinesAndTrim(EditText et) {
        return et.getText().toString().replaceAll("\\n", " ").trim();
    }
}