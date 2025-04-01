package com.dosmith.omdb_favourites.views;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.dosmith.omdb_favourites.R;
import com.dosmith.omdb_favourites.databinding.ActivityAuthBinding;
import com.dosmith.omdb_favourites.viewmodels.AuthActivityViewModel;

public class AuthActivity extends AppCompatActivity {

    ActivityAuthBinding binding;
    AuthActivityViewModel viewModel;

    FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityAuthBinding.inflate(getLayoutInflater());
        viewModel = new ViewModelProvider(this).get(AuthActivityViewModel.class);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        fragmentManager = getSupportFragmentManager();

        viewModel.getAuthFragment().observe(this, f->{
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(binding.authFragmentContainer.getId(), f);
            transaction.commit();
        });



    }
}