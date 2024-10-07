package com.bucheon.yeoddadae;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapTapi;
import com.skt.Tmap.address_info.TMapAddressInfo;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MyShareParkInformationActivity extends AppCompatActivity {
    String loginId;
    String documentId;
    HashMap<String, Object> shareParkInfo;
    String naviEndPointName;

    ImageButton myShareParkInfoBackBtn;
    TextView myShareParkInfoIdContentTxt;
    TextView myShareParkInfoStatusContentTxt;
    TextView myShareParkInfoCancelReasonTxt;
    TextView myShareParkInfoCancelReasonContentTxt;
    View myShareParkInfoCancelReasonLine;
    TextView myShareParkInfoUpTimeContentTxt;
    TextView myShareParkInfoPriceContentTxt;
    TextView myShareParkInfoHourPerTxt;
    TextView myShareParkInfoPtTxt;
    TextView myShareParkInfoShareParkNewAddressContentTxt;
    TextView myShareParkInfoShareParkOldAddressContentTxt;
    TextView myShareParkInfoShareParkDetailaddressContentTxt;
    TextView myShareParkInfoImageTxt;
    TextView myShareParkInfoShareTimeContentTxt;
    TextView myShareParkInfoReservationTimeContentTxt;
    ImageButton myShareParkNaviBtn;
    ImageButton myShareParkInfoCancelBtn;
    TextView myShareParkInfoCancelTxt;
    ImageButton myShareParkInfoCalculateBtn;
    TextView myShareParkInfoCalculateTxt;
    ImageView imageView1;
    ImageView imageView2;
    ImageView imageView3;
    ImageView imageView4;
    LinearLayout imageContainer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_share_park_information);

        myShareParkInfoBackBtn = findViewById(R.id.myShareParkInfoBackBtn);
        myShareParkInfoIdContentTxt = findViewById(R.id.myShareParkInfoIdContentTxt);
        myShareParkInfoStatusContentTxt = findViewById(R.id.myShareParkInfoStatusContentTxt);
        myShareParkInfoCancelReasonTxt = findViewById(R.id.myShareParkInfoCancelReasonTxt);
        myShareParkInfoCancelReasonContentTxt = findViewById(R.id.myShareParkInfoCancelReasonContentTxt);
        myShareParkInfoCancelReasonLine = findViewById(R.id.myShareParkInfoCancelReasonLine);
        myShareParkInfoUpTimeContentTxt = findViewById(R.id.myShareParkInfoUpTimeContentTxt);
        myShareParkInfoPriceContentTxt = findViewById(R.id.myShareParkInfoPriceContentTxt);
        myShareParkInfoHourPerTxt = findViewById(R.id.myShareParkInfoHourPerTxt);
        myShareParkInfoPtTxt = findViewById(R.id.myShareParkInfoPtTxt);
        myShareParkInfoShareParkNewAddressContentTxt = findViewById(R.id.myShareParkInfoShareParkNewAddressContentTxt);
        myShareParkInfoShareParkOldAddressContentTxt = findViewById(R.id.myShareParkInfoShareParkOldAddressContentTxt);
        myShareParkInfoShareParkDetailaddressContentTxt = findViewById(R.id.myShareParkInfoShareParkDetailaddressContentTxt);
        myShareParkInfoImageTxt = findViewById(R.id.myShareParkInfoImageTxt);
        myShareParkInfoShareTimeContentTxt = findViewById(R.id.myShareParkInfoShareTimeContentTxt);
        myShareParkInfoReservationTimeContentTxt = findViewById(R.id.myShareParkInfoReservationTimeContentTxt);
        myShareParkNaviBtn = findViewById(R.id.myShareParkNaviBtn);
        myShareParkInfoCancelBtn = findViewById(R.id.myShareParkInfoCancelBtn);
        myShareParkInfoCancelTxt = findViewById(R.id.myShareParkInfoCancelTxt);
        myShareParkInfoCalculateBtn = findViewById(R.id.myShareParkInfoCalculateBtn);
        myShareParkInfoCalculateTxt = findViewById(R.id.myShareParkInfoCalculateTxt);
        imageContainer = findViewById(R.id.imageContainer);
        imageView1 = findViewById(R.id.imageView1);
        imageView2 = findViewById(R.id.imageView2);
        imageView3 = findViewById(R.id.imageView3);
        imageView4 = findViewById(R.id.imageView4);


        Intent inIntent = getIntent();
        loginId = inIntent.getStringExtra("id");
        documentId = inIntent.getStringExtra("documentId");

        loadImagesFromFirestore();

        FirestoreDatabase fd = new FirestoreDatabase();
        fd.calculateFreeSharePark(loginId, new OnFirestoreDataLoadedListener() {
            @Override
            public void onDataLoaded(Object data) {
                fd.loadShareParkInfo(documentId, new OnFirestoreDataLoadedListener() {
                    @Override
                    public void onDataLoaded(Object data) {
                        shareParkInfo = (HashMap<String, Object>) data;

                        TMapData tMapdata = new TMapData();
                        tMapdata.reverseGeocoding((double) shareParkInfo.get("lat"), (double) shareParkInfo.get("lon"), "A10", new TMapData.reverseGeocodingListenerCallback() {
                            @Override
                            public void onReverseGeocoding(TMapAddressInfo tMapAddressInfo) {
                                if (tMapAddressInfo != null) {
                                    String [] adrresses = tMapAddressInfo.strFullAddress.split(",");
                                    naviEndPointName = adrresses[2] + " : " + shareParkInfo.get("parkDetailAddress");
                                }
                            }
                        });

                        init();
                    }

                    @Override
                    public void onDataLoadError(String errorMessage) {
                        Log.d(TAG, errorMessage);
                        Toast.makeText(getApplicationContext(), "오류 발생", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }

            @Override
            public void onDataLoadError(String errorMessage) {
                Log.d(TAG, errorMessage);
                Toast.makeText(getApplicationContext(), "오류 발생", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        myShareParkInfoBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void loadImagesFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("sharePark").document(documentId);

        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    List<String> imageUrls = (List<String>) document.get("imageUrls"); // 이미지 URL 리스트

                    imageContainer.setVisibility(View.GONE); // 기본적으로 숨김

                    if (imageUrls != null && !imageUrls.isEmpty()) {
                        imageContainer.setVisibility(View.VISIBLE); // 이미지가 있을 경우 보이기

                        for (int i = 0; i < imageUrls.size(); i++) {
                            String imageUrl = imageUrls.get(i);
                            ImageView imageView = findViewById(getResources().getIdentifier("imageView" + (i + 1), "id", getPackageName()));
                            imageView.setVisibility(View.VISIBLE);
                            Glide.with(this)
                                    .load(imageUrl)
                                    .into(imageView);

                            imageView.setOnClickListener(v -> showImageDialog(imageUrl));
                        }
                    }
                } else {
                    Toast.makeText(MyShareParkInformationActivity.this, "문서가 존재하지 않습니다", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MyShareParkInformationActivity.this, "데이터를 가져오는 데 실패했습니다: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showImageDialog(String imageUrl) {
        Log.d("MyShareParkInformationActivity", "Image URL in dialog: " + imageUrl);
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_image_view2);

        ImageView fullScreenImageView = dialog.findViewById(R.id.fullScreenImageView);
        ImageButton closeButton = dialog.findViewById(R.id.closeButton);

        Glide.with(this)
                .load(imageUrl)
                .into(fullScreenImageView);


        closeButton.setOnClickListener(v -> dialog.dismiss());

        dialog.setCancelable(true);
        dialog.setOnDismissListener(dialogInterface -> dialog.dismiss());

        dialog.show();
    }
    void init() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                myShareParkInfoIdContentTxt.setText(documentId);

                Timestamp timestamp = (Timestamp) shareParkInfo.get("upTime");
                if (timestamp != null) {
                    Date date = timestamp.toDate();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일 HH:mm:ss", Locale.KOREA);
                    String dateString = sdf.format(date);
                    myShareParkInfoUpTimeContentTxt.setText(dateString);
                }

                if ((long) shareParkInfo.get("price") == 0) {
                    myShareParkInfoHourPerTxt.setVisibility(View.GONE);
                    myShareParkInfoPtTxt.setVisibility(View.GONE);
                    myShareParkInfoPriceContentTxt.setText("무료");
                }
                else {
                    myShareParkInfoHourPerTxt.setVisibility(View.VISIBLE);
                    myShareParkInfoPtTxt.setVisibility(View.VISIBLE);
                    String formattedYdPoint = NumberFormat.getNumberInstance(Locale.KOREA).format((long) shareParkInfo.get("price"));

                    myShareParkInfoPriceContentTxt.setText(formattedYdPoint);
                }

                TMapData tMapData = new TMapData();
                tMapData.reverseGeocoding((double) shareParkInfo.get("lat"), (double) shareParkInfo.get("lon"), "A10", new TMapData.reverseGeocodingListenerCallback() {
                    @Override
                    public void onReverseGeocoding(TMapAddressInfo tMapAddressInfo) {
                        if (tMapAddressInfo != null) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    String [] adrresses = tMapAddressInfo.strFullAddress.split(",");
                                    myShareParkInfoShareParkNewAddressContentTxt.setText(adrresses[2]);
                                    myShareParkInfoShareParkOldAddressContentTxt.setText(adrresses[1]);
                                }
                            });
                        }
                    }
                });

                myShareParkInfoShareParkDetailaddressContentTxt.setText((String) shareParkInfo.get("parkDetailAddress"));

                HashMap<String, ArrayList<String>> shareTime = (HashMap<String, ArrayList<String>>) shareParkInfo.get("time");
                List<String> keys = new ArrayList<>(shareTime.keySet());
                Collections.sort(keys);
                StringBuilder textBuilder = new StringBuilder();
                for (String key : keys) {
                    ArrayList<String> values = shareTime.get(key);

                    String year = key.substring(0, 4);
                    String month = key.substring(4, 6);
                    String day = key.substring(6);

                    String startTimeString = values.get(0).substring(0,2) + ":" + values.get(0).substring(2);
                    String endTimeString = values.get(1).substring(0,2) + ":" + values.get(1).substring(2);

                    textBuilder.append(year + "년 " + month + "월 " + day + "일 " + startTimeString + "부터 " + endTimeString + "까지\n");
                }
                String shareTimeString = "";
                if (!textBuilder.toString().isEmpty()) {
                    shareTimeString = textBuilder.toString().substring(0, textBuilder.length() - 1);
                }

                myShareParkInfoShareTimeContentTxt.setText(shareTimeString);

                FirestoreDatabase fd2 = new FirestoreDatabase();
                fd2.loadAnotherReservations(documentId, new OnFirestoreDataLoadedListener() {
                    @Override
                    public void onDataLoaded(Object data) {
                        HashMap<String,  HashMap<String, ArrayList<String>>> anotherReservations = (HashMap<String,  HashMap<String, ArrayList<String>>>) data;

                        ArrayList<String> al = new ArrayList<>();

                        for (Map.Entry<String, HashMap<String, ArrayList<String>>> entry : anotherReservations.entrySet()) {
                            HashMap<String, ArrayList<String>> shareTime = entry.getValue();

                            List<String> keys = new ArrayList<>(shareTime.keySet());
                            Collections.sort(keys);

                            for (String key : keys) {
                                ArrayList<String> values = shareTime.get(key);

                                String year = key.substring(0, 4);
                                String month = key.substring(4, 6);
                                String day = key.substring(6);

                                String startTimeString = values.get(0).substring(0, 2) + ":" + values.get(0).substring(2);
                                String endTimeString = values.get(1).substring(0, 2) + ":" + values.get(1).substring(2);

                                al.add(year + "년 " + month + "월 " + day + "일 " + startTimeString + "부터 " + endTimeString + "까지\n");
                            }
                        }
                        Collections.sort(al);

                        String tempString = "";
                        for (String s : al) {
                            tempString += s;

                        }

                        final String reservationsText;
                        if (!tempString.isEmpty()) {
                            reservationsText = tempString.substring(0, tempString.length() - 1);
                        }
                        else {
                            reservationsText = tempString;
                        }

                        boolean isApproval = (boolean) shareParkInfo.get("isApproval");
                        boolean isCancelled = (boolean) shareParkInfo.get("isCancelled");
                        boolean isCalculated = (boolean) shareParkInfo.get("isCalculated");
                        final boolean[] isThereReservation = new boolean[1];

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (reservationsText.isEmpty()) {
                                    isThereReservation[0] = false;
                                    myShareParkInfoReservationTimeContentTxt.setText("없음");
                                }
                                else {
                                    isThereReservation[0] = true;
                                    myShareParkInfoReservationTimeContentTxt.setText(reservationsText);
                                }
                            }
                        });

                        if (isCancelled) {
                            myShareParkInfoStatusContentTxt.setText("취소됨");
                            myShareParkInfoCancelBtn.setVisibility(View.GONE);
                            myShareParkInfoCancelTxt.setVisibility(View.GONE);
                            myShareParkInfoCalculateBtn.setVisibility(View.GONE);
                            myShareParkInfoCalculateTxt.setVisibility(View.GONE);
                            myShareParkInfoCancelReasonTxt.setVisibility(View.VISIBLE);
                            myShareParkInfoCancelReasonContentTxt.setVisibility(View.VISIBLE);
                            myShareParkInfoCancelReasonContentTxt.setText((String) shareParkInfo.get("cancelReason"));
                            myShareParkInfoCancelReasonLine.setVisibility(View.VISIBLE);
                        }
                        else if (!isApproval) {
                            myShareParkInfoCancelReasonTxt.setVisibility(View.GONE);
                            myShareParkInfoCancelReasonContentTxt.setVisibility(View.GONE);
                            myShareParkInfoCancelReasonLine.setVisibility(View.GONE);

                            Calendar ca = Calendar.getInstance();
                            int year = ca.get(Calendar.YEAR);
                            int month = ca.get(Calendar.MONTH) + 1;
                            int day = ca.get(Calendar.DAY_OF_MONTH);
                            int hour = ca.get(Calendar.HOUR_OF_DAY);
                            int minute = ca.get(Calendar.MINUTE);
                            String nowString = "";
                            nowString += year;
                            if (month < 10) {
                                nowString += "0";
                            }
                            nowString += month;
                            if (day < 10) {
                                nowString += "0";
                            }
                            nowString += day;
                            if (hour < 10) {
                                nowString += "0";
                            }
                            nowString += hour;
                            if (minute < 10) {
                                nowString += "0";
                            }
                            nowString += minute;

                            HashMap<String, ArrayList<String>> shareTIme = (HashMap<String, ArrayList<String>>) shareParkInfo.get("time");
                            List<String> sortedKeys = new ArrayList<>(shareTIme.keySet());
                            Collections.sort(sortedKeys);
                            String firstTime = sortedKeys.get(0) + shareTIme.get(sortedKeys.get(0)).get(0);

                            Log.d(TAG, nowString);
                            Log.d(TAG, firstTime);

                            if (nowString.compareTo(firstTime) > 0) {
                                myShareParkInfoStatusContentTxt.setText("승인 실패");
                                myShareParkInfoCancelBtn.setVisibility(View.GONE);
                                myShareParkInfoCancelTxt.setVisibility(View.GONE);
                                myShareParkInfoCalculateBtn.setVisibility(View.GONE);
                                myShareParkInfoCalculateTxt.setVisibility(View.GONE);
                            }
                            else {
                                myShareParkInfoStatusContentTxt.setText("승인 대기 중");
                                myShareParkInfoCancelBtn.setVisibility(View.VISIBLE);
                                myShareParkInfoCancelTxt.setVisibility(View.VISIBLE);
                                myShareParkInfoCalculateBtn.setVisibility(View.GONE);
                                myShareParkInfoCalculateTxt.setVisibility(View.GONE);
                            }
                        }
                        else {
                            myShareParkInfoCancelReasonTxt.setVisibility(View.GONE);
                            myShareParkInfoCancelReasonContentTxt.setVisibility(View.GONE);
                            myShareParkInfoCancelReasonLine.setVisibility(View.GONE);

                            Calendar ca = Calendar.getInstance();
                            int year = ca.get(Calendar.YEAR);
                            int month = ca.get(Calendar.MONTH) + 1;
                            int day = ca.get(Calendar.DAY_OF_MONTH);
                            int hour = ca.get(Calendar.HOUR_OF_DAY);
                            int minute = ca.get(Calendar.MINUTE);
                            String nowString = "";
                            nowString += year;
                            if (month < 10) {
                                nowString += "0";
                            }
                            nowString += month;
                            if (day < 10) {
                                nowString += "0";
                            }
                            nowString += day;
                            if (hour < 10) {
                                nowString += "0";
                            }
                            nowString += hour;
                            if (minute < 10) {
                                nowString += "0";
                            }
                            nowString += minute;

                            HashMap<String, ArrayList<String>> shareTIme = (HashMap<String, ArrayList<String>>) shareParkInfo.get("time");
                            List<String> sortedKeys = new ArrayList<>(shareTIme.keySet());
                            Collections.sort(sortedKeys);
                            String firstTime = sortedKeys.get(0) + shareTIme.get(sortedKeys.get(0)).get(0);
                            String endTime = sortedKeys.get(sortedKeys.size() - 1) + shareTIme.get(sortedKeys.get(sortedKeys.size() - 1)).get(1);

                            Log.d(TAG, nowString);
                            Log.d(TAG, firstTime);
                            Log.d(TAG, endTime);

                            if (nowString.compareTo(firstTime) < 0) {
                                if (isThereReservation[0]) {
                                    myShareParkInfoStatusContentTxt.setText("승인됨. 공유 예정. 예약 있음");
                                    myShareParkInfoCancelBtn.setVisibility(View.GONE);
                                    myShareParkInfoCancelTxt.setVisibility(View.GONE);
                                    myShareParkInfoCalculateBtn.setVisibility(View.GONE);
                                    myShareParkInfoCalculateTxt.setVisibility(View.GONE);
                                }
                                else {
                                    myShareParkInfoStatusContentTxt.setText("승인됨. 공유 예정");
                                    myShareParkInfoCancelBtn.setVisibility(View.VISIBLE);
                                    myShareParkInfoCancelTxt.setVisibility(View.VISIBLE);
                                    myShareParkInfoCalculateBtn.setVisibility(View.GONE);
                                    myShareParkInfoCalculateTxt.setVisibility(View.GONE);
                                }
                            } else if (nowString.compareTo(endTime) <= 0) {
                                if (isThereReservation[0]) {
                                    myShareParkInfoStatusContentTxt.setText("승인됨. 공유 중. 예약 있음");
                                    myShareParkInfoCancelBtn.setVisibility(View.GONE);
                                    myShareParkInfoCancelTxt.setVisibility(View.GONE);
                                    myShareParkInfoCalculateBtn.setVisibility(View.GONE);
                                    myShareParkInfoCalculateTxt.setVisibility(View.GONE);
                                }
                                else {
                                    myShareParkInfoStatusContentTxt.setText("승인됨. 공유 중. 예약 없음");
                                    myShareParkInfoCancelBtn.setVisibility(View.VISIBLE);
                                    myShareParkInfoCancelTxt.setVisibility(View.VISIBLE);
                                    myShareParkInfoCalculateBtn.setVisibility(View.GONE);
                                    myShareParkInfoCalculateTxt.setVisibility(View.GONE);
                                }
                            }
                            else {
                                if(isCalculated) {
                                    myShareParkInfoStatusContentTxt.setText("공유 종료. 정산 완료");
                                    myShareParkInfoCancelBtn.setVisibility(View.GONE);
                                    myShareParkInfoCancelTxt.setVisibility(View.GONE);
                                    myShareParkInfoCalculateBtn.setVisibility(View.GONE);
                                    myShareParkInfoCalculateTxt.setVisibility(View.GONE);
                                }
                                else {
                                    myShareParkInfoStatusContentTxt.setText("공유 종료. 정산 대기 중");
                                    myShareParkInfoCancelBtn.setVisibility(View.GONE);
                                    myShareParkInfoCancelTxt.setVisibility(View.GONE);
                                    myShareParkInfoCalculateBtn.setVisibility(View.VISIBLE);
                                    myShareParkInfoCalculateTxt.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                    }

                    @Override
                    public void onDataLoadError(String errorMessage) {
                        Log.d(TAG, errorMessage);
                        Toast.makeText(getApplicationContext(), "오류 발생", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });

                myShareParkNaviBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TMapTapi tt = new TMapTapi(MyShareParkInformationActivity.this);
                        boolean isTmapApp = tt.isTmapApplicationInstalled();
                        if (isTmapApp) {
                            if (naviEndPointName != null) {
                                tt.invokeRoute(naviEndPointName, (float) ((double) shareParkInfo.get("lon")), (float) ((double) shareParkInfo.get("lat")));
                            }
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "TMAP이 설치되어 있지 않습니다", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                myShareParkInfoCancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MyShareParkInformationActivity.this);
                        builder.setTitle("공유 취소 확인");
                        builder.setMessage("정말 취소하시겠습니까\n(되돌릴 수 없습니다)");
                        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                fd2.cancelSharePark(loginId, documentId, "공유자가 취소", new OnFirestoreDataLoadedListener() {
                                    @Override
                                    public void onDataLoaded(Object data) {
                                        Toast.makeText(getApplicationContext(), "공유 취소되었습니다", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }

                                    @Override
                                    public void onDataLoadError(String errorMessage) {
                                        Log.d(TAG, errorMessage);
                                        Toast.makeText(getApplicationContext(), "오류 발생", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
                        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();

                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(MyShareParkInformationActivity.this, R.color.sub));
                        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(MyShareParkInformationActivity.this, R.color.disable));
                    }
                });



                myShareParkInfoCalculateBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        fd2.calculateShareParkPrice(loginId, documentId, new OnFirestoreDataLoadedListener() {
                            @Override
                            public void onDataLoaded(Object data) {
                                Toast.makeText(getApplicationContext(), "정산 완료되었습니다", Toast.LENGTH_SHORT).show();
                                finish();
                            }

                            @Override
                            public void onDataLoadError(String errorMessage) {
                                Log.d(TAG, errorMessage);
                                Toast.makeText(getApplicationContext(), "오류 발생", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

            }
        });
    }
}
