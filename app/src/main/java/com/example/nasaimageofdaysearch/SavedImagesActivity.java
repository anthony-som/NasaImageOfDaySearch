package com.example.nasaimageofdaysearch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;

public class SavedImagesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ImageListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_images);

        recyclerView = findViewById(R.id.recyclerView);
        adapter = new ImageListAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // For now, the adapter isn't populated. You'd populate it using LiveData and Room database in real scenarios.
    }
}
