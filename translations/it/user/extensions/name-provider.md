---
label: Fornitore di nomi
order: 6
---

# Fornitore di nomi

Fornisce al modello nomi reali tra cui scegliere, invece di inventarli: i nomi di battesimo e i cognomi più comuni di molti paesi. Il [Name generator](name-generator.md) crea nomi nuovi che suonano soltanto come una categoria, mentre questa estensione restituisce nomi che esistono, ciascuno con la grafia originale accanto a quella romanizzata: `Satō` e `佐藤`.

L'estensione non ha una schermata propria e non richiede alcuna configurazione. Altre estensioni ne offrono le funzioni al modello: il [Lore Generator](lore-generator.md#tools) e l'[Arc Outline](arc-outline.md), dove scegli quali liste il modello può usare. La sua suite è *Name provider* nella [finestra Strumenti](tools.md), accanto a quella di *Name generator*, e puoi attivare le liste di entrambe, di una sola delle due o di nessuna.

<a id="what-the-model-can-do"></a>

## Cosa può fare il modello

| Funzione | Cosa fa |
|---|---|
| `list_real_name_lists` | Elenca le liste di nomi; con un `filter` solo quelle che contengono le sue parole nel nome. |
| `pick_real_names` | Sceglie a caso fino a 100 nomi diversi di una lista. Ogni nome è una coppia: il nome romanizzato e quello originale. |

Una lista con meno nomi di quelli richiesti restituisce tutti quelli che contiene. Il modello riceve l'istruzione (vedi il suggerimento della suite *Name provider* nella [finestra Strumenti](tools.md#choosing-the-tools)) di scegliere un nome tra quelli ottenuti e di scriverlo così come è stato restituito, con l'originale accanto quando si adatta all'ambientazione; può combinare un nome di battesimo con un cognome dello stesso paese.

<a id="commands"></a>

## Comandi

Gli stessi nomi sono disponibili come [comandi](../commands/index.md), per la riga di comando e per gli [agenti](../agents.md), che possono usarli nei propri prompt (per esempio per `/generate-lore-entry` del [Lore Generator](lore-generator.md)):

| Comando | Cosa fa |
|---|---|
| `/list-real-name-lists [<filter>]` | Elenca le liste come array JSON; con un filtro, solo quelle il cui nome contiene tutte le parole indicate (ciascuna all'inizio di una parola, in qualsiasi ordine, senza distinguere maiuscole e minuscole). |
| `/pick-real-names list count` | Sceglie a caso fino a 100 nomi della lista e li restituisce come array JSON di coppie, il nome romanizzato e quello originale. |

Entrambi si limitano a leggere i dati.

<a id="lists"></a>

## Liste

Ci sono circa 180 liste, denominate `<Country> - Surnames` e `<Country> - Forenames`: cognomi di 75 paesi e nomi di battesimo di 106 (`Japan - Surnames`, `Czechia - Forenames`, `Kosovo - Surnames`). I nomi di entrambi i generi sono in un'unica lista. Ogni lista contiene da 5 a circa 150 nomi, i più comuni del paese. I nomi non vengono creati: un nome che ottieni è un nome che delle persone reali portano.

Alcune decine di voci sono **gruppi** di liste raggruppate per lingua: `<Language> - Forenames` e `<Language> - Surnames` per lo spagnolo (18 paesi), portoghese, francese, tedesco, inglese, arabo, slavo occidentale, orientale e meridionale, germanico settentrionale, nordico, uralico, baltico, turco, persiano, cinese, sud-asiatico, sud-est asiatico, caucasico, albanese e rumeno (dove esistono tali liste). Sono elencati come qualsiasi altra lista e compaiono come una sola voce nella [finestra Strumenti](tools.md). Ogni nome di un gruppo viene scelto a caso da una delle sue liste, con la stessa probabilità per ciascun paese: una richiesta di 20 nomi ottiene quindi nomi di più paesi, mai lo stesso nome due volte. I paesi in cui si parlano più lingue (Belgio, Lussemburgo, Malta, Andorra) non fanno parte di nessun gruppo. Un gruppo è una riga in `name-groups.json`, vedi il [README del plugin](https://github.com/Enerccio/Marginalia/blob/master/marginalia/plugins/name-provider/README.md#groups).

I dati provengono dal [dataset dei nomi popolari per paese](https://github.com/sigpwned/popular-names-by-country-dataset) (CC0), che li ricava dalle liste di Wikipedia dei cognomi più comuni e dei nomi di battesimo più diffusi. I nomi scritti in alfabeti non latini (cinese, giapponese, coreano, arabo, cirillico...) hanno una romanizzazione, presa dalla fonte o prodotta da traduzione automatica, quindi verifica la grafia quando conta.

<a id="usage"></a>

## Utilizzo

Carica l'estensione in *Admin → Extensions*. Si applica alla generazione successiva, anche quando viene installata dopo le estensioni che la usano o quando viene disattivata e poi reinstallata. Senza di essa e senza le altre suite non compare il pulsante *Strumenti*.
