package de.geosphere.speechplaning.feature.login.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.geosphere.speechplaning.core.model.AppUser
import de.geosphere.speechplaning.data.usecases.user.ObserveCurrentUserUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel für die User-Verwaltung.
 * Beobachtet den aktuell angemeldeten AppUser.
 */
class UserViewModel(
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase
) : ViewModel() {

    /**
     * Beobachtet den aktuellen User als StateFlow.
     * Gibt null zurück, wenn kein User angemeldet ist.
     * SharingStarted.WhileSubscribed(5000) sorgt dafür, dass der Flow aktiv bleibt,
     * auch wenn die UI kurzzeitig nicht beobachtet (z.B. bei Rotationen).
     */
    val currentUser: StateFlow<AppUser?> = observeCurrentUserUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
}
