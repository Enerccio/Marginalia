---
label: Benutzer
order: 100
---

# Benutzer

Der Reiter **Benutzer** der Verwaltungsseite listet alle Konten mit ihrem **Benutzernamen**, dem **Vollständigen Namen** und der Angabe, ob sie Administrator sind.

![Der Reiter Benutzer](../../images/admin-users.png)

<a id="adding-a-user"></a>

## Benutzer hinzufügen

1. Klicke auf **Benutzer hinzufügen**.
2. Trage **Benutzername**, **Vollständiger Name**, **Passwort** und **Passwort wiederholen** ein.
3. Aktiviere **Administrator**, wenn der Benutzer die Installation verwalten soll.
4. Klicke auf **OK**.

Gib der Person Benutzername und Passwort; sie kann das Passwort später selbst ändern (siehe [Konto & Einstellungen](../account.md#changing-the-password)).

!!!warning Immer ein Passwort festlegen
Marginalia akzeptiert ein Konto mit leerem Passwort – dann kann sich jeder allein mit dem Benutzernamen anmelden. Das solltest du nur in der Desktop-App auf deinem eigenen Computer tun. Beim Anlegen eines Benutzers ohne Passwort wird vorher eine Bestätigung verlangt.
!!!

Der Benutzername muss eindeutig sein.

<a id="editing-a-user"></a>

## Benutzer bearbeiten

Die Stift-Schaltfläche öffnet denselben Dialog für einen bestehenden Benutzer:

- Benutzername, vollständigen Namen und das Administrator-Kennzeichen ändern;
- beide Passwortfelder leer lassen, um das bisherige Passwort des Benutzers beizubehalten;
- um ein **neues Passwort** festzulegen, trage es in beide Passwortfelder ein;
- um das **Passwort zu löschen**, aktiviere **Passwort löschen** (die Passwortfelder werden dann deaktiviert) und bestätige. Das Konto hat danach kein Passwort.

- um zu erzwingen, dass sich die Browser des Benutzers erneut anmelden, aktiviere **Gespeicherte Anmeldungen löschen („Angemeldet bleiben“)**; die Option wird nur angezeigt, solange der Benutzer gespeicherte Anmeldungen hat. Eine offene Sitzung bleibt offen, die „Angemeldet bleiben“-Cookies des Benutzers funktionieren jedoch nicht mehr.

Für all das brauchst du das aktuelle Passwort des Benutzers nicht. Ein neues oder gelöschtes Passwort beendet alle gespeicherten Anmeldungen des Benutzers.

<a id="resetting-a-forgotten-password"></a>

### Vergessenes Passwort zurücksetzen

Marginalia bietet kein Zurücksetzen des Passworts per E-Mail. Du hast zwei Möglichkeiten:

- Lege hier ein neues Passwort fest und teile es dem Benutzer mit, der es später mit **Passwort ändern** ändern kann; oder
- aktiviere **Passwort löschen**, sage dem Benutzer, er soll sich mit seinem Benutzernamen und einem **leeren** Passwort anmelden und sofort über **Passwort ändern** ein neues Passwort setzen (das Feld *Aktuelles Passwort* bleibt leer). Auf diesem Weg erfährt der Administrator das neue Passwort nie.

!!!warning
Solange der Benutzer kein neues Passwort gesetzt hat, kann sich jeder, der den Benutzernamen kennt, bei einem Konto ohne Passwort anmelden. Auf einem Server solltest du das Passwort nur löschen, wenn sich der Benutzer sofort anmelden kann.
!!!

Der letzte Administrator kann das Administrator-Kennzeichen nicht verlieren: *Der letzte Administrator kann weder gelöscht noch herabgestuft werden.*

<a id="users-and-providers"></a>

## Benutzer und Anbieter

Zum Hinzufügen eines [Inferenz-Anbieters](../inference-providers.md) wird die Berechtigung **Eigene Modelle anlegen** benötigt; Administratoren haben sie immer. Ein Benutzer ohne diese Berechtigung kann nur die Anbieter nutzen, die Administratoren [freigegeben](../inference-providers.md#shared-providers) haben. Die Anbieter, die ein Benutzer bereits hat, funktionieren weiter und können weiterhin bearbeitet werden. Siehe [Berechtigungen](permissions.md).

<a id="permissions"></a>

## Berechtigungen

Die Schlüssel-Schaltfläche in der Zeile eines Benutzers öffnet dessen [Berechtigungen](permissions.md): was der Benutzer über das Übliche hinaus darf, etwa eigene Modelle anlegen oder den Hilfe-Assistenten nutzen. Ein neuer Benutzer hat keine Berechtigungen; gib ihm nach dem Anlegen, was er braucht.

<a id="deleting-a-user"></a>

## Benutzer löschen

Die Papierkorb-Schaltfläche löscht das Konto nach einer Bestätigung. Du kannst dein eigenes Konto oder den letzten Administrator nicht löschen.

Der Benutzer kann sich danach nicht mehr anmelden, gespeicherte Anmeldungen auf seinen Geräten funktionieren nicht mehr, und der Benutzername wird für ein neues Konto frei.

Seine Bücher, Lorebooks, Inferenz-Anbieter (mit ihren API-Schlüsseln), Protokolle, Tags und Einstellungen verschwinden zusammen mit dem Konto – niemand kann sie mehr sehen – und die nächste [Bereinigung](cleanup.md) entfernt sie zusammen mit dem Konto aus der Datenbank.

Die Dateien des Benutzers (hochgeladene Bilder und Ressourcen) bleiben erhalten: Sein Ordner im Datenverzeichnis wird in `<login>-deleted` umbenannt (`<login>-deleted-2` usw., falls dieser Name bereits belegt ist), sodass ein neues Konto mit demselben Benutzernamen mit einem leeren Ordner beginnt. Lösche den umbenannten Ordner von Hand, sobald du ihn nicht mehr brauchst.
