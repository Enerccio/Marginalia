---
label: Utenti
order: 100
---

# Utenti

La scheda **Utenti** della pagina di amministrazione elenca tutti gli account con il loro **Nome utente**, il **Nome completo** e l'indicazione se sono un **Amministratore**.

![La scheda Utenti](../../images/admin-users.png)

<a id="adding-a-user"></a>

## Aggiungere un utente

1. Fai clic su **Aggiungi utente**.
2. Compila **Nome utente**, **Nome completo**, **Password** e **Ripeti la password**.
3. Seleziona **Amministratore** se l'utente deve gestire l'installazione.
4. Fai clic su **OK**.

Comunica il nome utente e la password alla persona; in seguito potrà cambiare la password da sola (vedi [Account e impostazioni](../account.md#changing-the-password)).

!!!warning Imposta sempre una password
Marginalia accetta un account con password vuota: chiunque può quindi accedere usando solo il nome utente. Fallo solo nell'app desktop sul tuo computer. Creando un utente senza password, il programma chiede prima una conferma.
!!!

Il nome utente deve essere unico.

<a id="editing-a-user"></a>

## Modificare un utente

Il pulsante con la matita apre la stessa finestra per un utente esistente:

- modifica il nome utente, il nome completo e il flag di amministratore;
- lascia vuoti entrambi i campi password per mantenere la password attuale dell'utente;
- per impostare una **nuova password**, inseriscila in entrambi i campi password;
- per **cancellare la password**, seleziona **Cancella la password** (i campi password vengono disattivati) e conferma. L'account non avrà quindi alcuna password.

- per fare in modo che i browser dell'utente richiedano di nuovo l'accesso, seleziona **Cancella gli accessi salvati (ricordami)**; l'opzione compare solo mentre l'utente ha accessi salvati. Una sessione già aperta resta aperta, ma i cookie "ricordami" dell'utente smettono di funzionare.

Per tutte queste operazioni non serve la password attuale dell'utente. Una nuova password o una password cancellata termina tutti gli accessi salvati dell'utente.

<a id="resetting-a-forgotten-password"></a>

### Reimpostare una password dimenticata

Marginalia non prevede il ripristino della password via e-mail. Puoi:

- impostare qui una nuova password e comunicarla all'utente, che potrà poi cambiarla con **Cambia password**; oppure
- selezionare **Cancella la password**, dire all'utente di accedere con il proprio nome utente e una password **vuota**, e di impostare subito una nuova password con **Cambia password** (lascia vuoto *Password attuale*). In questo modo l'amministratore non conosce mai la nuova password.

!!!warning
Finché l'utente non imposta una nuova password, chiunque conosca il nome utente può accedere a un account senza password. Su un server, cancella la password solo quando l'utente può accedere subito.
!!!

L'ultimo amministratore non può perdere il flag di amministratore: *L'ultimo amministratore non può essere eliminato né retrocesso.*

<a id="users-and-providers"></a>

## Utenti e provider

Per aggiungere un [provider di inferenza](../inference-providers.md) serve il permesso **Crea modelli propri**; gli amministratori lo hanno sempre. Un utente senza questo permesso può usare solo i provider che gli amministratori [condividono](../inference-providers.md#shared-providers). I provider già posseduti da un utente continuano a funzionare e possono ancora essere modificati. Vedi [Permessi](permissions.md).

<a id="permissions"></a>

## Permessi

Il pulsante con la chiave, nella riga di un utente, apre i suoi [permessi](permissions.md): ciò che l'utente può fare oltre al normale, come aggiungere i propri modelli o usare l'assistente di aiuto. Un nuovo utente non ne ha nessuno: dopo averlo aggiunto, assegnagli ciò di cui ha bisogno.

<a id="deleting-a-user"></a>

## Eliminare un utente

Il pulsante con il cestino elimina l'account dopo una conferma. Non puoi eliminare il tuo account né l'ultimo amministratore.

L'utente non può più accedere, gli accessi salvati sui suoi dispositivi smettono di funzionare e il nome utente torna disponibile per un nuovo account.

I suoi libri, lorebook, provider di inferenza (con le relative chiavi API), protocolli, tag e impostazioni scompaiono insieme all'account: nessuno può più vederli, e la successiva [Pulizia](cleanup.md) li rimuove dal database insieme all'account.

I file dell'utente (immagini e risorse caricate) vengono conservati: la sua cartella nella directory dei dati viene rinominata in `<login>-deleted` (`<login>-deleted-2`... se il nome è già occupato), così un nuovo account con lo stesso nome utente parte con una cartella vuota. Elimina a mano la cartella rinominata quando non ti serve più.
