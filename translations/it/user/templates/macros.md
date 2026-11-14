---
label: Riferimento macro
order: 90
---

# Riferimento macro

Marginalia comprende le macro di [SillyTavern](https://docs.sillytavern.app/usage/core-concepts/macros/), quindi
i prompt e le informazioni del mondo scritte per SillyTavern funzionano senza modifiche. Le macro funzionano in ogni
[modello](templates.md): template principale, prompt utente, prompt del riassunto e voci del lorebook.

{%{

<a id="syntax"></a>

## Sintassi

| Forma | Esempio |
|---|---|
| senza argomenti | `{{user}}` |
| argomenti separati da `::` | `{{setvar::mood::nervous}}` |
| primo argomento dopo uno spazio | `{{getvar mood}}`, `{{setvar greeting::Hello}}` |
| singolo argomento in forma legacy `:` | `{{getvar:mood}}`, `{{random:north,south}}` |
| macro annidate | `{{getvar::{{user}}_mood}}` |
| con ambito (contenuto come ultimo argomento) | `{{setvar backstory}}Long text...{{/setvar}}` |

- I nomi delle macro non distinguono maiuscole e minuscole: `{{User}}` e `{{USER}}` funzionano.
- Gli spazi intorno a nomi e argomenti vengono ignorati: `{{ getvar :: mood }}`.
- I `::` extra vanno nell'ultimo argomento: `{{setvar::x::a::b}}` imposta `x` a `a::b`.
- Il contenuto con ambito viene prima valutato, poi privato dell'indentazione comune e ripulito agli estremi. Con `#` dopo le graffe,
  `{{#setvar name}}...{{/setvar}}`, gli spazi bianchi vengono mantenuti esattamente.

<a id="names-and-participants"></a>

## Nomi e partecipanti

Marginalia non ha personas né schede personaggio; i nomi provengono dalle istruzioni del turno della parte in scrittura.

| Macro | Risultato |
|---|---|
| `{{user}}` | Il personaggio POV. |
| `{{char}}` | Il personaggio POV. Alias `{{charIfNotGroup}}`. |
| `{{group}}` | I personaggi presenti, o il personaggio POV se non ne sono indicati. Alias `{{groupNotMuted}}`. |
| `{{notChar}}` | I personaggi presenti tranne il personaggio POV (separati da virgola o da a capo, uniti con `, `). |
| `{{description}}` | La descrizione del libro. |
| `{{scenario}}` | L'ambientazione della scena. |
| `{{input}}` | Le istruzioni per la parte. |

Anche i marcatori di SillyTavern `<USER>`, `<BOT>`, `<CHAR>`, `<GROUP>` e `<CHARIFNOTGROUP>` (in qualsiasi maiuscolo o minuscolo) funzionano.

Con personaggio POV *Alice* e personaggi presenti *Alice, Bob, Carol*:
`{{user}}` → *Alice*, `{{group}}` → *Alice, Bob, Carol*, `{{notChar}}` → *Bob, Carol*.

<a id="story-history"></a>

## Storia

| Macro | Risultato |
|---|---|
| `{{lastMessage}}` | Il testo dell'ultima parte del ramo attivo. Alias `{{lastCharMessage}}`. |
| `{{lastMessageId}}` | La posizione dell'ultima parte, contata da 0. |
| `{{lastUserMessage}}` | Le istruzioni dell'ultima parte. |
| `{{summary}}` | Il [riassunto](../books/summaries.md) più recente del ramo attivo. |
| `{{closest-message}}`, `{{closest-message::n}}` | La parte più vicina a quella in scrittura: l'ultima, oppure l'*n*-esima contata a ritroso da essa (`::2` è quella prima dell'ultima). Vuota quando non esiste una parte del genere. Alias `{{closestMessage}}`. |
| `{{closest-summary}}`, `{{closest-summary::n}}` | Lo stesso per i riassunti del ramo: il più recente, oppure l'*n*-esimo contato a ritroso. Alias `{{closestSummary}}`. |
| `{{current-message}}` | Il testo della parte che *Rigenera* o uno swipe sostituisce; vuoto per una parte nuova. Alias `{{currentMessage}}`. |

Per la prima parte di un libro sono vuote. Per *Rigenera*, "ultima parte" è quella che precede la parte rigenerata.

Il numero di `closest-message` e `closest-summary` può essere una macro, quindi può provenire da una variabile:
`{{setvar::back::3}}{{closest-message::{{getvar::back}}}}` è la terza parte dalla fine. Un numero che non sia un intero positivo
non produce nulla. `closest-summary` conta i riassunti delle parti del ramo, includendo un [meta riassunto](../books/summaries.md)
e i riassunti che esso sostituisce; `{{summary}}` è uguale a `{{closest-summary}}`.

<a id="date-and-time"></a>

## Data e ora

| Macro | Esempio di risultato |
|---|---|
| `{{time}}` | `2:30 PM` |
| `{{time::UTC+2}}` | `4:30 PM` - l'ora con uno scostamento UTC (anche `UTC-5:30` funziona) |
| `{{date}}` | `March 15, 2026` |
| `{{weekday}}` | `Sunday` |
| `{{isodate}}` | `2026-03-15` |
| `{{isotime}}` | `14:30` |
| `{{datetimeformat::YYYY-MM-DD HH:mm}}` | `2026-03-15 14:30` - [token di formato moment.js](https://momentjs.com/docs/#/displaying/format/), il testo in `[brackets]` viene stampato così com'è: `{{datetimeformat::dddd [the] D. MMMM}}` → `Sunday the 15. March` |
| `{{idleDuration}}` | `2 hours` - tempo trascorso da quando è stata scritta l'ultima parte, `just now` per la prima parte. Alias `{{idle_duration}}`. |
| `{{timeDiff::2026-01-01 12:00::2026-01-01 10:00}}` | `in 2 hours` - la differenza tra due momenti; funzionano `now`, le date (`2026-01-08`) e gli orari ISO. I valori sconosciuti non producono nulla. |

Gli orari sono nel fuso orario del computer o del server su cui gira Marginalia, e sempre in inglese. Per la
[versione Docker](../getting-started/docker-server.md), imposta il fuso orario con la variabile d'ambiente `TZ`, ad esempio
`TZ=Europe/Prague`.

<a id="variables"></a>

## Variabili

Le variabili conservano valori tra una parte e l'altra: contatori, l'umore di un personaggio, cose che la storia ha stabilito. Ci sono due
tipi:

- le variabili **locali** appartengono al ramo della storia: ogni parte ricorda i loro valori, e una nuova parte parte con i valori della parte precedente;
- le variabili **globali** appartengono al libro, condivise da tutti i rami.

Vedi [Cosa viene conservato tra le parti](templates.md#what-is-kept-between-parts).

| Locale | Globale | |
|---|---|---|
| `{{getvar::name}}` | `{{getglobalvar::name}}` | Il valore; vuoto se non impostato. |
| `{{setvar::name::value}}` | `{{setglobalvar::name::value}}` | Imposta il valore. Non stampa nulla. |
| `{{addvar::name::value}}` | `{{addglobalvar::name::value}}` | Somma un numero, oppure aggiunge testo quando uno dei due non è un numero. Non stampa nulla. |
| `{{incvar::name}}` | `{{incglobalvar::name}}` | Aggiunge 1 e stampa il nuovo valore. Un valore che non è un numero parte da 0. |
| `{{decvar::name}}` | `{{decglobalvar::name}}` | Sottrae 1 e stampa il nuovo valore. |
| `{{hasvar::name}}` | `{{hasglobalvar::name}}` | `true` oppure `false`. |
| `{{deletevar::name}}` | `{{deleteglobalvar::name}}` | Rimuove la variabile. Alias `flushvar` / `flushglobalvar`. |

I numeri sono stampati senza zeri finali: `1.50` più `1.5` fa `3`.

```handlebars
{{setvar::hp::10}}{{addvar::hp::5}}HP: {{getvar::hp}}      → HP: 15
{{setvar::items::a rope}}{{addvar::items::, a lamp}}{{getvar::items}}   → a rope, a lamp
```

<a id="shorthands"></a>

### Scorciatoie

`{{.name}}` è una variabile locale, `{{$name}}` una globale. Da sola, una scorciatoia stampa il valore. Con un operatore:

| Scorciatoia | Effetto | Stampa |
|---|---|---|
| `{{.hp = 20}}` | imposta il valore | nulla |
| `{{.hp += 5}}`, `{{.hp -= 5}}` | somma / sottrae (`+=` aggiunge testo al testo) | nulla |
| `{{.turn++}}`, `{{.turn--}}` | somma / sottrae 1 | il nuovo valore |
| `{{.hp == 17}}`, `{{.hp != 17}}` | confronta | `true` / `false` |
| `{{.hp > 10}}`, `>=`, `<`, `<=` | confronta numeri (`false` per il testo) | `true` / `false` |

Ripieghi e valori predefiniti:

- `{{.name || Guest}}` stampa *Guest* quando la variabile è vuota o falsa (`0`, `false`, `no`, `off`);
- `{{.name ?? Guest}}` stampa *Guest* solo quando la variabile non è affatto impostata;
- `{{.title ||= Wanderer}}` imposta la variabile a *Wanderer* se è vuota o falsa, poi la stampa;
- `{{.title ??= Wanderer}}` la imposta solo quando non è affatto impostata, poi la stampa.

I valori possono contenere macro: `{{.log += -> {{user}}}}`.

Un contatore di parti, per esempio in cima al prompt utente (`+=` non stampa nulla, e `{{trim}}` rimuove la riga vuota che lascia):

```handlebars
{{.part += 1}}{{trim}}
This is part {{.part}} of the story.
```

<a id="conditionals"></a>

## Condizionali

```handlebars
{{if presentCharacters}}
  Present: {{presentCharacters}}
{{else}}
  {{user}} is alone.
{{/if}}
```

La condizione può essere:

| Condizione | Vera quando |
|---|---|
| un nome di macro - `{{if user}}` | la macro restituisce un valore |
| una variabile - `{{if .mood}}`, `{{if $chapter}}` | la variabile ha un valore |
| una variabile di template - `{{if sceneSetting}}` | la variabile ha un valore |
| un confronto - `{{if .hp > 20}}` | il confronto è vero |
| un testo qualsiasi - `{{if yes}}` | sempre, tranne per i valori falsi |
| negazione - `{{if !user}}`, `{{if !.mood}}` | il contrario |

Sono falsi un valore vuoto e `0`, `false`, `no`, `off` (in qualsiasi maiuscolo o minuscolo). Tutto il resto è vero.

- La forma breve `{{if user::text}}` stampa *testo* quando la condizione è vera.
- Viene valutato solo il ramo che viene preso: un `{{setvar}}` nell'altro ramo non viene eseguito.
- I blocchi `{{if}}` vengono privati dell'indentazione e ripuliti come le macro con ambito, quindi possono essere indentati per leggibilità. `{{#if}}`
  (il [Handlebars if](templates.md#conditions)) mantiene gli spazi bianchi.
- Un `{{if}}` senza `{{/if}}` viene stampato come testo.
<a id="expressions"></a>

## Espressioni

`{{eval::expression}}` restituisce `true` o `false`. Usalo dove serve un confronto che `{{if}}` non sa fare: sui valori del template (non solo sulle variabili), uniti con `and`, `or` e `not`:

```handlebars
{{eval::povCharacter == "Fiora" and presentCharacters has "Mara"}}
{{if {{eval::.hp < 10 or not .armed}} }}Fiora is in danger.{{/if}}
```

| In un'espressione | Significato |
|---|---|
| `"text"`, `'text'` | Un testo. **Un testo va sempre tra virgolette**: una parola senza virgolette è un nome. |
| `12`, `-3.5`, `true`, `false` | Un numero o un valore di verità. |
| `povCharacter`, `presentCharacters`, `sceneSetting`... | Un valore del template, come in `{{povCharacter}}`. Funziona anche una macro senza argomenti (`user`, `summary`, `lastMessage`). |
| `.mood`, `$chapter` | Una [variabile](#variables) locale o globale; vuota quando non è impostata. |
| `( )` | Raggruppamento. |

Confronti:

| Operatore | Vero quando |
|---|---|
| `==`, `!=` | Uguale, diverso. I numeri sono confrontati per valore (`5 == 5.0`), tutto il resto esattamente, quindi `"Fiora" == "fiora"` è falso. |
| `~=` | Uguale, senza distinguere maiuscole e minuscole. |
| `<`, `<=`, `>`, `>=` | I numeri sono in quell'ordine. Falso quando un lato non è un numero. |
| `contains` | Il testo a sinistra contiene il testo a destra, senza distinguere maiuscole e minuscole: `presentCharacters contains "ma"` è vero anche per *Mara*. |
| `has` | La lista a sinistra, separata da virgole, punto e virgola o a capo, contiene l'elemento a destra, senza distinguere maiuscole e minuscole e senza tenere conto degli spazi attorno agli elementi: `presentCharacters has "Mara"` non è vero per *Marati*. |

Combinazioni: `and` (`&&`), `or` (`||`), `not` (`!`); `not` ha la precedenza più alta, poi `and`, poi `or`. Un valore da solo è vero a meno che non sia vuoto,
`0`, `false`, `no` o `off`. Le macro sono ammesse dentro un testo (`"{{user}}"`) e vengono calcolate per prime.

Un'espressione non valida stampa `Error`: un errore di sintassi, una parola senza virgolette che non è un nome (`povCharacter == Fiora`), un nome sconosciuto. Tutti i nomi vengono controllati, anche nella parte di un `and` / `or` che non verrebbe raggiunta, così un errore di battitura non può passare inosservato. In una [condizione del lorebook](../lorebooks/entries.md#a-condition-for-an-entry) un `Error` rende la voce non usata e l'anteprima lo segnala.

<a id="randomness"></a>

## Casualità

| Macro | Risultato |
|---|---|
| `{{random::red::green::blue}}` | Una delle opzioni, diversa ogni volta. Anche `{{random:red,green,blue}}` e `{{random}}red, green{{/random}}`. |
| `{{pick::sword::axe::bow}}` | Una delle opzioni, ma sempre **la stessa** per lo stesso libro e lo stesso punto del template. Usala per cose che devono restare coerenti, come un nome scelto a caso. |
| `{{roll::2d6+3}}` | Un lancio di dadi: `1d20`, `3d4-10`, `d100`; anche `{{roll d100}}`, `{{roll:6}}` (un dado a sei facce). I lanci non validi non danno niente. |

<a id="generation-info"></a>

## Informazioni sulla generazione

| Macro | Risultato |
|---|---|
| `{{model}}` | Il nome del [provider di inferenza](../inference-providers.md) del libro. |
| `{{maxContextTokens}}` | Il limite di contesto in token (del protocollo o del provider, vedi [Limiti](../protocols.md#limits)). Alias `{{maxContext}}`. |
| `{{maxResponseTokens}}` | Il limite della risposta in token. Alias `{{maxResponse}}`. |
| `{{maxPrompt}}` | Contesto meno risposta: lo spazio disponibile per il prompt. |
| `{{lastGenerationType}}` | `normal` per una nuova parte, `regenerate`, `swipe`, oppure `quiet` nei riassunti. |

<a id="commands"></a>

## Comandi

| Macro | Risultato |
|---|---|
| `{{command::name}}`, `{{command::name::argument::argument}}` | Ciò che un [comando](../commands/index.md) risponde, come testo. Si può usare solo un comando pensato per le macro (legge soltanto; un'estensione lo dichiara, per esempio `{{command::character-tracker-map}}` del [Character Tracker](../extensions/character-tracker.md)); qualsiasi altro nome stampa `Error`. Un comando che fallisce non stampa niente. |

Il comando viene eseguito quando la generazione raggiunge la macro (per niente quando si trova in un ramo di un `{{if}}` non preso) per il libro che viene generato, e solo nei prompt di una generazione (il prompt utente, le voci del lorebook e il template principale); altrove la macro non stampa niente. La risposta viene conservata insieme alla parte da cui continua la nuova parte: la generazione successiva dalla stessa parte e gli altri template della stessa generazione la usano invece di rieseguire il comando. **Rigenera** e **swipe** chiedono di nuovo il comando, così come ogni nuova parte, perché continua da una nuova ultima parte.

<a id="utility"></a>

## Utilità

| Macro | Risultato |
|---|---|
| `{{newline}}`, `{{newline::2}}` | Una o più interruzioni di riga. |
| `{{space}}`, `{{space::4}}` | Uno o più spazi. |
| `{{trim}}` | Rimuove le interruzioni di riga attorno a sé. Utile dopo le righe che impostano solo variabili. |
| `{{noop}}` | Niente. |
| `{{join::separator::a::b::c}}` | I testi dopo il primo, uniti con il primo come separatore, in qualsiasi numero (macro incluse): `{{join::-::{{user}}::{{scenario}}}}`. I testi vuoti vengono esclusi, così una macro opzionale non lascia separatori spuri. Gli spazi attorno a un argomento scritto vengono eliminati, quindi per un separatore con spazi usa una macro: `{{join::{{space}}::a::b}}`, `{{join::{{newline}}::a::b}}`. |
| `{{reverse::text}}` | Il testo al contrario. |
| `{{// comment}}` | Un commento, rimosso. `{{//}}` ... `{{///}}` commenta tutto ciò che sta tra i due, macro incluse. |
| `\{\{user\}\}` | Stampa `{{user}}` così com'è. |

<a id="sillytavern-only-macros"></a>

## Macro solo per SillyTavern

Le macro che hanno senso solo nelle chat di SillyTavern - le persona e i campi della scheda del personaggio (`{{persona}}`,
`{{personality}}`, `{{charPrompt}}`, `{{mesExamples}}`...), le sequenze della modalità instruct (`{{instructUserPrefix}}`...),
le note dell'autore, gli ID degli swipe, `{{outlet}}` e simili - sono riconosciute e stampano **niente**, così le informazioni del mondo importate non le fanno filtrare nel prompt. `{{isMobile}}` e `{{hasExtension}}` stampano `false`.

Una macro che Marginalia non conosce:

- con argomenti (`{{fooBar::x}}`) viene stampata così com'è, come fa SillyTavern;
- senza argomenti (`{{fooBar}}`) viene stampata come **`Error`**, come una [variabile del template](templates.md#typos) sconosciuta.

}%}
