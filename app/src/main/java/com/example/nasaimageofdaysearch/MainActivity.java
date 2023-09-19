package com.example.nasaimageofdaysearch;

import androidx.appcompat.app.AppCompatActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Button;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.app.DatePickerDialog;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

    private final String BASE_URL = "https://api.nasa.gov/";
    private final String API_KEY = "mjPPt1dcjX4B7LIzgtaYQ781ahZUxMvQXWPyCV4y";
    private String hdUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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




    }


    private class FetchImageTask extends AsyncTask<String, Void, Bitmap> {
        private String date; // For storing the date

        @Override
        protected Bitmap doInBackground(String... dates) {
            date = dates[0];
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
            URL url = new URL(BASE_URL + "planetary/apod?api_key=" + API_KEY + "&date=" + date);
            connection = (HttpURLConnection) url.openConnection();
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
            hdUrl = parentObject.getString("hdurl"); // Store the HDURL
            return parentObject.getString("url");

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
