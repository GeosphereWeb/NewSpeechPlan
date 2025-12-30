plugins {
    kotlin("jvm")
}

dependencies {
    // KEINE Abhängigkeit zu :core:model, da dies ein Android-Modul ist!
    // Wir definieren die benötigten Klassen hier lokal.
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("com.google.firebase:firebase-admin:9.4.3")
    implementation("org.slf4j:slf4j-simple:2.0.9")
}
