package com.bucheon.yeoddadae;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

public class SttService extends Service {

    private static final String TAG = "SttService";
    private SpeechRecognizer speechRecognizer;
    private SttCallback sttCallback;
    private boolean isListeningForWakeUpWord = false;

    public class SttBinder extends Binder {
        public SttService getService() {
            return SttService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new SttBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        AudioManager audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        listenForWakeUpWord();
        return START_STICKY;
    }

    private void stopContinuousListening() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }

    public void startListeningForMainCommand() {
        if (isListeningForWakeUpWord) {
            stopContinuousListening();
            listenForMainCommand();
        }
    }

    public void stopListening() {
        stopContinuousListening();
    }

    private void listenForWakeUpWord() {
        isListeningForWakeUpWord = true;

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
                Log.d(TAG, "호출어듣기 : 준비완료");
                if (sttCallback != null) {
                    sttCallback.onUpdateUI("호출어듣는중");
                }
            }

            @Override
            public void onBeginningOfSpeech() {Log.d(TAG, "호출어듣기 : 말하기시작");}

            @Override
            public void onResults(Bundle results) {
                List<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                if (matches != null && matches.size() > 0) {
                    String wakeUpWord = matches.get(0);

                    Log.d(TAG, "호출어듣기 : 내용 : " + wakeUpWord);

                    // 웨이크업 워드가 감지되었는지 확인
                    if (wakeUpWord.contains("음성 명령")) {
                        Log.d(TAG, "호출어듣기 : 호출어확인성공");
                        isListeningForWakeUpWord = false;  // 플래그를 false로 설정
                        listenForMainCommand();
                    }
                    else {
                        Log.d(TAG, "호출어듣기 : 호출어가아님");
                        // 계속해서 웨이크업 워드를 듣기 시작
                        // 돌아가기를 처리하기 전에 플래그를 확인합니다.
                        if (isListeningForWakeUpWord) {
                            listenForWakeUpWord();
                        }
                    }
                }
                else {
                    // 웨이크업 워드를 듣지 못한 경우에 대한 처리를 여기에 추가
                    Log.d(TAG, "호출어듣기 : 웨이크업 워드를 듣지 못했습니다. 다시 시도합니다.");
                    listenForWakeUpWord();
                }
            }

            @Override
            public void onError(int error) {
                switch (error) {
                    case SpeechRecognizer.ERROR_NO_MATCH:
                        // 음성을 인식하지 못했을 때의 처리를 여기에 추가
                        Log.d(TAG, "호출어듣기 : 음성을 인식하지 못했습니다");
                        listenForWakeUpWord(); // 다시 웨이크업 워드를 듣기 시작
                        break;
                    case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                        // 음성 입력이 타임아웃 되었을 때의 처리를 여기에 추가
                        Log.d(TAG, "호출어듣기 : 타임아웃");
                        listenForWakeUpWord(); // 다시 웨이크업 워드를 듣기 시작
                        break;
                    // 다른 오류 코드에 대한 처리를 추가할 수 있습니다.
                    default:
                        // 그 외의 경우에 대한 처리
                        break;
                }
            }

            @Override
            public void onRmsChanged(float rmsdB) {}
            @Override
            public void onBufferReceived(byte[] buffer) {}
            @Override
            public void onEndOfSpeech() {}
            @Override
            public void onPartialResults(Bundle partialResults) {}
            @Override
            public void onEvent(int eventType, Bundle params) {}
        });
    }

    private void listenForMainCommand() {
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
                Log.d(TAG, "명령어듣기 : 준비완료");
                if (sttCallback != null) {
                    sttCallback.onUpdateUI("메인명령어듣는중");
                }
            }

            @Override
            public void onBeginningOfSpeech() {
                Log.d(TAG, "명령어듣기 : 말하기시작");
            }

            @Override
            public void onResults(Bundle results) {
                List<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                if (matches != null && matches.size() > 0) {
                    String mainCommand = matches.get(0);

                    Log.d(TAG, "명령어듣기 : 내용 : " + mainCommand);

                    // 콜백을 사용하여 mainCommand 전달
                    if (sttCallback != null) {
                        sttCallback.onMainCommandReceived(mainCommand);
                    }
                }

                // 메인 명령을 처리한 후, 플래그에 따라 웨이크업 워드를 계속 듣을지 결정합니다.
                /*
                if (isListeningForWakeUpWord) {
                    listenForWakeUpWord();
                }
                */
            }

            @Override
            public void onError(int error) {
                switch (error) {
                    case SpeechRecognizer.ERROR_NO_MATCH:
                        // 음성을 인식하지 못했을 때의 처리를 여기에 추가
                        Log.d(TAG, "명령어듣기 : 음성을 인식하지 못했습니다");
                        listenForWakeUpWord();
                        break;
                    case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                        // 음성 입력이 타임아웃 되었을 때의 처리를 여기에 추가
                        Log.d(TAG, "명령어듣기 : 타임아웃");
                        listenForWakeUpWord();
                        break;
                    // 다른 오류 코드에 대한 처리를 추가할 수 있습니다.
                    default:
                        // 그 외의 경우에 대한 처리
                        break;
                }
            }

            @Override
            public void onRmsChanged(float rmsdB) {}
            @Override
            public void onBufferReceived(byte[] buffer) {}
            @Override
            public void onEndOfSpeech() {}
            @Override
            public void onPartialResults(Bundle partialResults) {}
            @Override
            public void onEvent(int eventType, Bundle params) {}
        });
    }

    public interface SttCallback {
        void onMainCommandReceived(String mainCommand);
        void onUpdateUI(String message);
    }

    public void setSttCallback(SttCallback callback) {
        sttCallback = callback;
    }

    @Override
    public void onDestroy() {
        stopContinuousListening();
        super.onDestroy();
    }
}