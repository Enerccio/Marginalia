---
label: Agenten (experimentell)
order: 958
icon: cpu
---

# Agenten (experimentell)

!!!warning Experimentell
Agenten sind neu in 1.4.0 und wurden mit echten Modellen bisher kaum eingesetzt. Rechne mit Ecken und Kanten: Werkzeugaufrufe, die ein schwaches Modell falsch ausführt, Läufe, die mehr Token kosten als gedacht, ein Prüfbildschirm, der mehr Arbeit macht als nötig, und ein Verhalten, das sich zwischen den Versionen ändert. Dein Buch ist geschützt (der Agent arbeitet auf einer Kopie, das Ergebnis wird geprüft, und vor dem Übernehmen wird ein Backup angelegt), aber **behalte selbst ein Backup eines Buches, bevor du das Ergebnis eines Agenten übernimmst**, setze ein Token-Budget und beobachte die ersten Läufe in der [Konsole](#the-console). Berichte über Fehler sind willkommen.
!!!

Ein **Agent** ist ein Modell, das eigenständig und über lange Zeit an einem deiner Bücher arbeitet und dabei die [Befehle](commands/index.md) aufruft, die du ihm als Werkzeuge erlaubst: Er kann die Geschichte lesen, sie mit dem Lorebook abgleichen und dir berichten. Im Reiter **Agenten** legst du sie an, legst fest, was sie dürfen, startest und stoppst ihre Läufe, unterhältst dich mit ihnen in der [Konsole](#the-console) und klonst sie.

**Starten** lässt den Agenten im Hintergrund arbeiten, Runde für Runde, wobei er die erlaubten Befehle und seine eigenen Dateiwerkzeuge aufruft, bis er fertig ist, dich etwas fragt, an eine Grenze stößt oder du ihn pausierst oder stoppst (siehe [Ein Lauf](#a-run)). Er arbeitet auf einer verborgenen [Kopie](#the-working-copy) des Buches, sodass dein Buch während der Arbeit nie verändert wird (das Buch selbst ist [gesperrt](#the-book-is-locked)). Ist er fertig, [prüfst du sein Ergebnis](#reviewing-the-result) und übernimmst, was du willst.

Der Reiter ist nur sichtbar, wenn ein Administrator dir die [Berechtigung](administration/permissions.md) **Agenten** gegeben hat. Administratoren haben sie immer, doch gelten für sie dieselben Grenzen wie für alle: die Standardwerte, sofern sie nicht die Einstellungen ihrer eigenen Berechtigung ausfüllen. Wer mehr Agenten, Läufe oder Platz im Arbeitsbereich braucht, stellt das dort ein; eine geänderte Grenze erreicht einen Lauf, der gerade arbeitet, wenn er pausiert und wieder fortgesetzt wird. Auf dem Telefon ist jeder Agent eine Karte mit denselben Schaltflächen, siehe [Auf dem Telefon](#on-a-phone).

<a id="the-table"></a>

## Die Tabelle

| Spalte | |
|---|---|
| **Agenten-ID** | Die Kennung des Agenten. |
| **Name** | Der Name, den du ihm gegeben hast. |
| **Buch** | Das Buch, an dem der Agent arbeitet. |
| **Status** | Was sein Lauf gerade tut: *Läuft*, *Wartet auf dich*, *Wartet auf Zustimmung*, *Wird pausiert*, *Pausiert*, *Stoppt / Wird gestoppt*, *Gestoppt, Ergebnis behalten*, *Fertig, wartet auf die Prüfung*, *Angewendet*. Ein Agent ohne Lauf zeigt *Läuft nicht*, oder *Deaktiviert*, wenn er ausgeschaltet ist. |
| **Letzter Lauf** | Wann der neueste Lauf des Agenten begann, *nie* bei einem Agenten ohne Lauf. |

![Der Reiter Agenten](../images/agents-tab.png)

Oberhalb der Tabelle engen zwei Felder die Liste ein: **Name** und **Buch** zeigen nur die Agenten, deren Name bzw. Buchname alle Wörter enthält, die du eingibst, in beliebiger Reihenfolge und unabhängig von Groß- und Kleinschreibung. Die Spaltenköpfe **Name**, **Buch**, **Status** und **Letzter Lauf** sortieren die Tabelle (ein erneuter Klick kehrt die Reihenfolge um; bei aufsteigender Sortierung nach dem letzten Lauf kommen die Agenten, die nie gelaufen sind, zuerst). Auf dem Telefon ordnet eine Auswahl **Sortieren nach** oberhalb der Karten sie nach Name, Status oder dem neuesten Lauf zuerst.

Die Schaltflächen am Ende einer Zeile hängen vom Lauf ab:

| Schaltfläche | |
|---|---|
| **Starten** | Öffnet ein Menü. **Sofort starten**: Der Agent bearbeitet seine Aufgabe (seine Anweisungen) selbstständig, und du erfährst davon, wenn er nachfragt oder fertig ist. **In der Konsole starten**: Öffnet die [Konsole](#the-console) und weist den Agenten an, noch nicht zu arbeiten, sondern dich sofort zu fragen, was du von ihm möchtest; der Lauf ist dann eine [Unterhaltung](#a-run), die weitergeht, bis du **Beenden** drückst. |
| **Fortsetzen** | Setzt einen pausierten oder gestoppten Lauf fort (wird anstelle von **Starten** angezeigt). Es fragt nach einer optionalen Notiz an den Agenten, siehe [Einen Lauf fortsetzen](#resuming-a-run). |
| **Pausieren** | Pausiert einen Lauf, der arbeitet oder auf dich wartet. |
| **Jetzt pausieren** | Wird angezeigt, während der Agent arbeitet: Pausiert ihn, ohne das Ende der Runde abzuwarten, siehe [Pause und Stopp](#a-run). |
| **Konsole** | Öffnet die [Konsole](#the-console) des Agenten. |
| **Stopp** | Fragt, ob das **Ergebnis behalten** (der Lauf kann später fortgesetzt werden) oder **verworfen** werden soll. |
| **Bearbeiten** | Öffnet den Agenten. |
| **Klonen** | Kopiert den Agenten, siehe [Klonen](#cloning). |
| **Löschen** | Verschiebt den Agenten mit seinen Läufen in den [Papierkorb](trash.md). |

Das Starten wird unter Angabe des Grundes verweigert, wenn der Agent deaktiviert ist, kein Modell hat oder du bereits alle Läufe ausschöpfst, die du gleichzeitig durchführen darfst.

![Das Menü der Schaltfläche Starten](../images/agent-start-menu.png)

Ein Agent hat höchstens einen Lauf, der nicht abgeschlossen ist, und ein Buch hat höchstens einen Agenten.
<a id="a-run"></a>

## Ein Lauf

Ein Lauf ist die Arbeit des Agenten. Er arbeitet in Runden: Das Modell wird einmal befragt, antwortet mit Text und/oder Aufrufen seiner Werkzeuge, die Ergebnisse gehen zurück, und so weiter.
Jede Runde wird gespeichert, daher kann ein Lauf pausiert und später fortgesetzt werden, auch nachdem der Server neu gestartet wurde. Der Agent hat die Befehle, die du erlaubt hast, und diese eigenen:

| Werkzeug | |
|---|---|
| Dateien | `list_files`, `read_file`, `write_file`, `append_file`, `replace_in_file`, `regex_replace_in_file`, `grep_files`, `move_file`, `copy_file`, `merge_files`, `unzip_file`, `delete_file` in seinem eigenen Arbeitsbereich (Notizen, die er zwischen den Läufen behält). Neben dem Arbeitsbereich sieht er die schreibgeschützte Datei `/AGENTS.md`, deine Anweisungen, so wie der Lauf mit ihnen begonnen hat; er kann sie jederzeit erneut lesen und niemals ändern. `list_files` und `grep_files` nehmen ein Namensmuster entgegen (`glob`, zum Beispiel `*.md` oder `chapters/*.md`), `copy_file` kopiert einen Ordner mit allem darin (ein schneller Weg, eine Version der Notizen aufzubewahren), `merge_files` fügt mehrere Dateien zu einer zusammen, ohne dass der Text durch das Modell läuft (der letzte Namensteil eines Pfades darf ein Muster wie `/chapters/*.md` sein), `unzip_file` entpackt eine [Zip-Datei](#unpacking-a-zip-file) des Arbeitsbereichs in einen neuen Ordner. `replace_in_file` und `regex_replace_in_file` nehmen ebenfalls einen **Ordner** als Pfad: Sie ändern jedes Vorkommen in allen Textdateien darunter (nur die Dateien, deren Name zu `glob` passt, sofern angegeben) und melden `path: n replacements` für jede Datei. Das ist ein **Probelauf** (nichts wird geschrieben, der Bericht zeigt, was sich ändern würde), außer der Agent übergibt `dry_run` false; es gilt alles oder nichts (kann eine Datei nicht geschrieben werden, wird keine geändert), und der Befehl wird abgelehnt, ohne dass etwas geändert wird, wenn mehr als `max_files` (100, sofern der Agent nicht mehr angibt) Dateien sich ändern würden. Auch eine einzelne Datei kann ein Probelauf sein. `list_command_results` und `save_command_result` bewahren das lange Ergebnis eines Befehls auf, ohne dass der Agent es erneut ausschreiben muss: Die letzten zehn Ergebnisse werden vollständig behalten (der Agent sieht ein langes davon gekürzt), und eines wird über seine Nummer in eine Datei gespeichert, standardmäßig das neueste, wenn keine Nummer angegeben ist; es gibt die Datei dann als `@file:/path` weiter. |
| `@file:/path` | Kein Werkzeug, sondern eine Art, eines aufzurufen: Ein Textargument eines Befehls, das genau `@file:/notes/chapter.md` lautet, wird durch den Inhalt dieser Datei des Arbeitsbereichs des Agenten ersetzt. So kann der Agent einen langen Text einmal in eine Datei schreiben, prüfen und an einen Befehl übergeben (das Ergebnis zeigt im Aufruf die kurze Verweisform, der Befehl bekommt den Text). `write_file` und `append_file` nehmen ihn ebenfalls als Inhalt entgegen, wodurch eine Datei kopiert oder an eine andere angehängt wird: Eine lange Datei kann in Teilen geschrieben werden, jeder Teil innerhalb der längsten Antwort des Modells, und auf diese Weise zusammengefügt werden. |
| `@result` | Wie `@file:`, nur für das Ergebnis eines Befehls: Ein Textargument, das genau `@result` lautet, wird durch das gesamte Ergebnis des zuletzt vom Agenten aufgerufenen Befehls ersetzt, `@result:3` durch das Ergebnis mit dieser Nummer (`list_command_results` zeigt die Nummern). Ein erzeugter Text wandert so von einem Befehl zum nächsten oder in `write_file` und `append_file`, ohne dass der Agent ihn ausschreiben muss. |
| `search_manual`, `read_manual_page` | Schlagen etwas im Benutzerhandbuch von Marginalia nach (dieselben Seiten, die der Hilfe-Assistent liest): wie eine Prompt-Vorlage oder ein Makro funktioniert, was eine Einstellung des Lorebooks bewirkt. Nur Lesezugriff; jeder Agent hat sie. |
| `view_image` | Sieht sich eine Bilddatei seines Arbeitsbereichs an (PNG, JPEG, GIF oder WebP): Das Bild wird dem Modell in der nächsten Nachricht gezeigt. Nur ein Agent, dessen Modell als bildsehend markiert ist (**Das Modell sieht Bilder** im Agenten), hat ihn. Ein großes Bild wird vor dem Senden verkleinert, ein GIF zeigt sein erstes Einzelbild, und nur die neuesten vier Bilder bleiben vor dem Modell (ein älteres wird zu einer Notiz, und der Agent kann erneut nachsehen). Die Bilder kommen von dir (**Datei hochladen** oder [an eine Nachricht angehängt](#the-console)); der Agent kann keines erzeugen. |
| Bilder für Befehle | Ein Befehl, der Bilder an ein eigenes Modell schickt, etwa [`generate-lore-entry`](extensions/lore-generator.md#commands), nimmt einen Parameter `images` entgegen: eine Liste von Pfaden zu Bilddateien des Arbeitsbereichs (höchstens 8). Das Modell des Befehls, nicht das des Agenten, muss sie sehen, daher kann der Agent Bilder übergeben, auch wenn sein eigenes Modell keine Bilder sieht. |
| `update_plan` | Seine To-do-Liste. |
| `report` | Ein Befund für dich (was er gefunden hat, wo, und wie ernst es ist: *Notiz*, *Warnung* oder *Problem*). Der Agent arbeitet danach weiter; du liest die Befunde in der [Konsole](#the-console) und im [Review](#reviewing-the-result), abgesehen von seinem Abschlussbericht. Ein Befund pro Aufruf, höchstens 200 pro Lauf. |
| `get_run_status` | Wie viele Runden und Tokens er verbraucht hat, wie viel Kontext er hat und die längste Antwort, die er schreiben kann; seinen Plan; und seine Unteragenten: eine Zeile für jeden mit seinem Zustand, Runde, Tokens, dem, was er zuletzt getan hat, und seiner Aufgabe. Dem Agenten werden diese Grenzen seines Modells außerdem in seinem Prompt mitgeteilt, damit er einen langen Text in Teilen schreibt. |
| `checkpoint`, `checkpoint_list`, `checkpoint_diff`, `checkpoint_restore`, `checkpoint_delete` | Seine [Checkpoints](#checkpoints): speichert einen Wiederherstellungspunkt der Arbeitskopie und seines Arbeitsbereichs (mit einer Bezeichnung), listet sie auf, zeigt, was sich seit einem geändert hat (die Einstellungen, Teile, Lorebooks und Einträge des Buches, die hinzugefügt, geändert oder gelöscht wurden, sowie die Dateien des Arbeitsbereichs; mit einem Pfad nur diese Datei oder diesen Ordner, und bei einer Textdatei die Sätze, die sich unterscheiden), kehrt zu einem zurück (wenn die Runde endet) und löscht einen, den er nicht mehr braucht. |
| `get_sync_report` | Nur nach einer Fortsetzung, die deine Änderungen übernommen hat: listet erneut auf, was du während der Pause am Buch geändert hast und was davon in die Arbeitskopie des Agenten gelangt ist, siehe [Einen Lauf fortsetzen](#resuming-a-run). |
| `request_compaction` | Verdichtet seinen eigenen Kontext: Wenn die Runde endet, schreibt er die Zusammenfassung seiner bisherigen Arbeit, wie bei `/compact`. Ihm wird gesagt, dass er zuvor seine Notizen in eine Datei speichern soll. Siehe [Kontext wird voll](#running-a-command-yourself). |
| `ask_user` | Eine Frage an dich. Der Lauf wartet (*Wartet auf dich*). |
| `finish` | Sein Bericht: Der Lauf ist *Erledigt*. Der Bericht wird als Grund des Status angezeigt. |

So endet ein Arbeitsabschnitt eines Laufs:

- **Wartet auf dich:** Der Agent hat ohne Werkzeugaufruf geantwortet oder dir etwas gefragt. Beantworte es in der [Konsole](#the-console).
- **Erledigt:** Der Agent hat `finish` aufgerufen (bei einem Lauf, den du *sofort* gestartet hast). Ein Lauf, den du *in der Konsole* gestartet hast, ist eine Unterhaltung: Der Bericht des Agenten kommt dir wie eine Frage zu, der Lauf wartet auf deine nächste Nachricht, und nur **Beenden** in der Konsole (oder **Stopp**) beendet ihn. Das Ergebnis eines *erledigten* Laufs wartet auf dein [Review](#reviewing-the-result).
- **Pausiert**, mit Grund: Die Grenze der Runden, das Token-Budget oder das Kontextlimit ist erreicht, das Modell konnte nicht befragt werden (ein Fehler des Anbieters, dein Limit für die KI), oder der Server wurde neu gestartet. **Fortsetzen** macht ab der letzten vollständigen Runde weiter, siehe [Wenn der Server anhält](#when-the-server-stops).
- **Fehlgeschlagen:** Der Runner selbst ist abgebrochen; der Grund sagt, was geschehen ist.

**Pausieren** wirkt zwischen zwei Runden: Die Anfrage an das Modell und die laufenden Aufrufe werden zuerst zu Ende geführt. Das kann
lange dauern (ein langsames Modell, eine lange Generierung), daher gibt es **Jetzt pausieren**: Es bricht die Anfrage an das Modell ab, deren Antwort verworfen wird, und den
laufenden Aufruf; die Aufrufe der Runde, die nicht als solche ausgeführt wurden, werden beantwortet, und der Lauf ist gleich darauf *Pausiert*. Was die verworfene Anfrage
gekostet hätte, ist verloren; setze wie gewohnt fort. **Stopp** tut dasselbe: Es wartet nicht auf die Anfrage oder den laufenden Aufruf, sie werden abgebrochen und der Lauf endet sofort
(je nach deiner Wahl behalten oder verworfen).

![Die Frage, wenn ein Lauf gestoppt wird](../images/agent-stop.png)

Ein laufender Befehl wird aufgefordert, anzuhalten, und ein Befehl, der so geschrieben ist, dass er angehalten werden kann (die, die mit einem Modell schreiben, suchen und ersetzen), endet sofort und lässt, was er bis dahin getan hat, bestehen. Einer, der nicht angehalten werden kann, darf trotzdem zu Ende laufen; in beiden Fällen wird dem Agenten gesagt, dass nicht bekannt ist, ob der Aufruf ausgeführt wurde, und er prüft den Zustand mit einem Lesebefehl, bevor er weitermacht. Die Befehle der Unteragenten werden auf dieselbe Weise abgebrochen, wenn der Lauf pausiert, gestoppt wird oder sofort eine Nachricht bekommt.

Während der Agent auf einen freien Platz bei seinem Modell wartet (siehe [Ein Modell, mehrere Agenten](#one-model-several-agents)), ist er *Läuft*; eine **Pause** oder ein **Stopp** beendet dieses Warten sofort.
<a id="the-console"></a>

## Die Konsole

Die **Konsole** öffnet ein Fenster für den letzten Lauf des Agenten, mit acht Reitern: **Lauf**, **Läufe**, **Arbeitsbereich**, **Checkpoints**, **Kontext**, **Buchkopie**, **Unteragenten** und **Diagnose**.

![Ein in der Konsole gestarteter Lauf: der Agent fragt, was zu tun ist](../images/agent-console-start.png)

**Lauf** zeigt das *Protokoll*: was du dem Agenten gesagt hast, was er geantwortet hat (beides als [Markdown](https://commonmark.org/help/), sodass Listen, Überschriften und Hervorhebungen als solche erscheinen) und jeden Befehl oder jedes Dateiwerkzeug, das er aufgerufen hat, auf eine Zeile zusammengeklappt mit den
Argumenten (klicke darauf, um sie und das Ergebnis zu sehen, das als Klartext angezeigt wird). Wenn das Modell **Überlegungen** sendet (was es sich überlegt hat, bevor es antwortete), zeigt ein eingeklappter Block *Überlegungen* sie vor der Antwort an, und der Download enthält sie. Ob du welche bekommst, hängt von Modell und Server ab: Das Modell muss sie melden, und bei deiner KI müssen die Überlegungen eingeschaltet sein. Während eine Anfrage läuft, zeigt die Konsole am Ende des Protokolls, über den wartenden Nachrichten, in einem mit *Arbeitet ...* markierten Block an, was das Modell bisher geschrieben hat (seine Überlegungen, aufgeklappt, damit du mitlesen kannst, und seinen Text). Dieser Block wird durch die echten Blöcke ersetzt, sobald die Runde vorbei ist. Solange er angezeigt wird, liest die Konsole den Lauf zweimal pro Sekunde, sonst alle zwei Sekunden. Nur der Agent selbst wird so angezeigt, nicht seine Unteragenten, und was das Modell innerhalb eines Aufrufs schreibt (etwa eine lange Datei), erscheint erst, wenn die Runde vorbei ist. Jede Nachricht hat rechts neben ihrer ersten Zeile die Uhrzeit, zu der sie geschrieben wurde (die Tageszeit, mit dem Datum, wenn es ein anderer Tag war; fahre mit der Maus darüber für die volle Zeit). So siehst du, wie lange der Agent gearbeitet hat und wann er aufgehört hat. Der Zustand des Laufs, der **Kontext** (wie viele Tokens die letzte Anfrage vom Limit des Modells verbraucht hat, sodass du siehst, wie nah der Lauf an einer Pause ist), die Runden und Tokens, die er verbraucht hat (und das Token-Budget, falls es eines gibt), bleiben oberhalb und das Feld für deine Nachricht darunter, während das Protokoll scrollt. Was der Agent erwartet, steht im Protokoll an der Stelle, an der es geschrieben wurde, als eigene Sprechblase, die dort bleibt, wenn der Lauf weitergeht:

![Die Konsole während der Agent arbeitet: eine Nachricht des Autors wartet in der Warteschlange](../images/agent-console-run.png)

- eine **Frage des Agenten**, wenn der Lauf *Wartet auf dich* ist; schreibe die Antwort in das Feld und drücke **Senden** (oder **Strg+Enter**), dann arbeitet der Agent weiter;
- ein **Bericht des Agenten**, wenn er `finish` aufgerufen hat.

![Die Frage eines Agenten, mit den Antworten, die er vorschlägt](../images/agent-console-question.png)

![Der Bericht des Agenten am Ende eines Gesprächs](../images/agent-console-report.png)

![Die Frage eines Agenten, mit den Antworten, die er vorschlägt](../images/agent-console-question.png)

![Der Bericht des Agenten am Ende eines Gesprächs](../images/agent-console-report.png)

Die Zeile über dem Protokoll zeigt den Zustand des Laufs und seine Zahlen: die Runden, die Größe des Kontexts im Verhältnis zu seinem Limit, das **Antwortlimit** (die höchstens Anzahl Tokens, die das Modell des Agenten in einer Antwort schreiben kann; ein Aufruf, der mehr braucht, wird abgeschnitten, deshalb schreibt der Agent lange Texte in Teilen) und die Tokens insgesamt. Wenn der Agent einen Plan erstellt hat (`update_plan`), erscheint dort eine Schaltfläche **Plan** mit den erledigten Schritten von allen (**Plan 2/5**); sie öffnet die Schritte in einem Popover, jeder mit einer Markierung für *Erledigt*, *In Arbeit* oder *Offen*. Wenn er Erkenntnisse gemeldet hat (`report`), öffnet eine Schaltfläche **Erkenntnisse** mit ihrer Anzahl (**Erkenntnisse 3**) sie auf die gleiche Weise, jede mit ihrer Schwere. Die Erkenntnisse werden beim Lauf gespeichert, sind also auch nach einer Pause, einem Neustart und bei einem älteren Lauf im Verlauf noch vorhanden; die Markdown-Datei eines Laufs (**Herunterladen**) enthält sie in einem Abschnitt **Erkenntnisse**.

![Der Plan des Agenten](../images/agent-console-plan.png)

![Die Erkenntnisse des Agenten](../images/agent-console-findings.png)

In einem Gespräch kannst du auch schreiben, während der Agent **arbeitet**. Die Nachricht wird **in die Warteschlange gestellt**: Sie erscheint als gestrichelte Sprechblase am Ende des Protokolls, und der Agent
liest sie, sobald die Runde, in der er gerade ist, vorbei ist: Die Anfrage an das Modell, die gerade unterwegs ist, und die Aufrufe, die er gemacht hat, werden zuerst beendet; die nächste Anfrage enthält dann bereits
deine Nachricht (mehrere Nachrichten in der Warteschlange gehen gemeinsam als eine). Wenn er nicht einmal darauf warten soll, drücke die
kleine Schaltfläche **Jetzt senden** (Blitz) an der Sprechblase: Der Agent verwirft die Runde, in der er steckt (die Anfrage an das Modell wird abgebrochen, ein Befehl, der auf deine Freigabe wartete, wird abgelehnt, nicht gemachte Aufrufe werden als übersprungen markiert), und liest als Nächstes deine Nachricht. Die **Papierkorb**-Schaltfläche daneben nimmt die Nachricht zurück, solange der Agent sie noch nicht gelesen hat (ebenso einen Befehl in der Warteschlange). Befehle, die bereits gelaufen sind, bleiben erledigt. Nachrichten und Befehle in der Warteschlange bleiben beim Neustart des Servers erhalten (sie werden gelesen, wenn du fortsetzt), und ein Lauf, der kein Gespräch ist (sofort gestartet), nimmt während der Arbeit keine Nachrichten an.
<a id="running-a-command-yourself"></a>

### Einen Befehl selbst ausführen

Unter dem Verlauf befindet sich die Befehlszeile der Anwendung (die des Reiters Geschichte: Du tippst `/`, darüber erscheinen die Liste und die Hilfe des Befehls) mit dem, was du beim Agenten ausführen kannst: `/compact`, `/clear-workspace` und die
[Befehle, die der Agent aufrufen darf](#allowed-commands), die nur Daten lesen oder hinzufügen (ein Befehl, der Daten ändert oder entfernt, wird nicht angeboten, er würde verlangen, dass du
in einem Fenster des Buches bestätigst, und dafür hat ein Lauf kein Fenster). Du führst den Befehl aus, nicht der Agent: Ihm wird in einer Nachricht von dir mitgeteilt, dass du ihn ausgeführt hast und was er geantwortet hat
(und dass das, was er zuvor gelesen hat, veraltet sein kann, wenn der Befehl die Arbeitskopie geändert hat). So glaubt er nie, einen Aufruf gemacht zu haben, den er nicht gemacht hat. Es funktioniert als Nachricht:
Wo der Lauf wartet (*Wartet auf dich*) oder *Pausiert* ist, läuft der Befehl sofort, und der Lauf bleibt, wie er ist. Wo eine [Unterhaltung](#the-console) arbeitet, wird er
eingereiht, als gestrichelte Sprechblase mit der Markierung *Befehl*, und er läuft am Ende des Zuges des Agenten, dort, wo der Agent sonst gewartet hätte (der Agent macht deshalb nicht weiter). 
Der kleine Knopf **Jetzt ausführen** an der Sprechblase verwirft die Runde, in der der Agent steckt, führt den Befehl aus und lässt den Agenten von dort weitermachen. Ein Befehl, der lange dauert, hält das Eingabefeld besetzt: Warte auf ihn, bevor du dem Agenten schreibst.

![Ein Befehl des Autors in der Konsole: /compact](../images/agent-console-command.png)

**`/compact [what to stress]`** gibt den Kontext frei. Der Agent wird gebeten, alles bisher Gelesene durchzugehen und eine Zusammenfassung zu schreiben (was du gefragt hast, was erledigt ist, was er gefunden hat,
seine Notizen und sein Plan, was er gerade tun wollte); diese Zusammenfassung ersetzt alles davor in dem, was das Modell liest. Im Verlauf siehst du weiterhin alles, und die
Zusammenfassung als eingeklappten Block **Zusammenfassung der bisherigen Arbeit**; der Export behält sie ebenfalls. Der Text nach dem Befehl, falls vorhanden, ist das, was die Zusammenfassung betonen soll.
Die Anfrage ist die des Agenten selbst, und ihre Tokens zählen zum Lauf. Die Frage, die dem Agenten gestellt wird, ist sein **Verdichtungsprompt**: der des Agenten, sonst dein
Standard in den [Einstellungen](account.md#templates), sonst der des Administrators, sonst der eingebaute. Wenn der Kontext schon so voll ist, dass die Anfrage
nicht hineinpasst, werden die ältesten Nachrichten aus ihr weggelassen (die Zusammenfassung sagt, wie viele). Mach es, bevor der Kontext voll ist; die Auslastungszeile zeigt, wie nah du dran bist. Direkt nach der Verdichtung ist der angezeigte Kontext eine Schätzung dessen, was die nächste Anfrage enthalten wird (die Zusammenfassung, der Systemprompt und die Definitionen der Werkzeuge); die erste Antwort des Modells danach ersetzt sie durch die echte Zahl.

**Kontext füllt sich.** Sobald der Kontext des Modells 75 % seiner Obergrenze überschreitet, erhält der Agent nach jeder Runde einen Hinweis (eine Nachricht des Laufs, du siehst sie im Verlauf): wie voll er ist, und dass er das, was er nicht verlieren darf, in eine Datei seines Arbeitsbereichs speichern, seinen Plan aktualisieren und `request_compaction` aufrufen soll. Über 90 % sagt der Hinweis, dass der Lauf stoppt, sobald der Kontext voll ist. Wenn der Agent nicht verdichtet, bevor der Kontext den Anteil **Verdichten bei** seiner Definition überschreitet (standardmäßig 90 %, sofern du nichts anderes angibst), erledigt der Lauf das selbst: dieselbe Verdichtung, die Zusammenfassung beginnt mit „automatisch verdichtet“, und der Agent wird zusätzlich nicht gewarnt. Eine Verdichtung, die fehlschlägt, ändert nichts, der Agent wird darüber informiert, und der Lauf versucht es erst einige Runden später erneut. Die Verdichtung, die der Agent anfordert, ist dieselbe wie deine (sein **Verdichtungsprompt**, siehe [Agenten anlegen und bearbeiten](#creating-and-editing-an-agent), ihre Tokens zählen zum Lauf); wenn sie fehlschlägt, bleibt alles unverändert, und der Agent wird darüber informiert.
Die Hinweise setzen eine Obergrenze des Kontexts voraus: die niedrigste von der Obergrenze des Agenten und den Grenzen seines Modells.

**Ein Aufruf, den der Modellserver nicht lesen konnte.** Wenn ein Modell innerhalb eines Aufrufs einen langen Text schreibt, kann seine Antwort mitten im Text enden (die Grenze einer *Antwort* ist erreicht, also die **Antwort-Tokens** des Protokolls oder die **max. Completion-Tokens** der KI; ist keine von beiden gesetzt, gilt ein Standard des Servers; oder das Modell hört einfach auf. Der Kontext, egal wie voll, ist eine andere Grenze), und der Server lehnt den unvollständigen Aufruf ab („Failed to parse tool call arguments as JSON“). Dieselbe Unterhaltung erneut anzufragen würde denselben Fehler erzeugen, deshalb pausiert der Lauf nicht einfach: Dem Agenten wird im Verlauf mitgeteilt, dass seine letzte Antwort nicht verwendet werden konnte und dass nichts getan wurde, und dass er langen Text in mehreren kleineren Schritten in eine Datei seines Arbeitsbereichs schreiben soll; danach wird er bis zu dreimal hintereinander erneut gefragt. Scheitert es weiterhin, pausiert der Lauf, und was dem Agenten mitgeteilt wurde, bleibt im Verlauf, sodass ein **Fortsetzen** eine andere Frage stellt als die, die gescheitert ist. Dasselbe gilt für einen Unteragenten.

Ein Aufruf, der abgeschnitten wurde, aber die Anwendung erreicht hat (das Ergebnis des Aufrufs sagt „not valid JSON ... cut off in the middle“), bleibt im Verlauf so, wie das Modell ihn geschrieben hat, damit du ihn lesen kannst; er wird aber mit leeren Argumenten an das Modell zurückgeschickt. Ein Server, der die früheren Aufrufe erneut liest, würde sonst jede folgende Anfrage ablehnen, und der Lauf könnte nicht weiterlaufen, ohne zurückgesetzt zu werden.

**Außer Kontrolle geratene Aufrufe.** Ein Modell kann in eine Schleife geraten und in einer einzigen Antwort denselben Aufruf hunderte Male schreiben; die Ergebnisse würden den Kontext füllen, bevor die nächste Anfrage kommt. Der Lauf führt höchstens 32 Aufrufe einer Antwort aus, und eine Wiederholung eines Aufrufs, der nur liest (dasselbe Werkzeug mit denselben Argumenten, in derselben Antwort), wird nur einmal ausgeführt. Die weggelassenen Aufrufe stehen nicht im Verlauf, und der Agent erhält einen Hinweis, der das sagt.

**Im Kreis laufen.** Ein Modell kann sich auch über viele Antworten hinweg wiederholen: dieselbe Abfrage (Dateien auflisten, Lorebooks auflisten ...) mit demselben Ergebnis, Runde für Runde, ohne voranzukommen. Der Lauf achtet darauf. Wenn der Agent Runden macht, die nur nachschauen, nichts ändern und denen früherer gleichen (dieselben Aufrufe, dieselben Ergebnisse), wird er bei der dritten darauf hingewiesen, dass sich nichts geändert hat. Macht er weiter, werden ihm bei der fünften seine Werkzeuge für eine Anfrage entzogen: Er muss in Klartext beantworten, was er tat und was ihn blockiert, und der Lauf **pausiert** mit dieser Antwort als letzter Nachricht, sodass du siehst, warum er feststeckte, statt für hundert weitere Runden zu bezahlen. **Fortsetzen** (oder eine Nachricht an ihn) gibt die Werkzeuge zurück und beginnt das Zählen von vorn. Eine Runde, in der sich etwas geändert hat oder während Unteragenten arbeiten, wird nicht mitgezählt. Ein Unteragent, der sich wiederholt, wird auf dieselbe Weise gestoppt, und was er gesagt hat, ist seine Antwort an den Agenten, der ihn gestartet hat.

**`/clear-workspace [what to keep]`** löscht die Dateien und alle Ordner des [Arbeitsbereichs](#the-working-copy) des Laufs (nach deiner Bestätigung, wie beim Knopf des Arbeitsbereichs). Nenne danach, was behalten werden soll, durch Leerzeichen getrennt: Dateien, Ordner (mit allem, was darin liegt) oder Muster wie `*.md` oder `chapters/*.md`, ein Pfad mit Leerzeichen in doppelten Anführungszeichen (`/clear-workspace /notes.md "/my drafts" *.png`). Die Ordner über einer behaltenen Datei bleiben stehen. Ist etwas Genanntes nicht vorhanden, wird nichts gelöscht. `/AGENTS.md` und die Checkpoints gehören nicht zum Arbeitsbereich und werden nie angerührt. Der Agent erfährt, was gelöscht und was behalten wurde. Im Verlauf ist ein Befehl, den du ausgeführt hast, eine Sprechblase mit dem Befehl und seiner Antwort.

Wenn das Modell des Agenten Bilder sieht (siehe [Agenten anlegen und bearbeiten](#creating-and-editing-an-agent)), steht **Bild anhängen** neben **Senden**: Wähle eine PNG-, JPEG-, GIF- oder WebP-Datei aus oder füge ein Bild aus der Zwischenablage in das Nachrichtenfeld ein (**Ctrl+V**). Es wartet über dem Feld (das Kreuz nimmt es zurück), bis du sendest. Bis zu acht Bilder gehören zu einer Nachricht; der Text darf leer sein.
Die Bilder werden im Ordner **uploads** des Arbeitsbereichs gespeichert (ein bereits vergebener Name bekommt eine Nummer: `map-2.png`), innerhalb der Grenzen und Regeln des Arbeitsbereichs, und dem Modell werden sie zusammen mit deiner Nachricht gezeigt. Lösche sie im Reiter **Arbeitsbereich**, wenn du sie nicht mehr brauchst.

Die Knöpfe unten sind **Pausieren**, **Fortsetzen** und **Stopp**, die tun, was in der Tabelle steht, und **Beenden**, das einen Lauf beendet, der auf dich wartet (seine Arbeit ist erledigt, *Erledigt*).

Das Fenster sieht alle zwei Sekunden nach dem Lauf, solange es geöffnet ist; **Aktualisieren** tut es sofort. Der Reiter **Lauf** zeigt den letzten Lauf, der nicht verworfen wurde, sodass ein Lauf, der beendet wurde oder fehlgeschlagen ist, dort noch gelesen werden kann, bis der Agent einen neuen Lauf beginnt; ältere Läufe stehen unter **Läufe**.

**Läufe** ist der Verlauf des Agenten: jeder Lauf, den er hatte, der neueste zuerst, mit Datum, Status, Runden, Tokens und Ergebnis. Nutze das Auge, um einen Lauf im Reiter **Lauf** zu **öffnen** und seine Schritte und seinen Bericht erneut zu lesen (der geöffnete Lauf bleibt dort, bis du einen anderen öffnest), den Pfeil, um seinen Verlauf als Markdown-Datei zu **herunterzuladen** (was gesagt wurde und wann, jeder Befehl mit Argumenten und Ergebnis, der Bericht, die Unterhaltungen der Unteragenten), und den Papierkorb, um ihn zu **löschen**.
Ein gelöschter Lauf landet im [Papierkorb](trash.md). Ein Lauf, der gerade läuft, kann nicht gelöscht werden, stoppe ihn zuerst. Läufe, die du gestoppt und **verworfen** hast, stehen ebenfalls in der Liste, sodass du lesen kannst, was sie getan haben, oder sie löschen kannst; der Reiter **Lauf** zeigt sie nur, wenn du sie öffnest.

![Der Reiter Läufe](../images/agent-runs.png)

**Arbeitsbereich** zeigt die Dateien des Agenten: seine Notizen und Berichte, die er zwischen den Läufen aufbewahrt. Klicke auf einen Ordner, um hineinzugehen (**Nach oben** geht zurück), klicke auf eine Textdatei, um sie zu lesen, nutze die Pfeile, um eine Datei herunterzuladen, oder den Papierkorb, um sie zu löschen, und **Leeren** löscht alles (veraltete Notizen führen einen neuen Lauf in die Irre). **Neuer Ordner** legt einen Ordner im geöffneten an, und **Datei hochladen** legt eine Datei von deinem Computer dort ab, sodass du dem Agenten ein Briefing, einen Stilleitfaden oder einen Entwurf geben kannst.
Dieselben Regeln gelten wie für die eigenen Dateien des Agenten: Namen dürfen `\ < > : " | ? *`, keinen abschließenden Punkt oder Leerzeichen enthalten und nicht zu lang oder zu tief verschachtelt sein; die Grenzen für Dateigröße und Anzahl der Dateien gelten; und eine hochgeladene Datei muss Text sein (UTF-8), denn der Agent kann nur Text lesen, ein Bild (PNG, JPEG, GIF oder WebP, erkannt an dem, was die Datei enthält, nicht an ihrem Namen; bis 5 MB, und es zählt wie jede Datei gegen die Gesamtgröße und die Anzahl der Dateien) oder eine Zip-Datei (bis 10 MB, unverändert behalten; der Agent entpackt sie, siehe [unten](#unpacking-a-zip-file)). Eine Datei wird durch einen Upload nie ersetzt: lösche zuerst die alte.
Dateien können nicht geändert werden, während der Agent arbeitet; pausiere ihn zuerst. Der Arbeitsbereich hat eine Grenze für Größe, Dateigröße und Anzahl der Dateien, die dein Administrator in deiner [Berechtigung](administration/permissions.md#settings-of-a-permission) festlegen kann; die Zeile unter der Tabelle zeigt, wie viel davon genutzt ist. Der Name `AGENTS.md` oben ist für deine Anweisungen reserviert, die der Agent dort sieht (die Datei kann nicht geschrieben oder gelöscht werden).

![Der Reiter Arbeitsbereich](../images/agent-console-workspace.png)
<a id="unpacking-a-zip-file"></a>

### Entpacken einer Zip-Datei

Lade eine Zip-Datei im Tab **Arbeitsbereich** hoch (**Datei hochladen**), wenn der Agent an vielen Dateien auf einmal arbeiten soll, zum Beispiel an den Kapiteln eines Entwurfs, und bitte ihn, sie zu entpacken: Der Agent hat das Werkzeug `unzip_file`, das `/uploads/draft.zip` in den neuen Ordner `/uploads/draft` entpackt (oder in einen Ordner, den er angegeben bekommt; der Ordner darf noch nicht existieren). Das Entpacken geschieht ganz oder gar nicht, und einem Archiv wird nie vertraut:

- Nur Textdateien (UTF-8), Bilder (PNG, JPEG, GIF, WebP) und Zip-Dateien werden behalten. Die anderen Einträge werden weggelassen und in der Antwort an den Agenten genannt. Eine Zip-Datei im Archiv wird abgelegt und nie für sich allein entpackt.
- Die Dateien müssen in den Platz passen, den der Arbeitsbereich bietet (Gesamtgröße, Dateigröße, Anzahl der Dateien), sonst wird nichts entpackt.
- Ein Archiv mit einem Eintrag, der aus dem Ordner hinausführt (`../x`), oder der mit `/` beginnt, wird als Ganzes abgelehnt, ebenso eines mit demselben Namen zweimal (auch wenn sich nur die Groß- und Kleinschreibung unterscheidet), einem Namen, der sowohl Datei als auch Ordner ist, mehr als 5 000 Einträgen oder einem verschlüsselten oder beschädigten Archiv.
- Eine Zip-Bombe wird abgelehnt: ein Archiv, das entpackt mehr enthalten würde, als der Arbeitsbereich fassen kann, oder mehr als das Hundertfache seiner eigenen Größe.

<a id="sub-agents"></a>

## Unteragenten

Ein Agent kann eine Aufgabe an einen **Unteragenten** übergeben: einen Helfer mit einer eigenen Unterhaltung, der eine Aufgabe erledigt und mit einem Text antwortet. Er hat die Befehle und Dateien des Agenten (oder des Modells, auf dem er läuft, siehe [unten](#models-for-sub-agents)) und arbeitet in derselben Arbeitskopie und im selben Arbeitsbereich (was er im Buch ändert, bleibt bestehen). Er kennt jedoch die Unterhaltung des Agenten nicht, deshalb muss der Agent in der Aufgabe alles sagen. Der Sinn liegt darin, dass das, was der Unteragent liest und ausprobiert, nicht in den Kontext des Agenten gelangt: ein Kapitel prüfen, die Namen überprüfen, einen Teil entwerfen.

Stelle in dem [Agenten](#creating-and-editing-an-agent) unter **Maximale Anzahl Unteragenten** ein, wie viele gleichzeitig arbeiten dürfen. Null (der Standardwert) bedeutet keine: Der Agent erfährt nicht einmal, dass es sie gibt.

- Bei **eins** startet der Agent einen Unteragenten und wartet (er wird *geparkt*), bis dieser fertig ist; der Aufruf liefert dessen Antwort zurück.
- Bei **mehr** kehrt der Start eines Unteragenten sofort zurück, und der Agent kann weitermachen, weitere starten, sie sich ansehen (`agent_status`), auf eine Antwort warten (`await_agent`) oder einen anstoppen (`cancel_agent`). Der Aufruf, der die Grenze erreicht, wartet, bis einer von ihnen beendet ist, und liefert zurück, was beendet wurde.

Regeln, die in jedem Fall gelten:

- Ein Unteragent **liest den gesamten Arbeitsbereich, schreibt aber nur in einen eigenen Ordner**, `/tmp/sub1`, `/tmp/sub2`... (er wird beim Start des Unteragenten angelegt, und was ein früherer Lauf in einem Ordner gleichen Namens hinterlassen hat, wird gelöscht). Seine Antwort wird dort als `result.md` gespeichert. Wird eine lange Antwort abgeschnitten angezeigt, liest der Agent den vollständigen Text aus dieser Datei. Du findest die Dateien im Tab **Arbeitsbereich**; sie belegen einen Teil des Kontingents des Arbeitsbereichs und bleiben erhalten, bis du sie leerst.
- Ein Unteragent läuft auf dem Hauptmodell des Agenten, sofern der Agent nicht ein anderes anfordert, siehe [Modelle für Unteragenten](#models-for-sub-agents).
- Ein Befehl eines Unteragenten, der Daten ändert, folgt der Einstellung **Destruktive Befehle** des Agenten: Bei *Erst fragen* fragt dich dasselbe [Fenster](#asking-you), und dieser Unteragent wartet auf deine Antwort (ein Pausieren des Laufs lehnt sie ab).
- Ein Unteragent kann **keine Unteragenten starten**, den Lauf beenden, dich etwas fragen oder Kontrollpunkte setzen. Er endet, indem er mit einem Text antwortet.
- Alles zählt in das **eine Budget des Laufs**: Runden, Tokens, die Grenzen des Modells. Ein Unteragent, der an eine Grenze stößt, scheitert und sagt dies; der Agent stößt dann an dieselbe Grenze.
- Wie viele Anfragen gleichzeitig beim Modell unterwegs sein dürfen, bestimmt die **gleichzeitigen Anfragen an das Modell**, die dir dein Administrator vorgegeben hat (eins, wenn nichts eingestellt ist): Bei eins wechseln sich der Agent und sein Unteragent ab.
- Aufrufe, die das Buch oder den Arbeitsbereich ändern, werden nacheinander ausgeführt, egal wer sie macht, sodass zwei Unteragenten nie im selben Moment schreiben. Trotzdem solltest du nicht zwei von ihnen auftragen, denselben Teil zu ändern.
- **Das Pausieren des Laufs pausiert auch die Unteragenten.** Sie halten an ihrem nächsten stabilen Punkt an (zwischen zwei Anfragen oder zwei Aufrufen, sofort, wenn du sofort pausierst), und was sie gesagt und getan haben, wird mit dem Lauf gespeichert. So macht ein **Fortsetzen** dort weiter, wo alles stand: Die Unteragenten führen ihre Unterhaltung fort, und ein Aufruf des Agenten, der auf einen von ihnen gewartet hat (`await_agent`, oder `spawn_agent` mit einer Grenze von eins), wartet erneut, als wäre nichts geschehen. Ein Unteragent, der vor der Pause bereits fertig war, bleibt für Rückfragen des Agenten erhalten. Das Anhalten und Behalten eines Laufs wirkt genauso; ein Verwerfen, ein Zurücksetzen auf einen Kontrollpunkt (das Buch, in dem die Unteragenten gearbeitet haben, wird dabei ersetzt) und das Beenden des Agenten oder eine Frage des Agenten an dich beenden sie endgültig. Ein Agent, der seinen Zug beendet, während Unteragenten noch arbeiten, wird einmal darauf hingewiesen; tut er es erneut, werden sie angehalten.
- Ihre Unterhaltungen erscheinen nicht im Tab **Lauf**. Der Tab **Unteragenten** der Konsole zeigt sie: Wähle oben einen aus, um zu lesen, was ihm der Agent aufgetragen hat, was er geantwortet hat, die Befehle, die er mit ihren Ergebnissen aufgerufen hat, und wie er geendet hat (die Antwort, die er gegeben hat, oder warum er gescheitert oder abgebrochen wurde), in denselben Blöcken wie im Verlauf. Er folgt dem Lauf, während dieser arbeitet, und zeigt auch die Unteragenten eines älteren Laufs (wähle zuerst diesen Lauf im Tab **Läufe**). Ein Aufruf `spawn_agent` im Tab **Lauf** hat eine Schaltfläche **Unteragent**, die seine Unterhaltung öffnet. Eine Unterhaltung, auf die der Agent zurückgesetzt hat (ein Zurückspulen), wird ausgegraut und als *vom Agenten vergessen* markiert. Der **Download** des Laufs (im Tab **Läufe**) enthält einen Abschnitt für jeden Unteragenten mit seiner Aufgabe, seinem Zustand und jeder Nachricht und jedem Aufruf.

![Der Tab Unteragenten](../images/agent-subagents.png)

<a id="models-for-sub-agents"></a>

### Modelle für Unteragenten

Der Agent hat ein **Hauptmodell**, das oben im Dialog gewählt wird. Unter **Modelle für Unteragenten** kannst du weitere hinzufügen, zum Beispiel ein günstiges Modell, das nur liest, oder ein flüssiges, das entwirft. Jedes hat eine **Rolle** (seinen Namen), eine **Beschreibung** (wann es einzusetzen ist: Der Agent liest sie, um auszuwählen), seine KI und sein Protokoll, eine optionale **Kontextgrenze** und das, was es verwenden darf:

![Ein Modell für Unteragenten](../images/agent-models.png)

- **Kann Befehle und Werkzeuge verwenden.** Ist dies aus, antwortet ein Unteragent auf diesem Modell nur mit Text: Er erhält keine Befehle und keine Werkzeuge.
- **Befehle einschränken.** Ist dies aus, hat ein Unteragent auf diesem Modell die Befehle des Agenten. Ist es an, nur die, die du ankreuzt, und davon nur die, die der Agent erlaubt bekommt (die erlaubten Befehle des Agenten bleiben die obere Grenze). Die Werkzeuge für Dateien sind keine Befehle, deshalb haben alle Unteragenten sie. Das Hauptmodell hat dieselbe Einstellung für die Unteragenten, die auf ihm laufen (die Schaltfläche **Bearbeiten** in seiner Zeile).

Der Agent erfährt die Rollen, die Beschreibungen und die Befehle jedes Modells und startet einen Unteragenten darauf mit `profile` von `spawn_agent`; lässt er es weg, nimmt er sein eigenes Modell. Das Kontrollkästchen **Unteragenten dürfen auf dem Hauptmodell laufen** (standardmäßig an) schaltet diese letzte Wahl ab: Der Agent muss dann ein anderes Modell nennen, und ohne ein solches erhält er überhaupt keine Werkzeuge für Unteragenten. Ein Unteragent hat die Grenzen seines eigenen Modells (wie viel er lesen kann und wie lang seine Antwort sein darf), und der Tab **Unteragenten** sowie die Diagnose zeigen, auf welchem Modell jeder lief.

<a id="one-model-several-agents"></a>

## Ein Modell, mehrere Agenten

Ein lokaler Server hat oft nur einen Steckplatz (eine GPU): Verlangst du zwei Antworten gleichzeitig, kriechen beide oder laufen in eine Zeitüberschreitung. Die **Verbindungen** des [Modells](inference-providers.md#general-settings) (1, sofern du nichts anderes einstellst) lassen jede Anfrage an es der Reihe nach laufen, in der Reihenfolge, in der sie eingegangen sind: die Anfragen deiner Agenten, der Agenten anderer Nutzer und dein eigenes Schreiben im Geschichtenfenster. Ein Lauf, der wartet, bleibt *Läuft*, und ein **Pausieren** oder **Stopp** beendet das Warten sofort. Zwischen zwei Antworten des Modells hält ein Agent keine Verbindung, sodass deine Generierung dazwischen laufen kann.
<a id="when-you-are-not-there"></a>

## Wenn Sie nicht da sind

Ein Agent, der Sie braucht, fertig wird oder fehlschlägt, meldet das den offenen Bildschirmen der Anwendung (dem Abzeichen am Reiter, einem Hinweis). Ist kein Bildschirm von Ihnen offen, kann er außerdem eine kurze Nachricht an eine Adresse senden, die Sie unter [Einstellungen](account.md) angeben, sofern ein Administrator Ihnen die [Berechtigung *Benachrichtigungen*](administration/permissions.md) gegeben hat (Administratoren haben sie). Gemeint ist „Nachricht an eine URL, wenn ein Agent Sie braucht“: Ihr **ntfy**-Thema (die ntfy-App auf dem Telefon zeigt es sofort an), eine **Gotify**-Nachrichten-URL oder ein **Discord**-Webhook; wählen Sie das **Format**, das der Dienst verlangt. **Testnachricht senden** probiert es mit dem aus, was in den Feldern steht. Die Nachricht nennt den Agenten und sagt, was er will („wartet auf Sie“, „fertig“, „bittet um die Genehmigung des Befehls ...“, „fehlgeschlagen“, „wurde pausiert: Grund“), in Ihrer Sprache. **Auch wenn die Anwendung geöffnet ist** sendet sie in jedem Fall.

Der Server ruft die Adresse auf. Deshalb kann ein Administrator das für alle abschalten (Admin, *Agenten*). Es werden nur Adressen im Internet aufgerufen, außer der Administrator hat in Ihrer Berechtigung zusätzlich **Adressen auf diesem Rechner und in privaten Netzwerken erlauben** angekreuzt. Für ein ntfy oder Gotify auf demselben Rechner oder in Ihrem eigenen Netzwerk bitten Sie darum. Die Testnachricht zeigt an, wenn die Adresse abgelehnt wird. Behandeln Sie die Adresse wie ein Passwort, wenn sie ein Geheimnis enthält: Wer sie kennt, kann lesen, was gesendet wird. Der Server schreibt sie nie in sein Protokoll, Weiterleitungen werden nicht verfolgt, und es werden nur `http` und `https` aufgerufen.

<a id="asking-you"></a>

## Wenn Sie gefragt werden

Wenn der Agent Sie braucht, wird Ihnen das überall in der App mitgeteilt, wo Sie gerade sind:

- Eine **Zahl am Reiter Agenten** zählt die Agenten, die auf Sie warten, und die Schaltfläche **Konsole** eines solchen Agenten ist hervorgehoben;
- ein **Hinweis** erscheint mit einer Schaltfläche **Öffnen**, die die Konsole dieses Agenten öffnet.

Der Agent braucht Sie, wenn er *Ihnen etwas fragt oder auf Ihre nächste Nachricht wartet*, Sie bittet, einen **Befehl zu genehmigen**, *fertig* ist, *fehlgeschlagen* ist oder *von selbst pausiert wurde* (ein Grenzwert ist erreicht, das Modell konnte nicht gefragt werden, der Server wurde neu gestartet). Eine Pause, die Sie selbst veranlasst haben, zählt nicht.

Agenten arbeiten, während Sie weg sind. Deshalb hängt das nicht davon ab, dass die Anwendung geöffnet ist: Die Zahl ist da, wenn Sie sich wieder anmelden, und jedes offene Browserfenster erhält den Hinweis. Das Öffnen der Konsole des Agenten löscht ihn (die anderen Fenster werden aktualisiert).

Ist der Agent auf **Erst fragen** eingestellt und will er einen Befehl ausführen, der Daten ändert oder entfernt, hält der Lauf bei *Wartet auf Zustimmung* an, und über der Konsole erscheint ein Fenster mit dem Befehl und seinen Argumenten. **Genehmigen** lässt ihn laufen, **Ablehnen** teilt dem Agenten mit, dass Sie es abgelehnt haben, und er macht weiter. Das Pausieren oder Anhalten des Laufs (oder das Anhalten des Servers) gilt nicht als Ablehnung: Der Agent erfährt nichts davon, und wenn Sie **Fortsetzen**, erscheint das Fenster für denselben Befehl erneut. Der Befehl wirkt weiterhin auf die [Kopie](#the-working-copy) des Buchs. Ist die Konsole geschlossen, erscheint das Fenster, sobald Sie sie öffnen.

![Das Fenster, das um die Genehmigung eines Befehls bittet](../images/agent-approval.png)

<a id="when-the-server-stops"></a>

## Wenn der Server anhält

Das Anhalten des Servers pausiert zuerst die arbeitenden Läufe, so wie **Jetzt pausieren** es tut (dem Server werden dafür einige Sekunden gegeben). Die laufende Anfrage und der laufende Aufruf werden abgebrochen: Der Lauf ist *Pausiert* mit dem Grund „Der Server wurde angehalten“, und ein **Fortsetzen** ist möglich. Wird der Server stattdessen beendet oder stürzt er ab, pausiert der nächste Start die Läufe, die er als arbeitend vorfindet („Durch einen Neustart des Servers unterbrochen“). Alles, was der Lauf getan hat, bleibt erhalten: die gespeicherten Runden, die Kopie, der Arbeitsbereich sowie die Nachrichten und Befehle, die Sie in die Warteschlange gestellt hatten. Verloren geht nur die Runde, die nicht abgeschlossen wurde. Beim Fortsetzen wird der Aufruf behandelt, in dem der Lauf gerade war:

- Ein Aufruf, der **noch nicht begonnen hatte** (er wartete auf Ihre Genehmigung oder auf seinen Zug nach einem anderen Aufruf), wird erneut ausgeführt, sodass ein Befehl, der Ihre Genehmigung braucht, Sie erneut fragt. Ein Aufruf, der nur liest, wird ebenfalls erneut ausgeführt.
- Ein Aufruf, der **bereits begonnen hatte, etwas zu ändern**, kann teilweise ausgeführt worden sein: Dem Agenten wird mitgeteilt, dass das Ergebnis unbekannt ist, und er prüft es mit einem Lesewerkzeug, bevor er es wiederholt. Die Aufrufe danach sind als nicht ausgeführt markiert.
- Ein Befehl von Ihnen, der lief, als der Server anhielt, läuft weiter, wenn der Lauf weiterläuft. `/compact` und `/clear-workspace` werden erneut ausgeführt. Jeder andere Befehl, der bereits begonnen hatte, wird **nicht** erneut ausgeführt (er kann teilweise ausgeführt worden sein): Der Agent wird darüber informiert und prüft es. Die Nachrichten und Befehle, die danach kamen, werden wie üblich zugestellt.
- Der Plan des Agenten und die Ergebnisse seiner Befehle (`list_command_results`, `@result`) bleiben ebenfalls erhalten, sodass der Agent sie nach dem Neustart hat.
- Die Aufrufe eines Unteragenten, die gerade liefen, werden wie die des Agenten behandelt: Die noch nicht begonnenen werden ausgeführt, der begonnene wird ihm als „Ergebnis unbekannt“ gemeldet, und ein Unteragent, der auf Ihre Genehmigung wartete, fragt erneut.
- Wird der Server **beendet** (`kill -9`, ein Stromausfall), während ein Befehl läuft, stirbt der Befehl mit ihm; was er bewirkt hat, ist wie oben beschrieben.

<a id="the-working-copy"></a>

## Die Arbeitskopie

Jeder Lauf arbeitet auf einer **Kopie Ihres Buchs** und seiner Lorebooks, die die App beim Start des Laufs anlegt: die Nachrichten, Zusammenfassungen, Tags und Einstellungen, so wie sie in diesem Moment sind, sowie das Lorebook mit seinen Unter-Lorebooks. Die Kopie ist versteckt: Sie erscheint nicht in den Listen der Bücher und Lorebooks. Was der Agent auch tut, er tut es an der Kopie; Ihr Buch bleibt, wie es war.

Ist beim Agenten **Das Buch mit dem Modell des Agenten schreiben** eingeschaltet (der Standardwert), erhält die Kopie außerdem das **Modell des Agenten** (die KI und das Protokoll seines Hauptmodellprofils) anstelle der KI und des Protokolls Ihres Buchs, jedes Mal wenn der Lauf startet oder fortgesetzt wird. Die Befehle, die mit einem Modell schreiben (`/next`, `/swipe`, `/regenerate`, `/redraft-part`, `/generate-annotation`, die Zusammenfassungen), und die Modellbefehle von Erweiterungen (der Lore Generator, die Arc Outline) verwenden daher das Modell des Agenten, auch wenn Ihr Buch auf ein anderes oder auf keines eingestellt ist. Ein im Aufruf genanntes `profile` hat weiterhin Vorrang. Ist die Einstellung ausgeschaltet, verwenden sie das Modell des Buchs. Das ist keine Änderung an Ihrem Buch: Die Prüfung listet sie nicht auf, und das Übernehmen des Ergebnisses lässt die KI und das Protokoll Ihres Buchs unverändert.

Die Kopie wird in den [Ordner](books/folders.md) Ihres Buchs gelegt, sodass der Agent mit den Einstellungen schreibt, die der Ordner vorgibt (Sprache, Vorlagen, Erzählperspektive, Zeitform, Stil und die Prompts), so wie es das Buch tut. Die Kopie ist kein Inhalt des Ordners: Sie wird dort nicht gezählt, und die Arbeit darin ändert das *Zuletzt geändert* des Ordners nicht. Die Kopie folgt dem Buch, wenn Sie es verschieben, während der Lauf pausiert ist. Das Übernehmen des Ergebnisses verschiebt Ihr Buch nie.

Ein pausierter Lauf behält seine Kopie und arbeitet beim Fortsetzen wieder darauf. Ein Lauf, der *Erledigt* ist, oder den Sie angehalten und behalten haben, behält sie, bis Sie [sein Ergebnis übernehmen oder verwerfen](#reviewing-the-result). Ist der Lauf *Fehlgeschlagen*, verwerfen Sie sein Ergebnis oder seine Änderungen werden übernommen, wird die Kopie endgültig gelöscht, und der nächste Lauf legt eine neue aus dem Buch an, so wie es dann ist.
<a id="resuming-a-run"></a>

## Fortsetzen eines Laufs

Ein pausierter oder behaltener Lauf sperrt dein Buch nicht, du kannst das Buch also in der Zwischenzeit bearbeiten. Wenn du den Lauf **Fortsetzen**, gleicht seine Arbeitskopie sich mit dem an, was du getan hast:

- Was du geändert und der Agent nicht geändert hat (ein Teil, ein Lorebook-Eintrag, eine Einstellung, ein Tag ...) kommt in die Kopie. Ein Teil, den du hinzugefügt hast, kommt ebenfalls hinein, und der Agent kann darauf aufbauen; was du gelöscht hast, wird aus der Kopie gelöscht.
- Was du und der Agent auf unterschiedliche Texte geändert habt, **bleibt so, wie der Agent es hat**: Nichts, was der Agent getan hat, geht verloren. Es ist ein [Konflikt](#reviewing-the-result), den du beim Prüfen des Ergebnisses auflöst, wobei beide Texte vor dir liegen.
- Was du geändert und der Agent gelöscht hat, bleibt gelöscht, und was du gelöscht und der Agent geändert hat, bleibt in der Kopie; die Prüfung zeigt beides.

Dafür musst du nichts anklicken: Der Abgleich gehört zum Fortsetzen und dauert einen Moment (einige Sekunden, wenn du hunderte Elemente geändert hast). Der Zustand der Kopie vor dem Abgleich wird als automatischer [Kontrollpunkt](#checkpoints) gespeichert. Der Agent wird beim Weitermachen in einer Nachricht darüber informiert, was passiert ist, mit den Namen, unter denen er die Elemente kennt („Während der Lauf pausiert war, hat der Benutzer das Buch geändert ..."), und kann mit `get_sync_report` erneut nachfragen.

**Ein Konflikt löst sich nicht von selbst.** Jedes Fortsetzen vergleicht die Kopie erneut mit dem Buch, daher wird ein Konflikt bei jedem Fortsetzen erneut gemeldet, und der Agent wird jedes Mal erneut darauf hingewiesen, bis du ihn löst: indem du deine Seite des Buchs änderst (auf den Text des Agenten oder zurück auf den früheren Stand) oder bei der Prüfung.

**Eine Notiz beim Fortsetzen.** **Fortsetzen** öffnet ein kleines Fenster mit dem Feld *Notiz für den Agenten (optional)*. Was du dort schreibst, liest der Agent als Erstes, wenn er weitermacht, nach dem Bericht über die Änderungen, die du am Buch vorgenommen hast. Sie ist für eine Richtungsänderung gedacht („lass das Lorebook in Ruhe", „beginne mit Kapitel 3"). Anders als eine Nachricht in einer Unterhaltung funktioniert sie für jeden Lauf, auch für einen, der nicht auf deine Nachrichten wartet. Lass sie leer, um wie bisher fortzusetzen.

![Fortsetzen eines Laufs mit einer Notiz](../images/agent-resume.png)

<a id="reviewing-the-result"></a>

## Prüfen des Ergebnisses

Wenn ein Lauf *Erledigt* ist (oder du ihn mit **Stoppen und das Ergebnis behalten** angehalten hast), haben die Zeile des Agenten und die Konsole eine Schaltfläche **Änderungen prüfen**. Sie öffnet ein Fenster mit allem, was der Agent geändert hat, verglichen mit deinem Buch **so, wie es jetzt ist**, eine Zeile pro Eintrag. Wenn der Agent Befunde gemeldet hat, werden sie oberhalb der Änderungen aufgelistet (**Befunde (3)**), sodass auch eine Prüfung, die nichts geändert hat, etwas zu zeigen hat:

![Prüfen des Ergebnisses eines Laufs](../images/agent-review.png)

- **Buch:** sein Name, seine Beschreibung, Sprache, Erzählperspektive, Zeitform, Stil, Vorlagen, die Anzahl der pro Teil gespeicherten Versionen, seine Tags und die Daten, die eine Erweiterung im Buch speichert (ein Handlungsbogen, der Figurenverfolger).
- **Teil:** ein Teil, dessen Text, Anweisungen, Szenenort, Perspektivfigur, anwesende Figuren oder Zusammenfassung der Agent geändert hat, ein Teil, den er geschrieben hat, ein Teil, den er gelöscht hat. Die Variablen, die eine Generierung zusammen mit einem Teil speichert (und die Daten von Erweiterungen), werden mit ihm übernommen, als Feld **Daten**.
- **Lorebook** und **Eintrag:** ein Lorebook oder ein Eintrag, das bzw. den der Agent angelegt, geändert (auch verschoben, umbenannt, mit anderen Tags oder anderen Unterbüchern) oder gelöscht hat.
- **Anmerkung:** eine [Notiz](books/annotations.md#annotations-and-agents) zu einer Stelle eines Teils, die der Agent angelegt, geändert (die Notiz, den Typ, den Status) oder gelöscht hat.

Markiere die Zeilen, die du willst, und klicke dann auf **Die gewählten Änderungen anwenden**. Die Schaltflächen über der Liste markieren **alle**, **keine**, **nur neue Teile** (der neue Zweig, den der Agent geschrieben hat, sonst nichts), **nur Anmerkungen** oder alles **ohne Konflikte**. Eine Zeile, die eine andere braucht (ein neuer Teil braucht den Teil, auf den er folgt, wenn auch dieser neu ist), nimmt sie beim Markieren mit, und sie verliert ihre Folgeelemente, wenn du sie abwählst. Klicke auf eine Zeile, um sie rechts zu sehen: den Text deines Buchs jetzt und den Text, der geschrieben wird, **den du vor dem Anwenden bearbeiten kannst**. Wenn du nichts schreibst, wird der Text des Agenten angewendet.

Dein Buch kann sich seit dem Start des Laufs geändert haben (du kannst es bearbeiten, sobald der Lauf nicht arbeitet). Was nur du geändert hast, steht nicht in der Liste und bleibt erhalten. Was ihr beide auf denselben Text geändert habt, ist keine Änderung. Was ihr beide auf **unterschiedliche** Texte geändert habt, ist ein **Konflikt**: Er beginnt unmarkiert, und wenn du ihn markierst, ersetzt der Text, den du rechts siehst, den Text des Buchs. **Die Version des Buchs verwenden** setzt den Text des Buchs in das Feld (die Zeile ändert dann für dieses Feld nichts), **Die Version des Agenten verwenden** setzt den des Agenten wieder ein; oder du schreibst das Ergebnis selbst. Eine Zeile, die nicht übernommen werden kann (der Teil, auf den sie folgt, oder das Lorebook, in dem sie liegt, wurde in deinem Buch gelöscht), weist darauf hin und lässt sich nicht markieren.

![Ein Konflikt in der Prüfung](../images/agent-review-conflict.png)

Unter den Texten zeigt **Unterschiede** an, was sich **Wort für Wort** ändert: Was nur im Buch jetzt steht, ist rot durchgestrichen, was nur im Text zum Anwenden steht, ist grün (es folgt dem, was du in das Feld schreibst). Bei einem Konflikt zeigt es außerdem, was der Agent geändert hat und was du seit dem Start des Laufs geändert hast, jeweils ausgehend vom Buch, wie es damals war, damit du siehst, wer was getan hat. Eine Zeile, die der Agent in mehr als einer Hinsicht geändert hat (ein Teil, dessen Text und Szenenort sich geändert haben, ein Eintrag mit neuem Inhalt und neuer Notiz), hat neben jeder dieser Angaben ein Kästchen **Diese Änderung übernehmen**: Wähle eine ab, um die Zeile ohne sie anzuwenden; das Buch behält dann seinen eigenen Wert für dieses Feld. Die Stelle eines Teils in der Geschichte gehört zum Teil und hat kein Kästchen, und was der Agent angelegt oder gelöscht hat, wird als Ganzes übernommen oder gelassen. **In der Kopie anzeigen** (bei einer Zeile, die der Agent angelegt oder geändert hat) öffnet die [Kopie des Buchs](#the-copy-of-the-book) an dem Teil oder Eintrag, damit du ihn an seiner Stelle lesen kannst.

![Die Unterschiede eines Konflikts, Wort für Wort](../images/agent-review-differences.png)
<a id="data-that-extensions-keep-json"></a>

### Daten, die Erweiterungen speichern (JSON)

Erweiterungen speichern ihre eigenen Daten in einem Buch, in dessen Teilen, Lorebooks und Einträgen (ein Bogenplan, der Figurentracker, die Variablen einer Generierung). Die Daten liegen als JSON vor, und die Prüfung zeigt sie **formatiert** an, zusammen mit der Zeile **JSON-Daten zusammenführen** neben dem Text, der übernommen wird. Die Daten werden nicht als Ganzes übernommen: Was nur der Agent geändert hat, wird vom Agenten übernommen, was nur Ihr Buch geändert hat, bleibt erhalten, und was beide an **verschiedenen Stellen** geändert haben, wird Stelle für Stelle zusammengeführt. Ein Konflikt liegt nur dann vor, wenn beide dieselbe **gleiche** Stelle auf unterschiedliche Werte geändert haben (die Reihenfolge der Schlüssel spielt keine Rolle). Solange Sie nicht entscheiden, wird dort die Version des Agenten verwendet.

**JSON-Daten zusammenführen** öffnet ein Fenster mit den Daten der drei Versionen, übereinandergelegt als Baum: *Beim Start des Laufs*, *Jetzt im Buch*, der *Agent* und das *Ergebnis* jeweils in einer eigenen Spalte, mit einer Markierung für das, was sich wo geändert hat (der Agent, das Buch, beide, ein Konflikt). Angezeigt werden nur die Stellen, die sich geändert haben (deaktivieren Sie dazu **Nur das Geänderte** für alle); eine Stelle wird erst geöffnet, wenn Sie sie öffnen, sodass auch ein großes Dokument kein Problem ist.

![Die Daten einer Erweiterung, übereinandergelegt](../images/agent-json-merge.png)

- Wählen Sie eine Stelle aus und übernehmen Sie ihre Daten aus einer Version (**Die Version des Agenten verwenden**, **Die Version des Buchs verwenden**, **Die vom Start des Laufs verwenden**), **Weglassen** (der Schlüssel oder das Element ist nicht im Ergebnis), oder **Aus dem Inneren zusammensetzen** (die Stellen darin entscheiden, jede für sich). Ein Schlüssel oder ein Element, das nur eine Version hat, wird in das Ergebnis kopiert, indem es aus dieser Version übernommen wird. Wenn Sie ein ganzes Objekt oder eine ganze Liste aus einer Version übernehmen, überschreibt das Ihre Auswahl darin, allerdings nicht endgültig: Setzen Sie es erneut aus dem Inneren zusammen, und Ihre Entscheidungen sind wieder da.
- Die Elemente einer **Liste** haben keine Namen, daher werden sie anhand ihres Inhalts zugeordnet: zuerst dieselben Daten, dann dieselben `uuid`, `id` oder `name`, dann die, die einander am ähnlichsten sind; was übrig bleibt und in gleicher Anzahl und derselben Art vorkommt, gilt als geändert. Wenn die Vermutung falsch ist, **Aufteilen** Sie ein Element in die Elemente jeder Version oder **Zuordnen zu …** ein anderes Element, mit dem das erste keine Version gemeinsam hat.
- **Öffnen** (oder ein Doppelklick) arbeitet nur mit einer Stelle: Der Pfad über dem Baum führt Sie wieder nach oben.
- Der Reiter **Text** zeigt die Daten der geöffneten Stelle als Text: Die Daten des Agenten können Sie **bearbeiten**, die anderen nur lesen (das Buch, den Start des Laufs, das Ergebnis). **Den Unterschied zu folgendem anzeigen** markiert, was sich Wort für Wort unterscheidet. Was Sie schreiben, wird als JSON geprüft, wenn Sie zum Baum zurückkehren, das Angezeigte ändern, eine andere Stelle öffnen oder das Ergebnis übernehmen. Ist es kein gültiges JSON, bleiben Sie, wo Sie sind, und erhalten eine Meldung, was nicht stimmt; nichts wird geändert. Gültiger Text ersetzt die Daten des Agenten an dieser Stelle, und alles darin wird erneut verglichen.
- **Von vorn beginnen** vergisst, was Sie entschieden und geschrieben haben; **Das Ergebnis übernehmen** gibt das Ergebnis an die Prüfung weiter, wo es der Text ist, der angewendet wird (die Zeile bleibt, wie Sie sie verlassen haben, solange die Prüfung offen ist).

Auch die Prüfung hat **Die Version des Agenten verwenden** und **Die Version des Buchs verwenden** für ein solches Feld, um eine der beiden unverändert zu übernehmen, sowie **Von vorn beginnen**, um zu den zusammengeführten Daten zurückzukehren.

![Die Daten des Bogenplans in der Prüfung, mit JSON-Daten zusammenführen](../images/agent-review-json.png)

Ein Lorebook, das andere Bücher verwenden, ist als **Geteilt** markiert: Die Änderung geht an das Lorebook selbst, sodass auch die anderen Bücher sie bekommen. Das Kästchen ist zunächst nicht angehakt.

Das Anwenden geschieht **direkt** am Buch: Die Teile behalten ihre Adressen, andere Bücher behalten ihre Verknüpfungen zu einem Lorebook, und was der Agent gelöscht hat, wird in den Papierkorb verschoben. Vorher wird eine **Sicherung des Buchs in seinem bisherigen Zustand** angelegt (sie befindet sich unter den Sicherungen des Buchs, mit seinen Lorebooks), und die Änderungen werden alle zusammen oder gar nicht übernommen. **Den Zweig anzeigen, auf dem der Agent geendet hat** wechselt außerdem das Buch zu dem Zweig, auf dem der Agent war, sofern er zu dem gehört, was Sie angewendet haben. Die Zusammenfassungen von Teilen, die bearbeitet wurden, sind veraltet, wie nach einer Bearbeitung von Hand; die eigenen Zusammenfassungen des Agenten werden unverändert übernommen.

Wenn die Änderungen angewendet sind, ist der Lauf *Angewendet*, und seine Kopie und Kontrollpunkte werden gelöscht. **Das Ergebnis verwerfen** wirft die Kopie weg und lässt das Buch, wie es ist. Bis Sie eines der beiden tun, hat der Agent einen unabgeschlossenen Lauf und kann keinen neuen starten.

<a id="the-copy-of-the-book"></a>

### Die Kopie des Buchs

Der Reiter **Buchkopie** zeigt das Buch, an dem der Agent arbeitet, zum Lesen: die **Geschichte**, jeweils einen Zweig (wählen Sie den Zweig oben; jeder Teil mit seiner Nummer, dem Austausch, in dem er unter den anderen Teilen nach demselben Teil steht, seinen Wörtern, dem Szenenort, der Erzählperspektive und, eingeklappt, seinen Anweisungen und seiner Zusammenfassung) sowie die **Lorebooks** mit ihren Einträgen (wählen Sie einen aus, um Inhalt, Notiz, Schlagwörter und Einstellungen zu sehen). Es ist kein Fenster auf Ihr Buch: Nichts darin lässt sich ändern, von der Kopie wird keine Sicherung angelegt, und keine Erweiterung sieht ein Buch geöffnet. Es wird beim Öffnen des Reiters und beim Drücken von **Aktualisieren** gelesen, auch während der Agent arbeitet, sodass es zeigt, was der Agent bisher geschrieben hat. Er ist vorhanden, solange der Lauf seine Kopie hat (nicht, nachdem das Ergebnis angewendet oder verworfen wurde).

![Der Reiter Buchkopie](../images/agent-book-copy.png)

<a id="diagnostics"></a>

### Diagnose

Der Reiter **Diagnose** zählt aus dem Protokoll des Laufs, was er getan hat, um herauszufinden, was ein Agent von seinen Werkzeugen und seiner Anweisung braucht. Er funktioniert bei jedem Lauf, auch bei einem älteren (wählen Sie ihn zuvor unter **Läufe** aus), und wird beim Öffnen des Reiters sowie beim Drücken von **Aktualisieren** erstellt.

![Der Reiter Diagnose](../images/agent-diagnostics.png)

- **Überblick**: Runden und Aufrufe, wie viele Aufrufe fehlschlugen, die Zeit des Modells, der Aufrufe und der Pausen (eine Lücke von 30 Minuten oder mehr zwischen zwei Schritten ist eine Pause, keine Arbeit), der Kontext auf seinem Höchststand und am Ende, die Tokens, Verdichtungen, Ihre Nachrichten und die Hinweise, die der Laufzeitumgebung dem Agenten gab (ein unlesbarer Aufruf, zu viele Aufrufe, ein voller Kontext).
- **Werkzeuge**: für jedes Werkzeug die Aufrufe, die Fehler, die gekürzten Ergebnisse, wie oft es der einzige Aufruf einer Runde war, die Zeichen, die der Agent geschrieben hat, wie viele Aufrufe einen Text als `@file:` oder `@result` übergaben, wie viele einen langen Text (300 Zeichen oder mehr) ausgeschrieben haben und wie viele davon ihn aus einem Ergebnis kopierten, das der Agent gerade erhalten hatte, die Tokens seiner Ergebnisse (ungefähr, aus den Zeichen), den Anteil an allen Ergebnissen, das längste Ergebnis und die Zeit.
- **Was die Anfragen meldeten**: wird während des Laufs aufgezeichnet, daher nur für Läufe, die nach dessen Einführung gemacht wurden (ein älterer Lauf meldet keine); wie lange die Anfragen auf das erste Wort warteten (der Server liest die Anweisung, eine kurze Zeit bedeutet also, dass sein Cache warm war), wie lange die Antworten zum Schreiben brauchten und wie viele Tokens pro Sekunde, wie viel der Anweisung der Server nicht erneut lesen musste (der Cache; llama.cpp und OpenAI melden ihn), wie viel der Antworten Argumentation war, warum die Antworten endeten (`length` bedeutet, dass das Limit einer Antwort sie abgeschnitten hat), die Aufrufe, die das Modell schrieb, aber nicht ausführte (Wiederholungen, die Grenzen, die Stapelregeln), die Zeit, die die Anfragen auf einen freien Platz warteten, die geschriebenen Zusammenfassungen und was sie kosteten, sowie dasselbe für jeden Block von 25 Runden.
- **Was das Modell schrieb**: die Zeichen der Argumente, des Textes neben den Aufrufen und der Argumentation, insgesamt und für jeden Block von 25 Runden mit der Zeit des Modells, um zu sehen, wohin seine Ausgabe geht und ob es irgendwann aufhörte, `@file` zu verwenden. **Längste Argumente** zeigt den Anfang der größten Aufrufe, mit dem Pfad eines Dateiwerkzeugs.
- **Runden, die einem Muster zum Opfer fielen**: dasselbe Werkzeug, Runde für Runde aufgerufen (drei oder mehr Mal), markiert, wenn eine Suche unmittelbar davor stand; und die Aufrufe, die mit denselben Argumenten erneut gemacht wurden. Sie zeigen, wo ein Werkzeug, das viele Elemente annimmt, oder mehr Aufrufe in einer Antwort Runden einsparen würden.
- **Fehler** mit den Argumenten des ersten Aufrufs, der jede Meldung erhielt (um zu sehen, was das Modell sendete, als ein Aufruf fehlschlug), die **größten Ergebnisse**, die **langsamsten Runden**, die **Verdichtungen** und die **Unteragenten** mit den Werkzeugen, die jeder aufrief.

**Herunterladen** gibt den Bericht als Markdown aus, **JSON herunterladen** als JSON, **Kopieren** legt das Markdown in die Zwischenablage. Die Zeiten sind die Abstände zwischen den gespeicherten Zeilen und daher ungefähr: Die Zeit einer Antwort schließt das Warten auf einen freien Platz beim Modell ein.
<a id="the-context"></a>

### Der Kontext

Der Tab **Kontext** zeigt, was das Modell mit seiner **nächsten Anfrage** liest, aufgeschlüsselt, damit du sehen kannst, wohin die Tokens fließen, und einen Agenten mit seltsamem Verhalten debuggen kannst. Er wird beim Öffnen des Tabs und beim Drücken von **Aktualisieren** erstellt (nicht während der Lauf arbeitet, denn dabei werden die Tokens jedes Teils gezählt).

- Drei Diagramme: die **Arten von Teilen** im Verhältnis zum Limit des Kontexts (der Prompt des Agenten, deine Anweisungen, die Notizen des Runners, die Werkzeugdefinitionen, die Zusammenfassung einer Verdichtung, deine Nachrichten, die Antworten des Agenten, die Ergebnisse seiner Aufrufe und was frei ist), **jedes Teil in der Reihenfolge, in der es gesendet wird** (ein Balken für jedes, in der Farbe seiner Art; ein Werkzeug mit einer riesigen Beschreibung oder ein Ergebnis, das den Kontext füllt, fällt auf), und den **Kontext bei jeder Antwort des Modells**: ein gestapelter Balken aus dem, woraus er bestand (dieselben Arten, geschätzt), die Größe, die das Modell **gemeldet** hat, als Linie, und das Limit als gestrichelte Linie. Wo die Linie über den Balken liegt, hat das Modell mehr gezählt als die Schätzung (Bilder, das Format des Anbieters); ein Balken, der fällt, ist eine Verdichtung oder ein Zurückspulen.
- Eine **Tabelle der Teile** mit Art, Name, Tokens und Anteil am Kontext. Wähle eine Zeile, um **genau den Text zu lesen, den das Modell** für diesen Teil erhält: einen Abschnitt der Systemnachricht, die Beschreibung und Parameter eines Werkzeugs, eine Nachricht, die Argumente eines Aufrufs oder das Ergebnis eines Aufrufs.
- **Herunterladen** gibt den gesamten Kontext als eine Markdown-Datei aus (die Systemnachricht mit ihren Abschnitten, jedes Werkzeug mit seiner Beschreibung und seinem Schema, die Unterhaltung Nachricht für Nachricht, jeder Text in einem Codeblock), **Kopieren** legt denselben Text in die Zwischenablage.

![Der Tab Kontext](../images/agent-context.png)

Die Größen der Teile sind Schätzungen; die Zahl, die das Modell gemeldet hat, ist die echte (die Anfrage und ihre Antwort). Im Diagramm der Antworten sind der Prompt, die Anweisungen und die Werkzeuge so, wie sie jetzt sind (eine ältere Antwort hat vielleicht andere gehabt, wenn du sie geändert hast), die Unterhaltung ist so, wie sie damals war: eine Zusammenfassung hat das ersetzt, was davor stand, und was ein Zurückspulen rückgängig gemacht hat, fehlt in den Antworten danach (eine Verdichtung, die selbst zurückgespult wurde, wird nicht wiederhergestellt; die Balken danach sind etwas zu klein). Die Systemnachricht und die Werkzeuge sind jene, die die nächste Anfrage verwenden würde, daher zeigt sich eine Änderung des Prompts, der Anweisungen oder der erlaubten Befehle sofort. Die Nachrichten sind das gespeicherte Protokoll, ohne das, was ein Fortsetzen hinzufügt (ein „weiter“, die Antworten auf Aufrufe, die eine Unterbrechung offen gelassen hat). Die Begründung ist nicht Teil des Kontexts (das Modell bekommt sie nicht zurückgeschickt), und Bilder erscheinen mit ihren Dateinamen. Was eine Verdichtung ersetzt hat, ist ebenfalls nicht im Kontext; der Tab **Lauf** und der Download von **Läufe** haben es noch.

## Checkpoints

Ein **Checkpoint** ist ein Wiederherstellungspunkt der Arbeitskopie des Laufs (die Teile, Zusammenfassungen, Lorebooks und die Daten der Erweiterungen, mit den Griffen, die der Agent bekommen hat) und des Arbeitsbereichs des Agenten, damit ein Auftrag, der schiefgegangen ist, rückgängig gemacht werden kann: Der Agent geht mit `checkpoint_restore` zurück, oder du tust es im Tab **Checkpoints** der Konsole. Wer sie erstellt:

- **Start des Laufs**, erstellt, wenn der Lauf beginnt. Er wird nie entfernt.
- **Automatisch**, erstellt vom Lauf vor einer Runde, in der der Agent etwas aufruft, das das Buch oder den Arbeitsbereich verändern kann (ein Befehl, der nicht nur liest, oder ein Dateiwerkzeug, das schreibt), und nur dann, wenn sich seit dem letzten Checkpoint etwas geändert hat. Die letzten fünf werden behalten; der älteste geht, wenn ein neuer erstellt wird.
- **Absichtlich erstellt**, vom Agenten mit `checkpoint` (er wird angewiesen, vor einem riskanten Schritt einen zu erstellen) oder von dir mit **Einen Checkpoint speichern**. Sie bleiben, bis sie gelöscht werden.

![Der Tab Checkpoints](../images/agent-checkpoints.png)

Alle Checkpoints eines Laufs teilen sich ein **Kontingent**, das die Administration in der [Berechtigung](administration/permissions.md#settings-of-a-permission) festlegt (leer bedeutet 100 MB). Ein Teil davon ist für die automatischen reserviert, damit der Agent nicht alles aufbrauchen kann; ein absichtlich erstellter Checkpoint, der nicht hineinpasst, wird **abgelehnt**, und es wird nichts hinter dem Rücken dessen gelöscht, der darum gebeten hat. Der Agent bekommt dann die Liste und löscht, was er nicht braucht (nicht den Checkpoint des Starts und nicht den neuesten); du kannst jeden im Tab löschen. Jeder ist ein Archiv (eine ZIP-Datei mit dem Buch als JSON, den Paaren und den Dateien des Arbeitsbereichs), das du **herunterladen** kannst.

**Wiederherstellen** macht die Arbeitskopie aus dem Checkpoint neu: Es ist eine neue Kopie, die alte wird gelöscht, der Arbeitsbereich ist der gespeicherte (danach geschriebene Dateien sind weg), und die Griffe des Agenten für die Elemente, die vorhanden waren, funktionieren weiter. Der Zustand davor wird zuerst als automatischer Checkpoint gespeichert, wenn Platz ist, damit eine Wiederherstellung zurückgenommen werden kann. Eine Wiederherstellung dauert ungefähr so lange wie der Start eines Laufs (eine halbe Minute für ein Buch mit zweitausend Teilen). Du kannst wiederherstellen, während der Agent nicht arbeitet; der Agent stellt sich selbst zwischen zwei Runden wieder her und wird darüber informiert, was zurückgekommen ist. Die Checkpoints verschwinden, wenn der Lauf seine Kopie freigibt.

**Zurückspulen** ist eine Wiederherstellung, die auch die *Unterhaltung* zurücknimmt. Eine reine Wiederherstellung lässt den Agenten sich an Arbeit erinnern, die nicht mehr im Buch steht; daher braucht ein Lauf, den du in eine andere Richtung schicken willst, ein Zurückspulen: Die Arbeitskopie und der Arbeitsbereich gehen zum Checkpoint zurück, die Nachrichten nach der Stelle, an der der Checkpoint erstellt wurde, sind im Protokoll ausgegraut (du kannst sie noch lesen, und der Export enthält sie), und der Agent liest sie nicht mehr; ein Hinweis teilt dem Agenten mit, was geschehen ist, damit er die Arbeit neu macht, falls sie noch gebraucht wird. Der Lauf muss angehalten oder gestoppt sein (einen Agenten, der auf dich wartet, hältst du zuerst an). Danach **Setzt** du ihn mit **Fortsetzen** fort, und er geht vom Hinweis aus weiter. Auf einen Checkpoint kann nur zurückgespult werden, solange seine Stelle in der Unterhaltung noch Teil von ihr ist: Nach einem Zurückspulen auf einen früheren Checkpoint können die späteren (der rückgängig gemachten Unterhaltung) nur wiederhergestellt werden. Ein Checkpoint, der vor dem Zurückspulen erstellt wurde, kennt seine Stelle nicht und kann nur wiederhergestellt werden. Was eine Verdichtung nach dem Checkpoint bewirkt hat, wird mit dem Zurückspulen rückgängig gemacht; der Agent liest die Nachrichten, die die Verdichtung ersetzt hatte, wieder.

**Zurück zu einer Nachricht.** Du musst keinen Checkpoint auswählen: Jede Nachricht, die du an den Agenten geschrieben hast, hat im Protokoll des Tabs **Lauf** eine Schaltfläche **Zurückspulen** darunter. Sie nimmt die Unterhaltung auf den Stand vor dieser Nachricht zurück: Der Agent vergisst die Nachricht und alles danach, und die Arbeitskopie und der Arbeitsbereich gehen zum neuesten Checkpoint zurück, der vor ihr erstellt wurde (der Runner erstellt einen vor jeder Runde, die etwas verändert, daher trifft das meist genau die Nachricht). Der Zustand eines Laufs ist nur an seinen Checkpoints bekannt; wenn der nächste davon älter ist als die Nachricht, werden auch die Nachrichten zwischen diesem Checkpoint und deiner vergessen. Die Bestätigung sagt, um welchen Checkpoint es sich handelt und wie viele Nachrichten wegfallen. Die vergessenen Nachrichten bleiben im Protokoll, ausgegraut. Die Schaltfläche funktioniert, während der Agent auf dich wartet (er ist dafür angehalten), angehalten ist oder gestoppt und behalten wurde; während der Agent arbeitet, ist sie deaktiviert. Danach setzt du den Lauf mit **Fortsetzen** fort, und er geht vom Hinweis aus weiter.
<a id="creating-and-editing-an-agent"></a>

## Einen Agenten anlegen und bearbeiten

**Neuer Agent** öffnet dasselbe Fenster wie **Bearbeiten**:

![Das Fenster eines neuen Agenten](../images/agent-dialog.png)

| Feld | |
|---|---|
| **Name** | Pflichtfeld. |
| **Aktiviert** | Ein deaktivierter Agent kann nicht gestartet werden; seine vorhandenen Läufe bleiben unberührt. |
| **Buch** | Das Buch, an dem der Agent arbeitet, Pflichtfeld. Es werden nur Bücher angeboten, die noch keinen Agenten haben, und das Buch eines Agenten kann später nicht mehr geändert werden. |
| **Modell**, **Protokolle** | Das Modell und das Protokoll, über die der Agent kommuniziert. Beide oder keines; ein Agent ohne sie kann nicht gestartet werden. |
| **Das Buch mit dem Modell des Agenten schreiben** | Eingeschaltet (der Standard): Die Befehle, die ein Modell verwenden, schreiben mit dem oben gewählten Modell, egal welches Modell dein Buch hat. Dazu gehören der nächste Zug, Swipes, Zusammenfassungen und die Befehle von Erweiterungen wie dem Lore-Generator und dem Handlungsbogen-Entwurf. Ausgeschaltet: Sie verwenden das Modell deines Buches (ein Erweiterungsbefehl greift auf dein Standardmodell aus den [Einstellungen](account.md) zurück, wenn das Buch keines hat). So kannst du Modelle mischen: ein starkes, das denkt, und ein günstigeres oder flüssigeres, das schreibt. Ein Befehl kann trotzdem ein anderes Modellprofil des Agenten benennen. |
| **Aufrufe in einer Antwort** | Wie viele der Aufrufe, die der Agent in einer Antwort schreibt, tatsächlich ausgeführt werden. *Alle zusammen, Stapelrichtlinien ignorieren*: jeder Aufruf (der schnellste). *Zusammen, als „Allein“ markierte Befehle für sich* (der Standard): Die Aufrufe werden zusammen ausgeführt, aber ein Befehl, dessen Spalte **Stapel** den Wert *Allein* hat, wie einer, der einen Aufruf an ein Modell macht, wird in einer eigenen Antwort ausgeführt, und der Agent wird darüber informiert. *Ein Aufruf je Antwort*: Nur der erste Aufruf wird ausgeführt, und der Agent schreibt den nächsten erst, nachdem er das Ergebnis gelesen hat; langsamer (jeder Aufruf ist eine eigene Runde mit dem Denken des Modells), dafür kann er auf jedes Ergebnis reagieren. Ein **Fehler beendet die Antwort immer**, in jedem Modus: Die Aufrufe nach dem fehlgeschlagenen werden nicht ausgeführt (sie werden mit „nicht ausgeführt, weil ein früherer Aufruf fehlgeschlagen ist“ beantwortet), und der Agent liest zuerst den Fehler. |
| **Das Modell sieht Bilder** | Anhaken, wenn das Modell Bilder verarbeiten kann (die API des Modells sagt das nicht, also musst du es angeben). Der Agent bekommt dann `view_image`, und du kannst deinen Nachrichten Bilder anhängen; ohne diese Option erhält er nie ein Bild. |
| **Sprache** | Die Sprache aller Texte, die der Agent liest: die Beschreibungen seiner Befehle, deren Ergebnisse und Fehler sowie die eingebauten Texte des Laufs (sein Standardsystemprompt, die Hinweise des Laufs, der Verdichtungsprompt), sofern sie in dieser Sprache vorliegen (Englisch, Japanisch, Chinesisch). Die [Promptsprache](account.md#prompt-language) deiner Einstellungen gilt nicht für Agenten. Englisch ist der Standard und die sicherste Wahl, weil die Befehle und ihre Parameter unabhängig von der Sprache auf Englisch sind (die Beschreibungen in einer anderen Sprache mischen beides), und Modelle, vor allem kleine, verwenden Werkzeuge so zuverlässiger. Die Liste enthält die in der Anwendung installierten Sprachen (eine Erweiterung kann eine weitere hinzufügen, etwa die chinesische Lokalisierung). Sie hat **nichts mit der Sprache des Buches zu tun**: Dem Agenten wird gesagt, den Text des Buches in der Sprache des Buches zu schreiben, die er mit `get-book-info` lesen kann. Ein Lauf behält die Sprache, mit der er gestartet wurde; eine Änderung gilt für den nächsten Lauf. |
| **Destruktive Befehle** | Was geschieht, wenn der Agent einen Befehl ausführen möchte, der Daten ändert oder entfernt: *Ausführen*, *Erst fragen* (ein [Fenster](#asking-you) bittet dich um Freigabe) oder *Ablehnen*. |
| **Maximale Runden**, **Token-Budget eines Laufs**, **Kontextlimit** | Optionale Grenzen eines Laufs. Das Limit, das dir dein Administrator vorgegeben hat, gilt in jedem Fall. |
| **Maximale Anzahl Unteragenten** | Wie viele [Unteragenten](#sub-agents) der Agent gleichzeitig arbeiten lassen kann, 0 bis 10. Leer oder null bedeutet keine. |
| **Modelle für Unteragenten** | Die [Modelle](#models-for-sub-agents), auf denen ein Unteragent laufen darf, und ob er auch auf dem Hauptmodell laufen darf. |
| **Verdichten bei (% des Kontexts)** | Wenn der Kontext des Modells diesen Anteil seines Limits überschreitet und der Agent seine Arbeit nicht selbst [verdichtet](#running-a-command-yourself) hat, übernimmt der Lauf das: Der Agent schreibt die Zusammenfassung seiner bisherigen Arbeit, die sie ersetzt. Leer bedeutet 90, null nie (der Lauf stoppt dann, wenn der Kontext voll ist), sonst 50 bis 99. |
| **Systemprompt** | Was dem Agenten als Rolle vorgegeben wird. Lässt du es leer, folgt der Agent dem Standard – deinem aus den [Einstellungen](account.md#templates), sonst dem deines Administrators, sonst dem eingebauten –, der grau angezeigt wird. Das Info-Symbol hat **Standardvorlage einfügen**, das den Standard einfügt, damit du eine Kopie bearbeiten kannst; leere das Feld wieder, um dem Standard zu folgen. |
| **Verdichtungsprompt** | Was der Agent gefragt wird, wenn du ihn [`/compact`](#running-a-command-yourself): Er soll die Zusammenfassung schreiben, die seine bisherige Arbeit ersetzt. Leer folgt dem Standard, wie beim Systemprompt, mit demselben Info-Symbol. |
| **Anweisungen (agents.md)** | Deine Anweisungen an den Agenten, die in jedem Lauf seinem Prompt hinzugefügt werden. Der Agent kann sie lesen, aber nie ändern. Leer folgt deinem Standard aus den [Einstellungen](account.md#templates), falls du einen hast (er wird grau angezeigt), sonst gibt es keine. Das Info-Symbol hat **Standardvorlage einfügen**, das deinen Standard einfügt, damit du eine Kopie bearbeiten kannst. Darunter steht eine Zeile, die angibt, wie viele Tokens der Prompt, die Anweisungen und die angehakten Befehle **bei jeder Anfrage** ausmachen (eine Schätzung, zuzüglich der Werkzeuge für Dateien und für den Lauf sowie der Hinweise des Laufs). |
| **Erlaubte Befehle** | Siehe unten. |

![Die Prompts des Agenten und die Hinweise der Standardvorlage](../images/agent-dialog-prompts.png)

<a id="allowed-commands"></a>

### Erlaubte Befehle

Dem Agenten werden nur die Befehle angeboten, die du anhakst. Die Tabelle listet jeden Befehl mit seiner Wirkung auf (*Destruktiv* ändert oder entfernt Daten, *Nicht destruktiv* liest nur oder fügt nur hinzu). Das Feld **Auswahl** hakt viele auf einmal an: **Alle**, **Keine** oder **Alle nicht destruktiven** (der Standard für einen neuen Agenten) oder eine Rolle; wenn du selbst einen Befehl anhakst, wird die Auswahl *Benutzerdefiniert*. Das Feld **Befehle suchen** über der Tabelle zeigt nur die Befehle, die alle Wörter enthalten, die du eingibst (in beliebiger Reihenfolge, im Namen oder in der Beschreibung); die Auswahl gilt weiterhin für alle Befehle, auch für die ausgeblendeten. Befehle, die eine Erweiterung mitbringt, bleiben erlaubt, auch wenn die Erweiterung nicht geladen ist; du siehst sie dann nur nicht. Die Spalte **Stapel** zeigt, ob der Befehl *Zusammen* mit anderen Aufrufen in einer Antwort ausgeführt wird oder *Allein* (ein Befehl, der einen Aufruf an ein Modell macht, wie `next`, `swipe`, `regenerate`, `redraft-part`, `generate-annotation` oder die Verdichtung, läuft allein; siehe unten **Aufrufe in einer Antwort**). Alle Spalten lassen sich in der Breite ändern. Die Spalte **Tokens** zeigt, was die Definition des Befehls (Name, Beschreibung und Parameter) zu jeder Anfrage des Agenten hinzufügt, und die Zeile unter den Anweisungen addiert die angehakten. Die Gesamtmenge liegt bei etwa 20 000 Tokens, daher lässt eine kleinere Auswahl (eine Rolle) mehr Platz im Kontext für die Arbeit.

![Die erlaubten Befehle mit den Rollen im Feld Auswahl](../images/agent-commands.png)

Die Rollen sind Befehlssätze für eine bestimmte Aufgabe. Alle können das Buch und die Lorebooks lesen (jeder Befehl, der nur liest, auch die von Erweiterungen), und keine kann Daten entfernen:

| Rolle | Neben dem Lesen |
|---|---|
| **Prüfer** | Nichts: Kontinuitätsprüfungen, Prüfung von Namen und Begriffen, Berichte. |
| **Lore-Hüter** | Legt Lorebook-Einträge und Lorebooks an und aktualisiert sie, außerdem deren Tags und Unter-Lorebooks. |
| **Lektor** | Bearbeitet und ergänzt Teile, setzt deren Felder, pflegt die Zusammenfassungen (lässt sie auch vom Modell neu schreiben) sowie die Buchinformationen und Tags. |
| **Entwerfer** | Fügt Teile und Verzweigungen hinzu, bearbeitet und ergänzt sie, lässt das Modell den nächsten Zug schreiben (`/next`, `/swipe`), pflegt die Zusammenfassungen und legt Lorebook-Einträge für das Erfundene an bzw. aktualisiert sie. |

Jeder angehakte Befehl fügt seine Definition zu jeder Anfrage hinzu, daher ist eine Rolle auch der günstigste Weg zu einem kleinen Prompt für ein schwächeres Modell.
<a id="on-a-phone"></a>

## Auf dem Telefon

Das Telefon-Layout hat in der Schublade einen Reiter **Agenten**, für Benutzer, die Agenten verwenden dürfen, mit der Anzahl der Agenten, die deine Aufmerksamkeit brauchen, neben dem Namen. Jeder Agent ist eine Karte mit seinem Namen, seinem Zustand und den Schaltflächen der Tabelle (sie brechen in mehrere Zeilen um). Die Fenster - die Konsole, die Prüfung, die Einstellungen des Agenten - füllen den Bildschirm, und die Prüfung zeigt die Tabelle oberhalb des Details des Eintrags. Alles funktioniert wie auf dem Desktop, nur kleiner. Die Konsole ist für den Daumen angelegt:

![Der Reiter Agenten auf einem Telefon](../images/phone-agents.png)

![Die Konsole auf einem Telefon](../images/phone-agent-console.png)

![Der Reiter Agenten auf einem Telefon](../images/phone-agents.png)

![Die Konsole auf einem Telefon](../images/phone-agent-console.png)

- **Die Reiter sind Symbole** (Lauf, Läufe, Arbeitsbereich, Kontrollpunkte, Kontext, Buchkopie, Unteragenten, Diagnose); der Titel des Fensters nennt den geöffneten.
- **Pausieren, Jetzt pausieren, Fortsetzen, Beenden und Stopp sind Symbolschaltflächen unten, und nur die, die jetzt benutzt werden können, werden angezeigt**; **Änderungen prüfen** behält seinen Text, denn darauf wartet ein beendeter Lauf. **Aktualisieren** und **Schließen** sind Symbole in der Kopfzeile.
- **Das Nachrichtenfeld ist eine Zeile** mit den Symbolen **Senden** und **Bild anhängen** daneben. Die **Befehlszeile** (das Terminal-Symbol) ist ausgeblendet, bis du sie öffnest, damit die Tastatur das Protokoll nicht unnötig verdeckt. Die Nachrichten des Protokolls nutzen die ganze Breite.
- **Tabellen sind Listen von Karten**: ein Lauf, ein Kontrollpunkt, ein Befehl aus den Einstellungen des Agenten, ein Eintrag der Prüfung zeigen jeweils ihren Namen und Zustand, die Details darunter und ihre Schaltflächen unten, anstelle von Spalten. Die Dateien des Arbeitsbereichs zeigen ihre Größe unter dem Namen, und die Werkzeugleiste des Arbeitsbereichs besteht aus Symbolen. Die Kontexttabelle behält Nummer, Name und Tokens; Typ und Anteil eines Teils stehen unter dem Text des gewählten Teils.

- **Die obere Leiste zeigt, wer auf dich wartet.** Solange die Schublade geschlossen ist, zählt eine rote Zahl in der oberen Leiste (neben dem Titel des geöffneten Teils) die Agenten, die deine Aufmerksamkeit brauchen, wie das Abzeichen des Reiters **Agenten** in der Schublade; tippe darauf, um zum Reiter **Agenten** zu gelangen. Sie ist ausgeblendet, wenn niemand wartet, während die Schublade geöffnet ist und im Reiter Agenten selbst.
- **Mit Wischen zwischen den Teilen wechseln.** Ein Wischen seitlich über den Inhalt führt zum nächsten Teil der Schublade (nach links wischen) oder zum vorherigen (nach rechts). Es tut nichts, wenn die Schublade geöffnet ist oder wenn die Anwendung die Navigation sperrt, und eine Berührung, die in einem Textfeld, einer Tabelle oder etwas beginnt, das seitlich scrollt, bleibt dem jeweiligen Element überlassen.

Für lange Arbeit mit dem Protokoll oder der Prüfung ist ein größerer Bildschirm weiterhin bequemer.

<a id="cloning"></a>

## Klonen

**Klonen** fragt nach einem Namen und einem Buch, das keinen Agenten hat, und kopiert die Definition des Agenten und seine Dateien (nicht seine Läufe) in einen neuen Agenten dieses Buches. Du kannst so viele Agenten haben, Klone eingeschlossen, wie dein Administrator erlaubt (fünf, sofern er keine andere Zahl festlegt); am Limit sind **Neuer Agent** und **Klonen** deaktiviert und sagen, warum.

![Einen Agenten klonen](../images/agent-clone.png)

<a id="lore-notes-of-an-imported-book"></a>

## Lore-Notizen eines importierten Buches

Nachdem du ein Manuskript [importiert](books/story-editor.md#after-an-import) hast, bietet die Anwendung einen Agenten an, der die Lore des Buches übernimmt:
**Lore-Notizen mit einem Agenten erstellen...** (derselbe Eintrag steht im Einstellungsmenü ⚙ einer Geschichte, **Analyse**). Zuerst fragt er nach:

![Die Frage vor dem Start des Agenten](../images/lore-notes-confirm.png)

Dann erstellt er einen Agenten namens **Lore-Notizen** für das Buch, mit dem Modell und dem Protokoll des Buches, und startet ihn (seine Konsole öffnet sich).

- Er liest die Geschichte vom ersten bis zum letzten Teil, einige Teile auf einmal, und hält seine Notizen in den Dateien seines Arbeitsbereichs fest (Figuren, Orte, Dinge, Begriffe), damit er nach einer Pause weitermachen kann. Die Zusammenfassungen helfen ihm, sich zurechtzufinden; die Teile sind seine Quelle.
- Danach schreibt er für jede Figur, jeden Ort, jedes Ding, jede Gruppe oder jeden Begriff einen Eintrag in das Lexikon des Buches (für ein Buch wird ein Lexikon angelegt, wenn es keins hat), mit den Namen, die die Geschichte verwendet, als Filter, in der Sprache des Buches. Er ergänzt Fakten zu einem vorhandenen Eintrag und schreibt dieselbe Sache nicht zweimal.
- Er darf die Geschichte und die Lexika lesen und Einträge schreiben; er kann die Geschichte nicht verändern und nichts löschen.

Er ist ein Agent wie jeder andere: Das Buch ist [gesperrt](#the-book-is-locked), während er arbeitet, er arbeitet an einer [Kopie](#the-working-copy), und was er geschrieben hat, gehört erst dir, nachdem du es [prüfst](#reviewing-the-result) und übernimmst. Lies die Einträge, bevor du das tust: Er weiß nur, was im Text steht, und ein langes Buch kostet viele Anfragen. Die Anweisungen sind die des Agenten (**Bearbeiten** zeigt sie, du kannst sie vor dem nächsten Start ändern).
Er wird abgelehnt, wenn du keine Agenten verwenden darfst, wenn das Buch kein Modell hat und wenn das Buch bereits einen Agenten hat (ein Buch hat genau einen).

<a id="the-book-is-locked"></a>

## Das Buch ist gesperrt

Während ein Lauf eines Agenten arbeitet oder auf dich wartet, **ist sein Buch gesperrt**: Du kannst es lesen, aber nichts daran ändern, damit der Agent nie an Text arbeitet, der sich unter ihm verschiebt. Die Buchliste öffnet ein gesperrtes Buch nicht, sondern sagt dir, welcher Agent es hält, und Generieren, Bearbeiten, Suchen und Ersetzen, Zusammenfassungen und das Löschen des Buches werden abgelehnt. **Pausieren** oder **Stopp** den Lauf, um das Buch wieder zu bearbeiten; ein pausierter, gestoppter oder beendeter Lauf sperrt es nicht.

![Ein Buch, an dem ein Agent arbeitet, in der Buchliste](../images/agent-locked-book.png)

Die **Lexika des Buches** sind mit ihm gesperrt: das Lexikon, das das Buch verwendet, und seine Unterlexika. Auf dem Reiter Lexika wird ein solches Lexikon auf dem Telefon als reiner Text ohne jede Bearbeitung angezeigt, mit einem Hinweis, welcher Agent es hält; **Aktualisieren** zeigt es wieder zur Bearbeitung an, nachdem du den Lauf pausiert oder gestoppt hast. Andere Lexika sind nicht betroffen.

<a id="agents-and-folders"></a>

### Agenten und Ordner

Ein Buch in einem [Ordner](books/folders.md), an dem ein Agent arbeitet, behält seinen Platz und seine Einstellungen: Das Verschieben oder Löschen eines Ordners darüber sowie das Ändern einer Einstellung, die ein solcher Ordner weitergibt (Sprache, Vorlagen, Erzählperspektive, Zeitform, Stil, Prompts), wird unter Nennung des Buches und des Agenten abgelehnt. Das Umbenennen eines Ordners oder das Ändern seiner Beschreibung wird nicht abgelehnt. Ein pausierter, gestoppter oder beendeter Lauf gibt sie wieder frei.

![Das Lexikon eines Buches, an dem ein Agent arbeitet](../images/agent-locked-lorebook.png)
