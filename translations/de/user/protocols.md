---
label: Protokolle
order: 970
icon: sliders
---

# Protokolle

Ein Protokoll ist eine Gruppe von Generierungseinstellungen: die Sampling-Parameter (Temperatur, Top P und Strafwerte) und,
optional, eigene Kontext- und Antwortlimits. Der [Inferenzanbieter](inference-providers.md) legt fest, *welches* Modell schreibt; das Protokoll legt fest, *wie*.

Protokolle sind von Anbietern getrennt. Daher kannst du dir mehrere anlegen, etwa ein vorsichtiges und ein kreatives, und
ein Buch zwischen ihnen wechseln lassen, ohne die Verbindung anzufassen. Wie Anbieter gehören Protokolle zu deinem Konto; du kannst eines [teilen](#sharing-a-protocol), damit andere Benutzer eine Kopie importieren können.

![Der Reiter Protokolle](../images/protocols-list.png)

<a id="creating-a-protocol"></a>

## Ein Protokoll anlegen

Öffne den Reiter **Protokolle** und klicke auf **Protokoll hinzufügen**. Vorhandene Protokolle bearbeitest du mit der Stift-Schaltfläche und löschst sie
mit der Papierkorb-Schaltfläche. Die Duplizieren-Schaltfläche (zwei Blätter) erstellt eine Kopie eines Protokolls mit allen seinen Einstellungen unter einem Namen, den du bestätigst (der des Originals mit *(Kopie)*). Das ist praktisch, um andere Sampling-Einstellungen auszuprobieren, ohne die bewährten zu verlieren.

**Von anderem Benutzer importieren** (über der Liste) zeigt die Protokolle, die andere Benutzer [geteilt](#sharing-a-protocol) haben, und importiert eine Kopie davon.

![Der Dialog für Protokolle](../images/protocol-dialog.png)

| Feld | |
|---|---|
| **Name** | Dein Name für das Protokoll, z. B. `Creative` oder `Long context`. |
| **Typ** | Das Anfrageformat. Derzeit nur **Chat Completion**. |
| **Maximale Kontext-Tokens** | Optional. Gesamtzahl der Tokens für Prompt und Antwort von Büchern, die dieses Protokoll verwenden. Leer lassen, um die **Maximale Kontextgröße** des Anbieters zu verwenden. |
| **Maximale Antwort-Tokens** | Optional. Die längste Antwort. Leer lassen, um die **Maximale Antwort-Tokens** des Anbieters zu verwenden. |
| **Temperatur** | Zufälligkeit des Textes. Niedriger (etwa `0.7`) ist fokussierter und vorhersehbarer, höher (`1.0` und mehr) ist abwechslungsreicher und überraschender. |
| **Top P** | Nucleus-Sampling: Das Modell wählt nur unter den wahrscheinlichsten Wörtern, deren Wahrscheinlichkeiten zusammen diesen Wert ergeben (`0`-`1`). `0.9`-`0.95` ist eine übliche Wahl. |
| **Frequency Penalty** | Bestraft Wörter nach der Häufigkeit, mit der sie bereits vorkamen, und verringert so Wiederholungen. Meist `0`-`1`. |
| **Presence Penalty** | Bestraft Wörter, die überhaupt schon vorkamen, und drängt das Modell so zu neuen Themen. Meist `0`-`1`. |
| **Mit anderen Benutzern teilen** | Ist das Feld angehakt, können andere Benutzer das Protokoll ansehen und eine Kopie importieren, siehe [unten](#sharing-a-protocol). Bei einem neuen Protokoll ausgeschaltet. |

Jeder Sampling-Parameter hat ein Kontrollkästchen **Aktivieren**. Ein nicht aktivierter Parameter wird von Marginalia nicht gesetzt, und das Modell verwendet seinen eigenen Standardwert. Aktiviere nur, was du ändern möchtest.

<a id="sharing-a-protocol"></a>

## Ein Protokoll teilen

Haken **Mit anderen Benutzern teilen** im Protokolldialog an, und jeder andere Benutzer dieser Installation sieht das Protokoll unter **Von anderem Benutzer importieren** (die Schaltfläche neben **Protokoll hinzufügen**), mit seinem Namen und seinem Autor (dein vollständiger Name oder, falls keiner hinterlegt ist, dein Login). Sie können eine Zeile öffnen, um die Einstellungen zu sehen, und auf **Importieren** klicken, wodurch ein eigenes Protokoll mit denselben Einstellungen und demselben Namen entsteht. Teilen gewährt nur Leserechte: Niemand sonst kann dein Protokoll ändern, löschen oder verwenden, und die Kopie ist privat und nicht mit deinem Protokoll verbunden – was einer von beiden danach ändert, erreicht den anderen nicht. Entfernst du den Haken, verschwindet das Protokoll aus ihrer Liste; Kopien, die sie bereits angelegt haben, bleiben ihnen erhalten.

## Limits

Die Limits des [Inferenzanbieters](inference-providers.md#general-settings) gelten immer. Ein Protokoll kann sie
jeweils einzeln senken, aber nie über die Werte des Anbieters anheben:

| | Protokollwert gesetzt | Protokollwert leer |
|---|---|---|
| Kontext | **Maximale Kontext-Tokens**, oder die **Maximale Kontextgröße** des Anbieters, falls dieser Wert kleiner ist | **Maximale Kontextgröße** des Anbieters |
| Antwort | **Maximale Antwort-Tokens**, oder die **Maximale Antwort-Tokens** des Anbieters, falls dieser Wert kleiner ist | **Maximale Antwort-Tokens** des Anbieters |

Ein Protokoll mit **Maximale Kontext-Tokens** 128 000, das mit einem Anbieter mit 32 000 verwendet wird, erhält 32 000: Maßgeblich ist, was der Dienst (oder der Administrator, der ihn teilt) erlaubt.

Marginalia hält Platz für die Antwort frei und füllt den übrigen Kontext mit dem Prompt: Vorlagen, Lorebook-Einträge, Zusammenfassungen und so viel von der Geschichte, wie hineinpasst. Bei einem Kontext von 32 000 Tokens und Antworten von bis zu 2 000 Tokens bekommt der Prompt 30 000 Tokens. Das Antwortlimit wird außerdem an die API übermittelt (`max_completion_tokens`).

Nutze die Protokolllimits, um Prompts kürzer zu halten, als das Modell erlaubt, etwa um lange Bücher mit einem Cloud-Modell mit großem Kontext günstiger und schneller zu machen, oder um Antworten bei manchen Büchern zu kürzen. Die Protokolllimits gelten auch für [Zusammenfassungen](books/summaries.md) von Büchern mit diesem Protokoll.

Lass beide leer, wenn du sie nicht brauchst: Dann gelten die Werte des Anbieters, und wenn du den Anbieter eines Buchs wechselst, wandern seine Limits mit.

<a id="using-a-protocol-in-a-book"></a>

## Ein Protokoll in einem Buch verwenden

Jedes Buch hat sein Protokoll im Feld **Protokolle** des Reiters **Info**. Um ein Protokoll für alle neuen Bücher zu verwenden, wähle es im Reiter **Einstellungen** als **Standardprotokoll** aus. Ein Buch ohne Protokoll kann nichts generieren und zeigt *Dem Buch fehlt ein Protokoll.* an.

<a id="suggested-settings"></a>

## Empfohlene Einstellungen

Ausgangspunkte, passe sie an dein Modell an:

| Verwendung | Temperatur | Top P | Frequency Penalty | Presence Penalty |
|---|---|---|---|---|
| Ausgewogene Prosa | `0.8` | `0.95` | aus | aus |
| Kreativ, abwechslungsreich | `1.0` | `0.95` | `0.3` | `0.3` |
| Konsistent, nah an den Anweisungen | `0.6` | `0.9` | aus | aus |

Reasoning-Modelle ignorieren Sampling-Parameter häufig oder lehnen sie ab; lass sie bei diesen deaktiviert.
