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

import com.dosmith.omdb_favourites.databinding.FragmentSearchResultsBinding;
import com.dosmith.omdb_favourites.models.FavouriteItem;
import com.dosmith.omdb_favourites.models.SearchResult;
import com.dosmith.omdb_favourites.utilities.FavouritesListAdapter;
import com.dosmith.omdb_favourites.utilities.SearchResultsAdapter;
import com.dosmith.omdb_favourites.viewmodels.SearchActivityViewModel;

// This fragment will display search results.
// I've cleverly, even deviously, stacked it behind the search form.
public class SearchResultsFragment extends Fragment implements SearchResultsAdapter.SearchResultViewHolder.OnItemClickListener {
    FragmentSearchResultsBinding binding;
    SearchActivityViewModel viewModel;
    SearchResultsAdapter adapter;

    public SearchResultsFragment() {
    }

    public static SearchResultsFragment newInstance() {
        SearchResultsFragment fragment = new SearchResultsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(SearchActivityViewModel.class);
        // Instantiate my custom adapter class
        adapter = new SearchResultsAdapter(viewModel.getSearchResults().getValue(), this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSearchResultsBinding.inflate(inflater, container, false);

        // set a layout manager
        binding.rvResults.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        // Set the adapter
        binding.rvResults.setAdapter(adapter);

        // Observe changes to searchResults in the viewmodel.
        viewModel.getSearchResults().observe(getViewLifecycleOwner(), items -> {;
            // update the adapter if they change
            adapter.updateData(viewModel.getSearchResults().getValue());
        });

        // If the user scrolls to the bottom of the results, and there are more
        // results to get, get them.
        binding.rvResults.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null) {
                    int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
                    int totalItemCount = layoutManager.getItemCount();

                    if (lastVisibleItemPosition == totalItemCount - 1) {
                        viewModel.queryResultsPage();
                    }
                }
            }
        });

        // IS THAT.... A REFRESH LISTENER?!
        binding.main.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                viewModel.reset();
                viewModel.queryResultsPage();
                Observer<Boolean> observer = new Observer<Boolean>() {
                    @Override
                    public void onChanged(Boolean b) {
                        if (!b){
                            binding.main.setRefreshing(false);
                            viewModel.getAddingResults().removeObserver(this);
                        }
                    }
                };
                viewModel.getAddingResults().observe(getViewLifecycleOwner(), observer);
            }
        });

        return binding.getRoot();
    }

    // On clicking on a search result, we're activating the interface to that item in the adapter.
    // Use it to create an intent and launch the Details activity.
    @Override
    public void onItemClick(SearchResult searchResult) {
        Intent intent = new Intent(this.getContext().getApplicationContext(), DetailsActivity.class);
        intent.putExtra("imdbId", searchResult.getImdbID());
        intent.putExtra("uID", viewModel.getUID().getValue());
        intent.putExtra("userName", viewModel.getUsername().getValue());
        startActivity(intent);
    }
}