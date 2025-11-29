package de.geosphere.speechplaning.feature.districts.di

import de.geosphere.speechplaning.feature.districts.DistrictsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val districtModule = module {
    viewModelOf(::DistrictsViewModel)
}
