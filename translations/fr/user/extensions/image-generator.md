---
label: Générateur d'images
order: 13
---

# Générateur d'images

Crée une image pour une partie de l'histoire. Le modèle rédige le prompt de l'image à partir de la partie, vous le modifiez, un moteur d'images dessine
l'image, vous relancez jusqu'à ce qu'elle convienne et vous l'acceptez : elle devient une image de la partie, comme celle que vous ajoutez à la main avec *Images* dans
le menu de la partie (elle est affichée dans l'histoire et dans les exports).

L'extension ne fabrique pas elle-même les images : elle communique avec un serveur **ComfyUI** ou avec un serveur utilisant l'API d'images **OpenAI** (également
parlée par stable-diffusion.cpp, LiteLLM et de nombreux services hébergés).

<a id="adding-a-backend"></a>

## Ajouter un moteur

*Paramètres → Paramètres de l'extension → Paramètres du Générateur d'images* liste les moteurs. Ajoutez-en un (+), remplissez-le puis enregistrez la page *Paramètres*.

| Paramètre | Ce qu'il fait |
|---|---|
| Nom, Type | Le nom affiché dans la fenêtre. **ComfyUI** ou **API d'images OpenAI**. |
| Adresse | L'adresse de base du serveur, par exemple `http://localhost:8188` pour ComfyUI. |
| Clé API | Envoyée comme jeton bearer. Elle est stockée chiffrée et n'est plus affichée ; saisissez une nouvelle clé pour la remplacer, ou **Supprimer la clé**. |
| Modèle | API d'images OpenAI uniquement : le `model` de la requête. |
| Workflow | ComfyUI uniquement : un workflow enregistré dans ComfyUI avec **Save (API Format)**. Écrivez `{{prompt}}`, `{{negative}}`, `{{seed}}`, `{{width}}` et `{{height}}` aux endroits voulus. |
| Le serveur est sur cette machine ou sur mon réseau | Autorise les adresses du réseau local. Laissez-le désactivé pour un serveur dans le cloud : il ne peut alors pas être amené à appeler votre propre machine. |
| Largeur, Hauteur, Prompt négatif | Ce avec quoi la fenêtre démarre (vous pouvez les modifier là pour une seule image). |
| Délai d'attente | Le temps d'attente pour une image. |
| Libérer après inactivité | Voir [Un seul GPU](#one-gpu-for-the-model-and-the-images). |

![Un moteur dans les paramètres](../../images/plugin-imagegenerator-settings.png)

**Tester la connexion** vérifie l'adresse et la clé.

Le panneau comporte aussi le **Style** placé devant chaque prompt (l'aspect du livre, par exemple *aquarelle, lumière douce*) et les
**Instructions pour le rédacteur de prompt**, le texte donné au modèle pour rédiger le prompt à partir d'une partie. Vide ou inchangé, c'est celui intégré qui est utilisé.

<a id="making-an-image"></a>

## Créer une image

![Générer une image dans le menu d'une partie](../../images/plugin-imagegenerator-menu.png)

1. Choisissez **Générer une image** dans le menu d'une partie. Le modèle de la fenêtre (celui du livre par défaut, le dernier choix est mémorisé)
   commence à rédiger le prompt de la partie ; vous pouvez l'arrêter avec **Abandonner**, modifier le prompt, ou appuyer sur **Rédiger le prompt avec le modèle** pour le réécrire. Avant de le réécrire, vous pouvez renseigner :
   - **Envoyer les entrées du lorebook** - les entrées du lorebook du livre (et de ses sous-lorebooks) sont envoyées au modèle comme lors d'une génération, afin qu'il sache à quoi ressemblent les personnages et les lieux. Une entrée est envoyée lorsque ses filtres sont trouvés dans la partie : son texte, ses instructions,
     le cadre de la scène, le point de vue et les personnages présents, ou dans vos *Détails*. Les entrées toujours actives sont aussi incluses. Seules celles qui tiennent dans le contexte du modèle sont envoyées (un avis indique combien ont été écartées). Le choix est mémorisé.
   - **Détails** - ce que l'image doit montrer ou la manière dont elle doit apparaître (*de nuit, gros plan sur son visage, la robe rouge*) ; le rédacteur de prompt en tient compte. Il ne s'applique qu'à cette fenêtre.
2. Choisissez le moteur et appuyez sur **Générer**. Sous *Taille et prompt négatif*, vous pouvez modifier la taille et le prompt négatif pour cette fenêtre.
3. L'image apparaît. **Relancer** en produit une autre avec une nouvelle graine, **Rejeter** l'écarte, **Accepter** l'ajoute à la partie et ferme la fenêtre. **Abandonner** arrête une image en cours de création (ComfyUI reçoit l'ordre d'interrompre la tâche).

![La fenêtre avec le prompt rédigé par le modèle](../../images/plugin-imagegenerator-prompt.png)

![L'image, avec Accepter, Rejeter et Relancer](../../images/plugin-imagegenerator.png)

La partie ouverte affiche la nouvelle image sous son texte immédiatement.

<a id="one-gpu-for-the-model-and-the-images"></a>

## Un seul GPU pour le modèle et les images

Avec un petit GPU, le modèle de langage et le modèle d'images ne peuvent pas être chargés ensemble. L'extension ne gère pas la mémoire ;
chaque moteur peut avoir deux listes de requêtes que vous configurez :

- **Avant la première image** - exécutée une fois, au moment où la fenêtre commence à utiliser le moteur. Par exemple une requête vers votre proxy qui décharge le modèle de langage et charge le modèle d'images.
- **Après la dernière image** - exécutée lorsque vous acceptez, rejetez ou fermez la fenêtre, ou lorsque les secondes de **Libérer après inactivité** se sont écoulées depuis la dernière image (ainsi une fenêtre oubliée ne garde pas le GPU occupé ; 0 désactive cette option). Pour ComfyUI, cela peut être `POST <adresse>/free` avec le corps `{"unload_models": true, "free_memory": true}` ; pour un proxy, une requête qui recharge le modèle de langage.

Avec `llama-server` en mode routeur derrière LiteLLM, la requête *avant* peut décharger le modèle de langage via le proxy : voir
[Libérer le GPU pour un autre modèle](../prompt-cache.md#freeing-the-gpu-for-another-model).

Les deux moments prennent une **liste de requêtes**, exécutées les unes après les autres (**Ajouter une requête** en ajoute une, la corbeille en supprime une). Un changement de modèle en nécessite souvent plusieurs : décharger le modèle de langage, puis démarrer le serveur d'images, et, une fois les images terminées, l'arrêter. Chaque requête a une adresse, une méthode
(`POST` par défaut), des en-têtes (`Nom : valeur`, un par ligne), un corps, un **délai d'attente** (vide : 60 secondes, 600 au maximum ; donnez davantage à un serveur qui charge un gros modèle) et **Attendre ensuite** (secondes de pause après la réponse, pour un serveur encore occupé après avoir répondu oui). Les en-têtes sont stockés chiffrés, comme la clé API (une requête porte souvent `Authorization: Bearer ...`). Le moteur reste réservé pendant que vous relancez, ainsi seule la première image paie le changement de modèle. Si un proxy placé devant les modèles les échange selon le nom du modèle, laissez les deux listes vides.

Lorsqu'une requête de la liste *avant* échoue, les suivantes ne sont pas exécutées et l'image s'arrête (le message indique la requête en échec : *Requête 2
sur 3 : ...*), afin que rien ne soit chargé sur un GPU encore plein. La liste *après* est exécutée **entièrement** quoi qu'il arrive à l'une de ses requêtes (chaque serveur doit être arrêté), et le premier échec est affiché comme avertissement. Un moteur enregistré avec la requête unique d'une version antérieure l'affiche comme première requête de la liste.

**Plusieurs personnes sur un même GPU.** Tous ceux dont le moteur a la même adresse de serveur (`http://hôte:port`) passent par une même porte : la requête *avant* est exécutée pour la première fenêtre qui démarre et la requête *après* lorsque la dernière est terminée, si bien que fermer une fenêtre ne décharge pas le modèle sous l'image d'une autre, et les images sont produites une à la fois (une seconde fenêtre attend son tour).
Les requêtes sont celles de l'utilisateur arrivé en premier et en dernier.

Un moteur peut aussi indiquer le GPU sur lequel il tourne (**GPU (nom)**). Les moteurs qui indiquent le même GPU s'attendent mutuellement, même si leurs adresses diffèrent, y compris les moteurs de l'[Audiobook](audiobook.md) (un serveur de synthèse vocale et un serveur d'images sur une même carte) : un utilisateur dont le moteur a besoin d'un autre modèle attend que les autres aient fini, puis exécute ses propres requêtes, au lieu de rejoindre un chargement qui ne le concerne pas. Laissez-le vide et seules les requêtes vers la même adresse s'attendent.

<a id="what-is-not-done"></a>

## Ce qui n'est pas fait

- Les moteurs sont propres à chaque utilisateur, et non à chaque livre.
