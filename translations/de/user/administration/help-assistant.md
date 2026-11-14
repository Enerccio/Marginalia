---
label: Hilfeassistent
order: 60
---

# Hilfeassistent

Der Hilfeassistent ist ein Chat, der Fragen zu Marginalia anhand des Handbuchs beantwortet. Ein Administrator wählt aus, welches
Modell er verwendet, und gibt in den [Berechtigungen](permissions.md) den Benutzern, die ihn nutzen dürfen, die Berechtigung **Hilfeassistent**
(Administratoren dürfen ihn immer nutzen); eigene Modelle brauchen die Benutzer dafür nicht. Benutzer, die bereits vorhanden waren, als die Berechtigungen eingeführt wurden, haben sie erhalten, wenn der Assistent zu diesem Zeitpunkt eingeschaltet war; neue Benutzer haben sie nicht.

Sobald ein Benutzer ihn nutzen darf, sieht er unten rechts im Fenster eine runde Roboter-Schaltfläche. Sie öffnet den Chat; das
Panel zeigt an, welches Modell antwortet, und warnt, wenn das Limit des Benutzers gilt (siehe [Wer bezahlt](#who-pays)). Das Modell sucht die Antworten
im Handbuch nach (es kann darin suchen und seine Seiten lesen), nennt die verwendeten Seiten als Links zum
[veröffentlichten Handbuch](https://enerccio.github.io/Marginalia/) (sie öffnen sich in einem neuen Tab) und erklärt: Es
ändert nichts und sieht die Bücher des Benutzers nur, wenn der Benutzer es erlaubt (siehe [Deine Bücher](#your-books)). Es kann auch nachsehen, was schiefgelaufen ist, wenn etwas fehlschlägt (siehe
[Wenn etwas schiefgeht](#when-something-goes-wrong)), und den Benutzer irgendwohin führen, wenn du es erlaubst (siehe
[Den Weg zeigen](#showing-the-way)). Die Unterhaltung bleibt erhalten, bis sich der Benutzer abmeldet (das Schließen des Panels
oder das Neuladen der Seite behält sie), die Besen-Schaltfläche löscht sie. Der Assistent liest das Handbuch in der Sprache der Benutzeroberfläche, wenn das Handbuch
in diese Sprache übersetzt ist, und die Links führen dann zum Handbuch in dieser Sprache; eine nicht übersetzte Seite wird auf Englisch gelesen.

![Der Hilfeassistent beantwortet eine Frage](../../images/help-assistant.png)

Die Schaltfläche erscheint, wenn ein Benutzer Marginalia öffnet; das Modell und die Limit-Warnung werden jedes Mal gelesen, wenn das Panel
geöffnet wird. Wenn du die Berechtigung entziehst oder das Modell entfernst, verliert die Schaltfläche bei Benutzern, die die Seite bereits geöffnet haben, ihre Wirkung, sobald sie die Seite neu laden (oder das Panel öffnen).

## Settings

Öffne **Admin → Hilfeassistent**:

![Der Reiter Hilfeassistent](../../images/admin-help-assistant.png)

| Einstellung | |
|---|---|
| **Modell** | Eines deiner *eigenen* [Modelle](../inference-providers.md), mit den Benutzern geteilt oder nicht. Modelle, die andere Administratoren teilen, stehen nicht in der Liste. |
| **Lesen von Administrationsdaten erlauben** | Erlaubt dem Assistenten, den Zustand der Erweiterungen und deren Protokolle, die Datenbank-Backups und die anderen Daten der Admin-Seite zu lesen, *wenn ein Administrator es verlangt*, siehe [Administrationsdaten](#administration-data). Standardmäßig aus. |
| **Prompt für Administratoren**, **Prompt für andere Benutzer** | Die Anweisungen, die das Modell bei jeder Frage erhält; siehe [Prompts](#prompts). |

Zwei Einstellungen hängen vom Benutzer ab und stehen in dessen **Hilfeassistent**-[Berechtigung](permissions.md#settings-of-a-permission) (öffne sie mit dem
Schlüssel-Symbol unter **Admin → Benutzer**; beide sind standardmäßig aus):

| Einstellung der Berechtigung | |
|---|---|
| **Limits anwenden** | Nur für ein geteiltes Modell, siehe [Wer bezahlt](#who-pays). |
| **Navigation in der Oberfläche erlauben** | Erlaubt dem Assistenten, einen Bereich zu öffnen, einen Reiter auszuwählen oder den Dialog zum Hinzufügen zu öffnen, wenn ein Benutzer ihn bittet, ihm zu zeigen, wo etwas zu finden ist; siehe [Den Weg zeigen](#showing-the-way). |

Ein Administrator ohne solche Berechtigung bekommt Antworten mit beiden Einstellungen auf aus.

Die Einstellungen werden mit **Speichern** gesichert. Der Assistent fragt das Modell ohne [Protokoll](../protocols.md), sodass die Einstellungen des Modells selbst gelten. Er hört von selbst auf zu funktionieren, wenn das Modell gelöscht wird
oder sein Besitzer kein Administrator mehr ist, damit ein Modell, das einem gewöhnlichen Benutzer gehört, niemals allen zur Verfügung gestellt wird.

## What the assistant sees of the screen

Das Modell kann sehen, was der Benutzer gerade tut (es wird darauf hingewiesen, wenn eine Frage sich auf „hier“ oder „das“ bezieht): welcher Bereich, Reiter oder Dialog geöffnet ist, als kurze Beschreibung für jeden Teil des Bildschirms – zum Beispiel der Titel des
geöffneten Buchs und dessen geöffneter Reiter oder der Titel eines geöffneten Dialogs. Reiter, die nicht geöffnet sind, werden nur aufgelistet, und er liest sie nur, wenn die Frage es erfordert. Für ein geöffnetes Formular erhält er außerdem, was daran gerade nicht stimmt: die Felder, die das Formular als ungültig markiert, mit der Meldung, die es anzeigt, die leeren Pflichtfelder und was das Formular feldübergreifend prüft (bei einem Modell: die Antwort muss kleiner als der Kontext sein). Nur die *Namen* der Felder, nie das, was darin eingetippt ist. Er erhält nie den Text einer Geschichte, eines Lorebook-Eintrags, einen API-Schlüssel oder ein Passwort.

![Der Assistent erklärt, warum das Formular eines neuen Modells noch nicht gespeichert werden kann](../../images/help-assistant-form.png)

## Your books

Das Panel des Assistenten hat das Kontrollkästchen **Den Assistenten meine Bücher ansehen lassen**, standardmäßig aus und von jedem Benutzer für sich selbst gesetzt (es wird in dessen Einstellungen gespeichert). Ein Hinweis darunter erklärt, was das bedeutet: Der Assistent liest Ordnernamen, Titel, Beschreibungen und die Schreibeinstellungen der Bücher und durchsucht deren Text, liest aber nie den Text einer Geschichte. Was er liest, wird an die KI geschickt, die antwortet – das ist das Modell, das du oben gewählt hast; sag es den Benutzern, wenn es sich um einen Dienst außerhalb deiner Kontrolle handelt.

![Das Panel des Assistenten mit dem Kontrollkästchen für die Bücher](../../images/help-assistant-books.png)

Ist es eingeschaltet, erhält das Modell drei Funktionen, alle nur lesend und nur für die Bücher des fragenden Benutzers:

| Funktion | |
|---|---|
| `list_folder` | Die Unterordner eines Ordners mit der Anzahl der darin enthaltenen Bücher und seine Bücher mit Titel und Beschreibung. Ein Pfad wie `Sci-fi/Expanse` benennt den Ordner, ohne Angabe ist es die oberste Ebene. |
| `search_books` | Findet Bücher anhand der Wörter von Titel oder Beschreibung oder anhand des Textes der Geschichte, optional innerhalb eines Ordners („Wo ist mein Buch über wahre Liebende?“). Es sagt, wo ein Treffer war und in welchem Ordner; der gefundene Text wird nicht zurückgegeben. |
| `get_book_settings` | Die Sprache, Erzählperspektive, Zeitform und den Stil eines Buchs und woher jeder Wert stammt (das Buch, ein Ordner, die Benutzereinstellungen oder der eingebaute Standard). Die Prompts werden nur als *ein eigener Text* oder *der eingebaute Text* gemeldet, mit Angabe, woher sie stammen. |

Er kann nichts ändern, verschieben oder erstellen. Die Listen werden gekürzt (hundert Einträge, zwanzig Treffer) und die Beschreibungen verkürzt.

## When something goes wrong

Wenn ein Benutzer sagt, dass etwas fehlgeschlagen ist („Ich bekomme einen Fehler“, „es wird nicht gespeichert“, „das Modell antwortet nicht“), kann der
Assistent lesen, was Marginalia dem Benutzer angezeigt hat: die letzten Fehler, die fehlgeschlagenen Anfragen an den Modellanbieter (falscher Schlüssel, Modell nicht vorhanden, Limit erreicht, Zeitüberschreitung ...), die Warnungen und die Meldungen von Formularen, die nicht gespeichert werden konnten, neueste zuerst und mit der Angabe, wie lange sie her sind. Er erklärt, was die Meldung bedeutet und was zu tun ist, und bei einem geöffneten Formular sagt er, welche Felder zu ändern sind.

Was gespeichert wird und wo:

* alles, was einem Benutzer als Fehler oder Warnung in einer Benachrichtigung angezeigt wird, sowie die Meldung einer fehlgeschlagenen Prüfung, wird für die Sitzung aufbewahrt (die letzten 20);
* unerwartete Fehler (die mit einem Dialog für die Stack-Trace-Ausgabe) und fehlgeschlagene Anfragen an einen Anbieter werden außerdem vom Browser-Tab aufbewahrt (die letzten drei, in seinem Sitzungsspeicher), sodass sie auch nach dem Neuladen der Seite oder nach Ablauf der Sitzung noch da sind.

Der Assistent erhält den Text, den der Benutzer gesehen hat, und bei einem Fehler die Namen und Meldungen der Ausnahmen, ohne den Stack-Trace. Jeder Text wird nach 400 Zeichen abgeschnitten, und was wie ein Schlüssel, ein Token oder ein Passwort aussieht, wird entfernt. Der Text einer Meldung kann aber trotzdem einen Teil dessen enthalten, was ein Anbieter oder das System gesagt hat, zum Beispiel die Antwort eines Anbieters auf eine Anfrage. Das antwortende Modell ist das, das du gewählt hast; es kann ein externer Anbieter sein. Behalte das im Blick, wenn du es [auswählst](#settings).
<a id="showing-the-way"></a>

## Den Weg zeigen

Wenn bei der Berechtigung des Benutzers **Navigation in der Oberfläche erlauben** eingeschaltet ist, kann der Assistent im Fenster selbst einige Dinge tun, wenn ein Benutzer ihn darum bittet
(„zeig mir, wo die Lorebooks sind", „bring mich zu den Backups", „wie füge ich ein Modell hinzu?"): einen Bereich öffnen (Books, Lorebooks, Settings, ...), die Admin-Seite
(nur Administratoren haben sie), einen Tab der Settings, der Admin-Seite oder des Story-Editors auswählen oder den Button drücken, der den Dialog zum Hinzufügen eines Buchs, eines Modells oder eines Protokolls öffnet (der Dialog wird nur geöffnet; nichts wird angelegt, bis der Benutzer ihn ausfüllt und bestätigt; er wird nicht angeboten, solange ein anderer Dialog offen ist). Der Assistent teilt dem Benutzer mit, was er getan hat.
Mehr kann er nicht: Er drückt keine anderen Buttons, tippt nichts, speichert nicht, löscht nichts, generiert nichts, schließt keinen Dialog und meldet niemanden ab. Ein Benutzer, der etwas nicht öffnen kann, kann auch nicht erreichen, dass der Assistent es für ihn öffnet. Er bewegt sich nicht von sich aus, sondern nur, wenn er darum gebeten wird.
Wo die Navigation gesperrt ist (der geöffnete Bereich hat ungespeicherte Änderungen, ein Buch wird gerade generiert), kann er sich nicht bewegen.

![Der Assistent hat auf Anfrage den Tab Database Backups geöffnet](../../images/help-assistant-navigate.png)

Wenn diese Option eingeschaltet ist, erfährt das Modell außerdem die Namen der Elemente auf dem Bildschirm und was sie anbieten. Das ist dieselbe Art von Information wie unter [Was der Assistent vom Bildschirm sieht](#what-the-assistant-sees-of-the-screen).

<a id="administration-data"></a>

## Administrationsdaten

Wenn **Lesen von Administrationsdaten erlauben** eingeschaltet ist, kann der Assistent die Daten der Admin-Seite lesen, und zwar für einen Administrator, der danach fragt
(„warum funktioniert Side Query nicht?", „wann war das letzte Backup?"), und für niemanden sonst. Ein normaler Benutzer bekommt sie nie, egal, was die Einstellung sagt, und bei jedem Lesen wird das erneut geprüft.

| Thema | Was gelesen wird |
|---|---|
| Extensions | Jede installierte [Erweiterung](../extensions/index.md) mit ihrer Version, ob sie aktiv ist, ob du sie angehalten hast oder ob sie nicht starten konnte, und das Ergebnis ihrer Prüfung |
| Das Log einer Erweiterung | Die letzten Zeilen ihres Logs (standardmäßig 40, höchstens 200), dasselbe wie **Admin → Extensions → Log** |
| Users | Wie viele Benutzer es gibt, wie viele Administratoren und wie viele gesperrt sind; keine Namen |
| Database Backups | Die Größe der Datenbank, wie viele Backups es gibt und wann das neueste erstellt wurde, der Zeitplan und ob eine Wiederherstellung wartet |
| Bereinigung | Was eine [Bereinigung](cleanup.md) entfernen würde (es wird nichts entfernt) und was sie blockiert |
| Application | Die Java-Version, der belegte Speicher und wie lange die Anwendung schon läuft |

Die Daten sind nur lesbar: Der Assistent kann dort nichts starten, anhalten oder entfernen. Bedenke, dass alles davon an das Modell geschickt wird, das antwortet, und das kann ein externer Anbieter sein. Außerdem kann das Log einer Erweiterung Text enthalten, den die Erweiterung geschrieben hat. Eine Logzeile wird nach 400 Zeichen abgeschnitten, und Schlüssel, Tokens und Passwörter werden entfernt, mehr wird damit nicht gemacht. Deshalb ist die Einstellung ausgeschaltet, bis du sie einschaltest.

## Prompts

Es gibt zwei Prompts: einen für Administratoren (die nach der Admin-Seite fragen dürfen) und einen für alle anderen (denen mitgeteilt wird, dass die Administration Administratoren vorbehalten ist). Beide sind mit den Standardwerten ausgefüllt. Wenn du den Text änderst und speicherst, stellt **Reset to default** den Standard wieder her. Ein Prompt, der leer ist oder dem Standard entspricht, wird als „der Standard" gespeichert, sodass er sich mit einer neuen Version von Marginalia verbessert. Einen Prompt, den du geändert hast, behältst du selbst und musst ihn selbst aktuell halten.

`{{manualIndex}}` in einem Prompt wird durch die Liste der Seiten des Handbuchs ersetzt (Pfad, Titel und worum es auf der Seite geht). So erfährt das Modell, was es lesen kann. Lässt du es weg, findet das Modell Seiten nur über die Suche. Lass die Anweisungen drin, im Handbuch nachzuschlagen und zu sagen, wenn es eine Frage nicht abdeckt, sonst werden die Antworten unzuverlässiger. Die Regeln für die Werkzeuge, die Fehler lesen, durch die Oberfläche navigieren und die Administrationsdaten lesen, fügt Marginalia selbst dem Prompt hinzu (du musst sie nicht darin halten), und zwar nur für die Werkzeuge, die erlaubt sind.

<a id="who-pays"></a>

## Wer zahlt

Die Benutzer sehen das Modell und seinen API-Schlüssel nie; ihre Fragen laufen über die Anwendung.

| Modell | **Limits anwenden** (Berechtigung des Benutzers) | Was passiert |
|---|---|---|
| Geteilt | an | Eine Frage ist eine Anfrage wie jede andere an das geteilte Modell: Das [Limit](../inference-providers.md#limits-of-shared-providers), das du für den Benutzer eingestellt hast, wird geprüft, und die Tokens werden zur Nutzung des Benutzers gezählt. Ein Benutzer, dessen Limit erreicht ist (oder der das Modell nicht verwenden darf), bekommt keine Antwort. |
| Geteilt | aus | Der Eigentümer des Modells zahlt. Das Limit des Benutzers wird ignoriert, und für den Benutzer wird nichts gezählt. |
| Nicht geteilt | (keine Wirkung) | Der Eigentümer des Modells zahlt: kein Limit, und für den Benutzer wird nichts gezählt. Das Modell wird nur vom Assistenten genutzt. |

Deine eigenen Fragen werden genauso gezählt. Für dich ist das Modell einfach ein weiteres Modell.
