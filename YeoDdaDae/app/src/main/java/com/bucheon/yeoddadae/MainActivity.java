package com.bucheon.yeoddadae;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                FirestoreDatabase fd = new FirestoreDatabase();

                fd.selectOne("test", "속성3", 3333, "속성2", new OnDataLoadedListener() {
                    @Override
                    public void onSelectOneLoaded(Object resultValue) {
                        Log.d(TAG, resultValue.toString());
                    }
                    @Override
                    public void onSelectAllLoaded (List<Object> resultList) {}

                    @Override
                    public void onDataLoadError(String errorMessage) {
                        // 오류 발생
                    }
                });
            }
        });
    }
}