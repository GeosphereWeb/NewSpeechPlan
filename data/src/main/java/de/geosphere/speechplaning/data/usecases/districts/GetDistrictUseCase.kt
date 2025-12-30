package de.geosphere.speechplaning.data.usecases.districts

import de.geosphere.speechplaning.core.model.District
import de.geosphere.speechplaning.data.repository.DistrictRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class GetDistrictUseCase(private val repository: DistrictRepository) {
    // WICHTIG: Kein 'suspend' mehr, da ein Flow direkt zurückgegeben wird
    operator fun invoke(): Flow<Result<List<District>>> {
        return repository.getAllDistrictFlow()
            .map { list -> // Sortierung und Success-Wrapper
                // Sortieren nach Namen statt ID, da IDs oft UUIDs (Strings) sind und toInt() fehlschlägt
                Result.success(list.sortedBy { it.id })
            }
            .catch { e ->
                // Fehler abfangen und als Result.failure senden
                emit(Result.failure(e))
            }
    }
}
