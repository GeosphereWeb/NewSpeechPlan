package de.geosphere.speechplaning.mocking.di

import androidx.lifecycle.LifecycleCoroutineScope
import de.geosphere.speechplaning.mocking.BuildDummyDBConnection
import org.koin.dsl.module

@Suppress("ForbiddenComment")
val mockingModule =
    module {
        factory { (scope: LifecycleCoroutineScope) -> BuildDummyDBConnection(scope) }
    }
