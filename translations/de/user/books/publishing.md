---
label: Veröffentlichung & Betrachter
order: 40
---

# Veröffentlichung & Betrachter

Der **Betrachter** ist eine Leseansicht Ihrer Bücher: nur der Text der Geschichte, ohne den Editor drumherum. Er funktioniert
gut auf Handys und Tablets. **Veröffentlichen** eines Buches erlaubt anderen Benutzern derselben Marginalia-Installation, es im
Betrachter zu lesen.

<a id="opening-the-viewer"></a>

## Den Betrachter öffnen

Der Betrachter befindet sich unter `/view` auf Ihrer Marginalia-Adresse, zum Beispiel:

- Desktop-App: `http://127.0.0.1:8765/view`
- Server: `https://marginalia.example.com/view`

Er verwendet dieselbe Anmeldung wie Marginalia.

![Die Buchliste des Betrachters](../../images/viewer-list.png)

Die Liste zeigt Ihre Bücher mit ihren Schlagwörtern und dem Zeitpunkt, zu dem Sie sie zuletzt geöffnet haben. Filtern Sie sie nach **Name** (Bücher, deren Name mit dem Text beginnt) und **Schlagwörtern** (Bücher mit allen ausgewählten Schlagwörtern). Sortieren Sie sie nach **Zuletzt geöffnet** oder **Name**; ein erneuter Klick auf die aktive Sortierschaltfläche kehrt die Reihenfolge um.

Klicken Sie auf ein Buch, um es zu lesen.

<a id="folders-in-the-list"></a>

### Ordner in der Liste

Die Liste zeigt Ihre [Ordner](folders.md) so, wie es die Telefonliste des Arbeitsbereichs tut: Zuerst kommen die Ordner, jeweils mit
der Anzahl der Bücher darunter, dann die Bücher des aktuellen Ordners. Klicken Sie auf einen Ordner, um hineinzugehen. Der Pfad über
den Filtern (`Books / Novels / Portal fantasy`) zeigt, wo Sie sich befinden; ein Klick auf einen Teil davon führt dorthin zurück.

![Der Betrachter innerhalb eines Ordners](../../images/viewer-folder.png)

