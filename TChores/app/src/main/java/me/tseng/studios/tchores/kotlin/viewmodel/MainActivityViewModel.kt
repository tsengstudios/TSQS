package me.tseng.studios.tchores.kotlin.viewmodel

import android.arch.lifecycle.ViewModel
import me.tseng.studios.tchores.kotlin.Filters

/**
 * ViewModel for [me.tseng.studios.tchores.MainActivity].
 */

class MainActivityViewModel : ViewModel() {

    var isSigningIn: Boolean = false
    var filters: Filters = Filters.default
}
