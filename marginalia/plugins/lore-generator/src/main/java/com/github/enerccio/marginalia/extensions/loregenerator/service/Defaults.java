package com.github.enerccio.marginalia.extensions.loregenerator.service;

public class Defaults {

    public static String COMPACTION_PROMPT = """
[System Instruction: You are an expert data-compaction engine. Your sole task is to ingest the raw text provided below and convert it into a machine-readable, ultra-dense lore archive entry for an LLM's background memory.]

CRITICAL RULES:

1. Strip out 100% of atmospheric filler, flowery prose, narrative setup, in-universe formatting (like report headers or sign-offs), and rhetorical flavor.
2. Extract only the hard facts: rules, spatial mechanics, demographics, physical traits, identifiers, motivations, and functional boundaries.
   - NARRATIVE EXCEPTION: If the source text contains an anecdote, backstory event, or historical narrative, DO NOT delete it. Instead, compress it into a "[Historical Precedent]" or "[Psychological Core]" block. Strip the storytelling prose, but preserve the exact causal relationship dynamic, trauma, grudge, or behavioral trigger created by that event. Include necessary details in the historical precedences and keep ALL histortical precedences.
3. Structure the output entirely using clear, bolded markdown attributes and clean, minimalist bullet points.
4. Maintain absolute precision with spatial or physical descriptions (e.g., specific sides of a face, precise geometric layouts, exact numbers).
5. Output ONLY the raw markdown block. Do not include any intro, outro, or conversational confirmation. Start immediately with the first attribute block.
6. IMPORTANT: Output it into copyable tripple quoted markdown (code block)!
7. Do NOT summarize information that is already existing lore.

==== START RAW SOURCE MATERIAL ====
{{compactionData}}
==== END RAW SOURCE MATERIAL ====
""";

    public static String GENERATION_PROMPT = """
[ You are an expert worldbuilder and a master of highly descriptive, atmospheric prose. Your task is to craft a detailed lore entry based strictly on the parameters provided below. Use vivid imagery and rich language while ensuring every core detail is seamlessly embedded. ]

### Input Parameters
- Subject: [Insert Subject Name]
- Core Details to Weave In: [Insert specific details, facts, or mechanics]
- Target Length/Scope: [e.g., Deep Dive (800+ words), Medium Vignette (400 words), or Micro-Lore (150 words)]
- Required Structural Breakdown: [Tell it exactly what sections to write, or write "Dealers Choice: Generate at least X distinct thematic sections"]

### Writing Guidelines
1. Tone & Style: Rich, evocative, and atmospheric. Use elegant metaphors and immersive storytelling appropriate for the setting (Fantasy/Sci-Fi/Modern).
2. Pacing & Depth: Expand heavily on the lore. Do not just state a fact; dedicate time to the history, the cultural impact, the sensory experience, and the rumors surrounding it. Match the requested Target Length.
3. Structure: You must follow the Required Structural Breakdown provided above. Use descriptive markdown headers for each section.
4. Fact Integration: Seamlessly drape the core details in luxurious prose. Ensure they remain clear enough beneath the imagery to be parsed by an LLM later.
5. Name selection: If you need to generate a name, pick less common names, pick names that won't cause contextual drift with already generated lore entries.

### Output
Provide only the stylized lore entry. Do not include any introductory or concluding conversational filler.
""";

}
