package de.geosphere.speechplaning.data.usecases.congregation

import de.geosphere.speechplaning.core.model.Congregation
import de.geosphere.speechplaning.data.repository.CongregationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class GetAllCongregationsUseCase(
    private val congregationRepository: CongregationRepository
) {
    /**
     * @return Ein Flow, der eine Liste von Versammlungen emittiert, verpackt in ein Result.
     */
    operator fun invoke(): Flow<Result<List<Congregation>>> {
        return congregationRepository.getAllCongregationsGlobalFlow()
            .map { list ->
                // Erfolgreiche Daten in Result.success verpacken
                Result.success(list)
            }
            .catch { exception ->
                // Fehler abfangen und als Result.failure emittieren
                emit(Result.failure(exception))
            }
    }
}
