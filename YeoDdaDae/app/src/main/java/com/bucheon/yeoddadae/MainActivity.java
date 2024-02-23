package com.bucheon.yeoddadae;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    String account = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = (Button) findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                FirestoreDatabase fd = new FirestoreDatabase();

                if (account == null) {
                    Intent loginIntent = new Intent(getApplicationContext(), loginActivity.class);
                    startActivityForResult(loginIntent, 1);
                }
                else {

                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                // 결과가 성공적으로 돌아왔을 때
                account = data.getStringExtra("accountId");
                TextView nowIdTxt = (TextView) findViewById(R.id.nowId);
                nowIdTxt.setText(account);

            } else if (resultCode == RESULT_CANCELED) {
                // 사용자가 선택을 취소했을 때
            }
        }
    }
}