package com.magicregan.prankcaller.data.repository

import com.magicregan.prankcaller.data.PrankDataSource
import com.magicregan.prankcaller.data.model.CallRecord
import com.magicregan.prankcaller.data.model.Prank
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrankRepository @Inject constructor() {

    private val _callHistory = MutableStateFlow<List<CallRecord>>(emptyList())
    val callHistory: StateFlow<List<CallRecord>> = _callHistory.asStateFlow()

    private val _credits = MutableStateFlow(25)
    val credits: StateFlow<Int> = _credits.asStateFlow()

    fun getAllPranks(): List<Prank> = PrankDataSource.getAllPranks()

    fun getPopularPranks(): List<Prank> = PrankDataSource.getPopularPranks()

    fun getRecentPranks(): List<Prank> = PrankDataSource.getRecentPranks()

    fun searchPranks(query: String): List<Prank> = PrankDataSource.searchPranks(query)

    fun getPrankById(id: Int): Prank? = PrankDataSource.getPrankById(id)

    fun addCallRecord(record: CallRecord) {
        _callHistory.value = listOf(record) + _callHistory.value
    }

    fun useCredit(): Boolean {
        var success = false
        _credits.update { current ->
            if (current > 0) {
                success = true
                current - 1
            } else {
                success = false
                current
            }
        }
        return success
    }

    fun refundCredit() {
        _credits.update { it + 1 }
    }

    fun addCredits(amount: Int) {
        _credits.update { it + amount }
    }
}
