package io.github.jroy.mastergrocerylist.db;

import android.content.Context;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ListItemRepo {
    private static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(2);
    private final ListItemDao itemDao;

    public ListItemRepo(Context context) {
        this.itemDao = ListItemDatabase.getDatabase(context).listItemDao();
    }

    public void insert(ListItem listItem) {
        databaseWriteExecutor.execute(() -> itemDao.insert(listItem));
    }

    public void update(ListItem listItem) {
        databaseWriteExecutor.execute(() -> itemDao.update(listItem));
    }

    public void delete(ListItem listItem) {
        databaseWriteExecutor.execute(() -> itemDao.delete(listItem));
    }

    public ListItemDao getItemDao() {
        return itemDao;
    }

    public LiveData<List<ListItem>> getAll() {
        return itemDao.getAll();
    }
}
