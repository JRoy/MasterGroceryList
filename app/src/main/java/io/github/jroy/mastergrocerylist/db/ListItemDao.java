package io.github.jroy.mastergrocerylist.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ListItemDao {
    @Query("SELECT * FROM listItems order by id desc")
    LiveData<List<ListItem>> getAll();

    @Query("SELECT * FROM listItems order by id desc")
    List<ListItem> getAllSync();

    @Update
    void update(ListItem listItem);

    @Insert
    void insert(ListItem listItem);

    @Delete
    void delete(ListItem listItem);
}
