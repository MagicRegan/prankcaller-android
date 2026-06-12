package com.magicregan.prankcaller.ui.screens.home

import androidx.lifecycle.ViewModel
import com.magicregan.prankcaller.data.model.Prank
import com.magicregan.prankcaller.data.repository.PrankRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: PrankRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _pranks = MutableStateFlow<List<Prank>>(emptyList())
    val pranks: StateFlow<List<Prank>> = _pranks.asStateFlow()

    val credits = repository.credits

    init {
        _pranks.value = repository.getPopularPranks()
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        _pranks.value = if (query.isBlank()) {
            repository.getPopularPranks()
        } else {
            repository.searchPranks(query)
        }
    }

    fun loadPopular() {
        _searchQuery.value = ""
        _pranks.value = repository.getPopularPranks()
    }

    fun loadRecent() {
        _searchQuery.value = ""
        _pranks.value = repository.getRecentPranks()
    }
}
