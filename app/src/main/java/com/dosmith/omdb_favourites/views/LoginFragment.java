package com.dosmith.omdb_favourites.views;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.dosmith.omdb_favourites.databinding.FragmentLoginBinding;
import com.dosmith.omdb_favourites.viewmodels.AuthActivityViewModel;

// Simple login fragment
public class LoginFragment extends Fragment {

    FragmentLoginBinding binding;
    AuthActivityViewModel viewModel;

    public LoginFragment() {
        // Required empty public constructor
    }

    public static LoginFragment newInstance() {
        LoginFragment fragment = new LoginFragment();
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
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(AuthActivityViewModel.class);

        // Hook up to the relevant viewmodel method
        binding.btnLogin.setOnClickListener(v-> {
            if (!binding.etPassword.getText().toString().isEmpty() && !binding.etEmail.getText().toString().isEmpty()) {
                viewModel.login(binding.etEmail.getText().toString(), binding.etPassword.getText().toString());
            }
        });

        return binding.getRoot();
    }
}