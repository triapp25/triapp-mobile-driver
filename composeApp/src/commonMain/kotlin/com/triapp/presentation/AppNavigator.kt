package com.triapp.presentation


import androidx.navigation.NavHostController

class AppNavigator(
    private val navController: NavHostController
) {
    fun navigateTo(route: String) = navController.navigate(route)

    fun navigateAndClearStack(route: String) {
        navController.navigate(route) {
            popUpTo(0)
            launchSingleTop = true
        }
    }

    fun back() = navController.popBackStack()

    fun toLogin() = navigateAndClearStack(NavRoutes.LOGIN)
    fun toSignup() = navigateAndClearStack(NavRoutes.SIGNUP)
    fun toHome() = navigateAndClearStack(NavRoutes.HOME)
    fun toProfile() = navigateAndClearStack(NavRoutes.PROFILE)
    fun toRating() = navigateAndClearStack(NavRoutes.RATING)
    fun toResetCode() = navigateTo(NavRoutes.RESET_CODE)
}

object NavRoutes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val HOME = "home"
    const val PROFILE = "profile"
    const val RATING = "rating"
    const val RESET_CODE = "reset_code"
}

