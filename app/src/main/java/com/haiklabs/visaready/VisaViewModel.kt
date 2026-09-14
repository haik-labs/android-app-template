package com.haiklabs.visaready

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class VisaScreen { WELCOME, DESTINATION, TRIP, PERSONAL, FINANCIAL, HISTORY, DOCUMENTS, REVIEW, ANALYSIS, PAYWALL, RESULT }

data class VisaUiState(val screen: VisaScreen = VisaScreen.WELCOME, val profile: VisaProfile = VisaProfile(), val purchased: Boolean = false)

class VisaViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(VisaUiState())
    val uiState = _uiState.asStateFlow()
    fun go(screen: VisaScreen) = _uiState.update { it.copy(screen = screen) }
    fun update(block: (VisaProfile) -> VisaProfile) = _uiState.update { it.copy(profile = block(it.profile)) }
    fun purchaseComplete() = _uiState.update { it.copy(purchased = true, screen = VisaScreen.RESULT) }
    fun next() = go(when (_uiState.value.screen) {
        VisaScreen.WELCOME -> VisaScreen.DESTINATION; VisaScreen.DESTINATION -> VisaScreen.TRIP
        VisaScreen.TRIP -> VisaScreen.PERSONAL; VisaScreen.PERSONAL -> VisaScreen.FINANCIAL
        VisaScreen.FINANCIAL -> VisaScreen.HISTORY; VisaScreen.HISTORY -> VisaScreen.DOCUMENTS
        VisaScreen.DOCUMENTS -> VisaScreen.REVIEW; VisaScreen.REVIEW -> VisaScreen.ANALYSIS
        VisaScreen.ANALYSIS -> VisaScreen.PAYWALL; VisaScreen.PAYWALL -> VisaScreen.RESULT
        VisaScreen.RESULT -> VisaScreen.RESULT
    })
    fun back() = go(when (_uiState.value.screen) {
        VisaScreen.DESTINATION -> VisaScreen.WELCOME; VisaScreen.TRIP -> VisaScreen.DESTINATION
        VisaScreen.PERSONAL -> VisaScreen.TRIP; VisaScreen.FINANCIAL -> VisaScreen.PERSONAL
        VisaScreen.HISTORY -> VisaScreen.FINANCIAL; VisaScreen.DOCUMENTS -> VisaScreen.HISTORY
        VisaScreen.REVIEW -> VisaScreen.DOCUMENTS; VisaScreen.ANALYSIS -> VisaScreen.REVIEW
        VisaScreen.PAYWALL -> VisaScreen.REVIEW; VisaScreen.RESULT -> VisaScreen.PAYWALL
        else -> VisaScreen.WELCOME
    })
}
