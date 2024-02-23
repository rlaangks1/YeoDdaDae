package com.bucheon.yeoddadae;

import static android.content.ContentValues.TAG;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
    public boolean duplicationCheck(String collection, String targetField, Object targetValue) {
        boolean[] isDuplicate = {true};

        db.collection(collection)
                .whereEqualTo(targetField, targetValue)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.size() > 0) {
                        isDuplicate[0] = false;
                        Log.d(TAG, "데이터 with " + targetField + " = " + targetValue + " 가 이미 존재함");
                    } else {
                        isDuplicate[0] = true;
                        Log.d(TAG, "데이터 with " + targetField + " = " + targetValue + " 가 없음");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "중복검사 중 오류", e);
                });

        return isDuplicate[0];
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
    public void selectAll(String collection, String targetField, String orderField, OnDataLoadedListener listener) {
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

                    listener.onSelectAllLoaded(resultList);
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
        public void onSelectAllLoaded(List<Object> resultList) {
            //resultList로 할 작업 작성
        }

        @Override
        public void onDataLoadError(String errorMessage) {
            //오류 발생
        }

        @Override
        public void onSelectOneLoaded (List<Object> resultList) {}
     });
     */

    // 콜렉션의 데이터 중 targetField 필드가 targetValue인 문서의 resultField 필드의 값을 리턴
    // 사용법은 아래에
    // String collection : 콜렉션명, String targetField : 찾을 속성명, Object targetValue : 찾는 값, String resultField : 반환할 필드명
    public void selectOne(String collection, String targetField, Object targetValue, String resultField, OnDataLoadedListener listener) {
        db.collection(collection)
                .whereEqualTo(targetField, targetValue)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Object resultValue = document.get(resultField);

                        if (resultValue != null) {
                            Log.d(TAG, "데이터 검색 성공");
                            listener.onSelectOneLoaded(resultValue);
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
    fd.selectOne("test", "속성3", 3333, "속성2", new OnDataLoadedListener() {
                    @Override
                    public void onSelectOneLoaded(Object resultValue) {
                        // resultValue로 할 작업 작성
                    }

                    @Override
                    public void onDataLoadError(String errorMessage) {
                        // 오류 발생
                    }

                    @Override
                    public void onSelectAllLoaded (List<Object> resultList) {}
                });
     */

    public void login(String id, String pw, OnLoginResultListener listener) {
        db.collection("account")
                .whereEqualTo("id", id)
                .whereEqualTo("pw", pw)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.size() > 0) {
                        Log.d(TAG, "로그인 성공");
                        listener.onLoginSuccess();
                    } else {
                        Log.d(TAG, "아이디 또는 비밀번호가 일치하지 않음");
                        listener.onLoginFailure("아이디 또는 비밀번호가 일치하지 않습니다.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "로그인 중 오류", e);
                    listener.onLoginFailure(e.getMessage());
                });
    }
}
