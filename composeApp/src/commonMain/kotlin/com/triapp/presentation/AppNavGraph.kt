package com.triapp.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import androidx.navigation.navDeepLink
import com.triapp.domain.model.RatingArgs
import com.triapp.presentation.feature.home.HomeScreen
import com.triapp.presentation.feature.login.LoginScreen
import com.triapp.presentation.feature.profile.ProfileFlowScreen
import com.triapp.presentation.feature.rating.TripRatingFlowScreen
import com.triapp.presentation.feature.signup.SignupFlowScreen
import com.triapp.utils.FirebaseAuthManager
import com.triapp.utils.UrlEncoder
import kotlinx.serialization.json.Json

@Composable
fun AppNavigationHost(
    modifier: Modifier = Modifier,
    activity: Any?,
    authManager: FirebaseAuthManager
) {
    val navController = rememberNavController()

    val currentUser = authManager.getCurrentUser()
    val startDestination = if (currentUser != null) NavRoutes.HOME else NavRoutes.LOGIN

    SecurityGuard(navController, authManager)

    AppNavGraph(
        navController = navController,
        startDestination = startDestination,
        activity = activity,
        modifier = modifier
    )
}

@Composable
fun SecurityGuard(
    navController: NavHostController,
    authManager: FirebaseAuthManager
) {
    // Rotas que não exigem login
    val publicRoutes = listOf(NavRoutes.LOGIN, NavRoutes.SIGNUP, NavRoutes.RESET_CODE)

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(currentRoute) {
        // Se a rota mudou (ex: via DeepLink) e não é pública
        if (currentRoute != null &&
            currentRoute !in publicRoutes &&
            !currentRoute.startsWith(NavRoutes.LOGIN) // garante que não bloqueie sub-rotas de login se houver
        ) {
            // Verifica se tem usuário
            val user = authManager.getCurrentUser()
            if (user == null) {
                // Bloqueia e joga para o Login limpando a pilha
                navController.navigate(NavRoutes.LOGIN) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String,
    activity: Any?,
    modifier: Modifier = Modifier
) {
    val navigator = AppNavigator(navController)

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(
            route = NavRoutes.LOGIN,
            deepLinks = listOf(navDeepLink { uriPattern = "triapp://login" })
        ) {
            LoginScreen(
                activity,
                { navigator.toSignup() },
                { navigator.toHome() }
            )
        }

        composable(
            route = NavRoutes.SIGNUP,
            deepLinks = listOf(navDeepLink { uriPattern = "triapp://signup" })
        ) {
            SignupFlowScreen(
                activity,
                { navigator.toLogin() },
                { navigator.toHome() }
            )
        }

        composable(
            route = NavRoutes.HOME,
            deepLinks = listOf(navDeepLink { uriPattern = "triapp://home" })
        ) {
            HomeScreen(
                onNavigateToRate = {
                    val dummyArgs = RatingArgs(
                        tripId = "12345",
                        driverName = "Carlos Silva",
                        driverCar = "Toyota Corolla",
                        driverPhotoUrl = null,
                        price = "R$ 15,90",
                        time = "10 min"
                    )
                    navigator.toRating(dummyArgs)
                },
                onLogout = {
                    navigator.toLogin()
                })
        }

        composable(
            route = NavRoutes.PROFILE,
            deepLinks = listOf(navDeepLink { uriPattern = "triapp://profile" })
        ) {
            ProfileFlowScreen({}, {})
        }

        // Rota de Rating com Argumentos
        composable(
            route = NavRoutes.RATING,
            arguments = listOf(navArgument("args") { type = NavType.StringType }),
            deepLinks = listOf(navDeepLink { uriPattern = "triapp://rating/{args}" })
        ) { backStackEntry ->
            val argsJsonEncoded = backStackEntry.arguments?.getString("args")

            if (argsJsonEncoded != null) {
                val jsonRaw = UrlEncoder.decode(argsJsonEncoded)
                val args = Json.decodeFromString<RatingArgs>(jsonRaw)

                TripRatingFlowScreen(args) {
                    navigator.toHome()
                }
            }
        }

        composable(route = NavRoutes.RESET_CODE) {
            // Tela de reset
        }
    }
}