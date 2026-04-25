package de.geosphere.speechplaning.data.repository.base

import de.geosphere.speechplaning.core.model.SavableDataClass
import kotlinx.coroutines.flow.Flow

interface IFirestoreSubcollectionRepository<T : SavableDataClass, PID : Any> {
    suspend fun save(entity: T, vararg parentIds: PID): String
    suspend fun getById(id: String, vararg parentIds: PID): T?
    suspend fun delete(id: String, vararg parentIds: PID)
    fun getAllFlow(vararg parentIds: PID): Flow<List<T>>
}
