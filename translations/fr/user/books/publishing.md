---
label: Publication et visionneuse
order: 40
---

# Publication et visionneuse

La **visionneuse** est une vue de lecture de vos livres : uniquement le texte de l'histoire, sans l'éditeur autour. Elle fonctionne
bien sur les téléphones et les tablettes. **Publier** un livre permet aux autres utilisateurs de la même installation Marginalia de le lire
dans la visionneuse.

<a id="opening-the-viewer"></a>

## Ouvrir la visionneuse

La visionneuse se trouve à `/view` sur votre adresse Marginalia, par exemple :

- application de bureau : `http://127.0.0.1:8765/view`
- serveur : `https://marginalia.example.com/view`

Elle utilise le même identifiant que Marginalia.

![La liste des livres de la visionneuse](../../images/viewer-list.png)

La liste affiche vos livres, avec leurs mots-clés et la date de votre dernière ouverture. Filtrez-la par **Nom** (livres dont le nom
commence par le texte) et **Mots-clés** (livres ayant tous les mots-clés sélectionnés). Triez-la par **Dernière ouverture** ou **Nom** ; un clic
sur le bouton de tri actif inverse l'ordre.

Cliquez sur un livre pour le lire.

<a id="folders-in-the-list"></a>

### Dossiers dans la liste

La liste affiche vos [dossiers](folders.md) comme la liste du téléphone dans l'espace de travail : les dossiers apparaissent en premier, chacun avec
le nombre de livres qu'il contient, puis les livres du dossier courant. Cliquez sur un dossier pour y entrer. Le chemin au-dessus des
filtres (`Books / Novels / Portal fantasy`) indique où vous vous trouvez, et un clic sur une partie de ce chemin permet d'y revenir.

![La visionneuse dans un dossier](../../images/viewer-folder.png)

- **Inclure les sous-dossiers** affiche chaque livre situé sous le dossier courant, à n'importe quelle profondeur, avec le dossier de chacun inscrit sous son nom.
- **Favoris**, le dossier de premier niveau qui existe tant qu'un livre est en favori, affiche les livres favoris de tous
  les dossiers. Les favoris se définissent dans la [liste des livres](folders.md#favorites), et non dans la visionneuse.
- Les filtres s'appliquent dans le dossier courant (ou en dessous avec *Inclure les sous-dossiers*) ; pour chercher dans tous vos livres, revenez au
  premier niveau et cochez la case. **Dernière ouverture** trie les dossiers selon leur dernière modification.
- La visionneuse s'ouvre dans le dossier où vous l'avez laissée : après la lecture d'un livre, la flèche vous ramène au même dossier, et la
  visite suivante, ou un rechargement, commence aussi là. Cet emplacement est conservé dans un cookie de votre navigateur (`marginalia_viewer_folder`) ; le
  serveur ne le stocke pas, donc un autre navigateur commence au premier niveau. Un dossier supprimé correspond au premier niveau.

<a id="reading"></a>

## Lecture

La vue de lecture affiche la [branche active](branches-and-story-tree.md) du livre, de la première à la dernière partie,
dans le style de lecture de livre (justifié, avec césure selon la [langue](managing-books.md#the-about-tab) du livre). Les autres branches, les consignes de tour et les métadonnées ne sont pas affichées. Le bouton flèche en
haut à gauche revient à la liste.

Le bouton rond en haut à droite ouvre un menu de **styles de texte** pour lire : les styles *Normal* et *Livre*
ainsi que les styles que vous avez créés dans [Paramètres → Styles](../account.md#styles) (celui qui est coché est affiché ; le style Livre tant que vous n'en choisissez pas un).
Seuls vos propres styles sont proposés, y compris dans un livre publié d'un autre utilisateur : ce que l'auteur utilise pour écrire n'atteint pas
le lecteur. Le choix est conservé dans un cookie de votre navigateur (`marginalia_reader_style`), il vaut donc pour chaque livre que vous lisez
là-bas, et le serveur ne le stocke pas ; un style qui n'est pas le vôtre (un autre utilisateur connecté dans le même navigateur) est ignoré.

![Le menu des styles dans le lecteur](../../images/viewer-style-menu.png)

![Lecture d'un livre](../../images/viewer-reading.png)

Ce que vous lisez est le livre tel qu'il est à présent : les nouvelles parties apparaissent à la prochaine ouverture de la page.

Un long livre est lu par morceaux : les parties autour de l'endroit où vous êtes sont affichées, la suivante (et la précédente) s'ajoutent
au fil du défilement, et celles qui sont loin sont retirées ; une mention *Chargement* apparaît au bord si vous faites défiler plus vite
qu'elles n'arrivent. Pas plus que le réglage **Parties chargées à la fois dans l'histoire** de [votre compte](../account.md) ne sont chargées à la fois ;
vide charge le livre entier d'un coup.

La visionneuse retient où vous vous êtes arrêté dans chaque livre : elle ouvre le livre à cette partie la fois suivante. La position est
conservée uniquement dans le stockage local de votre navigateur (par livre) ; elle n'est pas enregistrée sur le serveur, donc un autre navigateur ou un stockage
du navigateur vidé repart du début.

Les [images des parties](story-editor.md#images) sont affichées à l'endroit où l'auteur les a placées (après le texte, sauf si l'auteur a choisi un autre emplacement), pour vous, et pour les lecteurs d'un
livre [publié](#publishing-a-book), sauf si vous décochez **Montrer les images aux lecteurs** dans l'onglet *À propos* du livre (la case est cochée par défaut). Lorsqu'elle est décochée, les lecteurs voient le texte sans les images. Les images ne sont lues depuis votre compte que tant que le livre est publié, et non lorsque vous le dépubliez.

<a id="publishing-a-book"></a>

## Publier un livre

Par défaut, vous seul pouvez ouvrir vos livres dans la visionneuse. Pour permettre à d'autres utilisateurs de lire un livre :

1. Ouvrez le livre et cochez **Publié** dans son onglet *À propos*.
2. Copiez le lien de lecture qui apparaît sous la case dans l'onglet *À propos*. Le lien est l'adresse de votre installation (y compris son chemin de contexte, si Marginalia n'est pas servi à la racine), suivie de `/view/` et de
   l'identifiant du livre : `<address of Marginalia>/view/<book ID>`. Avec Docker, où Marginalia fonctionne comme application racine, il
   ressemble à `https://marginalia.example.com/view/0b6f3c1e-...` ; s'il est déployé sous un chemin de contexte tel que
   `/marginalia`, le lien est `https://example.com/marginalia/view/0b6f3c1e-...`. (Vous pouvez aussi copier cette même adresse
   depuis la barre d'adresse du navigateur lorsque le livre est ouvert dans la visionneuse.)
3. Envoyez l'adresse aux autres utilisateurs.

Les autres utilisateurs ont besoin d'un compte sur la même installation Marginalia ; ils se connectent et voient le livre. Les livres publiés
n'apparaissent pas dans leur liste de la visionneuse, seule l'adresse permet de les ouvrir. Ils peuvent seulement lire le livre, pas le modifier.

Publier partage le livre tel qu'il est : seul le texte des parties est partagé, les images des parties restent privées.

Décochez **Publié** pour arrêter le partage : l'adresse affiche alors *Livre introuvable* à tout le monde sauf à vous. Les livres supprimés
ne peuvent pas du tout être ouverts.
