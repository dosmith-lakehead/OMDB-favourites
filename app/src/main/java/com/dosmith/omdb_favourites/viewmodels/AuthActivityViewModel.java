package com.dosmith.omdb_favourites.viewmodels;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dosmith.omdb_favourites.views.LoginFragment;
import com.dosmith.omdb_favourites.views.RegisterFragment;

public class AuthActivityViewModel extends ViewModel {

    private MutableLiveData<Fragment> authFragment = new MutableLiveData<>();

    public void setLoginFragment(){
        authFragment.setValue(new LoginFragment());
    }
    public void setRegisterFragment(){
        authFragment.setValue(new RegisterFragment());
    }

    public LiveData<Fragment> getAuthFragment(){
        return authFragment;
    }

}
