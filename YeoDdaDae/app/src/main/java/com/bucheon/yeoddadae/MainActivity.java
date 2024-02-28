package com.bucheon.yeoddadae;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    String loginId = null;
    boolean isAdmin = false;

    final int loginIntentRequestCode = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button toLoginBtn = (Button) findViewById(R.id.toLoginBtn);
        TextView nowIdTxt = (TextView) findViewById(R.id.nowId);
        TextView isAdminTxt = (TextView) findViewById(R.id.isAdmin);

        nowIdTxt.setText(loginId);
        isAdminTxt.setText(Boolean.toString(isAdmin));

        toLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                FirestoreDatabase fd = new FirestoreDatabase();

                if (loginId == null) {
                    Intent loginIntent = new Intent(getApplicationContext(), loginActivity.class);
                    startActivityForResult(loginIntent, loginIntentRequestCode);
                }
                else {
                    loginId = null;
                    isAdmin = false;

                    nowIdTxt.setText(loginId);
                    isAdminTxt.setText(Boolean.toString(isAdmin));

                    toLoginBtn.setText("로그인");
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Button toLoginBtn = (Button) findViewById(R.id.toLoginBtn);
        TextView nowIdTxt = (TextView) findViewById(R.id.nowId);
        TextView isAdminTxt = (TextView) findViewById(R.id.isAdmin);

        if (requestCode == loginIntentRequestCode) {
            if (resultCode == RESULT_OK) {
                loginId = data.getStringExtra("loginId");
                isAdmin = data.getBooleanExtra("isAdmin", false);

                nowIdTxt.setText(loginId);
                isAdminTxt.setText(Boolean.toString(isAdmin));

                toLoginBtn.setText("로그아웃");
            }
        }
    }
}