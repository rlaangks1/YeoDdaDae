package com.bucheon.yeoddadae;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        FirestoreDatabase fd = new FirestoreDatabase();

        ImageButton backBtn = (ImageButton) findViewById(R.id.loginBackBtn);
        EditText idTxt = (EditText) findViewById(R.id.loginIdTxt);
        EditText pwTxt = (EditText) findViewById(R.id.loginPwTxt);
        Button loginBtn = (Button) findViewById(R.id.loginBtn);
        Button registerBtn = (Button) findViewById(R.id.toRegisterBtn);

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

                if (id.length() <= 5) {
                    return;
                }
                else if (pw.length() <= 5) {
                    return;
                }
                else {
                    fd.login(id, pw, new OnFirestoreDataLoadedListener() {
                        @Override
                        public void onDataLoaded(Object isAdmin) {
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("loginId", id);
                            resultIntent.putExtra("isAdmin", (Boolean) isAdmin);
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        }

                        @Override
                        public void onDataLoadError(String errorMessage) {
                            // Handle login failure
                        }
                    });
                }
            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(getApplicationContext(), RegisterAccount.class);
                startActivity(registerIntent);
            }
        });
    }
}
