package io.github.jroy.mastergrocerylist;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.LiveData;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import io.github.jroy.mastergrocerylist.db.ListItem;
import io.github.jroy.mastergrocerylist.db.ListItemRepo;

public class MasterListActivity extends AppCompatActivity {
    public static final Comparator<ListItem> COMPARATOR = Comparator.comparing(o -> o.name);

    private ListItemRepo mDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_master_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mDao = new ListItemRepo(getApplicationContext());

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            final AlertDialog.Builder alert = new AlertDialog.Builder(MasterListActivity.this)
                    .setTitle(R.string.new_item)
                    .setMessage(R.string.enter_the_name_of_a_new_item);

            final EditText input = new EditText(MasterListActivity.this);
            input.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT));
            alert.setView(input);

            final DialogInterface.OnClickListener listener = (dialog, which) -> {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    mDao.insert(new ListItem(input.getText().toString().trim().toLowerCase()));
                }
            };
            alert.setPositiveButton(R.string.add, listener);
            alert.setNegativeButton(R.string.cancel, listener);
            final AlertDialog dialog = alert.create();
            final Window window = dialog.getWindow();

            if (window != null) {
                //noinspection deprecation not getting removed for a while and the alternative is a pita
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            }
            dialog.show();
            input.requestFocus();
        });

        final LiveData<List<ListItem>> data = mDao.getAll();
        final ListView listView = findViewById(R.id.master_list);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            PopupMenu menu = new PopupMenu(MasterListActivity.this, view);
            menu.getMenuInflater().inflate(R.menu.master_list_menu, menu.getMenu());

            menu.setOnMenuItemClickListener(item -> {
                final int id1 = item.getItemId();
                ListItem clickedItem = Objects.requireNonNull(data.getValue()).get(position);
                if (id1 == R.id.item_edit) {
                    final AlertDialog.Builder alert = new AlertDialog.Builder(MasterListActivity.this)
                            .setTitle(R.string.edit_item)
                            .setMessage(R.string.enter_the_new_name_of_item);

                    final EditText input = new EditText(MasterListActivity.this);
                    input.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT));
                    input.setText(clickedItem.name);
                    alert.setView(input);

                    final DialogInterface.OnClickListener listener = (dialog, which) -> {
                        if (which == DialogInterface.BUTTON_POSITIVE) {
                            clickedItem.name = input.getText().toString().trim();
                            mDao.update(clickedItem);
                        }
                    };
                    alert.setPositiveButton(R.string.edit, listener);
                    alert.setNegativeButton(R.string.cancel, listener);
                    final AlertDialog dialog = alert.create();
                    final Window window = dialog.getWindow();

                    if (window != null) {
                        //noinspection deprecation not getting removed for a while and the alternative is a pita
                        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                    }

                    dialog.show();
                    input.requestFocus();
                } else if (id1 == R.id.item_delete) {
                    mDao.delete(clickedItem);
                }
                return true;
            });
            menu.show();
        });

        final ArrayAdapter<ListItem> adapter = new ArrayAdapter<>(MasterListActivity.this, android.R.layout.simple_list_item_1, new ArrayList<>());
        listView.setAdapter(adapter);
        data.observe(this, listItems -> {
            adapter.clear();
            listItems.sort(COMPARATOR);
            adapter.addAll(listItems);
            adapter.notifyDataSetChanged();
        });
    }
}