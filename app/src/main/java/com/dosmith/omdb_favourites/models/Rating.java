package com.dosmith.omdb_favourites.models;

public class Rating {
    private String Source;
    private String Value;

    // Simple model to display ratings in the MovieDetails activity

    public Rating(){
    }
    public String getValue() {
        return Value;
    }
    public String getSource() {
        return Source;
    }
}
