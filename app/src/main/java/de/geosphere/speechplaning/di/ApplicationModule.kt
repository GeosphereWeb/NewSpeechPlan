package de.geosphere.speechplaning.di

import androidx.lifecycle.LifecycleCoroutineScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import de.geosphere.speechplaning.data.EventMapper
import de.geosphere.speechplaning.data.SpiritualStatusMapper
import de.geosphere.speechplaning.data.repository.CongregationEventRepository
import de.geosphere.speechplaning.data.repository.CongregationRepository
import de.geosphere.speechplaning.data.repository.DistrictRepository
import de.geosphere.speechplaning.data.repository.SpeakerRepository
import de.geosphere.speechplaning.data.repository.SpeechRepository
import de.geosphere.speechplaning.data.repository.authentication.AuthRepository
import de.geosphere.speechplaning.data.repository.authentication.AuthRepositoryImpl
import de.geosphere.speechplaning.data.repository.authentication.UserRepository
import de.geosphere.speechplaning.data.repository.authentication.UserRepositoryImpl
import de.geosphere.speechplaning.data.services.FirestoreService
import de.geosphere.speechplaning.data.services.FirestoreServiceImpl
import de.geosphere.speechplaning.mockup.BuildDummyDBConnection
import de.geosphere.speechplaning.ui.login.AuthViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

@Suppress("ForbiddenComment")
val appModule =
    module {
        // Database
        single<FirebaseFirestore> { FirebaseFirestore.getInstance() }
        single<FirestoreService> { FirestoreServiceImpl(get()) }
        single<FirebaseAuth> { FirebaseAuth.getInstance() }

        // Coroutine Scope für Repositories
        single { CoroutineScope(Dispatchers.IO) } // oder SupervisorJob() + Dispatchers.Default

        // Repositories
        single { DistrictRepository(get()) }
        single { SpeechRepository(get()) }
        single { CongregationRepository(get()) }
        single { SpeakerRepository(get()) }
        single { CongregationEventRepository(get()) }

        single<UserRepository> { UserRepositoryImpl(get()) }
        single<AuthRepository> { AuthRepositoryImpl(get(), get(), get(), androidContext()) }

        single { SpiritualStatusMapper(androidContext()) }
        single { EventMapper(androidContext()) }

        factory { (scope: LifecycleCoroutineScope) -> BuildDummyDBConnection(scope) }

        // Use Cases Speech

        // viewModels
        viewModel { AuthViewModel(get()) }
    }
