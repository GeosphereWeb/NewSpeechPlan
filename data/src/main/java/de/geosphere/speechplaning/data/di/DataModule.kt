package de.geosphere.speechplaning.data.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import de.geosphere.speechplaning.data.authentication.AuthRepository
import de.geosphere.speechplaning.data.authentication.AuthRepositoryImpl
import de.geosphere.speechplaning.data.authentication.UserRepository
import de.geosphere.speechplaning.data.authentication.UserRepositoryImpl
import de.geosphere.speechplaning.data.repository.CongregationEventRepository
import de.geosphere.speechplaning.data.repository.CongregationRepository
import de.geosphere.speechplaning.data.repository.DistrictRepository
import de.geosphere.speechplaning.data.repository.SpeakerRepository
import de.geosphere.speechplaning.data.repository.SpeechRepository
import de.geosphere.speechplaning.data.repository.services.FirestoreService
import de.geosphere.speechplaning.data.repository.services.FirestoreServiceImpl
import de.geosphere.speechplaning.data.usecases.login.CreateUserWithEmailAndPasswordUseCase
import de.geosphere.speechplaning.data.usecases.login.DetermineAppUserStatusUseCase
import de.geosphere.speechplaning.data.usecases.login.GoogleSignInUseCase
import de.geosphere.speechplaning.data.usecases.login.SignInWithEmailAndPasswordUseCase
import de.geosphere.speechplaning.data.usecases.login.SignOutUseCase
import de.geosphere.speechplaning.data.util.GoogleIdTokenParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val dataModule = module {
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

    singleOf(::UserRepositoryImpl) { bind<UserRepository>() }
    singleOf(::AuthRepositoryImpl) { bind<AuthRepository>() }

    factoryOf(::SignInWithEmailAndPasswordUseCase)

    factoryOf(::CreateUserWithEmailAndPasswordUseCase)
    factoryOf(::DetermineAppUserStatusUseCase) // <== Diese Definition ist jetzt korrekt
    factoryOf(::GoogleSignInUseCase)
    factoryOf(::GoogleIdTokenParser) // Füge diese Zeile hinzu
    // factoryOf(::SignInWithEmailAndPasswordUseCase)
    factoryOf(::SignOutUseCase)
}
