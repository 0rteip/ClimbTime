package com.example.climbtime

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.example.climbtime.data.repository.HomeRepository
import com.example.climbtime.data.repository.SettingsRepository
import com.example.climbtime.data.repository.UserRepository
import com.example.climbtime.ui.viewmodel.HomeViewModel
import com.example.climbtime.ui.viewmodel.SettingsViewModel
import com.example.climbtime.ui.viewmodel.UserViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val Context.dataStore by preferencesDataStore(name = "climb_time_data_store")

val appModule = module {
    // Firebase
    factory { Firebase.firestore }
    factory { Firebase.auth }
    // DataStore
    single { get<Context>().dataStore }
    // Repository
    single { HomeRepository(get(), get()) }
    single { UserRepository(get(), get(), androidContext()) }
    single { SettingsRepository(get()) }

    //ViewModel
    viewModel {
        HomeViewModel(get())
    }

    viewModel {
        UserViewModel(get())
    }

    viewModel {
        SettingsViewModel(get())
    }

}