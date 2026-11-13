---
label: Erweiterungen verwalten
order: 70
---

# Erweiterungen verwalten

Der Reiter **Extensions** der Administrationsseite installiert und entfernt [Erweiterungen](../extensions/index.md) für die gesamte
Installation.

![Der Reiter Extensions](../../images/admin-extensions.png)

<a id="installing"></a>

## Installieren

Klicken Sie auf **Erweiterung laden (.jar)** (oder ziehen Sie die Datei darauf) und wählen Sie die JAR-Datei der Erweiterung aus. Sie wird in den
Ordner `extensions` des [Datenordners](index.md#the-data-folder) kopiert und sofort gestartet; *Extension loaded
successfully.* bestätigt dies. Die Erweiterung erscheint in der Liste:

| Spalte | |
|---|---|
| **ID** | Die interne Nummer der Erweiterung in diesem Lauf. |
| **Name der Erweiterung** | Ihr Name. |
| **Version** | Ihre Version. Die Schaltfläche **#** daneben zeigt beim Darüberfahren den SHA-256-Hash der JAR-Datei der Erweiterung; klicken Sie darauf, um den Hash zu kopieren, z. B. um ihn mit dem vom Autor veröffentlichten zu vergleichen. |
| **Status** | *Aktiv*, wenn sie läuft; *Gestoppt*, wenn Sie sie angehalten haben (siehe [Anhalten und Starten](#stopping-and-starting)); *Installiert* oder *Aufgelöst*, wenn der Start fehlgeschlagen ist (siehe [Wenn eine Erweiterung fehlschlägt](#when-an-extension-fails)). |
| **Überprüfung** | *Gültig* oder *Ungültig*; klicken Sie darauf, um den Bericht der Prüfung zu lesen, die Marginalia vor dem Start der Erweiterung durchgeführt hat. |
| **Aktionen** | **Stopp** / **Starten**, **Protokoll** und **Entladen**. |

Benutzer sehen die Erweiterung in Fenstern, die sie von nun an öffnen - ein Buch, das bereits offen war, muss geschlossen und erneut geöffnet werden, oder die Seite neu geladen werden.

JAR-Dateien können auch direkt in den Ordner `extensions` kopiert werden, etwa auf einem Server ohne Zugriff auf die Oberfläche; sie werden beim nächsten Start geladen. So lässt sich auch die Docker-Installation vorbereiten: Legen Sie die JARs in `./data/extensions`. Die JARs können auch in Docker gebaut werden, siehe
[Plugins](../getting-started/docker-server.md#plugins) im Server-Handbuch.

!!!warning Für diese Version gebaute Erweiterungen verwenden
Erweiterungen verändern Teile der Benutzeroberfläche einer bestimmten Marginalia-Version. Verwenden Sie Erweiterungen, die aus demselben Quellcode wie die Anwendung gebaut wurden (siehe die [Plugins-README](https://github.com/Enerccio/Marginalia/blob/master/marginalia/plugins/README.md)).
Eine inkompatible Erweiterung kann sich nicht laden lassen oder Teile der Anwendung beschädigen. Die Erweiterungs-API ist in 1.0 nicht eingefroren, daher sollten Sie Erweiterungen zusammen mit Marginalia aktualisieren.
!!!

<a id="stopping-and-starting"></a>

## Anhalten und Starten

**Stopp** entlädt die Erweiterung sofort, ohne sie zu entfernen: Sie verschwindet aus den Bildschirmen, zu denen sie hinzugefügt wurde, aber ihre JAR-Datei und ihre Daten bleiben erhalten, und sie wird mit dem Status *Gestoppt* aufgelistet. Die Schaltfläche heißt dann **Starten**, und damit wird sie wieder geladen
(vorher wird sie geprüft, wie beim Start der Anwendung; schlägt die Prüfung fehl, bleibt sie gestoppt und der Bericht wird angezeigt). Eine angehaltene Erweiterung bleibt nach einem Neustart von Marginalia angehalten. Das Hochladen einer neuen Version einer angehaltenen Erweiterung startet sie.

Das Anhalten wird durch eine leere Datei gespeichert, die wie die JAR-Datei heißt, mit angehängtem `.STOPPED` (`sidequery.jar.STOPPED`) im Ordner `extensions`. Auf einem Server ohne Zugriff auf die Oberfläche legen Sie diese Datei an, um das Starten einer Erweiterung zu verhindern, oder löschen Sie sie, damit die Erweiterung beim nächsten Start startet.

<a id="the-extension-log"></a>

## Das Protokoll der Erweiterung

**Protokoll** öffnet das Protokoll der Erweiterung: was die Erweiterung selbst mit dem Logger geschrieben hat, den Marginalia für Erweiterungen bereitstellt
([Logging](../../developer/plugins/overview.md#logging)). Es zeigt den letzten Teil der Datei; **Aktualisieren** liest sie erneut ein und **Leeren** löscht sie. Die Datei ist `extensions/<name>.jar.log` (zum Beispiel `sidequery.jar.log`); wächst sie über 2 MB, wird sie nach `.log.old` verschoben und eine neue begonnen. Sie wird gelöscht, wenn die Erweiterung entladen wird. Erweiterungen, die nichts protokollieren, haben ein leeres Protokoll. Was die Erweiterung protokolliert, landet außerdem im Anwendungsprotokoll (mit dem Namen `extension.<name>`, auf der Stufe der jeweiligen Zeile), es steht also auch im Protokoll des Servers; Probleme von Marginalia selbst stehen nur dort.

![Das Protokoll einer Erweiterung](../../images/extension-log.png)

<a id="removing"></a>

## Entfernen

**Entladen** fragt nach einer Bestätigung und stoppt dann die Erweiterung sofort und löscht ihre JAR-Datei (und ihr Protokoll) aus dem Ordner `extensions`.
Ihre Daten bleiben erhalten (siehe [Wo die Daten einer Erweiterung gespeichert werden](../extensions/index.md#where-extension-data-is-kept)); wenn Sie sie erneut laden, sind die Daten wieder da.

<a id="updating"></a>

## Aktualisieren

Um eine neuere Version einer Erweiterung zu installieren, verwenden Sie **Erweiterung laden (.jar)** mit der neuen Datei - der Dateiname spielt keine Rolle. Die installierte Version wird entladen, ihre JAR-Datei gelöscht und die neue Version an ihrer Stelle gestartet. Schlägt das Laden der neuen Version fehl, zeigt der Upload den Fehler an und die alte Version läuft weiter.

<a id="when-an-extension-fails"></a>

## Wenn eine Erweiterung fehlschlägt

Kann eine hochgeladene Erweiterung nicht geladen werden, zeigt der Upload *Failed to load extension: ...* an, und ihre JAR-Datei wird nicht behalten.

Marginalia prüft eine Erweiterung gegen die Version, in der sie läuft, bevor sie gestartet wird. Eine Erweiterung, die nicht passt - typischerweise eine für eine andere Marginalia-Version gebaute - wird nicht geladen. Ein Upload zeigt den Bericht der Prüfung und wird nicht behalten; eine Erweiterung im Ordner `extensions` bleibt in der Liste, nicht gestartet, mit **Ungültig** in der Spalte *Überprüfung*, und der Bericht steht im Protokoll. Die Prüfung wird nach einer Aktualisierung von Marginalia wiederholt, daher kann eine Erweiterung, die zuvor funktionierte, nachträglich abgelehnt werden: Besorgen Sie sich die Version der Erweiterung, die für die neue Version von Marginalia gebaut wurde, und laden Sie sie. Solange eine Erweiterung ungültig ist, zeigt ein **roter Balken oben im Arbeitsbereich** allen an, dass etwas nicht stimmt: Ein Administrator sieht beim Darüberfahren, welche Erweiterungen betroffen sind, und öffnet mit einem Klick auf den Balken diese Seite; andere Benutzer werden gebeten, es dem Administrator mitzuteilen (eine Erweiterung, die Sie selbst angehalten haben, wird nicht mitgezählt). Neben der JAR-Datei jeder Erweiterung finden Sie eine kleine Datei `.valid` oder `.invalid` mit dem Bericht; sie wird zusammen mit der Erweiterung entfernt und kann auch unangetastet bleiben.

![Der rote Balken über dem Arbeitsbereich, wenn eine Erweiterung ungültig ist](../../images/invalid-extensions-warning.png)

![Der Prüfbericht einer Erweiterung, die nicht passt, und eine angehaltene Erweiterung (Schaltfläche Starten)](../../images/extension-verification.png)

Eine Erweiterung im Ordner `extensions`, die nicht startet (zum Beispiel eine für eine andere Version gebaute), hindert die anderen nicht am Laden. Sie wird mit dem Status *Installiert* oder *Aufgelöst* statt *Aktiv* aufgelistet und kann mit **Entladen** entfernt werden. Eine Datei, die gar keine Erweiterung ist, wird übersprungen und nicht aufgelistet - löschen Sie sie aus dem Ordner `extensions`. Das Protokoll (`docker compose logs` oder `desktop/logs/marginalia.log` für die Desktop-App) enthält die Details des Fehlers.
