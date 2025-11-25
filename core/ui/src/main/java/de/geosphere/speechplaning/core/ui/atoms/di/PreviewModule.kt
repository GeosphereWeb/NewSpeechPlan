package de.geosphere.speechplaning.core.ui.atoms.di

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import de.geosphere.speechplaning.data.di.dataModule
import org.koin.android.ext.koin.androidContext
import org.koin.compose.KoinApplication

@Composable
fun PreviewKoin(content: @Composable () -> Unit) {
    val context = LocalContext.current
    KoinApplication(application = {
        androidContext(context)
        modules(dataModule)
    }) {
        content()
    }
}
