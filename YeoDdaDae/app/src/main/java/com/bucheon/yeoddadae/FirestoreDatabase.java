package com.bucheon.yeoddadae;

import static android.content.ContentValues.TAG;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class FirestoreDatabase {
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    // 데이터 추가
    // String collection : 콜렉션명, HashMap<String, Object> hm : 데이터의 속성과 값을 가진 HashMap
    public void insertData (String collection, HashMap<String, Object> hm) {
        db.collection(collection)
                .add(hm)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "데이터 추가 성공 with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "데이터 추가 오류", e);
                    }
                });
    }

    // 중복검사 true : 중복없음, false : 중복임
    // String collection : 콜렉션명, String targetField : 속성명, Object targetValue : 찾는값
    public void duplicationCheck(String collection, String targetField, Object targetValue, OnFirestoreDataLoadedListener listener) {
        db.collection(collection)
                .whereEqualTo(targetField, targetValue)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.size() > 0) {
                        listener.onDataLoaded(false);
                        Log.d(TAG, "데이터 with " + targetField + " = " + targetValue + " 가 이미 존재함");
                    } else {
                        listener.onDataLoaded(true);
                        Log.d(TAG, "데이터 with " + targetField + " = " + targetValue + " 가 없음");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "중복검사 중 오류", e);
                });
    }

    // 데이터 수정
    // String collection : 콜렉션, String targetField : 찾을속성, Object targetValue : 찾을값, String updateField : 변경할속성, Object updateValue : 변경할값
    public void updateData (String collection, String targetField, Object targetValue, String updateField, Object updateValue) {
        db.collection(collection)
                .whereEqualTo(targetField, targetValue)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        // Check if the document contains the updateField
                        if (document.contains(updateField)) {
                            // Update the specified field with the new value
                            document.getReference().update(updateField, updateValue)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "데이터 수정 성공");
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.d(TAG, "데이터 수정 오류", e);
                                    });
                        } else {
                            Log.d(TAG, "데이터에 " + updateField + " 필드가 존재하지 않음");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "데이터 검색 오류", e);
                });
    }

    // 데이터삭제
    // String collection : 콜렉션명, String targetField : 속성명, Object targetValue : 찾는값
    public void deleteData (String collection, String targetField, Object targetValue) {
        db.collection(collection)
                .whereEqualTo(targetField, targetValue)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        db.collection(collection)
                                .document(document.getId())
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "데이터 삭제 성공");
                                })
                                .addOnFailureListener(e -> {
                                    Log.d(TAG, "데이터 삭제 오류", e);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "데이터 검색 오류", e);
                });
    }

    // 콜렉션의 모든 데이터의 targetField의 값을 orderField의 값 순서대로 검색
    // 사용법은 아래에
    // String collection : 콜렉션명, String targetField : 속성명, String orderField : 정렬속성명
    public void selectAll(String collection, String targetField, String orderField, OnFirestoreDataLoadedListener listener) {
        List<Object> resultList = new ArrayList<>();

        db.collection(collection)
                .orderBy(orderField)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Object fieldValue = document.get(targetField);
                        if (fieldValue != null) {
                            resultList.add(fieldValue);
                        }
                    }
                    if (queryDocumentSnapshots.size() > 0) {
                        Log.d(TAG, "데이터 검색 성공");
                    } else {
                        Log.d(TAG, "해당 데이터가 없음");
                    }

                    listener.onDataLoaded(resultList);
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "데이터 검색 오류", e);
                    listener.onDataLoadError(e.getMessage());
                });
    }

    // selectAll 사용법 예시
    /*
    fd.selectAll("콜렉션", "속성1", "속성2", new OnDataLoadedListener() {
        @Override
        public void onDataLoaded(Object data) {
            List<Object> resultList = (List<Object>) data;
            //resultList로 할 작업 작성
        }

        @Override
        public void onDataLoadError(String errorMessage) {
            //오류 발생
        }
     });
     */

    // 콜렉션의 데이터 중 targetField 필드가 targetValue인 문서의 resultField 필드의 값을 리턴
    // 사용법은 아래에
    // String collection : 콜렉션명, String targetField : 찾을 속성명, Object targetValue : 찾는 값, String resultField : 반환할 필드명
    public void selectOne(String collection, String targetField, Object targetValue, String resultField, OnFirestoreDataLoadedListener listener) {
        db.collection(collection)
                .whereEqualTo(targetField, targetValue)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Object resultValue = document.get(resultField);
                        if (resultValue != null) {
                            Log.d(TAG, "데이터 검색 성공");
                            listener.onDataLoaded(resultValue);
                            return;
                        }
                        else {
                            Log.d(TAG, "결과 값이 null");
                            return;
                        }
                    }
                    Log.d(TAG, "해당 데이터가 없음");
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "데이터 검색 오류", e);
                    listener.onDataLoadError(e.getMessage());
                });
    }

    // selectOne 사용법 예시
    /*
    fd.selectOne("test", "속성1", 1111, "속성2", new OnDataLoadedListener() {
                    @Override
                    public void onDataLoaded(Object resultValue) {
                        // resultValue로 할 작업 작성
                    }

                    @Override
                    public void onDataLoadError(String errorMessage) {
                        // 오류 발생
                    }
     */

    public void login(String id, String pw, OnFirestoreDataLoadedListener listener) {
        db.collection("account")
                .whereEqualTo("id", id)
                .whereEqualTo("pw", pw)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.size() > 0) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);

                        // Get the value of isAdmin property
                        boolean isAdmin = document.getBoolean("isAdmin");

                        Log.d(TAG, "로그인 성공");
                        listener.onDataLoaded(isAdmin);
                    }
                    else {
                        Log.d(TAG, "아이디 또는 비밀번호가 일치하지 않음");
                        listener.onDataLoadError("아이디 또는 비밀번호가 일치하지 않습니다.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "로그인 중 오류", e);
                    listener.onDataLoadError(e.getMessage());
                });
    }

    public void findSharePark(String id, double nowLat, double nowLon, double radiusKiloMeter, OnFirestoreDataLoadedListener listener) {
        List<ParkItem> resultList = new ArrayList<>();

        if (id == null || id.equals("")) {
            Log.d(TAG, "아이디가 null이거나 빈 문자열임");
            return;
        }

        int[] i = {1};
        Timestamp now = Timestamp.now();

        db.collection("sharePark")
                .whereEqualTo("isApproval", true)
                .whereNotEqualTo("ownerId", id)
                .whereGreaterThan("upTime", now)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Double resultLat = (Double) document.get("lat");
                        Double resultLon = (Double) document.get("lon");

                        final double R = 6371e3; // 지구 반경 (미터)

                        double lat1Rad = Math.toRadians(nowLat); // 위도 및 경도를 라디안으로 변환
                        double lon1Rad = Math.toRadians(nowLon);
                        double lat2Rad = Math.toRadians(resultLat);
                        double lon2Rad = Math.toRadians(resultLon);

                        double deltaLat = lat2Rad - lat1Rad; // 위도 및 경도의 차이 계산
                        double deltaLon = lon2Rad - lon1Rad;

                        // Haversine 공식 적용
                        double a = Math.pow(Math.sin(deltaLat / 2), 2) + Math.cos(lat1Rad) * Math.cos(lat2Rad) * Math.pow(Math.sin(deltaLon / 2), 2);
                        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

                        double distance = (R * c) / 1000; // 거리 계산 km 단위

                        if (distance <= radiusKiloMeter) {
                            resultList.add(new ParkItem(3, Integer.toString(i[0]), Double.toString(distance), "10000", (String) document.get("ownerPhone"), "부가", 0, Double.toString(resultLat), Double.toString(resultLon), document.getId()));
                            i[0]++;
                        }
                    }
                    if (resultList != null && resultList.size() != 0) {
                        Log.d(TAG, "공유주차장 검색 성공. 결과 수 : " + resultList.size());
                        listener.onDataLoaded(resultList);
                    }
                    else {
                        Log.d(TAG, "해당 공유주차장이 없음");
                        listener.onDataLoaded(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "데이터 검색 오류", e);
                    listener.onDataLoadError(e.getMessage());
                });
    }

    public void loadShareParkInfo (String firestoreDocumentId, OnFirestoreDataLoadedListener listener) {
        HashMap<String, Object> hm = new HashMap<>();

        db.collection("sharePark")
                .document(firestoreDocumentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // 문서가 존재할 경우 해당 문서의 데이터를 HashMap에 넣음
                        hm.putAll(documentSnapshot.getData());
                        listener.onDataLoaded(hm);
                    } else {
                        listener.onDataLoadError("해당 문서가 존재하지 않음");
                    }
                })
                .addOnFailureListener(e -> {
                    listener.onDataLoadError(e.getMessage());
                });
    }

    public void loadAnotherReservations(String firestoreDocumentId, OnFirestoreDataLoadedListener listener) {
        db.collection("reservation")
                .whereEqualTo("shareParkDocumentName", firestoreDocumentId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // 쿼리 결과에서 문서들을 순회하며 shareParkDocumentName 값을 추출하여 Map에 저장
                    HashMap<String,  HashMap<String, ArrayList<String>>> resultMap = new HashMap<>();
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                        String documentId = documentSnapshot.getId(); // 문서의 ID를 가져옴
                        HashMap<String, ArrayList<String>> times = (HashMap<String, ArrayList<String>>) documentSnapshot.get("time");
                        // Map에 저장, 여기서는 문서 ID를 키로, shareParkDocumentName 값을 값으로 사용
                        resultMap.put(documentId, times);
                    }
                    // 결과 Map을 리스너의 onDataLoaded 메서드를 통해 전달
                    listener.onDataLoaded(resultMap);
                })
                .addOnFailureListener(e -> {
                    // 조회 실패 시 리스너의 onDataLoadError 메서드를 통해 오류 메시지 전달
                    listener.onDataLoadError(e.getMessage());
                });
    }

    public void loadMyReservations (String loginId, OnFirestoreDataLoadedListener listener) {
        db.collection("reservation")
                .whereEqualTo("id", loginId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {

                    }
                    if (resultList != null && resultList.size() != 0) {
                        Log.d(TAG, "공유주차장 검색 성공. 결과 수 : " + resultList.size());
                        listener.onDataLoaded(resultList);
                    }
                    else {
                        Log.d(TAG, "해당 공유주차장이 없음");
                        listener.onDataLoaded(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "데이터 검색 오류", e);
                    listener.onDataLoadError(e.getMessage());
                });
    }

    public void my (OnFirestoreDataLoadedListener listener) {
        Log.d (TAG, "my 호출됨");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d (TAG, "my 호출됨2");
                    StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/B553881/Parking/PrkSttusInfo"); /*URL*/
                    urlBuilder.append("?" + URLEncoder.encode("serviceKey","UTF-8") + "=SW%2Bzk19oOq3MYSQHFSd08425DV%2BqaKp%2F5%2B1ECPndYBDZeTwVuvDqm6iKLl5haFOJmpXQ3%2BhjVRHF3PL4eg7rug%3D%3D"); /*Service Key*/
                    urlBuilder.append("&" + URLEncoder.encode("pageNo","UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")); /*페이지번호*/
                    urlBuilder.append("&" + URLEncoder.encode("numOfRows","UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")); /*한 페이지 결과 수*/
                    urlBuilder.append("&" + URLEncoder.encode("format","UTF-8") + "=" + URLEncoder.encode("2", "UTF-8")); /*XML : 1, JSON : 2*/
                    URL url = new URL(urlBuilder.toString());
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Content-type", "application/json");
                    System.out.println("Response code: " + conn.getResponseCode());
                    BufferedReader rd;
                    if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
                        rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    } else {
                        rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    }
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = rd.readLine()) != null) {
                        response.append(line);
                    }
                    rd.close();
                    conn.disconnect();
                    Log.d(TAG, "my 호출됨3");

                    // JSON 문자열을 JSONObject로 파싱
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    // 원하는 데이터 추출
                    int totalCount = Integer.parseInt(jsonResponse.getString("totalCount"));

                    int numOfRows = 100; // 한 페이지 결과 수를 100으로 설정
                    int totalPages = (int) Math.ceil((double) totalCount / numOfRows); // 총 페이지 수 계산

                    for (int page = 1; page <= totalPages; page++) {
                        Log.d(TAG, "Page : " + page + "/" + totalPages + " 시작");
                        
                        // 각 페이지에 대한 URL 생성
                        StringBuilder pageUrlBuilder = new StringBuilder("http://apis.data.go.kr/B553881/Parking/PrkSttusInfo");
                        pageUrlBuilder.append("?" + URLEncoder.encode("serviceKey", "UTF-8") + "=SW%2Bzk19oOq3MYSQHFSd08425DV%2BqaKp%2F5%2B1ECPndYBDZeTwVuvDqm6iKLl5haFOJmpXQ3%2BhjVRHF3PL4eg7rug%3D%3D"); /*Service Key*/
                        pageUrlBuilder.append("&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(page), "UTF-8")); /*페이지번호*/
                        pageUrlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(numOfRows), "UTF-8")); /*한 페이지 결과 수*/
                        pageUrlBuilder.append("&" + URLEncoder.encode("format", "UTF-8") + "=" + URLEncoder.encode("2", "UTF-8")); /*XML : 1, JSON : 2*/

                        URL pageUrl = new URL(pageUrlBuilder.toString());
                        HttpURLConnection pageConn = (HttpURLConnection) pageUrl.openConnection();
                        pageConn.setRequestMethod("GET");
                        pageConn.setRequestProperty("Content-type", "application/json");
                        System.out.println("Response code: " + pageConn.getResponseCode());

                        BufferedReader pageRd;
                        if (pageConn.getResponseCode() >= 200 && pageConn.getResponseCode() <= 300) {
                            pageRd = new BufferedReader(new InputStreamReader(pageConn.getInputStream()));
                        } else {
                            pageRd = new BufferedReader(new InputStreamReader(pageConn.getErrorStream()));
                        }
                        StringBuilder pageResponse = new StringBuilder();
                        String pageLine;
                        while ((pageLine = pageRd.readLine()) != null) {
                            pageResponse.append(pageLine);
                        }
                        pageRd.close();
                        pageConn.disconnect();

                        JSONObject pageJsonResponse = new JSONObject(pageResponse.toString());
                        listener.onDataLoaded(pageJsonResponse.toString());

                        // "PrkSttusInfo" 키의 값을 JSONArray로 가져옴
                        JSONArray prkSttusInfoArray = pageJsonResponse.getJSONArray("PrkSttusInfo");

                        // JSONArray를 순회하며 각각의 객체에 대해 처리
                        for (int i = 0; i < prkSttusInfoArray.length(); i++) {
                            Log.d (TAG, (i + 1) + "/" + prkSttusInfoArray.length() + "데이터");
                            JSONObject prkInfo = prkSttusInfoArray.getJSONObject(i);

                            // 필요한 데이터 추출
                            String prkCmprtCo = prkInfo.getString("prk_cmprt_co");
                            String prkCenterId = prkInfo.getString("prk_center_id");
                            String prkPlceNm = prkInfo.getString("prk_plce_nm");
                            String prkPlceAdres = prkInfo.getString("prk_plce_adres");
                            String prkPlceEntrcLo = prkInfo.getString("prk_plce_entrc_lo");
                            String prkPlceEntrcLa = prkInfo.getString("prk_plce_entrc_la");

                            // 필요한 데이터를 HashMap으로 구성
                            HashMap<String, Object> hm = new HashMap<>();
                            hm.put("prk_cmprt_co", prkCmprtCo);
                            hm.put("prk_center_id", prkCenterId);
                            hm.put("prk_plce_nm", prkPlceNm);
                            hm.put("prk_plce_adres", prkPlceAdres);
                            hm.put("prk_plce_entrc_lo", prkPlceEntrcLo);
                            hm.put("prk_plce_entrc_la", prkPlceEntrcLa);

                            // insertData 메서드를 호출하여 Firestore에 데이터 삽입
                            insertData("parkApi", hm);
                        }
                    }
                }
                catch (Exception e) {
                    listener.onDataLoadError(String.valueOf(e));
                }
            }
        }).start();
    }
}
