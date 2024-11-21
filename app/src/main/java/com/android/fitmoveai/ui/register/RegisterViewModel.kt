package com.android.fitmoveai.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.fitmoveai.core.utils.UserModel
import com.android.fitmoveai.core.utils.UserPreference

import kotlinx.coroutines.launch

class RegisterViewModel(private val pref: UserPreference) : ViewModel() {
    fun saveUser(user: UserModel) {
        viewModelScope.launch {
            pref.saveUserData(user)
        }
    }
}