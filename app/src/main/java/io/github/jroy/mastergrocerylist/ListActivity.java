package io.github.jroy.mastergrocerylist;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.PopupMenu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.LiveData;

import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.jroy.mastergrocerylist.db.ListItem;
import io.github.jroy.mastergrocerylist.db.ListItemRepo;
import io.github.jroy.mastergrocerylist.db.SubListItem;
import io.github.jroy.mastergrocerylist.db.SubListRepo;
import io.github.jroy.mastergrocerylist.util.StrikingArrayAdapter;

public class ListActivity extends AppCompatActivity {
    private final static Pattern QUANTITY_PATTERN = Pattern.compile("^(\\d)(\\d?)(\\d?)x ");
    private final static Comparator<SubListItem> COMPARATOR = Comparator.comparing(o -> {
        if (o == null) {
            return null;
        }

        final Matcher matcher = QUANTITY_PATTERN.matcher(o.name);
        if (matcher.lookingAt()) {
            return o.name.substring(matcher.end());
        }
        return o.name;
    });
    private SubListRepo mDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        Toolbar toolbar = findViewById(R.id.slave_toolbar);
        setSupportActionBar(toolbar);

        mDao = new SubListRepo(getApplicationContext());

        final ListView listView = findViewById(R.id.sub_list);
        final LiveData<List<SubListItem>> data = mDao.getAll();
        final StrikingArrayAdapter adapter = new StrikingArrayAdapter(ListActivity.this, new ArrayList<>());
        listView.setAdapter(adapter);
        data.observe(this, subListItems -> {
            adapter.clear();
            subListItems.sort(COMPARATOR);
            adapter.addAll(subListItems);
            adapter.notifyDataSetChanged();
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            final SubListItem item = adapter.getItem(position);
            if (item != null) {
                item.strucken = item.strucken != 0 ? 0 : 1;
                mDao.update(item);
            }
        });

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            PopupMenu menu = new PopupMenu(ListActivity.this, view);
            menu.getMenuInflater().inflate(R.menu.sub_list_menu, menu.getMenu());

            menu.setOnMenuItemClickListener(item -> {
                mDao.delete(Objects.requireNonNull(data.getValue()).remove(position));
                return true;
            });
            menu.show();
            return true;
        });

        final SpeedDialView speedDialView = findViewById(R.id.speedDial);
        speedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id.fab_add_item, android.R.drawable.ic_input_add).create());
        speedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id.fab_edit_master, R.drawable.ic_baseline_settings_24).create());
        speedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id.fab_trash, R.drawable.ic_baseline_restore_from_trash_24).create());

        speedDialView.setOnActionSelectedListener(actionItem -> {
            if (actionItem.getId() == R.id.fab_add_item) {
                new ListItemRepo(getApplicationContext()).getAll().observe(ListActivity.this, masterItemsFu -> {
                    final List<ListItem> filtered = new ArrayList<>(masterItemsFu);
                    filtered.sort(MasterListActivity.COMPARATOR);
                    final List<SubListItem> subSnapshot = data.getValue();
                    if (subSnapshot != null) {
                        ListIterator<ListItem> listIterator = filtered.listIterator();
                        while (listIterator.hasNext()) {
                            ListItem item = listIterator.next();

                            for (SubListItem subItem : subSnapshot) {
                                final Matcher matcher = QUANTITY_PATTERN.matcher(subItem.name);
                                final String cleanName;
                                if (matcher.lookingAt()) {
                                    cleanName = subItem.name.substring(matcher.end());
                                } else {
                                    cleanName = subItem.name;
                                }

                                if (item.name.equals(cleanName)) {
                                    listIterator.remove();
                                }
                            }
                        }
                    }
                    runOnUiThread(() -> {
                        final AlertDialog.Builder itemListBuilder = new AlertDialog.Builder(ListActivity.this);
                        itemListBuilder.setTitle(R.string.add_items);
                        ArrayAdapter<ListItem> addAdapter = new ArrayAdapter<>(ListActivity.this, android.R.layout.simple_list_item_1, filtered);
                        itemListBuilder.setAdapter(addAdapter, null);
                        itemListBuilder.setPositiveButton(R.string.ok, null);
                        itemListBuilder.setNeutralButton(R.string.temporary_item, (dialog, which) -> {
                            final AlertDialog.Builder tempItemBuilder = new AlertDialog.Builder(ListActivity.this);
                            tempItemBuilder.setTitle(R.string.add_temporary_item);
                            tempItemBuilder.setMessage(R.string.temporary_item_desc);

                            final EditText input = new EditText(ListActivity.this);
                            input.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.MATCH_PARENT));
                            tempItemBuilder.setView(input);
                            tempItemBuilder.setPositiveButton(R.string.ok, (dialog1, which1) -> mDao.insert(new SubListItem(input.getText().toString().trim().toLowerCase())));
                            tempItemBuilder.setNegativeButton(R.string.cancel, null);

                            final AlertDialog tempItemDialog = tempItemBuilder.create();
                            final Window window = tempItemDialog.getWindow();

                            if (window != null) {
                                //noinspection deprecation not getting removed for a while and the alternative is a pita
                                tempItemDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                            }
                            tempItemDialog.show();
                            input.requestFocus();
                        });

                        final AlertDialog itemListDialog = itemListBuilder.show();
                        itemListDialog.getListView().setOnItemClickListener((parent, view, position, id) -> {
                            final ListItem selected = addAdapter.getItem(position);
                            if (selected == null) {
                                return;
                            }

                            mDao.insert(new SubListItem(selected.name));
                            addAdapter.remove(selected);
                            addAdapter.notifyDataSetChanged();
                        });
                        itemListDialog.getListView().setOnItemLongClickListener((parent, view, position, id) -> {
                            final ListItem selected = addAdapter.getItem(position);

                            if (selected == null) {
                                return false;
                            }

                            final NumberPicker numberPicker = new NumberPicker(ListActivity.this);
                            numberPicker.setMinValue(1);
                            numberPicker.setMaxValue(100);

                            final AlertDialog.Builder quantityBuilder = new AlertDialog.Builder(ListActivity.this);
                            quantityBuilder.setView(numberPicker);
                            quantityBuilder.setTitle(R.string.pick_quantity);
                            quantityBuilder.setMessage(quantityBuilder.getContext().getString(R.string.quantity_item_desc, selected.name));
                            quantityBuilder.setPositiveButton(R.string.ok, (dialog, which) -> {
                                mDao.insert(new SubListItem(numberPicker.getValue() + "x " + selected.name));
                                addAdapter.remove(selected);
                                addAdapter.notifyDataSetChanged();
                            });
                            quantityBuilder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
                            quantityBuilder.show();
                            return true;
                        });
                    });
                });
            } else if (actionItem.getId() == R.id.fab_edit_master) {
                startActivity(new Intent(ListActivity.this, MasterListActivity.class));
            } else if (actionItem.getId() == R.id.fab_trash) {
                new AlertDialog.Builder(ListActivity.this)
                        .setTitle(R.string.delete_current_list)
                        .setMessage(R.string.delete_list_desc)
                        .setPositiveButton(R.string.yes, (dialog, which) -> mDao.nuke())
                        .setNegativeButton(R.string.no, null)
                        .show();
            }
            return false;
        });
    }
}