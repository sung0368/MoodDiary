package com.cookandroid.moodiaryfinal;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.kherud.llama.InferenceParameters;
import de.kherud.llama.LlamaModel;
import de.kherud.llama.ModelParameters;

public class ModelManager {
    private static ModelManager instance;
    private LlamaModel model;
    private boolean isLoaded = false;

    private ModelManager() {}

    public static synchronized ModelManager getInstance() {
        if (instance == null) {
            instance = new ModelManager();
        }
        return instance;
    }

    public void loadModel(Context context) {
        if (isLoaded) return;

        new Thread(() -> {
            try {
                String modelPath = "/storage/emulated/0/llama-3.2-Korean-Bllossom-3B-gguf-Q4_K_M.gguf";
                File file = new File(modelPath);
                if (!file.exists()) {
                    Log.e("ModelManager", "❌ 모델 파일이 존재하지 않음");
                    return;
                }

                int cores = Runtime.getRuntime().availableProcessors();
                Log.d("ModelManager", "🧩 사용 가능한 CPU 코어 수: " + cores);
                int threads = Math.min(cores, 6); // 최대 6개 스레드로 제한

                ModelParameters params = new ModelParameters()
                        .setModel(modelPath)
                        .setThreads(threads)
                        .setBatchSize(512)
                        .setCtxSize(2048);

                try {
                    params.enableMlock();
                } catch (Exception e) {
                    Log.w("ModelManager", "⚠️ mlock 실패: " + e.getMessage());
                }

                model = new LlamaModel(params);
                isLoaded = true;
                Log.d("ModelManager", "✅ 모델 로딩 완료");
            } catch (Exception e) {
                Log.e("ModelManager", "❌ 모델 로딩 실패", e);
            }
        }).start();
    }


    public boolean isModelReady() {
        return isLoaded && model != null;
    }

    public String infer(String prompt) {
        if (!isModelReady()) return "❌ 모델이 아직 로딩되지 않았습니다.";

        Log.d("ModelManager", "🧠 infer() 호출됨. 프롬프트: " + prompt);

        InferenceParameters inferParams = new InferenceParameters()
                .setPrompt(prompt + "\n")
                .setNPredict(128)
                .setTemperature(0.7f)
                .setTopK(40)
                .setTopP(0.9f)
                .setRepeatPenalty(1.1f)
                .setStopStrings("질문:", "답변:");

        String result = model.complete(inferParams).trim();

        Log.d("ModelManager", "✅ infer() 추론 완료. 결과: " + result);
        return result;
    }

    public void closeModel() {
        if (model != null) model.close();
        isLoaded = false;
    }

    // ModelManager.java 내부
    public String inferWithPrompt(String diaryContent) {
        if (!isModelReady()) return "❌ 모델이 아직 로딩되지 않았습니다.";

        String prompt = "너는 심리상담가야.\n" +
                "아래 일기를 읽고 다음 감정 항목별로 느껴지는 비율을 분석해줘.\n" +
                "각 감정 항목은 반드시 아래 형식으로 제공해줘:\n" +
                "감정 비율은 반드시 숫자로 % 형태로 포함해야 하며, 각 항목이 누락되지 않아야 해.\n" +
                "마지막에 분석한 감정에 따른 피드백(감정에 대한 조언)을 40자 이내로 제공해줘.\n\n" +
                "감정 항목과 피드백은 한 번만 출력해줘. 중복해서 출력하지 마.\n" +
                "아래 응답형식 이외에는 다른걸 쓰지 말아줘\n" +
                "일기:\n" + diaryContent + "\n" +
                "응답 형식:\n" +
                "* 분노: % \n" +
                "* 기쁨: % \n" +
                "* 당황: % \n" +
                "* 슬픔: % \n" +
                "* 불안: % \n" +
                "* 상처: % \n" +
                "* 피드백: 심리상담가로서의 공감 (40자 이내)\n";


        Log.d("ModelManager", "🧠 inferWithPrompt() 호출됨. 프롬프트: " + prompt);

        InferenceParameters params = new InferenceParameters()
                .setPrompt(prompt)
                .setNPredict(100)
                .setTemperature(0.7f)
                .setTopK(20)
                .setTopP(0.85f)
                .setRepeatPenalty(1.1f)
                .setStopStrings("질문:", "답변:");

        String result = model.complete(params).trim();

        Log.d("ModelManager", "✅ inferWithPrompt() 추론 완료. 결과: " + result);
        return result;
    }

