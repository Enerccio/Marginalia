---
label: Guide du style
order: 54
---

# Guide du style : donner à votre livre son apparence

Un **style de texte** détermine l'apparence du texte de vos parties : la police, les couleurs, la largeur de la colonne, les titres, le trait entre les scènes. Ce guide vous mène de la première modification à une apparence achevée, et se termine par un exemple complet à télécharger : un style pour un livre d'horreur qui utilise toutes les options et une part de CSS personnalisé.

Où un style est utilisé :

| Où | Ce qui se passe |
|---|---|
| **L'éditeur de l'histoire** | Le style du livre (ou votre style par défaut) donne l'apparence de chaque partie. Voir [Styles de texte](story-editor.md#text-styles). |
| **Le lecteur** | Un [livre publié](publishing.md) est lu dans le style que le *lecteur* a choisi parmi ses propres styles (le style du livre tant qu'il n'en choisit pas). |
| **Exports** | Un style marqué **Pour les exports** est choisi dans la [boîte de dialogue d'export](export.md#print-styles). HTML et EPUB prennent la feuille de style, PDF et Word prennent les valeurs numériques (voir [ce que prend chaque export](#what-the-exports-take)). |

<a id="make-your-first-style"></a>

## Créer votre premier style

1. Ouvrez **Paramètres → Styles** (l'[onglet Styles](../account.md#styles)).
2. Sélectionnez **Book** dans la liste et appuyez sur **Dupliquer**. Les styles intégrés ne peuvent pas être modifiés : vous partez donc toujours d'une copie nommée *Book (copy)*. Donnez-lui un nom.
3. Modifiez quelques cases de l'onglet **Options**. L'**Aperçu** sous le formulaire affiche un texte d'exemple (un titre de chapitre, de l'emphase, une citation, un changement de scène, un titre plus petit) et suit chaque modification au fil de la saisie.
4. Appuyez sur **Enregistrer** dans les paramètres. Rien n'est conservé avant cela, et **Abandonner les modifications** efface le travail.
5. Dans l'éditeur de l'histoire, ouvrez le menu des paramètres (⚙) → **Apparence** → **Changer les styles du texte** et cochez votre style. Il est enregistré dans le livre. Pour utiliser le style dans tous vos livres, sélectionnez-le dans la liste et appuyez sur **Définir par défaut**.

![Les styles dans le menu de l'histoire](../../images/story-style-menu.png)

Une case laissée vide conserve l'apparence de l'application ; un style ne décrit donc que ce que vous voulez changer. Un style dont toutes les cases sont vides donne l'apparence simple.

<a id="the-options"></a>

## Les options

Chaque case accepte une valeur CSS. Unités que vous utiliserez : `rem` (la taille du texte de la page ; 1rem vaut normalement 16px), `em` (la taille du texte courant), `px`, `%`, et les couleurs sous la forme `#336699`, `rgb(51, 102, 153)` ou un nom comme `ivory`.

<a id="text"></a>

### Texte

| Option | Ce qu'elle fait | Exemple |
|---|---|---|
| **Font family** | La police du texte. Donnez une liste : la première que l'ordinateur possède est utilisée ; la dernière devrait être une famille générique (`serif`, `sans-serif`, `monospace`). | `"Garamond", "Georgia", serif` |
| **Font size** | La taille du texte. | `1.1rem` |
| **Line height** | La distance entre les lignes. Un simple nombre est un multiple de la taille de police. | `1.7` |
| **Letter spacing**, **Word spacing** | Espace supplémentaire entre les lettres, entre les mots. Petites valeurs : un très léger espacement des lettres aère le texte. | `0.01em`, `0.05em` |
| **Text color** | La couleur du texte. | `#cfc6bb` |
| **Text align** | `left`, `right`, `center` ou `justify`. | `justify` |
| **Hyphenation** | Coupe les mots longs en fin de ligne. Elle utilise le dictionnaire de la [langue](managing-books.md#the-about-tab) du livre ; réglez donc d'abord la langue. Convient surtout avec *justify*. | on |
| **Paragraph spacing** | L'espace sous un paragraphe. Un livre en a généralement peu, voire aucun lorsqu'il y a un retrait de première ligne. | `0.3em` |
| **First line indent** | Renfonce la première ligne d'un paragraphe, comme dans un livre imprimé, sauf après un titre ou un changement de scène. | `1.5em` |
| **Drop cap** | Une grande première lettre pour le premier paragraphe qui suit un titre. | on |

### Page

| Option | Ce qu'elle fait | Exemple |
|---|---|---|
| **Text width** | Une colonne de sa propre largeur, centrée. Une ligne de 60 à 75 caractères se lit le mieux, soit environ `34em` à `40em`. | `38em` |
| **Background color** | La couleur derrière le texte ; elle remplit toute la zone, pas seulement les blocs de texte. Choisissez une couleur de texte lisible sur ce fond. | `#0c0a0a` |

<a id="emphasis-links-and-code"></a>

### Emphase, liens et code

| Option | Ce qu'elle fait |
|---|---|
| **Emphasis color** | La couleur du texte en *italique*. |
| **Strong color** | La couleur du texte en **gras**. |
| **Link color** | La couleur des liens. |
| **Code font family** | La police du `code` et des blocs de code. |

<a id="quotes"></a>

### Citations

| Option | Ce qu'elle fait |
|---|---|
| **Quote color** | La couleur du texte d'une citation (`> une ligne`). |
| **Quote border color** | La couleur de la barre sur le côté d'une citation. |

<a id="headings"></a>

### Titres

| Option | Ce qu'elle fait |
|---|---|
| **Heading font family** | La police des titres `#`, `##` et `###`. |
| **Heading size** | La taille du premier niveau ; les niveaux inférieurs sont plus petits proportionnellement. |
| **Heading color** | La couleur des titres. |
| **Heading align** | `left`, `center` ou `right`. |
| **Heading small caps** | Écrit les titres en petites capitales. |

<a id="scene-break"></a>

### Changement de scène

Une ligne contenant `---` dans le texte est une coupure entre les scènes. Par défaut, il s'agit de trois points. **Scene break text** est ce qui s'affiche à la place (`* * *`, `~`, `† † †`), **Scene break color** sa couleur.

<a id="custom-css"></a>

## CSS personnalisé

Les options couvrent les besoins courants. Pour le reste, l'onglet **Custom CSS** accepte votre propre CSS, ajouté après tout le reste et appliqué uniquement à l'intérieur du style. Deux manières de l'écrire :

- **Déclarations simples** s'appliquent à toute la zone qui contient le texte :

  ```css
  box-shadow: inset 0 0 120px rgba(0, 0, 0, 0.8);
  ```

- **Règles** avec un sélecteur. Écrivez-les avec `&` (la zone du style) devant, ou sans lui : dans les deux cas, elles n'atteignent que le texte qui a ce style.

  ```css
  & .chat-message-markdown h1 { text-shadow: 0 0 12px #8e1b17; }
  & .chat-message-markdown a:hover { color: #b3261e; }
  ```

Le texte d'une partie se trouve dans un élément de classe `chat-message-markdown` ; un sélecteur pour un élément du texte commence donc par là. Le champ **Generated CSS** sous la zone CSS affiche la feuille de style produite par les options : copiez ses sélecteurs pour voir ce que vous pouvez remplacer.

| Vous voulez modifier | Sélecteur (après `& .chat-message-markdown`) |
|---|---|
| paragraphes | `p` |
| titres | `h1`, `h2`, `h3` |
| italique, gras | `em`, `strong` |
| liens | `a`, `a:hover` |
| citations | `blockquote` |
| listes | `ul`, `ol`, `li` |
| code | `code` (en ligne), `pre` (bloc) |
| changement de scène | `hr`, `hr::after` (le symbole) |
| la première ligne d'un paragraphe | `p::first-line` |
| texte sélectionné | `::selection` |

À savoir :

- **Les accolades `{ }` doivent correspondre**, sinon le style est refusé à l'enregistrement. Les commentaires `/* ... */` sont acceptés.
- Quelques règles nécessaires à l'application sont en `!important` (le texte ne dépasse jamais sa zone, les mots longs passent à la ligne, les paragraphes gardent leurs sauts de ligne). Votre CSS ne peut pas les remplacer ; il peut remplacer tout le reste, parce qu'il vient en dernier.
- Le CSS peut modifier l'apparence des choses, pas ce qu'elles disent. Il n'atteint pas les barres d'outils et les onglets autour du texte.
- Le CSS personnalisé est utilisé dans l'histoire, dans le lecteur et dans les exports HTML. EPUB, PDF et Word ne le prennent pas en compte.

<a id="what-the-exports-take"></a>

## Ce que prennent les exports

Un style pour les exports se choisit dans la [boîte de dialogue d'export](export.md#print-styles) ; **Dupliquer** un style et cochez **Pour les exports**, ou partez de l'un des quatre fournis par l'application. Les couleurs qui dépendent du thème de l'application (le texte clair du thème sombre) deviennent des couleurs sombres sur une page blanche lors de l'export.

| | HTML | EPUB | PDF | Word |
|---|---|---|---|---|
| polices, tailles, hauteur de ligne, espacements, alignement, retrait de première ligne | ✓ | ✓ | ✓ | ✓ |
| couleurs du texte, de l'emphase, du gras, des citations, des titres, du changement de scène | ✓ | ✓ | ✓ | ✓ |
| couleur de fond | ✓ | ✓ | ✓ | ✓ (la couleur de la page) |
| largeur du texte, lettrines, césure, petites capitales des titres | ✓ | ✓ | | |
| CSS personnalisé | ✓ | | | |

Les liseuses électroniques utilisent souvent leurs propres polices, tailles et couleurs, quoi que dise la feuille de style. PDF et Word ne peuvent utiliser que les polices que le format possède : le PDF choisit serif, sans ou monospace à partir de la liste que vous avez écrite ; Word emploie la première police de la liste et affiche la sienne si l'ordinateur ne possède pas celle-ci.
<a id="share-a-style-export-and-import"></a>

## Partager un style : exporter et importer

Sous la liste des styles, **Exporter** enregistre le style sélectionné dans un fichier (`name.style.json` ; un style intégré peut aussi être exporté) et **Importer**
lit un tel fichier. Un style importé est ajouté à vos styles personnels comme nouveau style ; il n'est pas conservé avant que vous appuyiez sur **Enregistrer**, et il garde son
nom, donc renommez-le si vous en avez déjà un semblable.

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

Il peut contenir plusieurs styles, et vous pouvez l'écrire à la main. Les options sont nommées en majuscules (`FONT_SIZE`, `TEXT_COLOR`, `SCENE_BREAK_TEXT`...) ; l'[exemple horreur](#an-example-a-horror-book) les utilise toutes. Une option que cette version de Marginalia
ne connaît pas est ignorée (on vous indique combien), un fichier provenant d'une version plus récente du format est refusé, de même qu'un style dont le CSS personnalisé
comporte des accolades qui ne correspondent pas.

## An example: a horror book

[Télécharger le style horreur](../../files/horror-book.style.json) (`horror-book.style.json`), importez-le dans *Paramètres → Styles* et sélectionnez-le
pour le voir dans l'aperçu. Il définit **toutes les options** et utilise du CSS personnalisé pour ce que les options ne couvrent pas :

| | |
|---|---|
| **Page** | Fond presque noir `#0c0a0a`, une colonne de `38em`, et une vignette sombre qui se resserre sur les bords (CSS personnalisé). |
| **Texte** | Garamond en `1.12rem`, interligne `1.8`, justifié avec césure, un retrait de première ligne de `1.6em` et presque pas d'espace entre les paragraphes, comme dans un roman imprimé. Le texte est gris papier ancien `#cfc6bb`, avec une lettrine après chaque titre. |
| **Emphase** | Italique rose poussiéreux `#b99a94`, gras rouge sang `#b3261e`. |
| **Liens** | Vert maladif `#8fa07e` qui devient rouge et ondulé lorsque le pointeur passe dessus (CSS personnalisé). |
| **Citations** | Texte gris avec une barre rouge sombre `#5c1010` et une légère tache rouge en arrière-plan (CSS personnalisé). |
| **Titres** | Palatino en petites capitales, centré, rouge sombre `#8e1b17`, avec une légère lueur (CSS personnalisé). |
| **Changement de scène** | `† † †` en rouge sombre ; les listes reçoivent une croix au lieu d'un point (CSS personnalisé). |
| **Code** | Courier New, sur un fond presque noir. |

![Une histoire dans le style horreur](../../images/story-style-horror.png)

Pour l'essayer sur un vrai texte, collez ceci dans une partie (ou l'aperçu affiche un exemple semblable) :

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

Modifiez-le ensuite : mettez le fond à `#14100c` pour un brun de lumière de bougie, réglez **Largeur du texte** sur `32em` pour une page plus étroite, ou supprimez le CSS personnalisé pour voir
ce que les options seules font. Quand vous obtenez un résultat qui vous plaît, **Exporter** le style et conservez le fichier avec votre livre.
