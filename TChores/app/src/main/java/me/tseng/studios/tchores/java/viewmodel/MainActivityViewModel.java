package me.tseng.studios.tchores.java.viewmodel;

import android.arch.lifecycle.ViewModel;

import me.tseng.studios.tchores.java.Filters;

/**
 * ViewModel for {@link me.tseng.studios.tchores.MainActivity}.
 */

public class MainActivityViewModel extends ViewModel {

    private boolean mIsSigningIn;
    private Filters mFilters;

    public MainActivityViewModel() {
        mIsSigningIn = false;
        mFilters = Filters.getDefault();
    }

    public MainActivityViewModel(String currentUserName) {
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
