package com.dosmith.omdb_favourites.viewmodels;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dosmith.omdb_favourites.models.MovieDetails;
import com.dosmith.omdb_favourites.repository.Repository;

// Fairly simple viewmodel for the details activity
public class DetailsActivityViewModel extends ViewModel {
    // the MovieDetails object that will be exposed to the view
    private MutableLiveData<MovieDetails> movieDetails = new MutableLiveData<>();
    public LiveData<MovieDetails> getMovieDetails() {
        return movieDetails;
    }

    // Ask the repository to fetch a MovieDetails object on a background thread.
    // Once it's done, post its value into movieDetails property
    public void queryMovieDetails(String imdbId){
        movieDetails.setValue(null);
        Thread backgroundThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                Handler handler = new Handler(Looper.myLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        // Call the repository method that will get the data
                        Repository.setMovieDetails(imdbId);
                        // wait for the data
                        while (Repository.getActiveReqCount() > 0){
                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        // once the api call is resolved, post the value.
                        movieDetails.postValue(Repository.getMovieDetails());
                    }
                });
                Looper.loop();
            }
        });
        backgroundThread.start();
    }
}

