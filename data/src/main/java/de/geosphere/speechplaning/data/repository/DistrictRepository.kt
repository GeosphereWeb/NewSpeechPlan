package de.geosphere.speechplaning.data.repository

import de.geosphere.speechplaning.core.model.District
import de.geosphere.speechplaning.data.repository.services.ICollectionActions
import de.geosphere.speechplaning.data.repository.services.IFlowActions
import kotlinx.coroutines.flow.Flow

private const val DISTRICT_COLLECTION = "districts"

@Suppress("TooGenericExceptionCaught", "TooGenericExceptionThrown")
class DistrictRepository(
    private val collectionActions: ICollectionActions,
    private val flowActions: IFlowActions
) {

    suspend fun getActiveDistricts(): List<District> {
        return try {
            collectionActions.getDocuments(DISTRICT_COLLECTION, District::class.java)
                .filter { it.active }
        } catch (e: Exception) {
            throw RuntimeException("Failed to get active districts from $DISTRICT_COLLECTION", e)
        }
    }

    suspend fun deleteDistrict(id: String) {
        collectionActions.deleteDocument(DISTRICT_COLLECTION, id)
    }

    suspend fun getAllDistrict(): List<District> {
        return try {
            collectionActions.getDocuments(DISTRICT_COLLECTION, District::class.java)
        } catch (e: Exception) {
            throw RuntimeException("Failed to get all districts from $DISTRICT_COLLECTION", e)
        }
    }

    suspend fun saveDistrict(district: District): String {
        return collectionActions.setDocument(DISTRICT_COLLECTION, district.id, district).let { district.id }
    }

    fun getAllDistrictFlow(): Flow<List<District>> {
        return flowActions.getCollectionFlow(DISTRICT_COLLECTION, District::class.java)
    }
}
