package de.geosphere.speechplaning.di

import androidx.lifecycle.LifecycleCoroutineScope
import de.geosphere.speechplaning.mocking.BuildDummyDBConnection
import de.geosphere.speechplaning.resterampe.EventMapper
import de.geosphere.speechplaning.resterampe.SpiritualStatusMapper
import de.geosphere.speechplaning.ui.login.AuthViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

@Suppress("ForbiddenComment")
val appModule =
    module {
        // // Database
        // single<FirebaseFirestore> { FirebaseFirestore.getInstance() }
        // single<FirebaseAuth> { FirebaseAuth.getInstance() }
        // singleOf(::FirestoreServiceImpl) { bind<FirestoreService>() }
        //
        // // Coroutine Scope für Repositories
        // single { CoroutineScope(Dispatchers.IO) } // oder SupervisorJob() + Dispatchers.Default
        //
        // // Repositories
        // singleOf(::CongregationRepository)
        // singleOf(::CongregationEventRepository)
        // singleOf(::DistrictRepository)
        // singleOf(::SpeakerRepository)
        // singleOf(::SpeechRepository)

        singleOf(::SpiritualStatusMapper)
        singleOf(::EventMapper)

        factory { (scope: LifecycleCoroutineScope) -> BuildDummyDBConnection(scope) }

        // Use Cases
        // factoryOf(::CreateUserWithEmailAndPasswordUseCase)
        // factoryOf(::DetermineAppUserStatusUseCase) // <== Diese Definition ist jetzt korrekt
        // factoryOf(::GoogleSignInUseCase)
        // factoryOf(::GoogleIdTokenParser) // Füge diese Zeile hinzu
        // // factoryOf(::SignInWithEmailAndPasswordUseCase)
        // factoryOf(::SignOutUseCase)

        // singleOf(::UserRepositoryImpl) { bind<UserRepository>() }
        // singleOf(::AuthRepositoryImpl) { bind<AuthRepository>() }

        // viewModels
        viewModelOf(::AuthViewModel)
    }
