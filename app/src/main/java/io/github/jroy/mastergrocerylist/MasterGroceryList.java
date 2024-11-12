package io.github.jroy.mastergrocerylist;

import android.app.Application;

import com.google.android.material.color.DynamicColors;

public class MasterGroceryList extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        DynamicColors.applyToActivitiesIfAvailable(this);
    }
}
