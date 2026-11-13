---
label: Note de l'auteur
order: 23
---

# Note de l'auteur

Une note destinée au modèle, envoyée avec chaque génération du livre : le ton souhaité à ce moment, le sujet du chapitre
en cours, un rappel que le modèle oublie sans cesse. Contrairement aux instructions d'un tour, la note reste en place
jusqu'à ce que vous la modifiiez.

<a id="the-authors-note-tab"></a>

## L'onglet Note de l'auteur

La barre latérale gauche de l'[éditeur d'histoire](../books/story-editor.md) comporte un onglet **Note de l'auteur**, à côté de *Plan*.
Les modifications sont enregistrées dans le livre au fur et à mesure que vous tapez et sont utilisées par la génération suivante.

![L'onglet Note de l'auteur](../../images/plugin-authorsnote.png)

| Champ | |
|---|---|
| **Insérer dans le prompt** | Active ou désactive la note sans la supprimer. |
| **Profondeur d'insertion** | Où la note est placée, comptée en messages depuis la fin du prompt : `0` - après les instructions du tour (le tout dernier message), `1` - juste avant elles, `2` - avant la dernière partie de l'histoire, et ainsi de suite. Une grande profondeur place la note juste après le prompt système. |
| **Rôle** | Envoie la note comme message *Système*, *Utilisateur* ou *Assistant*. |
| **Note de l'auteur** | Le texte envoyé au modèle. Une note vide n'est pas envoyée. Sous le champ, une estimation de sa taille en tokens. |
| **Note de l'auteur (privée)** | Notes pour vous-même : plans, rappels. Jamais envoyées au modèle. |

!!!
Plus la note est proche de la fin du prompt, plus son effet est fort. Une profondeur `1` avec le rôle *Système* est un bon point de départ.
La note n'est pas traitée comme un [modèle](../templates/macros.md) : les macros sont envoyées telles qu'elles sont écrites.
!!!

<a id="how-the-prompt-looks"></a>

## À quoi ressemble le prompt

Une génération envoie d'abord le prompt système, puis l'histoire sous forme de conversation - chaque partie comme une réponse
du modèle à *Générer l'histoire* - et enfin les instructions du tour. Avec une profondeur `1`, la note est insérée juste
avant les instructions :

1. prompt système (modèle, lore, résumés)
2. parties de l'histoire…
3. **la Note de l'auteur**
4. les instructions du tour

Les tokens de la note sont pris sur la place réservée à l'histoire : une note plus longue signifie moins de parties de
l'histoire dans le prompt. Une note qui ne laisse même pas la place au reste du prompt arrête la génération avec
*Limite de contexte insuffisante*.

<a id="the-note-as-a-macro"></a>

## La note sous forme de macro

Au lieu de l'insertion automatique (ou en plus), vous pouvez placer la note vous-même avec la macro
[{%{`{{command::authors-note}}`}%}](../templates/macros.md#commands) : mettez-la dans le modèle principal, le prompt utilisateur ou une
entrée de lorebook, et elle affiche le texte de la note à l'endroit de votre choix dans le prompt. Elle affiche la note telle
qu'elle est stockée actuellement, que **Insérer dans le prompt** soit activé ou non ; désactivez donc cette option lorsque vous
placez la note par macro, sinon le modèle la recevra deux fois. La profondeur et le rôle ne s'appliquent pas à la macro (la note
fait partie du message qui contient la macro), et une note vide n'affiche rien.

<a id="storage"></a>

## Stockage

La note est stockée avec le livre ; elle fait donc partie des [sauvegardes du livre](../books/backups.md) et des copies.
