package de.geosphere.speechplaning

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import de.geosphere.speechplaning.mockup.BuildDummyDBConnection
import de.geosphere.speechplaning.ui.main.MainScreenComponent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

@Suppress("UnusedPrivateProperty")
class MainActivity :
    ComponentActivity(),
    KoinComponent {

    private val dummyDbBuilder: BuildDummyDBConnection by inject {
        parametersOf(lifecycleScope)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // enableEdgeToEdge()
        // Das Ausblenden der Systemleisten kann auch in eine Extension-Function ausgelagert werden

        WindowCompat.getInsetsController(window, window.decorView).hidestSystemBars()

        // /////
        dummyDbBuilder()
        // /////

        setContent {
            MainScreenComponent()
        }
    }
}

private fun WindowInsetsControllerCompat.hidestSystemBars() {
    hide(WindowInsetsCompat.Type.statusBars())
    hide(WindowInsetsCompat.Type.navigationBars())
    systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
}
