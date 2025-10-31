package de.geosphere.speechplaning.feature.login.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SignInScreen(
    authViewModel: AuthViewModel,
    onGoToSignUp: () -> Unit // Callback für den Wechsel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    OutlinedTextField(
        value = email,
        onValueChange = { email = it },
        label = { Text("E-Mail") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
    Spacer(modifier = Modifier.height(8.dp))
    PasswordTextfieldComponent(
        value = password,
        onValueChange = { password = it.trim() },
        label = "Passwort",
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(16.dp))
    Button(
        onClick = { authViewModel.signInWithEmailAndPassword(email, password) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Anmelden")
    }
    Spacer(modifier = Modifier.height(8.dp))
    TextButton(onClick = onGoToSignUp) {
        Text("Noch kein Konto? Jetzt registrieren")
    }
}
