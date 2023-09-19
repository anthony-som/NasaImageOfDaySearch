package com.example.nasaimageofdaysearch;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ImageDAO {

    @Insert
    void insert(Image image);

    @Delete
    void delete(Image image);

    @Query("SELECT * FROM image_table")
    List<Image> getAllSavedImages();
}
