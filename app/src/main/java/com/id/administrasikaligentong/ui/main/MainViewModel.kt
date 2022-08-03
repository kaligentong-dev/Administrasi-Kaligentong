package com.id.administrasikaligentong.ui.main

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel : ViewModel() {
    private val _shouldShowSplash = MutableStateFlow(true)
    val shouldShowSplash = _shouldShowSplash.asStateFlow()

    fun toggleShouldShowSplash(state: Boolean) = _shouldShowSplash.tryEmit(state)
}