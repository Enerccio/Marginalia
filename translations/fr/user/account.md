---
label: Compte et paramètres
order: 920
icon: person
---

# Compte et paramètres

Tout ce que vous créez dans Marginalia appartient à votre compte : livres, lorebooks, étiquettes, fournisseurs d'inférence, protocoles
et paramètres. Les autres utilisateurs ne les voient pas (sauf les livres que vous [publiez](books/publishing.md)), et vous ne voyez pas les leurs.

<a id="your-account"></a>

## Votre compte

Les boutons en bas de l'espace de travail :

| Bouton | |
|---|---|
| **Langue** | La langue de l'interface utilisateur, voir [Langue](#language). |
| bouton soleil / lune | À côté de la langue : bascule entre le thème sombre et le thème clair, voir [Thème](#theme). |
| **Changer le mot de passe** | Change votre mot de passe, votre nom d'utilisateur et votre nom complet, voir ci-dessous. |
| **Déconnexion** | Vous déconnecte sur cet appareil. |
| **Administration** | Réservé aux administrateurs, voir [Administration](administration/index.md). |

<a id="changing-the-password"></a>

### Changer le mot de passe

**Changer le mot de passe** ouvre une boîte de dialogue avec votre **Nom d'utilisateur**, votre **Nom complet**, votre **Mot de passe actuel**, votre **Mot de passe** et
**Répéter le mot de passe** :

- saisissez votre **mot de passe actuel** pour chaque modification (laissez-le vide si votre compte n'a pas de mot de passe) ;
- pour changer de mot de passe, saisissez le nouveau dans les deux champs de mot de passe ;
- laissez les deux champs de mot de passe vides pour ne changer que le nom et conserver le mot de passe ;
- cochez **Se déconnecter partout ailleurs (autres navigateurs et toutes les connexions mémorisées)** pour déconnecter votre compte sur tous les autres appareils
  sans changer le mot de passe, voir plus bas ;
- le nom d'utilisateur ne doit pas être utilisé par un autre compte.

Cliquez sur **OK** pour enregistrer. Avec un mot de passe actuel erroné, rien n'est enregistré.

Changer le mot de passe met fin à toutes les connexions mémorisées (*Rester connecté*), sur tous les appareils, y compris celui-ci : ils doivent se reconnecter
avec le nouveau mot de passe. Vos autres sessions encore ouvertes (un navigateur sur un autre appareil affichant toujours l'espace de travail) sont
également terminées ; seule celle où vous travaillez reste active. Si un appareil a été perdu ou si votre mot de passe a fuité,
changer le mot de passe déconnecte cet appareil.

**Se déconnecter partout ailleurs** fait la même chose sans nouveau mot de passe : chaque connexion mémorisée de votre compte est oubliée et
chaque autre session ouverte est terminée. Utilisez-le si vous êtes resté connecté quelque part, par exemple sur un ordinateur qui n'est pas le vôtre. Comme pour toute autre modification de cette boîte de dialogue, il demande votre mot de passe actuel. La session dans laquelle vous êtes reste active, mais
sa connexion mémorisée est elle aussi oubliée, donc ce navigateur demandera aussi le mot de passe la prochaine fois.

<a id="language"></a>

### Langue

Le sélecteur **Langue**, au-dessus des boutons, fait passer l'interface utilisateur dans une autre langue. Le choix est enregistré dans
votre compte : la page se recharge dans la nouvelle langue, et il en va de même pour chaque autre page ouverte (sur un autre appareil
aussi), et il est réutilisé à votre prochaine connexion. L'écran de connexion lui-même est toujours dans la langue par défaut.

La liste ne contient que les langues installées. L'anglais, le japonais, le français, l'espagnol, l'allemand, le coréen et l'italien sont fournis avec Marginalia ; un administrateur
peut en ajouter en chargeant une [extension](extensions/index.md), par exemple
[la localisation en chinois](extensions/chinese-localization.md). Les textes qu'une langue ne traduit pas s'affichent en anglais. Les extensions fournies apportent leurs propres textes en japonais, chinois, français, espagnol, allemand, coréen et italien (les versions chinoises s'affichent lorsque la localisation chinoise est chargée).
Les dates sont écrites selon les usages de cette langue. Vos livres et leurs textes ne sont pas traduits, seule l'interface l'est.

![Le sélecteur de langue](../images/language-selector.png)

![L'espace de travail en japonais](../images/ui-japanese.png)

<a id="theme"></a>

### Thème

Le bouton à côté du sélecteur **Langue** bascule l'interface utilisateur entre le thème sombre (par défaut) et le thème clair.
Le choix est enregistré dans votre compte et réutilisé à votre prochaine connexion, sur n'importe quel appareil ; une page déjà ouverte dans un autre onglet
change lorsqu'elle est rechargée. Les graphiques et la carte des relations prennent les couleurs du thème au moment de leur ouverture.

![L'espace de travail en thème clair](../images/theme-light.png)

<a id="logging-in"></a>

### Connexion

**Rester connecté** sur l'écran de connexion vous garde connecté sur cet appareil pendant 30 jours (cela nécessite HTTPS sur un serveur, voir
[Serveur (Docker)](getting-started/docker-server.md#https-and-reverse-proxy)). **Déconnexion** y met fin.

Un compte peut avoir un mot de passe vide ; vous vous connectez alors avec le nom d'utilisateur seul. Si un administrateur a effacé votre mot de passe oublié, connectez-vous ainsi et définissez aussitôt un nouveau mot de passe avec **Changer le mot de passe**. C'est pratique pour l'application de bureau sur votre propre ordinateur, mais ne le faites jamais sur un serveur accessible à d'autres personnes.

<a id="settings"></a>

## Paramètres

L'onglet **Paramètres** contient vos valeurs par défaut pour tous vos livres.

![L'onglet Paramètres](../images/settings-defaults.png)

En haut, trois boutons :

- **Importer la sauvegarde comme nouveau livre** - crée un livre à partir d'un fichier de sauvegarde, voir
  [Sauvegardes de livres](books/backups.md#importing-a-backup-as-a-new-book) ;
- **Abandonner les modifications** - annule les modifications non enregistrées et recharge les paramètres enregistrés ;
- **Enregistrer** - enregistre les paramètres.

!!!info Modifications non enregistrées
Les paramètres ne sont pas enregistrés automatiquement. Dès que vous modifiez un champ (y compris les paramètres d'extension), *Modifications non enregistrées*
apparaît en haut et les autres onglets, **Administration** et **Déconnexion** sont verrouillés. Cliquez sur **Enregistrer** ou **Abandonner les modifications** pour les déverrouiller.
!!!

<a id="general-settings"></a>

### Paramètres généraux

| Champ | |
|---|---|
| **Modèle par défaut** | Le [fournisseur d'inférence](inference-providers.md) avec lequel démarrent les nouveaux livres. |
| **Protocole par défaut** | Le [protocole](protocols.md) avec lequel démarrent les nouveaux livres. |
| **Modèle par défaut pour l'annotation**, **Protocole par défaut pour l'annotation** | Le modèle qui [annote une partie](books/annotations.md#letting-a-model-annotate) lorsque vous n'en choisissez pas un autre pour l'exécution. Vide correspond au modèle du livre. |
| **Stratégie de sauvegarde** | [Sauvegardes automatiques de livres](books/backups.md#automatic-backups) pour les livres qui n'ont pas leur propre stratégie : *Aucune sauvegarde*, *Après N messages* ou *Après N minutes*, avec le nombre à côté. |
| **Versions conservées par partie** | Combien de versions antérieures du texte d'une partie l'[historique](books/version-history.md#how-many-versions-are-kept) conserve pour tous vos livres, sauf si un dossier, un livre ou l'administrateur en limite davantage le nombre. Vide ne fixe ici aucune limite ; **0** ne conserve aucun historique. |
| **Parties chargées à la fois dans l'histoire** | Combien de parties d'un livre [l'histoire](books/story-editor.md) et la [visionneuse](books/publishing.md) gardent chargées. L'histoire commence avec les dernières parties, la visionneuse à l'endroit où vous vous êtes arrêté de lire (ou avec les premières parties) ; les parties avant et après sont chargées au fil du défilement, et celles éloignées de vous sont à nouveau retirées. Vide charge toutes les parties d'un coup. |
| **Objectif quotidien : mots écrits**, **Objectif quotidien : minutes d'édition** | L'objectif d'une journée pour le calendrier des [statistiques d'écriture](books/statistics.md#the-daily-goal) : les mots que vous écrivez vous-même et les minutes que vous éditez, sur l'ensemble de vos livres. Vide signifie pas d'objectif. |
| **Enregistrer les statistiques d'écriture**, **Conserver les statistiques pendant (mois)** | Indique si vos [statistiques d'écriture](books/statistics.md#privacy-clearing-not-recording-keeping-less) sont comptées, et combien de mois on en conserve (vide ou 0 : tous). Un administrateur peut désactiver l'enregistrement pour tout le monde et fixer une durée plus courte. |

*Modèle par défaut* et *Protocole par défaut* ne servent qu'à la création d'un livre ; modifier ces valeurs ne change pas les livres existants. L'extension [Lore Generator](extensions/lore-generator.md) n'a pas de livre et génère avec eux. La stratégie de sauvegarde s'applique à tous les livres sans *Réglage propre au manuscrit*, à partir de leur prochaine partie générée.
<a id="templates"></a>

### Modèles

Les valeurs par défaut des [prompts de livre](books/prompts.md) :

| Champ | |
|---|---|
| **Langue des prompts** | La langue des prompts intégrés : anglais (par défaut), japonais, français, espagnol, allemand, italien, coréen, ou chinois lorsque la [localisation chinoise](extensions/chinese-localization.md) est chargée. Voir [Langue des prompts](#prompt-language) ci-dessous. |
| **Modèle principal** | Le cadre du prompt, voir [Modèle principal](books/prompts.md#master-template). |
| **Point de vue (POV)**, **Temps** | La voix narrative et le temps. |
| **Style** | Consignes de style et de ton. |
| **Prompt utilisateur par défaut** | La demande pour chaque partie, voir [Prompt utilisateur](books/prompts.md#user-prompt). |
| **Prompt de résumé par défaut** | La demande de [résumé](books/summaries.md#the-summary-prompt). |
| **Prompt de méta-résumé par défaut** | La demande de [méta-résumé](books/summaries.md#the-meta-summary-prompt), qui fusionne des résumés. |
| **Prompt système de réécriture**, **Prompt de réécriture** | Le message système et le dernier message d'une [réécriture](books/story-editor.md#redrafting-a-part) d'une partie, voir [Prompt de réécriture](books/prompts.md#redraft-prompt). |
| **Prompt système d'annotation**, **Prompt d'annotation** | Le message système et le dernier message de la demande faite à un modèle pour [annoter](books/annotations.md#letting-a-model-annotate) une partie, voir [Prompt d'annotation](books/prompts.md#annotate-prompt). |
| **Instructions d'annotation** | Ce que le modèle cherche lorsqu'il annote, pour les livres et dossiers qui n'en définissent pas, voir [Prompt d'annotation](books/prompts.md#annotate-prompt). |
| **Prompt de rédaction des instructions** | La demande faite à un modèle pour [rédiger les instructions](books/story-editor.md#drafting-the-instructions) du prochain tour à votre place, voir [Prompt de rédaction des instructions](books/prompts.md#impersonate-prompt). |
| **Prompt de compactage des agents** | Seulement pour les utilisateurs qui peuvent utiliser des [agents](agents.md). Ce qui est demandé à vos agents lorsque vous les [`/compact`](agents.md#running-a-command-yourself), si leur propre prompt de compactage est vide ; vide ici signifie celui défini par votre administrateur, sinon celui intégré. |
| **Instructions des agents (agents.md)** | Seulement pour les utilisateurs qui peuvent utiliser des [agents](agents.md). Vos instructions pour les agents qui n'en ont pas de propres, ajoutées à leur prompt à chaque exécution. Vide signifie aucune. |
| **Message à une URL quand un agent a besoin de vous**, **Format du message**, **Aussi quand l'application est ouverte**, **Envoyer un message de test** | Seulement pour les utilisateurs disposant de l'autorisation [*Notifications*](administration/permissions.md) (et pas seulement ceux qui utilisent des [agents](agents.md)), dans l'onglet *General*, et seulement si votre administrateur l'autorise. Où envoyer un court message lorsqu'un agent demande quelque chose, attend votre réponse, termine ou échoue, et que vous avez fermé l'application : voir [Quand vous n'êtes pas là](agents.md#when-you-are-not-there). |
| **Prompt système des agents** | Seulement pour les utilisateurs qui peuvent utiliser des [agents](agents.md). Ce que l'on indique à vos agents d'être lorsque leur propre prompt système est vide ; vide ici signifie celui défini par votre administrateur, sinon celui intégré. Texte brut, pas un modèle. |

![L'onglet Modèles](../images/settings-templates.png)

Plus bas, pour un utilisateur qui peut utiliser des agents, figurent les prompts des agents :

![Les prompts des agents dans l'onglet Modèles](../images/settings-templates-agents.png)

Chaque champ affiche en gris la valeur par défaut intégrée tant qu'il est vide, et possède la fenêtre d'aide avec
**Insérer le modèle par défaut** et les [variables et macros](templates/index.md) disponibles.

<a id="prompt-language"></a>

#### Langue des prompts

**Langue des prompts** choisit la langue des prompts intégrés : ce que représente un champ vide (le texte gris change
dès que vous choisissez une autre langue), et donc ce qui est envoyé au modèle pour la génération, les résumés, la
réécriture, les annotations et la rédaction des instructions. Les prompts des extensions qui ont les leurs (Arc Outline, Beta Readers, Book Description Generator,
Lore Generator, Manga Generator, SillyTavern Converter, Knowledge Tracker, Audiobook) et ceux des agents la suivent aussi.
Elle est indépendante de la langue de l'interface : vous pouvez utiliser l'application en anglais et envoyer des prompts en japonais.
Un modèle suit mieux les instructions dans la langue de l'histoire, donc choisissez celle dans laquelle vos livres sont écrits. Seuls les
prompts intégrés changent : un prompt que vous avez écrit vous-même, ici ou dans un livre ou un dossier, reste tel quel. Un [agent](agents.md)
lit ses prompts et ses notifications dans sa propre langue (*Language* dans les paramètres de l'agent), et non dans celle-ci.

Ces valeurs par défaut s'appliquent à **chaque livre dont le champ propre est vide**, immédiatement, et pas seulement aux nouveaux livres. Un livre qui
définit sa propre valeur dans son onglet *Prompts* la conserve. Donc :

- définissez ici ce que tous vos livres ont en commun ;
- définissez dans l'onglet *Prompts* d'un livre ce qui est propre à ce livre ;
- videz le champ d'un livre pour le ramener à la valeur par défaut des *Paramètres*.

### Styles

Les styles de texte de vos livres : l'apparence du texte des parties dans l'histoire et chez le lecteur. L'onglet liste les styles **Normal**
et **Book**, que l'application fournit et qui ne peuvent pas être modifiés (ils sont marqués *intégré*), ainsi que vos propres styles. Sélectionnez un style pour le modifier à droite ; les boutons sous la liste :

- **Dupliquer** - crée un style à vous à partir du style sélectionné (y compris un style intégré : c'est ainsi que l'on commence), nommé
  *... (copy)*, que vous pouvez ensuite renommer et modifier ;
- **Supprimer** - supprime un style à vous ; un style intégré ne peut pas être supprimé ;
- **Définir par défaut** - fait de ce style celui qu'utilisent les livres, sauf s'ils [en choisissent un autre](books/story-editor.md#text-styles) dans le menu des paramètres de l'histoire. Le style par défaut est marqué *par défaut* dans la liste.
- **Exporter** et **Importer** - enregistrent le style sélectionné dans un fichier `.style.json`, et ajoutent les styles d'un tel fichier aux vôtres (non conservés
  tant que vous n'avez pas enregistré les paramètres). Voir [Partager un style](books/styling-guide.md#share-a-style-export-and-import).

Le [guide de style](books/styling-guide.md) passe en revue chaque option et propose un style d'exemple à télécharger.

![L'onglet Styles, avec le style horreur du guide de style sélectionné](../images/settings-styles.png)

Pour un style à vous, le formulaire de droite comporte un **Nom**, la case **Pour les exports (non proposé pour les livres)** et deux onglets. Un style est
soit pour les livres, soit pour les exports : un style pour les exports est proposé dans la [fenêtre d'export](books/export.md#print-styles) (l'application en fournit
quatre, les apparences des exports HTML, EPUB, PDF et Word) et ne peut pas être le style par défaut des livres, et un style pour les livres n'est pas
proposé dans la fenêtre d'export. La liste les marque *(export)*.

- **Options** - par groupes : *Text* (police, taille, hauteur de ligne, espacement des lettres et des mots, couleur, alignement, césure, l'espace
  entre les paragraphes, le **retrait de première ligne** des paragraphes et une **lettrine** après les titres), *Page* (la largeur de la colonne
  de texte, centrée, et la **couleur de fond**, qui remplit toute la zone derrière le texte), *Emphasis, links and code* (les couleurs du texte en italique et en gras et des liens, la
  police du code), *Quotes*, *Headings* (police, taille du premier niveau, couleur, alignement, petites capitales) et le symbole et la
  couleur du *Scene break* (`---`). Une case accepte toute valeur CSS (`1.1rem`, `#336699`, `var(--lumo-body-text-color)`) ; une case vide
  conserve l'apparence de l'application. Les cases de police proposent des familles de polices courantes, et acceptent les vôtres.
- **CSS personnalisé** - du CSS à vous, ajouté après les options et appliqué à l'intérieur du style, de sorte qu'une règle que vous écrivez ne vaut que pour le
  texte de ce style : `.chat-message-markdown p { text-indent: 1.5em !important; }` met en retrait les paragraphes, les déclarations simples
  (`font-size: 1.2rem;`) s'appliquent à tout le texte. En dessous, **CSS généré** montre la feuille de style que produisent les options (lecture seule).
  Les accolades `{ }` doivent correspondre, sinon le style n'est pas enregistré.

![L'onglet CSS personnalisé d'un style](../images/settings-styles-css.png)

Sous le formulaire, l'**Aperçu** montre un texte d'exemple dans le style, et suit chaque modification pendant que vous tapez.

Comme les autres onglets, l'onglet Styles est enregistré avec **Enregistrer** des paramètres, et **Abandonner les modifications** annule aussi ses changements :
dupliquer, modifier, supprimer et définir le style par défaut ne modifient que la copie de travail, et *Unsaved changes* (ainsi que le verrou) apparaît dès que vous faites l'une de ces actions. Un style sans nom, ou un CSS personnalisé dont les accolades ne correspondent pas, est
refusé à l'enregistrement.
### Exports

Ce que contient chaque format de fichier à l'export : l'onglet a une section pour chaque format qui a quelque chose à régler (**Plain text**, **Markdown**, **HTML**,
**Word**, **PDF**, **EPUB**, **Markdown with front matter**, **Obsidian vault** ; cliquez sur son nom pour l'ouvrir).

- **Print style** (HTML, Word, PDF et EPUB, les formats avec polices et couleurs) - le style que la [boîte de dialogue d'export](books/export.md) présélectionne
  pour le format. Il part de l'apparence que l'application fournit pour le format (**HTML page**, **PDF book**...) ; la liste contient les
  [styles pour les exports](#styles), les intégrés et les vôtres. Choisissez-en un autre pour exporter ce format avec lui à chaque fois, jusqu'à ce que vous modifiiez le
  choix dans la boîte de dialogue pour un export donné.
- **Front matter** (Markdown with front matter et Obsidian vault) - le bloc YAML qui commence le fichier, avec `{{title}}`, `{{author}}`,
  `{{language}}` et `{{exportDate}}`. Vide, c'est le bloc par défaut.
- **Title page** (tous les formats sauf ceux destinés à d'autres outils ; le coffre Obsidian l'a dans la note du livre) - le modèle de la page de titre, un modèle Handlebars qui écrit du Markdown, avec `{{title}}` et
  `{{author}}` (l'icône d'information du champ les liste). Vide, c'est le modèle par défaut : le titre en titre, et l'auteur en italique dessous.
  **Fill default** dans la fenêtre d'information part du modèle par défaut.
- **Authorship statement** (tous les formats sauf le chat SillyTavern) - le texte que la case **Include authorship statement** de l'export ajoute à la fin : `{{title}}`, `{{author}}`, `{{totalWords}}`, `{{writtenWords}}`, `{{generatedWords}}`, `{{writtenPercent}}`,
  `{{generatedPercent}}` et `{{models}}` (les modèles séparés par des virgules, vide s'il n'y en a aucun). Vide, c'est le texte par défaut.

Ce sont vos versions : un livre ou un dossier peut avoir les siennes, voir [Title page template](books/export.md#title-page-template). L'onglet est enregistré avec **Save** des réglages, et **Abandonner les modifications** les jette, comme pour les autres. Un modèle non valide est refusé à l'enregistrement. Un style que vous créez dans l'onglet Styles peut être choisi ici après l'enregistrement.

![L'onglet Exports, avec la section HTML ouverte](../images/settings-exports.png)

### Tags

Un tableau de tous vos tags (ceux que vous donnez aux livres, aux lorebooks et aux entrées de lorebook) avec le nombre d'objets auxquels chaque
tag est rattaché. Cliquez sur un en-tête de colonne pour trier par elle. Contrairement aux autres onglets, il agit immédiatement : il ne fait pas partie des
modifications non enregistrées et n'a rien à enregistrer.

![L'onglet Tags](../images/settings-tags.png)

| Bouton | |
|---|---|
| **Renommer** (crayon) | Demande le nouveau nom du tag ; chaque objet garde le tag sous le nouveau nom. |
| **Supprimer** (corbeille) | Demande confirmation, puis supprime le tag et le retire de tous les objets qui l'ont. |

Si le nouveau nom est celui d'un autre tag (les noms sont comparés sans tenir compte de la casse), renommer rendrait les deux tags
identiques, on vous demande donc si vous voulez les **fusionner**. Fusionner déplace tout ce qui porte le tag renommé vers le tag
existant et supprime le tag renommé ; un objet qui a les deux finit avec le tag existant, une seule fois. Annulez et rien
ne change, le tag garde son ancien nom.

![Demande de fusion de deux tags](../images/tag-merge.png)

### Keyboard shortcuts

Les touches des [raccourcis de l'éditeur d'histoire](books/story-editor.md#keyboard-shortcuts) (elles fonctionnent dans l'éditeur
d'histoire sur ordinateur, la [disposition téléphone](phone.md) n'en a pas).
Le tableau liste chaque action, y compris celles qu'apportent les [extensions](extensions/index.md), avec sa touche, ainsi que les touches des boutons de la fenêtre [Organise](books/folders.md#organise) de la liste des livres. Chaque élément du menu de réglages de l'histoire, du menu d'une partie et du menu du clic droit sur son texte est ici une action (la plupart n'ont pas de touche tant que vous n'en donnez pas une), et les menus affichent la touche à côté de l'élément.

| Bouton | |
|---|---|
| **Enregistrer** | Appuyez sur la nouvelle combinaison de touches (modificateurs d'abord ou ensemble, par ex. `Ctrl+Shift+K`) ; un clic ailleurs annule. |
| **Rétablir la valeur par défaut** | Rend à l'action sa touche par défaut. |
| **Effacer** | L'action n'a pas de touche. |

Une note en rouge signale une touche que deux actions partagent, une touche que le navigateur garde pour lui (`Ctrl+N`, `Ctrl+T`, `Ctrl+W` et similaires, la page ne les reçoit jamais) et `Alt` avec une lettre (qui tape un caractère sur certains claviers, par ex. macOS). Une touche sans `Ctrl`, `Alt` ni `Meta` ne fonctionne que lorsqu'aucun champ de texte n'a le focus. Les touches sont enregistrées avec **Save** comme les autres réglages et s'appliquent aussitôt aux histoires ouvertes.

![Réglages, Raccourcis clavier](../images/settings-shortcuts.png)

### Notifications

Une notification de bureau du navigateur quand un modèle a fini, pour que vous puissiez quitter l'onglet pendant qu'il travaille. L'onglet a une case **Utiliser les notifications du bureau**, les boutons ci-dessous et une liste de ce qui notifie :

- **Génération de l'histoire**, **Annoter avec l'IA**, **Annoter avec l'IA (automatic)**, **Réécrire**, **Réécrire (automatic)**, **Rédiger des instructions**,
  **Résumé**, **Refaire tous les résumés** et **Assistant d'aide**, ainsi que ce qu'ajoutent les [extensions](extensions/index.md), nommé d'après l'extension
  (*Reviewer: generate a review*, *Lore Generator: generate lore*, *Image Generator: generate the image*, *Arc Outline: generate chapters*...). Les entrées *Manga Generator* marquées *(automatic)* correspondent à son mode continu.
- Les entrées marquées *(automatic)* sont les exécutions que le livre démarre de lui-même, l'[annotation automatique](books/story-editor.md) après une génération
  et la réécriture qui la suit. Elles se choisissent à part de la boîte de dialogue que vous ouvrez vous-même : avec **Annoter avec l'IA** cochée et
  **Annoter avec l'IA (automatic)** non cochée, l'annotation automatique est silencieuse ; avec seulement l'entrée automatique cochée, seule l'annotation automatique notifie.
- Une exécution qui se termine par une erreur notifie aussi (*… failed*), celle que vous arrêtez vous-même non.

La case active les notifications pour votre compte et rend utilisables deux boutons. **Enregistrer ce navigateur** demande au navigateur la
permission (à partir de ce clic) ; la ligne sous les boutons indique ce que dit ce navigateur : autorisé, bloqué (à autoriser dans les réglages de sites du
navigateur), ou pas encore demandé. La permission appartient au navigateur, pas à votre compte : chaque navigateur ou appareil où vous voulez des
notifications doit être enregistré une fois, la case et la liste sont les mêmes partout. **Envoyer une notification de test** en affiche une aussitôt,
même pendant que la page est au premier plan.

Le champ **Filtrer** réduit la liste par nom, **Cocher ceux affichés** et **Décocher ceux affichés** agissent sur les entrées qu'il affiche et laissent les autres
telles quelles. Une notification n'est affichée que lorsque vous ne regardez pas la page (un autre onglet, ou la fenêtre n'est pas au premier plan) ; cliquer dessus
ramène la page.

Les notifications nécessitent une adresse `https://` ou `localhost` : sur une adresse `http://` simple d'un serveur de votre réseau, le navigateur ne les propose pas, et l'onglet le signale. Un onglet que le navigateur a mis en veille (en général sur un téléphone) ne peut pas non plus les afficher. L'onglet est enregistré avec **Save** des réglages, comme les autres.

### Extension Settings

Réglages des [extensions](extensions/index.md) installées, une section par extension (par exemple *Reviewer Settings*
et *SideQuery Settings*). Ils sont enregistrés avec le même bouton **Save**. L'onglet est vide lorsqu'aucune extension ayant des réglages n'est installée.

![L'onglet Extension Settings avec les réglages de Lore Generator](../images/settings-extensions.png)
