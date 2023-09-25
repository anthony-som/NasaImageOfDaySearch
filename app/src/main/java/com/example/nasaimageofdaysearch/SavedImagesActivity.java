package com.example.nasaimageofdaysearch;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SavedImagesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ImageListAdapter adapter;

    @Override
    protected void onResume() {
        super.onResume();
        new LoadImagesTask().execute();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_images);

        Button btnReturnHome = findViewById(R.id.btnReturnHome);
        btnReturnHome.setOnClickListener(v -> {
            Intent intent = new Intent(SavedImagesActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
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
            List<Image> validImages = new ArrayList<>();
            for (Image image : images) {
                if (uriExists(image.getImagePath())) {
                    validImages.add(image);
                } else {
                    new Thread(() -> {
                        NasaImageDatabase db = NasaImageDatabase.getInstance(getApplicationContext());
                        db.imageDAO().deleteImage(image);
                    }).start();
                }
            }

            if (validImages.isEmpty()) {
                Log.d("Debug", "No valid images found in database.");
            } else {
                Log.d("Debug", "Valid images found in database: " + validImages.size());
            }

            adapter = new ImageListAdapter(validImages, SavedImagesActivity.this);
            recyclerView.setAdapter(adapter);
        }

        private boolean uriExists(String uriString) {
            try (Cursor cursor = getContentResolver().query(Uri.parse(uriString), null, null, null, null)) {
                return cursor != null && cursor.moveToFirst();
            } catch (Exception e) {
                return false;
            }
        }
    }
}
