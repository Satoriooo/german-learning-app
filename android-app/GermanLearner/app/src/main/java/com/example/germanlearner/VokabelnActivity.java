package com.example.germanlearner;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

public class VokabelnActivity extends AppCompatActivity {

    private RecyclerView vocabularyRecyclerView;
    // We will set up the adapter in a later step

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the content view to our new layout file
        setContentView(R.layout.activity_vokabeln);

        // Find the RecyclerView from the layout
        vocabularyRecyclerView = findViewById(R.id.vocabulary_recycler_view);

        // Set its layout manager
        vocabularyRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // In the next steps, we will create the adapter and set it here, like:
        // vocabularyRecyclerView.setAdapter(your_adapter);
    }
}