package com.github.oryanmat.trellowidget.viewmodels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.oryanmat.trellowidget.data.TrelloWidgetRepository
import com.github.oryanmat.trellowidget.data.model.User
import com.github.oryanmat.trellowidget.data.remote.ApiResponse
import com.github.oryanmat.trellowidget.util.Constants.FULL_NAME_PREF_KEY
import com.github.oryanmat.trellowidget.util.Constants.USERNAME_PREF_KEY
import com.github.oryanmat.trellowidget.util.preferences
import kotlinx.coroutines.launch

class LoggedInViewModel(private val repository: TrelloWidgetRepository) : ViewModel() {
    val liveUser: LiveData<ApiResponse<User>> = repository.user
    var loggedInUser: User? = null

    fun retrieveUser(context: Context) {
        val preference = context.preferences()
        val username = preference.getString(USERNAME_PREF_KEY, "")
        val fullName = preference.getString(FULL_NAME_PREF_KEY, "")
        if (username.isNullOrEmpty() || fullName.isNullOrEmpty())
            return
        loggedInUser = User(fullName, username)
    }

    fun storeUser(context: Context, user: User) {
        loggedInUser = user
        context.preferences().edit()
            .putString(USERNAME_PREF_KEY, user.username)
            .putString(FULL_NAME_PREF_KEY, user.fullName)
            .apply()
    }

    fun tryFetchUser() {
        viewModelScope.launch {
            repository.fetchUser()
        }
    }
}