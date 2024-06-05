package com.bucheon.yeoddadae;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.ExoPlayerLibraryInfo;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.auth.User;

public class LoginActivity extends AppCompatActivity {
    FirebaseAuth mAuth;

    ImageButton backBtn;
    EditText idTxt;
    EditText pwTxt;
    ImageButton loginBtn;
    ImageButton registerBtn;
    TextView toChangePasswordTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        backBtn = findViewById(R.id.loginBackBtn);
        idTxt = findViewById(R.id.loginIdTxt);
        pwTxt = findViewById(R.id.loginPwTxt);
        loginBtn = findViewById(R.id.loginBtn);
        registerBtn = findViewById(R.id.toRegisterBtn);
        toChangePasswordTxt = findViewById(R.id.toChangePasswordTxt);

        mAuth = FirebaseAuth.getInstance();
        mAuth.signOut();

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
                }
                else if (pw.equals("")) {
                    Toast.makeText(getApplicationContext(), "비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show();
                }
                else if (id.length() <= 5 || id.length() >= 21) {
                    Toast.makeText(getApplicationContext(), "ID는 6~20자 입니다", Toast.LENGTH_SHORT).show();
                }
                else if (pw.length() <= 5 || pw.length() >= 21) {
                    Toast.makeText(getApplicationContext(), "비밀번호는 6~20자 입니다", Toast.LENGTH_SHORT).show();
                }
                else {
                    FirestoreDatabase fd = new FirestoreDatabase();

                    fd.findEmailAndIsAdminById(id, new OnFirestoreDataLoadedListener() {
                        @Override
                        public void onDataLoaded(Object data) {
                            String email = ((String[]) data)[0];
                            boolean isAdmin = Boolean.parseBoolean(((String[]) data)[1]);

                            mAuth.signInWithEmailAndPassword(email, pw)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(getApplicationContext(), "로그인 성공", Toast.LENGTH_SHORT).show();
                                            Intent resultIntent = new Intent();
                                            resultIntent.putExtra("loginId", id);
                                            resultIntent.putExtra("isAdmin", isAdmin);
                                            setResult(RESULT_OK, resultIntent);
                                            finish();
                                        }
                                        else {
                                            Toast.makeText(getApplicationContext(), "로그인 실패", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }

                        @Override
                        public void onDataLoadError(String errorMessage) {
                            Log.d(TAG, errorMessage);
                            Toast.makeText(getApplicationContext(), "오류 발생", Toast.LENGTH_SHORT).show();
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

        toChangePasswordTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent changePwIntent = new Intent(getApplicationContext(), ChangePasswordActivity.class);
                startActivity(changePwIntent);
            }
        });
    }
}
