package com.bucheon.yeoddadae;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class UserManagementActivity extends AppCompatActivity {
    UserAdapter ua = new UserAdapter();

    ImageButton userManagementBackBtn;
    EditText searchContentEditTxt;
    ImageButton searchTxtClearBtn;
    ImageButton searchBtn;
    ListView userManagementListView;
    TextView userManagementNoTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_management);

        userManagementBackBtn = findViewById(R.id.userManagementBackBtn);
        searchContentEditTxt = findViewById(R.id.searchContentEditTxt);
        searchTxtClearBtn = findViewById(R.id.searchTxtClearBtn);
        searchBtn = findViewById(R.id.searchBtn);
        userManagementListView = findViewById(R.id.userManagementListView);
        userManagementNoTxt = findViewById(R.id.userManagementNoTxt);

        userManagementBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        searchContentEditTxt.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    searchBtn.callOnClick();
                }

                return false;
            }
        });

        searchContentEditTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (searchContentEditTxt.getText().toString().isEmpty()) {
                    searchTxtClearBtn.setVisibility(View.GONE);
                }
                else {
                    searchTxtClearBtn.setVisibility(View.VISIBLE);
                }
            }
        });

        searchTxtClearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchContentEditTxt.setText("");
                getUsers();
            }
        });

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!replaceNewlinesAndTrim(searchContentEditTxt).isEmpty()) {
                    ua.loadSavedItems();
                    int searchCount = ua.searchUser(replaceNewlinesAndTrim(searchContentEditTxt));

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (searchCount == 0) {
                                userManagementListView.setVisibility(View.GONE);
                                userManagementNoTxt.setVisibility(View.VISIBLE);
                            }
                            else {
                                userManagementListView.setVisibility(View.VISIBLE);
                                userManagementNoTxt.setVisibility(View.GONE);
                            }
                        }
                    });
                }
                else {
                    Toast.makeText(getApplicationContext(), "검색어를 입력해주세요", Toast.LENGTH_SHORT).show();
                }
            }
        });

        userManagementListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                UserItem clickedItem = (UserItem) ua.getItem(position);

                Intent toUserManagementInformationIntent = new Intent(getApplicationContext(), UserManagementInformationActivity.class);

                toUserManagementInformationIntent.putExtra("id", clickedItem.getId());
                toUserManagementInformationIntent.putExtra("email", clickedItem.getEmail());
                toUserManagementInformationIntent.putExtra("ydPoint", clickedItem.getYdPoint());
                toUserManagementInformationIntent.putExtra("registerTimeSec", clickedItem.getRegisterTime().getSeconds());
                toUserManagementInformationIntent.putExtra("registerTimeNanoSec", clickedItem.getRegisterTime().getNanoseconds());

                startActivity(toUserManagementInformationIntent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        getUsers();
    }

    void getUsers() {
        ua.clear();
        searchContentEditTxt.setText("");

        FirestoreDatabase fd = new FirestoreDatabase();
        fd.selectUsers(new OnFirestoreDataLoadedListener() {
            @Override
            public void onDataLoaded(Object data) {
                ArrayList<UserItem> result = (ArrayList<UserItem>) data;

                for (UserItem item : result) {
                    ua.addItem(item);
                }

                ua.sortByRegisterTime();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (ua == null || ua.getCount() == 0) {
                            userManagementListView.setVisibility(View.GONE);
                            userManagementNoTxt.setVisibility(View.VISIBLE);
                        }
                        else {
                            userManagementListView.setAdapter(ua);
                            userManagementListView.setVisibility(View.VISIBLE);
                            userManagementNoTxt.setVisibility(View.GONE);
                        }
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

    String replaceNewlinesAndTrim(EditText et) {
        return et.getText().toString().replaceAll("\\n", " ").trim();
    }
}
