---
label: Traducteur de langues réelles
order: 3
---

# Traducteur de langues réelles

Permet aux personnages de votre histoire de parler une **langue réelle** : une histoire se déroulant au Japon peut être écrite en anglais par vous et le modèle, et les dialogues directs des personnages deviennent du japonais authentique (kana et kanji, ou rōmaji), l'anglais restant placé derrière. C'est le [Traducteur de langues construites](conlang-translator.md) pour une langue qui existe : la même idée et les mêmes écrans, sans lexique.

!!!
Le texte de l'histoire contient la **traduction**. Tout ce qui lit l'histoire pour un modèle (la génération suivante, les résumés, l'analyse du livre de connaissances, les annotations, les autres extensions) lit l'**anglais** à partir duquel la parole a été traduite ; vous écrivez et dirigez donc l'histoire en anglais. Un clic sur une parole traduite affiche l'anglais.
!!!

!!!warning Travail en cours
La traduction de parties, de plages et de phrases, les paramètres, l'onglet du livre, la visionneuse, l'exportation et les [commandes](#commands) sont disponibles. Une seconde passe qui peaufine la traduction, plusieurs langues réelles dans un même livre et une langue par personnage ne le sont pas encore, voir [Limites](#limits).
!!!

<a id="the-idea"></a>

## Le principe

1. Une **langue** est un court ensemble de règles pour le traducteur : l'**écriture** (kana et kanji, kana seulement, rōmaji), le fait d'écrire ou non les **suffixes honorifiques** des noms, le **niveau de politesse** choisi à partir de la relation entre le locuteur et l'interlocuteur, la façon dont les **noms** sont écrits, et toute autre règle que vous souhaitez faire respecter. Le modèle connaît la langue elle-même, il n'y a donc pas de lexique. Les langues sont conservées une fois dans vos paramètres, et autant de livres que vous voulez peuvent les utiliser.
2. Un **livre** (ou un dossier de livres) choisit sa langue. Une partie est traduite **à la main** depuis son menu, **automatiquement** lorsqu'elle est définitive, ou **par plage**.
3. Une requête au modèle par partie classe chaque dialogue direct (est-il prononcé dans la langue, ou en anglais, par ce personnage, compris de l'interlocuteur ?) et traduit le dialogue qui l'est, dans la voix et le niveau de politesse que le personnage et la relation demandent.
4. L'anglais est conservé (dans les données du livre, comme *source* du dialogue) et lu par chaque modèle. La traduction est l'original : ce que vous modifiez à la main y reste, et une partie que vous éditez n'est jamais écrasée à votre insu.

L'extension vérifie gratuitement ce qu'elle peut : le dialogue cité doit figurer dans la partie, la langue doit être celle du livre, et la traduction doit être dans l'**écriture** demandée (une traduction en rōmaji contenant des kanji, ou une traduction en kana seulement contenant des kanji, est conservée et *signalée*). Elle ne peut pas vérifier le japonais lui-même : lisez quelques parties avant une longue exécution.

<a id="languages"></a>

## Langues

Une langue se modifie dans **Paramètres → Paramètres des extensions → Traducteur de langues réelles**.

| Bouton | |
|---|---|
| **Add** | Une nouvelle langue. Elle démarre à partir d'un des **préréglages** (japonais en kana et kanji, en kana seulement, en rōmaji, ou une autre langue) qui remplit les champs ; modifiez ce que vous voulez. |
| **Modifier**, **Supprimer**, **Monter**, **Descendre** | La langue sélectionnée. Une suppression demande confirmation. |
| **Dupliquer** | Une copie indépendante portant un nouveau nom. Un livre qui choisit une langue la partage avec tous les autres livres qui la choisissent ; une copie permet de conserver une langue pour un seul livre. |

L'éditeur comporte ces champs :

![Les langues dans les paramètres](../../images/plugin-reallang-settings.png)

| Champ | |
|---|---|
| **Nom** | Le nom que vous donnez (« Japonais, rōmaji pour le roman »). |
| **La langue telle que le modèle la nomme** | Le mot avec lequel le modèle répond pour un dialogue dans la langue (« Japanese ») ; vide : le nom. |
| **Écriture** | *Kana et kanji*, *Kana seulement*, *Rōmaji (lettres latines)*, ou *Une autre écriture (non vérifiée)*. |
| **Écrire les suffixes honorifiques des noms** | san, sama, kun... |
| **Écrire aussi la lecture (rōmaji)** | Le modèle écrit aussi la lecture de la traduction ; la fenêtre contextuelle l'affiche au-dessus de l'anglais. |
| **Politesse et niveau de langue**, **Noms de personnes et de lieux**, **Autres règles** | Courts textes destinés au traducteur. Les préréglages japonais en fournissent pour les deux premiers (forme simple entre égaux proches, forme polie avec les inconnus, keigo envers les supérieurs ; un nom étranger en katakana). |

![L'éditeur d'une langue](../../images/plugin-reallang-editor.png)

Sous la liste se trouvent les choix que les livres et les dossiers **héritent** de vous : la langue, le fait qu'une partie soit traduite automatiquement, et le modèle et le protocole de la traduction. **Enregistrer** l'écran des paramètres pour conserver vos modifications.

<a id="choosing-the-language-of-a-book"></a>

## Choisir la langue d'un livre

L'onglet **Prompts** de la fenêtre du livre, sous *Paramètres des extensions*, contient les champs de l'extension ; la fenêtre du dossier en a de identiques. Vide signifie *hériter* : le dossier parent, puis vos paramètres.

| Champ | |
|---|---|
| **Langue des dialogues** | La langue dans laquelle le livre est traduit. Vide : le livre n'est pas traduit. |
| **Traduire automatiquement une nouvelle partie** | Une partie est traduite lorsqu'elle est définitive : après une génération, après l'annotation automatique et la réécriture si le livre les prévoit, comme étape de [ce qui suit la partie](../books/story-editor.md#what-follows-a-generated-part) (le panneau des exécutions l'affiche, son **Arrêter** l'interrompt). |
| **Modèle de la traduction**, **Protocole de la traduction** | Vide : le modèle et le protocole du livre, sinon vos valeurs par défaut. |
| **Les lecteurs du livre publié voient** | Ce que la [visionneuse](#what-the-reader-sees) affiche. |

![Les champs d'un livre](../../images/plugin-reallang-book.png)

<a id="the-real-language-tab-of-the-book"></a>

## L'onglet Langue réelle du livre

La fenêtre de l'histoire comporte un onglet **Langue réelle** (dans le même panneau que les chapitres et le livre de connaissances) pour la langue de *ce* livre, avec **Actualiser** pour la relire.

![L'onglet Langue réelle de l'histoire, avec les locuteurs et leur compréhension](../../images/plugin-reallang-tab.png)

**Locuteurs.** Qui parle la langue **dans ce livre** (un livre comportant [plusieurs langues](#more-than-one-language-in-a-book) possède un onglet *Locuteurs* pour chacune). Par défaut, tout le monde la parle, sauf les personnages listés (ou seulement ceux listés). Le texte peut modifier cela (« elle dit en anglais », « le garde ne comprenait pas un mot »). Pour chaque personnage, notez le **genre** et la **voix** : le niveau de politesse en découle. Écrivez ici l'âge, le rang et la façon dont le personnage parle aux autres (« un jeune officier, poli avec ses supérieurs, brusque avec ses amis ») : c'est ce qui permet au traducteur de choisir entre la forme simple et le keigo. Les locuteurs appartiennent au livre.

Chaque personnage peut aussi avoir un niveau de **compréhension** : *ne comprend rien*, *connaît quelques mots*, *suit l'essentiel* ou *suit tout*. Laissez-le vide (ou *suit tout*) et les modèles ne reçoivent aucune indication. Utilisez-le pour les personnes qui entendent la langue sans la parler : *Tomáš : ne comprend rien (japonais)*, *Autres villageois : suit l'essentiel* (le nom n'est qu'un texte, un groupe convient donc). Les niveaux sont **statiques pour tout le livre** et sont transmis aux modèles comme information ; ce que fait un personnage de cette information relève du modèle, et vous pouvez toujours préciser dans les instructions (« Tomáš ne comprend pas ce qu'ils disent »). Ils atteignent les modèles de deux façons : le **modèle de l'histoire** reçoit un court message avec les niveaux (inséré dans le prompt comme la Note de l'auteur ; la case au-dessus de la liste le désactive, et le nombre indique à partir de combien de messages en partant de la fin il est inséré) ; et le modèle qui **relit une partie** (*Annoter avec l'IA*) les reçoit sous forme de section des faits, afin de pouvoir signaler un personnage qui réagit à des mots qu'il ne peut pas suivre. Si vous préférez placer le texte vous-même, désactivez l'insertion et utilisez la [macro](../templates/macros.md#commands) {%{`{{command::reallang-understanding}}`}%} dans un modèle ; elle accepte une liste facultative de personnages à conserver.

**Langues.** Les autres langues du livre, en plus de celle que nomme son champ **Langue des dialogues** : voir [ci-dessous](#more-than-one-language-in-a-book).

**Traduction rapide.** Écrivez un texte en anglais, éventuellement qui le dit et à qui, puis appuyez sur **Traduire** : le modèle le traduit selon les règles de la langue (si plusieurs langues existent, choisissez laquelle). Rien n'est écrit dans l'histoire ; vous copiez la traduction.

**Glossaire.** La façon dont les noms et les mots particuliers de l'histoire s'écrivent dans la langue (une [section plus bas](#the-glossary-of-names-and-special-words)) ; un onglet *Glossaire* pour chaque langue du livre.
<a id="the-glossary-of-names-and-special-words"></a>

## Le glossaire des noms et des mots particuliers

Sans glossaire, le modèle choisit à nouveau, dans chaque partie, la façon d'écrire un nom : `Saellen` devient `サエレン` dans une partie et reste en lettres latines dans la suivante, `Tomáš` devient `トマーシュ` ou `トマシュ`. Le glossaire fixe ce choix une fois pour toutes. Une entrée comporte :

- le **mot** tel que l'histoire l'écrit (`Saellen`, `witch`), clé de l'entrée, sans distinction de casse ;
- **la façon de l'écrire** dans la langue (`サエレン`, `魔女`), ou *keep it as it is* pour un titre, un lieu ou un terme inventé qui n'est pas traduit ;
- **la façon de le lire** (facultatif, pour une langue qui a une lecture : `魔女` se lit `ウィッチ`), et une **note** pour le traducteur (la formule d'adresse que porte le personnage, l'ordre du nom de famille) ;
- si elle **s'applique**.

**Trois niveaux, fusionnés.** Le glossaire d'une langue existe à trois niveaux : la **langue** (dans son éditeur, dans *Settings → Extension Settings* : les noms et termes de tous les livres de cette langue), les **dossiers** dans lesquels se trouve le livre (une série définit les noms que partagent ses livres ; les dossiers s'emboîtent) et le **livre** (le sien, celui qui est le plus utilisé). Le modèle reçoit l'*union* ; pour un même mot, **le niveau le plus proche l'emporte**, de sorte qu'un livre peut écrire un nom autrement que son dossier. L'onglet *Glossary* de l'onglet *Real language* montre toutes ces entrées avec le niveau dont chacune provient, et modifie une entrée là où elle se trouve. Un mot d'un niveau supérieur que ce livre ne doit pas utiliser est **désactivé pour ce livre** (une entrée du livre qui est désactivée ; réactivez-la de la même manière, ou supprimez-la pour retrouver le mot du niveau supérieur). Une entrée du livre peut être **remontée** vers un dossier ou vers la langue lorsqu'elle s'avère commune. Le glossaire d'un dossier et d'un livre est tenu **pour chaque langue** du livre, car un nom s'écrit autrement dans chaque langue.

**Ce que fait la traduction avec le glossaire.** Le glossaire fait partie de la requête : le modèle écrit les mots comme ils sont listés. Un glossaire de jusqu'à 100 entrées est envoyé en entier dans chaque requête (un serveur qui met le prompt en cache ne le lit qu'une fois) ; un glossaire plus grand est envoyé avec les entrées qui concernent la partie. Après la réponse, l'extension **vérifie** le résultat : une réplique dont l'anglais contient un mot du glossaire, mais dont la traduction ne l'écrit pas comme le glossaire l'indique, est conservée et **signalée** (c'est à l'auteur de trancher). Un nom pour lequel le modèle a **dû choisir** une forme écrite, et que le glossaire ne liste pas, est ajouté au glossaire du livre comme **proposition** (la colonne *Status* indique *Proposed*) : le modèle l'utilise à partir de là, et *Accept* signifie que vous l'avez examiné, tandis que *Edit* permet de le modifier. Un mot que l'un des niveaux possède déjà (même désactivé) n'est jamais proposé à nouveau.

![Le glossaire du livre : les entrées des trois niveaux, un nom proposé par le modèle attend d'être accepté](../../images/plugin-reallang-glossary.png)

![Une entrée du glossaire](../../images/plugin-reallang-glossary-entry.png)

![Le glossaire à la fin de l'éditeur d'une langue](../../images/plugin-reallang-editor-glossary.png)

Les commandes correspondantes figurent dans le tableau ci-dessous. Une partie qu'un livre a traduite avant l'existence du glossaire conserve sa traduction ; traduisez-la à nouveau pour utiliser le glossaire.

<a id="extracting-the-words-by-model"></a>

## Extraire les mots par le modèle

Écrire le glossaire à la main est long pour un livre volumineux. L'onglet **Word extraction** (à côté des onglets du glossaire dans l'onglet *Real language*) demande au modèle de lire une **plage de parties** et de relever les noms et les mots particuliers qu'il y trouve, avec la forme que chacun prend dans la langue. Tout ce qu'il trouve et que le glossaire ne contient pas encore est ajouté au glossaire du livre comme **proposition** : le modèle l'utilise à partir de la requête suivante, et vous acceptez, modifiez ou supprimez chaque entrée dans l'onglet *Glossary*. Rien ne change dans l'histoire.

![L'onglet Word extraction après une exécution : les termes trouvés par le modèle figurent dans le glossaire comme propositions](../../images/plugin-reallang-extraction.png)

- **Language.** Lorsque le livre comporte plus d'une langue, la langue à laquelle le glossaire est destiné.
- **Prompt.** Consignes données au modèle : ce qu'il doit chercher et la façon dont chaque type de mot s'écrit dans la langue. Choisissez-en un dans la bibliothèque, ou *None: only my specification*. L'extension fournit un prompt pour le japonais (lieux et organisations réels sous leur forme usuelle en japonais, noms étrangers inventés en katakana, noms japonais en kanji avec leur lecture, termes forgés propres au monde sous forme de kanji avec une lecture en katakana, *gikun*) ; il est marqué *built-in* et reste toujours disponible.
- **Specification.** Texte libre pour *ce* livre, ajouté après le prompt : ce qu'il faut laisser de côté (les personnages secondaires), la manière d'écrire un type de mot dans ce livre, une graphie que vous voulez. Sans prompt, c'est tout ce que le modèle reçoit comme consigne.
- **From part / To part**, puis **Extract the words**. Les parties sont lues l'une après l'autre (une partie demande quelques minutes de temps à un modèle lent), l'onglet affiche *Part 3 of 12*, et **Stop** arrête après la partie en cours de lecture. Une partie qui échoue est comptée et l'exécution continue ; un livre sans langue, sans modèle, ou sans prompt ni spécification, arrête l'exécution.

Les mots que le glossaire contient déjà (à n'importe quel niveau, même désactivés) sont communiqués au modèle et ne sont pas proposés à nouveau : une seconde exécution sur la même plage n'ajoute que ce qui est nouveau. La *catégorie* sous laquelle le modèle classe un terme (« Fictional coined term / gikun ») est inscrite dans la note de l'entrée. Vous ne décrivez pas le résultat : l'extension demande un format fixe, quelles que soient les consignes du prompt.

**La bibliothèque de prompts.** *Settings → Extension Settings → Real Language Translator* contient la liste **Extraction prompts** : vos propres prompts, avec un nom et des consignes, à ajouter, modifier, supprimer et réordonner. **Copy a built-in prompt** place dans votre liste une copie du prompt intégré, à modifier. Les prompts vous appartiennent, et non à un livre ; un livre retient celui qu'il a choisi ainsi que sa spécification.

![La bibliothèque des prompts d'extraction dans les paramètres](../../images/plugin-reallang-prompts.png)

<a id="more-than-one-language-in-a-book"></a>

## Plus d'une langue dans un livre

Une histoire avec des locuteurs japonais et coréens a besoin des deux. Un livre désigne **une langue d'abord** (le champ ci-dessus, que peut aussi donner un dossier ou vos paramètres), et l'onglet **Languages** ajoute **d'autres langues** issues de votre bibliothèque, une à une, et les retire de la même manière.

- **Chaque langue a ses propres locuteurs**, dans un onglet à part. La première langue est parlée par tout le monde, sauf les personnages que vous listez, comme auparavant ; **une langue ajoutée commence par « seuls les personnages listés la parlent », sans personne de listé**, de sorte qu'elle ne réclame rien tant que vous n'avez pas indiqué qui la parle. Listez un personnage comme exception de la première langue lorsque une autre langue est la sienne, faute de quoi la première langue sera aussi demandée pour sa parole (une réplique que deux langues revendiquent reste à celle qui est demandée en premier). Un personnage peut parler plusieurs langues (listez-le dans chacune ; le texte de la partie indique au modèle dans laquelle une réplique est prononcée).
- **Une requête pour chaque langue** : les règles et les notes d'une langue, et seulement les répliques de ses locuteurs. Une partie qui contient les répliques de deux langues donne lieu à deux requêtes, l'une après l'autre ; la seconde lit la partie avec l'anglais des répliques de la première langue et laisse ces répliques traduites. Une partie dont le point de vue et les personnages présents sont tous en dehors des locuteurs d'une langue n'est pas demandée pour cette langue (une partie qui ne nomme aucun personnage est demandée pour chaque langue qui a des locuteurs).
- **Un passage connaît sa langue.** **Translate dialogs (real language)** relancé pour une langue laisse les répliques des autres intactes, et **Put the English back** peut agir sur une langue ou sur toutes.
- **Les menus demandent la langue** lorsque le livre en a plusieurs : le menu d'une partie (l'une des langues ou *All languages*) et le clic droit sur une phrase sélectionnée (une langue).
  **Translate parts** possède un champ *Language* (toutes les langues par défaut) ; *the parts that are not translated* signifie « non traduites dans cette langue ».
- **L'exécution automatique** traduit une nouvelle partie dans chaque langue dont des locuteurs peuvent y figurer.
<a id="translating"></a>

## Traduction

- **À la main :** le menu d'une partie a le sous-menu **Traduire** (le clic droit sur le texte en a un aussi), et on y trouve **Translate dialogs (real language)** (ces entrées et **Translate parts (real language)** ne sont affichées que dans un livre qui a une langue de cette extension). Une partie déjà traduite est traduite de nouveau **à partir de son anglais**. Un livre avec plusieurs langues demande d'abord [laquelle](#more-than-one-language-in-a-book).
- **Automatiquement :** avec **Translate a new part by itself**, une partie générée est traduite dès qu'elle est définitive. Une *régénération* d'une partie lui donne un nouvel anglais et supprime l'ancienne traduction.
- **Suivi d'une traduction.** La traduction d'une partie (à la main, d'une sélection ou automatiquement) ne bloque pas la page : elle est listée dans le [panneau des exécutions](../books/story-editor.md#the-panel-of-the-runs) en bas à gauche, avec la fin du **raisonnement** du modèle ou de sa **réponse** au fur et à mesure qu'ils arrivent, et un bouton **Arrêter**. La partie affiche *Translating the speech of the part...* pendant ce temps, et rien d'autre ne peut la traduire. **Arrêter** annule la requête immédiatement et n'en lance aucune autre ; avec plusieurs langues, les langues écrites avant reste traduites. La traduction d'une [plage](#translating) a sa propre fenêtre, qui reste ouverte à côté de l'histoire.
- **Répliques oubliées par le modèle :** une traduction laisse parfois une réplique en anglais. **Traduire → Translate missing dialogs (real language)** dans le menu d'une partie ne traduit que les répliques encore en anglais et laisse les répliques traduites telles quelles ; avec du texte sélectionné, le clic droit **Traduire → Translate missing dialogs in selection (real language)** le fait pour les répliques que la sélection touche. Le modèle reçoit la partie telle qu'elle est, avec les répliques traduites, et on ne lui demande que les répliques encore en anglais, donc l'exécution coûte moins qu'une traduction de la partie entière. Seules les répliques sont écrites : ce n'est donc pas un moyen de traduire une phrase que le modèle ne voit pas comme une réplique. Utilisez *Translate selected text* pour cela.
- **Une phrase que vous avez écrite :** sélectionnez-la dans le texte d'une partie, faites un clic droit et choisissez **Traduire → Translate selected text (real language)**. Le modèle la traduit avec l'histoire qui l'entoure ; l'anglais est
  conservé comme source. Une sélection qui contient une réplique traduite est refusée.
- **Une plage :** **Translate parts (real language)** dans le menu outils de l'histoire ouvre une fenêtre qui reste ouverte à côté de l'histoire : *Translate the parts that are not translated*, *Translate the
  translated parts again* ou *Put the English back*, d'une partie à une autre, avec un **Arrêter**.

![La fenêtre qui traduit une plage de parties](../../images/plugin-reallang-range.png)

Une traduction est enregistrée comme tout changement du texte : l'historique de la partie reçoit une version, la partie n'est **pas marquée comme modifiée**, et l'anglais est conservé. **Une réplique que vous avez modifiée à la main est
laissée telle quelle** lorsque la partie est traduite de nouveau ou remise en anglais, car elle contient vos mots.

<a id="what-the-reader-sees"></a>

## Ce que voit le lecteur

Une réplique traduite a un **trait pointillé** ; un clic dessus affiche l'anglais dans une bulle : la lecture (lorsque la langue la demande), qui parle et si l'auditeur ne comprend pas, ainsi qu'une
note rouge lorsque vous avez modifié la traduction après sa création.

**Edit the source** (un bouton de la bulle, dans vos propres vues seulement) ouvre une fenêtre avec la traduction telle que l'histoire la contient et l'anglais que lisent les modèles. Utilisez-la lorsqu'un locuteur natif a corrigé le
japonais à la main et que l'anglais dit désormais autre chose, ou lorsque le modèle a mal compris l'anglais. Enregistrer donne à la réplique son nouvel anglais, prend la traduction telle qu'elle est maintenant comme celle à laquelle l'anglais appartient (la note rouge disparaît) et les modèles lisent le nouvel anglais à sa place. Le texte de la partie n'est pas modifié. `reallang-edit-source` fait la même chose depuis la ligne de commande.

**Search and replace in the sources** (le bouton avec les flèches en haut de l'onglet *Real language*) ouvre la fenêtre de recherche et remplacement de l'application sur les sources anglaises des répliques traduites
de la branche active (texte brut ou expression régulière). Seules les sources changent ; les mots de la traduction elle-même sont remplacés avec la recherche et remplacement du livre.

![La fenêtre qui modifie la source d'une réplique](../../images/plugin-reallang-source.png)

![Recherche et remplacement sur les sources anglaises des répliques](../../images/plugin-reallang-search-replace.png)

![Une réplique traduite avec l'anglais dans une bulle](../../images/plugin-reallang-story.png)

Le [livre publié](../books/publishing.md) l'affiche comme le livre le demande, avec **Readers of the published book see** : *the translation, the English on click* (la valeur par défaut), *the translation
only* (l'anglais n'est jamais envoyé au navigateur du lecteur) ou *the English only*.

<a id="exporting"></a>

## Export

Lorsque le livre contient des répliques traduites, la boîte de dialogue d'[export](../books/export.md) a le champ **Translated speech (real language)** : *the translation as it is*, *the English only*, *the translation with
the English in footnotes* ou *the English with the translation in footnotes*. Le texte enregistré du livre n'est jamais modifié par un export.

![La boîte de dialogue d'export avec les répliques traduites](../../images/plugin-reallang-export.png)

<a id="together-with-the-conlang-translator"></a>

## Avec le Conlang Translator

Un livre peut contenir les deux : une langue inventée pour les elfes et le japonais pour la ville. Chaque extension a ses propres locuteurs, son propre réglage de langue et ses propres passages, et les passages des deux ne se touchent pas. L'anglais que lit chaque modèle contient les deux. L'[export](../books/export.md) combine les notes de bas de page des deux dans une seule numérotation, et la visionneuse dessine les répliques des deux dans la même page. Réglez la même valeur dans **Readers of the published book see** pour les deux : une page n'est dessinée qu'une fois, par l'extension qui l'atteint en premier.
<a id="commands"></a>

## Commandes

Tout ce que font les écrans est aussi une commande, pour la [ligne de commande](../commands/index.md) et pour les [agents](../agents.md). Une langue est désignée par son **nom** (la casse n'a pas d'importance) ou par son identifiant ; une partie, par son identifiant ou par sa position sur la branche active (la première partie est 1).

| Commande | |
|---|---|
| `reallang-list`, `reallang-create`, `reallang-update`, `reallang-delete` | La bibliothèque. `reallang-create` prend un `preset` (`ja-kana-kanji`, `ja-kana`, `ja-romaji`, `other`). `reallang-delete` demande confirmation. |
| `reallang-set-book-language` | La langue du livre ouvert (le champ **Language of the speech** de ses *Paramètres des extensions*). |
| `reallang-add-book-language`, `reallang-remove-book-language` | Les autres langues du livre ouvert. La suppression demande confirmation à l'utilisateur. |
| `reallang-status` | La langue du livre et les parties traduites. |
| `reallang-translate-part`, `reallang-translate-range`, `reallang-translate-sentence`, `reallang-translate-text`, `reallang-revert-to-source` | Traduire : une partie (`missingOnly` ne traduit que les répliques encore en anglais, `within` cite un passage pour limiter la traduction à celui-ci), une plage (`mode` *new* ou *again*), une phrase d'une partie, un texte qui est seulement renvoyé, et retour à l'anglais. Avec l'option `language`, elles portent sur une langue du livre (toutes par défaut, la première pour une phrase ou un texte). |
| `reallang-glossary`, `reallang-glossary-set`, `reallang-glossary-remove`, `reallang-glossary-accept`, `reallang-glossary-switch`, `reallang-glossary-move` | Le [glossaire](#the-glossary-of-names-and-special-words) du livre ouvert dans une langue : lire les entrées fusionnées avec leur niveau, ajouter ou modifier une entrée à un `level` (`language`, `folder` avec le nom du `folder`, `book`), en supprimer une (demande confirmation), accepter les propositions, désactiver ou réactiver pour le livre un mot d'un niveau supérieur, déplacer une entrée du livre vers le haut. |
| `reallang-extract-terms`, `reallang-extraction-set` | L'[extraction des mots](#extracting-the-words-by-model) : `reallang-extraction-set` choisit le `prompt` et écrit la `specification` du livre ouvert (ce qui n'est pas indiqué reste en place), `reallang-extract-terms` lit les parties de `from` à `to` et propose les termes au glossaire du livre (`language` pour la langue). |
| `reallang-extraction-prompts`, `reallang-extraction-prompt-set`, `reallang-extraction-prompt-remove` | La bibliothèque des prompts d'extraction : lister les prompts intégrés et les vôtres, en ajouter ou en modifier un (nommer un prompt intégré en crée une copie), en supprimer un (demande confirmation). |
| `reallang-edit-source` | Définit la source anglaise d'une réplique traduite (`part`, `speech` tel que `reallang-list-passages` le numérote, `source`) ; la traduction telle qu'elle est maintenant est considérée comme celle à laquelle elle appartient. Remplace l'ancienne source. |
| `reallang-get-source`, `reallang-list-passages` | Lecture : le texte d'une partie tel que les modèles le lisent (l'anglais), et les répliques traduites d'une partie avec leur lecture et leur anglais. |
| `reallang-speakers`, `reallang-set-speakers-mode`, `reallang-set-speaker`, `reallang-remove-speaker` | Qui parle la langue dans le livre ouvert (`language` désigne l'une des langues du livre, la première par défaut) ; `reallang-set-speaker` règle aussi le niveau `understands`. |
| `reallang-set-understanding-prompt` | Indique si les niveaux de compréhension sont insérés d'eux-mêmes dans le prompt de l'histoire, et combien de messages en partant de la fin. |
| `reallang-understanding` | Qui ne suit pas quelle langue, sous forme de texte, pour la [macro](../templates/macros.md#commands) {%{`{{command::reallang-understanding::<characters>}}`}%} (les personnages sont facultatifs). |

Une langue est un ensemble de quelques règles et n'a pas de copie de travail pour une exécution d'agent (le Conlang Translator copie une langue avec son lexique) : un agent qui crée ou modifie une langue modifie immédiatement votre bibliothèque. Les locuteurs, les paramètres du livre et les passages appartiennent au livre, et la copie du livre utilisée par l'exécution les porte.

<a id="limits"></a>

## Limites

- **La qualité dépend du modèle.** La politesse est la partie difficile du japonais ; les règles et les notes sur les caractères la guident, mais quelques parties relues par une personne diront si cela suffit.
- **Les locuteurs décident des langues demandées**, à partir du point de vue et des personnages présents dans une partie. Une réplique d'un personnage que la partie ne nomme pas peut échapper à une langue dont les locuteurs sont listés ; le texte de la partie décide à l'intérieur d'une requête.
- **Pas de seconde passe** qui réécrive une traduction pour la rendre plus naturelle, et pas encore de vérification par retraduction.
- **Pas de furigana.** La lecture est une ligne dans la fenêtre contextuelle, et non un texte ruby dans la page.
- **Une partie que l'autre plugin de traduction a traduite** est lue par ce modèle avec cette traduction dans le texte (et non son anglais) ; il est seulement informé que la réplique y est dans une autre langue.
- **Un téléphone** dispose du panneau et de l'éditeur ; le côté récit (menus, fenêtre de plage, clic dans le récit) n'y a pas été essayé.
