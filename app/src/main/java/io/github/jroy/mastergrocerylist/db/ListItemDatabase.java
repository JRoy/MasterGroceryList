package io.github.jroy.mastergrocerylist.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {ListItem.class}, version = 1, exportSchema = false)
public abstract class ListItemDatabase extends RoomDatabase {
    public abstract ListItemDao listItemDao();

    private static volatile ListItemDatabase INSTANCE;
    public static ListItemDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (ListItemDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context,
                            ListItemDatabase.class, "masterData")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
