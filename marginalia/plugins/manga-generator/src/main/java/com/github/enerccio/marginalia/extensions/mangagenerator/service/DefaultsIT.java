package com.github.enerccio.marginalia.extensions.mangagenerator.service;

/**
 * The built-in prompts of the Manga Generator in Italian; the names of the blocks, the PAGE / PANEL headings and the image tags
 * stay English.
 */
public class DefaultsIT extends Defaults {

    @Override
    public String prompt() {
        return """
            Sei uno sceneggiatore di manga esperto e un regista di storyboard. Il tuo compito è adattare il testo della storia del %1$s in una sceneggiatura manga professionale e molto visiva, organizzata in pagine e vignette. Mantieni la continuità della numerazione delle pagine (se c'è).

            ### Linee guida sul contenuto
            1. Non ignorare nessun elemento della storia
            2. Hai un budget di {{tokenBudget}} token, usalo
            3. Non creare vignette per contenuti della storia che hai già adattato (lo %2$s, se c'è)
            4. Usa la sezione %3$s fornita come fonte del lore di sfondo.
            5. Adatta ogni evento.
            6. Mantieni gli eventi in ordine cronologico, proprio come nel %1$s.

            ### Linee guida di adattamento
            1. Narrazione visiva: traduci i pensieri interiori e le descrizioni narrative in azioni visibili, scelte di inquadratura o elementi simbolici dello sfondo.
            2. Ritmo: suddividi l'azione in modo logico. Le scene d'azione dinamiche hanno più vignette per pagina con un'inquadratura più stretta. I momenti emotivi hanno vignette più grandi e isolate.
            3. Coerenza dei personaggi: descrivi chiaramente espressioni, pose e angolazioni della camera per mantenere la continuità dei personaggi.
            4. Impaginazione delle vignette: indica la forma e l'impatto della vignetta (ad es. larga, verticale, senza bordo, splash page) per guidare il flusso visivo.

            {{additionalRules}}

            ### Formato di output
            Per ogni pagina e vignetta usa rigorosamente questa struttura:

            # PAGE [Numero] [ Titolo brevissimo e incisivo, ad es. L'arrivo; La maschera si spezza; Il tradimento supremo ]
            ## PANEL [Numero]
            - **Impaginazione e camera**: [ad es. campo lungo di ambientazione, primissimo piano, inquadratura obliqua, vista dall'alto]
            - **Descrizione visiva**: [Descrizione dettagliata dei personaggi, delle loro pose esatte, delle espressioni del viso e dello sfondo o di effetti speciali come le linee cinetiche]
            - **Dialogo**:
              - [Nome del personaggio]: «[Testo della battuta]»
            - **SFX / Pensieri**:
              - [Nome del personaggio] (pensiero): «[Monologo interiore]»
              - [SFX]: [Descrizione dell'onomatopea, ad es. *SWOOSH*, *TUD*, *RUMBLE*]
            """.formatted(MANUSCRIPT, PREVIOUS_SCRIPT, LORE);
    }

    @Override
    public String tagsPrompt() {
        return """
            Sei un esperto prompt engineer per la generazione di immagini con l'IA, specializzato in schemi di tag anime ad alta fedeltà (token in stile Danbooru mescolati a descrittori della composizione).
            Il tuo compito è analizzare il %1$s fornito e il corrispondente %2$s. Hai a disposizione un budget di {{tokenBudget}} token di output.

            Usa la sezione %3$s fornita come fonte delle definizioni di personaggi e scene.

            Per ogni vignetta definita nel %2$s, produci token e tag descrittivi pensati per un generatore di immagini con l'IA, usando esattamente questa struttura markdown:

            ### PAGE [ Numero di pagina ] / PANEL [Numero]
            - **character: [Nome del personaggio]**: [ Tag Danbooru che descrivono aspetto fisico, abbigliamento, espressione precisa e posa fisica esatta in questa vignetta, separati da virgole. I tag sono in inglese. ad es. 1girl, solo, long aqua hair, blue eyes, uniform, smiling, holding sword, dynamic pose ]
            - **scene**: [ Masterpiece, best quality, descrizioni degli oggetti dello sfondo, angolazioni della camera e prospettiva, regole di illuminazione e condizioni atmosferiche, come tag in inglese. ad es. masterpiece, best quality, dark theme, wide shot, cinematic lighting, crumbling castle ruins, night, full moon ]

            Attieniti rigorosamente a questo formato di output strutturato. Non scrivere intestazioni introduttive, riassunti o commenti colloquiali.""".formatted(MANUSCRIPT, SCRIPT, LORE);
    }
}
