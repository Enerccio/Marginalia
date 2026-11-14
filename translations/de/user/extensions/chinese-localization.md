---
label: Chinesische Lokalisierung
order: 18
---

# Chinesische Lokalisierung

Fügt Vereinfachtes Chinesisch (中文（简体）) zu den Sprachen der Benutzeroberfläche hinzu.

<a id="using-it"></a>

## Verwendung

Nachdem ein Administrator die Erweiterung geladen hat ([Erweiterungen verwalten](../administration/extensions.md)), steht *中文（简体）*
im Auswahlfeld **Sprache** unten im Arbeitsbereich (im Menü auf einem [Telefon](../phone.md)), neben
Englisch und Japanisch. Wählen Sie es aus, und die Seite wird auf Chinesisch neu geladen. Die Auswahl wird in Ihrem Konto gespeichert und gilt daher auf jedem Gerät, auf dem Sie sich anmelden. Jeder Benutzer wählt seine eigene Sprache.

![Der Geschichtseditor auf Chinesisch](../../images/plugin-chineselocalization.png)

Die Oberfläche ist übersetzt: Menüs, Schaltflächen, Meldungen, die Beschreibungen der Vorlagenvariablen und Makros. Ein Text, für den es noch keine chinesische Übersetzung gibt, wird auf Englisch angezeigt.

Die Erweiterung fügt außerdem Chinesisch zur **Sprache der Prompts** in den Einstellungen hinzu (*Einstellungen → Vorlagen*, siehe
[Sprache der Prompts](../account.md#prompt-language)): Die eingebauten Prompts der Anwendung und der Plugins werden dann auf Chinesisch gesendet. Es handelt sich um eine eigene Auswahl, unabhängig von der Sprache der Oberfläche. Ihre Bücher, Lorebooks und selbst geschriebenen Prompts werden nicht verändert.

<a id="removing-it"></a>

## Entfernen

Das Entladen der Erweiterung entfernt die Sprache aus der Oberfläche und aus den Prompts. Benutzer, die sie gewählt hatten, sehen und senden bis zur Wahl einer anderen Sprache Englisch; ihre Auswahl bleibt erhalten und gilt wieder, sobald die Erweiterung geladen wird.

<a id="for-developers"></a>

## Für Entwickler

Die Erweiterung ist das Beispiel für das Hinzufügen einer Sprache: siehe
[Beispiel-Plugin → Chinesische Lokalisierung](../../developer/plugins/example.md#walkthrough-chinese-localization).
