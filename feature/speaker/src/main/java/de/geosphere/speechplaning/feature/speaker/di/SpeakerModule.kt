package de.geosphere.speechplaning.feature.speaker.di

import de.geosphere.speechplaning.feature.speaker.ui.SpeakerViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val speakerModule = module {
    viewModelOf(::SpeakerViewModel)
}
