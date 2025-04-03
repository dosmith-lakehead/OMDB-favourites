package com.dosmith.omdb_favourites.viewmodels;

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

public class FavouritesDetailsActivityViewModel extends ViewModel {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private MutableLiveData<CollectionReference> userFavourites = new MutableLiveData<>();
    private MutableLiveData<String> favouriteDescription = new MutableLiveData<>();

    // the MovieDetails object that will be exposed to the view
    private MutableLiveData<MovieDetails> movieDetails = new MutableLiveData<>();
    public LiveData<MovieDetails> getMovieDetails() {
        return movieDetails;
    }

    private MutableLiveData<Boolean> removed = new MutableLiveData<>(false);

    public void setRemoved(){
        removed.postValue(true);
    }
    public LiveData<Boolean> getRemoved(){
        return removed;
    }
    private MutableLiveData<String> uID = new MutableLiveData<>();

    public void setUID(String input){
        uID.setValue(input);
    }
    public LiveData<String> getUID(){
        return uID;
    }

    public LiveData<String> getFavouriteDescription(){
        return favouriteDescription;
    }

    public LiveData<CollectionReference> getUserFavourites(){
        return userFavourites;
    }

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

    public void setUserFavourites(){
        CollectionReference users = db.collection("Users");
        Query userQuery = users.whereEqualTo("UserId", uID.getValue()).limit(1);
        userQuery.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        Log.d("DBQuery", "Querying for uID");
                        if (task.isSuccessful()) {
                            if (!task.getResult().isEmpty()) {
                                userFavourites.postValue(task.getResult().getDocuments().get(0).getReference().collection("Favourites"));
                            }
                        }
                        else {
                            Log.d("DBQuery", "Well shit.");
                        }
                    }
                });
    }

    public void setFavouriteDescription(){
        Query favouritesQuery = userFavourites.getValue().whereEqualTo("IMDBID", movieDetails.getValue().getImdbID());
        favouritesQuery.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        Log.d("DBQuery", "Getting specific favourite");
                        if (task.isSuccessful()) {
                            if (!task.getResult().isEmpty()) {
                                favouriteDescription.postValue(task.getResult().getDocuments().get(0).get("Description").toString());
                            }
                        }
                        else {
                            Log.d("DBQuery","Getting specific favourite failed");
                        }
                    }
                });
    }

    public void removeOneFavourite(){
        Query favouritesQuery = userFavourites.getValue().whereEqualTo("IMDBID", movieDetails.getValue().getImdbID());
        favouritesQuery.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        Log.d("DBQuery", "Getting specific favourite");
                        if (task.isSuccessful()) {
                            if (!task.getResult().isEmpty()) {
                                task.getResult().getDocuments().get(0).getReference().delete()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Log.d("DBQuery", "Getting specific favourite");
                                                if (task.isSuccessful()) {
                                                    Log.d("DBQuery", "favourite deleted");
                                                    removed.setValue(true);
                                                }
                                                else {
                                                    Log.d("DBQuery", "Delete failed");
                                                }
                                            }
                                        });
                            }
                        }
                        else {
                            Log.d("DBQuery","Getting specific favourite failed");
                        }
                    }
                });
    }
    public void editFavourite(String description){
        Query favouritesQuery = userFavourites.getValue().whereEqualTo("IMDBID", movieDetails.getValue().getImdbID());
        favouritesQuery.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        Log.d("DBQuery", "Getting specific favourite");
                        if (task.isSuccessful()) {
                            if (!task.getResult().isEmpty()) {
                                task.getResult().getDocuments().get(0).getReference().update("Description", description)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Log.d("DBQuery", "Getting specific favourite");
                                                if (task.isSuccessful()) {
                                                    Log.d("DBQuery", "favourite updated");
                                                }
                                                else {
                                                    Log.d("DBQuery", "Update failed");
                                                }
                                            }
                                        });
                            }
                        }
                        else {
                            Log.d("DBQuery","Getting specific favourite failed");
                        }
                    }
                });
    }
}

