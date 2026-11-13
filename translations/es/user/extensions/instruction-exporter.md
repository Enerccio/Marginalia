---
label: Exportador de instrucciones
order: 12
---

# Exportador de instrucciones

Copia al portapapeles las instrucciones de la historia, con el escenario, el personaje POV y los personajes presentes.

<a id="how-it-works"></a>

## Cómo funciona

La extensión añade **Export instructions** al menú de ajustes (⚙) del [editor de historias](../books/story-editor.md).
Recorre las partes de la rama activa, desde la primera parte hasta el final actual de la historia, recoge los campos
a partir de los que se generó cada parte y los copia como un único texto. Cada campo rellenado se escribe con su nombre
seguido de dos puntos y su contenido en la línea siguiente; las partes se separan con una línea en blanco, en el orden
de la historia:

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

Los campos vacíos se omiten, y también las partes sin ningún campo rellenado. Pega el texto donde quieras: para conservar
el esquema de la trama del libro, para reutilizar las instrucciones en otro libro o para pasárselas a otra persona.

<a id="usage"></a>

## Uso

No hay nada que configurar. Abre un libro y elige *Export instructions* en el menú de ajustes; un aviso confirma
que el texto está en el portapapeles. Si la rama no tiene ninguno de estos campos rellenado, no se copia nada y se te indica así.

Solo se exporta la rama activa; para exportar otra, cámbiate antes a ella.

!!!
Los navegadores solo permiten que una página escriba en el portapapeles cuando se abre por `https://` o en `localhost`. Por `http://` en otra dirección el elemento no tiene efecto.
!!!
