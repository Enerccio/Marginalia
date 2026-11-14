---
label: Exportateur d'instructions
order: 12
---

# Exportateur d'instructions

Copie dans le presse-papiers les instructions de l'histoire, avec le cadre de la scène, le personnage narrateur et les personnages présents.

<a id="how-it-works"></a>

## Fonctionnement

L'extension ajoute **Export instructions** au menu des paramètres (⚙) de l'[éditeur d'histoire](../books/story-editor.md).
Elle parcourt les parties de la branche active, de la première partie jusqu'à la fin actuelle de l'histoire, récupère les champs
à partir desquels chaque partie a été générée et les copie en un seul texte. Chaque champ rempli est écrit sous son nom suivi
de deux-points, son contenu figurant à la ligne suivante ; les parties sont séparées par une ligne vide, dans l'ordre de l'histoire :

![Export instructions in the settings menu](../../images/plugin-instructionexporter.png)

```
Scene Setting:
A harbour town at dawn
POV Character:
Mara
Present Characters:
Mara, the harbourmaster
Instructions:
Mara arrives and is questioned.

Scene Setting:
...
```

Les champs vides sont omis, de même que les parties dont aucun champ n'est rempli. Collez le texte où vous voulez : pour garder le plan
du livre, pour réutiliser les instructions dans un autre livre ou pour les transmettre à quelqu'un d'autre.

<a id="usage"></a>

## Utilisation

Aucune configuration n'est nécessaire. Ouvrez un livre et choisissez *Export instructions* dans le menu des paramètres ; une notification confirme
que le texte est dans le presse-papiers. Si aucun de ces champs n'est rempli sur la branche, rien n'est copié et un message vous le signale.

Seule la branche active est exportée : pour exporter une autre branche, commencez par y basculer.

!!!
Les navigateurs n'autorisent une page à écrire dans le presse-papiers que lorsqu'elle est ouverte en `https://` ou sur `localhost`. En `http://` simple sur une autre adresse, l'élément n'a aucun effet.
!!!
