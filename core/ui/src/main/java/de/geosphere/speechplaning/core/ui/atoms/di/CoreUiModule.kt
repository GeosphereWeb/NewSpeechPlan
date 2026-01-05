package de.geosphere.speechplaning.core.ui.atoms.di

import de.geosphere.speechplaning.core.ui.atoms.AppAvatarResourceProvider
import de.geosphere.speechplaning.core.ui.atoms.AvatarProvider
import de.geosphere.speechplaning.core.ui.provider.EventMapper
import de.geosphere.speechplaning.core.ui.atoms.SpiritualStatusMapper
import de.geosphere.speechplaning.core.ui.provider.AppSpiritualStatusStringProvider
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val coreUiModule = module {
    // Diese Definition gehört hierher, weil das ViewModel Teil des Login-Features ist.
    singleOf(::SpiritualStatusMapper)
    factoryOf(::EventMapper)
    factoryOf(::AvatarProvider)
    singleOf(::AppAvatarResourceProvider)
    singleOf(::AppSpiritualStatusStringProvider)
}
