package de.geosphere.speechplaning.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import de.geosphere.speechplaning.data.EventMapper
import de.geosphere.speechplaning.data.SpiritualStatusMapper
import de.geosphere.speechplaning.data.repository.AuthRepository
import de.geosphere.speechplaning.data.repository.AuthRepositoryImpl
import de.geosphere.speechplaning.data.repository.CongregationEventRepository
import de.geosphere.speechplaning.data.repository.CongregationRepository
import de.geosphere.speechplaning.data.repository.DistrictRepository
import de.geosphere.speechplaning.data.repository.SpeakerRepository
import de.geosphere.speechplaning.data.repository.SpeechRepository
import de.geosphere.speechplaning.data.repository.UserRepository
import de.geosphere.speechplaning.data.repository.UserRepositoryImpl
import de.geosphere.speechplaning.data.services.FirestoreService
import de.geosphere.speechplaning.data.services.FirestoreServiceImpl
import de.geosphere.speechplaning.ui.auth.AuthViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

@Suppress("ForbiddenComment")
val appModule =
    module {
        // Application-wide CoroutineScope - Bleibt klassisch, da es eine Factory ist
        single {
            // SupervisorJob sorgt dafür, dass ein Fehler in einer Child-Coroutine nicht den ganzen Scope beendet
            CoroutineScope(SupervisorJob() + Dispatchers.Default)
        }

        // Database - Bleiben klassisch, da es Factory-Aufrufe sind (getInstance)
        single<FirebaseFirestore> { FirebaseFirestore.getInstance() }
        single { FirebaseAuth.getInstance() }

        // Services - Umgestellt auf Constructor DSL
        singleOf(::FirestoreServiceImpl) { bind<FirestoreService>() }

        // Repositories
        singleOf(::AuthRepositoryImpl) { bind<AuthRepository>() }
        singleOf(::UserRepositoryImpl) { bind<UserRepository>() }
        singleOf(::DistrictRepository)
        singleOf(::SpeechRepository)
        singleOf(::CongregationRepository)
        singleOf(::SpeakerRepository)
        singleOf(::CongregationEventRepository)

        // Mappers - Bleiben klassisch, da sie eine spezielle Koin-Funktion (androidContext) verwenden
        single { SpiritualStatusMapper(androidContext()) }
        single { EventMapper(androidContext()) }

        // Use Cases Speech

        // viewModels
        viewModelOf(::AuthViewModel)
    }
