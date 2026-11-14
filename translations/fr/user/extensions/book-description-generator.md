---
label: Générateur de description du livre
order: 21
---

# Générateur de description du livre

Laisse le modèle rédiger le **nom** et la **description** d'un livre à partir de son histoire. Tu choisis la partie de l'histoire
qu'il lit, et tu copies ce qu'il a écrit dans le livre.

<a id="opening-it"></a>

## Ouvrir le générateur

Une fois l'extension installée, l'onglet **À propos** de la fenêtre du livre affiche un bouton **Générer le nom et la description** à
la fin. Il fonctionne aussi dans la [disposition téléphone](../phone.md#extensions). La boîte de dialogue affiche les options à
gauche et le résultat à droite.

![La fenêtre qui génère le nom et la description](../../images/plugin-book-description.png)

| Option | |
|---|---|
| **Modèle**, **Protocoles** | Ce avec quoi la génération s'exécute. Elle démarre avec le modèle et le protocole du livre, sinon tes valeurs par défaut dans *Paramètres* (voir [Compte et paramètres](../account.md)) ; le choix ne vaut que pour cette génération. Sans modèle, elle affiche seulement un avertissement. |
| **Depuis le message**, **Jusqu'au message** | La plage de l'histoire, en numéros de messages contenant du texte de la branche active. Le début du message est affiché sous le champ, pour que tu saches lequel c'est. Elle démarre avec toute l'histoire. |
| **Utiliser les résumés** | Un [résumé](../books/summaries.md) remplace les messages qu'il couvre, lorsqu'ils sont tous dans la plage ; cela rend une longue plage bien moins coûteuse. Un résumé qui dépasse la plage n'est pas utilisé, pas plus qu'un résumé qui ne décrit plus l'histoire (les messages sont alors utilisés à la place). Retenu pour la prochaine fois. |
| **Format de la réponse** | *Automatique* demande du JSON avec un schéma lorsque le service du modèle sait le gérer, et du Markdown dans le cas contraire. *JSON* impose un schéma. *JSON sans schéma* décrit le JSON dans le prompt à la place, pour un serveur qui ne sait pas combiner un schéma avec la réflexion du modèle. *Markdown* demande le nom en premier titre et la description en dessous. Quoi que le modèle écrive, il est lu comme du JSON ou du Markdown, donc un modèle qui ignore la consigne est quand même compris. Retenu pour la prochaine fois. |

Sous les options, un compteur indique combien de tokens représente la plage avec le prompt, combien le modèle en accepte, et si
l'histoire est envoyée telle quelle ou doit d'abord être transformée en notes.

**Générer** lance le modèle, le même bouton l'arrête. Le nom et la description apparaissent à droite, chacun avec son propre bouton
**Copier dans le livre** : il place le texte dans le champ de l'onglet À propos et enregistre le livre immédiatement (le titre de la
fenêtre du livre change aussi). Si la réponse ne peut pas être lue comme un nom et une description, la boîte de dialogue affiche le
texte écrit par le modèle.

<a id="a-story-that-is-too-long"></a>

## Une histoire trop longue

Lorsque la plage est plus grande que ce que le modèle accepte, l'histoire est découpée en parties qui tiennent, dans l'ordre, et le
modèle écrit des **notes** sur chacune d'elles (elles apparaissent sous *Notes* au fur et à mesure). La description est alors
rédigée à partir des notes au lieu de l'histoire. Les notes encore trop longues sont découpées et annotées à nouveau, jusqu'à quatre
tours. Un message unique plus grand qu'une partie est coupé à un paragraphe, une ligne ou une phrase.

!!!
Si ni le modèle ni le protocole n'ont de taille de contexte définie, l'extension ne peut pas savoir ce qui est trop long et envoie
toujours l'histoire telle quelle.
!!!

## Prompts

Il y a deux prompts : celui qui rédige le nom et la description, et celui qui rédige les notes d'une partie d'une histoire longue.
Ce sont des [paramètres hérités](../books/prompts.md#inherited-settings) : un livre utilise les siens, sinon le dossier le plus
proche au-dessus de lui qui en définit un, sinon les tiens, sinon le texte intégré.

| Prompt | Variables |
|---|---|
| **Prompt de description** | `{{story}}` (l'histoire, ou les notes qui en sont faites), `{{fromNotes}}`, `{{bookName}}`, `{{bookDescription}}` |
| **Prompt de notes** | `{{story}}` (la partie), `{{part}}`, `{{parts}}`, `{{bookName}}`, `{{bookDescription}}` |

Règle-les dans l'onglet *Prompts* du livre, sous *Paramètres des extensions*, dans la boîte de dialogue d'un dossier, ou pour tous tes
livres dans *Paramètres → Paramètres des extensions → Book Description Generator*. Les instructions qui indiquent au modèle comment
écrire la réponse (JSON ou Markdown) sont ajoutées après le prompt de description par l'extension, donc ton prompt n'a qu'à dire ce
qu'il faut écrire.
