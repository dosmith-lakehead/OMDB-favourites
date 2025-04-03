package com.dosmith.omdb_favourites.utilities;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.dosmith.omdb_favourites.databinding.FavouriteItemBinding;
import com.dosmith.omdb_favourites.models.FavouriteItem;

import java.util.ArrayList;
import java.util.List;

// This class makes views for a RecyclerView from FavouritesList objects
public class FavouritesListAdapter extends RecyclerView.Adapter<FavouritesListAdapter.FavouriteItemViewHolder> {

    // The list of FavouritesList
    private List<FavouriteItem> favouriteItems;

    // The On-Click listener
    private FavouriteItemViewHolder.OnItemClickListener listener;
    private FavouriteItemViewHolder.UnfavouriteListener unfavouriteListener;

    // ViewHolder class
    public static class FavouriteItemViewHolder extends RecyclerView.ViewHolder {
        // This interface is used to handle clicks
        public interface OnItemClickListener {
            void onItemClick(FavouriteItem favouriteItem);
        }

        public interface UnfavouriteListener {
            void onUnfavourite(FavouriteItem favouriteItem);
        }

        // The binding for the view
        private final FavouriteItemBinding binding;

        // Constructor. Takes a binding and sticks it into a property.
        // Calls the superconstructor on the root of the binding.
        public FavouriteItemViewHolder(FavouriteItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        // bind properties of a FavouriteItem to the views contained in the viewbinding
        public void bind(FavouriteItem favouriteItem){
            binding.etTitle.setText(favouriteItem.getTitle());
            binding.etYear.setText("Release Year: " + favouriteItem.getYear());
            binding.rating.setText("IMDB Rating: " + favouriteItem.getImdbRating());
            binding.description.setText("Description:\n\n" + favouriteItem.getDescription());
            if (favouriteItem.getPosterImg() != null) {
                binding.imgPoster.setImageBitmap(favouriteItem.getPosterImg());
            }
            else {

            }
        }
    }

    // Adapter constructor. take a list of FavouritesList and a listener
    public FavouritesListAdapter(List<FavouriteItem> objects, FavouriteItemViewHolder.OnItemClickListener listener, FavouriteItemViewHolder.UnfavouriteListener unfavouriteListener) {
        this.favouriteItems = objects;
        this.listener = listener;
        this.unfavouriteListener = unfavouriteListener;
    }

    // On creation of a new view holder, pass the binding to the viewholder's constructor.
    @Override
    public FavouriteItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the binding layout
        FavouriteItemBinding binding = FavouriteItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new FavouriteItemViewHolder(binding);
    }

    // This function gets the appropriate FavouriteItem object and binds it to the viewholder
    @Override
    public void onBindViewHolder(FavouriteItemViewHolder holder, int position) {
        final FavouriteItem searchResult = favouriteItems.get(position);
        FavouriteItem currentItem = favouriteItems.get(position);
        holder.bind(currentItem);
        holder.itemView.setOnClickListener(v->{
            listener.onItemClick(searchResult);
        });
        holder.binding.btnUnfavourite.setOnClickListener(v->{
            unfavouriteListener.onUnfavourite(currentItem);
        });
    }

    // I'm not sure where this is used if anywhere
    @Override
    public int getItemCount() {
        return favouriteItems.size();
    }

    // Replace the favouriteItems list with a new one
    public void updateData(ArrayList<FavouriteItem> favouriteItems) {
        if (favouriteItems!= null) {
            this.favouriteItems.clear();
            this.favouriteItems.addAll(favouriteItems);
            notifyDataSetChanged();
        }
    }
}
