package de.geosphere.speechplaning.di

import de.geosphere.speechplaning.provider.AppEventStringProvider
import de.geosphere.speechplaning.provider.AppSpiritualStatusStringProvider
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val appModule = module {
    singleOf(::AppEventStringProvider)
    singleOf(::AppSpiritualStatusStringProvider)
}
