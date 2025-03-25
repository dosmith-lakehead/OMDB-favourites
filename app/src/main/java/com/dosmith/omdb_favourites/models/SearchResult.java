package com.dosmith.omdb_favourites.models;

import android.graphics.Bitmap;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class SearchResult implements Serializable {

    // annotations allow GSON to deserialize capitalized JSON into my uncapitalized fields.
    @SerializedName("Title")
    private String title;
    @SerializedName("Year")
    private String year;
    private String imdbID;
    @SerializedName("Type")
    private String type;
    @SerializedName("Poster")
    private String posterURL;

    private Bitmap posterImg;

    public SearchResult(){
    }

    public String getImdbID() {
        return imdbID;
    }

    public String getPosterURL() {
        return posterURL;
    }

    public Bitmap getPosterImg() {
        return posterImg;
    }

    public String getTitle() {
        return title;
    }

    public String getType() {
        return type;
    }

    public String getYear() {
        return year;
    }

    public void setPosterImg(Bitmap bitmap){
        posterImg = bitmap;
    }
}
