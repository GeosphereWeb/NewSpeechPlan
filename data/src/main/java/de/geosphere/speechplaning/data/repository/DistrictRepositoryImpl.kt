package de.geosphere.speechplaning.data.repository

import de.geosphere.speechplaning.core.model.District
import de.geosphere.speechplaning.data.repository.services.IFirestoreService
import kotlinx.coroutines.flow.Flow

private const val DISTRICT_COLLECTION = "districts"

@Suppress("TooGenericExceptionCaught", "TooGenericExceptionThrown")
class DistrictRepositoryImpl(
    private val firestoreService: IFirestoreService
) {

    suspend fun getActiveDistricts(): List<District> {
        return try {
            // This assumes a method like getDocumentsWithQuery in your service.
            // For now, let's get all and filter.
            firestoreService.getDocuments(DISTRICT_COLLECTION, District::class.java)
                .filter { it.active }
        } catch (e: Exception) {
            throw RuntimeException("Failed to get active districts from $DISTRICT_COLLECTION", e)
        }
    }

    suspend fun deleteDistrict(id: String) {
        firestoreService.deleteDocument(DISTRICT_COLLECTION, id)
    }

    suspend fun getAllDistrict(): List<District> {
        return try {
            firestoreService.getDocuments(DISTRICT_COLLECTION, District::class.java)
        } catch (e: Exception) {
            throw RuntimeException("Failed to get all districts from $DISTRICT_COLLECTION", e)
        }
    }

    suspend fun saveDistrict(district: District): String {
        return firestoreService.setDocument(DISTRICT_COLLECTION, district.id, district).let { district.id }
    }

    fun getAllDistrictFlow(): Flow<List<District>> {
        return firestoreService.getCollectionFlow(DISTRICT_COLLECTION, District::class.java)
    }
}
