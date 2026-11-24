---
label: Générateur d'images
order: 13
---

# Générateur d'images

Crée une image pour une partie de l'histoire. Le modèle écrit le prompt de l'image à partir de la partie, vous le modifiez, un backend d'images dessine l'image, vous relancez jusqu'à ce qu'elle vous convienne et vous l'acceptez : elle devient une image de la partie, comme celle que vous ajoutez à la main avec *Images* dans le menu de la partie (elle est affichée dans l'histoire et dans les exports).

L'extension ne fabrique pas elle-même les images : elle communique avec un serveur **ComfyUI** ou avec un serveur doté de l'**API d'images OpenAI** (également parlée par stable-diffusion.cpp, LiteLLM et de nombreux services hébergés).

<a id="adding-a-backend"></a>

## Ajouter un backend

*Paramètres → Extension Settings → Image Generator Settings* liste les backends. Ajoutez-en un (+), remplissez-le et enregistrez la page *Paramètres*.

| Réglage | Ce qu'il fait |
|---|---|
| Name, Type | Le nom affiché dans la fenêtre. **ComfyUI** ou **OpenAI images API**. |
| Address | L'adresse de base du serveur, par exemple `http://localhost:8188` pour ComfyUI. |
| Api key | Envoyée comme jeton bearer au serveur d'images. Elle est stockée chiffrée et n'est plus affichée ; saisissez-en une nouvelle pour la remplacer, ou **Remove the key**. |
| Key of the requests around the images | Envoyée comme `Authorization: Bearer` avec chaque requête [avant et après les images](#one-gpu-for-the-model-and-the-images) qui n'a pas d'en-tête `Authorization` propre, par exemple la clé d'un proxy LiteLLM. Stockée chiffrée. Vide : la clé d'API est utilisée. |
| Model | API d'images OpenAI uniquement : le `model` de la requête. |
| Workflow | ComfyUI uniquement : un workflow enregistré dans ComfyUI avec **Save (API Format)**. Écrivez `{{prompt}}`, `{{negative}}`, `{{seed}}`, `{{width}}` et `{{height}}` là où ils doivent aller, et `{{lora}}` / `{{lora_strength}}` dans un loader LoRA pour [choisir le LoRA dans la fenêtre](#choosing-a-lora). |
| The server is on this machine or on my network | Autorise les adresses du réseau local. Laissez-le désactivé pour un serveur dans le cloud : on ne peut alors pas lui faire appeler votre propre machine. |
| Width, Height | Ce avec quoi la fenêtre démarre (vous pouvez les changer là pour une seule image). |
| Prompt form | Comment le modèle de langage écrit le prompt pour ce backend, selon son modèle d'images : **Mots-clés** (courtes expressions séparées par des virgules, pour SDXL, Pony, Illustrious), **Descriptive sentences** (un paragraphe, pour Flux, Krea, Qwen-Image, Z-Image) ou **Free text**, vos propres consignes pour le rédacteur de prompts (elles partent de celle qui a été choisie). Un backend créé avant ce réglage écrit des mots-clés, ou utilise en texte libre les consignes que vous aviez alors réglées. Les consignes intégrées sont dans votre [langue des prompts](../account.md#prompt-language) et demandent le prompt d'image en anglais, que les modèles d'images lisent le mieux. |
| Timeout | Durée d'attente pour une image. |
| GPU lock | Le [verrou GPU](../administration/gpus.md) du GPU sur lequel tourne le serveur, lorsqu'il le partage avec le modèle de langage ou d'autres serveurs ; voir [Un seul GPU](#one-gpu-for-the-model-and-the-images). Affiché seulement si l'administrateur a configuré les verrous GPU. |

![Un backend dans les paramètres](../../images/plugin-imagegenerator-settings.png)

**Test connection** vérifie l'adresse et la clé.

Le panneau contient aussi deux textes qu'un livre peut modifier :

- **Style** - placé devant chaque prompt : l'aspect des images, par exemple *watercolor, soft light*. Le rédacteur de prompts en est informé également, pour que le prompt ne décrive pas un autre style.
- **Negative prompt** - ce que les images ne doivent pas montrer. Un modèle turbo qui fonctionne sans guidage (CFG 1) l'ignore. Le negative prompt qu'un backend avait auparavant est repris ici la première fois que vous ouvrez le panneau.

Ce que vous réglez dans le panneau est votre valeur par défaut. Un livre (son onglet *Prompts*) ou un [dossier](../books/folders.md) peut définir les siens sous *Extension Settings → Image Generator* ; un livre utilise les siens, sinon ceux du dossier le plus proche, sinon les vôtres.

<a id="making-an-image"></a>

## Créer une image

![Générer une image dans le menu d'une partie](../../images/plugin-imagegenerator-menu.png)

1. Choisissez **Generate image** dans le menu d'une partie. Rien n'est encore envoyé à un modèle : remplissez d'abord ce que le rédacteur de prompts doit suivre, puis appuyez sur **Write the prompt with the model** (ou <kbd>Ctrl+Enter</kbd> dans *Details*). Le modèle de la fenêtre (celui du livre par défaut ; le dernier choix est mémorisé) écrit le prompt de la partie ; vous pouvez l'interrompre avec **Abort**, modifier le prompt, l'écrire vous-même ou le réécrire. Ces réglages ne s'appliquent qu'à l'écriture du prompt, pas à l'image elle-même :
   - **Send lorebook entries** - les entrées du lorebook du livre (et de ses sous-lorebooks) sont envoyées au modèle comme lors d'une génération, afin qu'il sache à quoi ressemblent les personnages et les lieux. Une entrée est envoyée lorsque ses filtres se retrouvent dans la partie : son texte, ses instructions, le cadre de la scène, le point de vue et les personnages présents, ou dans vos *Details*. Les entrées toujours actives sont aussi envoyées. Seul le nombre d'entrées qui tient dans le contexte du modèle est envoyé (un avertissement indique combien ont été laissées de côté). Le choix est mémorisé.
   - **Details** - ce que l'image doit montrer ou la façon dont elle doit apparaître (*at night, close up on her face, the red dress*) ; le rédacteur de prompts suit ces indications. Elles ne valent que pour cette fenêtre.
2. Choisissez le backend et appuyez sur **Générer**. L'image est créée à partir du **Prompt**, précédé du **Style**, et du **Negative prompt** ; les deux commencent avec ceux du livre (ou votre valeur par défaut), et une modification ne vaut que pour cette fenêtre (le style est aussi indiqué au rédacteur de prompts). La taille commence avec celle du backend et peut être changée pour cette fenêtre.
3. L'image apparaît. **Reroll** en crée une autre avec une nouvelle graine, **Discard** la supprime, **Accept** l'ajoute à la partie et ferme la fenêtre. **Abort** arrête une image en cours de création (ComfyUI reçoit l'ordre d'interrompre la tâche).

![La fenêtre avec le prompt écrit par le modèle](../../images/plugin-imagegenerator-prompt.png)

![L'image, avec Accept, Discard et Reroll](../../images/plugin-imagegenerator.png)

La partie ouverte affiche aussitôt la nouvelle image sous son texte.

La fenêtre garde en mémoire ce que vous avez utilisé pour chaque partie : les détails, le style, le negative prompt, le prompt et la taille sont conservés avec la partie lorsque le prompt a été écrit et lorsque vous générez, et la prochaine fois que vous ouvrez la fenêtre sur cette partie, elle démarre avec ces valeurs. Une partie pour laquelle vous n'avez jamais créé d'image démarre avec le style et le negative prompt du livre.

<a id="choosing-a-lora"></a>

## Choisir un LoRA

Un LoRA est un petit module complémentaire du modèle d'images, généralement pour un style. Avec ComfyUI, vous pouvez le choisir dans la fenêtre au lieu d'enregistrer de nouveau le workflow pour chacun :

1. Dans le workflow exporté, écrivez `{{lora}}` comme `lora_name` du loader LoRA (*Load LoRA*, `LoraLoaderModelOnly`, ou `LoraLoader`), et `{{lora_strength}}` comme sa force (`strength_model` ; `strength_clip` aussi pour `LoraLoader`).
2. La fenêtre affiche alors **LoRA** avec les fichiers LoRA du serveur (son dossier `models/loras`) et **LoRA strength**. Vous pouvez aussi saisir le nom d'un fichier que la liste n'affiche pas.
3. **Aucun** (pas de LoRA) retire les loaders LoRA du workflow, de sorte que l'image est créée sans eux.

Le LoRA et la force avec lesquels vous générez sont mémorisés pour le backend et sont là la prochaine fois. Il en va de même pour la liste : un serveur qui partage le GPU est souvent éteint à l'ouverture de la fenêtre (les requêtes qui précèdent la première image le démarrent), donc la fenêtre propose les LoRA listés en dernier par le serveur et demande à nouveau après la première image. Ce n'est que lorsqu'elle n'a jamais pu interroger le serveur qu'un avertissement indique que les LoRA n'ont pas pu être lus ; vous pouvez tout de même saisir le nom d'un fichier.
<a id="one-gpu-for-the-model-and-the-images"></a>

## Un seul GPU pour le modèle et les images

Avec un petit GPU, le modèle de langage et le modèle d'images ne peuvent pas être chargés ensemble. L'administrateur configure cela une seule fois, pour
tous les modèles et serveurs installés sur ce GPU, dans [Administration → GPU](../administration/gpus.md) : un **Verrou GPU** pour le modèle de langage et un pour
le serveur d'images, chacun avec les requêtes qui le chargent et celles qui libèrent le GPU. Choisissez le verrou du serveur d'images dans **Verrou GPU** du backend (et celui du modèle de langage dans ses [réglages IA](../inference-providers.md)).

Chaque image attend ensuite son tour pour utiliser le GPU :

- Si le GPU est déjà chargé avec le serveur d'images, l'image démarre aussitôt ; rien n'est déchargé entre deux relances.
- S'il contient le modèle de langage (ou un autre serveur), l'image attend que le modèle ait répondu, que le verrou du modèle libère le GPU, puis que le verrou du serveur d'images le charge.
- Rien n'est conservé entre les images : fermer ou replier la fenêtre, ou la laisser ouverte, ne réserve aucun GPU. La requête suivante adressée au modèle de langage libère d'elle-même le GPU du serveur d'images, lequel reste chargé jusque-là.

La fenêtre affiche *Generating the image* pendant qu'elle attend son tour.

**Requêtes avant et après chaque image.** Un backend peut aussi avoir ses propres deux listes de requêtes, exécutées à l'intérieur du verrou autour de chaque
image : **Before each image** et **After each image**. La libération du GPU relève du verrou GPU ; utilisez ces listes pour tout autre besoin du
serveur autour d'une image. Les requêtes de chaque liste sont exécutées les unes après les autres (**Add a request** en ajoute une, la corbeille en supprime une). Chaque
requête a une adresse, une méthode (`POST` par défaut), des en-têtes (`Name: value`, une par ligne), un corps, un **timeout** (vide : 60 secondes,
600 au maximum) et **Wait afterwards** (nombre de secondes de pause après la réponse). Les en-têtes sont enregistrés chiffrés, comme la clé API.

Lorsqu'une requête de la liste *before* échoue, les suivantes ne sont pas envoyées et l'image s'arrête (le message indique quelle requête a échoué : *Request 2
of 3: ...*). La liste *after* est exécutée **entièrement** quoi qu'il arrive à l'une de ses requêtes, et un échec est consigné dans le journal
de l'extension ; l'image est conservée. Un backend enregistré avec la requête unique d'une version antérieure apparaît comme la première requête de la
liste.

Les backends enregistrés avant l'existence des verrous GPU perdent leurs réglages **GPU (name)** et **Release after idle** : choisissez plutôt un verrou GPU, et déplacez les requêtes qui libéraient le GPU (déchargement du modèle de langage, `POST <address>/free` de ComfyUI) dans les étapes des verrous.

<a id="what-is-not-done"></a>

## Ce qui n'est pas fait

- Les backends sont propres à chaque utilisateur, et non à chaque livre.
