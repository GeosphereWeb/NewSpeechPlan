package de.geosphere.speechplaning.data.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import de.geosphere.speechplaning.data.authentication.AuthRepository
import de.geosphere.speechplaning.data.authentication.AuthRepositoryImpl
import de.geosphere.speechplaning.data.authentication.UserRepository
import de.geosphere.speechplaning.data.authentication.UserRepositoryImpl
import de.geosphere.speechplaning.data.authentication.permission.CongregationEventPermissionPolicy
import de.geosphere.speechplaning.data.authentication.permission.CongregationPermissionPolicy
import de.geosphere.speechplaning.data.authentication.permission.DistrictPermissionPolicy
import de.geosphere.speechplaning.data.authentication.permission.SpeakerPermissionPolicy
import de.geosphere.speechplaning.data.authentication.permission.SpeechPermissionPolicy
import de.geosphere.speechplaning.data.repository.CongregationEventRepository
import de.geosphere.speechplaning.data.repository.CongregationRepository
import de.geosphere.speechplaning.data.repository.DistrictRepository
import de.geosphere.speechplaning.data.repository.SpeakerRepository
import de.geosphere.speechplaning.data.repository.SpeechRepository
import de.geosphere.speechplaning.data.repository.services.FirestoreServiceImpl
import de.geosphere.speechplaning.data.repository.services.ICollectionActions
import de.geosphere.speechplaning.data.repository.services.IFlowActions
import de.geosphere.speechplaning.data.repository.services.ISubcollectionActions
import de.geosphere.speechplaning.data.usecases.congregation.DeleteCongregationUseCase
import de.geosphere.speechplaning.data.usecases.congregation.GetAllCongregationsUseCase
import de.geosphere.speechplaning.data.usecases.congregation.GetCongregationUseCase
import de.geosphere.speechplaning.data.usecases.congregation.ObserveAllCongregationsUseCase
import de.geosphere.speechplaning.data.usecases.congregation.SaveCongregationUseCase
import de.geosphere.speechplaning.data.usecases.congregationEvent.DeleteCongregationEventUseCase
import de.geosphere.speechplaning.data.usecases.congregationEvent.GetAllCongregationEventUseCase
import de.geosphere.speechplaning.data.usecases.congregationEvent.ObserveAllEventsForCongregationUseCase
import de.geosphere.speechplaning.data.usecases.congregationEvent.SaveCongregationEventUseCase
import de.geosphere.speechplaning.data.usecases.districts.DeleteDistrictUseCase
import de.geosphere.speechplaning.data.usecases.districts.GetAllDistrictsUseCase
import de.geosphere.speechplaning.data.usecases.districts.GetDistrictUseCase
import de.geosphere.speechplaning.data.usecases.districts.SaveDistrictUseCase
import de.geosphere.speechplaning.data.usecases.login.CreateUserWithEmailAndPasswordUseCase
import de.geosphere.speechplaning.data.usecases.login.DetermineAppUserStatusUseCase
import de.geosphere.speechplaning.data.usecases.login.GoogleSignInUseCase
import de.geosphere.speechplaning.data.usecases.login.SignInWithEmailAndPasswordUseCase
import de.geosphere.speechplaning.data.usecases.login.SignOutUseCase
import de.geosphere.speechplaning.data.usecases.speaker.DeleteSpeakerUseCase
import de.geosphere.speechplaning.data.usecases.speaker.GetSpeakersUseCase
import de.geosphere.speechplaning.data.usecases.speaker.SaveSpeakerUseCase
import de.geosphere.speechplaning.data.usecases.speeches.DeleteSpeechUseCase
import de.geosphere.speechplaning.data.usecases.speeches.GetSpeechesUseCase
import de.geosphere.speechplaning.data.usecases.speeches.SaveSpeechUseCase
import de.geosphere.speechplaning.data.usecases.user.ObserveCurrentUserUseCase
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
    singleOf(::FirestoreServiceImpl) {
        bind<ICollectionActions>()
        bind<ISubcollectionActions>()
        bind<IFlowActions>()
    }

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

    // Permissions
    singleOf(::SpeechPermissionPolicy)
    singleOf(::DistrictPermissionPolicy)
    singleOf(::CongregationPermissionPolicy)
    singleOf(::CongregationEventPermissionPolicy)
    singleOf(::SpeakerPermissionPolicy)

    // UseCases
    factoryOf(::SignInWithEmailAndPasswordUseCase)

    factoryOf(::CreateUserWithEmailAndPasswordUseCase)
    factoryOf(::DetermineAppUserStatusUseCase)
    factoryOf(::GoogleSignInUseCase)
    factoryOf(::GoogleIdTokenParser)

    factoryOf(::GetSpeechesUseCase)
    factoryOf(::DeleteSpeechUseCase)
    factoryOf(::SaveSpeechUseCase)

    // Speaker UseCases
    factoryOf(::GetSpeakersUseCase)
    factoryOf(::DeleteSpeakerUseCase)
    factoryOf(::SaveSpeakerUseCase)

    factoryOf(::GetDistrictUseCase)
    factoryOf(::DeleteDistrictUseCase)
    factoryOf(::SaveDistrictUseCase)
    factoryOf(::GetAllDistrictsUseCase)

    factoryOf(::GetCongregationUseCase)
    factoryOf(::DeleteCongregationUseCase)
    factoryOf(::SaveCongregationUseCase)
    factoryOf(::GetAllCongregationsUseCase)
    factoryOf(::ObserveAllCongregationsUseCase)

    factoryOf(::DeleteCongregationEventUseCase)
    factoryOf(::GetAllCongregationEventUseCase)
    factoryOf(::ObserveAllEventsForCongregationUseCase)
    factoryOf(::SaveCongregationEventUseCase)

    factoryOf(::SignOutUseCase)

    factoryOf(::ObserveCurrentUserUseCase)
}
