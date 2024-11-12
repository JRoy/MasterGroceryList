package io.github.jroy.mastergrocerylist.db;

import android.content.Context;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SubListRepo {
    private static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(2);
    private final SubListDao subListDao;

    public SubListRepo(Context context) {
        this.subListDao = SubListDatabase.getDatabase(context).subListDao();
    }

    public void insert(SubListItem listItem) {
        databaseWriteExecutor.execute(() -> subListDao.insert(listItem));
    }

    public void update(SubListItem listItem) {
        databaseWriteExecutor.execute(() -> subListDao.update(listItem));
    }

    public void delete(SubListItem listItem) {
        databaseWriteExecutor.execute(() -> subListDao.delete(listItem));
    }

    public void nuke() {
        databaseWriteExecutor.execute(subListDao::nuke);
    }

    public LiveData<List<SubListItem>> getAll() {
        return subListDao.getAll();
    }

    public SubListDao getSubListDao() {
        return subListDao;
    }
}
