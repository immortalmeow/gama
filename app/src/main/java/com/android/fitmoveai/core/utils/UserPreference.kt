package com.android.fitmoveai.core.utils

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPreference private constructor(private val dataStore: DataStore<Preferences>) {

    fun getUser(): Flow<UserModel> {
        return dataStore.data.map { preferences ->
            UserModel(
                preferences[NAME_KEY] ?: "",
                preferences[EMAIL_KEY] ?: "",
                preferences[PASSWORD_KEY] ?: "",
                preferences[STATE_KEY] ?: false
            )
        }
    }

    suspend fun saveUserData(user: UserModel) {
        dataStore.edit { preferences ->
            preferences[NAME_KEY] = user.username
            preferences[EMAIL_KEY] = user.email
            preferences[PASSWORD_KEY] = user.password
            preferences[STATE_KEY] = user.isLogin
        }
    }

    suspend fun login() {
        dataStore.edit { preference ->
            preference[STATE_KEY] = true
        }
    }

    suspend fun logout() {
        dataStore.edit { preference ->
            preference[STATE_KEY] = false
        }
    }

    fun getUserToken(): Flow<UserToken> = dataStore.data.map { preference ->
        UserToken(preference[TOKEN_KEY] ?: "")
    }

    suspend fun saveUserToken(UserToken: UserToken) {
        dataStore.edit { preference ->
            preference[TOKEN_KEY] = UserToken.token
        }
    }

    suspend fun deleteToken() {
        dataStore.edit { preference ->
            preference[TOKEN_KEY] = ""
            preference[NAME_KEY] = ""
            preference[EMAIL_KEY] = ""
            preference[PASSWORD_KEY] = ""
        }
    }


    companion object {
        @Volatile
        private var INSTANCE: UserPreference? = null

        private val NAME_KEY = stringPreferencesKey("name")
        private val EMAIL_KEY = stringPreferencesKey("email")
        private val PASSWORD_KEY = stringPreferencesKey("password")
        private val STATE_KEY = booleanPreferencesKey("state")
        private val TOKEN_KEY = stringPreferencesKey("token")

        fun getInstance(dataStore: DataStore<Preferences>): UserPreference {
            return INSTANCE ?: synchronized(this) {
                val instance = UserPreference(dataStore)
                INSTANCE = instance
                instance
            }
        }
    }
}