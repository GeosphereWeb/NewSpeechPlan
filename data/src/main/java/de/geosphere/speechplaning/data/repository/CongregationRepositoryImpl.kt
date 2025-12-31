package de.geosphere.speechplaning.data.repository

import de.geosphere.speechplaning.core.model.Congregation
import de.geosphere.speechplaning.data.repository.base.FirestoreSubcollectionRepository
import de.geosphere.speechplaning.data.repository.services.IFlowActions
import de.geosphere.speechplaning.data.repository.services.ISubcollectionActions
import kotlinx.coroutines.flow.Flow

private const val CONGREGATIONS_SUBCOLLECTION = "congregations"
private const val DISTRICTS_COLLECTION = "districts"

@Suppress("TooGenericExceptionCaught", "TooGenericExceptionThrown")
class CongregationRepositoryImpl(
    subcollectionActions: ISubcollectionActions,
    private val flowActions: IFlowActions
) : FirestoreSubcollectionRepository<Congregation, String, String>(
    subcollectionActions = subcollectionActions,
    flowActions = flowActions,
    subcollectionName = CONGREGATIONS_SUBCOLLECTION,
    clazz = Congregation::class.java
) {

    override fun extractIdFromEntity(entity: Congregation): String {
        return entity.id
    }

    override fun buildParentCollectionPath(vararg parentIds: String): String {
        require(parentIds.size == 1) { "Expected districtId as the sole parentId" }
        return DISTRICTS_COLLECTION
    }

    override fun getParentDocumentId(vararg parentIds: String): String {
        require(parentIds.size == 1) { "Expected districtId as the sole parentId" }
        return parentIds[0]
    }

    suspend fun saveCongregation(districtId: String, congregation: Congregation): String {
        return super.save(congregation, districtId)
    }

    fun getCongregationsForDistrict(districtId: String): Flow<List<Congregation>> {
        return super.getAllFlow(districtId)
    }

    suspend fun deleteCongregation(districtId: String, congregationId: String) {
        super.delete(congregationId, districtId)
    }

    fun getCongregationsForDistrictFlow(districtId: String): Flow<List<Congregation>> {
        return getAllFlow(districtId)
    }

    fun getAllCongregationsGlobalFlow(): Flow<List<Congregation>> {
        return flowActions.getCollectionGroupFlow(CONGREGATIONS_SUBCOLLECTION, Congregation::class.java)
    }
}
