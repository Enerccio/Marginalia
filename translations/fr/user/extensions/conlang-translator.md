---
label: Traducteur de langues construites
order: 17
---

# Traducteur de langues construites

Permet aux personnages de votre histoire de parler une langue construite. Vous décrivez la langue (ses règles et ses mots), et le modèle écrit les **discours directs** des personnages qui la parlent dans cette langue, tandis que l'histoire conserve l'anglais qui se cache derrière.

!!!
Le texte de l'histoire contient la **langue construite**. Tout ce qui lit l'histoire pour un modèle (la génération suivante, les résumés, l'analyse du lorebook, les annotations, les autres extensions) lit l'**anglais** à partir duquel le discours a été traduit. Ainsi, le modèle n'a jamais besoin de comprendre votre langue pour poursuivre l'histoire. Un clic sur un discours traduit affiche l'anglais.
!!!

!!!warning En cours de développement
L'extension fonctionne pour une personne qui utilise les écrans décrits ici et, par l'intermédiaire des [commandes](#commands), pour un agent. Le téléphone ne propose qu'une partie des fonctions, voir [Limites](#limits).
!!!

<a id="the-idea"></a>

## L'idée

1. Une **langue** est constituée de deux éléments : des **règles** (un texte rédigé pour le modèle, à la manière d'un livre de grammaire) et un **lexique** (les mots, rédigés surtout par le modèle et évalués par vous). Les langues sont conservées une seule fois dans vos paramètres, et un nombre quelconque de livres peuvent les utiliser.
2. Un **livre** (ou un dossier de livres) choisit sa langue. Une partie est traduite **à la main** depuis son menu, **automatiquement** lorsqu'elle est définitive, ou **par plage**.
3. Une seule requête au modèle par partie classe chaque discours direct (est-il prononcé dans la langue, ou dans la langue commune de l'histoire, par ce personnage, et compris de l'interlocuteur ?) et traduit ceux qui sont dans la langue. Un mot qui manque à la langue est inventé selon les règles, **signalé avec la raison**, et ajouté au lexique comme **nouveau**, pour que vous l'acceptiez, le modifiiez, le verrouilliez ou le supprimiez.
4. L'anglais est conservé (dans les données du livre, en tant que *source* du discours) et lu par chaque modèle. La langue construite est l'original : ce que vous modifiez à la main y reste, et une partie que vous éditez n'est jamais écrasée à votre insu.
<a id="languages"></a>

## Langues

Une langue se modifie dans **Paramètres → Paramètres des extensions → Conlang Translator**, le panneau qui liste vos langues.

![Les langues dans les paramètres](../../images/plugin-conlang-settings.png)

| Bouton | |
|---|---|
| **New language** | Une langue vide. |
| **Design a language** | Un [assistant](#the-designer) dans lequel le modèle propose chaque partie d'une nouvelle langue, que vous modifiez et acceptez. |
| **Modifier** | L'[éditeur](#the-editor) de la langue sélectionnée. |
| **Dupliquer** | Une copie indépendante avec un nouveau nom, mots compris. Utilisez-la pour essayer une modification (une copie est aussi le seul moyen d'avoir une langue *pour un seul livre* : un livre qui choisit une langue la partage avec tous les autres livres qui la choisissent). |
| **Compare** | Apporte les modifications d'une autre langue dans la langue sélectionnée, après une [revue](#comparing-and-merging). |
| **Exporter**, **Importer** | Une langue sous forme de document (JSON) : les règles et le lexique, sans les liens vers un livre. L'import d'une langue dont vous avez déjà le nom propose la revue des différences ou une nouvelle langue. |
| **Supprimer** | Après une confirmation. |

En haut se trouvent le **modèle et le protocole du concepteur**. Sous la liste figurent les choix que les livres et les dossiers **héritent** de vous : la langue, le fait qu'une partie soit traduite seule,
et le modèle et le protocole de la traduction. Un livre ou un dossier peut définir les siens, voir [Choisir la langue d'un
livre](#choosing-the-language-of-a-book). **Enregistrer** l'écran des paramètres pour conserver vos modifications (les mots qu'une traduction a ajoutés entre-temps
ne sont pas perdus : une langue que vous n'avez pas touchée est relue lorsque l'écran est enregistré).

<a id="the-editor"></a>

### L'éditeur

L'éditeur est une fenêtre à cinq onglets ; il travaille sur une copie jusqu'à **Enregistrer**, et peut être redimensionné, déplacé et agrandi.

| Onglet | |
|---|---|
| **Overview** | Le nom, une description pour vous, l'option **A hyphen is a part of a word** (désactivée : `thal-orun` et `thalorun` sont le même mot ; activée, le trait d'union fait partie de la forme), et le **example book** et le **example lorebook** que lit le concepteur. |
| **Rules** | Les règles, en **sections** (sons et orthographe, grammaire, formation des mots...). Chaque section indique **qui la lit** : *the translator* (envoyée à chaque requête), *only the designer* (l'histoire de la langue, notes pour fabriquer des mots) ou *nobody* (vos propres notes). Le plugin n'analyse pas les règles : ce sont des textes destinés au modèle. |
| **Categories** | Des groupes de mots (*People & Social*, *Nature*, *Particles*...). Le lexique est affiché et envoyé au modèle sous ces intitulés. Une catégorie **toujours envoyée** (particules, pronoms, nombres) est transmise à chaque requête, même lorsque le lexique est trop gros pour être envoyé en entier. |
| **Registers** | Les registres de langue (*formal*, *soft*, *vulgar*, *archaic*...) avec une note. Un concept peut avoir une forme pour chaque registre ; le modèle choisit selon qui parle et à qui. |
| **Lexicon** | Les mots, voir ci-dessous. |

![Les règles d'une langue](../../images/plugin-conlang-rules.png)

<a id="the-lexicon"></a>

### Le lexique

![Le lexique d'une langue](../../images/plugin-conlang-lexicon.png)

La grille dispose d'une **recherche** (par mot ou par sens), de filtres par **catégorie** et par **statut**, et des actions **Add**, **Modifier**, **Accept**,
**Lock**, **Retire**, **Supprimer** et **Accept all new**. Un mot comporte :

- la **forme** (la façon dont l'histoire l'écrit), éventuellement la forme dans un **script**, une **catégorie** et un **genre** (racine, composé, particule,
  nom propre, nombre, expression);
- un ou plusieurs **sens** (un homophone en a plusieurs, chacun avec une classe grammaticale et une note) et les **autres formes du concept**
  (le mot formel, doux et vulgaire pour la même chose, affichés et envoyés ensemble);
- la **justification** : pourquoi le modèle a choisi ce mot (la règle ou les racines). Vous la lisez pour juger le mot, et elle est reprise dans les requêtes suivantes
  afin que l'invention suivante suive la même logique;
- une **origine** : la vôtre, ou celle du modèle (avec la partie pour laquelle il a été inventé, le modèle et l'heure). Un mot du modèle que vous modifiez devient le vôtre;
- un **statut** : **New** (le modèle l'a créé et vous ne l'avez pas encore examiné), **Accepted**, **Locked** (le modèle peut l'utiliser mais ne peut jamais le redéfinir) ou **Retired**
  (conservé, non proposé au modèle : pour un mot qui a été remplacé).

Un mot dont le modèle a besoin mais qui manque au lexique n'est **jamais écarté parce qu'il est nouveau** : il est ajouté immédiatement, afin que la partie suivante dise la même chose de la même
façon, et il reste en *New*. Une traduction qui utilise des mots absents du lexique et que le modèle **n'a pas** signalés est marquée, car
personne ne peut savoir s'il s'agit d'une erreur.

<a id="names-for-the-name-generator"></a>

### Noms pour le générateur de noms

L'onglet **Names** de l'éditeur rend la langue disponible pour le [générateur de noms](name-generator.md) : les noms qu'il produit sonnent comme la langue. Ajoutez une **catégorie de noms**
(par exemple *Places* ou *People*), puis cochez les mots du lexique qui la composent. La **recherche** et le **filtre de catégorie** restreignent la liste, **Check all shown** et **Uncheck all shown**
agissent sur les mots affichés, et **Preview names** produit 20 noms à partir des mots cochés à cet instant, avant que vous n'enregistriez quoi que ce soit. Le générateur de noms entraîne une chaîne de Markov sur les
mots cochés, si bien qu'une catégorie a besoin de quelques mots (une douzaine ou plus donne de meilleurs noms).

Seul votre choix est conservé, pas les mots : un mot que vous modifiez est lu tel qu'il est maintenant, et un mot que vous supprimez ou retirez est laissé de côté lorsque les noms sont produits. Après l'enregistrement des paramètres, la
catégorie est proposée dans le générateur de noms (la boîte de dialogue **Outils** du générateur de lore et de la trame de l'arc, la commande `generate-names`, une exécution d'agent) sous la forme *Conlang - langue - catégorie*.
Elle vous est propre : un autre utilisateur de l'installation ne voit que ses propres catégories.

![L'onglet Names : une catégorie de noms, les mots qui la composent et vingt noms produits à partir d'eux](../../images/plugin-conlang-names.png)

<a id="the-designer"></a>

### Le concepteur

Le menu **Design with AI** de l'éditeur demande de l'aide au modèle, toujours avec la langue **telle qu'elle est** (ses règles, registres, catégories, lexique et le lorebook d'exemple), et
toujours sous forme de **proposition** : rien n'est écrit avant que vous ne l'ayez relu.

![Le menu du concepteur](../../images/plugin-conlang-designer-menu.png)

| Action | |
|---|---|
| **Generate words...** | Des mots pour une catégorie, un nombre donné, avec une note (« la cuisine d'un camp de siège »). |
| **Add a section with AI...** | Une nouvelle section des règles pour ce qui manque (nombres, couleurs, politesse) et les mots dont elle a besoin. |
| **Rewrite a section...** | Le modèle réécrit une section selon votre demande. Le modèle renvoie la **section entière**, si bien qu'une section très longue est lente et coûteuse : gardez les sections courtes, un seul sujet chacune. |
| **Review the language** | Une liste de contradictions, de lacunes et de mots qui cassent le système des sons, chacun avec une section et une correction. Rien n'est modifié. |
| **Ask the designer...** | Une conversation ; une réponse contenant des mots ou des sections peut être relue comme des modifications. |

**Design a language** dans le panneau est un assistant en sept étapes (le cahier des charges, les sons, l'écriture, la grammaire, la formation des mots, les premiers mots par catégorie, et un test avec
des phrases). Chaque étape est une section que le modèle propose et que vous modifiez, affinez avec une note et acceptez ; la langue n'existe que dans la fenêtre jusqu'à **Terminer**.

![L'assistant qui conçoit une langue](../../images/plugin-conlang-wizard.png)

<a id="comparing-and-merging"></a>

### Comparer et fusionner

**Compare** (et l'import d'une langue que vous possédez déjà) ouvre une fenêtre avec les différences entre deux langues, chacune sur une ligne avec une case à cocher : ce qui a été ajouté,
modifié, supprimé ou déplacé parmi les champs, règles, catégories, registres, mots et catégories de noms de la langue. Une modification que les deux côtés ont faite différemment est un **conflit** :
votre valeur est conservée jusqu'à ce que vous choisissiez la leur ou que vous écriviez la vôtre. Ce que vous retenez est écrit dans la langue à l'écran, puis enregistré lorsque vous enregistrez l'écran des paramètres. La
même fenêtre montre ce que le concepteur propose.

![La revue des différences entre deux langues](../../images/plugin-conlang-review.png)

<a id="choosing-the-language-of-a-book"></a>

## Choisir la langue d'un livre

L'onglet **Prompts** de la fenêtre du livre, sous *Extension Settings*, contient les champs de l'extension ; la fenêtre du dossier a les mêmes. Vide signifie *hériter* : le dossier
au-dessus, puis les paramètres de votre utilisateur.

![Les champs d'un livre](../../images/plugin-conlang-book.png)

| Champ | |
|---|---|
| **Language of the speech** | La langue dans laquelle le livre est traduit. Vide : le livre n'est pas traduit. |
| **Translate a new part by itself** | Une partie est traduite lorsqu'elle est définitive : après une génération, après l'annotation automatique et la réécriture si le livre les prévoit, comme une étape de [ce qui suit la partie](../books/story-editor.md#what-follows-a-generated-part) (le panneau des exécutions le montre, son **Arrêter** l'arrête). |
| **Model of the translation**, **Protocol of the translation** | Vide : le modèle et le protocole du livre, sinon vos valeurs par défaut. |
| **Readers of the published book see** | Ce que montre la [visionneuse](#the-viewer). |
<a id="the-conlang-tab-of-the-book"></a>

## L'onglet Conlang du livre

La fenêtre de l'histoire comporte un onglet **Conlang** (dans le même dock que les chapitres et le lorebook ; faites-le glisser où vous voulez) pour la langue de *ce* livre. Il comporte quatre onglets
qui lui sont propres, et **Actualiser** relit la langue après que vous l'avez modifiée dans la fenêtre du livre. Un livre [avec plusieurs langues](#more-than-one-language-in-a-book) dispose d'un champ
à côté de **Actualiser** qui choisit laquelle des langues concernent les onglets *Mots*, *Locuteurs* et *Traduction rapide*.

<a id="words"></a>

### Mots

Le lexique de la langue du livre, avec la recherche, les filtres et les actions de l'[éditeur](#the-lexicon) : consulter un mot pendant l'écriture, le modifier, accepter les mots inventés par le modèle, verrouiller ou retirer un mot. La modification est enregistrée dans votre bibliothèque immédiatement (les mots ajoutés entre-temps par une traduction ne sont pas perdus).

<a id="speakers"></a>

### Locuteurs

Qui parle la langue **dans ce livre**. Les locuteurs appartiennent au livre, et non à la langue : la même langue est parlée par d'autres personnes dans une autre histoire ; une langue que vous dupliquez, exportez ou comparez n'emporte donc aucun personnage.

Par défaut, **tout le monde parle la langue, à l'exception des personnages listés** (ou, à l'inverse, seuls les personnages listés la parlent). Ce n'est qu'une valeur par défaut : le texte la remplace (« elle dit dans la langue commune », « le garde ne comprenait pas un mot »), et le modèle décide pour chaque réplique. Pour chaque personnage, vous pouvez noter le **genre** (une langue qui marque le genre se trompe si le passage dit seulement « elle ») et la **voix** (« formelle, sèche, rude »), que le modèle fait correspondre aux registres de la langue. Les modifications sont enregistrées dans le livre immédiatement.

Chaque personnage peut aussi avoir un niveau de **compréhension** : *ne comprend rien*, *connaît quelques mots*, *suit l'essentiel* ou *suit tout*. Laissez le champ vide (ou choisissez *suit tout*) et les modèles ne reçoivent aucune information à ce sujet. Utilisez-le pour les personnes qui entendent la langue sans la parler : *Tomáš : ne comprend rien*, *Autres villageois : suit l'essentiel* (le nom n'est qu'un texte, donc un groupe convient). Les niveaux sont **statiques pour tout le livre** et sont transmis aux modèles comme simple information ; ce que fait un personnage de cette information relève du modèle, et vous pouvez toujours préciser davantage dans les instructions (« Tomáš ne comprend pas ce qu'ils disent »). Ils atteignent les modèles de deux façons : le **modèle de l'histoire** reçoit un court message avec les niveaux (inséré dans le prompt comme la Note de l'auteur ; la case au-dessus de la liste le désactive, et le nombre indique à partir de combien de messages en partant de la fin il est inséré) ; et le modèle qui **relit un passage** (*Annoter avec l'IA*) les reçoit sous forme de section des faits, afin de pouvoir signaler un personnage qui réagit à des mots qu'il ne comprend pas. Si vous préférez placer vous-même le texte, désactivez l'insertion et utilisez la [macro](../templates/macros.md#commands) {%{`{{command::conlang-understanding}}`}%} dans un modèle ; elle accepte une liste facultative de personnages à conserver.

![L'onglet Conlang : les locuteurs, et deux personnages qui ne suivent pas la langue](../../images/plugin-conlang-tab.png)

<a id="languages"></a>

### Langues

Les autres langues du livre, en plus de celle que désigne son champ **Langue de la parole** : voir [Plusieurs langues dans un livre](#more-than-one-language-in-a-book).

<a id="quick-translate"></a>

### Traduction rapide

Écrivez un texte en anglais (une phrase, un nom, une réplique que vous n'avez pas encore écrite), éventuellement qui le dit et à qui, puis appuyez sur **Traduire** : le modèle le traduit dans la langue du livre, avec les règles, les registres et le lexique, ainsi que les notes des personnages nommés dans le texte. Rien n'est écrit dans l'histoire. Le résultat comprend la traduction, une note sur les choix, et les **mots dont la traduction avait besoin** et que le lexique ne contenait pas, chacun avec son sens et sa raison ; sélectionnez ceux que vous voulez, puis **Importer les mots sélectionnés** : ils entrent dans le lexique comme *nouveaux*, comme les mots d'un passage traduit. Les mots de la traduction qui ne figurent ni dans le lexique ni dans la liste sont signalés en avertissement.

<a id="more-than-one-language-in-a-book"></a>

## Plusieurs langues dans un livre

Une épopée peut avoir une langue pour les elfes, une autre pour les nains et une troisième pour les orques. Un livre désigne **une langue en premier** (le champ ci-dessus, que peuvent aussi fournir un dossier ou vos paramètres), et l'onglet **Langues** de l'onglet **Conlang** ajoute **d'autres langues** depuis votre bibliothèque, une à une, et les retire de nouveau.

- **Chaque langue a ses propres locuteurs.** L'onglet *Locuteurs* concerne la langue que choisit le champ en haut. La première langue est parlée par tout le monde, à l'exception des personnages que vous listez, comme auparavant ; **une langue que vous ajoutez démarre avec « seuls les personnages listés la parlent », sans personne listé**, de sorte qu'elle ne réclame rien tant que vous n'avez pas indiqué qui la parle. Listez un personnage comme exception de la première langue lorsqu'une autre langue est la sienne, sinon la première langue sera aussi demandée pour sa parole (une parole que deux langues revendiquent reste à celle qui est demandée en premier). Un personnage peut parler plusieurs langues (listez-les dans les deux ; le texte du passage indique au modèle laquelle est employée pour une réplique).
- **Une requête par langue.** Une requête ne contient que les règles et le lexique d'une seule langue, et seulement les répliques de ses locuteurs ; un passage contenant les répliques de deux langues donne lieu à deux requêtes, l'une après l'autre. La seconde lit le passage avec l'anglais des répliques de la première et laisse ces répliques traduites. Un passage dont le point de vue et les personnages présents sont tous hors des locuteurs d'une langue n'est pas soumis pour cette langue (un passage qui ne nomme aucun personnage est soumis pour chaque langue qui a des locuteurs).
- **Un passage connaît sa langue.** Le popover, l'export et la visionneuse fonctionnent comme avant ; **Traduire les dialogues (langue construite)** pour une seule langue laisse les répliques des autres intactes, et **Remettre l'anglais** peut porter sur une langue ou sur toutes.
- **Les menus demandent quelle langue** lorsque le livre en a plusieurs : le menu d'un passage (*Traduire les dialogues (langue construite)* : l'une des langues ou *Toutes les langues*) et le clic droit sur une phrase sélectionnée (une langue). **Traduire les passages (langue construite)** comporte un champ *Langue* (toutes les langues par défaut) ; *les passages non traduits* signifie « non traduits dans cette langue ».
- **L'exécution automatique** traduit un nouveau passage dans chaque langue dont les locuteurs peuvent y figurer.
<a id="translating"></a>

## Traduction

- **À la main :** le menu d'une partie contient le sous-menu **Traduire** (le clic droit sur le texte en a un aussi), et on y trouve **Traduire les dialogues (langue construite)** (ces éléments et **Traduire les parties** n'apparaissent que dans un livre qui a une langue de cette extension). Une partie déjà traduite est retraduite **à partir de son anglais** : c'est donc aussi la manière de retraduire une partie après un changement des règles ou du lexique. Un livre avec plusieurs langues demande d'abord [laquelle](#more-than-one-language-in-a-book).
- **Automatiquement :** avec **Traduire une nouvelle partie automatiquement**, une partie générée est traduite dès qu'elle est finale. Un *régénérer* d'une partie lui donne un nouvel anglais et supprime l'ancienne traduction.
- **Dialogues oubliés par le modèle :** une traduction laisse parfois un dialogue en anglais. **Traduire → Traduire les dialogues manquants (langue construite)** dans le menu d'une partie ne traduit que les dialogues encore en anglais et laisse les dialogues traduits tels quels ; avec du texte sélectionné, le clic droit **Traduire → Traduire les dialogues manquants de la sélection (langue construite)** le fait pour les dialogues que la sélection touche. Le modèle reçoit la partie telle quelle, avec les dialogues déjà traduits, et on ne lui demande que les dialogues restant en anglais : l'opération coûte donc moins cher qu'une traduction complète de la partie. Seuls les dialogues sont écrits, ce n'est donc pas un moyen de traduire une phrase que le modèle ne voit pas comme un dialogue : utilisez alors *Traduire le texte sélectionné*.
- **Une phrase que vous avez écrite :** sélectionnez-la dans le texte d'une partie, faites un clic droit et choisissez **Traduire → Traduire le texte sélectionné (langue construite)**. Le modèle traduit le texte sélectionné (les espaces et une paire de guillemets qui l'entourent sont exclus) avec la langue, les notes des personnages et l'histoire autour ; le texte est remplacé par la traduction et l'anglais est conservé comme source du dialogue, si bien qu'un clic dessus montre l'anglais comme pour tout dialogue traduit. Les mots que le modèle a dû inventer sont ajoutés au lexique comme *nouveaux*, comme pour une partie traduite. Une sélection qui contient un dialogue traduit est refusée (remettez d'abord l'anglais), et il en va de même pour une sélection faite dans une partie modifiée entre-temps.
- **Une plage :** **Traduire les parties (langue construite)** dans le menu des outils de l'histoire ouvre une fenêtre qui reste ouverte à côté de l'histoire.

  ![Traduire les parties](../../images/plugin-conlang-range.png)

  Choisissez **Depuis la partie** et **Jusqu'à la partie** (parties avec du texte, comptées à partir de 1 comme dans l'export) puis l'action : *Traduire les parties qui ne sont pas traduites*, *Traduire de nouveau les parties traduites* (après un changement des règles ou du lexique), ou *Remettre l'anglais*, après une question. La fenêtre indique combien de parties contient la plage et combien l'action concerne, affiche *Partie n sur m*, et un bouton **Arrêter** qui prend effet après la partie en cours de traitement (une requête au modèle déjà commencée ne peut pas être annulée). Une partie en échec est comptée et le traitement continue ; une langue ou un modèle manquant l'arrête. L'histoire est redessinée à la fin du traitement. Une partie que vous modifiez pendant le traitement fait échouer cette seule partie, pas le traitement entier.

Une traduction est enregistrée comme toute modification du texte : l'historique de la partie reçoit une version, la partie n'est **pas marquée comme modifiée**, et l'anglais est conservé. **Un dialogue que vous avez modifié à la main est laissé tel quel** lorsque la partie est retraduite ou remise en anglais, car il contient vos mots.

!!!
Une traduction prend autant de temps que le modèle en met pour la partie : quelques minutes avec un modèle local qui réfléchit. L'extension demande au modèle de réfléchir (la traduction du genre, du nombre et du registre est bien meilleure ainsi), donnez donc au modèle une limite de sortie généreuse.
!!!

<a id="what-the-reader-sees"></a>

## Ce que voit le lecteur

<a id="in-the-story"></a>

### Dans l'histoire

Un dialogue traduit est souligné d'un **trait pointillé**. Un clic dessus affiche l'anglais dans une bulle : qui a parlé, et *(non compris par l'interlocuteur)* lorsque l'interlocuteur ne comprend pas la langue ; une note rouge indique que vous avez modifié le texte de la langue après la traduction, si bien que l'anglais ne dit peut-être plus ce qu'il dit maintenant.

**Modifier la source** (un bouton de la bulle, uniquement dans vos propres vues ; les lecteurs d'un livre publié ne l'ont pas) ouvre une petite fenêtre avec la traduction telle que l'histoire la contient et l'anglais que lisent les modèles. Utilisez-la lorsqu'un locuteur natif a corrigé la traduction à la main et que l'anglais dit autre chose maintenant, ou lorsque le modèle a mal rendu l'anglais. L'enregistrement donne au dialogue son nouvel anglais, prend la traduction telle qu'elle est maintenant comme celle à laquelle l'anglais correspond (la note rouge disparaît), et à partir de là les modèles lisent le nouvel anglais à sa place. Le texte de la partie n'est pas modifié, et *Traduire les dialogues (langue construite)* à nouveau remplace les deux. `conlang-edit-source` fait la même chose en ligne de commande.

![La fenêtre qui modifie la source d'un dialogue](../../images/plugin-conlang-source.png)

**Chercher et remplacer dans les sources** (le bouton avec les flèches en haut de l'onglet Langue construite) ouvre la fenêtre de recherche et remplacement de l'application sur les sources anglaises des dialogues traduits de la branche active (texte brut ou expression régulière, avec ou sans respect de la casse ; le tableau liste, par partie et numéro de dialogue, les dialogues où il y a une correspondance). Utilisez-la pour un nom qui a été écrit de deux façons dans l'anglais. Seules les sources changent ; pour modifier les mots de la traduction elle-même, utilisez la recherche et le remplacement du livre. Un dialogue dont la source a été modifiée après la recherche est laissé tel quel et compté.

![Chercher et remplacer sur les sources anglaises des dialogues](../../images/plugin-conlang-search-replace.png)

![L'anglais derrière un dialogue traduit](../../images/plugin-conlang-story.png)

<a id="the-viewer"></a>

### La visionneuse

La visionneuse d'un [livre publié](../books/publishing.md) affiche le dialogue traduit de la manière que le livre demande, avec le champ **Les lecteurs du livre publié voient** (un choix du livre ou de ses dossiers ; vos valeurs par défaut ne décident jamais de ce que voit un lecteur) :

| | |
|---|---|
| **La langue construite, l'anglais au clic** | Le défaut. Un clic (un toucher sur un téléphone) ouvre la bulle avec l'anglais. |
| **La langue construite seulement** | L'anglais n'est jamais envoyé au navigateur du lecteur. |
| **L'anglais seulement** | Le livre comme s'il n'avait jamais été traduit. |

Dans le mode au clic, la page ne contient que la langue construite ; l'anglais d'un dialogue est chargé depuis le serveur lorsque le lecteur clique dessus.

![L'anglais derrière un dialogue traduit, dans la visionneuse](../../images/plugin-conlang-viewer.png)

<a id="exporting"></a>

## Exporter

Lorsque le livre contient des dialogues traduits, la boîte de dialogue d'[export](../books/export.md) a le champ **Dialogues traduits** :

![La boîte de dialogue d'export](../../images/plugin-conlang-export.png)

| | |
|---|---|
| **La langue construite, telle quelle** | Le défaut : le texte tel qu'il est enregistré. |
| **L'anglais seulement** | Le dialogue est remplacé par son anglais. |
| **La langue construite, l'anglais en notes de bas de page** | Chaque dialogue traduit reçoit une note de bas de page avec l'anglais (et le locuteur). |
| **L'anglais, la langue construite en notes de bas de page** | L'inverse. |

Les notes de bas de page sont les [notes de bas de page de l'export](../books/export.md#footnotes) : des notes liées en HTML et en EPUB, des notes de fin après chaque partie en texte brut, de vraies notes en bas de page dans Word et PDF. Un dialogue que vous avez modifié après sa traduction reste tel quel et n'a pas de note. Le texte enregistré du livre n'est jamais modifié par un export.
<a id="commands"></a>

## Commandes

Tout ce que fait l'interface est aussi une commande, utilisable depuis la [ligne de commande](../commands/index.md) et par les [agents](../agents.md) : un agent peut créer une langue, l'enrichir, traduire un livre et vérifier le résultat sans
personne devant les boîtes de dialogue. Une langue s'adresse par son **nom** (la casse n'a pas d'importance) ou par son identifiant ; une section des règles par son titre ou son numéro dans `conlang-list-rules` ; une catégorie par son nom ou son numéro ; un mot par sa
forme (l'identifiant de `conlang-list-words` ne sert qu'à distinguer les homophones) ; une partie comme dans les autres commandes de l'application, par son descripteur ou sa position sur la branche active (la première partie est la 1). La ligne de commande accepte tous
les arguments d'une commande ; un agent peut omettre les arguments facultatifs.

| Commande | |
|---|---|
| `conlang-list`, `conlang-get`, `conlang-create`, `conlang-duplicate`, `conlang-update`, `conlang-delete`, `conlang-export`, `conlang-import`, `conlang-set-references` | La bibliothèque. `conlang-delete` demande confirmation et n'est pas disponible pour un agent. |
| `conlang-list-name-categories`, `conlang-add-name-category`, `conlang-update-name-category`, `conlang-delete-name-category`, `conlang-preview-names` | Les catégories de noms du [générateur de noms](name-generator.md) (l'onglet **Names**). L'ajout et la mise à jour prennent les mots du lexique (séparés par `;`) ou une catégorie entière du lexique ; la suppression demande confirmation à l'utilisateur ; l'aperçu nécessite le générateur de noms. Les commandes `list-name-categories` et `generate-names` du générateur listent et produisent les noms. |
| `conlang-set-book-language` | La langue du livre ouvert (le champ **Language of the speech** de ses *Extension Settings*). |
| `conlang-add-book-language`, `conlang-remove-book-language` | Les autres langues du livre ouvert. La suppression demande confirmation à l'utilisateur et n'est pas disponible pour un agent. |
| `conlang-list-rules`, `conlang-get-rules`, `conlang-add-rules`, `conlang-update-rules`, `conlang-delete-rules`, `conlang-move-rules` | Les sections des règles. |
| `conlang-list-categories`, `conlang-add-category`, `conlang-update-category`, `conlang-move-category`, `conlang-delete-category` | Les catégories des mots. |
| `conlang-list-registers`, `conlang-add-register`, `conlang-update-register`, `conlang-delete-register` | Les registres. |
| `conlang-list-words` | Le lexique, par pages, avec des filtres : catégorie, état (*new*, accepted, locked, retired) et un texte. |
| `conlang-get-word`, `conlang-add-word`, `conlang-update-word`, `conlang-delete-word` | Un mot. Un mot qu'un agent ajoute ou modifie appartient au modèle et est **new** ; un mot verrouillé ne peut être ni modifié ni supprimé. |
| `conlang-lock-word`, `conlang-unlock-word`, `conlang-retire-word`, `conlang-restore-word`, `conlang-accept-words` | L'état des mots. |
| `conlang-check-lexicon` | Les sens répétés, les mots sans catégorie, les composés dont les parties ne sont pas dans le lexique, et les homophones (à titre indicatif). Sans modèle. |
| `conlang-status` | La langue du livre et les parties déjà traduites. |
| `conlang-translate-part`, `conlang-translate-range`, `conlang-translate-sentence`, `conlang-translate-text`, `conlang-revert-to-source` | La traduction : une partie (`missingOnly` ne traduit que les répliques encore en anglais, `within` cite un passage pour limiter la traduction à celui-ci), un intervalle (`mode` *new* ou *again*), une phrase d'une partie, un texte qui est seulement renvoyé, et le retour à l'anglais. Un intervalle demande à l'utilisateur confirmation pour *again* ; il en va de même pour le retour à l'anglais. Avec l'argument facultatif `language`, elles concernent une langue du livre (toutes par défaut, la première pour une phrase ou un texte). |
| `conlang-edit-source` | Fixe la source anglaise d'une réplique traduite (`part`, `speech` selon la numérotation de `conlang-list-passages`, `source`) ; la traduction telle qu'elle est maintenant est considérée comme la sienne. Remplace l'ancienne source. |
| `conlang-check-translation`, `conlang-get-source`, `conlang-list-passages` | La lecture : les mots d'une réplique traduite que le lexique ne contient pas, le texte d'une partie tel que lisent les modèles (l'anglais), et les répliques traduites d'une partie avec leur anglais. |
| `conlang-speakers`, `conlang-set-speakers-mode`, `conlang-set-speaker`, `conlang-remove-speaker` | Qui parle la langue dans le livre ouvert (`language` désigne une des langues du livre, la première par défaut) ; `conlang-set-speaker` fixe aussi le niveau de `understands`. |
| `conlang-set-understanding-prompt` | Indique si les niveaux de compréhension sont insérés d'eux-mêmes dans le prompt de l'histoire, et sur combien de messages en partant de la fin. |
| `conlang-design-words`, `conlang-design-rules`, `conlang-rewrite-rules`, `conlang-design-stage`, `conlang-ask-designer`, `conlang-try-sentences` | Le concepteur. Avec `ask true`, une commande renvoie la proposition sous forme de liste numérotée de modifications et la conserve ; rien n'est modifié. |
| `conlang-review` | L'examen de la langue par le concepteur : des constats seulement. |
| `conlang-apply-changes` | Applique les modifications en attente, toutes ou les numéros donnés (identique à **Appliquer** dans l'examen). |
| `conlang-pending-changes`, `conlang-take-pending-changes`, `conlang-discard-pending-changes` | Ce qu'une exécution d'agent a laissé dans le livre, voir [plus bas](#agent-runs). |
| `conlang-understanding` | Qui ne suit pas quelle langue, sous forme de texte, pour la macro {%{`{{command::conlang-understanding::<characters>}}`}%} (les personnages sont facultatifs). |
| `conlang-lexicon`, `conlang-rules` | Le lexique et les règles qu'envoie une demande de traduction, sous forme de texte, pour les [macros](../templates/macros.md#commands) {%{`{{command::conlang-lexicon::<language>}}`}%} et {%{`{{command::conlang-rules::<language>}}`}%}. |

<a id="agent-runs"></a>

### Exécutions d'agents

Un agent travaille sur une **copie du livre**, et les langues ne font pas partie du livre : un agent ne peut donc pas modifier votre bibliothèque à votre insu. La première fois qu'une exécution **modifie** une langue (un mot, une règle, ou les mots qu'une traduction invente), cette langue est copiée dans la copie du livre, avec la version dont elle provient ; à partir de là, l'exécution lit et écrit sa propre copie. Une exécution qui ne fait que lire, ou qui ne traduit qu'avec les mots existants, ne copie jamais rien. La copie suit la copie du livre : les points de contrôle et leur restauration, l'examen de l'exécution, et sa suppression.

Lorsque vous **appliquez** le résultat de l'exécution, la langue entre dans le livre, **et non dans votre bibliothèque**. L'onglet **Conlang** du livre affiche alors *An agent run changed the language ...* avec **Examiner** et **Drop**.
Examiner compare trois versions : la langue au moment où l'exécution en a fait la copie, la version de l'exécution, et votre bibliothèque telle qu'elle est maintenant. Ce que vous avez modifié entre-temps dans la bibliothèque est donc conservé, et ce que vous avez modifié tous les deux constitue un conflit que vous tranchez. Depuis les commandes : `conlang-pending-changes` liste les modifications par numéros et `conlang-take-pending-changes` les prend (elle vous demande confirmation). Une langue créée par l'exécution est ajoutée entière après votre confirmation.

L'exécution lit la bibliothèque jusqu'à la première modification d'une langue, puis sa propre copie : ce que vous modifiez dans une langue pendant que l'exécution travaille n'est pas vu par l'exécution, mais n'est pas perdu non plus.

<a id="limits"></a>

## Limites

- **La qualité relève du modèle.** L'extension vérifie gratuitement ce qu'elle peut (que la réplique citée figure dans la partie, la langue, les mots par rapport au lexique et à ses règles de composés et d'affixes) et signale le reste ; elle ne contrôle pas votre grammaire. Essayez une langue sur quelques parties avant une longue exécution, et utilisez **Examiner la langue** du concepteur lorsqu'une erreur se répète.
- **Une langue est partagée par les livres qui l'utilisent.** Un mot que vous modifiez, ou un mot qu'une traduction ajoute, change pour chaque livre de la série. C'est voulu (un seul monde), et **Dupliquer** permet de séparer une langue.
- **Les modifications de langue d'un agent attendent dans le livre.** Vous les intégrez à la bibliothèque après avoir appliqué l'exécution ; une langue n'est pas verrouillée pendant qu'une exécution travaille.
- **Les commandes ne remplacent pas un mot dans l'histoire**, et une nouvelle traduction ne peut pas conserver les traductions dont l'anglais n'a pas changé (chaque partie de l'intervalle est à nouveau demandée).
- **Un téléphone** affiche le panneau, l'éditeur, l'examen et l'assistant, mais la grille du lexique défile horizontalement ; le toucher d'une réplique dans la visionneuse fonctionne ; le côté histoire (le menu d'une partie, la fenêtre d'intervalle, le clic dans l'histoire) n'y a pas été essayé.
