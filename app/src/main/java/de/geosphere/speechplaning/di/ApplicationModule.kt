package de.geosphere.speechplaning.di

import de.geosphere.speechplaning.data.repository.authentication.AuthRepositoryImpl
import de.geosphere.speechplaning.domain.usecase.auth.CreateUserWithEmailAndPasswordUseCase
import SignInWithEmailAndPasswordUseCase
import SignOutUseCase
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
import de.geosphere.speechplaning.data.repository.authentication.UserRepository
import de.geosphere.speechplaning.data.repository.authentication.UserRepositoryImpl
import de.geosphere.speechplaning.data.services.FirestoreService
import de.geosphere.speechplaning.data.services.FirestoreServiceImpl
import de.geosphere.speechplaning.domain.usecase.auth.DetermineAppUserStatusUseCase
import de.geosphere.speechplaning.domain.usecase.auth.GoogleSignInUseCase
import de.geosphere.speechplaning.domain.util.GoogleIdTokenParser
import de.geosphere.speechplaning.mockup.BuildDummyDBConnection
import de.geosphere.speechplaning.ui.login.AuthViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

@Suppress("ForbiddenComment")
val appModule =
    module {
        // Database
        single<FirebaseFirestore> { FirebaseFirestore.getInstance() }
        single<FirebaseAuth> { FirebaseAuth.getInstance() }
        singleOf(::FirestoreServiceImpl) { bind<FirestoreService>() }

        // Coroutine Scope für Repositories
        single { CoroutineScope(Dispatchers.IO) } // oder SupervisorJob() + Dispatchers.Default

        // Repositories
        singleOf(::CongregationRepository)
        singleOf(::CongregationEventRepository)
        singleOf(::DistrictRepository)
        singleOf(::SpeakerRepository)
        singleOf(::SpeechRepository)

        singleOf(::SpiritualStatusMapper)
        singleOf(::EventMapper)

        factory { (scope: LifecycleCoroutineScope) -> BuildDummyDBConnection(scope) }

        // Use Cases
        factoryOf(::CreateUserWithEmailAndPasswordUseCase)
        factoryOf(::DetermineAppUserStatusUseCase) // <== Diese Definition ist jetzt korrekt
        factoryOf(::GoogleSignInUseCase)
        factoryOf(::GoogleIdTokenParser) // Füge diese Zeile hinzu
        factoryOf(::SignInWithEmailAndPasswordUseCase)
        factoryOf(::SignOutUseCase)

        singleOf(::UserRepositoryImpl) { bind<UserRepository>() }
        singleOf(::AuthRepositoryImpl) { bind<AuthRepository>() }

        // viewModels
        viewModelOf(::AuthViewModel)
    }
