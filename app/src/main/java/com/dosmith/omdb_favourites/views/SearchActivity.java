package com.dosmith.omdb_favourites.views;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.dosmith.omdb_favourites.R;
import com.dosmith.omdb_favourites.databinding.ActivitySearchBinding;
import com.dosmith.omdb_favourites.repository.Repository;
import com.dosmith.omdb_favourites.viewmodels.SearchActivityViewModel;

public class SearchActivity extends AppCompatActivity implements SearchFormFragment.SearchShrinkListener {

    SearchActivityViewModel viewModel;
    ActivitySearchBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Repository.setContext(this.getApplicationContext());
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        viewModel = new ViewModelProvider(this).get(SearchActivityViewModel.class);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });
        viewModel.setUID(getIntent().getStringExtra("uID"));
        viewModel.setUsername(getIntent().getStringExtra("userName"));
    }

    // This fancy little listener shrinks the search fragment. I think it looks neat.
    @Override
    public void shrinkSearch(boolean shrink) {
        LinearLayout.LayoutParams searchContainerParams = (LinearLayout.LayoutParams) binding.searchFormContainer.getLayoutParams();
        LinearLayout.LayoutParams spaceParams = (LinearLayout.LayoutParams) binding.filler.getLayoutParams();

        // I had to learn how to do this. I used web resources. It might be a little messy.
        ValueAnimator animator;
        // Either go from 1 to 0 or 0 to 1
        if (shrink){
            animator = ValueAnimator.ofFloat(1.0f, 0.0f);
        }
        else {
            animator = ValueAnimator.ofFloat(0.0f, 1.0f);
        }
        // duration: 1 sec
        animator.setDuration(1000);
        // This kind of eases the transition. I played with values until it looked right.
        animator.setInterpolator(new DecelerateInterpolator(2f));

        // When the animated value updates:
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                // Get the animated value
                float animatedValue = (float) animator.getAnimatedValue();
                // update the weight of the searchContainer so it shrinks (or grows)
                searchContainerParams.weight = animatedValue;
                // Update the weight of a space that takes up the rest of the... space
                spaceParams.weight = 1.0f - animatedValue;
                // Apply the parameters.
                binding.filler.setLayoutParams(spaceParams);
                binding.searchFormContainer.setLayoutParams(searchContainerParams);
            }
        });
        // GO!
        animator.start();
    }
}