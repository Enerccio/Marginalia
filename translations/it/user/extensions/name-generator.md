---
label: Generatore di nomi
order: 7
---

# Generatore di nomi

Offre al modello un modo per chiedere nomi invece di inventarli. I nomi provengono da liste di nomi reali - nomi di battesimo e cognomi di molti paesi, luoghi, divinità, mostri, farmaci, aziende, cibi e altro - e vengono rinnovati da una catena di Markov, così suonano come la categoria senza essere copie di essa.

L'estensione non ha una schermata propria e non richiede alcuna configurazione. Altre estensioni mettono le sue funzioni a disposizione del modello; al momento sono il [Generatore di lore](lore-generator.md#tools) e l'[Arc Outline](arc-outline.md), dove scegli quali categorie il modello può usare (vedi [Strumenti per il modello](tools.md)); la sua suite è *Generatore di nomi*, accanto al *Fornitore di nomi* del [Fornitore di nomi](name-provider.md), che restituisce nomi reali invece di nomi inventati.

![Scelta delle categorie di nomi nella finestra Strumenti del Generatore di lore](../../images/tools-dialog.png)

<a id="what-the-model-can-do"></a>

## Cosa può fare il modello

| Funzione | Cosa fa |
|---|---|
| `list_name_categories` | Elenca le categorie di nomi; con un `filter` solo quelle che hanno le sue parole nel nome. |
| `generate_names` | Crea fino a 100 nuovi nomi di una categoria. Facoltativamente con una lunghezza minima e massima. |

