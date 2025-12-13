import tkinter as tk
from tkinter import filedialog, messagebox, ttk
import csv
import json
import os

class CSVtoJSONConverter:
    def __init__(self, root):
        self.root = root
        self.root.title("CSV zu JSON Konverter")
        self.root.geometry("700x600")
        self.root.resizable(True, True)
        
        self.csv_file_path = None
        self.json_file_path = None
        
        self.setup_ui()
    
    def setup_ui(self):
        # Hauptframe
        main_frame = ttk.Frame(self.root, padding="20")
        main_frame.pack(fill=tk.BOTH, expand=True)
        
        # Titel
        title_label = ttk.Label(
            main_frame, 
            text="CSV zu JSON Konverter", 
            font=("Arial", 16, "bold")
        )
        title_label.pack(pady=(0, 20))
        
        # CSV Datei auswählen
        csv_frame = ttk.LabelFrame(main_frame, text="1. CSV Datei auswählen", padding="10")
        csv_frame.pack(fill=tk.X, pady=(0, 15))
        
        self.csv_label = ttk.Label(csv_frame, text="Keine Datei ausgewählt", foreground="gray")
        self.csv_label.pack(side=tk.LEFT, fill=tk.X, expand=True, padx=(0, 10))
        
        csv_button = ttk.Button(csv_frame, text="Durchsuchen...", command=self.select_csv_file)
        csv_button.pack(side=tk.RIGHT)
        
        # Encoding-Auswahl
        encoding_frame = ttk.LabelFrame(main_frame, text="2. Encoding auswählen", padding="10")
        encoding_frame.pack(fill=tk.X, pady=(0, 15))
        
        self.encoding_var = tk.StringVar(value="utf-8")
        encodings = [("UTF-8", "utf-8"), ("Latin-1 (ISO-8859-1)", "latin-1"), ("Windows-1252", "cp1252")]
        
        for text, encoding in encodings:
            rb = ttk.Radiobutton(
                encoding_frame, 
                text=text, 
                variable=self.encoding_var, 
                value=encoding
            )
            rb.pack(anchor=tk.W, pady=2)
        
        # Trennzeichen
        delimiter_frame = ttk.LabelFrame(main_frame, text="3. Trennzeichen", padding="10")
        delimiter_frame.pack(fill=tk.X, pady=(0, 15))
        
        self.delimiter_var = tk.StringVar(value=";")
        delimiters = [("Semikolon (;)", ";"), ("Komma (,)", ","), ("Tab", "\t")]
        
        for text, delimiter in delimiters:
            rb = ttk.Radiobutton(
                delimiter_frame, 
                text=text, 
                variable=self.delimiter_var, 
                value=delimiter
            )
            rb.pack(anchor=tk.W, pady=2)
        
        # JSON Format
        format_frame = ttk.LabelFrame(main_frame, text="4. JSON Format", padding="10")
        format_frame.pack(fill=tk.X, pady=(0, 15))
        
        self.indent_var = tk.BooleanVar(value=True)
        indent_check = ttk.Checkbutton(
            format_frame, 
            text="Formatiert (mit Einrückung)", 
            variable=self.indent_var
        )
        indent_check.pack(anchor=tk.W)
        
        # Konvertieren Button
        convert_button = ttk.Button(
            main_frame, 
            text="In JSON konvertieren", 
            command=self.convert_to_json,
            style="Accent.TButton"
        )
        convert_button.pack(pady=20, ipadx=20, ipady=5)
        
        # Status
        self.status_label = ttk.Label(main_frame, text="", foreground="blue")
        self.status_label.pack()
    
    def select_csv_file(self):
        file_path = filedialog.askopenfilename(
            title="CSV Datei auswählen",
            filetypes=[("CSV Dateien", "*.csv"), ("Alle Dateien", "*.*")]
        )
        
        if file_path:
            self.csv_file_path = file_path
            self.csv_label.config(
                text=os.path.basename(file_path), 
                foreground="black"
            )
            self.status_label.config(text="")
    
    def convert_csv_to_hierarchical_json(self, csv_data):
        """Konvertiert flache CSV-Daten in hierarchische JSON-Struktur"""
        kreise_dict = {}
        
        for row in csv_data:
            # Kreis-ID als Schlüssel
            kreis_id = row.get('ID_Kreis', '')
            
            # Kreis erstellen falls nicht vorhanden
            if kreis_id not in kreise_dict:
                kreise_dict[kreis_id] = {
                    'ID_Kreis': kreis_id,
                    'Name': row.get('tblKreis.Name', ''),
                    'Versammlungen': {}
                }
            
            kreis = kreise_dict[kreis_id]
            
            # Versammlung hinzufügen
            versammlung_id = row.get('ID_Versammlung', '')
            if versammlung_id not in kreis['Versammlungen']:
                kreis['Versammlungen'][versammlung_id] = {
                    'ID_Versammlung': versammlung_id,
                    'Name': row.get('tblVersammlung.Name', ''),
                    'Strasse': row.get('Strasse', ''),
                    'Hausnummer': row.get('Hausnummer', ''),
                    'Hausnummer_Zusatz': row.get('Hausnummer Zusatz', ''),
                    'PLZ': row.get('PLZ', ''),
                    'Ort': row.get('Ort', ''),
                    'Kreis_ID': row.get('Kreis_ID', ''),
                    'Vortragskoordinator': row.get('Vortragskoordinator', ''),
                    'Aktiv': row.get('tblVersammlung.Aktiv', ''),
                    'Redner': {}
                }
            
            versammlung = kreis['Versammlungen'][versammlung_id]
            
            # Redner hinzufügen
            redner_id = row.get('ID_Redner', '')
            if redner_id not in versammlung['Redner']:
                versammlung['Redner'][redner_id] = {
                    'ID_Redner': redner_id,
                    'Nachname': row.get('Nachname', ''),
                    'Vorname': row.get('Vorname', ''),
                    'Telefon': row.get('Telefon', ''),
                    'Telefon2': row.get('Telefon2', ''),
                    'E-Mail': row.get('E-Mail', ''),
                    'Versammlung_ID': row.get('Versammlung_ID', ''),
                    'Stand_ID': row.get('Stand_ID', ''),
                    'Aktiv': row.get('tblRedner.Aktiv', ''),
                    'Ranking': row.get('Ranking', ''),
                    'Anmerkung': row.get('Anmerkung', ''),
                    'Vortraege': []
                }
            
            redner = versammlung['Redner'][redner_id]
            
            # Vortrag hinzufügen
            vortrag = {
                'Nummer': row.get('Nummer', ''),
                'Titel': row.get('Titel', ''),
                'Aktiv': row.get('NEU_tblVortaege.Aktiv', '')
            }
            redner['Vortraege'].append(vortrag)
        
        # Dictionaries in Listen umwandeln
        result = {'Kreise': []}
        for kreis in kreise_dict.values():
            kreis['Versammlungen'] = list(kreis['Versammlungen'].values())
            for versammlung in kreis['Versammlungen']:
                versammlung['Redner'] = list(versammlung['Redner'].values())
            result['Kreise'].append(kreis)
        
        return result
    
    def convert_to_json(self):
        if not self.csv_file_path:
            messagebox.showwarning("Warnung", "Bitte wählen Sie zuerst eine CSV-Datei aus.")
            return
        
        # JSON Speicherort auswählen
        json_file_path = filedialog.asksaveasfilename(
            title="JSON Datei speichern als",
            defaultextension=".json",
            filetypes=[("JSON Dateien", "*.json"), ("Alle Dateien", "*.*")],
            initialfile=os.path.splitext(os.path.basename(self.csv_file_path))[0] + ".json"
        )
        
        if not json_file_path:
            return
        
        try:
            # CSV einlesen
            encoding = self.encoding_var.get()
            delimiter = self.delimiter_var.get()
            
            with open(self.csv_file_path, 'r', encoding=encoding) as csv_file:
                csv_reader = csv.DictReader(csv_file, delimiter=delimiter)
                data = list(csv_reader)
            
            # In hierarchische Struktur konvertieren
            hierarchical_data = self.convert_csv_to_hierarchical_json(data)
            
            # In JSON konvertieren
            indent = 2 if self.indent_var.get() else None
            
            with open(json_file_path, 'w', encoding='utf-8') as json_file:
                json.dump(hierarchical_data, json_file, ensure_ascii=False, indent=indent)
            
            # Statistiken berechnen
            total_kreise = len(hierarchical_data['Kreise'])
            total_versammlungen = sum(len(k['Versammlungen']) for k in hierarchical_data['Kreise'])
            total_redner = sum(len(v['Redner']) for k in hierarchical_data['Kreise'] 
                              for v in k['Versammlungen'])
            total_vortraege = sum(len(r['Vortraege']) for k in hierarchical_data['Kreise'] 
                                 for v in k['Versammlungen'] for r in v['Redner'])
            
            self.status_label.config(
                text=f"✓ Erfolgreich konvertiert! {total_kreise} Kreise, {total_versammlungen} Versammlungen, {total_redner} Redner, {total_vortraege} Vorträge", 
                foreground="green"
            )
            
            messagebox.showinfo(
                "Erfolg", 
                f"Die CSV-Datei wurde erfolgreich konvertiert!\n\n"
                f"Kreise: {total_kreise}\n"
                f"Versammlungen: {total_versammlungen}\n"
                f"Redner: {total_redner}\n"
                f"Vorträge: {total_vortraege}\n\n"
                f"Gespeichert unter:\n{json_file_path}"
            )
            
        except UnicodeDecodeError:
            messagebox.showerror(
                "Encoding-Fehler", 
                "Die Datei konnte mit dem gewählten Encoding nicht gelesen werden.\n"
                "Versuchen Sie ein anderes Encoding (z.B. Latin-1 oder Windows-1252)."
            )
            self.status_label.config(text="✗ Fehler beim Encoding", foreground="red")
            
        except Exception as e:
            messagebox.showerror("Fehler", f"Ein Fehler ist aufgetreten:\n{str(e)}")
            self.status_label.config(text="✗ Konvertierung fehlgeschlagen", foreground="red")

if __name__ == "__main__":
    root = tk.Tk()
    app = CSVtoJSONConverter(root)
    root.mainloop()