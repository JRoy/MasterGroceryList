package io.github.jroy.mastergrocerylist.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface SubListDao {
    @Query("SELECT * FROM subListItems order by id desc")
    LiveData<List<SubListItem>> getAll();

    @Update
    void update(SubListItem listItem);

    @Insert
    void insert(SubListItem listItem);

    @Delete
    void delete(SubListItem listItem);

    @Query("DELETE FROM subListItems")
    void nuke();
}
