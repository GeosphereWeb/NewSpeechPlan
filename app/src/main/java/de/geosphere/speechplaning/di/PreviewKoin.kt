package de.geosphere.speechplaning.di

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.auth.FirebaseAuth
import de.geosphere.speechplaning.data.ci.dataModule
import io.mockk.mockk
import org.koin.android.ext.koin.androidContext
import org.koin.compose.KoinApplication
import org.koin.dsl.module

val previewModule = module {
    single<FirebaseAuth> { mockk(relaxed = true) }
}

@Composable
fun PreviewKoin(content: @Composable () -> Unit) {
    val context = LocalContext.current
    KoinApplication(application = {
        androidContext(context)
        modules(dataModule, appModule, previewModule)
    }) {
        content()
    }
}
