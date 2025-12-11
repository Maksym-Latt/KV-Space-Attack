package com.chicken.spaceattack.ui.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chicken.spaceattack.domain.UpgradeRepository
import com.chicken.spaceattack.domain.UpgradeState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class MenuViewModel @Inject constructor(
    private val upgradeRepository: UpgradeRepository
) : ViewModel() {

    val state: StateFlow<UpgradeState> = upgradeRepository.state
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), upgradeRepository.currentState())

    fun upgradeShield() {
        upgradeRepository.upgradeShield()
    }

    fun upgradeNuclear() {
        upgradeRepository.upgradeNuclear()
    }
}
