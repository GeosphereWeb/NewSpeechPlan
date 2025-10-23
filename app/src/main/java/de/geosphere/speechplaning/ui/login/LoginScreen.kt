package de.geosphere.speechplaning.ui.login

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import de.geosphere.speechplaning.data.authentication.AuthUiState
import de.geosphere.speechplaning.di.PreviewKoin
import de.geosphere.speechplaning.ui.theme.SpeechPlaningTheme
import de.geosphere.speechplaning.ui.theme.ThemePreviews
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel = koinViewModel(),
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current

    // --- NEU: Zustand für die aktuelle Ansicht (SignIn oder SignUp) ---
    var currentScreen by remember { mutableStateOf(LoginSubScreen.SignIn) }

    // --- ÄNDERUNG: Die Callbacks für die Unter-Screens definieren ---
    val onGoToSignUp = { currentScreen = LoginSubScreen.SignUp }
    val onGoToSignIn = { currentScreen = LoginSubScreen.SignIn }

    val authUiState by authViewModel.getAuthUiState().collectAsState(initial = AuthUiState.Loading)
    val actionUiState by authViewModel.actionUiState.collectAsState()

    // Wenn die Anzeige des LoginScreens verlassen wird (onDispose),
    // wird der temporäre Fehler- oder Ladezustand zurückgesetzt.
    DisposableEffect(Unit) {
        onDispose { authViewModel.resetActionState() }
    }

    // --- NEU: LaunchedEffect, der auf den authUiState reagiert ---
    LaunchedEffect(authUiState) {
        if (authUiState is AuthUiState.Authenticated) {
            Toast.makeText(context, "Anmeldung erfolgreich!", Toast.LENGTH_SHORT).show()
            // Hier würdest du typischerweise als Nächstes zum Hauptbildschirm deiner App navigieren.
            onLoginSuccess()
        }
    }

    // Ein Container für unsere UI-Elemente
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (currentScreen) {
            LoginSubScreen.SignIn -> SignInScreen(authViewModel = authViewModel, onGoToSignUp = onGoToSignUp)
            LoginSubScreen.SignUp -> SignUpScreen(authViewModel = authViewModel, onGoToSignIn = onGoToSignIn)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            // Activity holen und Anmeldevorgang starten
            val activity = context as? Activity
            if (activity != null) {
                authViewModel.signInWithGoogle(activity)
            }
            // Optional: Zeige eine Fehlermeldung, falls der Context keine Activity ist
        }) {
            Text(text = "Mit Google anmelden")
        }

        // Zeige einen Lade-Spinner, wenn eine Aktion ausgeführt wird ODER der globale Zustand noch lädt
        if (actionUiState.isLoading || authUiState is AuthUiState.Loading) {
            CircularProgressIndicator()
        }

        // Zeige einen Fehler an, wenn die Login/Registrierungs-Aktion fehlschlägt
        actionUiState.error?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Zeige eine spezifische Nachricht an, wenn der Nutzer angemeldet ist, aber noch auf Freigabe wartet.
        if (authUiState is AuthUiState.NeedsApproval) {
            Text(
                text = "Dein Konto wurde erstellt und wartet auf die Freigabe durch einen Administrator.",
                modifier = Modifier.padding(top = 8.dp)
            )
            // Button, um den Status zu prüfen
            Button(
                onClick = { authViewModel.checkUserStatusAgain() },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Status erneut überprüfen")
            }
        }

        // Der Erfolgsfall wird jetzt durch den globalen `authUiState` abgedeckt.
        // Wenn `authUiState` zu `Authenticated` wechselt, würdest du normalerweise
        // zu einem anderen Bildschirm navigieren. Die reine Erfolgsmeldung hier
        // ist für die weitere Entwicklung nicht mehr nötig.
        // `actionUiState.isSuccess` kann aber als Trigger für die Navigation dienen.

        if (actionUiState.isSuccess) {
            // onLoginSuccess()
        }
    }
}

private enum class LoginSubScreen {
    SignIn,
    SignUp
}

// Eine Vorschau, damit du das Design in Android Studio sehen kannst
@ThemePreviews
@Composable
fun LoginScreenPreview() = PreviewKoin {
    SpeechPlaningTheme {
        LoginScreen(onLoginSuccess = {})
    }
}
