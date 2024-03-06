package com.bucheon.yeoddadae;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

/*
public class Stt extends AppCompatActivity {
    String sttString;

    void sttStart () {
        // Create a SpeechRecognizer
        SpeechRecognizer speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR"); // Set language to Korean

        // Start listening
        speechRecognizer.startListening(intent);

        // Set up RecognitionListener
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                // 이 메소드에 대한 구현은 필요하지 않습니다
            }

            @Override
            public void onResults(Bundle results) {
                // Get recognition results
                List<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                if (matches != null && matches.size() > 0) {
                    // Use the first match as the recognized string
                    String sttString = matches.get(0);
                }
            }

            // RecognitionListener의 다른 메소드를 필요에 따라 구현합니다

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

    String getSttString() {
        return sttString;
    }
}
*/