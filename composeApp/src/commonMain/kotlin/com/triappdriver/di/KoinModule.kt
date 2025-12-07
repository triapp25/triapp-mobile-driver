package com.triappdriver.di

import androidx.room.RoomDatabase
import androidx.sqlite.SQLiteDriver
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.Settings
import com.triappdriver.data.ApiService
import com.triappdriver.data.NetworkProvider
import com.triappdriver.data.repository.DataRepository
import com.triappdriver.data.repository.DataRepositoryImpl
import com.triappdriver.data.repository.MapboxSearchRepository
import com.triappdriver.data.repository.MapboxSearchRepositoryImpl
import com.triappdriver.data.repository.RatingRepository
import com.triappdriver.data.repository.RatingRepositoryImpl
import com.triappdriver.data.repository.RideRepository
import com.triappdriver.data.repository.RideRepositoryImpl
import com.triappdriver.domain.model.RatingArgs
import com.triappdriver.domain.usecase.CardOptionsUseCase
import com.triappdriver.domain.usecase.CreateLocalRatingUseCase
import com.triappdriver.domain.usecase.GetDataUseCase
import com.triappdriver.domain.usecase.GetRatingLastUseCase
import com.triappdriver.domain.usecase.GetRideHistoryUseCase
import com.triappdriver.domain.usecase.GetSignUpDraftUseCase
import com.triappdriver.domain.usecase.SaveRideUseCase
import com.triappdriver.domain.usecase.SaveSignUpDraftUseCase
import com.triappdriver.domain.usecase.SendRatingUseCase
import com.triappdriver.domain.usecase.TaxiUseCase
import com.triappdriver.local.AppDatabase
import com.triappdriver.local.AppPreferences
import com.triappdriver.local.getRoomDatabase
import com.triappdriver.local.provideObservableSettings
import com.triappdriver.presentation.feature.home.HomeViewModel
import com.triappdriver.presentation.feature.login.LoginViewModel
import com.triappdriver.presentation.feature.profile.ProfileViewModel
import com.triappdriver.presentation.feature.rating.RatingViewModel
import com.triappdriver.presentation.feature.signup.SignupViewModel
import com.triappdriver.utils.FirebaseAuthManager
import com.triappdriver.utils.FirebaseServiceImpl
import com.triappdriver.utils.LocationProvider
import com.triappdriver.utils.LocationRepository
import de.jensklingenberg.ktorfit.Ktorfit
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

expect fun getPlatformHttpClientEngineFactory(): HttpClientEngineFactory<*>

val networkModule = module {

    single<HttpClientEngineFactory<*>> { getPlatformHttpClientEngineFactory() }

//    single { get<TenantService>().getCurrentTenantConfig() }

    single {
        Json {
            ignoreUnknownKeys = true
            prettyPrint = true
            isLenient = true
        }
    }

    single {
        //val config: TenantConfig = get<TenantConfig>()
        HttpClient(engineFactory = get<HttpClientEngineFactory<*>>()) {
            install(ContentNegotiation) {
                json(get())
            }
            // Configuração Multi-Tenant no Ktor:
            defaultRequest {
                url("")
                //url(config.baseUrl)
                //header("X-Tenant-Id", config.tenantId)
            }
        }
    }

    // Builder Ktorfit
    single {
        Ktorfit.Builder()
            .httpClient(get<HttpClient>())
            .build()
    }

    single<ApiService> { get<Ktorfit>().create() }

    single<NetworkProvider> {
        NetworkProvider(
            apiService = get()
        )
    }
}

val authModule = module {
    // Fornece a instância do SDK do Firebase Auth (GitLive)
    single { Firebase.auth }
    singleOf(::FirebaseServiceImpl).bind<FirebaseAuthManager>()

}

val repositoryModule = module {
    single<DataRepository> { DataRepositoryImpl(get()) }
    single { LocationRepository(get()) }
    single<RideRepository> { RideRepositoryImpl(get()) }
    single<RatingRepository> { RatingRepositoryImpl(get(), get()) }
    single<MapboxSearchRepository> { MapboxSearchRepositoryImpl() }

    //single<MapController> { DefaultMapController() }
    single<LocationProvider> { LocationProvider() }

}

val domainModule = module {
    single { GetDataUseCase(get()) }
    single { TaxiUseCase(get()) }
    single { SaveSignUpDraftUseCase(get()) }
    single { GetSignUpDraftUseCase(get(), get()) }
    single { SendRatingUseCase(get()) }
    single { CardOptionsUseCase(get()) }
    single { GetRideHistoryUseCase(get()) }
    single { SaveRideUseCase(get()) }
    single { GetRatingLastUseCase(get()) }
    single { CreateLocalRatingUseCase(get()) }
}

val presentationModule = module {
    viewModel { SignupViewModel(get()) }
    viewModel { LoginViewModel(get()) }
    viewModel { HomeViewModel(get(), get(), get(), get(), get()) }
    viewModel { ProfileViewModel(get(), get(), get(), get()) }
    viewModel { (args: RatingArgs) ->
        RatingViewModel(
            sendRatingUseCase = get(),
            createRatingUseCase = get(),
            args = args
        )
    }
}

val storageModule = module {

    single { Settings() }
    single { AppPreferences(get(), get()) }

    single<AppDatabase> {
        getRoomDatabase(get())
    }

    // DAOs
    single { get<AppDatabase>().notificationDao() }
    single { get<AppDatabase>().rideDao() }
    single { get<AppDatabase>().ratingDao() }
}

val appModule =
    listOf(
        networkModule,
        repositoryModule,
        domainModule,
        presentationModule,
        storageModule,
        authModule
    )   