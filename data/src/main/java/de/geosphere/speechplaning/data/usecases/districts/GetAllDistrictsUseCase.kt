package de.geosphere.speechplaning.data.usecases.districts

import de.geosphere.speechplaning.core.model.District
import de.geosphere.speechplaning.data.repository.DistrictRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class GetAllDistrictsUseCase(
    private val districtRepository: DistrictRepository
) {
    /**
     * @return Ein Flow, der eine Liste von Versammlungen emittiert, verpackt in ein Result.
     */
    operator fun invoke(): Flow<Result<List<District>>> {
        return districtRepository.getAllDistrictFlow()
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
