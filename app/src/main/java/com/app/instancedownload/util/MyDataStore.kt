package com.app.instancedownload.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MyDataStore @Inject constructor(private val context: Context) {

    companion object {
        val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
        private val isDeleteKey = booleanPreferencesKey("isDelete")
        private val isFirstTimeLaunchKey = booleanPreferencesKey("IsFirstTimeLaunch")
        private val themSettingKey = stringPreferencesKey("themSetting")
    }

    //Insert user id
    suspend fun isDelete(id: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[isDeleteKey] = id
        }
    }

    //Get user id
    val isDelete: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[isDeleteKey] ?: false
        }

    //User login or not save
    suspend fun isFirstTimeLaunch(isLogin: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[isFirstTimeLaunchKey] = isLogin
        }
    }

    //Check user login or not
    val isFirstTimeLaunch: Flow<Boolean?> = context.dataStore.data
        .map { preferences ->
            preferences[isFirstTimeLaunchKey] ?: false
        }

    //User login or not save
    suspend fun themSetting(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[themSettingKey] = theme
        }
    }

    //Check user login or not
    val themSetting: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[themSettingKey] ?: "system"
        }

}