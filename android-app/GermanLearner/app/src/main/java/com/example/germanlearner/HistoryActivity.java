// HistoryActivity.java
// Updated to open a detail view when a row is tapped.
package com.example.germanlearner;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import androidx.appcompat.app.AppCompatActivity;

public class HistoryActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        dbHelper = new DatabaseHelper(this);
        listView = findViewById(R.id.historyListView);

        displayHistory();

        // --- NEW: Set click listener for list items ---
        listView.setOnItemClickListener((parent, view, position, id) -> {
            // 'id' is the database row ID (_id) of the item that was clicked.
            Intent intent = new Intent(HistoryActivity.this, RecordDetailActivity.class);
            intent.putExtra("RECORD_ID", id); // Pass the ID to the detail activity
            startActivity(intent);
        });
    }

    private void displayHistory() {
        // ... this method remains the same as before ...
        Cursor cursor = dbHelper.getAllScores();
        String[] fromColumns = {
                DatabaseHelper.COLUMN_DATE,
                DatabaseHelper.COLUMN_TIME,
                DatabaseHelper.COLUMN_SCORE
        };
        int[] toViews = {
                R.id.dateTextView,
                R.id.timeTextView,
                R.id.scoreTextView
        };
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                this,
                R.layout.history_list_item,
                cursor,
                fromColumns,
                toViews,
                0
        );
        listView.setAdapter(adapter);
    }
}