Senza una lunghezza, i nomi sono lunghi quanto i nomi di quella categoria di solito (una categoria di nomi di aziende lunghi dà nomi lunghi, una di nomi di battesimo brevi dà nomi brevi). Il modello può chiedere altre lunghezze tra 2 e 30 lettere. Una categoria piccola può restituire meno nomi di quelli richiesti, perché la catena finisce quelli diversi. Il modello è libero di scegliere uno dei nomi, modificarlo o combinarne due; vedi il suggerimento della suite *Generatore di nomi* nella [finestra degli strumenti](tools.md#choosing-the-tools).

<a id="commands"></a>

## Comandi

Gli stessi nomi sono disponibili come [comandi](../commands/index.md), per la riga di comando e per gli [agenti](../agents.md), che possono usarli nei propri prompt (per esempio per `/generate-lore-entry` del [Generatore di lore](lore-generator.md)):

| Comando | Cosa fa |
|---|---|
| `/list-name-categories [<filter>]` | Elenca le categorie come array JSON; con un filtro solo quelle il cui nome contiene tutte le sue parole (ciascuna all'inizio di una parola, in qualsiasi ordine, senza distinguere maiuscole e minuscole: `chad male` trova *Africa - Central Africa - Chad - Male* ma non *…Female*). |
| `/generate-names category count [min_length [max_length]]` | Crea fino a 100 nomi della categoria e li restituisce come array JSON. Ogni volta nomi diversi. |

Entrambi si limitano a leggere.

<a id="categories"></a>

## Categorie

Ci sono circa 1270 categorie. I loro nomi dicono di cosa si tratta:

- **Nomi di battesimo e cognomi del mondo**, dall'Onomastikon di Kate Monk: `Europe Western - France - Surnames`,
  `Ancient World - Egypt - Male`, `India - Hindu Gods - Goddesses`. Ne sono alcune centinaia.
- **Cognomi di 75 paesi**, i cognomi traslitterati dal [dataset dei nomi popolari per paese](https://github.com/sigpwned/popular-names-by-country-dataset)
  (CC0): `Armenia - Surnames`, `Japan - Surnames`, `Kosovo - Surnames`. Sono liste piccole (da 6 a circa 150 nomi), quindi le
  catene restano brevi e i nomi nuovi sono vicini a quelli reali; per quelli reali usa il [Fornitore di nomi](name-provider.md).
- **Liste da altre raccolte**: dèi, angeli, demoni, antiche città e stati, imperatori, pianeti (fantasygen),
  Corpora (animali, tarocchi, nomi tolkieniani, casate di Game of Thrones, farmaci, ospedali, tessuti, gemme, linguaggi di
  programmazione...) e markov-namegen-lib (Pokémon, draghi, cocktail, banche, toponimi romani...).

<a id="groups"></a>

### Gruppi

Circa 170 voci sono **gruppi** di categorie. Riuniscono le tante piccole liste di una lingua, così chiedi
`German - Male` invece di scegliere tra *Germany - Germanic - Male*, *Germany - Biblical - Male*, *Austria - First names - Male*
e le altre. Sono elencati come qualsiasi categoria, e un gruppo è una voce nella [finestra degli strumenti](tools.md). I gruppi:

- **Per lingua**, ciascuno come `- Male`, `- Female` e `- Surnames` dove esistono tali liste: tedesco, francese, spagnolo (i
  cognomi includono l'America Latina), italiano, portoghese, olandese, scandinavo, finnico, baltico, irlandese, scozzese, gallese, celtico (tutti
  insieme), slavo occidentale, orientale e meridionale, greco, ungherese, rumeno, albanese, caucasico, turcico, persiano, arabo, ebraico, indù
  (le quattro liste alfabetiche di nomi maschili come una sola), sudasiatico, sudorientale asiatico, malese-indonesiano, polinesiano e le regioni
  africane (occidentale, orientale, meridionale, settentrionale, centrale). `Chinese`, `Japanese`, `Korean` e `Vietnamese` hanno `- Surnames`.
- **Qualsiasi genere**: `<Language> - Forenames` (`German - Forenames`, `Spanish - Forenames`, `Celtic - Forenames`...) riunisce le
  liste maschili e femminili della lingua e le sue liste di nomi per entrambi i generi, quindi una chiamata restituisce nomi maschili e femminili mescolati
  (le proporzioni seguono le dimensioni delle liste, per lo più tra la metà e tre quarti di uomini). Anche cinese, giapponese, coreano e vietnamita
  lo hanno. Le lingue in cui il mescolamento sarebbe sbilanciato (turcico, sudasiatico, africano occidentale e meridionale, malese-indonesiano) hanno solo
  i gruppi maschile e femminile.
- **Del passato**: anglosassone, germanico medievale, inglese medievale, Vicino Oriente antico.
- **Nativi americani**: `Native American - Male`, `- Female` e `- Surnames` (le categorie nordamericane dell'Onomastikon:
  Apache, Sioux, Irochesi, Cherokee, Inuit...) e lo stesso per l'America centrale e meridionale (azteco, maya, inca, amazzonico).
- **Altro**: `English - Surnames` (patronimici, mestieri, luoghi...), `Saints`, `Deities`, `Fantasy Creatures`, `Game of Thrones Houses`.

Per ogni nome l'estensione sceglie prima a caso una delle categorie del gruppo, e poi crea il nome con la catena di quella
categoria (con le sue lunghezze abituali). Un nome suona come un popolo e una lingua invece che come una miscela di tutti, e una
chiamata restituisce nomi di più di uno. Le liste dei paesi, divise per origine o per lettera, sono scelte in base alle
dimensioni (una lista di 600 nomi viene scelta più spesso di una da 10); nei gruppi di lingue e paesi interi (turcico,
caucasico, baltico, polinesiano, le regioni africane, nativi americani...) ogni membro ha la stessa probabilità. Le fonti di alcuni
gruppi sono brevi e in parte traduzioni inglesi (*Calf-Woman*), quindi i nomi sono grezzi.
Un gruppo è una riga di `name-groups.json` (i membri e il modo in cui sono scelti), vedi il [README del plugin](https://github.com/Enerccio/Marginalia/blob/master/marginalia/plugins/name-generator/README.md#groups).

Ogni categoria ha una catena della lunghezza che le si adatta meglio (le liste corte hanno catene corte, che ripetono meno gli originali)
e le lunghezze che di solito hanno i suoi nomi. Le liste in sé non fanno parte di Marginalia: lo sono solo le
probabilità, quindi i nomi originali non possono essere letti dall'estensione. Alcune fonti hanno licenze proprie, vedi il
[README del plugin](https://github.com/Enerccio/Marginalia/blob/master/marginalia/plugins/name-generator/README.md).

<a id="usage"></a>

## Utilizzo

Carica l'estensione in *Admin → Extensions*. Si applica alla prossima generazione; le estensioni che la usano la trovano al caricamento, anche se è installata dopo di loro o disinstallata e reinstallata - non serve riavviare il Generatore di lore. Senza di essa e le altre suite, il Generatore di lore non ha il pulsante *Strumenti*.