- **Unterordner einbeziehen** listet jedes Buch unterhalb des aktuellen Ordners auf, in beliebiger Tiefe, mit dem Ordner jedes Buches unter dessen Namen.
- **Favoriten**, der Ordner der obersten Ebene, der existiert, solange Sie ein Lieblingsbuch haben, listet die Favoritenbücher aller
  Ordner auf. Favoriten werden in der [Liste der Bücher](folders.md#favorites) gesetzt, nicht im Betrachter.
- Die Filter wirken innerhalb des aktuellen Ordners (oder darunter, mit *Unterordner einbeziehen*). Um alle Ihre Bücher zu durchsuchen, gehen Sie daher auf die oberste Ebene und setzen Sie das Häkchen. **Zuletzt geöffnet** sortiert die Ordner nach ihrer letzten Änderung.
- Der Betrachter öffnet den Ordner, in dem Sie zuletzt waren: Nach dem Lesen eines Buches bringt Sie der Pfeil zurück in denselben Ordner, und beim nächsten Besuch oder nach einem Neuladen beginnt er dort ebenfalls. Die Stelle wird in einem Cookie Ihres Browsers gespeichert (`marginalia_viewer_folder`); der Server speichert sie nicht, daher beginnt ein anderer Browser auf der obersten Ebene. Ein gelöschter Ordner gilt als oberste Ebene.

<a id="reading"></a>

## Lesen

Die Leseansicht zeigt den [aktiven Zweig](branches-and-story-tree.md) des Buches vom ersten bis zum letzten Abschnitt,
im Lesestil für Bücher (Blocksatz, mit Silbentrennung in der [Sprache](managing-books.md#the-about-tab) des Buches). Andere Zweige, die Schreibanweisungen und die Metadaten werden nicht angezeigt. Die Pfeilschaltfläche in
der linken oberen Ecke führt zurück zur Liste.

Die runde Schaltfläche in der rechten oberen Ecke öffnet ein Menü der **Textstile**, in denen Sie lesen können: die Stile *Normal* und *Buch*
sowie die Stile, die Sie unter [Einstellungen → Stile](../account.md#styles) angelegt haben (der angehakte wird angezeigt; bis Sie wählen, gilt der Stil Buch).
Angeboten werden nur Ihre eigenen Stile, auch in einem veröffentlichten Buch eines anderen Benutzers: Was der Autor zum Schreiben verwendet, erreicht
den Leser nicht. Die Wahl wird in einem Cookie Ihres Browsers gespeichert (`marginalia_reader_style`), gilt daher für jedes Buch, das Sie dort
lesen, und der Server speichert sie nicht; ein Stil, der nicht Ihnen gehört (weil ein anderer Benutzer im selben Browser angemeldet war), wird ignoriert.

![Das Menü der Stile im Leser](../../images/viewer-style-menu.png)

![Ein Buch lesen](../../images/viewer-reading.png)

Was Sie lesen, ist das Buch in seinem aktuellen Stand: Neue Abschnitte erscheinen beim nächsten Öffnen der Seite.

Ein langes Buch wird in Teilen geladen: Die Abschnitte um Ihre Position werden angezeigt, die nächsten (und vorherigen) werden beim Scrollen ergänzt,
und die weit entfernten werden wieder verworfen; eine Markierung *Laden* am Rand erscheint, wenn Sie schneller scrollen, als sie eintreffen. Es werden nicht mehr als die Einstellung **Auf einmal geladene Abschnitte in der Geschichte** Ihres [Kontos](../account.md) gleichzeitig geladen;
leer lädt das ganze Buch auf einmal.

Der Betrachter merkt sich, wo Sie bei jedem Buch aufgehört haben: Beim nächsten Mal öffnet er das Buch an dieser Stelle. Die Position wird nur im lokalen Speicher Ihres Browsers gehalten (je Buch), sie wird nicht auf dem Server gespeichert; ein anderer Browser oder ein geleerter Browserspeicher beginnt daher am Anfang.

[Bilder der Abschnitte](story-editor.md#images) werden dort angezeigt, wo der Autor sie platziert hat (nach dem Text, sofern der Autor keine andere Stelle gewählt hat), Ihnen sowie den Lesern eines
[veröffentlichten](#publishing-a-book) Buches, es sei denn, Sie entfernen das Häkchen bei **Die Bilder den Lesern zeigen** auf dem Reiter *Info* des Buches (es ist standardmäßig gesetzt). Ohne Häkchen sehen die Leser den Text ohne die Bilder. Die Bilder werden aus Ihrem Konto nur gelesen, solange das Buch veröffentlicht ist, und nicht, wenn Sie die Veröffentlichung zurücknehmen.

<a id="publishing-a-book"></a>

## Ein Buch veröffentlichen

Standardmäßig können nur Sie Ihre Bücher im Betrachter öffnen. Damit andere Benutzer ein Buch lesen können:

1. Öffnen Sie das Buch und setzen Sie auf dem Reiter *Info* das Häkchen bei **Veröffentlicht**.
2. Kopieren Sie den Leselink, der unter dem Kontrollkästchen auf dem Reiter *Info* erscheint. Der Link ist die Adresse Ihrer
   Installation (einschließlich ihres Kontextpfads, falls Marginalia nicht unter der Wurzel bereitgestellt wird), gefolgt von `/view/` und der
   ID des Buches: `<address of Marginalia>/view/<book ID>`. In Docker, wo Marginalia als Root-Anwendung läuft, sieht er so aus: `https://marginalia.example.com/view/0b6f3c1e-...`; wird es unter einem Kontextpfad wie
   `/marginalia` bereitgestellt, lautet der Link `https://example.com/marginalia/view/0b6f3c1e-...`. (Sie können dieselbe Adresse auch aus der Adressleiste des Browsers kopieren, wenn das Buch im Betrachter geöffnet ist.)
3. Schicken Sie die Adresse an die anderen Benutzer.

Andere Benutzer brauchen ein Konto auf derselben Marginalia-Installation; sie melden sich an und sehen das Buch. Veröffentlichte Bücher
erscheinen nicht in ihrer Liste im Betrachter, nur die Adresse öffnet sie. Sie können das Buch nur lesen, nicht ändern.

Die Veröffentlichung teilt das Buch so, wie es ist: Nur der Text der Abschnitte wird geteilt, die Bilder der Abschnitte bleiben privat.

Entfernen Sie das Häkchen bei **Veröffentlicht**, um das Teilen zu beenden: Die Adresse zeigt dann allen außer Ihnen *Buch nicht gefunden* an. Gelöschte Bücher
können überhaupt nicht geöffnet werden.
