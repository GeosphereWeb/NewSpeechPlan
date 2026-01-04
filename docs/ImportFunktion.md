Ich habe jetzt ein neues Modul namens `:tools:importer` erstellt.

Hier ist, was ich getan habe:
1. Ein neues Verzeichnis `tools/importer` angelegt mit einem build.gradle.kts.
2. Das Modul in `settings.gradle.kts` registriert.
3. Die `CsvImporter.kt` in das neue Modul kopiert und angepasst, sodass sie das Firebase Admin SDK verwendet.

Wichtig: Nächste Schritte für dich
Damit das Skript funktioniert, musst du noch eine Sache tun, die ich nicht automatisch erledigen kann (aus Sicherheitsgründen):

**1. Service Account JSON herunterladen:**
* Gehe in die Firebase Console.
* Wähle dein Projekt aus.
* Gehe auf das Zahnrad (Projekteinstellungen) -> "Dienstkonten" (Service Accounts).
* Klicke auf "Neuen privaten Schlüssel generieren".
* Speichere die JSON-Datei auf deinem PC.

**2. Pfad anpassen:**
* Öffne die Datei `tools/importer/src/main/java/de/geosphere/speechplaning/tools/importer/CsvImporter.kt`.
* Ändere die Variable serviceAccountPath in Zeile 19 so, dass sie auf den Pfad zeigt, wo du die JSON-Datei gerade gespeichert hast.

**3. Synchronisieren:**
* Klicke in Android Studio oben rechts auf "Sync Now" (den Elefanten), damit das neue Modul erkannt wird.

**4. Ausführen:**
* Gehe zur main() Funktion in der neuen Datei (tools/.../CsvImporter.kt).
* Jetzt sollte ein kleiner grüner Pfeil neben `fun main()` erscheinen. Klicke darauf und wähle "`Run 'CsvImporterKt'`".Die alte Datei im `:data` Modul kannst du danach löschen oder ignorieren, da sie dort nicht lauffähig ist.
