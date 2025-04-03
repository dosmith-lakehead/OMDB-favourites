package com.dosmith.omdb_favourites.viewmodels;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dosmith.omdb_favourites.views.AuthActivity;
import com.dosmith.omdb_favourites.views.LoginFragment;
import com.dosmith.omdb_favourites.views.RegisterFragment;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import javax.xml.transform.Result;

public class AuthActivityViewModel extends ViewModel {
    private MutableLiveData<String> userId = new MutableLiveData<>();
    private MutableLiveData<String> userName = new MutableLiveData<>();
    private MutableLiveData<Fragment> authFragment = new MutableLiveData<>();

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void setLoginFragment() {
        authFragment.setValue(new LoginFragment());
    }

    public void setRegisterFragment() {
        authFragment.setValue(new RegisterFragment());
    }

    public LiveData<Fragment> getAuthFragment() {
        return authFragment;
    }

    public LiveData<String> getUserId() {
        return userId;
    }
    public LiveData<String> getUserName() {
        return userName;
    }

    public void registerUser(String email, String password, String username) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("Auth", "createUserWithEmail:success");
                            FirebaseUser currentUser = mAuth.getCurrentUser();
                            Map<String, Object> userInfo = new HashMap<String, Object>();
                            userInfo.put("Username", username);
                            userInfo.put("Email Address", email);
                            userInfo.put("UserId", currentUser.getUid());
                            db.collection("Users").add(userInfo).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.d("Auth", "Post user to DB: Success");
                                        setLoginFragment();
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w("Auth", "Post user to DB: failure", task.getException());
                                    }
                                }
                            });
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("Auth", "createUserWithEmail:failure", task.getException());
                        }
                    }
                });


    }

    public void login(String email, String password){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("Auth", "signInWithEmail:success");
                            FirebaseUser currentUser = mAuth.getCurrentUser();
                            userId.postValue(currentUser.getUid());
                            CollectionReference usersRef = db.collection("Users");
                            Query query = usersRef.whereEqualTo("UserId", currentUser.getUid()).limit(1);
                            query.get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            Log.d("Auth", "Querying for Username");
                                            if (task.isSuccessful()) {
                                                if(!task.getResult().isEmpty()){
                                                    userName.postValue(task.getResult().getDocuments().get(0).get("Username").toString());
                                                }
                                            } else {
                                                Log.d("Auth", "Well shit.");
                                            }
                                        }
                                    });
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("Auth", "signInWithEmail:failure", task.getException());
                        }
                    }
                });
    }
}
