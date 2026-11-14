---
label: Erster Start
order: 80
---

# Erster Start

<a id="create-the-administrator"></a>

## Administrator anlegen

Wenn Marginalia mit einer leeren Datenbank startet, fragt es nach dem ersten Konto, statt die Anmeldemaske anzuzeigen.
Dieses Konto ist der **Administrator**: Er kann Benutzer, Datenbank-Backups, Bereinigung und Erweiterungen verwalten.

![Anlegen des Administratorkontos](../../images/first-start.png)

Fülle folgende Felder aus:

| Feld | |
|---|---|
| **Benutzername** | Der Anmeldename. |
| **Vollständiger Name** | Der Name, der in der Anwendung angezeigt wird. |
| **Passwort**, **Passwort wiederholen** | Beide müssen übereinstimmen. Marginalia akzeptiert auch ein leeres Passwort – lass es auf einem Server niemals leer, sonst könnte sich jeder mit dem Benutzernamen allein als Administrator anmelden. |

*Administrator* ist für das erste Konto angekreuzt und kann nicht geändert werden. Klicke auf **OK**. Die Seite wird neu geladen und zeigt die
Anmeldemaske.

!!!
Merke dir das Passwort. Es gibt keine Passwortzurücksetzung per E-Mail. Andere Administratoren können für einen
Benutzer unter *Admin → Users* ein neues Passwort setzen.
!!!

<a id="log-in"></a>

## Anmelden

Melde dich mit dem neuen Konto an. Ist **Angemeldet bleiben** aktiviert, bleibt der Browser 30 Tage lang angemeldet. Das funktioniert nur
über HTTPS oder auf `127.0.0.1` mit der Desktop-App (siehe [Server](docker-server.md#https-and-reverse-proxy)).

![Die Anmeldemaske](../../images/login.png)

Unter dem Anmeldeformular werden die Version von Marginalia und die kurze ID des Git-Commits angezeigt, aus dem es gebaut wurde (zum Beispiel `Marginalia 1.5.0 (93ec118)`).

<a id="the-workspace"></a>

## Der Arbeitsbereich

Nach der Anmeldung siehst du den Arbeitsbereich. Die Reiter auf der linken Seite sind:

| Reiter | |
|---|---|
| **Bücher** | Deine Bücher. Ein Buch öffnen zeigt seinen Story-Editor. Siehe [Bücher](../books/index.md). |
| **Lorebooks** | Welt- und Figurennotizen für den Prompt. Siehe [Lorebooks](../lorebooks/index.md). |
| **Einstellungen** | Deine persönlichen Einstellungen: Standardmodell und Protokoll für neue Bücher, Prompt-Vorlagen, Backups. Siehe [Konto & Einstellungen](../account.md). |
| **Protokolle** | Generierungseinstellungen. Siehe [Protokolle](../protocols.md). |
| **Inferenzanbieter** | Die Modelle, mit denen Marginalia sich verbindet. Siehe [Inferenzanbieter](../inference-providers.md). |
| **Ressourcen** | Die Dateien, die du hochgeladen hast. Siehe [Ressourcen](../resources.md). |
| **Papierkorb** | Was du gelöscht hast, bis ein Administrator aufräumt. Siehe [Papierkorb](../trash.md). |

Unten befinden sich **Admin** (nur für Administratoren), die Auswahl **Sprache**, **Passwort ändern** und **Abmelden**
(siehe [Administration](../administration/index.md) für die *Admin*-Seiten).

![Der Arbeitsbereich nach der ersten Anmeldung](../../images/workspace.png)

Derselbe Versionstext steht in winziger Schrift in der linken unteren Ecke der Seite; fahre mit der Maus darüber, um die vollständige Commit-ID sowie Zeitpunkt des Commits und des Builds zu sehen. Nenne sie, wenn du ein Problem meldest. Ein Build aus einem Entwicklungs-Checkout
(`mvn jetty:run`) zeigt statt der Version `dev` an.

Marginalia merkt sich, wo du zuletzt warst. Beim nächsten Anmelden (auf jedem Gerät) öffnet es denselben Reiter, mit denselben Filtern der Buchliste, demselben Lorebook und denselben geöffneten Lorebook-Einträgen und, falls du ein Buch geöffnet gelassen hast, dieses Buch auf demselben Reiter – mit der Szene, der POV-Figur, den anwesenden Figuren und den Anweisungen für den nächsten Teil, die du eingegeben hattest. Was nicht mehr existiert (ein gelöschtes Buch, Lorebook oder Tag), wird übersprungen; ein gelöschtes Buch wird nicht geöffnet.

Weiter mit dem [Schnellstart](quick-start.md).
