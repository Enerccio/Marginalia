---
label: Einträge und Aktivierung
order: 90
---

# Einträge und Aktivierung

<a id="the-entry-list"></a>

## Die Eintragsliste

Jede Zeile des Lorebook-Editors ist ein Eintrag:

| Spalte | |
|---|---|
| Kontrollkästchen | **Aktiviert**. Deaktivierte Einträge werden nie verwendet. |
| **Eintragsname** | Dein Name für den Eintrag. Wird nicht an das Modell gesendet. |
| **Reihenfolge** | Position des Eintrags im Prompt, siehe [Reihenfolge und Einfügung](#order-and-insertion). Neue Einträge erhalten `100`. |
| **Tags** | Tags, die den Eintrag aktivieren, siehe [Tags](#tags). |
| ⌄ | Zeigt die Details des Eintrags. |
| Erweitern | Öffnet den Eintrag im [Vollbild-Editor](lorebooks.md#editing-in-full-screen) (Desktop). |
| Papierkorb | Löscht den Eintrag (nach Bestätigung). |

**Eintrag hinzufügen** in der Werkzeugleiste fügt einen Eintrag mit der Reihenfolge `100` hinzu. Änderungen werden beim Tippen gespeichert.

<a id="searching-and-deleting-many-entries"></a>

### Viele Einträge suchen und löschen

Das Feld mit der Lupe neben den Schaltflächen zeigt nur die Einträge, die den Text in ihrem Namen, Inhalt, ihrer Notiz oder ihrem Filter haben (Groß- und Kleinschreibung spielt keine Rolle); leere es, um wieder alle zu sehen. Es betrachtet nur die Einträge des ausgewählten Lorebooks, nicht die Unter-Lorebooks. Das Telefon-Layout zeigt das Feld über der Liste.

Neben dem Feld gibt es zwei weitere Felder, die die Liste einschränken. **Tags** zeigt die Einträge mit dem Tag (es listet die Tags auf, die mindestens ein Eintrag des Lorebooks hat; die *negativen* Tags werden nicht berücksichtigt). **Status** zeigt **Alle**, **Aktiviert**, **Deaktiviert** oder **Konstant (keine Schlüssel)** - die Einträge, die aktiviert sind und keinen Filter haben, also für jeden Abschnitt verwendet werden. Suche, Tag und Status werden kombiniert: Ein Eintrag muss alle erfüllen. Das Telefon-Layout zeigt die beiden Felder unter dem Suchfeld.

Die Kontrollkästchen-Spalte links wählt Einträge aus (das Kästchen in der Kopfzeile wählt alle angezeigten aus, sodass eine Suche und danach das Kopfzeilenkästchen die Treffer auswählt). Wenn etwas ausgewählt ist, erscheint **Ausgewählte löschen** in der Werkzeugleiste mit der Anzahl der Einträge; es fragt einmal nach und verschiebt sie dann in den [Papierkorb](../trash.md). Die [Massenbearbeitung](lorebooks.md#mass-edit) hat ebenfalls eine Schaltfläche **Markierte löschen**. Auf dem Telefon erscheint im **⋮**-Menü der Einträge **Einträge auswählen**: Vor den Einträgen erscheinen Kontrollkästchen, und **Ausgewählte löschen** erscheint mit der Anzahl der markierten; ein Eintrag hat dort außerdem eine eigene Schaltfläche **Eintrag löschen**.


<a id="search-and-replace"></a>

## Suchen und Ersetzen

**Suchen und Ersetzen** (rechts neben **Eintrag hinzufügen**) ändert ein Wort, einen Namen oder eine Phrase im Namen, Text und Kommentar aller Einträge des Lorebooks auf einmal. Es funktioniert wie das [Suchen und Ersetzen der Geschichte](../books/story-editor.md#search-and-replace), ohne die Optionen für Zusammenfassungen und Verzweigungen: **Suchen** listet die Einträge mit Treffern auf (die Tabelle zeigt ihre Namen) sowie die Treffer darin, wie sie sind und wie sie sein werden, **Ersetzen** ändert die Einträge. Es hat die Optionen **Per Regex suchen** (siehe [Reguläre Ausdrücke](../regular-expressions.md)) und **Groß-/Kleinschreibung beachten** (standardmäßig aktiv; gesperrt, solange Regex eingeschaltet ist, wobei `(?i)` verwendet werden kann, um die Groß-/Kleinschreibung zu ignorieren). Deaktivierte Einträge werden ebenfalls durchsucht. Auf dem Telefon öffnet die Schaltfläche (ein Symbol neben dem Kontrollkästchen *Aktiviert*) dasselbe Fenster im Telefon-Layout.

![Das Fenster Suchen und Ersetzen eines Lorebooks, die Treffer in einem Eintrag geöffnet](../../images/lorebook-search-replace.png)

Die Befehlszeile des Geschichte-Editors hat `/search-replace-lore-entries` für das Lorebook des geöffneten Buchs, siehe [Befehle](../commands/index.md#commands-that-come-with-marginalia).

<a id="entry-details"></a>

## Eintragsdetails

Die Schaltfläche ⌄ öffnet die Details:

![Ein Eintrag mit seinen Details](../../images/lorebook-entry.png)

| Feld | |
|---|---|
| **Inhalt** | Der Text, der in den Prompt geht. Eine [Vorlage](#templates-in-entries): Sie kann Variablen und Makros verwenden. |
| **Filtermodus** | **Schlüsselwörter (beliebiges)**, **Schlüsselwörter (alle)**, **Text** oder **Regex**, legt fest, wie *Filtertext/-muster* abgeglichen wird. |
| **Filtertext/-muster** | Der Filter, siehe [Filter](#filters). Leer bedeutet kein Filter. |
| **Einfügemodus** | **Im Lore-Block** oder **Vor dem Benutzerprompt**, siehe [Reihenfolge und Einfügung](#order-and-insertion). |
| **Aktivierungskontext** | Wo nach dem Filter gesucht wird: **Anweisungen** (die Anfrage für den Abschnitt, der Standard) oder **Anweisungen und Abschnitte der Geschichte**, siehe [Blick in die Geschichte](#looking-into-the-story). |
| **Abschnitte** | Bei *Anweisungen und Abschnitte der Geschichte*: wie viele der neuesten Abschnitte der Geschichte zusammen mit der Anfrage durchsucht werden, mindestens 1. |
| **Ab Nachricht** | Der Eintrag wird erst ab dem Abschnitt mit dieser Nummer verwendet, siehe [Einen Eintrag auf einige Abschnitte beschränken](#limiting-an-entry-to-some-parts). Leer: von Anfang an. |
| **Bis Nachricht** | Der Eintrag wird nur bis zum Abschnitt mit dieser Nummer verwendet. Leer: bis zum Ende. |
| **Bedingung** | Eine Vorlage, die einen wahren Wert liefern muss, damit der Eintrag verwendet wird, siehe [Eine Bedingung für einen Eintrag](#a-condition-for-an-entry). Leer: keine Bedingung. |
| **Negative Tags (Ausschluss)** | Tags, die den Eintrag ausschalten, siehe [Tags](#tags). |
| **Auch die Zusammenfassungen durchsuchen** | Wenn angehakt, wird der Filter auch in den [Zusammenfassungen](../books/summaries.md) der bisherigen Geschichte gesucht, und zwar in denen, die die nächste Anfrage senden würde (eine Zusammenfassung, die nicht mehr zu ihrer Geschichte passt, wird ausgelassen). Benötigt einen Filter. |
| **Den Inhalt nicht nach anderen Einträgen durchsuchen** | Wenn angehakt, bringen die Wörter in diesem Eintragsinhalt nie andere Einträge ins Spiel, siehe [Einträge, die durch andere Einträge hinzukommen](#entries-brought-in-by-other-entries). |
| **Nur über andere Einträge** | Wenn angehakt, wird der Eintrag nie wegen der Anfrage oder der Geschichte selbst verwendet, sondern nur, wenn sein Filter im Inhalt eines anderen verwendeten Eintrags gefunden wird, siehe [Einträge, die durch andere Einträge hinzukommen](#entries-brought-in-by-other-entries). |
| **Notiz** | Deine Notizen. Werden nicht an das Modell gesendet. Aus SillyTavern importierte Einträge listen hier die Einstellungen auf, die nicht importiert werden konnten. |

Schreibe den Inhalt so, wie das Modell ihn lesen soll – eine kurze, sachliche Beschreibung funktioniert am besten:

> **Hauptmann Varro** - Kommandant der Hafenwache von Ostra, Mitte fünfzig, vernarbte linke Hand. Treu dem Herzog ergeben, misstraut
> der Gilde der Magier. Spricht in kurzen, abgehackten Sätzen.
<a id="activation"></a>

## Aktivierung

Vor jedem generierten Abschnitt geht Marginalia die Einträge des Lorebooks des Buchs und seiner
[Unter-Lorebooks](lorebooks.md#sub-lorebooks) durch. Ein Eintrag wird **aktiviert**, wenn:

1. der Eintrag aktiviert ist und sein Lorebook ebenfalls;
2. der gerade geschriebene Abschnitt innerhalb seines Abschnittsbereichs liegt, falls er einen hat (**Ab Nachricht**, **Bis Nachricht**);
3. keines seiner **Ausschluss-Tags** ein Tag des Buchs ist;
4. er keine **Tags** hat oder mindestens eines davon ein Tag des Buchs ist;
5. seine **Bedingung**, falls vorhanden, wahr ist, siehe [Eine Bedingung für einen Eintrag](#a-condition-for-an-entry);
6. er keinen **Filter** hat oder der Filter zu den Anweisungen für den Abschnitt passt (oder, bei **Aktivierungskontext** *Anweisungen und Abschnitte der Geschichte*, zu den Anweisungen und den neuesten Abschnitten der Geschichte).

Alle Bedingungen müssen erfüllt sein. Ein Eintrag ohne Tags und Filter ist immer aktiv.

### Tags

Tags verbinden Einträge mit **Buch-Tags** (festgelegt im Reiter [Info](../books/managing-books.md#the-about-tab) des Buchs).
Damit teilen sich mehrere Bücher ein Lorebook, obwohl sie unterschiedliche Teile davon brauchen:

| Eintrags-Tags | Ausschluss-Tags | Aktiv in einem Buch mit dem Tag... |
|---|---|---|
| - | - | immer |
| `winter` | - | `winter` |
| `winter`, `book-2` | - | `winter` **oder** `book-2` |
| - | `book-1` | allen außer `book-1` |
| `winter` | `book-1` | `winter`, sofern es nicht zugleich `book-1` ist |

Tags müssen genau übereinstimmen. Ein Ausschluss-Tag hat immer Vorrang vor einem Tag.

Tags lassen sich auch einem ganzen Lorebook geben (das Feld **Tags** des [Lorebooks](lorebooks.md); aus Dateien importierte Lorebooks
können sie mitbringen). Sie zählen dann als Tags jedes seiner Einträge.

<a id="filters"></a>

### Filter

Ein Filter aktiviert den Eintrag nur, wenn die **Anfrage für den Abschnitt** ihn erwähnt. Er wird mit dem
[Benutzer-Prompt](../books/prompts.md#user-prompt) des Abschnitts abgeglichen, so wie er gesendet wird - also mit dem
*Schauplatz der Szene*, der *POV-Figur*, den *Anwesenden Figuren* und den *Anweisungen*, die Sie eingegeben haben, innerhalb des Wortlauts der Benutzer-Prompt-Vorlage.

| Modus | Passt, wenn |
|---|---|
| **Schlüsselwörter (beliebiges)** | Der Filter ist eine durch Kommas getrennte Liste von Schlüsselwörtern (`Varro, Ostra`); die Anfrage enthält mindestens eines davon, ohne Berücksichtigung der Groß- und Kleinschreibung. Leerzeichen um ein Schlüsselwort werden ignoriert. |
| **Schlüsselwörter (alle)** | Dieselbe Liste, doch die Anfrage muss jedes Schlüsselwort enthalten. |
| **Text** | Der Text kommt irgendwo in der Anfrage vor, ohne Berücksichtigung der Groß- und Kleinschreibung. `varro` passt zu *Hauptmann Varro*. |
| **Regex** | Der [reguläre Ausdruck](../regular-expressions.md#in-a-lorebook-entry-filter) trifft irgendwo in der Anfrage zu, ohne Berücksichtigung der Groß- und Kleinschreibung, über Zeilen hinweg. |

Zum Beispiel trifft der Regex `\b(varro|captain)\b` das ganze Wort *Varro* oder *Captain*, und
`varro.*(harbor|docks)` passt nur, wenn Varro und der Hafen oder die Docks beide erwähnt werden, in dieser Reihenfolge.

!!!warning Standardmäßig sieht der Filter die Anfrage, nicht die Geschichte
Sofern der Eintrag nicht in die Geschichte hineinschaut (siehe [In die Geschichte schauen](#looking-into-the-story)), prüft ein Filter nur die Anweisungen für den
neuen Abschnitt, nicht den Text früherer Abschnitte. Ein Eintrag über Varro
wird aktiviert, wenn Varro in der Szene, bei den Figuren oder in den Anweisungen des Abschnitts erwähnt wird, den Sie gerade schreiben - nehmen Sie ihn
in *Anwesende Figuren* auf, wenn er in der Szene ist.

Die Anfrage enthält auch den Text der Benutzer-Prompt-Vorlage selbst. Ein Filter für ein Wort, das die Vorlage verwendet
(die Standardvorlage nennt *Figuren*, *Handlung*, *Ton*...), ist immer aktiv.
!!!

Ein ungültiger regulärer Ausdruck trifft nie zu; der Eintrag bleibt inaktiv.

Kombinieren Sie Tags und einen Filter, wenn ein Eintrag nur in manchen Büchern und dort nur bei Bedarf verwendet werden soll.

<a id="looking-into-the-story"></a>

### In die Geschichte schauen

Standardmäßig wird der Filter nur in der Anfrage für den Abschnitt gesucht, sodass ein Eintrag über den Drachen verwendet wird, wenn *Sie* den Drachen erwähnen. Soll die Geschichte den Eintrag im Spiel halten - der Drache war im letzten Abschnitt, Ihre neue Anweisung
lautet nur „er fliegt davon“ -, stellen Sie den **Aktivierungskontext** des Eintrags auf **Anweisungen und Abschnitte der Geschichte** und **Abschnitte** auf die Nummer der neuesten Abschnitte, die zusammen mit der Anfrage durchsucht werden (`1` ist der Abschnitt vor dem gerade geschriebenen, `2` die beiden davor usw.).

- Der Filter wird im Text dieser Abschnitte und in der Anfrage zusammen gesucht, sodass Schlüsselwörter von **Schlüsselwörter (alle)** auf sie verteilt sein dürfen. Die Anfrage zählt immer mit.
- Durchsucht wird nur der Text der Abschnitte, nicht deren Zusammenfassungen, und nur die Abschnitte des Zweigs, den Sie gerade schreiben (ein Swipe oder eine Neugenerierung durchsucht nicht den Abschnitt, den sie ersetzt).
- Eine Geschichte, die kürzer ist als **Abschnitte**, wird ganz durchsucht. Marginalia liest nur so viele Abschnitte, wie die eingeschalteten Einträge brauchen,
  sodass ein Lorebook ohne solche Einträge die Geschichte gar nicht anfasst.
- Einträge, die durch [andere Einträge](#entries-brought-in-by-other-entries) hineingeholt werden, werden nicht in der Geschichte gesucht, nur im
  Inhalt der verwendeten Einträge.

<a id="limiting-an-entry-to-some-parts"></a>

### Einen Eintrag auf einige Abschnitte beschränken

Manches Wissen gilt nur für einen Teil der Geschichte: die Verkleidung einer Figur, die in Kapitel 5 auffliegt, oder die Gerüchte
vor Kriegsbeginn. Tragen Sie in **Ab Nachricht** und/oder **Bis Nachricht** die Nummern der Abschnitte ein, so wie der Gliederungsbaum der Geschichte sie zählt (der erste Abschnitt ist `1`; beide Zahlen sind eingeschlossen). Der Eintrag wird dann nur verwendet, wenn der gerade geschriebene Abschnitt eine Nummer innerhalb des Bereichs hat, unabhängig davon, was sein Filter sagt - ein Eintrag ohne Filter ist nur innerhalb des Bereichs ein konstanter Eintrag.

- Ein leeres Feld bedeutet keine Begrenzung: nur **Ab Nachricht** `10` heißt „ab Abschnitt 10“, nur **Bis Nachricht** `4` heißt „bis Abschnitt 4“.
- Der gerade geschriebene Abschnitt ist der nach dem letzten Abschnitt des Zweigs (eine Geschichte mit 9 Abschnitten schreibt Abschnitt `10`). Ein Swipe oder eine Neugenerierung schreibt den Abschnitt, den sie ersetzt.
- Abschnitte ohne Text zählen mit, wie in der Gliederung. Löschen oder Einfügen von Abschnitten verschiebt die Nummern.
- Ist **Ab Nachricht** größer als **Bis Nachricht**, wird der Eintrag nie verwendet.
- Ein Eintrag außerhalb seines Bereichs wird auch nicht durch [andere Einträge](#entries-brought-in-by-other-entries) hineingeholt.
- Die Erweiterung [Lorebook-VCS](../extensions/index.md) behält den Bereich in den Versionen eines Eintrags; Import, Export und Klonen eines Lorebooks behalten ihn ebenfalls.

<a id="a-condition-for-an-entry"></a>

### Eine Bedingung für einen Eintrag

Die **Bedingung** ist eine Vorlage, geschrieben wie der [Inhalt eines Eintrags](#templates-in-entries), die Marginalia vor jedem
Abschnitt auswertet. Der Eintrag wird nur verwendet, wenn das Ergebnis **wahr** ist: alles außer einem leeren Ergebnis, `0`, `false`, `no` und `off`
(in beliebiger Schreibweise). Sie sieht dasselbe wie der Inhalt: die POV-Figur, die anwesenden Figuren, den Schauplatz, die
Anweisungen des Zuges, die bisherige Geschichte und die Variablen.

![Ein Eintrag mit Bedingung: Der Eintrag wird nur verwendet, wenn Tomáš die POV-Figur ist und Mizuki anwesend ist](../../images/lorebook-condition.png)

| Bedingung | Der Eintrag wird verwendet, wenn |
|---|---|
| `{%{{{#if presentCharacters}}yes{{/if}}}%}` | der Zug anwesende Figuren hat |
| `{%{{{eval::povCharacter == "Fiora"}}}%}` | die POV-Figur Fiora ist (siehe [Ausdrücke](../templates/macros.md#expressions)) |
| `{%{{{eval::povCharacter == "Fiora" and presentCharacters has "Mara"}}}%}` | Fiora die POV-Figur ist und Mara anwesend ist |
| `{%{{{getvar::revealed}}}%}` | die Variable `revealed` einen Wert hat (gesetzt durch einen früheren Abschnitt, siehe [Makro-Referenz](../templates/macros.md#variables)) |
| `{%{{{command::authors-note}}}%}` | der [Befehl](../commands/index.md) etwas zurückgibt (nur ein Befehl, der für Makros gedacht ist, kann verwendet werden) |

So funktioniert es:

- Die Prüfung erfolgt nach dem Schalter für aktiviert, dem Abschnittsbereich und den Tags und **vor dem Filter**: Ein Eintrag, dessen Bedingung falsch ist,
  wird nicht verwendet, selbst wenn sein Filter passt, und auch nicht durch [andere Einträge](#entries-brought-in-by-other-entries) hineingeholt.
- Eine Bedingung, die **nicht entschieden werden kann**, gilt als falsch, und die [Vorschau](#preview-activation) sagt warum: Sie ist keine gültige Vorlage,
  sie verwendet einen Namen, den es nicht gibt (ein Tippfehler), einen Befehl, den es nicht gibt oder der nicht für Makros gedacht ist, oder die Zeit ist abgelaufen.
  Das Feld wird bei den ersten beiden Fehlern während des Tippens rot.
- Sie liest nur. Ein `{%{{{setvar}}}%}` in einer Bedingung ändert nichts, weder für den Prompt noch für die anderen Einträge, und die Befehle sind
  nur die, die nur lesen.
- Sie wird für jeden Eintrag und Abschnitt einmal ausgewertet, sodass `{%{{{random::yes::no}}}%}` beim ersten Durchlauf und bei den
  Durchläufen der Scan-Tiefe nicht seine Meinung ändert.
- Die [Vorschau](#preview-activation) nennt die Einträge, deren Bedingung falsch ist: *Nicht verwendet: Bedingung ist falsch*.

  ![Die Vorschau der Aktivierung: Der Eintrag von Mizuki wird nicht verwendet, weil seine Bedingung falsch ist](../../images/lorebook-condition-preview.png)
- Alle Bedingungen eines Abschnitts teilen sich **60 Sekunden**. Ist diese Zeit aufgebraucht, gelten die verbleibenden Bedingungen als falsch
  (*Bedingung fehlgeschlagen*, Zeitüberschreitung). Eine laufende Bedingung wird nicht abgebrochen; die Zeit wird vor der nächsten geprüft.
- Import und Export von Lorebooks, Klonen und die Erweiterung Lorebook-VCS behalten die Bedingung.

<a id="entries-brought-in-by-other-entries"></a>

### Einträge, die durch andere Einträge hineingeholt werden

Normalerweise aktiviert nur die Anfrage für den Abschnitt Einträge. Mit einer **Scan-Tiefe** (das Feld des
[Lorebooks](lorebooks.md#the-lorebook-editor); `0`, der Standardwert, schaltet dies aus) wird der Inhalt der aktivierten Einträge nach den Filtern der nicht aktivierten Einträge durchsucht, als wäre er Teil der Anfrage. Ein Eintrag, dessen Filter dort gefunden wird, wird ebenfalls aktiviert. Das ist **Durchlauf 1**; der Inhalt der Einträge, die er aktiviert hat, wird im Durchlauf 2 durchsucht, und so weiter, so viele Durchläufe, wie die Scan-Tiefe angibt (höchstens 5).

Zum Beispiel: Bei einer Scan-Tiefe von `1` wird ein Eintrag *Hauptmann Varro* (Filter `varro`) durch die Anfrage aktiviert, und sein Inhalt
sagt „...dient dem Herzog von Ostra“. Ein Eintrag *Ostra* (Filter `ostra`) wird dann ebenfalls aktiviert, obwohl die Anfrage die Stadt nie erwähnt. Bei einer Scan-Tiefe von `2` würde der Eintrag *Ostra* wiederum einen Eintrag über den Hafen hineinholen, den er erwähnt.

- Nur **Filter** zählen: Ein Eintrag, der bereits verwendet wird, weil er keinen Filter hat, braucht nichts, und ein Eintrag, der ausgeschaltet ist oder durch seine Tags ausgeschlossen wird, wird nicht hineingeholt.
- Durchsucht wird der Text des Eintrags, so wie er geschrieben ist; die Vorlagen darin werden nicht ausgeführt.
- Die **Scan-Tiefe** des Lorebooks, das das **Buch verwendet**, zählt für seine eigenen Einträge und für die Einträge seiner Unter-Lorebooks. Ein Unter-Lorebook kann eine eigene Tiefe haben (**Scan-Tiefe als Unter-Lorebook** in seinen Einstellungen; leer: die des Lorebooks, das das Buch verwendet): Seine Einträge werden dann durch den Inhalt anderer nur in den Durchläufen bis zu dieser Tiefe gefunden, `0` nie. Die Anzahl der Durchläufe richtet sich nach der größten Tiefe. Das Feld eines Unter-Lorebooks zählt nur, solange es ein Unter-Lorebook ist; verwendet ein Buch es direkt, zählt dessen normale **Scan-Tiefe**.
- Ein Eintrag mit angehakter Option **Den Inhalt nicht nach anderen Einträgen durchsuchen** kann aktiviert werden, holt aber niemanden hinein.
- Ein Eintrag mit angehakter Option **Nur über andere Einträge** wird nie durch die Anfrage gefunden: Er wird nur verwendet, wenn der Inhalt eines verwendeten Eintrags seinen Filter enthält. Ohne Scan-Tiefe oder ohne Filter wird er überhaupt nie verwendet; die [Vorschau](#preview-activation) sagt dann *Nicht verwendet: nur über den Inhalt anderer Einträge verwendet*.
- Die Reihenfolge im Prompt hängt nicht davon ab, wie ein Eintrag gefunden wurde, siehe [Reihenfolge und Einfügung](#order-and-insertion).
- Erweiterungen, die ändern, welche Einträge verwendet werden (siehe [Erweiterungen](../extensions/index.md)), werden auch zu den hineingeholten Einträgen befragt.
<a id="preview-activation"></a>

## Vorschau der Aktivierung

**Vorschau der Aktivierung** (die Schaltfläche mit dem Auge in der Werkzeugleiste des Lorebook-Editors und auf dem Telefon neben der Suchen-und-Ersetzen-Schaltfläche) beantwortet die Frage „Welche Einträge würden für diesen Text verwendet?“, ohne etwas zu generieren. Gib den Text der Anfrage in **Text des Zugs** ein (die Szene, die Figuren und die Anweisungen, so wie du sie auch sonst eingeben würdest). Die Tabelle listet dann jeden Eintrag des Lorebooks und seiner Unter-Lorebooks in der Reihenfolge auf, in der sie verwendet werden:

- ein grünes Häkchen für die Einträge, die verwendet würden, und eine graue Markierung für die übrigen;
- das Lorebook des Eintrags, seine **Reihenfolge**, den **Durchgang** (leer für die Einträge, die durch den Text aktiviert wurden, `1`, `2` ... für die Einträge, die durch [andere Einträge](#entries-brought-in-by-other-entries) hinzugezogen wurden);
- den **Grund**: kein Filter, Filter im Text gefunden, Filter im Inhalt eines anderen Eintrags gefunden (mit dem Filter und dem Eintrag), oder warum nicht: ausgeschaltet, der Teil liegt außerhalb seines Bereichs, ein negatives Tag des Buchs, dem Buch fehlt ein Tag des Eintrags, die Bedingung ist falsch oder konnte nicht entschieden werden, der Filter wurde nicht gefunden.

Auch die Tags entscheiden mit, daher wähle unter **Tags des Buchs** ein Buch aus, um dessen Tags zu verwenden (ohne Auswahl zählt kein Tag). Dieses Buch legt außerdem die Teile seiner Geschichte für die Einträge fest, die [in die Geschichte hineinschauen](#looking-into-the-story) (ohne Buch durchsuchen sie nur den Text), sowie die Nummer des nächsten Teils für die Einträge, die [auf einige Teile beschränkt](#limiting-an-entry-to-some-parts) sind (ohne Buch wird ihr Bereich nicht geprüft). Der Text, den du eingibst, dient als Anweisungen des Zugs für die [Bedingungen](#a-condition-for-an-entry); mit einem Buch werden zusätzlich dessen Variablen und die Erzählperspektive, die anwesenden Figuren und die Szene des letzten Teils verwendet (die Befehle einer Bedingung werden in der Vorschau nicht ausgeführt). Unter dem Text steht eine Zeile, die angibt, wie viele Einträge verwendet würden und wie viele Tokens an Lore das ungefähr sind. Die Scantiefe des Lorebooks wird verwendet.

Im [Vollbild-Editor](lorebooks.md#editing-in-full-screen) befindet sich das Auge außerdem in der Namenszeile jedes Eintrag-Tabs: Die Vorschau öffnet sich für diesen Eintrag, seine Zeile wird ausgewählt, und eine Zeile über der Tabelle sagt, ob er verwendet würde und warum; sie ändert sich, während du tippst. Ändere den Filter im Tab und drücke dann in der Vorschau auf **Aktualisieren**, um die Wirkung zu sehen.

![Vorschau der Aktivierung eines Lorebooks](../../images/lorebook-preview-activation.png)

Die Vorschau wendet nur die Regeln von Marginalia an: Der Text ist der, den du eingibst (nicht die Benutzerprompt-Vorlage darum herum), und was eine [Erweiterung](../extensions/index.md) während der Generierung eines Teils verändert, ist darin nicht enthalten.

<a id="order-and-insertion"></a>

## Reihenfolge und Einfügung

Aktivierte Einträge werden nach **Reihenfolge** sortiert in den Prompt eingefügt, die niedrigste zuerst. Einträge mit gleicher Reihenfolge behalten die Reihenfolge, in der sie erstellt wurden. Einträge aus allen eingebundenen Lorebooks werden gemeinsam sortiert.

Der **Einfügemodus** bestimmt, wohin ein Eintrag kommt:

| Modus | Wohin |
|---|---|
| **Im Lore-Block** | In den Block `{%{{{backgroundLore}}}%}` der [Hauptvorlage](../books/prompts.md#master-template) – *Welt- und Lore-Kontext* in der Standardvorlage, in der Systemnachricht. Verwende ihn für Hintergrundwissen. |
| **Vor dem Benutzerprompt** | Direkt vor der Anfrage für den Teil, am Ende des Prompts. Das Modell achtet darauf am stärksten. Verwende ihn sparsam, für alles, was genau diesen Teil beeinflussen muss (der aktuelle Zustand einer Figur, eine Regel für die Szene). |

Einträge werden durch eine leere Zeile voneinander getrennt. Lore zählt zum [Kontextlimit](../protocols.md#limits); bei vielen langen Einträgen passt weniger von der Geschichte in den Prompt.

<a id="templates-in-entries"></a>

## Vorlagen in Einträgen

Der Inhalt eines Eintrags ist eine Vorlage wie die [Prompts der Bücher](../books/prompts.md#templates). Sie kann Folgendes verwenden:

| Variable | |
|---|---|
| `{%{{{sceneSetting}}}%}`, `{%{{{povCharacter}}}%}`, `{%{{{presentCharacters}}}%}`, `{%{{{instructions}}}%}` | Die Anweisungen für den Teil. |
| `{%{{{narrativePov}}}%}`, `{%{{{narrativeTense}}}%}`, `{%{{{style}}}%}` | Die erzählerischen Einstellungen des Buchs. |

und alle [Makros](../templates/macros.md). Zum Beispiel ein Eintrag, der sich an die Figur der Erzählperspektive anpasst:

{%{
```handlebars
Varro {{#povCharacter}}is seen through the eyes of {{.}}: {{/povCharacter}}a stern, scarred officer.
```
}%}

Alle aktivierten Einträge eines Teils teilen ihre Variablen in der Reihenfolge der Aktivierung: Ein Eintrag kann mit `setvar` einen Wert setzen, den später folgende Einträge (und die Hauptvorlage) mit `getvar` lesen.

Ein Syntaxfehler im Inhalt wird unter dem Feld angezeigt. Der Eintrag wird trotzdem gespeichert; bis er korrigiert ist, wird sein Inhalt als reiner Text verwendet, ohne dass die Vorlage verarbeitet wird.

<a id="tips"></a>

## Tipps

- Halte Einträge kurz und sachlich; das Modell braucht keine Prosa, sondern Fakten.
- Ein Eintrag pro Figur, Ort oder Konzept, damit jeder einzeln aktiviert werden kann.
- Verwende immer aktive Einträge für den Kern der Welt, Filter für Figurenpersonal und Orte und Tags, um ein Lorebook zwischen Büchern zu teilen.
- Prüfe, was aktiviert wurde, mit **Prompt anzeigen** bei einem generierten Teil, oder probiere einen Text vor dem Schreiben mit der [Vorschau der Aktivierung](#preview-activation) aus.
- Verwende die Scantiefe sparsam: Ein Durchgang reicht für die meisten Bücher, und jeder Durchgang kann mehr Lore hereinholen, als du wolltest.
