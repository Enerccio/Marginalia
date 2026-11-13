---
label: Veröffentlichung & Betrachter
order: 40
---

# Veröffentlichung & Betrachter

Der **Betrachter** ist eine Leseansicht Ihrer Bücher: nur der Text der Geschichte, ohne den Editor drumherum. Er funktioniert
gut auf Handys und Tablets. **Veröffentlichen** eines Buchs ermöglicht anderen Benutzern derselben Marginalia-Installation,
es im Betrachter zu lesen.

<a id="opening-the-viewer"></a>

## Den Betrachter öffnen

Der Betrachter liegt unter `/view` auf Ihrer Marginalia-Adresse, zum Beispiel:

- Desktop-App: `http://127.0.0.1:8765/view`
- Server: `https://marginalia.example.com/view`

Er verwendet dieselbe Anmeldung wie Marginalia.

![Die Buchliste des Betrachters](../../images/viewer-list.png)

Die Liste zeigt Ihre Bücher mit ihren Schlagwörtern und dem Zeitpunkt, zu dem Sie sie zuletzt geöffnet haben. Filtern Sie sie
nach **Name** (Bücher, deren Name mit dem Text beginnt) und **Schlagwörter** (Bücher mit allen ausgewählten Schlagwörtern).
Sortieren Sie sie nach **Zuletzt geöffnet** oder **Name**; ein erneuter Klick auf die aktive Sortierschaltfläche kehrt die
Reihenfolge um.

Klicken Sie auf ein Buch, um es zu lesen.

<a id="folders-in-the-list"></a>

### Ordner in der Liste

Die Liste zeigt Ihre [Ordner](folders.md) so, wie die Telefonliste des Arbeitsbereichs es tut: zuerst die Ordner, jeweils mit
der Anzahl der Bücher darunter, dann die Bücher des aktuellen Ordners. Klicken Sie auf einen Ordner, um hineinzugehen. Der Pfad
über den Filtern (`Bücher / Romane / Portal-Fantasy`) zeigt, wo Sie sich befinden, und ein Klick auf einen Teil davon führt
dorthin zurück.

![Der Betrachter innerhalb eines Ordners](../../images/viewer-folder.png)

- **Unterordner einbeziehen** listet jedes Buch unterhalb des aktuellen Ordners auf, unabhängig von der Tiefe, mit dem Ordner
  jedes Buchs unter dessen Namen.
