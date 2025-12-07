package com.triappdriver.presentation

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import androidx.navigation.NavHostController
import com.triappdriver.domain.model.RatingArgs
import com.triappdriver.utils.UrlEncoder

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

    fun toRating(args: RatingArgs) {
        // 1. Converte Objeto -> JSON String
        val json = Json.encodeToString(args)

        // 2. Codifica para URL (segurança contra caracteres especiais)
        val encodedJson = UrlEncoder.encode(json)

        // 3. Navega para "rating/O_JSON_ENCODED"
        navigateAndClearStack("${NavRoutes.RATING_BASE}/$encodedJson")
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
    const val RATING_BASE = "rating"
    const val RATING = "$RATING_BASE/{args}"
    const val RESET_CODE = "reset_code"
}

