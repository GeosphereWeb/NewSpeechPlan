package de.geosphere.speechplaning.feature.login.ui

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
import de.geosphere.speechplaning.core.ui.atoms.di.PreviewKoin
import de.geosphere.speechplaning.data.authentication.AuthUiState
import de.geosphere.speechplaning.theme.SpeechPlaningTheme
import de.geosphere.speechplaning.theme.ThemePreviews
import org.koin.androidx.compose.koinViewModel

/**
 * Der zustandsbehaftete (stateful) LoginScreen.
 * Er ist verantwortlich für das Holen des ViewModels und das Sammeln der Zustände.
 */
@Composable
fun LoginScreen(
    authViewModel: AuthViewModel = koinViewModel(),
    onLoginSuccess: () -> Unit,
) {
    val context = LocalContext.current
    val authUiState by authViewModel.getAuthUiState().collectAsState(initial = AuthUiState.Loading)
    val actionUiState by authViewModel.actionUiState.collectAsState()

    // Wenn die Anzeige des LoginScreens verlassen wird (onDispose),
    // wird der temporäre Fehler- oder Ladezustand zurückgesetzt.
    DisposableEffect(Unit) {
        onDispose { authViewModel.resetActionState() }
    }

    LoginScreenContent(
        authUiState = authUiState,
        actionUiState = actionUiState,
        onLoginSuccess = onLoginSuccess,
        onSignInWithEmailAndPassword = authViewModel::signInWithEmailAndPassword,
        onCreateUserWithEmailAndPassword = authViewModel::createUserWithEmailAndPassword,
        onSignInWithGoogleClick = {
            val activity = context as? Activity
            activity?.let { authViewModel.signInWithGoogle(it) }
        },
        onCheckUserStatusAgain = authViewModel::checkUserStatusAgain,
        onResetActionState = authViewModel::resetActionState,
    )
}

/**
 * Der zustandslose (stateless) LoginScreenContent.
 * Er enthält nur die UI, nimmt Zustände und Events als Parameter entgegen und ist daher leicht als Vorschau
 * darstellbar.
 */
@Composable
@Suppress("kotlin:S107")
fun LoginScreenContent(
    authUiState: AuthUiState,
    actionUiState: AuthActionUiState,
    onLoginSuccess: () -> Unit,
    onSignInWithEmailAndPassword: (String, String) -> Unit,
    onCreateUserWithEmailAndPassword: (String, String, String) -> Unit,
    onSignInWithGoogleClick: () -> Unit,
    onCheckUserStatusAgain: () -> Unit,
    onResetActionState: () -> Unit,
) {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf(LoginSubScreen.SignIn) }

    val onGoToSignUp = { currentScreen = LoginSubScreen.SignUp }
    val onGoToSignIn = { currentScreen = LoginSubScreen.SignIn }

    // Effekt für erfolgreiche Authentifizierung (Anmeldung)
    LaunchedEffect(authUiState) {
        if (authUiState is AuthUiState.Authenticated) {
            Toast.makeText(context, "Anmeldung erfolgreich!", Toast.LENGTH_SHORT).show()
            onLoginSuccess()
        }
    }

    // Effekt für erfolgreiche Registrierung
    LaunchedEffect(actionUiState.isSuccess) {
        if (actionUiState.isSuccess) {
            Toast.makeText(
                context,
                "Registrierung erfolgreich! Bitte melde dich an.",
                Toast.LENGTH_LONG,
            ).show()
            onResetActionState()
            onGoToSignIn()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        when (currentScreen) {
            LoginSubScreen.SignIn -> SignInScreen(
                onSignInClick = onSignInWithEmailAndPassword,
                onGoToSignUp = onGoToSignUp,
            )

            LoginSubScreen.SignUp -> SignUpScreen(
                onSignUpClick = onCreateUserWithEmailAndPassword,
                onGoToSignIn = onGoToSignIn,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onSignInWithGoogleClick) {
            Text(text = "Mit Google anmelden")
        }

        // Zeige einen Lade-Spinner, wenn eine Aktion ausgeführt wird oder der globale Zustand noch lädt
        if (actionUiState.isLoading || authUiState is AuthUiState.Loading) {
            CircularProgressIndicator()
        }

        actionUiState.error?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        if (authUiState is AuthUiState.NeedsApproval) {
            Text(
                text = "Dein Konto wurde erstellt und wartet auf die Freigabe durch einen Administrator.",
                modifier = Modifier.padding(top = 8.dp),
            )
            Button(
                onClick = onCheckUserStatusAgain,
                modifier = Modifier.padding(top = 8.dp),
            ) {
                Text("Status erneut überprüfen")
            }
        }
    }
}

private enum class LoginSubScreen {
    SignIn,
    SignUp,
}

// --- PREVIEWS ---

@ThemePreviews
@Composable
fun LoginScreenPreviewUnauthenticatedPreview() = PreviewKoin {
    SpeechPlaningTheme {
        LoginScreenContent(
            authUiState = AuthUiState.Unauthenticated,
            actionUiState = AuthActionUiState(),
            onLoginSuccess = {},
            onSignInWithEmailAndPassword = { _, _ -> },
            onCreateUserWithEmailAndPassword = { _, _, _ -> },
            onSignInWithGoogleClick = {},
            onCheckUserStatusAgain = {},
            onResetActionState = {},
        )
    }
}

@ThemePreviews
@Composable
fun LoginScreenPreviewLoadingPreview() = PreviewKoin {
    SpeechPlaningTheme {
        LoginScreenContent(
            authUiState = AuthUiState.Loading,
            actionUiState = AuthActionUiState(isLoading = true),
            onLoginSuccess = {},
            onSignInWithEmailAndPassword = { _, _ -> },
            onCreateUserWithEmailAndPassword = { _, _, _ -> },
            onSignInWithGoogleClick = {},
            onCheckUserStatusAgain = {},
            onResetActionState = {},
        )
    }
}

@ThemePreviews
@Composable
fun LoginScreenPreviewNeedsApprovalPreview() = PreviewKoin {
    SpeechPlaningTheme {
        LoginScreenContent(
            authUiState = AuthUiState.NeedsApproval,
            actionUiState = AuthActionUiState(),
            onLoginSuccess = {},
            onSignInWithEmailAndPassword = { _, _ -> },
            onCreateUserWithEmailAndPassword = { _, _, _ -> },
            onSignInWithGoogleClick = {},
            onCheckUserStatusAgain = {},
            onResetActionState = {},
        )
    }
}

@ThemePreviews
@Composable
fun LoginScreenPreviewErrorPreview() = PreviewKoin {
    SpeechPlaningTheme {
        LoginScreenContent(
            authUiState = AuthUiState.Unauthenticated,
            actionUiState = AuthActionUiState(error = "E-Mail oder Passwort ist falsch."),
            onLoginSuccess = {},
            onSignInWithEmailAndPassword = { _, _ -> },
            onCreateUserWithEmailAndPassword = { _, _, _ -> },
            onSignInWithGoogleClick = {},
            onCheckUserStatusAgain = {},
            onResetActionState = {},
        )
    }
}
