---
label: Erweiterungen verwalten
order: 70
---

# Erweiterungen verwalten

Der Reiter **Extensions** der Admin-Seite installiert und entfernt [Erweiterungen](../extensions/index.md) für die gesamte Installation.

![Der Reiter Extensions](../../images/admin-extensions.png)

<a id="installing"></a>

## Installieren

Klicken Sie auf **Load Extension (.jar)** (oder ziehen Sie die Datei darauf) und wählen Sie die JAR-Datei der Erweiterung aus. Sie wird in den Ordner `extensions` des [Datenordners](index.md#the-data-folder) kopiert und sofort gestartet; *Extension loaded successfully.* bestätigt das. Die Erweiterung erscheint in der Liste:

| Spalte | |
|---|---|
| **ID** | Die interne Nummer der Erweiterung in diesem Lauf. |
| **Extension Name** | Ihr Name. |
| **Version** | Ihre Version. Die Schaltfläche **#** daneben zeigt beim Darüberfahren den SHA-256-Hash der JAR-Datei der Erweiterung; klicken Sie darauf, um den Hash zu kopieren, z. B. um ihn mit dem vom Autor veröffentlichten zu vergleichen. |
| **State** | *Active*, wenn sie läuft; *Stopped*, wenn Sie sie angehalten haben (siehe [Anhalten und Starten](#stopping-and-starting)); *Installed* oder *Resolved*, wenn sie nicht starten konnte (siehe [Wenn eine Erweiterung fehlschlägt](#when-an-extension-fails)). |
| **Verification** | *Valid* oder *Invalid*; klicken Sie darauf, um den Prüfbericht zu lesen, den Marginalia vor dem Start der Erweiterung erstellt hat. |
| **Actions** | **Stop** / **Start**, **Log** und **Unload**. |

Benutzer sehen die Erweiterung ab dann in Fenstern, die sie öffnen – ein Buch, das bereits offen war, muss geschlossen und erneut geöffnet werden, oder die Seite neu geladen werden.

JAR-Dateien können auch direkt in den Ordner `extensions` kopiert werden, zum Beispiel auf einem Server ohne Zugriff auf die Oberfläche; sie werden beim nächsten Start geladen. So lässt sich auch das Docker-Setup vorbereiten: Legen Sie die JARs in `./data/extensions` ab. Die JARs können auch in Docker gebaut werden, siehe [Plugins](../getting-started/docker-server.md#plugins) im Server-Handbuch.

!!!warning Erweiterungen für diese Version verwenden
Erweiterungen verändern Teile der Benutzeroberfläche einer bestimmten Marginalia-Version. Verwenden Sie Erweiterungen, die aus demselben Quellcode wie die Anwendung gebaut wurden (siehe die [Plugins-README](https://github.com/Enerccio/Marginalia/blob/master/marginalia/plugins/README.md)).
Eine inkompatible Erweiterung lässt sich unter Umständen nicht laden oder beschädigt Teile der Anwendung. Die Erweiterungs-API ist in 1.0 nicht eingefroren; rechnen Sie daher damit, Erweiterungen zusammen mit Marginalia aktualisieren zu müssen.
!!!

<a id="stopping-and-starting"></a>

## Anhalten und Starten

**Stop** entlädt die Erweiterung sofort, ohne sie zu entfernen: Sie verschwindet aus den Bildschirmen, zu denen sie etwas hinzugefügt hat, aber ihre JAR-Datei und ihre Daten bleiben erhalten, und sie wird mit dem Status *Stopped* aufgeführt. Die Schaltfläche heißt dann **Start**; sie lädt die Erweiterung wieder (vorher wird sie geprüft, wie beim Start der Anwendung; schlägt die Prüfung fehl, bleibt sie angehalten und der Bericht wird angezeigt). Eine angehaltene Erweiterung bleibt nach einem Neustart von Marginalia angehalten. Das Hochladen einer neuen Version einer angehaltenen Erweiterung startet sie.

Das Anhalten wird durch eine leere Datei gespeichert, die wie die JAR-Datei benannt ist, mit `.STOPPED` angehängt (`sidequery.jar.STOPPED`), im Ordner `extensions`. Auf einem Server ohne Zugriff auf die Oberfläche legen Sie sie an, damit eine Erweiterung nicht startet, oder löschen Sie sie, damit die Erweiterung beim nächsten Start startet.

<a id="the-extension-log"></a>

## Das Log der Erweiterung

**Log** öffnet das Log der Erweiterung: was die Erweiterung selbst mit dem Logger geschrieben hat, den Marginalia Erweiterungen zur Verfügung stellt ([Logging](../../developer/plugins/overview.md#logging)). Es zeigt den letzten Teil der Datei; **Refresh** liest sie erneut ein und **Clear** löscht sie. Die Datei heißt `extensions/<name>.jar.log` (zum Beispiel `sidequery.jar.log`); wenn sie über 2 MB wächst, wird sie nach `.log.old` verschoben und eine neue begonnen. Sie wird gelöscht, wenn die Erweiterung entladen wird. Erweiterungen, die nichts protokollieren, haben ein leeres Log. Was die Erweiterung protokolliert, landet auch im Anwendungslog (mit dem Namen `extension.<name>`, auf der Ebene der Zeile), also auch im Log des Servers; Probleme von Marginalia selbst stehen nur dort.

![Das Log einer Erweiterung](../../images/extension-log.png)

<a id="removing"></a>

## Entfernen

**Unload** fragt nach einer Bestätigung und stoppt dann die Erweiterung sofort und löscht ihre JAR-Datei (und ihr Log) aus dem Ordner `extensions`.
Ihre Daten bleiben erhalten (siehe [Wo Erweiterungsdaten gespeichert werden](../extensions/index.md#where-extension-data-is-kept)); wird sie erneut geladen, sind die Daten wieder da.

<a id="updating"></a>

## Aktualisieren

Um eine neuere Version einer Erweiterung zu installieren, verwenden Sie **Load Extension (.jar)** mit der neuen Datei – der Dateiname spielt keine Rolle. Die installierte Version wird entladen, ihre JAR-Datei gelöscht und die neue Version an ihrer Stelle gestartet. Lässt sich die neue Version nicht laden, zeigt der Upload den Fehler an, und die alte Version läuft weiter.

Erweiterungen lassen sich auch aus einem Skript installieren, aktualisieren, anhalten und entfernen, mit einem [API-Schlüssel](../account.md#api-keys), der das Recht *Manage the extensions* hat. Ein Skript kann zunächst den Commit vergleichen, den der Server ausführt, mit dem Quellcode, aus dem die Plugins gebaut wurden, um zu wissen, ob sie ohne Aktualisierung von Marginalia selbst übernommen werden können (siehe das [Entwicklerhandbuch](../../developer/api.md#plugins-without-a-full-update)).

<a id="when-an-extension-fails"></a>

## Wenn eine Erweiterung fehlschlägt

Lässt sich eine hochgeladene Erweiterung nicht laden, zeigt der Upload *Failed to load extension: ...* an, und ihre JAR-Datei wird nicht behalten.

Marginalia prüft eine Erweiterung vor dem Start gegen die Version, in der es läuft. Eine Erweiterung, die nicht passt – typischerweise eine, die für eine andere Version von Marginalia gebaut wurde –, wird nicht geladen. Ein Upload zeigt den Bericht der Prüfung an und wird nicht behalten; eine Erweiterung im Ordner `extensions` bleibt in der Liste, ungestartet, mit **Invalid** in der Spalte *Verification*, und der Bericht steht im Log. Die Prüfung wird nach einer Aktualisierung von Marginalia wiederholt, daher kann eine Erweiterung, die vorher funktioniert hat, danach abgelehnt werden: Holen Sie sich die Version der Erweiterung, die für die neue Version von Marginalia gebaut wurde, und laden Sie sie. Solange eine Erweiterung ungültig ist, zeigt ein **roter Balken oben im Arbeitsbereich** allen an, dass etwas nicht stimmt: Ein Administrator sieht beim Darüberfahren, welche Erweiterungen betroffen sind, und öffnet mit einem Klick auf den Balken diese Seite; andere Benutzer werden gebeten, es dem Administrator mitzuteilen (eine Erweiterung, die Sie selbst angehalten haben, wird nicht mitgezählt). Neben der JAR-Datei jeder Erweiterung finden Sie eine kleine Datei `.valid` oder `.invalid` mit dem Bericht; sie wird zusammen mit der Erweiterung entfernt und kann auch unangetastet bleiben.

![Der rote Balken über dem Arbeitsbereich, wenn eine Erweiterung ungültig ist](../../images/invalid-extensions-warning.png)

![Der Prüfbericht einer nicht passenden Erweiterung und eine angehaltene Erweiterung (Schaltfläche Start)](../../images/extension-verification.png)

Eine Erweiterung im Ordner `extensions`, die nicht startet (zum Beispiel eine, die für eine andere Version gebaut wurde), hindert die anderen nicht am Laden. Sie wird mit dem Status *Installed* oder *Resolved* statt *Active* aufgeführt und lässt sich mit **Unload** entfernen. Eine Datei, die gar keine Erweiterung ist, wird übersprungen und nicht aufgeführt – löschen Sie sie aus dem Ordner `extensions`. Das Log (`docker compose logs`, bzw. `desktop/logs/marginalia.log` für die Desktop-App) enthält die Details des Fehlers.
