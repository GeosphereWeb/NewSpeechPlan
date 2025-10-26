package de.geosphere.speechplaning.mocking.di

import androidx.lifecycle.LifecycleCoroutineScope
import de.geosphere.speechplaning.mocking.BuildDummyDBConnection

@Suppress("ForbiddenComment")
val mockingModule =
    module {
        factory { (scope: LifecycleCoroutineScope) -> BuildDummyDBConnection(scope) }
    }
