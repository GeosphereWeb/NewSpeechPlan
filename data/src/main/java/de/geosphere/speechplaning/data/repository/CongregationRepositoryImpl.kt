package de.geosphere.speechplaning.data.repository

import de.geosphere.speechplaning.core.model.Congregation
import de.geosphere.speechplaning.data.repository.base.FirestoreSubcollectionRepositoryImpl
import de.geosphere.speechplaning.data.repository.services.IFirestoreService
import kotlinx.coroutines.flow.Flow

private const val CONGREGATIONS_SUBCOLLECTION = "congregations"
private const val DISTRICTS_COLLECTION = "districts"

@Suppress("TooGenericExceptionCaught", "TooGenericExceptionThrown")
class CongregationRepositoryImpl(
    firestoreService: IFirestoreService
) : FirestoreSubcollectionRepositoryImpl<Congregation>(
    firestoreService = firestoreService,
    subcollectionName = CONGREGATIONS_SUBCOLLECTION,
    clazz = Congregation::class.java
) {

    override fun extractIdFromEntity(entity: Congregation): String {
        return entity.id
    }

    /**
     * Erwartet districtId als parentIds[0].
     * Der Pfad zur Collection, die das Elterndokument (District) enthält, ist die Root-Collection "districts".
     */
    override fun buildParentCollectionPath(vararg parentIds: String): String {
        require(parentIds.size == 1) { "Expected districtId as the sole parentId" }
        // Die "congregations" Subcollection hängt direkt unter einem Dokument in der "districts" Collection.
        // Daher ist der Pfad zur *Collection des Parent-Dokuments* einfach "districts".
        return DISTRICTS_COLLECTION
    }

    /**
     * Erwartet districtId als parentIds[0].
     * Gibt die districtId zurück, da dies das direkte Elterndokument der Congregations-Subcollection ist.
     */
    override fun getParentDocumentId(vararg parentIds: String): String {
        require(parentIds.size == 1) { "Expected districtId as the sole parentId" }
        return parentIds[0] // districtId
    }

    /**
     * Speichert eine Versammlung in der Subcollection eines bestimmten Districts.
     *
     * @param districtId Die ID des Districts, zu dem die Versammlung gehört.
     * @param congregation Das zu speichernde Congregation-Objekt.
     * @return Die ID des gespeicherten Dokuments.
     */
    suspend fun saveCongregation(districtId: String, congregation: Congregation): String {
        // Ruft die save-Methode der Basisklasse auf und übergibt die districtId als parentId.
        return super.save(congregation, districtId)
    }

    /**
     * Ruft alle Versammlungen für einen bestimmten District ab.
     *
     * @param districtId Die ID des Districts, dessen Versammlungen abgerufen werden sollen.
     * @return Eine Liste von Congregation-Objekten.
     */
    suspend fun getCongregationsForDistrict(districtId: String): List<Congregation> {
        // Ruft die getAll-Methode der Basisklasse auf und übergibt die districtId als parentId.
        return super.getAll(districtId)
    }

    /**
     * Löscht eine bestimmte Versammlung aus einem District.
     *
     * @param districtId Die ID des Districts.
     * @param congregationId Die ID der zu löschenden Versammlung.
     */
    suspend fun deleteCongregation(districtId: String, congregationId: String) {
        // Ruft die delete-Methode der Basisklasse auf und übergibt die districtId als parentId.
        super.delete(congregationId, districtId)
    }

    /**
     * Ruft alle Versammlungen eines Bezirks als Echtzeit-Flow ab.
     *
     * @param districtId Die ID des übergeordneten Bezirks.
     * @return Ein Flow mit der Liste der Versammlungen.
     */
    fun getCongregationsForDistrictFlow(districtId: String): Flow<List<Congregation>> {
        return getAllFlow(districtId)
    }

    /**
     * Ruft ALLE Versammlungen aus der gesamten Datenbank ab,
     * unabhängig davon, in welchem District sie liegen.
     */
    fun getAllCongregationsGlobalFlow(): Flow<List<Congregation>> {
        // "congregations" ist der Name der Subcollection, definiert in der Konstante oben
        return firestoreService.getCollectionGroupFlow("congregations", Congregation::class.java)
    }
}
