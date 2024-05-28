package com.bucheon.yeoddadae;

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

import com.google.android.exoplayer2.ExoPlayerLibraryInfo;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.HashMap;

public class MyReservationFragment extends Fragment {

    String loginId;

    Button myReservationBackBtn;
    ListView myReservationListView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_reservation, container, false);

        // UI 컴포넌트 초기화
        myReservationBackBtn = view.findViewById(R.id.myReservationBackBtn);
        myReservationListView = view.findViewById(R.id.myReservationListView);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        // 액티비티에서 인텐트로 전달받은 로그인 아이디 가져오기
        Intent inIntent = requireActivity().getIntent();
        loginId = inIntent.getStringExtra("loginId");

        // 예약 목록 불러오기
        FirestoreDatabase fd = new FirestoreDatabase();
        ReservationAdapter ra = new ReservationAdapter(requireActivity());
        myReservationListView.setAdapter(ra);

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
            }

            @Override
            public void onDataLoadError(String errorMessage) {
                Log.d(ExoPlayerLibraryInfo.TAG, errorMessage);
                Toast.makeText(requireContext(), "오류 발생", Toast.LENGTH_SHORT).show();
                requireActivity().finish();
            }
        });

        // 리스트뷰 아이템 클릭 이벤트 처리
        myReservationListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent toReservationInformationActivityIntent = new Intent(requireContext(), ReservationInformationActivity.class);
                toReservationInformationActivityIntent.putExtra("id", loginId);
                toReservationInformationActivityIntent.putExtra("documentId", ((ReservationItem) ra.getItem(position)).getDocumentId());
                startActivity(toReservationInformationActivityIntent);
            }
        });

        // 뒤로 가기 버튼 클릭 이벤트 처리
        myReservationBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().finish();
            }
        });
    }
}