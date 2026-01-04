package de.geosphere.speechplaning.di

import de.geosphere.speechplaning.core.ui.provider.AppEventStringProvider
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val appModule = module {
    singleOf(::AppEventStringProvider)
}
