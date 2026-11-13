---
label: Importación y exportación
order: 80
---

# Importación y exportación

<a id="exporting"></a>

## Exportar

Selecciona el lorebook en el editor y haz clic en el botón **exportación** (icono de archivo, *Exportar lorebook*). El navegador descarga `<nombre del lorebook>.json` con:

- el lorebook y **todos sus sub lorebooks** (todos los niveles), con sus nombres, descripción, estado de activación y etiquetas;
- cada entrada con su contenido, nota, orden, filtro, modo de inserción, etiquetas y etiquetas negativas.

Sirve para guardar una copia, compartir un mundo con otros usuarios o llevarlo a otra instalación. Los datos que las [extensiones](../extensions/index.md) guardan en un lorebook o en una entrada también van en el archivo y se restauran al importarlo. Las revisiones de entradas de la extensión [Lorebook VCS](../extensions/lorebook-vcs.md) pertenecen a las entradas antiguas y no aparecen en las entradas que crea la importación; la extensión tiene su propia exportación para ellas.

<a id="exporting-for-sillytavern"></a>

### Exportar para SillyTavern

El botón **exportación** abre un menú con dos elementos: **Exportar lorebook** (el archivo de arriba) y **Exportar a SillyTavern**, que descarga el lorebook como un archivo de información del mundo de SillyTavern que SillyTavern puede importar. Es lo contrario de la [importación](#importing-sillytavern-world-info):

| Marginalia | SillyTavern |
|---|---|
| *Nombre de la entrada* | Título / memo (`comment`) |
| *Contenido*, *Orden*, *Activado* | Content, order, disabled |
| Sin filtro | Entrada constante (🔵) |
| Filtro *Texto* | Una clave primaria |
| *Palabras clave (cualquiera)* | Una clave primaria por cada palabra clave |
| *Palabras clave (todas)* | La primera palabra clave es la clave primaria; las demás son claves secundarias con la lógica *AND ALL* |
| *Expresión regular* | Una clave escrita como `/patrón/banderas` |
| *En el bloque de lore* | Posición *before character definitions* |
| *Antes del prompt de usuario* | Posición *at depth* 1 |
| Etiquetas | Filtro de personaje (por nombre); etiquetas negativas solo cuando la entrada no tiene etiquetas (*exclude*) |

Los **sub lorebooks se fusionan**: el archivo tiene primero las entradas del lorebook y después las de sus sub lorebooks, porque SillyTavern no tiene lorebooks dentro de lorebooks. Se omite lo que SillyTavern no tiene equivalente: la *Nota* de una entrada, las etiquetas negativas junto con etiquetas y los datos de las extensiones. SillyTavern compara las claves con el chat reciente; para el sentido contrario, consulta [qué funciona de forma distinta](#what-works-differently).

!!! warning
Una expresión regular se escribe con la sintaxis de Java. SillyTavern usa JavaScript, así que las expresiones sencillas funcionan, pero algo exclusivo de Java (cuantificadores posesivos, clases `\p{...}`) no coincide allí.
!!!

<a id="importing-a-marginalia-lorebook"></a>

## Importar un lorebook de Marginalia

1. Haz clic en el botón **importar** (icono de archivo) y elige **Importar lorebook de Marginalia**.
2. Sube un archivo exportado desde Marginalia.

La importación crea siempre lorebooks **nuevos** (el lorebook y sus sub lorebooks, con la misma estructura), aunque ya tengas lorebooks con los mismos nombres. El lorebook importado queda seleccionado en el editor. Las etiquetas se asocian por nombre con tus etiquetas existentes, o se crean.

También se pueden importar archivos de versiones antiguas de Marginalia (un único lorebook sin sub lorebooks).

!!!
Los lorebooks también viajan dentro de las [copias de seguridad del libro](../books/backups.md). Al restaurar o clonar una copia, puede enlazarse con lorebooks que ya tengas en lugar de crear copias; consulta [Restaurar una copia de seguridad](../books/backups.md#restoring-a-backup).
!!!

<a id="importing-a-lorebook-another-user-shares"></a>

## Importar un lorebook que otro usuario comparte

**Importar de otro usuario** en el menú de importación lista los lorebooks que otros usuarios [comparten](lorebooks.md#sharing-a-lorebook) contigo, con su nombre, autor y descripción. Abre una fila para leer las entradas y haz clic en **Importar** para obtener una copia privada. Solo se incluyen los sub lorebooks que el autor comparte. A diferencia de la importación desde un archivo, no hace falta exportar nada ni enviarlo: el autor solo marca **Compartir con otros usuarios**.

<a id="importing-an-archive-of-text-files"></a>

## Importar un archivo comprimido de textos

1. Haz clic en el botón **importar** y elige **Importar desde un archivo comprimido**.
2. Sube un archivo `.zip` con archivos `.md` (Markdown).
3. Revisa los **nombres de las entradas** en la tabla y cámbialos si quieres; después haz clic en **Importar**.

![La ventana para subir el archivo comprimido](../../images/archive-lorebook-upload.png)

![Los nombres de las entradas antes de la importación](../../images/archive-entry-names.png)

Marginalia crea un lorebook nuevo con el nombre del archivo (sin `.zip`) y **una entrada por cada archivo `.md`**. Los archivos se toman en el orden de sus nombres, comparando los números dentro de los nombres como números (`2.md` va antes que `10.md`), también en las carpetas del archivo comprimido; las entradas reciben el *Orden* 1, 2, 3... en esa secuencia. Todo el archivo es el *Contenido*. Se omiten otros archivos, carpetas, archivos ocultos (`.nombre`) y la carpeta `__MACOSX` de los archivos comprimidos hechos en un Mac.

Los **nombres de las entradas** se toman, en este orden:

1. un archivo `names.txt` en la raíz del archivo comprimido: un nombre por línea, la primera línea para el primer archivo `.md` en el orden anterior, y así sucesivamente (una línea vacía deja ese archivo para la siguiente regla; las líneas sobrantes se ignoran). Sirve para nombrar muchos archivos a la vez, por ejemplo desde un script;
2. el primer encabezado del archivo (`# Capítulo 1` da *Capítulo 1*);
3. el nombre del archivo sin `.md`.

La tabla del diálogo muestra el resultado, y puedes escribir encima de cualquier nombre.

La importación muestra después su progreso. **Detener** la termina después de la entrada que se está guardando: el lorebook conserva las entradas ya importadas (la importación no se deshace), y un mensaje indica cuántos archivos se importaron.

Las entradas se importan **desactivadas**: no tienen filtro, así que activadas se enviarían con cada petición, algo poco deseable para un libro entero de capítulos. Activa las que necesites, con un filtro o con [etiquetas](entries.md#tags); **Edición masiva** es la forma rápida de hacerlo con muchas entradas.

Para meter los mismos archivos en una historia como partes, consulta [Importar en el libro](../books/story-editor.md#importing-into-the-book).

<a id="importing-sillytavern-world-info"></a>

## Importar información del mundo de SillyTavern

1. Haz clic en el botón **importar** y elige **Importar desde SillyTavern**.
2. Sube el archivo de información del mundo (`.json`) exportado desde SillyTavern.

Marginalia crea un lorebook nuevo con el nombre de la información del mundo (o del archivo) y convierte cada entrada:

| SillyTavern | Marginalia |
|---|---|
| Título / memo (`comment`) | *Nombre de la entrada* (la primera clave cuando no hay título) |
| Content | *Contenido*: macros como `{%{{{user}}}%}` y `{%{{{char}}}%}` siguen funcionando; consulta [macros](../templates/macros.md) |
| Order | *Orden* |
| Disabled | *Activado* desmarcado |
| Constante (🔵), o sin claves | siempre activa (sin filtro) |
| Claves primarias, claves secundarias opcionales con su lógica | un filtro, véase más abajo |
| Case-sensitive, match whole words | incluido en el filtro |
| Posición *before / after character definitions*, *example messages* | *En el bloque de lore* |
| Posición *author's note* (arriba / abajo), *at depth* | *Antes del prompt de usuario* |
| Filtro de personaje | etiquetas (o etiquetas negativas para *exclude*), por nombre de personaje y etiqueta |

**Las claves se convierten en filtros.** Una única clave simple se convierte en un filtro *Texto* (`dragon`). Todo lo demás (varias claves, claves secundarias: *AND ANY*, *AND ALL*, *NOT ANY*, *NOT ALL*; claves escritas como `/regex/`; distinción de mayúsculas o coincidencia de palabras completas) se convierte en un único filtro *Expresión regular* que hace lo mismo. Por ejemplo, las claves `dragon`, `wyrm` con la clave secundaria `fire` (*AND ANY*) se convierten en:

```
(?s)^(?=.*(?:\Qdragon\E|\Qwyrm\E))(?=.*(?:\Qfire\E))
```

No necesitas entender estas expresiones; edítalas solo si conoces las expresiones regulares.

<a id="what-works-differently"></a>

### Qué funciona de forma distinta

Los ajustes de SillyTavern sin equivalente en Marginalia aparecen en la **Nota** de la entrada, por ejemplo *Imported from SillyTavern, not supported: probability 50%, inclusion group "weather".* Son:

- probabilidad, grupos de inclusión, sticky, cooldown y delay: la entrada simplemente está activa siempre que coincide;
- inserción *at depth*: la entrada va antes del prompt de usuario.

La diferencia más importante es **contra qué se comparan las claves**. SillyTavern analiza el chat reciente. Marginalia compara con la [petición para la parte que se está escribiendo](entries.md#filters) (su escena, personajes e instrucciones), y una entrada importada empieza exactamente así (su **Activation Context** es *Instructions*). Por eso, una entrada que SillyTavern activaba porque un nombre aparecía en los últimos mensajes solo se activa en Marginalia cuando mencionas el nombre en el turno. Para obtener lo que hace SillyTavern, pon el **Activation Context** de la entrada en *Instructions and parts of the story* y **Partes** con el número de las partes más recientes que hay que revisar; consulta [Mirar en la historia](entries.md#looking-into-the-story). La activación recursiva (entradas que activan otras entradas) tampoco la configura la importación: actívala con la **scan depth** del lorebook; consulta [Entradas traídas por otras entradas](entries.md#entries-brought-in-by-other-entries).

Tras importar, revisa las entradas y decide cuáles deben estar siempre activas, cuáles mantienen sus filtros y cuáles se gestionan mejor con [etiquetas](entries.md#tags).
