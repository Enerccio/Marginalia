---
label: Espressioni regolari
order: 935
icon: search
---

# Espressioni regolari

Un'espressione regolare (*regex*) è un breve modello che descrive il testo invece di scriverlo per esteso: "la parola Harry, ma
non Harrys", "due parole qualsiasi tra virgolette", "tre o più spazi di seguito". Marginalia le usa in due punti:

- **Cerca e sostituisci** della [storia](books/story-editor.md#search-and-replace) e di un
  [lorebook](lorebooks/entries.md#search-and-replace), e i relativi [comandi](commands/index.md), quando **Cerca con
  regex** è attivo. Il modello trova il testo, la sostituzione può riutilizzare parti di esso.
- Il filtro **Regex** di una [voce del lorebook](lorebooks/entries.md#activation), che decide quando la voce entra nel
  prompt.

Il dialetto è quello di Java. Questa pagina mostra ciò che userai più spesso; il riferimento completo è linkato alla fine.

!!!
Per le parole semplici non servono le regex. Con **Cerca con regex** disattivato il testo viene cercato esattamente come
lo scrivi, quindi un `.` o una `(` al suo interno sono semplicemente un punto o una parentesi. Attiva la regex solo quando
un unico testo fisso non basta.
!!!

<a id="the-building-blocks"></a>

## I mattoni di base

| Scrivi | Trova | Esempio |
|---|---|---|
| una lettera o una cifra | quel carattere | `cat` trova *cat* in *concatenare* |
| `.` | un carattere qualsiasi (non un a capo) | `c.t` trova *cat*, *cot*, *c7t* |
| `\d` `\w` `\s` | una cifra, una lettera/cifra/`_`, uno spazio o un a capo | `\d\d` trova *42* |
| `[abc]` `[a-z]` | uno dei caratteri elencati, o un intervallo | `gr[ae]y` trova *grey* e *gray* |
| `[^abc]` | un carattere che *non* è elencato | `[^"]` è qualsiasi cosa tranne le virgolette |
| `\b` | il confine di una parola | `\bcat\b` trova *cat* ma non *concatenare* |
| `^` `$` | l'inizio e la fine di una riga | `^Capitolo` trova *Capitolo* all'inizio di una riga |
| `x?` `x*` `x+` | `x` zero o una volta, un numero qualsiasi di volte, almeno una volta | `colou?r` trova *color* e *colour* |
| `x{2,4}` | `x` da due a quattro volte | `\.{3}` sono tre punti di seguito |
| `\` | il carattere successivo è preso alla lettera | `\.` è un punto, `\?` un punto interrogativo, `\(` una parentesi |

Le parentesi formano un **gruppo**, e una barra verticale dentro un gruppo significa "oppure": `(Mr|Mrs) Dursley` trova *Mr Dursley* e
*Mrs Dursley*, e `\b(Voldemort|You-Know-Who)\b` trova uno dei due nomi. Un `?`, `*` o `+` dopo un gruppo si applica a tutto il gruppo:
`(very )+good` trova *very good* e *very very good*.

I caratteri `. ? * + ^ $ | \ ( ) [ ] { }` hanno un significato proprio. Per cercarne uno come carattere normale,
mettigli davanti un `\`: per trovare *Davvero?!* scrivi `Davvero\?!`.

Di default una regex non ignora nulla: *harry* non trova *Harry*. Metti `(?i)` all'inizio per ignorare le maiuscole:
`(?i)harry` trova *Harry*, *HARRY* e *harry*. (Le finestre di Cerca e sostituisci bloccano **Distingui maiuscole e minuscole** mentre la regex
è attiva, quindi è così che lo disattivi lì; il filtro **Regex** del lorebook ignora sempre le maiuscole.)

Una corrispondenza di lunghezza zero (un modello come `a*` che può non trovare nulla) viene ignorata da Cerca e sostituisci.

<a id="typical-uses-in-a-story"></a>

## Usi tipici in una storia

Ogni riga è qualcosa che potresti voler fare su un libro. **Cerca** è ciò che digiti in *Cerca*, **Sostituisci** ciò che digiti in *Sostituisci con*.

| Cosa vuoi fare | Cerca | Sostituisci |
|---|---|---|
| Rinominare un personaggio, ma non una parte di una parola più lunga (*Harry* ma non *Harrys* o *Harrison*) | `\bHarry\b` | `Henry` |
| Rinominare, qualunque sia la maiuscola | `(?i)\bharry\b` | `Henry` |
| Rinominare un nome e il suo possessivo in una volta | `\bHarry('s)?\b` | `Henry$1` |
| Trasformare le virgolette dritte in virgolette tipografiche | `"([^"]*)"` | `“$1”` |
| Tre punti in un unico carattere di puntini di sospensione | `\.{3}` | `…` |
| Due o più spazi in uno | ` {2,}` | ` ` (uno spazio) |
| Rimuovere gli spazi alla fine delle righe | ` +$` | (vuoto) |
| Un trattino lungo fatto di due trattini | ` ?-- ?` | ` — ` |
| Scambiare *Cognome, Nome* in *Nome Cognome* | `(\w+), (\w+)` | `$2 $1` |
| Trovare dove è usata una parola, senza cambiare nulla | il modello | (lascia vuoto e premi solo **Cerca**) |

Per far diventare più grafie un unico nome, cerca `\b(Voldemort|You-Know-Who|He-Who-Must-Not-Be-Named)\b` e sostituisci con
`Tom Riddle`.

<a id="taking-the-match-into-the-replacement"></a>

### Portare la corrispondenza nella sostituzione

Tutto ciò che una regex mette tra parentesi tonde è un **gruppo**, numerato da sinistra secondo la sua parentesi di apertura: la sostituzione
`$1` è il testo del primo gruppo, `$2` del secondo, `$0` tutta la corrispondenza. In `(\w+), (\w+)` il primo gruppo è la
parola prima della virgola, il secondo quella dopo, quindi `$2 $1` le scambia: *Potter, Harry* diventa *Harry Potter*.

Puoi dare un nome a un gruppo, `(?<last>\w+), (?<first>\w+)`, e usarlo nella sostituzione: `${first} ${last}`.

Un `$` o un `\` che deve restare come sono nella sostituzione va escapato: per mettere *$5* nel testo, scrivi `\$5`.
Senza regex la sostituzione viene sempre presa esattamente come digitata.

Se il modello è sbagliato (una parentesi mai chiusa) o la sostituzione nomina un gruppo che il modello non ha, una notifica
lo segnala e non succede nulla; una ricerca non cambia mai nulla.

<a id="match-only-in-a-context"></a>

### Trovare solo in un contesto

Un *lookahead* `(?=...)` e un *lookbehind* `(?<=...)` guardano il testo intorno alla corrispondenza senza prenderlo, quindi viene
sostituita solo la parte al di fuori di essi:

| Cerca | Trova |
|---|---|
| `Harry(?= Potter)` | *Harry* solo quando segue *Potter* (e lascia *Potter* com'è) |
| `(?<=Professor )McGonagall` | *McGonagall* solo dopo *Professor* |
| `Harry(?! Potter)` | *Harry* non seguito da *Potter* |

<a id="lazy-and-greedy"></a>

### Pigro e avido

`*` e `+` prendono quanto possono: in `He said "yes" and "no"` il modello `".*"` trova tutto `"yes" and "no"`.
Aggiungi un `?` (`".*?"`), oppure indica ciò che non è ammesso (`"[^"]*"`), per fermarti alla prima virgoletta di chiusura.

La ricerca tratta ogni riga a sé: `.` non attraversa un a capo, e `^` e `$` corrispondono all'inizio e alla fine di ogni riga di una parte,
non solo dell'intero testo. Per far corrispondere un a capo scrivi `\n`.

<a id="in-a-lorebook-entry-filter"></a>

## Nel filtro di una voce del lorebook

Il filtro di una voce del lorebook impostata su **Regex** viene confrontato con la richiesta per la parte, ovunque in essa. Ignora
le maiuscole, e `.` attraversa qui i ritorni a capo, quindi `varro.*harbor` trova Varro e il porto anche su righe diverse.
Filtri tipici:

- `\bvarro\b` - Varro come parola intera.
- `\b(varro|captain)\b` - Varro o il capitano.
- `varro.*(harbor|docks)` - Varro e poi, più avanti, il porto o i moli.
- `dragons?` - *dragon* o *dragons*.
- `(?<!no )magic` - *magic*, a meno che non stia subito dopo *no*.

Una regex non valida non attiva mai la voce, e il motivo viene scritto nel log. Vedi
[voci e attivazione](lorebooks/entries.md#activation) per il resto di come una voce viene scelta.

<a id="in-the-commands"></a>

## Nei comandi

[`/search-replace-parts`](commands/index.md#commands-that-come-with-marginalia) e `/search-replace-lore-entries` accettano la regex come terzo argomento (`true`), dopo la ricerca e la sostituzione. La riga di comando si divide sugli spazi, quindi un modello o una sostituzione con uno spazio va messo tra virgolette doppie. Una barra rovesciata davanti a una lettera viene passata così com'è
(`\b`, `\d`), ma `\\` diventa una singola barra rovesciata e `\"` una virgoletta, quindi una barra rovesciata letterale nel modello è `\\\\`.

```
/search-replace-parts "\bHarry\b" Henry true
/search-replace-parts "(\w+), (\w+)" "$2 $1" true
/search-replace-parts "(?i)\bharry\b" Henry true
```

I comandi non mostrano un elenco delle corrispondenze: ti chiedono se sostituire il numero di corrispondenze trovate. Quando non sei
sicuro di un modello, provalo prima nella finestra **Cerca e sostituisci**, che mostra ogni corrispondenza così com'è e come sarà.

<a id="tips"></a>

## Suggerimenti

- Prova un modello con **Cerca** per primo e apri alcune delle righe: la finestra mostra ogni corrispondenza con il suo contesto,
  com'è e come sarà. Sostituisci solo quando il risultato è quello che volevi.
- Parti semplice e aggiungi al modello passo dopo passo; un modello che trova troppo di solito ha bisogno di `\b` attorno a una parola, o di `?`
  per fermare un `*`.
- Un [backup](books/backups.md) prima di una sostituzione grande costa poco (`/take-backup`), e una sostituzione non può essere fermata una volta
  iniziata.
- Riassunti modificati: quando la sostituzione tocca il testo da cui è stato creato un [riassunto](books/summaries.md), la finestra avvisa
  che il riassunto diventerebbe obsoleto; vedi [Cerca e sostituisci](books/story-editor.md#search-and-replace).

<a id="further-reading"></a>

## Approfondimenti

Questa pagina copre le basi. La sintassi completa di Java, con ogni costrutto, flag e classe di caratteri, si trova nella
[documentazione di `java.util.regex.Pattern`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/regex/Pattern.html),
e la sintassi di sostituzione (`$1`, `${name}`, `\$`) in
[`Matcher.appendReplacement`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/regex/Matcher.html#appendReplacement(java.lang.StringBuilder,java.lang.String)).
