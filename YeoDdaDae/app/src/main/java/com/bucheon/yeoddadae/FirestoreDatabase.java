package com.bucheon.yeoddadae;

import static android.content.ContentValues.TAG;

import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Transaction;
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
import java.util.Collections;
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

    public void searchDocumentId (String collection, String targetField1, Object targetValue1, String targetField2, Object targetValue2, OnFirestoreDataLoadedListener listener) {
        db.collection(collection)
                .whereEqualTo(targetField1, targetValue1)
                .whereEqualTo(targetField2, targetValue2)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.size() == 1) {
                        String documentId = queryDocumentSnapshots.getDocuments().get(0).getId();
                        listener.onDataLoaded(documentId);
                    }
                    else {
                        Log.d(TAG, "데이터가 없거나 여러개임");
                        listener.onDataLoadError("데이터가 없거나 여러개임");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "데이터 검색 오류", e);
                    listener.onDataLoadError(e.getMessage());
                });
    }

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
                        listener.onDataLoadError("아이디 또는 비밀번호가 일치하지 않습니다");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "로그인 중 오류", e);
                    listener.onDataLoadError(e.getMessage());
                });
    }

    public void loadYdPoint(String id, OnFirestoreDataLoadedListener listener) {
        db.collection("account")
                .whereEqualTo("id", id)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.size() > 0) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);

                        long ydPoint = (long) document.get("ydPoint");

                        Log.d(TAG, "보유 여따대포인트 불러오기 성공");
                        listener.onDataLoaded(ydPoint);
                    }
                    else {
                        listener.onDataLoadError("해당 문서가 없음");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "로그인 중 오류", e);
                    listener.onDataLoadError(e.getMessage());
                });
    }

    public void chargeYdPoint(String id, int price, OnFirestoreDataLoadedListener listener) {
        db.collection("account")
                .whereEqualTo("id", id)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.size() > 0) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        Long ydPoint = document.getLong("ydPoint");

                        if (ydPoint != null) {
                            db.collection("account")
                                    .document(document.getId())
                                    .update("ydPoint", ydPoint + price)
                                    .addOnSuccessListener(aVoid -> {
                                        HashMap<String, Object> hm = new HashMap<>();
                                        hm.put("id", id);
                                        hm.put("chargedYdPoint", price);
                                        hm.put("upTime", FieldValue.serverTimestamp());
                                        insertData("chargeYdPoint", hm);

                                        Log.d(TAG, "포인트 충전 성공");
                                        listener.onDataLoaded(true);
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.d(TAG, "포인트 충전 실패", e);
                                        listener.onDataLoadError("포인트 충전에 실패했습니다.");
                                    });
                        } else {
                            Log.d(TAG, "ydPoint 값이 null입니다");
                            listener.onDataLoadError("ydPoint 값이 null입니다.");
                        }
                    } else {
                        Log.d(TAG, "해당 ID의 계정 없음");
                        listener.onDataLoadError("해당 ID의 계정을 찾을 수 없습니다.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "데이터 검색 오류", e);
                    listener.onDataLoadError(e.getMessage());
                });
    }

    public void receiveYdPoint(String id, int price, String type, OnFirestoreDataLoadedListener listener) {
        // type : "환불"
        db.collection("account")
                .whereEqualTo("id", id)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.size() > 0) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        Long ydPoint = document.getLong("ydPoint");

                        if (ydPoint != null) {
                            db.collection("account")
                                    .document(document.getId())
                                    .update("ydPoint", ydPoint + price)
                                    .addOnSuccessListener(aVoid -> {
                                        HashMap<String, Object> hm = new HashMap<>();
                                        hm.put("id", id);
                                        hm.put("type", type);
                                        hm.put("receivedYdPoint", price);
                                        hm.put("upTime", FieldValue.serverTimestamp());
                                        insertData("receiveYdPoint", hm);

                                        Log.d(TAG, "포인트 받기 성공");
                                        listener.onDataLoaded(true);
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.d(TAG, "포인트 받기 실패", e);
                                        listener.onDataLoadError("포인트 받기에 실패했습니다.");
                                    });
                        } else {
                            Log.d(TAG, "ydPoint 값이 null입니다");
                            listener.onDataLoadError("ydPoint 값이 null입니다.");
                        }
                    } else {
                        Log.d(TAG, "해당 ID의 계정 없음");
                        listener.onDataLoadError("해당 ID의 계정을 찾을 수 없습니다.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "데이터 검색 오류", e);
                    listener.onDataLoadError(e.getMessage());
                });
    }

    public void payByYdPoint(String id, int price, OnFirestoreDataLoadedListener listener) {
        db.collection("account")
                .whereEqualTo("id", id)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.size() > 0) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        Long ydPoint = document.getLong("ydPoint");

                        if (ydPoint != null && ydPoint >= price) {
                            db.collection("account")
                                    .document(document.getId())
                                    .update("ydPoint", ydPoint - price)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "포인트 차감 성공");
                                        listener.onDataLoaded(true);
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.d(TAG, "포인트 차감 실패", e);
                                        listener.onDataLoadError("포인트 차감에 실패했습니다.");
                                    });
                        } else {
                            Log.d(TAG, "포인트가 부족합니다");
                            listener.onDataLoadError("포인트가 부족합니다");
                        }
                    } else {
                        Log.d(TAG, "해당 ID의 계정 없음");
                        listener.onDataLoadError("해당 ID의 계정을 찾을 수 없습니다.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "데이터 검색 오류", e);
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

        db.collection("sharePark")
                .whereEqualTo("isApproval", true)
                .whereEqualTo("isCancelled", false)
                .whereNotEqualTo("ownerId", id)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
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

                        HashMap<String, ArrayList<String>> shareTime = (HashMap<String, ArrayList<String>>) document.get("time");
                        List<String> sortedKeys = new ArrayList<>(shareTime.keySet());
                        Collections.sort(sortedKeys);
                        String endTime = sortedKeys.get(sortedKeys.size() - 1) + shareTime.get(sortedKeys.get(sortedKeys.size() - 1)).get(1);

                        if (nowString.compareTo(endTime) > 0) {
                            continue;
                        }

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
                            resultList.add(new ParkItem(3, Integer.toString(i[0]), Double.toString(distance), "10000", (String) document.get("ownerPhone"), "부가", 0, Double.toString(resultLat), Double.toString(resultLon), null, document.getId()));
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
                    }
                    else {
                        listener.onDataLoadError("해당 문서가 존재하지 않음");
                    }
                })
                .addOnFailureListener(e -> {
                    listener.onDataLoadError(e.getMessage());
                });
    }

    public void cancelSharePark (String id, String firestoreDocumentId ,OnFirestoreDataLoadedListener listener) {
        db.collection("sharePark")
                .document(firestoreDocumentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        if(documentSnapshot.get("ownerId").equals(id)) {
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

                            HashMap<String, ArrayList<String>> shareTime = (HashMap<String, ArrayList<String>>) documentSnapshot.get("time");
                            List<String> sortedKeys = new ArrayList<>(shareTime.keySet());
                            Collections.sort(sortedKeys);
                            String endTime = sortedKeys.get(sortedKeys.size() - 1) + shareTime.get(sortedKeys.get(sortedKeys.size() - 1)).get(1);

                            Log.d(TAG, "현재 시각 : " + nowString);
                            Log.d(TAG, "공유 마지막 날의 끝 시각 : " + endTime);
                            if (nowString.compareTo(endTime) > 0) {
                                Log.d(TAG, "공유 시간이 지나서 취소 불가");
                                listener.onDataLoadError("공유 시간이 지나서 취소 불가");
                            }
                            else {
                                db.collection("reservation")
                                        .whereNotEqualTo("isCancelled", false)
                                        .get()
                                        .addOnSuccessListener(documentSnapshots2 -> {
                                            if (documentSnapshots2.size() > 0) {
                                                listener.onDataLoadError("예약이 있어서 취소 불가");
                                            }
                                            else {
                                                db.collection("sharePark")
                                                        .document(firestoreDocumentId)
                                                        .update("isCancelled", true)
                                                        .addOnSuccessListener(aVoid -> {
                                                            listener.onDataLoaded(true);
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Log.d(TAG, "공유 취소 실패");
                                                            listener.onDataLoadError(e.getMessage());
                                                        });
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.d(TAG, "해당 공유주차장의 예약 찾기 실패");
                                            listener.onDataLoadError(e.getMessage());
                                        });
                            }
                        }
                        else {
                            listener.onDataLoadError("ID가 일치하지 않음");
                        }
                    }
                    else {
                        listener.onDataLoadError("문서가 존재하지 않음");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "데이터 검색 오류", e);
                    listener.onDataLoadError(e.getMessage());
                });
    }

    void calculateShareParkPrice(String id, String firestoreDocumentId, OnFirestoreDataLoadedListener listener) {
        db.collection("sharePark")
                .document(firestoreDocumentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (id.equals((String) documentSnapshot.get("ownerId"))) {
                        if (!(boolean) documentSnapshot.get("isCalculated")) {
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

                            HashMap<String, ArrayList<String>> shareTime = (HashMap<String, ArrayList<String>>) documentSnapshot.get("time");
                            List<String> sortedKeys = new ArrayList<>(shareTime.keySet());
                            Collections.sort(sortedKeys);
                            String endTime = sortedKeys.get(sortedKeys.size() - 1) + shareTime.get(sortedKeys.get(sortedKeys.size() - 1)).get(1);

                            Log.d(TAG, "현재 시각 : " + nowString);
                            Log.d(TAG, "공유 마지막 날의 끝 시각 : " + endTime);
                            if (nowString.compareTo(endTime) > 0) {
                                db.collection("reservation")
                                        .whereEqualTo("shareParkDocumentName", firestoreDocumentId)
                                        .whereEqualTo("isCancelled", false)
                                        .get()
                                        .addOnSuccessListener(queryDocumentSnapshots -> {
                                            long totalPrice = 0;
                                            for (QueryDocumentSnapshot documentSnapshot2 : queryDocumentSnapshots) {
                                                totalPrice += (long) documentSnapshot2.get("price");
                                            }
                                            if (queryDocumentSnapshots != null && queryDocumentSnapshots.size() > 0) {
                                                Log.d(TAG, "정산할 예약 수 : " + queryDocumentSnapshots.size() );
                                            }
                                            else {
                                                Log.d(TAG, "정산할 예약이 없음");
                                            }
                                            receiveYdPoint(id, (int) totalPrice, "공유주차장 정산", new OnFirestoreDataLoadedListener() {
                                                @Override
                                                public void onDataLoaded(Object data) {
                                                    db.collection("sharePark")
                                                            .document(firestoreDocumentId)
                                                            .update("isCalculated", true)
                                                            .addOnSuccessListener(aVoid -> {
                                                                listener.onDataLoaded(true);
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                Log.d(TAG, "isCalculated true로 변경 실패");
                                                                listener.onDataLoadError(e.getMessage());
                                                            });
                                                }

                                                @Override
                                                public void onDataLoadError(String errorMessage) {

                                                }
                                            });
                                        })
                                        .addOnFailureListener(e -> {

                                        });
                            }
                            else {
                                listener.onDataLoadError("사용완료된 공유주차장이 아님");
                            }
                        }
                        else {
                            listener.onDataLoadError("이미 정산됨");
                        }
                    }
                    else {
                        listener.onDataLoadError("ID가 일치하지 않음");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, e.getMessage());
                    listener.onDataLoadError(e.getMessage());
                });
    }

    public void loadAnotherReservations(String firestoreDocumentId, OnFirestoreDataLoadedListener listener) {
        db.collection("reservation")
                .whereEqualTo("shareParkDocumentName", firestoreDocumentId)
                .whereEqualTo("isCancelled", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    HashMap<String,  HashMap<String, ArrayList<String>>> resultMap = new HashMap<>();
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                        String documentId = documentSnapshot.getId();
                        HashMap<String, ArrayList<String>> times = (HashMap<String, ArrayList<String>>) documentSnapshot.get("time");
                        resultMap.put(documentId, times);
                    }
                    if (resultMap.size() > 0 && resultMap != null) {
                        Log.d (TAG, "다른 예약들 찾기 성공 갯수 : " + resultMap.size());
                    }
                    else {
                        Log.d(TAG, "다른 예약이 없음");
                    }
                    listener.onDataLoaded(resultMap);
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, e.getMessage());
                    listener.onDataLoadError(e.getMessage());
                });
    }

    public void loadMyReservations (String loginId, OnFirestoreDataLoadedListener listener) {
        db.collection("reservation")
                .whereEqualTo("id", loginId)
                .orderBy("upTime", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<HashMap<String, Object>> resultArrayList = new ArrayList<>();
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        HashMap<String, Object> data = new HashMap<>(documentSnapshot.getData());
                        data.put("documentId", documentSnapshot.getId());
                        resultArrayList.add(data);
                    }
                    if (resultArrayList.size() > 0 && resultArrayList != null) {
                        Log.d (TAG, "내 예약들 찾기 성공 갯수 : " + resultArrayList.size());
                    }
                    else {
                        Log.d(TAG, "내 예약이 없음");
                    }
                    listener.onDataLoaded(resultArrayList);
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "데이터 검색 오류", e);
                    listener.onDataLoadError(e.getMessage());
                });
    }

    public void loadReservation (String loginId, String firestoreDocumentId, OnFirestoreDataLoadedListener listener) {
        HashMap<String, Object> hm = new HashMap<>();

        db.collection("reservation")
                .document(firestoreDocumentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        if(documentSnapshot.get("id").equals(loginId)) {
                            hm.putAll(documentSnapshot.getData());
                            listener.onDataLoaded(hm);
                        }
                        else {
                            listener.onDataLoadError("아이디가 일치하지 않음");
                        }
                    }
                    else {
                        listener.onDataLoadError("해당 문서가 존재하지 않음");
                    }
                })
                .addOnFailureListener(e -> {
                    listener.onDataLoadError(e.getMessage());
                });
    }

    public void cancelReservation(String loginId, String firestoreDocumentId, OnFirestoreDataLoadedListener listener) {
        db.collection("reservation")
                .document(firestoreDocumentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        if(documentSnapshot.get("id").equals(loginId)) {
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

                            HashMap<String, ArrayList<String>> reservationTime = (HashMap<String, ArrayList<String>>) documentSnapshot.get("time");
                            List<String> sortedKeys = new ArrayList<>(reservationTime.keySet());
                            Collections.sort(sortedKeys);
                            String firstTime = sortedKeys.get(0) + reservationTime.get(sortedKeys.get(0)).get(0);

                            Log.d(TAG, "현재 시각 : " + nowString);
                            Log.d(TAG, "예약 첫 날의 시작 시각 : " + firstTime);
                            if (nowString.compareTo(firstTime) > 0) {
                                Log.d(TAG, "예약 시간이 지나서 취소 불가");
                                listener.onDataLoadError("예약 시간이 지나서 취소 불가");
                                return;
                            }
                            int price = (int)((long) documentSnapshot.get("price"));

                            db.collection("reservation")
                                    .document(firestoreDocumentId)
                                    .update("isCancelled", true)
                                    .addOnSuccessListener(aVoid -> {
                                        receiveYdPoint(loginId, price, "환불", new OnFirestoreDataLoadedListener() {
                                            @Override
                                            public void onDataLoaded(Object data) {
                                                Log.d(TAG, "예약 취소 성공");
                                                listener.onDataLoaded(1);
                                            }

                                            @Override
                                            public void onDataLoadError(String errorMessage) {

                                            }
                                        });


                                    })
                                    .addOnFailureListener(e -> {
                                        Log.d(TAG, "예약 취소 실패");
                                        listener.onDataLoadError(e.getMessage());
                                    });
                        }
                        else {
                            listener.onDataLoadError("ID가 일치하지 않음");
                        }
                    }
                    else {
                        listener.onDataLoadError("문서가 존재하지 않음");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "데이터 검색 오류", e);
                    listener.onDataLoadError(e.getMessage());
                });
    }

    public void loadMyReports (String loginId, OnFirestoreDataLoadedListener listener) {
        db.collection("reportDiscountPark")
                .whereEqualTo("reporterId", loginId)
                .orderBy("upTime", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<HashMap<String, Object>> resultArrayList = new ArrayList<>();
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        HashMap<String, Object> data = new HashMap<>(documentSnapshot.getData());
                        data.put("documentId", documentSnapshot.getId());
                        resultArrayList.add(data);
                    }
                    if (resultArrayList.size() > 0 && resultArrayList != null) {
                        Log.d (TAG, "내 제보들 찾기 성공 갯수 : " + resultArrayList.size());
                    }
                    else {
                        Log.d(TAG, "내 제보가 없음");
                    }
                    listener.onDataLoaded(resultArrayList);
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "데이터 검색 오류", e);
                    listener.onDataLoadError(e.getMessage());
                });
    }

    public void loadOneReport (String firestoreDocumentId, OnFirestoreDataLoadedListener listener) {
        HashMap<String, Object> hm = new HashMap<>();

        db.collection("reportDiscountPark")
                .document(firestoreDocumentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        hm.putAll(documentSnapshot.getData());
                        listener.onDataLoaded(hm);
                    }
                    else {
                        listener.onDataLoadError("해당 문서가 존재하지 않음");
                    }
                })
                .addOnFailureListener(e -> {
                    listener.onDataLoadError(e.getMessage());
                });
    }

    public void loadMyOneReport (String loginId, String firestoreDocumentId, OnFirestoreDataLoadedListener listener) {
        HashMap<String, Object> hm = new HashMap<>();

        db.collection("reportDiscountPark")
                .document(firestoreDocumentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        if(documentSnapshot.get("reporterId").equals(loginId)) {
                            hm.putAll(documentSnapshot.getData());
                            listener.onDataLoaded(hm);
                        }
                        else {
                            listener.onDataLoadError("아이디가 일치하지 않음");
                        }
                    }
                    else {
                        listener.onDataLoadError("해당 문서가 존재하지 않음");
                    }
                })
                .addOnFailureListener(e -> {
                    listener.onDataLoadError(e.getMessage());
                });
    }

    public void cancelReport (String loginId, String firestoreDocumentId, OnFirestoreDataLoadedListener listener) {
        db.collection("reportDiscountPark")
                .document(firestoreDocumentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        if(documentSnapshot.get("reporterId").equals(loginId)) {
                            if (!(boolean) documentSnapshot.get("isApproval")) {
                                db.collection("reportDiscountPark")
                                        .document(firestoreDocumentId)
                                        .update("isCancelled", true)
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d(TAG, "제보 취소 성공");
                                            listener.onDataLoaded(1);
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.d(TAG, "제보 취소 실패");
                                            listener.onDataLoadError(e.getMessage());
                                        });
                            }
                            else {
                                Log.d(TAG, "dsdsdsdsddssds");
                                listener.onDataLoadError("승인된 제보임");
                            }
                        }
                        else {
                            listener.onDataLoadError("ID가 일치하지 않음");
                        }
                    }
                    else {
                        listener.onDataLoadError("문서가 존재하지 않음");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "데이터 검색 오류", e);
                    listener.onDataLoadError(e.getMessage());
                });
    }

    public void loadAnotherReports (int targetDistanceKM, double nowLat, double nowLon, String loginId, OnFirestoreDataLoadedListener listener) {
        db.collection("reportDiscountPark")
                .whereNotEqualTo("reporterId", loginId)
                .whereEqualTo("isApproval", false)
                .whereEqualTo("isCancelled", false)
                .orderBy("upTime", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<HashMap<String, Object>> resultArrayList = new ArrayList<>();
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {

                        if (targetDistanceKM == 0) {
                            HashMap<String, Object> data = new HashMap<>(documentSnapshot.getData());
                            data.put("documentId", documentSnapshot.getId());
                            resultArrayList.add(data);
                        }
                        else {
                            Double resultLat = (Double) documentSnapshot.get("poiLat");
                            Double resultLon = (Double) documentSnapshot.get("poiLon");

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

                            if (distance <= targetDistanceKM) {
                                HashMap<String, Object> data = new HashMap<>(documentSnapshot.getData());
                                data.put("documentId", documentSnapshot.getId());
                                resultArrayList.add(data);
                            }
                        }
                    }
                    if (resultArrayList.size() > 0 && resultArrayList != null) {
                        Log.d (TAG, "제보들 찾기 성공 갯수 : " + resultArrayList.size());
                    }
                    else {
                        Log.d(TAG, "제보가 없음");
                    }
                    listener.onDataLoaded(resultArrayList);
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "데이터 검색 오류", e);
                    listener.onDataLoadError(e.getMessage());
                });
    }

    public void loadRateCount (String firestoreDocumentId, OnFirestoreDataLoadedListener listener) {
        int[] perfectCount = {0};
        int[] mistakeCount = {0};
        int[] wrongCount = {0};

        db.collection("rateReport")
                .whereEqualTo("reportDocumentID", firestoreDocumentId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String rate = documentSnapshot.getString("rate");
                        if (rate.equals("perfect")) {
                            perfectCount[0]++;
                        }
                        else if (rate.equals("mistake")) {
                            mistakeCount[0]++;
                        }
                        else if (rate.equals("wrong")) {
                            wrongCount[0]++;
                        }
                    }

                    int [] result = {perfectCount[0], mistakeCount[0], wrongCount[0]};
                    listener.onDataLoaded(result);
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "데이터 검색 오류", e);
                    listener.onDataLoadError(e.getMessage());
                });
    }

    public void updateRate (int clickedBtn, String loginId, String firestoreDocumentId, OnFirestoreDataLoadedListener listener) {
        // clickedBtn은 0 : 완벽, 1 : 실수있음, 2 : 완전히 틀림

        String[] rate = {""};
        switch (clickedBtn) {
            case 0:
                rate[0] = "perfect";
                break;
            case 1:
                rate[0] = "mistake";
                break;
            case 2:
                rate[0] = "wrong";
                break;
        }

        // 먼저 해당 조건에 맞는 문서가 있는지 확인
        db.collection("rateReport")
                .whereEqualTo("id", loginId)
                .whereEqualTo("reportDocumentID", firestoreDocumentId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            // 문서 삭제
                            if (documentSnapshot.get("rate").equals(rate[0])) {
                                documentSnapshot.getReference().delete()
                                        .addOnSuccessListener(aVoid -> {
                                            listener.onDataLoaded("평가 삭제됨 : " + rate[0]);
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.d(TAG, "문서 삭제 오류", e);
                                            listener.onDataLoadError(e.getMessage());
                                        });
                            }
                            // 문서 rate 수정
                            else {
                                documentSnapshot.getReference().update("rate", rate[0])
                                        .addOnSuccessListener(aVoid -> {
                                            listener.onDataLoaded("평가 수정됨 : " + rate[0]);
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.d(TAG, "문서 업데이트 오류", e);
                                            listener.onDataLoadError(e.getMessage());
                                        });
                            }
                        }
                    }
                    else {
                        // 문서가 존재하지 않으면 새로 추가
                        HashMap<String, Object> data = new HashMap<>();
                        data.put("id", loginId);
                        data.put("reportDocumentID", firestoreDocumentId);
                        data.put("rate", rate[0]);
                        
                        insertData("rateReport", data);
                        listener.onDataLoaded("새로 평가함 : " + rate[0]);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "데이터 검색 오류", e);
                    listener.onDataLoadError(e.getMessage());
                });
    }

    public void loadUnapprovedShareParks(OnFirestoreDataLoadedListener listener) {
        db.collection("sharePark")
                .whereEqualTo("isApproval", false)
                .whereEqualTo("isCancelled", false)
                .orderBy("upTime", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<HashMap<String, Object>> resultArrayList = new ArrayList<>();
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
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

                        HashMap<String, ArrayList<String>> shareTime = (HashMap<String, ArrayList<String>>) documentSnapshot.get("time");
                        List<String> sortedKeys = new ArrayList<>(shareTime.keySet());
                        Collections.sort(sortedKeys);
                        String firstTime = sortedKeys.get(0) + shareTime.get(sortedKeys.get(0)).get(0);

                        Log.d(TAG, "현재 시각 : " + nowString);
                        Log.d(TAG, "예약 첫 날의 시작 시각 : " + firstTime);
                        if (nowString.compareTo(firstTime) > 0) {
                            continue;
                        }

                        HashMap<String, Object> data = new HashMap<>(documentSnapshot.getData());
                        data.put("documentId", documentSnapshot.getId());
                        resultArrayList.add(data);
                    }
                    
                    if (resultArrayList.size() > 0 && resultArrayList != null) {
                        Log.d (TAG, "승인 안된 공유 주차장 찾기 성공 갯수 : " + resultArrayList.size());
                    }
                    else {
                        Log.d(TAG, "승인 안된 공유 주차장이 없음");
                    }
                    listener.onDataLoaded(resultArrayList);
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "데이터 검색 오류", e);
                    listener.onDataLoadError(e.getMessage());
                });
    }

    public void loadMyShareParks(String id, OnFirestoreDataLoadedListener listener) {
        db.collection("sharePark")
                .whereEqualTo("ownerId", id)
                .orderBy("upTime", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<HashMap<String, Object>> resultArrayList = new ArrayList<>();

                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        HashMap<String, Object> data = new HashMap<>(documentSnapshot.getData());
                        data.put("documentId", documentSnapshot.getId());
                        resultArrayList.add(data);
                    }

                    if (resultArrayList.size() > 0 && resultArrayList != null) {
                        Log.d (TAG, "내 공유 주차장 찾기 성공 갯수 : " + resultArrayList.size());
                    }
                    else {
                        Log.d(TAG, "내 공유 주차장이 없음");
                    }
                    listener.onDataLoaded(resultArrayList);
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "데이터 검색 오류", e);
                    listener.onDataLoadError(e.getMessage());
                });
    }

    public void approveSharePark(String firestoreDocumentId, OnFirestoreDataLoadedListener listener) {
        db.runTransaction((Transaction.Function<Void>) transaction -> {
            DocumentSnapshot documentSnapshot = transaction.get(db.collection("sharePark").document(firestoreDocumentId));

            if (!(boolean) documentSnapshot.get("isApproval")) {
                if (!(boolean) documentSnapshot.get("isCancelled")) {
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

                    HashMap<String, ArrayList<String>> reservationTime = (HashMap<String, ArrayList<String>>) documentSnapshot.get("time");
                    List<String> sortedKeys = new ArrayList<>(reservationTime.keySet());
                    Collections.sort(sortedKeys);
                    String firstTime = sortedKeys.get(0) + reservationTime.get(sortedKeys.get(0)).get(0);

                    Log.d(TAG, "현재 시각 : " + nowString);
                    Log.d(TAG, "예약 첫 날의 시작 시각 : " + firstTime);
                    if (nowString.compareTo(firstTime) < 0) {
                        transaction.update(db.collection("sharePark").document(firestoreDocumentId), "isApproval", true);
                    } else {
                        throw new FirebaseFirestoreException("공유시간이 지남", FirebaseFirestoreException.Code.ABORTED);
                    }
                } else {
                    throw new FirebaseFirestoreException("취소된 공유주차장임", FirebaseFirestoreException.Code.ABORTED);
                }
            } else {
                throw new FirebaseFirestoreException("이미 승인된 공유주차장임", FirebaseFirestoreException.Code.ABORTED);
            }

            return null;
        }).addOnSuccessListener(aVoid -> {
            listener.onDataLoaded(true);
        }).addOnFailureListener(e -> {
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
