package com.dosmith.omdb_favourites.views;

import static android.view.Gravity.END;
import static android.view.Gravity.START;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.dosmith.omdb_favourites.R;
import com.dosmith.omdb_favourites.databinding.ActivityDetailsBinding;
import com.dosmith.omdb_favourites.models.Rating;
import com.dosmith.omdb_favourites.viewmodels.DetailsActivityViewModel;

public class DetailsActivity extends AppCompatActivity {

    DetailsActivityViewModel viewModel;
    ActivityDetailsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        viewModel = new ViewModelProvider(this).get(DetailsActivityViewModel.class);
        binding = ActivityDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // When the ViewModel's movieDetails property changes:
        viewModel.getMovieDetails().observe(this, movieDetails -> {
            // Display all the info in the view
            if (movieDetails != null) {
                binding.tvTitle.setText(movieDetails.getTitle());
                if (movieDetails.getPosterImg() != null){
                    binding.imgPoster.setImageBitmap(movieDetails.getPosterImg());
                }
                binding.tvYear.setText(movieDetails.getYear());
                if (movieDetails.getRuntime()!=null || movieDetails.getRuntime().equals("N/A")) {
                    binding.tvTime.setText(movieDetails.getRuntime());
                }
                else {
                    binding.tvTime.setVisibility(View.INVISIBLE);
                }
                String type = movieDetails.getType().substring(0,1).toUpperCase() + movieDetails.getType().substring(1);
                binding.tvType.setText(type);
                binding.tvRating.setText(movieDetails.getRated());
                binding.tvSummary.setText(movieDetails.getPlot());
                binding.tvGenres.setText(movieDetails.getGenre());
                binding.tvDirector.setText(movieDetails.getDirector());
                binding.tvWriter.setText(movieDetails.getWriter());
                binding.tvActors.setText(movieDetails.getActors());
                binding.tvLanguage.setText(movieDetails.getLanguage());
                binding.tvCountry.setText(movieDetails.getCountry());
                binding.tvReleaseDate.setText(movieDetails.getReleased());
                binding.tvBoxOffice.setText(movieDetails.getBoxOffice());
                binding.tvAwards.setText(movieDetails.getAwards());
                binding.tvMetaScore.setText(movieDetails.getMetascore());
                binding.imdbRating.setText(movieDetails.getMetascore());
                String votes = "(" + movieDetails.getImdbVotes() + " Votes)";
                binding.imdbVotes.setText(votes);
                // ratings gets some special treatment
                if (movieDetails.getRatings().length > 0){
                    for (Rating rating : movieDetails.getRatings()) {
                        LinearLayout ratingContainer = new LinearLayout(this);
                        ratingContainer.setOrientation(LinearLayout.HORIZONTAL);
                        LinearLayout.LayoutParams ratingContainerParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        ratingContainer.setLayoutParams(ratingContainerParams);
                        TextView source = new TextView(this);
                        source.setText(rating.getSource());
                        source.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                        LinearLayout.LayoutParams sourceParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
                        sourceParams.gravity = START;
                        sourceParams.weight = 0.7f;
                        source.setLayoutParams(sourceParams);
                        TextView score = new TextView(this);
                        score.setText(rating.getValue());
                        score.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                        LinearLayout.LayoutParams scoreParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
                        scoreParams.gravity = END;
                        scoreParams.weight = 0.3f;
                        score.setLayoutParams(scoreParams);
                        ratingContainer.addView(source);
                        ratingContainer.addView(score);
                        binding.innerRatingsContainer.addView(ratingContainer);
                    }
                }
                else {
                    binding.receptionContainer.removeView(binding.tvRatingsHeader);
                    binding.receptionContainer.removeView(binding.ratingsContainer);
                }
                binding.tvDVD.setText(movieDetails.getDvd());
                binding.tvProduction.setText(movieDetails.getProduction());
                binding.tvWebsite.setText(movieDetails.getWebsite());
                binding.tvimdbId.setText(movieDetails.getImdbID());
                binding.btnBack.setOnClickListener(v->{
                    finish();
                });
            }
        });

        // After having set up the view to observe the ViewModel,
        // all that's left is to get the imdbId from the intent and
        // use it to query for details.
        Intent intent = getIntent();
        String imdbId = intent.getStringExtra("imdbId");
        viewModel.queryMovieDetails(imdbId);

    }
}