---
label: App desktop
order: 100
---

# App desktop

L'app desktop è una versione autonoma di Marginalia per un solo computer. Include il proprio runtime Java e il
server, risponde solo sul tuo computer e si apre nel browser predefinito.

## Download

Scarica il programma di installazione o l'archivio per il tuo sistema dalla
[pagina delle release](https://github.com/Enerccio/Marginalia/releases). Per ogni sistema vengono pubblicate quattro varianti:

| File | Contenuto |
|---|---|
| `marginalia-<version>-<os>-<arch>.deb` / `.dmg` / `.msi` | Il programma di installazione: `.deb` su Linux (Debian, Ubuntu e derivate), `.dmg` su macOS, `.msi` su Windows |
| `marginalia-<version>-<os>-<arch>-full.deb` / `.dmg` / `.msi` | Lo stesso programma di installazione **con tutti i [plugin](../extensions/index.md) integrati** già installati (vedi sotto) |
| `marginalia-<version>-<os>-<arch>-app` | L'app: `Marginalia.app` su macOS, `Marginalia.exe` su Windows, `bin/Marginalia` su Linux |
| `marginalia-<version>-<os>-<arch>` | Una cartella portabile avviata con uno script, con [opzioni](#options) da riga di comando |

Le build sono disponibili per Windows x64, macOS (Apple Silicon e Intel) e Linux (x64 e arm64). `<arch>` nel
nome del file indica `x86_64` per i processori Intel e AMD a 64 bit (Windows, macOS Intel, Linux) e `aarch64` per ARM
(Apple Silicon, Linux arm64).

<a id="install-and-start"></a>

## Installazione e avvio

+++ macOS
Con il programma di installazione: apri la `.dmg` e trascina *Marginalia* in *Applicazioni*. Con l'archivio:

1. Estrai l'archivio e sposta `Marginalia.app` in *Applicazioni*.
2. Aprilo. L'app non è ancora firmata, quindi la prima volta macOS ne rifiuta l'apertura. Fai clic destro sull'app e scegli
   *Apri*, oppure esegui:

   ```sh
   xattr -dr com.apple.quarantine /Applications/Marginalia.app
   ```
+++ Windows
Con il programma di installazione: esegui `.msi`. Si installa per il tuo utente (non servono i diritti di amministratore), aggiunge Marginalia al menu Start e al desktop, e una `.msi` più recente aggiorna la versione installata (i tuoi dati restano in `.marginalia`). Con l'archivio:

1. Estrai l'archivio.
2. Esegui `Marginalia\Marginalia.exe`.
3. SmartScreen può segnalare un editore sconosciuto. Fai clic su *Ulteriori informazioni* → *Esegui comunque*.
+++ Linux
Con il `.deb` (Debian, Ubuntu e derivate):

```sh
sudo apt install ./marginalia-<version>-linux-<arch>.deb
```

Si installa in `/opt/marginalia` e aggiunge *Marginalia* al menu delle applicazioni; `sudo apt remove marginalia` lo rimuove (i tuoi dati in `~/.marginalia` restano). Con l'archivio:

1. Estrai l'archivio.
2. Esegui `Marginalia/bin/Marginalia`.
+++ Cartella portabile
1. Estrai l'archivio.
2. Esegui `bin/marginalia` (`bin\marginalia.bat` su Windows).
+++

L'avvio richiede qualche secondo. Marginalia si apre poi nel browser all'indirizzo `http://127.0.0.1:8765` e mostra la
finestra di [primo avvio](first-start.md).

<a id="installer-with-the-plugins"></a>

### Programma di installazione con i plugin

Il programma di installazione **completo** (`-full` nel nome del file) è la stessa app e include tutti i plugin che fanno parte di Marginalia
(i provider Anthropic e Google, Reviewer, Beta Readers, Side Query, Arc Outline, i generatori e gli altri). La prima volta che
Marginalia si avvia, i plugin vengono copiati in `~/.marginalia/extensions` e compaiono in *Admin → Extensions*;
con il programma di installazione semplice li installi tu stesso da lì, uno alla volta.

- Un plugin si installa una sola volta. Se lo rimuovi da *Admin → Extensions*, resta rimosso anche dopo l'aggiornamento a una
  versione più recente di Marginalia (che porta i plugin più recenti e li affianca a quelli vecchi; Marginalia usa il
  più recente e conserva i JAR più vecchi in `extensions/old-versions`). Un plugin nuovo in un aggiornamento viene installato.
- Installare il programma completo sopra quello semplice (o viceversa) mantiene i tuoi dati e le impostazioni, e così ogni
  aggiornamento.
- Il programma di installazione semplice non rimuove i plugin. I plugin che hai installato tu non vengono mai toccati.

<a id="tray-icon"></a>

## Icona nell'area di notifica

Mentre è in esecuzione, Marginalia mostra la sua icona (una pagina di manoscritto con un piccolo albero ramificato nel margine) nell'area di notifica di sistema (nella barra dei menu su macOS). Il suo menu ha due
voci:

- **Apri Marginalia** - apre di nuovo Marginalia nel browser, per esempio dopo che hai chiuso la scheda.
- **Esci da Marginalia** - arresta il server. Chiudere la scheda del browser non arresta Marginalia.

Avviare l'app una seconda volta mentre è già in esecuzione si limita ad aprirla nel browser.

![L'icona nell'area di notifica e il suo menu](../../images/desktop-tray.png)

<a id="where-your-data-is"></a>

## Dove si trovano i tuoi dati

| Cosa | Dove |
|---|---|
| Database, backup, extension | `~/.marginalia` (`%USERPROFILE%\.marginalia` su Windows) |
| Log | `~/.marginalia/desktop/logs/marginalia.log` (l'esecuzione precedente viene conservata come `marginalia.log.1`) |

Per spostare Marginalia su un altro computer, chiudilo e copia la cartella `.marginalia` (insieme a `secret.key`, che
decifra le chiavi API salvate). Prima di modifiche importanti, fai un
[backup del database](../administration/database-backups.md).

<a id="options"></a>

## Opzioni

Il launcher portabile accetta queste opzioni:

```
marginalia --port 8765        # port to listen on; a free one is picked when 8765 is taken
marginalia --host 127.0.0.1   # address to listen on
marginalia --home /path       # directory that holds the .marginalia folder (default: your home directory)
marginalia --no-browser       # don't open the browser
marginalia --no-tray          # no tray icon; stop with Ctrl+C
marginalia --help
```

!!!warning Altri indirizzi
Con `--host 0.0.0.0` (o un altro indirizzo) Marginalia è raggiungibile dalla tua rete tramite HTTP non cifrato. Per l'accesso da
altri dispositivi usa invece il [server](docker-server.md) dietro HTTPS.
!!!

<a id="updating"></a>

## Aggiornamento

Chiudi Marginalia, sostituisci l'app (o la cartella portabile) con la nuova versione e avviala di nuovo. I tuoi dati restano in
`.marginalia` e vengono aggiornati automaticamente al primo avvio; prima che la struttura del database cambi,
Marginalia salva una [copia](../administration/database-backups.md#copy-before-an-upgrade) in `db-backups`. Le extension sono compilate per una versione di Marginalia, quindi
aggiornale nello stesso momento (vedi [Gestione delle extension](../administration/extensions.md)).

<a id="uninstalling"></a>

## Disinstallazione

Elimina l'app o la cartella. Per rimuovere anche i tuoi dati, elimina la cartella `.marginalia`.
