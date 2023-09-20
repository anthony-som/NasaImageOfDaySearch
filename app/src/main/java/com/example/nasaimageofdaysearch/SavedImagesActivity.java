package com.example.nasaimageofdaysearch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import java.util.List;

public class SavedImagesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ImageListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_images);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        new LoadImagesTask().execute();
    }

    private class LoadImagesTask extends AsyncTask<Void, Void, List<Image>> {

        @Override
        protected List<Image> doInBackground(Void... voids) {
            NasaImageDatabase db = NasaImageDatabase.getInstance(getApplicationContext());
            return db.imageDAO().getAllImages();
        }

@Override
protected void onPostExecute(List<Image> images) {
    if(images.isEmpty()) {
        Log.d("SavedImagesActivity", "No images found in database.");
    } else {
        adapter = new ImageListAdapter(images, SavedImagesActivity.this);
        recyclerView.setAdapter(adapter);
    }
}

    }
}
