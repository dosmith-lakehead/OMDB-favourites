package com.dosmith.omdb_favourites.views;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.dosmith.omdb_favourites.databinding.FragmentAuthMenuBinding;
import com.dosmith.omdb_favourites.viewmodels.AuthActivityViewModel;

public class AuthMenuFragment extends Fragment {

    FragmentAuthMenuBinding binding;
    AuthActivityViewModel viewModel;

    public AuthMenuFragment() {
        // Required empty public constructor
    }

    public static AuthMenuFragment newInstance() {
        AuthMenuFragment fragment = new AuthMenuFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAuthMenuBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(AuthActivityViewModel.class);

        binding.btnShowLogin.setOnClickListener(v->{
            viewModel.setLoginFragment();
        });

        binding.btnShowRegister.setOnClickListener(v->{
            viewModel.setRegisterFragment();
        });

        return binding.getRoot();
    }
}