package com.triapp.di

import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.Settings
import com.triapp.data.ApiService
import com.triapp.data.NetworkProvider
import com.triapp.data.WebSocketProvider
import com.triapp.data.repository.DataRepository
import com.triapp.data.repository.DataRepositoryImpl
import com.triapp.domain.usecase.GetDataUseCase
import com.triapp.domain.usecase.GetSignUpDraftUseCase
import com.triapp.domain.usecase.SaveSignUpDraftUseCase
import com.triapp.local.AppDatabase
import com.triapp.local.AppDatabaseConstructor
import com.triapp.local.AppPreferences
import com.triapp.local.provideObservableSettings
import com.triapp.presentation.feature.home.DefaultMapController
import com.triapp.presentation.feature.home.HomeViewModel
import com.triapp.presentation.feature.home.MapController
import com.triapp.presentation.feature.login.LoginViewModel
import com.triapp.presentation.feature.signup.SignupViewModel
import com.triapp.utils.FirebaseAuthManager
import com.triapp.utils.FirebaseServiceImpl
import com.triapp.utils.LocationProvider
import com.triapp.utils.LocationRepository
import com.triapp.utils.TenantConfig
import com.triapp.utils.TenantService
import com.triapp.utils.getTenantService
import org.koin.dsl.module
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import de.jensklingenberg.ktorfit.Ktorfit
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind

expect fun getPlatformHttpClientEngineFactory(): HttpClientEngineFactory<*>

val networkModule = module {

    single { getPlatformHttpClientEngineFactory() }

    single { get<TenantService>().getCurrentTenantConfig() }

    single {
        Json {
            ignoreUnknownKeys = true
            prettyPrint = true
            isLenient = true
        }
    }

    single {
        val config: TenantConfig = get<TenantConfig>()
        HttpClient(get()) {
            install(ContentNegotiation) {
                json(get())
            }
            // Configuração Multi-Tenant no Ktor:
            defaultRequest {
                url(config.baseUrl)
                header("X-Tenant-Id", config.tenantId)
            }
        }
    }

    // Builder Ktorfit
    single {
        Ktorfit.Builder()
            .httpClient(get<HttpClient>())
            .build()
    }

    // Service Ktorfit
    single<ApiService> { get<Ktorfit>().create() }

    single<WebSocketProvider> {
        val config: TenantConfig = get()
        WebSocketProvider(config)
    }

    // NetworkProvider decide entre REST e WebSocket dinamicamente
    single<NetworkProvider> {
        NetworkProvider(
            apiService = get(),
            webSocketProvider = get()
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
    single<MapController> { DefaultMapController() }
    single { LocationRepository(get()) }
    single<LocationProvider> { LocationProvider() }

}

val domainModule = module {
    single { GetDataUseCase(get()) }
    single { SaveSignUpDraftUseCase(get()) }
    single { GetSignUpDraftUseCase(get(), get()) }
}

val presentationModule = module {
    viewModel { SignupViewModel() }
    viewModel { LoginViewModel(get()) }
    viewModel { HomeViewModel(get()) }
}

val storageModule = module {
    // Multiplatform Settings
    single<ObservableSettings> { provideObservableSettings() }

    single<TenantService> { getTenantService() }

    single { Settings() }
    single { AppPreferences(get(), get()) }



    // Room Database
    single { get<AppDatabaseConstructor>().initialize() }

    // DAOs
    single { get<AppDatabase>().notificationDao() }
    single { get<AppDatabase>().rideDao() }
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