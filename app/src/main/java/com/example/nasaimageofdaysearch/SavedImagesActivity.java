package com.example.nasaimageofdaysearch;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;

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

        // Initialize and set the custom Toolbar as the ActionBar
        Toolbar toolbar = findViewById(R.id.toolbar_saved);
        setSupportActionBar(toolbar);

//        // Setup Button to return to MainActivity
//        Button btnReturnHome = findViewById(R.id.btnReturnHome);
//        btnReturnHome.setOnClickListener(v -> {
//            Intent intent = new Intent(SavedImagesActivity.this, MainActivity.class);
//            startActivity(intent);
//            finish();
//        });

        // Setup RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Load images to display
        new LoadImagesTask().execute();

        // Setup Navigation Drawer
        DrawerLayout drawerLayoutSaved = findViewById(R.id.drawer_layout_saved);
        NavigationView navigationViewSaved = findViewById(R.id.nav_view_saved);

        // Set up ActionBarDrawerToggle to sync the state of the drawer toggle
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayoutSaved, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayoutSaved.addDrawerListener(toggle);
        toggle.syncState();

        // Set the item selected listener for the NavigationView
        navigationViewSaved.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            // TODO: Replace the IDs below with your actual menu item IDs from the drawer_menu.xml
            if (itemId == R.id.home) {
                Intent homeIntent = new Intent(SavedImagesActivity.this, MainActivity.class);
                startActivity(homeIntent);
                finish();
            } else if (itemId == R.id.saved_images) {
                drawerLayoutSaved.closeDrawers();  // You're already in the SavedImagesActivity
            }
            // ... Handle other menu items if needed ...
            else {
                drawerLayoutSaved.closeDrawers();
            }

            return true;
        });
    }

    //Help
    private void displayHelpDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Help")
                .setMessage("Instructions on how to use the interface...")
                .setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_help, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_help) {
            displayHelpDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    //Help end
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
