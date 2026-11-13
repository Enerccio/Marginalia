---
label: Fornitore di luoghi
order: 5
---

# Fornitore di luoghi

Fornisce al modello luoghi reali della Terra da consultare invece di inventarli: città, paesi, mari e baie, fiumi, laghi,
isole, montagne, deserti, porti e aeroporti, con le loro coordinate e i paesi in cui si trovano. Può anche dire che cosa si trova
intorno a un luogo e quanto distano due luoghi, così che una storia ambientata sulla mappa reale abbia distanze e vicinanze corrette.

L'estensione non ha una schermata propria e non richiede alcuna configurazione. [Lore Generator](lore-generator.md#tools) e
[Arc Outline](arc-outline.md) la offrono al modello come suite *Place provider* del loro pulsante **Strumenti**
([Strumenti per il modello](tools.md)), attiva o disattivata; è disponibile anche come comandi per [agenti](../agents.md) e per la riga di comando.

<a id="what-it-knows"></a>

## Che cosa conosce

I dati provengono da [Natural Earth](https://www.naturalearthdata.com/) (di pubblico dominio), una mappa dei luoghi **noti**: circa
18 000, tra cui 4 900 città, 250 paesi, 4 500 province e stati, 1 100 fiumi, 500 laghi, 400 isole, 350 montagne e catene montuose,
1 000 porti e 900 aeroporti. Non è un dizionario geografico: un piccolo torrente o un villaggio non ci sono. Natural Earth
rappresenta inoltre la maggior parte delle insenature come baie, quindi chiedendo le insenature del Canada si ottengono le quattro
che contiene (Hamilton Inlet, Bathurst Inlet, Minto Inlet e Richard Collinson Inlet), non le centinaia della costa della Columbia Britannica.

<a id="commands"></a>

## Comandi

Tutti leggono soltanto.

| Comando | Che cosa fa |
|---|---|
| `/list-place-types` | Elenca i tipi di luogo (`city`, `capital`, `bay`, `inlet`, `river`, `mountain range`, `island`...) con quanti ce ne sono. |
| `/search-places query [type [country [limit]]]` | Cerca per nome, tipo e paese, con la corrispondenza migliore per prima. `*` come query indica qualsiasi nome: `/search-places * inlets Canada` elenca le insenature del Canada. |
| `/places-near place radius_km [type [limit]]` | I luoghi entro il raggio da un luogo o da un punto, dal più vicino, con la distanza in chilometri e la direzione. |
| `/distance-between from to` | La distanza in linea retta sulla Terra (non su strada) in chilometri e miglia, con la direzione. |

Un *luogo* è un nome (`Ottawa`; `Paris, France` quando più luoghi hanno lo stesso nome, la virgola e il paese o la provincia ne scelgono
uno) oppure coordinate (`45.42, -75.69`). Un *tipo* può essere scritto al plurale o come lo si dice comunemente: `inlets`, `mountains`, `towns`,
`states`. Un *paese* è un nome o un codice: `Canada`, `Czechia`, `CA`, `CAN`. I nomi sono confrontati senza distinguere maiuscole
e accenti. Vengono restituiti al massimo 100 luoghi (25 senza limite), e la risposta indica quanti ce ne sono in tutto.

Una ricerca per tipo trova anche i luoghi di un tipo affine legato all'acqua che hanno la parola nel nome: `inlet` trova Hamilton Inlet,
che Natural Earth classifica come baia.

<a id="using-it-from-an-agent"></a>

## Uso da un agente

Un agente a cui sono consentiti i comandi (sono nella selezione *All non-destructive* e nel ruolo *Auditor*) può richiamarli come
funzioni. Sono pensati per una richiesta del tipo "ambienta il primo capitolo in un villaggio di pescatori su un fiordo in Norvegia, a due giorni di mare
dalla capitale": l'agente cerca `fjords of Norway`, ne sceglie uno e chiede la distanza da Oslo.

<a id="usage"></a>

## Utilizzo

Carica l'estensione in *Admin → Extensions*. In Lore Generator e Arc Outline spunta *Usa questa suite* della suite Place
provider in **Strumenti**; il suo suggerimento indica al modello di consultare un luogo reale invece di indovinare dove si trova o quanto dista.
