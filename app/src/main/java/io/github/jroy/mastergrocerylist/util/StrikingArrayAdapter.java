package io.github.jroy.mastergrocerylist.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import io.github.jroy.mastergrocerylist.db.SubListItem;

public class StrikingArrayAdapter extends ArrayAdapter<SubListItem> {
    public StrikingArrayAdapter(@NonNull Context context, @NonNull List<SubListItem> objects) {
        super(context, android.R.layout.simple_list_item_1, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        TextView view = (TextView) super.getView(position, convertView, parent);
        view.setTextSize(28);

        SubListItem item = getItem(position);
        if (item != null && item.strucken != 0) {
            view.setPaintFlags(Paint.FAKE_BOLD_TEXT_FLAG | Paint.STRIKE_THRU_TEXT_FLAG);
            view.setTextColor(Color.YELLOW);
        } else {
            view.setPaintFlags(0);
            view.setTextColor(Color.WHITE);
        }
        return view;
    }
}
