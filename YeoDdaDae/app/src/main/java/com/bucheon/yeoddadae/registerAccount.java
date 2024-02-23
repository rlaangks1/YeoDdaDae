package com.bucheon.yeoddadae;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;

public class registerAccount extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        FirestoreDatabase fd = new FirestoreDatabase();

        EditText idTxt = (EditText) findViewById(R.id.registerIdTxt);
        EditText pwTxt = (EditText) findViewById(R.id.registerPwTxt);
        Button registerBtn = (Button) findViewById(R.id.registerBtn);

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = idTxt.getText().toString();
                String pw = pwTxt.getText().toString();

                if (id.length() <= 6) {

                }
                else if (pw.length() <= 6) {

                }
                else {
                    HashMap<String, Object> newAccount = new HashMap<String, Object>();

                    newAccount.put("id", id);
                    newAccount.put("pw", pw);

                    fd.insertData("account", newAccount);
                }
            }
        });
    }
}
