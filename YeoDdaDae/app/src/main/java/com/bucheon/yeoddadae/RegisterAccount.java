package com.bucheon.yeoddadae;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.regex.Pattern;

public class RegisterAccount extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        FirestoreDatabase fd = new FirestoreDatabase();

        Button backBtn = (Button) findViewById(R.id.registerBackBtn);
        EditText idTxt = (EditText) findViewById(R.id.registerIdTxt);
        EditText pwTxt = (EditText) findViewById(R.id.registerPwTxt);
        Button registerBtn = (Button) findViewById(R.id.registerBtn);

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
                String pw = pwTxt.getText().toString();

                if (id.length() <= 5) {
                    return;
                }
                else if (pw.length() <= 5) {
                    return;
                }
                else {
                    if (!Pattern.matches("^[a-zA-Z0-9]+$", id)) {
                        return;
                    }
                    else if (!Pattern.matches("^[a-zA-Z0-9]+$", id)) {
                        return;
                    }
                    else {
                        fd.duplicationCheck("account", "id", id,  new OnFirestoreDataLoadedListener() {
                            @Override
                            public void onDataLoaded(Object isNotDuplication) {
                                if ((Boolean) isNotDuplication == true) {
                                    HashMap<String, Object> newAccount = new HashMap<String, Object>();

                                    newAccount.put("id", id);
                                    newAccount.put("pw", pw);
                                    newAccount.put("isAdmin", false);

                                    fd.insertData("account", newAccount);
                                    finish();
                                }
                                else {

                                }
                            }

                            @Override
                            public void onDataLoadError(String errorMessage) {
                                // Handle login failure
                            }
                        });
                    }
                }
            }
        });
    }
}
