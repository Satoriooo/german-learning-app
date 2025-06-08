// MainActivity.java
// Updated to handle the new history button.
package com.example.germanlearner;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button writingButton = findViewById(R.id.writingButton);
        Button historyButton = findViewById(R.id.historyButton);
        Button vokabelnButton = findViewById(R.id.vokabeln_button);
        vokabelnButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, VokabelnActivity.class);
            startActivity(intent);
        });

        writingButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, WritingActivity.class);
            startActivity(intent);
        });

        historyButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            startActivity(intent);
        });
    }
}