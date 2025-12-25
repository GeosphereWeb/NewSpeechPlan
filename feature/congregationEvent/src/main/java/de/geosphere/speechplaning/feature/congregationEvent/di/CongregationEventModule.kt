package de.geosphere.speechplaning.feature.congregationEvent.di

import de.geosphere.speechplaning.feature.congregationEvent.CongregationEventViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val congregationEventModule = module {
    viewModelOf(::CongregationEventViewModel)
}
