package de.geosphere.speechplaning.feature.planning.di

import de.geosphere.speechplaning.data.usecases.planning.DeleteCongregationEventUseCase
import de.geosphere.speechplaning.data.usecases.planning.GetCongregationEventsUseCase
import de.geosphere.speechplaning.data.usecases.planning.SaveCongregationEventUseCase
import de.geosphere.speechplaning.feature.planning.ui.CongregationEventViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val planningModule = module {
    // Use Cases
    factoryOf(::GetCongregationEventsUseCase)
    factoryOf(::SaveCongregationEventUseCase)
    factoryOf(::DeleteCongregationEventUseCase)

    // ViewModel
    viewModelOf(::CongregationEventViewModel)
}
