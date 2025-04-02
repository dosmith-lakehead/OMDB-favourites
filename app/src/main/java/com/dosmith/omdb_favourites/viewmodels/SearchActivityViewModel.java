package com.dosmith.omdb_favourites.viewmodels;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dosmith.omdb_favourites.models.SearchResult;
import com.dosmith.omdb_favourites.repository.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// ViewModel for the search activity
public class SearchActivityViewModel extends ViewModel {
    // The list of SearchResults to expose to the view
    private MutableLiveData<ArrayList<SearchResult>> searchResults = new MutableLiveData<>(new ArrayList<>());
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
