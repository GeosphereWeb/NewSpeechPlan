package de.geosphere.speechplaning.ui.login

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Ein wiederverwendbares Textfeld für die Passworteingabe mit einem Button
 * zum Ein- und Ausblenden des Passworts.
 *
 * @param value Der aktuelle Wert des Textfeldes.
 * @param onValueChange Callback, der aufgerufen wird, wenn sich der Wert ändert.
 * @param label Die Beschriftung des Textfeldes.
 * @param modifier Der Modifier, der auf dieses Composable angewendet wird.
 */
@Composable
fun PasswordTextfieldComponent(
    value: String, onValueChange: (String) -> Unit, label: String, modifier: Modifier = Modifier
) {
    var passwortSichtbar by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier,
        singleLine = true,
        visualTransformation = if (passwortSichtbar) VisualTransformation.Companion.None
        else PasswordVisualTransformation(),
        trailingIcon = {
            val image = if (passwortSichtbar) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
            val description = if (passwortSichtbar) "Passwort ausblenden" else "Passwort anzeigen"
            IconButton(onClick = { passwortSichtbar = !passwortSichtbar }) {
                Icon(imageVector = image, contentDescription = description)
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Companion.Password)
    )
}