    private String extractEmotion(String text, String emotionLabel) {
        String[] lines = text.split("\n");
        for (String line : lines) {
            if (line.contains(emotionLabel)) {
                // 정규식으로 퍼센트 값 추출
                String[] parts = line.split(":");
                if (parts.length > 1 && parts[1].contains("%")) {
                    return parts[1].trim().split("%")[0].trim() + "%";
                }
            }
        }
        return "0%";
    }

    public float extractEmotionValue(String result, String emotionLabel) {
        Pattern pattern = Pattern.compile(emotionLabel + "\\s*:\\s*(\\d+)%");
        Matcher matcher = pattern.matcher(result);
        if (matcher.find()) {
            try {
                return Float.parseFloat(matcher.group(1));  // % 제거된 숫자만
            } catch (NumberFormatException e) {
                return 0f;
            }
        }
        return 0f;
    }


    private String extractFeedback(String text) {
        String[] lines = text.split("\n");
        for (String line : lines) {
            if (line.startsWith("* 피드백:") || line.startsWith("피드백:")) {
                return line.replace("* 피드백:", "").replace("피드백:", "").trim();
            }
        }
        return "";
    }
    public void saveEmotionAnalysisToDB(Context context, String seq, String diaryContent) {
        int attempt = 0;
        final int maxAttempts = 2;
        String result = null;

        while (attempt < maxAttempts) {
            result = inferWithPrompt(diaryContent);

            if (result.startsWith("❌")) {
                Log.e("ModelManager", "모델이 아직 로딩되지 않았습니다.");
                return;
            }

            try {
                // 결과 파싱
                String anger = extractEmotion(result, "분노");
                String joy = extractEmotion(result, "기쁨");
                String embarrassment = extractEmotion(result, "당황");
                String sadness = extractEmotion(result, "슬픔");
                String anxiety = extractEmotion(result, "불안");
                String hurt = extractEmotion(result, "상처");
                String feedback = extractFeedback(result);

                float angerVal = extractEmotionValue(result, "분노");
                float joyVal = extractEmotionValue(result, "기쁨");
                float embarrassmentVal = extractEmotionValue(result, "당황");
                float sadnessVal = extractEmotionValue(result, "슬픔");
                float anxietyVal = extractEmotionValue(result, "불안");
                float hurtVal = extractEmotionValue(result, "상처");

                // ✅ 가장 높은 감정 추출
                Map<String, Float> emotionMap = new HashMap<>();
                emotionMap.put("anger", angerVal);
                emotionMap.put("joy", joyVal);
                emotionMap.put("embarrassment", embarrassmentVal);
                emotionMap.put("sadness", sadnessVal);
                emotionMap.put("anxiety", anxietyVal);
                emotionMap.put("hurt", hurtVal);

                String topEmotion = getTopEmotion(emotionMap);

                // DB 저장
                MyDatabaseHelper dbHelper = new MyDatabaseHelper(context);
                dbHelper.insertOrUpdateEmotion(seq, anger, joy, embarrassment, sadness, anxiety, hurt, feedback, topEmotion);

                // 저장된 값 로그 출력
                Log.d("ModelManager", "✅ 감정 분석 DB 저장 완료:");
                Log.d("ModelManager", "📌 seq: " + seq);
                Log.d("ModelManager", "* 분노: " + anger);
                Log.d("ModelManager", "* 기쁨: " + joy);
                Log.d("ModelManager", "* 당황: " + embarrassment);
                Log.d("ModelManager", "* 슬픔: " + sadness);
                Log.d("ModelManager", "* 불안: " + anxiety);
                Log.d("ModelManager", "* 상처: " + hurt);
                Log.d("ModelManager", "* 피드백: " + feedback);
                Log.d("ModelManager", "* top_emotion: " + topEmotion);
                return; // 성공하면 종료

            } catch (Exception e) {
                Log.e("ModelManager", "❌ 감정 분석 파싱 또는 DB 저장 실패 (시도 " + (attempt + 1) + ")", e);
                attempt++;
            }
        }

        Log.e("ModelManager", "❌ 감정 분석 최종 실패: 모델 응답이 예상된 형식이 아닙니다.\n응답:\n" + result);
    }

    public String getTopEmotion(Map<String, Float> emotionScores) {
        String topEmotion = null;
        float max = Float.MIN_VALUE;
        for (Map.Entry<String, Float> entry : emotionScores.entrySet()) {
            if (entry.getValue() > max) {
                max = entry.getValue();
                topEmotion = entry.getKey();
            }
        }
        return topEmotion;
    }


}