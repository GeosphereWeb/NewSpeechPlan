package de.geosphere.speechplaning.feature.congregation.di

import de.geosphere.speechplaning.feature.congregation.CongregationViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val congregationModule = module {
    viewModelOf(::CongregationViewModel)
}
