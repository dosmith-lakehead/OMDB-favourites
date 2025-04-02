package com.dosmith.omdb_favourites.views;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.dosmith.omdb_favourites.databinding.FragmentRegisterBinding;
import com.dosmith.omdb_favourites.viewmodels.AuthActivityViewModel;

import java.util.HashMap;
import java.util.Map;

public class RegisterFragment extends Fragment {

    FragmentRegisterBinding binding;
    AuthActivityViewModel viewModel;

    public RegisterFragment() {
        // Required empty public constructor
    }

    public static RegisterFragment newInstance() {
        RegisterFragment fragment = new RegisterFragment();
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
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(AuthActivityViewModel.class);

        binding.btnRegister.setOnClickListener(v-> {
            if (!binding.etPassword.getText().toString().isEmpty() &&
                    !binding.etEmail.getText().toString().isEmpty() &&
                    !binding.etUsername.getText().toString().isEmpty()) {
                viewModel.registerUser(binding.etEmail.getText().toString(), binding.etPassword.getText().toString(), binding.etUsername.getText().toString());
            }
        });

        return binding.getRoot();
    }
}