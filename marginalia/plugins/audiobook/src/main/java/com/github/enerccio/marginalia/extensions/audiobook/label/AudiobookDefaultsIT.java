package com.github.enerccio.marginalia.extensions.audiobook.label;

/**
 * The built-in prompts of the Audiobook in Italian; the JSON fields and their values are the English ones.
 */
public class AudiobookDefaultsIT extends AudiobookDefaults {

    @Override
    public String labelTemplate() {
        return """
            Etichetti i frammenti di una storia per un audiolibro a più voci. I frammenti sono numerati; N è narrazione, D è discorso diretto. {{#if characters}}I personaggi noti finora sono: {{characters}}. Possono parlare anche altri; chiamali come li chiama il testo.{{else}}Non sono noti: chiama chi parla come lo chiama il testo.{{/if}}
            Per ogni frammento rispondi con:
            - "id": il numero del frammento;
            - "speaker": per D il personaggio che lo dice, ricavato dal contesto (un nome nella narrazione intorno, un vocativo, chi risponde a chi). Per N è "Narrator", tranne quando il contesto mostra che il testo sono parole proprie di un personaggio: una lettera, un biglietto, un messaggio, un cartello o un'iscrizione che un personaggio ha scritto e che la storia cita, oppure i pensieri di un personaggio. In quel caso indica quel personaggio (chi scrive, una firma, chi pensa), anche per più paragrafi di fila. Il corsivo (segni come *...* o _..._) è solo un indizio: la stessa formattazione si usa anche per l'enfasi, i suoni e le parole straniere, che legge il narratore. Nel dubbio è "Narrator";
            - "delivery": come va letto ({{deliveries}}) e "intensity" da 0.0 a 1.0;
            - "tagged_text": per N una stringa vuota. Per D il frammento parola per parola, invariato, salvo che puoi inserire un tag sonoro dove il contesto richiede chiaramente quel suono dal personaggio che parla, e di rado (al massimo un tag ogni {{wordsPerTag}} parole dell'intero testo). Tag ammessi: {{tags}}, scritti esattamente così. Non cambiare, aggiungere o togliere nessun'altra parola. Per ogni frammento senza tag usa una stringa vuota: non copiare mai il testo di un frammento che non ha tag.
            I frammenti segnati "context" sono già stati etichettati: leggili per capire chi parla, non etichettarli.
            Rispondi solo con il JSON, una voce per frammento, in ordine.""";
    }
}
