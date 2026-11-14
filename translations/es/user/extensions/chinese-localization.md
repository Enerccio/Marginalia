---
label: Localización al chino
order: 18
---

# Localización al chino

Añade el chino simplificado (中文（简体）) a los idiomas de la interfaz de usuario.

<a id="using-it"></a>

## Uso

Una vez que un administrador carga la extensión ([Gestión de extensiones](../administration/extensions.md)), *中文（简体）*
aparece en el selector **Idioma**, en la parte inferior del espacio de trabajo (en el menú de un [teléfono](../phone.md)), junto
a English y 日本語. Selecciónalo y la página se recarga en chino; la elección se guarda en tu cuenta, así que se usa en
todos los dispositivos en los que inicies sesión. Cada usuario elige su propio idioma.

![El editor de historias en chino](../../images/plugin-chineselocalization.png)

La interfaz está traducida: menús, botones, mensajes, las descripciones de las variables de plantilla y de las macros. Un
texto que todavía no tiene traducción al chino se muestra en inglés.

La extensión también añade el chino a **Idioma de los prompts** en la configuración (*Configuración → Plantillas*, véase
[Idioma de los prompts](../account.md#prompt-language)): los prompts predefinidos de la aplicación y de los plugins se envían
entonces en chino. Es una elección propia, independiente del idioma de la interfaz. Tus libros, tus lorebooks y los
prompts que escribiste tú mismo no se modifican.

<a id="removing-it"></a>

## Desactivación

Descargar la extensión retira el idioma, tanto de la interfaz como de los prompts. Los usuarios que lo habían elegido ven,
y envían, en inglés hasta que escojan otro idioma; su elección se conserva y vuelve a aplicarse cuando la extensión se
cargue de nuevo.

<a id="for-developers"></a>

## Para desarrolladores

La extensión sirve de ejemplo para añadir un idioma: véase
[Plugin de ejemplo → Localización al chino](../../developer/plugins/example.md#walkthrough-chinese-localization).
