package com.example.nasaimageofdaysearch;

import android.Manifest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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

    private final String BASE_URL = "https://api.nasa.gov/";
    private final String API_KEY = "mjPPt1dcjX4B7LIzgtaYQ781ahZUxMvQXWPyCV4y";
    private String hdUrl;
    private static final int WRITE_STORAGE_PERMISSION_REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btnViewSavedImages = findViewById(R.id.btnViewSavedImages);
        Button deleteButton = findViewById(R.id.deleteButton);
        imageView = findViewById(R.id.imageView);
        urlDisplay = findViewById(R.id.urlDisplay);
        dateDisplay = findViewById(R.id.dateDisplay);
        pickDateButton = findViewById(R.id.pickDateButton);
        saveButton = findViewById(R.id.saveButton);

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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                            REQUEST_MEDIA_ACCESS_PERMISSION);
                } else {
                    saveImageToStorageAndDatabase();
                }
            } else {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            WRITE_STORAGE_PERMISSION_REQUEST_CODE);
                } else {
                    saveImageToStorageAndDatabase();
                }
            }

        });

        deleteButton.setOnClickListener(view -> deleteImageFromStorageAndDatabase());

        btnViewSavedImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SavedImagesActivity.class);
                startActivity(intent);
            }
        });

    }


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

    private void deleteImageFromStorage(String imagePath) {
        File file = new File(imagePath);
        if (file.exists()) {
            file.delete();
            MediaScannerConnection.scanFile(this, new String[]{file.getAbsolutePath()}, null, null);
        }
    }

    private void deleteImageFromSharedStorage(String imageUri) {
        getContentResolver().delete(Uri.parse(imageUri), null, null);
    }


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


        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);

                dateDisplay.setText("Date: " + date);
                urlDisplay.setText("URL: " + hdUrl);

                urlDisplay.setOnClickListener(v -> {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(hdUrl));
                    startActivity(browserIntent);
                });
            }

        }
    }

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
