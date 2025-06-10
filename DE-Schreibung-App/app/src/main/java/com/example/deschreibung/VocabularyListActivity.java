package com.example.deschreibung;

import android.content.Intent; // <-- ADD THIS IMPORT
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.deschreibung.databinding.ActivityVocabularyListBinding;
import com.example.deschreibung.helper.DatabaseHelper;
import com.example.deschreibung.models.Vocabulary;
import java.util.Collections;
import java.util.List;

public class VocabularyListActivity extends AppCompatActivity implements VocabularyListAdapter.VocabularyClickListener {

    private ActivityVocabularyListBinding binding;
    private DatabaseHelper dbHelper;
    private VocabularyListAdapter adapter;
    private List<Vocabulary> vocabularyList;
    private ActionMode actionMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVocabularyListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        dbHelper = new DatabaseHelper(this);
        setupToolbar();
        setupRecyclerView();

        // NEW: Set up the click listener for the Floating Action Button
        binding.fabAddVocabulary.setOnClickListener(v -> {
            Intent intent = new Intent(VocabularyListActivity.this, EditVocabularyActivity.class);
            startActivity(intent);
        });
    }

    // NEW: The onResume() method is called every time the activity comes to the foreground
    @Override
    protected void onResume() {
        super.onResume();
        // We reload the data here to see any new words that were added.
        loadVocabularyData();
    }

    private void setupToolbar() {
        binding.toolbarVocabulary.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        // We only set up the recycler view once
        binding.recyclerViewVocabulary.setLayoutManager(new LinearLayoutManager(this));
    }

    // NEW: Logic for loading/reloading data is now in its own method
    private void loadVocabularyData() {
        vocabularyList = dbHelper.getAllVocabulary();
        adapter = new VocabularyListAdapter(vocabularyList, this);
        binding.recyclerViewVocabulary.setAdapter(adapter);
    }


    // --- Click Listener Methods from Adapter Interface ---
    @Override
    public void onItemClicked(int position) {
        if (adapter.isSelectionMode()) {
            toggleSelection(position);
        } else {
            // This part can be extended later to EDIT an existing item
            Vocabulary clickedItem = vocabularyList.get(position);
            Intent intent = new Intent(this, VocabularyDetailActivity.class);
            intent.putExtra(VocabularyDetailActivity.EXTRA_VOCAB_ID, clickedItem.getId());
            startActivity(intent);
        }
    }

    @Override
    public void onItemLongClicked(int position) {
        if (!adapter.isSelectionMode()) {
            actionMode = startSupportActionMode(actionModeCallback);
            adapter.setSelectionMode(true);
        }
        toggleSelection(position);
    }

    @Override
    public void onDeleteItemClicked(int position) {
        Vocabulary itemToDelete = vocabularyList.get(position);
        long idToDelete = itemToDelete.getId();
        dbHelper.deleteVocabularyItems(Collections.singletonList(idToDelete));
        vocabularyList.remove(position);
        adapter.notifyItemRemoved(position);
        Toast.makeText(this, "'" + itemToDelete.getGermanWord() + "' gelöscht.", Toast.LENGTH_SHORT).show();
    }


    // --- Multi-select and Action Bar logic (no changes here) ---
    private void toggleSelection(int position) {
        adapter.toggleSelection(position);
        int count = adapter.getSelectedItemCount();
        if (count == 0 && actionMode != null) {
            actionMode.finish();
        } else if (actionMode != null) {
            actionMode.setTitle(count + " ausgewählt");
            actionMode.invalidate();
        }
    }

    private final ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.cab_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getItemId() == R.id.action_delete) {
                deleteSelectedItems();
                mode.finish();
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            adapter.clearSelections();
            actionMode = null;
        }
    };

    private void deleteSelectedItems() {
        List<Long> selectedIds = adapter.getSelectedItemsIds();
        dbHelper.deleteVocabularyItems(selectedIds);
        Toast.makeText(this, selectedIds.size() + " Vokabel(n) gelöscht.", Toast.LENGTH_SHORT).show();
        loadVocabularyData(); // Refresh the list
    }
}