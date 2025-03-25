package com.dosmith.omdb_favourites.utilities;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

// This class is necessary for Volley, my HTTP request library.
// It follows a singleton pattern.
public class VolleySingleton {
    private static VolleySingleton instance;
    private RequestQueue requestQueue;
    private static Context ctx;

    // Instantiate a new instance
    private VolleySingleton(Context context) {
        ctx = context;
        requestQueue = getRequestQueue();
    }

    // Get the active instance or create one if none exists
    public static synchronized VolleySingleton getInstance(Context context) {
        if (instance == null) {
            instance = new VolleySingleton(context);
        }
        return instance;
    }

    // Get the request queue for the instance
    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
        }
        return requestQueue;
    }

    // Add a request to the queue
    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
}
