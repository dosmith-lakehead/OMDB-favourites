package com.dosmith.omdb_favourites.viewmodels;

import android.graphics.Movie;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dosmith.omdb_favourites.models.FavouriteItem;
import com.dosmith.omdb_favourites.models.MovieDetails;
import com.dosmith.omdb_favourites.repository.Repository;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

// Fairly simple viewmodel for the details activity
public class DetailsActivityViewModel extends ViewModel {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    // the MovieDetails object that will be exposed to the view
    private MutableLiveData<MovieDetails> movieDetails = new MutableLiveData<>();
    public LiveData<MovieDetails> getMovieDetails() {
        return movieDetails;
    }
    private MutableLiveData<String> uID = new MutableLiveData<>();
    public void setUID(String input){
        uID.setValue(input);
    }
    public LiveData<String> getUID(){
        return uID;
    }
    private MutableLiveData<String> username = new MutableLiveData<>();

    public void setUsername(String input){
        username.setValue(input);
    }
    public LiveData<String> getUsername(){
        return username;
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

    public void addOneFavourite(){
        MovieDetails movie = movieDetails.getValue();
        CollectionReference users = db.collection("Users");
        Query query = users.whereEqualTo("UserId", uID.getValue()).limit(1);
        query.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        Log.d("DBQuery", "Querying for uID");
                        if (task.isSuccessful()) {
                            if(!task.getResult().isEmpty()){
                                CollectionReference userFavourites = task.getResult().getDocuments().get(0).getReference().collection("Favourites");
                                Map<String, Object> newFavourite = new HashMap<>();
                                newFavourite.put("Title", movie.getTitle());
                                newFavourite.put("Description", movie.getPlot());
                                newFavourite.put("PosterURL", movie.getPosterURL());
                                newFavourite.put("IMDBID", movie.getImdbID());
                                newFavourite.put("DateAdded", new Timestamp(new Date()));
                                newFavourite.put("IMDBRating", movie.getImdbRating());
                                FavouriteItem item = new FavouriteItem();
                                item.setTitle(newFavourite.get("Title").toString());
                                item.setDescription(newFavourite.get("Description").toString());
                                item.setImdbID(newFavourite.get("IMDBID").toString());
                                item.setPosterURL(newFavourite.get("PosterURL").toString());
                                Timestamp date = (Timestamp) newFavourite.get("DateAdded");
                                item.setDateAdded(date.toDate());
                                item.setImdbRating(newFavourite.get("IMDBRating").toString());
                                userFavourites.add(newFavourite).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentReference> task) {
                                        if (task.isSuccessful()) {
                                            Thread backgroundThread = new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Looper.prepare();
                                                    Handler handler = new Handler(Looper.myLooper());
                                                    handler.post(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            // query
                                                            Repository.addFavourite(item);
                                                            // wait
                                                            while (Repository.getActiveReqCount() > 0){
                                                                try {
                                                                    Thread.sleep(10);
                                                                } catch (InterruptedException e) {
                                                                    throw new RuntimeException(e);
                                                                }
                                                            }
                                                        }
                                                    });
                                                    Looper.loop();
                                                }
                                            });
                                            backgroundThread.start();


                                        } else {
                                            Log.d("DBQuery", "Error getting documents: ", task.getException());
                                        }
                                    }
                                });
                            }
                        } else {
                            Log.d("Auth", "Well shit.");
                        }
                    }
                });
    }
}

