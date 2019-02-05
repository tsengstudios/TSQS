package me.tseng.studios.tchores.java.viewmodel;

import android.arch.lifecycle.ViewModel;

import me.tseng.studios.tchores.java.Filters;


public class ChoresFragmentViewModel extends ViewModel {

    private boolean mIsSigningIn;
    private Filters mFilters;

    public ChoresFragmentViewModel() {
        mIsSigningIn = false;
        mFilters = Filters.getDefault();
    }

    public ChoresFragmentViewModel(String currentUserName) {
        mIsSigningIn = false;
        mFilters = Filters.getDefault(currentUserName);
    }

    public boolean getIsSigningIn() {
        return mIsSigningIn;
    }

    public void setIsSigningIn(boolean mIsSigningIn) {
        this.mIsSigningIn = mIsSigningIn;
    }

    public Filters getFilters() {
        return mFilters;
    }

    public void setFilters(Filters mFilters) {
        this.mFilters = mFilters;
    }
}
