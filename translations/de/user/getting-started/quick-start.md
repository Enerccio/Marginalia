---
label: Schnellstart
order: 70
---

# Schnellstart

Von einer frischen Installation bis zum ersten generierten Teil einer Geschichte. Voraussetzung ist, dass Marginalia
[installiert](index.md) ist und du als [Administrator](first-start.md) angemeldet bist.

<a id="1-connect-a-model"></a>

## 1. Ein Modell verbinden

1. Öffne den Tab **Inferenzanbieter** und klicke auf **Inferenzanbieter hinzufügen**.
2. Gib einen **Name** ein, zum Beispiel `OpenRouter - Claude`.
3. Setze unter **Allgemeine Einstellungen** die **Maximale Kontextgröße** auf die Kontextgröße des Modells, zum Beispiel `32000`, und
   die **Maximale Antwort-Tokens** auf den längsten Teil, den du haben möchtest, zum Beispiel `2000`.
4. Trage unter **Einstellungen je Typ** die **OpenAI URL** (sie endet auf `/v1`) und den **API-Schlüssel** ein (lass ihn leer für einen lokalen Server ohne Schlüssel), klicke auf **Aktualisieren** und
   wähle das **Modell** aus.
5. Klicke auf **OK**.

Die Felder werden unter [Inferenzanbieter](../inference-providers.md) beschrieben, mit URLs für gängige Dienste.

<a id="2-create-a-protocol"></a>

## 2. Ein Protokoll anlegen

1. Öffne den Tab **Protokolle** und klicke auf **Protokoll hinzufügen**.
2. Gib einen **Name** ein, zum Beispiel `Default`.
3. Lass **Maximale Kontext-Tokens** und **Maximale Antwort-Tokens** leer - dann werden die Grenzen des Inferenzanbieters verwendet.
4. Klicke auf **OK**.

Siehe [Protokolle](../protocols.md).

<a id="3-set-the-defaults"></a>

## 3. Die Standardwerte festlegen

Öffne den Tab **Einstellungen** und wähle den neuen Anbieter als **Standardmodell** und das neue Protokoll als
**Standardprotokoll**. Klicke auf **Speichern**. Neue Bücher starten dann mit beidem, sodass du es nicht jedes Mal auswählen musst.

![Standardmodell und Protokoll in den Einstellungen](../../images/settings-defaults.png)

<a id="4-create-a-book"></a>

## 4. Ein Buch anlegen

1. Öffne den Tab **Bücher** und klicke auf **Buch hinzufügen**.
2. Gib den Namen des Buchs ein. Das Buch öffnet sich.
3. Prüfe auf dem Tab **Info**, dass **Modell** und **Protokolle** gesetzt sind (sie stammen aus deinen Standardwerten), und trage bei Bedarf eine
   **Beschreibung** der Geschichte ein.

<a id="5-write-the-first-part"></a>

## 5. Den ersten Teil schreiben

1. Öffne den Tab **Geschichte**.
2. Klicke unten rechts auf **+**. Das Panel *Anweisungen für den neuen Zug* öffnet sich.
3. Beschreibe unter **Anweisungen**, was passieren soll, zum Beispiel:
   > Eine Leuchtturmwärterin auf einer abgelegenen Insel findet eine Flaschenpost, die an sie adressiert ist und auf
   > fünfzig Jahre in der Zukunft datiert ist. Stelle sie und die Insel vor. Etwa 800 Wörter.

   *Schauplatz der Szene*, *POV-Figur* und *Anwesende Figuren* sind optional; fülle sie aus, um dem Modell mehr Richtung zu geben.
4. Klicke auf **Generieren**. Der Text erscheint, während das Modell ihn schreibt. Die rote Schaltfläche **Stopp** (an der Stelle von **+**)
   unterbricht die Generierung.

![Der erste generierte Teil](../../images/quick-start-first-part.png)

Klicke für den nächsten Teil erneut auf **+**. Jeder Teil lässt sich über sein Menü (☰) bearbeiten, neu generieren oder für eine alternative Version wischen; siehe [Geschichtseditor](../books/story-editor.md) und
[Verzweigungen & Story-Baum](../books/branches-and-story-tree.md).

<a id="next-steps"></a>

## Nächste Schritte

- Beschreibe deine Welt und Figuren in einem [Lorebook](../lorebooks/index.md).
- Passe Stil, Erzählperspektive und Zeitform in den [Prompts](../books/prompts.md) des Buchs an.
- [Fasse](../books/summaries.md) ältere Teile zusammen, während das Buch wächst.
- Richte [Buchsicherungen](../books/backups.md) ein.
