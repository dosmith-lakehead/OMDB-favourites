package com.dosmith.omdb_favourites.views;

import android.content.Intent;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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

        // Observe which auth fragment to display from the viewmodel, and display
        // the appropriate one.
        viewModel.getAuthFragment().observe(this, f->{
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(binding.authFragmentContainer.getId(), f);
            transaction.commit();
        });

        // If the viewmodel is storing aa username, it means the user has logged in.
        viewModel.getUserName().observe(this, userName -> {
            if (!userName.isEmpty()){
                // Proceed to main activity (SearchActivity)
                Intent proceedToMain = new Intent(getApplicationContext(), SearchActivity.class);
                proceedToMain.putExtra("uID", viewModel.getUserId().getValue());
                proceedToMain.putExtra("userName", viewModel.getUserName().getValue());
                startActivity(proceedToMain);
            }
        });
    }
}