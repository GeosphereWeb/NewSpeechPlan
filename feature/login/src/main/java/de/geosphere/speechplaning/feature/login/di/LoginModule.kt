package de.geosphere.speechplaning.feature.login.di

import de.geosphere.speechplaning.feature.login.ui.AuthViewModel
import de.geosphere.speechplaning.feature.login.ui.UserViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val loginModule = module {
    // Diese Definition gehört hierher, weil das ViewModel Teil des Login-Features ist.
    viewModelOf(::AuthViewModel)
    viewModelOf(::UserViewModel)
}
