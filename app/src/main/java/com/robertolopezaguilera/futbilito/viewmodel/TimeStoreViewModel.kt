package com.robertolopezaguilera.futbilito.viewmodel

// TimeStoreViewModel.kt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TimeStoreViewModel : ViewModel() {

    private val _timeStoreState = MutableStateFlow(TimeStoreState())
    val timeStoreState: StateFlow<TimeStoreState> = _timeStoreState

    var timeExtensionsUsed = 0

    fun showTimeStoreDialog(userCoins: Int) {
        _timeStoreState.value = TimeStoreState(
            isVisible = true,
            userCoins = userCoins,
            timeExtensionsUsed = timeExtensionsUsed
        )
    }

    fun hideTimeStoreDialog() {
        _timeStoreState.value = _timeStoreState.value.copy(isVisible = false)
    }

    fun purchaseTimeWithCoins(userCoins: Int): Boolean {
        val timeToAdd = if (timeExtensionsUsed == 0) 30 else 15

        return if (userCoins >= 30) {
            timeExtensionsUsed++
            _timeStoreState.value = _timeStoreState.value.copy(
                purchaseSuccess = true,
                timeAdded = timeToAdd,
                timeExtensionsUsed = timeExtensionsUsed
            )
            true
        } else {
            false
        }
    }

    fun watchAdForTime() {
        val timeToAdd = if (timeExtensionsUsed == 0) 30 else 15
        _timeStoreState.value = _timeStoreState.value.copy(
            watchAdSelected = true,
            timeAdded = timeToAdd
        )
    }

    fun resetPurchaseState() {
        _timeStoreState.value = _timeStoreState.value.copy(
            purchaseSuccess = false,
            timeAdded = 0,
            watchAdSelected = false
        )
    }
}

data class TimeStoreState(
    val isVisible: Boolean = false,
    val userCoins: Int = 0,
    val timeExtensionsUsed: Int = 0,
    val purchaseSuccess: Boolean = false,
    val timeAdded: Int = 0,
    val watchAdSelected: Boolean = false
)