package de.geosphere.speechplaning.ui.login

import android.widget.Toast
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun SignUpScreen(
    authViewModel: AuthViewModel,
    onGoToSignIn: () -> Unit // Callback für den Wechsel
) {
    var displayName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Dieser LaunchedEffect reagiert, wenn die Registrierung erfolgreich war,
    // zeigt einen Toast an und wechselt zurück zum Anmeldebildschirm.
    LaunchedEffect(key1 = authViewModel.loginActionUiState.collectAsState().value.isSuccess) {
        val state = authViewModel.loginActionUiState.value
        if (state.isSuccess) {
            Toast.makeText(
                context,
                "Registrierung erfolgreich! Bitte melde dich an.",
                Toast.LENGTH_LONG
            ).show()
            authViewModel.resetActionState() // Zustand zurücksetzen
            onGoToSignIn() // Zurück zum Login-Screen
        }
    }

    Text("Neues Konto erstellen", style = MaterialTheme.typography.headlineMedium)
    Spacer(modifier = Modifier.Companion.height(16.dp))

    OutlinedTextField(
        value = displayName,
        onValueChange = { displayName = it },
        label = { Text("Name") },
        modifier = Modifier.Companion.fillMaxWidth(),
        singleLine = true
    )
    Spacer(modifier = Modifier.Companion.height(8.dp))

    OutlinedTextField(
        value = email,
        onValueChange = { email = it },
        label = { Text("E-Mail") },
        modifier = Modifier.Companion.fillMaxWidth(),
        singleLine = true
    )
    Spacer(modifier = Modifier.Companion.height(8.dp))
    PasswordTextfieldComponent(
        value = password,
        onValueChange = { password = it.trim() },
        label = "Passwort",
        modifier = Modifier.Companion.fillMaxWidth()
    )
    Spacer(modifier = Modifier.Companion.height(16.dp))
    Button(
        onClick = { authViewModel.createUserWithEmailAndPassword(email, password) },
        modifier = Modifier.Companion.fillMaxWidth()
    ) {
        Text("Registrieren")
    }
    Spacer(modifier = Modifier.Companion.height(8.dp))
    TextButton(onClick = onGoToSignIn) {
        Text("Bereits ein Konto? Zur Anmeldung")
    }
}
