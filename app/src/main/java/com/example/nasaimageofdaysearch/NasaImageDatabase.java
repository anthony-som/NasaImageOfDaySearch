package com.example.nasaimageofdaysearch;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * Represents the SQLite database for storing NASA imagee
 * provide access to data through ImageDAO
 */
@Database(entities = {Image.class}, version = 1)
public abstract class NasaImageDatabase extends RoomDatabase {
    private static volatile NasaImageDatabase INSTANCE;

    public abstract ImageDAO imageDAO();

    public static NasaImageDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (NasaImageDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    NasaImageDatabase.class, "nasa_image_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}

