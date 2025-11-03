package com.triapp.di

import org.koin.dsl.KoinAppDeclaration
import org.koin.core.context.startKoin

fun initKoin(appDeclaration: KoinAppDeclaration = {}) = startKoin {
    appDeclaration()
    // Adicionar loggers e outras configurações do Koin
    modules(appModule)
}