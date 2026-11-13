---
label: Agenten (experimentell)
order: 958
icon: cpu
---

# Agenten (experimentell)

!!!warning Experimentell
Agenten sind neu in 1.4.0 und wurden mit echten Modellen bisher kaum genutzt. Rechne mit Ecken und Kanten: Werkzeugaufrufe, die ein schwaches Modell falsch ausführt,
Läufe, die mehr Tokens verbrauchen als gedacht, einen Prüfbildschirm, der mehr Arbeit macht als nötig, und ein Verhalten, das sich
zwischen Versionen ändert. Dein Buch ist geschützt (der Agent arbeitet an einer Kopie, das Ergebnis wird geprüft und vor dem
Übernehmen wird ein Backup angelegt), aber **behalte selbst ein Backup eines Buches, bevor du das Ergebnis eines Agenten übernimmst**, setze ein Token-Budget und beobachte die
ersten Läufe in der [Konsole](#the-console). Berichte über Probleme sind willkommen.
!!!

Ein **Agent** ist ein Modell, das eigenständig und über lange Zeit an einem deiner Bücher arbeitet und dabei die
[Befehle](commands/index.md), die du ihm erlaubst, als seine Werkzeuge aufruft: Er kann die Geschichte lesen, sie mit dem Lorebook abgleichen und dir berichten.
Auf dem Tab **Agenten** legst du sie an, legst fest, was jeder darf, startest und stoppst ihre Läufe, sprichst in der [Konsole](#the-console) mit ihnen und klonst sie.

**Starten** lässt den Agenten laufen: Er arbeitet im Hintergrund, Runde für Runde, ruft die Befehle auf, die du erlaubt hast, sowie seine eigenen Dateiwerkzeuge, bis er
fertig ist, dich etwas fragt, eine Grenze erreicht oder du ihn pausierst oder stoppst (siehe [Ein Lauf](#a-run)). Er arbeitet auf einer versteckten [Kopie](#the-working-copy) des
Buches, dein Buch wird also während der Arbeit nie angefasst (das Buch selbst ist [gesperrt](#the-book-is-locked)). Wenn er fertig ist, [prüfst du sein Ergebnis](#reviewing-the-result) und übernimmst, was du willst.

Der Tab ist nur sichtbar, wenn ein Administrator dir die [Berechtigung](administration/permissions.md) **Agenten** gegeben hat (Administratoren haben sie immer,
aber für alle gelten dieselben Grenzen: die Standardwerte, außer der Administrator füllt die Einstellungen seiner eigenen Berechtigung aus. Wer mehr Agenten, Läufe oder Platz im Arbeitsbereich braucht, stellt das dort ein. Eine geänderte Grenze gilt für einen laufenden Lauf, der pausiert und dann fortgesetzt wird). Auf einem Telefon ist jeder Agent eine Karte mit denselben Schaltflächen, siehe [Auf dem Telefon](#on-a-phone).

<a id="the-table"></a>

## Die Tabelle

| Spalte | |
|---|---|
| **Agenten-ID** | Die Kennung des Agenten. |
| **Name** | Der Name, den du ihm gegeben hast. |
| **Buch** | Das Buch, an dem der Agent arbeitet. |
| **Status** | Was sein Lauf gerade tut: *Läuft*, *Wartet auf dich*, *Wartet auf Zustimmung*, *Wird pausiert*, *Pausiert*, *Stoppt / Wird gestoppt*, *Gestoppt, Ergebnis behalten*, *Fertig, wartet auf die Prüfung*, *Angewendet*. Ein Agent ohne Lauf zeigt *Läuft nicht*, oder *Deaktiviert*, wenn er ausgeschaltet ist. |
| **Letzter Lauf** | Wann der neueste Lauf des Agenten begann, *nie* bei einem Agenten ohne Lauf. |

![Der Tab Agenten](../images/agents-tab.png)

Über der Tabelle grenzen zwei Felder die Liste ein: **Name** und **Buch** zeigen nur die Agenten, deren Name bzw. Buchname alle Wörter enthält, die du eingibst, in beliebiger Reihenfolge und unabhängig von Groß- und Kleinschreibung. Die Überschriften **Name**, **Buch**, **Status** und **Letzter Lauf** sortieren die Tabelle (erneut klicken kehrt die Reihenfolge um; Agenten, die nie gelaufen sind, stehen bei aufsteigender Sortierung nach **Letzter Lauf** vorn). Auf einem Telefon ordnet eine Auswahl **Sortieren nach** über den Karten sie nach Name, Status oder dem neuesten Lauf zuerst.

Die Schaltflächen am Ende einer Zeile hängen vom Lauf ab:

| Schaltfläche | |
|---|---|
| **Starten** | Öffnet ein Menü. **Sofort starten**: Der Agent bearbeitet selbstständig seine Aufgabe (seine Anweisungen), und du erfährst davon, wenn er nachfragt oder fertig ist. **In der Konsole starten**: Öffnet die [Konsole](#the-console) und sagt dem Agenten, dass er noch nicht arbeiten, sondern dich sofort fragen soll, was du von ihm willst; der Lauf ist dann eine [Unterhaltung](#a-run), die weiterläuft, bis du **Beenden** drückst. |
| **Fortsetzen** | Führt einen pausierten oder gestoppten Lauf weiter (wird statt **Starten** angezeigt). Es fragt nach einer optionalen Notiz an den Agenten, siehe [Einen Lauf fortsetzen](#resuming-a-run). |
| **Pausieren** | Pausiert einen Lauf, der arbeitet oder auf dich wartet. |
| **Jetzt pausieren** | Wird angezeigt, während der Agent arbeitet: Pausiert ihn, ohne das Ende der Runde abzuwarten, siehe [Pause und Stopp](#a-run). |
| **Konsole** | Öffnet die [Konsole](#the-console) des Agenten. |
| **Stopp** | Fragt, ob das **Ergebnis behalten** werden soll (der Lauf kann später fortgesetzt werden) oder ob es **verworfen** wird. |
| **Bearbeiten** | Öffnet den Agenten. |
| **Klonen** | Kopiert den Agenten, siehe [Klonen](#cloning). |
| **Löschen** | Verschiebt den Agenten mit seinen Läufen in den [Papierkorb](trash.md). |

Das Starten wird unter Angabe des Grundes verweigert, wenn der Agent deaktiviert ist, kein Modell hat oder du bereits so viele Läufe hast, wie du gleichzeitig ausführen darfst.

![Das Menü der Schaltfläche Starten](../images/agent-start-menu.png)

Ein Agent hat höchstens einen Lauf, der nicht beendet ist, und ein Buch hat höchstens einen Agenten.
<a id="a-run"></a>

## Ein Lauf

Ein Lauf ist die Aufgabe des Agenten. Er arbeitet in Runden: Das Modell wird einmal befragt, antwortet mit Text und/oder Aufrufen seiner Werkzeuge, die Ergebnisse gehen zurück, und so weiter.
Jede Runde wird gespeichert, daher kann ein Lauf pausiert und später fortgesetzt werden, auch nachdem der Server neu gestartet wurde. Der Agent hat die Befehle, die du erlaubt hast, und diese eigenen:

| Werkzeug | |
|---|---|
| Dateien | `list_files`, `read_file`, `write_file`, `append_file`, `replace_in_file`, `regex_replace_in_file`, `grep_files`, `move_file`, `copy_file`, `merge_files`, `unzip_file`, `delete_file` in seinem eigenen Arbeitsbereich (Notizen, die er zwischen den Läufen behält). Neben dem Arbeitsbereich sieht er die schreibgeschützte Datei `/AGENTS.md`, deine Anweisungen so, wie der Lauf sie beim Start hatte; er kann sie jederzeit erneut lesen und niemals ändern. `list_files` und `grep_files` nehmen ein Namensmuster (`glob`, zum Beispiel `*.md` oder `chapters/*.md`), `copy_file` kopiert einen Ordner mit allem darin (ein schneller Weg, eine Version der Notizen zu behalten), `merge_files` fügt mehrere Dateien zu einer zusammen, ohne dass der Text durch das Modell läuft (der letzte Namensteil eines Pfads darf ein Muster sein, etwa `/chapters/*.md`), `unzip_file` entpackt eine [Zip-Datei](#unpacking-a-zip-file) des Arbeitsbereichs in einen neuen Ordner. `replace_in_file` und `regex_replace_in_file` nehmen auch einen **Ordner** als Pfad: Sie ändern jedes Vorkommen in allen Textdateien darunter (nur die Dateien, deren Name zu `glob` passt, falls angegeben) und melden `path: n replacements` für jede Datei. Das ist ein **Probelauf** (nichts wird geschrieben, der Bericht zeigt, was sich ändern würde), außer der Agent übergibt `dry_run` als false; es gilt alles oder nichts (kann eine Datei nicht geschrieben werden, wird keine geändert) und es wird abgelehnt, ohne etwas zu ändern, wenn mehr als `max_files` (100, sofern der Agent nicht mehr angibt) Dateien sich ändern würden. Auch eine einzelne Datei kann ein Probelauf sein. `list_command_results` und `save_command_result` behalten das lange Ergebnis eines Befehls, ohne dass der Agent es erneut ausschreiben muss: Die letzten zehn Ergebnisse werden vollständig behalten (der Agent sieht ein langes abgeschnitten), und eines wird über seine Nummer in eine Datei gespeichert, das neueste, wenn keine angegeben ist; er gibt die Datei dann als `@file:/path` weiter. |
| `@file:/path` | Kein Werkzeug, sondern eine Art, eines aufzurufen: Ein Textargument eines Befehls, das genau `@file:/notes/chapter.md` lautet, wird durch den Inhalt dieser Datei des Arbeitsbereichs des Agenten ersetzt. So kann der Agent einen langen Text einmal in eine Datei schreiben, prüfen und an einen Befehl übergeben (das Ergebnis zeigt im Aufruf die kurze Referenz, der Befehl bekommt den Text). `write_file` und `append_file` nehmen es ebenfalls als Inhalt, wodurch eine Datei kopiert oder an eine andere angehängt werden kann: Eine lange Datei kann in Teilen geschrieben werden, jeder Teil innerhalb der längsten Antwort des Modells, und auf diese Weise zusammengefügt werden. |
| `@result` | Wie `@file:`, aber für das Ergebnis eines Befehls: Ein Textargument, das genau `@result` lautet, wird durch das vollständige Ergebnis des zuletzt aufgerufenen Befehls des Agenten ersetzt, `@result:3` durch das Ergebnis mit dieser Nummer (`list_command_results` zeigt die Nummern). Ein erzeugter Text gelangt so von einem Befehl zum nächsten oder in `write_file` und `append_file`, ohne dass der Agent ihn ausschreibt. |
| `search_manual`, `read_manual_page` | Etwas im Benutzerhandbuch von Marginalia nachschlagen (dieselben Seiten, die der Hilfe-Assistent liest): wie eine Prompt-Vorlage oder ein Makro funktioniert, was eine Einstellung des Lorebooks bewirkt. Nur lesend; jeder Agent hat sie. |
| `view_image` | Betrachtet eine Bilddatei des Arbeitsbereichs (PNG, JPEG, GIF oder WebP): Das Bild wird dem Modell in der nächsten Nachricht gezeigt. Nur ein Agent, dessen Modell als bildsehend markiert ist (**Das Modell sieht Bilder** im Agenten), hat es. Ein großes Bild wird vor dem Senden verkleinert, ein GIF zeigt sein erstes Bild, und nur die neuesten vier Bilder bleiben vor dem Modell (ein älteres wird zu einer Notiz, und der Agent kann erneut hinsehen). Die Bilder kommen von dir (**Datei hochladen** oder [an eine Nachricht angehängt](#the-console)); der Agent kann keines erzeugen. |
| Bilder für Befehle | Ein Befehl, der Bilder an ein eigenes Modell schickt, etwa [`generate-lore-entry`](extensions/lore-generator.md#commands), nimmt einen Parameter `images` entgegen: eine Liste von Pfaden zu Bilddateien des Arbeitsbereichs (höchstens 8). Es ist das Modell des Befehls, nicht das des Agenten, das sie sehen muss; so kann der Agent Bilder weitergeben, auch wenn sein eigenes Modell keine Bilder sieht. |
| `update_plan` | Seine To-do-Liste. |
| `report` | Ein Befund für dich (was er gefunden hat, wo, und wie schwer: *Notiz*, *Warnung* oder *Problem*). Der Agent arbeitet nach ihm weiter; du liest die Befunde in der [Konsole](#the-console) und im [Review](#reviewing-the-result), abgesehen von seinem Abschlussbericht. Ein Befund pro Aufruf, höchstens 200 pro Lauf. |
| `get_run_status` | Wie viele Runden und Token er verbraucht hat, wie viel Kontext er hat und die längste Antwort, die er schreiben kann; seinen Plan; und seine Unteragenten: eine Zeile für jeden mit Zustand, Runde, Token, letzter Tätigkeit und Aufgabe. Dem Agenten werden diese Grenzen seines Modells außerdem in seinem Prompt mitgeteilt, damit er einen langen Text in Teilen schreibt. |
| `checkpoint`, `checkpoint_list`, `checkpoint_diff`, `checkpoint_restore`, `checkpoint_delete` | Seine [Checkpoints](#checkpoints): speichert einen Wiederherstellungspunkt der Arbeitskopie und seines Arbeitsbereichs (mit einer Bezeichnung), listet sie auf, zeigt, was sich seitdem geändert hat (die Einstellungen, Teile, Lorebooks und Einträge des Buchs, die hinzugefügt, geändert oder gelöscht wurden, und die Dateien des Arbeitsbereichs; mit einem Pfad nur diese Datei oder diesen Ordner, und bei einer Textdatei die Sätze, die sich unterscheiden), kehrt zu einem zurück (wenn die Runde endet) und löscht einen, den er nicht braucht. |
| `get_sync_report` | Nur nach einer Fortsetzung, die Änderungen von dir übernommen hat: listet erneut auf, was du während der Pause im Buch geändert hast und was davon in die Kopie des Agenten gelangt ist, siehe [Einen Lauf fortsetzen](#resuming-a-run). |
| `request_compaction` | Verdichtet seinen eigenen Kontext: Wenn die Runde endet, schreibt er die Zusammenfassung seiner bisherigen Arbeit, wie bei `/compact`. Er wird angewiesen, vorher seine Notizen in einer Datei zu sichern. Siehe [Kontext wird voll](#running-a-command-yourself). |
| `ask_user` | Eine Frage an dich. Der Lauf wartet (*Wartet auf dich*). |
| `finish` | Sein Bericht: Der Lauf ist *Erledigt*. Der Bericht wird als Grund des Status angezeigt. |

So endet ein Abschnitt der Arbeit eines Laufs:

- **Wartet auf dich:** Der Agent hat ohne Werkzeugaufruf geantwortet oder dich etwas gefragt. Beantworte es in der [Konsole](#the-console).
- **Erledigt:** Der Agent hat `finish` aufgerufen (bei einem Lauf, den du *sofort* gestartet hast). Ein Lauf, den du *in der Konsole* gestartet hast, ist ein Gespräch: Der Bericht des Agenten kommt zu dir wie eine Frage, der Lauf wartet auf deine nächste Nachricht, und nur **Beenden** in der Konsole (oder **Stopp**) beendet ihn. Das Ergebnis eines *erledigten* Laufs wartet auf dein [Review](#reviewing-the-result).
- **Pausiert**, mit dem Grund: Die Grenze der Runden, das Token-Budget oder das Kontextlimit ist erreicht, das Modell konnte nicht befragt werden (ein Fehler des Anbieters, dein Limit für die KI), oder der Server wurde neu gestartet. **Fortsetzen** geht von der letzten vollständigen Runde weiter, siehe [Wenn der Server anhält](#when-the-server-stops).
- **Fehlgeschlagen:** Der Ausführer selbst ist abgebrochen; der Grund sagt, was geschehen ist.

**Pausieren** wirkt zwischen zwei Runden: Die Anfrage an das Modell und die laufenden Aufrufe werden zuerst beendet. Das kann lange dauern (ein langsames Modell, eine lange Generierung), daher gibt es **Jetzt pausieren**: Es bricht die Anfrage an das Modell ab, deren Antwort verworfen wird, und der laufende Aufruf beantwortet die Aufrufe der Runde, die nicht als solche ausgeführt wurden; der Lauf ist gleich darauf *Pausiert*. Was die verworfene Anfrage gekostet hätte, ist verloren; setze wie gewohnt fort. **Stopp** tut dasselbe: Es wartet nicht auf die Anfrage oder den laufenden Aufruf, diese werden abgebrochen, und der Lauf endet sofort (behalten oder verworfen, wie du entschieden hast).

![Die Frage, wenn ein Lauf gestoppt wird](../images/agent-stop.png)

Ein laufender Befehl wird aufgefordert zu stoppen, und ein Befehl, der so geschrieben ist, dass er gestoppt werden kann (die, die mit einem Modell schreiben, suchen und ersetzen), endet sofort und hinterlässt, was er bis dahin getan hat. Einer, der nicht gestoppt werden kann, darf trotzdem fertig werden; in jedem Fall wird der Agent darüber informiert, dass nicht bekannt ist, ob der Aufruf ausgeführt wurde, und er prüft beim Weitermachen den Zustand mit einem lesenden Befehl. Die Befehle der Unteragenten werden beim Pausieren, Stoppen oder wenn sofort eine Nachricht geschickt wird, ebenso abgebrochen.

Während der Agent auf einen freien Platz bei seinem Modell wartet (siehe [Ein Modell, mehrere Agenten](#one-model-several-agents)), ist er *Läuft*; ein **Pausieren** oder **Stopp** beendet dieses Warten sofort.
<a id="the-console"></a>

## Die Konsole

**Konsole** öffnet ein Fenster für den letzten Lauf des Agenten, mit acht Reitern: **Lauf**, **Läufe**, **Arbeitsbereich**, **Checkpoints**, **Kontext**, **Buchkopie**, **Unteragenten** und **Diagnose**.

![Ein in der Konsole gestarteter Lauf: der Agent fragt, was er tun soll](../images/agent-console-start.png)

**Lauf** zeigt das *Protokoll*: was du dem Agenten gesagt hast, was er geantwortet hat (beides als [Markdown](https://commonmark.org/help/), sodass Listen, Überschriften und Hervorhebungen als solche erscheinen) sowie jeden Befehl und jedes Dateiwerkzeug, das er aufgerufen hat, auf eine Zeile mit den Argumenten zusammengeklappt (klicke darauf, um sie und das Ergebnis, als Klartext angezeigt, zu sehen). Wenn das Modell seine **Begründung** (was es vor der Antwort gedacht hat) mitschickt, zeigt ein eingeklapptes Block *Begründung* sie vor der Antwort an, und der Download enthält sie. Ob du sie erhältst, hängt vom Modell und vom Server ab: Er muss sie melden, und deine KI muss das Denken eingeschaltet haben. Während eine Anfrage läuft, zeigt die Konsole, was das Modell bisher geschrieben hat (seine Begründung, geöffnet, damit du mitlesen kannst, und seinen Text), in einem Block mit der Aufschrift *Arbeitet ...* am Ende des Protokolls, oberhalb der wartenden Nachrichten; er wird durch die echten Blöcke ersetzt, wenn die Runde vorbei ist. Solange er angezeigt wird, liest die Konsole den Lauf zweimal pro Sekunde, sonst alle zwei Sekunden. Nur der Agent selbst wird auf diese Weise angezeigt, nicht seine Unteragenten, und was das Modell innerhalb eines Aufrufs schreibt (etwa eine lange Datei), erscheint erst, wenn die Runde vorbei ist. Jede Nachricht hat rechts neben ihrer ersten Zeile den Zeitpunkt, zu dem sie geschrieben wurde (die Uhrzeit, mit Datum, wenn es ein anderer Tag war; fahre mit der Maus darüber, um die vollständige Zeit zu sehen), damit du erkennen kannst, wie lange der Agent gearbeitet hat und wann er aufgehört hat. Der Zustand des Laufs, der **Kontext** (wie viele Token die letzte Anfrage vom Limit des Modells verbraucht hat, sodass du siehst, wie nahe der Lauf an einer Pause ist), die Runden und die verbrauchten Token (und das Token-Budget, falls es eins gibt) bleiben oberhalb des Protokolls und das Feld für deine Nachricht darunter stehen, während das Protokoll scrollt. Worauf der Agent wartet, steht im Protokoll an der Stelle, wo es geschrieben wurde, als eigene Sprechblase, die dort bleibt, wenn der Lauf weitergeht:

![Die Konsole, während der Agent arbeitet: eine Nachricht des Autors wartet in der Warteschlange](../images/agent-console-run.png)

- eine **Frage des Agenten**, wenn der Lauf *Wartet auf dich* ist; schreibe die Antwort in das Feld und drücke **Senden** (oder **Strg+Enter**), dann macht der Agent weiter;
- ein **Bericht des Agenten**, wenn er `finish` aufgerufen hat.

![Die Frage eines Agenten, mit den Antworten, die er vorschlägt](../images/agent-console-question.png)

![Der Bericht des Agenten am Ende einer Unterhaltung](../images/agent-console-report.png)

![Die Frage eines Agenten, mit den Antworten, die er vorschlägt](../images/agent-console-question.png)

![Der Bericht des Agenten am Ende einer Unterhaltung](../images/agent-console-report.png)

Die Zeile oberhalb des Protokolls zeigt den Zustand des Laufs und seine Zahlen: die Runden, die Größe des Kontexts im Verhältnis zu seiner Obergrenze, das **Antwortlimit** (die meisten Token, die das Modell des Agenten in einer Antwort schreiben kann; ein Aufruf, der mehr braucht, wird abgeschnitten, deshalb schreibt der Agent lange Texte in Teilen) und die Token insgesamt. Hat der Agent einen Plan erstellt (`update_plan`), erscheint dort ein Knopf **Plan**, mit den erledigten Schritten von allen (**Plan 2/5**); er öffnet die Schritte in einem Popover, jeder mit einer Markierung für *Erledigt*, *In Arbeit* oder *Offen*. Hat er Erkenntnisse gemeldet (`report`), öffnet ein Knopf **Erkenntnisse** mit ihrer Anzahl (**Erkenntnisse 3**) sie auf dieselbe Weise, jede mit ihrer Schwere. Die Erkenntnisse werden mit dem Lauf gespeichert, sind also nach einer Pause, einem Neustart und wenn du einen älteren Lauf im Verlauf ansiehst noch da; die Markdown-Datei eines Laufs (**Herunterladen**) enthält sie in einem Abschnitt **Erkenntnisse**.

![Der Plan des Agenten](../images/agent-console-plan.png)

![Die Erkenntnisse des Agenten](../images/agent-console-findings.png)

In einer Unterhaltung kannst du auch schreiben, während der Agent **arbeitet**. Die Nachricht wird **in die Warteschlange gestellt**: Sie erscheint als gestrichelte Sprechblase am Ende des Protokolls, und der Agent
liest sie, sobald die Runde, in der er gerade ist, vorbei ist: Die Anfrage an das Modell, die unterwegs ist, und die Aufrufe, die er gemacht hat, werden zuerst beendet, und die nächste Anfrage enthält bereits
deine Nachricht (mehrere Nachrichten in der Warteschlange gehen gemeinsam als eine). Wenn er nicht einmal so lange warten soll, drücke den
kleinen Knopf **Jetzt senden** (Blitz) an der Sprechblase: Der Agent verwirft die Runde, in der er steckt (die Anfrage an das Modell wird abgebrochen, ein Befehl, der auf deine Freigabe wartete, wird abgelehnt, nicht erfolgte Aufrufe werden als übersprungen markiert), und liest danach deine Nachricht. Der **Papierkorb**-Knopf daneben nimmt die Nachricht zurück, solange der Agent sie noch nicht gelesen hat (ebenso einen Befehl in der Warteschlange). Bereits ausgeführte Befehle bleiben erledigt. Nachrichten und Befehle in der Warteschlange bleiben beim Neustart des Servers erhalten (sie werden gelesen, wenn du fortsetzt), und ein Lauf, der keine Unterhaltung ist (sofort gestartet), nimmt während seiner Arbeit keine Nachrichten an.
<a id="running-a-command-yourself"></a>

### Einen Befehl selbst ausführen

Unter dem Transkript steht die Befehlszeile der Anwendung (die des Reiter „Geschichte“: Tippe `/`, darüber erscheinen die Liste und die Hilfe der Befehle) mit dem, was du beim Agenten ausführen kannst: `/compact`, `/clear-workspace` und die
[Befehle, die der Agent aufrufen darf](#allowed-commands), die nur Daten lesen oder hinzufügen (ein Befehl, der Daten ändert oder entfernt, wird nicht angeboten, er würde dich bitten, in einem Fenster des Buchs zu bestätigen, und ein Lauf hat kein solches Fenster). Du führst den Befehl aus, nicht der Agent: Ihm wird in einer Nachricht von dir mitgeteilt, dass du ihn ausgeführt hast und was er geantwortet hat (und dass, was er zuvor gelesen hat, veraltet sein kann, falls der Befehl die Arbeitskopie geändert hat). So glaubt er nie, einen Aufruf gemacht zu haben, den er nicht gemacht hat. Er wirkt wie eine Nachricht: Wo der Lauf wartet (*Wartet auf dich*) oder *Pausiert* ist, läuft der Befehl sofort, und der Lauf bleibt, wie er ist. Wo eine [Unterhaltung](#the-console) läuft, wird er eingereiht, eine gestrichelte Blase mit der Markierung *Befehl*, und er läuft am Ende der Runde des Agenten, dort, wo der Agent sonst gewartet hätte (der Agent macht wegen ihm nicht weiter). Der kleine Knopf **Jetzt ausführen** auf der Blase verwirft die Runde, in der der Agent steckt, führt den Befehl aus und lässt den Agenten von dort weitermachen. Ein Befehl, der lange dauert, blockiert das Eingabefeld: Warte darauf, bevor du dem Agenten schreibst.

![Ein Befehl des Autors in der Konsole: /compact](../images/agent-console-command.png)

**`/compact [was betont werden soll]`** gibt den Kontext frei. Der Agent wird gebeten, alles bisher Gelesene durchzugehen und eine Zusammenfassung zu schreiben (was du gefragt hast, was erledigt ist, was er gefunden hat, seine Notizen und sein Plan, was er gerade tun wollte); diese Zusammenfassung ersetzt alles davor in dem, was das Modell liest. Im Transkript siehst du weiterhin alles, und die Zusammenfassung als eingeklappten Block, **Zusammenfassung der bisherigen Arbeit**; der Export behält sie ebenfalls. Der Text nach dem Befehl, falls vorhanden, ist das, was die Zusammenfassung betonen soll.
Die Anfrage gehört dem Agenten selbst, und ihre Tokens zählen gegen den Lauf. Die Frage, die dem Agenten gestellt wird, ist sein **Verdichtungsprompt**: der des Agenten, sonst dein Standard in den [Einstellungen](account.md#templates), sonst der des Administrators, sonst der eingebaute. Ist der Kontext bereits so voll, dass die Anfrage nicht hineinpasst, werden die ältesten Nachrichten aus ihr weggelassen (die Zusammenfassung sagt, wie viele). Tu es, bevor der Kontext voll ist; die Auslastungszeile zeigt, wie nah du dran bist. Direkt nach der Verdichtung ist der angezeigte Kontext eine Schätzung dessen, was die nächste Anfrage enthalten wird (die Zusammenfassung, der Systemprompt und die Definitionen der Werkzeuge); die erste Antwort des Modells danach ersetzt sie durch die echte Zahl.

**Kontext füllt sich.** Sobald der Kontext des Modells 75 % seiner Obergrenze überschreitet, erhält der Agent nach jeder Runde einen Hinweis (eine Nachricht des Laufs, du siehst sie im Transkript): wie voll er ist und dass er speichern soll, was er nicht verlieren darf, in einer Datei seines Arbeitsbereichs, seinen Plan aktualisieren und `request_compaction` aufrufen sollte. Über 90 % sagt der Hinweis, dass der Lauf endet, wenn der Kontext voll ist. Verdichtet der Agent nicht, bevor der Kontext den Anteil **Verdichten bei** seiner Definition überschreitet (90 %, sofern du nichts anderes angibst), tut es der Lauf von selbst: dieselbe Verdichtung, die Zusammenfassung beginnt mit „automatisch verdichtet“, und der Agent wird nicht zusätzlich gewarnt. Eine fehlgeschlagene Verdichtung ändert nichts, der Agent wird darüber informiert, und der Lauf versucht es erst einige Runden später erneut. Die Verdichtung, die der Agent anfordert, ist dieselbe wie deine (sein **Verdichtungsprompt**, siehe [Agent anlegen und bearbeiten](#creating-and-editing-an-agent), seine Tokens zählen gegen den Lauf); schlägt sie fehl, bleibt alles unverändert, und der Agent wird informiert.
Die Hinweise setzen eine Obergrenze des Kontexts voraus: die niedrigste von der Obergrenze des Agenten und den Grenzen seines Modells.

**Ein Aufruf, den der Modellserver nicht lesen konnte.** Schreibt ein Modell in einem Aufruf einen langen Text, kann seine Antwort mitten im Text enden (die Grenze einer *Antwort*, die **Antwort-Tokens** des Protokolls oder die **Max. Completion-Tokens** der KI, ist erreicht, ein Standardwert des Servers greift, wenn keiner von beiden gesetzt ist, oder das Modell hört einfach auf; der Kontext, so voll er ist, ist eine andere Grenze), und der Server lehnt den unvollständigen Aufruf ab („Failed to parse tool call arguments as JSON“). Dieselbe Unterhaltung erneut zu senden, würde denselben Fehler erzeugen, deshalb pausiert der Lauf nicht einfach: Der Agent wird im Transkript darüber informiert, dass seine letzte Antwort nicht verwendet werden konnte und nichts getan wurde, und dass er langen Text in mehreren kleineren Schritten in eine Datei seines Arbeitsbereichs schreiben soll; danach wird er erneut gefragt, bis zu dreimal hintereinander. Scheitert es weiterhin, pausiert der Lauf, und was dem Agenten gesagt wurde, bleibt im Transkript, sodass ein **Fortsetzen** eine andere Frage stellt als die, die gescheitert ist. Dasselbe gilt für einen Unteragenten.

Ein Aufruf, der abgeschnitten wurde, den Anwendung aber erreicht hat (das Ergebnis des Aufrufs sagt „not valid JSON ... cut off in the middle“), bleibt im Transkript so, wie das Modell ihn geschrieben hat, damit du ihn lesen kannst, wird aber mit leeren Argumenten an das Modell zurückgeschickt: Ein Server, der die früheren Aufrufe erneut liest, würde sonst jede folgende Anfrage ablehnen, und der Lauf könnte ohne Zurückspulen nicht weitergehen.

**Endlose Aufrufe.** Ein Modell kann in eine Schleife geraten und in einer einzigen Antwort hunderte Male denselben Aufruf schreiben; die Ergebnisse würden den Kontext füllen, bevor die nächste Anfrage geht. Der Lauf macht höchstens 32 Aufrufe pro Antwort, und eine Wiederholung eines Aufrufs, der nur liest (dasselbe Werkzeug mit denselben Argumenten, in derselben Antwort), wird nur einmal ausgeführt. Die weggelassenen Aufrufe stehen nicht im Transkript, und der Agent erhält einen Hinweis, der das sagt.

**Im Kreis gehen.** Ein Modell kann sich auch über viele Antworten hinweg wiederholen: dasselbe Nachschlagen (Dateien auflisten, Lorebooks auflisten ...) mit demselben Ergebnis, Runde für Runde, ohne weiterzukommen. Der Lauf achtet darauf. Macht der Agent Runden, die nur schauen, nichts ändern und denen früherer gleichen (gleiche Aufrufe, gleiche Ergebnisse), wird er bei der dritten darauf hingewiesen, dass sich nichts geändert hat. Macht er weiter, werden ihm bei der fünften seine Werkzeuge für eine Anfrage entzogen: Er muss in reinem Text antworten, was er tat und was ihn blockiert, und der Lauf **pausiert** mit dieser Antwort als letzter Nachricht, damit du siehst, warum er feststeckte, statt für hundert weitere Runden zu bezahlen. **Fortsetzen** (oder ihm schreiben) gibt die Werkzeuge zurück und beginnt das Zählen von vorn. Eine Runde, in der sich etwas geändert hat, oder während Unteragenten arbeiten, wird nicht gezählt. Ein Unteragent, der sich wiederholt, wird auf dieselbe Weise angehalten, und was er gesagt hat, ist seine Antwort an den Agenten, der ihn gestartet hat.

**`/clear-workspace [was behalten werden soll]`** löscht die Dateien und alle Ordner des [Arbeitsbereichs](#the-working-copy) des Laufs (nachdem du bestätigt hast, wie beim Knopf des Arbeitsbereichs). Nenne danach, was behalten werden soll, durch Leerzeichen getrennt: Dateien, Ordner (mit allem darin) oder Muster wie `*.md` oder `chapters/*.md`, ein Pfad mit Leerzeichen in doppelten Anführungszeichen
(`/clear-workspace /notes.md "/my drafts" *.png`). Die Ordner über einer behaltenen Datei bleiben bestehen. Fehlt etwas Genanntes, wird nichts gelöscht. `/AGENTS.md` und die Checkpoints gehören nicht zum Arbeitsbereich und werden nie angerührt. Der Agent wird darüber informiert, was gelöscht und was behalten wurde. Im Transkript ist ein Befehl, den du ausgeführt hast, eine Blase mit dem Befehl und seiner Antwort.

Wenn das Modell des Agenten Bilder sieht (siehe [Agent anlegen und bearbeiten](#creating-and-editing-an-agent)), steht **Bild anhängen** neben **Senden**: Wähle eine PNG-, JPEG-, GIF- oder WebP-Datei, oder füge ein Bild aus der Zwischenablage in das Nachrichtenfeld ein (**Ctrl+V**). Es wartet über dem Feld (das Kreuz nimmt es zurück), bis du sendest. Bis zu acht Bilder gehören zu einer Nachricht; der Text darf leer sein.
Die Bilder werden im Ordner **uploads** des Arbeitsbereichs gespeichert (ein bereits vergebener Name bekommt eine Nummer: `map-2.png`), innerhalb der Grenzen und Regeln des Arbeitsbereichs, und dem Modell werden sie zusammen mit deiner Nachricht gezeigt. Lösche sie im Reiter **Arbeitsbereich**, wenn du sie nicht mehr brauchst.

Die Knöpfe unten sind **Pausieren**, **Fortsetzen** und **Stopp**, die tun, was in der Tabelle steht, und **Beenden**, das einen Lauf beendet, der auf dich wartet (seine Arbeit ist fertig, *Erledigt*).

Das Fenster schaut alle zwei Sekunden nach dem Lauf, solange es offen ist; **Aktualisieren** tut es sofort. Der Reiter **Lauf** zeigt den neuesten Lauf, der nicht verworfen wurde, sodass ein beendeter oder fehlgeschlagener Lauf dort noch gelesen werden kann, bis der Agent einen neuen Lauf startet; ältere Läufe stehen unter **Läufe**.

**Läufe** ist die Historie des Agenten: jeder Lauf, den er hatte, der neueste zuerst, mit Datum, Status, Runden, Tokens und Ergebnis. Benutze das Auge, um einen Lauf im Reiter **Lauf** zu **öffnen** und seine Schritte und seinen Bericht erneut zu lesen (der geöffnete Lauf bleibt dort, bis du einen anderen öffnest), den Pfeil, um sein Transkript als Markdown-Datei zu **herunterzuladen** (was gesagt wurde und wann, jeder Befehl mit Argumenten und Ergebnis, der Bericht, die Unterhaltungen der Unteragenten), und den Papierkorb, um ihn zu **löschen**. Ein gelöschter Lauf landet im [Papierkorb](trash.md). Ein Lauf, der gerade läuft, lässt sich nicht löschen; stoppe ihn zuerst. Läufe, die du gestoppt und **verworfen** hast, stehen ebenfalls in der Liste, damit du lesen kannst, was sie getan haben, oder sie löschen kannst; der Reiter **Lauf** zeigt sie nur, wenn du sie öffnest.

![Der Reiter Läufe](../images/agent-runs.png)

**Arbeitsbereich** zeigt die Dateien des Agenten: seine Notizen und Berichte, die er zwischen den Läufen behält. Klicke auf einen Ordner, um hineinzugehen (**Nach oben** geht zurück), klicke auf eine Textdatei, um sie zu lesen, benutze die Pfeile, um eine Datei herunterzuladen, oder den Papierkorb, um sie zu löschen, und **Leeren** löscht alles (veraltete Notizen führen einen neuen Lauf in die Irre). **Neuer Ordner** legt einen Ordner im geöffneten an, und **Datei hochladen** legt eine Datei von deinem Computer dort ab, sodass du dem Agenten ein Briefing, einen Stilleitfaden oder einen Entwurf geben kannst. Dieselben Regeln gelten wie für die eigenen Dateien des Agenten: Namen dürfen nicht `\ < > : " | ? *` enthalten, keinen abschließenden Punkt oder Leerzeichen haben, und sie dürfen nicht zu lang oder zu tief verschachtelt sein; die Grenzen für Dateigröße und Anzahl der Dateien gelten. Eine hochgeladene Datei muss Text sein (UTF-8), denn der Agent kann nur Text lesen; oder ein Bild (PNG, JPEG, GIF oder WebP, erkannt an dem, was die Datei enthält, nicht an ihrem Namen; bis 5 MB, und es zählt wie jede Datei gegen Gesamtgröße und Anzahl der Dateien); oder eine Zip-Datei (bis 10 MB, unverändert behalten; der Agent entpackt sie, siehe [unten](#unpacking-a-zip-file)). Eine Datei wird durch einen Upload nie ersetzt: Lösche zuerst die alte.
Dateien lassen sich nicht ändern, während der Agent arbeitet; pausiere ihn zuerst. Der Arbeitsbereich hat eine Grenze für Größe, Dateigröße und Anzahl der Dateien, die dein Administrator in deiner [Berechtigung](administration/permissions.md#settings-of-a-permission) setzen kann; die Zeile unter der Tabelle sagt, wie viel davon genutzt wird. Der Name `AGENTS.md` oben ist deinen Anweisungen vorbehalten, die der Agent dort sieht (die Datei kann nicht geschrieben oder gelöscht werden).

![Der Reiter Arbeitsbereich](../images/agent-console-workspace.png)
<a id="unpacking-a-zip-file"></a>

### Ein ZIP-Archiv entpacken

Lade eine ZIP-Datei im Tab **Arbeitsbereich** hoch (**Datei hochladen**), wenn der Agent an vielen Dateien gleichzeitig arbeiten soll, zum Beispiel an den Kapiteln eines Entwurfs, und bitte ihn, sie zu entpacken: Der Agent hat das Werkzeug `unzip_file`, das `/uploads/draft.zip` in den neuen Ordner `/uploads/draft` entpackt (oder in einen Ordner, den er angegeben bekommt; dieser Ordner darf noch nicht existieren). Das Entpacken geschieht ganz oder gar nicht, und ein Archiv wird nie blind vertraut:

- Behalten werden nur Textdateien (UTF-8), Bilder (PNG, JPEG, GIF, WebP) und ZIP-Dateien. Die übrigen Einträge werden weggelassen und in der Antwort an den Agenten genannt. Eine ZIP-Datei im Archiv wird gespeichert, niemals für sich allein entpackt.
- Die Dateien müssen in den Platz passen, den der Arbeitsbereich bietet (Größe, Dateigröße, Anzahl der Dateien), sonst wird nichts entpackt.
- Ein Archiv mit einem Eintrag, der über den Ordner hinausführt (`../x`), oder der mit `/` beginnt, wird komplett abgelehnt. Ebenso eines mit demselben Namen zweimal (auch wenn sich nur die Groß- und Kleinschreibung unterscheidet), einem Namen, der zugleich Datei und Ordner ist, mehr als 5 000 Einträgen oder einem verschlüsselten oder beschädigten Archiv.
- Eine Zip-Bombe wird abgelehnt: ein Archiv, das mehr entpackt, als der Arbeitsbereich fassen kann, oder mehr als das 100-Fache seiner eigenen Größe.

<a id="sub-agents"></a>

## Unteragenten

Ein Agent kann einen Auftrag an einen **Unteragenten** übergeben: einen Helfer mit einer eigenen Unterhaltung, der eine Aufgabe erledigt und mit einem Text antwortet. Er hat die Befehle und Dateien des Agenten (oder des Modells, auf dem er läuft, siehe [unten](#models-for-sub-agents)) und arbeitet im selben Arbeitsexemplar und Arbeitsbereich (was er im Buch ändert, bleibt bestehen), aber er kennt die Unterhaltung des Agenten nicht, deshalb muss der Agent alles in der Aufgabe sagen. Der Sinn besteht darin, dass das, was der Unteragent liest und ausprobiert, nicht in den Kontext des Agenten gelangt: ein Kapitel prüfen, die Namen überprüfen, einen Teil entwerfen.

Stelle unter [Agent](#creating-and-editing-an-agent) bei **Maximale Anzahl Unteragenten** ein, wie viele gleichzeitig arbeiten dürfen. Null (die Voreinstellung) bedeutet keine: Der Agent erfährt nicht einmal, dass es sie gibt.

- Bei **eins** startet der Agent einen Unteragenten und wartet (er wird *geparkt*), bis dieser fertig ist; der Aufruf liefert dessen Antwort zurück.
- Bei **mehr** kehrt der Start eines Unteragenten sofort zurück, und der Agent kann weitermachen, weitere starten, nach ihnen sehen (`agent_status`), auf eine Antwort warten (`await_agent`) oder einen abbrechen (`cancel_agent`). Der Aufruf, der die Grenze erreicht, wartet, bis einer von ihnen beendet ist, und liefert zurück, was beendet wurde.

Regeln, die in beiden Fällen gelten:

- Ein Unteragent **liest den gesamten Arbeitsbereich, schreibt aber nur in einen eigenen Ordner**, `/tmp/sub1`, `/tmp/sub2` usw. (dieser wird beim Start des Unteragenten angelegt, und was ein früherer Lauf in einem Ordner gleichen Namens hinterlassen hat, wird gelöscht). Seine Antwort wird dort als `result.md` gespeichert. Wenn eine lange Antwort abgeschnitten angezeigt wird, liest der Agent den vollständigen Text aus dieser Datei. Du findest die Dateien im Tab **Arbeitsbereich**; sie zählen zum Teil des Kontingents des Arbeitsbereichs und bleiben erhalten, bis du sie löschst.
- Ein Unteragent läuft auf dem Hauptmodell des Agenten, sofern der Agent nicht ein anderes anfordert, siehe [Modelle für Unteragenten](#models-for-sub-agents).
- Ein Befehl eines Unteragenten, der Daten verändert, folgt der Einstellung **Destruktive Befehle** des Agenten: Bei *Erst fragen* fragt dasselbe [Fenster](#asking-you) dich, und dieser Unteragent wartet auf deine Antwort (ein Pausieren des Laufs lehnt sie ab).
- Ein Unteragent kann **keine Unteragenten starten**, den Lauf beenden, dich fragen oder Kontrollpunkte setzen; er endet mit einer Textantwort.
- Alles zählt im **einen Budget des Laufs**: Runden, Token, die Grenzen des Modells. Ein Unteragent, der an eine Grenze stößt, scheitert und meldet das; der Agent stößt dann auf dieselbe Grenze.
- Wie viele Anfragen gleichzeitig an das Modell unterwegs sind, bestimmen die **Anfragen an das Modell gleichzeitig**, die dir dein Administrator gesetzt hat (eins, wenn nichts gesetzt ist): Bei eins wechseln sich der Agent und sein Unteragent ab.
- Aufrufe, die das Buch oder den Arbeitsbereich verändern, werden nacheinander ausgeführt, egal wer sie macht, sodass zwei Unteragenten nie im selben Augenblick schreiben. Trotzdem solltest du nicht zwei von ihnen anweisen, denselben Teil zu verändern.
- **Das Pausieren des Laufs pausiert auch die Unteragenten.** Sie halten an ihrem nächsten stabilen Punkt an (zwischen zwei Anfragen oder zwei Aufrufen, sofort, wenn du sofort pausierst), und was sie gesagt und getan haben, bleibt beim Lauf erhalten. So macht ein **Fortsetzen** dort weiter, wo alles stand: Die Unteragenten führen ihre Unterhaltung fort, und ein Aufruf des Agenten, der auf einen von ihnen wartete (`await_agent` oder `spawn_agent` mit einer Grenze von eins), wartet erneut, als wäre nichts geschehen. Ein Unteragent, der vor der Pause bereits beendet war, bleibt für Rückfragen des Agenten verfügbar. Das Stoppen eines Laufs mit Behalten wirkt genauso; das Verwerfen, ein Zurücksetzen auf einen Kontrollpunkt (das Buch, in dem die Unteragenten arbeiteten, wird ersetzt) sowie das Beenden des Laufs durch den Agenten oder eine Nachfrage bei dir beenden sie endgültig. Ein Agent, der seinen Zug beendet, während Unteragenten noch arbeiten, wird einmal darauf hingewiesen; tut er das erneut, werden sie gestoppt.
- Ihre Unterhaltungen erscheinen nicht im Tab **Lauf**. Der Tab **Unteragenten** der Konsole zeigt sie: Wähle oben einen aus, um zu lesen, was der Agent ihm aufgetragen hat, was er gesagt hat, die Befehle, mit denen er aufgerufen wurde, samt ihren Ergebnissen, und wie er endete (die Antwort, die er gab, oder warum er scheiterte oder abgebrochen wurde), in denselben Blöcken wie im Protokoll. Er folgt dem Lauf, während dieser arbeitet, und zeigt auch die Unteragenten eines älteren Laufs (wähle zuerst diesen Lauf im Tab **Läufe**). Ein Aufruf von `spawn_agent` im Tab **Lauf** hat eine Schaltfläche **Unteragent**, die ihre Unterhaltung öffnet. Eine Unterhaltung, auf die der Agent zurückgegangen ist (ein Zurückspulen), ist ausgegraut und als *vom Agenten vergessen* markiert. Der **Download** des Laufs (im Tab **Läufe**) enthält einen Abschnitt für jeden Unteragenten, mit seiner Aufgabe, seinem Zustand und jeder Nachricht und jedem Aufruf.

![Der Tab Unteragenten](../images/agent-subagents.png)

<a id="models-for-sub-agents"></a>

### Modelle für Unteragenten

Der Agent hat ein **Hauptmodell**, das oben im Dialog gewählt wird. Unter **Modelle für Unteragenten** kannst du weitere hinzufügen, zum Beispiel ein günstiges Modell, das nur liest, oder ein sprachlich gewandtes, das entwirft. Jedes hat eine **Rolle** (seinen Namen), eine **Beschreibung** (wann es zu verwenden ist: Der Agent liest sie, um zu wählen), seine KI und sein Protokoll, eine optionale **Kontextgrenze** und das, was es verwenden darf:

![Ein Modell für Unteragenten](../images/agent-models.png)

- **Kann Befehle und Werkzeuge verwenden.** Ausgeschaltet antwortet ein Unteragent auf diesem Modell nur mit Text: Er bekommt keine Befehle und keine Werkzeuge.
- **Befehle einschränken.** Ausgeschaltet hat ein Unteragent auf diesem Modell die Befehle des Agenten. Eingeschaltet nur die, die du ankreuzt, und von diesen nur die, die der Agent verwenden darf (die erlaubten Befehle des Agenten bleiben die obere Grenze). Die Werkzeuge für Dateien sind keine Befehle, daher haben alle Unteragenten sie. Das Hauptmodell hat dieselbe Einstellung, für die Unteragenten, die auf ihm laufen (die Schaltfläche **Bearbeiten** seiner Zeile).

Der Agent erfährt die Rollen, Beschreibungen und Befehle jedes Modells und startet einen Unteragenten auf einem davon mit dem `profile` von `spawn_agent`; lässt er es weg, nimmt er sein eigenes Modell. Das Kontrollkästchen **Unteragenten dürfen auf dem Hauptmodell laufen** (standardmäßig eingeschaltet) schaltet diese letzte Wahl ab: Der Agent muss dann ein anderes Modell nennen, und gibt es keines, bekommt er überhaupt keine Werkzeuge für Unteragenten. Ein Unteragent hat die Grenzen seines eigenen Modells (wie viel er lesen kann und wie lang seine Antwort sein darf), und der Tab **Unteragenten** sowie die Diagnose zeigen, auf welchem Modell jeder lief.

<a id="one-model-several-agents"></a>

## Ein Modell, mehrere Agenten

Ein lokaler Server hat oft einen Steckplatz (eine GPU): Verlangst du zwei Antworten zugleich, kriechen beide oder laufen in eine Zeitüberschreitung. Die **Verbindungen** des [Modells](inference-providers.md#general-settings) (1, wenn du nichts anderes einstellst) lassen alle Anfragen an es der Reihe nach laufen, in der Reihenfolge, in der sie eintrafen: die Anfragen deiner Agenten, der Agenten anderer Nutzer und dein eigenes Schreiben im Geschichtenfenster. Ein Lauf, der wartet, bleibt *Läuft*, und ein **Pausieren** oder **Stopp** beendet das Warten sofort. Zwischen zwei Antworten des Modells hält ein Agent keine Verbindung, sodass deine Generierung dazwischen laufen kann.
<a id="when-you-are-not-there"></a>

## Wenn Sie nicht da sind

Ein Agent, der Sie braucht, fertig ist oder fehlschlägt, meldet das den offenen Bildschirmen der Anwendung (dem Abzeichen am Reiter, einem Hinweis). Wenn kein Bildschirm von Ihnen offen ist, kann er außerdem eine kurze Nachricht an eine Adresse senden, die Sie unter [Einstellungen](account.md) angeben, sofern ein Administrator Ihnen die [Berechtigung *Benachrichtigungen*](administration/permissions.md) erteilt hat (Administratoren haben sie): („Nachricht an eine URL, wenn ein Agent Sie braucht“). Dafür gibt es Ihr **ntfy**-Thema (die ntfy-App auf dem Telefon zeigt es sofort an), eine **Gotify**-Nachrichten-URL oder einen **Discord**-Webhook; wählen Sie das **Format**, das der Dienst verlangt. **Testnachricht senden** probiert es mit den Angaben in den Feldern aus. Die Nachricht nennt den Agenten und sagt, was er möchte („wartet auf Sie“, „fertig“, „bittet um die Zustimmung zum Befehl ...“, „fehlgeschlagen“, „wurde pausiert: Grund“), in Ihrer Sprache. **Auch wenn die Anwendung geöffnet ist** sendet sie in jedem Fall.

Der Server ruft die Adresse auf, daher kann ein Administrator das für alle ausschalten (Admin, *Agenten*). Außerdem werden nur Adressen im Internet aufgerufen, es sei denn, der Administrator hat in Ihrer Berechtigung zusätzlich **Adressen auf diesem Rechner und in privaten Netzwerken erlauben** angekreuzt: Für ein ntfy oder Gotify auf demselben Rechner oder in Ihrem eigenen Netzwerk bitten Sie darum. Die Testnachricht zeigt an, wenn die Adresse abgelehnt wird. Behandeln Sie die Adresse wie ein Passwort, wenn sie ein Geheimnis enthält: Wer sie kennt, kann mitlesen, was gesendet wird. Der Server schreibt sie nie in sein Protokoll, Weiterleitungen werden nicht verfolgt, und nur `http` und `https` werden aufgerufen.

<a id="asking-you"></a>

## Ihre Aufmerksamkeit anfordern

Wenn der Agent Sie braucht, wird Ihnen das überall in der App mitgeteilt, wo Sie gerade sind:

- Eine **Zahl am Reiter Agenten** zählt die Agenten, die auf Sie warten, und der Button **Konsole** eines solchen Agenten ist hervorgehoben;
- ein **Hinweis** erscheint mit einem Button **Öffnen**, der die Konsole dieses Agenten öffnet.

Der Agent braucht Sie, wenn er *Ihnen etwas fragt oder auf Ihre nächste Nachricht wartet*, Sie bittet, einen **Befehl zu genehmigen**, *fertig* ist, *fehlgeschlagen* ist oder *von selbst pausiert* wurde (ein Limit ist erreicht, das Modell konnte nicht befragt werden, der Server wurde neu gestartet). Eine Pause, die Sie selbst veranlasst haben, zählt nicht.

Agenten arbeiten, während Sie weg sind, daher hängt das nicht davon ab, ob die Anwendung geöffnet ist: Die Zahl ist da, wenn Sie sich wieder anmelden, und jedes offene Browserfenster erhält den Hinweis. Wenn Sie die Konsole des Agenten öffnen, wird er gelöscht (die anderen Fenster aktualisieren sich).

Wenn der Agent auf **Erst fragen** gestellt ist und einen Befehl ausführen will, der Daten ändert oder entfernt, stoppt der Lauf in *Wartet auf Zustimmung*, und über der Konsole erscheint ein Fenster mit dem Befehl und seinen Argumenten. **Genehmigen** lässt ihn laufen, **Ablehnen** teilt dem Agenten mit, dass Sie es abgelehnt haben, und er macht weiter. Das Pausieren oder Anhalten des Laufs (oder das Anhalten des Servers) gilt nicht als Ablehnung: Dem Agenten wird nichts mitgeteilt, und wenn Sie **Fortsetzen**, erscheint das Fenster erneut für denselben Befehl. Der Befehl wirkt weiterhin auf die [Kopie](#the-working-copy) des Buchs. Wenn die Konsole geschlossen ist, ist das Fenster da, sobald Sie sie öffnen.

![Das Fenster, das um die Zustimmung zu einem Befehl bittet](../images/agent-approval.png)

<a id="when-the-server-stops"></a>

## Wenn der Server anhält

Das Anhalten des Servers pausiert zuerst die laufenden Läufe, wie es **Jetzt pausieren** tut (dem Server werden dafür einige Sekunden gegeben): Die Anfrage und der Aufruf, der gerade läuft, werden abgebrochen, der Lauf ist *Pausiert* mit dem Grund „Der Server wurde angehalten“, und ein **Fortsetzen** ist möglich. Wenn der Server stattdessen beendet wird oder abstürzt, pausiert der nächste Start die Läufe, die er als laufend vorfindet („Durch einen Neustart des Servers unterbrochen“). Alles, was der Lauf getan hat, bleibt erhalten: die gespeicherten Runden, die Kopie, der Arbeitsbereich sowie die Nachrichten und Befehle, die Sie eingereiht hatten. Verloren geht nur die Runde, die nicht fertig wurde. Beim Fortsetzen wird der Aufruf behandelt, in dem der Lauf war:

- Ein Aufruf, der **noch nicht begonnen** hatte (er wartete auf Ihre Zustimmung oder auf seinen Platz nach einem anderen Aufruf), wird erneut ausgeführt, sodass ein Befehl, der Ihre Zustimmung braucht, Sie erneut fragt. Ein Aufruf, der nur liest, wird ebenfalls erneut ausgeführt.
- Ein Aufruf, der **bereits begonnen hatte, etwas zu ändern**, ist möglicherweise teilweise erledigt: Dem Agenten wird mitgeteilt, dass sein Ergebnis unbekannt ist, und er prüft es mit einem Lesewerkzeug, bevor er es wiederholt. Die Aufrufe danach werden als nicht ausgeführt markiert.
- Ein Befehl von Ihnen, der lief, als der Server anhielt, läuft weiter, wenn der Lauf weiterläuft. `/compact` und `/clear-workspace` werden erneut ausgeführt; jeder andere Befehl, der bereits begonnen hatte, wird **nicht** erneut ausgeführt (er kann teilweise erledigt sein): Der Agent wird darüber informiert und prüft es. Die Nachrichten und Befehle, die danach kamen, werden wie üblich zugestellt.
- Der Plan des Agenten und die Ergebnisse seiner Befehle (`list_command_results`, `@result`) bleiben ebenfalls erhalten, sodass der Agent sie nach dem Neustart noch hat.
- Die Aufrufe eines Unter-Agenten, die gerade liefen, werden wie die des Agenten behandelt: Die noch nicht begonnenen werden ausgeführt, der begonnene wird ihm als „Ergebnis unbekannt“ gemeldet, und ein Unter-Agent, der auf Ihre Zustimmung wartete, fragt erneut.
- Wenn der Server **abgebrochen** wird (`kill -9`, Stromausfall), während ein Befehl läuft, stirbt der Befehl mit ihm; was er getan hat, ist wie oben beschrieben.

<a id="the-working-copy"></a>

## Die Arbeitskopie

Jeder Lauf arbeitet an einer **Kopie Ihres Buchs** und seiner Lorebooks, die die App beim Start des Laufs anlegt: die Nachrichten, Zusammenfassungen, Tags und Einstellungen so, wie sie in diesem Moment sind, sowie das Lorebook mit seinen Unter-Lorebooks. Die Kopie ist verborgen: Sie steht nicht in den Listen der Bücher und Lorebooks. Was der Agent auch tut, er tut es an der Kopie; Ihr Buch bleibt, wie es war.

Wenn der Agent **Das Buch mit dem Modell des Agenten schreiben** eingeschaltet hat (Standard), erhält die Kopie außerdem das **Modell des Agenten** (die KI und das Protokoll seines Hauptmodellprofils) anstelle der KI und des Protokolls Ihres Buchs, jedes Mal, wenn der Lauf startet oder fortgesetzt wird. Die Befehle, die mit einem Modell schreiben (`/next`, `/swipe`, `/regenerate`, `/redraft-part`, `/generate-annotation`, die Zusammenfassungen), und die Modellbefehle von Erweiterungen (der Lore Generator, die Arc Outline) verwenden also das Modell des Agenten, auch wenn Ihr Buch auf ein anderes oder auf keines eingestellt ist; ein in dem Aufruf genanntes `profile` hat dennoch Vorrang. Ist die Einstellung aus, verwenden sie das Modell des Buchs. Das ist keine Änderung an Ihrem Buch: Die Überprüfung führt sie nicht auf, und das Übernehmen des Ergebnisses lässt die KI und das Protokoll Ihres Buchs unverändert.

Die Kopie wird in den [Ordner](books/folders.md) Ihres Buchs gelegt, sodass der Agent mit den Einstellungen schreibt, die der Ordner vorgibt (Sprache, Vorlagen, Erzählperspektive, Zeitform, Stil und die Prompts), wie es auch das Buch tut. Die Kopie ist kein Inhalt des Ordners: Sie wird dort nicht gezählt, und die Arbeit darin ändert nicht das *Zuletzt geändert* des Ordners. Die Kopie folgt dem Buch, wenn Sie es verschieben, während der Lauf pausiert ist. Das Übernehmen des Ergebnisses verschiebt Ihr Buch nie.

Ein pausierter Lauf behält seine Kopie und arbeitet beim Fortsetzen wieder daran. Ein Lauf, der *Erledigt* ist oder den Sie angehalten und behalten haben, behält sie, bis Sie [das Ergebnis übernehmen oder verwerfen](#reviewing-the-result). Wenn der Lauf *Fehlgeschlagen* ist, Sie sein Ergebnis verwerfen oder seine Änderungen übernommen wurden, wird die Kopie endgültig gelöscht, und der nächste Lauf legt eine neue aus dem Buch in seinem jeweiligen Zustand an.
<a id="resuming-a-run"></a>

## Fortsetzen eines Laufs

Ein pausierter oder behaltener Lauf sperrt dein Buch nicht, du kannst das Buch also in der Zwischenzeit bearbeiten. Wenn du den Lauf **Fortsetzen**, gleicht seine Arbeitskopie sich mit dem an, was du getan hast:

- Was du geändert hast und der Agent nicht (ein Teil, ein Lorebook-Eintrag, eine Einstellung, ein Tag ...), kommt in die Kopie. Ein Teil, den du hinzugefügt hast, kommt ebenfalls hinein, und der Agent kann darauf aufbauen; was du gelöscht hast, wird in der Kopie gelöscht.
- Was du und der Agent beide auf unterschiedliche Texte geändert habt, **bleibt so, wie der Agent es hat**: Nichts, was der Agent getan hat, geht verloren. Es ist ein [Konflikt](#reviewing-the-result), den du beim Prüfen des Ergebnisses auflöst, wobei beide Texte vor dir liegen.
- Was du geändert und der Agent gelöscht hat, bleibt gelöscht, und was du gelöscht und der Agent geändert hat, bleibt in der Kopie; die Prüfung zeigt beides.

Dafür musst du nichts anklicken: Der Abgleich ist Teil des Fortsetzens und dauert einen Moment (einige Sekunden, wenn du hunderte Elemente geändert hast). Der Stand der Kopie vorher wird als automatischer [Kontrollpunkt](#checkpoints) gespeichert. Der Agent wird beim Weitermachen in einer Nachricht darüber informiert, mit den Namen, unter denen er die Elemente kennt ("Während der Lauf pausiert war, hat der Benutzer das Buch geändert ..."), und kann mit `get_sync_report` erneut nachfragen.

**Ein Konflikt verschwindet nicht von allein.** Bei jedem Fortsetzen wird die Kopie erneut mit dem Buch verglichen, daher wird ein Konflikt jedes Mal erneut gemeldet, und der Agent wird jedes Mal erneut darauf hingewiesen, bis du ihn auflöst: indem du deine Seite des Buchs änderst (auf den Text des Agenten oder zurück auf den ursprünglichen Stand) oder bei der Prüfung.

**Eine Notiz beim Fortsetzen.** **Fortsetzen** öffnet ein kleines Fenster mit einem Feld *Notiz für den Agenten (optional)*. Was du dort schreibst, liest der Agent als Erstes, wenn er weitermacht, direkt nach dem Bericht über die Änderungen, die du am Buch vorgenommen hast. Sie ist für eine Richtungsänderung gedacht ("lass das Lorebook in Ruhe", "beginne mit Kapitel 3"), und anders als eine Nachricht in einer Unterhaltung funktioniert sie bei jedem Lauf, auch bei einem, der nicht auf deine Nachrichten wartet. Lass sie leer, um wie bisher fortzusetzen.

![Fortsetzen eines Laufs mit einer Notiz](../images/agent-resume.png)

<a id="reviewing-the-result"></a>

## Prüfen des Ergebnisses

Wenn ein Lauf *Erledigt* ist (oder du ihn mit **Stoppen und das Ergebnis behalten** angehalten hast), haben die Agentenzeile und die Konsole eine Schaltfläche **Änderungen prüfen**. Sie öffnet ein Fenster mit allem, was der Agent geändert hat, verglichen mit deinem Buch **so, wie es jetzt ist**, eine Zeile pro Änderung. Wenn der Agent Befunde gemeldet hat, werden sie oberhalb der Änderungen aufgelistet (**Befunde (3)**), sodass auch eine Prüfung, die nichts geändert hat, etwas zeigt:

![Prüfen des Ergebnisses eines Laufs](../images/agent-review.png)

- **Buch:** sein Name, seine Beschreibung, Sprache, Erzählperspektive, Zeitform, Stil, Vorlagen, die Anzahl der behaltenen Versionen pro Teil, seine Tags und die Daten, die eine Erweiterung im Buch speichert (ein Handlungsbogen-Gerüst, der Figurenverfolger).
- **Teil:** ein Teil, dessen Text, Anweisungen, Szenenkulisse, Perspektivfigur, anwesende Figuren oder Zusammenfassung der Agent geändert hat, ein Teil, den er geschrieben hat, ein Teil, den er gelöscht hat. Die Variablen, die eine Generierung zusammen mit einem Teil speichert (und die Daten von Erweiterungen), werden mit ihm übernommen, als Feld **Daten**.
- **Lorebook** und **Eintrag:** ein Lorebook oder ein Eintrag, den der Agent angelegt, geändert (auch verschoben, umbenannt oder mit anderen Tags oder anderen Unterbüchern) oder gelöscht hat.
- **Anmerkung:** eine [Notiz](books/annotations.md#annotations-and-agents) zu einer Textstelle eines Teils, die der Agent angelegt, geändert (die Notiz, den Typ, den Status) oder gelöscht hat.

Hake die Zeilen an, die du willst, und klicke dann auf **Die gewählten Änderungen anwenden**. Die Schaltflächen über der Liste haken **alle** an, **keine**, **nur neue Teile** (den neuen Zweig, den der Agent geschrieben hat, sonst nichts), **nur Anmerkungen** oder alles **ohne Konflikte**. Eine Zeile, die eine andere braucht (ein neuer Teil braucht den Teil, auf den er folgt, wenn auch dieser neu ist), nimmt diese beim Anhaken mit, und sie verliert ihre Nachfolger, wenn du sie abhakst. Klicke auf eine Zeile, um sie rechts zu sehen: den Text deines Buchs jetzt und den Text, der geschrieben wird, **den du vor dem Anwenden bearbeiten kannst**. Wenn du nichts schreibst, wird der Text des Agenten angewendet.

Dein Buch kann sich seit dem Start des Laufs geändert haben (du kannst es bearbeiten, sobald der Lauf nicht arbeitet). Was nur du geändert hast, steht nicht in der Liste und bleibt bestehen. Was ihr beide auf dasselbe geändert habt, ist keine Änderung. Was ihr beide auf **unterschiedliche** Texte geändert habt, ist ein **Konflikt**: Er startet nicht angehakt, und wenn du ihn anhakst, ersetzt der Text, den du rechts siehst, den Text des Buchs. **Die Version des Buchs verwenden** legt den Text des Buchs in das Feld (die Zeile ändert dann für dieses Feld nichts), **Die Version des Agenten verwenden** legt den des Agenten wieder hinein; oder du schreibst das Ergebnis selbst. Eine Zeile, die nicht übernommen werden kann (der Teil, auf den sie folgt, oder das Lorebook, in dem sie liegt, wurde in deinem Buch gelöscht), sagt das und lässt sich nicht anhaken.

![Ein Konflikt in der Prüfung](../images/agent-review-conflict.png)

Unter den Texten zeigt **Unterschiede**, was sich **Wort für Wort** ändert: Was nur im Buch steht, ist rot durchgestrichen, was nur im zu übernehmenden Text steht, ist grün (es folgt dem, was du in das Feld schreibst). Bei einem Konflikt zeigt es außerdem, was der Agent und was du seit dem Start des Laufs geändert haben, jeweils ausgehend vom Buch in seinem damaligen Stand, damit du siehst, wer was getan hat. Eine Zeile, die der Agent an mehr als einer Stelle geändert hat (ein Teil, dessen Text und Szenenkulisse sich geändert haben, ein Eintrag mit neuem Inhalt und neuer Notiz), hat neben jeder dieser Stellen ein Feld **Diese Änderung übernehmen**: Enthake eines, um die Zeile ohne dieses zu übernehmen; das Buch behält dann seinen eigenen Wert dieses Feldes. Die Position eines Teils in der Geschichte gehört zum Teil und hat kein Feld, und was der Agent angelegt oder gelöscht hat, wird als Ganzes übernommen oder gelassen. **In der Kopie anzeigen** (bei einer Zeile, die der Agent angelegt oder geändert hat) öffnet die [Kopie des Buchs](#the-copy-of-the-book) an dem Teil oder Eintrag, damit du ihn an seiner Stelle lesen kannst.

![Die Unterschiede eines Konflikts, Wort für Wort](../images/agent-review-differences.png)
<a id="data-that-extensions-keep-json"></a>

### Daten, die Erweiterungen behalten (JSON)

Erweiterungen speichern ihre eigenen Daten in einem Buch, in dessen Teilen, Lorebooks und Einträgen (eine Bogenübersicht, der Figurentracker, die Variablen einer Generierung). Es handelt sich um JSON, und die Prüfung zeigt es **formatiert** an, zusammen mit der Zeile
**JSON-Daten zusammenführen** neben dem Text, der übernommen wird. Die Daten werden nicht als Ganzes übernommen: Was nur der Agent geändert hat, wird vom Agenten übernommen, was nur Ihr Buch geändert hat, bleibt erhalten, und was beide an **unterschiedlichen
Stellen** geändert haben, wird Stelle für Stelle zusammengeführt. Ein Konflikt liegt nur dann vor, wenn beide dieselbe **gleiche** Stelle auf unterschiedliche Werte geändert haben (die Reihenfolge der Schlüssel spielt keine Rolle); bis Sie entscheiden, wird dort die Version des Agenten verwendet.

**JSON-Daten zusammenführen** öffnet ein Fenster mit den Daten der drei Versionen, die als Baum übereinandergelegt werden: *Beim Start des Laufs*, *Jetzt im Buch*, der *Agent* und das *Ergebnis* in jeweils einer Spalte, mit einer Markierung für das, was sich geändert hat
und wo (der Agent, das Buch, beide, ein Konflikt). Es werden nur die Stellen angezeigt, die sich geändert haben (entfernen Sie dafür das Häkchen bei **Nur das Geänderte**); eine Stelle wird erst geöffnet, wenn Sie sie öffnen, sodass auch ein großes Dokument kein Problem ist.

![Die Daten einer Erweiterung, übereinandergelegt](../images/agent-json-merge.png)

- Wählen Sie eine Stelle aus und nehmen Sie ihre Daten aus einer Version (**Die Version des Agenten verwenden**, **Die Version des Buchs verwenden**, **Die vom Start des Laufs verwenden**), **Weglassen** (der Schlüssel oder das Element ist nicht im Ergebnis) oder **Aus den Stellen darin zusammenführen** (die Stellen darin entscheiden, jede für sich). Ein Schlüssel oder ein Element, das nur eine Version hat, wird in das Ergebnis kopiert, indem es aus dieser Version übernommen wird. Ein ganzes Objekt oder eine ganze Liste aus einer Version zu übernehmen, hebt Ihre Auswahl darin auf, aber nicht endgültig: Führen Sie es erneut aus den Stellen darin zusammen, und Ihre Entscheidungen sind wieder da.
- Die Elemente einer **Liste** haben keine Namen, daher werden sie über das zugeordnet, was sie sind: zuerst über dieselben Daten, dann über dieselbe `uuid`, `id` oder denselben `name`, dann über die, die einander am ähnlichsten sehen; was übrig bleibt, in gleicher Anzahl und gleicher Art, gilt als geändert. Liegt die Vermutung daneben, **Aufteilen** Sie ein Element in die Elemente jeder Version oder **Zuordnen zu …** ein anderes Element, mit dem das erste keine Version gemeinsam hat.
- **Öffnen** (oder ein Doppelklick) betrifft nur eine einzelne Stelle: Der Pfad über dem Baum führt Sie wieder nach oben.
- Der Reiter **Text** zeigt die Daten der geöffneten Stelle als Text: die Daten des Agenten können Sie **bearbeiten**, die anderen nur lesen (das Buch, den Start des Laufs, das Ergebnis), und **Den Unterschied zu folgendem anzeigen** markiert, was sich Wort für Wort unterscheidet. Was Sie schreiben, wird als JSON geprüft, wenn Sie zum Baum zurückkehren, die Anzeige wechseln, eine andere Stelle öffnen oder das Ergebnis übernehmen; ist es kein gültiges JSON, bleiben Sie, wo Sie sind, und erhalten eine Meldung, was nicht stimmt, und nichts wird geändert. Gültiger Text ersetzt die Daten des Agenten an dieser Stelle, und alles darin wird erneut verglichen.
- **Von vorn beginnen** vergisst, was Sie entschieden und geschrieben haben; **Das Ergebnis übernehmen** gibt das Ergebnis an die Prüfung weiter, wo es der anzuwendende Text ist (die Zeile bleibt so, wie Sie sie hinterlassen haben, solange die Prüfung offen ist).

Auch die Prüfung hat **Die Version des Agenten verwenden** und **Die Version des Buchs verwenden** für ein solches Feld, um eine der beiden Versionen unverändert zu übernehmen, sowie **Von vorn beginnen**, um zu den zusammengeführten Daten zurückzukehren.

![Die Daten der Bogenübersicht in der Prüfung, mit JSON-Daten zusammenführen](../images/agent-review-json.png)

Ein Lorebook, das andere Bücher verwenden, ist mit **Geteilt** markiert: Die Änderung geht an das Lorebook selbst, sodass die anderen Bücher sie ebenfalls erhalten. Es ist zunächst nicht angehakt.

Das Anwenden erfolgt **direkt** im Buch: Die Teile behalten ihre Adressen, andere Bücher behalten ihre Verknüpfungen zu einem Lorebook, und was der Agent gelöscht hat, wandert in den Papierkorb. Vorher wird eine **Sicherung des Buchs in seinem bisherigen Zustand** angelegt (sie befindet sich unter den Sicherungen des Buchs, mit seinen Lorebooks), und die Änderungen werden alle gemeinsam oder gar nicht vorgenommen. **Den Zweig anzeigen, auf dem der Agent geendet hat** wechselt außerdem das Buch zu dem Zweig, auf dem der Agent war, sofern dieser unter dem ist, was Sie angewendet haben. Die Zusammenfassungen bearbeiteter Teile sind veraltet, wie nach einer manuellen Änderung; die eigenen Zusammenfassungen des Agenten werden unverändert übernommen.

Sobald die Änderungen angewendet sind, ist der Lauf *Angewendet*, und seine Kopie und Kontrollpunkte werden gelöscht. **Das Ergebnis verwerfen** wirft die Kopie weg und lässt das Buch unverändert. Bis Sie eines von beiden tun, hat der Agent einen unvollendeten Lauf und kann keinen weiteren starten.

<a id="the-copy-of-the-book"></a>

### Die Kopie des Buchs

Der Reiter **Buchkopie** zeigt das Buch, an dem der Agent arbeitet, zum Lesen: die **Geschichte**, jeweils einen Zweig (wählen Sie den Zweig oben aus; jeder Teil mit seiner Nummer, dem Durchgang, den er unter den anderen zum selben Teil hat, seinen Wörtern, Szenenkulisse, Erzählperspektive und, eingeklappt, seinen Anweisungen und seiner Zusammenfassung) sowie die **Lorebooks** mit ihren Einträgen (wählen Sie einen aus, um Inhalt, Notiz, Schlagwörter und Einstellungen zu sehen). Es ist kein Fenster auf Ihr Buch: Nichts darin lässt sich ändern, es wird keine Sicherung der Kopie angelegt, und keine Erweiterung sieht ein geöffnetes Buch. Gelesen wird beim Öffnen des Reiters und beim Drücken von **Aktualisieren**, auch während der Agent arbeitet, sodass man sieht, was der Agent bisher geschrieben hat. Der Reiter steht zur Verfügung, solange der Lauf seine Kopie hat (nicht mehr, nachdem das Ergebnis angewendet oder verworfen wurde).

![Der Reiter Buchkopie](../images/agent-book-copy.png)

<a id="diagnostics"></a>

### Diagnose

Der Reiter **Diagnose** zählt aus dem Protokoll des Laufs, was er getan hat, um herauszufinden, was ein Agent von seinen Werkzeugen und seiner Anweisung braucht. Er funktioniert für jeden Lauf, auch für einen alten (wählen Sie ihn zuerst unter **Läufe** aus), und wird beim Öffnen des Reiters sowie beim Drücken von **Aktualisieren** erstellt.

![Der Reiter Diagnose](../images/agent-diagnostics.png)

- **Überblick**: Runden und Aufrufe, wie viele Aufrufe fehlschlugen, die Zeit des Modells, der Aufrufe und der Pausen (eine Lücke von 30 Minuten oder mehr zwischen zwei Schritten ist eine Pause, keine Arbeit), der Kontext auf seinem Höchststand und am Ende, die Token, Verdichtungen, Ihre Nachrichten und die Hinweise, die der Laufzeitumgebung dem Agenten gab (ein unlesbarer Aufruf, zu viele Aufrufe, ein voller Kontext).
- **Werkzeuge**: für jedes Werkzeug die Aufrufe, die Fehler, die Ergebnisse, die gekürzt wurden, wie oft es der einzige Aufruf einer Runde war, die Zeichen, die der Agent schrieb, wie viele Aufrufe einen Text als `@file:` oder `@result` übergaben, wie oft er einen langen Text (300 Zeichen oder mehr) ausgab und wie viele davon er aus einem Ergebnis kopierte, das der Agent gerade erhalten hatte, die Token seiner Ergebnisse (ungefähr, aus den Zeichen), den Anteil an allen Ergebnissen, das längste Ergebnis und die Zeit.
- **Was die Anfragen meldeten**: wird während der Lauf arbeitet erfasst, daher nur für Läufe, die gemacht wurden, nachdem dies hinzugefügt wurde (ein älterer Lauf meldet dazu nichts): wie lange die Anfragen auf das erste Wort warteten (der Server liest den Prompt, eine kurze Zeit bedeutet also, dass sein Cache warm ist), wie lange die Antworten zum Schreiben brauchten und wie viele Token pro Sekunde, wie viel vom Prompt der Server nicht erneut las (der Cache; llama.cpp und OpenAI melden ihn), wie viel der Antworten Überlegung war, warum die Antworten endeten (`length` bedeutet, das Limit einer Antwort hat sie abgeschnitten), die Aufrufe, die das Modell schrieb, aber nicht ausgeführt wurden (Wiederholungen, die Grenzen, die Batch-Richtlinien), die Zeit, die die Anfragen auf einen Platz warteten, die geschriebenen Zusammenfassungen und was sie kosteten, sowie dasselbe für jeden Block von 25 Runden.
- **Was das Modell schrieb**: die Zeichen der Argumente, des Texts neben den Aufrufen und der Überlegung, insgesamt und für jeden Block von 25 Runden mit der Zeit des Modells, um zu sehen, wohin seine Ausgabe geht und ob es irgendwann aufhörte, `@file` zu verwenden. **Längste Argumente** zeigt den Anfang der größten Aufrufe, mit dem Pfad eines Dateiwerkzeugs.
- **Runden, verloren an einem Muster**: dasselbe Werkzeug, Runde für Runde aufgerufen (drei oder mehr Mal), markiert, wenn direkt davor eine Suche stand; und die Aufrufe, die mit denselben Argumenten erneut gemacht wurden. Diese zeigen, wo ein Werkzeug, das viele Elemente annimmt, oder mehr Aufrufe in einer Antwort Runden einsparen würde.
- **Fehler** mit den Argumenten des ersten Aufrufs, der jede Meldung erhielt (um zu sehen, was das Modell sendete, als ein Aufruf fehlschlug), die **größten Ergebnisse**, die **langsamsten Runden**, die **Verdichtungen** und die **Unteragenten** mit den Werkzeugen, die jeder aufrief.

**Herunterladen** gibt den Bericht als Markdown aus, **JSON herunterladen** als JSON, **Kopieren** legt das Markdown in die Zwischenablage. Die Zeiten sind die Abstände zwischen den gespeicherten Zeilen und daher ungefähr: Die Zeit einer Antwort schließt das Warten auf einen freien Platz beim Modell ein.
<a id="the-context"></a>

### Der Kontext

Der Tab **Kontext** zeigt, was das Modell mit seiner **nächsten Anfrage** liest, aufgeschlüsselt, damit Sie sehen, wohin die Token gehen, und einen Agenten mit merkwürdigem Verhalten debuggen können. Er wird beim Öffnen des Tabs und beim Drücken von **Aktualisieren** erstellt (nicht während der Lauf arbeitet: dann werden die Token jedes Teils gezählt).

- Drei Diagramme: die **Arten von Teilen** gegen das Limit des Kontexts (der Prompt des Agenten, Ihre Anweisungen, die Notizen des Laufs, die Werkzeugdefinitionen, die Zusammenfassung einer Verdichtung, Ihre Nachrichten, die Antworten des Agenten, die Ergebnisse seiner Aufrufe und was frei ist), **jeden Teil in der Reihenfolge, in der er gesendet wird** (ein Balken für jeden, in der Farbe seiner Art: ein Werkzeug mit riesiger Beschreibung oder ein Ergebnis, das den Kontext füllt, fällt auf), und den **Kontext bei jeder Antwort des Modells**: ein gestapelter Balken aus dem, woraus er bestand (dieselben Arten, geschätzt), die Größe, die das Modell **gemeldet** hat, als Linie, und das Limit als gestrichelte Linie. Liegt die Linie über den Balken, hat das Modell mehr gezählt als die Schätzung (Bilder, das Format des Anbieters); ein Balken, der sinkt, ist eine Verdichtung oder ein Zurückspulen.
- Eine **Tabelle der Teile** mit Art, Name, Token und Anteil am Kontext. Wählen Sie eine Zeile, um **genau den Text zu lesen, den das Modell für diesen Teil erhält**: einen Abschnitt der Systemnachricht, die Beschreibung und Parameter eines Werkzeugs, eine Nachricht, die Argumente eines Aufrufs oder das Ergebnis eines solchen.
- **Herunterladen** liefert den gesamten Kontext als eine Markdown-Datei (die Systemnachricht mit ihren Abschnitten, jedes Werkzeug mit Beschreibung und Schema, die Unterhaltung Nachricht für Nachricht, jeder Text in einem Codeblock), **Kopieren** legt denselben Text in die Zwischenablage.

![Der Tab Kontext](../images/agent-context.png)

Die Größen der Teile sind Schätzungen; die Zahl, die das Modell gemeldet hat, ist die echte (die Anfrage und ihre Antwort). Im Diagramm der Antworten sind der Prompt, die Anweisungen und die Werkzeuge so, wie sie jetzt sind (eine ältere Antwort hatte vielleicht andere, wenn Sie etwas geändert haben), die Unterhaltung ist so, wie sie damals war: eine Zusammenfassung hat das ersetzt, was davor stand, und was ein Zurückspulen zurückgenommen hat, ist in den Antworten danach nicht mehr vorhanden (eine Verdichtung, die selbst zurückgespult wurde, wird nicht wiederhergestellt, die Balken danach sind etwas zu klein). Die Systemnachricht und die Werkzeuge sind die, die die nächste Anfrage verwenden würde, daher zeigt sich eine Änderung des Prompts, der Anweisungen oder der erlaubten Befehle sofort. Die Nachrichten sind das gespeicherte Protokoll, ohne das, was ein Fortsetzen hinzufügt (ein „mach weiter“, die Antworten auf Aufrufe, die eine Unterbrechung offen gelassen hat). Überlegungen sind nicht Teil des Kontexts (das Modell bekommt sie nicht zurückgeschickt), und Bilder erscheinen mit ihren Dateinamen. Was eine Verdichtung ersetzt hat, ist ebenfalls nicht im Kontext; der Tab **Lauf** und der Download **Läufe** haben es noch.

## Checkpoints

Ein **Checkpoint** ist ein Wiederherstellungspunkt der Arbeitskopie des Laufs (die Teile, Zusammenfassungen, Lorebooks und die Daten der Erweiterungen, mit den Griffen, die der Agent bekommen hat) und des Arbeitsbereichs des Agenten, damit ein Auftrag, der schiefgegangen ist, rückgängig gemacht werden kann: Der Agent geht mit `checkpoint_restore` zurück, oder Sie tun es im Tab **Checkpoints** der Konsole. Wer sie erstellt:

- **Start des Laufs**, erstellt, wenn der Lauf beginnt. Er wird nie ausgerollt.
- **Automatisch**, erstellt vom Lauf vor einer Runde, in der der Agent etwas aufruft, das das Buch oder den Arbeitsbereich ändern kann (ein Befehl, der nicht bloß ein Lesen ist, oder ein Dateiwerkzeug, das schreibt), und nur wenn sich seit dem letzten Checkpoint etwas geändert hat. Die letzten fünf werden behalten; der älteste geht, wenn ein neuer erstellt wird.
- **Absichtlich erstellt**, vom Agenten mit `checkpoint` (er wird angewiesen, vor einem riskanten Schritt einen zu erstellen) oder von Ihnen mit **Einen Checkpoint speichern**. Sie bleiben, bis sie gelöscht werden.

![Der Tab Checkpoints](../images/agent-checkpoints.png)

Alle Checkpoints eines Laufs teilen sich ein **Kontingent**, das der Administrator in der [Berechtigung](administration/permissions.md#settings-of-a-permission) festlegt (100 MB, wenn leer). Ein Teil davon ist für die automatischen reserviert, damit der Agent nicht alles aufbrauchen kann; ein absichtlich erstellter Checkpoint, der nicht hineinpasst, wird **abgelehnt**, nichts wird hinter dem Rücken dessen gelöscht, der darum gebeten hat. Der Agent bekommt dann die Liste und löscht, was er nicht braucht (nicht den Checkpoint des Starts und nicht den neuesten); Sie können jeden im Tab löschen. Jeder ist ein Archiv (eine ZIP-Datei mit dem Buch als JSON, den Paaren und den Dateien des Arbeitsbereichs), das Sie **herunterladen** können.

**Wiederherstellen** macht die Arbeitskopie aus dem Checkpoint erneut: Es ist eine neue Kopie, die alte wird gelöscht, der Arbeitsbereich ist der gespeicherte (danach geschriebene Dateien sind weg), und die Griffe des Agenten für die Teile, die dort waren, funktionieren weiterhin. Der Zustand davor wird zuerst als automatischer Checkpoint gespeichert, falls Platz ist, damit eine Wiederherstellung zurückgenommen werden kann. Eine Wiederherstellung dauert etwa so lange wie der Start eines Laufs (eine halbe Minute bei einem Buch mit zweitausend Teilen). Sie können wiederherstellen, während der Agent nicht arbeitet; der Agent stellt sich zwischen zwei Runden selbst wieder her und wird darüber informiert, was zurückgekommen ist. Die Checkpoints verschwinden, wenn der Lauf seine Kopie loslässt.

**Zurückspulen** ist eine Wiederherstellung, die auch die *Unterhaltung* zurücknimmt. Eine bloße Wiederherstellung lässt den Agenten sich an Arbeit erinnern, die nicht mehr im Buch steht; deshalb braucht ein Lauf, den Sie in eine andere Richtung schicken wollen, ein Zurückspulen: Die Arbeitskopie und der Arbeitsbereich gehen zum Checkpoint zurück, die Nachrichten nach der Stelle, an der der Checkpoint erstellt wurde, werden im Protokoll ausgegraut (Sie können sie noch lesen, und der Export enthält sie), und der Agent liest sie nicht mehr; ein Hinweis teilt dem Agenten mit, was geschehen ist, damit er die Arbeit neu macht, falls sie noch gebraucht wird. Der Lauf muss angehalten oder gestoppt sein (einen Agenten, der auf Sie wartet, zuerst anhalten); danach **Fortsetzen** Sie ihn, und er geht vom Hinweis aus weiter. Auf einen Checkpoint kann nur zurückgespult werden, solange seine Stelle in der Unterhaltung noch Teil von ihr ist: Nach einem Zurückspulen zu einem früheren Checkpoint können die späteren (der Unterhaltung, die zurückgenommen wurde) nur wiederhergestellt werden. Ein Checkpoint, der vor dem Zurückspulen erstellt wurde, kennt seine Stelle nicht und kann nur wiederhergestellt werden. Was eine Verdichtung nach dem Checkpoint getan hat, wird mit dem Zurückspulen rückgängig gemacht; der Agent liest die Nachrichten, die die Verdichtung ersetzt hatte, wieder.

**Zurück zu einer Nachricht gehen.** Sie müssen keinen Checkpoint auswählen: Jede Nachricht, die Sie an den Agenten geschrieben haben, hat unter sich im Protokoll des Tabs **Lauf** eine Schaltfläche **Zurückspulen**. Sie nimmt die Unterhaltung bis vor diese Nachricht zurück: Der Agent vergisst die Nachricht und alles danach, und die Arbeitskopie und der Arbeitsbereich gehen zum neuesten Checkpoint zurück, der vor ihr erstellt wurde (der Lauf erstellt vor jeder Runde, die etwas ändert, einen, daher stimmt das meist genau bei der Nachricht). Der Zustand eines Laufs ist nur an seinen Checkpoints bekannt; liegt der nächste davon älter als die Nachricht, werden auch die Nachrichten zwischen diesem Checkpoint und Ihrer vergessen; die Bestätigung sagt, um welchen Checkpoint es sich handelt und wie viele Nachrichten wegfallen. Die vergessenen Nachrichten bleiben im Protokoll, ausgegraut. Die Schaltfläche funktioniert, während der Agent auf Sie wartet (er ist dafür angehalten), angehalten ist oder gestoppt und behalten wurde; während der Agent arbeitet, ist sie deaktiviert. Danach **Fortsetzen** Sie den Lauf, und er geht vom Hinweis aus weiter.
<a id="creating-and-editing-an-agent"></a>

## Erstellen und Bearbeiten eines Agenten

**Neuer Agent** öffnet dasselbe Fenster wie **Bearbeiten**:

![Das Fenster eines neuen Agenten](../images/agent-dialog.png)

| Feld | |
|---|---|
| **Name** | Pflichtfeld. |
| **Aktiviert** | Ein deaktivierter Agent kann nicht gestartet werden; seine bereits vorhandenen Läufe bleiben unberührt. |
| **Buch** | Das Buch, an dem der Agent arbeitet, Pflichtfeld. Es werden nur Bücher angeboten, die noch keinen Agenten haben, und das Buch eines Agenten kann später nicht mehr geändert werden. |
| **Modell**, **Protokolle** | Das Modell und das Protokoll, über das der Agent mit ihm spricht. Beides oder nichts; ein Agent ohne beides kann nicht gestartet werden. |
| **Das Buch mit dem Modell des Agenten schreiben** | Eingeschaltet (der Standard): Die Befehle, die ein Modell benötigen, schreiben mit dem oben genannten Modell, egal welches Modell dein Buch hat. Dazu gehören der nächste Abschnitt, Swipes, Zusammenfassungen und die Befehle von Erweiterungen wie der Lore-Generator und die Handlungsbogen-Gliederung. Ausgeschaltet: Sie verwenden das Modell deines Buches (ein Erweiterungsbefehl greift auf dein Standardmodell aus den [Einstellungen](account.md) zurück, wenn das Buch keines hat). So kombinierst du Modelle: ein starkes, das denkt, und ein günstigeres oder flüssigeres, das schreibt. Ein Befehl kann trotzdem ein anderes Modellprofil des Agenten benennen. |
| **Aufrufe in einer Antwort** | Wie viele der Aufrufe, die der Agent in einer Antwort schreibt, ausgeführt werden. *Alle zusammen, Stapelrichtlinien ignorieren*: jeder Aufruf (am schnellsten). *Zusammen, als „Allein“ markierte Befehle für sich* (der Standard): Die Aufrufe werden gemeinsam ausgeführt, aber ein Befehl, dessen Spalte **Stapel** den Wert *Allein* hat, wie ein Befehl, der ein Modell aufruft, wird in einer eigenen Antwort ausgeführt, und der Agent wird darüber informiert. *Ein Aufruf je Antwort*: Nur der erste Aufruf wird ausgeführt, und der Agent schreibt den nächsten erst, nachdem er das Ergebnis gelesen hat; langsamer (jeder Aufruf ist eine eigene Runde mit dem Denken des Modells), aber er kann auf jedes Ergebnis reagieren. Ein **Fehler beendet immer die Antwort**, in allen Modi: Die Aufrufe nach dem fehlgeschlagenen werden nicht ausgeführt (sie werden mit „nicht ausgeführt, weil ein früherer Aufruf fehlgeschlagen ist“ beantwortet), und der Agent liest zuerst den Fehler. |
| **Das Modell sieht Bilder** | Ankreuzen, wenn das Modell Bilder verarbeiten kann (die API des Modells sagt das nicht, deshalb musst du es angeben). Der Agent erhält dann `view_image`, und du kannst deinen Nachrichten Bilder anhängen; ohne diese Option bekommt er nie ein Bild zu sehen. |
| **Sprache** | Die Sprache aller Texte, die der Agent liest: die Beschreibungen seiner Befehle, deren Ergebnisse und Fehler sowie die eingebauten Texte des Laufs (sein Standardsystemprompt, die Hinweise des Laufs, der Verdichtungsprompt), sofern sie in dieser Sprache vorliegen (Englisch, Japanisch, Chinesisch). Die [Prompt-Sprache](account.md#prompt-language) deiner Einstellungen gilt für Agenten nicht. Englisch ist der Standard und die sicherste Wahl, weil die Befehle und ihre Parameter unabhängig von der Sprache auf Englisch sind (die Beschreibungen in einer anderen Sprache mischen beides), und Modelle, vor allem kleine, setzen Werkzeuge auf diese Weise zuverlässiger ein. Die Liste enthält die in der Anwendung installierten Sprachen (eine Erweiterung kann eine weitere hinzufügen, wie die chinesische Lokalisierung). Sie hat **nichts mit der Sprache des Buches zu tun**: Dem Agenten wird gesagt, dass er den Buchtext in der Sprache des Buches schreiben soll, die er mit `get-book-info` lesen kann. Ein Lauf behält die Sprache, mit der er gestartet wurde; eine Änderung wirkt auf den nächsten Lauf. |
| **Befehle, die Daten ändern** | Was passiert, wenn der Agent einen Befehl ausführen will, der Daten ändert oder entfernt: *Ausführen*, *Erst fragen* (ein [Fenster](#asking-you) bittet dich, es zu genehmigen) oder *Ablehnen*. |
| **Maximale Runden**, **Token-Budget eines Laufs**, **Kontextlimit** | Optionale Grenzen eines Laufs. Das Limit, das dir dein Administrator vorgegeben hat, gilt in jedem Fall. |
| **Maximale Anzahl Unteragenten** | Wie viele [Unteragenten](#sub-agents) der Agent gleichzeitig arbeiten lassen kann, 0 bis 10. Leer oder null bedeutet keine. |
| **Modelle für Unteragenten** | Die [Modelle](#models-for-sub-agents), auf denen ein Unteragent laufen darf, und ob er auf dem Hauptmodell laufen darf. |
| **Verdichten bei (% des Kontexts)** | Überschreitet der Kontext des Modells diesen Anteil seines Limits, und der Agent hat seine Arbeit nicht selbst [verdichtet](#running-a-command-yourself), übernimmt das der Lauf: Der Agent schreibt die Zusammenfassung seiner bisherigen Arbeit, die diese ersetzt. Leer bedeutet 90, null nie (der Lauf hält dann an, wenn der Kontext voll ist), andernfalls 50 bis 99. |
| **Systemprompt** | Was dem Agenten vorgegeben wird, wer er ist. Lässt du das Feld leer, folgt der Agent dem Standard – deinem aus den [Einstellungen](account.md#templates), sonst dem deines Administrators, sonst dem eingebauten –, der grau angezeigt wird. Das Info-Symbol hat **Standardvorlage einfügen**, das den Standard einträgt, damit du eine Kopie davon bearbeiten kannst; leere das Feld wieder, um dem Standard zu folgen. |
| **Verdichtungsprompt** | Was der Agent gefragt wird, wenn du ihn mit [`/compact`](#running-a-command-yourself) verdichtest: die Zusammenfassung zu schreiben, die seine bisherige Arbeit ersetzt. Leer folgt dem Standard, wie beim Systemprompt, mit demselben Info-Symbol. |
| **Anweisungen (agents.md)** | Deine Anweisungen an den Agenten, die bei jedem Lauf seinem Prompt hinzugefügt werden. Der Agent kann sie lesen, aber nie ändern. Leer folgt deinem Standard aus den [Einstellungen](account.md#templates), falls du einen hast (er wird grau angezeigt), sonst gibt es keine. Das Info-Symbol hat **Standardvorlage einfügen**, das deinen Standard einträgt, damit du eine Kopie davon bearbeiten kannst. Darunter zeigt eine Zeile, was der Prompt, die Anweisungen und die angekreuzten Befehle bei **jeder Anfrage** an Tokens ausmachen (eine Schätzung, zuzüglich der Werkzeuge für Dateien und für den Lauf sowie der Hinweise des Laufs). |
| **Erlaubte Befehle** | Siehe unten. |

![Die Prompts des Agenten und die Hinweise der Standardvorlage](../images/agent-dialog-prompts.png)

<a id="allowed-commands"></a>

### Erlaubte Befehle

Dem Agenten werden nur die Befehle angeboten, die du ankreuzt. Die Tabelle listet jeden Befehl mit seiner Wirkung auf (*Destruktiv* ändert oder entfernt Daten, *Nicht destruktiv* liest nur oder fügt nur hinzu). Das Feld **Auswahl** kreuzt viele auf einmal an: **Alle**, **Keine** oder **Alle nicht destruktiven** (der Standard für einen neuen Agenten) oder eine Rolle; wenn du selbst einen Befehl ankreuzt, wird die Auswahl *Benutzerdefiniert*. Das Feld **Befehle suchen** über der Tabelle zeigt nur die Befehle, die alle Wörter enthalten, die du eintippst (in beliebiger Reihenfolge, im Namen oder in der Beschreibung); die Auswahl gilt trotzdem für alle Befehle, auch für die ausgeblendeten. Befehle, die eine Erweiterung mitbringt, bleiben erlaubt, wenn die Erweiterung nicht geladen ist; du siehst sie dann nur nicht. Die Spalte **Stapel** gibt an, ob der Befehl *Zusammen* mit anderen Aufrufen in einer Antwort verwendet wird oder *Allein* (ein Befehl, der ein Modell aufruft, wie `next`, `swipe`, `regenerate`, `redraft-part`, `generate-annotation` oder die Verdichtung, läuft allein; siehe unten **Aufrufe in einer Antwort**). Alle Spalten lassen sich in der Breite ändern. Die Spalte **Tokens** gibt an, was die Definition des Befehls (sein Name, seine Beschreibung und seine Parameter) jeder Anfrage des Agenten hinzufügt; die Zeile unter den Anweisungen addiert die angekreuzten. Die Gesamtmenge beträgt etwa 20 000 Tokens, daher lässt eine kleinere Auswahl (eine Rolle) mehr Platz im Kontext für die Arbeit.

![Die erlaubten Befehle mit den Rollen des Feldes Auswahl](../images/agent-commands.png)

Die Rollen sind Befehlssätze für eine bestimmte Aufgabe. Alle können das Buch und die Lorebooks lesen (jeder Befehl, der nur liest, auch die von Erweiterungen), und keine kann Daten entfernen:

| Rolle | Darüber hinaus |
|---|---|
| **Auditor** | Nichts: Kontinuitätsprüfungen, Prüfungen von Namen und Begriffen, Berichte. |
| **Lore-Hüter** | Erstellt und aktualisiert Lorebook-Einträge und Lorebooks, deren Tags und Unter-Lorebooks. |
| **Lektor** | Bearbeitet und ergänzt Teile, setzt deren Felder, pflegt die Zusammenfassungen (lässt sie auch vom Modell neu schreiben) sowie die Buchinformationen und Tags. |
| **Entwerfer** | Fügt Teile und Verzweigungen hinzu, bearbeitet und ergänzt sie, lässt das Modell den nächsten Abschnitt schreiben (`/next`, `/swipe`), pflegt die Zusammenfassungen, erstellt und aktualisiert Lorebook-Einträge zu dem, was er erfindet. |

Jeder angekreuzte Befehl fügt seine Definition jeder Anfrage hinzu; eine Rolle ist daher auch der günstigste Weg zu einem kleinen Prompt für ein schwaches Modell.
<a id="on-a-phone"></a>

## Auf dem Telefon

Die Telefonansicht hat in der Schublade einen Reiter **Agenten** für Benutzer, die Agenten verwenden dürfen, mit der Anzahl der Agenten, die auf Sie warten, neben dem Namen. Jeder Agent ist eine Karte mit seinem Namen, seinem Zustand und den Schaltflächen der Tabelle (sie brechen in mehrere Zeilen um). Die Fenster - die Konsole, die Prüfung, die Einstellungen des Agenten - füllen den Bildschirm, und die Prüfung zeigt die Tabelle über den Details des Eintrags. Alles funktioniert wie auf dem Desktop, nur kleiner. Die Konsole ist für den Daumen angelegt:

![Der Reiter Agenten auf dem Telefon](../images/phone-agents.png)

![Die Konsole auf dem Telefon](../images/phone-agent-console.png)

![Der Reiter Agenten auf dem Telefon](../images/phone-agents.png)

![Die Konsole auf dem Telefon](../images/phone-agent-console.png)

- **Die Reiter sind Symbole** (Lauf, Läufe, Arbeitsbereich, Kontrollpunkte, Kontext, Buchkopie, Unteragenten, Diagnose); der Titel des Fensters nennt den geöffneten.
- **Pausieren, Jetzt pausieren, Fortsetzen, Beenden und Stopp sind Symbolschaltflächen unten, und es werden nur die angezeigt, die jetzt benutzt werden können**; **Änderungen prüfen** behält seinen Wortlaut, denn darauf wartet ein abgeschlossener Lauf. **Aktualisieren** und **Schließen** sind Symbole in der Kopfzeile.
- **Das Nachrichtenfeld ist einzeilig** mit den Symbolen **Senden** und **Bild anhängen** daneben. Die **Befehlszeile** (das Terminalsymbol) ist ausgeblendet, bis Sie sie öffnen, damit die Tastatur das Protokoll nicht unnötig verdeckt. Die Nachrichten des Protokolls nutzen die volle Breite.
- **Tabellen sind Listen von Karten**: ein Lauf, ein Kontrollpunkt, ein Befehl aus den Einstellungen des Agenten, ein Eintrag der Prüfung zeigen jeweils ihren Namen und Zustand, darunter die Details und unten ihre Schaltflächen, anstelle von Spalten. Die Dateien des Arbeitsbereichs zeigen ihre Größe unter dem Namen, und die Werkzeugleiste des Arbeitsbereichs besteht aus Symbolen. Die Kontexttabelle behält Nummer, Name und Tokens; Typ und Anteil eines Teils stehen unter dem Text des gewählten Teils.

- **Die obere Leiste zeigt, wer auf Sie wartet.** Solange die Schublade geschlossen ist, zählt eine rote Zahl in der oberen Leiste (neben dem Titel des geöffneten Teils) die Agenten, die Sie brauchen, so wie das Abzeichen am Reiter **Agenten** in der Schublade. Tippen Sie darauf, um zum Reiter **Agenten** zu gelangen. Sie ist ausgeblendet, wenn niemand wartet, solange die Schublade offen ist und im Reiter Agenten selbst.
- **Wischen Sie zwischen den Teilen.** Ein seitliches Wischen über den Inhalt führt zum nächsten Teil der Schublade (nach links wischen) oder zum vorherigen (nach rechts). Bei offener Schublade oder wenn die Anwendung die Navigation sperrt, geschieht nichts; eine Berührung, die in einem Textfeld, einer Tabelle oder in einem Bereich beginnt, der seitlich scrollt, gehört diesem Bereich.

Für lange Arbeit am Protokoll oder an der Prüfung ist ein größerer Bildschirm weiterhin bequemer.

<a id="cloning"></a>

## Klonen

**Klonen** fragt nach einem Namen und einem Buch ohne Agenten und kopiert die Definition des Agenten und seine Dateien (nicht seine Läufe) in einen neuen Agenten dieses Buchs. Sie können so viele Agenten haben, Klone eingeschlossen, wie Ihr Administrator erlaubt (fünf, sofern er keine andere Zahl festlegt); am Limit sind **Neuer Agent** und **Klonen** deaktiviert und sagen, warum.

![Einen Agenten klonen](../images/agent-clone.png)

<a id="lore-notes-of-an-imported-book"></a>

## Figurennotizen eines importierten Buches

Nachdem Sie ein Manuskript [importiert](books/story-editor.md#after-an-import) haben, bietet die Anwendung einen Agenten an, der das Lore des Buches übernimmt: **Figurennotizen mit einem Agenten erstellen...** (derselbe Eintrag steht im Einstellungsmenü ⚙ einer Geschichte, **Analyse**). Zuerst fragt sie nach:

![Die Frage vor dem Start des Agenten](../images/lore-notes-confirm.png)

Dann erstellt sie einen Agenten namens **Lore notes** für das Buch, mit dem Modell und dem Protokoll des Buches, und startet ihn (seine Konsole öffnet sich).

- Er liest die Geschichte vom ersten bis zum letzten Teil, einige Teile auf einmal, und hält seine Notizen in den Dateien seines Arbeitsbereichs fest (Figuren, Orte, Dinge, Begriffe), damit er nach einer Pause weitermachen kann. Die Zusammenfassungen helfen ihm, sich zurechtzufinden; die Teile sind seine Quelle.
- Danach schreibt er für jede Figur, jeden Ort, jedes Ding, jede Gruppe oder jeden Begriff einen Eintrag in das Lorebook des Buches (für das Buch wird ein Lorebook angelegt, falls es keines hat), mit den Namen, die die Geschichte verwendet, als Filter, in der Sprache des Buches. Er ergänzt Fakten zu einem bestehenden Eintrag und schreibt dieselbe Sache nicht zweimal.
- Er darf die Geschichte und die Lorebooks lesen und Einträge schreiben; er kann die Geschichte nicht ändern und nichts löschen.

Er ist wie jeder andere Agent: Das Buch ist [gesperrt](#the-book-is-locked), solange er arbeitet, er arbeitet auf einer [Kopie](#the-working-copy), und was er geschrieben hat, gehört erst dann Ihnen, wenn Sie es [prüfen](#reviewing-the-result) und übernehmen. Lesen Sie die Einträge, bevor Sie das tun: Er kennt nur das, was im Text steht, und ein langes Buch kostet viele Anfragen. Die Anweisungen sind die des Agenten (**Bearbeiten** zeigt sie, Sie können sie vor einem erneuten Start ändern).
Er wird abgelehnt, wenn Sie keine Agenten verwenden dürfen, wenn das Buch kein Modell hat und wenn das Buch bereits einen Agenten hat (ein Buch hat einen).

<a id="the-book-is-locked"></a>

## Das Buch ist gesperrt

Solange ein Lauf eines Agenten arbeitet oder auf Sie wartet, **ist sein Buch gesperrt**: Sie können es lesen, aber nichts verändert es, sodass der Agent nie an einem Text arbeitet, der sich unter ihm verschiebt. Die Buchliste öffnet ein gesperrtes Buch nicht, sondern sagt Ihnen, welcher Agent es hält, und Erzeugen, Bearbeiten, Suchen und Ersetzen, Zusammenfassungen und das Löschen des Buches werden abgelehnt. **Pausieren** oder **Stopp** den Lauf, um das Buch wieder zu bearbeiten; ein pausierter, gestoppter oder abgeschlossener Lauf sperrt es nicht.

![Ein Buch, an dem ein Agent arbeitet, in der Buchliste](../images/agent-locked-book.png)

Die **Lorebooks des Buches** sind mit ihm gesperrt: das Lorebook, das das Buch verwendet, und seine Unter-Lorebooks. Auf dem Reiter Lorebooks wird ein solches Lorebook so angezeigt, wie auf einem Telefon, als reiner Text und ohne jede Bearbeitung, mit einem Hinweis, welcher Agent es hält; **Aktualisieren** zeigt es nach dem Pausieren oder Stoppen des Laufs wieder zur Bearbeitung an. Andere Lorebooks sind nicht betroffen.

<a id="agents-and-folders"></a>

### Agenten und Ordner

Ein Buch in einem [Ordner](books/folders.md), an dem ein Agent arbeitet, behält seinen Platz und seine Einstellungen: Das Verschieben oder Löschen eines Ordners darüber sowie das Ändern einer Einstellung, die so ein Ordner weitergibt (Sprache, Vorlagen, Erzählperspektive, Zeitform, Stil, Anweisungen), wird mit dem Namen des Buches und des Agenten abgelehnt. Das Umbenennen eines Ordners oder das Ändern seiner Beschreibung wird nicht abgelehnt. Ein pausierter, gestoppter oder abgeschlossener Lauf gibt es frei.

![Das Lorebook eines Buches, an dem ein Agent arbeitet](../images/agent-locked-lorebook.png)
