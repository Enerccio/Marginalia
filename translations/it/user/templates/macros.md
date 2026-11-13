---
label: Riferimento macro
order: 90
---

# Riferimento macro

Marginalia riconosce le macro di [SillyTavern](https://docs.sillytavern.app/usage/core-concepts/macros/), quindi
prompt e world info scritti per SillyTavern funzionano senza modifiche. Le macro funzionano in ogni
[template](templates.md): template principale, prompt utente, prompt di riepilogo e voci del lorebook.

{%{

<a id="syntax"></a>

## Sintassi

| Forma | Esempio |
|---|---|
| senza argomenti | `{{user}}` |
| argomenti separati da `::` | `{{setvar::mood::nervous}}` |
| primo argomento dopo uno spazio | `{{getvar mood}}`, `{{setvar greeting::Hello}}` |
| singolo `:` (forma legacy) | `{{getvar:mood}}`, `{{random:north,south}}` |
| macro annidate | `{{getvar::{{user}}_mood}}` |
| con ambito (contenuto come ultimo argomento) | `{{setvar backstory}}Testo lungo...{{/setvar}}` |

- I nomi delle macro non distinguono maiuscole e minuscole: `{{User}}` e `{{USER}}` funzionano.
- Gli spazi attorno a nomi e argomenti vengono ignorati: `{{ getvar :: mood }}`.
- Gli `::` in più finiscono nell'ultimo argomento: `{{setvar::x::a::b}}` imposta `x` a `a::b`.
- Il contenuto con ambito viene valutato per primo, poi privato dell'indentazione comune e ripulito dagli spazi
  iniziali e finali. Con `#` dopo le parentesi graffe, `{{#setvar name}}...{{/setvar}}`, lo spazio bianco viene
  mantenuto esattamente.

<a id="names-and-participants"></a>

## Nomi e partecipanti

Marginalia non ha personas né schede personaggio; i nomi vengono dalle istruzioni del turno della parte in scrittura.

| Macro | Risultato |
|---|---|
| `{{user}}` | Il personaggio POV. |
| `{{char}}` | Il personaggio POV. Alias `{{charIfNotGroup}}`. |
| `{{group}}` | I personaggi presenti, oppure il personaggio POV se non ne sono indicati. Alias `{{groupNotMuted}}`. |
| `{{notChar}}` | I personaggi presenti tranne il personaggio POV (separati da virgola o da a capo, uniti con `, `). |
| `{{description}}` | La descrizione del libro. |
| `{{scenario}}` | L'ambientazione della scena. |
| `{{input}}` | Le istruzioni per la parte. |

Funzionano anche i marcatori di SillyTavern `<USER>`, `<BOT>`, `<CHAR>`, `<GROUP>` e `<CHARIFNOTGROUP>` (in qualsiasi
maiuscolo o minuscolo).

Con personaggio POV *Alice* e personaggi presenti *Alice, Bob, Carol*:
`{{user}}` → *Alice*, `{{group}}` → *Alice, Bob, Carol*, `{{notChar}}` → *Bob, Carol*.

<a id="story-history"></a>

## Storia del racconto

| Macro | Risultato |
|---|---|
| `{{lastMessage}}` | Il testo dell'ultima parte del ramo attivo. Alias `{{lastCharMessage}}`. |
| `{{lastMessageId}}` | La posizione dell'ultima parte, contata da 0. |
| `{{lastUserMessage}}` | Le istruzioni dell'ultima parte. |
| `{{summary}}` | Il [riepilogo](../books/summaries.md) più recente del ramo attivo. |
| `{{closest-message}}`, `{{closest-message::n}}` | La parte più vicina a quella in scrittura: l'ultima, oppure la *n*-esima contando indietro da essa (`::2` è quella prima dell'ultima). Vuota quando non esiste una tale parte. Alias `{{closestMessage}}`. |
| `{{closest-summary}}`, `{{closest-summary::n}}` | Lo stesso per i riepiloghi del ramo: il più recente, oppure l'*n*-esimo contando indietro. Alias `{{closestSummary}}`. |
| `{{current-message}}` | Il testo della parte che *Rigenera* o una variante sostituisce; vuota per una parte nuova. Alias `{{currentMessage}}`. |

Per la prima parte di un libro sono vuote. Per *Rigenera*, "ultima parte" è quella precedente la parte rigenerata.

Il numero di `closest-message` e `closest-summary` può essere a sua volta una macro, quindi può venire da una variabile:
`{{setvar::back::3}}{{closest-message::{{getvar::back}}}}` è la terza parte dalla fine. Un numero che non sia un intero
positivo non produce nulla. `closest-summary` conta i riepiloghi delle parti del ramo, includendo un [meta-riepilogo](../books/summaries.md)
e i riepiloghi che sostituisce; `{{summary}}` è uguale a `{{closest-summary}}`.

<a id="date-and-time"></a>

## Data e ora

| Macro | Risultato di esempio |
|---|---|
| `{{time}}` | `2:30 PM` |
| `{{time::UTC+2}}` | `4:30 PM` - l'ora con uno scarto UTC (funziona anche `UTC-5:30`) |
| `{{date}}` | `March 15, 2026` |
| `{{weekday}}` | `Sunday` |
| `{{isodate}}` | `2026-03-15` |
| `{{isotime}}` | `14:30` |
| `{{datetimeformat::YYYY-MM-DD HH:mm}}` | `2026-03-15 14:30` - [token di formato moment.js](https://momentjs.com/docs/#/displaying/format/), il testo tra `[parentesi quadre]` viene stampato così com'è: `{{datetimeformat::dddd [the] D. MMMM}}` → `Sunday the 15. March` |
| `{{idleDuration}}` | `2 hours` - tempo trascorso dalla scrittura dell'ultima parte, `just now` per la prima parte. Alias `{{idle_duration}}`. |
| `{{timeDiff::2026-01-01 12:00::2026-01-01 10:00}}` | `in 2 hours` - la differenza tra due orari; funzionano `now`, le date (`2026-01-08`) e gli orari ISO. I valori sconosciuti non producono nulla. |

Gli orari sono nel fuso orario del computer o del server su cui gira Marginalia, e sono sempre in inglese. Per la
[versione Docker](../getting-started/docker-server.md), imposta il fuso orario con la variabile d'ambiente `TZ`, ad
esempio `TZ=Europe/Prague`.

<a id="variables"></a>

## Variabili

Le variabili conservano valori tra una parte e l'altra: contatori, l'umore di un personaggio, fatti che la storia ha
stabilito. Ce ne sono due tipi:

- le variabili **locali** appartengono al ramo della storia: ogni parte ricorda i loro valori, e una nuova parte inizia
  con i valori della parte precedente;
- le variabili **globali** appartengono al libro e sono condivise da tutti i rami.

Vedi [Cosa viene conservato tra le parti](templates.md#what-is-kept-between-parts).

| Locale | Globale | |
|---|---|---|
| `{{getvar::name}}` | `{{getglobalvar::name}}` | Il valore; vuoto se non impostato. |
| `{{setvar::name::value}}` | `{{setglobalvar::name::value}}` | Imposta il valore. Non stampa nulla. |
| `{{addvar::name::value}}` | `{{addglobalvar::name::value}}` | Somma un numero, oppure aggiunge testo quando uno dei due non è un numero. Non stampa nulla. |
| `{{incvar::name}}` | `{{incglobalvar::name}}` | Somma 1 e stampa il nuovo valore. Un valore che non è un numero parte da 0. |
| `{{decvar::name}}` | `{{decglobalvar::name}}` | Sottrae 1 e stampa il nuovo valore. |
| `{{hasvar::name}}` | `{{hasglobalvar::name}}` | `true` o `false`. |
| `{{deletevar::name}}` | `{{deleteglobalvar::name}}` | Rimuove la variabile. Alias `flushvar` / `flushglobalvar`. |

I numeri vengono stampati senza zeri finali: `1.50` più `1.5` dà `3`.

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

Valori di ripiego e predefiniti:

- `{{.name || Guest}}` stampa *Guest* quando la variabile è vuota o falsa (`0`, `false`, `no`, `off`);
- `{{.name ?? Guest}}` stampa *Guest* solo quando la variabile non è impostata affatto;
- `{{.title ||= Wanderer}}` imposta la variabile a *Wanderer* se è vuota o falsa, poi la stampa;
- `{{.title ??= Wanderer}}` la imposta solo quando non è impostata affatto, poi la stampa.

I valori possono contenere macro: `{{.log += -> {{user}}}}`.

Un contatore di parti, ad esempio, in cima al prompt utente (`+=` non stampa nulla, e `{{trim}}` rimuove la riga vuota
che lascia):

```handlebars
{{.part += 1}}{{trim}}
Questa è la parte {{.part}} della storia.
```

<a id="conditionals"></a>

## Condizionali

```handlebars
{{if presentCharacters}}
  Presenti: {{presentCharacters}}
{{else}}
  {{user}} è solo.
{{/if}}
```

La condizione può essere:

| Condizione | Vera quando |
|---|---|
| il nome di una macro - `{{if user}}` | la macro produce un valore |
| una variabile - `{{if .mood}}`, `{{if $chapter}}` | la variabile ha un valore |
| una variabile del template - `{{if sceneSetting}}` | la variabile ha un valore |
| un confronto - `{{if .hp > 20}}` | il confronto è vero |
| un testo qualsiasi - `{{if yes}}` | sempre, tranne per i valori falsi |
| una negazione - `{{if !user}}`, `{{if !.mood}}` | l'opposto |

Sono falsi un valore vuoto e `0`, `false`, `no`, `off` (in qualsiasi maiuscolo o minuscolo). Tutto il resto è vero.

- La forma breve `{{if user::testo}}` stampa *testo* quando la condizione è vera.
- Viene valutato solo il ramo che viene preso: un `{{setvar}}` nell'altro ramo non viene eseguito.
- I blocchi `{{if}}` vengono privati dell'indentazione comune e ripuliti dagli spazi come le macro con ambito, quindi
  possono essere indentati per leggibilità. `{{#if}}`
  (il [Handlebars if](templates.md#conditions)) mantiene lo spazio bianco.
- Un `{{if}}` senza `{{/if}}` viene stampato come testo.
<a id="expressions"></a>

## Espressioni

`{{eval::expression}}` restituisce `true` o `false`. Usalo dove serve un confronto che `{{if}}` non sa fare: sui valori del modello (non solo sulle variabili), unendo i casi con `and`, `or` e `not`:

```handlebars
{{eval::povCharacter == "Fiora" and presentCharacters has "Mara"}}
{{if {{eval::.hp < 10 or not .armed}} }}Fiora is in danger.{{/if}}
```

| In un'espressione | Significato |
|---|---|
| `"testo"`, `'testo'` | Un testo. **Un testo va sempre tra virgolette**: una parola senza virgolette è un nome. |
| `12`, `-3.5`, `true`, `false` | Un numero o un valore di verità. |
| `povCharacter`, `presentCharacters`, `sceneSetting`... | Un valore del modello, come in `{{povCharacter}}`. Funziona anche una macro senza argomenti (`user`, `summary`, `lastMessage`). |
| `.mood`, `$chapter` | Una [variabile](#variables) locale o globale; vuota quando non è impostata. |
| `( )` | Raggruppamento. |

Confronti:

| Operatore | Vero quando |
|---|---|
| `==`, `!=` | Uguali, diversi. I numeri si confrontano per valore (`5 == 5.0`), il resto esattamente, quindi `"Fiora" == "fiora"` è falso. |
| `~=` | Uguali, ignorando maiuscole e minuscole. |
| `<`, `<=`, `>`, `>=` | I numeri sono in quell'ordine. Falso quando un lato non è un numero. |
| `contains` | Il testo a sinistra contiene al suo interno il testo a destra, ignorando maiuscole e minuscole: `presentCharacters contains "ma"` è vero anche per *Mara*. |
| `has` | La lista a sinistra, separata da virgole, punti e virgola o a capo, contiene l'elemento a destra, ignorando maiuscole e minuscole e gli spazi attorno agli elementi: `presentCharacters has "Mara"` non è vero per *Marati*. |

Unione: `and` (`&&`), `or` (`||`), `not` (`!`); `not` lega più di tutti, poi `and`, poi `or`. Un valore da solo è vero a meno che non sia vuoto, `0`, `false`, `no` o `off`. Le macro sono ammesse dentro un testo (`"{{user}}"`) e vengono risolte per prime.

Un'espressione non valida stampa `Error`: un errore di sintassi, una parola senza virgolette che non è un nome (`povCharacter == Fiora`), un nome sconosciuto. Tutti i nomi vengono controllati, anche nella parte di un `and` / `or` che non verrebbe raggiunta, così un refuso non può nascondersi. In una [condizione di lorebook](../lorebooks/entries.md#a-condition-for-an-entry) un `Error` rende la voce inutilizzata e l'anteprima lo segnala.

<a id="randomness"></a>

## Casualità

| Macro | Risultato |
|---|---|
| `{{random::rosso::verde::blu}}` | Una delle opzioni, diversa ogni volta. Anche `{{random:rosso,verde,blu}}` e `{{random}}rosso, verde{{/random}}`. |
| `{{pick::spada::ascia::arco}}` | Una delle opzioni, ma sempre **la stessa** per lo stesso libro e lo stesso punto del modello. Usala per le cose che devono restare coerenti, come un nome scelto a caso. |
| `{{roll::2d6+3}}` | Un tiro di dado: `1d20`, `3d4-10`, `d100`; anche `{{roll d100}}`, `{{roll:6}}` (un dado a sei facce). I tiri non validi non danno nulla. |

<a id="generation-info"></a>

## Informazioni sulla generazione

| Macro | Risultato |
|---|---|
| `{{model}}` | Il nome del [fornitore di inferenza](../inference-providers.md) del libro. |
| `{{maxContextTokens}}` | Il limite di contesto in token (del protocollo o del fornitore, vedi [Limiti](../protocols.md#limits)). Alias `{{maxContext}}`. |
| `{{maxResponseTokens}}` | Il limite della risposta in token. Alias `{{maxResponse}}`. |
| `{{maxPrompt}}` | Contesto meno risposta: lo spazio disponibile per il prompt. |
| `{{lastGenerationType}}` | `normal` per una nuova parte, `regenerate`, `swipe`, o `quiet` nei riassunti. |

<a id="commands"></a>

## Comandi

| Macro | Risultato |
|---|---|
| `{{command::nome}}`, `{{command::nome::argomento::argomento}}` | Ciò che risponde un [comando](../commands/index.md), come testo. Si può usare solo un comando pensato per le macro (legge soltanto; un'estensione lo dichiara, per esempio `{{command::character-tracker-map}}` del [Character Tracker](../extensions/character-tracker.md)); qualsiasi altro nome stampa `Error`. Un comando che fallisce non stampa nulla. |

Il comando viene eseguito quando la generazione raggiunge la macro (per niente quando si trova in un ramo di un `{{if}}` non seguito) per il libro che viene generato, e solo nei prompt di una generazione (il prompt utente, le voci del lorebook e il modello principale); altrove la macro non stampa nulla. La risposta viene conservata con la parte da cui continua la nuova parte: la generazione successiva dalla stessa parte e gli altri modelli della stessa generazione la usano invece di eseguire di nuovo il comando. **Rigenera** e lo swipe chiedono di nuovo il comando, e così fa ogni nuova parte, perché continua da una nuova ultima parte.

<a id="utility"></a>

## Utilità

| Macro | Risultato |
|---|---|
| `{{newline}}`, `{{newline::2}}` | Uno o più a capo. |
| `{{space}}`, `{{space::4}}` | Uno o più spazi. |
| `{{trim}}` | Rimuove gli a capo attorno a sé. Utile dopo le righe che impostano solo variabili. |
| `{{noop}}` | Niente. |
| `{{join::separatore::a::b::c}}` | I testi dopo il primo, uniti con il primo come separatore tra l'uno e l'altro, in qualsiasi numero (anche con macro): `{{join::-::{{user}}::{{scenario}}}}`. I testi vuoti vengono esclusi, così una macro facoltativa non lascia separatori spuri. Gli spazi attorno a un argomento scritto vengono eliminati, quindi per un separatore con spazi usa una macro: `{{join::{{space}}::a::b}}`, `{{join::{{newline}}::a::b}}`. |
| `{{reverse::testo}}` | Il testo al contrario. |
| `{{// commento}}` | Un commento, rimosso. `{{//}}` ... `{{///}}` commenta tutto ciò che sta tra i due, macro incluse. |
| `\{\{user\}\}` | Stampa `{{user}}` alla lettera. |

<a id="sillytavern-only-macros"></a>

## Macro solo per SillyTavern

Le macro che hanno senso solo nelle chat di SillyTavern - persona e campi della scheda personaggio (`{{persona}}`, `{{personality}}`, `{{charPrompt}}`, `{{mesExamples}}`...), sequenze della modalità instruct (`{{instructUserPrefix}}`...), note dell'autore, ID degli swipe, `{{outlet}}` e simili - sono riconosciute e non stampano **nulla**, così il world info importato non porta quelle macro nel prompt. `{{isMobile}}` e `{{hasExtension}}` stampano `false`.

Una macro che Marginalia non conosce:

- con argomenti (`{{fooBar::x}}`) viene stampata così com'è, come fa SillyTavern;
- senza argomenti (`{{fooBar}}`) viene stampata come **`Error`**, allo stesso modo di una [variabile del modello](templates.md#typos) sconosciuta.

}%}
