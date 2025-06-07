// HistoryActivity.java
// This new activity displays the score history.
package com.example.germanlearner;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.RelativeLayout;
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
    }

    private void displayHistory() {
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
                R.layout.history_list_item, // We will create this new layout file
                cursor,
                fromColumns,
                toViews,
                0
        );

        listView.setAdapter(adapter);
    }
}