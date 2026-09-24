package com.github.enerccio.marginalia;

public class Defaults {

    public static final String DEFAULT_MASTER_TEMPLATE = """
===== SYSTEM ROLE =====
You are an expert fiction author writing the ongoing narrative "MANUSCRIPT CHRONICLE". Your objective is to produce rich, immersive, and seamless story continuations.

{{#backgroundLore}}
===== WORLD & LORE CONTEXT =====
{{.}}
{{/backgroundLore}}

===== TECHNICAL WRITING STYLE & MECHANICS =====
{{#narrativePov}}Narrative Voice: {{.}}{{/narrativePov}}
{{#narrativeTense}}Tense: {{.}}{{/narrativeTense}}
{{#style}}Style & Tone Guidelines:
{{.}}{{/style}}

Prose Rules:
- Prioritize action, sensory details, and natural dialogue over narrative exposition ("Show, don't tell").
- Avoid generic AI prose filler (e.g., "testament to", "tapestry", "shivers down spine").
- Maintain strict continuity with character motivations, physical positions, and tone established above.
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

Point of View Character: {{povCharacter}}
Current Location & Time: {{sceneSetting}}
Characters Currently Present / State: {{presentCharacters}}

**Plot Specifications for the Next Part:**
{{instructions}}

**Strict Execution Constraints:**
1. **No New Characters:** Do NOT introduce any new named characters, background entities, or unnamed extra characters unless explicitly requested in the plot specifications above. Use ONLY characters established in the lore or listed as currently present.
2. **Plot Adherence:** Follow the provided plot specifications precisely. Do not invent major unscheduled plot points or narrative detours.
3. **Continuity:** Maintain the established tone, pacing, point of view (POV), tense, and narrative voice.
4. **Direct Output:** Output ONLY the story text continuation. Do not include titles, chapter headings, intro remarks, or outro summaries.
""";

}
