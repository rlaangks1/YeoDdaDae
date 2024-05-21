package com.bucheon.yeoddadae;

import static com.google.android.exoplayer2.ExoPlayerLibraryInfo.TAG;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.skt.Tmap.TMapView;

public class StartActivity extends AppCompatActivity {
    final int loginIntentRequestCode = 1;
    String loginId;
    boolean isAdmin;
    ImageButton toLoginBtn;
    Button loginSkipBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        toLoginBtn = findViewById(R.id.toLoginBtn);
        loginSkipBtn = findViewById(R.id.loginSkipBtn);

        toLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivityForResult(loginIntent, loginIntentRequestCode);
            }
        });

        loginSkipBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent skipLoginIntent = new Intent(getApplicationContext(), MainActivity.class);
                skipLoginIntent.putExtra("loginId", "kim111");
                skipLoginIntent.putExtra("isAdmin", true);
                startActivity(skipLoginIntent);
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == loginIntentRequestCode) {
            if (resultCode == RESULT_OK) {
                loginId = data.getStringExtra("loginId");
                isAdmin = data.getBooleanExtra("isAdmin", false);

                Intent mainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
                mainActivityIntent.putExtra("loginId", loginId);
                mainActivityIntent.putExtra("isAdmin", isAdmin);
                startActivity(mainActivityIntent);
                finish();
            }
        }
    }
}
