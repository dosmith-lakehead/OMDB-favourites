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
import com.dosmith.omdb_favourites.models.SearchResult;
import com.dosmith.omdb_favourites.repository.Repository;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

// ViewModel for the search activity
public class SearchActivityViewModel extends ViewModel {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    // The list of SearchResults to expose to the view
    private MutableLiveData<ArrayList<SearchResult>> searchResults = new MutableLiveData<>(new ArrayList<>());
    // The list of FavouriteItems to expose to the view
    private MutableLiveData<ArrayList<FavouriteItem>> favourites = new MutableLiveData<>(new ArrayList<>());
    // Bring favourites to front or not
    private MutableLiveData<Boolean> showFavourites = new MutableLiveData<>(false);
    // Tracks what page of results has been queried so far
    private int resultsPage = 0;
    // Tracks if all the results queryable for a given search have been queried
    private boolean allResultsLoaded = false;
    // Is the Repository currently getting results? (this might be redundant)
    private MutableLiveData<Boolean> addingResults = new MutableLiveData<>(false);
    // A message (gotten from repository) to expose to the view about search success / failure
    private MutableLiveData<String> searchMessage = new MutableLiveData<>();
    // Search parameters
    private Map<String,String> params = new HashMap<>();

    // getters
    public LiveData<Boolean> getAddingResults(){
        return addingResults;
    }
    public LiveData<ArrayList<SearchResult>> getSearchResults(){
        return searchResults;
    }
    public LiveData<ArrayList<FavouriteItem>> getFavourites(){
        return favourites;
    }
    public LiveData<String> getSearchMessage(){
        return searchMessage;
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
    // Switch between showing favourites on top and not
    public void toggleFavourites(){
        showFavourites.setValue(!showFavourites.getValue());
    }
    // getter for the view to observe to see if it should show favourites on top or not
    public LiveData<Boolean> getShowFavourite(){
        return showFavourites;
    }

    // Use a background thread to ask the repository to fetch a page of results.
    // If there are additional results to fetch for the given query, append them
    // into searchResults.
    public void queryResultsPage(){
        if (!(addingResults.getValue() || allResultsLoaded)){
            addingResults.setValue(true);
            // get the existing results
            ArrayList<SearchResult> tempResults = searchResults.getValue();
            // use a background thread when querying
            Thread backgroundThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Looper.prepare();
                    Handler handler = new Handler(Looper.myLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            // query
                            Repository.addResultsPage(params, ++resultsPage);
                            // wait
                            while (Repository.getActiveReqCount() > 0){
                                try {
                                    Thread.sleep(10);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            // find out if there were new results. If so, add them.
                            allResultsLoaded = Repository.getAllResultsLoaded();
                            if (!allResultsLoaded) {
                                tempResults.addAll(Repository.getSearchResults());
                                searchResults.postValue(tempResults);
                            }
                            addingResults.postValue(false);
                            searchMessage.postValue(Repository.getSearchMessage());
                        }
                    });
                    Looper.loop();
                }
            });
            backgroundThread.start();
        }
    }

    // Get a list of FavouriteItems from the DB, then send them to the repository
    // to attach images and store them in memory
    public void populateFavourites(){
        ArrayList<FavouriteItem> tempFavourites = new ArrayList<>();
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
                                userFavourites.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                FavouriteItem item = new FavouriteItem();
                                                item.setYear(document.get("Year").toString());
                                                item.setTitle(document.get("Title").toString());
                                                item.setDescription(document.get("Description").toString());
                                                item.setImdbID(document.get("IMDBID").toString());
                                                item.setPosterURL(document.get("PosterURL").toString());
                                                Timestamp date = (Timestamp) document.get("DateAdded");
                                                item.setDateAdded(date.toDate());
                                                item.setImdbRating(document.get("IMDBRating").toString());
                                                tempFavourites.add(item);
                                            }

                                            Thread backgroundThread = new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Looper.prepare();
                                                    Handler handler = new Handler(Looper.myLooper());
                                                    handler.post(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            // query
                                                            Repository.populateFavourites(tempFavourites);
                                                            // wait
                                                            while (Repository.getActiveReqCount() > 0){
                                                                try {
                                                                    Thread.sleep(10);
                                                                } catch (InterruptedException e) {
                                                                    throw new RuntimeException(e);
                                                                }
                                                            }
                                                            favourites.postValue(Repository.getFavourites());
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

    // Remove a specific FavouriteItem from the repository and DB
    public void removeOneFavourite(FavouriteItem item){
        CollectionReference users = db.collection("Users");
        Query query = users.whereEqualTo("UserId", uID.getValue()).limit(1);
        query.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        Log.d("DBQuery", "Querying for uID");
                        if (task.isSuccessful()) {
                            if (!task.getResult().isEmpty()) {
                                CollectionReference userFavourites = task.getResult().getDocuments().get(0).getReference().collection("Favourites");
                                Query favouritesQuery = userFavourites.whereEqualTo("IMDBID", item.getImdbID());
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
                                                                            Repository.removeOneFavourite(item);
                                                                            refreshFavourites();
                                                                        } else {
                                                                            Log.d("DBQuery", "Delete failed");
                                                                        }
                                                                    }
                                                                });
                                                    }
                                                } else {
                                                    Log.d("DBQuery", "Getting specific favourite failed");
                                                }
                                            }
                                        });
                            }
                        }
                    }
                });
    }

    public void refreshFavourites(){
        this.favourites.postValue(Repository.getFavourites());
    }

    // called from the view, this feeds search params into the ViewModel
    public void storeParams(Map<String, String> params){
        this.params = params;
    }

    // reset certain properties
    public void reset(){
        resultsPage = 0;
        allResultsLoaded = false;
        addingResults.setValue(false);
        searchResults.setValue(new ArrayList<>());
        Repository.reset();
    }
}
