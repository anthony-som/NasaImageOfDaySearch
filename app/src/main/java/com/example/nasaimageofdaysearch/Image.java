package com.example.nasaimageofdaysearch;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "image_table")
public class Image {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String date;
    private String url;
    private String hdUrl;
    private String imagePath;

    // Constructors
    public Image(String date, String url, String hdUrl, String imagePath) {
        this.date = date;
        this.url = url;
        this.hdUrl = hdUrl;
        this.imagePath = imagePath;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public String getUrl() {
        return url;
    }

    public String getHdUrl() {
        return hdUrl;
    }

    public String getImagePath() {
        return imagePath;
    }
}
