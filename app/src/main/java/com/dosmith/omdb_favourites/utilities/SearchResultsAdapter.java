package com.dosmith.omdb_favourites.utilities;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.dosmith.omdb_favourites.databinding.SearchResultBinding;
import com.dosmith.omdb_favourites.models.SearchResult;

import java.util.ArrayList;
import java.util.List;

// This class makes views for a RecyclerView from SearchResults objects
public class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.SearchResultViewHolder> {

    // The list of SearchResults
    private List<SearchResult> searchResults;

    // The On-Click listener
    private SearchResultViewHolder.OnItemClickListener listener;

    // ViewHolder class
    public static class SearchResultViewHolder extends RecyclerView.ViewHolder {
        // This interface is used to handle clicks
        public interface OnItemClickListener {
            void onItemClick(SearchResult searchResult);
        }

        // The binding for the view
        private final SearchResultBinding binding;

        // Constructor. Takes a binding and sticks it into a property.
        // Calls the superconstructor on the root of the binding.
        public SearchResultViewHolder(SearchResultBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        // bind properties of a SearchResult to the views contained in the viewbinding
        public void bind(SearchResult searchResult){
            binding.etTitle.setText(searchResult.getTitle());
            binding.etYear.setText("Release Year: " + searchResult.getYear());
            binding.imdbId.setText("IMDBid: " + searchResult.getImdbID());
            String type = Character.toUpperCase(searchResult.getType().charAt(0)) + searchResult.getType().substring(1);
            binding.type.setText(type);
            if (searchResult.getPosterImg() != null) {
                binding.imgPoster.setImageBitmap(searchResult.getPosterImg());
            }
            else {

            }
        }
    }

    // Adapter constructor. take a list of SearchResults and a listener
    public SearchResultsAdapter(List<SearchResult> objects, SearchResultViewHolder.OnItemClickListener listener) {
        this.searchResults = objects;
        this.listener = listener;
    }

    // On creation of a new view holder, pass the binding to the viewholder's constructor.
    @Override
    public SearchResultViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the binding layout
        SearchResultBinding binding = SearchResultBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new SearchResultViewHolder(binding);
    }

    // This function gets the appropriate SearchResult object and binds it to the viewholder
    @Override
    public void onBindViewHolder(SearchResultViewHolder holder, int position) {
        final SearchResult searchResult = searchResults.get(position);
        SearchResult currentItem = searchResults.get(position);
        holder.bind(currentItem);
        holder.itemView.setOnClickListener(v->{
            listener.onItemClick(searchResult);
        });
    }

    // I'm not sure where this is used if anywhere
    @Override
    public int getItemCount() {
        return searchResults.size();
    }

    // Replace the searchResults list with a new one
    public void updateData(ArrayList<SearchResult> searchResults) {
        this.searchResults.clear();
        this.searchResults.addAll(searchResults);
        notifyDataSetChanged();
    }
}
