package com.example.germanlearner;

import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class WritingActivity extends AppCompatActivity {

    private EditText editText;
    private Button getFeedbackButton, newTopicButton;
    private TextView feedbackTextView, topicTextView, scoreTextView;
    private ProgressBar progressBar;
    private DatabaseHelper dbHelper;

    // IMPORTANT: Make sure this points to your live Render URL
    private static final String SERVER_URL = "https://german-app-backend-vjdu.onrender.com";
    private static final String FEEDBACK_ENDPOINT = "/feedback";
    private static final String TOPIC_ENDPOINT = "/topic";
    private OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_writing);

        // --- Initialization ---
        dbHelper = new DatabaseHelper(this);
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        // --- View Binding ---
        editText = findViewById(R.id.editText);
        getFeedbackButton = findViewById(R.id.getFeedbackButton);
        newTopicButton = findViewById(R.id.newTopicButton);
        feedbackTextView = findViewById(R.id.feedbackTextView);
        topicTextView = findViewById(R.id.topicTextView);
        scoreTextView = findViewById(R.id.scoreTextView);
        progressBar = findViewById(R.id.progressBar);

        // --- Click Listeners ---
        getFeedbackButton.setOnClickListener(v -> {
            String userText = editText.getText().toString();
            String topic = topicTextView.getText().toString();
            if (userText.trim().isEmpty()) {
                Toast.makeText(this, "Please write something first.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (topic.isEmpty() || topic.startsWith("Could not")) {
                Toast.makeText(this, "Please get a topic first.", Toast.LENGTH_SHORT).show();
                return;
            }
            getFeedbackFromServer(userText, topic);
        });

        newTopicButton.setOnClickListener(v -> getNewTopic());

        // --- Initial Action ---
        getNewTopic();
    }

    void getNewTopic() {
        runOnUiThread(() -> {
            feedbackTextView.setVisibility(View.GONE);
            scoreTextView.setVisibility(View.INVISIBLE);
            topicTextView.setText("Getting new topic...");
        });

        Request request = new Request.Builder().url(SERVER_URL + TOPIC_ENDPOINT).get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("API_FAILURE", "Failed to get topic", e);
                runOnUiThread(() -> topicTextView.setText("Could not fetch topic. Check connection."));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        String topic = jsonResponse.getString("topic");
                        runOnUiThread(() -> topicTextView.setText(topic));
                    } catch (Exception e) {
                        Log.e("API_FAILURE", "Error parsing topic response", e);
                        runOnUiThread(() -> topicTextView.setText("Error parsing topic."));
                    }
                } else {
                    Log.e("API_FAILURE", "Server error getting topic: " + response.code());
                    runOnUiThread(() -> topicTextView.setText("Server error fetching topic."));
                }
            }
        });
    }

    void getFeedbackFromServer(String text, String topic) {
        runOnUiThread(() -> {
            progressBar.setVisibility(View.VISIBLE);
            feedbackTextView.setVisibility(View.GONE);
            scoreTextView.setVisibility(View.INVISIBLE);
        });

        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("text", text);
            jsonObject.put("topic", topic);
        } catch (JSONException e) {
            e.printStackTrace();
            return; // Don't proceed if JSON creation fails
        }

        RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
        Request request = new Request.Builder()
                .url(SERVER_URL + FEEDBACK_ENDPOINT)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("API_FAILURE", "Failed to get feedback", e);
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(WritingActivity.this, "Failed to connect to server.", Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseBody = response.body().string();
                if (response.isSuccessful()) {
                    try {
                        JSONObject json = new JSONObject(responseBody);
                        int score = json.getInt("score");
                        String evaluation = json.getString("evaluation");
                        String correctedText = json.getString("corrected_text");
                        String explanation = json.getString("explanation");

                        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                        dbHelper.addRecord(currentDate, currentTime, score, topic, text, correctedText, evaluation, explanation);

                        final String formattedFeedback = "<b>B2-Niveau Bewertung:</b><br>" + evaluation +
                                "<br><br><b>Korrigierter Text:</b><br>" + formatCorrectedText(correctedText) +
                                "<br><br><b>Fehlererklärung:</b><br>" + explanation.replace("\\n", "<br>");

                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            scoreTextView.setText("Score: " + score);
                            scoreTextView.setVisibility(View.VISIBLE);
                            feedbackTextView.setText(Html.fromHtml(formattedFeedback, Html.FROM_HTML_MODE_COMPACT));
                            feedbackTextView.setVisibility(View.VISIBLE);
                        });

                    } catch (JSONException e) {
                        Log.e("API_FAILURE", "Error parsing successful response: " + responseBody, e);
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(WritingActivity.this, "Error: Invalid response from server.", Toast.LENGTH_LONG).show();
                        });
                    }
                } else {
                    Log.e("API_FAILURE", "Server returned error: " + response.code() + " Body: " + responseBody);
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(WritingActivity.this, "Server error: " + response.code(), Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }

    private String formatCorrectedText(String text) {
        return text.replace("<c>", "<font color='#FF6347'>").replace("</c>", "</font>");
    }
}
