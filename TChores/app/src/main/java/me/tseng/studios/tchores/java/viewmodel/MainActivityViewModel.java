package me.tseng.studios.tchores.java.viewmodel;

import android.arch.lifecycle.ViewModel;

import me.tseng.studios.tchores.java.Filters;


public class MainActivityViewModel extends ViewModel {

    private boolean mIsSigningIn;

    public MainActivityViewModel() {
        mIsSigningIn = false;

    }

    public MainActivityViewModel(String currentUserName) {
        mIsSigningIn = false;
    }

    public boolean getIsSigningIn() {
        return mIsSigningIn;
    }

    public void setIsSigningIn(boolean mIsSigningIn) {
        this.mIsSigningIn = mIsSigningIn;
    }


}
