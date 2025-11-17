package de.geosphere.speechplaning.data.usecases.user

import de.geosphere.speechplaning.core.model.AppUser
import de.geosphere.speechplaning.data.authentication.UserRepository
import kotlinx.coroutines.flow.Flow

class ObserveCurrentUserUseCase(
    private val userRepository: UserRepository
) {
    // Der Operator invoke erlaubt den Aufruf wie eine Funktion: observeCurrentUserUseCase()
    operator fun invoke(): Flow<AppUser?> {
        return userRepository.currentUser
    }
}
