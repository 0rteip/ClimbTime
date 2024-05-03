package com.example.climbtime.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.climbtime.data.model.theme.Theme
import com.example.climbtime.data.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


data class SettingsState(val theme: Theme)

class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    val state = settingsRepository.theme.map { theme -> SettingsState(theme) }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = SettingsState(Theme.SYSTEM)
    )

    fun changeTheme(theme: Theme) = viewModelScope.launch {
        settingsRepository.setTheme(theme)
    }
}