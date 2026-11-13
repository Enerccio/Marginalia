---
label: Lecture à voix haute
order: 26
---

# Lecture à voix haute

Lit un livre publié à voix haute dans la [visionneuse](../books/publishing.md), avec la voix de votre appareil.

<a id="using-it"></a>

## Utilisation

Le lecteur dispose d'une petite commande à côté du bouton de style, en haut à droite :

- **Lire** commence à la partie affichée à l'écran, ou à la partie où commence votre sélection de texte. La partie lue est
  mise en surbrillance et amenée à l'écran, et les parties suivantes sont chargées au fil de la lecture.
- **Pause** et **Arrêter**.
- La **roue dentée** ouvre les réglages : la **Voix** (les voix de votre navigateur, celles de la langue du livre en premier ;
  la valeur par défaut est la meilleure voix pour cette langue), la **Vitesse** et la **Hauteur**.

<a id="good-to-know"></a>

## Bon à savoir

- La parole est produite par votre navigateur (`speechSynthesis`). Rien n'est envoyé au serveur, et les réglages sont conservés
  dans le navigateur, ils sont donc propres à cet appareil et à ce navigateur. Les voix et leur qualité en dépendent.
- Ce qui est lu est le texte de la page : la parole traduite des traducteurs [Conlang](conlang-translator.md) et
  [Langue réelle](real-language-translator.md) est lue telle que le livre la présente.
- La pause ne fonctionne pas sur tous les téléphones ; là, la lecture s'arrête à l'endroit où elle se trouvait.
- Avec l'extension [Livre audio](audiobook.md#a-part-without-audio), la voix du navigateur peut lire les parties qui n'ont pas encore d'audio, lorsque vous appuyez sur le bouton que son lecteur propose à cet endroit.
- Quitter le livre arrête la lecture. La commande est masquée lorsque le navigateur ne peut pas parler.
