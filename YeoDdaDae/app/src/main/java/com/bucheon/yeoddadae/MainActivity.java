package com.bucheon.yeoddadae;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    String loginId = null;
    boolean isAdmin = false;

    final int loginIntentRequestCode = 1;
    final int sttIntentRequestCode = 2;

    private SpeechRecognizer speechRecognizer;
    private final Handler handler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button toLoginBtn = (Button) findViewById(R.id.toLoginBtn);
        TextView nowIdTxt = (TextView) findViewById(R.id.nowId);
        TextView isAdminTxt = (TextView) findViewById(R.id.isAdmin);
        TextView toSttBtn = (TextView) findViewById(R.id.toSttBtn);

        nowIdTxt.setText(loginId);
        isAdminTxt.setText(Boolean.toString(isAdmin));

        toLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                if (loginId == null) {
                    Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
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

        toSttBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sttIntent = new Intent(getApplicationContext(), SttActivity.class);
                startActivityForResult(sttIntent, sttIntentRequestCode);
            }
        });

        // SpeechRecognizer 초기화
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
        // 웨이크업 워드를 듣기 시작
        startListeningForWakeUpWord();
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

    private void startListeningForWakeUpWord() {
        // 음성 인식을 위한 Intent 생성
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR"); // 언어를 한국어로 설정

        // 듣기 시작
        speechRecognizer.startListening(intent);

        // RecognitionListener 설정
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                // 이 메소드에 대한 구현은 필요하지 않습니다
            }

            @Override
            public void onResults(Bundle results) {
                List<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                if (matches != null && matches.size() > 0) {
                    String wakeUpWord = matches.get(0);

                    // 웨이크업 워드가 감지되었는지 확인
                    if (wakeUpWord.contains("여따대")) {
                        // UI 업데이트
                        TextView stt = findViewById(R.id.stt);
                        stt.setText("듣는 중");

                        // 5초 후에 메인 명령을 듣기 시작
                        handler.postDelayed(() -> startListeningForMainCommand(), 5000);
                    } else {
                        // 계속해서 웨이크업 워드를 듣기 시작
                        startListeningForWakeUpWord();
                    }
                }
            }

            @Override
            public void onError(int error) {
                // 필요하다면 음성인식 오류를 처리합니다
            }

            // 추가된 메소드
            @Override
            public void onRmsChanged(float rmsdB) {
                // 이 메소드에 대한 구현은 필요하지 않습니다
            }

            @Override
            public void onBeginningOfSpeech() {
                // 이 메소드에 대한 구현은 필요하지 않습니다
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
                // 이 메소드에 대한 구현은 필요하지 않습니다
            }

            @Override
            public void onEndOfSpeech() {
                // 이 메소드에 대한 구현은 필요하지 않습니다
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                // 이 메소드에 대한 구현은 필요하지 않습니다
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
                // 이 메소드에 대한 구현은 필요하지 않습니다
            }
        });
    }
    private void startListeningForMainCommand() {
        // 음성 인식을 위한 Intent 생성
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR"); // 언어를 한국어로 설정

        // 듣기 시작
        speechRecognizer.startListening(intent);

        // RecognitionListener 설정
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                // 이 메소드에 대한 구현은 필요하지 않습니다
            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int i) {

            }

            @Override
            public void onResults(Bundle results) {
                String mainCommand;
                List<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                if (matches != null && matches.size() > 0) {
                    mainCommand = matches.get(0);

                    if (mainCommand.contains("로그인")) {
                        if (loginId == null) {
                            Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
                            startActivityForResult(loginIntent, loginIntentRequestCode);
                        }

                    }
                    else if (mainCommand.contains("로그아웃")) {
                        if (loginId != null) {
                            loginId = null;
                            isAdmin = false;

                            Button toLoginBtn = (Button) findViewById(R.id.toLoginBtn);
                            TextView nowIdTxt = (TextView) findViewById(R.id.nowId);
                            TextView isAdminTxt = (TextView) findViewById(R.id.isAdmin);

                            nowIdTxt.setText(loginId);
                            isAdminTxt.setText(Boolean.toString(isAdmin));

                            toLoginBtn.setText("로그인");
                        }
                    }
                }

                // 다시 웨이크업 워드를 듣기 시작
                startListeningForWakeUpWord();
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }

            // ... (다른 RecognitionListener 메소드)
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // SpeechRecognizer 자원 해제
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }
}