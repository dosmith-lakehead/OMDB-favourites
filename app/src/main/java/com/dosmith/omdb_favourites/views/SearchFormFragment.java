package com.dosmith.omdb_favourites.views;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.dosmith.omdb_favourites.databinding.FragmentSearchFormBinding;
import com.dosmith.omdb_favourites.models.SearchResult;
import com.dosmith.omdb_favourites.viewmodels.SearchActivityViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// This fragment holds the search form
public class SearchFormFragment extends Fragment {
    // viewbinding
    FragmentSearchFormBinding binding;
    // viewmodel
    SearchActivityViewModel viewModel;
    // used in shrinking or growing the form
    boolean maximized = true;
    // listener to shrink or grow the form
    private SearchShrinkListener shrinkListener;

    // interface for shrinking or growing the form
    public interface SearchShrinkListener {
        void shrinkSearch(boolean shrink);
    }

    // empty constructor
    public SearchFormFragment() {
    }

    // much of this is boilerplate from the create-fragment option
    public static SearchFormFragment newInstance(String param1, String param2) {
        SearchFormFragment fragment = new SearchFormFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        shrinkListener = (SearchShrinkListener) context;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSearchFormBinding.inflate(inflater, container, false);

        viewModel = new ViewModelProvider(requireActivity()).get(SearchActivityViewModel.class);

        // simple adapter for the spinner on my search form
        ArrayAdapter<String> spnAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                // Customize how the selected item looks in the spinner
                TextView view = (TextView) super.getView(position, convertView, parent);
                view.setText(getItem(position));
                // Modify your view (e.g., set an image or change text color)
                return view;
            }
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                view.setText(getItem(position));
                view.setPadding(5, 10, 10, 10);
                return view;
            }
        };
        spnAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spnType.setAdapter(spnAdapter);

        spnAdapter.add("Any");
        spnAdapter.add("Movies");
        spnAdapter.add("Series");
        spnAdapter.add("Episodes");

        // On Clicker for my search button
        binding.btnSearch.setOnClickListener(v->{
            // get the search parameters
            Map<String, String> params = new HashMap<>();
            if(!binding.etTitle.getText().toString().isEmpty()){
                params.put("s", binding.etTitle.getText().toString());
            }
            if (!binding.etYear.getText().toString().isEmpty()){
                params.put("y", binding.etYear.getText().toString());
            }
            switch (binding.spnType.getSelectedItem().toString()){
                case "Movies":
                    params.put("type", "movie");
                    break;
                case "Series":
                    params.put("type", "series");
                    break;
                case "Episodes":
                    params.put("type", "episode");
                    break;
                default:
                    break;
            }
            // reset certain properties of the viewmodel
            viewModel.reset();
            // store the params in the viewmodel
            viewModel.storeParams(params);
            // call the viewmodel method that will query the repository
            viewModel.queryResultsPage();

            // Observe changes to the viewmodel's search results. I'm creating an Observer class
            // so I can remove it, within its onChanged method.
            Observer<ArrayList<SearchResult>> observer = new Observer<ArrayList<SearchResult>>() {
                @Override
                public void onChanged(ArrayList<SearchResult> items) {
                    if (!items.isEmpty()){
                        binding.btnBack.setEnabled(true);
                        maximized = false;
                        if (shrinkListener != null) {
                            shrinkListener.shrinkSearch(!maximized);
                        }
                        viewModel.getSearchResults().removeObserver(this);
                    }
                }
            };
            viewModel.getSearchResults().observe(getViewLifecycleOwner(), observer);
        });

        // back button stuff (shrink form)
        binding.btnBack.setEnabled(false);
        binding.btnBack.setOnClickListener(v->{
            maximized = false;
            if (shrinkListener != null) {
                shrinkListener.shrinkSearch(!maximized);
            }
        });

        // click on the root stuff (enlarge form)
        binding.getRoot().setOnClickListener(v->{
            if (!maximized){
                maximized = true;
                if (shrinkListener != null) {
                    shrinkListener.shrinkSearch(!maximized);
                }
            }
        });

        // Observe the message from the viewmodel
        viewModel.getSearchMessage().observe(getViewLifecycleOwner(), v->{
            binding.tvMessage.setText(viewModel.getSearchMessage().getValue());
            int i = 1;
        });

        return binding.getRoot();
    }
}