package com.dearmoon.shield.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {SystemEvent.class}, version = 1, exportSchema = false)
public abstract class ShieldDatabase extends RoomDatabase {
    public abstract SystemEventDao systemEventDao();
    
    private static volatile ShieldDatabase INSTANCE;
    
    public static ShieldDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (ShieldDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.getApplicationContext(),
                        ShieldDatabase.class,
                        "shield_database"
                    ).build();
                }
            }
        }
        return INSTANCE;
    }
}
