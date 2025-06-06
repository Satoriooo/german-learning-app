// WritingActivity.java
package com.example.germanlearner;

import android.os.Bundle;
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
    private Button getFeedbackButton;
    private Button newTopicButton; // New button
    private TextView feedbackTextView;
    private TextView topicTextView; // New TextView
    private ProgressBar progressBar;

    private static final String SERVER_URL = "http://10.0.0.18:5000"; // Base URL
    private static final String FEEDBACK_ENDPOINT = "/feedback";
    private static final String TOPIC_ENDPOINT = "/topic";

    private OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_writing);

        // Initialize OkHttpClient with increased timeouts
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
        progressBar = findViewById(R.id.progressBar);

        getFeedbackButton.setOnClickListener(v -> {
            String userText = editText.getText().toString();
            if (!userText.isEmpty()) {
                getFeedbackFromServer(userText);
            }
        });

        // Set listener for the new topic button
        newTopicButton.setOnClickListener(v -> getNewTopic());

        // Fetch a topic when the activity starts
        getNewTopic();
    }

    void getNewTopic() {
        Request request = new Request.Builder()
                .url(SERVER_URL + TOPIC_ENDPOINT)
                .get() // This is a GET request
                .build();

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
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }


    void getFeedbackFromServer(String text) {
        runOnUiThread(() -> {
            progressBar.setVisibility(View.VISIBLE);
            feedbackTextView.setVisibility(View.GONE);
        });

        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("text", text);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
        Request request = new Request.Builder()
                .url(SERVER_URL + FEEDBACK_ENDPOINT)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("API_FAILURE", "Failed to connect to server", e);
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    feedbackTextView.setText("Failed to connect to server. Check Logcat for details.");
                    feedbackTextView.setVisibility(View.VISIBLE);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String responseBody = response.body().string();
                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        String feedback = jsonResponse.getString("feedback");
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            feedbackTextView.setText(feedback);
                            feedbackTextView.setVisibility(View.VISIBLE);
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            feedbackTextView.setText("Error parsing server response.");
                            feedbackTextView.setVisibility(View.VISIBLE);
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        feedbackTextView.setText("Server error: " + response.code() + " " + response.message());
                        feedbackTextView.setVisibility(View.VISIBLE);
                    });
                }
            }
        });
    }
}
