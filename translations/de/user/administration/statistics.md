---
label: Statistik
order: 87
---

# Statistik

Die [Schreibstatistik](../books/statistics.md) jedes Benutzers (die Wörter, die aufgewendete Zeit, die Größe jedes Buchs, Tag für Tag)
wird in der Datenbank gespeichert. Sie hält fest, wann die Benutzer an ihren Büchern gearbeitet haben, sowie die Namen und Ordner der
Bücher, auch die gelöschter Bücher. Der Reiter **Statistik** der Admin-Seite bietet dir die Möglichkeit, dies einzuschränken. Benutzer
können dasselbe für sich selbst in ihren [Einstellungen](../account.md) tun und ihre eigene Statistik in den Statistikfenstern löschen.

![Der Reiter Statistik der Admin-Seite](../../images/admin-statistics.png)

| | |
|---|---|
| **Schreibstatistik erfassen** | Aus beendet die Erfassung für alle Benutzer, unabhängig davon, was sie gewählt haben. Was bereits erfasst wurde, wird dadurch nicht gelöscht. Die Statistikfenster weisen darauf hin, dass nichts Neues gezählt wird. |
| **Statistik aufbewahren für (Monate)** | Tage, die älter sind als dieser Zeitraum, werden aus der Statistik aller Benutzer endgültig gelöscht, einmal täglich (gegen 4:20 Uhr und kurz nach dem Start) sowie beim Speichern. Leer oder 0 behält alles. Ein Benutzer kann für sich selbst einen kürzeren Zeitraum wählen, niemals einen längeren. |
| **Die Statistik des Benutzers löschen** | Löscht nach einer Rückfrage alle Statistiken des gewählten Benutzers. Seine Bücher werden nicht verändert. |
| **Die Statistik aller löschen** | Dasselbe für alle Benutzer. |

Gelöschte Statistiken lassen sich nicht wiederherstellen, außer aus einer [Datenbanksicherung](database-backups.md): Die Statistiken sind
Teil der Datenbank (die Sicherung eines einzelnen Buchs enthält sie nicht).

Die [Bereinigung](cleanup.md) rührt die Statistiken nicht an, denn sie sind keine gelöschten Objekte: Sie verweisen auf das Buch über
dessen ID und überdauern es absichtlich.
