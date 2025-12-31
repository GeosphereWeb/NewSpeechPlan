package de.geosphere.speechplaning.data.repository.base

import de.geosphere.speechplaning.core.model.SavableDataClass
import kotlinx.coroutines.flow.Flow

interface IFirestoreSubcollectionRepository<T : SavableDataClass, ID : Any, PID : Any> {
    suspend fun save(entity: T, vararg parentIds: PID): ID
    suspend fun getById(id: ID, vararg parentIds: PID): T?
    suspend fun delete(id: ID, vararg parentIds: PID)
    fun getAllFlow(vararg parentIds: PID): Flow<List<T>>
}
