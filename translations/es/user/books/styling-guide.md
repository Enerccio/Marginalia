---
label: Guía de estilos
order: 54
---

# Guía de estilos: haz que tu libro se vea como tu libro

Un **estilo de texto** decide cómo se ve el texto de tus partes: la fuente, los colores, el ancho de la columna, los títulos y la línea
entre escenas. Esta guía te lleva desde el primer cambio hasta un aspecto terminado, y termina con un ejemplo completo para descargar: un estilo
para un libro de terror que usa todas las opciones y un poco de CSS propio.

Dónde se usa un estilo:

| Dónde | Qué ocurre |
|---|---|
| **El editor de la historia** | El estilo del libro (o tu predeterminado) es el aspecto de cada parte. Consulta [Estilos de texto](story-editor.md#text-styles). |
| **El lector** | Un [libro publicado](publishing.md) se lee con el estilo que el *lector* elige entre sus propios estilos (el estilo del libro, hasta que elija uno). |
| **Exportaciones** | Un estilo marcado como **Para exportaciones** se elige en el [diálogo de exportación](export.md#print-styles). HTML y EPUB usan la hoja de estilos; PDF y Word usan los valores numéricos (consulta [qué usa cada exportación](#what-the-exports-take)). |

<a id="make-your-first-style"></a>

## Crea tu primer estilo

1. Abre **Configuración → Estilos** (la [pestaña Estilos](../account.md#styles)).
2. Selecciona **Book** en la lista y pulsa **Duplicar**. Los estilos integrados no se pueden cambiar, así que siempre empiezas con una copia llamada
   *Book (copy)*. Ponle un nombre.
3. Cambia algunas casillas en la pestaña **Opciones**. La **Vista previa** bajo el formulario muestra un texto de ejemplo (un título de capítulo, énfasis, una cita, un cambio de escena,
   un título más pequeño) y sigue cada cambio mientras escribes.
4. Pulsa **Guardar** en la configuración. Antes de eso no se guarda nada, y **Descartar los cambios** tira el trabajo a la basura.
5. En el editor de la historia abre el menú de configuración (⚙) → **Apariencia** → **Cambiar los estilos del texto** y marca tu estilo. Queda guardado en el
   libro. Para usar el estilo en todos tus libros, selecciónalo en la lista y pulsa **Establecer como predeterminado**.

![Los estilos en el menú de la historia](../../images/story-style-menu.png)

Una casilla vacía mantiene el aspecto de la aplicación, así que un estilo solo dice lo que quieres cambiar. Un estilo con todas las casillas vacías es el aspecto sencillo.

<a id="the-options"></a>

## Las opciones

Cada casilla acepta un valor CSS. Unidades que usarás: `rem` (el tamaño del texto de la página; 1rem suele ser 16px), `em` (el tamaño del
texto actual), `px`, `%`, y colores como `#336699`, `rgb(51, 102, 153)` o un nombre como `ivory`.

<a id="text"></a>

### Texto

| Opción | Qué hace | Ejemplo |
|---|---|---|
| **Font family** | La fuente del texto. Escribe una lista: se usa la primera que tenga el ordenador, y la última debería ser una familia genérica (`serif`, `sans-serif`, `monospace`). | `"Garamond", "Georgia", serif` |
| **Font size** | El tamaño del texto. | `1.1rem` |
| **Line height** | La distancia entre líneas. Un número sin unidad es un múltiplo del tamaño de la fuente. | `1.7` |
| **Letter spacing**, **Word spacing** | Espacio extra entre letras, entre palabras. Valores pequeños: un espaciado mínimo entre letras hace que el texto respire. | `0.01em`, `0.05em` |
| **Text color** | El color del texto. | `#cfc6bb` |
| **Text align** | `left`, `right`, `center` o `justify`. | `justify` |
| **Hyphenation** | Parte las palabras largas al final de una línea. Usa el diccionario del [idioma](managing-books.md#the-about-tab) del libro, así que configura primero el idioma. Va mejor junto con *justify*. | on |
| **Paragraph spacing** | El espacio bajo un párrafo. Un libro suele tener poco, o ninguno si usa sangría en la primera línea. | `0.3em` |
| **First line indent** | Sangra la primera línea de un párrafo, como en un libro impreso, excepto después de un título o de un cambio de escena. | `1.5em` |
| **Drop cap** | Una letra inicial grande en el primer párrafo después de un título. | on |

<a id="page"></a>

### Página

| Opción | Qué hace | Ejemplo |
|---|---|---|
| **Text width** | Una columna de ancho propio, centrada. Una línea de 60 a 75 caracteres se lee mejor, unos `34em` a `40em`. | `38em` |
| **Background color** | El color detrás del texto; rellena toda la zona, no solo los bloques de texto. Elige un color de texto que se lea sobre él. | `#0c0a0a` |

<a id="emphasis-links-and-code"></a>

### Énfasis, enlaces y código

| Opción | Qué hace |
|---|---|
| **Emphasis color** | El color del texto en *cursiva*. |
| **Strong color** | El color del texto en **negrita**. |
| **Link color** | El color de los enlaces. |
| **Code font family** | La fuente de `code` y de los bloques de código. |

<a id="quotes"></a>

### Citas

| Opción | Qué hace |
|---|---|
| **Quote color** | El color del texto de una cita (`> a line`). |
| **Quote border color** | El color de la barra al lado de una cita. |

<a id="headings"></a>

### Títulos

| Opción | Qué hace |
|---|---|
| **Heading font family** | La fuente de los títulos `#`, `##` y `###`. |
| **Heading size** | El tamaño del primer nivel; los niveles inferiores son más pequeños en proporción. |
| **Heading color** | El color de los títulos. |
| **Heading align** | `left`, `center` o `right`. |
| **Heading small caps** | Escribe los títulos en versalitas. |

<a id="scene-break"></a>

### Cambio de escena

Una línea con `---` en el texto es una separación entre escenas. Por defecto son tres puntos. **Scene break text** es lo que se muestra
en su lugar (`* * *`, `~`, `† † †`); **Scene break color** es su color.

<a id="custom-css"></a>

## CSS personalizado

Las opciones cubren lo habitual. Para el resto, la pestaña **CSS personalizado** acepta CSS propio, añadido después de todo lo demás y aplicado solo
dentro del estilo. Hay dos formas de escribirlo:

- **Declaraciones sueltas** se aplican a toda la zona que contiene el texto:

  ```css
  box-shadow: inset 0 0 120px rgba(0, 0, 0, 0.8);
  ```

- **Reglas** con un selector. Escríbelas con `&` (la zona del estilo) delante, o sin él: en cualquier caso solo alcanzan el texto que tiene este estilo.

  ```css
  & .chat-message-markdown h1 { text-shadow: 0 0 12px #8e1b17; }
  & .chat-message-markdown a:hover { color: #b3261e; }
  ```

El texto de una parte está en un elemento con la clase `chat-message-markdown`, así que un selector para algo dentro del texto empieza ahí. El
campo **CSS generado** bajo la caja de CSS muestra la hoja de estilos que crean las opciones: copia sus selectores para ver qué puedes sobrescribir.

| Quieres cambiar | Selector (después de `& .chat-message-markdown`) |
|---|---|
| párrafos | `p` |
| títulos | `h1`, `h2`, `h3` |
| cursiva, negrita | `em`, `strong` |
| enlaces | `a`, `a:hover` |
| citas | `blockquote` |
| listas | `ul`, `ol`, `li` |
| código | `code` (en línea), `pre` (bloque) |
| cambio de escena | `hr`, `hr::after` (el símbolo) |
| la primera línea de un párrafo | `p::first-line` |
| texto seleccionado | `::selection` |

Cosas que debes saber:

- **Las llaves `{ }` deben coincidir**, o el estilo se rechaza al guardar. Los comentarios `/* ... */` están permitidos.
- Algunas reglas que la aplicación necesita son `!important` (el texto nunca se hace más ancho que su zona, las palabras largas se parten y los párrafos conservan sus saltos de línea). Tu CSS no puede sobrescribir esas reglas; sí puede sobrescribir todo lo demás, porque va al final.
- El CSS puede cambiar cómo se ven las cosas, no lo que dicen. No llega a las barras de herramientas ni a las pestañas alrededor del texto.
- El CSS personalizado se usa en la historia, en el lector y en las exportaciones HTML. EPUB, PDF y Word no lo usan.

<a id="what-the-exports-take"></a>

## Qué usa cada exportación

Un estilo para exportaciones se elige en el [diálogo de exportación](export.md#print-styles); **Duplicar** un estilo y marca **Para exportaciones**, o parte de uno de los cuatro
que trae la aplicación. Los colores que dependen del tema de la aplicación (el texto claro del tema oscuro) se convierten en oscuros para una
página blanca al exportar.

| | HTML | EPUB | PDF | Word |
|---|---|---|---|---|
| fuentes, tamaños, interlineado, espaciado, alineación, sangría de la primera línea | ✓ | ✓ | ✓ | ✓ |
| colores del texto, énfasis, negrita, citas, títulos, cambio de escena | ✓ | ✓ | ✓ | ✓ |
| color de fondo | ✓ | ✓ | ✓ | ✓ (el color de la página) |
| ancho del texto, letras capitales, separación por guiones, versalitas de los títulos | ✓ | ✓ | | |
| CSS personalizado | ✓ | | | |

Los lectores de libros electrónicos suelen usar sus propias fuentes, tamaños y colores, sea cual sea lo que diga la hoja de estilos. PDF y Word solo pueden usar las fuentes que tiene el formato: el PDF elige serif, sans o monoespaciada a partir de la lista que escribiste; Word usa la primera fuente de
la lista y muestra la suya propia si el ordenador no la tiene.
<a id="share-a-style-export-and-import"></a>

## Compartir un estilo: exportar e importar

Debajo de la lista de estilos, **Exportar** guarda el estilo seleccionado como un archivo (`name.style.json`; también se puede exportar un estilo integrado) e **Importar**
lee un archivo de este tipo. Un estilo importado se añade a tus propios estilos como un estilo nuevo; no se guarda hasta que pulses **Guardar**, y
conserva su nombre, así que renómbralo si ya tienes uno parecido.

El archivo es JSON:

```json
{
  "format": "marginalia-text-style",
  "version": 1,
  "styles": [
    {
      "name": "My style",
      "forExport": false,
      "options": { "FONT_SIZE": "1.1rem", "TEXT_COLOR": "#222222" },
      "customCss": "& .chat-message-markdown h1 { letter-spacing: 0.1em; }"
    }
  ]
}
```

Puede contener varios estilos, y también puedes escribir uno a mano. Las opciones se nombran en mayúsculas (`FONT_SIZE`, `TEXT_COLOR`, `SCENE_BREAK_TEXT`...); el [ejemplo de terror](#an-example-a-horror-book) las tiene todas. Una opción que esta versión de Marginalia
no conoce se omite (se te indica cuántas), un archivo de un formato de versión más nueva se rechaza, y también se rechaza un estilo cuyo CSS personalizado
tenga llaves que no coinciden.

<a id="an-example-a-horror-book"></a>

## Un ejemplo: un libro de terror

[Descarga el estilo de terror](../../files/horror-book.style.json) (`horror-book.style.json`), impórtalo en *Configuración → Estilos* y selecciónalo
para verlo en la vista previa. Establece **todas las opciones** y usa CSS personalizado para lo que las opciones no cubren:

| | |
|---|---|
| **Página** | Fondo casi negro `#0c0a0a`, una columna de `38em`, y una viñeta oscura que se cierra sobre los bordes (CSS personalizado). |
| **Texto** | Garamond a `1.12rem`, altura de línea `1.8`, justificado con guionado, sangría de primera línea de `1.6em` y casi nada de espacio entre párrafos, como en una novela impresa. El texto es del gris del papel antiguo `#cfc6bb`, con una letra capital después de cada título. |
| **Énfasis** | Cursiva en rosa polvoriento `#b99a94`, negrita en rojo sangre `#b3261e`. |
| **Enlaces** | Verde enfermizo `#8fa07e` que se vuelve rojo y ondulado cuando el puntero está encima (CSS personalizado). |
| **Citas** | Texto gris con una barra rojo oscuro `#5c1010` y una mancha roja tenue detrás (CSS personalizado). |
| **Títulos** | Palatino en versalitas, centrado, rojo oscuro `#8e1b17`, con un resplandor tenue (CSS personalizado). |
| **Cambio de escena** | `† † †` en rojo oscuro; las listas llevan una cruz en lugar de un punto (CSS personalizado). |
| **Código** | Courier New, sobre un fondo casi negro. |

![Un relato en estilo de terror](../../images/story-style-horror.png)

Para probarlo con un texto real, pega esto en una parte (o la vista previa muestra un ejemplo similar):

```markdown
# Chapter One: The House on Harrow Lane

The rain had been falling for *three days* when Mara found the door. It was not on the plan of the house, and it was **warm** to the touch.

> Do not open it after dark. — scratched into the plaster, in a hand that was not hers

She counted the steps down. Thirteen. At the bottom there were:

- a lantern, still burning;
- a chair, turned to face the wall;
- her own name, written in the dust.

---

By morning the lantern was out, and the [house records](https://example.com) said nobody had lived there for forty years.
```

Después, modifícalo: cambia el fondo a `#14100c` para un marrón de luz de vela, o pon **Ancho del texto** en `32em` para una página más estrecha, o elimina el CSS personalizado para ver
lo que las opciones hacen por sí solas. Cuando tengas un resultado que te guste, **Exportar** el estilo y guarda el archivo junto a tu libro.
