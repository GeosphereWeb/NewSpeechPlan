package de.geosphere.speechplaning.feature.speeches.di

import de.geosphere.speechplaning.feature.speeches.ui.SpeechViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val speechModule = module {
    viewModelOf(::SpeechViewModel)
}
