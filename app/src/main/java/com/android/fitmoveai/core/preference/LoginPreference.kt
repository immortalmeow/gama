package com.android.fitmoveai.core.preference

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import java.util.prefs.Preferences

class LoginPreference(context: Context) {

    private val preferenceName = "UserPresence"
    private val sharedPref: SharedPreferences = context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)

    fun getPreferenceString(key_name: String): String? {
        return sharedPref.getString(key_name, null)
    }



    fun getPreferenceBoolean(key_name: String): Boolean {
        return sharedPref.getBoolean(key_name, false)
    }

    fun saveBoolean(key_name: String, value: Boolean) {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putBoolean(key_name, value)
        editor.apply()
    }


    companion object {
        @Volatile
        private var INSTANCE: LoginPreference? = null


        fun getInstance(dataStore: DataStore<Preferences>, context: Context): LoginPreference {
            return INSTANCE ?: synchronized(this) {
                val instance = LoginPreference(context)
                INSTANCE = instance
                instance
            }
        }
    }

}