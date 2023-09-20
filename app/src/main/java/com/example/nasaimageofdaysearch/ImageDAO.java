package com.example.nasaimageofdaysearch;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;


@Dao
public interface ImageDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertImage(Image image);

    @Query("SELECT * FROM images")
    List<Image> getAllImages();

    @Delete
    void deleteImage(Image image);

    @Query("SELECT * FROM images WHERE date = :date")
    Image getImageByDate(String date);
}

