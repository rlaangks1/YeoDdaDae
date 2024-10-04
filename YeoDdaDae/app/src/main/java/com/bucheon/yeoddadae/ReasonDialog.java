package com.bucheon.yeoddadae;

import static android.content.ContentValues.TAG;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.HashMap;

public class ReasonDialog extends Dialog {
    String firestoreDocumentId;
    Context context;
    ReasonAdapter ra;
    FirestoreDatabase fd;

    ImageButton reasonBackBtn;
    ListView reasonListView;
    TextView reasonNoTxt;

    public ReasonDialog(String firestoreDocumentId, @NonNull Context context) {
        super(context);
        this.firestoreDocumentId = firestoreDocumentId;
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_reason);

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes(params);

        reasonBackBtn = findViewById(R.id.reasonBackBtn);
        reasonListView = findViewById(R.id.reasonListView);
        reasonNoTxt = findViewById(R.id.reasonNoTxt);

        ra = new ReasonAdapter();

        reasonBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        getReasons();
    }

    void getReasons() {
        ra.clear();

        fd = new FirestoreDatabase();
        fd.loadReasons(firestoreDocumentId, new OnFirestoreDataLoadedListener() {
            @Override
            public void onDataLoaded(Object data) {
                ArrayList<HashMap<String, Object>> result = (ArrayList<HashMap<String, Object>>) data;

                for (HashMap<String, Object> one : result) {
                    ra.addItem(new ReasonItem((String) one.get("id"), (String) one.get("reason"), (Timestamp) one.get("reasonUpTime")));
                }

                reasonListView.setAdapter(ra);

                if (ra.getCount() == 0) {
                    reasonListView.setVisibility(View.GONE);
                    reasonNoTxt.setVisibility(View.VISIBLE);
                }
                else {
                    reasonListView.setVisibility(View.VISIBLE);
                    reasonNoTxt.setVisibility(View.GONE);
                }
            }

            @Override
            public void onDataLoadError(String errorMessage) {
                Log.d(TAG, errorMessage);
                Toast.makeText(context, "오류 발생", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