- **Favoriten**, der Ordner der obersten Ebene, der existiert, solange Sie ein Favoriten-Buch haben, listet die Favoriten-Bücher
  aller Ordner. Favoriten werden in der [Liste der Bücher](folders.md#favorites) gesetzt, nicht im Betrachter.
- Die Filter arbeiten innerhalb des aktuellen Ordners (oder darunter mit *Unterordner einbeziehen*). Um alle Ihre Bücher zu
  durchsuchen, gehen Sie also auf die oberste Ebene und setzen Sie das Häkchen. **Zuletzt geöffnet** sortiert die Ordner nach
  ihrer letzten Änderung.
- Der Betrachter öffnet sich in dem Ordner, in dem Sie zuletzt waren: Nach dem Lesen eines Buchs bringt Sie der Pfeil zurück in
  denselben Ordner, und beim nächsten Besuch oder nach einem Neuladen beginnt es dort ebenfalls. Der Ort wird in einem Cookie Ihres
  Browsers gespeichert (`marginalia_viewer_folder`); der Server speichert ihn nicht, daher beginnt ein anderer Browser auf der
  obersten Ebene. Ein gelöschter Ordner gilt als oberste Ebene.

<a id="reading"></a>

## Lesen

Die Leseansicht zeigt den [aktiven Zweig](branches-and-story-tree.md) des Buchs vom ersten bis zum letzten Abschnitt, im
Lesesatz des Buchs (Blocksatz, mit Silbentrennung für die [Sprache](managing-books.md#the-about-tab) des Buchs). Andere Zweige,
die Wendungsanweisungen und die Metadaten werden nicht angezeigt. Die Pfeilschaltfläche in der oberen linken Ecke kehrt zur
Liste zurück.

Die runde Schaltfläche in der oberen rechten Ecke öffnet ein Menü mit **Textstilen** zum Lesen: die Stile *Normal* und *Buch*
sowie die Stile, die Sie unter [Einstellungen → Stile](../account.md#styles) angelegt haben (der angekreuzte wird angezeigt; bis
Sie etwas wählen, gilt der Stil Buch). Angeboten werden nur Ihre eigenen Stile, auch in einem veröffentlichten Buch eines anderen
Benutzers: Was der Autor zum Schreiben verwendet, erreicht den Leser nicht. Die Auswahl wird in einem Cookie Ihres Browsers
gespeichert, daher gilt sie für jedes Buch, das Sie dort lesen, und der Server speichert sie nicht; ein Stil, der nicht Ihnen
gehört (weil ein anderer Benutzer im selben Browser angemeldet ist), wird ignoriert.

![Das Menü der Stile im Leser](../../images/viewer-style-menu.png)

![Ein Buch lesen](../../images/viewer-reading.png)

Gelesen wird das Buch so, wie es jetzt ist: Neue Abschnitte erscheinen beim nächsten Öffnen der Seite.

Ein langes Buch wird in Teilen gelesen: Die Abschnitte rund um Ihre Stelle werden angezeigt, die nächsten (und vorherigen)
werden beim Scrollen ergänzt, und die weit entfernten werden wieder verworfen. Eine Markierung *Lädt* am Rand erscheint, wenn
Sie schneller scrollen, als sie eintreffen. Es werden nicht mehr als die Einstellung **Auf einmal geladene Abschnitte in der
Geschichte** Ihres [Kontos](../account.md) auf einmal geladen; ist sie leer, wird das ganze Buch auf einmal geladen.

Der Betrachter merkt sich, wo Sie jedes Buch zuletzt angehalten haben: Beim nächsten Öffnen beginnt das Buch an dieser Stelle.
Die Position wird nur im lokalen Speicher Ihres Browsers gehalten (je Buch), sie wird nicht auf dem Server gespeichert; ein
anderer Browser oder ein geleerter Browserspeicher beginnt daher am Anfang.

[Bilder der Abschnitte](story-editor.md#images) werden dort angezeigt, wo der Autor sie platziert hat (nach dem Text, sofern der
Autor nicht einen anderen Ort gewählt hat), Ihnen sowie den Lesern eines [veröffentlichten](#publishing-a-book) Buchs, es sei
denn, Sie entfernen das Häkchen bei **Die Bilder den Lesern zeigen** auf dem Reiter *Info* des Buchs (standardmäßig ist es gesetzt).
Ohne Häkchen sehen die Leser den Text ohne die Bilder. Die Bilder werden nur aus Ihrem Konto gelesen, solange das Buch
veröffentlicht ist, und nicht mehr, wenn Sie die Veröffentlichung zurücknehmen.

<a id="publishing-a-book"></a>

## Ein Buch veröffentlichen

Standardmäßig können nur Sie Ihre Bücher im Betrachter öffnen. Damit andere Benutzer ein Buch lesen können:

1. Öffnen Sie das Buch und setzen Sie auf dem Reiter *Info* das Häkchen bei **Veröffentlicht**.
2. Kopieren Sie den Leselink, der unter dem Kontrollkästchen auf dem Reiter *Info* erscheint. Der Link ist die Adresse Ihrer
   Installation (einschließlich ihres Kontextpfads, falls Marginalia nicht über die Wurzel bereitgestellt wird), gefolgt von
   `/view/` und der ID des Buchs: `<Adresse von Marginalia>/view/<Buch-ID>`. In Docker, wo Marginalia als Root-Anwendung läuft,
   sieht er so aus: `https://marginalia.example.com/view/0b6f3c1e-...`; wird es unter einem Kontextpfad wie `/marginalia`
   bereitgestellt, lautet der Link `https://example.com/marginalia/view/0b6f3c1e-...`. (Dieselbe Adresse können Sie auch aus
   der Adressleiste des Browsers kopieren, wenn das Buch im Betrachter geöffnet ist.)
3. Senden Sie die Adresse an die anderen Benutzer.

Andere Benutzer brauchen ein Konto auf derselben Marginalia-Installation; sie melden sich an und sehen das Buch. Veröffentlichte
Bücher erscheinen nicht in ihrer Liste im Betrachter, nur die Adresse öffnet sie. Sie können das Buch nur lesen, nicht ändern.

Die Veröffentlichung teilt das Buch so, wie es ist: Geteilt wird nur der Text der Abschnitte, die Bilder der Abschnitte bleiben
privat.

Entfernen Sie das Häkchen bei **Veröffentlicht**, um das Teilen zu beenden: Die Adresse zeigt dann allen außer Ihnen *Buch nicht
gefunden*. Gelöschte Bücher können überhaupt nicht geöffnet werden.
