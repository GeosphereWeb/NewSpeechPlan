package de.geosphere.speechplaning.mockup

import android.util.Log
import androidx.lifecycle.LifecycleCoroutineScope
import de.geosphere.speechplaning.data.repository.DistrictRepository
import de.geosphere.speechplaning.data.repository.SpeechRepository
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Build dummy d b connection.
 *
 * @property districtRepository
 * @property speechRepository
 * @property lifecycleScope
 * @constructor Create empty Build dummy d b connection
 */
class BuildDummyDBConnection(
    val lifecycleScope: LifecycleCoroutineScope
): KoinComponent {
    // Abhängigkeiten direkt hier injizieren
    private val districtRepository: DistrictRepository by inject()
    private val speechRepository: SpeechRepository by inject()

    operator fun invoke() {
        val test = MockedListOfDummyClasses.districtMockupList.toList()
        test.forEach {
            Log.i("Werner", "onCreate: $it")
            lifecycleScope.launch {
                districtRepository.save(it)
            }
        }

        val test2 = MockedListOfDummyClasses.speechesMockupList.toList()
        test2.forEach {
            Log.i("Werner", "onCreate: $it")
            lifecycleScope.launch {
                speechRepository.save(it)
            }
        }
    }
}
