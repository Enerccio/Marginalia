---
label: Libro de ejemplo
order: 80
---

# Libro de ejemplo

*Unregistered* es un libro terminado para abrir, leer y desmontar: unas 100 partes (unas 99 000 palabras) de una sola historia, con todo lo que dejan tras de sí las funciones de Marginalia. Es una buena forma de ver cómo es un libro largo en el [editor de la historia](../books/story-editor.md), los [resúmenes](../books/summaries.md), las [anotaciones](../books/annotations.md) y las [estadísticas](../books/statistics.md), o de probar las extensiones con algo que ya existe.

> Un guía de bodegas checo que aprendió japonés leyendo novelas y una enfermera japonesa de vacaciones atraviesan un arco tapiado en una bodega de Praga y despiertan en un valle fluvial gobernado por un imperio que habla vrakht.
> Ninguno de los dos conoce el idioma, y un tasador del impuesto de vida ha encontrado a dos personas que no figuran en su registro.

<a id="what-is-in-it"></a>

## Qué contiene

| | |
|---|---|
| **La historia** | 100 partes en la línea principal y una rama lateral de 3 partes, en 32 capítulos (los títulos están en el texto). |
| **Resúmenes** | Uno para cada capítulo, de cualquier longitud (el último capítulo de cada 25 partes se deja entero, tal como lo lee el modelo en bruto), y los metarresúmenes que fusionan los más antiguos, en el formato compacto del [prompt de resumen](../books/summaries.md) predeterminado. |
| **Anotaciones** | Unas 450 notas de revisión (continuidad, estilo, lore, punto de vista, ritmo) que propuso la propia función *Anotar con IA* del libro y que la reescritura atendió; cada parte tiene además sus versiones (360 en total, ver [historial de versiones](../books/version-history.md)). |
| **Lorebook** | 31 entradas: el mundo, los lugares, las personas y los idiomas. |
| **Prompts** | Cada parte conserva el prompt con el que se escribió y el lore que se activó: **Mostrar el prompt**, **Comparar prompt** y **Uso del lore** en el menú de la parte ([Prompts del libro](../books/prompts.md)). |
| **Knowledge Tracker** | Siete personajes seguidos y lo que cada uno sabe y no sabe, parte a parte ([Knowledge Tracker](../extensions/knowledge-tracker.md)). |
| **Diálogos traducidos** | Unos 1 250 pasajes traducidos: el habla del Dominio en **Vrakht** y la del valle en **Aethel-Vael** ([Conlang Translator](../extensions/conlang-translator.md)), y en **japonés** y **checo** ([Real Language Translator](../extensions/real-language-translator.md)). |
| **Capítulos** | El esquema de la historia, hecho por [Arc Outline](../extensions/arc-outline.md) a partir del texto. |
| **Una imagen** | Una ilustración, en la parte 31. |

Los datos de las cuatro extensiones están en el libro, pero solo se ven con las extensiones cargadas; sin ellas se conservan y se ignoran. El libro no incluye modelo ni protocolo: tras la importación, elige el tuyo en los ajustes del libro.

<a id="download"></a>

## Descargar

Los archivos se adjuntan a la versión [example-book-1](https://github.com/Enerccio/Marginalia/releases/tag/example-book-1):

| Archivo | |
|---|---|
| [`unregistered-book.zip`](https://github.com/Enerccio/Marginalia/releases/download/example-book-1/unregistered-book.zip) (5.7 MB) | El libro: la copia de seguridad con su imagen. |
| [`unregistered-lorebook.json`](https://github.com/Enerccio/Marginalia/releases/download/example-book-1/unregistered-lorebook.json) | El lorebook del libro, por separado. El archivo del libro también lo incluye; este solo sirve si quieres ver el lorebook sin el libro. |
| [`vrakht.json`](https://github.com/Enerccio/Marginalia/releases/download/example-book-1/vrakht.json) | La lengua construida del Dominio, para el Conlang Translator. |
| [`aethel-vael.json`](https://github.com/Enerccio/Marginalia/releases/download/example-book-1/aethel-vael.json) | La lengua construida del valle, para el Conlang Translator. |

<a id="importing-it"></a>

## Importarlo

1. **Los idiomas** (si tienes el Conlang Translator): en **Ajustes → Extension Settings → Conlang Translator** haz clic en **Importar** y elige `vrakht.json`; después repite con `aethel-vael.json`
   ([Conlang Translator](../extensions/conlang-translator.md#languages)). Hazlo antes que el libro, para que los ajustes de idioma del libro tengan adónde apuntar.
2. **El libro:** en la pestaña *Ajustes* de la ventana del libro haz clic en **Importar copia de seguridad como libro nuevo**, escribe un nombre y sube `unregistered-book.zip`
   ([cómo se importa una copia de seguridad](../books/backups.md#importing-a-backup-as-a-new-book)). Cuando Marginalia pregunte por el lorebook, elige **Importar**: está en el archivo.
3. Abre el libro nuevo y, en sus ajustes, elige tu modelo y tu protocolo.

Los diálogos traducidos se guardan en el libro, así que el habla en vrakht, aethel-vael, japonés y checo aparece tal como se tradujo. Para traducir más con el Real Language Translator, crea allí los idiomas *Japanese* y *Czech* (el plugin los ofrece como preajustes), porque el plugin no exporta sus idiomas junto con un libro.

<a id="where-it-comes-from-and-the-licence"></a>

## De dónde procede y licencia

La historia, el lorebook y las dos lenguas son originales. El esquema, el lorebook, las lenguas y el plan de las partes los escribió el autor de Marginalia; el texto de las partes lo **escribió un modelo de IA** (Claude Haiku) a través del propio bucle de generación de Marginalia: generar, anotar, reescribir, seguir el conocimiento, traducir, resumir. Es una demostración del programa, no una novela publicada, y tiene los defectos de la prosa de un modelo.

- El texto, el lorebook y las dos lenguas se dedican al dominio público bajo [CC0 1.0](https://creativecommons.org/publicdomain/zero/1.0/): úsalos para lo que quieras.
- La imagen es *Interior de una casa en Scheveningen* (1887) de Willem Adriaan Alexander Liernur, de dominio público, tomada de [Rawpixel](https://www.rawpixel.com/image/13744617/image-face-person-art).
