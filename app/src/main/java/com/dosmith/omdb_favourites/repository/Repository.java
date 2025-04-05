package com.dosmith.omdb_favourites.repository;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.dosmith.omdb_favourites.R;
import com.dosmith.omdb_favourites.models.FavouriteItem;
import com.dosmith.omdb_favourites.models.MovieDetails;
import com.dosmith.omdb_favourites.models.SearchResult;
import com.dosmith.omdb_favourites.utilities.VolleySingleton;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;

// This isn't exactly the pattern discussed in class, so let me explain:

// My MVVM pattern uses a repository to keep the data model classes isolated
// from API calls. This repository is responsible for making calls to the API,
// converting the results into instances of my model classes, and storing them
// for retrieval by my ViewModels.
public class Repository {

    // These variables are used to determine if the repository is actively
    // fetching data, and if all the available data has been fetched
    private static int activeReqCount = 0;
    private static boolean allResultsLoaded = false;

    // My repository has to be contain a reference to the application
    // context because Volley needs it.
    private static Context context;

    private static String searchMessage;

    // These variables contain the results of the API calls,
    // formatted into instances of my model classes.
    private static ArrayList<SearchResult> searchResults;
    private static ArrayList<FavouriteItem> favourites;
    private static MovieDetails movieDetails;

    // Context setter
    public static void setContext(Context ctx){
        context = ctx;
    }

    // Getters for repository properties
    public static int getActiveReqCount(){
        return activeReqCount;
    }
    public static boolean getAllResultsLoaded(){
        return allResultsLoaded;
    }
    public static ArrayList<SearchResult> getSearchResults(){
        return searchResults;
    }
    public static ArrayList<FavouriteItem> getFavourites(){
        return favourites;
    }
    public static MovieDetails getMovieDetails(){
        return movieDetails;
    }
    public static String getSearchMessage(){
        return searchMessage;
    }

