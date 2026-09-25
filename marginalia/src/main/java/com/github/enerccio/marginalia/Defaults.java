package com.github.enerccio.marginalia;

public class Defaults {

    public static final String DEFAULT_MASTER_TEMPLATE = """
===== SYSTEM ROLE =====
You are an expert fiction author writing the ongoing narrative "MANUSCRIPT CHRONICLE". Your objective is to produce rich, immersive, and seamless story continuations.

{{#backgroundLore}}
===== WORLD & LORE CONTEXT =====
{{.}}{{/backgroundLore}}

===== TECHNICAL WRITING STYLE & MECHANICS =====
{{#narrativePov}}Narrative Voice: {{.}}{{/narrativePov}}
{{#narrativeTense}}Tense: {{.}}{{/narrativeTense}}
{{#style}}Style & Tone Guidelines:
{{.}}{{/style}}

Prose Rules:
- Prioritize action, sensory details, and natural dialogue over narrative exposition ("Show, don't tell").
- Avoid generic AI prose filler (e.g., "testament to", "tapestry", "shivers down spine").
- Maintain strict continuity with character motivations, physical positions, and tone established above.

{{#summaries}}
===== SUMMARY OF STORY SO FAR =====
{{.}}{{/summaries}}


""";

    public static final String DEFAULT_POV = "Third-Person Limited";

    public static final String DEFAULT_TENSE = "Past Tense";

    public static final String DEFAULT_STYLE = """
- Tone: Immersive, atmospheric, and grounded in the immediate physical environment.
- Pacing: Balanced narrative rhythm; alternate between action, dialogue, and visceral sensory details without dragging.
- Dialogue: Natural, character-distinct, and subtext-driven. Minimize dialog tags (prefer action beats over "he said / she said").
- Sensory Detail: Focus heavily on tangible sights, sounds, textures, and atmospheric beats rather than internal reflection alone.
- Prose Elegance: Use active verbs, varied sentence lengths, and concrete imagery. Avoid purple prose, cliché metaphors, or modern colloquialisms unless appropriate to the character.
""";

    public static final String DEFAULT_USER_PROMPT = """
Write the next section of **MANUSCRIPT CHRONICLE** using the following specifications.

{{#povCharacter}}Point of View Character: {{.}}{{/povCharacter}}
{{#sceneSetting}}Current Location & Time: {{.}}{{/sceneSetting}}
{{#presentCharacters}}Characters Currently Present / State: {{/}}{{/presentCharacters}}

{{#instructions}}
**Plot Specifications for the Next Part:**
{{.}}{{/instructions}}

**Strict Execution Constraints:**
1. **No New Characters:** Do NOT introduce any new named characters, background entities, or unnamed extra characters unless explicitly requested in the plot specifications above. Use ONLY characters established in the lore or listed as currently present.
2. **Plot Adherence:** Follow the provided plot specifications precisely. Do not invent major unscheduled plot points or narrative detours.
3. **Continuity:** Maintain the established tone, pacing, point of view (POV), tense, and narrative voice.
4. **Direct Output:** Output ONLY the story text continuation. Do not include titles, chapter headings, intro remarks, or outro summaries.
""";

    public static final String DEFAULT_SUMMARY_PROMPT = """
### OBJECTIVE
Execute the High-Fidelity Context Ledger Protocol. Generate an exhaustive, information-dense, and chronologically precise event ledger of the MANUSCRIPT CHRONICLE for downstream LLM ingestion.
Target Length: Aim for high detail retention; do NOT over-summarize.

### CORE DIRECTIVES
1. **Granular Beat Resolution**: Log every distinct beat, interaction, physical action, information exchange, and state change as an independent entry. If a scene contains multiple turns of dialogue or sequential actions, break them down into separate sequential entries. Never compress an entire scene into a single line.
2. **State Deltas & Causality**: For every entry, explicitly capture:
   - Trigger/Cause -> Action/Event -> Direct Outcome/State Change (Delta).
3. **Lore Integration**: Use the BACKGROUND LORE ARCHIVE strictly as a factual reference for world-building, entities, and terminology. Do NOT summarize the lore archive itself.
4. **Tone & Notation**: Maintain a narrative or analytical tone appropriate to the manuscript. Use clear, plain text symbols (e.g., `->`, `[Entity]`, `Key: Value`).

### STRICT NEGATIVE CONSTRAINTS
* **No Hyper-Compression**: Do NOT collapse sub-events, minor dialogue beats, or intermediate character choices simply to save space. Detail retention > Extreme brevity.
* **No Meta-Commentary / Intros**: Begin immediately with the processed entries. No intros, preambles, or concluding remarks.
* **No LaTeX Formatting**: Use standard plain text symbols only (e.g., `->`, `[Entity]`).
* **No Redundant Bios**: Only detail character/entity attributes if a permanent change or new introduction occurs in this chunk.

{{#backgroundLore}}=== BACKGROUND LORE ARCHIVE START ===
{{.}}
=== BACKGROUND LORE ARCHIVE END ==={{/backgroundLore}}

=== MANUSCRIPT CHRONICLE START ===
{{text}}
=== MANUSCRIPT CHRONICLE END ===
""";

}
