---
label: Side Query
order: 1
---

# Side Query

Ein Chat mit dem Modell neben deiner Geschichte: Frag nach der Handlung, prüfe die Kontinuität, sammle Namensideen oder den nächsten Kapitelverlauf – ohne etwas dem Buch hinzuzufügen.

!!!
Side Query funktioniert im [Telefon-Layout](../phone.md#extensions): Der Tab liegt in der Gliederungsschublade der Geschichte.
!!!

![Side Query neben der Geschichte](../../images/plugin-sidequery.png)

<a id="the-side-query-tab"></a>

## Der Side-Query-Tab

Die linke Seitenleiste des [Geschichteneditors](../books/story-editor.md) erhält neben *Gliederung* einen Tab **Side Query**. Zieh den Trenner, um ihm mehr Platz zu geben.

Der Tab enthält Unterhaltungen, jede in einem eigenen Reiter: **+** öffnet eine neue (*Tab 2*, *Tab 3* ...), der Stift benennt einen Reiter um, **×** schließt ihn (der letzte Reiter lässt sich nicht schließen). Mit *Enable AI Tab Naming* im Profil benennt das Modell einen Reiter nach der ersten Antwort, sofern du ihn nicht selbst umbenannt hast. Unterhaltungen gehören zum Buch und bleiben erhalten, bis du ihren Reiter schließt.

<a id="asking"></a>

## Fragen stellen

Tipp deine Frage in das Feld unten und klick auf **SEND**. Die Antwort erscheint, während sie geschrieben wird; **STOP** (an der Stelle von *SEND*) unterbricht sie.

| Schaltfläche | |
|---|---|
| **SEND** | Sendet die Frage. |
| **UNDO** | Entfernt die letzte Nachricht. War es deine Frage, wandert sie zurück in das Feld, um sie zu bearbeiten. |
| **REGENERATE** | Entfernt die letzte Antwort und fragt erneut. |

Jede Nachricht hat Schaltflächen, um sie nach oben oder unten zu **verschieben**, zu **bearbeiten**, in die Zwischenablage zu **kopieren** und aus dem Kontext **auszuschließen** (oder wieder einzuschließen). Ausgeschlossene Nachrichten bleiben in der Unterhaltung, werden aber nicht an das Modell geschickt – praktisch, um eine falsche Antwort loszuwerden, ohne sie zu löschen.

<a id="what-the-model-sees"></a>

## Was das Modell sieht

Die Optionen über der Unterhaltung bestimmen, was zusammen mit deinen Fragen gesendet wird:

| Option | |
|---|---|
| **Lorebook** | Das Lore des Buches (siehe unten). |
| **Chat Logs** *from* … *to* | Die Geschichtenteile des aktiven Zweigs in diesem Bereich, nummeriert ab 1 wie in der Gliederung: `1` bis `6` sind die Teile `#1` bis `#6`. |
| **Stecker-Schaltfläche** | Öffnet eine Liste der Daten, die andere Erweiterungen anbieten, mit einem Kontrollkästchen für jede (siehe unten). |
| **Tokens** | Die ungefähre Größe der Anfrage. |

!!!
*Lorebook* sammelt das Lore so, wie es auch eine Generierung tut: das Lorebook des Buches und seine Unter-Lorebooks (deaktivierte Lorebooks werden übersprungen), dazu die aktivierten Einträge, deren Tags zu den Tags des Buches passen, mit verarbeiteten [Makros](../templates/macros.md). Schlüsselwortfilter werden nicht angewendet – es gibt keinen Geschichten-Prompt, mit dem sie abgeglichen werden könnten, daher wird jeder passende Eintrag gesendet. Variablen, die die Einträge setzen, werden nicht gespeichert.
!!!

<a id="data-of-other-extensions"></a>

### Daten anderer Erweiterungen

Die Stecker-Schaltfläche öffnet ein Popover mit den **zusätzlichen Quellen**, die andere Erweiterungen anbieten, zum Beispiel stellt der [Arc Outline](arc-outline.md) deine Gliederung bereit. Jede Quelle hat ein Kontrollkästchen; die Liste hat bis zu zwei Teile:

- **Über das Buch**: Eine angekreuzte Quelle wird nach dem Lorebook unter ihrem Namen an die Anfrage angehängt.
- **Über Geschichtenteile**: Eine angekreuzte Quelle wird für jeden Geschichtenteil im Bereich *Chat Logs* abgefragt, und was sie zurückgibt, wird nach dem Text dieses Teils angefügt. Sie wirkt nur, solange *Chat Logs* eingeschaltet ist.

Die Auswahl wird mit dem Tab gespeichert. Ist eine Erweiterung nicht installiert (oder gestoppt), stehen ihre Quellen nicht in der Liste und werden nicht gesendet; sie kehren zurück, sobald die Erweiterung wieder da ist. Bietet keine Erweiterung etwas an, sagt das Popover das.

Die Anfrage wird so aufgebaut:

1. eine Systemnachricht: der *Initial System Query* des Profils, die Lorebook-Einträge (*Lorebook Context*), die angekreuzten Quellen über das Buch und die Geschichtenteile (*Chat History Context*, beschriftet mit ihren Gliederungsnummern *Message #1*, *#2* ..., jeweils gefolgt von den angekreuzten Quellen über Geschichtenteile);
2. die eingeschlossenen Nachrichten der Unterhaltung als Chat. Die *Instructions Before User Input* des Profils werden an den Anfang deiner letzten Frage gestellt, vor ihren Text.

Modell und Protokoll kommen aus dem Profil (siehe unten), standardmäßig die des Buches. Für Fragen zur Geschichte reicht meist ein günstiges, schnelles Modell.

<a id="saved-queries"></a>

## Gespeicherte Anfragen

Häufig gestellte Fragen lassen sich speichern:

- **Save prompt** (Diskettensymbol) speichert den Text im Feld unter dem in der Liste gewählten Namen, oder fragt nach einem Namen;
- **Save prompt as...** speichert ihn unter einem neuen Namen;
- wähl einen Namen in der Liste, um den gespeicherten Text in das Feld zu setzen;
- die Papierkorb-Schaltfläche löscht die gewählte gespeicherte Anfrage.

Gespeicherte Anfragen gehören zu deinem Konto und sind in jedem Buch verfügbar.

Einige nützliche Beispiele:

- *Liste jede benannte Figur auf, die bisher aufgetreten ist, mit einer Zeile zu jeder.*
- *Welche Handlungsfäden sind noch offen?*
- *Schlag fünf Namen für eine Hafenstadt in dieser Welt vor.*

<a id="profiles"></a>

## Profile

Die Einstellungen für Side Query findest du unter **Einstellungen → Extension Settings → SideQuery Settings**; sie sind in **Profilen** organisiert, wie bei den [Reviewer](reviewer.md#profiles)-Profilen. Das gewählte Profil ist das, das verwendet wird:

![Die SideQuery-Einstellungen](../../images/plugin-sidequery-settings.png)

| Feld | |
|---|---|
| **Profile**, **New**, **Rename**, **Delete** | Profile auswählen und verwalten. *_Default* lässt sich nicht umbenennen oder löschen. |
| **SideQuery Model**, **SideQuery Protocol** | Der [Inferenzanbieter](../inference-providers.md) und das [Protokoll](../protocols.md), die verwendet werden. Leer: die des Buches. |
| **Initial System Query** | Der Anfang der Systemnachricht, z. B. die Rolle des Assistenten. |
| **Instructions Before User Input** | Text, der direkt vor deine letzte Frage gestellt wird – Regeln für die Antworten, z. B. *Antworte kurz. Zitiere die Geschichte, wenn möglich.* |
| **Enable AI Tab Naming** | Nach der ersten Antwort in einem Reiter wird das Modell um einen kurzen Namen für die Unterhaltung gebeten (eine zusätzliche Anfrage). Umbenannte Reiter behalten ihren Namen. |

Klick oben auf der Seite *Einstellungen* auf **Save**, um die Änderungen zu speichern.

<a id="storage"></a>

## Speicherung

Unterhaltungen werden zusammen mit dem Buch gespeichert, gehören also zu den [Buchsicherungen](../books/backups.md) und Kopien. Profile und gespeicherte Anfragen werden in deinen Benutzereinstellungen abgelegt.
