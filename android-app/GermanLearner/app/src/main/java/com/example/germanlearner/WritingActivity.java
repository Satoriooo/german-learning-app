// WritingActivity.java
// Major update: handles new JSON response, saves score, formats feedback with colors.
package com.example.germanlearner;

import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
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
    private TextView feedbackTextView, topicTextView, scoreTextView; // Added scoreTextView
    private ProgressBar progressBar;
    private DatabaseHelper dbHelper;

    private static final String SERVER_URL = "https://german-app-backend-vjdu.onrender.com"; // <-- IMPORTANT: USE YOUR RENDER URL
    private static final String FEEDBACK_ENDPOINT = "/feedback";
    private static final String TOPIC_ENDPOINT = "/topic";
    private OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_writing);

        dbHelper = new DatabaseHelper(this);

        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        editText = findViewById(R.id.editText);
        getFeedbackButton = findViewById(R.id.getFeedbackButton);
        newTopicButton = findViewById(R.id.newTopicButton);
        feedbackTextView = findViewById(R.id.feedbackTextView);
        topicTextView = findViewById(R.id.topicTextView);
        scoreTextView = findViewById(R.id.scoreTextView); // Initialize score TextView
        progressBar = findViewById(R.id.progressBar);

        getFeedbackButton.setOnClickListener(v -> {
            String userText = editText.getText().toString();
            String topic = topicTextView.getText().toString();
            if (!userText.isEmpty() && !topic.isEmpty()) {
                getFeedbackFromServer(userText, topic);
            }
        });

        newTopicButton.setOnClickListener(v -> getNewTopic());
        getNewTopic();
    }

    // ... (getNewTopic method remains the same)

    void getFeedbackFromServer(String text, String topic) {
        runOnUiThread(() -> { /* ... same as before */ });

        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("text", text);
            jsonObject.put("topic", topic); // Send the topic to the server
        } catch (JSONException e) { e.printStackTrace(); }

        RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
        Request request = new Request.Builder()
                .url(SERVER_URL + FEEDBACK_ENDPOINT)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) { /* ... same as before */ }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String responseBody = response.body().string();
                    try {
                        JSONObject json = new JSONObject(responseBody);
                        int score = json.getInt("score");
                        String evaluation = json.getString("evaluation");
                        String correctedText = json.getString("corrected_text");
                        String explanation = json.getString("explanation");

                        // Save the score to the database
                        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                        dbHelper.addScore(currentDate, currentTime, score);

                        // Format the output for display
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

                    } catch (JSONException e) { /* ... handle error */ }
                } else { /* ... handle error */ }
            }
        });
    }

    // New method to format corrected text with red color
    private String formatCorrectedText(String text) {
        return text.replace("<c>", "<font color='red'>").replace("</c>", "</font>");
    }

    // getNewTopic method (no changes from previous version)
    void getNewTopic() {
        Request request = new Request.Builder().url(SERVER_URL + TOPIC_ENDPOINT).get().build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> topicTextView.setText("Could not fetch topic."));
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String responseBody = response.body().string();
                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        String topic = jsonResponse.getString("topic");
                        runOnUiThread(() -> topicTextView.setText(topic));
                    } catch (JSONException e) { e.printStackTrace(); }
                }
            }
        });
    }
}