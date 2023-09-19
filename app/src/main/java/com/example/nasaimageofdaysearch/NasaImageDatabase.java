package com.example.nasaimageofdaysearch;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Image.class}, version = 1, exportSchema = false)
public abstract class NasaImageDatabase extends RoomDatabase {
    public abstract ImageDAO imageDAO();
}
