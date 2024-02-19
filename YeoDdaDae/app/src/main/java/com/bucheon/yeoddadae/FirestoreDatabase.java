package com.bucheon.yeoddadae;

import static android.content.ContentValues.TAG;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class FirestoreDatabase {
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    //String collection : 콜렉션명, HashMap<String, Object> hm : 데이터의 속성과 값을 가진 HashMap
    public void insertData (String collection, HashMap<String, Object> hm) {
        db.collection(collection)
                .add(hm)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }

    //String collection : 콜렉션명, String field : 속성명, Object targetValue : 찾는값
    public boolean duplicationCheck(String collection, String field, Object targetValue) {
        final boolean[] isDuplicate = {false};

        db.collection(collection)
                .whereEqualTo(field, targetValue)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.size() > 0) {
                        // Document with the specified field value already exists
                        isDuplicate[0] = true;
                        Log.d(TAG, "Document with " + field + " = " + targetValue + " already exists.");
                    } else {
                        // No document with the specified field value found
                        isDuplicate[0] = false;
                        Log.d(TAG, "No document with " + field + " = " + targetValue + " found.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking for duplication", e);
                    // Handle the failure scenario, you may want to return false or throw an exception
                });

        return isDuplicate[0];
    }

    //String collection : 콜렉션명, String resultField : 찾을속성, String orderField : 정렬속성, String targetField : 찾는속성, Object targetValue : 찾는값
    public List<Object> selectData(String collection, String resultField, String orderField, String targetField, Object targetValue) {
        List<Object> resultList = new ArrayList<>();

        return resultList;
    }
}
