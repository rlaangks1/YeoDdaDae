package com.bucheon.yeoddadae;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ImageButton backBtn = findViewById(R.id.registerBackBtn);
        EditText idTxt = findViewById(R.id.registerIdTxt);
        EditText emailTxt = findViewById(R.id.registerEmailTxt);
        EditText pwTxt = findViewById(R.id.registerPwTxt);
        ImageButton registerBtn = findViewById(R.id.registerBtn);

        mAuth = FirebaseAuth.getInstance();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                finish();
            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = idTxt.getText().toString();
                String email = emailTxt.getText().toString();
                String pw = pwTxt.getText().toString();

                if (id.equals("")) {
                    Toast.makeText(getApplicationContext(), "ID를 입력해주세요", Toast.LENGTH_SHORT).show();
                }
                else if (email.equals("")) {
                    Toast.makeText(getApplicationContext(), "이메일을 입력해주세요", Toast.LENGTH_SHORT).show();
                }
                else if (pw.equals("")) {
                    Toast.makeText(getApplicationContext(), "비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show();
                }
                else if (id.length() <= 5 || id.length() >= 21) {
                    Toast.makeText(getApplicationContext(), "ID는 6~20자이어야 합니다", Toast.LENGTH_SHORT).show();
                }
                else if (!isValidEmail(email)) {
                    Toast.makeText(getApplicationContext(), "유효하지 않은 이메일입니다", Toast.LENGTH_SHORT).show();
                }
                else if (pw.length() <= 5 || pw.length() >= 21) {
                    Toast.makeText(getApplicationContext(), "비밀번호는 6~20자이어야 합니다", Toast.LENGTH_SHORT).show();
                }
                else {
                    registerUser (id, email, pw);
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

    private void registerUser(String id, String email, String pw) {
        mAuth.createUserWithEmailAndPassword(email, pw)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            HashMap<String, Object> newAccount = new HashMap<>();
                            newAccount.put("id", id);
                            newAccount.put("email", email);
                            newAccount.put("pw", pw); // 암호화 필요
                            newAccount.put("isAdmin", false);
                            newAccount.put("ydPoint", 0);
                            newAccount.put("registerTime", FieldValue.serverTimestamp());

                            FirestoreDatabase fd = new FirestoreDatabase();
                            fd.register(id, email, pw, user.getUid(), new OnFirestoreDataLoadedListener() {
                                @Override
                                public void onDataLoaded(Object data) {
                                    Toast.makeText(getApplicationContext(), "회원가입 성공", Toast.LENGTH_SHORT).show();
                                    finish();
                                }

                                @Override
                                public void onDataLoadError(String errorMessage) {
                                    Toast.makeText(getApplicationContext(), "회원 문서 추가 중 오류 발생", Toast.LENGTH_SHORT).show();
                                }
                            });
                            
                    }
                    else {
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            Toast.makeText(getApplicationContext(), "이미 존재하는 이메일입니다", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Log.d(TAG, "회원가입 실패 : " + task.getException().getMessage());
                            Toast.makeText(getApplicationContext(), "회원가입 실패", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
