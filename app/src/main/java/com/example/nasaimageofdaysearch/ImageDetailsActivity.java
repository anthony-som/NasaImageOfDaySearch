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

        Image image = (Image) getIntent().getSerializableExtra("IMAGE");
        if (image != null) {
            dateTextView.setText(image.getDate());
            urlTextView.setText(image.getUrl());
        }
    }
}
