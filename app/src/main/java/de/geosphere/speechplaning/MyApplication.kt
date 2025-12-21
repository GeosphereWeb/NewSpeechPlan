package de.geosphere.speechplaning

import android.app.Application
import com.google.firebase.FirebaseApp
import de.geosphere.speechplaning.core.ui.atoms.di.coreUiModule
import de.geosphere.speechplaning.data.di.dataModule
import de.geosphere.speechplaning.di.appModule
import de.geosphere.speechplaning.feature.congregation.di.congregationModule
import de.geosphere.speechplaning.feature.congregationEvent.di.congregationEventModule
import de.geosphere.speechplaning.feature.districts.di.districtModule
import de.geosphere.speechplaning.feature.login.di.loginModule
import de.geosphere.speechplaning.feature.speaker.di.speakerModule
import de.geosphere.speechplaning.feature.speeches.di.speechModule
import de.geosphere.speechplaning.mocking.di.mockingModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin

class MyApplication : Application() {
    companion object {
        const val TAG = "SpeechPlaning"
    }

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

        startKoin {
            androidLogger()
            androidContext(this@MyApplication)
            modules(
                appModule,
                dataModule,
                mockingModule,
                loginModule,
                coreUiModule,
                speechModule,
                districtModule,
                congregationModule,
                speakerModule,
                congregationEventModule
            )
        }
    }
}
