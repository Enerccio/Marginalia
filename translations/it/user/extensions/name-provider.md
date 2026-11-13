---
label: Name provider
order: 6
---

# Name provider

Fornisce al modello nomi reali tra cui scegliere, invece di inventarli: i nomi di battesimo e i cognomi più comuni di molti
paesi. Mentre il [Generatore di nomi](name-generator.md) crea nomi nuovi che suonano come una categoria, questa
estensione distribuisce nomi che esistono, ciascuno con la grafia originale accanto a quella traslitterata: `Satō` e `佐藤`.

L'estensione non ha una schermata propria e non richiede alcuna configurazione. Altre estensioni offrono al modello le sue funzioni: il
[Generatore di lore](lore-generator.md#tools) e lo [Schema dell'arco narrativo](arc-outline.md), dove scegli quali liste il
modello può usare. La sua suite si chiama *Name provider* nella [finestra degli strumenti](tools.md), accanto a quella del *Generatore di nomi*, e puoi
attivare le liste di entrambe, di una sola o di nessuna.

<a id="what-the-model-can-do"></a>

## Cosa può fare il modello

| Funzione | Cosa fa |
|---|---|
| `list_real_name_lists` | Elenca le liste di nomi; con un `filter` solo quelle che contengono le sue parole nel nome. |
| `pick_real_names` | Sceglie a caso fino a 100 nomi diversi di una lista. Ogni nome è una coppia: il nome traslitterato e quello originale. |

Una lista con meno nomi di quelli richiesti restituisce tutti i nomi che contiene. Al modello viene indicato (vedi il suggerimento della suite *Name provider* nella [finestra degli strumenti](tools.md#choosing-the-tools)) di scegliere un nome tra quelli ricevuti e di scriverlo così come è stato restituito, con accanto l'originale quando si adatta all'ambientazione; può combinare un nome di battesimo con un cognome dello stesso paese.

<a id="commands"></a>

## Comandi

Gli stessi nomi sono disponibili come [comandi](../commands/index.md), per la riga di comando e per gli [agenti](../agents.md),
che possono usarli nei propri prompt (per esempio per `/generate-lore-entry` del [Generatore di lore](lore-generator.md)):

| Comando | Cosa fa |
|---|---|
| `/list-real-name-lists [<filter>]` | Elenca le liste come array JSON; con un filtro solo quelle il cui nome contiene tutte le parole indicate (ciascuna all'inizio di una parola, in qualsiasi ordine, senza distinzione tra maiuscole e minuscole). |
| `/pick-real-names list count` | Sceglie a caso fino a 100 nomi della lista e li restituisce come array JSON di coppie, il nome traslitterato e quello originale. |

Entrambi si limitano a leggere.

<a id="lists"></a>

## Liste

Ci sono circa 180 liste, con nomi del tipo `<Paese> - Surnames` e `<Paese> - Forenames`: cognomi di 75 paesi e nomi di battesimo di
106 (`Japan - Surnames`, `Czechia - Forenames`, `Kosovo - Surnames`). I nomi di battesimo di entrambi i generi stanno in un'unica lista. Una lista ha
da 5 a circa 150 nomi, i più comuni del paese. I nomi non vengono creati: un nome che ottieni è un nome che esiste davvero.

Alcune decine di voci sono **gruppi** di liste per lingua: `<Lingua> - Forenames` e `<Lingua> - Surnames` per lo spagnolo
(18 paesi), il portoghese, il francese, il tedesco, l'inglese, l'arabo, le lingue slave occidentali, orientali e meridionali, le lingue germaniche settentrionali, nordiche, uraliche,
baltiche, turche, persiane, cinese, sudasiatiche, sudorientali, caucasiche, albanese e rumena (dove esistono tali liste). Sono elencati come qualsiasi
altra lista e sono una voce nella [finestra degli strumenti](tools.md). Ogni nome di un gruppo viene scelto da una delle sue liste,
selezionata a caso, con uguale probabilità per ciascun paese; così una richiesta di 20 nomi ottiene nomi di più paesi, mai lo stesso nome
due volte. I paesi in cui si parlano più lingue (Belgio, Lussemburgo, Malta, Andorra) non fanno parte di alcun gruppo. Un gruppo corrisponde a una
riga in `name-groups.json`, vedi il [README del plugin](https://github.com/Enerccio/Marginalia/blob/master/marginalia/plugins/name-provider/README.md#groups).

I dati provengono dal [popular names by country dataset](https://github.com/sigpwned/popular-names-by-country-dataset) (CC0), che
li ricava dalle liste di Wikipedia dei cognomi più comuni e dei nomi di battesimo più popolari. I nomi scritti in
alfabeti non latini (cinese, giapponese, coreano, arabo, cirillico...) hanno una traslitterazione, presa dalla fonte o
generata con traduzione automatica, quindi verifica la grafia quando conta.

<a id="usage"></a>

## Utilizzo

Carica l'estensione in *Admin → Extensions*. Ha effetto dalla generazione successiva, anche se viene installata dopo le
estensioni che la usano o se viene scaricata e reinstallata. Senza di essa e senza le altre suite non compare il pulsante *Strumenti*.
