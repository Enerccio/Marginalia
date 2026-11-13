---
label: Notes de l'histoire
order: 27
---

# Notes de l'histoire

Un petit wiki pour votre livre, dans la barre latérale : des pages pour l'intrigue que vous préparez, une idée que vous n'êtes pas encore prêt à utiliser, les règles d'un système de magie, ce que le méchant veut vraiment. Ce sont vos propres réflexions, dans vos propres mots, avec des mots-clés et des liens entre les pages. Contrairement à la [Note de l'auteur](authors-note.md), qui est un texte unique envoyé avec chaque génération, les notes ne sont pas envoyées au modèle en tant que telles. L'[assistant de plan](arc-outline.md#developing-the-plot-in-a-conversation) et les agents peuvent les lire, et les modifier si vous les y autorisez.

<a id="the-notes-tab"></a>

## L'onglet Notes

La barre latérale gauche de l'[éditeur de l'histoire](../books/story-editor.md) comporte un onglet **Notes** à côté de *Plan*. Il liste les pages du livre, classées par titre. Au-dessus de la liste se trouvent **Add** (une nouvelle page), un filtre par **tag** (affiché dès qu'une page a un mot-clé) et une recherche sur les titres, les mots-clés et le texte des pages.

Chaque page affiche son titre, la première ligne de son texte et ses mots-clés. Cliquez sur la page, ou sur le crayon, pour l'ouvrir ; la corbeille la supprime après une confirmation. Deux petits repères peuvent accompagner le titre : un œil barré (la page est [masquée aux assistants](#what-assistants-can-see)) et une baguette (la dernière modification vient d'une commande, et non de vous).

![L'onglet Notes : une page masquée aux assistants (l'œil) et une page à laquelle l'assistant de plan a ajouté du texte (la baguette)](../../images/plugin-storynotes-tab.png)

<a id="a-page"></a>

## Une page

La boîte de dialogue d'une page contient :

| | |
|---|---|
| **Title** | Obligatoire, unique dans le livre (la casse n'a pas d'importance), sans crochets ni barre verticale (`|`). C'est ce à quoi les autres pages renvoient. |
| **Tags** | Séparés par des virgules. Ils filtrent la liste. |
| **Text** | Markdown. **Write** et **Preview** basculent entre le texte et son rendu. `[[Title]]` dans le texte renvoie à la page portant ce titre ; `[[Title|autres mots]]` renvoie aussi à cette page, mais affiche *autres mots* dans l'aperçu (`[[Language barrier|how they talk]]`). |
| **Visible to assistants** | Activé par défaut. Voir ci-dessous. |

Sous le texte, la boîte de dialogue liste les pages vers lesquelles le texte **renvoie** : un lien vers une page existante l'ouvre, un lien vers une page inexistante propose **Create** et démarre une nouvelle page portant ce titre. En dessous figurent les pages qui **renvoient ici**. Renommer une page modifie les `[[liens]]` vers elle dans les autres pages (les mots affichés par un lien restent inchangés).

![Une page avec les pages vers lesquelles elle renvoie et la page qui renvoie vers elle](../../images/plugin-storynotes-page.png)

**Preview** affiche le texte en Markdown, chaque `[[lien]]` étant un lien cliquable : cliquez dessus pour ouvrir cette page (un lien vers une page inexistante en démarre une nouvelle portant ce titre) :

![L'aperçu de la même page](../../images/plugin-storynotes-preview.png)

Rien n'est enregistré avant **Save** ; fermer une page modifiée demande confirmation avant d'abandonner les changements. Si la page a été modifiée par une commande pendant que vous la rédigiez, il vous est demandé si votre version doit l'écraser. **Ctrl/Cmd+Enter** enregistre. Les pages sont stockées avec le livre, elles figurent donc dans ses sauvegardes et ses copies.

<a id="what-assistants-can-see"></a>

## Ce que voient les assistants

Les « assistants » désignent les commandes qu'apportent les notes, que utilisent les agents, les macros et l'assistant de plan. Une page dont **Visible to assistants** est désactivé n'existe pas pour eux : elle n'est ni listée, ni trouvée, ni lue, ni modifiée, et un titre que seule elle possède est refusé lorsqu'ils tentent de la créer. Désactivez cette option pour ce que vous seul devez voir. Les pages que vous avez masquées restent présentes dans vos sauvegardes.

Les commandes (pour la barre de commandes et pour les agents ; le titre est l'adresse d'une page) :

| Commande | |
|---|---|
| `/list-notes [<tag>]`, `/get-note <note>`, `/search-notes <query>` | Les pages, une page avec son texte, ses liens et les pages qui y renvoient, et une recherche. |
| `/create-note <title> [<content> [<tags>]]` | Ajoute une page. |
| `/append-to-note <note> <text>` | Ajoute un paragraphe à la fin d'une page. |
| `/update-note <note> [<title> [<content> [<tags>]]]`, `/edit-note <note> <find> <replace> [<all>]` | Modifient une page, ou remplacent un passage de son texte : `find` doit figurer dans la page exactement tel qu'écrit (la casse compte) et, sauf si `all` vaut `true`, une seule fois ; avec `all`, chaque occurrence est remplacée, pour un nom ou un terme à changer dans toute la page. La réponse indique combien de passages ont été remplacés. Ces commandes modifient du texte existant : elles sont donc *destructives*, et un agent configuré pour demander vous interroge d'abord. |
| `/delete-note <note>` | Supprime une page, après une confirmation. |
| `/note-text <note>` | Le texte d'une page, destiné aux macros : {%{`{{command::note-text::Magic}}`}%} l'insère dans un modèle. Vide pour une page inexistante ou masquée. |

Une page modifiée par une commande est marquée de la baguette jusqu'à ce que vous l'enregistriez vous-même.

<a id="with-the-plot-wizard"></a>

## Avec l'assistant de plan

Lorsque cette extension est installée, l'[assistant de plan](arc-outline.md#developing-the-plot-in-a-conversation) du Plan d'arc dispose d'une option **Send the notes** (les titres avec leurs premières lignes, le texte complet, ou rien) et d'un groupe d'outils **Notes** : le modèle peut chercher et lire les pages, suivre leurs liens, ajouter une page ou compléter une page lorsque vous lui demandez de retenir quelque chose, et modifier du texte dans une page. La modification du texte d'une page est soumise à votre accord tant que *Ask me before it changes or deletes something* est activé ; l'assistant ne peut pas supprimer de page. Voir l'option et le groupe dans le tableau de l'assistant.

![L'assistant de plan avec les outils Notes : il a lu une page et y a ajouté la question ouverte](../../images/plugin-storynotes-wizard.png)

<a id="other-extensions"></a>

## Autres extensions

L'extension propose deux sources de texte aux autres extensions (le [Side Query](side-query.md) peut les joindre depuis son bouton de branchement) : **Story notes (titles)** et **Story notes (all)**. Les deux excluent les pages masquées.
