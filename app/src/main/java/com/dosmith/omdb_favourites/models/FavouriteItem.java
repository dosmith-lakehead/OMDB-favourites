package com.dosmith.omdb_favourites.models;

import android.graphics.Bitmap;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;

public class FavouriteItem implements Serializable {

        // annotations allow GSON to deserialize capitalized JSON into my uncapitalized fields.
        private String title;
        private String year;
        private String imdbID;
        private String description;
        private String posterURL;
        private String imdbRating;
        private Date dateAdded;

        private Bitmap posterImg;

        public FavouriteItem(){}

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

        public String getDescription() {
            return description;
        }

        public String getYear() {
            return year;
        }

        public String getImdbRating() {return imdbRating;}
        public Date getDateAdded(){return dateAdded;}

        public void setDateAdded(Date dateAdded) {
                this.dateAdded = dateAdded;
        }

        public void setDescription(String description) {
                this.description = description;
        }

        public void setImdbRating(String imdbRating) {
                this.imdbRating = imdbRating;
        }

        public void setImdbID(String imdbID) {
                this.imdbID = imdbID;
        }

        public void setPosterURL(String posterURL) {
                this.posterURL = posterURL;
        }

        public void setTitle(String title) {
                this.title = title;
        }

        public void setYear(String year) {
                this.year = year;
        }

        public void setPosterImg(Bitmap bitmap){
            posterImg = bitmap;
        }
}
