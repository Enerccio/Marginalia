---
label: Guide de style
order: 54
---

# Guide de style : donnez à votre livre l'allure qui lui correspond

Un **style de texte** détermine l'apparence du texte de vos parties : la police, les couleurs, la largeur de la colonne, les titres, le
trait entre les scènes. Ce guide vous mène de la première modification à un rendu abouti, et se termine par un exemple complet à télécharger : un style
pour un livre d'horreur qui utilise toutes les options et une part de CSS personnel.

Où un style est utilisé :

| Où | Ce qui se passe |
|---|---|
| **L'éditeur d'histoire** | Le style du livre (ou votre style par défaut) donne l'apparence de chaque partie. Voir [Styles de texte](story-editor.md#text-styles). |
| **La lecture** | Un [livre publié](publishing.md) est lu dans le style que le *lecteur* choisit parmi ses propres styles (le style du livre tant qu'il n'en choisit pas). |
| **Exports** | Un style marqué **Pour les exports** est choisi dans la [boîte de dialogue d'export](export.md#print-styles). HTML et EPUB prennent la feuille de style, PDF et Word prennent les valeurs numériques (voir [ce que prend chaque export](#what-the-exports-take)). |

<a id="make-your-first-style"></a>

## Créer votre premier style

1. Ouvrez **Paramètres → Styles** (l'[onglet Styles](../account.md#styles)).
2. Sélectionnez **Book** dans la liste et appuyez sur **Dupliquer**. Les styles intégrés ne peuvent pas être modifiés : vous partez donc toujours d'une copie nommée
   *Book (copy)*. Donnez-lui un nom.
3. Modifiez quelques cases dans l'onglet **Options**. L'**Aperçu** sous le formulaire affiche un texte d'exemple (un titre de chapitre, de l'emphase, une citation, un changement de scène,
   un titre plus petit) et suit chaque modification au fil de la frappe.
4. Appuyez sur **Enregistrer** dans les paramètres. Rien n'est conservé avant cela, et **Abandonner les modifications** efface le travail en cours.
5. Dans l'éditeur d'histoire, ouvrez le menu des paramètres (⚙) → **Apparence** → **Changer les styles du texte** et cochez votre style. Il est enregistré dans le
   livre. Pour utiliser ce style pour tous vos livres, sélectionnez-le dans la liste et appuyez sur **Définir par défaut**.

![Les styles dans le menu de l'histoire](../../images/story-style-menu.png)

Une case laissée vide conserve l'apparence de l'application : un style ne précise donc que ce que vous voulez modifier. Un style dont toutes les cases sont vides donne l'apparence neutre.

<a id="the-options"></a>

## Les options

Chaque case accepte une valeur CSS. Unités que vous utiliserez : `rem` (la taille du texte de la page, 1rem fait normalement 16px), `em` (la taille du
texte courant), `px`, `%`, et les couleurs sous la forme `#336699`, `rgb(51, 102, 153)` ou un nom comme `ivory`.

<a id="text"></a>

### Texte

| Option | Ce qu'elle fait | Exemple |
|---|---|---|
| **Famille de polices** | La police du texte. Donnez une liste : la première que l'ordinateur possède est utilisée, la dernière devrait être une famille générique (`serif`, `sans-serif`, `monospace`). | `"Garamond", "Georgia", serif` |
| **Taille de police** | La taille du texte. | `1.1rem` |
| **Hauteur de ligne** | La distance entre les lignes. Un simple nombre est un multiple de la taille de police. | `1.7` |
| **Espacement des lettres**, **Espacement des mots** | Espace supplémentaire entre les lettres, entre les mots. Petites valeurs : un léger espacement des lettres aère le texte. | `0.01em`, `0.05em` |
| **Couleur du texte** | La couleur du texte. | `#cfc6bb` |
| **Alignement du texte** | `left`, `right`, `center` ou `justify`. | `justify` |
| **Césure** | Coupe les mots longs en fin de ligne. Elle utilise le dictionnaire de la [langue](managing-books.md#the-about-tab) du livre, donc réglez d'abord la langue. Fonctionne mieux avec *justifié*. | activée |
| **Espacement des paragraphes** | L'espace sous un paragraphe. Un livre en a généralement peu, voire aucun lorsqu'il y a un retrait de première ligne. | `0.3em` |
| **Retrait de première ligne** | Décale la première ligne d'un paragraphe, comme dans un livre imprimé, sauf après un titre ou un changement de scène. | `1.5em` |
| **Lettrine** | Une grande première lettre pour le premier paragraphe qui suit un titre. | activée |

### Page

| Option | Ce qu'elle fait | Exemple |
|---|---|---|
| **Largeur du texte** | Une colonne de sa propre largeur, centrée. Une ligne de 60 à 75 caractères se lit le mieux, soit environ `34em` à `40em`. | `38em` |
| **Couleur de fond** | La couleur derrière le texte ; elle remplit toute la zone, et pas seulement les blocs de texte. Choisissez une couleur de texte qui se lit sur ce fond. | `#0c0a0a` |

<a id="emphasis-links-and-code"></a>

### Emphase, liens et code

| Option | Ce qu'elle fait |
|---|---|
| **Couleur d'emphase** | La couleur du texte en *italique*. |
| **Couleur du gras** | La couleur du texte en **gras**. |
| **Couleur des liens** | La couleur des liens. |
| **Famille de polices du code** | La police de `code` et des blocs de code. |

<a id="quotes"></a>

### Citations

| Option | Ce qu'elle fait |
|---|---|
| **Couleur de citation** | La couleur du texte d'une citation (`> a line`). |
| **Couleur de la bordure de citation** | La couleur de la barre sur le côté d'une citation. |

<a id="headings"></a>

### Titres

| Option | Ce qu'elle fait |
|---|---|
| **Famille de polices des titres** | La police des titres `#`, `##` et `###`. |
| **Taille des titres** | La taille du premier niveau ; les niveaux inférieurs sont plus petits proportionnellement. |
| **Couleur des titres** | La couleur des titres. |
| **Alignement des titres** | `left`, `center` ou `right`. |
| **Petites capitales des titres** | Écrit les titres en petites capitales. |

<a id="scene-break"></a>

### Changement de scène

Une ligne contenant `---` dans le texte marque un changement de scène. Par défaut, ce sont trois points. **Texte du changement de scène** est ce qui s'affiche à la place
(`* * *`, `~`, `† † †`), **Couleur du changement de scène** en est la couleur.

<a id="custom-css"></a>

## CSS personnalisé

Les options couvrent les besoins courants. Pour le reste, l'onglet **CSS personnalisé** accepte votre propre CSS, ajouté après tout le reste et appliqué uniquement
à l'intérieur du style. Deux façons de l'écrire :

- **Déclarations simples** s'appliquent à toute la zone qui contient le texte :

  ```css
  box-shadow: inset 0 0 120px rgba(0, 0, 0, 0.8);
  ```

- **Règles** avec un sélecteur. Écrivez-les avec `&` (la zone du style) devant, ou sans lui : dans les deux cas, elles n'atteignent que le texte qui utilise ce style.

  ```css
  & .chat-message-markdown h1 { text-shadow: 0 0 12px #8e1b17; }
  & .chat-message-markdown a:hover { color: #b3261e; }
  ```

Le texte d'une partie se trouve dans un élément de classe `chat-message-markdown` ; un sélecteur pour un élément du texte commence donc là. Le
champ **CSS généré** sous la zone CSS affiche la feuille de style produite par les options : copiez ses sélecteurs pour voir ce que vous pouvez remplacer.

| Vous voulez modifier | Sélecteur (après `& .chat-message-markdown`) |
|---|---|
| les paragraphes | `p` |
| les titres | `h1`, `h2`, `h3` |
| l'italique, le gras | `em`, `strong` |
| les liens | `a`, `a:hover` |
| les citations | `blockquote` |
| les listes | `ul`, `ol`, `li` |
| le code | `code` (en ligne), `pre` (bloc) |
| le changement de scène | `hr`, `hr::after` (le symbole) |
| la première ligne d'un paragraphe | `p::first-line` |
| le texte sélectionné | `::selection` |

À savoir :

- **Les accolades `{ }` doivent être équilibrées**, sinon le style est refusé à l'enregistrement. Les commentaires `/* ... */` sont acceptés.
- Quelques règles nécessaires à l'application sont `!important` (le texte ne dépasse jamais sa zone, les mots longs passent à la ligne, les paragraphes conservent leurs sauts de ligne). Votre CSS ne peut pas les remplacer ; il peut remplacer tout le reste, car il vient en dernier.
- Le CSS modifie l'apparence, pas le contenu. Il n'atteint pas les barres d'outils et les onglets autour du texte.
- Le CSS personnalisé est utilisé dans l'histoire, la lecture et les exports HTML. EPUB, PDF et Word ne le prennent pas en compte.

<a id="what-the-exports-take"></a>

## Ce que prennent les exports

Un style pour les exports se choisit dans la [boîte de dialogue d'export](export.md#print-styles) ; **Dupliquer** un style et cochez **Pour les exports**, ou partez de l'un des quatre
fournis avec l'application. Les couleurs qui dépendent du thème de l'application (le texte clair du thème sombre) deviennent sombres pour une
page blanche lors de l'export.

| | HTML | EPUB | PDF | Word |
|---|---|---|---|---|
| polices, tailles, hauteur de ligne, espacements, alignement, retrait de première ligne | ✓ | ✓ | ✓ | ✓ |
| couleurs du texte, de l'emphase, du gras, des citations, des titres, du changement de scène | ✓ | ✓ | ✓ | ✓ |
| couleur de fond | ✓ | ✓ | ✓ | ✓ (la couleur de la page) |
| largeur du texte, lettrines, césure, petites capitales des titres | ✓ | ✓ | | |
| CSS personnalisé | ✓ | | | |

Les liseuses électroniques utilisent souvent leurs propres polices, tailles et couleurs, quoi que dise la feuille de style. PDF et Word ne peuvent utiliser que les polices que le format gère : le PDF choisit l'empattée, la sans empattement ou la chasse fixe parmi la liste que vous avez écrite ; Word retient la première police de
la liste et affiche la sienne si l'ordinateur ne possède pas celle-ci.
<a id="share-a-style-export-and-import"></a>

## Partager un style : exporter et importer

Sous la liste des styles, **Exporter** enregistre le style sélectionné dans un fichier (`name.style.json` ; un style intégré peut aussi être exporté) et **Importer**
lit un tel fichier. Un style importé est ajouté à vos propres styles comme nouveau style ; il n'est pas conservé avant que vous appuyiez sur **Enregistrer**, et il garde
son nom, donc renommez-le si vous en avez déjà un semblable.

Le fichier est au format JSON :

```json
{
  "format": "marginalia-text-style",
  "version": 1,
  "styles": [
    {
      "name": "My style",
      "forExport": false,
      "options": { "FONT_SIZE": "1.1rem", "TEXT_COLOR": "#222222" },
      "customCss": "& .chat-message-markdown h1 { letter-spacing: 0.1em; }"
    }
  ]
}
```

Il peut contenir plusieurs styles, et vous pouvez l'écrire à la main. Les options sont nommées en capitales (`FONT_SIZE`, `TEXT_COLOR`, `SCENE_BREAK_TEXT`...) ; le [exemple horreur](#an-example-a-horror-book) les utilise toutes. Une option que cette version de Marginalia
ne connaît pas est ignorée (on vous indique combien), un fichier provenant d'une version plus récente du format est refusé, et il en va de même pour un style dont le CSS personnalisé
a des accolades qui ne correspondent pas.

<a id="an-example-a-horror-book"></a>

## Un exemple : un livre d'horreur

[Télécharger le style horreur](../../files/horror-book.style.json) (`horror-book.style.json`), importez-le dans *Paramètres → Styles* et sélectionnez-le
pour le voir dans l'aperçu. Il définit **toutes les options** et utilise du CSS personnalisé pour ce que les options ne couvrent pas :

| | |
|---|---|
| **Page** | Fond presque noir `#0c0a0a`, une colonne de `38em`, et une vignette sombre qui se resserre sur les bords (CSS personnalisé). |
| **Texte** | Garamond en `1.12rem`, interligne `1.8`, justifié avec césure, un retrait de première ligne de `1.6em` et presque pas d'espace entre les paragraphes, comme un roman imprimé. Le texte est le gris du vieux papier `#cfc6bb`, avec une lettrine après chaque titre. |
| **Emphase** | Italiques en rose poussiéreux `#b99a94`, gras en rouge sang `#b3261e`. |
| **Liens** | Vert maladif `#8fa07e` qui devient rouge et ondulé quand le pointeur est dessus (CSS personnalisé). |
| **Citations** | Texte gris avec une barre rouge foncé `#5c1010` et une faible tache rouge en arrière-plan (CSS personnalisé). |
| **Titres** | Palatino en petites capitales, centré, rouge foncé `#8e1b17`, avec une lueur discrète (CSS personnalisé). |
| **Changement de scène** | `† † †` en rouge foncé ; les listes reçoivent une croix au lieu d'un point (CSS personnalisé). |
| **Code** | Courier New, sur un fond presque noir. |

![Une histoire dans le style horreur](../../images/story-style-horror.png)

Pour l'essayer sur un vrai texte, collez ceci dans une partie (ou l'aperçu affiche un échantillon similaire) :

```markdown
# Chapter One: The House on Harrow Lane

The rain had been falling for *three days* when Mara found the door. It was not on the plan of the house, and it was **warm** to the touch.

> Do not open it after dark. — scratched into the plaster, in a hand that was not hers

She counted the steps down. Thirteen. At the bottom there were:

- a lantern, still burning;
- a chair, turned to face the wall;
- her own name, written in the dust.

---

By morning the lantern was out, and the [house records](https://example.com) said nobody had lived there for forty years.
```

Puis modifiez-le : rendez le fond `#14100c` pour un brun de bougie, réglez **Largeur du texte** sur `32em` pour une page plus étroite, ou supprimez le CSS personnalisé pour voir
ce que les options seules donnent. Quand le résultat vous plaît, **Exporter** le style et gardez le fichier avec votre livre.
