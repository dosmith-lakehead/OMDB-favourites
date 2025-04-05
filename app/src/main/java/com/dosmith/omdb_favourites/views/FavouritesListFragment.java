package com.dosmith.omdb_favourites.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.dosmith.omdb_favourites.databinding.FragmentFavouritesListBinding;
import com.dosmith.omdb_favourites.models.FavouriteItem;
import com.dosmith.omdb_favourites.models.SearchResult;
import com.dosmith.omdb_favourites.utilities.FavouritesListAdapter;
import com.dosmith.omdb_favourites.viewmodels.SearchActivityViewModel;

// This fragment will display search results.
// I've cleverly, even deviously, stacked it behind the search form.
public class FavouritesListFragment extends Fragment implements FavouritesListAdapter.FavouriteItemViewHolder.OnItemClickListener, FavouritesListAdapter.FavouriteItemViewHolder.UnfavouriteListener  {
    FragmentFavouritesListBinding binding;
    SearchActivityViewModel viewModel;
    FavouritesListAdapter adapter;

    public FavouritesListFragment() {
    }

    public static FavouritesListFragment newInstance() {
        FavouritesListFragment fragment = new FavouritesListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(SearchActivityViewModel.class);
        // Instantiate my custom adapter class
        adapter = new FavouritesListAdapter(viewModel.getFavourites().getValue(), this, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFavouritesListBinding.inflate(inflater, container, false);

        // set a layout manager
        binding.rvResults.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        // Set the adapter
        binding.rvResults.setAdapter(adapter);

        // Observe changes to searchResults in the viewmodel.
        viewModel.getFavourites().observe(getViewLifecycleOwner(), items -> {;
            // update the adapter if they change
            adapter.updateData(viewModel.getFavourites().getValue());
        });

        viewModel.getUsername().observe(getViewLifecycleOwner(), txt->{
            if (txt != null){
                String usernameFavourite = txt + (txt.charAt(txt.length()-2) == 's' ? "' Favourites" : "'s Favourites");
                binding.tvUsername.setText(usernameFavourite);
            }
        });

        // If the user scrolls to the bottom of the results, and there are more
        // results to get, get them.


        return binding.getRoot();
    }

    // On clicking on a search result, we're activating the interface to that item in the adapter.
    // Use it to create an intent and launch the Details activity.

    @Override
    public void onItemClick(FavouriteItem favouriteItem) {
        Intent intent = new Intent(this.getContext().getApplicationContext(), FavouritesDetailsActivity.class);
        intent.putExtra("imdbId", favouriteItem.getImdbID());
        intent.putExtra("uID", viewModel.getUID().getValue());
        intent.putExtra("userName", viewModel.getUsername().getValue());
        startActivity(intent);
    }

    @Override
    public void onUnfavourite(FavouriteItem favouriteItem) {
        viewModel.removeOneFavourite(favouriteItem);
    }
}