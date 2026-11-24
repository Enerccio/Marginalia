---
label: Compte et paramètres
order: 920
icon: person
---

# Compte et paramètres

Tout ce que vous créez dans Marginalia appartient à votre compte : livres, lorebooks, tags, fournisseurs d'inférence, protocoles et paramètres. Les autres utilisateurs ne les voient pas (sauf les livres que vous [publiez](books/publishing.md)), et vous ne voyez pas les leurs.

<a id="your-account"></a>

## Votre compte

Les boutons en bas de l'espace de travail :

| Bouton | |
|---|---|
| **Language** | La langue de l'interface utilisateur, voir [Langue](#language). |
| bouton soleil / lune | À côté de la langue : bascule entre le thème sombre et le thème clair, voir [Thème](#theme). |
| **Change Password** | Modifie votre mot de passe, votre nom d'utilisateur et votre nom complet, voir ci-dessous. |
| **Logout** | Vous déconnecte sur cet appareil. |
| **Admin** | Réservé aux administrateurs, voir [Administration](administration/index.md). |

<a id="changing-the-password"></a>

### Changer le mot de passe

**Change Password** ouvre une boîte de dialogue avec vos **Username**, **Full Name**, **Current Password**, **Password** et **Repeat password** :

- saisissez votre **mot de passe actuel** pour chaque modification (laissez-le vide si votre compte n'a pas de mot de passe) ;
- pour changer le mot de passe, saisissez le nouveau dans les deux champs de mot de passe ;
- laissez les deux champs de mot de passe vides pour ne changer que le nom et conserver le mot de passe ;
- cochez **Log out everywhere else (other browsers and all saved logins)** pour déconnecter votre compte sur tous les autres appareils sans changer le mot de passe, voir ci-dessous ;
- le nom d'utilisateur ne doit pas être utilisé par un autre compte.

Cliquez sur **OK** pour enregistrer. Avec un mot de passe actuel incorrect, rien n'est enregistré.

Changer le mot de passe met fin à toutes les connexions enregistrées (*Save login*), sur tous les appareils, y compris celui-ci : ils doivent se reconnecter avec le nouveau mot de passe. Vos autres sessions ouvertes à cet instant (un navigateur sur un autre appareil affichant encore l'espace de travail) sont aussi terminées ; seule celle dans laquelle vous travaillez reste active. Si un appareil a été perdu ou si votre mot de passe a fuité, changer le mot de passe déconnecte cet appareil.

**Log out everywhere else** fait la même chose sans nouveau mot de passe : toutes les connexions enregistrées de votre compte sont oubliées et toutes les autres sessions ouvertes sont terminées. Utilisez-le si vous êtes resté connecté quelque part, par exemple sur un ordinateur qui n'est pas le vôtre. Il demande votre mot de passe actuel, comme toute autre modification de cette boîte de dialogue. La session dans laquelle vous êtes reste active, mais sa connexion enregistrée est aussi oubliée : ce navigateur demandera donc aussi le mot de passe la prochaine fois.

<a id="language"></a>

### Langue

Le sélecteur **Language** au-dessus des boutons change la langue de l'interface. Le choix est enregistré dans votre compte : la page se recharge dans la nouvelle langue, de même que toute autre page ouverte (sur un autre appareil aussi), et il est réutilisé à la prochaine connexion. L'écran de connexion est toujours dans la langue par défaut.

La liste ne contient que les langues installées. L'anglais, le japonais, le français, l'espagnol, l'allemand, le coréen et l'italien sont fournis avec Marginalia ; un administrateur peut en ajouter en chargeant une [extension](extensions/index.md), par exemple la [localisation chinoise](extensions/chinese-localization.md). Les textes qu'une langue ne traduit pas s'affichent en anglais. Les extensions fournies apportent leurs propres textes en japonais, chinois, français, espagnol, allemand, coréen et italien (les textes chinois s'affichent lorsque la localisation chinoise est chargée). Les dates suivent l'usage de chaque langue. Vos livres et leurs textes ne sont pas traduits, seule l'interface l'est.

![Le sélecteur Language](../images/language-selector.png)

![L'espace de travail en japonais](../images/ui-japanese.png)

<a id="theme"></a>

### Thème

Le bouton à côté du sélecteur **Language** bascule l'interface entre le thème sombre (par défaut) et le thème clair. Le choix est enregistré dans votre compte et réutilisé à la prochaine connexion, sur n'importe quel appareil ; une page déjà ouverte dans un autre onglet change après rechargement. Les graphiques et la carte des relations prennent les couleurs du thème lorsqu'ils sont ouverts.

![L'espace de travail en thème clair](../images/theme-light.png)

<a id="logging-in"></a>

### Se connecter

**Save login** sur l'écran de connexion vous garde connecté sur cet appareil pendant 30 jours (cela nécessite HTTPS sur un serveur, voir [Serveur (Docker)](getting-started/docker-server.md#https-and-reverse-proxy)). **Logout** met fin à cette connexion.

Un compte peut avoir un mot de passe vide ; vous vous connectez alors avec le seul nom d'utilisateur. Si un administrateur a effacé votre mot de passe oublié, connectez-vous ainsi et définissez immédiatement un nouveau mot de passe avec **Change Password**. C'est pratique pour l'application de bureau sur votre propre ordinateur, mais ne faites jamais cela sur un serveur accessible à d'autres.

<a id="settings"></a>

## Paramètres

L'onglet **Settings** contient vos valeurs par défaut pour tous vos livres.

![L'onglet Settings](../images/settings-defaults.png)

En haut, trois boutons :

- **Import Backup as New Book** - crée un livre à partir d'un fichier de sauvegarde, voir
  [Sauvegardes de livres](books/backups.md#importing-a-backup-as-a-new-book) ;
- **Discard Changes** - annule les modifications non enregistrées et recharge les paramètres enregistrés ;
- **Save** - enregistre les paramètres.

!!!info Unsaved changes
Les paramètres ne sont pas enregistrés automatiquement. Dès que vous modifiez un champ (y compris les paramètres d'extension), *Unsaved changes* apparaît en haut et les autres onglets, **Admin** et **Logout** sont verrouillés. Cliquez sur **Save** ou **Discard Changes** pour les déverrouiller.
!!!

<a id="general-settings"></a>

### Paramètres généraux

| Champ | |
|---|---|
| **Default Model** | Le [fournisseur d'inférence](inference-providers.md) avec lequel démarrent les nouveaux livres. |
| **Default Protocol** | Le [protocole](protocols.md) avec lequel démarrent les nouveaux livres. |
| **Default model for annotating**, **Default protocol for annotating** | Le modèle qui [annote une partie](books/annotations.md#letting-a-model-annotate) lorsque vous n'en choisissez pas un autre pour l'exécution. Vide correspond au modèle du livre. |
| **Backup Strategy** | Sauvegardes automatiques des [livres](books/backups.md#automatic-backups) sans stratégie propre : *No Backups*, *After N Messages* ou *After N Minutes*, avec le nombre à côté. |
| **Versions kept per part** | Nombre de versions antérieures du texte d'une partie que l'[historique](books/version-history.md#how-many-versions-are-kept) conserve pour tous vos livres, sauf si un dossier, un livre ou l'administrateur en limite davantage le nombre. Vide ne fixe aucune limite ici ; **0** ne conserve aucun historique. |
| **Parts loaded at once in the story** | Nombre de parties d'un livre que le [récit](books/story-editor.md) et la [visionneuse](books/publishing.md) gardent chargées. Le récit s'ouvre sur les dernières parties, la visionneuse à l'endroit où vous vous êtes arrêté (ou sur les premières parties) ; les parties avant et après sont chargées au fil du défilement et celles qui s'éloignent de vous sont à nouveau retirées. Vide charge toutes les parties d'un coup. |
| **Daily goal: words written**, **Daily goal: minutes of editing** | L'objectif d'une journée pour le calendrier des [statistiques d'écriture](books/statistics.md#the-daily-goal) : les mots que vous écrivez vous-même et les minutes d'édition, sur l'ensemble de vos livres. Vide signifie aucun objectif. |
| **Record writing statistics**, **Keep statistics for (months)** | Indique si vos [statistiques d'écriture](books/statistics.md#privacy-clearing-not-recording-keeping-less) sont comptées, et combien de mois sont conservés (vide ou 0 : tous). Un administrateur peut désactiver l'enregistrement pour tout le monde et fixer une durée plus courte. |

*Default Model* et *Default Protocol* ne servent qu'à la création d'un livre ; les modifier ne change pas les livres existants. L'extension [Lore Generator](extensions/lore-generator.md) n'a pas de livre et génère avec eux. La stratégie de sauvegarde s'applique à tous les livres sans *Per manuscript override*, à partir de leur prochaine partie générée.
<a id="templates"></a>

### Modèles

Les valeurs par défaut des [prompts de livre](books/prompts.md) :

| Champ | |
|---|---|
| **Prompt language** | La langue des prompts intégrés : anglais (par défaut), japonais, français, espagnol, allemand, italien, coréen, ou chinois lorsque la [localisation chinoise](extensions/chinese-localization.md) est chargée. Voir [Langue des prompts](#prompt-language) ci-dessous. |
| **Master Template** | Le cadre du prompt, voir [Gabarit maître](books/prompts.md#master-template). |
| **Point of View (POV)**, **Tense** | La voix narrative et le temps verbal. |
| **Style** | Consignes de style et de ton. |
| **Default User Prompt** | La demande pour chaque partie, voir [Prompt utilisateur](books/prompts.md#user-prompt). |
| **Default Summary Prompt** | La demande de [résumé](books/summaries.md#the-summary-prompt). |
| **Default Meta Summary Prompt** | La demande de [méta-résumé](books/summaries.md#the-meta-summary-prompt), qui fusionne des résumés. |
| **Redraft System Prompt**, **Redraft Prompt** | Le message système et le dernier message d'une [réécriture](books/story-editor.md#redrafting-a-part) d'une partie, voir [Prompt de réécriture](books/prompts.md#redraft-prompt). |
| **Annotate System Prompt**, **Annotate Prompt** | Le message système et le dernier message de la demande faite à un modèle pour [annoter](books/annotations.md#letting-a-model-annotate) une partie, voir [Prompt d'annotation](books/prompts.md#annotate-prompt). |
| **Annotate Instructions** | Ce que le modèle cherche lorsqu'il annote, pour les livres et les dossiers qui ne définissent rien, voir [Prompt d'annotation](books/prompts.md#annotate-prompt). |
| **Impersonate Prompt** | La demande faite à un modèle pour [rédiger les instructions](books/story-editor.md#drafting-the-instructions) du tour suivant à votre place, voir [Prompt d'incarnation](books/prompts.md#impersonate-prompt). |
| **Compaction prompt of agents** | Seulement pour les utilisateurs autorisés à utiliser des [agents](agents.md). Ce que l'on demande à vos agents lorsque vous les [`/compact`](agents.md#running-a-command-yourself), si leur propre prompt de compaction est vide ; vide ici signifie celui défini par votre administrateur, sinon celui intégré. |
| **Instructions of agents (agents.md)** | Seulement pour les utilisateurs autorisés à utiliser des [agents](agents.md). Vos instructions pour les agents qui n'en ont pas en propre, ajoutées à leur prompt à chaque exécution. Vide signifie aucune. |
| **Message to a URL when an agent needs you**, **Format of the message**, **Also when the application is open**, **Send a test message** | Seulement pour les utilisateurs disposant de l'autorisation [*Notifications*](administration/permissions.md) (et pas seulement ceux qui utilisent des [agents](agents.md)), dans l'onglet *General*, et seulement si votre administrateur l'autorise. Où envoyer un court message lorsqu'un agent pose une question, attend votre réponse, termine ou échoue alors que l'application est fermée : voir [Quand vous n'êtes pas là](agents.md#when-you-are-not-there). |
| **System prompt of agents** | Seulement pour les utilisateurs autorisés à utiliser des [agents](agents.md). Ce que l'on dit à vos agents d'être lorsque leur propre prompt système est vide ; vide ici signifie celui défini par votre administrateur, sinon celui intégré. Texte brut, pas un gabarit. |

![L'onglet Templates](../images/settings-templates.png)

Plus bas, pour un utilisateur autorisé à utiliser des agents, se trouvent les prompts des agents :

![Les prompts des agents dans l'onglet Templates](../images/settings-templates-agents.png)

Chaque champ affiche en gris la valeur intégrée par défaut tant qu'il est vide, et possède la bulle d'aide avec **Insert Default Template** et les [variables et macros](templates/index.md) disponibles.

<a id="prompt-language"></a>

#### Langue des prompts

**Prompt language** choisit la langue des prompts intégrés : ce que représente un champ vide (le texte gris change dès que vous choisissez une autre langue), et donc ce qui est envoyé au modèle pour la génération, les résumés, la réécriture, les annotations et l'incarnation. Les prompts des plugins qui ont les leurs (Arc Outline, Beta Readers, Book Description Generator, Lore Generator, Manga Generator, SillyTavern Converter, Knowledge Tracker, Audiobook) et ceux des agents le suivent aussi. Elle est indépendante de la langue de l'interface : vous pouvez utiliser l'application en anglais et envoyer des prompts en japonais. Un modèle suit le mieux les instructions dans la langue de l'histoire, choisissez donc celle dans laquelle vos livres sont écrits. Seuls les prompts intégrés changent : un prompt que vous avez écrit vous-même, ici ou dans un livre ou un dossier, reste tel quel. Un [agent](agents.md) lit ses prompts et ses remarques dans sa propre langue (*Language* dans les réglages de l'agent), et non dans celle-ci.

Ces valeurs par défaut s'appliquent à **chaque livre dont le champ propre est vide**, immédiatement, et pas seulement aux nouveaux livres. Un livre qui définit sa propre valeur dans son onglet *Prompts* la conserve. Ainsi :

- définissez ici ce que tous vos livres ont en commun ;
- définissez ce qui est propre à un livre dans son onglet *Prompts* ;
- videz le champ d'un livre pour le ramener à la valeur par défaut des *Settings*.

### Styles

Les styles de texte de vos livres : l'aspect du texte des parties dans l'histoire et dans la lecture. L'onglet liste les styles **Normal** et **Book**, que l'application fournit et qui ne peuvent pas être modifiés (ils sont marqués *built in*), ainsi que les vôtres. Sélectionnez un style pour le modifier à droite ; les boutons sous la liste :

- **Duplicate** - crée un style à vous à partir du style sélectionné (y compris d'un style intégré : c'est ainsi que l'on part), nommé *... (copy)*, que vous pouvez ensuite renommer et modifier ;
- **Delete** - supprime un style à vous ; un style intégré ne peut pas être supprimé ;
- **Set as default** - fait du style celui qu'utilisent les livres, sauf s'ils [en choisissent un autre](books/story-editor.md#text-styles) dans le menu des paramètres de l'histoire. Le style par défaut est marqué *default* dans la liste ;
- **Export** et **Import** - enregistrent le style sélectionné dans un fichier `.style.json`, et ajoutent les styles d'un tel fichier aux vôtres (non conservés tant que vous n'avez pas enregistré les réglages). Voir [Partager un style](books/styling-guide.md#share-a-style-export-and-import).

Le [guide de mise en forme](books/styling-guide.md) passe en revue chaque option et propose un exemple de style à télécharger.

![L'onglet Styles, avec le style horror du guide de mise en forme sélectionné](../images/settings-styles.png)

Pour un style à vous, le formulaire de droite comporte un **Name**, la case **For exports (not offered for books)** et deux onglets. Un style est soit pour les livres, soit pour les exports : un style pour les exports est proposé dans la [boîte de dialogue d'export](books/export.md#print-styles) (l'application en fournit quatre, les apparences des exports HTML, EPUB, PDF et Word) et ne peut pas être le style par défaut des livres, et un style pour les livres n'est pas proposé dans la boîte de dialogue d'export. La liste les marque *(export)*.

- **Options** - en groupes : *Text* (police, taille, hauteur de ligne, espacement des lettres et des mots, couleur, alignement, césure, espace entre les paragraphes, **first line indent** des paragraphes et **drop cap** après les titres), *Page* (largeur de la colonne de texte, centré, et **background color**, qui remplit toute la zone derrière le texte), *Emphasis, links and code* (couleurs du texte en italique, en gras et des liens, police du code), *Quotes*, *Headings* (police, taille du premier niveau, couleur, alignement, petites capitales) et le symbole et la couleur de la *Scene break* (`---`). Une case accepte n'importe quelle valeur CSS (`1.1rem`, `#336699`, `var(--lumo-body-text-color)`) ; une case vide conserve l'aspect de l'application. Les cases de police proposent des familles de polices courantes, et acceptent les vôtres.
- **Custom CSS** - CSS à vous, ajouté après les options et appliqué à l'intérieur du style, de sorte qu'une règle que vous écrivez ne vaut que pour le texte de ce style : `.chat-message-markdown p { text-indent: 1.5em !important; }` met en retrait les paragraphes, les déclarations simples (`font-size: 1.2rem;`) s'appliquent à tout le texte. En dessous, **Generated CSS** affiche la feuille de style que produisent les options (lecture seule). Les accolades `{ }` doivent correspondre, sinon le style n'est pas enregistré.

![L'onglet Custom CSS d'un style](../images/settings-styles-css.png)

Sous le formulaire, le **Preview** affiche un texte d'exemple dans le style, et suit chaque modification à mesure que vous tapez.

Comme les autres onglets, l'onglet Styles est enregistré avec le **Save** des réglages, et **Discard Changes** annule aussi ses modifications : dupliquer, modifier, supprimer et définir la valeur par défaut ne changent que la copie de travail, et *Unsaved changes* (ainsi que le verrou) apparaît dès que vous faites l'une de ces actions. Un style sans nom, ou un CSS personnalisé dont les accolades ne correspondent pas, est refusé à l'enregistrement.
<a id="exports"></a>

### Exportations

Le réglage de chaque format d'export : l'onglet comporte une section pour chaque format qui a quelque chose à régler (**Plain text**, **Markdown**, **HTML**,
**Word**, **PDF**, **EPUB**, **Markdown with front matter**, **Obsidian vault** ; cliquez sur son nom pour l'ouvrir).

- **Print style** (HTML, Word, PDF et EPUB, les formats avec polices et couleurs) - le style que la [boîte de dialogue d'export](books/export.md) présélectionne
  pour le format. Il part de l'apparence que l'application fournit pour le format (**HTML page**, **PDF book**...) ; la liste contient les
  [styles pour les exports](#styles), les intégrés et les vôtres. Choisissez-en un autre pour exporter ce format dans ce style à chaque fois, jusqu'à ce que vous changiez le choix dans la boîte de dialogue pour un export donné.
- **Front matter** (Markdown with front matter et Obsidian vault) - le bloc YAML qui commence le fichier, avec `{{title}}`, `{{author}}`,
  `{{language}}` et `{{exportDate}}`. Vide correspond au bloc par défaut.
- **Title page** (tous les formats sauf ceux destinés à d'autres outils ; le coffre Obsidian l'a dans la note du livre) - le modèle de la page de titre, un modèle Handlebars qui écrit du Markdown, avec `{{title}}` et
  `{{author}}` (l'icône d'information du champ les liste). Vide correspond au modèle par défaut : le titre en tant que titre et l'auteur en italique en dessous.
  **Fill default** dans la fenêtre d'information part du modèle par défaut.
- **Authorship statement** (tous les formats sauf le chat SillyTavern) - le texte que la case **Include authorship statement** de l'export ajoute à la fin : `{{title}}`, `{{author}}`, `{{totalWords}}`, `{{writtenWords}}`, `{{generatedWords}}`, `{{writtenPercent}}`,
  `{{generatedPercent}}` et `{{models}}` (les modèles séparés par des virgules, vide s'il n'y en a aucun). Vide correspond au texte par défaut.

Ce sont vos versions : un livre ou un dossier peut avoir les siennes, voir [Modèle de page de titre](books/export.md#title-page-template). L'onglet est enregistré avec **Save** des réglages, et **Discard Changes** annule ses modifications, comme pour les autres. Un modèle non valide est refusé à l'enregistrement. Un style créé dans l'onglet Styles peut être choisi ici après l'enregistrement.

![L'onglet Exports, avec la section HTML ouverte](../images/settings-exports.png)

<a id="tags"></a>

### Étiquettes

Un tableau de toutes vos étiquettes (celles que vous donnez aux livres, aux lorebooks et aux entrées de lorebook) avec le nombre d'objets auxquels chaque
étiquette est liée. Cliquez sur un en-tête de colonne pour trier selon elle. Contrairement aux autres onglets, il agit immédiatement : il ne fait pas partie des modifications non enregistrées et n'a rien à enregistrer.

![L'onglet Tags](../images/settings-tags.png)

| Bouton | |
|---|---|
| **Rename** (crayon) | Demande le nouveau nom de l'étiquette ; chaque objet garde l'étiquette sous le nouveau nom. |
| **Delete** (corbeille) | Demande une confirmation, puis supprime l'étiquette et la retire de tous les objets qui l'ont. |

Si le nouveau nom est celui d'une autre étiquette (les noms sont comparés sans tenir compte de la casse), le renommage rendrait les deux étiquettes identiques : on vous demande alors si vous voulez les **merge** (fusionner). Fusionner déplace tout ce qui porte l'étiquette renommée vers l'étiquette existante et supprime l'étiquette renommée ; un objet qui possède les deux finit avec l'étiquette existante, une seule fois. Cancel n'altère rien, l'étiquette garde son ancien nom.

![Question de fusion de deux étiquettes](../images/tag-merge.png)

<a id="keyboard-shortcuts"></a>

### Raccourcis clavier

Les touches des [raccourcis de l'éditeur de récit](books/story-editor.md#keyboard-shortcuts) (elles fonctionnent dans l'éditeur de récit sur ordinateur ; la [disposition téléphone](phone.md) n'en a aucun).
Le tableau énumère chaque action, y compris celles qu'apportent les [extensions](extensions/index.md), avec sa touche, ainsi que les touches des boutons fenêtre [Organise](books/folders.md#organise) de la liste des livres. Chaque élément du menu de réglages du récit, du menu d'une partie et du menu contextuel de son texte est ici une action (la plupart n'ont pas de touche tant que vous ne leur en donnez pas une), et les menus affichent la touche à côté de l'élément.

| Bouton | |
|---|---|
| **Record** | Appuyez sur la nouvelle combinaison de touches (modificateurs d'abord ou tous ensemble, par exemple `Ctrl+Shift+K`) ; un clic ailleurs annule. |
| **Reset to default** | Rend à l'action sa touche par défaut. |
| **Clear** | L'action n'a plus de touche. |

Une note rouge signale une touche que deux actions partagent, une touche que le navigateur garde pour lui (`Ctrl+N`, `Ctrl+T`, `Ctrl+W` et semblables, la page ne les reçoit jamais) et `Alt` avec une lettre (tape un caractère sur certains claviers, par exemple macOS). Une touche sans `Ctrl`, `Alt` ou `Meta` ne fonctionne que lorsqu'aucun champ de texte n'a le focus. Les touches sont enregistrées avec **Save**, comme les autres réglages, et s'appliquent aussitôt aux récits ouverts.

![Réglages, Keyboard shortcuts](../images/settings-shortcuts.png)

### Notifications

Une notification de bureau du navigateur lorsqu'un modèle a fini, pour que vous puissiez quitter l'onglet pendant qu'il travaille. L'onglet comporte une case **Use desktop notifications**, les boutons ci-dessous et une liste de ce qui notifie :

- **Story generation**, **Annotate with AI**, **Annotate with AI (automatic)**, **Redraft**, **Redraft (automatic)**, **Draft Instructions**,
  **Summary**, **Resummarize all summaries** et **Help assistant**, ainsi que ce que les [extensions](extensions/index.md) ajoutent, nommé d'après l'extension
  (*Reviewer: generate a review*, *Lore Generator: generate lore*, *Image Generator: generate the image*, *Arc Outline: generate chapters*...). Les entrées *Manga Generator* marquées *(automatic)* correspondent à son mode continu.
- Les entrées marquées *(automatic)* sont les exécutions que le livre lance de lui-même : l'[annotation automatique](books/story-editor.md) après une génération et la réécriture qui la suit. Elles se choisissent à part de la boîte de dialogue que vous ouvrez vous-même : avec **Annotate with AI** coché et **Annotate with AI (automatic)** non coché, l'annotation automatique reste silencieuse ; avec seulement l'entrée automatique cochée, seule l'annotation automatique notifie.
- Une exécution qui se termine par une erreur notifie également (*… failed*), celle que vous arrêtez vous-même ne notifie pas.

La case active les notifications pour votre compte et rend actifs deux boutons. **Register this browser** demande au navigateur l'autorisation (à partir de ce clic) ; la ligne sous les boutons indique ce que dit ce navigateur : autorisé, bloqué (à autoriser dans les réglages du site du navigateur), ou pas encore demandé. L'autorisation appartient au navigateur, non à votre compte : chaque navigateur ou appareil où vous voulez des notifications doit être enregistré une fois ; la case et la liste sont les mêmes partout. **Send a test notification** en affiche une aussitôt, y compris pendant que la page est au premier plan.

Le champ **Filter** réduit la liste par nom, **Check shown** et **Uncheck shown** agissent sur les entrées affichées et laissent les autres telles quelles. Une notification n'apparaît que lorsque vous ne regardez pas la page (un autre onglet, ou la fenêtre n'est pas au premier plan) ; un clic dessus ramène la page.

Les notifications exigent une adresse `https://` ou `localhost` : sur une adresse `http://` simple d'un serveur de votre réseau, le navigateur ne les propose pas, et l'onglet le signale. Un onglet que le navigateur a mis en veille (souvent sur téléphone) ne peut pas non plus les afficher. L'onglet est enregistré avec **Save** des réglages, comme les autres.

<a id="phrase-watch-list"></a>

### Liste de surveillance des expressions

Votre [liste de surveillance des expressions](books/annotations.md#watching-for-phrases), que tous les livres utilisent (les dossiers et les livres y ajoutent les leurs), et les **annotation templates** dont ses entrées et les entrées des dossiers et des livres se servent : ce que dit la note d'une expression trouvée, son type et si elle est transmise au modèle. Enregistrée avec **Save** des réglages, comme les autres onglets.

![La liste de surveillance des expressions dans les réglages](../images/settings-phrase-watch.png)
<a id="api-keys"></a>

### Clés d'API

Clés pour les programmes et scripts qui utilisent Marginalia via son API HTTP (`/api/v1`) : un programme envoie la clé dans l'en-tête `Authorization: Bearer <key>` et fait ce que vous pourriez faire dans vos livres. Réservé aux administrateurs et aux utilisateurs ayant la permission [*API access*](administration/permissions.md) ; sans elle, l'onglet le signale.

![L'onglet des clés d'API](../images/settings-api-keys.png)

![Une nouvelle clé, affichée une seule fois](../images/api-key-created.png)

**New API key** demande un **Name** (ce qui utilise la clé, pour savoir laquelle révoquer), les **Access** et l'échéance **Expires** (après un nombre de jours, ou jamais si le champ est vide). Les droits (**Access**) sont une combinaison libre de :

| Accès | |
|---|---|
| **Read** | Vos livres et les commandes qui ne font que lire. |
| **Write** | Toutes les commandes, y compris celles qui modifient ou suppriment ; inclut *Read*. |
| **Update the installation** | Réservé aux administrateurs : la [mise à jour](administration/updates.md) de l'installation (son état, son journal, son lancement). |
| **Manage the extensions** | Réservé aux administrateurs : installer, mettre à jour, arrêter et supprimer des [extensions](administration/extensions.md). |

Chaque clé peut lire la version de Marginalia. Ne donnez à une clé que ce dont son programme a besoin : un script qui installe des plugins n'a besoin que de *Manage the extensions*, et une telle clé ne peut pas toucher à vos livres. La mise à jour et les extensions cessent de fonctionner pour une clé lorsque son propriétaire cesse d'être administrateur ; ses autres droits restent.

La clé est affichée une seule fois, juste après sa création : copiez-la à ce moment-là, car Marginalia n'en garde qu'une empreinte et ne peut pas la montrer de nouveau. La liste présente vos clés avec le début de chacune, sa date de création, son échéance et sa dernière utilisation ; le bouton corbeille **révoque** une clé, et les programmes qui l'utilisent perdent l'accès immédiatement. Une clé cesse aussi de fonctionner à son échéance, ou lorsqu'un administrateur lui retire la permission. Les clés sont enregistrées dès leur création ou leur révocation, et non par le **Save** des paramètres.

**La page de l'API** : ouvrez `/api/v1/` de votre Marginalia dans un navigateur (par exemple `https://marginalia.example.org/api/v1/`) pour voir chaque appel, ses arguments et ses réponses. Collez une clé dans **API key** en haut : la page liste alors aussi chaque commande que cette clé peut exécuter, avec ses arguments, et **Try it out** → **Execute** envoie l'appel avec cette clé et affiche la réponse. Les appels sont réels : une commande modifie votre livre, et une mise à jour redémarre le serveur. La page ne garde la clé que tant que l'onglet est ouvert, jamais sur le disque.

Pour les développeurs, les appels sont décrits dans le [manuel du développeur](../developer/api.md).

<a id="extension-settings"></a>

### Paramètres des extensions

Paramètres des [extensions](extensions/index.md) installées, une section par extension (par exemple *Reviewer Settings* et *SideQuery Settings*). Ils sont enregistrés avec le même bouton **Save**. L'onglet est vide lorsqu'aucune extension dotée de paramètres n'est installée.

![L'onglet Extension Settings avec les paramètres de Lore Generator](../images/settings-extensions.png)
