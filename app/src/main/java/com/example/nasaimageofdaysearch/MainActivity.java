package com.example.nasaimageofdaysearch;

import android.Manifest;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Button;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.app.DatePickerDialog;

import java.io.BufferedReader;
import java.io.File;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

import android.content.Intent;
import android.net.Uri;

import org.json.JSONObject;

import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {
    private TextView urlDisplay;
    private TextView dateDisplay;
    private Button pickDateButton;
    private ImageView imageView;
    private Button saveButton;
    private String currentDate;
    private String date;
    private String url;
    private static final int REQUEST_MEDIA_ACCESS_PERMISSION = 102;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    private final String BASE_URL = "https://api.nasa.gov/";
    private final String API_KEY = "mjPPt1dcjX4B7LIzgtaYQ781ahZUxMvQXWPyCV4y";
    private String hdUrl;
    private static final int WRITE_STORAGE_PERMISSION_REQUEST_CODE = 101;

    /**
     * Initalizes main acitivty and UI components such as button to save images into database and button to view images
     * Initalizes date picker for the app
     * Requests permission to read/write storage for app
     * Initalizes navigation drawer
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnViewSavedImages = findViewById(R.id.btnViewSavedImages);
        imageView = findViewById(R.id.imageView);
        urlDisplay = findViewById(R.id.urlDisplay);
        dateDisplay = findViewById(R.id.dateDisplay);
        pickDateButton = findViewById(R.id.pickDateButton);
        saveButton = findViewById(R.id.saveButton);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        pickDateButton.setOnClickListener(view -> {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this, (datePicker, selectedYear, selectedMonth, selectedDay) -> {
                String selectedDate = selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDay;
                new FetchImageTask().execute(selectedDate);
            }, year, month, day);
            datePickerDialog.show();
        });

        saveButton.setOnClickListener(view -> {
            //Display toast is user tries to save an image that is not there
            if (imageView.getDrawable() == null) {
                Toast.makeText(MainActivity.this, "Pick a date", Toast.LENGTH_SHORT).show();
                return;
            }
            // Ask permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_MEDIA_ACCESS_PERMISSION);
                } else {
                    saveImageToStorageAndDatabase();
                }
            } else {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_STORAGE_PERMISSION_REQUEST_CODE);
                } else {
                    saveImageToStorageAndDatabase();
                }
            }
        });

        // Navigate to SavedImagesActivity when button is clicked
        btnViewSavedImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SavedImagesActivity.class);
                startActivity(intent);
            }
        });

        // Set up the navigation view listener
        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.home) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else if (itemId == R.id.saved_images) {
                Intent intentSavedImages = new Intent(MainActivity.this, SavedImagesActivity.class);
                startActivity(intentSavedImages);
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });


        // drawer toggle
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    /**
     * Help dialog
     * Inflates the menu with a help dialog. When pressed, display instructions to use the current displayed page
     */

    private void displayHelpDialog() {
        new AlertDialog.Builder(this).setTitle("Help").setMessage("Click \"Pick Date\" to select a photo." + "\n" + "Click \"Save Image\" button to save a photo" + "\n" + "Click \"View Saved\" to view the saved images").setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss()).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_help, menu);
        return true;
    }

    // When user clicks help, call displayHelpDialog method
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_help) {
            displayHelpDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * Save and delete NASA images to/from device storage and database
     * Manage permission results for storage access
     * Handle UI feedback
     */

    // saves the bitmap image to external storage with a name based on the provided date
    private String saveImageToStorage(Bitmap bitmap, String date) {
        String savedImagePath = null;
        String imageFileName = "NASA_" + date + ".jpg";
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/NasaImages");
        boolean success = true;
        if (!storageDir.exists()) {
            success = storageDir.mkdirs();
        }

        if (success) {
            File imageFile = new File(storageDir, imageFileName);
            savedImagePath = imageFile.getAbsolutePath();
            try {
                OutputStream fOut = new FileOutputStream(imageFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                fOut.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            MediaScannerConnection.scanFile(this, new String[]{imageFile.getAbsolutePath()}, null, null);
        }

        return savedImagePath;
    }

    // inserts the image's metadata into the database.
    private void saveImageDetailsToDatabase(String date, String url, String hdUrl, String imagePath) {
        Image image = new Image();
        image.setDate(date);
        image.setUrl(url);
        image.setHdUrl(hdUrl);
        image.setImagePath(imagePath);

        NasaImageDatabase db = NasaImageDatabase.getInstance(this);
        new Thread(() -> {
            db.imageDAO().insertImage(image);
        }).start();
    }

    // retrieve the current image from the view, saves it to storage, and stores its details in the database
    private void saveImageToStorageAndDatabase() {
        Bitmap imageBitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        String imagePath;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            imagePath = saveImageToSharedStorage(imageBitmap);
        } else {
            imagePath = saveImageToStorage(imageBitmap, date);
        }

        if (imagePath != null) {
            saveImageDetailsToDatabase(date, url, hdUrl, imagePath);
            Toast.makeText(this, "Image saved successfully!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Error saving image!", Toast.LENGTH_SHORT).show();
        }
    }

    // Saves the provided bitmap image to the shared storage
    private String saveImageToSharedStorage(Bitmap bitmap) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "NASA_" + date + ".jpg");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");

        Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        if (uri != null) {
            try (OutputStream out = getContentResolver().openOutputStream(uri)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                return uri.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    // Handles the result of permission requests to save images
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_MEDIA_ACCESS_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveImageToStorageAndDatabase();
            } else {
                Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == WRITE_STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveImageToStorageAndDatabase();
            } else {
                Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Deletes the selected image from both the storage and the database
    private void deleteImageFromStorageAndDatabase() {
        NasaImageDatabase db = NasaImageDatabase.getInstance(this);
        new Thread(() -> {
            Image image = db.imageDAO().getImageByDate(date);
            if (image != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    deleteImageFromSharedStorage(image.getImagePath());
                } else {
                    deleteImageFromStorage(image.getImagePath());
                }
                db.imageDAO().deleteImage(image);
            }
            runOnUiThread(() -> {
                imageView.setImageBitmap(null);
                Toast.makeText(this, "Image deleted successfully!", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    // Deletes the image at the provided path from the storage
    private void deleteImageFromStorage(String imagePath) {
        File file = new File(imagePath);
        if (file.exists()) {
            file.delete();
            MediaScannerConnection.scanFile(this, new String[]{file.getAbsolutePath()}, null, null);
        }
    }

    // Removes the image with the provided URI from shared storage
    private void deleteImageFromSharedStorage(String imageUri) {
        getContentResolver().delete(Uri.parse(imageUri), null, null);
    }


    // Asynchronously fetches and displays a NASA image for a given date.
    private class FetchImageTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... dates) {
            date = dates[0];
            currentDate = date = dates[0];
            String imageUrl = fetchNasaImageUrl(date);
            if (imageUrl != null) {
                return loadImageFromUrl(imageUrl);
            }
            return null;
        }

        // display date of image
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
                dateDisplay.setText("Date: " + date);
                urlDisplay.setText(hdUrl);
                urlDisplay.setOnClickListener(v -> {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(hdUrl));
                    startActivity(browserIntent);
                });
            }

        }
    }

    // Fetches image URL
    private String fetchNasaImageUrl(String date) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            URL requestUrl = new URL(BASE_URL + "planetary/apod?api_key=" + API_KEY + "&date=" + date);
            connection = (HttpURLConnection) requestUrl.openConnection();
            connection.connect();

            InputStream stream = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(stream));
            StringBuilder buffer = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }

            String finalJson = buffer.toString();
            JSONObject parentObject = new JSONObject(finalJson);
            hdUrl = parentObject.getString("hdurl");
            String imageUrl = parentObject.getString("url");
            return imageUrl;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    // Downloads and returns bitmap image from the URL
    private Bitmap loadImageFromUrl(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


}
