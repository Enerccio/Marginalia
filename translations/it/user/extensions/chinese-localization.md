---
label: Localizzazione in cinese
order: 18
---

# Localizzazione in cinese

Aggiunge il cinese semplificato (中文（简体）) alle lingue dell'interfaccia utente.

<a id="using-it"></a>

## Come si usa

Dopo che un amministratore ha caricato l'estensione ([Gestione delle estensioni](../administration/extensions.md)), *中文（简体）*
si trova nel selettore **Lingua** in fondo all'area di lavoro (nel menu su un [telefono](../phone.md)), accanto
a inglese e giapponese. Selezionalo e la pagina si ricarica in cinese; la scelta viene salvata nel tuo account, quindi
viene usata su ogni dispositivo su cui accedi. Ogni utente sceglie la propria lingua.

![L'editor della storia in cinese](../../images/plugin-chineselocalization.png)

L'interfaccia è tradotta: menu, pulsanti, messaggi, le descrizioni delle variabili dei modelli e delle macro. Un testo
che non ha ancora una traduzione in cinese viene mostrato in inglese.

L'estensione aggiunge il cinese anche alla **Lingua dei prompt** delle impostazioni (*Impostazioni → Modelli*, vedi
[Lingua dei prompt](../account.md#prompt-language)): i prompt predefiniti dell'applicazione e dei plugin vengono quindi
inviati in cinese. Si tratta di una scelta a sé, indipendente dalla lingua dell'interfaccia. I tuoi libri, i lorebook e
i prompt che hai scritto tu non vengono modificati.

<a id="removing-it"></a>

## Come rimuoverla

Scaricare l'estensione elimina la lingua, sia dell'interfaccia sia dei prompt. Gli utenti che l'avevano scelta vedono
e inviano in inglese finché non selezionano un'altra lingua; la loro scelta viene conservata e torna ad applicarsi
quando l'estensione viene caricata di nuovo.

<a id="for-developers"></a>

## Per gli sviluppatori

L'estensione è l'esempio per aggiungere una lingua: vedi
[Plugin di esempio → Localizzazione in cinese](../../developer/plugins/example.md#walkthrough-chinese-localization).
