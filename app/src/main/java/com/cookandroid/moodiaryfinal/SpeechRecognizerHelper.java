package com.cookandroid.moodiaryfinal;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.widget.Toast;

import java.util.ArrayList;

public class SpeechRecognizerHelper {

    public interface Callback {
        void onSpeechResult(String result);
    }

    private final Context context;
    private final Callback callback;
    private final SpeechRecognizer speechRecognizer;
    private final Intent recognizerIntent;

    private String lastResult = "";

    public SpeechRecognizerHelper(Context context, Callback callback) {
        this.context = context;
        this.callback = callback;

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);

        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");

        initListener();
    }

    private void initListener() {
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override public void onReadyForSpeech(Bundle params) {}
            @Override public void onBeginningOfSpeech() {}
            @Override public void onRmsChanged(float rmsdB) {}
            @Override public void onBufferReceived(byte[] buffer) {}
            @Override public void onEndOfSpeech() {}
            @Override public void onPartialResults(Bundle partialResults) {}
            @Override public void onEvent(int eventType, Bundle params) {}

            @Override
            public void onError(int error) {
                Toast.makeText(context, "🎙️ 오류 발생: " + error, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String result = matches.get(0);
                    if (!result.equals(lastResult)) {
                        lastResult = result;
                        callback.onSpeechResult(result);
                    }
                }
            }
        });
    }

    public void startListening() {
        speechRecognizer.startListening(recognizerIntent);
    }

    public void destroy() {
        speechRecognizer.destroy();
    }
}
