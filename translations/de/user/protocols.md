---
label: Protokolle
order: 970
icon: sliders
---

# Protokolle

Ein Protokoll ist ein Satz von Generierungseinstellungen: die Sampling-Parameter (Temperatur, Top P und Strafen) und optional eigene Grenzwerte für Kontext und Antwort. Der [Inferenzanbieter](inference-providers.md) legt *welches* Modell schreibt fest; das Protokoll legt *wie* geschrieben wird.

Protokolle sind von Anbietern getrennt, sodass Sie mehrere behalten können, etwa ein vorsichtiges und ein kreatives, und ein Buch zwischen ihnen wechseln können, ohne die Verbindung anzufassen. Wie Anbieter gehören Protokolle zu Ihrem Konto; Sie können eines [teilen](#sharing-a-protocol), damit andere Benutzer eine Kopie importieren können.

![Der Reiter Protokolle](../images/protocols-list.png)

<a id="creating-a-protocol"></a>

## Ein Protokoll anlegen

Öffnen Sie den Reiter **Protokolle** und klicken Sie auf **Protokoll hinzufügen**. Vorhandene Protokolle bearbeiten Sie mit der Stift-Schaltfläche und löschen sie mit der Papierkorb-Schaltfläche. Die Duplizieren-Schaltfläche (zwei Blätter) erstellt eine Kopie eines Protokolls mit allen seinen Einstellungen unter einem Namen, den Sie bestätigen (der des Originals mit *(Kopie)*). Das ist praktisch, um andere Sampling-Einstellungen auszuprobieren, ohne die funktionierenden zu verlieren.

**Von anderem Benutzer importieren** (über der Liste) zeigt die Protokolle, die andere Benutzer [teilen](#sharing-a-protocol), und importiert eine Kopie davon.

![Der Dialog für Protokolle](../images/protocol-dialog.png)

| Feld | |
|---|---|
| **Name** | Ihr Name für das Protokoll, z. B. `Kreativ` oder `Langer Kontext`. |
| **Typ** | Das Anfrageformat. Derzeit nur **Chat Completion**. |
| **Maximale Kontext-Tokens** | Optional. Gesamtzahl der Tokens für Prompt und Antwort von Büchern, die dieses Protokoll verwenden. Leer lassen, um die **Maximale Kontextgröße** des Anbieters zu verwenden. |
| **Maximale Antwort-Tokens** | Optional. Die längste Antwort. Leer lassen, um die **Maximale Antwort-Tokens** des Anbieters zu verwenden. |
| **Temperatur** | Zufälligkeit des Textes. Niedriger (etwa `0.7`) ist fokussierter und vorhersehbarer, höher (`1.0` und mehr) ist abwechslungsreicher und überraschender. |
| **Top P** | Nucleus-Sampling: Das Modell wählt nur aus den wahrscheinlichsten Wörtern, deren Wahrscheinlichkeiten sich zu diesem Wert addieren (`0`–`1`). `0.9`–`0.95` ist eine übliche Wahl. |
| **Frequenzstrafe** | Bestraft Wörter nach der Häufigkeit, mit der sie bereits vorkamen, und verringert so Wiederholungen. Meist `0`–`1`. |
| **Präsenzstrafe** | Bestraft Wörter, die überhaupt schon vorkamen, und lenkt das Modell so auf neue Themen. Meist `0`–`1`. |
| **Mit anderen Benutzern teilen** | Wenn aktiviert, können andere Benutzer das Protokoll ansehen und eine Kopie importieren, siehe [unten](#sharing-a-protocol). Bei einem neuen Protokoll ausgeschaltet. |

Jeder Sampling-Parameter hat ein Kontrollkästchen **Aktivieren**. Ein Parameter, der nicht aktiviert ist, wird von Marginalia nicht gesetzt, und das Modell verwendet seinen eigenen Standardwert. Aktivieren Sie nur, was Sie ändern möchten.

<a id="sharing-a-protocol"></a>

## Ein Protokoll teilen

Aktivieren Sie **Mit anderen Benutzern teilen** im Protokolldialog, und jeder andere Benutzer dieser Installation sieht das Protokoll unter **Von anderem Benutzer importieren** (die Schaltfläche neben **Protokoll hinzufügen**), mit seinem Namen und seinem Autor (Ihr vollständiger Name oder, falls keiner hinterlegt ist, Ihr Anmeldename). Sie können eine Zeile öffnen, um die Einstellungen zu sehen, und auf **Importieren** klicken, wodurch ein eigenes Protokoll mit denselben Einstellungen und demselben Namen entsteht. Teilen ist nur lesend: Niemand sonst kann Ihr Protokoll ändern, löschen oder verwenden, und die Kopie ist privat und nicht mit Ihrer verbunden – was einer von Ihnen danach ändert, erreicht den anderen nicht. Deaktivieren Sie das Kontrollkästchen, verschwindet das Protokoll aus deren Liste; bereits erstellte Kopien bleiben in deren Besitz.

<a id="limits"></a>

## Grenzwerte

Die Grenzwerte des [Inferenzanbieters](inference-providers.md#general-settings) gelten immer. Ein Protokoll kann sie senken, jeden einzeln, aber nie über die Werte des Anbieters anheben:

| | Protokollwert gesetzt | Protokollwert leer |
|---|---|---|
| Kontext | **Maximale Kontext-Tokens**, oder die **Maximale Kontextgröße** des Anbieters, falls diese kleiner ist | **Maximale Kontextgröße** des Anbieters |
| Antwort | **Maximale Antwort-Tokens**, oder die **Maximale Antwort-Tokens** des Anbieters, falls diese kleiner ist | **Maximale Antwort-Tokens** des Anbieters |

Ein Protokoll mit **Maximale Kontext-Tokens** 128 000, das mit einem Anbieter von 32 000 verwendet wird, erhält 32 000: Maßgeblich ist der Anbieter, also das, was der Dienst (oder der Administrator, der ihn teilt) erlaubt.

Marginalia hält den Platz für die Antwort frei und füllt den übrigen Kontext mit dem Prompt: Vorlagen, Lorebook-Einträge, Zusammenfassungen und so viel von der Geschichte, wie hineinpasst. Bei einem Kontext von 32 000 Tokens und Antworten von bis zu 2 000 Tokens erhält der Prompt 30 000 Tokens. Der Antwortgrenzwert wird außerdem an die API gesendet (`max_completion_tokens`).

Nutzen Sie die Grenzwerte des Protokolls, um Prompts kürzer zu halten, als das Modell erlaubt, etwa um lange Bücher mit einem Cloud-Modell mit großem Kontext günstiger und schneller zu machen, oder um Antworten bei einigen Büchern kürzer zu halten. Die Grenzwerte des Protokolls werden außerdem für [Zusammenfassungen](books/summaries.md) von Büchern mit diesem Protokoll verwendet.

Lassen Sie beide leer, sofern Sie sie nicht brauchen: Dann gelten die Werte des Anbieters, und wenn Sie den Anbieter eines Buches wechseln, kommen dessen Grenzwerte mit.

<a id="using-a-protocol-in-a-book"></a>

## Ein Protokoll in einem Buch verwenden

Jedes Buch hat sein Protokoll im Feld **Protokolle** des Reiters **Info**. Um ein Protokoll für alle neuen Bücher zu verwenden, wählen Sie es im Reiter **Einstellungen** als **Standardprotokoll** aus. Ein Buch ohne Protokoll kann nicht generieren und zeigt *Dem Buch fehlt ein Protokoll.* an.

<a id="suggested-settings"></a>

## Empfohlene Einstellungen

Ausgangswerte, passen Sie sie an Ihr Modell an:

| Verwendung | Temperatur | Top P | Frequenzstrafe | Präsenzstrafe |
|---|---|---|---|---|
| Ausgewogene Prosa | `0.8` | `0.95` | aus | aus |
| Kreativ, abwechslungsreich | `1.0` | `0.95` | `0.3` | `0.3` |
| Konsistent, nah an den Anweisungen | `0.6` | `0.9` | aus | aus |

Reasoning-Modelle ignorieren Sampling-Parameter häufig oder lehnen sie ab; lassen Sie diese bei ihnen deaktiviert.
