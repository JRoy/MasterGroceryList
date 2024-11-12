package io.github.jroy.mastergrocerylist.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {SubListItem.class}, version = 1, exportSchema = false)
public abstract class SubListDatabase extends RoomDatabase {
    public abstract SubListDao subListDao();

    private static volatile SubListDatabase INSTANCE;
    public static SubListDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (SubListDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context, SubListDatabase.class, "subData")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
