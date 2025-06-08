// RecordDetailActivity.java
// --- THIS IS A BRAND NEW FILE ---
// Create this new Java class. It controls the new detail screen.
package com.example.germanlearner;

import android.database.Cursor;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class RecordDetailActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private TextView topic, originalText, correctedText, evaluation, explanation, score;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_detail);

        dbHelper = new DatabaseHelper(this);

        topic = findViewById(R.id.detailTopic);
        originalText = findViewById(R.id.detailOriginal);
        correctedText = findViewById(R.id.detailCorrected);
        evaluation = findViewById(R.id.detailEvaluation);
        explanation = findViewById(R.id.detailExplanation);
        score = findViewById(R.id.detailScore);

        long recordId = getIntent().getLongExtra("RECORD_ID", -1);
        if (recordId != -1) {
            loadRecordDetails(recordId);
        }
    }

    private void loadRecordDetails(long id) {
        Cursor cursor = dbHelper.getRecordById(id);
        if (cursor != null && cursor.moveToFirst()) {
            topic.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TOPIC)));
            originalText.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ORIGINAL_TEXT)));

            // Format the corrected text with red color
            String corrected = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CORRECTED_TEXT));
            correctedText.setText(Html.fromHtml(formatCorrectedText(corrected), Html.FROM_HTML_MODE_COMPACT));

            evaluation.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EVALUATION)));
            explanation.setText(cursor.getString(cursor.getColumnIndexOrThrow(Database_Helper.COLUMN_EXPLANATION)).replace("\\n", "\n"));
            score.setText(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SCORE))));

            cursor.close();
        }
    }

    private String formatCorrectedText(String text) {
        return text.replace("<c>", "<font color='red'>").replace("</c>", "</font>");
    }
}