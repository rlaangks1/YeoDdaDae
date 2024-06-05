package com.bucheon.yeoddadae;

import static android.content.ContentValues.TAG;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.ExoPlayerLibraryInfo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChangePasswordActivity extends AppCompatActivity {
    FirebaseAuth mAuth;

    Button changePasswordBackBtn;
    EditText changePasswordIdTxt;
    EditText changePasswordEmailTxt;
    Button changePasswordSendEmailBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        changePasswordBackBtn = findViewById(R.id.changePasswordBackBtn);
        changePasswordIdTxt = findViewById(R.id.changePasswordIdTxt);
        changePasswordEmailTxt = findViewById(R.id.changePasswordEmailTxt);
        changePasswordSendEmailBtn = findViewById(R.id.changePasswordSendEmailBtn);

        mAuth = FirebaseAuth.getInstance();

        changePasswordBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                finish();
            }
        });

        changePasswordSendEmailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                String id = changePasswordIdTxt.getText().toString();
                String email = changePasswordEmailTxt.getText().toString();

                if (id.equals("")) {
                    Toast.makeText(getApplicationContext(), "ID를 입력해주세요", Toast.LENGTH_SHORT).show();
                }
                else if (email.equals("")) {
                    Toast.makeText(getApplicationContext(), "이메일을 입력해주세요", Toast.LENGTH_SHORT).show();
                }
                else if (id.length() <= 5 || id.length() >= 21) {
                    Toast.makeText(getApplicationContext(), "ID는 6~20자 입니다", Toast.LENGTH_SHORT).show();
                }
                else if (!isValidEmail(email)) {
                    Toast.makeText(getApplicationContext(), "유효하지 않은 이메일 형식입니다", Toast.LENGTH_SHORT).show();
                }
                else {
                    FirestoreDatabase fd = new FirestoreDatabase();
                    fd.findEmailAndIsAdminById(id, new OnFirestoreDataLoadedListener() {
                        @Override
                        public void onDataLoaded(Object data) {
                            String fDEmail = ((String[]) data)[0];

                            if (email.equals(fDEmail)) {
                                mAuth.sendPasswordResetEmail(email)
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(getApplicationContext(), "이메일을 확인하여 비밀번호 변경 후 다시 로그인하세요", Toast.LENGTH_SHORT).show();
                                                finish();
                                            }
                                            else {
                                                Log.d(ContentValues.TAG, "인증 이메일 전송 실패 : " + task.getException().getMessage());
                                                Toast.makeText(getApplicationContext(), "인증 이메일 전송 실패", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                            else {
                                Toast.makeText(getApplicationContext(), "ID 계정의 이메일이 일치하지 않습니다", Toast.LENGTH_SHORT).show();
                            }
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
    }

    private boolean isValidEmail(String email) {
        String regex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}