    /**
     * Uses Volley to make an API call, the results of which
     * will overwrite the searchResults property with a page of
     * searchResults
     * @param params a map of search parameters
     * @param page which page of results to ask for
     */
    public static void addResultsPage(Map<String, String> params, int page){
        // Indicates that an API request is pending
        activeReqCount += 1;
        // Build the URL
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append("https://www.omdbapi.com/?apikey=2904fe35");
        for (String paramKey : params.keySet()){
            urlBuilder.append("&");
            urlBuilder.append(paramKey);
            urlBuilder.append("=");
            urlBuilder.append(params.get(paramKey));
        }
        urlBuilder.append("&page=");
        urlBuilder.append(page);
        String url = urlBuilder.toString();

        // Create the JsonObjectRequest (returns a JSONObject or an error)
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                // GSON is a google library I'm using to deserialize JSON objects.
                // I did mention this to you in class one day, you seemed
                // somewhat non-committal but didn't refuse.
                Gson gson = new Gson();
                try {
                    // If the response contains "Response": false,
                    if (!response.getBoolean("Response")){
                        // We've gotten all the results we can from these parameters (which may be none)
                        allResultsLoaded = true;
                        if (searchResults.isEmpty()){
                            searchMessage = "No results found.";
                        }
                    }
                    // Otherwise, there is response data/
                    else {
                        searchResults = new ArrayList<>(10);
                        // Get the array of results
                        JSONArray resultsArray = response.getJSONArray("Search");
                        // For each item in the array:
                        for (int i = 0; i < resultsArray.length(); i++){
                            // Put a placeholder in searchResults
                            searchResults.add(null);
                            // Create a SearchResult from the item
                            JSONObject resultItem = resultsArray.getJSONObject(i);
                            SearchResult searchResult = gson.fromJson(resultItem.toString(), SearchResult.class);
                            // use getImage to download the image. This function will ultimately insert
                            // the SearchResult into searchResults, and it uses the i parameter to know
                            // where in the list the SearchResult should go. I'm doing this to preserve result order
                            // even though one image request may take longer or shorter than another.
                            getImage(searchResult, i);
                        }
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                finally {
                    // The request is now finished
                    activeReqCount -= 1;
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                searchMessage = "An error occurred.";
                activeReqCount -= 1;
            }
        });

        // I don't want the request to retry, and I'm setting the timeout to 5 seconds.
        getRequest.setRetryPolicy(new DefaultRetryPolicy(5000,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Volley uses a singleton pattern. Here I'm getting my volley instance (or creating one if it doesn't exist yet)
        // and then adding the request to that instance's request queue.
        VolleySingleton.getInstance(context).addToRequestQueue(getRequest);
    }

    /**
     * Downloads the image relevant to a SearchResult. When complete, the SearchResult is inserted
     * into its correct position in searchResults.
     * @param searchResult the SearchResult to get an image for
     * @param i the position that searchResult will be inserted in searchResults
     */
    public static void getImage(SearchResult searchResult, int i){
        // If the listed poster URL isn't "N/A"
        if (!searchResult.getPosterURL().equals("N/A")) {
            // A request is pending
            activeReqCount += 1;
            // create the request, using the posterURL property of searchResult as the url
            ImageRequest imgRequest = new ImageRequest(searchResult.getPosterURL(), new Response.Listener<Bitmap>() {
                @Override
                public void onResponse(Bitmap response) {
                    // On a successful response, set searchResult's posterImg property and insert it into searchResults
                    searchResult.setPosterImg(response);
                    searchResults.set(i, searchResult);
                    activeReqCount -= 1;
                }
            }, 300, 448, ImageView.ScaleType.CENTER, Bitmap.Config.ARGB_8888, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // If we don't get a 200 response, the image couldn't be retrieved.
                    // We're going to use a default image in that case.
                    searchResult.setPosterImg(BitmapFactory.decodeResource(context.getResources(), R.drawable.no_poster));
                    searchResults.set(i, searchResult);
                    activeReqCount -= 1;
                }
            });

            //See addResultsPage method for explanation of the below lines
            imgRequest.setRetryPolicy(new DefaultRetryPolicy(5000,
                    0,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            VolleySingleton.getInstance(context).addToRequestQueue(imgRequest);
        }
        // Here, the poster URL WAS "N/A" so we use the placeholder.
        else {
            searchResult.setPosterImg(BitmapFactory.decodeResource(context.getResources(), R.drawable.no_poster));
            searchResults.set(i, searchResult);
        }
    }

    /**
     * Make an API call to retrieve details about a single movie; insert those details into the
     * repositories movieDetails field.
     * @param imdbId the imdbId of the movie to search for
     */
    public static void setMovieDetails(String imdbId){
        // a req is pending
        activeReqCount += 1;
        // empty the movieDetails property
        movieDetails = null;
        // build the url
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append("https://www.omdbapi.com/?apikey=2904fe35&i=");
        urlBuilder.append(imdbId);
        String url = urlBuilder.toString();

        // build the request
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                // On successful response, deserialize the response using GSON into a MovieDetails object.
                Gson gson = new Gson();
                try {
                    if (!response.getBoolean("Response")){
                        searchMessage = "An unexpected error occurred. The IMDB ID related to this movie does not have a related page.";
                    }
                    else {
                        movieDetails = gson.fromJson(response.toString(), MovieDetails.class);
                        // Again we need to get an image. It's possible I'm making a mistake in trying to get
                        // the image before passing along any data to the ViewModel.
                        getImage(movieDetails);
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                finally {
                    // request is finished
                    activeReqCount -= 1;
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                searchMessage = "An error occurred.";
                activeReqCount -= 1;
            }
        });

        // Explained previously
        getRequest.setRetryPolicy(new DefaultRetryPolicy(5000,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        VolleySingleton.getInstance(context).addToRequestQueue(getRequest);
    }

    /**
     * Get a MovieDetails image, then put the MovieDetails into the repositories movieDetails property.
     * @param movie the MovieDetails to get the image for
     */
    public static void getImage(MovieDetails movie){
        if (!movieDetails.getPosterURL().equals("N/A")) {
            // req is active
            activeReqCount += 1;
            // build the req
            ImageRequest imgRequest = new ImageRequest(movieDetails.getPosterURL(), new Response.Listener<Bitmap>() {
                @Override
                public void onResponse(Bitmap response) {
                    // On successful response, attach the img to movie and set movieDetails to movie.
                    movie.setPosterImg(response);
                    movieDetails = movie;
                    activeReqCount -= 1;
                }
            }, 300, 448, ImageView.ScaleType.CENTER, Bitmap.Config.ARGB_8888, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // on fail, pass the movie into movieDetails anyway
                    movieDetails = movie;
                    activeReqCount -= 1;
                }
            });
            // explained previously
            imgRequest.setRetryPolicy(new DefaultRetryPolicy(5000,
                    0,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            VolleySingleton.getInstance(context).addToRequestQueue(imgRequest);
        }
        // If there's no image to get, just pass the movie into movieDetails
        else {
            movieDetails = movie;
        }
    }

    /**
     * Gets bitmap image for FavouriteItem, then adds it to the repository in the correct place.
     * Works a lot like the version for MovieDetails objects
     * @param favourite
     * @param i
     */
    public static void getImage(FavouriteItem favourite, int i){
        // If the listed poster URL isn't "N/A"
        if (!favourite.getPosterURL().equals("N/A")) {
            // A request is pending
            activeReqCount += 1;
            // create the request, using the posterURL property of searchResult as the url
            ImageRequest imgRequest = new ImageRequest(favourite.getPosterURL(), new Response.Listener<Bitmap>() {
                @Override
                public void onResponse(Bitmap response) {
                    // On a successful response, set searchResult's posterImg property and insert it into searchResults
                    favourite.setPosterImg(response);
                    favourites.set(i, favourite);
                    activeReqCount -= 1;
                }
            }, 300, 448, ImageView.ScaleType.CENTER, Bitmap.Config.ARGB_8888, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // If we don't get a 200 response, the image couldn't be retrieved.
                    // We're going to use a default image in that case.
                    favourite.setPosterImg(BitmapFactory.decodeResource(context.getResources(), R.drawable.no_poster));
                    favourites.set(i, favourite);
                    activeReqCount -= 1;
                }
            });

            //See addResultsPage method for explanation of the below lines
            imgRequest.setRetryPolicy(new DefaultRetryPolicy(5000,
                    0,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            VolleySingleton.getInstance(context).addToRequestQueue(imgRequest);
        }
        // Here, the poster URL WAS "N/A" so we use the placeholder.
        else {
            favourite.setPosterImg(BitmapFactory.decodeResource(context.getResources(), R.drawable.no_poster));
            favourites.set(i, favourite);
        }
    }

    /**
     * Takes an input list of FavouriteItem objects and sets the size of
     * the favourites field to match, then passes each object to getImage,
     * where they will be given images and inserted in order into favourites
     * @param inputFavourites
     */
    public static void populateFavourites(ArrayList<FavouriteItem> inputFavourites){
        favourites = new ArrayList<>(inputFavourites.size());
        for (int i = 0; i < inputFavourites.size(); i++){
            favourites.add(null);
        }
        inputFavourites.sort(Comparator.comparing(FavouriteItem::getDateAdded, Comparator.reverseOrder()));
        for (int i=0; i<inputFavourites.size();i++){
            getImage(inputFavourites.get(i), i);
        }
    }

    /**
     * Add a single FavouriteItem to the repository
     * @param inputFavourite
     */
    public static void addFavourite(FavouriteItem inputFavourite){
        favourites.add(0, null);
        getImage(inputFavourite, 0);
    }

    /**
     * Remove a single favourites item from the repository
     * @param inputFavourite
     */
    public static void removeOneFavourite(FavouriteItem inputFavourite){
        for (FavouriteItem item : favourites){
            if(Objects.equals(item.getImdbID(), inputFavourite.getImdbID())){
                favourites.remove(item);
                break;
            }
        }
    }

    // Clear out the repository of searchResults
    public static void reset(){
        allResultsLoaded = false;
        searchMessage = "";
        searchResults = new ArrayList<>();
    }
}
