package com.triapp.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import com.triapp.presentation.feature.home.HomeScreenTwo
import com.triapp.presentation.feature.login.LoginScreen
import com.triapp.presentation.feature.profile.ProfileFlowScreen
import com.triapp.presentation.feature.rating.TripRatingFlowScreen
import com.triapp.presentation.feature.signup.SignupFlowScreen

@Composable
fun AppNavigationHost(modifier: Modifier = Modifier, activity: Any?) {
    val navController = rememberNavController()
    AppNavGraph(navController = navController, activity = activity, modifier = modifier)
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    activity: Any?,
    modifier: Modifier = Modifier
) {
    val navigator = AppNavigator(navController)

    NavHost(
        navController = navController,
        startDestination = NavRoutes.HOME,
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
            SignupFlowScreen(activity)
                {
                    navigator.toHome()
                }
        }

        composable(
            route = NavRoutes.HOME,
            deepLinks = listOf(navDeepLink { uriPattern = "triapp://home" })
        ) {
            HomeScreenTwo() {
                navigator.toRating()
            }
        }
        composable(
            route = NavRoutes.PROFILE,
            deepLinks = listOf(navDeepLink { uriPattern = "triapp://profile" })
        ) {
            ProfileFlowScreen({},{})
        }

        composable(
            route = NavRoutes.RATING,
            deepLinks = listOf(navDeepLink { uriPattern = "triapp://rating" })
        ) {
            TripRatingFlowScreen {
                navigator.toHome()
            }
        }
    }
}
