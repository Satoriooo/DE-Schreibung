package com.example.deschreibung;

import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton; // <-- ADD THIS IMPORT
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.deschreibung.models.Vocabulary;
import java.util.ArrayList;
import java.util.List;

public class VocabularyListAdapter extends RecyclerView.Adapter<VocabularyListAdapter.VocabularyViewHolder> {

    private final List<Vocabulary> vocabularyList;
    private final VocabularyClickListener clickListener;
    private final SparseBooleanArray selectedItems = new SparseBooleanArray();
    private boolean isSelectionMode = false;

    // THE LISTENER INTERFACE IS UPDATED WITH A NEW METHOD
    public interface VocabularyClickListener {
        void onItemClicked(int position);
        void onItemLongClicked(int position);
        void onDeleteItemClicked(int position); // <-- NEW METHOD
    }

    public VocabularyListAdapter(List<Vocabulary> vocabularyList, VocabularyClickListener clickListener) {
        this.vocabularyList = vocabularyList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public VocabularyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vocabulary, parent, false);
        return new VocabularyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VocabularyViewHolder holder, int position) {
        Vocabulary currentItem = vocabularyList.get(position);
        holder.bind(currentItem);
        holder.itemView.setActivated(selectedItems.get(position, false));
    }

    @Override
    public int getItemCount() {
        return vocabularyList.size();
    }


    // --- Selection State Helper Methods (No changes here) ---
    public void setSelectionMode(boolean selectionMode) { isSelectionMode = selectionMode; }
    public boolean isSelectionMode() { return isSelectionMode; }
    public int getSelectedItemCount() { return selectedItems.size(); }
    public void toggleSelection(int position) {
        if (selectedItems.get(position, false)) {
            selectedItems.delete(position);
        } else {
            selectedItems.put(position, true);
        }
        notifyItemChanged(position);
    }
    public void clearSelections() {
        isSelectionMode = false;
        selectedItems.clear();
        notifyDataSetChanged();
    }
    public List<Long> getSelectedItemsIds() {
        List<Long> ids = new ArrayList<>(selectedItems.size());
        for (int i = 0; i < selectedItems.size(); i++) {
            int position = selectedItems.keyAt(i);
            ids.add(vocabularyList.get(position).getId());
        }
        return ids;
    }


    // --- ViewHolder (Updated) ---
    class VocabularyViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewGermanWord;
        private final ImageButton buttonDelete; // <-- NEW: Reference to the delete button

        VocabularyViewHolder(View itemView) {
            super(itemView);
            textViewGermanWord = itemView.findViewById(R.id.textViewGermanWord);
            buttonDelete = itemView.findViewById(R.id.buttonDelete); // <-- NEW: Find the button by its ID

            // Click listener for the entire row
            itemView.setOnClickListener(v -> {
                if (clickListener != null) clickListener.onItemClicked(getAdapterPosition());
            });

            // Long click listener for the entire row
            itemView.setOnLongClickListener(v -> {
                if (clickListener != null) clickListener.onItemLongClicked(getAdapterPosition());
                return true;
            });

            // NEW: Click listener specifically for the delete button
            buttonDelete.setOnClickListener(v -> {
                if (clickListener != null) clickListener.onDeleteItemClicked(getAdapterPosition());
            });
        }

        public void bind(Vocabulary item) {
            textViewGermanWord.setText(item.getGermanWord());
        }
    }
}