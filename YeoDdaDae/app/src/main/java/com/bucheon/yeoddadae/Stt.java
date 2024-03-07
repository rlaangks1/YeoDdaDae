package com.bucheon.yeoddadae;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;


public class Stt {
    private Context context;
    SpeechRecognizer speechRecognizer;
    CountDownTimer mainCommandTimer;
    
    //0 : 발동단어든는중, 1 : 메인명령어든는중
    int whatListening;



    public Stt(Context context) {
        this.context = context;
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
    }

    void startListeningForWakeUpWord() {
        // 음성 인식을 위한 Intent 생성
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR"); // 언어를 한국어로 설정

        // 듣기 시작

        whatListening = 0;
        speechRecognizer.startListening(intent);

        // RecognitionListener 설정
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                Log.d(TAG, "호출어듣기 : 준비완료");
            }

            @Override
            public void onResults(Bundle results) {
                List<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                if (matches != null && matches.size() > 0) {
                    String wakeUpWord = matches.get(0);

                    Log.d(TAG, "호출어듣기 : 내용 : " + wakeUpWord);

                    // 웨이크업 워드가 감지되었는지 확인
                    if (wakeUpWord.contains("음성 명령")) {
                        Log.d(TAG, "호출어듣기 : 호출어확인성공");
                        startListeningForMainCommand();

                    }
                    else {
                        Log.d(TAG, "호출어듣기 : 호출어가아님");
                        // 계속해서 웨이크업 워드를 듣기 시작
                        startListeningForWakeUpWord();
                    }
                }
                else {
                    // 웨이크업 워드를 듣지 못한 경우에 대한 처리를 여기에 추가
                    Log.d(TAG, "호출어듣기 : 웨이크업 워드를 듣지 못했습니다. 다시 시도합니다.");
                    startListeningForWakeUpWord();
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
                Log.d(TAG, "호출어듣기 : 말하기시작");
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

    String startListeningForMainCommand() {
        String[] mainCommand = {null};

        // Start a timer for 5 seconds
        mainCommandTimer = new CountDownTimer(5000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Do nothing on tick
            }

            @Override
            public void onFinish() {
                // Timer finished, no command received within 5 seconds
                // Switch back to listening for wake-up word
                startListeningForWakeUpWord();
            }
        }.start();

        // 음성 인식을 위한 Intent 생성
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR"); // 언어를 한국어로 설정

        // 듣기 시작
        whatListening = 1;
        speechRecognizer.startListening(intent);
        Log.d(TAG, "메인명령듣기");

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
                List<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                if (matches != null && matches.size() > 0) {
                    mainCommand[0] = matches.get(0);

                    // Cancel the timer as command is received
                    if (mainCommandTimer != null) {
                        mainCommandTimer.cancel();
                    }
                }
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });

        return mainCommand[0];
    }

    void DestroySpeechRecognizer() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }
}
