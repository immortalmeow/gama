package com.android.fitmoveai.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.android.fitmoveai.core.utils.UserModel
import com.android.fitmoveai.core.utils.UserPreference
import com.android.fitmoveai.core.utils.UserToken
import kotlinx.coroutines.launch

class LoginViewModel(private val pref: UserPreference) : ViewModel() {

    fun getUser(): LiveData<UserModel> {
        return pref.getUser().asLiveData()
    }

    fun login() {
        viewModelScope.launch {
            pref.login()
        }
    }

    fun saveUserData(userData: UserToken) {
        viewModelScope.launch {
            pref.saveUserToken(userData)
        }
    }

}