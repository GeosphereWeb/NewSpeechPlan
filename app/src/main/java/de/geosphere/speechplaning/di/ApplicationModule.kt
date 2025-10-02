package de.geosphere.speechplaning.di

import androidx.lifecycle.LifecycleCoroutineScope
import com.google.firebase.firestore.FirebaseFirestore
import de.geosphere.speechplaning.data.EventMapper
import de.geosphere.speechplaning.data.SpiritualStatusMapper
import de.geosphere.speechplaning.data.repository.CongregationEventRepository
import de.geosphere.speechplaning.data.repository.CongregationRepository
import de.geosphere.speechplaning.data.repository.DistrictRepository
import de.geosphere.speechplaning.data.repository.SpeakerRepository
import de.geosphere.speechplaning.data.repository.SpeechRepository
import de.geosphere.speechplaning.data.services.FirestoreService
import de.geosphere.speechplaning.data.services.FirestoreServiceImpl
import de.geosphere.speechplaning.mockup.BuildDummyDBConnection
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

@Suppress("ForbiddenComment")
val appModule =
    module {
        // Database
        single<FirebaseFirestore> { FirebaseFirestore.getInstance() }
        single<FirestoreService> { FirestoreServiceImpl(get()) }

        // Repositories
        single { DistrictRepository(get()) }
        single { SpeechRepository(get()) }
        single { CongregationRepository(get()) }
        single { SpeakerRepository(get()) }
        single { CongregationEventRepository(get()) }

        single { SpiritualStatusMapper(androidContext()) }
        single { EventMapper(androidContext()) }

        factory { (scope: LifecycleCoroutineScope) -> BuildDummyDBConnection(scope) }

        // Use Cases Speech

        // viewModels

    }
