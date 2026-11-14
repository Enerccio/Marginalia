---
label: Publicación y visor
order: 40
---

# Publicación y visor

El **visor** es una vista de lectura de tus libros: solo el texto de la historia, sin el editor alrededor. Funciona
bien en teléfonos y tabletas. **Publicar** un libro permite que otros usuarios de la misma instalación de Marginalia lo lean
en el visor.

<a id="opening-the-viewer"></a>

## Abrir el visor

El visor está en `/view` en la dirección de tu Marginalia, por ejemplo:

- aplicación de escritorio: `http://127.0.0.1:8765/view`
- servidor: `https://marginalia.example.com/view`

Usa el mismo inicio de sesión que Marginalia.

![La lista de libros del visor](../../images/viewer-list.png)

La lista muestra tus libros, con sus etiquetas y la fecha en que los abriste por última vez. Filtra por **Nombre** (libros
cuyo nombre empieza por el texto) y por **Etiquetas** (libros con todas las etiquetas seleccionadas). Ordena por **Abierto por última vez**
o por **Nombre**; al pulsar de nuevo el botón de orden activo, el orden se invierte.

Pulsa un libro para leerlo.

<a id="folders-in-the-list"></a>

### Carpetas en la lista

La lista muestra tus [carpetas](folders.md) igual que la lista del espacio de trabajo en el teléfono: primero las carpetas, cada una con
el número de libros que contiene, y después los libros de la carpeta actual. Pulsa una carpeta para entrar en ella. La ruta encima de los
filtros (`Books / Novels / Portal fantasy`) muestra dónde estás, y al pulsar una parte de ella se vuelve a ese lugar.

![El visor dentro de una carpeta](../../images/viewer-folder.png)

- **Incluir subcarpetas** muestra todos los libros que hay debajo de la carpeta actual, a cualquier profundidad, con la carpeta de cada uno indicada bajo su nombre.
- **Favoritos**, la carpeta del nivel superior que existe mientras tengas un libro favorito, muestra los libros favoritos de todas
  las carpetas. Los favoritos se marcan en la [lista de libros](folders.md#favorites), no en el visor.
- Los filtros actúan dentro de la carpeta actual (o debajo de ella con *Incluir subcarpetas*), así que para buscar en todos tus libros ve al
  nivel superior y marca la casilla. **Abierto por última vez** ordena las carpetas por su última modificación.
- El visor se abre en la carpeta donde lo dejaste: después de leer un libro, la flecha te lleva de vuelta a la misma carpeta, y la
  siguiente visita, o una recarga, también empieza allí. El lugar se guarda en una cookie de tu navegador (`marginalia_viewer_folder`); el
  servidor no lo almacena, así que otro navegador empieza en el nivel superior. Una carpeta que se ha borrado equivale al nivel superior.

<a id="reading"></a>

## Leer

La vista de lectura muestra la [rama activa](branches-and-story-tree.md) del libro desde la primera hasta la última parte,
con el estilo de lectura de libro (justificado, con guiones para la [lengua](managing-books.md#the-about-tab) del libro). Las demás ramas, las instrucciones de turno y los metadatos no se muestran. El botón con flecha de la
esquina superior izquierda vuelve a la lista.

El botón redondo de la esquina superior derecha abre un menú de **estilos de texto** para leer: los estilos *Normal* y *Libro*
y los estilos que hayas creado en [Ajustes → Estilos](../account.md#styles) (se muestra el marcado; el estilo Libro hasta que elijas otro).
Solo se ofrecen tus propios estilos, también en un libro publicado por otro usuario: lo que el autor usa para escribir no
llega al lector. La elección se guarda en una cookie de tu navegador (`marginalia_reader_style`), así que vale para cada libro que
leas allí y el servidor no la almacena; un estilo que no es tuyo (otro usuario con sesión iniciada en el mismo navegador) se ignora.

![El menú de estilos en el lector](../../images/viewer-style-menu.png)

![Leyendo un libro](../../images/viewer-reading.png)

Lo que lees es el libro tal como está ahora: las partes nuevas aparecen la próxima vez que se abre la página.

Un libro largo se lee por trozos: se muestran las partes alrededor de donde estás, la siguiente (y la anterior) se añaden al desplazarte, y las
que quedan lejos se descartan; aparece una marca *Cargando* en el borde si te desplazas más rápido de lo que llegan. No se cargan más partes de las que indica el ajuste **Partes cargadas a la vez en la historia** de [tu cuenta](../account.md) a la
vez; si está vacío, se carga todo el libro de una vez.

El visor recuerda dónde dejaste de leer cada libro: al volver a abrirlo, lo abre en esa parte. La posición se guarda solo en el
almacenamiento local de tu navegador (por libro), no se guarda en el servidor, así que otro navegador o un almacenamiento del navegador borrado empieza desde el principio.

Las [imágenes de las partes](story-editor.md#images) se muestran donde el autor las colocó (después del texto, salvo que el autor haya elegido otro lugar), a ti, y a los lectores de un
libro [publicado](#publishing-a-book), salvo que desmarques **Mostrar las imágenes a los lectores** en la pestaña *Información* del libro (viene marcada por defecto). Con ella desmarcada, los lectores ven el texto sin las imágenes. Las imágenes se leen de tu cuenta solo mientras el libro está publicado, y no cuando lo dejas de publicar.

<a id="publishing-a-book"></a>

## Publicar un libro

Por defecto, solo tú puedes abrir tus libros en el visor. Para que otros usuarios lean un libro:

1. Abre el libro y marca **Publicado** en su pestaña *Información*.
2. Copia el enlace de lectura que aparece debajo de la casilla en la pestaña *Información*. El enlace es la dirección de tu
   instalación (incluida su ruta de contexto, si Marginalia no se sirve desde la raíz), seguida de `/view/` y el
   ID del libro: `<address of Marginalia>/view/<book ID>`. En Docker, donde Marginalia se ejecuta como aplicación raíz, tiene este aspecto:
   `https://marginalia.example.com/view/0b6f3c1e-...`; si se despliega bajo una ruta de contexto como
   `/marginalia`, el enlace es `https://example.com/marginalia/view/0b6f3c1e-...`. (También puedes copiar la misma dirección
   desde la barra de direcciones del navegador cuando el libro está abierto en el visor.)
3. Envía la dirección a los otros usuarios.

Los demás usuarios necesitan una cuenta en la misma instalación de Marginalia; inician sesión y ven el libro. Los libros publicados no
aparecen en su lista del visor, solo la dirección los abre. Solo pueden leer el libro, no cambiarlo.

Publicar comparte el libro tal como está: solo se comparte el texto de las partes; las imágenes de las partes siguen siendo privadas.

Desmarca **Publicado** para dejar de compartirlo: la dirección muestra entonces *Libro no encontrado* a todos menos a ti. Los libros
borrados no se pueden abrir de ninguna forma.
