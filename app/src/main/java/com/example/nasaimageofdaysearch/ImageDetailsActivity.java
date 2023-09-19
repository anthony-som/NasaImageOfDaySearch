package com.example.nasaimageofdaysearch;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class ImageDetailsActivity extends AppCompatActivity {

    private ImageView detailsImageView;
    private TextView dateTextView;
    private TextView urlTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_details);

        detailsImageView = findViewById(R.id.detailsImageView);
        dateTextView = findViewById(R.id.dateTextView);
        urlTextView = findViewById(R.id.urlTextView);

        // Fetch Image details, populate the views and display them.
        // For the sake of example, let's assume you pass Image object via Intent:
        Image image = (Image) getIntent().getSerializableExtra("IMAGE");
        if (image != null) {
            dateTextView.setText(image.getDate());
            urlTextView.setText(image.getUrl());
            // Load the image into ImageView, perhaps using a library like Picasso or Glide.
        }
    }
}
