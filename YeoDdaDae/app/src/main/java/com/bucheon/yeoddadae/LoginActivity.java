package com.bucheon.yeoddadae;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;

public class LoginActivity extends AppCompatActivity {
    FirestoreDatabase fd = new FirestoreDatabase();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ImageButton backBtn = findViewById(R.id.loginBackBtn);
        EditText idTxt = findViewById(R.id.loginIdTxt);
        EditText pwTxt = findViewById(R.id.loginPwTxt);
        Button loginBtn = findViewById(R.id.loginBtn);
        Button registerBtn = findViewById(R.id.toRegisterBtn);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                finish();
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                String id = idTxt.getText().toString();
                String pw = pwTxt.getText().toString();

                if (id.equals("")) {
                    Toast.makeText(getApplicationContext(), "ID를 입력해주세요", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if (pw.equals("")) {
                    Toast.makeText(getApplicationContext(), "비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if (id.length() <= 5 || id.length() >= 21) {
                    Toast.makeText(getApplicationContext(), "ID는 6~20자 입니다", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if (pw.length() <= 5 || pw.length() >= 21) {
                    Toast.makeText(getApplicationContext(), "비밀번호는 6~20자 입니다", Toast.LENGTH_SHORT).show();
                    return;
                }
                else {
                    fd.login(id, pw, new OnFirestoreDataLoadedListener() {
                        @Override
                        public void onDataLoaded(Object isAdmin) {
                            Toast.makeText(getApplicationContext(), "로그인 성공", Toast.LENGTH_SHORT).show();
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("loginId", id);
                            resultIntent.putExtra("isAdmin", (Boolean) isAdmin);
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        }

                        @Override
                        public void onDataLoadError(String errorMessage) {
                            // Handle login failure
                            Toast.makeText(getApplicationContext(), "로그인 실패", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(registerIntent);
            }
        });
    }
}
