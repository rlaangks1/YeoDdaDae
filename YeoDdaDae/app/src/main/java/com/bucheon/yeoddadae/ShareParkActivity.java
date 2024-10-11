package com.bucheon.yeoddadae;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.format.ArrayWeekDayFormatter;
import com.prolificinteractive.materialcalendarview.format.TitleFormatter;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.address_info.TMapAddressInfo;

import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShareParkActivity extends AppCompatActivity {
    final int GPS_INTENT_REQUEST_CODE = 1;
    final int PICK_IMAGE_REQUEST_CODE = 2;
    String loginId;
    double lat = 0;
    double lon = 0;
    ArrayList<Uri> imageUris = new ArrayList<>();
    ImageDialog id;
    StorageReference mStorageRef;
    TimeAdapter ta;

    ImageButton shareParkBackBtn;
    TextView parkNewAddressContentTxt;
    TextView parkOldAddressContentTxt;
    ImageButton gpsBtn;
    EditText parkDetailAddressEditTxt;
    EditText sharerNameEditTxt;
    EditText sharerPhoneEditTxt;
    EditText sharerEmailEditTxt;
    EditText sharerRelationEditTxt;
    ImageButton openImageBtn;
    TextView imageCountTxt;
    ImageView selectedImageView1;
    ImageButton cancelImageBtn1;
    ImageView selectedImageView2;
    ImageButton cancelImageBtn2;
    ImageView selectedImageView3;
    ImageButton cancelImageBtn3;
    ImageView selectedImageView4;
    ImageButton cancelImageBtn4;
    EditText parkPriceEditTxt;
    ImageButton resetBtn;
    MaterialCalendarView parkDateCalendar;
    ListView parkTimeListView;
    ImageButton registrationBtn;

    public ListView getParkTimeListView() {
        return parkTimeListView;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_park);

        shareParkBackBtn = findViewById(R.id.shareParkBackBtn);
        parkNewAddressContentTxt = findViewById(R.id.parkNewAddressContentTxt);
        parkOldAddressContentTxt = findViewById(R.id.parkOldAddressContentTxt);
        gpsBtn = findViewById(R.id.gpsBtn);
        parkDetailAddressEditTxt = findViewById(R.id.parkDetailAddressEditTxt);
        sharerNameEditTxt = findViewById(R.id.sharerNameEditTxt);
        sharerPhoneEditTxt = findViewById(R.id.sharerPhoneEditTxt);
        sharerEmailEditTxt = findViewById(R.id.sharerEmailEditTxt);
        sharerRelationEditTxt = findViewById(R.id.sharerRelationEditTxt);
        openImageBtn = findViewById(R.id.openImageBtn);
        imageCountTxt = findViewById(R.id.imageCountTxt);
        selectedImageView1 = findViewById(R.id.selectedImageView1);
        cancelImageBtn1 = findViewById(R.id.cancelImageBtn1);
        selectedImageView2 = findViewById(R.id.selectedImageView2);
        cancelImageBtn2 = findViewById(R.id.cancelImageBtn2);
        selectedImageView3 = findViewById(R.id.selectedImageView3);
        cancelImageBtn3 = findViewById(R.id.cancelImageBtn3);
        selectedImageView4 = findViewById(R.id.selectedImageView4);
        cancelImageBtn4 = findViewById(R.id.cancelImageBtn4);
        parkPriceEditTxt = findViewById(R.id.parkPriceEditTxt);
        resetBtn = findViewById(R.id.resetBtn);
        parkDateCalendar = findViewById(R.id.parkDateCalendar);
        parkTimeListView = findViewById(R.id.parkTimeListView);
        registrationBtn = findViewById(R.id.registrationBtn);

        Intent inIntent = getIntent();
        loginId = inIntent.getStringExtra("loginId");

        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");

        ta = new TimeAdapter(ShareParkActivity.this);
        parkTimeListView.setAdapter(ta);

        gpsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gpsIntent = new Intent(getApplicationContext(), GpsCertificationActivity.class);
                startActivityForResult(gpsIntent, GPS_INTENT_REQUEST_CODE);
            }
        });

        openImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageChooser();
            }
        });

        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (CalendarDay element : ta.clear()) {
                    parkDateCalendar.setDateSelected(element, false);
                }
                setHeightOfParkTimeListView();
            }
        });

        parkDateCalendar.setWeekDayFormatter(new ArrayWeekDayFormatter(getResources().getTextArray(R.array.custom_weekdays)));
        parkDateCalendar.setTitleFormatter(new TitleFormatter() {
            @Override
            public CharSequence format(CalendarDay day) {
                // CalendarDay라는 클래스는 LocalDate 클래스를 기반으로 만들어진 클래스다
                // 때문에 MaterialCalendarView에서 연/월 보여주기를 커스텀하려면 CalendarDay 객체의 getDate()로 연/월을 구한 다음 LocalDate 객체에 넣어서
                // LocalDate로 변환하는 처리가 필요하다
                LocalDate inputText = day.getDate();
                String[] calendarHeaderElements = inputText.toString().split("-");
                StringBuilder calendarHeaderBuilder = new StringBuilder();
                calendarHeaderBuilder.append(calendarHeaderElements[0])
                        .append(" ")
                        .append(calendarHeaderElements[1]);
                return calendarHeaderBuilder.toString();
            }
        });

        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        CalendarDay currentCalendarDay = CalendarDay.from(currentYear, currentMonth + 1, currentDay);

        calendar.add(Calendar.DAY_OF_MONTH, 99);
        int futureYear = calendar.get(Calendar.YEAR);
        int futureMonth = calendar.get(Calendar.MONTH);
        int futureDay = calendar.get(Calendar.DAY_OF_MONTH);
        CalendarDay futureCalendarDay = CalendarDay.from(futureYear, futureMonth + 1, futureDay);

        parkDateCalendar.state().edit()
                .setMinimumDate(currentCalendarDay)
                .setMaximumDate(futureCalendarDay)
                .commit();

        parkDateCalendar.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                if (selected) {
                    Log.d(TAG, "선택한 날짜: " + date.getYear() + "년 " + date.getMonth() + "월 " + date.getDay() + "일");
                    ta.addItem(new TimeItem(date));
                    ta.sortByDate();
                    setHeightOfParkTimeListView();
                } else {
                    Log.d(TAG, "선택 해제 한 날짜: " + date.getYear() + "년 " + date.getMonth() + "월 " + date.getDay() + "일");
                    ta.removeItem(ta.findItem(date));
                    setHeightOfParkTimeListView();
                }
            }
        });

        shareParkBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        registrationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String parkDetailAddress = replaceNewlinesAndTrim(parkDetailAddressEditTxt);
                String ownerName = replaceNewlinesAndTrim(sharerNameEditTxt);
                String ownerPhone = replaceNewlinesAndTrim(sharerPhoneEditTxt);
                String ownerEmail = replaceNewlinesAndTrim(sharerEmailEditTxt);
                String ownerParkingRelation = replaceNewlinesAndTrim(sharerRelationEditTxt);
                String priceText = replaceNewlinesAndTrim(parkPriceEditTxt);

                int price;

                if (lat == 0 || lon == 0) {
                    Toast.makeText(getApplicationContext(), "GPS로 주소를 찾으세요", Toast.LENGTH_SHORT).show();
                    return;
                } else if (parkDetailAddress.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "상세주소를 입력하세요", Toast.LENGTH_SHORT).show();
                    return;
                } else if (ownerName.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "공유자명을 입력하세요", Toast.LENGTH_SHORT).show();
                    return;
                } else if (ownerPhone.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "전화번호를 입력하세요", Toast.LENGTH_SHORT).show();
                    return;
                } else if (!isValidPhoneNumber(ownerPhone)) {
                    Toast.makeText(getApplicationContext(), "유효하지 않은 전화번호입니다", Toast.LENGTH_SHORT).show();
                    return;
                } else if (ownerEmail.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "이메일을 입력하세요", Toast.LENGTH_SHORT).show();
                    return;
                } else if (!isValidEmail(ownerEmail)) {
                    Toast.makeText(getApplicationContext(), "유효하지 않은 이메일 형식입니다", Toast.LENGTH_SHORT).show();
                    return;
                } else if (ownerParkingRelation.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "주차장과의 관계를 입력하세요", Toast.LENGTH_SHORT).show();
                    return;
                } else if (priceText.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "가격을 설정하세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    price = Integer.parseInt(priceText);
                } catch (NumberFormatException e) {
                    Toast.makeText(getApplicationContext(), "가격을 숫자로 입력하세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (ta.getAllTime() == null) {
                    Toast.makeText(getApplicationContext(), "시간설정이 잘못되었습니다", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (isShareTimeAfterNow()) {
                    Toast.makeText(getApplicationContext(), "현재시각 이후로 공유하세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                HashMap<String, Object> hm = new HashMap<>();
                hm.put("ownerId", loginId);
                hm.put("lat", lat);
                hm.put("lon", lon);
                hm.put("time", ta.getAllTime());
                hm.put("parkDetailAddress", parkDetailAddress);
                hm.put("ownerName", ownerName);
                hm.put("ownerPhone", ownerPhone);
                hm.put("ownerEmail", ownerEmail);
                hm.put("ownerParkingRelation", ownerParkingRelation);
                hm.put("price", price);
                hm.put("upTime", FieldValue.serverTimestamp());
                hm.put("isApproval", false);
                hm.put("isCancelled", false);
                hm.put("cancelReason", null);
                hm.put("isCalculated", false);

                uploadImageAndRegister(hm);
            }
        });
    }

    void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");  // 이미지만 선택 가능
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "사진을 선택하세요"), PICK_IMAGE_REQUEST_CODE);
    }

    void uploadImageAndRegister(HashMap<String, Object> hm) {
        if (!imageUris.isEmpty()) {
            ArrayList<String> imageUrlsList = new ArrayList<>();
            int uploadCount [] = {0};

            for (Uri imageUri : imageUris) {
                StorageReference fileRef = mStorageRef.child(System.currentTimeMillis() + ".jpg");

                fileRef.putFile(imageUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String imageUrl = uri.toString();
                                        imageUrlsList.add(imageUrl);

                                        uploadCount [0] ++;

                                        if (uploadCount [0] == imageUris.size()) {
                                            hm.put("imageUrls", imageUrlsList);
                                            registerUser(hm);
                                        }
                                    }
                                });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "이미지 업로드 실패: " + e.getMessage());
                                Toast.makeText(getApplicationContext(), "이미지 업로드 중 오류 발생", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }
        else {
            registerUser(hm);
        }
    }

    private void registerUser(HashMap<String, Object> hm) {
        FirestoreDatabase fd = new FirestoreDatabase();
        fd.insertData("sharePark", hm, new OnFirestoreDataLoadedListener() {
            @Override
            public void onDataLoaded(Object data) {
                Toast.makeText(getApplicationContext(), "공유주차장 등록 신청되었습니다", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onDataLoadError(String errorMessage) {
                Log.d(TAG, errorMessage);
                Toast.makeText(getApplicationContext(), "공유주차장 등록 신청 중 오류 발생", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        String regex = "^010[0-9]{8}$"; // 010으로 시작하고, 그 뒤에 8자리 숫자가 온다는 정규식
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(phoneNumber);
        return matcher.matches();
    }

    private boolean isValidEmail(String email) {
        String regex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private boolean isShareTimeAfterNow() {
        boolean isAfterNow = true;

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
        Log.d(TAG, "현재 시각 : " + nowString);

        ta.sortByDate();
        CalendarDay shareTimeFirstDay = ((TimeItem) ta.getItem(0)).getDate();

        int shareYear = shareTimeFirstDay.getYear();
        int shareMonth = shareTimeFirstDay.getMonth();
        int shareDay = shareTimeFirstDay.getDay();

        String shareStartTimeString = "";

        shareStartTimeString += shareYear;
        if (shareMonth < 10) {
            shareStartTimeString += "0";
        }
        shareStartTimeString += shareMonth;
        if (shareDay < 10) {
            shareStartTimeString += "0";
        }
        shareStartTimeString += shareDay + ((TimeItem) ta.getItem(0)).getStartTime();
        Log.d(TAG, "공유 처음 시간 : " + shareStartTimeString);

        if (Long.parseLong(nowString) <= Long.parseLong(shareStartTimeString)) {
            isAfterNow = false;
        }

        return isAfterNow;
    }

    public void setHeightOfParkTimeListView() {
        int itemHeightDp = 60;

        int totalHeightPx = 0;

        int numberOfItem = ta.getCount();
        int dividerHeightPx = parkTimeListView.getDividerHeight();

        totalHeightPx += dpToPx(itemHeightDp) * numberOfItem;

        if (dividerHeightPx * (numberOfItem - 1) > 0) {
            totalHeightPx += dividerHeightPx * (numberOfItem - 1);
        }

        ViewGroup.LayoutParams params = parkTimeListView.getLayoutParams();
        params.height = totalHeightPx;
        parkTimeListView.setLayoutParams(params);
        parkTimeListView.requestLayout();
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GPS_INTENT_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                lat = data.getDoubleExtra("lat", 0);
                lon = data.getDoubleExtra("lon", 0);

                TMapData tMapData = new TMapData();
                tMapData.reverseGeocoding(lat, lon, "A10", new TMapData.reverseGeocodingListenerCallback() {
                    @Override
                    public void onReverseGeocoding(TMapAddressInfo tMapAddressInfo) {
                        if (tMapAddressInfo != null) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    String[] adrresses = tMapAddressInfo.strFullAddress.split(",");

                                    parkNewAddressContentTxt.setText(adrresses[2]);
                                    parkOldAddressContentTxt.setText(adrresses[1]);
                                }
                            });
                        }
                    }
                });
            }
        }
        else if (requestCode == PICK_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                imageUris.clear();
                if (data.getClipData() != null) { // 이미지 여러개 선택
                    int count = data.getClipData().getItemCount();
                    for (int i = 0; i < count && i < 4; i++) {
                        Uri imageUri = data.getClipData().getItemAt(i).getUri();
                        imageUris.add(imageUri);
                    }
                }
                else if (data.getData() != null) { // 이미지 하나 선택
                    imageUris.add(data.getData());
                }
                showSelectedImages();
            }
        }
    }

    private void showSelectedImages() {
        ImageView[] imageViews = {selectedImageView1, selectedImageView2, selectedImageView3, selectedImageView4};

        ImageButton[] cancelButtons = {cancelImageBtn1, cancelImageBtn2, cancelImageBtn3, cancelImageBtn4};

        for (int i = 0; i < 4; i++) {
            if (i < imageUris.size()) {
                imageViews[i].setImageURI(imageUris.get(i));
                imageViews[i].setVisibility(View.VISIBLE);
                cancelButtons[i].setVisibility(View.VISIBLE); // 취소 버튼 보이기

                final int index = i; // 현재 인덱스를 final로 설정
                int copyI = i;
                imageViews[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showImageDialog(copyI); // 다이얼로그 호출
                    }
                });

                cancelButtons[i].setOnClickListener(new View.OnClickListener() { // 클릭 리스너 설정
                    @Override
                    public void onClick(View v) {
                        imageUris.remove(index); // 이미지 URI 리스트에서 제거
                        showSelectedImages(); // 갱신된 이미지 목록 보여주기
                    }
                });
            }
            else {
                imageViews[i].setVisibility(View.GONE);
                cancelButtons[i].setVisibility(View.GONE); // 취소 버튼 숨기기
            }
        }
        imageCountTxt.setText("(" + imageUris.size() + " / 4)");
    }

    private void showImageDialog(int position) {
        String imageUrl = imageUris.get(position).toString();

        id = new ImageDialog(ShareParkActivity.this, imageUrl);
        id.show();
    }

    String replaceNewlinesAndTrim(EditText et) {
        return et.getText().toString().replaceAll("\\n", " ").trim();
    }
}

