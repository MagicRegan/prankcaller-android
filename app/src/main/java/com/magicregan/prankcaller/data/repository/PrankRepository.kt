package com.magicregan.prankcaller.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.magicregan.prankcaller.data.PrankDataSource
import com.magicregan.prankcaller.data.model.CallRecord
import com.magicregan.prankcaller.data.model.Prank
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrankRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("prank_caller_prefs", Context.MODE_PRIVATE)

    private val _callHistory = MutableStateFlow<List<CallRecord>>(emptyList())
    val callHistory: StateFlow<List<CallRecord>> = _callHistory.asStateFlow()

    private val _credits = MutableStateFlow(prefs.getInt(KEY_CREDITS, 0))
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
                val newVal = current - 1
                saveCredits(newVal)
                newVal
            } else {
                success = false
                current
            }
        }
        return success
    }

    fun refundCredit() {
        _credits.update {
            val newVal = it + 1
            saveCredits(newVal)
            newVal
        }
    }

    fun addCredits(amount: Int) {
        _credits.update {
            val newVal = it + amount
            saveCredits(newVal)
            newVal
        }
    }

    fun setCredits(amount: Int) {
        _credits.value = amount
        saveCredits(amount)
    }

    fun initNewUserCredits() {
        if (!prefs.getBoolean(KEY_INITIAL_CREDITS_GIVEN, false)) {
            addCredits(FREE_STARTING_CREDITS)
            prefs.edit().putBoolean(KEY_INITIAL_CREDITS_GIVEN, true).apply()
        }
    }

    private fun saveCredits(amount: Int) {
        prefs.edit().putInt(KEY_CREDITS, amount).apply()
    }

    companion object {
        private const val KEY_CREDITS = "credits"
        private const val KEY_INITIAL_CREDITS_GIVEN = "initial_credits_given"
        const val FREE_STARTING_CREDITS = 5
    }
}
